# Build, run and set up a machine

## Requirements
- JDK 17 or newer (Temurin 17 recommended; 21 also works).
- Maven: nothing to install. The Maven wrapper (`mvnw` / `mvnw.cmd`) downloads the pinned Maven 3.9.12
  into `~/.m2/wrapper/` on first use (`.mvn/wrapper/maven-wrapper.properties`). A local `mvn` 3.6+ also works.
- Git with Git Credential Manager (needed to push; cloning the public repo needs no login).

## Build and run
```
./mvnw -B -DskipTests package       # fast build (PowerShell/cmd: .\mvnw.cmd ...)
./mvnw -B verify                    # build + fast tier tests (see "Test tiers"; the gate is tools/test_level.py)
java -jar futtoboru-desktop/target/futtoboru-desktop-0.5.0-SNAPSHOT-jar-with-dependencies.jar
```
Scripts at the repo root, each in a Windows `.bat` and a POSIX `.sh` version (Linux, Git Bash, WSL):

| Script | What it does |
|---|---|
| `build` | `generate-build-info`, then `clean install -DskipTests` (`.sh` uses `./mvnw`, passes extra args to Maven) |
| `build-and-run` | `build`, checks the jar exists, then `run-game-debug` |
| `run-game` | runs the desktop jar |
| `run-game-debug` | runs the jar, output on screen and in `game-debug.log` |
| `run-game-debug-simple` | runs the jar, output only in `game-debug.log`, printed after exit |
| `generate-build-info` | writes `futtoboru-core/src/main/resources/build.properties` (git-ignored), build number +1 |

The `.sh` scripts take `JAR=<path>` to run another jar. Line endings are fixed by `.gitattributes`
(`*.sh`, `mvnw`: LF; `*.bat`, `*.cmd`: CRLF), and `mvnw`/`*.sh` are executable in git.
`build.bat` still calls a hard-coded `C:\apache-maven-3.6.3`; without it use `.\mvnw.cmd -B clean install -DskipTests`.
Save games go to `~/Documents/RndModGames/Futtuboru/savegames/`.
macOS is not supported yet (LWJGL3 needs `-XstartOnFirstThread`).

## Screenshot tour (V-01, dev only)
```
tools/ui/screenshots.sh [outDir=target/screenshots] [passes=1x,2x] [locale=en_US]   # cmd: tools\ui\screenshots.bat
```
Compiles core test sources and runs `ScreenshotTour` (`futtoboru-core/src/test/.../tools/`, not a JUnit test, never
run by `verify`) in a real window: seed 1888, a `NewGameFixture` game, every screen, drill-down and dialog, one PNG
each in `<outDir>/1x` (1280x720, UI scale 1x) and `<outDir>/2x` (2560x1440, 2x; needs a monitor that can hold that
window). About 2 minutes. Saves go to a temp folder; preferences and Documents are not touched. Exit code 1 and
`[tour] PROBLEM` lines when a stop failed. How to use the images: `docs/design/INTERFACE_GUIDE.md` section 8.

## New machine (BOOK3PRO or any Windows PC), PowerShell
```
java -version ; git --version
git clone https://github.com/Geomancer86/Futtoboru.git C:\git\futtoboru
cd C:\git\futtoboru
git switch develop
git config user.name "Nicolas Alvargonzalez"
git config user.email "<your email>"
git config core.autocrlf true
.\mvnw.cmd -B verify

# private companion repo (business drafts, full history of the old local repos), OUTSIDE the clone
git clone https://bitbucket.org/Geomancer/futtoboru-private.git C:\git\futtoboru-private
```
Then read `CLAUDE.md`, `PRIVACY.md` and `docs/HANDOFF.md`, claim an item, and branch: `git switch -c b3/<item> origin/develop`.

## Daily routine (both machines)
```
git fetch origin ; git switch develop ; git pull --ff-only
git switch -c b3/<item>                     # ws/<item> on WORKSTATION
# ... small commits ...
.\tools\test-changed.ps1                     # while coding: only the affected tests
git fetch origin ; git merge origin/develop ; python tools/test_level.py --run   # the level gate, once
git push -u origin HEAD
# green? merge your own branch (develop did not move since the gate: nothing to run again):
git switch develop ; git pull --ff-only ; git merge --no-ff b3/<item> ; python tools/test_level.py --run ; git push origin develop
```
Never force-push. If `git push origin develop` is rejected, merge origin/develop and run the gate again (it only
runs what the merge brought).

