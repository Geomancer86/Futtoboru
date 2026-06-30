# Department: World & Data — v1 Deep Dive

**Department expert:** _________________  
**Status:** Start season mod 18 ~**85%** content (556 players, 35 clubs); **procedural seasons 2+ are engine-owned**  
**North star:** [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) · **PM plan:** [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md)

**Season model:** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) — **no** `mods/seasons/19/` required for v1 proof; additional rows in `seasons.txt` = **new pickable start years**, not simulated years.

---

## 1. Mission (v1)

Own **start-season mod data** and **loaders** so the picked entry point (1888–89) has correct clubs, squads, stadiums, **competition rules**, and **in-season timeline scripts**. **Simulated years 2+** are not separate mod folders — the engine rolls forward from save state.

---

## 2. Season 18 inventory (verified 2026-06-27)

| Asset | Count / status |
|-------|----------------|
| `mods/seasons.txt` | 1 row (id `18`) |
| `clubs.txt` | **35** clubs (12 league + 23 cup/other) — all with `foundation_year` |
| `club_players/` | **35 files**, **556** named players total |
| `club_stadiums/` | **35 files** |
| `competition_rules.txt` | ✅ Present — loaded by `CompetitionRulesLoader` |
| `scripts/` (filesystem) | ❌ Missing — hardcoded in `ScriptsLoader.java` |
| `leagues.txt` | Stub — **never loaded** |
| FA Cup edition | **32** first-round-proper participants in `competitions/1/` — audited vs Wikipedia ([ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md](ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md), 2026-06-30) |
| Modifiers / professions | ✅ Present |
| Playable countries in season | **1** (England `1000`; Wales `1002` commented out) |

**League squad sizes (ids 1–12):** 18–35 players · **Cup-only clubs:** often 11 players (minimum placeholder).

---

## 3. Additional start seasons (post–v1 — not procedural years)

**PO decision:** Simulated year 2+ does **not** need `mods/seasons/19/`. A **new folder** is only for **another pickable career start** (e.g. “begin in 1899–1900”).

Example minimum package for **one** new start id:

```
mods/seasons.txt                    → add row (e.g. id 25 = 1899–1900)
mods/seasons/25/countries.txt
mods/seasons/25/clubs.txt
mods/seasons/25/competition_rules.txt
mods/seasons/25/club_players/*.txt
mods/seasons/25/club_stadiums/*.txt
mods/seasons/25/scripts/           → league/cup timeline (not league re-foundation if already exists)
mods/seasons/25/competitions/
```

**Prerequisite:** DB-1–DB-5 in [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) before authoring.

**Recommended first content mods:** see [PM_HISTORICAL_DATA_EXPANSION_REPORT.md §6](PM_HISTORICAL_DATA_EXPANSION_REPORT.md).

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

> **Note:** Example only — a **1899–1900 start mod** would ship its own rules block; simulated year 2+ does **not** require a new mod folder ([DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)).

**Loader:** New `CompetitionRulesLoader.load(Season)` → `CompetitionRules` + optional amendments list.

---

## 6. Player ID strategy (decide before second start mod)

| Option | Scheme | Effort |
|--------|--------|--------|
| **A** | `{clubId * 1000 + localId}` | Medium migration |
| **B** | Scope all lookups by `(clubId, playerId)` | Code-wide |
| **C** | Delta-only overlay (reuse save-state players) | Requires rollover code |

**Recommend Option A** — document in player file headers.

---

## 7. Multi-season club loading (decide before second start mod row)

| Option | Description |
|--------|-------------|
| **A** | Second start mod omits `clubs.txt`; rollover reuses save clubs |
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
| WD-6 | **Second start mod** (PO picks era) | Loads without error; passes V-1–V-9 |
| WD-7 | Fix `region_modifiers` countryId | Lancashire = 1000 |
| WD-8 | Data validation tests V-1–V-9 | CI gate |
| WD-9 | Player ID scheme documented + applied | No global collisions |

### 8.1 In-play world evolution (Wave 1.5)

After DB-1–DB-2; before bulk new start mods. See [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md).

| ID | Deliverable | Acceptance |
|----|-------------|------------|
| WE-1 | `WorldEvolutionManager` — incorporate club/person/rules into save | Single API; no duplicate IDs |
| WE-2 | `first_active_date` in mod schema + loader | Dormant clubs not in save at new game |
| WE-3 | New game lazy club load (active on start date only) | Save size matches era |
| WE-4 | Script types 1002–1007 on disk | Nov 1888 rules OR 1892 club test |
| WE-5 | Rollover uses current league membership from save | Not frozen participant copy |
| WE-6 | No `DatabaseLoader` lookups during `continueGame()` | Save-as-truth enforced |
| WE-7 | Save schema version for mid-career entity adds | Load old saves |
| WE-8 | `WorldEvolutionIT` | Script date → entity in save |

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
| V-10 | `foundation_year` / `first_active_date` consistent |
| V-11 | Global IDs unique across dormant + active sets |
| V-12 | Post-start activations have script or auto date |
| V-13 | Simulation: start earliest → date X → entity in save |

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
6. Author **second start mod** when DB-1–DB-5 done (WD-6)
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
- [ ] Season 18 passes validation V-1–V-9 in CI
- [ ] Second start mod (when PO selects era) loads without duplicate club IDs
- [ ] Second start mod does not attach 1888-only league-creation script inappropriately
