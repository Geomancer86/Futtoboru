# T-01 (D27): the level gate (tools/test_level.py --run). Options: --full --offline --base <rev> --json
Set-Location (Join-Path $PSScriptRoot "..\..")
python tools/test_level.py --run @args
exit $LASTEXITCODE
