# Futtoboru 1888 — Release Definition (R-1888)

**Document type:** Product scope / acceptance criteria  
**Date:** 2026-07-03  
**Status:** **Active** — first named release tranche  
**Tag:** `1888.0.0` · **Internal id:** `release_1888`  

**Supersedes for active work:** [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) (same criteria, legacy `v1.0` label)  
**Readiness analysis:** [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md)  
**PM wrap-up:** [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md)  

---

## 1. Release vision

> **Futtoboru 1888** lets a manager start in the **1888–89 Football League**, play league + FA Cup under **historical rules**, manage a **CM1-minimum loop** (XI, tactics, preview, result, ratings, Victorian injuries), and continue **procedurally forever** — without loading a new mod per simulated year.

**Not in R-1888:** 1892 Division Two, transfers, training depth, CM93 insurance UI, world news forks, unemployed start, public distribution (PO: local dev until ready).

---

## 2. Pre-human gate (four stages)

| Stage | Exit criterion |
|-------|----------------|
| **Analysis** | [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) published |
| **Design** | [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) §5 signed |
| **Development** | ME-1 injury + Layer B UI blockers closed |
| **Automated tests** | 131 PR + 5 nightly green; soak PM-signed |
| **Human QA** | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) B-9 — **after** above |

---

## 3. Acceptance criteria (inherited from Layer A + B)

Use [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §4 IDs **NG-***, **PL-***, **SC-***, **TR-***, **LP-***, **Cup-***, **LC-*** with these R-1888 overrides:

| Override | R-1888 rule |
|----------|-------------|
| Injury UX | **ME-1 only** — Trainer, no Insure/Lilleshall/Surgeon ([DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md)) |
| Marketing | “Victorian injury care” not “CM93 injuries” |
| Re-election SC-6 | **Defer** or message-only (PO) |
| Nov 1888 points SC-7 | **Defer** |
| Unemployed | **Defer** post-R-1888 |

### 3.1 Layer B additions (R-1888)

| ID | Criterion | Automated evidence |
|----|-----------|-------------------|
| LB-1 | Picked XI used in sim | `StartingElevenIT`, `Cm1ManagementLoopIT` |
| LB-2 | Post-match 0–10 ratings | `MatchRatingsTest`, `MatchFeedbackIT` |
| LB-3 | Preview lineup = sim lineup | `MatchLineupPreviewIT` |
| LB-4 | Attendance uses table/form/derby | `AttendanceModelIT` (nightly) |
| LB-5 | Fitness/injury affects availability | `InjuredPlayerLineupIT`, `FitnessSimModifierIT` |
| LB-6 | 5-season Layer B soak stable | `LayerBMultiSeasonSoakIT` (nightly) |
| LB-7 | **ME-1 injury profile** | `MedicalEraR1888GateTest` (when shipped) |

---

## 4. Test commands (R-1888 sign-off)

```powershell
# L1 + L2 (required every commit)
mvn test -pl futtoboru-core

# L3 nightly (required before human QA)
mvn test -pl futtoboru-core -Pnightly

# L3 soak (PM sign-off once)
.\scripts\run-soak-with-preflight.ps1
```

**Expected:** 131 + 5 = **136** automated methods green before L4.

---

## 5. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial R-1888 release definition |

---

*Human B-9 is the final gate — not a substitute for automated L1–L3.*
