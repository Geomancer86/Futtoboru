#!/bin/sh
# Futtoboru launcher, debug mode: output on screen AND in game-debug.log.
# Port of run-game-debug.bat.
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

# POSIX sh has no pipefail: keep java's exit code in a temp file.
rcfile=$(mktemp 2>/dev/null || echo "${TMPDIR:-/tmp}/futtoboru-rc.$$")
# shellcheck disable=SC2086
{ java $DEV_FLAG $JAVA_OPTS_EXTRA -jar "$JAR" 2>&1; echo $? > "$rcfile"; } | tee "$LOG"
status=$(cat "$rcfile" 2>/dev/null)
rm -f "$rcfile"
[ -n "$status" ] || status=1

echo
echo "========================================"
echo "Game has exited. Exit code: $status"
echo "========================================"
echo
echo "Check $LOG for all output and errors"
exit "$status"
