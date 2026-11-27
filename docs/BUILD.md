# Build, run and set up a machine

## Requirements
- JDK 17 or newer (Temurin 17 recommended; 21 also works).
- Maven 3.6+ (3.9 works since 2026-11-27; the old hard-coded `C:\apache-maven-3.6.3` in `build.bat` is no longer needed).
- Git with Git Credential Manager (needed to push; cloning the public repo needs no login).

## Build and run
```
mvn -B -DskipTests package          # fast build
mvn -B verify                       # build + tests (run before every push)
java -jar futtoboru-desktop/target/futtoboru-desktop-0.4.0-SNAPSHOT-jar-with-dependencies.jar
```
`build.bat` / `run-game.bat` still work on Windows. Save games go to `~/Documents/RndModGames/Futtuboru/savegames/`.
macOS is not supported yet (LWJGL3 needs `-XstartOnFirstThread`).

## New machine (BOOK3PRO or any Windows PC), PowerShell
```
java -version ; mvn -v ; git --version
git clone https://github.com/Geomancer86/Futtoboru.git C:\git\futtoboru
cd C:\git\futtoboru
git switch develop
git config user.name "Nicolas Alvargonzalez"
git config user.email "<your email>"
git config core.autocrlf true
mvn -B verify

# private companion repo (business drafts, full history of the old local repos), OUTSIDE the clone
git clone https://bitbucket.org/Geomancer/futtoboru-private.git C:\git\futtoboru-private
```
Then read `CLAUDE.md`, `PRIVACY.md` and `docs/HANDOFF.md`, claim an item, and branch: `git switch -c b3/<item> origin/develop`.

## Daily routine (both machines)
```
git fetch origin ; git switch develop ; git pull --ff-only
git switch -c b3/<item>                     # ws/<item> on WORKSTATION
# ... small commits ...
git fetch origin ; git merge origin/develop ; mvn -B verify
git push -u origin HEAD
# green? merge your own branch:
git switch develop ; git pull --ff-only ; git merge --no-ff b3/<item> ; mvn -B verify ; git push origin develop
```
Never force-push. If `git push origin develop` is rejected, pull and verify again.

## Parallel agents on one machine
```
git worktree add .worktrees/<item> -b b3/<item> origin/develop
```
