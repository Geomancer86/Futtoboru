#!/usr/bin/env python3
"""
T-01 (D27): which tests a change needs, from the files it touches (ported from DarkBlade tools/test_level.py).

    python tools/test_level.py                 the level and the commands for what changed since the last green run
    python tools/test_level.py --run           runs them (Maven runs of the whole tier through tools/suite_lock.py)
    python tools/test_level.py --base <rev>    against a revision instead (origin/develop, ORIG_HEAD after a merge)
    python tools/test_level.py --full          the full tier (-Pfull) instead of the fast one at level simulation
    python tools/test_level.py --offline       Maven offline (-o)
    python tools/test_level.py --json          the decision as JSON (CI and agents)
    python tools/test_level.py --affected      while coding (tools/test-changed): only the tests touched by the
                                               uncommitted change (--base develop: by the branch), no lock, no record

What changed: the working tree (committed, staged, unstaged and untracked files) against the tree of the last green
--run of this machine's worktree (target/.last-green-<tier>, never committed), or against the merge base with
origin/develop when there is none. So after `git merge origin/develop` brought only docs or HANDOFF lines, nothing
runs again; when it brought code, only the level that code needs runs.

Levels, lightest to heaviest; a change takes the heaviest level of its files:
  docs          docs/, *.md, .claude/, CI files, tools/ scripts: no Maven (pytest of the changed tool folder only)
  data          gamedata/, tools/data/: validate.py, pytest tools/data, the guard tests and the tests that load data
  presentation  screens, ui, menus, tables, dialogs, the viewer, skins/art/language files, the desktop launcher, test
                sources: the guard tests plus the tests that name a changed class (as tools/test-changed)
  simulation    everything else in core main code (engine, system, data, match, game), POMs, the Maven wrapper: the
                whole fast tier (./mvnw test, untagged tests), once, under the suite lock; -Pfull with --full
After a Maven run, python tools/test_tiers.py fails when an untagged class took more than 5 s.
The guard tests always run at data and presentation (they are fast and catch what a narrow selection misses).
"""
import argparse
import json
import os
import re
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LEVELS = ["docs", "data", "presentation", "simulation"]
CORE_MAIN = "futtoboru-core/src/main/java/com/rndmodgames/"
GAMEDATA = "futtoboru-core/src/main/resources/gamedata/"
TEST_ROOTS = ("futtoboru-core/src/test/", "futtoboru-desktop/src/test/")
PRESENTATION = ("components/", "localization/", "PreferencesManager.java", "futtoboru/screens/", "futtoboru/ui/",
                "futtoboru/menu/", "futtoboru/tables/", "futtoboru/dialogs/", "futtoboru/fields/",
                "futtoboru/match/ecs/view/")
GUARD_TESTS = ["LoggingStyleGuardTest", "ReferenceDataGuardTest", "EcsDeterminismGuardTest", "LanguageBundleKeysTest",
               "UiTooltipsKeysTest"]
DATA_TESTS = ["GameDataTests", "RuntimeIdsTests", "DatabaseLoaderTests", "*LoaderTests", "SeasonLeaguesAndTeamsTests",
              "PositionsAndDribblingTests", "HistoryProviderTests", "ReferenceResultsTests"]
# more selected tests than this and the selection is not worth it: the whole fast tier
MAX_SELECTED = 40
GREEN = os.path.join(ROOT, "target", ".last-green-%s")


def git(*args, env=None):
    out = subprocess.run(["git", *args], cwd=ROOT, capture_output=True, text=True, encoding="utf-8", env=env)
    return out.stdout.strip() if out.returncode == 0 else ""


def working_tree():
    """The working tree (tracked + untracked, .gitignore respected) as a git tree object, the index untouched"""
    fd, index = tempfile.mkstemp(prefix="futtoboru-index-")
    os.close(fd)
    env = dict(os.environ, GIT_INDEX_FILE=index)
    try:
        git("read-tree", "HEAD", env=env)
        git("add", "-A", env=env)
        return git("write-tree", env=env)
    finally:
        os.remove(index)


def last_green(tier):
    """(tree, file) of the last green run of this tier (a full run also counts for fast), or (None, None)"""
    for t in ([tier, "full"] if tier == "fast" else [tier]):
        path = GREEN % t
        try:
            with open(path, encoding="utf-8") as handle:
                tree = handle.read().strip()
        except OSError:
            continue
        if tree and git("cat-file", "-t", tree) == "tree":
            return tree, path
    return None, None


