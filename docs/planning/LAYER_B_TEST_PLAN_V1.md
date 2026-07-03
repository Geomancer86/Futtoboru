# Layer B — Test Plan (Unit + Headless E2E)

**Document type:** QA / engineering test plan  
**Date:** 2026-07-03 (R-1888 inventory refresh)  
**Branch:** `analyze-ai-branch`  
**Release:** **R-1888** — [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md)  
**Parent:** [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) · [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) · [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md)

---

## 1. Principles

| Principle | Layer B application |
|-----------|---------------------|
| **No feature without tests** | Each MVP ships with unit test(s) and, where the loop is touched, a harness IT |
| **Headless first** | Same pattern as `LeagueAndCupIT`, `SaveLoadIT` — `GameTestHarness` + `HeadlessApplication` |
| **Regression** | Existing 62+ tests + soak profiles must stay green after each phase |
| **Manual last** | **L4 human QA only after L1–L3 green + R-1888 dev blockers** — see [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) |

**Commands:**

```bash
mvn test -pl futtoboru-core                    # L1+L2 PR gate — 131 tests (~2 min)
mvn test -pl futtoboru-core -Pnightly          # L3 — 5 nightly tests (~90s)
mvn test -pl futtoboru-core -Psoak             # L3 — 60-season LongRunSoakIT (~5 min quiet)
```

See [SIMULATION_TEST_SUITE_V2.md](SIMULATION_TEST_SUITE_V2.md) for health gates and tier design.

---

## 2. Test inventory

### B0 — Layer A (done)

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `CompetitionRulesLoaderTest` | Unit | Season 18 mod → 2-1-0, goal average tie-break | ✅ |
| `LeagueStandingsManagerTest` | Unit | Goal average sort; points order; friendlies excluded | ✅ |
| `LeagueCompetitionRulesFormatterTest` | Unit | League UI rules text (2-1-0, Goal Average) | ✅ |
| `HistoricalLeagueRulesIT` | IT | New game rules + full season 2-1-0 points | ✅ |

---

### B1 — Sim core ✅ (engine)

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `FormationRegistryTest` | Unit | 3 templates; default | ✅ |
| `BestElevenSelectorTest` | Unit | Picks 11 | ✅ |
| `ClubTacticsSetupSaveLoadIT` | IT | JSON roundtrip | ✅ |
| `MatchSimulatorV2Test` | Unit | Lineup strength | ✅ |
| `StartingElevenIT` | IT | Weak vs strong XI | ✅ |
| `TenSeasonReportIT` | E2E | Balance regression | ✅ |

**UI gap:** manual XI — see [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md)

---

### B2 — Crowds + economy watch

| Class | Type | Proves | Depends on | Status |
|-------|------|--------|------------|--------|
| `AttendanceCalculatorTest` | Unit | positionFactor, opponentFactor, formFactor, derbyFactor, clamp | MVP-4 | ✅ |
| `AttendanceModelIT` | IT | 1st vs 18th at same stadium → 1st higher attendance | MVP-4, H-1 | ✅ |
| `AttendanceOverwriteIT` | IT | Post-sim attendance equals calculator (not random re-roll) | H-1 | ✅ |
| `EconomyHealthMonitorTest` | Unit | Negative balance scan; league roster scope | ECON-B2 | ✅ |
| `LongRunEconomySoakIT` | E2E | 3 seasons complete; FL clubs stay solvent | ECON-B2 | ✅ |

---

### B3 — Feedback ✅

Design: [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md)

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `MatchRatingsTest` | Unit L1 | Rating formula | ✅ |
| `MatchFeedbackIT` | IT L2 | Ratings after sim | ✅ |
| `MatchLineupPreviewIT` | IT L2 | Preview lineup = sim lineup | ✅ |
| `Cm1ManagementLoopIT` | IT L2 | Full headless CM1 loop one match | ✅ |
| `MatchRatingsSaveLoadIT` | IT L2 | Ratings persist in save | ✅ |

### B4 — Tactics UX ✅

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `FormationSimWeightTest` | Unit L1 | Template changes strength | ✅ |

### B5 — Player condition ✅ (+ R-1888 gap)

