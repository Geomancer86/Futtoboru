# Build, run and set up a machine

## Requirements
- JDK 17 or newer (Temurin 17 recommended; 21 also works).
- Maven: nothing to install. The Maven wrapper (`mvnw` / `mvnw.cmd`) downloads the pinned Maven 3.9.12
  into `~/.m2/wrapper/` on first use (`.mvn/wrapper/maven-wrapper.properties`). A local `mvn` 3.6+ also works.
- Git with Git Credential Manager (needed to push; cloning the public repo needs no login).

## Build and run
```
./mvnw -B -DskipTests package       # fast build (PowerShell/cmd: .\mvnw.cmd ...)
./mvnw -B verify                    # build + tests (run before every push)
java -jar futtoboru-desktop/target/futtoboru-desktop-0.5.0-alpha-jar-with-dependencies.jar
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
git fetch origin ; git merge origin/develop ; .\mvnw.cmd -B verify   # Git Bash: ./mvnw
git push -u origin HEAD
# green? merge your own branch:
git switch develop ; git pull --ff-only ; git merge --no-ff b3/<item> ; .\mvnw.cmd -B verify ; git push origin develop
```
Never force-push. If `git push origin develop` is rejected, pull and verify again.

## Parallel agents on one machine
```
git worktree add .worktrees/<item> -b b3/<item> origin/develop
```

## CI
- **GitHub Actions** (`.github/workflows/ci.yml`): `./mvnw -B verify` on JDK 17 + Xvfb, plus `pytest tools/data` and `tools/data/validate.py`, on every push and PR.
- **Local Jenkins** (`Jenkinsfile`): the same checks on the agent labelled `EXECUTOR_BACKUP` (DarkBlade pattern). Parameter `BUILD_BRANCH` builds any `ws/...` or `b3/...` branch before it is merged. Publishes JUnit results and archives the desktop jar. Job setup (once): Pipeline job, "Pipeline script from SCM", this repo, script path `Jenkinsfile`, credentials `GITHUB_GEOMANCER86`.
