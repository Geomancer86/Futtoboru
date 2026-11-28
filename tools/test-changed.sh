#!/usr/bin/env bash
# T-01 (D27), as DarkBlade tools/test-changed.ps1: while coding, only the tests touched by the change, no lock.
#   tools/test-changed.sh                 against HEAD (what is not committed)
#   tools/test-changed.sh --base origin/develop   everything of the branch
cd "$(dirname "$0")/.." && exec python tools/test_level.py --affected "$@"
