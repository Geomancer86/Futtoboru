# Implementation — Player Condition B5 (as-built)

**Document type:** Implementation record — **what shipped in Layer B**  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Design baseline:** [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md)  
**Gap / next:** [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md)  

**Manual chapter:** [docs/manual/README.md](../manual/README.md) §6 — partial until B5.1  

---

## 1. Shipped scope

Layer B5 delivered **engine + partial UI** for fitness, morale, fatigue, sharpness (engine-only), and **minimal** injuries.

| COND ID | Shipped? | Depth |
|---------|----------|-------|
| COND-1 Fitness | ✅ | Partial |
| COND-2 Sharpness | ⚠️ tick only | Partial — no UI/sim |
| COND-3 Injuries | ⚠️ | Minimal |
| COND-4 Fatigue | ⚠️ | Partial |
| COND-5 Morale | ⚠️ | Partial — no goal bonus |

---

## 2. Code map

| Component | Path |
|-----------|------|
| Data model | `data/PlayerCondition.java` |
| Nested on player | `data/Player.java` → `getCondition()` |
| Daily tick | `engine/simulation/PlayerConditionService.tickDaily()` |
| Post-match | `PlayerConditionService.applyPostMatchEffects()` |
| Injury roll | `engine/simulation/InjuryGenerator.java` |
| Fitness sim | `PlayerConditionService.fitnessStrengthFactor()` → `LineupStrengthCalculator` |
| XI filter | `engine/tactics/BestElevenSelector.java`, `isAvailableForLineup()` |
| Engine integration | `engine/FuttoboruGameEngine.java` — daily tick call |
| Match sim hook | `engine/simulation/MatchSimulator.java` — post-match effects |
| Squad UI | `tables/squad/PlayersListTable.java` — Fit, Mor, Injury columns |
| JSON date save | `system/JsonLocalDateSerializer.java` |

---

## 3. Behaviour reference (authoritative for manual)

### 3.1 Defaults (new game / new player)

| Field | Default |
|-------|---------|
| fitnessPct | 100 |
| morale | 70 |
| sharpnessPct | 70 |
| fatiguePct | 0 |
| injury | none |

### 3.2 Daily tick (no match yesterday)

- fitness +2 (cap 100)  
- sharpness −1 (floor 40)  
- fatigue −3 (floor 0)  
- clear injury if `injuryReturnDate` before today  

### 3.3 After match (starting XI only)

- fitness −5  
- sharpness +5 (cap 100)  
- fatigue +8 (cap 100)  
- morale: win +3, loss −2, rating ≥8 +1, rating &lt;5 −1  
- injury roll per player (see §3.4)  

### 3.4 Injury roll

- Skip if already injured  
- Base chance 2%; +2% if fitness &lt; 60; +2% if fatigue &gt; 70  
- On hit: type always `strain`, display “Strain”, return date = today + 1–2 weeks (85%) or 3–5 weeks (15%)  
- RNG seeded from `match.getId()`  

### 3.5 Match simulation

- Slot strength multiplied by `0.5 + fitnessPct/200`  
- Sharpness, morale, fatigue **do not** affect strength in B5  

### 3.6 Lineup availability

- Injured players (`injuryReturnDate` ≥ today) excluded from Best XI and manual availability checks  

---

## 4. UI (as-built)

| Screen | Behaviour |
|--------|-----------|
| Squad | Columns: **Fit**, **Mor**, **Injury** (name or “Fit”) |
| Tactics | No injury/fitness visual guard |
| Result | No injury section |
| Player detail | Condition not dedicated section |

i18n: `fitness_pct`, `morale`, `injury_status` in `BaseBundle_en_uk.properties`.

---

## 5. Mod content

**None shipped** — injury types hardcoded in Java. Design files `mods/injuries/types.txt` not created.

---

## 6. Tests

| Class | Layer |
|-------|-------|
| `PlayerConditionDefaultsTest` | L1 |
| `PlayerFitnessTest` | L1 |
| `PlayerFormTest` | L1 |
| `FatigueRecoveryTest` | L1 |
| `MoraleUpdateTest` | L1 |
| `InjuryGeneratorTest` | L1 |
| `InjuredPlayerLineupIT` | L2 |
| `FitnessSimModifierIT` | L2 |
| `PlayerConditionSaveLoadIT` | L2 |

Covered in L3 `LayerBMultiSeasonSoakIT` (@Tag nightly).

---

## 7. Known gaps vs design (B5.1 backlog)

| Design item | Status |
|-------------|--------|
| Mod injury types | ❌ |
| Squad sharpness column | ❌ |
| Return date column | ❌ |
| Tactics grey-out | ❌ |
| Result injury line | ❌ |
| Goal → morale +2 | ❌ |
| Sharpness in sim | ❌ |
| Fatigue slows fitness recovery | ❌ |
| Inbox injury message | ❌ |

See [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md).

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial as-built record for Layer B5 ship |

---

*Update this file when B5.2 (insurance/physio) ships — add §10.*

---

## 9. B5.1 delta (Option A — 2026-06-30)

**PO decision:** Option A (floor) — no physio multiplier, no inbox.

