# Layer B — Early CM / FM Feature Benchmark

**Document type:** Gap analysis / design reference  
**Date:** 2026-06-30  
**Status:** **Active** — defines **whole Layer B** scope vs genre  
**Sources:** [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) · [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) (attendance only for B2)

**Master plan:** [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)

---

## 1. Scope rule

**Layer B = CM1 (1992) minimum management loop + CM1-tracked player state + FM attendance borrow.**

| Include in Layer B | Defer (Layer C+ / FM depth) |
|--------------------|----------------------------|
| Pick XI, formation, preview, result, 0–10 ratings | Transfers, scouting, training schedules |
| Fitness, morale, basic injuries (CM1/CM93 floor) | FM jadedness, press, staff roles |
| Abstract match sim driven by lineup | 2D/3D match engine, xG |
| Crowds from table + form + derby | Dynamic reputation, media |

---

## 2. Feature matrix — CM1 / early FM vs Futtoboru

| # | CM1 / genre feature | CM1 | Early FM (attendance) | Futtoboru ID | Code | Unit | E2E | Long | Human |
|---|---------------------|-----|----------------------|--------------|------|------|-----|------|-------|
| 1 | Pick starting XI (11) | ✅ | ✅ | MVP-1 | ⚠️ engine ✅ UI ❌ | ✅ | ✅ | — | ❌ |
| 2 | Formation presets | ✅ | ✅ | MVP-2 | ⚠️ registry ✅ UI ❌ | ✅ | ❌ | — | ❌ |
| 3 | Tactics menu | ✅ | ✅ | MVP-9 | ❌ | — | ❌ | — | ❌ |
| 4 | Match uses your XI | ✅ | ✅ | MVP-3 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 5 | Match preview before kick-off | ✅ | ✅ | MVP-7 | ❌ | — | ❌ | — | ❌ |
| 6 | Text/abstract result + score | ✅ | ✅ | MVP-6 | ⚠️ partial | — | ❌ | — | ❌ |
| 7 | Named scorers on result | ✅ | ✅ | MVP-6 | ⚠️ engine ✅ UI ❌ | — | ❌ | — | ❌ |
| 8 | **0–10 ratings** (genre first) | ✅ | ✅ | MVP-5 | ❌ | ❌ | ❌ | — | ❌ |
| 9 | Crowds react to context | ⚠️ implicit | ✅ | MVP-4 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 10 | Continue / advance time | ✅ | ✅ | A-2 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 11 | Fitness tracked | ✅ | ✅ | COND-1 | ❌ | ❌ | ❌ | — | ❌ |
| 12 | Morale tracked | ✅ | ✅ | COND-5 | ❌ | ❌ | ❌ | — | ❌ |
| 13 | Basic injuries | ⚠️ | CM93+ | COND-3 | ❌ | ❌ | ❌ | — | ❌ |
| 14 | Match fatigue / sharpness | ⚠️ | ✅ | COND-2,4 | ❌ | ❌ | ❌ | — | ❌ |
| 15 | Condition affects sim | ✅ | ✅ | COND-1+sim | ❌ | ❌ | ❌ | ❌ | ❌ |
| 16 | Squad shows fit / injured | ✅ | ✅ | COND UI | ❌ | — | ❌ | — | ❌ |
| 17 | Tactics + XI persist in save | ✅ | ✅ | DATA-B1 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 18 | Multi-season stability | ✅ | ✅ | MVP-8 | ✅ | — | ✅ | ✅ | ❌ |

**Legend:** Code = implemented · Unit/E2E/Long/Human = test tier green

---

## 3. What is done (2026-06-30)

### Engine & data ✅

| Component | Location |
|-----------|----------|
| `ClubTacticsSetup`, `MatchLineup`, 3 formations | `data/tactics/*`, `FormationRegistry` |
| `BestElevenSelector`, `MatchLineupBuilder` | `engine/tactics/*` |
| `MatchSimulator` v2 (XI only, scorers from XI) | `engine/simulation/MatchSimulator.java` |
| `AttendanceCalculator`, `DerbyRegistry` | `engine/simulation/*` |
| `EconomyHealthMonitor` | `engine/economy/*` |
| Scorers + lineup ids on `Match` | `data/Match.java` |

