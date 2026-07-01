# Analysis — Fitness & Injury Module: CM93 Parity Status + CM/FM Field Map

**Document type:** Research synthesis / parity audit  
**Date:** 2026-07-01  
**Status:** **Active** — post B5.2 Complete (`7056902`)  
**Audience:** PO, PM, manual authors, QA  

**Authoritative as-built:** [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10  
**CM93 ledger:** [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md)  
**Prior synthesis:** [ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md](ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md) (superseded for status by §1–3 here)

---

## 1. Executive summary

| Module | CM93 parity (management) | Futtoboru depth | Honest claim |
|--------|--------------------------|-----------------|--------------|
| **Injury subsystem** | **~95%** of documented CM93 management loop | **Core+** | *“Full CM93 injury economy”* — defensible |
| **Fitness / condition (non-injury)** | **~70%** of CM93 squad-condition feel | **Partial+** | *“CM93-style fitness tracking”* — not full parity |
| **Combined player-condition pillar** | **~82%** weighted | **Partial+ / Core+ split** | Do **not** merge into one marketing line |

**What changed since 2026-06-30 depth audit:** M-B5.2 Complete (Option B) shipped — physio estimate, INJURY PRONE, Rehabilitate, inbox, insurance, Lilleshall, surgeon abroad. The old “~39% CM93 injury parity” figure is **obsolete**.

**What is still not CM93:** in-match “taken off injured” commentary, `[IJ]` icon parity, physio **match-fitness** improvement (manual §Staff ii), forced-retirement insurance payout, reserve-squad auto-fill when many injured, morale **bands** vs numeric UI, morale in match sim.

**What is correctly out of scope:** FM body-part map, medical centre, rehab % scans, sickness outbreaks, jadedness — see §4.

---

## 2. CM93 parity scorecard — injury module (M-B5.2)

Scored against [PM_MILESTONE_CM93_INJURY_PARITY.md](PM_MILESTONE_CM93_INJURY_PARITY.md) tiers + [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) IDs.

### Tier 1 — existence (6/6 ✅)

| ID | CM93 behaviour | Futtoboru | Notes |
|----|----------------|-----------|-------|
| T1-1 | Post-match injury | ✅ | Weighted roll + proneness + fitness/fatigue |
| T1-2 | Text description | ✅ | 5 types via `mods/injuries/types.txt` |
| T1-3 | Weeks until return | ✅ | Return date + physio estimate column |
| T1-4 | Unavailable `[IJ]` | ✅ | Engine + tactics exclude; UI text not icon |
| T1-5 | Result feedback | ✅ | INJURIES block + physio label |
| T1-6 | Severity spectrum | ✅ | MINOR / MODERATE / SERIOUS |

### Tier 2 — management feel (6/6 ✅)

| ID | CM93 behaviour | Futtoboru | Gap notes |
|----|----------------|-----------|-----------|
| T2-1 | INJURY PRONE | ✅ | Hidden 1–20; roll modifier |
| T2-2 | Physio staff quality | ✅ | `Person.staffAbility`; default OK if vacant |
| T2-3 | Physio **estimate** on squad | ✅ | `~N wk (OK physio)` |
| T2-4 | Rehabilitate | ✅ | MINOR/MODERATE; mod costs |
| T2-5 | Injury news | ✅ | Inbox MODERATE+ on manager club |
| T2-6 | Fitness/fatigue → risk | ✅ | Documented in IMPLEMENTATION §10 |

### Tier 3 — economic layer (5/6 ✅, 1 deferred)

| ID | CM93 behaviour | Futtoboru | Gap notes |
|----|----------------|-----------|-----------|
| T3-1 | Insure / Uninsure | ✅ | 5% wage premium mod |
| T3-2 | Wages while injured (insured) | ✅ | `ExpenseCalculator` skip |
| T3-3 | Insured basic treatment | ✅ | Rehab free if insured |
| T3-4 | Lilleshall | ✅ | SERIOUS; ×0.6 remaining weeks |
| T3-5 | Surgeon abroad | ✅ | SERIOUS + balance gate; 10% fail roll |
| T3-6 | Forced retirement compensation | ❌ **Deferred PO** | Documented out of M-B5.2 |

### Presentation & adjacent CM93 (not in M-B5.2 scope)

| ID | CM93 behaviour | Futtoboru | Parity |
|----|----------------|-----------|--------|
| CM93-M1 | Match commentary “taken off injured” | ❌ | **0%** — separate milestone |
| CM93-M2 | `[IJ]` on match/ratings | ⚠️ | Text labels only |
| CM93-N2 | Weekly transfer news | N/A | Out of condition scope |
| `[RSRV]` | Reserve fill when many injured | ❌ | Layer C |

**Injury module verdict:** **17/18** scoped CM93 management items ✅ → **~95%** (excluding deferred T3-6 and presentation items).

---

## 3. CM93 parity scorecard — fitness & condition (non-injury)

CM93 documents **squad condition** as fitness + morale + injury text on one screen. Futtoboru splits these into engine fields with 1888 simplifications.

| Capability | CM93 manual | Futtoboru B5.2 | Depth | Gap |
|------------|-------------|----------------|-------|-----|
| Per-player fitness display | ✅ Squad §(4) | ✅ Fit% column | **Core** | CM93 used fit + shirt #; we use 0–100 |
| Fitness drops from playing | ✅ (implicit) | ✅ −5 per match XI | **Core** | — |
| Fitness rises on rest | ✅ | ✅ +2/day (+1 if tired) | **Core** | No **training** recovery hook |
| Fitness affects match sim | ✅ | ✅ `fitnessStrengthFactor` | **Core** | — |
| Morale per player | ✅ Bands VERY LOW→SUPERB | ✅ 0–100 numeric | **Partial** | Presentation differs |
| Morale from results/goals/ratings | ✅ | ✅ incl. goal +2 (B5.1) | **Core** | — |
| Morale affects match sim | ✅ (CM93 implied) | ❌ | **None** | FM/CM both use morale later |
| Fatigue / tiredness | ✅ Part of condition | ✅ fatigue%; injury risk | **Partial** | Not in sim strength |
| Sharpness / match form | ⚠️ CM93 lighter | ✅ tracked + sim factor | **Partial+** | Exceeds CM1; CM93 unclear |
| Physio **match fitness** boost | ✅ Staff §(ii) | ❌ | **None** | Physio = injury recovery only |
| Physio injury recovery | ✅ | ✅ multiplier + estimate | **Core** | — |
| Training / match prep | ⚠️ Adjacent menus | ❌ stub screen | **Stub** | Layer C |
| Low fitness warning in XI | ⚠️ inferred | ✅ tactics warn &lt;60% | **Partial+** | — |
| Condition save/load | — | ✅ `PlayerConditionSaveLoadIT` | **Core** | — |

**Fitness/condition verdict:** **~70%** of CM93 **non-injury** squad-condition feel. Injury parity **does not** close the training/morale-sim/physio-match-fitness gaps.

---

## 4. Condensed field map — CM & FM releases (same domain)

**Domain:** player fitness, fatigue, morale, injuries, medical staff, treatment economy.

**Legend:** ✅ documented Core · ⚠️ Partial / simplified · ❌ absent · 🔮 FM-era (post-CM93) · — not applicable

| Feature | CM1 (1992) | CM93/94 | CM96–CM03 | CM4+ / early FM | Modern FM (2010s+) | Futtoboru (B5.2) |
|---------|------------|---------|-----------|-----------------|---------------------|-------------------|
| **Display & state** |
| Injured marker `[IJ]` | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ text |
| Injury text description | ⚠️ | ✅ | ✅ | ✅ richer labels | ✅ + type catalogue | ✅ |
| Recovery time estimate | ❌ | ✅ physio weeks | ✅ | ✅ | ✅ + prognosis % | ✅ physio estimate |
| Per-player fitness | ⚠️ team energy avg | ✅ | ✅ | ✅ state bands | ✅ + jadedness | ✅ Fit% |
| Morale | ✅ team + player bands | ✅ bands | ✅ | ✅ | ✅ + happiness vectors | ⚠️ numeric % |
| Match sharpness / form | — | ⚠️ | ⚠️ | ✅ “match fit” states | ✅ sharpness/jaded | ✅ sharpness % |
| Match fatigue / tired | ⚠️ energy | ✅ | ✅ | ✅ CM4 ladder | ✅ condition % | ✅ fatigue % |
| **Occurrence** |
| Match injuries | ✅ IJ flag | ✅ | ✅ | ✅ | ✅ + variety | ✅ post-match roll |
| Training injuries | — | ⚠️ inferred | ✅ | ✅ | ✅ intensity-linked | ❌ |
| INJURY PRONE (hidden) | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Context risk (fitness/fatigue) | ⚠️ | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Staff** |
| Physio role | — | ✅ | ✅ | ✅ | ✅ | ✅ profession id 9 |
| Physio ability Fair→Superb | — | ✅ | ✅ | ✅ | ✅ + specialisms | ✅ injury recovery |
| Physio improves **match** fitness | — | ✅ manual | ✅ | ✅ | ✅ | ❌ |
| Doctor / medical centre | — | ❌ | ⚠️ | ⚠️ | 🔮 full dept | ❌ |
| **Manager actions** |
| Rehabilitate (paid) | ❌ | ✅ | ✅ | ✅ | ✅ physio plan | ✅ |
| Insure / Uninsure | ❌ | ✅ | ✅ | ✅ | ⚠️ rare later | ✅ |
| Lilleshall / national rehab | ❌ | ✅ | ✅ | ⚠️ equivalents | 🔮 specialist | ✅ |
| Surgeon abroad | ❌ | ✅ rich clubs | ✅ | ✅ | ✅ | ✅ balance gate |
| Forced retirement payout | ❌ | ✅ insurance | ⚠️ | ⚠️ | 🔮 | ❌ deferred |
| **Economy** |
| Wages while injured (insured) | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Treatment fees | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ mod tables |
| **Presentation** |
| In-match injury event | ✅ flag | ✅ commentary | ✅ | ✅ | ✅ 3D + text | ❌ |
| News / inbox injuries | ❌ | ✅ | ✅ | ✅ | ✅ + social | ✅ MODERATE+ |
| **FM-only depth (do not chase for CM93 claim)** |
| Body-part injury map | ❌ | ❌ | ❌ | ⚠️ early | 🔮 | ❌ |
| Scan / % rehab per area | ❌ | ❌ | ❌ | ❌ | 🔮 | ❌ |
| Sickness / virus | ❌ | ❌ | ⚠️ | ⚠️ | 🔮 | ❌ |
| Light training when injured | ❌ | ❌ | ⚠️ | ✅ | 🔮 | ❌ |
| Press / happiness / jadedness | ❌ | ❌ | ❌ | ⚠️ | 🔮 | ❌ |

### How to read the table

- **CM1 → CM93** is the largest step-up (insurance, physio estimate, treatment menu). Futtoboru **matches CM93 on that step-up** for injuries; **matches CM1+ on fitness existence** and **exceeds CM1 on injuries**.
- **CM96–CM03 / CM4** add fitness **state labels**, training injuries, richer labels — Futtoboru is **behind** those polish items unless training ships.
- **Modern FM** adds anatomical and medical-centre simulation — explicitly **out of Futtoboru Layer B** per [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md).

---

## 5. Futtoboru vs CM93 — one-page truth table

| PO question | Answer |
|-------------|--------|
| Can we claim **CM93 injury parity**? | **Yes**, with footnotes: no forced-retirement payout; no in-match commentary; `[IJ]` icon cosmetic only |
| Can we claim **CM93 fitness parity**? | **No** — missing physio match-fitness, morale sim, training recovery, morale bands |
| Can we claim **better than CM1**? | **Yes** on entire condition pillar |
| Are we at **FM injury depth**? | **No** — ~15–20% of modern FM medical column in §4 |
| Manual-ready for injuries? | **Yes** — ch.6 + ch.7 draft synced to IMPLEMENTATION §10 |
| Manual-ready for full condition pillar? | **Partial** — ch.6 OK; training/morale sim need “not in game” or future chapter |

---

## 6. Recommended PO / doc actions (analysis only)

| Priority | Item | Rationale |
|----------|------|-----------|
| P0 | Stop citing **~39%** parity anywhere | Superseded by B5.2 |
| P1 | Split marketing: **injury economy** vs **fitness/condition** | Avoid overclaim on training/morale |
| P2 | Optional **M-B5.3** micro-milestone | Physio match-fitness tick; morale sim hook — CM93 §Staff ii |
| P3 | Defer **M-B5.4 presentation** | In-match injury line + `[IJ]` styling |
| P4 | Keep **T3-6 forced retirement** deferred | Rare; needs tribunal design |

---

## 7. Document chain update

```
RESEARCH_CM93_INJURY_SUBSYSTEM.md
RESEARCH_EARLY_CM_INJURIES_VS_FM.md
    → ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md (this doc)
    → IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md §9–§10
    → PM_MILESTONE_CM93_INJURY_PARITY.md
    → manual 06 + 07
```

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-01 | Initial post-B5.2 parity audit + CM/FM condensed field map |

---

*Refresh after M-B5.3 fitness polish or presentation milestone.*
