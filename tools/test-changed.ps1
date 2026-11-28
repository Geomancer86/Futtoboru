<#
.SYNOPSIS
    T-01 (D27), as DarkBlade tools/test-changed.ps1: while coding, only the tests touched by the change
    (the changed tests and the tests that name a changed class), with -Dtest, through the suite lock (one Maven run at a time).
.EXAMPLE
    .	ools	est-changed.ps1                          # against HEAD: what is not committed
    .	ools	est-changed.ps1 --base origin/develop    # everything of the branch
    .	ools	est-changed.ps1 --offline
#>
Set-Location (Join-Path $PSScriptRoot "..")
python tools/test_level.py --affected @args
exit $LASTEXITCODE
