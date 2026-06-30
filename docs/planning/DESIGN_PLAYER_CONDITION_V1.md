# Design — Player Condition (Fitness, Form, Injuries, Fatigue, Morale)

**Document type:** Architecture / product design  
**Date:** 2026-06-30  
**Status:** **In Layer B scope** — implement after B3/B4 engine path; before human QA  
**Audience:** PO, Match/Sim, World/Data, QA  

**Parent:** [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md) · [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md)

---

## 1. Scope boundary

| Term | Meaning in Futtoboru | Layer |
|------|----------------------|-------|
| **Team form** | Club last N match results (W/D/L streak) | **B2 MVP-4** — attendance + optional sim nudge |
| **Attribute delta “form”** | UI label for 14-day attribute change | **Exists** — `AttributeTrackingConstants`, not match sharpness |
| **Player condition** | Fitness, sharpness, injury, fatigue, morale | **B5** — this doc |

**PO lock (2026-06-30):** B5 is **part of whole Layer B** — CM1 tracked fitness/morale; CM93-style **basic** injuries. Full FM medical depth deferred.

**Testing:** L1 unit tests per COND; L2 `FitnessSimModifierIT`, `InjuredPlayerLineupIT`; L3 `LayerBMultiSeasonSoakIT`; L4 squad columns in human checklist — see [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md).

---

## 2. CM / FM reference

| Field | CM1 | CM93 | FM | Futtoboru B5 |
|-------|-----|------|-----|--------------|
| Fitness / energy | ✅ Squad screen aggregate | ✅ Per player | ✅ + jadedness | `fitnessPct` 0–100 |
| Morale | ✅ | ✅ | ✅ + happiness | `morale` 1–20 or 0–100 normalized |
| Injury | ⚠️ Basic | ✅ Types + weeks | ✅ Detailed | `Injury` record + weeks out |
| Match fatigue | ⚠️ Implicit | ✅ | ✅ | `fatiguePct` post-match bump |
| Training recovery | ✅ Basic | ✅ | ✅ | Daily tick (when training ships) |
| Sharpness / match form | ❌ | ⚠️ | ✅ | `sharpnessPct` — rises with minutes |

Manual evidence: [CM1_REFERENCE.md §4.4](CM1_REFERENCE.md) — “Fitness and morale tracked”; [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) — CM1 team energy average.

---

## 3. Data model (extensible)

Add to `Player` (or nested `PlayerCondition` — preferred for save clarity):

```java
PlayerCondition {
  Integer fitnessPct;        // 0–100, default 100
  Integer sharpnessPct;      // 0–100, default 70 at season start
  Integer fatiguePct;        // 0–100, 0 = fresh
  Integer morale;            // 0–100, default 70
  Injury currentInjury;      // null if fit
  LocalDate injuryReturnDate;
}

Injury {
  String typeId;             // mod: "strain", "bruise", "twisted_knee"
  String displayName;
  int weeksOut;
  InjurySeverity severity;   // MINOR, MODERATE, SERIOUS
}
```

**Save:** JSON on existing `Player` via LibGDX serialization — same approach as `ClubTacticsSetup` (MVP-10 versioning deferred).

**Defaults for 1888 start mod:** all fit, fitness 100, no injuries (historical DB has no injury records).

---

## 4. Systems behaviour

### 4.1 Fitness (`COND-1`)

| Event | Effect |
|-------|--------|
| Match played (in XI) | −3 to −8 fitness by minutes (MVP: flat −5) |
| Rest day (no match) | +2 fitness (cap 100) |
| Training (future) | +4 to +10 by intensity |

**Sim hook:** `effectiveStrength = base × (0.5 + fitnessPct/200)` — 50% floor at 0 fitness.

### 4.2 Sharpness / short form (`COND-2`)

| Event | Effect |
|-------|--------|
| Match in XI | +5 sharpness (cap 100) |
| No match 7+ days | −3 sharpness per week (floor 40) |

Distinct from attribute snapshots (long-term skill drift).

### 4.3 Injuries (`COND-3`)

| Trigger | Mechanism |
|---------|-----------|
| Post-match roll | Base 2% per XI player; ↑ with low fitness, fatigue |
| Mod weights | `mods/injuries/types.txt` (B5) |

**Effects:** player excluded from `BestElevenSelector`; shown in squad UI; optional inbox message.

CM93-style types (strain 1–2 weeks, serious 4–8) — keep table small for 1888.

### 4.4 Fatigue (`COND-4`)

Accumulated `fatiguePct` from matches; decays daily. High fatigue → injury risk ↑, fitness recovery ↓.

### 4.5 Morale (`COND-5`)

Updated after match from:

| Input | Δ morale |
|-------|----------|
| Win (in XI) | +3 |
| Loss | −2 |
| Goal scored | +2 |
| Rating &lt; 5.0 | −1 |
| Rating &gt; 8.0 | +1 |

Clamp 0–100. Optional future: feed from board / transfers.

---

## 5. UI (minimal B5)

| Screen | Change |
|--------|--------|
| **Squad** | Columns: Fit%, Sharp%, Morale; injury icon + return date |
| **Tactics / XI** | Grey out injured; warn if fitness &lt; 60 |
| **Match result** | “Injured: Player X (2 weeks)” line if any |

No new sidebar required — extend `SquadScreenTable`.

---

## 6. Integration with match sim

```
MatchLineupBuilder
  → skip injured players
  → prefer fit + sharp for Best XI

MatchSimulator v3 (B5)
  → per-player condition multipliers on slot strength
  → injury roll after sim

Daily tick (FuttoboruGameEngine)
  → recover fitness/fatigue
  → decrement injury weeks
```

**Order:** Implement only after `MatchSimulator` v2 (B1) and ratings (B3) are stable.

---

## 7. Tests

| Class | Type | Proves |
|-------|------|--------|
| `PlayerConditionDefaultsTest` | Unit | New game players fit, 100% fitness |
| `PlayerFitnessTest` | Unit | Match reduces fitness; rest recovers |
| `PlayerFormTest` | Unit | Sharpness up after match; decays when idle |
| `InjuryGeneratorTest` | Unit | Roll respects fitness; produces valid `Injury` |
| `InjuredPlayerLineupIT` | IT | Injured player never in auto XI |
| `FitnessSimModifierIT` | IT | Low-fitness XI loses more vs same XI at 100% |
| `MoraleUpdateTest` | Unit | Win/loss/rating adjust morale within clamp |
| `FatigueRecoveryTest` | Unit | Daily decay toward 0 |

Harness additions: `setPlayerFitness`, `setPlayerInjury`, `simulateDaysWithConditionTick`.

---

## 8. Mod content (B5)

| File | Purpose |
|------|---------|
| `mods/injuries/types.txt` | Injury id, name, weeks, severity |
| `mods/condition/defaults.txt` | Optional era overrides |

Empty files OK — hardcoded defaults in Java for first ship.

---

## 9. Engineering task IDs

| ID | Task | Depends on |
|----|------|------------|
| **COND-1** | `PlayerCondition` + fitness tick + sim modifier | B-9 |
| **COND-2** | Sharpness tick | COND-1 |
| **COND-3** | `InjuryGenerator` + squad UI | COND-1 |
| **COND-4** | Fatigue accumulation | COND-1 |
| **COND-5** | Morale from ratings/results | MVP-5, COND-1 |

---

## 10. Explicitly out of scope (B5)

- Physio staff / medical centre (FM)
- Rehab plans per injury type
- PFA / injury lawsuits
- Drug test / doping
- International duty fatigue (no intl matches in 1888 v1)

---

*Next update: when PO schedules B5 or CM93 research expands injury types.*
