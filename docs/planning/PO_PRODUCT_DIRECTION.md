# PO Product Direction (Locked)

**Date:** 2026-06-30 (updated 2026-07-03)  
**Status:** **Active** — overrides release-first wording in older docs until those docs are updated  
**Audience:** PO, PM, all contributors  

> **PO routine reading:** [PO_BRIEF.md](PO_BRIEF.md) only. This doc = north star + principles.

---

## North star (long term)

Build toward **full parity with the combined feature set of Football Manager and Championship Manager across their history**. That horizon is **years away**. We are not pretending otherwise.

---

## How we work (short & mid term)

| Principle | Direction |
|-----------|-----------|
| **Release** | **No public release planned.** Work **locally** until the team feels ready — whether that is after 1 season of polish or 50 seasons of depth. |
| **Release naming** | When milestones ship, use **`Futtoboru {YYYY}`** / tag **`{YYYY}.0.0`** per [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md). First tranche: **R-1888**. |
| **Sequencing** | **Short term and mid term first.** Nail the next playable layer before expanding world/history scope. |
| **Current focus** | **R-1888** — Futtoboru 1888; ~92% pre-human QA. See [PO_BRIEF.md](PO_BRIEF.md) |
| **Long soaks / 60 seasons** | **Engineering confidence**, not a ship gate. Run with preflight when useful ([SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md)). |
| **Layer A UI bugs** | Fix **when touched** or when they block Layer B work — not a release checklist blocker. |

---

## Horizon map (simplified)

```
NOW       Layer B — CM1-minimum management loop (XI, tactics, sim, ratings)
MID       Layer C — 1892 expansion, world evolution in live saves
LONG      News, forks, knowledge, multi-country DB, FM-depth systems
FAR       Full CM + FM historical feature completeness
```

---

## What this replaces

- “Ship v1.0 when QA passes” is **not** the active plan.  
- [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) remains **reference acceptance criteria**, not a release deadline.  
- Eagle view and PM status docs follow **this** document.

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | PO_BRIEF as routine PO surface; R-1888 focus replaces stale ~42% Layer B row |
| 2026-07-03 | R-YYYY release naming row |
| 2026-06-30 | Whole Layer B scope: B5 + four-tier testing; human QA last |
| 2026-06-30 | PO lock: no release pressure; Layer B now; long-term CM+FM north star |
