# Layer B — Testing Pyramid (Unit → E2E → Long → Human)

**Document type:** QA strategy  
**Date:** 2026-06-30  
**Status:** **Active** — **no human testers until L1–L3 green**  
**Parent:** [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) · [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)

---

## 1. Policy (PO locked 2026-06-30)

```
L1 Unit          → every class / formula
L2 E2E (IT)      → GameTestHarness headless loop
L3 Long-term     → multi-season + soak profiles
L4 Human QA      → ONLY after L1–L3 green on analyze-ai-branch
```

**No public release implied.** Human testing validates the CM1 loop feels right locally before Layer C.

---

## 2. Pyramid diagram

```
                    ┌─────────────────────┐
                    │  L4 Human (B-9)     │  CM1 walkthrough checklist
                    │  after L1–L3 green  │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L3 Long-term       │  10 / 60 season, Layer B soak
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L2 E2E / IT        │  Loop, preview, condition
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │  L1 Unit            │  Calculators, selectors, registry
                    └─────────────────────┘
```

---

## 3. Commands by tier

| Tier | Command | When |
|------|---------|------|
| **L1 + L2** | `mvn test -pl futtoboru-core` | Every commit |
| **L2 nightly** | `mvn test -pl futtoboru-core -Pnightly` | Pre-merge / weekly |
| **L3 soak** | `.\scripts\run-soak-with-preflight.ps1` | After Layer B feature batch |
| **L3 Layer B** | `mvn test -pl futtoboru-core -Dtest=LayerBMultiSeasonSoakIT` | When added |
| **L4 human** | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) | After PM signs L3 |

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
| `MatchRatingsTest` | MVP-5 | ❌ |
| `FormationSimWeightTest` | MVP-2 | ❌ |
| `PlayerFitnessTest` | COND-1 | ❌ |
| `PlayerFormTest` | COND-2 | ❌ |
| `InjuryGeneratorTest` | COND-3 | ❌ |
| `MoraleUpdateTest` | COND-5 | ❌ |
| `FatigueRecoveryTest` | COND-4 | ❌ |

### L2 — E2E / IT ✅ / ❌

| Class | MVP / COND | Status |
|-------|------------|--------|
| `StartingElevenIT` | MVP-1, MVP-3 | ✅ |
| `ClubTacticsSetupSaveLoadIT` | DATA-B1 | ✅ |
| `AttendanceModelIT` | MVP-4 | ✅ |
| `AttendanceOverwriteIT` | H-1 | ✅ |
| `LongRunEconomySoakIT` | ECON-B2 | ✅ |
| `MatchFeedbackIT` | MVP-5, MVP-6 | ❌ |
| `MatchLineupPreviewIT` | MVP-7 | ❌ |
| `Cm1ManagementLoopIT` | B1–B4 loop | ❌ **new** |
| `PlayerConditionSaveLoadIT` | COND | ❌ **new** |
| `FitnessSimModifierIT` | COND-1 | ❌ |
| `InjuredPlayerLineupIT` | COND-3 | ❌ |

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
| **`LayerBMultiSeasonSoakIT`** | **5–10 seasons: XI persist, ratings every match, no condition NaN, health clean** | ❌ **new** |

Run `LayerBMultiSeasonSoakIT` in **`-Pnightly`** until stable; optional **`-Psoak`** extension later.

### L4 — Human QA (last)

| Gate | Doc | Start when |
|------|-----|------------|
| **B-9** CM1 loop walkthrough | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) | PM confirms L1–L3 green |
| UI scale 200% | Same checklist | Human testers recruited |

**Human testers do not run until automated pyramid is green** — avoids wasting manual cycles on known-missing ratings/preview/tactics.

---

## 5. Layer B complete — test gate summary

| Tier | Required for “Layer B complete” |
|------|----------------------------------|
| L1 | All rows in §4 L1 **green** |
| L2 | All rows in §4 L2 **green** |
| L3 | `TenSeasonReportIT` + `LayerBMultiSeasonSoakIT` + preflight soak **green** |
| L4 | B-9 checklist signed (human) |

---

## 6. Traceability

| CM1 loop step | L1 | L2 | L3 | L4 |
|---------------|----|----|-----|-----|
| Pick XI | `BestElevenSelectorTest` | `StartingElevenIT` | soak | M1 |
| Formation | `FormationSimWeightTest` | `Cm1ManagementLoopIT` | soak | M2 |
| Preview → match | — | `MatchLineupPreviewIT` | — | M3 |
| Ratings | `MatchRatingsTest` | `MatchFeedbackIT` | soak | M4 |
| Crowds | `AttendanceCalculatorTest` | `AttendanceModelIT` | `MultiSeasonSoakIT` | M7 |
| Fitness/injury | `PlayerFitnessTest`, `InjuryGeneratorTest` | `FitnessSimModifierIT`, `InjuredPlayerLineupIT` | `LayerBMultiSeasonSoakIT` | M9 |

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Four-tier pyramid; human QA last; new long/E2E specs |

---

*Implement missing tests in the same commit series as each feature.*
