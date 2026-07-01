# Design Proposal — Player Condition B5.1 (Injuries & Fitness uplift)

**Document type:** Design proposal — **Option A approved & implemented**  
**Date:** 2026-06-30  
**Status:** **Shipped (B5.1 Option A)** — 2026-06-30  
**Audience:** PO, Match/Sim, UX, QA  

**Inputs:** [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) · [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) · [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md)

**Baseline implementation:** [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md)

---

## 1. Proposal summary

Elevate player condition from **Minimal/Partial** to **CM93 Core floor** for injuries and **Partial+** for fitness/sharpness — without FM body-part medical simulation.

| Track | Outcome |
|-------|---------|
| **Injuries** | Mod-driven types; severity; UI + result feedback; optional physio multiplier |
| **Fitness** | Close design gaps (goal morale); document formulas |
| **Sharpness** | Visible in squad; affects lineup strength |
| **Fatigue** | Slows fitness recovery when high |
| **Manual** | Enables honest `docs/manual/06-squad-and-condition.md` |

---

## 2. Scope options (pick one at PO meeting)

### Option A — **B5.1 floor** (recommended minimum)

| Include | Exclude |
|---------|---------|
| 5 injury types via mod | Insurance, Lilleshall, surgeon abroad |
| Severity enum (MINOR/MODERATE/SERIOUS) | Forced retirement |
| Squad: return date column, sharpness column | Full staff hiring UI |
| Tactics: grey-out injured + fitness &lt; 60 warn | |
| Result: injury lines for match | |
| Sharpness in `LineupStrengthCalculator` | |
| Goal-scored morale +2 (design debt) | |

### Option B — **B5.1 + physio** (CM93 parity claim)

Everything in **A**, plus:

| Include |
|---------|
| `injuryProne` 1–20 on player (hidden in 1888 UI; visible in debug) |
| Club physio `ability` → recovery week multiplier (0.85–1.15) |
| Squad shows physio estimate text: “~N weeks (physio)” |

### Option C — **B5.1 + comms**

Everything in **A**, plus:

| Include |
|---------|
| Inbox message on SERIOUS injury |
| Optional: in-match injury flag on result header (no live commentary) |

**PO lock needed:** A, B, or C before engineering starts.

---

## 3. Data model changes

### 3.1 `PlayerCondition` (extend)

```java
// Existing fields unchanged

// Add:
InjurySeverity injurySeverity;   // MINOR, MODERATE, SERIOUS — set with injury
Integer injuryWeeksRemaining;    // denormalized for UI; synced from return date
```

Keep `injuryReturnDate` as source of truth for save/load. `injuryWeeksRemaining` recalculated on load from dates.

### 3.2 `Player` (optional B)

```java
Integer injuryProneness;  // 1–20, default 10; null = 10
```

### 3.3 Mod: `mods/injuries/types.txt`

```
# id | display_name | min_weeks | max_weeks | severity | base_weight
strain | Strain | 1 | 2 | MINOR | 40
bruise | Bruise | 1 | 3 | MINOR | 25
twisted_ankle | Twisted ankle | 2 | 4 | MODERATE | 20
knee | Knee injury | 3 | 6 | MODERATE | 10
serious | Serious injury | 6 | 12 | SERIOUS | 5
```

Loader: `InjuryTypesLoader` — fallback to hardcoded table if file missing (same pattern as `DerbyRegistry`).

### 3.4 Mod: `mods/condition/defaults.txt` (optional)

```
default_fitness=100
default_morale=70
default_sharpness=70
base_injury_chance=0.02
```

---

## 4. Injury generation (revised)

### 4.1 Roll pipeline

```
for each player in XI after match:
  if already injured → skip
  chance = base_injury_chance
         + (fitness < 60 ? 0.02 : 0)
         + (fatigue > 70 ? 0.02 : 0)
         + (injuryProneness - 10) * 0.001   // Option B only
  if roll fails → continue
  pick type weighted from mod table
  weeks = random(min, max)
  if Option B: weeks = round(weeks * physioRecoveryMultiplier(club))
  set injury fields + return date
  if SERIOUS && Option C → enqueue inbox message
```

### 4.2 Physio multiplier (Option B)

```java
// physio ability: 1=Fair .. 5=Superb mapped from ClubStaff
multiplier = 1.15 - (abilityIndex * 0.06)  // Superb ≈ 0.85 weeks
clamp weeks minimum 1
```

---

