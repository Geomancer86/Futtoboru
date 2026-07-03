# Analysis — R-1888 First Release Readiness (Four-Stage Gate)

**Document type:** Release readiness analysis  
**Date:** 2026-07-03  
**Status:** **Active** — gates human QA (L4)  
**Release:** **R-1888** (`Futtoboru 1888` / tag `1888.0.0`)  
**Branch:** `analyze-ai-branch`  

**Naming:** [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)  
**CM/FM benchmark:** [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md) · [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md)  
**Injury:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9 · [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md)  

---

## 1. Executive summary

Human testers have **not** run B-9 — by policy ([LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md)) L1–L3 automated must finish first. This analysis scores **four pre-human stages** for **R-1888 only** (1888–89 start mod, Victorian injury care, CM1 management loop).

| Stage | Readiness | Blocker for human QA? |
|-------|-----------|------------------------|
| **1 — Analysis** | **~95%** | No — sufficient to proceed |
| **2 — Design** | **~92%** | No — ME-1 spec ready; minor PO calls open |
| **3 — Development** | **~88%** | **Yes** — ME-1 injury skin + UI regressions |
| **4 — Automated tests** | **~93%** | **Yes** — soak preflight unsigned; ME-1 tests missing |

**Overall R-1888 pre-human:** **~92%** — **not ready for L4** until §6 blockers close.

**PO framing:** CM/FM research covers **centuries**; R-1888 ships **one year’s chapter**. Document 1906 insurance; do **not** implement until **R-1906**.

---

## 2. R-1888 scope (what “first release” means)

Consolidates [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) (Layer A) + Layer B CM1 loop + R-1888 injury ceiling.

| Pillar | In R-1888 | Out (ledger / later R-YYYY) |
|--------|-----------|------------------------------|
| Start mod | 1888–89 (season id `18`), 12-club FL + FA Cup | 1892 Div 2, other starts |
| Season loop | Procedural rollover ∞ from save | Per-year mod folders |
| Management | Pick XI, 3 formations, preview → kick off → result, 0–10 ratings | Transfers, training schedules |
| Crowds | Table/form/derby attendance model | Dynamic reputation |
| Condition | Fitness, morale, fatigue, **ME-1 injuries** (Trainer) | CM93 insurance/Lilleshall (R-1993) |
| Rules | 2-1-0, goal average | Nov 1888 points change (optional defer) |
| World | Static squads, employed start | Regen, unemployed, world news |
| UI | Squad, tactics, preview, result, finances | Full inbox/news evolution |

---

## 3. Stage 1 — Analysis

### 3.1 CM / FM research coverage

| Domain | Research / analysis doc | R-1888 slice defined? |
|--------|-------------------------|------------------------|
| CM1 core loop | [CM1_REFERENCE.md](CM1_REFERENCE.md), [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) | ✅ |
| CM93 injuries (reference) | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) | ✅ engine target |
| Victorian medicine | [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md) | ✅ ME-1 |
| Feature depth vs CM | [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) | ✅ (injury banner needs R-1888 line) |
| Layer B benchmark | [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md) | ✅ |
| 1888 economy | [ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md](ANALYSIS_ECONOMIC_SYSTEM_1888_V1.md) | ✅ |
| Yearly release model | [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md) | ✅ |
| FM attendance borrow | [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) | ✅ B2 only |

**Analysis gaps (non-blocking):**

| Gap | Severity | Action |
|-----|----------|--------|
| Depth doc §1 still lists CM93 insurance as R-1888 player feature | Doc | Update banner → ME-1 only |
| Re-election / Nov 1888 points | Product | PO defer or message-only for R-1888 |
| UI bug inventory scattered | PM | Consolidated in §3.4 |

### 3.2 Genre parity claim (honest)

| Claim | Valid for R-1888? |
|-------|-------------------|
| CM1-minimum management loop (headless) | ✅ Proven |
| CM1-minimum management loop (UI) | ⚠️ Pending human B-9 |
| CM93 injury economy (player-facing) | ❌ Use **Victorian injury care** |
| FM medical depth | ❌ Out of scope |
| Historically accurate 1888 wages/rules | ✅ Mod + ITs |
| Historically accurate 1888 medicine | ❌ Until ME-1 ships |

### 3.3 Feature matrix — R-1888 vs CM1 (condensed)

| # | Feature | CM1 | R-1888 depth | Analysis done? |
|---|---------|-----|--------------|----------------|
| 1 | Pick XI | ✅ | Core | ✅ |
| 2 | Formations | ✅ | Partial (3 templates) | ✅ |
| 3 | Tactics menu | ✅ | Partial | ✅ |
| 4 | Match uses XI | ✅ | Core | ✅ |
| 5 | Preview → kick off | ✅ | Partial | ✅ |
| 6 | Result + scorers | ✅ | Core | ✅ |
| 7 | 0–10 ratings | ✅ | Partial | ✅ |
| 8 | Crowds context | ⚠️ | Partial (FM model) | ✅ |
| 9 | Fitness / morale | ✅ | Partial | ✅ |
| 10 | Injuries | ⚠️ IJ | **ME-1 target** (B5.2 CM93 engine today) | ✅ gap documented |
| 11 | Season loop ∞ | ✅ | Core | ✅ |
| 12 | 2-1-0 / goal avg | ✅ | Core | ✅ |

### 3.4 Known player-facing gaps (analysis)

