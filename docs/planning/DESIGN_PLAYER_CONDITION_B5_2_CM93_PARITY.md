# Design — Player Condition B5.2 (CM93 Injury Parity)

**Document type:** Architecture / product design  
**Date:** 2026-06-30  
**Status:** **Awaiting PO scope lock** — analysis & design only  
**Audience:** Match/Sim, UX, World/Data, QA, PM  

**PO milestone:** [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md)  
**Research:** [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md)  
**Baseline:** B5.1 — [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9  

---

## 1. Design intent

Implement **CM93 injury management** — not FM medical simulation — as a **bounded milestone** PO can sign off.

| In scope | Out of scope |
|----------|--------------|
| Physio estimate + staff quality | Body-part model |
| INJURY PRONE (hidden) | FM scan / % rehab per area |
| Rehabilitate (paid treatment) | Training-injury schedule |
| Insurance + Lilleshall + surgeon (Complete tier) | Forced retirement tribunal (defer PO call) |
| Inbox injury news (Core recommended) | In-match commentary stream |

---

## 2. PO scope switches

| Switch | Core (A) | Complete (B) |
|--------|----------|----------------|
| INJURY PRONE | ✅ | ✅ |
| Physio multiplier + estimate UI | ✅ | ✅ |
| Rehabilitate | ✅ | ✅ |
| Inbox MODERATE+ | ✅ recommended | ✅ |
| Insure / Uninsure | ❌ | ✅ |
| Wages while injured (insured) | ❌ | ✅ |
| Lilleshall | ❌ | ✅ |
| Surgeon abroad | ❌ | ✅ |

Engineering implements **feature flags** or phase tags `B5.2a`–`e` so Core can ship without insurance code paths enabled.

---

## 3. Data model

### 3.1 Player — injury proneness

```java
// Player.java
Integer injuryProneness;  // 1–20, null → 10 (average)
```

| Value | Label (internal) | Roll modifier |
|-------|------------------|---------------|
| 1–5 | Low | −0.005 × (10 − prone) |
| 10 | Average | 0 |
| 15–20 | High | +0.001 × (prone − 10) |

**1888 UI:** not shown on squad (CM93-faithful). Optional scout/debug screen later.

**Mod:** `mods/condition/injury_proneness.txt` optional per player id — else generator at career start.

---

### 3.2 Player — insurance (Complete only)

```java
Boolean injuryInsured;           // weekly policy active
BigDecimal injuryInsuranceWeekly; // cost snapshot
```

Persist on `Player` or nested `PlayerContract` extension — prefer `Player` flag + expense line for simplicity.

---

### 3.3 PlayerCondition — treatment state

```java
Boolean rehabTreatmentActive;    // paid basic rehab in progress
LocalDate lilleshallEndDate;     // if at Lilleshall (Complete)
Boolean surgeonAbroadUsed;       // one-shot flag per injury (Complete)
```

`injuryReturnDate` remains authoritative for “selectable again”.

---

### 3.4 Staff — physio ability

```java
// Person or staff assignment wrapper
Integer staffAbility;  // 1=Fair .. 5=Superb (CM93 mapping)
```

**Default for 1888 start:** physio assigned via existing club staff init → ability **3 (OK)** if unset.

**Recovery multiplier:**

```java
physioMultiplier = 1.20 - (abilityIndex * 0.07)
// Fair(1) ≈ 1.13 weeks, Superb(5) ≈ 0.85 weeks
weeksOut = max(1, round(baseWeeks * physioMultiplier))
```

Display **estimate** separately from calendar math:

```text
"~{weeksOut} weeks (physio: {abilityLabel})"
```

---

## 4. Systems behaviour

### 4.1 Injury roll (extend B5.1)

```java
chance = base
       + fitnessModifier
       + fatigueModifier
       + (injuryProneness - 10) * 0.001
```

Pick type from `InjuryTypesRegistry` (unchanged).

Apply **physio multiplier** to rolled weeks **before** setting `injuryReturnDate`.

Store `baseWeeksBeforePhysio` optionally for UI “was 4w, now 3w” — defer if scope tight.

---

### 4.2 Rehabilitate (Tier 2)

**Trigger:** Player detail / status menu on **injured** player, MINOR or MODERATE only (CM93: basic treatment).

| Input | Effect |
|-------|--------|
| Cost | `mods/injuries/rehab_costs.txt` — e.g. MINOR £25, MODERATE £75 (1888 money) |
| Recovery | −1 week (MINOR) or −2 weeks (MODERATE), min 1 week remaining |
| Insurance | If insured (Complete): cost £0 |
| Finance | `ExpenseCalculator` one-off line “Medical treatment” |

**UI flow:** Confirm dialog → deduct → shorten `injuryReturnDate` → message “Treatment arranged.”

---

### 4.3 Insurance (Tier 3 — Complete)

**Insure (fit players only):**

- Weekly premium = f(player wage, age) — e.g. 5% of weekly wage  
- Sets `injuryInsured = true`  
- Expense tick each week  

**While injured + insured:**

- Skip wage charge OR mark as “covered by insurance” in finance report  
- Rehabilitate cost = 0  

**Uninsure:** clears flag; no refund.

---

### 4.4 Lilleshall (Tier 3 — Complete)

**Offer when:** new injury severity **SERIOUS**, or MODERATE with bad luck roll — PO pick: **SERIOUS only** (manual-aligned).

**Dialog:**

```
Send {player} to Lilleshall National Rehabilitation Centre?
Cost: £{cost}. Recovery may be faster.
[Yes] [No]
```

| Effect | Value |
|--------|-------|
| Cost | £200–400 mod table |
| Recovery | multiply remaining weeks × 0.6 |
| Insurance | pays cost if insured |
| Flag | `lilleshallEndDate` for UI flavour |

---

### 4.5 Surgeon abroad (Tier 3 — Complete)

**Offer when:** SERIOUS injury **and** `club.balance > threshold` (rich club gate).

| Effect | Value |
|--------|-------|
| Cost | £500+ |
| Recovery | multiply remaining weeks × 0.5 |
| Risk | optional 10% fail → no reduction (CM93 “if successful” — can defer fail roll) |
| One per injury | `surgeonAbroadUsed` |

---

### 4.6 Inbox (Tier 2 recommended)

On injury roll severity ≥ MODERATE:

```java
MessageManager.enqueue(
  category: SQUAD,
  mandatory: false,
  body: "{player} injured — {displayName}. Physio estimates ~{weeks} weeks."
);
```

SERIOUS: add “Consider specialist treatment.”

---

## 5. UI specification

### 5.1 Squad (`PlayersListTable`)

| Column | Core change |
|--------|-------------|
| Injury | Keep display name |
| Return | Show **physio estimate** primary: `~3 wk (OK physio)`; tooltip with calendar date |

### 5.2 Player detail / status menu (new or extend `PersonDetailsScreenTable`)

| Action | Tier | Enabled when |
|--------|------|--------------|
| Rehabilitate | Core | Injured, MINOR/MODERATE |
| Insure | Complete | Fit, not insured |
| Uninsure | Complete | Insured |
| Send to Lilleshall | Complete | SERIOUS, not yet sent |
| Surgeon abroad | Complete | SERIOUS, balance OK, not used |

### 5.3 Staff screen (existing club detail)

Show physio name + **Ability: Fair…Superb** — read-only in Core unless staff hiring already works.

### 5.4 Result screen

Keep B5.1 INJURIES block; append physio estimate:  
`Twisted ankle (~3 wk, Good physio)`

---

## 6. Mod content

| File | Purpose |
|------|---------|
| `mods/injuries/types.txt` | ✅ exists |
| `mods/injuries/rehab_costs.txt` | MINOR/MODERATE costs |
| `mods/injuries/treatment_costs.txt` | Lilleshall, surgeon fees |
| `mods/injuries/insurance.txt` | Premium % of wage |
| `mods/condition/injury_proneness.txt` | Optional per-player overrides |

---

## 7. Integration points

| System | Hook |
|--------|------|
| `InjuryGenerator` | Proneness + physio weeks |
| `PlayerConditionService` | Unchanged daily tick; clear treatment flags on heal |
| `ExpenseCalculator` | Insurance weekly + rehab + Lilleshall |
| `ClubStaffManager` | Resolve physio ability |
| `MessageManager` | Injury inbox |
| `PersonDetailsScreenTable` | Player actions menu |

---

## 8. Tests (acceptance mapping)

| Milestone tier | Tests |
|----------------|-------|
| B5.2a | `InjuryPronenessTest`, `PhysioRecoveryTest`, `PhysioEstimateUITest` (headless state) |
| B5.2b | `RehabilitateTreatmentTest`, `RehabFinanceIT` |
| B5.2c | `InjuryInboxIT` |
| B5.2d | `PlayerInsuranceTest`, `InsuredWagesIT` |
| B5.2e | `LilleshallTreatmentIT`, `SurgeonAbroadIT` |
| Regression | All B5.1 + `LayerBMultiSeasonSoakIT` |

---

## 9. Manual & traceability

On M-B5.2 Core complete:

1. Update [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10  
2. Rewrite [06-squad-and-condition.md](../manual/06-squad-and-condition.md) — remove “not in game yet” for shipped tiers  
3. Add [07-player-actions-insurance.md](../manual/07-player-actions-insurance.md) if Complete tier  
4. Update [FEATURE_TRACEABILITY_MATRIX.md](FEATURE_TRACEABILITY_MATRIX.md)  

---

## 10. Engineering task IDs

| ID | Task | Tier |
|----|------|------|
| **B5.2a-1** | `injuryProneness` on Player + mod | Core |
| **B5.2a-2** | Physio ability resolution + multiplier | Core |
| **B5.2a-3** | Estimate UI squad + result | Core |
| **B5.2b-1** | Rehabilitate action + costs | Core |
| **B5.2c-1** | Inbox injury messages | Core |
| **B5.2d-1** | Insure/Uninsure + weekly premium | Complete |
| **B5.2d-2** | Wage cover while injured | Complete |
| **B5.2e-1** | Lilleshall dialog + effect | Complete |
| **B5.2e-2** | Surgeon abroad + rich gate | Complete |

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial B5.2 design aligned to PM milestone pack |

---

*Implementation starts only after PO records Option A or B in [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md) §8.*