def level_of(path):
    if path.startswith(GAMEDATA) or path.startswith("tools/data/"):
        return "data"
    if path.startswith(("docs/", ".claude/", ".github/", "tools/")) or path.endswith(".md") \
            or path in ("Jenkinsfile", ".gitignore", ".gitattributes", "LICENSE", "THIRD_PARTY_NOTICES.txt"):
        return "docs"
    if path.startswith(TEST_ROOTS) or path.startswith("futtoboru-desktop/src/main/"):
        return "presentation"
    if path.startswith("futtoboru-core/src/main/resources/"):
        return "presentation"  # skins, art, language files, ui theme
    if path.startswith(CORE_MAIN):
        return "presentation" if path[len(CORE_MAIN):].startswith(PRESENTATION) else "simulation"
    return "simulation"  # POMs, mvnw, .mvn, scripts, unknown: be safe


def class_name(path):
    return os.path.splitext(os.path.basename(path))[0]


def is_test_file(name):
    return name.endswith(("Test.java", "Tests.java"))


def affected_tests(files):
    """The changed tests and the tests whose source names a changed main class"""
    names = {class_name(f) for f in files if f.endswith(".java") and not f.startswith(TEST_ROOTS)}
    picked = {class_name(f) for f in files if f.startswith(TEST_ROOTS) and is_test_file(f)}
    if names:
        pattern = re.compile(r"\b(" + "|".join(sorted(map(re.escape, names))) + r")\b")
        for test_root in TEST_ROOTS:
            for folder, _, entries in os.walk(os.path.join(ROOT, test_root)):
                for entry in entries:
                    if is_test_file(entry):
                        with open(os.path.join(folder, entry), encoding="utf-8", errors="ignore") as handle:
                            if pattern.search(handle.read()):
                                picked.add(entry[:-5])
    return sorted(picked)


# D35: tests run while coding and in the data/presentation gate skip the slow tiers (Jenkins runs them)
FAST_ONLY = "-Dtest.excludedGroups=slow,season,smoke,soak"


def mvnw():
    return "mvnw.cmd" if os.name == "nt" else "./mvnw"


def decide(base=None, tier="fast", offline=False, use_green=True):
    tree = working_tree()
    if not tree:
        sys.exit("test_level: git failed here (is git on PATH? a WSL bash on Windows runs another git: use Git Bash)")
    source = None
    if base is None and use_green:
        green, path = last_green(tier)
        if green:
            base, source = green, "the last green %s run (%s)" % (tier, os.path.relpath(path, ROOT))
    if base is None:
        base = git("merge-base", "origin/develop", "HEAD") or "origin/develop"
        source = "the merge base with origin/develop (%s)" % base[:9]
    elif source is None:
        merge_base = git("merge-base", base, "HEAD")
        source = base
        base = merge_base if merge_base and git("cat-file", "-t", base) == "commit" else base
    files = sorted(f for f in git("diff", "--name-only", base, tree).splitlines() if f)
    level = "docs"
    why = {}
    for f in files:
        lv = level_of(f)
        why.setdefault(lv, []).append(f)
        if LEVELS.index(lv) > LEVELS.index(level):
            level = lv
    tests = []
    if level == "data":
        tests = GUARD_TESTS + DATA_TESTS
    elif level == "presentation":
        selected = affected_tests(files)
        if len(selected) > MAX_SELECTED:
            level = "simulation"
            why.setdefault("simulation", []).append("(%d tests selected: more than %d)" % (len(selected), MAX_SELECTED))
        else:
            tests = GUARD_TESTS + (DATA_TESTS if "data" in why else []) + [t for t in selected if t not in GUARD_TESTS]
    if tier == "full" and level != "docs":
        level = "simulation"  # --full: the whole full tier for any code or data change
    desktop = any(f.startswith("futtoboru-desktop/") for f in files)
    core_only = all(f.startswith("futtoboru-core/") or level_of(f) in ("docs", "data") for f in files)

    maven = []
    base_args = ["-B", "-ntp"] + (["-o"] if offline else [])
    if level == "simulation":
        maven = base_args + ["test"] + (["-Pfull"] if tier == "full" else [])
        if core_only:
            maven += ["-pl", "futtoboru-core"]
    elif level in ("data", "presentation"):
        maven = base_args + ["test", "-Dtest=" + ",".join(tests), "-Dsurefire.failIfNoSpecifiedTests=false"]
        if tier != "full":
            maven += [FAST_ONLY]  # D35: a named slow class stays out of the fast gate
        maven += ["-pl", "futtoboru-desktop", "-am"] if desktop else ["-pl", "futtoboru-core"]
    pytest = []
    if any(f.startswith("tools/data/") or f.startswith(GAMEDATA) for f in files):
        pytest.append("tools/data")
    if any(f.startswith("tools/art/") for f in files):
        pytest.append("tools/art")
    if any(re.match(r"tools/[^/]+\.py$", f) for f in files):
        pytest.append("tools/test_dev_tools.py")
    validate = any(f.startswith(GAMEDATA) or f.startswith("tools/data/") for f in files)
    return {"base": source, "tree": tree, "tier": tier, "level": level, "files": len(files),
            "why": {k: v[:8] for k, v in why.items()}, "tests": tests, "maven": maven,
            "locked": level == "simulation", "pytest": pytest, "validate": validate}