### Automated tests ✅ (Layers 1–2 partial)

| Test | Proves |
|------|--------|
| `FormationRegistryTest`, `BestElevenSelectorTest` | DATA-B1, MVP-1 |
| `MatchSimulatorV2Test`, `StartingElevenIT` | MVP-3 |
| `ClubTacticsSetupSaveLoadIT` | Persist tactics |
| `AttendanceCalculatorTest`, `AttendanceModelIT`, `AttendanceOverwriteIT` | MVP-4, H-1 |
| `EconomyHealthMonitorTest`, `LongRunEconomySoakIT` | ECON-B2 |
| `TenSeasonReportIT`, `MultiSeasonSoakIT` | Long-run sim stability |

### UI partial ⚠️

| Screen | State |
|--------|-------|
| Squad | Best XI button; no manual slots |
| Result | Score, attendance; no scorers/ratings |
| Preview | Fixture title only |
| Tactics | Placeholder; not in sidebar |

---

## 4. What is missing (whole Layer B)

### B3 — Feedback (CM1 ratings + result loop)

| Gap | Design |
|-----|--------|
| `MatchRatingsCalculator` | [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) |
| Preview → kick off → result flow | Same §2 |
| Result scorers + ratings UI | Same §2.4 |

### B4 — Tactics UX (CM1 tactics menu)

| Gap | Design |
|-----|--------|
| Manual XI slot UI | [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) |
| Formation dropdown (3 templates exist) | Same |
| Sidebar Tactics button | Same |

### B5 — Player condition (CM1 fitness/morale + CM93 injury floor)

| Gap | Design |
|-----|--------|
| `PlayerCondition` on `Player` | [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) |
| Daily tick + sim modifier | Same §4 |
| `InjuryGenerator` + squad columns | Same §5 |
| Exclude injured from XI | `BestElevenSelector` + IT |

**CM1 evidence:** squad tracked fitness and morale; CM93 adds injury types — B5 uses **small injury table**, not FM detail.

### Testing gaps (Layers 2–4)

| Missing test | Tier | Proves |
|--------------|------|--------|
| `MatchRatingsTest` | Unit | MVP-5 formula |
| `MatchFeedbackIT` | E2E | Ratings after sim |
| `MatchLineupPreviewIT` | E2E | Preview flow |
| `FormationSimWeightTest` | Unit | MVP-2 |
| `Cm1ManagementLoopIT` | E2E | Full headless CM1 loop |
| `LayerBMultiSeasonSoakIT` | Long | XI + ratings + condition over 5–10 seasons |
| `FitnessSimModifierIT`, `InjuredPlayerLineupIT` | E2E | B5 |
| Human checklist B-9 | Human | **After** L1–L3 green |

Pyramid: [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md)

---

## 5. Explicitly out of Layer B

| Feature | CM/FM | When |
|---------|-------|------|
| Transfers | CM1 ✅ | Layer C+ |
| Training menus | CM1 basic | Layer C+ |
| Match commentary text | CM1 ✅ | Optional polish post B-9 |
| Hotseat multiplayer | CM1 ✅ | Post–Layer B |
| FM tactics roles / OI | FM | Far horizon |

---

## 6. Completion estimate

| Phase | Weight | Done |
|-------|--------|------|
| B1 engine | 15% | **100%** |
| B1 UI | 10% | **25%** |
| B2 | 15% | **100%** |
| B3 | 20% | **0%** |
| B4 | 15% | **15%** |
| B5 | 15% | **0%** |
| Test pyramid L2–L3 | 10% | **40%** (B1/B2 only) |

**Overall Layer B: ~42%** (features + tests weighted).

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Whole-layer benchmark; B5 in scope; test tiers per feature |

---

*Update when a feature or test tier turns green.*