## Test tiers (T-01, D27: DarkBlade's cycle, from Gradle to Maven)
| Tier | Command | What |
|---|---|---|
| fast | `./mvnw -B verify` (default) | every test without a tag (~300 tests, ~15 s of tests) |
| slow | `-Pslow` | `@Tag("slow")` (and the old `season`): full seasons, save round trips, seeded databases |
| full | `-Pfull` | fast + slow |
| smoke | `-Pfull,smoke` | + `DesktopSmokeIT`: the real desktop jar opens, loads the data, shows the main menu, exits 0 (failsafe, after `package`; skipped without a display) |

- `-Dtest=SomeTests` runs that class whatever its tier. `-Pcoverage` adds JaCoCo (T-02; Jenkins only).
- **Rule:** a test class over 2 s gets `@Tag("slow")` (on the class, or on its slow methods); `python tools/test_tiers.py`
  (after any Maven run, from the surefire reports) fails when an untagged class took more than 5 s.
- **`tools/test-changed.ps1` / `.sh`** (while coding, no lock): the changed tests and the tests that name a changed class, `-Dtest`.
- **`python tools/test_level.py [--run] [--full] [--offline] [--base <rev>]`** (= `tools/dev/check.ps1` / `.sh`), the gate:
  levels from the files changed since the last green `--run` (`target/.last-green-<tier>`, the working tree as a git
  tree) or since the merge base with origin/develop: **docs** (docs, `*.md`, CI files, tools) nothing;
  **data** (`gamedata/`, `tools/data/`) `validate.py` + pytest + the guard and data-loading tests; **presentation**
  (screens, ui, tables, menus, dialogs, skins and language files, desktop launcher, test sources) the guard tests +
  the affected tests; **simulation** (engine, system, data model, match, POMs) the whole fast tier under the lock,
  one module when only core changed. Prints the decision; `--run` runs it and records the green tree.
- **`python tools/suite_lock.py run -- <command>`**: one heavy run per machine (lock folder `%TEMP%/futtoboru-locks`,
  `FUTTOBORU_LOCK_DIR`); waits inside, a dead or 20-minute-silent holder loses it; `status`, `release`.
- `SeasonMemo` (test code) plays the seeded season once per test JVM and hands copies (save codec) to AutosaveTests
  and SaveGameTests.
- No `clean` locally (a deleted test resource can survive in `target/` until you clean). One surefire fork: measured
  4 forks slower than 1 (each JVM starts libGDX and loads the game data again: 49 s vs 37 s).
- Differences from DarkBlade, and why: Maven profiles instead of Gradle `fastTest`/`slowTest` tasks; tag `slow`, not
  `lento` (the repo is in English); smoke is a failsafe IT against the jar, not an autotest script (we have none yet).

## Parallel agents on one machine
```
git worktree add .worktrees/<item> -b b3/<item> origin/develop
```

## CI
- **GitHub Actions** (`.github/workflows/ci.yml`, JDK 17 + Xvfb): a feature-branch push runs the level of what the
  branch changed (`test_level.py --base origin/develop`: docs-only skips Java, data-only runs the data job and the Java
  data tests, code runs the fast tier); develop, main and PRs into develop run `-Pfull`; nightly and manual runs
  `-Pfull,smoke`. pytest + `validate.py` when data, art or tools changed (always on develop/main).
- **Local Jenkins**, job "Futtoboru DEV" (`Jenkinsfile`, agent `EXECUTOR_BACKUP` on WORKSTATION): `TEST_TIER=auto`
  runs the slow tier + coverage after each develop commit (polls every 15 min), full + smoke + coverage at night
  (cron) with a first-parent `git bisect` between the last green nightly and HEAD when red, full + smoke for main,
  the fast tier for other branches. **To verify a lane's branch without a local full run:** "Build with Parameters",
  `BUILD_BRANCH=b3/<item>`, `TEST_TIER=full` (from a script: `POST <jenkins>/job/Futtoboru%20DEV/buildWithParameters?BUILD_BRANCH=...&TEST_TIER=full`
  with a user API token; URL and token are not in the repo, see HANDOFF). Publishes JUnit results and archives the
  desktop jar. Job setup (once): Pipeline job, "Pipeline script from SCM", this repo, script path `Jenkinsfile`,
  credentials `GITHUB_GEOMANCER86`.
