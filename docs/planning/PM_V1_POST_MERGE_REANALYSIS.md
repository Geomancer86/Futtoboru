# PM — v1 Post-Merge Reanalysis

**Date:** 2026-06-26  
**Branch:** `analyze-ai-branch` (merged `origin/feature/competition-completion-v1` @ `37a2f72` on top of `origin/develop` @ `8fe6f7e`)  
**Merge commit:** `3497d4b`  
**Audience:** PM, PO, QA, Tech lead

---

## 1. Executive summary

We merged **~151 commits** of competition-completion work from another developer machine into our integration branch **without dropping Phase 0 testing**. After merge fixes:

| Check | Result |
|-------|--------|
| `mvn clean test` | **BUILD SUCCESS** |
| Core tests | **49** (was 44 pre-merge; +5 `MandatoryGateIT` now in Surefire) |
| Phase 0 harness | **Preserved** — `GameTestHarness`, `MandatoryGateIT`, CI workflow, `filtering=false` |
| Other dev unit tests | **Preserved** — `LeagueFixtureGeneratorTest`, `CupBracketGeneratorTest` (×2 packages) |

**v1 readiness moved from ~35% → ~55%** on acceptance criteria: FA Cup scaffolding, league/cup completion wiring, draw gating, and schedule unit tests landed. **Season 19+ data, full rollover soak, and `SeasonPlaythroughIT` remain open.**

---

## 2. What was merged (game features)

### League & season completion

- `AuthorityManager.checkLeagueCompletion()` → `LeagueStandingsManager.isLeagueComplete()` → champion/runner-up messages and prizes
- `checkNewSeasonGeneration()` / `generateNextSeason()` — partial rollover (triggers after league completion on feature branch)
- League fixture generation at league-creation script time (`ScriptsManager` STEP 2)

### FA Cup / cup system

- `CupBracketGenerator`, `CupBracketManager`, cup UI (`CupDrawScreenTable`, `CupDetailScreenTable`, …)
- FA Cup creation script in `ScriptsLoader` (clubs 13–35 in season 18 mod)
- Mandatory `CUP_DRAW` messages and draw screens
- Cup replays, round progression, semi-finals count validation

### Mandatory draw gating (engine)

- `FuttoboruGameEngine.getNextGameAction()` blocks on `LEAGUE_DRAW`, `CUP_DRAW`, `FIXTURE_DRAW`
- `continueGame()` **now blocks** when mandatory draw unread (deliver scheduled messages first, then block)
- Match simulation skipped while mandatory draws pending

### Other

- `DebugLogManager` — categorized engine logging
- Match engine debug UI (`match/engine/`)
- Extensive design/analysis docs from other dev (`AUTOMATED_TESTING_SOLUTION.md`, `TESTING_COMPETITION_COMPLETION.md`, …)

---

## 3. What we preserved (Phase 0 + planning)

| Asset | Path | Status |
|-------|------|--------|
| Headless harness | `futtoboru-core/.../harness/GameTestHarness.java` | ✅ + post-merge isolation fixes |
| Integration tests | `MandatoryGateIT.java` (5), `GameTestHarnessTest.java` (3) | ✅ Green |
| CI | `.github/workflows/maven-test.yml` | ✅ |
| Maven install script | `scripts/install-maven-windows.ps1` | ✅ |
| Resource filtering fix | `futtoboru-core/pom.xml` `filtering=false` | ✅ Kept through merge |
| `refreshCurrentGame()` | `AuthorityManager.java` | ✅ Still present |
| v1 plan / PO decisions | `V1_TESTING_AND_SIMULATION_PLAN.md`, `RELEASE_V1_DEFINITION.md` | ✅ Updated references |

---

## 4. Post-merge test fixes (this session)

### 4.1 Surefire did not run `*IT.java`

Maven Surefire default excludes `MandatoryGateIT`. Added explicit includes in `futtoboru-core/pom.xml`:

- `**/*Test.java`, `**/*Tests.java`, `**/*IT.java`

### 4.2 Harness test isolation

`DatabaseLoader` shares **one** `Authority` and **mutable** `Club` instances across saves. Phase 0 tests passed in isolation but failed in suite order.

**Fixes in `GameTestHarness.newGameEmployedAtClub()`:**

