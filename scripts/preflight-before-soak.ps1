# Fast simulation gate (~30–90s) — run BEFORE any long soak.
# Exits non-zero on first failure so 60-season runs are not started blindly.
$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

Write-Host "=== Soak preflight 1/2: PR gate (LeagueAndCupIT + MultiSeasonSoakIT) ===" -ForegroundColor Cyan
mvn test -pl futtoboru-core `
    "-Dtest=LeagueAndCupIT,MultiSeasonSoakIT" `
    "-Dfuttoboru.test.logProfile=quiet"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Preflight FAILED at PR gate. Do not run -Psoak." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "=== Soak preflight 2/2: nightly cup gate (MultiSeasonLeagueAndCupSoakIT) ===" -ForegroundColor Cyan
mvn test -pl futtoboru-core -Pnightly `
    "-Dtest=MultiSeasonLeagueAndCupSoakIT" `
    "-Dfuttoboru.test.logProfile=quiet"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Preflight FAILED at cup multi-season gate. Do not run -Psoak." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "Preflight PASSED. Safe to run: mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT" -ForegroundColor Green
exit 0
