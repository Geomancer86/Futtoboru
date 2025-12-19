# Season 1 Cup Teams Research - Analysis

**Created:** 2025-12-14  
**Branch:** `season-1-data-collection`  
**Status:** Analysis Phase - Ready for Research

---

## 📊 Overview

### Context
- **League Teams:** ✅ 12/12 complete (all player rosters researched and tested)
- **Cup Teams:** ⏳ 23/23 remaining (FA Cup participants without player data)
- **Total Teams in Season 1:** 35 teams
- **FA Cup Participants:** 32 teams (12 league + 20 additional cup teams)

---

## 🎯 Objective

Research and create player roster files for all **23 non-league cup teams** that participate in the 1888-89 FA Cup but are not part of the Football League.

---

## 📋 Cup Teams Breakdown

### FA Cup Participant Club IDs (from `competition_edition.txt`)
```
"9,13,14,16,11,15,17,18,2,19,20,1,7,4,21,22,23,24,25,26,3,27,10,28,29,30,31,32,33,6,34,35"
```

### Teams Already Complete (League Teams - IDs 1-12)
1. ✅ Preston North End (ID: 1)
2. ✅ Aston Villa (ID: 2)
3. ✅ Wolverhampton Wanderers (ID: 3)
4. ✅ Blackburn Rovers (ID: 4)
5. ✅ Bolton Wanderers (ID: 5)
6. ✅ West Bromwich Albion (ID: 6)
7. ✅ Accrington (ID: 7)
8. ✅ Everton (ID: 8)
9. ✅ Burnley (ID: 9)
10. ✅ Derby County (ID: 10)
11. ✅ Notts County (ID: 11)
12. ✅ Stoke (ID: 12)

### Cup Teams Requiring Research (IDs 13-35)

#### Category 1: Well-Known Clubs (Higher Priority)
13. **Old Westminsters** (ID: 13)
    - Source: `https://en.wikipedia.org/wiki/Old_Westminsters_F.C.`
    - Founded: 1880
    - Country: England (1000)
    - Status: Old boys' club

14. **Swifts** (ID: 14)
    - Source: `https://en.wikipedia.org/wiki/Swifts_F.C.`
    - Founded: 1869
    - Country: England (1000)
    - Status: Historical club

15. **Old Brightonians** (ID: 15)
    - Source: `https://en.wikipedia.org/wiki/Old_Brightonians_F.C.`
    - Founded: 1881
    - Country: England (1000)
    - Status: Old boys' club

16. **Wrexham** (ID: 16)
    - Source: `https://en.wikipedia.org/wiki/Wrexham_A.F.C.`
    - Founded: 1864
    - Country: Wales (1002)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

17. **Nottingham Forest** (ID: 17)
    - Source: `https://en.wikipedia.org/wiki/Nottingham_Forest_F.C.`
    - Founded: 1865
    - Country: England (1000)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

18. **Linfield** (ID: 18)
    - Source: `https://en.wikipedia.org/wiki/Linfield_F.C.`
    - Founded: 1886
    - Country: Ireland (83)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

19. **Witton Albion** (ID: 19)
    - Source: `https://en.wikipedia.org/wiki/Witton_Albion_F.C.`
    - Founded: 1887
    - Country: England (1000)
    - Status: Lower league club

20. **Bootle** (ID: 20)
    - Source: `https://en.wikipedia.org/wiki/Bootle_F.C._(1879)`
    - Founded: 1879
    - Country: England (1000)
    - Status: Historical club

21. **Chatham Town** (ID: 21)
    - Source: `https://en.wikipedia.org/wiki/Chatham_Town_F.C.`
    - Founded: 1882
    - Country: England (1000)
    - Status: Lower league club

22. **South Shore** (ID: 22)
    - Source: `https://en.wikipedia.org/wiki/South_Shore_F.C.`
    - Founded: 1879
    - Country: England (1000)
    - Status: Historical club

23. **Grimsby Town** (ID: 23)
    - Source: `https://en.wikipedia.org/wiki/Grimsby_Town_F.C.`
    - Founded: 1878
    - Country: England (1000)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

