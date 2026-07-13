# Layer B — Testing Pyramid (Unit → E2E → Long → Human)

**Document type:** QA strategy  
**Date:** 2026-07-03 (R-1888 counts verified)  
**Status:** **Active** — **no human testers until L1–L3 green + R-1888 dev blockers**  
**Parent:** [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) · [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md)

---

## 1. Policy (PO locked 2026-06-30)

```
L1 Unit          → every class / formula
L2 E2E (IT)      → GameTestHarness headless loop
L3 Long-term     → multi-season + soak profiles
L4 Human QA      → ONLY after L1–L3 green + ME-1 injury + UI blockers (R-1888)
```

**No public release implied.** Human testing validates the CM1 loop feels right locally before Layer C.

---

## 2. Pyramid diagram

```
                    ┌─────────────────────┐
                    │  L5 Agent vision    │  PROPOSED — optional exploratory
                    │  (screenshot CU)    │  NEVER merge gate
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L4 Human (B-9)     │  CM1 walkthrough checklist
                    │  after L1–L3 green  │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L2.5 Scene2D UI    │  PROPOSED post–R-1888
                    │  player (clicks)    │  see ANALYSIS_AGENT_PLAYER_E2E_V1
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L3 Long-term       │  10 / 60 season, Layer B soak
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L2 E2E / IT        │  Engine harness — NOT real UI clicks
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L1 Unit            │  Calculators, selectors, registry
                    └─────────────────────┘
```

**Important:** L2 “E2E” = career/engine loop via `GameTestHarness`. It does **not** open VisUI or click buttons. GUI click automation is **L2.5 (proposed)** or **L4 human**. Full analysis: [ANALYSIS_AGENT_PLAYER_E2E_V1.md](ANALYSIS_AGENT_PLAYER_E2E_V1.md).

---

## 3. Commands by tier

| Tier | Command | When |
|------|---------|------|
| **L1 + L2** | `mvn test -pl futtoboru-core` | Every commit — **141 tests** |
| **L2 nightly** | `mvn test -pl futtoboru-core -Pnightly` | Before human QA — **+5 tests** |
| **L3 soak** | `.\scripts\run-soak-with-preflight.ps1` | PM sign-off once — progress/ETA design: [DESIGN_SOAK_PROGRESS_REPORTING_V1.md](DESIGN_SOAK_PROGRESS_REPORTING_V1.md) |
| **L3 Layer B** | Included in `-Pnightly` | `LayerBMultiSeasonSoakIT` |
| **L4 human** | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) | [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) §4 |

Preflight: [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md)

---

## 4. Test inventory by tier

### L1 — Unit ✅ / ❌

| Class | MVP / COND | Status |
|-------|------------|--------|
| `FormationRegistryTest` | DATA-B1 | ✅ |
| `BestElevenSelectorTest` | MVP-1 | ✅ |
| `MatchSimulatorV2Test` | MVP-3 | ✅ |
| `AttendanceCalculatorTest` | MVP-4 | ✅ |
| `EconomyHealthMonitorTest` | ECON-B2 | ✅ |
| `LeagueStandingsManagerTest` | A-5 | ✅ |
| `MatchRatingsTest` | MVP-5 | ✅ |
| `FormationSimWeightTest` | MVP-2 | ✅ |
| `PlayerFitnessTest` | COND-1 | ✅ |
| `PlayerFormTest` | COND-2 | ✅ |
| `InjuryGeneratorTest` | COND-3 | ✅ |
| `MoraleUpdateTest` | COND-5 | ✅ |
| `FatigueRecoveryTest` | COND-4 | ✅ |
| `PlayerConditionDefaultsTest` | COND | ✅ |
| `InjuryEconomyB52Test` | B5.2 CM93 | ✅ (tag `cm93` recommended) |
| `InjuryTypesRegistryTest` | B5.2 | ✅ |
| `MoraleGoalBonusTest` | B5.1 | ✅ |
| `FatigueSlowsRecoveryTest` | B5.1 | ✅ |
| `PlayerSharpnessFactorTest` | B5.1 | ✅ |
| `MatchInjuryTrackingTest` | B5.1 | ✅ |
| `MedicalEraR1888GateTest` | R-1888 ME-1 | ❌ **pending HIST-a** |
| `MedicalEraR1888CostTest` | R-1888 ME-1 | ❌ **pending HIST-b** |

### L2 — E2E / IT ✅ / ❌

