#!/bin/sh
# Futtoboru game launcher. Port of run-game.bat.
# Override the jar with JAR=path/to.jar if the version changes.
cd "$(dirname "$0")" || exit 1

JAR=${JAR:-futtoboru-desktop/target/futtoboru-desktop-0.6.0-alpha-jar-with-dependencies.jar}
# LWJGL3 on macOS needs the first thread (macOS is not officially supported yet)
JAVA_OPTS_EXTRA=
[ "$(uname -s)" = Darwin ] && JAVA_OPTS_EXTRA=-XstartOnFirstThread

echo "========================================"
echo "Futtoboru Game Launcher"
echo "========================================"
echo
echo "Starting game..."
echo

# shellcheck disable=SC2086
java $JAVA_OPTS_EXTRA -jar "$JAR"
status=$?

echo
echo "========================================"
echo "Game has exited. Exit code: $status"
echo "========================================"
exit "$status"