24. **Sunderland Albion** (ID: 24)
    - Source: `https://en.wikipedia.org/wiki/Sunderland_Albion_F.C.`
    - Founded: 1888
    - Country: England (1000)
    - Status: Historical club (defunct)

25. **Halliwell** (ID: 25)
    - Source: `https://en.wikipedia.org/wiki/Halliwell_F.C.`
    - Founded: 1877
    - Country: England (1000)
    - Status: Historical club

26. **Crewe Alexandra** (ID: 26)
    - Source: `https://en.wikipedia.org/wiki/Crewe_Alexandra_F.C.`
    - Founded: 1877
    - Country: England (1000)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

27. **Old Carthusians** (ID: 27)
    - Source: `https://en.wikipedia.org/wiki/Old_Carthusians_F.C.`
    - Founded: 1876
    - Country: England (1000)
    - Status: Old boys' club (FA Cup winners 1881)

28. **Derby Junction** (ID: 28)
    - Source: `https://en.wikipedia.org/wiki/Derby_Junction_F.C.`
    - Founded: 1870
    - Country: England (1000)
    - Status: Historical club

29. **Notts Rangers** (ID: 29)
    - Source: `https://en.wikipedia.org/wiki/Notts_Rangers_F.C.`
    - Founded: 1868
    - Country: England (1000)
    - Status: Historical club

30. **Sheffield Wednesday** (ID: 30)
    - Source: `https://en.wikipedia.org/wiki/Sheffield_Wednesday_F.C.`
    - Founded: 1867
    - Country: England (1000)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

31. **Birmingham St George's** (ID: 31)
    - Source: `https://en.wikipedia.org/wiki/Birmingham_St_George%27s_F.C.`
    - Founded: 1875
    - Country: England (1000)
    - Status: Historical club

32. **Long Eaton Rangers** (ID: 32)
    - Source: `https://en.wikipedia.org/wiki/Long_Eaton_Rangers_F.C.`
    - Founded: 1881
    - Country: England (1000)
    - Status: Historical club

33. **Small Heath** (ID: 33)
    - Source: `https://en.wikipedia.org/wiki/Birmingham_City_F.C.`
    - Founded: 1875
    - Country: England (1000)
    - Status: **Major club** - Now Birmingham City, still exists
    - **Priority:** HIGH (well-documented)

34. **Walsall** (ID: 34)
    - Source: `https://en.wikipedia.org/wiki/Walsall_F.C.`
    - Founded: 1888
    - Country: England (1000)
    - Status: **Major club** - Still exists today
    - **Priority:** HIGH (well-documented)

35. **Heeley** (ID: 35)
    - Source: `https://en.wikipedia.org/wiki/Heeley_F.C.`
    - Founded: 1862
    - Country: England (1000)
    - Status: Historical club

---

## 📊 Research Priority Matrix

### High Priority (Well-Documented Modern Clubs)
These clubs still exist and have extensive historical records:
1. **Wrexham** (ID: 16) - Wales
2. **Nottingham Forest** (ID: 17) - England
3. **Linfield** (ID: 18) - Ireland
4. **Grimsby Town** (ID: 23) - England
5. **Crewe Alexandra** (ID: 26) - England
6. **Sheffield Wednesday** (ID: 30) - England
7. **Small Heath** (ID: 33) - England (now Birmingham City)
8. **Walsall** (ID: 34) - England

**Estimated:** 8 teams with good documentation

### Medium Priority (Historical/Old Boys' Clubs)
These have some historical records but may be less complete:
1. **Old Westminsters** (ID: 13)
2. **Swifts** (ID: 14)
3. **Old Brightonians** (ID: 15)
4. **Old Carthusians** (ID: 27) - FA Cup winners 1881
5. **Witton Albion** (ID: 19)
6. **Bootle** (ID: 20)
7. **Chatham Town** (ID: 21)
8. **South Shore** (ID: 22)
9. **Sunderland Albion** (ID: 24)
10. **Halliwell** (ID: 25)
11. **Derby Junction** (ID: 28)
12. **Notts Rangers** (ID: 29)
13. **Birmingham St George's** (ID: 31)
14. **Long Eaton Rangers** (ID: 32)
15. **Heeley** (ID: 35)

