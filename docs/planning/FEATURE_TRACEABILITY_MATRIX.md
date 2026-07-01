# Feature Traceability Matrix (starter)

**Document type:** Registry — links research → design → code → tests → manual  
**Date:** 2026-06-30  
**Status:** **Starter** — Layer B player condition row complete; expand per [ANALYSIS_DOCUMENTATION_PIPELINE_READINESS_V1.md](ANALYSIS_DOCUMENTATION_PIPELINE_READINESS_V1.md) DOC-1  

**How to use:** Add one row per shippable feature before writing manual text. Manual § must point to `IMPLEMENTATION_*` not `DESIGN_*`.

---

## Layer B — player condition (B5)

| Field | Value |
|-------|-------|
| **Feature ID** | COND / B5 |
| **Research** | [RESEARCH_EARLY_CM_INJURIES_VS_FM.md](../research/RESEARCH_EARLY_CM_INJURIES_VS_FM.md) |
| **Analysis** | [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) · [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) §4.1 |
| **Design (shipped)** | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) |
| **Design (proposed)** | [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md) |
| **Implementation** | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) |
| **Code** | `PlayerCondition.java`, `PlayerConditionService.java`, `InjuryGenerator.java`, `PlayersListTable.java` |
| **Tests** | `PlayerConditionDefaultsTest`, `PlayerFitnessTest`, `PlayerFormTest`, `FatigueRecoveryTest`, `MoraleUpdateTest`, `InjuryGeneratorTest`, `InjuredPlayerLineupIT`, `FitnessSimModifierIT`, `PlayerConditionSaveLoadIT` |
| **Manual §** | [docs/manual/README.md](../manual/README.md) §6 — 🔲 draft after B5.1 |
| **i18n keys** | `fitness_pct`, `morale`, `injury_status` |
| **Shipped depth** | Minimal injuries · Partial fitness/morale |
| **PO next** | Choose B5.1 option A/B/C |

---

## Layer B — tactics & XI (B4) — 🔲 row TBD

| Field | Value |
|-------|-------|
| **Feature ID** | MVP-1, MVP-2, MVP-9 / B4 |
| **Design** | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md), [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) |
| **Implementation** | 🔲 `IMPLEMENTATION_TACTICS_B4.md` not written |
| **Code** | `TacticsScreenTable.java`, `ClubTacticsSetup.java`, `FormationRegistry.java`, `BestElevenSelector.java` |
| **Manual §** | [docs/manual/README.md](../manual/README.md) §4 |

---

## Layer B — match ratings (B3) — 🔲 row TBD

| Field | Value |
|-------|-------|
| **Feature ID** | MVP-5 / B3 |
| **Design** | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) |
| **Implementation** | 🔲 not written |
| **Code** | `MatchRatingsCalculator.java`, `MatchResultScreenTable.java` |
| **Manual §** | [docs/manual/README.md](../manual/README.md) §5 |

---

## Template (copy for new rows)

```markdown
| **Feature ID** | |
| **Research** | |
| **Analysis** | |
| **Design** | |
| **Implementation** | |
| **Code** | |
| **Tests** | |
| **Manual §** | |
| **i18n keys** | |
| **Shipped depth** | |
```

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Player condition row + template |
