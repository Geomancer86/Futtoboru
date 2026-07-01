# Research — CM93 Injury Subsystem (Primary-Source Ledger)

**Date:** 2026-06-30  
**Status:** **Active** — authoritative for M-B5.2 parity scope  
**Purpose:** Every CM93 injury feature traced to manual / secondary source before PO claims parity  

**Feeds:** [PM_MILESTONE_CM93_INJURY_PARITY.md](../planning/PM_MILESTONE_CM93_INJURY_PARITY.md) · [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](../planning/DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md)

---

## 1. Research question

What did Championship Manager 93/94 actually implement for injuries — and what must Futtoboru reproduce to honestly claim **“CM93-style injury management”**?

---

## 2. Source hierarchy

| Priority | Source | Location |
|----------|--------|----------|
| **P1 Primary** | Domark CM93 Amiga manual extract | [CM93_Manual_LemonAmiga_Extract.md](manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md) |
| **P1 Primary** | CM93 menu / icon extract | [CM93_Manual_Menu_Extract.md](manuals/cm93-94/CM93_Manual_Menu_Extract.md) |
| **P2 Secondary** | StrategyWiki CM93 Your team (staff) | [StrategyWiki](https://strategywiki.org/wiki/Championship_Manager_93/Your_team) |
| **P2 Secondary** | Good Old Days retrospective (insurance gameplay) | Web — treatment + insurance narrative |
| **P3 Context** | CM1 manual (IJ icon only) | [CM1_Manual_Menu_Extract.md](manuals/cm1/CM1_Manual_Menu_Extract.md) |
| **Out of scope** | Modern FM wikis | Do not use for CM93 parity definition |

---

## 3. CM93 injury feature catalogue

### 3.1 Squad & availability display

| ID | Feature | Manual evidence | Confidence |
|----|---------|-----------------|------------|
| CM93-S1 | Icon `[IJ]` injured | Menu extract | P1 |
| CM93-S2 | Injury **description** (text) | Squad screen §(4) — “description of the condition” | P1 |
| CM93-S3 | **Physio estimate** of recovery time | Squad §(4) — “estimate given by the physio, of the time to recover” | P1 |
| CM93-S4 | Fitness + shirt number when fit | Squad §(4) | P1 |
| CM93-S5 | Morale band (VERY LOW … SUPERB) | Squad §(3) | P1 — adjacent to condition |

**Not in manual:** body part labels (knee/ankle as simulation axes — descriptions may say “knee” as **flavour text**, not a simulation graph).

---

### 3.2 Hidden / derived attributes

| ID | Feature | Manual evidence | Confidence |
|----|---------|-----------------|------------|
| CM93-A1 | **INJURY PRONE** — “Likelihood of player getting injured” | Attributes §5 INJURIES | P1 |
| CM93-A2 | Hidden from normal screens | “These attributes are not displayed on any screen” | P1 |

**Futtoboru note:** CM93 hides prone; manager learns via experience. 1888 mode should default **hidden** with debug/scout reveal optional.

---

### 3.3 Staff — physio

| ID | Feature | Manual evidence | Secondary |
|----|---------|-----------------|-----------|
| CM93-ST1 | Physio role exists per club | Staff §(ii) | P1 |
| CM93-ST2 | “Improving match fitness and helping players recover from injury” | Staff §(ii) | P1 |
| CM93-ST3 | Staff **Ability** Fair → Superb | StrategyWiki staff page | P2 |
| CM93-ST4 | Better physio → shorter recovery | StrategyWiki + manual §5 INJURIES “good physio will naturally help” | P1+P2 |

**Futtoboru leverage:** `ClubStaffManager.getClubStaff(club, physioProfession)` already exists; need **ability rating** on `Person` or profession instance (may default if missing in 1888 DB).

---

### 3.4 Player status menu actions

| ID | Action | Manual evidence | Parity tier |
|----|--------|-----------------|-------------|
| CM93-P1 | **Insure Him** — weekly cost, confirmation | Player status (vii) | Tier 3 |
| CM93-P2 | Cannot insure while injured | Note under (vii) | Tier 3 |
| CM93-P3 | **Uninsure Him** | (viii) | Tier 3 |
| CM93-P4 | **Rehabilitate** — “arrange treatment for injured players, at a cost!” | (xi) | Tier 2 |
| CM93-P5 | Fine / wage / contract (context) | Adjacent menus | Out of injury scope |

---

### 3.5 Insurance benefits (economic)

| ID | Benefit | Manual § Player Insurance | Tier |
|----|---------|---------------------------|------|
| CM93-I1 | Lilleshall fees paid by insurer | (i) | 3 |
| CM93-I2 | Operation medical fees paid | (ii) | 3 |
| CM93-I3 | Forced retirement → club compensation (tribunal value) | (iii) | 3 defer |
| CM93-I4 | Minor injury basic treatment covered | (iv) | 3 |
| CM93-I5 | Wages paid while under treatment | §5 INJURIES prose | 3 |

---

### 3.6 Serious injury treatment choices

| ID | Choice | Manual § Player Injuries | Tier |
|----|--------|--------------------------|------|
| CM93-T1 | **Send to Lilleshall** — faster recovery, cost shown | (i) | 3 |
| CM93-T2 | **Surgeon abroad** — very serious, cost, “restricted to rich clubs” | (ii) | 3 |
| CM93-T3 | Offered for **serious** injuries (one or both options) | Opening paragraph | 2/3 |

---

### 3.7 Match & news presentation

| ID | Feature | Evidence | Tier |
|----|---------|----------|------|
| CM93-M1 | Match commentary: player “taken off injured” | Manual match section (see [RESEARCH_EARLY_CM_INJURIES_VS_FM.md](RESEARCH_EARLY_CM_INJURIES_VS_FM.md)) | **Separate** from M-B5.2 |
| CM93-M2 | `[IJ]` on match/ratings output | CM1/93 menu extract | Presentation |
| CM93-N1 | News feed: squad injuries | Manual news — “injuries etc” | Tier 2 (inbox) |
| CM93-N2 | Weekly transfer news (adjacent) | Manual | Out of scope |

---

### 3.8 Injury occurrence (inferred — manual partial)

Manual describes **management** more than generation formula. Inferred from §5 + INJURY PRONE:

| Inferred rule | Support |
|---------------|---------|
| Injuries happen in match context | `[IJ]` match flag |
| Proneness varies by player | INJURY PRONE attribute |
| Physio does not prevent initial injury | Physio helps **recovery** |
| Insurance does not prevent injury | Insurance pays **after** |

**Futtoboru B5.1:** post-match roll + fitness/fatigue — **aligned enough** for Tier 1; add INJURY PRONE in Tier 2.

---

## 4. CM1 vs CM93 vs Futtoboru (2026-07-01)

| Capability | CM1 | CM93 | Futtoboru B5.2 | Gap |
|------------|-----|------|----------------|-----|
| Injured state | ✅ IJ | ✅ IJ | ✅ | `[IJ]` icon cosmetic |
| Text condition | ⚠️ | ✅ | ✅ types | — |
| Physio weeks estimate | ❌ | ✅ | ✅ estimate | — |
| INJURY PRONE | ❌ | ✅ hidden | ✅ | — |
| Physio staff effect (injury) | ❌ | ✅ | ✅ | Match-fitness boost ❌ |
| Rehabilitate | ❌ | ✅ | ✅ | — |
| Insurance | ❌ | ✅ | ✅ | Forced retirement ❌ |
| Lilleshall / surgeon | ❌ | ✅ | ✅ | — |
| Inbox injury | ❌ | ✅ | ✅ | — |

**Full scorecard:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](../planning/ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md)

---

## 5. What CM93 did **not** have (FM confusion guard)

| FM-era feature | In CM93? |
|----------------|----------|
| Body-part injury map | **No** |
| Scan / % recovery per body area | **No** |
| Medical centre room UI | **No** |
| Sickness / virus outbreaks as separate system | **Not documented** |
| Rehab training micro | **No** — Lilleshall is a **choice**, not schedule |

---

## 6. Futtoboru implementation status (2026-07-01)

See [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10.

**B5.1 closed Tier 1. B5.2 Complete closed Tier 2–3** (except deferred forced-retirement payout). Parity audit: [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](../planning/ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md).

---

## 7. External references

- [Lemon Amiga CM93 manual](https://www.lemonamiga.com/doc/championship-manager-93/315)  
- [StrategyWiki — CM93 Your team](https://strategywiki.org/wiki/Championship_Manager_93/Your_team)  
- [MobyGames CM93](https://www.mobygames.com/game/1757/championship-manager-93/) — reserve squads, injury time (match engine, not management)  

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-01 | §4–§6 updated post-B5.2 Complete |
| 2026-06-30 | Full CM93 injury subsystem ledger for M-B5.2 |

---

*Update when new primary manual scans add injury detail.*
