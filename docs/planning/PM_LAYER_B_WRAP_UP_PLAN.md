# PM Plan — Layer B Complete (Whole Feature Set)

**Document type:** PM master plan  
**Date:** 2026-06-30 (R-1888 wrap-up 2026-07-03)  
**Branch:** `analyze-ai-branch`  
**Direction:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md)  
**First release:** **R-1888** — [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md)  
**Benchmark:** [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md)  
**Testing:** [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) — **human QA last**

---

## 1. Layer B complete = whole CM1 management layer

Not a partial MVP. **All** of B1–B5 plus full automated test pyramid before human testers.

```
Pick XI + formation (UI)
    → preview → kick off
    → abstract match (lineup + condition)
    → scorers + 0–10 ratings + attendance
    → fitness / morale / injuries tick
    → repeat for many seasons (automated soak)
    → THEN human CM1 walkthrough (B-9)
```

**Overall progress: ~88% development · ~93% automated · ~92% pre-human (R-1888)** — see [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md).

**Out of R-1888:** transfers, training menus, 2D engine, 1892, world news, CM93 injury UI, public release.

---

## 2. Done vs missing (summary)

| Phase | Feature scope | Status |
|-------|---------------|--------|
| **B1** | XI + sim v2 + save | ✅ Engine + Tactics XI UI |
| **B2** | Attendance + economy | ✅ Complete + tested |
| **B3** | Ratings + preview/result flow | ✅ Engine + UI · L1/L2 green |
| **B4** | Tactics menu + formation picker | ✅ UI + sidebar · L1 green |
| **B5** | Fitness, morale, injuries | ✅ B5.2 engine · **ME-1 R-1888 skin pending** |

### Testing tiers

| Tier | Status |
|------|--------|
| **L1 Unit** | ✅ B1–B5 (131 PR tests) |
| **L2 E2E** | ✅ Loop/preview/condition |
| **L3 Long-term** | ✅ nightly 5 green · ☐ soak PM sign-off |
| **L4 Human** | ⏸ **Blocked** — ME-1 + UI fixes + soak |

---

## 3. R-1888 blockers (before human QA)

| Blocker | Owner | Doc |
|---------|-------|-----|
| M-B5.3-HIST ME-1 injury skin | Match/Sim | [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) |
| C-1 Continue double-click | Platform | [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md) |
| PL-5 history click / PL-7 save UI | Platform | Same |
| Soak preflight sign-off | QA/PM | [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) |
| Manual ch.6–7 R-1888 | Manual | [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) R-D22 |

---

## 4. Layer B exit criteria

### Automated (L1–L3) — required before human testers

- [x] All L1 tests in [LAYER_B_TESTING_PYRAMID_V1.md §4](LAYER_B_TESTING_PYRAMID_V1.md) **green**
- [x] All L2 tests **green**
- [x] Nightly profile **green** (5 tests)
- [ ] ME-1 injury tests green (with HIST-a/b)
- [ ] Preflight **`LongRunSoakIT`** PM sign-off
- [x] `mvn test -pl futtoboru-core` **131/131**

### Feature (from CM1 benchmark)

- [x] Manual XI + Best XI + 3 formations  
- [x] Tactics in sidebar  
- [x] Preview → kick off → result with scorers + ratings  
- [x] Fitness/morale visible; injured excluded from XI  
- [x] Condition affects sim (`FitnessSimModifierIT`)
- [ ] **ME-1** injury UX (Trainer, no CM93 menus)

### Human (L4) — after automated + dev blockers

- [ ] [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) M1–M9 signed  
- [ ] PM notes issues for R-1889 / Layer C backlog

---

## 5. Document map

| Doc | Role |
|-----|------|
| [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) | **Active PM status** |
| [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) | Four-stage gate |
| [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) | Design sign-off |
| [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md) | Acceptance criteria |
| [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) | Unit → E2E → long → human |

---

## 6. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | R-1888 wrap-up; accurate %; blockers; 131+nightly tests |
| 2026-06-30 | Whole Layer B scope: B5 + test pyramid + human QA last |
| 2026-06-30 | Initial wrap-up (~55%, B5 deferred) — superseded |

---

*Human testers: see [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) §4.*
