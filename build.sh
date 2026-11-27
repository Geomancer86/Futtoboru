#!/bin/sh
# Futtoboru build: generate build info, then mvnw clean install (no tests).
# Port of build.bat. Uses JAVA_HOME if set, otherwise the java on PATH (JDK 17+).
# Extra arguments are passed to Maven.
cd "$(dirname "$0")" || exit 1

if [ -n "$JAVA_HOME" ]; then
    echo "Using Java: $JAVA_HOME"
    PATH="$JAVA_HOME/bin:$PATH"
    export PATH
else
    echo "JAVA_HOME not set, using the java on PATH (needs JDK 17+)"
fi

echo "Generating build information..."
sh ./generate-build-info.sh || exit $?
echo

echo "Building project..."
echo
sh ./mvnw -B clean install -DskipTests "$@"
status=$?
echo
if [ "$status" -eq 0 ]; then
    echo "Build complete."
else
    echo "Build FAILED (exit code $status)."
fi
exit "$status"