| B5.1 item | Shipped |
|-----------|---------|
| `mods/injuries/types.txt` + `InjuryTypesRegistry` | ✅ |
| 5 weighted injury types + severity | ✅ |
| Squad Sharp + Return columns | ✅ |
| Tactics injured exclude + low-fitness warn | ✅ |
| Result INJURIES block | ✅ |
| Sharpness sim factor | ✅ |
| Goal → morale +2 | ✅ |
| High fatigue → slower fitness recovery | ✅ |
| `Match.homeInjuredPlayerIds` / away | ✅ |

**New tests:** `InjuryTypesRegistryTest`, `MoraleGoalBonusTest`, `FatigueSlowsRecoveryTest`, `PlayerSharpnessFactorTest`, `MatchInjuryTrackingTest`

**Manual:** [docs/manual/06-squad-and-condition.md](../manual/06-squad-and-condition.md) (draft)

**Depth after B5.1:** Injuries **Partial** (was Minimal); fitness/sharpness **Partial+**

**Still missing for CM93 Core:** ~~physio estimate, INJURY PRONE, Rehabilitate, insurance, Lilleshall~~ → shipped in **§10 B5.2 Complete**

---

## 10. B5.2 delta (Option B Complete — M-B5.2 — 2026-07-01)

**PO decision:** Option **B — M-B5.2 Complete** (Tier 1–3): full CM93 injury economy.

| Phase | Shipped |
|-------|---------|
| **B5.2a** INJURY PRONE + physio multiplier + estimate UI | ✅ |
| **B5.2b** Rehabilitate (MINOR/MODERATE) | ✅ |
| **B5.2c** Inbox MODERATE+ (`MessageCategory.SQUAD`) | ✅ |
| **B5.2d** Insure/Uninsure + weekly premium + insured wage cover | ✅ |
| **B5.2e** Lilleshall + surgeon abroad (rich-club gate) | ✅ |

### Code map (B5.2 additions)

| Component | Path |
|-----------|------|
| Economy mods | `mods/injuries/rehab_costs.txt`, `treatment_costs.txt`, `insurance.txt` |
| Registry | `system/loaders/InjuryEconomyRegistry.java` |
| Physio | `engine/simulation/PhysioService.java`, `Person.staffAbility`, profession `Physio` (id 9) |
| Injury roll | `InjuryGenerator.applyInjury()` — proneness + physio weeks |
| Treatments | `InjuryTreatmentService.java` |
| Finances | `ExpenseCalculator` — insurance premiums in other expenses; skip insured injured wages |
| Inbox | `MessageManager.createInjuryNewsMessage()` |
| UI | `PlayerDetailScreenTable` (Rehabilitate, Insure, Lilleshall, Surgeon), squad Return estimate, club physio row |

### Behaviour (authoritative)

- **INJURY PRONE:** hidden 1–20 on `Player`; default rolled at attribute gen; modifier `(prone − 10) × 0.001` on roll.  
- **Physio:** ability 1–5 (Fair→Superb); multiplier `1.20 − (ability × 0.07)` on base weeks; estimate shown as `~N wk (OK physio)`.  
- **Rehabilitate:** MINOR −1 wk / MODERATE −2 wk; costs from mod; free if insured.  
- **Insurance:** 5% of weekly wage premium; weekly tick; wages not charged while injured if insured.  
- **Lilleshall:** SERIOUS only; remaining weeks × 0.6; cost £300 mod default.  
- **Surgeon abroad:** SERIOUS + balance ≥ £500; remaining weeks × 0.5 (10% fail roll); cost £500.  
- **Inbox:** MODERATE+ injuries on human manager club after match.

### Tests (131 green)

**New:** `InjuryEconomyB52Test` (8 cases — proneness, physio, rehab, insurance, wages, Lilleshall, surgeon, inbox)

**Test isolation note:** `GameTestHarness` reuses `DatabaseLoader` club objects across runs. B5.2 ITs reset squad injury/insurance state in `@BeforeEach` — see `resetSquadForTests()`. Without this, earlier tests in the full suite left players injured/insured and caused order-dependent failures (`NOT_FIT_FOR_INSURANCE`).

**Manual:** [06-squad-and-condition.md](../manual/06-squad-and-condition.md), [07-player-actions-insurance.md](../manual/07-player-actions-insurance.md)

**Depth after B5.2 Complete:** Injuries **Core+** (~100% CM93 Tier 2+3 parity per [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md))

---

## 11. Historical accuracy gap (M-B5.3-HIST — not shipped)

B5.2 reproduces **CM93 (1993)** treatment menus against an **1888–89** start mod. That is **correct for CM parity** and **incorrect for period simulation** until era gating lands.

| Issue | As-built | Target (proposed) |
|-------|----------|-------------------|
| Lilleshall / insurance / surgeon abroad visible from day one | Always on | Hidden until medical era ≥ ME-4/ME-6 |
| Staff labelled “Physio” | CM93 string | “Trainer” in ME-1 |
| Rehab costs £25–75 | CM93 magnitudes in “1888” mod files | ME-1 table ~£0.25–0.75 |
| Recovery harshness | CM93 baseline | ME-1 ×1.20 base weeks |

**Research:** [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md)  
**Design:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md)  
**Audit:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9  

**PO framing:** Claim **CM93 injury economy** today; do **not** claim **1888-historical injury simulation** until §11 closed.

### Changelog

| Date | Change |
|------|--------|
| 2026-06-25 | §11 historical accuracy gap + M-B5.3-HIST links |
| 2026-07-01 | B5.2 Complete shipped (Option B) |
