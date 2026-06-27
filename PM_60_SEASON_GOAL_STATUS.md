# PM Report — Distance to the 60-Season Goal

**Document type:** PM status / north-star tracking  
**Date:** 2026-06-26 (updated after WP-1–6 implementation)  
**Branch:** `analyze-ai-branch`  
**Audience:** PM, PO, Sponsor, Tech lead, QA

**Season model (PO locked):** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)

---

## 1. What “60 seasons” means

| | |
|--|--|
| **Goal** | A single save simulates **60 in-game competition years** procedurally — list **60 league champions** in automated soak — not 60 mod folders. |
| **PR gate (PO)** | `MultiSeasonSoakIT` — **3 procedural seasons** green on every `mvn test`. |
| **North star** | `LongRunSoakIT` — **60 seasons** (nightly `-Psoak` profile, not yet implemented). |
| **v1 ship bar** | One full season + **3-season soak proven** + manual QA checklist. |

---

## 2. Executive scorecard (updated)

| Dimension | Target | Current | % of 60-season goal |
|-----------|--------|---------|---------------------|
| **Test infra + CI** | Harness, 55+ tests | **55 tests green**, CI | **100%** Tier 0 |
| **Single season E2E** | `SeasonPlaythroughIT` | ✅ **2 tests green** (~8s) | **~75%** |
| **3-season soak E2E** | `MultiSeasonSoakIT` | ✅ **1 test green** (~8s) | **~25%** of 60 (3/60) |
| **60-season soak** | `LongRunSoakIT` | ❌ Not implemented | **0%** |
| **SeasonManager / rollover** | Procedural loop | ✅ Implemented + fixes | **~70%** code |
| **Rules from start mod** | `competition_rules.txt` | ✅ Loaded for season 18 | **~80%** |
| **RB-7 friendlies** | League-only completion | ✅ Fixed + unit tests | **100%** |

### Overall distance to 60-season goal

```
Proven by automated tests:     3 / 60 seasons  ≈  5% of north star
PR gate (3-season soak):       ✅ MET
v1 procedural loop:            ✅ MET (code + E2E)
Remaining to north star:       LongRunSoakIT (57 seasons) + nightly CI profile
```

**Plain language:** We now **prove procedurally** that a headless save runs **three consecutive league years** with three champion editions — without any future-year mod folders. The **60-season** cert is the next soak milestone, not new content mods.

---

## 3. What landed in this development pass

| WP | Deliverable | Status |
|----|-------------|--------|
| **WP-1** | RB-7 — league-only stats & completion (`MatchSimulator`, `LeagueStandingsManager`) | ✅ |
| **WP-2** | `mods/seasons/18/competition_rules.txt` + `CompetitionRulesLoader` | ✅ |
| **WP-3** | `SeasonManager` + `SaveGame.startSeasonId` / `simulatedSeasonIndex` | ✅ |
| **WP-4** | Rollover: stat reset, archive fixtures, rules snapshot, edition completion fix | ✅ |
| **WP-5** | `SeasonPlaythroughIT` (1 season + rollover hop) | ✅ |
| **WP-6** | `MultiSeasonSoakIT` (3 champions) | ✅ |

### Production fixes (multi-season)

| Fix | Impact |
|-----|--------|
| `isLeagueAlreadyCompleted()` uses **edition `championsId`**, not inbox message | Season 2+ can complete |
| `LeagueFixtureGenerator` reads **live** `SaveGame` | Rollover fixture gen works in tests |
| Cup `CUP_DRAW` delivery in harness | Simulation not blocked after May 1888 |

---

## 4. Test inventory (55 core + 1 desktop)

| Tier | Class | Tests | Status |
|------|-------|-------|--------|
| Unit | `LeagueStandingsManagerTest` | 2 | ✅ |
| Unit | `CompetitionRulesLoaderTest` | 1 | ✅ |
| E2E | `MandatoryGateIT` | 5 | ✅ |
| E2E | `SeasonPlaythroughIT` | 2 | ✅ |
| E2E | `MultiSeasonSoakIT` | 1 | ✅ **PR gate** |
| Smoke | `GameTestHarnessTest` | 3 | ✅ |
| Other unit/loaders | … | 41 | ✅ |

**Command:** `mvn clean test` → **BUILD SUCCESS** (~26s)

---

## 5. Remaining work to 60 seasons

| ID | Work | Unlocks |
|----|------|---------|
| **WP-7** | `LongRunSoakIT` — 60 champion editions; assert champion list size | **60-season goal** |
| **WP-7b** | Maven profile `soak` — nightly only; timeout budget | CI hygiene |
| **WP-8** | Cup in season-complete gate (optional PO) | Full v1 league+cup loop IT |
| **WP-9** | `SaveLoadIT` mid-season + post-rollover | PL-7 |
| **WP-10** | Production `NewGameOverviewScreen` shared Authority reset | C-6 |

---

## 6. v1 release vs 60-season goal

| Milestone | Status |
|-----------|--------|
| **3-season procedural soak (PR gate)** | ✅ **MET** |
| **1-season + rollover E2E** | ✅ **MET** |
| **v1 shippable loop (league + manual cup QA)** | ⚠️ Close — cup E2E still open |
| **60-season north star** | ❌ **5% proven** (3/60) — `LongRunSoakIT` pending |

---

## 7. Recommended next steps

1. Add **`LongRunSoakIT`** with `@Tag("soak")` / `-Psoak` (do not run on every PR — ~minutes runtime).  
2. **`LeagueAndCupIT`** — full 1888–89 with FA Cup in same harness.  
3. Manual QA: [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md).  

---

*Implementation commit on `analyze-ai-branch` · Design: [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)*
