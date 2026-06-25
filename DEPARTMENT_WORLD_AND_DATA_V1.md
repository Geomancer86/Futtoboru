# Department: World & Data — v1 Deep Dive

**Department expert:** _________________  
**Status:** Season 18 ~70% content; Season 19 0%; loaders need refactor for multi-season  
**Blocks release:** Yes — rules files and season 19 content required for v1 proof

---

## 1. Mission (v1)

Own **all mod data** and **loaders** so each season has correct clubs, squads, stadiums, **competition rules**, and scripts — and season 19 proves the **forever loop**.

---

## 2. Season 18 inventory (current)

| Asset | Count / status |
|-------|----------------|
| `mods/seasons.txt` | 1 row (id `18`) |
| `clubs.txt` | 35 clubs (12 league + 23 cup-only) |
| `club_players/` | **12 files** (league clubs only) |
| `club_stadiums/` | **12 files** (league clubs only) |
| `competition_rules.txt` | ❌ Missing |
| `scripts/` (filesystem) | ❌ Missing — hardcoded in `ScriptsLoader.java` |
| `leagues.txt` | Stub — **never loaded** |
| FA Cup edition | 32 participants in `competitions/1/` |
| Modifiers / professions | ✅ Present |

**Squad sizes (league):** Preston 18 … Stoke 32 (see dept tasks for full table in research).

---

## 3. Season 19 (required for v1)

**Nothing exists.** Minimum package:

```
mods/seasons.txt                    → add id 19
mods/seasons/19/countries.txt
mods/seasons/19/clubs.txt           → same 12 ids (1–12)
mods/seasons/19/competition_rules.txt
mods/seasons/19/club_players/1–12.txt
mods/seasons/19/club_stadiums/1–12.txt   → Wolves: Molineux (1889)
mods/seasons/19/scripts/season_start.txt  → NOT league creation
```

**Historical notes for authors:**
- Same 12 clubs (no relegation yet)
- 2-1-0 from day one (no Nov amendment in 1889–90)
- Goal average tie-break
- Wolves moved to Molineux in 1889

---

## 4. Gaps not in initial analysis (new findings)

1. **Multi-season `DatabaseLoader` duplicates clubs** — Loading two seasons appends same IDs 1–12 twice to global registry.
2. **`ScriptsLoader` attaches 1888 league-creation script to every season** — Season 19 would re-found the league.
3. **Modifier loaders hardcoded to `mods/seasons/18/`** — Season 19 squads get 1888 modifier context.
4. **`initializeLeagues()` dead** — Global `mods/leagues.txt` never used at runtime.
5. **Player IDs are per-club local** (each file starts at 1) — Global person registry collision risk.
6. **`region_modifiers.txt` bug** — Lancashire row has `countryId=1` (Afghanistan); should be `1000`.
7. **`clubs.txt` header wrong** — Says state/city; column 7 is `country_id`.
8. **Clubs 13–35 have no players/stadiums** — FA Cup IDs only; not match-ready.
9. **Competition edition format** — No dates, champion, round rules in mod files.
10. **All 35 clubs loaded into new game** — Not just 12 league clubs.

---

## 5. Proposed `competition_rules.txt` schema

**Location:** `mods/seasons/{seasonId}/competition_rules.txt`

```ini
[LEAGUE]
league_name=English Football League
match_format=DOUBLE_ROUND_ROBIN
points_win=2
points_draw=1
points_loss=0
tie_breakers=POINTS,GOAL_AVERAGE,GOALS_SCORED,ALPHABETICAL
season_window_start=1888-09-01
season_window_end=1889-04-30

[AMENDMENT]
effective_date=1888-11-01
points_win=2
points_draw=1
description=Points system formalized November 1888
```

Season 19 file: same 2-1-0 block, **no AMENDMENT**, window `1889-09-01` → `1890-04-30`.

**Loader:** New `CompetitionRulesLoader.load(Season)` → `CompetitionRules` + optional amendments list.

---

## 6. Player ID strategy (decide before season 19)

