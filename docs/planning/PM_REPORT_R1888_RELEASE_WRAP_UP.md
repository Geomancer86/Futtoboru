# PM Report — R-1888 First Release Wrap-Up

**Document type:** PM status / release gate  
**Date:** 2026-07-03  
**Branch:** `analyze-ai-branch` (~105 commits ahead of tracking)  
**Audience:** PO, PM, all department leads, QA, Sponsor  

**Master readiness:** [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md)  
**Design sign-off:** [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md)  
**Release naming:** [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)  

---

## 1. Executive summary

We are preparing **Futtoboru 1888** (`R-1888`, tag `1888.0.0`) — the first **yearly-named** release. Human testers have **not** started; that is **correct policy**. L4 runs only after analysis, design, development, and **automated** L1–L3 are complete.

| Stage | Status | Ready for human QA? |
|-------|--------|---------------------|
| **Analysis** | **~95%** | ✅ sufficient |
| **Design** | **~92%** | ✅ sufficient (ME-1 spec ready) |
| **Development** | **~88%** | ❌ ME-1 injury + UI fixes |
| **Automated tests** | **~93%** | ❌ soak unsigned + ME-1 tests |
| **Human QA (L4)** | **0%** | ⏸ **Blocked** — by design |

**Bottom line:** R-1888 is **~92% pre-human**. Estimate **1–2 sprints** to clear blockers, then invite B-9 testers.

---

## 2. What R-1888 is (PO-aligned)

| In | Out (documented for later R-YYYY) |
|----|-----------------------------------|
| 1888–89 start mod, FL + FA Cup, procedural ∞ | 1892 Div 2, other start mods |
| CM1 loop: XI, tactics, preview, result, ratings | Transfers, training depth |
| Fitness, morale, **Victorian injuries (ME-1)** | CM93 insurance/Lilleshall in UI |
| 131 PR + 5 nightly tests green | 60-season soak PM sign-off pending |
| Static squads, employed start | Regen, unemployed, world news |

**Research vs ship:** Full CM/FM/medical history lives in research docs. **1906 insurance is not built** until **R-1906**.

---

## 3. Four-stage detail

### 3.1 Analysis — ~95% ✅

**Done:**

- CM1 / CM93 / early FM feature maps ([ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md), [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md))
- Victorian medical eras ME-0→ME-7 ([RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md))
- Yearly release model ([DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md))
- R-1888 four-stage readiness ([ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md))

**Remaining (non-blocking):** PO calls on re-election / Nov 1888 points (defer OK for R-1888).

### 3.2 Design — ~92% ✅

**Done:** 21/22 pillars signed in [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md).

**Open:**

| ID | Item | Owner |
|----|------|-------|
| R-D12 | ME-1 injury — spec ready, **awaiting impl** | Match/Sim |
| R-D22 | Manual ch.6–7 R-1888 wording | Manual authors |
| PO | Hard release ceiling at ME-1 | PO |

### 3.3 Development — ~88% ❌ blocker

**Shipped (Layer B):**

| Area | Evidence |
|------|----------|
| XI + tactics + save | [IMPLEMENTATION_TACTICS_B4_V1.md](IMPLEMENTATION_TACTICS_B4_V1.md) |
| Ratings + preview/result | [IMPLEMENTATION_MATCH_RATINGS_B3_V1.md](IMPLEMENTATION_MATCH_RATINGS_B3_V1.md) |
| Condition + B5.2 CM93 engine | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) |

**Blockers before human QA:**

| Priority | Deliverable | Sprint |
|----------|-------------|--------|
| **P0** | **M-B5.3-HIST ME-1** — hide CM93 menus, Trainer labels, shilling costs | ~1 sprint |
| **P0** | **C-1** Continue double-click fix | Platform |
| **P1** | **PL-5** match history click | Platform |
| **P1** | **PL-7** save/load UI paths (engine IT exists) | Platform |
| **P2** | Manual R-1888 slice | UX/docs |

### 3.4 Automated testing — ~93% ❌ blocker

**Verified 2026-07-03:**

| Profile | Tests | Result |
|---------|-------|--------|
| PR gate `mvn test -pl futtoboru-core` | **131** | ✅ green |
| Nightly `mvn test -pl futtoboru-core -Pnightly` | **5** | ✅ green |
| Soak `LongRunSoakIT` (60 seasons) | 1 | ☐ **PM not signed** — run [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) |

