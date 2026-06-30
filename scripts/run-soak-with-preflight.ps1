# Preflight then 60-season north-star soak (league only). Uses quiet logs for realistic timing.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

& "$Root\scripts\preflight-before-soak.ps1"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Soak: LongRunSoakIT (60 league seasons, quiet logs) ===" -ForegroundColor Cyan
mvn test -pl futtoboru-core -Psoak `
    "-Dtest=LongRunSoakIT" `
    "-Dfuttoboru.test.logProfile=quiet"
exit $LASTEXITCODE
