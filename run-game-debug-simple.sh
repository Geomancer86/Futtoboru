#!/bin/sh
# Futtoboru launcher, debug mode: all output goes to game-debug.log, shown after exit.
# Port of run-game-debug-simple.bat.
cd "$(dirname "$0")" || exit 1

JAR=${JAR:-futtoboru-desktop/target/futtoboru-desktop-0.5.0-alpha-jar-with-dependencies.jar}
LOG=game-debug.log
JAVA_OPTS_EXTRA=
[ "$(uname -s)" = Darwin ] && JAVA_OPTS_EXTRA=-XstartOnFirstThread

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
java $JAVA_OPTS_EXTRA -jar "$JAR" > "$LOG" 2>&1
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