| Class | MVP / COND | Status |
|-------|------------|--------|
| `StartingElevenIT` | MVP-1, MVP-3 | ✅ |
| `ClubTacticsSetupSaveLoadIT` | DATA-B1 | ✅ |
| `AttendanceModelIT` | MVP-4 | ✅ |
| `AttendanceOverwriteIT` | H-1 | ✅ |
| `LongRunEconomySoakIT` | ECON-B2 | ✅ |
| `MatchFeedbackIT` | MVP-5, MVP-6 | ✅ |
| `MatchLineupPreviewIT` | MVP-7 | ✅ |
| `Cm1ManagementLoopIT` | B1–B4 loop | ✅ |
| `PlayerConditionSaveLoadIT` | COND | ✅ |
| `FitnessSimModifierIT` | COND-1 | ✅ |
| `InjuredPlayerLineupIT` | COND-3 | ✅ |
| `MatchRatingsSaveLoadIT` | MVP-5 | ✅ |
| `AttendanceModelIT` | MVP-4 | ✅ nightly |

#### `Cm1ManagementLoopIT` (spec)

Headless employed career, one match:

1. `setClubTacticsSetup` with forced XI  
2. `kickOffNextMatchForCurrentClub()` (after B3b)  
3. Assert lineup ids used, ratings size 11, scorers non-empty if goals > 0  
4. Assert attendance > 0  

#### `PlayerConditionSaveLoadIT` (spec)

1. Set fitness/morale/injury on player  
2. Save/load roundtrip  
3. Values preserved  

### L3 — Long-term ✅ / ❌

| Class | Proves | Status |
|-------|--------|--------|
| `TenSeasonReportIT` | Balance + economy | ✅ |
| `MultiSeasonSoakIT` | 3 procedural seasons | ✅ |
| `MultiSeasonLeagueAndCupSoakIT` | Cup + league | ✅ nightly |
| `LongRunSoakIT` | 60 seasons league | ✅ soak profile |
| `LongRunEconomySoakIT` | FL solvency | ✅ |
| **`LayerBMultiSeasonSoakIT`** | **5–10 seasons: XI persist, ratings every match, no condition NaN, health clean** | ✅ nightly (5 seasons) |

Run `LayerBMultiSeasonSoakIT` in **`-Pnightly`** until stable; optional **`-Psoak`** extension later.

### L4 — Human QA (last)

| Gate | Doc | Start when |
|------|-----|------------|
| **B-9** CM1 loop walkthrough | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) | PM confirms L1–L3 green |
| UI scale 200% | Same checklist | Human testers recruited |

**Human testers do not run until automated pyramid is green** — avoids wasting manual cycles on known-missing ratings/preview/tactics.

---

## 5. R-1888 automated gate summary

| Tier | Count | Status | Required before L4? |
|------|-------|--------|---------------------|
| L1 + L2 (PR) | **131** | ✅ green 2026-07-03 | Yes |
| L3 nightly | **+5** | ✅ green 2026-07-03 | Yes |
| L3 soak | **1** (`LongRunSoakIT`) | ☐ PM unsigned | Yes |
| R-1888 ME-1 | **+3** (proposed) | ❌ | Yes |
| **Total target** | **~139** | **136/139** | — |

---

## 6. Layer B complete — test gate summary

| Tier | Required for “Layer B complete” |
|------|----------------------------------|
| L1 | All rows in §4 L1 **green** |
| L2 | All rows in §4 L2 **green** |
| L3 | Nightly + preflight + soak **PM-signed** | ⚠️ nightly ✅ · soak ☐ |
| L4 | B-9 checklist signed (human) | After §5 gate |

---

## 7. Traceability

| CM1 loop step | L1 | L2 | L3 | L4 |
|---------------|----|----|-----|-----|
| Pick XI | `BestElevenSelectorTest` | `StartingElevenIT` | soak | M1 |
| Formation | `FormationSimWeightTest` | `Cm1ManagementLoopIT` | soak | M2 |
| Preview → match | — | `MatchLineupPreviewIT` | — | M3 |
| Ratings | `MatchRatingsTest` | `MatchFeedbackIT` | soak | M4 |
| Crowds | `AttendanceCalculatorTest` | `AttendanceModelIT` | `MultiSeasonSoakIT` | M7 |
| Fitness/injury | `PlayerFitnessTest`, `InjuryGeneratorTest` | `FitnessSimModifierIT`, `InjuredPlayerLineupIT` | `LayerBMultiSeasonSoakIT` | M9 |

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-12 | Clarified L2 ≠ GUI; proposed L2.5 Scene2D + L5 vision — [ANALYSIS_AGENT_PLAYER_E2E_V1.md](ANALYSIS_AGENT_PLAYER_E2E_V1.md) |
| 2026-07-03 | R-1888 counts 131+5; ME-1 pending tests; gate §5 |
| 2026-06-30 | Four-tier pyramid; human QA last; new long/E2E specs |

---

*Implement missing tests in the same commit series as each feature.*
