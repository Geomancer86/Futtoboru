# Layer B — Test Plan (Unit + Headless E2E)

**Document type:** QA / engineering test plan  
**Date:** 2026-06-25  
**Branch:** `analyze-ai-branch`  
**Parent:** [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) · [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)

---

## 1. Principles

| Principle | Layer B application |
|-----------|---------------------|
| **No feature without tests** | Each MVP ships with unit test(s) and, where the loop is touched, a harness IT |
| **Headless first** | Same pattern as `LeagueAndCupIT`, `SaveLoadIT` — `GameTestHarness` + `HeadlessApplication` |
| **Regression** | Existing 62+ tests + soak profiles must stay green after each phase |
| **Manual last** | Automated green → manual CM1 walkthrough → **B-9** PO sign-off |

**Commands:**

```bash
mvn test -pl futtoboru-core
mvn test -pl futtoboru-core -Psoak    # after B1 (TenSeasonReportIT sensitive)
```

---

## 2. Test inventory — to implement

### B1 — Sim core

| Class | Type | Proves | Depends on |
|-------|------|--------|------------|
| `FormationRegistryTest` | Unit | 3 templates load; 11 slots each; default `2-3-5-1888` | DATA-B1 |
| `BestElevenSelectorTest` | Unit | Picks 11 by attribute sum; handles <11 squad edge | MVP-1 |
| `ClubTacticsSetupSaveLoadIT` | IT | `ClubTacticsSetup` survives `SaveLoadSystem.copySaveGame` | DATA-B1 |
| `MatchSimulatorV2Test` | Unit | Stronger `MatchLineup` → higher expected goals over N runs | MVP-3 |
| `StartingElevenIT` | IT | Set weak XI vs strong XI on same club fixture → weak loses more | MVP-1, MVP-3 |
| `TenSeasonReportIT` | E2E soak | No deadlock; champion count >1 club after MVP-8 tune | MVP-8 |

**`StartingElevenIT` sketch:**

```java
@Test void weakLineupLosesMoreOftenThanStrongLineup() {
  harness.newGameEmployedAtClub(PNE, SEASON_18);
  // build strong XI = best 11; weak XI = worst 11
  harness.setClubLineup(homeId, weakIds);
  int weakWins = harness.simulateSameFixtureNTimes(50);
  harness.setClubLineup(homeId, strongIds);
  int strongWins = harness.simulateSameFixtureNTimes(50);
  assertTrue(strongWins > weakWins);
}
```

---

### B2 — Crowds

| Class | Type | Proves | Depends on |
|-------|------|--------|------------|
| `AttendanceCalculatorTest` | Unit | positionFactor, opponentFactor, formFactor, derbyFactor, clamp | MVP-4 |
| `AttendanceModelIT` | IT | 1st vs 18th at same stadium → 1st higher attendance | MVP-4, H-1 |
| `AttendanceOverwriteIT` | IT | Post-sim attendance equals calculator (not random re-roll) | H-1 |

---

### B3 — Feedback

| Class | Type | Proves | Depends on |
|-------|------|--------|------------|
| `MatchRatingsTest` | Unit | Scorer > non-scorer; win bonus; clamp 4–9.5 | MVP-5 |
| `MatchFeedbackIT` | IT | Sim match → ratings map size 11; scorers in result data | MVP-5, MVP-6 |
| `MatchLineupPreviewIT` | IT | Preview snapshot ids == sim lineup ids | MVP-7 |

---

### B4 — Tactics UX

| Class | Type | Proves | Depends on |
|-------|------|--------|------------|
| `FormationSimWeightTest` | Unit | Different templates → different strength for same XI | MVP-2 |
| Manual only | Manual | Tactics in sidebar; formation change persists | MVP-9 |

---

## 3. `GameTestHarness` API additions

Implement in `futtoboru-core/src/test/.../harness/GameTestHarness.java`:

| Method | Phase |
|--------|-------|
| `void setClubTacticsSetup(long clubId, String formationId, List<Long> playerIds)` | B1 |
| `ClubTacticsSetup getClubTacticsSetup(long clubId)` | B1 |
| `MatchLineup getLineupUsedInLastSimulation(long clubId)` | B1 |
| `Match simulateNextLeagueMatchForClub(long clubId)` | B1 |
| `int simulateSameFixtureNTimes(int n)` | B1 |
| `int getLastMatchAttendance()` | B2 |
| `Map<Long, Float> getLastMatchRatings(long clubId)` | B3 |
| `void confirmPreviewLineup()` | B3 |

---

## 4. CI / soak impact

| Profile | When to run | Layer B note |
|---------|-------------|--------------|
| Default `mvn test` | Every commit | All unit + IT except long soak |
| `-Psoak` | Weekly / pre-B-9 | `TenSeasonReportIT`, `LongRunSoakIT` after sim v2 |
| PR gate | All new tests in default profile | StartingElevenIT may be slow — cap iterations |

---

## 5. Manual QA checklist (B-9)

Run at **200% UI scale** on Galaxy Book or 1920×1080 after automated suite green.

| # | Step | Pass |
|---|------|------|
| M1 | Load career → Squad → pick 11 (or Best XI) → save persists after reload | ☐ |
| M2 | Tactics sidebar → formation dropdown → slots update | ☐ |
| M3 | Match preview → edit XI → Continue → match sim runs | ☐ |
| M4 | Result screen → score, scorers, attendance, ratings visible | ☐ |
| M5 | Match history → click past match → result (A-4 regression) | ☐ |
| M6 | League table → 2-1-0 points (A-5 regression) | ☐ |
| M7 | Top club home match → attendance visibly higher than early-season low table | ☐ |
| M8 | Continue once per click (A-2 regression) | ☐ |

**B-9 PO sign-off:** all M1–M8 checked.

---

## 6. Traceability matrix

| CM1 loop step | MVP | Unit | IT/E2E | Manual |
|---------------|-----|------|--------|--------|
| Pick XI | MVP-1 | `BestElevenSelectorTest` | `StartingElevenIT` | M1 |
| Formation | MVP-2,9 | `FormationSimWeightTest` | — | M2 |
| Continue | A-2 | — | `MandatoryGateIT` | M8 |
| Match uses 11 | MVP-3 | `MatchSimulatorV2Test` | `StartingElevenIT` | M3 |
| Crowds | MVP-4 | `AttendanceCalculatorTest` | `AttendanceModelIT` | M7 |
| Ratings | MVP-5 | `MatchRatingsTest` | `MatchFeedbackIT` | M4 |
| Result/preview | MVP-6,7 | — | `MatchLineupPreviewIT` | M3, M4, M5 |

---

*Implement tests in the same PR/commit series as each MVP — no untested Layer B code on `analyze-ai-branch`.*
