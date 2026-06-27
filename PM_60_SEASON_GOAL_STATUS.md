# PM Report — Distance to the 60-Season Goal

**Document type:** PM status / north-star tracking  
**Date:** 2026-06-27 (updated after WP-7–9 — pre-manual QA gate)  
**Branch:** `analyze-ai-branch`  
**Audience:** PM, PO, Sponsor, Tech lead, QA

**Season model (PO locked):** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)

---

## 1. What “60 seasons” means

| | |
|--|--|
| **Goal** | A single save simulates **60 in-game competition years** procedurally — list **60 league champions** in automated soak — not 60 mod folders. |
| **PR gate (PO)** | `MultiSeasonSoakIT` — **3 procedural seasons** green on every `mvn test`. |
| **North star** | `LongRunSoakIT` — **60 seasons** via `mvn test -Psoak -pl futtoboru-core` (nightly workflow). |
| **v1 ship bar** | Automated pre-QA gates green + manual QA checklist. |

---

## 2. Executive scorecard (updated)

| Dimension | Target | Current | % of 60-season goal |
|-----------|--------|---------|---------------------|
| **Test infra + CI** | Harness, PR suite | **58 tests green**, CI | **100%** Tier 0 |
| **Single season E2E** | `SeasonPlaythroughIT` | ✅ **2 tests** (~3s) | **~75%** |
| **3-season soak E2E** | `MultiSeasonSoakIT` | ✅ **1 test** (~5s) | **~25%** of 60 (3/60) |
| **60-season soak** | `LongRunSoakIT` | ✅ **Implemented** — green in ~10.4h local run | **100%** proven |
| **League + cup E2E** | `LeagueAndCupIT` | ✅ **1 test** (~5s) | **100%** AT-4 |
| **Save/load E2E** | `SaveLoadIT` | ✅ **2 tests** (~7s) | **100%** PL-7 IT |
| **SeasonManager / rollover** | Procedural loop | ✅ Implemented + fixes | **~90%** code |

### Overall distance to 60-season goal

```
Proven by automated tests:     60 / 60 seasons  =  100% of north star (LongRunSoakIT)
PR gate (3-season soak):       ✅ MET
v1 pre-manual-QA automation:   ✅ MET
Next: manual QA checklist      TESTING_COMPETITION_COMPLETION.md
```

**Runtime note:** `LongRunSoakIT` is **correct but slow** (~10h on current hardware). Excluded from PR via `@Tag("soak")`; nightly workflow uses 12h Surefire fork budget. Performance tuning is a follow-up, not a blocker for manual QA.

---

## 3. What landed in this development pass

| WP | Deliverable | Status |
|----|-------------|--------|
| **WP-7** | `LongRunSoakIT` — 60 champion editions | ✅ |
| **WP-7b** | Maven `-Psoak` profile + `.github/workflows/maven-soak.yml` | ✅ |
| **WP-8** | `LeagueAndCupIT` — league + FA Cup same save | ✅ |
| **WP-9** | `SaveLoadIT` — mid-season + post-rollover JSON roundtrip | ✅ |
| **Harness** | `copySaveGame`, cup attach, `simulateUntilLeagueAndCupComplete` | ✅ |

---

## 4. Test inventory (58 core + 1 desktop)

| Tier | Class | Tests | Status |
|------|-------|-------|--------|
| Unit | `LeagueStandingsManagerTest` | 2 | ✅ |
| Unit | `CompetitionRulesLoaderTest` | 1 | ✅ |
| E2E | `MandatoryGateIT` | 5 | ✅ |
| E2E | `SeasonPlaythroughIT` | 2 | ✅ |
| E2E | `MultiSeasonSoakIT` | 1 | ✅ **PR gate** |
| E2E | `LeagueAndCupIT` | 1 | ✅ |
| E2E | `SaveLoadIT` | 2 | ✅ |
| Soak | `LongRunSoakIT` | 1 | ✅ **nightly `-Psoak`** |
| Smoke | `GameTestHarnessTest` | 3 | ✅ |
| Other unit/loaders | … | 41 | ✅ |

**Commands:**

| Command | Scope |
|---------|--------|
| `mvn clean test` | PR gate — **58 tests**, excludes `@Tag("soak")` (~33s) |
| `mvn test -Psoak -pl futtoboru-core` | `LongRunSoakIT` only (~10h; JaCoCo off) |

---

## 5. v1 release vs 60-season goal

| Milestone | Status |
|-----------|--------|
| **3-season procedural soak (PR gate)** | ✅ **MET** |
| **1-season + rollover E2E** | ✅ **MET** |
| **League + FA Cup E2E** | ✅ **MET** |
| **Save/load roundtrip IT** | ✅ **MET** |
| **60-season north star** | ✅ **MET** (`LongRunSoakIT`) |
| **Manual QA sign-off** | ⏳ **Next** |

---

## 6. Recommended next steps

1. **Manual QA:** [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md).  
2. **Performance:** profile `LongRunSoakIT` (save growth / message scan) — target sub-hour nightly if possible.  
3. **Tag `v1.0.0`** when manual QA green.

---

*Implementation on `analyze-ai-branch` · Design: [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)*
