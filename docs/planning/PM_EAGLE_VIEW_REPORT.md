# Eagle View — PO / PM Brief

**Date:** 2026-06-30 · **Branch:** `analyze-ai-branch`  
**Direction lock:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md)

---

## In one sentence

We are **building locally, not shipping** — **Layer B** (management feel) is **now**, while the **long-term goal** remains the full **CM + FM** feature horizon.

---

## How we think about time

| Horizon | Focus |
|---------|--------|
| **Short term** | Layer B — XI, tactics, match sim, attendance, ratings |
| **Mid term** | 1892 world expansion, save evolution, deeper sim systems |
| **Long term** | News, forks, multi-country DB, FM-grade depth |
| **North star** | Everything CM and FM ever did — **light years ahead** |

**No release date.** One season or fifty seasons of local work is fine until it feels right.

---

## Traffic lights

| Area | Status |
|------|--------|
| **Season engine (Layer A headless)** | 🟢 League, cup, save/load, tests green |
| **Layer B management loop** | 🟡 **Active work** — designed, coding starts now |
| **Long soak (60 seasons)** | 🟡 Optional confidence run — preflight first |
| **1892 / world / forks** | ⚪ Mid term — after Layer B core |
| **Public release** | ⚪ **Not planned** — local development only |

---

## Current priority (locked)

**Start Layer B.** Execution: [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) · design: [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md)

| Layer B deliverable | Player-visible outcome |
|---------------------|------------------------|
| MVP-1 Pick XI | Your 11 plays, not random squad average |
| MVP-2 / MVP-9 Tactics | Formation presets affect match |
| MVP-3 Sim v2 | Results reflect team strength, less RNG |
| MVP-4 Attendance | Crowds follow table, form, derbies |
| MVP-5 / MVP-7 Ratings | Post-match 0–10 ratings |

Fix Layer A UI bugs (**C-1**, load/save, menu) **alongside** Layer B when they get in the way — not as a ship gate.

---

## Critical path

```
NOW     Layer B — CM1-minimum loop
MID     Layer C — 1892 + world evolution
LONG    Historical world + FM-depth systems
FAR     Full CM + FM historical completeness
```

---

## Engineering hygiene (unchanged)

- PR test suite stays green (`mvn test -pl futtoboru-core`)  
- Long soaks: **preflight first** — [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md)  
- Do not pipe soak output through filters  

---

## Detail docs

| Topic | Document |
|-------|----------|
| PO direction (authoritative) | [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) |
| Layer B execution | [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) |
| Tests & soak | [PM_V1_STATUS_AND_NEXT_STEPS.md](PM_V1_STATUS_AND_NEXT_STEPS.md) |
| Old release criteria (reference only) | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | PO lock: no release; Layer B now; CM+FM north star; short/mid before long |
| 2026-06-30 | Simplified meeting brief (superseded by direction lock) |
| 2026-06-27 | Initial eagle view |
