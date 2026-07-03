# Design — R-1888 Release Wrap-Up (Sign-Off Checklist)

**Document type:** Design consolidation / release sign-off  
**Date:** 2026-07-03  
**Status:** **Active** — design stage ~92% for R-1888  
**Release:** **R-1888** (`Futtoboru 1888`)  
**Analysis:** [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md)  

---

## 1. Purpose

Single **design sign-off sheet** for the first named release. Confirms every R-1888 pillar has an **approved design** before human QA — separate from the **full historical ledger** (1906+, CM93, FM).

**Rule:** If it is not in §3 with status **Signed** or **Deferred (PO)**, it is **not** in R-1888 player scope.

---

## 2. Design principles (R-1888)

1. **CM1 loop minimum** — pick XI → preview → kick off → abstract result → ratings → condition tick ([DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md)).
2. **1888 data truth** — season 18 mod, 2-1-0, goal average, 1888 wages ([ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md)).
3. **Victorian injury care (ME-1)** — Trainer, shilling-scale rehab; **no** insurance/Lilleshall/surgeon ([DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) HIST-a/b).
4. **Document all, ship one year** — [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md).
5. **Human QA last** — after L1–L3 automated ([LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md)).

---

## 3. Pillar sign-off matrix

| ID | Pillar | Authoritative design | Impl record | Design status | PO / PM |
|----|--------|---------------------|-------------|---------------|---------|
| R-D1 | New game + season 18 start | [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | Season loaders | ✅ **Signed** | — |
| R-D2 | League 2-1-0 + goal average | `competition_rules.txt` + [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | `CompetitionRulesLoader` | ✅ **Signed** | — |
| R-D3 | FA Cup knockout | [DESIGN_KNOCKOUT_BRACKET_V1.md](DESIGN_KNOCKOUT_BRACKET_V1.md) | Cup ITs | ✅ **Signed** | — |
| R-D4 | Procedural rollover | [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | `SeasonManager` | ✅ **Signed** | — |
| R-D5 | Pick XI + Best XI | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) | [IMPLEMENTATION_TACTICS_B4_V1.md](IMPLEMENTATION_TACTICS_B4_V1.md) | ✅ **Signed** | — |
| R-D6 | 3 formations (1888) | [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) | `FormationRegistry` | ✅ **Signed** (3 templates) | — |
| R-D7 | Match sim v2 (XI + condition) | [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) | `MatchSimulator` | ✅ **Signed** | — |
| R-D8 | Preview → kick off | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) | [IMPLEMENTATION_MATCH_RATINGS_B3_V1.md](IMPLEMENTATION_MATCH_RATINGS_B3_V1.md) | ✅ **Signed** | — |
| R-D9 | 0–10 ratings + scorers | Same as R-D8 | Same | ✅ **Signed** | — |
| R-D10 | Attendance model | B2 in [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) | `AttendanceCalculator` | ✅ **Signed** | — |
| R-D11 | Fitness / morale / fatigue | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9 | ✅ **Signed** | — |
| R-D12 | **Injuries ME-1 (R-1888)** | [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) HIST-a/b | §11 gap | ☐ **Pending impl** | PO: hard ceiling |
| R-D13 | CM93 injury engine (dev) | [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md) | §10 shipped | ✅ **Signed** (not R-1888 UX) | — |
| R-D14 | 1888 wages / expenses | [ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md) | `ExpenseCalculator` | ✅ **Signed** | — |
| R-D15 | Static squads | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §4.7 | — | ✅ **Signed** | — |
| R-D16 | Employed start only | Same | — | ✅ **Signed** | — |
| R-D17 | Training / transfers UI | — | Stub | ⏸ **Deferred** R-1888+ | PO locked |
| R-D18 | World news / forks | [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) | — | ⏸ **Deferred** Layer C | — |
| R-D19 | Re-election bottom 4 | Historical note | — | ⏸ **Defer** message-only? | PO call |
| R-D20 | Nov 1888 points rule | Script design TBD | — | ⏸ **Defer** | PO call |
| R-D21 | Release naming R-YYYY | [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md) | — | ✅ **Signed** 2026-07-03 | — |
| R-D22 | Manual player docs | [docs/manual/README.md](../manual/README.md) | ch.6–7 draft | ⚠️ **Needs R-1888 slice** | Before L4 |

---

## 4. ME-1 injury design summary (R-1888 player-facing)

Full spec: [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md).

| Surface | R-1888 design |
|---------|---------------|
| Staff label | **Trainer** (not Physio) |
| Estimate column | Trainer’s estimate |
| Rehabilitate | Send to trainer; **£0.25** MINOR / **£0.75** MODERATE |
| Insure / Uninsure | **Hidden** — ships in R-1906 |
| Lilleshall / Surgeon | **Hidden** — R-1951 / R-1993 |
| Recovery | Base weeks × **1.20**; rehab deltas 0/1 |
| Release ceiling | `max_implemented_medical_era=ME-1` |

---

## 5. Design stage exit criteria

Design stage is **complete for R-1888** when:

- [x] All §3 rows are ✅ Signed, ⏸ Deferred with PO note, or ☐ R-D12 spec approved  
- [x] R-D21 yearly naming locked  
- [ ] R-D12 PO confirms hard ceiling  
- [ ] R-D22 manual outline approved for ME-1 wording  
- [ ] PM records sign-off date below  

**PM design sign-off:** _________________ Date: _______

---

## 6. Traceability

```
DESIGN_YEARLY_RELEASE_SCOPE.md
DESIGN_R1888_RELEASE_WRAP_UP_V1.md (this doc)
    → ANALYSIS_R1888_RELEASE_READINESS_V1.md
    → IMPLEMENTATION_* / FEATURE_TRACEABILITY_MATRIX.md
    → LAYER_B_TEST_PLAN_V1.md
    → manual 06 + 07
```

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial R-1888 design wrap-up matrix |

---

*Update R-D12 to ✅ when M-B5.3-HIST ME-1 ships.*
