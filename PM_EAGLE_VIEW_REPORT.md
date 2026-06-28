# PM Eagle View — Futtoboru Product & Roadmap

**Document type:** PO / executive visibility report  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch` (62 commits ahead of `origin/feature/competitions-finances-seasons`)  
**Audience:** **PO first**, then PM, sponsors, department leads  
**Purpose:** One place to see **what we are building**, **how far we are**, **what is designed but not built**, and **what only the PO can decide** — before scope grows further.

> **Drill-down docs:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) (execution) · [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) (product north star) · [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) (full map)

---

## 1. What Futtoboru is (one paragraph)

An **open-source historical football management sim** starting in **1888 England**, built to grow into a **CM1-class loop** with **FM-grade historical data ambition**: real league structures, hiatuses, and world events reported through an **inbox / newspaper**; players discover clubs and people over time; and **Hearts-of-Iron-style** career rules let history stay strict or diverge at fork events (Argentina vote, 1892 re-election, etc.). The **season engine is proven headless**; the **management game and living world** are still mostly design + stubs.

---

## 2. Four horizons (eagle map)

Think of the product as **four stacked horizons** — not four releases you must ship at once.

```
┌─────────────────────────────────────────────────────────────────────────┐
│  H4  WORLD & TIME          V2 nations · Americas · master DB · forks    │
│      (design heavy)        WE-1 · NEWS · FLEX · KNOW · R-H4b–R-H12      │
├─────────────────────────────────────────────────────────────────────────┤
│  H3  LAYER C  (V1.5)       1892 Division Two · new clubs in live save   │
│      (~0% built)           WorldEvolutionManager · 1892 content packs   │
├─────────────────────────────────────────────────────────────────────────┤
│  H2  LAYER B  (V1 MVP)     Pick XI · tactics · sim v2 · fans · ratings  │
│      (~25% built)          “Feels like managing” — biggest gameplay gap │
├─────────────────────────────────────────────────────────────────────────┤
│  H1  LAYER A  (V1.0)       Playable UI loop · league + cup · save/load   │
│      (~90% auto · QA open) Manual sign-off · 3 UI fixes landed in code  │
└─────────────────────────────────────────────────────────────────────────┘
         ▲
         NOW — finish H1 QA, then H2 is the critical path