1. Reset `mainAuthority.setLeagues(new ArrayList<>())` per run  
2. **Deep-copy** season scripts (`isExecuted=false`) instead of `addAll(season.getSeasonScripts())`  
3. Clear `club.getScheduledMatches()` on shared club objects before each run  

### 4.3 `MandatoryGateIT` aligned with merged engine

- Renamed `rawContinueStillAdvancesWhileGateUnread` → `rawContinueDoesNotAdvanceWhileGateUnread` (engine now correctly blocks `continueGame()` on unread draws)
- Draw gate detection accepts `LEAGUE_DRAW`, `CUP_DRAW`, `FIXTURE_DRAW`
- Longer wait window (30 days) and uses `getNextGameAction() == DRAW_ACTION` as gate signal

---

## 5. Test inventory after merge

| Class | Tests | Layer |
|-------|-------|-------|
| `MandatoryGateIT` | 5 | E2E headless — mandatory draw gates |
| `GameTestHarnessTest` | 3 | Harness smoke |
| `LeagueFixtureGeneratorTest` | 5 | Unit — league schedule |
| `CupBracketGeneratorTest` (temporal) | 6 | Unit — bracket structure |
| `CupBracketGeneratorTest` (cup) | 6 | Unit — duplicate coverage |
| Other loaders / system tests | 24 | Unit |
| **Total core** | **49** | |
| `BasicTests` (desktop) | 1 | |

**Complementary (other dev, not duplicated by harness):** see [AUTOMATED_TESTING_SOLUTION.md](AUTOMATED_TESTING_SOLUTION.md) and [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) for manual QA checklists.

---

## 6. Phase roadmap — revised status

| Phase | Plan | Post-merge reality |
|-------|------|-------------------|
| **0** Test infra | ✅ Done | ✅ **49 tests green**, CI, harness |
| **1** Unblock simulation | Pending | **~60%** — draw gating wired; C-2 refresh done; league completion wired |
| **2** Season loop | Pending | **~30%** — partial `generateNextSeason()`; need `SeasonManager` + ITs |
| **3** FA Cup in v1 | Pending | **~50%** — cup create/draw/bracket/sim path landed; needs `LeagueAndCupIT` |
| **4** Soak & ship | Pending | **0%** — `LongRunSoakIT` not started; 60-year sim still **impossible** |

---

## 7. Still missing for v1 / north star

| Gap | Impact |
|-----|--------|
| `SeasonManager` + full rollover (stat reset, cup sync) | Multi-season loop untrusted |
| `SeasonPlaythroughIT` / `MultiSeasonSoakIT` / `LongRunSoakIT` | **60-season goal unproven** |
| Start-mod `competition_rules.txt` | Wrong 3-1-0 rules |
| In-season timeline script types (1002+) | FM-style mid-era events deferred |

**60-season champion list:** still **NO** — procedural rollover incomplete; no soak test. See [PM_60_SEASON_GOAL_STATUS.md](PM_60_SEASON_GOAL_STATUS.md).

**Not a v1 blocker:** `mods/seasons/19/` or other future-year mod folders (PO 2026-06-26).

---

## 8. PO decisions (unchanged)

| Topic | v1 |
|-------|-----|
| FA Cup | In v1 with league |
| Unemployed start | v1.1 |
| Regens / retirement | None (static squads) |
| Nov 1888 / re-election | Defer |
| Long soak | Post-v1; target ≥3 seasons first |

---

## 9. Cleanup candidates (non-blocking)

| Item | Action |
|------|--------|
| `futtoboru-core/temp_develop.java` | Accidental binary blob — delete when confirmed |
| Duplicate `CupBracketGeneratorTest` in two packages | Both pass; consolidate later if desired |
| Production `NewGameOverviewScreen` shared `Authority` | Consider fresh leagues list per save |

---

## 10. Recommended next steps

1. **`SeasonPlaythroughIT`** — one season league (+ cup) using harness + `acknowledgeMandatoryDraws()`  
2. **Start-mod rules file** — not season 19 folder  
3. **`MultiSeasonSoakIT` (3 seasons)** — PR gate  
4. **`LongRunSoakIT` (60 seasons)** — north star; nightly profile  
4. **Human QA** — [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) checklist after every `mvn test` green  

---

## 11. Commands

```bash
mvn clean test
```

Windows Maven (if not on PATH): [scripts/README.md](scripts/README.md)

---

*Cross-links: [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) · [PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md](PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md) · [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md)*