def commands(decision):
    out = []
    if decision["validate"]:
        out.append(([sys.executable, "tools/data/validate.py"], False))
    if decision["pytest"]:
        out.append(([sys.executable, "-m", "pytest", "-q"] + decision["pytest"], False))
    if decision["maven"]:
        out.append(([mvnw()] + decision["maven"], decision["locked"]))
        out.append(([sys.executable, "tools/test_tiers.py"], False))
    return out


def show(command, locked):
    return ("python tools/suite_lock.py run -- " if locked else "") + " ".join(command).replace(sys.executable, "python")


def affected(base, offline):
    """tools/test-changed: the changed tests and the tests naming a changed class, against base (HEAD: uncommitted)"""
    files = set(git("diff", "--name-only", base).splitlines())
    files |= set(git("ls-files", "--others", "--exclude-standard", "--", "futtoboru-core", "futtoboru-desktop").splitlines())
    tests = affected_tests(sorted(f for f in files if f))
    print("changed: %d files; tests: %d" % (len(files), len(tests)))
    if not tests:
        return 0
    for t in tests:
        print("  " + t)
    desktop = any(f.startswith("futtoboru-desktop/") for f in files)
    command = [mvnw(), "-B", "-ntp", "-q"] + (["-o"] if offline else []) + [
        "test", "-Dtest=" + ",".join(tests), "-Dsurefire.failIfNoSpecifiedTests=false", FAST_ONLY]
    command += ["-pl", "futtoboru-desktop", "-am"] if desktop else ["-pl", "futtoboru-core"]
    print("run: " + " ".join(command))
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import suite_lock
    code = suite_lock.run_locked(command, cwd=ROOT)  # every Maven run takes turns, small ones too (T-03)
    print("tests " + ("OK" if code == 0 else "FAILED"))
    return code


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--base")
    parser.add_argument("--run", action="store_true")
    parser.add_argument("--full", action="store_true")
    parser.add_argument("--offline", action="store_true")
    parser.add_argument("--no-green", action="store_true", help="ignore target/.last-green-* (CI)")
    parser.add_argument("--json", action="store_true")
    parser.add_argument("--affected", action="store_true")
    args = parser.parse_args(argv)
    if args.affected:
        return affected(args.base or "HEAD", args.offline)
    decision = decide(args.base, "full" if args.full else "fast", args.offline, not args.no_green)
    todo = commands(decision)
    if args.json:
        print(json.dumps(decision, indent=2))
    else:
        print("level: %s (%d files changed against %s)" % (decision["level"], decision["files"], decision["base"]))
        for lv in reversed(LEVELS):
            if lv in decision["why"]:
                print("  %s: %s" % (lv, ", ".join(decision["why"][lv])))
        if not todo:
            print("run: nothing (%s)" % ("nothing changed" if not decision["files"] else "docs/tools only"))
        for command, locked in todo:
            print("run: " + show(command, locked))
    if not args.run:
        return 0
    sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
    import suite_lock
    for command, locked in todo:
        print("test_level: " + show(command, locked), flush=True)
        if locked:
            code = suite_lock.run_locked(command)
        else:
            code = subprocess.call(suite_lock.runnable(command), cwd=ROOT,
                                   shell=os.name == "nt" and command[0].endswith(".cmd"))
        if code != 0:
            print("test_level: FAILED (exit %d): %s" % (code, show(command, locked)))
            return code
    os.makedirs(os.path.dirname(GREEN), exist_ok=True)
    with open(GREEN % decision["tier"], "w", encoding="utf-8") as handle:
        handle.write(decision["tree"] + "\n")
    print("test_level: green (%s, level %s): tree %s recorded in target/.last-green-%s"
          % (decision["tier"], decision["level"], decision["tree"][:9], decision["tier"]))
    return 0


if __name__ == "__main__":
    sys.exit(main())
