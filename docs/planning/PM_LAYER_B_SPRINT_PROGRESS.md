# PM Report — Layer B Sprint Progress

**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Plan:** [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)  
**Benchmark:** [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md)  
**Tests:** [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md)

---

## Overall: ~88% of whole Layer B

| Phase | Features | L1 Unit | L2 E2E | L3 Long |
|-------|----------|---------|--------|---------|
| B1 | ✅ 95% (engine + Tactics XI UI) | ✅ | ✅ | ✅ |
| B2 | ✅ 100% | ✅ | ✅ | ✅ |
| B3 | ✅ 95% (ratings + preview/kick-off + result UI) | ✅ | ✅ | — |
| B4 | ✅ 90% (Tactics screen + sidebar + formation picker) | ✅ | ✅ | — |
| B5 | ✅ 85% (condition tick, injury, squad XI marker) | ✅ | ✅ | ✅ nightly |
| **L4 Human** | — | **blocked** until nightly soak signed off | | |

**PR gate:** `mvn test -pl futtoboru-core` → **112 tests green** (2026-06-30).

---

## Done ✅ (this sprint)

### Engine
- `MatchRatingsCalculator` + per-match rating maps on `Match`
- Preview → `kickOffMatch()` flow; player-club matches deferred on match day only
- `PlayerCondition`, `PlayerConditionService`, `InjuryGenerator`, post-match + daily tick
- `JsonLocalDateSerializer` for save/load of injury return dates
- `BestElevenSelector` uses game date; injured players excluded

### UI
- `MatchPreviewScreenTable` — fixture, lineups, **Kick off**
- `MatchResultScreenTable` — scorers + user-club ratings
- `TacticsScreenTable` — formation dropdown, 11 slot pickers, Best XI / Clear
- `TacticsButton` in manager sidebar
- Squad list **XI** column

### Tests added / green
- L1: `MatchRatingsTest`, `FormationSimWeightTest`, `PlayerFitnessTest`, `InjuryGeneratorTest`, `MoraleUpdateTest`
- L2: `MatchFeedbackIT`, `MatchLineupPreviewIT`, `Cm1ManagementLoopIT`, `FitnessSimModifierIT`, `InjuredPlayerLineupIT`, `PlayerConditionSaveLoadIT`
- L3: `LayerBMultiSeasonSoakIT` (`@Tag("nightly")`, 5 seasons)

---

## Remaining ❌ (to “Layer B complete”)

- Human **B-9** checklist (L4) — after PM signs off nightly `LayerBMultiSeasonSoakIT` + preflight soak
- Optional polish: preview inline XI edit, squad fitness/injury columns, opponent report stub
- Run `mvn test -pl futtoboru-core -Pnightly` and `.\scripts\run-soak-with-preflight.ps1` before L4

---

## Next engineering order

1. PM review: nightly tier + optional preflight soak  
2. **L4 human** B-9 walkthrough (local only — no public release)  
3. Layer C planning (transfers / training) — out of Layer B scope

---

*Update when a phase or test tier completes.*
