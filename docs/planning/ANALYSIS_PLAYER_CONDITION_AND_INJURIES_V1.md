# Analysis — Player Condition, Fitness & Injuries (CM research + as-built)

**Document type:** Research synthesis + codebase audit  
**Date:** 2026-06-30  
**Status:** **Historical as-built (pre-B5.2)** — see [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) for current parity  
**Audience:** PO, Match/Sim, QA, manual authors  

**Chain:** [RESEARCH_EARLY_CM_INJURIES_VS_FM.md](../research/RESEARCH_EARLY_CM_INJURIES_VS_FM.md) → this doc → [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md) → [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md)

---

## 1. Questions this analysis answers

1. What did CM1 and CM93 actually model for fitness and injuries?  
2. What did we **design** (B5), what did we **build**, and where do they diverge?  
3. What must change to reach CM93 **Core** injury floor without FM medical depth?  
4. What can a future game manual chapter truthfully say **today** vs after B5.1?

---

## 2. Historical research summary

### 2.1 CM1 (1992)

| Topic | Evidence | Depth |
|-------|----------|-------|
| Fitness | Team **energy average** on squad; morale aggregate | **Core** (aggregate) |
| Morale | Tracked; unhappy `[U]` icon | **Core** (basic) |
| Injury state | `[IJ]` icon; post-match flags on ratings screen | **Partial** |
| Injury subsystem | No insurance, physio estimate, or treatment chapter in manual | Lighter than CM93 |
| Body parts | **None** | N/A |

Sources: [CM1_Manual_Menu_Extract.md](../research/manuals/cm1/CM1_Manual_Menu_Extract.md), [CM1_REFERENCE.md §4.4](CM1_REFERENCE.md).

### 2.2 CM93/94 (1993)

| Topic | Evidence | Depth |
|-------|----------|-------|
| Per-player fitness | Squad availability column | **Core** |
| Injury description | Text condition on squad screen | **Core** |
| Recovery estimate | **Physio** gives weeks to return | **Core** |
| INJURY PRONE | Hidden attribute | **Core** |
| Physio staff | Ability Fair→Superb; faster recovery | **Core** |
| Insurance | Weekly cost; covers treatment, Lilleshall, ops, forced retirement | **Core** |
| Lilleshall | Optional rehab — faster recovery, cost | **Core** |
| Surgeon abroad | Serious injuries; rich clubs | **Core** |
| Match presentation | Commentary “taken off injured”; `[IJ]` | **Core** |
| News | Squad injury items | **Core** |
| Body parts | **None** — text injury only | N/A |

