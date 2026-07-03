# Feature Traceability Matrix (starter)

**Document type:** Registry — links research → design → code → tests → manual  
**Date:** 2026-07-03 (R-1888 release row)  
**Status:** Layer B core + R-1888 gate docs complete  

**How to use:** Manual § must point to `IMPLEMENTATION_*` not `DESIGN_*`.

---

## Layer B — player condition (B5 / B5.1 / B5.2)

| Field | Value |
|-------|-------|
| **Feature ID** | COND / B5 / B5.1 / B5.2 |
| **Research** | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) · [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md) |
| **Analysis** | [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9 historical · [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) |
| **Design** | [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md) · [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) (proposed) |
| **Implementation** | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10 |
| **Code** | `InjuryGenerator.java`, `InjuryTreatmentService.java`, `PhysioService.java`, `InjuryEconomyRegistry.java`, `PlayerDetailScreenTable.java`, `ExpenseCalculator.java`, `MessageManager.createInjuryNewsMessage()` |
| **Tests** | B5.1 suite + `InjuryEconomyB52Test` (8) · **131 tests green** |
| **Manual §** | [06-squad-and-condition.md](../manual/06-squad-and-condition.md), [07-player-actions-insurance.md](../manual/07-player-actions-insurance.md) |
| **Release** | **R-1888** (ME-1 injury target) · ME-2+ → **R-1906**+ |
| **Shipped depth** | Injuries **Core+** CM93 engine · **R-1888 historical 0%** until M-B5.3-HIST ME-1 |
| **Next** | **M-B5.3-HIST a+b (ME-1)** · M-B5.3 fitness polish · in-match injury · forced retirement (deferred) |

---

## Layer B — tactics & XI (B4)

| Field | Value |
|-------|-------|
| **Feature ID** | MVP-1, MVP-2, MVP-9 / B4 |
| **Design** | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) |
| **Implementation** | [IMPLEMENTATION_TACTICS_B4_V1.md](IMPLEMENTATION_TACTICS_B4_V1.md) |
| **Manual §** | [docs/manual/README.md](../manual/README.md) §4 |

---

## Layer B — match ratings (B3)

| Field | Value |
|-------|-------|
| **Feature ID** | MVP-5 / B3 |
| **Design** | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) |
| **Implementation** | [IMPLEMENTATION_MATCH_RATINGS_B3_V1.md](IMPLEMENTATION_MATCH_RATINGS_B3_V1.md) |
| **Manual §** | [docs/manual/README.md](../manual/README.md) §5 |

---

---

## R-1888 first release

| Field | Value |
|-------|-------|
| **Release id** | R-1888 / `1888.0.0` |
| **Analysis** | [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) |
| **Design** | [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) |
| **PM** | [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) |
| **Acceptance** | [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md) |
| **Tests** | 131 PR + 5 nightly green · ME-1 tests pending · soak PM unsigned |
| **Human QA** | ⏸ Blocked — [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) §5 |

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | R-1888 release traceability row |
| 2026-07-03 | R-YYYY release column; R-1888 ME-1 scope |
| 2026-06-25 | M-B5.3-HIST research/design linked; 1888 historical gap noted |
| 2026-06-30 | Player condition row + template |
| 2026-06-30 | B5.1 shipped; B3/B4 implementation docs linked |
