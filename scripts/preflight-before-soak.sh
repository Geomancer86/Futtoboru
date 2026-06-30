#!/usr/bin/env bash
# Fast simulation gate — run BEFORE any long soak.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== Soak preflight 1/2: PR gate (LeagueAndCupIT + MultiSeasonSoakIT) ==="
mvn test -pl futtoboru-core \
    -Dtest=LeagueAndCupIT,MultiSeasonSoakIT \
    -Dfuttoboru.test.logProfile=quiet

echo "=== Soak preflight 2/2: nightly cup gate (MultiSeasonLeagueAndCupSoakIT) ==="
mvn test -pl futtoboru-core -Pnightly \
    -Dtest=MultiSeasonLeagueAndCupSoakIT \
    -Dfuttoboru.test.logProfile=quiet

echo "Preflight PASSED. Safe to run: mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT"