## 5. Fitness & sharpness (revised)

### 5.1 Morale — close design debt

Add to `applyPostMatchEffects`:

| Event | Δ |
|-------|---|
| Player scored (in scorer list) | +2 |

### 5.2 Sharpness sim hook

```java
sharpnessFactor = 0.7 + (sharpnessPct / 333.0)  // 70% at 0, 100% at 100
slotStrength *= sharpnessFactor
```

Apply in `LineupStrengthCalculator` alongside fitness.

### 5.3 Fatigue recovery interaction

In `tickDaily`, when recovering fitness on rest days:

```java
if fatiguePct > 50:
  fitnessGain = 1  // instead of 2
```

---

## 6. UI specification

### 6.1 Squad (`PlayersListTable`)

| Column | Content |
|--------|---------|
| Fit% | existing |
| Sharp% | **new** |
| Mor | existing |
| Injury | display name or “Fit” |
| Return | **new** — `dd MMM` or “—” |

i18n keys to add: `sharpness_pct`, `injury_return`, `condition_fit_warning`.

### 6.2 Tactics (`TacticsScreenTable`)

- Injured players: disabled in slot dropdown; row tint grey.  
- Fitness &lt; 60: amber warning label on slot.  
- Tooltip: “Injured until {date}” / “Low fitness ({n}%)”.

### 6.3 Result (`MatchResultScreenTable`)

After scorers block:

```
INJURIES
  Player Name — Twisted ankle (~3 weeks)
```

Only players injured **this match** (track `injuredThisMatch` set in post-match pass).

---

## 7. Manual & i18n deliverables (with B5.1)

| Artefact | Owner |
|----------|-------|
| `docs/manual/06-squad-and-condition.md` | PO + UX |
| `docs/manual/appendix-glossary.md` entries: Fit%, Sharpness, Morale, Fatigue | PO |
| Long help strings in `BaseBundle_en_uk.properties` | UX |
| Update [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) → B5.1 section post-ship | Match/Sim |

**Player-facing injury paragraph (draft for manual):**

> Injuries keep a player out of your team for a period shown on the squad screen. The physio estimates how many weeks until they return. More serious injuries keep players out longer. Players with low fitness or heavy recent workloads are more likely to pick up knocks. Keep your squad rested and avoid fielding injured players — they cannot be selected until recovered.

(Adjust if Option B not chosen — remove physio sentence.)

---

## 8. Tests (add / extend)

| Test | Option |
|------|--------|
| `InjuryTypesLoaderTest` | A |
| `InjuryGeneratorTest` — multiple types from weights | A |
| `InjuryGeneratorTest` — physio reduces weeks | B |
| `SharpnessSimModifierIT` | A |
| `MoraleGoalBonusTest` | A |
| `FatigueSlowsRecoveryTest` | A |
| `MatchResultInjuryLineIT` | A |
| `TacticsInjuredGreyOutIT` | A (headless UI or harness hook) |

---

## 9. Engineering task IDs (proposed)

| ID | Task | Option |
|----|------|--------|
| **B5.1-1** | `InjuryTypesLoader` + mod file | A |
| **B5.1-2** | Revise `InjuryGenerator` weighted pick + severity | A |
| **B5.1-3** | Squad columns sharpness + return | A |
| **B5.1-4** | Tactics grey-out / warn | A |
| **B5.1-5** | Result injury block | A |
| **B5.1-6** | Sharpness sim factor | A |
| **B5.1-7** | Goal morale + fatigue recovery | A |
| **B5.1-8** | Physio multiplier + injury prone | B |
| **B5.1-9** | Inbox SERIOUS injury message | C |

**Depends on:** Layer B automated gate green (✅). **Blocks:** human B-9 injury checklist items.

---

## 10. Explicitly still out of scope (B5.1)

- Insurance toggle and wage cover during injury  
- Lilleshall / surgeon abroad choice UI  
- In-match live commentary injury events  
- FM body-part / medical centre  
- Rehab training plans  

→ Layer C or **B5.2** if CM93 insurance is required for marketing.

---

## 11. Decision log

| Date | Decision | By |
|------|----------|-----|
| 2026-06-30 | Proposal drafted | Agent |
| 2026-06-30 | **Option A approved** (recommended next steps) | PO implicit via execution request |
| 2026-06-30 | B5.1 Option A implemented — 123 tests green | Match/Sim |

---

*Approve option in PO meeting, then implement B5.1-* tasks in order.*
