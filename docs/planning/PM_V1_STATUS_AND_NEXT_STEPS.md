# PM Report — v1 Status, Test Readiness & Next Steps

**Document type:** PM status report  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, Tech lead, QA  
**PO direction:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) · **Eagle view:** [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md)

---

## 1. Executive summary

**Direction:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) — **no public release**; **Layer B now**; long-term CM+FM completeness.

| Question | Answer |
|----------|--------|
| **Are we shipping v1?** | **No** — local development until the team is satisfied. |
| **What are we building now?** | **Whole Layer B** (~42%) — B3/B4/B5 + test pyramid; human QA last ([PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)) |
| **Is the season engine solid?** | **Yes** — league + cup E2E green; 100/100 PR suite (2026-06-30). |
| **Long-term goal?** | Full **CM + FM** historical feature set — years of work ahead. |
| **60-season soak?** | Optional confidence check — preflight first ([SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md)). |

---

## 2. What landed recently (cup + QA)

| Area | Deliverable | Status |
|------|-------------|--------|
| **Cup scheduling** | Generic `CupBracketGenerator`, single edition path, deferred staging draws | ✅ |
| **Health gates** | `SimulationHealthMonitor`, `HarnessCriticalLogCounter` | ✅ |
| **Log tiers** | default quiet / `-Pnightly` / `-Psoak` verbose | ✅ |
| **Test isolation** | Harness resets shared `DatabaseLoader` club schedules between runs | ✅ |
| **Data audit** | 32 FA Cup participants verified vs Wikipedia | ✅ |

---

## 3. Test matrix (verified 2026-06-30)

| Tier | Command | Count | Key E2E | Result |
|------|---------|-------|---------|--------|
| **PR (default)** | `mvn test -pl futtoboru-core` | **100** | `LeagueAndCupIT`, `MultiSeasonSoakIT` (3 league seasons), `SeasonPlaythroughIT`, `SaveLoadIT` | ✅ **Green** |
| **Nightly** | `mvn test -pl futtoboru-core -Pnightly` | +soak-tagged | `MultiSeasonLeagueAndCupSoakIT` (2 seasons **league+cup**), `AttendanceModelIT`, `TenSeasonReportIT`, … | ✅ Cup soak green; run full nightly before merge |
| **Soak** | `mvn test -pl futtoboru-core -Psoak` | 60-season north star | `LongRunSoakIT` (**league only**, cups disabled) | ✅ Proven historically ~5 min; **re-run after merge** |

### E2E coverage map

| Test | Proves | Cup? |
|------|--------|------|
| `LeagueAndCupIT` | One full 1888–89 league + FA Cup, champions, **health clean** | ✅ |
| `MultiSeasonSoakIT` | 3 procedural **league** seasons + economy + health | ❌ |
| `MultiSeasonLeagueAndCupSoakIT` | 2 seasons **league + cup** + health | ✅ (nightly) |
| `LongRunSoakIT` | 60 league champions, timing | ❌ (by design) |
| `SaveLoadIT` | JSON roundtrip mid-season + post-rollover | — |

**Gap:** No automated **60-season league+cup** soak. North-star soak is league-only to bound runtime. Cup multi-season is covered at 2 seasons nightly.

---

## 4. Long-run soak — how to run safely

**Read first:** [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) (post-mortem: 2026-06-30 aborted 41-min run).

```powershell
# 1) Preflight ONLY (~60–90s) — run this before any soak
.\scripts\preflight-before-soak.ps1

# 2) Full north star (preflight + 60 seasons, quiet, ~5 min soak after preflight passes)
.\scripts\run-soak-with-preflight.ps1
```

Manual:

```bash
# Preflight (required)
mvn test -pl futtoboru-core -Dtest=LeagueAndCupIT,MultiSeasonSoakIT -Dfuttoboru.test.logProfile=quiet
mvn test -pl futtoboru-core -Pnightly -Dtest=MultiSeasonLeagueAndCupSoakIT -Dfuttoboru.test.logProfile=quiet

# Soak (only if preflight green) — quiet ~5 min
mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT -Dfuttoboru.test.logProfile=quiet
```

**Do not** pipe soak output through `Select-String` or similar — logs are lost.  
**Do not** use `-Psoak` with verbose logs for timing unless actively debugging (40+ min).

---

## 5. What is still missing (honest backlog)

### Engine / simulation (P0–P1)

| ID | Gap | Impact |
|----|-----|--------|
| **SOAK-CUP-1** | No 10–60 season **league+cup** automated soak | Cup regressions only caught at nightly (2 seasons) |
| **SCHED-1** | Duplicate match IDs on club lists (report-only) | Noise in health reports; not a gate yet |
| **QUAL-1** | FA Cup qualifying rounds not modelled | Historically correct for first round proper; future feature |
| **CUP-UI** | Cup draw / bracket UI incomplete | Headless path OK; player-facing WIP |

### UI bugs (fix when blocking Layer B — from [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md))

| ID | Gap |
|----|-----|
| **C-1** | Continue double-click advances 2 days |
| **C-3 / C-4** | Load game / JobManager stale save |
| **H-3** | Match history navigation |
| **H-9** | Top menu Load/Settings/Quit |
| **Manual QA** | [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) — useful when playing locally; **not a release gate** |

### Data (low urgency for v1)

| Item | Notes |
|------|-------|
| Qualifying clubs (Warwick County, Ulster, …) | Only if we model qualifying |
| Display names (Witton, Walsall Town Swifts, Sheffield Heeley) | Cosmetic |
| Wales / second country | Commented out in mod |

---

## 6. Recommended next steps (priority order)

**Master plan:** [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md) (~42% — whole layer including B5)

### Now — finish whole Layer B (automated first)

1. **B3** — Ratings engine + preview flow + result UI  
2. **B4 + B1 UI** — Tactics + manual XI  
3. **B5** — Player condition (CM1 fitness/morale + CM93 basic injuries)  
4. **L2** — `Cm1ManagementLoopIT`, `MatchFeedbackIT`, condition ITs  
5. **L3** — `LayerBMultiSeasonSoakIT` + preflight `LongRunSoakIT`  
6. **L4** — Human testers → B-9 checklist ([LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md))

---

## 7. Risk register

| Risk | Mitigation |
|------|------------|
| Long soak started without green E2E | **Preflight script** — [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) |
| Soak verbose logs → 40+ min wasted | `-Psoak` defaults **quiet**; verbose only for debug |
| Soak stdout piped / no log artifact | Do not filter; see protocol §4 |
| Shared `DatabaseLoader` clubs mutated across tests | ✅ Harness reset in `newGameEmployedAtClub` (2026-06-30) |
| Cup replay counted as extra semi-final | ✅ Health monitor excludes `parentMatch1Id` replays |
| 60-season soak hides cup bugs | Nightly `MultiSeasonLeagueAndCupSoakIT`; plan SOAK-CUP-1 |
| Probabilistic attendance IT flakes | Moved to `@Tag("nightly")`; unit tests cover formula |

---

## 8. Related documents

- [SIMULATION_TEST_SUITE_V2.md](SIMULATION_TEST_SUITE_V2.md)
- [DESIGN_KNOCKOUT_BRACKET_V1.md](DESIGN_KNOCKOUT_BRACKET_V1.md)
- [ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md](ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md)
- [PM_60_SEASON_GOAL_STATUS.md](PM_60_SEASON_GOAL_STATUS.md)
- [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md)