Design: [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) · B5.2 [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `PlayerConditionDefaultsTest` | Unit L1 | New players fit | ✅ |
| `PlayerFitnessTest` | Unit L1 | Match −fitness | ✅ |
| `PlayerFormTest` | Unit L1 | Sharpness tick | ✅ |
| `InjuryGeneratorTest` | Unit L1 | Injury roll | ✅ |
| `MoraleUpdateTest` | Unit L1 | Win/loss/rating | ✅ |
| `FatigueRecoveryTest` | Unit L1 | Daily decay | ✅ |
| `InjuryEconomyB52Test` | Unit L1 | CM93 injury economy (8 cases) | ✅ `@Tag("cm93")` **recommended** |
| `InjuryTypesRegistryTest` | Unit L1 | Mod types load | ✅ |
| `MoraleGoalBonusTest` | Unit L1 | Goal morale | ✅ |
| `FatigueSlowsRecoveryTest` | Unit L1 | Fatigue recovery | ✅ |
| `PlayerSharpnessFactorTest` | Unit L1 | Sharpness sim | ✅ |
| `MatchInjuryTrackingTest` | Unit L1 | Match injury ids | ✅ |
| `PlayerConditionSaveLoadIT` | IT L2 | Condition in JSON | ✅ |
| `FitnessSimModifierIT` | IT L2 | Low fitness underperforms | ✅ |
| `InjuredPlayerLineupIT` | IT L2 | Injured excluded from XI | ✅ |

### B5-R1888 — ME-1 injury profile ❌ (required before human QA)

Design: [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) HIST-a/b

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `MedicalEraRegistryTest` | Unit L1 | 1888 → ME-1 | ❌ |
| `MedicalEraR1888GateTest` | Unit L1 | No insure/Lilleshall/surgeon | ❌ |
| `MedicalEraR1888CostTest` | Unit L1 | Rehab ≤ 1 week avg wage | ❌ |

### L3 — Long-term ✅ / ☐

| Class | Type | Proves | Status |
|-------|------|--------|--------|
| `LayerBMultiSeasonSoakIT` | Soak L3 | 5 seasons: XI, ratings, condition, health | ✅ nightly |
| `TenSeasonReportIT` | Soak L3 | Balance regression | ✅ nightly |
| `LongRunSoakIT` | Soak L3 | 60 seasons | ✅ soak profile · ☐ **PM sign-off** |
| `MultiSeasonLeagueAndCupSoakIT` | Nightly L3 | Cup + league | ✅ nightly |
| `LongRunEconomySoakIT` | Nightly L3 | FL solvency 3 seasons | ✅ nightly |
| `MultiSeasonSoakIT` | E2E L3 | 3 procedural seasons | ✅ PR |
| `LeagueAndCupIT` | E2E L3 | Full 1888 league + cup | ✅ PR |

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
| `int getLastMatchAttendance()` | B2 ✅ |
| `double averageHomeAttendance(Club club)` | B2 ✅ |
| `void assertNoNegativePrimaryLeagueBalances()` | B2 ✅ |
| `void assertPlayerClubSolvent()` | B2 ✅ |
| `Map<Long, Float> getLastMatchRatings(long clubId)` | B3 |
| `void confirmPreviewLineup()` | B3 |
| `void setPlayerFitness(long playerId, int pct)` | B5 |
| `void setPlayerInjury(long playerId, String typeId, int weeks)` | B5 |

---

## 4. CI / soak impact

| Profile | When to run | Layer B note |
|---------|-------------|--------------|
| Default `mvn test` | Every commit | **131** tests — excludes `@Tag(nightly|soak)` |
| `-Pnightly` | Before human QA | **5** additional nightly tests |
| `-Psoak` | PM sign-off once | `LongRunSoakIT` — use [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) |

---

## 5. Manual QA checklist (B-9) — **L4 human tier**

**Start only when** [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) §4 checklist is complete.

Run at **200% UI scale** on Galaxy Book or 1920×1080.

| # | Step | Pass |
|---|------|------|
| M1 | Load career → Squad → pick 11 (or Best XI) → save persists after reload | ☐ |
| M2 | Tactics sidebar → formation dropdown → slots update | ☐ |
| M3 | Match preview → edit XI → Continue → match sim runs | ☐ |
| M4 | Result screen → score, scorers, attendance, ratings visible | ☐ |
| M5 | Match history → click past match → result (A-4 regression) | ☐ |
| M6 | League table → 2-1-0 points (A-5 regression) | ✅ automated — `HistoricalLeagueRulesIT` |
| M7 | Top club home match → attendance visibly higher than early-season low table | ☐ |
| M8 | Continue once per click (A-2 regression) | ☐ |
| M9 | Squad shows fitness / morale / injury; injured not selectable in XI | ☐ |

**B-9 sign-off:** M1–M9 checked **after automated pyramid green**.

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
| 2-1-0 rules | A-5 | `LeagueCompetitionRulesFormatterTest` | `HistoricalLeagueRulesIT` | M6 (auto) |
| Player condition | COND-* | `PlayerFitnessTest`, etc. | `FitnessSimModifierIT` | Squad columns (B5) |

---

| Date | Change |
|------|--------|
| 2026-07-03 | B3–B5 marked green; B5-R1888 ME-1 tests; 131+nightly counts |
| 2026-06-30 | Initial Layer B test plan |