**Estimated:** 15 teams with variable documentation

---

## 🔍 Research Strategy

### Phase 1: High Priority Teams (8 teams)
- Focus on well-documented modern clubs first
- These should have complete Wikipedia season pages
- Expected to have full squad lists and player data

### Phase 2: Medium Priority Teams (15 teams)
- Research historical clubs and old boys' teams
- May require more extensive research
- Some may have limited player data available

### Research Sources
1. **Primary:** Wikipedia season pages (e.g., `1888-89 [Club Name] season`)
2. **Secondary:** Individual player Wikipedia pages
3. **Tertiary:** Historical football databases
4. **Fallback:** Club official histories and archives

### Data Requirements (Same as League Teams)
- **Format:** `id, name, lastname, country, birthdate`
- **Birthdate:** `yyyy-MM-dd` or `yyyy` (year-only acceptable)
- **Country IDs:** 1000 (England), 1001 (Scotland), 1002 (Wales), 83 (Ireland)
- **Minimum:** Player names and countries (birthdates optional but preferred)

---

## 📁 File Structure

### Target Location (PM Repo)
```
project-management/research-data/season-1/club_players/
├── 1.txt  (Preston - ✅ Complete)
├── 2.txt  (Aston Villa - ✅ Complete)
├── ...
├── 12.txt (Stoke - ✅ Complete)
├── 13.txt (Old Westminsters - ⏳ TODO)
├── 14.txt (Swifts - ⏳ TODO)
├── ...
└── 35.txt (Heeley - ⏳ TODO)
```

### File Format (Same as League Teams)
```csv
# [Club Name] FC - Players File for 1888-89 League
#
# NOTE: no club id data, and no contract data, initially the contracts will be generated at runtime on new game setup
#
# Source: https://en.wikipedia.org/wiki/1888%E2%80%9389_[Club]_season
#
# Additional Sources:
#   - [Additional Wikipedia pages]
#
# Birthdate format: yyyy-MM-dd
#
# NOTE: some players don't have a known birthdate so the game generates month and day on new game
#
# NOTE 2: this is only the known players name, common name or nickname, it doesn't include the players full name
#
# COLUMNS
# id, name, lastname, country, birthdate 
1,Player,Name,1000,1865-01-01
2,Player,Name,1000,1866
...
```

---

## 📈 Estimated Scope

### Player Count Estimates
- **High Priority Teams:** ~15-25 players per team (8 teams) = **120-200 players**
- **Medium Priority Teams:** ~10-20 players per team (15 teams) = **150-300 players**
- **Total Estimated Players:** **270-500 players**

### Research Time Estimates
- **High Priority:** ~1-2 hours per team = **8-16 hours**
- **Medium Priority:** ~2-4 hours per team = **30-60 hours**
- **Total Estimated Time:** **38-76 hours**

### Data Quality Expectations
- **High Priority:** 60-80% full birthdates, 20-40% year-only
- **Medium Priority:** 40-60% full birthdates, 40-60% year-only
- **All Teams:** 100% names and countries (minimum requirement)

---

## ✅ Success Criteria

1. **All 23 cup teams have player roster files created**
2. **All files follow the same format as league teams**
3. **All players have at minimum: name, lastname, country**
4. **Birthdates in correct format (`yyyy-MM-dd` or `yyyy`)**
5. **All sources documented in file headers**
6. **Files tested in game (same process as Aston Villa)**

---

## 🚀 Next Steps

1. **Create feature branch:** `feature/research-cup-teams-season-1`
2. **Start with High Priority teams** (8 teams)
3. **Research and create files one by one**
4. **Test each file in game before moving to next**
5. **Move to Medium Priority teams** (15 teams)
6. **Final testing and integration**

---

## 📝 Notes

- Some cup teams may have very limited historical records
- Old boys' clubs may have incomplete squad data
- Defunct clubs (e.g., Sunderland Albion) may require extensive research
- Some teams may need to use estimated/placeholder data where historical records are unavailable
- All research should be documented with sources for future verification

---

*This analysis document will be updated as research progresses.*
