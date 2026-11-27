# GitFlow Workflow Guide for Futtoboru

## Setup Complete ✅

GitFlow has been initialized with the following configuration:
- **Production branch**: `main`
- **Development branch**: `develop`
- **Feature prefix**: `feature/`
- **Bugfix prefix**: `bugfix/`
- **Release prefix**: `release/`
- **Hotfix prefix**: `hotfix/`

## Current Status

- ✅ Repository connected to: `https://github.com/Geomancer86/futtoboru`
- ✅ Currently on `develop` branch
- ✅ Planning documents committed
- ✅ GitFlow initialized

## GitFlow Workflow

### Daily Development Workflow

#### Starting a New Feature
```bash
# Start a new feature branch from develop
git flow feature start <feature-name>

# Example: Starting match engine feature
git flow feature start match-engine

# Work on your feature, commit as usual
git add .
git commit -m "feat: implement basic match simulation"

# Finish the feature (merges to develop and deletes feature branch)
git flow feature finish match-engine
```

#### Starting a Bug Fix
```bash
# Start a bugfix branch from develop
git flow bugfix start <bugfix-name>

# Example: Fixing null pointer exception
git flow bugfix start fix-null-pointer-startup

# Work on the fix, commit
git add .
git commit -m "fix: add null checks in MainGameScreen"

# Finish the bugfix
git flow bugfix finish fix-null-pointer-startup
```

#### Creating a Release
```bash
# Start a release branch from develop
git flow release start v1.0.0

# Update version numbers, changelog, etc.
# Commit any release-related changes
git commit -m "chore: update version to 1.0.0"

# Finish the release (creates tag, merges to main and develop)
git flow release finish v1.0.0

# Push everything including tags
git push origin main
git push origin develop
git push --tags
```

#### Hotfix (for critical production bugs)
```bash
# Start a hotfix from main
git flow hotfix start v1.0.1

# Fix the critical bug
git commit -m "fix: critical match engine crash"

# Finish hotfix (merges to main and develop, creates tag)
git flow hotfix finish v1.0.1

# Push everything
git push origin main
git push origin develop
git push --tags
```

## Branch Strategy

```
main (production)
  ↑
  ├── hotfix/v1.0.1
  │
develop (development)
  ↑
  ├── feature/match-engine
  ├── feature/player-system
  ├── bugfix/fix-null-pointer
  └── release/v1.0.0
```

## Common Commands

### Feature Branches
```bash
git flow feature list              # List all feature branches
git flow feature start <name>      # Start new feature
git flow feature finish <name>     # Finish and merge feature
git flow feature publish <name>    # Publish feature to remote
git flow feature track <name>      # Track remote feature branch
```

### Bugfix Branches
```bash
git flow bugfix start <name>       # Start new bugfix
git flow bugfix finish <name>      # Finish and merge bugfix
```

### Release Branches
```bash
git flow release start <version>  # Start new release
git flow release finish <version> # Finish release (creates tag)
```

### Hotfix Branches
```bash
git flow hotfix start <version>   # Start hotfix from main
git flow hotfix finish <version>   # Finish hotfix
```

## Best Practices

### Commit Messages
Follow conventional commits format:
- `feat:` - New feature
- `fix:` - Bug fix
- `docs:` - Documentation
- `style:` - Code style changes
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `chore:` - Maintenance tasks

Examples:
```bash
git commit -m "feat: implement match simulation engine"
git commit -m "fix: resolve null pointer in MainGameScreen"
git commit -m "docs: update roadmap for v1.0"
```

### Working with Remote
```bash
# Always pull latest develop before starting new work
git checkout develop
git pull origin develop

# Push feature branches to remote for backup/collaboration
git flow feature publish <name>

# Pull latest changes in feature branch
git flow feature pull <name>
```

## Next Steps

1. **Push current changes to remote:**
   ```bash
   git push origin develop
   ```

2. **Start your first feature branch:**
   ```bash
   git flow feature start fix-null-pointer-startup
   ```

3. **Work on the fix, then finish:**
   ```bash
   git flow feature finish fix-null-pointer-startup
   ```

## Integration with v1.0 Roadmap

- **Phase 1 (Bug Fixes)**: Use `bugfix/` branches
- **Phase 2-7 (Features)**: Use `feature/` branches
- **v1.0 Release**: Use `release/v1.0.0` branch
- **Critical Issues**: Use `hotfix/` branches

## Troubleshooting

### If you need to cancel a feature/bugfix:
```bash
git flow feature delete <name>     # Delete local feature branch
# Or manually:
git checkout develop
git branch -D feature/<name>
```

### If you need to update develop in your feature:
```bash
git checkout develop
git pull origin develop
git checkout feature/<name>
git merge develop
```

---

*GitFlow setup completed: 2025-12-14*
*Repository: https://github.com/Geomancer86/futtoboru*

