# Season 1 Data Collection - Isolated Workflow

**Purpose:** Isolated workflow for Season 1 (1888-1889) data collection work, completely separate from main `develop` branch.

**Base Branch:** `season-1-data-collection`  
**Status:** Active development branch for data collection

---

## 🌿 Branch Structure

### Main Branches
- **`season-1-data-collection`** - Main branch for all data collection work
- **`develop`** - Main repo development (DO NOT use for data work)
- **`master`** - Production (DO NOT use for data work)

### Feature Branches (Branch from `season-1-data-collection`)
All data collection features branch from `season-1-data-collection`:
- `feature/scrape-aston-villa-players`
- `feature/scrape-wolves-players`
- `feature/scrape-blackburn-players`
- `feature/validate-player-data`
- `feature/add-staff-data`
- `feature/scraping-tool-improvements`
- etc.

### Bugfix Branches (Branch from `season-1-data-collection`)
- `bugfix/fix-player-format`
- `bugfix/correct-birthdates`
- `bugfix/fix-country-codes`
- etc.

---

## 🔄 Workflow Commands

### Starting a New Data Collection Feature

```bash
# Make sure we're on the data collection branch
git checkout season-1-data-collection
git pull origin season-1-data-collection  # if remote exists

# Create feature branch manually (since we're not using git flow for this branch)
git checkout -b feature/scrape-aston-villa-players

# Work on the feature, commit changes
git add .
git commit -m "feat: scrape Aston Villa 1888-89 player roster from Wikipedia"

# When done, merge back to season-1-data-collection
git checkout season-1-data-collection
git merge feature/scrape-aston-villa-players
git branch -d feature/scrape-aston-villa-players  # delete local branch
```

### Daily Workflow

```bash
# Start work - switch to data collection branch
git checkout season-1-data-collection

# Create feature branch for today's work
git checkout -b feature/scrape-[club-name]-players

# Work, commit frequently
git add .
git commit -m "feat: add [club] player data"

# When feature complete, merge back
git checkout season-1-data-collection
git merge feature/scrape-[club-name]-players
git branch -d feature/scrape-[club-name]-players
```

### Creating Bugfix Branches

```bash
# From season-1-data-collection
git checkout season-1-data-collection
git checkout -b bugfix/fix-[issue-description]

# Fix, commit, merge
git add .
git commit -m "fix: correct [issue]"
git checkout season-1-data-collection
git merge bugfix/fix-[issue-description]
git branch -d bugfix/fix-[issue-description]
```

---

## 📋 Feature Branch Naming Convention

### Player Data Collection
- `feature/scrape-[club-name]-players`
- Examples:
  - `feature/scrape-aston-villa-players`
  - `feature/scrape-wolves-players`
  - `feature/scrape-blackburn-players`
  - `feature/scrape-bolton-players`

### Staff Data Collection
- `feature/scrape-[club-name]-staff`
- Examples:
  - `feature/scrape-preston-staff`

### Data Validation
- `feature/validate-[club-name]-data`
- `feature/validate-all-clubs-data`
- `feature/validate-birthdates`

### Tooling/Infrastructure
- `feature/scraping-tool-improvements`
- `feature/data-validation-scripts`
- `feature/automated-parsing`
- `feature/wikipedia-api-integration`

### Bug Fixes
- `bugfix/fix-[club-name]-player-format`
- `bugfix/correct-birthdate-format`
- `bugfix/fix-country-codes`
- `bugfix/fix-duplicate-players`

---

## 🎯 Current Work Status

### Active Features
- [List active feature branches here]

### Completed Features
- ✅ `feature/season_1_analysis` - Initial analysis document

### Planned Features
- [ ] `feature/scrape-aston-villa-players`
- [ ] `feature/scrape-wolves-players`
- [ ] `feature/scrape-blackburn-players`
- [ ] `feature/scrape-bolton-players`
- [ ] `feature/scrape-west-brom-players`
- [ ] `feature/scrape-accrington-players`
- [ ] `feature/scrape-everton-players`
- [ ] `feature/scrape-burnley-players`
- [ ] `feature/scrape-derby-players`
- [ ] `feature/scrape-notts-county-players`
- [ ] `feature/scrape-stoke-players`
- [ ] `feature/validate-all-player-data`
- [ ] `feature/add-staff-data` (where available)

