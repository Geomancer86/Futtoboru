#!/usr/bin/env bash
# T-01 (D27): the level gate (tools/test_level.py --run). Options: --full --offline --base <rev> --json
cd "$(dirname "$0")/../.." || exit 2
for PY in python3 python py; do "$PY" -c "" >/dev/null 2>&1 && break; done
exec "$PY" tools/test_level.py --run "$@"
