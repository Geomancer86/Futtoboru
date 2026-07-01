# Feature Traceability Matrix (starter)

**Document type:** Registry — links research → design → code → tests → manual  
**Date:** 2026-06-30 (updated post B5.1)  
**Status:** Layer B core rows complete  

**How to use:** Manual § must point to `IMPLEMENTATION_*` not `DESIGN_*`.

---

## Layer B — player condition (B5 / B5.1)

| Field | Value |
|-------|-------|
| **Feature ID** | COND / B5 / B5.1 |
| **Research** | [RESEARCH_EARLY_CM_INJURIES_VS_FM.md](../research/RESEARCH_EARLY_CM_INJURIES_VS_FM.md) |
| **Analysis** | [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) |
| **Design** | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) · [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md) |
| **Implementation** | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9 |
| **Code** | `PlayerCondition.java`, `PlayerConditionService.java`, `InjuryGenerator.java`, `InjuryTypesRegistry.java`, `PlayersListTable.java`, `TacticsScreenTable.java`, `MatchResultScreenTable.java` |
| **Tests** | L1: `PlayerConditionDefaultsTest`, `PlayerFitnessTest`, `PlayerFormTest`, `FatigueRecoveryTest`, `FatigueSlowsRecoveryTest`, `MoraleUpdateTest`, `MoraleGoalBonusTest`, `InjuryGeneratorTest`, `InjuryTypesRegistryTest`, `PlayerSharpnessFactorTest`, `MatchInjuryTrackingTest` · L2: `InjuredPlayerLineupIT`, `FitnessSimModifierIT`, `PlayerConditionSaveLoadIT` |
| **Manual §** | [06-squad-and-condition.md](../manual/06-squad-and-condition.md) ⚠️ draft |
| **i18n keys** | `fitness_pct`, `sharpness_pct`, `morale`, `injury_status`, `injury_return`, `condition_fit`, `condition_fit_warning`, `injured_until`, `slot_status` |
| **Shipped depth** | Injuries **Partial** (Tier 1) · **~39% CM93 parity** — [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md) |
| **Next** | PO lock M-B5.2 Core vs Complete → [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md) |

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