---

## 📊 Progress Tracking

### Club Data Collection Status

| Club ID | Club Name | Players | Staff | Validation | Status | Branch |
|---------|-----------|---------|-------|------------|--------|--------|
| 1 | Preston North End | ✅ 18 | ❌ | ✅ | Complete | - |
| 2 | Aston Villa | ❌ | ❌ | ❌ | Pending | - |
| 3 | Wolverhampton Wanderers | ❌ | ❌ | ❌ | Pending | - |
| 4 | Blackburn Rovers | ❌ | ❌ | ❌ | Pending | - |
| 5 | Bolton Wanderers | ❌ | ❌ | ❌ | Pending | - |
| 6 | West Bromwich Albion | ❌ | ❌ | ❌ | Pending | - |
| 7 | Accrington | ❌ | ❌ | ❌ | Pending | - |
| 8 | Everton | ❌ | ❌ | ❌ | Pending | - |
| 9 | Burnley | ❌ | ❌ | ❌ | Pending | - |
| 10 | Derby County | ❌ | ❌ | ❌ | Pending | - |
| 11 | Notts County | ❌ | ❌ | ❌ | Pending | - |
| 12 | Stoke | ❌ | ❌ | ❌ | Pending | - |

**Progress: 1/12 clubs complete (8.3%)**

---

## 🔗 Integration with Main Repo

### When Data is Ready for Main Repo

Once data collection is complete and validated in this branch:

1. **Data files are in PM repo** (planning, analysis, scripts)
2. **Actual data files go to main repo** at:
   - `futtoboru-core/src/main/resources/mods/seasons/18/club_players/{clubId}.txt`
3. **Create PR or merge** from `season-1-data-collection` to main repo's `develop`
4. **Or manually copy** validated data files to main repo

### File Locations

**PM Repo (This Repo):**
- Analysis documents: `project-management/ANALYSIS_SEASON_1_DATA_COLLECTION.md`
- Workflow docs: `project-management/SEASON_1_DATA_WORKFLOW.md`
- Scraping scripts: `project-management/scripts/season-1/`
- Collected data (temporary): `project-management/data/season-1/`

**Main Repo (When Ready):**
- Player files: `futtoboru-core/src/main/resources/mods/seasons/18/club_players/{clubId}.txt`
- Staff files: `futtoboru-core/src/main/resources/mods/seasons/18/club_staff/{clubId}.txt` (if created)
- Stadium files: `futtoboru-core/src/main/resources/mods/seasons/18/club_stadiums/{clubId}.txt`

---

## 💡 Best Practices

### Commits
- Use descriptive commit messages
- Reference club names and data types
- Include source URLs in commit messages
- Example: `feat: add Aston Villa 1888-89 roster (18 players) from Wikipedia`

### Feature Branches
- One feature per branch
- Keep branches focused (one club at a time)
- Merge when complete
- Delete branches after merging
- Don't let branches get stale

### Data Quality
- Validate before committing
- Document sources in commit messages
- Note any missing/incomplete data
- Test data format matches existing files

### Branch Isolation
- **NEVER** merge `season-1-data-collection` into `develop` directly
- Keep data collection work isolated
- Only merge to main repo when data is validated and ready

---

## 🚀 Quick Start Guide

### First Time Setup
```bash
# Clone/checkout the PM repo
cd G:\git\futtoboru_cursor_pm

# Switch to data collection branch
git checkout season-1-data-collection

# You're ready to work!
```

### Starting a New Club Data Collection
```bash
# Make sure you're on the right branch
git checkout season-1-data-collection

# Create feature branch
git checkout -b feature/scrape-aston-villa-players

# Do your work (scrape, validate, format)
# ... work ...

# Commit
git add .
git commit -m "feat: scrape Aston Villa 1888-89 roster (18 players)"

# Merge back
git checkout season-1-data-collection
git merge feature/scrape-aston-villa-players
git branch -d feature/scrape-aston-villa-players
```

---

## 📝 Notes

- This branch is **completely isolated** from `develop`
- All data collection work happens here
- Feature branches are created manually (not using git flow)
- This allows parallel work without conflicts
- When data is ready, it can be integrated into main repo separately

---

*This workflow keeps data collection work isolated and organized, allowing parallel development without conflicts with main development work.*
