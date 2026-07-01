# PM Milestone — CM93 Injury Parity (M-B5.2)

**Document type:** PM brief / PO decision pack  
**Date:** 2026-06-30  
**Audience:** **PO first**, then PM, Match/Sim, UX, QA, Sponsor  
**Branch:** `analyze-ai-branch`  
**Status:** **Option B locked — B5.2 Complete shipped** (2026-07-01)

**Companion design:** [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md)  
**Research:** [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md)  
**As-built today:** [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9 (B5.1)

---

## 1. PO headline — read this first

### What we are **not** saying today

B5.1 (shipped) gives Futtoboru **credible injuries** — types, weeks out, squad/tactics/result UI. That is **Partial** depth vs CM93. It is **not** CM93 parity.

The old label **“Option B = CM93 parity”** in [DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md](DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md) was **too narrow** (physio multiplier only). Real CM93 parity is a **management subsystem**, not one formula.

### What we **can** claim after milestone M-B5.2

> **“CM93-style injury management”** — text injury descriptions, physio recovery estimates, injury-prone players, optional insurance and treatment choices — **without** modern FM body-part or medical-centre simulation.

That sentence is **defensible** if PO approves **M-B5.2 Core** scope (§4) and acceptance criteria (§6) are met.

### What we must **never** imply (PO guardrails)

| Do not say | Why |
|------------|-----|
| “Same as Football Manager injuries” | FM medical model is a different era |
| “Body-part / knee ligament simulation” | **Not in CM93** — primary manuals have no anatomical map |
| “Layer B is finished” | M-B5.2 is one milestone inside Layer B; tactics/transfers still Partial |
| “Ready for public release” | [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) — no release pressure |
| “B5.1 = CM93” | Understates gap; overpromises to anyone who played CM93 |

---

## 2. PO alignment — what you need, want, and should expect

Synced with [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) and [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md).

| PO need | How M-B5.2 serves it |
|---------|----------------------|
| **Honest depth labels** | Named milestone with testable checklist — no more binary ✅ on injuries |
| **CM lineage credibility** | CM93 manual-verified loop, not FM feature creep |
| **No release fiction** | Milestone = **quality marker** + manual chapter completeness, not a ship date |
| **Whole Layer B** | Injuries are B5; milestone closes the biggest B5 gap before human B-9 |
| **Long-term CM+FM north star** | M-B5.2 is **bounded** — stops at CM93; FM medical centre stays FAR horizon |
| **Documentation for eventual manual/wiki** | Parity checklist maps 1:1 to [06-squad-and-condition.md](../manual/06-squad-and-condition.md) + player menu chapter |

| PO want (inferred) | Milestone delivers |
|--------------------|-------------------|
| Stakeholders understand **what “CM93” means** | §3 feature map with manual page refs |
| One **sign-off moment** | §6 acceptance demo script (~15 min) |
| PM can schedule without re-debate | Phased B5.2a–e with effort (§5) |
| Wrong promises avoided | §1 guardrails + “out of parity” list |

| PO should expect | Reality |
|------------------|---------|
| ~**2–3 weeks** eng for **M-B5.2 Core** after PO lock | Tier 2 + finances hooks |
| ~**+1 week** for **M-B5.2 Complete** (insurance + Lilleshall) | Tier 3 optional |
| **1888 simplifications** | No forced retirement tribunal; staff ability may use existing `ClubStaffManager` |
| Human B-9 **after** automated tests green | Same pyramid rule as rest of Layer B |

---

## 3. CM93 injury subsystem — research summary (primary sources)

Full ledger: [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md).

CM93 (1993 manual + StrategyWiki staff pages) treats injuries as a **manager-facing loop**, not simulation of anatomy:

```
Match / training load
    → injury occurs (hidden INJURY PRONE + context)
    → squad shows condition TEXT + physio WEEK ESTIMATE
    → player unavailable [IJ]
    → manager choices: Rehabilitate (cost) · Insure (ongoing) · Lilleshall / surgeon (serious)
    → physio quality modifies recovery
    → news may mention squad injuries
    → player returns; fit for selection
```

**Confirmed in CM93, absent in CM1:** physio estimate text, insurance, Lilleshall, surgeon abroad, INJURY PRONE attribute, Rehabilitate on player menu.

**Confirmed absent in CM93 (do not build for parity claim):** body-part diagram, rehab focus per muscle, FM-style medical centre UI.

---

## 4. Parity tiers — definition of done

### Tier 1 — **Injury existence** (B5.1 ✅ shipped)

| # | CM93 behaviour | Futtoboru B5.1 | Source |
|---|----------------|----------------|--------|
| T1-1 | Post-match injury can occur | ✅ Weighted types | Engine |
| T1-2 | Text injury description | ✅ 5 types in mod | Manual § squad (4) |
| T1-3 | Weeks until return | ✅ Return date column | Manual § squad (4) |
| T1-4 | Injured unavailable for selection | ✅ Tactics filter + Best XI | Manual `[IJ]` |
| T1-5 | Result / feedback | ✅ INJURIES block | Manual match `[IJ]` |
| T1-6 | Severity spectrum | ✅ MINOR/MODERATE/SERIOUS | Manual serious → Lilleshall |

**Tier 1 alone:** “We have injuries.” **Not parity.**

---

### Tier 2 — **CM93 management feel** (M-B5.2 Core — **recommended parity claim**)

| # | CM93 behaviour | Futtoboru today | B5.2 deliverable |
|---|----------------|-----------------|------------------|
| T2-1 | Hidden **INJURY PRONE** affects likelihood | ❌ | Player attribute + roll modifier |
| T2-2 | **Physio** staff quality | ⚠️ Staff in DB, no link | `ClubStaffManager` → recovery multiplier |
| T2-3 | Squad shows **physio estimate** (not just date) | ❌ “Return 15 Oct” only | “~N weeks (physio: Good)” |
| T2-4 | **Rehabilitate** — pay for treatment (player menu) | ❌ | Basic treatment reduces weeks (MINOR/MODERATE) |
| T2-5 | Injury news in **news feed** | ❌ | Inbox item on MODERATE+ (optional in Core) |
| T2-6 | Fitness / fatigue affect injury risk | ✅ Partial | Keep + document |

**PO-safe marketing after Tier 2:**  
*“Physio estimates, injury-prone players, and treatment choices — CM93-style.”*

---

### Tier 3 — **CM93 economic layer** (M-B5.2 Complete — optional stretch)

| # | CM93 behaviour | B5.2 deliverable |
|---|----------------|------------------|
| T3-1 | **Insure / Uninsure** player (weekly cost) | Player action + `ClubExpenses` |
| T3-2 | Insured: wages paid while injured | Wage ledger skip when insured + injured |
| T3-3 | Insured: basic treatment covered | Rehab cost waived if insured |
| T3-4 | **Lilleshall** — serious injury, faster recovery, cost | Choice dialog SERIOUS only |
| T3-5 | **Surgeon abroad** — very serious, rich clubs | SERIOUS + club balance gate |
| T3-6 | Forced retirement + compensation | **Defer** — rare; PO call |

**PO-safe marketing after Tier 3:**  
*“Full CM93 injury economy — insurance, Lilleshall, and specialist treatment.”*

---

### Explicitly **out of M-B5.2** (wrong to bundle)

- In-match scrolling commentary “taken off injured” (match presentation — separate milestone)  
- Reserve squad auto-fill when many injured (CM93 `[RSRV]` — Layer C)  
- Staff hiring UI overhaul (use existing staff assignment + ability field)  
- FM body areas, scan, medical centre, sickness  
- Historical 1888 injury records in DB (no source data)

---

## 5. Milestone M-B5.2 — PM plan

### Milestone card

| Field | Value |
|-------|-------|
| **ID** | **M-B5.2** |
| **Name** | CM93 Injury Parity |
| **Depends on** | B5.1 ✅ · Layer B automated gate ✅ · finances backend ✅ |
| **Blocks** | Human B-9 injury checklist · manual ch.6 “player-ready” · parity marketing |
| **Owner** | Match/Sim (G2) · UX for player menu · Platform for inbox |
| **PO gate** | Scope: **Core** vs **Complete** (§4 Tier 2 vs 2+3) |

### Phased delivery (after PO lock)

| Phase | Scope | Effort (est.) | Tests |
|-------|--------|---------------|-------|
| **B5.2a** | INJURY PRONE + physio multiplier + estimate UI | 2–3 d | `InjuryPronenessTest`, `PhysioRecoveryTest` |
| **B5.2b** | Rehabilitate (basic treatment, cost) | 2 d | `RehabilitateTreatmentIT` |
| **B5.2c** | Inbox injury message (MODERATE+) | 1 d | `InjuryInboxIT` |
| **B5.2d** | Insurance + wage/treatment cover | 3–4 d | `PlayerInsuranceTest`, finance IT |
| **B5.2e** | Lilleshall + surgeon abroad choices | 2–3 d | `SeriousInjuryTreatmentIT` |

**M-B5.2 Core** = B5.2a + B5.2b (+ B5.2c recommended)  
**M-B5.2 Complete** = Core + B5.2d + B5.2e

### Schedule suggestion (no release implied)

```
Week 1   PO locks Core vs Complete
Week 2   B5.2a + B5.2b (+2c)
Week 3   B5.2d–e if Complete approved
Week 4   Manual ch.6 update + human B-9 injury slice
```

---

## 6. Acceptance criteria — PO sign-off demo

PO (or delegate) runs this **once** on a build where M-B5.2 scope is marked done. All must pass for parity claim.

### Core demo script (~15 min)

1. **Squad** — injured player shows **condition name** + **physio estimate weeks** (not date-only).  
2. **Attributes** — debug/ scout view shows high **injury proneness** player gets injured more over 20 match soak (or deterministic test shown).  
3. **Staff** — replace/worse physio → estimate weeks **increase**; better physio → **decrease**.  
4. **Player menu** — **Rehabilitate** on MINOR injury; club balance drops; weeks reduce.  
5. **Tactics** — injured still blocked; returned player selectable on/after date.  
6. **Result** — INJURIES block still lists match injuries.  
7. **Manual** — [06-squad-and-condition.md](../manual/06-squad-and-condition.md) matches behaviour (no “coming soon” for shipped features).

### Complete demo adds

8. **Insure** player before injury; injury occurs; wages still paid; **Rehabilitate** free for minor.  
9. **Serious injury** — offered Lilleshall; accept; faster return; cost applied (insurance covers if insured).  
10. **Rich club** — surgeon abroad offered on SERIOUS; decline/accept paths work.

### Automated gate (PM — not optional)

| Tier | Min tests |
|------|-----------|
| Core | All B5.1 tests green + B5.2a–c L1/L2 |
| Complete | + B5.2d–e finance + treatment ITs |
| Long | `LayerBMultiSeasonSoakIT` still green (no injury deadlock) |

---

## 7. Progress dashboard (2026-07-01 — B5.2 Complete shipped)

| Tier | Items | Done | % |
|------|-------|------|---|
| Tier 1 | 6 | 6 | **100%** |
| Tier 2 | 6 | 6 | **100%** |
| Tier 3 | 6 | 6 | **100%** |
| **Overall CM93 injury parity** | 18 | 18 | **~100%** |

**Honest one-liner for Sponsor:**  
*“CM93-style injury management is in — physio estimates, treatment choices, insurance, and specialist options — without FM body-part simulation. Squad fitness/morale still Partial vs CM93 (no training hook, morale not in sim).”*

**Parity detail:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md)