| Option | Scheme | Effort |
|--------|--------|--------|
| **A** | `{clubId * 1000 + localId}` | Medium migration |
| **B** | Scope all lookups by `(clubId, playerId)` | Code-wide |
| **C** | Delta-only season 19 (reuse save-state players) | Requires rollover code |

**Recommend Option A** — document in player file headers.

---

## 7. Multi-season club loading (decide before season 19 row)

| Option | Description |
|--------|-------------|
| **A** | Season 19 omits `clubs.txt`; rollover reuses save clubs |
| **B** | Full `clubs.txt` with **merge-by-ID** in loader (not append) |
| **C** | Shared global club registry + season overlays |

**Recommend B** with `ClubsLoader` fix — matches current mod shape.

---

## 8. v1 deliverables (this department)

| ID | Deliverable | Acceptance |
|----|-------------|------------|
| WD-1 | `mods/seasons/18/competition_rules.txt` | Parses to historical rules |
| WD-2 | `CompetitionRulesLoader` | Used by `ScriptsManager.createLeague()` |
| WD-3 | Fix modifier loaders → `mods/seasons/{id}/` | Dynamic season path |
| WD-4 | Refactor `ScriptsLoader` | File-based or season-specific scripts |
| WD-5 | Fix multi-season club registry | No duplicate IDs |
| WD-6 | Full season 19 folder | Loads without error |
| WD-7 | Fix `region_modifiers` countryId | Lancashire = 1000 |
| WD-8 | Data validation tests V-1–V-9 | CI gate |
| WD-9 | Player ID scheme documented + applied | No global collisions |

---

## 9. Validation rules (CI)

| ID | Rule |
|----|------|
| V-1 | Every `seasons.txt` id has folder with `countries.txt`, `clubs.txt` |
| V-2 | League clubs 1–12 have players (≥11) and stadium files |
| V-3 | Player country IDs exist in `countries.txt` |
| V-4 | Player IDs globally unique (once scheme adopted) |
| V-5 | FA Cup participant IDs ⊆ `clubs.txt` |
| V-6 | `competition_rules.txt` parses to valid enums |
| V-7 | Season scripts reference valid IDs |
| V-8 | No duplicate club IDs after multi-season load |
| V-9 | Modifier `countryId` references valid countries |

---

## 10. Loader pipeline (reference)

```
DatabaseLoader.getInstance()
  → initializeSeasons()          // seasons.txt
  → CompetitionsLoader
  → SeasonsLoader.load()       // countries, clubs, players, stadiums per season
  → ScriptsLoader.load()       // HARDCODED today
  → PlayerProfessionsLoader    // HARDCODED season 18 path
  → NationalityModifiersLoader // HARDCODED season 18 path
  → RegionModifiersLoader      // HARDCODED season 18 path
```

**New:** `CompetitionRulesLoader` after `SeasonsLoader` or inside it.

---

## 11. Implementation order

```
1. Document player ID scheme (WD-9)
2. competition_rules.txt for season 18 + CompetitionRulesLoader (WD-1, WD-2)
3. Fix region_modifiers countryId (WD-7)
4. Dynamic modifier paths (WD-3)
5. ClubsLoader merge-by-ID + ScriptsLoader refactor (WD-4, WD-5)
6. Author season 19 content (WD-6)
7. Validation test suite (WD-8)
```

---

## 12. Dependencies

| To department | Provides |
|---------------|----------|
| **Seasons & Rules** | Rules files, script file format, season 19 scripts |
| **Match & Simulation** | Valid squads (≥11 players), stadium capacity |
| **Platform & UX** | Validation tests in CI |

---

## 13. Sign-off checklist (expert)

- [ ] Season 18 rules file loads; 2-1-0 + goal average
- [ ] Season 19 folder complete; `seasons.txt` row added
- [ ] Adding season 19 does not duplicate clubs in DB
- [ ] Season 19 does not attach 1888 league-creation script
- [ ] Validation tests V-1–V-9 pass in CI
- [ ] Wolves stadium updated for 1889 in season 19
