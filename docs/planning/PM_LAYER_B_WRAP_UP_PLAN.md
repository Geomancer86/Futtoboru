# PM Plan — Layer B Complete (Whole Feature Set)

**Document type:** PM master plan  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Direction:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md)  
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

**Overall progress: ~42%** — see benchmark doc for per-feature matrix.

**Out of Layer B:** transfers, training menus, 2D engine, 1892, world news, public release.

---

## 2. Done vs missing (summary)

| Phase | Feature scope | Status |
|-------|---------------|--------|
| **B1** | XI + sim v2 + save | Engine ✅ · UI ⚠️ Best XI only |
| **B2** | Attendance + economy | ✅ Complete + tested |
| **B3** | Ratings + preview/result flow | ❌ Not started — design ready |
| **B4** | Tactics menu + formation picker | ❌ Registry ✅ · UI ❌ |
| **B5** | Fitness, morale, injuries (CM1/CM93) | ❌ Design ready — **in Layer B scope** |

### Testing tiers

| Tier | Status |
|------|--------|
| **L1 Unit** | B1/B2 ✅ · B3/B4/B5 ❌ |
| **L2 E2E** | Partial ✅ · loop/preview/condition ❌ |
| **L3 Long-term** | Season soaks ✅ · **`LayerBMultiSeasonSoakIT`** ❌ |
| **L4 Human** | **Blocked** until L1–L3 green |

---

## 3. Gap analysis (designs for all missing work)

| Gap | CM/FM basis | Design doc |
|-----|-------------|------------|
| Ratings 0–10 | CM1 **genre first** | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) |
| Preview → kick off | CM1 match week | Same |
| Result scorers + ratings | CM1 result screen | Same |
| Manual XI + Tactics menu | CM1 main menu | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) |
| Formation picker | CM1 pyramid variants | Same + 3 templates in `FormationRegistry` |
| Player fitness | CM1 squad energy | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) |
| Morale | CM1 tracked | Same COND-5 |
| Basic injuries | CM93 floor | Same COND-3 |
| Condition → sim | CM1 implicit | Same §4.1 + `FitnessSimModifierIT` |

---

## 4. Execution order (whole layer)

```
B3a  MatchRatingsCalculator + Match fields + L1 tests
B3b  Preview → kick off → result engine flow + L2 ITs
B3c  Result/preview UI (scorers, ratings)
B4   Tactics screen + sidebar + formation picker + L1 FormationSimWeightTest
B1UI Manual XI slots on Tactics + squad XI marker
B5   PlayerCondition + daily tick + injury gen + squad UI + L1/L2 tests
INT  Cm1ManagementLoopIT + LayerBMultiSeasonSoakIT (L3)
     mvn test + nightly + preflight soak — all green
L4   Human testers → B-9 checklist
```

**Estimate:** **8–12 eng-weeks** (one developer) for remaining **~58%**.

---

## 5. Sprint detail

### B3 — Feedback ([design](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md))

| Deliverable | L1 | L2 |
|-------------|----|----|
| `MatchRatingsCalculator` + `Match` rating maps | `MatchRatingsTest` | `MatchFeedbackIT` |
| Continue → preview on match day | — | `MatchLineupPreviewIT` |
| Kick off → result navigation | — | `Cm1ManagementLoopIT` (partial) |
| Result UI scorers + ratings | — | manual prep for L4 |

### B4 + B1 UI ([design](DESIGN_TACTICS_UI_V1.md))

| Deliverable | L1 | L2 |
|-------------|----|----|
| `TacticsScreenTable` + sidebar | `FormationSimWeightTest` | `Cm1ManagementLoopIT` |
| Manual 11 slot assignment | — | `ClubTacticsSetupSaveLoadIT` regression |

### B5 — Condition ([design](DESIGN_PLAYER_CONDITION_V1.md))

| Deliverable | L1 | L2 | L3 |
|-------------|----|----|-----|
| `PlayerCondition` on `Player` | `PlayerFitnessTest`, `MoraleUpdateTest` | `PlayerConditionSaveLoadIT` | — |
| Daily tick in engine | `FatigueRecoveryTest`, `PlayerFormTest` | — | — |
| `InjuryGenerator` | `InjuryGeneratorTest` | `InjuredPlayerLineupIT` | — |
| Sim modifier | — | `FitnessSimModifierIT` | `LayerBMultiSeasonSoakIT` |
| Squad UI columns (fit, morale, injury) | — | — | L4 M9 |

**B5 scope (CM1-aligned):** fitness + morale + **simple** injuries (2–3 types, 1–4 weeks) — not FM medical centre.

### Integration — long-term

| Test | Proves |
|------|--------|
| `Cm1ManagementLoopIT` | Full headless CM1 loop one match |
| `LayerBMultiSeasonSoakIT` | 5–10 seasons: tactics persist, ratings every match, condition bounded, health gates clean |
| `LongRunSoakIT` + preflight | No regression at 60 seasons after Layer B |
| `MultiSeasonLeagueAndCupSoakIT` | Cup path with Layer B features |

Tag `LayerBMultiSeasonSoakIT` as `@Tag("nightly")` initially.

---

## 6. Layer B exit criteria

### Automated (L1–L3) — required before human testers

- [ ] All L1 tests in [LAYER_B_TESTING_PYRAMID_V1.md §4](LAYER_B_TESTING_PYRAMID_V1.md) **green**
- [ ] All L2 tests **green**
- [ ] `TenSeasonReportIT` + `LayerBMultiSeasonSoakIT` + preflight **`LongRunSoakIT`** **green**
- [ ] `mvn test -pl futtoboru-core` **100%** PR suite

### Feature (from CM1 benchmark)

- [ ] Manual XI + Best XI + 3 formations  
- [ ] Tactics in sidebar  
- [ ] Preview → kick off → result with scorers + ratings  
- [ ] Fitness/morale visible; injured excluded from XI  
- [ ] Condition affects sim (`FitnessSimModifierIT`)

### Human (L4) — after automated green

- [ ] [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) M1–M9 signed  
- [ ] PM notes issues for Layer C backlog — **not a release gate**

---

## 7. PO decisions (updated)

| # | Decision | Locked answer |
|---|----------|---------------|
| D1 | B5 in Layer B? | **Yes** — CM1 tracked fitness/morale |
| D2 | Manual XI required? | **Yes** |
| D3 | Human testers when? | **After L1–L3 automated green** |
| D4 | Auto-sim other clubs on Continue? | **Yes** — preview only for user match |

---

## 8. Document map

| Doc | Role |
|-----|------|
| [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md) | Per-feature done/missing + CM1 reference |
| [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) | Unit → E2E → long → human |
| [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) | B3 |
| [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) | B4 + B1 UI |
| [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) | B5 |
| [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) | Detailed test specs + human checklist |

---

## 9. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Whole Layer B scope: B5 + test pyramid + human QA last |
| 2026-06-30 | Initial wrap-up (~55%, B5 deferred) — superseded |

---

*Update benchmark matrix when features or test tiers turn green.*
