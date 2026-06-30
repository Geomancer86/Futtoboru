# PM Report — v1 Status, Test Readiness & Next Steps

**Document type:** PM status report  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, Tech lead, QA

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **Is anything missing for 1888–89 FA Cup first round?** | **No** — all 32 historical first-round teams are in mod data ([ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md](ANALYSIS_FA_CUP_1888_89_PARTICIPANTS_V1.md)). |
| **Is the main E2E test working?** | **Yes** — `LeagueAndCupIT` completes league + cup with health gates (~3–5s). |
| **Is the PR test suite green?** | **Yes** — `mvn test -pl futtoboru-core` → **100/100** (2026-06-30). |
| **Ready to restart long soaks?** | **Yes, with tiered commands** — see §4. Cup scheduling blockers are fixed; use `-Psoak` for verbose logs. |
| **Is v1 shippable?** | **Not yet** — automation green for core loop; manual QA + UI blockers remain. |

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

```bash
# PR gate (~60s) — quiet logs, CRITICAL fails tests
mvn test -pl futtoboru-core

# Pre-merge / weekly — league+cup 2 seasons, verbose optional
mvn test -pl futtoboru-core -Pnightly

# North star — 60 league seasons, full DebugLogManager verbosity
mvn test -pl futtoboru-core -Psoak
```

**Logging:** Default profile suppresses engine spam; `-Psoak` enables full logs for overnight debugging. Override: `-Dfuttoboru.test.logProfile=verbose`.

**Do not** run unbounded soaks without `-Psoak` only if you need quiet CI-like output; use soak profile when investigating regressions.

---

## 5. What is still missing (honest backlog)

### Engine / simulation (P0–P1)

| ID | Gap | Impact |
|----|-----|--------|
| **SOAK-CUP-1** | No 10–60 season **league+cup** automated soak | Cup regressions only caught at nightly (2 seasons) |
| **SCHED-1** | Duplicate match IDs on club lists (report-only) | Noise in health reports; not a gate yet |
| **QUAL-1** | FA Cup qualifying rounds not modelled | Historically correct for first round proper; future feature |
| **CUP-UI** | Cup draw / bracket UI incomplete | Headless path OK; player-facing WIP |

### Product / v1 ship (from [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md))

| ID | Gap |
|----|-----|
| **C-1** | Continue double-click advances 2 days |
| **C-3 / C-4** | Load game / JobManager stale save |
| **H-3** | Match history navigation |
| **H-9** | Top menu Load/Settings/Quit |
| **Manual QA** | [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) checklist not signed off |

### Data (low urgency for v1)

| Item | Notes |
|------|-------|
| Qualifying clubs (Warwick County, Ulster, …) | Only if we model qualifying |
| Display names (Witton, Walsall Town Swifts, Sheffield Heeley) | Cosmetic |
| Wales / second country | Commented out in mod |

---

## 6. Recommended next steps (priority order)

### Sprint A — Soak & confidence (this week)

1. **Run and record** `mvn test -pl futtoboru-core -Pnightly` and `-Psoak` on CI or locally; archive report.
2. **Add `LongRunLeagueAndCupSoakIT`** (optional `@Tag("soak")`) — 5–10 seasons league+cup with health gates; bridges gap between nightly-2 and north-star-60-league-only.
3. Keep **default profile quiet**; document soak failure triage in [SIMULATION_TEST_SUITE_V2.md](SIMULATION_TEST_SUITE_V2.md).

### Sprint B — v1 playable loop (next)

4. **Manual QA pass** — employed manager, one full 1888–89 season in UI (not just headless).
5. Fix **C-1** (continue double-click) and **C-3/C-4** (load/save job cache).
6. **Cup draw UI** — wire `CupDrawScreenTable` to same path as `AuthorityManager` (no duplicate draw).

### Sprint C — Depth (post–v1)

7. FA Cup qualifying + invited-club scripts.
8. Second start season mod (e.g. 1899–1900) per [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md).
9. Layer B management features (training, transfers) per master plan.

---

## 7. Risk register

| Risk | Mitigation |
|------|------------|
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