```

| Horizon | PO question it answers | Built? | Designed? |
|---------|------------------------|--------|-----------|
| **H1 Layer A** | Can I play 1888 league+cup in the UI without broken buttons? | **Mostly** | ✅ |
| **H2 Layer B** | Does picking a team and tactics **matter**? | **No** (RNG sim) | ✅ |
| **H3 Layer C** | Does 1892 history **enter my save** without restart? | **No** | ✅ |
| **H4 World** | Do news, forks, latency, and new countries feel **historical**? | **Stubs only** | ✅ |

---

## 3. Progress at a glance

| Area | % (honest) | Evidence |
|------|------------|----------|
| **Season loop (headless)** | **~95%** | 60-season soak, league+cup ITs, rollover |
| **Layer A playable UI** | **~85%** | A-2/A-3/A-4 coded; **manual QA not done** |
| **Layer B management feel** | **~25%** | Full-squad RNG sim; tactics placeholder |
| **Layer C 1892 expansion** | **~0%** | Design + PO briefs only |
| **Historical world (news/forks/knowledge)** | **~5%** | Docs + `HistoricalMode` + latency stub |
| **Automated tests** | **62/63 pass** | 1 legacy `PreferenceManagerTests` asset error |
| **Documentation** | **High** | 30+ active PM/design docs on branch |

**Bottom line:** We have a **strong engine prototype** and a **large design corpus**. The gap is **player-facing management** (H2) and **everything that makes history “alive”** (H3–H4).

---

## 4. Three product pillars (scope is growing here)

These three pillars were separate threads; they are now **one product story**. PO should treat them as **linked but sequenced**.

| Pillar | PO intent | Status | Primary doc |
|--------|-----------|--------|-------------|
| **A. Finish the loop** | Employed manager, 1888, procedural years | H1 — near done | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |
| **B. Management sim** | XI, tactics, credible sim, crowds, ratings | H2 — not started | [PM_V1_MVP_AND_1892_MASTER_PLAN.md §5](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |
| **C. Historical world** | News, hiatus, forks, knowledge, new countries | H3–H4 — design only | [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) |

**Scope risk:** Pillar C adds **many systems** (NEWS-*, FLEX-*, KNOW-*, WE-*, R-H*) before Pillar B ships. **Recommendation:** lock sequencing — **H1 → H2 → H3**, with H4 fork **content** only after `WorldEvolutionManager` exists.

---

## 5. What is proven vs designed vs coded (PO cheat sheet)

| Capability | Proven 🧪 | Designed 📄 | Coded ⚙️ |
|------------|-----------|-------------|----------|
| Procedural season + FA Cup | ✅ | ✅ | ✅ |
| Save/load JSON | ✅ | ✅ | ✅ |
| Mandatory draw inbox gates | ✅ | ✅ | ✅ |
| Continue / load / match history UI | — | ✅ | ⚙️ **needs QA** |
| Pick XI affects match | ❌ | ✅ | ❌ |
| Attendance = table + form + derby | ❌ | ✅ | ❌ |
| Post-match ratings | ❌ | ✅ | ❌ |
| 1892 Div 2 in running save | ❌ | ✅ | ❌ |
| `WorldEvolutionManager` | ❌ | ✅ | ❌ |
| World news / hiatus messages | ❌ | ✅ | ❌ |
| Historical Mode on save | ✅ | ✅ | ⚙️ **save field only** |
| News latency by distance | ✅ | ✅ | ⚙️ **resolver stub** |
| Fork votes / journalist agency | ❌ | ✅ | ❌ |
| Player knowledge fog | ❌ | ✅ | ❌ |
| Scotland / Ireland V2 packs | ❌ | ✅ | ❌ |

---

## 6. PO decision queue

### 6.1 Locked (do not re-litigate without PO)

| Topic | Decision |
|-------|----------|
| Formations MVP | 2–3 templates on **extensible schema** |
| Attendance | Table + opponent + **form** + **derbies** |
| Historical Mode default | **`SANDBOX_FIRST`**; second = **`HISTORICAL_FIRST`** |
| Americas | **On roadmap** (Argentina fork template) |
| Agency | **All leagues**; remote = **journalist inbox**; distance weights |
| News | **Latency by era + distance** |
| Git / distribution | PO-only; **no agent push** |

Refs: [PM_REPORT_HISTORICAL_MODE_PO_LOCK.md](PM_REPORT_HISTORICAL_MODE_PO_LOCK.md) · [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md)

### 6.2 Open — blocks or branches work

| Priority | Decision | Options | Blocks |
|----------|----------|---------|--------|
| **P0** | **Layer A sign-off** | Run manual QA checklist | Any “V1.0” call |
| **P0** | **1892 Div 1/Div 2 membership** | A / B / C / D | 1892 content scripts |
| **P1** | **V2 first country** | Scotland (rec.) / Ireland / Wales | R-H4b pack order |
| **P1** | **Argentina alt 1892 depth** | Minimal vs full branch | R-H12-alt scope |
| **P2** | Inbox cap long careers | Archive vs waiver BLK-4 | 60-season UX |
| **P2** | Promotion/relegation post-1892 | Defer (default) | Post-1892 sim |

Briefs: [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) · [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md)

---

## 7. Recommended sequence (single critical path)

PO can adopt this as the **default plan** unless you explicitly reprioritize.

```
NOW     H1  Manual QA (A-1, A-5, A-9)           ~1–2 weeks
        ↓
