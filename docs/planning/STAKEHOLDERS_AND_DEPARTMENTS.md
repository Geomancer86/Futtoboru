# Futtoboru — Stakeholders & Feature Departments

**Document type:** Governance / organization  
**Date:** 2025-06-25  
**Status:** Active — use for v1 delivery

---

## 1. Purpose

This document defines **who decides what** and **how work is grouped** so the Product Owner is not the bottleneck for every feature. Each **department** owns a family of similar features. Each department has a **feature expert** who signs off core vs broader scope.

**Related deep dives (v1 core departments):**
- [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md)
- [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md)
- [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md)
- [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) *(supporting gate)*

**Product scope:** [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md)  
**Issue backlog:** [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md)

---

## 2. Leadership roles

| Role | Accountable for | Does not own |
|------|-----------------|--------------|
| **Product Owner (PO)** | Priority, v1 in/out, trade-offs | Historical accuracy details, CI config |
| **Project Manager (PM)** | Schedule, milestones, release, CI policy | Feature design |
| **Sponsor / Business** | Time and budget ceiling | Day-to-day backlog |

---

## 3. Feature departments (six)

```text
                         PO (priority) + PM (schedule)
                                    │
    ┌───────────────┬───────────────┼───────────────┬───────────────┐
    ▼               ▼               ▼               ▼               ▼
 SEASONS         MATCH           WORLD           ECONOMY         ROSTER
 & RULES         & SIM           & DATA          & CLUB          & PEOPLE
 [CORE v1]       [CORE v1]       [CORE v1]       [support]       [broader]
    │               │               │
    └───────────────┴───────────────┴──► PLATFORM & UX [CI gate]
```

### v1 focus rule

**Only three departments are core for v1.0:** Seasons & Rules, Match & Simulation, World & Data.  
Platform & UX provides the **test harness and CI gate**. Economy, Roster, and broader UI are **supporting or deferred** unless PO promotes them.

---

## 4. Department roster

| Department | Feature expert *(assign name)* | Core (v1) | Broader (post-v1) |
|------------|----------------------------------|-----------|-------------------|
| **Seasons & Rules** | _________________ | Season loop, rules, scripts, completion, rollover | Cups, re-election, multi-country |
| **Match & Simulation** | _________________ | League fixtures, sim, standings input | Tactics, injuries, cup sim |
| **World & Data** | _________________ | Season 18+19 mods, rules files, validation | Season 20+, modding tools |
| **Platform & UX** | _________________ | Harness test, CI, save/load, critical UI gates | Full GUI E2E, all screens |
| **Economy & Club** | _________________ | Expense reset on rollover | Transfers, board |
| **Roster & People** | _________________ | Squads load for sim | Jobs, training, scouting |

---

## 5. Core vs broader (definitions)

| Tier | Definition | PO rule |
|------|------------|---------|
| **Core** | Required for [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) | Ships in v1.0 |
| **Broader** | FM depth, polish, extra modes | Backlog until core harness is green |

Department experts propose core/broader split; PO approves. Experts **veto** scope in their domain (e.g. historical designer rejects wrong 1888 rules).

---

## 6. Department dependencies (v1)

```text
World & Data ──rules files, season 19──► Seasons & Rules
Seasons & Rules ──calendar, completion──► Match & Simulation
World & Data ──squads, stadiums────────► Match & Simulation
Match & Simulation ──results───────────► Seasons & Rules (season complete?)
Seasons & Rules ──rollover─────────────► World & Data (load season 19)
Platform & UX ──GameTestHarness────────► all (integration proof)
Economy & Club ──season finance reset────► Seasons & Rules (rollover)
```

**Integration disputes:** PO decides. **Domain disputes:** department expert decides.

---

## 7. Sign-off matrix (before v1 tag)

| Gate | Expert | Criterion |
|------|--------|-----------|
| **G1 — Data** | World & Data | Season 18+19 folders, rules files, validation tests pass |
| **G2 — Match** | Match & Simulation | 132 league games sim; attendance bugs fixed; league-only completion count |
| **G3 — Seasons** | Seasons & Rules | Season completes, champion, rollover to season 19 with correct rules |
| **G4 — Platform** | Platform & UX | `SeasonPlaythroughIT` green; CI fails on test failure |
| **G5 — PO** | Product Owner | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) checklist signed |
| **G6 — PM** | Project Manager | Release notes, branch merged to `develop`, tag created |

---

## 8. Cadence

| Meeting | Attendees | Output |
|---------|-----------|--------|
| **Weekly core council** (30 min) | PO, PM, 3 core dept experts, Platform expert | Blockers, dependency order |
| **Sprint planning** | PO + PM + relevant experts | Core items only per department |
| **Pre-merge review** | Owning dept expert | Feature sign-off in their lane |
| **Pre-release** | All experts + PO | G1–G6 gates |

---

## 9. RACI (v1 season loop)

| Activity | PO | PM | Seasons | Match | World | Platform |
|----------|----|----|---------|-------|-------|----------|
| v1 scope | **A** | C | C | C | C | C |
| Historical rules | I | I | **A** | C | C | I |
| Mod content season 19 | I | I | C | I | **A** | I |
| SeasonManager / rollover | C | C | **A** | C | C | C |
| Match sim bugs | I | I | C | **A** | I | C |
| GameTestHarness + CI | I | **A** | C | C | C | **R** |
| Release tag | **A** | **R** | C | C | C | C |

*A = accountable, R = responsible, C = consulted, I = informed*

---

## 10. Open assignments (fill in)

| Department | Expert name | Backup | Start date |
|------------|-------------|--------|------------|
| Seasons & Rules | | | |
| Match & Simulation | | | |
| World & Data | | | |
| Platform & UX | | | |
| Economy & Club | | | |
| Roster & People | | | |
| PO | | | |
| PM | | | |

---

## 11. One-line strategy

**v1 = three core departments deliver season loop + rules config; Platform proves it with an automated two-season test; everything else stays broader until G1–G4 are green.**