**Layer B coverage:** All L1 unit + L2 IT rows in [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) are **green** in default profile.

**Missing for R-1888:**

| Test | With feature |
|------|--------------|
| `MedicalEraR1888GateTest` | M-B5.3-HIST HIST-a |
| `MedicalEraR1888CostTest` | M-B5.3-HIST HIST-b |
| `@Tag("cm93")` on B5.2 economy tests | Profile isolation |

**No automated cover:** C-1, PL-5 (human B-9 M5/M8 unless IT added).

---

## 4. Human QA gate — when we invite testers

**Policy (unchanged):** [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) — L4 **only** after L1–L3 green.

**Checklist before first human session:**

1. [ ] M-B5.3-HIST ME-1 shipped + tests green  
2. [ ] C-1 fixed (or PO waiver documented in B-9)  
3. [ ] PL-5 / PL-7 fixed or waived  
4. [ ] Preflight + `LongRunSoakIT` PM sign-off  
5. [ ] Manual ch.6–7 R-1888 accurate  
6. [ ] PM signs [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) §5  

**Checklist:** [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) — M1–M9 at 200% UI scale.

---

## 5. Execution plan (next 1–2 sprints)

### Sprint A — R-1888 dev blockers

| # | Task | Dept | Exit |
|---|------|------|------|
| A1 | M-B5.3-HIST HIST-a/b (ME-1 only) | Match/Sim | No CM93 menus in 1888 |
| A2 | ME-1 automated tests | QA/Eng | 4+ new tests green |
| A3 | C-1 double-click guard | Platform | M8 ready |
| A4 | Tag `InjuryEconomyB52Test` `@Tag("cm93")` | Match/Sim | PR default = R-1888 profile |

### Sprint B — automated sign-off + docs

| # | Task | Dept | Exit |
|---|------|------|------|
| B1 | Run `.\scripts\run-soak-with-preflight.ps1` | QA | PM sign-off |
| B2 | PL-5 / PL-7 fixes | Platform | M5/M1 reload |
| B3 | Manual ch.6–7 R-1888 | Manual | Trainer, no insurance |
| B4 | PM + PO sign design §5 | PM/PO | L4 invite |

### Sprint C — Human QA (L4)

| # | Task | Exit |
|---|------|------|
| C1 | B-9 checklist M1–M9 | Signed sheet |
| C2 | Log issues → Layer C / R-1889 backlog | PM triage |

---

## 6. Traffic lights by department

| Dept | Status | Note |
|------|--------|------|
| **Research** | 🟢 | CM/FM + medical eras complete |
| **Design** | 🟢 | R-1888 wrap-up matrix done |
| **Match/Sim** | 🟡 | ME-1 injury skin remaining |
| **Platform/UX** | 🟡 | C-1, PL-5, PL-7 |
| **QA automated** | 🟢 PR+nightly · 🟡 soak | 136 tests green |
| **QA human** | ⚪ | Correctly not started |
| **Manual** | 🟡 | CM93 wording in ch.7 |
| **PM** | 🟢 | This report + readiness doc |

---

## 7. Honest claims (stakeholders)

| Say | Do not say |
|-----|------------|
| “R-1888 targets CM1-minimum management loop” | “Full CM93 experience in 1888” |
| “131 automated tests + 5 nightly green” | “Ready for players” (human QA pending) |
| “Victorian injury care (ME-1) in design” | “Historically accurate injuries shipped” (until ME-1) |
| “Research covers 1906–1993 medicine” | “1906 insurance in game” |

---

## 8. Document map (R-1888 chain)

```
DESIGN_YEARLY_RELEASE_SCOPE.md          (naming)
ANALYSIS_R1888_RELEASE_READINESS_V1.md  (4-stage gate)
DESIGN_R1888_RELEASE_WRAP_UP_V1.md      (design sign-off)
PM_REPORT_R1888_RELEASE_WRAP_UP.md      (this doc)
RELEASE_R1888_DEFINITION.md             (acceptance criteria)
LAYER_B_TESTING_PYRAMID_V1.md           (L1–L4)
IMPLEMENTATION_* / manual 06–07
```

---

## 9. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial R-1888 wrap-up PM report — four-stage gate, blockers, sprint plan |

---

*Next PM update: after Sprint A (ME-1 + C-1) or soak sign-off.*
