#!/bin/sh
# Futtoboru launcher, debug mode: all output goes to game-debug.log, shown after exit.
# Port of run-game-debug-simple.bat.
cd "$(dirname "$0")" || exit 1

JAR=${JAR:-futtoboru-desktop/target/futtoboru-desktop-0.6.0-alpha-jar-with-dependencies.jar}
LOG=game-debug.log
JAVA_OPTS_EXTRA=
[ "$(uname -s)" = Darwin ] && JAVA_OPTS_EXTRA=-XstartOnFirstThread
# dev tools (Match Engine Debug on the main menu) only on request: FUTTOBORU_DEV=true ./run-game-debug.sh
DEV_FLAG=
[ "${FUTTOBORU_DEV:-}" = true ] && DEV_FLAG=-Dfuttoboru.dev=true

echo "========================================"
echo "Futtoboru Game Launcher (Debug Mode)"
echo "========================================"
echo
echo "Starting game with error logging..."
echo "All output will be saved to: $LOG"
echo
echo "========================================"
echo

# shellcheck disable=SC2086
java $DEV_FLAG $JAVA_OPTS_EXTRA -jar "$JAR" > "$LOG" 2>&1
status=$?

echo
echo "========================================"
echo "Game has exited. Exit code: $status"
echo "========================================"
echo
echo "Contents of $LOG:"
echo
cat "$LOG"
exit "$status"
