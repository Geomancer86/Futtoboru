# Season 1 Data Collection - GitFlow Workflow

**Purpose:** Isolated GitFlow workflow for Season 1 (1888-1889) data collection work, separate from main `develop` branch.

---

## 🌿 Branch Structure

### Main Branches
- **`season-1-data-collection`** - Main branch for data collection work (equivalent to `develop`)
- **`season-1-data-collection-main`** - Production/stable branch (if needed)

### Feature Branches
All data collection features branch from `season-1-data-collection`:
- `feature/scrape-aston-villa-players`
- `feature/scrape-wolves-players`
- `feature/scrape-blackburn-players`
- `feature/validate-player-data`
- `feature/add-staff-data`
- etc.

### Bugfix Branches
For fixing issues in data collection:
- `bugfix/fix-player-format`
- `bugfix/correct-birthdates`
- etc.

---

## 🔄 Workflow

### Starting a New Data Collection Feature

```bash
# Make sure we're on the data collection branch
git checkout season-1-data-collection
git pull

# Start a new feature
git flow feature start scrape-aston-villa-players

# Work on the feature, commit changes
git add .
git commit -m "feat: scrape Aston Villa 1888-89 player roster"

# Finish the feature (merges back to season-1-data-collection)
git flow feature finish scrape-aston-villa-players
```

### Daily Workflow

```bash
# Start work
git checkout season-1-data-collection
git pull

# Create feature branch for today's work
git flow feature start scrape-[club-name]-players

# Work, commit, finish
git flow feature finish scrape-[club-name]-players
```

---

## 📋 Feature Branch Naming Convention

### Player Data Collection
- `feature/scrape-[club-name]-players`
- Examples:
  - `feature/scrape-aston-villa-players`
  - `feature/scrape-wolves-players`
  - `feature/scrape-blackburn-players`

### Staff Data Collection
- `feature/scrape-[club-name]-staff`
- Examples:
  - `feature/scrape-preston-staff`

### Data Validation
- `feature/validate-[club-name]-data`
- Examples:
  - `feature/validate-all-clubs-data`

### Tooling/Infrastructure
- `feature/scraping-tool-improvements`
- `feature/data-validation-scripts`
- `feature/automated-parsing`

### Bug Fixes
- `bugfix/fix-[club-name]-player-format`
- `bugfix/correct-birthdate-format`
- `bugfix/fix-country-codes`

---

## 🎯 Current Work Status

### Active Features
- [List active feature branches]

### Completed Features
- [List completed features]

### Planned Features
- [ ] Scrape Aston Villa players
- [ ] Scrape Wolverhampton Wanderers players
- [ ] Scrape Blackburn Rovers players
- [ ] Scrape Bolton Wanderers players
- [ ] Scrape West Bromwich Albion players
- [ ] Scrape Accrington players
- [ ] Scrape Everton players
- [ ] Scrape Burnley players
- [ ] Scrape Derby County players
- [ ] Scrape Notts County players
- [ ] Scrape Stoke players
- [ ] Validate all player data
- [ ] Add staff data (where available)

---

## 📊 Progress Tracking

### Club Data Collection Status

| Club | Players | Staff | Validation | Status |
|------|---------|-------|------------|--------|
| Preston North End | ✅ | ❌ | ✅ | Complete |
| Aston Villa | ❌ | ❌ | ❌ | Pending |
| Wolverhampton Wanderers | ❌ | ❌ | ❌ | Pending |
| Blackburn Rovers | ❌ | ❌ | ❌ | Pending |
| Bolton Wanderers | ❌ | ❌ | ❌ | Pending |
| West Bromwich Albion | ❌ | ❌ | ❌ | Pending |
| Accrington | ❌ | ❌ | ❌ | Pending |
| Everton | ❌ | ❌ | ❌ | Pending |
| Burnley | ❌ | ❌ | ❌ | Pending |
| Derby County | ❌ | ❌ | ❌ | Pending |
| Notts County | ❌ | ❌ | ❌ | Pending |
| Stoke | ❌ | ❌ | ❌ | Pending |

---

## 🔗 Integration with Main Repo

### When Data is Ready

Once data collection is complete and validated:

1. **Create Pull Request** from `season-1-data-collection` to main repo's `develop`
2. **Review** data files
3. **Merge** to main repo
4. **Update** main repo's data files

### Data File Locations (Main Repo)
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
- Keep branches focused
- Finish features when complete
- Don't let branches get stale

### Data Quality
- Validate before committing
- Document sources
- Note any missing/incomplete data
- Test loading in game before finishing feature

---

*This workflow keeps data collection work isolated and organized, allowing parallel development without conflicts.*
