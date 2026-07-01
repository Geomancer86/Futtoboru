# Feature Traceability Matrix (starter)

**Document type:** Registry — links research → design → code → tests → manual  
**Date:** 2026-07-01 (updated post B5.2 Complete)  
**Status:** Layer B core rows complete  

**How to use:** Manual § must point to `IMPLEMENTATION_*` not `DESIGN_*`.

---

## Layer B — player condition (B5 / B5.1 / B5.2)

| Field | Value |
|-------|-------|
| **Feature ID** | COND / B5 / B5.1 / B5.2 |
| **Research** | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) |
| **Analysis** | [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) · historical: [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) |
| **Design** | [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md) |
| **Implementation** | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10 |
| **Code** | `InjuryGenerator.java`, `InjuryTreatmentService.java`, `PhysioService.java`, `InjuryEconomyRegistry.java`, `PlayerDetailScreenTable.java`, `ExpenseCalculator.java`, `MessageManager.createInjuryNewsMessage()` |
| **Tests** | B5.1 suite + `InjuryEconomyB52Test` (8) · **131 tests green** |
| **Manual §** | [06-squad-and-condition.md](../manual/06-squad-and-condition.md), [07-player-actions-insurance.md](../manual/07-player-actions-insurance.md) |
| **Shipped depth** | Injuries **Core+** · **~100% CM93 Tier 2+3** — [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md) |
| **Next** | Human B-9 regression · forced retirement tribunal (deferred PO) |

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

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Player condition row + template |
| 2026-06-30 | B5.1 shipped; B3/B4 implementation docs linked |
