#!/usr/bin/env bash
# T-01 (D27): the level gate (tools/test_level.py --run). Options: --full --offline --base <rev> --json
cd "$(dirname "$0")/../.." && exec python tools/test_level.py --run "$@"
