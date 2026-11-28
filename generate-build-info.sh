#!/bin/sh
# Generate futtoboru-core/src/main/resources/build.properties with build information.
# Port of generate-build-info.bat. The build number increments on EVERY build, not just commits.
cd "$(dirname "$0")" || exit 1

PROPS=futtoboru-core/src/main/resources/build.properties
echo "Generating build.properties..."

GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null)
[ -n "$GIT_COMMIT" ] || GIT_COMMIT=unknown

CURRENT_BUILD=0
if [ -f "$PROPS" ]; then
    CURRENT_BUILD=$(sed -n 's/^build\.number=[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$PROPS" | head -n 1)
    [ -n "$CURRENT_BUILD" ] || CURRENT_BUILD=0
fi
if [ "$CURRENT_BUILD" -lt 1 ]; then
    BUILD_NUMBER=1
    echo "No valid build number found, starting at: $BUILD_NUMBER"
else
    BUILD_NUMBER=$((CURRENT_BUILD + 1))
    echo "Found existing build number: $CURRENT_BUILD, incrementing to: $BUILD_NUMBER"
fi

BUILD_TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# Version from pom.xml (hardcoded for now, as in the .bat)
VERSION=0.5.0-SNAPSHOT

{
    echo "version=$VERSION"
    echo "build.number=$BUILD_NUMBER"
    echo "git.commit.id.abbrev=$GIT_COMMIT"
    echo "build.timestamp=$BUILD_TIMESTAMP"
} > "$PROPS"

echo "Build info generated:"
echo "  Version: $VERSION"
echo "  Build Number: $BUILD_NUMBER"
echo "  Commit: $GIT_COMMIT"
echo "  Timestamp: $BUILD_TIMESTAMP"
echo
echo "File created: $PROPS"
