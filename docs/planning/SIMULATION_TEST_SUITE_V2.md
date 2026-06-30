# Simulation Test Suite v2 — Design

**Document type:** QA architecture / remediation plan  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Trigger:** Long-run soak runs emitted thousands of CRITICAL logs (null cup teams, cup semi-final bracket errors) while tests still passed — **false green**.

---

## 1. Problem statement

| Symptom | Why tests passed anyway |
|---------|-------------------------|
| `MatchScheduler` CRITICAL: null HomeClubId/AwayClubId | Cup TBD placeholders hit ticket-sales path every day × every club |
| `CupBracketGenerator` semi-finals ≠ 2 matches | No bracket-shape assertion in ITs; only power-of-2 unit tests |
| Soak runs take 30+ minutes with log spam | CPU wasted on error logging loops; looks like “still simulating” |
| `LeagueAndCupIT` flaky deadlock | Cup bracket incomplete in some paths; 1200-day cap hit |
| Multi-season soaks disable FA Cup | `setAllCups(empty)` hides cup bugs |

**Root cause:** Gates checked **champion count + economy only**. They did not check **simulation health** (schedule integrity, bracket shape, CRITICAL logs).

---

## 2. New health layer

### 2.1 `SimulationHealthMonitor` (save-state scan)

Location: `engine/health/SimulationHealthMonitor.java`

Scans at season boundaries and end of harness runs:

| Check | Fail when |
|-------|-----------|
| TBD matches in 7-day ticket window | `homeClubId` or `awayClubId` null and match date within window |
| Duplicate match IDs across clubs | Same `match.id` on multiple club schedules |
| Cup semi-final round | Penultimate round has ≠ 2 matches (deduped cup fixtures, excludes replay suffix `R`, active editions only) |

### 2.2 `HarnessCriticalLogCounter` (log gate)

Location: `test/.../harness/HarnessCriticalLogCounter.java`

- Hooks `DebugLogManager.error()` via `setTestErrorListener`
- Counts messages containing `CRITICAL`
- `GameTestHarness.assertSimulationHealthy()` fails if count > 0

### 2.3 MatchScheduler fix (H-10)

Skip cup TBD placeholders **before** ticket sales (align with `MatchSimulator` / match-day checks). Downgrades unknown-id cases from CRITICAL to WARN.

---

## 3. Test tier model

```
┌─────────────────────────────────────────────────────────────┐
│  fast (default mvn test)     ~2 min   PR gate              │
│  unit + 1-season ITs + MultiSeasonSoakIT (3 league seasons)│
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  nightly (-Pnightly)         ~15–25 min   pre-merge optional│
│  TenSeasonReportIT · LongRunEconomySoakIT                    │
│  MultiSeasonLeagueAndCupSoakIT (2 seasons league+cup)      │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│  soak (-Psoak)               60-season north star           │
│  LongRunSoakIT (league only, health + economy at end)      │
└─────────────────────────────────────────────────────────────┘
```

**Maven profiles** (`futtoboru-core/pom.xml`):

| Profile | Groups | Excluded from default |
|---------|--------|----------------------|
| default | — | `soak`, `nightly` |
| `-Pnightly` | `nightly` | — |
| `-Psoak` | `soak` | — |

---

## 4. Test catalog (v2)

### Fast gate (every commit)

| Test | Proves |
|------|--------|
| `CupBracketGeneratorTest` | Bracket shape 8/16/32 teams; semi-finals always 2 |
| `SimulationHealthMonitorTest` | Health scan detects TBD + bad semi-finals |
| `LeagueAndCupIT` | 1 season league+cup complete + **health clean** |
| `MultiSeasonSoakIT` | 3 procedural league champions + economy + **health** |
| All existing unit/IT | Regression |

### Nightly

| Test | Proves |
|------|--------|
| `TenSeasonReportIT` | 10 league seasons, report, economy, health |
| `LongRunEconomySoakIT` | 3 seasons economy gate |
| `MultiSeasonLeagueAndCupSoakIT` | **2 seasons league+cup** — cup champion + bracket health |

### Soak

| Test | Proves |
|------|--------|
| `LongRunSoakIT` | 60 league seasons; timing; health at end |

---

## 5. Known engine defects (tracked, not hidden)

| ID | Issue | Status |
|----|-------|--------|
| **CUP-B1** | Duplicate draw / replay inflated semi-final counts | **Fixed** 2026-06-30 |
| **CUP-B2** | Cup fixtures not archived on rollover | **Fixed** 2026-06-30 |
| **H-10** | TBD cup matches in ticket-sales path | **Fixed** |
| **TEST-1** | Soaks passed despite CRITICAL spam | **Fixed** — health + log gates |
| **TEST-2** | Harness shared club state polluted ITs | **Fixed** 2026-06-30 — reset schedules + stats in `newGameEmployedAtClub` |
| **SOAK-CUP-1** | No 60-season league+cup soak | Tracked — nightly 2-season cup soak covers regression |

---

## 6. Harness API (v2)

| Method | Purpose |
|--------|---------|
| `scanSimulationHealth()` | Run `SimulationHealthMonitor.scan(game())` |
| `assertSimulationHealthy()` | Zero CRITICAL logs + clean health report |
| `MultiSeasonSimulationReport.recordHealthScan()` | Report markdown includes health issues |

All `simulateWithReport`, `simulateUntilPrimaryLeagueComplete`, and `simulateUntilLeagueAndCupComplete` call health gates on success.

---

## 7. Success criteria

1. **Fast gate** completes in **< 3 minutes** on dev machine with **zero CRITICAL** lines in stderr for league-only 3-season run.
2. **Nightly** league+cup 2-season test passes or fails with **actionable assertion** (not silent log spam).
3. **Soak** 60-season run completes in bounded time once log spam removed (target: minutes not tens of minutes — measure after H-10).
4. No orphaned `surefirebooter` Java processes after aborted runs (kill Maven child on cancel).

---

## 8. Next engine work (outside test suite)

1. Fix `CupBracketGenerator` for 12 entrants (bye model rewrite).
2. Archive cup fixtures in `SeasonManager` on edition complete / rollover.
3. Capture `System.err` bracket CRITICAL lines in harness (or route through `DebugLogManager`).
4. Implement save/load roundtrip every N seasons in soak (V1 plan §6.2 `SaveLoadIT` extension).

---

*Parent:* [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) · [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)
