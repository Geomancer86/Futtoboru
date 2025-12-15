# Season 1 Research Data - Quick Summary

**Purpose:** Concise summary of all research data needed for Season 1 (1888-1889) MVP

---

## 🎯 MVP Critical Requirements

### Must Have (P0 - MVP Blocker)
- ✅ **Player rosters for all 12 Football League clubs**
  - Minimum: 15-20 players per club
  - Required: Name, Lastname, Country, Birthdate (year minimum)

### Already Complete
- ✅ Club basic info (names, foundation years, URLs)
- ✅ Stadium data (all 12 clubs)

### Missing (Critical)
- ❌ **Player rosters: 11/12 clubs missing** (only Preston North End complete)

---

## 📋 Research Data Needed Per Club

### Player Data (Per Club)
- **First Name** - e.g., "John"
- **Last Name** - e.g., "Goodall"
- **Country ID** - 1000 (England), 1001 (Scotland), etc.
- **Birthdate** - `yyyy-MM-dd` or `yyyy` (if full date unknown)

### Minimum Requirements
- **15-20 players per club**
- **All players must have names and countries**
- **50% minimum must have birthdates (year acceptable)**

---

## 🔍 Research Sources

### Primary Source
**Wikipedia Season Pages:**
- Format: `https://en.wikipedia.org/wiki/1888%E2%80%9389_{Club}_F.C._season`
- Contains: Squad lists, player names, birthdates, nationalities

### All 12 Clubs Have Wikipedia Pages:
1. ✅ Preston North End - `1888–89 Preston North End F.C. season`
2. ✅ Aston Villa - `1888–89 Aston Villa F.C. season`
3. ✅ Wolverhampton Wanderers - `1888–89 Wolverhampton Wanderers F.C. season`
4. ✅ Blackburn Rovers - `1888–89 Blackburn Rovers F.C. season`
5. ✅ Bolton Wanderers - `1888–89 Bolton Wanderers F.C. season`
6. ✅ West Bromwich Albion - `1888–89 West Bromwich Albion F.C. season`
7. ✅ Accrington - `Accrington F.C.` (may need season-specific)
8. ✅ Everton - `1888–89 Everton F.C. season`
9. ✅ Burnley - `1888–89 Burnley F.C. season`
10. ✅ Derby County - `1888–89 Derby County F.C. season`
11. ✅ Notts County - `1888–89 Notts County F.C. season`
12. ✅ Stoke - `1888–89 Stoke F.C. season`

---

## 📊 Missing Data Breakdown

### Clubs Missing Player Data (11/12)
1. ❌ Aston Villa
2. ❌ Wolverhampton Wanderers
3. ❌ Blackburn Rovers
4. ❌ Bolton Wanderers
5. ❌ West Bromwich Albion
6. ❌ Accrington
7. ❌ Everton
8. ❌ Burnley
9. ❌ Derby County
10. ❌ Notts County
11. ❌ Stoke

**Total Needed:** ~198 players (11 clubs × ~18 players average)

---

## 🎯 Team-by-Team Approach

### Recommended Process
1. **Pick one club** (start with Aston Villa - likely best documented)
2. **Research** - Check Wikipedia page, extract player data
3. **Extract** - Get names, birthdates, countries
4. **Format** - Create file matching existing format
5. **Test** - Load in game, verify it works
6. **Fix** - Address any issues
7. **Move to next club** - Repeat process

### Why Team-by-Team?
- ✅ Test approach early
- ✅ Catch format issues quickly
- ✅ Validate before scaling
- ✅ Easier progress tracking
- ✅ Can parallelize later if needed

---

## 📝 Data Format

### File Structure
```
# {Club Name} FC - Players File for 1888-89 League
# Source: https://en.wikipedia.org/wiki/1888%E2%80%9389_{Club}_F.C._season
# COLUMNS: id, name, lastname, country, birthdate
1,John,Goodall,1000,1863-06-19
2,Fred,Dewhurst,1000,1863-12-16
3,George,Drummond,1001,1865
...
```

### Example (Preston North End - Complete)
- 18 players
- All have names
- All have countries
- Most have full birthdates, some year-only

---

## ✅ MVP Success Criteria

### Must Complete Before MVP
- [ ] All 12 clubs have player files
- [ ] Minimum 15 players per club
- [ ] All players have names + countries
- [ ] 50%+ have birthdates (year minimum acceptable)
- [ ] All files load without errors
- [ ] Sources documented

### Current Progress
- **1/12 clubs complete** (8.3%)
- **11/12 clubs remaining** (91.7%)
- **~198 players needed**

---

## 🚨 Critical Notes

### Data Quality
- **1888 records are sparse** - accept partial data
- **Year-only birthdates are OK** for MVP
- **Better to have fewer accurate players** than many incorrect
- **Document all gaps** in file comments

### Priority
- **This is a MVP blocker** - game cannot function without player data
- **Must complete before marketing efforts**
- **Focus on accuracy over completeness**

---

## 🎯 Clear Goal

**"Complete player rosters for all 12 founding Football League clubs (1888-1889) with minimum 15 players per club, including names, nationalities, and birthdates (year minimum), validated and integrated before MVP release."**

---

*This is the foundation for MVP. Without complete player data, the match engine and squad management cannot function properly.*
