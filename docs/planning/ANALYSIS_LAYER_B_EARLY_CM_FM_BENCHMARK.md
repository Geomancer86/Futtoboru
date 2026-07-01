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
| 1 | Pick starting XI (11) | ✅ | ✅ | MVP-1 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 2 | Formation presets | ✅ | ✅ | MVP-2 | ✅ | ✅ | ✅ | — | ❌ |
| 3 | Tactics menu | ✅ | ✅ | MVP-9 | ✅ | — | ✅ | — | ❌ |
| 4 | Match uses your XI | ✅ | ✅ | MVP-3 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 5 | Match preview before kick-off | ✅ | ✅ | MVP-7 | ✅ | — | ✅ | — | ❌ |
| 6 | Text/abstract result + score | ✅ | ✅ | MVP-6 | ✅ | — | ✅ | — | ❌ |
| 7 | Named scorers on result | ✅ | ✅ | MVP-6 | ✅ | — | ✅ | — | ❌ |
| 8 | **0–10 ratings** (genre first) | ✅ | ✅ | MVP-5 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 9 | Crowds react to context | ⚠️ implicit | ✅ | MVP-4 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 10 | Continue / advance time | ✅ | ✅ | A-2 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 11 | Fitness tracked | ✅ | ✅ | COND-1 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 12 | Morale tracked | ✅ | ✅ | COND-5 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 13 | Basic injuries | ⚠️ | CM93+ | COND-3 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 14 | Match fatigue / sharpness | ⚠️ | ✅ | COND-2,4 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 15 | Condition affects sim | ✅ | ✅ | COND-1+sim | ✅ | ✅ | ✅ | ✅ | ❌ |
| 16 | Squad shows fit / injured | ✅ | ✅ | COND UI | ✅ | — | — | — | ❌ |
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
| `MatchSimulator` v2 + ratings + condition hooks | `engine/simulation/*` |
| `MatchRatingsCalculator` | `engine/simulation/MatchRatingsCalculator.java` |
| `PlayerCondition`, `PlayerConditionService`, `InjuryGenerator` | `data/PlayerCondition.java`, `engine/simulation/*` |
| `AttendanceCalculator`, `DerbyRegistry` | `engine/simulation/*` |
| Preview → kick-off flow | `FuttoboruGameEngine.kickOffMatch` |

### UI ✅

| Screen | State |
|--------|-------|
| Squad | Best XI + XI marker + Fit/Mor/Injury columns |
| Tactics | Formation + manual XI + sidebar |
| Preview | Lineups + Kick off |
| Result | Score, scorers, ratings, attendance |

### Automated tests ✅ (L1–L3)

**117 PR-gate tests + 5 nightly tests — all green.** See [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md).

---

## 4. What remains before human QA

| Gap | Tier |
|-----|------|
| Preflight 60-season soak | L3 gate |
| Human B-9 checklist | L4 |
| Optional polish: tactics injury grey-out, result injury line | Post B-9 |

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
| B1 UI | 10% | **100%** |
| B2 | 15% | **100%** |
| B3 | 20% | **100%** |
| B4 | 15% | **100%** |
| B5 | 15% | **95%** |
| Test pyramid L2–L3 | 10% | **100%** (L4 human pending) |

**Overall Layer B: ~95% automated** — human B-9 + preflight soak remain.

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Whole-layer benchmark; B5 in scope; test tiers per feature |

---

*Update when a feature or test tier turns green.*
