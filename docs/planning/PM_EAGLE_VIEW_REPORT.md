# Eagle View — PO / PM Meeting Brief

**Date:** 2026-06-30 · **Branch:** `analyze-ai-branch`  
**Audience:** Product Owner (primary), PM  
**Use this doc in the meeting.** Detail lives in linked docs — do not read those unless deciding.

---

## In one sentence

The **1888 league + FA Cup simulation works in automated tests**; **v1 is not shippable yet** because nobody has signed off **playing it in the UI** and a few **UI bugs** remain.

---

## Traffic lights

| Area | Status | Plain English |
|------|--------|---------------|
| **Season engine** | 🟢 | League, cup, save/load, multi-season — proven headless |
| **Automated tests** | 🟢 | 100/100 PR suite; cup E2E green |
| **Long soak (60 seasons)** | 🟡 | Not re-run since fix; **preflight first** (~90s), then ~5 min quiet run |
| **Playable v1 in UI** | 🔴 | Manual QA not done; continue/load/menu bugs open |
| **“Feels like managing” (tactics, XI matter)** | 🔴 | Designed, not built — **after** v1 loop ships |
| **1892 / world news / forks** | ⚪ | Designed only — **not now** |

---

## Three decisions for this meeting

Pick **yes / no / defer** on each. Everything else follows from these.

| # | Decision | Recommendation | If yes → |
|---|----------|----------------|----------|
| **1** | **Ship v1.0 when UI QA passes** (employed manager, one full 1888 season in the game, not just tests) | **Yes** — this is the product goal | Team fixes C-1, load/save, menu; PO or delegate runs [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) |
| **2** | **Run 60-season soak after preflight** (confidence check, not a player feature) | **Yes, once** — record pass/fail | `.\scripts\run-soak-with-preflight.ps1` (~6 min total); see [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) |
| **3** | **Start Layer B** (pick XI, tactics affect results) **before** v1 UI sign-off | **No** — defer | Finish H1 first; Layer B is the **next** product phase |

**Not on the table today:** 1892 Division Two, Argentina fork, Scotland pack, cup qualifying rounds, full news/inbox world sim.

---

## Critical path (one line)

**UI manual QA → fix blockers → tag v1.0 → then Layer B management depth.**

```
NOW     Play 1888 in UI + sign checklist     (~1–2 weeks)
NEXT    Layer B — XI / tactics / believable sim   (~6–10 weeks)
LATER   1892 expansion + living world (needs PO picks later)
```

---

## What changed recently (30 seconds)

- FA Cup scheduling fixed; 32 correct 1888 teams verified  
- Test suite stable (100 green)  
- Aborted 41-min soak explained — **preflight + quiet logs** so we do not waste time again  

---

## Open UI blockers (v1 only)

| Bug | Player impact |
|-----|----------------|
| C-1 | Continue can skip 2 days |
| C-3 / C-4 | Load game / stale job state |
| H-9 | Top menu Load / Settings / Quit |
| H-3 | Match history navigation |

Full list: [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md)

---

## If PO wants more detail

| Topic | Document |
|-------|----------|
| Test status & soak commands | [PM_V1_STATUS_AND_NEXT_STEPS.md](PM_V1_STATUS_AND_NEXT_STEPS.md) |
| What v1.0 means | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |
| Full roadmap / horizons | Previous eagle sections archived in git history; execution: [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | **Simplified for PO meeting** — 3 decisions, traffic lights, one critical path |
| 2026-06-27 | Initial eagle view (horizons, pillars, decision queue) |

---

*Update after PO locks decisions 1–3 or Layer A QA sign-off.*