From [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) and platform dept — **must close or PO-waive before human QA**:

| ID | Issue | Impact on B-9 |
|----|-------|---------------|
| C-1 | Continue double-click advances 2 days | M8 fail |
| PL-5 | Match history click broken | M5 fail |
| PL-7 | Save/load job paths | Reload career fail |
| INJ-R1888 | CM93 injury menus at 1888 start | M9 wrong UX / economy |

---

## 4. Stage 2 — Design

**Wrap-up doc:** [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md)

| Pillar | Design doc | Shipped spec? | R-1888 sign-off |
|--------|------------|---------------|-----------------|
| Season / scripts | [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | ✅ | ✅ |
| Tactics + XI | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) | ✅ | ✅ |
| Ratings + flow | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) | ✅ | ✅ |
| Player condition | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) | ✅ | ✅ |
| CM93 injury engine | [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md) | ✅ shipped | Dev reference only |
| **R-1888 injury skin** | [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) HIST-a/b | ✅ proposed | ☐ PO hard ceiling |
| Release naming | [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md) | ✅ | ✅ |
| Historical DB | [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) | ✅ | ✅ (1888 only) |

**Open design calls (minor):**

- Hard vs calendar release ceiling (recommend **hard** for R-1888)
- ME-1 rehab week deltas (0/1 vs 1/2)
- Re-election: script vs inbox message only

---

## 5. Stage 3 — Development

**Implementation records:** [IMPLEMENTATION_TACTICS_B4_V1.md](IMPLEMENTATION_TACTICS_B4_V1.md) · [IMPLEMENTATION_MATCH_RATINGS_B3_V1.md](IMPLEMENTATION_MATCH_RATINGS_B3_V1.md) · [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md)

| Area | Status | R-1888 blocker? |
|------|--------|-----------------|
| Engine: XI + sim v2 + ratings | ✅ | No |
| Engine: attendance + economy | ✅ | No |
| Engine: condition tick + B5.2 injury | ✅ | Partial — wrong **player** surface |
| **M-B5.3-HIST ME-1** | ❌ not started | **Yes** |
| UI: tactics + squad + preview + result | ✅ wired | Verify in L4 |
| UI: C-1 / PL-5 / PL-7 | ⚠️ open | **Yes** for clean B-9 |
| Training / transfers screens | Stub | No (out of scope) |
| Manual ch.6–7 | Draft, CM93 wording | **Yes** — needs R-1888 slice |

---

## 6. Stage 4 — Automated testing

**Pyramid:** [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) · **Inventory:** [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)

### 6.1 Verified counts (2026-07-03)

| Profile | Command | Tests | Result |
|---------|---------|-------|--------|
| **L1 + L2 (PR gate)** | `mvn test -pl futtoboru-core` | **131** | ✅ green |
| **L3 nightly** | `mvn test -pl futtoboru-core -Pnightly` | **5** | ✅ green |
| **L3 soak (60 seasons)** | `.\scripts\run-soak-with-preflight.ps1` | 1 (`LongRunSoakIT`) | ☐ **PM unsigned** |

Nightly classes: `AttendanceModelIT`, `LayerBMultiSeasonSoakIT`, `LongRunEconomySoakIT`, `MultiSeasonLeagueAndCupSoakIT`, `TenSeasonReportIT`.

**Combined automated coverage:** **136 test methods** in PR + nightly profiles (non-overlapping by Maven group filter).

### 6.2 Tier checklist vs R-1888

| Tier | Requirement | Status |
|------|-------------|--------|
| L1 | All Layer B unit rows green | ✅ |
| L2 | All Layer B IT rows green | ✅ |
| L3 | Nightly + preflight + soak | ⚠️ nightly ✅ · soak ☐ |
| L4 | B-9 human | ❌ not started (correct) |

### 6.3 Missing tests (R-1888 blockers)

| Test (proposed) | Proves | When |
|-----------------|--------|------|
| `MedicalEraRegistryTest` | 1888 → ME-1 | With HIST-a |
| `MedicalEraR1888GateTest` | No insure/Lilleshall/surgeon | With HIST-a |
| `MedicalEraR1888CostTest` | Rehab ≤ 1 week wage | With HIST-b |
| `@Tag("cm93")` on `InjuryEconomyB52Test` | CM93 profile isolated from R-1888 default | With HIST-a |

### 6.4 UI regressions without automated cover

| Issue | Automated? | Human B-9 |
|-------|------------|-----------|
| C-1 double-click | ❌ | M8 |
| History click | ❌ | M5 |
| Save/load UI | Partial (`SaveLoadIT` engine) | M1 reload |

**Recommendation:** Add `ContinueSingleStepIT` (C-1) before or during first human cycle — optional for R-1888 if PO accepts B-9-only regression.

---

## 7. Human QA gate (L4) — prerequisites

All must be true before inviting testers:

- [ ] §6.3 ME-1 tests green  
- [ ] §6.1 soak preflight signed (PM)  
- [ ] §5 M-B5.3-HIST ME-1 in player UI  
- [ ] §5 C-1 / PL-5 / PL-7 fixed or PO-waived with checklist notes  
- [ ] Manual [06](../manual/06-squad-and-condition.md) + [07](../manual/07-player-actions-insurance.md) R-1888 slice  
- [ ] PM signs [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) §5  

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial four-stage R-1888 readiness analysis |

---

*Refresh after each blocker closes or soak signs off.*
