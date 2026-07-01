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

*Update this file when B5.1 ships — add §9 B5.1 delta or separate IMPLEMENTATION_B5_1 doc.*
