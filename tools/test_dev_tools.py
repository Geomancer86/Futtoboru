"""T-01 (D27): tests of tools/test_level.py, tools/test_tiers.py and tools/suite_lock.py (python -m pytest tools)."""
import json
import os
import sys

import pytest

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import suite_lock  # noqa: E402
import test_level  # noqa: E402
import test_tiers  # noqa: E402


def test_levels_of_files():
    assert test_level.level_of("docs/HANDOFF.md") == "docs"
    assert test_level.level_of("README.md") == "docs"
    assert test_level.level_of("Jenkinsfile") == "docs"
    assert test_level.level_of("tools/test_level.py") == "docs"
    assert test_level.level_of("tools/data/validate.py") == "data"
    assert test_level.level_of("futtoboru-core/src/main/resources/gamedata/core/pack.json") == "data"
    assert test_level.level_of("futtoboru-core/src/main/java/com/rndmodgames/futtoboru/screens/MenuScreen.java") \
        == "presentation"
    assert test_level.level_of("futtoboru-core/src/main/resources/mods/language/BaseBundle.properties") == "presentation"
    assert test_level.level_of("futtoboru-core/src/test/java/com/rndmodgames/futtoboru/BasicTests.java") == "presentation"
    assert test_level.level_of("futtoboru-desktop/src/main/java/com/rndmodgames/desktop/DesktopLauncher.java") \
        == "presentation"
    assert test_level.level_of("futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/X.java") == "simulation"
    assert test_level.level_of("pom.xml") == "simulation"
    assert test_level.level_of("mvnw") == "simulation"


def test_slow_tags_from_the_source():
    source = '\n'.join([
        "@Tag(\"slow\")",
        "class A {",
        "}",
    ])
    assert test_tiers.slow_parts(source) == (True, set())
    source = '\n'.join([
        "/** doc */",
        "class B {",
        "    @Test",
        "    @Tag(\"slow\")",
        "    void heavy() {",
        "    }",
        "    @Test",
        "    void light() {",
        "    }",
        "}",
    ])
    assert test_tiers.slow_parts(source) == (False, {"heavy"})


def test_the_lock_is_exclusive_and_stale_locks_are_taken_over(tmp_path, monkeypatch):
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    owner = suite_lock.acquire("suite", "first", wait_min=0, expire_min=20)
    assert owner is not None
    assert suite_lock.acquire("suite", "second", wait_min=0, expire_min=20) is None  # held, no waiting
    # a holder whose process is gone loses the lock
    path = suite_lock.lock_path("suite")
    held = suite_lock.read_owner(path)
    held["pid"] = 999999999
    suite_lock.write_owner(path, held)
    assert suite_lock.acquire("suite", "third", wait_min=0, expire_min=20) is not None


def test_run_locked_returns_the_exit_code_and_logs_history(tmp_path, monkeypatch):
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    code = suite_lock.run_locked([sys.executable, "-c", "import sys; sys.exit(3)"], cwd=str(tmp_path))
    assert code == 3
    assert not os.path.exists(suite_lock.lock_path("suite")), "released"
    with open(os.path.join(str(tmp_path), suite_lock.HISTORY), encoding="utf-8") as handle:
        assert json.loads(handle.readline())["exit"] == 3


def test_the_command_knows_it_holds_the_lock(tmp_path, monkeypatch):
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    monkeypatch.delenv(suite_lock.HELD_ENV, raising=False)
    probe = "import os, sys; sys.exit(0 if 'suite' in os.environ.get('%s', '').split(',') else 4)" % suite_lock.HELD_ENV
    assert suite_lock.run_locked([sys.executable, "-c", probe], cwd=str(tmp_path)) == 0


def test_a_nested_run_of_the_same_lock_runs_directly(tmp_path, monkeypatch):
    # `suite_lock.py run -- python tools/test_level.py --run` deadlocked: test_level waited for the lock its parent held
    monkeypatch.setattr(suite_lock, "LOCK_DIR", str(tmp_path))
    assert suite_lock.acquire("suite", "the outer run", wait_min=0, expire_min=20) is not None
    monkeypatch.setenv(suite_lock.HELD_ENV, "suite")
    assert suite_lock.run_locked([sys.executable, "-c", "import sys; sys.exit(5)"], wait_min=0, cwd=str(tmp_path)) == 5
    monkeypatch.delenv(suite_lock.HELD_ENV)
    assert suite_lock.run_locked([sys.executable, "-c", "pass"], wait_min=0, cwd=str(tmp_path)) == suite_lock.NOT_TAKEN


def test_ci_remote_reads_failing_tests_from_the_reports(tmp_path, monkeypatch):
    import ci_remote
    reports = tmp_path / "core" / "target" / "surefire-reports"
    reports.mkdir(parents=True)
    (reports / "TEST-a.xml").write_text(
        '<testsuite><testcase classname="com.x.SeasonTests" name="plays"/>'
        '<testcase classname="com.x.ArtTests" name="loads"><failure type="org.opentest4j.AssertionFailedError" '
        'message="expected: 3 but was: 2">trace</failure></testcase>'
        '<testcase classname="com.x.ArtTests" name="reads"><error type="java.io.FileNotFoundException">'
        'art/match/Index.json\nat ...</error></testcase></testsuite>', encoding="utf-8")
    monkeypatch.setattr(ci_remote, "ROOT", str(tmp_path))
    assert ci_remote.failures() == [("ArtTests.loads", "AssertionFailedError: expected: 3 but was: 2"),
                                    ("ArtTests.reads", "FileNotFoundException: art/match/Index.json")]


def test_ci_remote_request_branches_name_the_tier():
    import datetime as dt
    import ci_remote
    when = dt.datetime(2026, 11, 28, 3, 4, 5, tzinfo=dt.timezone.utc)
    assert ci_remote.request_branch("full", "b3", when) == "ci/full/b3-20261128-030405"


@pytest.mark.skipif(os.name != "nt", reason="the mvnw -> mvnw.cmd mapping is Windows only")
def test_dot_slash_mvnw_runs_the_cmd_wrapper_on_windows(tmp_path):
    for name in ("mvnw", "mvnw.cmd"):
        (tmp_path / name).write_text("")
    argv = suite_lock.runnable(["./mvnw", "-B", "verify"], cwd=str(tmp_path))
    assert argv[0].lower().endswith("mvnw.cmd") and os.path.isabs(argv[0]) and argv[1:] == ["-B", "verify"]