NEXT    H2  MVP-10 → MVP-1 XI → MVP-3 sim → MVP-4 attendance → MVP-5 ratings   ~6–10 weeks
        ↓         (parallel: DB-1/DB-2 infra for 1892 — data only)
        ↓
THEN    H3  WE-1 WorldEvolutionManager + 1892 pack (after PO picks 1892 A/B/C/D)   ~8–12 weeks
        ↓
THEN    H4  NEWS-2 world messages + FLEX-2 forks (Argentina proves pipeline)
        ↓
THEN    V2  Scotland (R-H4b) → Ireland → … · Americas R-H12 when fork engine proven
```

**Do not start in parallel unless PO adds capacity:** full fork/agency UI + Layer B sim + 1892 content = **three products at once**.

---

## 8. Engineering tracks (for PO visibility)

| Track | IDs | Depends on | User-visible when |
|-------|-----|------------|-------------------|
| **Ship** | A-* | — | Playable 1888 career |
| **Management** | MVP-1–10 | Layer A | Pick team, believable results |
| **World evolution** | WE-1–8 | DB-1, Layer B stable | 1892 clubs appear mid-save |
| **News** | NEWS-1–8 | WE-1 | Inbox reports world events |
| **Forks / agency** | FLEX-1–6, AGENCY-1 | WE-1, NEWS-7 | HoI-style choices |
| **Knowledge** | KNOW-1–5 | Layer B | Fog of war on players |
| **Content** | R-H*, 1892-*, R-H12 | PO decisions | New leagues/countries |

---

## 9. Risks (eagle level)

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Design outruns code** | PO expects living world; game is still RNG loop | Eagle view + sequence §7; sign H1 then H2 |
| **1892 membership undecided** | Data team blocked on scripts | PO picks A/B/C/D ([brief](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md)) |
| **Fork scope before engine** | Argentina UI without `WorldEvolutionManager` | FLEX after WE-1 |
| **Test drift** | 63 tests; 1 asset flake | Fix `PreferenceManagerTests` or exclude from gate |
| **Doc sprawl** | PO loses thread | **This doc** + index hub; drill down only when deciding |

---

## 10. Recent delivery (branch snapshot)

| Date | Deliverable |
|------|-------------|
| 2026-06-27 | Historical accuracy + news + knowledge design |
| 2026-06-27 | HoI-style forks + Argentina PO brief |
| 2026-06-27 | PO lock: Sandbox default, agency, latency |
| 2026-06-27 | Layer A code: C-1, C-3/H-9, H-3; FLEX-1 + NEWS-8 stubs |

Latest commit: `6840610` — see [PM_LAYER_A_SPRINT_PROGRESS.md](PM_LAYER_A_SPRINT_PROGRESS.md).

---

## 11. Where PO goes next (suggested agenda)

1. **Approve or adjust sequence §7** — especially whether H2 is mandatory before any 1892 work.  
2. **Run or delegate Layer A manual QA** — unblocks “V1.0 complete” language.  
3. **Pick 1892 membership option** (A/B/C/D) — unblocks World & Data.  
4. **Confirm V2 first country** (Scotland recommended) — unblocks research packs.  
5. **Explicitly defer or commit** to fork/agency **UI** in Layer B vs after WE-1.

---

## 12. Document map (one screen)

| If PO wants to understand… | Read |
|----------------------------|------|
| **Whole picture** | **This doc** |
| Execution layers & MVP IDs | [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |
| Historical world + news + forks | [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) |
| Live save evolution | [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) |
| 1892 England choice | [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) |
| Argentina fork | [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md) |
| V2 countries | [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) |
| What code actually does | [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) |
| Latest sprint | [PM_LAYER_A_SPRINT_PROGRESS.md](PM_LAYER_A_SPRINT_PROGRESS.md) |

---

## 13. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial eagle view for PO visibility |

---

*Single entry point for scope conversations. Update when PO locks 1892 membership or Layer A sign-off.*
