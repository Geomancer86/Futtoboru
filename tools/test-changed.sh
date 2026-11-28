#!/usr/bin/env bash
# T-01 (D27), as DarkBlade tools/test-changed.ps1: while coding, only the tests touched by the change, no lock.
#   tools/test-changed.sh                          against HEAD (what is not committed)
#   tools/test-changed.sh --base origin/develop    everything of the branch
cd "$(dirname "$0")/.." || exit 2
for PY in python3 python py; do "$PY" -c "" >/dev/null 2>&1 && break; done
exec "$PY" tools/test_level.py --affected "$@"
