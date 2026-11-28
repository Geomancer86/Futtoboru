#!/bin/sh
# Futtoboru build and run: build.sh, then run-game-debug.sh.
# Port of build-and-run.bat.
cd "$(dirname "$0")" || exit 1

JAR=${JAR:-futtoboru-desktop/target/futtoboru-desktop-0.5.0-alpha-jar-with-dependencies.jar}
export JAR

echo "========================================"
echo "Futtoboru Build and Run"
echo "========================================"
echo
echo "Step 1: Building project..."
echo

sh ./build.sh
BUILD_EXIT_CODE=$?
echo
echo "Build script exited with code: $BUILD_EXIT_CODE"

if [ "$BUILD_EXIT_CODE" -ne 0 ]; then
    echo
    echo "========================================"
    echo "BUILD FAILED - Cannot run game"
    echo "========================================"
    echo
    echo "Please check the build errors above."
    exit "$BUILD_EXIT_CODE"
fi

if [ ! -f "$JAR" ]; then
    echo
    echo "========================================"
    echo "ERROR: JAR file not found after build!"
    echo "========================================"
    echo
    echo "Expected location:"
    echo "  $JAR"
    exit 1
fi

echo
echo "JAR file verified: $JAR"
echo
echo "========================================"
echo "Step 2: Starting game in debug mode..."
echo "========================================"
echo

exec sh ./run-game-debug.sh