**PO decision (locked):** **Option B — M-B5.2 Complete** (2026-07-01). Implementation: [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10.

---

## 8. PO decision

**Locked: Option B — M-B5.2 Complete** (2026-07-01). See [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10.

<details>
<summary>Original options (archived)</summary>

| Option | Scope | Parity claim allowed | Effort |
|--------|--------|----------------------|--------|
| **A — M-B5.2 Core** | Tier 1 ✅ + Tier 2 | “CM93-style injury management” | ~1.5–2 w |
| **B — M-B5.2 Complete** | Tier 1–3 | “Full CM93 injury economy” | ~3–4 w |
| **C — Defer** | Stay at B5.1 | “Early injuries — not CM93 parity” | 0 |

</details>

**Secondary PO calls:**

1. INJURY PRONE **hidden** in 1888 UI (CM93-faithful) vs shown in scout report?  
2. Include **B5.2c inbox** in Core? (Recommended: yes — CM93 news mentions injuries.)  
3. **Forced retirement** in Complete or defer to Layer C?

---

## 9. Risks & mitigations (PM)

| Risk | Mitigation |
|------|------------|
| PO hears “parity” and expects FM | §1 guardrails in all outward docs |
| Finances + insurance scope creep | Tier 3 isolated; Core claim without insurance |
| Staff ability not in 1888 data | Default physio “Fair”; mod override per club |
| Manual overpromises | Traceability: manual § only after IMPLEMENTATION § updated |
| B-9 before M-B5.2 | PM: run B-9 injuries as **regression** after milestone, not before |

---

## 10. Document chain (for audit)

```
RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md
RESEARCH_CM93_INJURY_SUBSYSTEM.md
    → PM_MILESTONE_CM93_INJURY_PARITY.md (this doc — PO decision)
    → DESIGN_INJURY_HISTORICAL_ERA_GATING.md (M-B5.3-HIST proposed)
    → DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md (engineering spec)
    → IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md §10–§11
    → docs/manual/06-squad-and-condition.md
    → FEATURE_TRACEABILITY_MATRIX.md
```

---

## 12. Follow-on — M-B5.3-HIST (1888 historical accuracy)

**M-B5.2 is complete for CM93 parity.** It is **not** complete for **1888-period injury simulation**.

| Claim | After M-B5.2 | After M-B5.3-HIST (proposed) |
|-------|--------------|------------------------------|
| CM93-style injury management | ✅ | ✅ (ME-6 calendar profile) |
| Historically accurate 1888 injuries | ❌ | ✅ target |

**Research:** [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md) — medical eras ME-0→ME-7  
**Design:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md)  
**Audit:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9  

**PO action:** Sign era boundary defaults (insurance from ME-2 vs ME-6; Lilleshall from 1951 vs 1986) before eng schedules HIST-a.

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-06-25 | §12 M-B5.3-HIST follow-on; 1888 vs CM93 framing |
| 2026-07-01 | Option B shipped — parity ~100% |
| 2026-06-30 | Initial PO/PM milestone pack; clarifies B5.1 ≠ parity |

---

*PO: M-B5.2 closed. Next optional lock: M-B5.3-HIST era gates (§12).*
