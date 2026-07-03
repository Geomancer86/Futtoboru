# Eagle View — PO Brief (R-1888 Gap & Roadmap)

**Date:** 2026-07-03 · **Branch:** `analyze-ai-branch`  
**Audience:** PO (primary) · PM · Sponsor  

**Detail:** [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) · [PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md](PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md)  
**PO action:** [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md)  

---

## In one sentence

**Futtoboru 1888 is ~92% ready for human testers** — eight known blockers remain; **~55 CM93 features are intentionally deferred** to later named releases (1892 transfers, 1906 insurance, 1993 CM93 menus), not missing by accident.

---

## Three buckets (do not merge in conversation)

```
┌─────────────────────────────────────────────────────────┐
│  TIER A — R-1888 BLOCKERS (8)     → fix before B-9      │
│  ME-1 injury · UI bugs · soak · manual                  │
├─────────────────────────────────────────────────────────┤
│  TIER B — INTENTIONAL DEFER (~55) → later R-YYYY        │
│  transfers · scouts · discipline · reserves · CM93 UI   │
├─────────────────────────────────────────────────────────┤
│  TIER C — FULL CM93 (~66 None)    → multi-year north star│
│  not R-1888 claim                                       │
└─────────────────────────────────────────────────────────┘
```

---

## R-1888: in vs out

| In (ship) | Out (document; ship later) |
|-----------|----------------------------|
| League + cup + ∞ seasons | Transfers & market (15 CM93 features) |
| Pick XI, tactics, preview, ratings | CM93 insurance / Lilleshall / surgeon |
| Victorian injuries (Trainer, ME-1) | Scouts, coach hiring, discipline |
| 1888 data & wages | Reserves, Div 2, national teams |
| 131 + 5 automated tests | Hotseat, commentary, full CM93 tactics |

**Catalogue:** 106 CM93 features traced — **~35 Partial+ for R-1888 scope** ([RESEARCH_CM93_FEATURE_CATALOGUE_V1.md](../research/RESEARCH_CM93_FEATURE_CATALOGUE_V1.md)).

---

## When missing features return (PO-facing)

| When | What | Why |
|------|------|-----|
| **Now → B-9** | Tier A blockers only | Finish first release honestly |
| **R-1888.1** | Polish (icons, result lines) | Human QA feedback |
| **R-1892** | Div 2, reserves, **transfers Core** | World expansion — Layer C |
| **R-1906** | Injury **insurance** UI (ME-2) | Workmen’s Comp era |
| **R-1951** | Lilleshall (ME-4) | NSC opens |
| **R-1965** | In-match **substitutes** | Football League rule |
| **R-1993** | Full **CM93 injury menus** (ME-6) | Only then claim CM93 injury parity |

**Assistant managers & stadium expansions:** not CM93 — **CM4 / CM99/00** tranches (~R-2003+), not on 1888 critical path.

---

## Traffic lights

| | |
|-|-|
| 🟢 | CM93 catalogue · gap analysis · yearly release model · automated tests |
| 🟡 | PO sign-off on roadmap · ME-1 implementation |
| 🔴 | 8 Tier A blockers · human QA not started (correct) |
| ⚪ | Public release — still none per [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) |

---

## PO decision needed

Sign **[PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md)** — locks R-1888 scope, Tier A list, and release sequencing. Reply template included for paste into decision log.

---

## Horizons (unchanged north star)

| Now | **R-1888** — Victorian CM1 loop + ME-1 |
| Mid | **R-1892** — expansion + transfers |
| Long | Medical eras 1906→1993, news, multi-country |
| Far | Full CM + FM historical completeness |

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | R-1888 gap eagle view — supersedes stale ~42% Layer B figure |

---

*PM full report: [PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md](PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md).*
