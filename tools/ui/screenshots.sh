#!/usr/bin/env sh
# V-01: dev-only screenshot tour. Opens the game in a window, plays a seeded 1888-89 game and writes one PNG per
# screen, drill-down and dialog: <outDir>/1x/*.png (1280x720, UI scale 1x) and <outDir>/2x/*.png (2560x1440, 2x).
# Usage: tools/ui/screenshots.sh [outDir=target/screenshots] [passes=1x,2x] [locale=en_US]
# Exit code 1 (and "[tour] PROBLEM" lines) when a stop failed or the window could not get the size.
set -e
cd "$(dirname "$0")/../.."
OUT="${1:-target/screenshots}"
ROOT="$(pwd -W 2>/dev/null || pwd)"  # Git Bash: a Windows path for java
case "$OUT" in /*|?:*) ;; *) OUT="$ROOT/$OUT" ;; esac
./mvnw -B -q -pl futtoboru-core test-compile org.codehaus.mojo:exec-maven-plugin:3.1.0:exec \
  -Dexec.classpathScope=test -Dexec.executable=java \
  -Dexec.args="-cp %classpath com.rndmodgames.futtoboru.tools.ScreenshotTour $OUT ${2:-1x,2x} ${3:-en_US}"