Sources: [CM93_Manual_LemonAmiga_Extract.md](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md), [StrategyWiki CM93 Your team](https://strategywiki.org/wiki/Championship_Manager_93/Your_team).

### 2.3 Modern FM (out of scope)

Anatomical areas, medical centre, rehab phases, sickness — **defer**. Futtoboru B5 target = **CM93 text injury floor**.

---

## 3. Futtoboru design intent (B5)

Authoritative design: [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) (2026-06-30).

| COND ID | Designed behaviour |
|---------|-------------------|
| COND-1 | `fitnessPct` 0–100; match −5; rest +2; sim multiplier |
| COND-2 | `sharpnessPct`; match +5; idle decay |
| COND-3 | Post-match injury roll; **mod injury types**; squad + result UI |
| COND-4 | `fatiguePct`; injury risk modifier |
| COND-5 | Morale from result + rating |

Designed UI: Fit%, Sharp%, Morale columns; injury + **return date**; tactics grey-out; result injury line.

---

## 4. As-built audit (2026-06-30)

### 4.1 Data model — **Partial** (model richer than usage)

| Field | Class | Persisted | Used in sim/UI |
|-------|-------|-----------|------------------|
| `fitnessPct` | `PlayerCondition` | ✅ | ✅ sim + squad |
| `morale` | `PlayerCondition` | ✅ | ✅ squad; ❌ sim |
| `fatiguePct` | `PlayerCondition` | ✅ | ✅ injury roll only |
| `sharpnessPct` | `PlayerCondition` | ✅ | ❌ not in sim; ❌ not in squad UI |
| `injuryTypeId` / display / return | `PlayerCondition` | ✅ | Partial squad label |

### 4.2 Systems — implementation map

| System | File | Design match |
|--------|------|--------------|
| Daily tick | `PlayerConditionService.tickDaily()` | ✅ rest fitness, fatigue decay, sharpness −1 idle, clear expired injury |
| Post-match | `PlayerConditionService.applyPostMatchEffects()` | ✅ fitness/sharpness/fatigue/morale/injury roll |
| Fitness sim hook | `PlayerConditionService.fitnessStrengthFactor()` | ✅ `0.5 + fitness/200` |
| Injury roll | `InjuryGenerator.maybeInjure()` | ⚠️ **Minimal** — single type |
| XI exclusion | `BestElevenSelector`, `isAvailableForLineup()` | ✅ |
| Engine hook | `FuttoboruGameEngine` daily tick | ✅ |

### 4.3 Constants (as-built)

| Constant | Value | Design doc |
|----------|-------|------------|
| `MATCH_FITNESS_COST` | 5 | ✅ flat −5 |
| `REST_FITNESS_GAIN` | 2 | ✅ |
| `MATCH_SHARPNESS_GAIN` | 5 | ✅ |
| `MATCH_FATIGUE_GAIN` | 8 | Design implied similar |
| `DAILY_FATIGUE_RECOVERY` | 3 | ✅ |
| `BASE_INJURY_CHANCE` | 0.02 (+ modifiers) | ✅ |
| Injury types | 1 (`strain`) | ❌ design: mod table |
| Morale deltas | win +3, loss −2, rating ±1 | ✅ (goal +2 in design — **not implemented**) |

**Gap:** Design §4.5 lists “Goal scored +2” — not in `applyPostMatchEffects()` today.

### 4.4 UI — implementation map

| Screen | Designed | Built |
|--------|----------|-------|
| Squad Fit% | ✅ | ✅ `PlayersListTable` |
| Squad Morale | ✅ | ✅ |
| Squad Sharp% | ✅ | ❌ |
| Squad injury + return date | ✅ | ⚠️ label only; no return date column |
| Tactics grey-out injured | ✅ | ❌ |
| Result injury line | ✅ | ❌ |

### 4.5 Mod content

| Designed file | Exists |
|---------------|--------|
| `mods/injuries/types.txt` | ❌ |
| `mods/condition/defaults.txt` | ❌ |

### 4.6 Tests (automation proof)

| Test | Proves |
|------|--------|
| `PlayerConditionDefaultsTest` | Defaults |
| `PlayerFitnessTest` | Fitness factor |
| `PlayerFormTest` | Sharpness after match |
| `FatigueRecoveryTest` | Daily fatigue decay |
| `MoraleUpdateTest` | Morale deltas |
| `InjuryGeneratorTest` | Strain injury on forced roll |
| `InjuredPlayerLineupIT` | XI exclusion |
| `FitnessSimModifierIT` | Low fitness weakens sim |
| `PlayerConditionSaveLoadIT` | Persistence |

**Manual implication:** We can document fitness, morale, basic injury exclusion with test backing. We **cannot** document injury types, physio, insurance, or sharpness UI yet.

---

## 5. Gap matrix — design vs built vs CM93

| Capability | B5 design | B5 built | CM93 | B5.1 proposal |
|------------|-----------|----------|------|---------------|
| Fitness % + sim effect | ✅ | ✅ | ✅ | Polish + manual text |
| Morale % | ✅ | ✅ (no goal bonus) | ✅ | Add goal bonus; optional sim hook |
| Sharpness % | ✅ | ⚠️ engine only | ⚠️ | UI column + sim hook |
| Fatigue % | ✅ | ⚠️ partial | ✅ | Recovery ↓ when high fatigue |
| Injury types (3–5) | ✅ | ❌ | ✅ | **Mod table + loader** |
| Injury severity | ✅ enum in design | ❌ | ✅ implied | Enum on `PlayerCondition` |
| Return date in UI | ✅ | ❌ column | ✅ | Squad column |
| Result injury line | ✅ | ❌ | ✅ | Result screen |
| Tactics grey-out | ✅ | ❌ | ✅ | Tactics screen |
| INJURY PRONE | ❌ out of B5 | ❌ | ✅ | B5.1 optional |
| Physio recovery mult. | ❌ out of B5 | ❌ | ✅ | B5.1 optional |
| Insurance / Lilleshall | ❌ out of B5 | ❌ | ✅ | B5.2 / Layer C |
| Inbox injury message | optional | ❌ | ✅ | B5.1 optional |
| Mod `injuries/types.txt` | ✅ | ❌ | N/A | B5.1 |

---

## 6. Fitness deep dive — player-manual wording (draft)

**Fit% (0–100)**  
Measures how ready a player is for physical exertion. Playing in the starting eleven reduces fitness; resting days without a match restore it. Very low fitness makes a player perform worse in matches and may increase injury risk.

**As-built formula (internal):** match strength multiplier = `0.5 + fitnessPct/200` (50% effectiveness at 0 fitness).

**Sharpness (designed, partially built)**  
Tracks match readiness from recent playing time. Increases when a player starts; slowly drops when idle. **Not yet shown in squad UI or match strength.**

**Fatigue (partial)**  
Builds up from matches; recovers daily. High fatigue increases injury chance in the post-match roll.

**Morale (0–100)**  
Rises after wins and good ratings; falls after losses and poor ratings. **Does not yet affect match simulation.**

---

## 7. Injuries deep dive — player-manual wording

### What manual can say today (B5 as-built)

> If a player is injured, they cannot be selected in your starting eleven. The squad list shows **Injury** with the label **Strain**. Injuries occur rarely after matches and keep the player out for a number of weeks.

### What manual must NOT say yet

- Multiple injury types (bruise, knee, etc.)  
- Physio staff shortening recovery  
- Insurance or treatment choices  
- Lilleshall or surgery abroad  

### What CM93 manual said (target tone for B5.1)

> When a player is injured, the squad screen shows a **description of the condition** and the **physio’s estimate** of how long recovery will take. For serious injuries you may send a player for specialist treatment. Insuring players covers treatment costs.

(B5.1 proposal simplifies insurance/Lilleshall to later phase — see design doc.)

---

## 8. Recommendations for PO decision

| Option | Scope | Manual-ready? | Effort |
|--------|-------|---------------|--------|
| **A — B5.1 floor** | 5 injury types, mod loader, UI columns, result line, tactics grey-out, sharpness in sim + UI | ⚠️ One manual chapter | ~3–5 days |
| **B — B5.1 + physio** | A + physio ability on recovery weeks + INJURY PRONE | ✅ CM93-aligned prose | +2 days |
| **C — B5.1 + inbox only** | A + mandatory injury inbox message | ⚠️ | +1 day |
| **D — defer** | Keep minimal stub | ❌ manual must warn “early access” | 0 |

**Recommendation:** **Option A** minimum before calling injuries “documented for release”; **M-B5.2 Core** before any **“CM93 parity”** claim — see [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md).

---

## 10. CM93 parity milestone (M-B5.2) — superseded

| Tier | Status (2026-07-01) | PO claim |
|------|---------------------|----------|
| Tier 1 — existence | **100%** ✅ | “We have injuries” |
| Tier 2 — management | **100%** ✅ | “CM93-style management” |
| Tier 3 — economy | **83%** (5/6; T3-6 deferred) | “Full CM93 injury economy” |

**Overall injury parity: ~95%** — [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) · [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md) §7

**PO decision:** Option **B Complete** shipped — [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10.

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-07-01 | §10 superseded by B5.2 Complete; link to parity status analysis |
| 2026-06-30 | Initial analysis — research + as-built + gap matrix |
| 2026-06-30 | M-B5.2 parity milestone cross-link |

---

*Current truth: [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md). §4–§7 here describe pre-B5.2 as-built only.*
