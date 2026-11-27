# Futtoboru Branch Status Checker
# This script checks the status of all branches in the main repository

$MainRepoPath = "G:\git\futtoboru_cursor\futtoboru"
$OutputFile = "G:\git\futtoboru_cursor_pm\project-management\BRANCH_STATUS_CURRENT.txt"

Write-Host "Checking branch status for Futtoboru..." -ForegroundColor Cyan

if (-not (Test-Path $MainRepoPath)) {
    Write-Host "Error: Main repository not found at $MainRepoPath" -ForegroundColor Red
    exit 1
}

Push-Location $MainRepoPath

# Get current branch
$currentBranch = git rev-parse --abbrev-ref HEAD
Write-Host "Current branch: $currentBranch" -ForegroundColor Yellow

# Get all branches
Write-Host "`nAll branches:" -ForegroundColor Cyan
git branch -a

# Check develop branch status
Write-Host "`n=== DEVELOP BRANCH STATUS ===" -ForegroundColor Green
git checkout develop 2>$null
git pull origin develop 2>$null

$developCommits = git rev-list --count HEAD
$developAhead = (git rev-list --count origin/develop..HEAD 2>$null)
$developBehind = (git rev-list --count HEAD..origin/develop 2>$null)

Write-Host "Commits on develop: $developCommits"
Write-Host "Ahead of origin/develop: $developAhead"
Write-Host "Behind origin/develop: $developBehind"

# Check feature branches
Write-Host "`n=== FEATURE BRANCHES ===" -ForegroundColor Green

$featureBranches = @("cursor/feature-unemployed-job-system", "cursor/feature-match-engine")

foreach ($branch in $featureBranches) {
    $branchExists = git branch -a | Select-String $branch
    if ($branchExists) {
        Write-Host "`nBranch: $branch" -ForegroundColor Yellow
        git log --oneline develop..$branch -5 2>$null
        $commitsAhead = (git rev-list --count develop..$branch 2>$null)
        Write-Host "Commits ahead of develop: $commitsAhead"
    } else {
        Write-Host "Branch $branch not found" -ForegroundColor Gray
    }
}

# Recent commits
Write-Host "`n=== RECENT COMMITS (Last 10) ===" -ForegroundColor Green
git log --oneline --graph --all -10

# Files changed in last commit
Write-Host "`n=== FILES CHANGED IN LAST COMMIT ===" -ForegroundColor Green
git diff-tree --no-commit-id --name-only -r HEAD

# Return to original branch
git checkout $currentBranch 2>$null

Pop-Location

Write-Host "`nStatus check complete!" -ForegroundColor Green
