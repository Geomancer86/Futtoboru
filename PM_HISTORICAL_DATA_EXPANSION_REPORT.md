# PM Report — Historical Data Expansion & Research Round 2

**Document type:** PM analysis / planning  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, Sponsor, World & Data, Research leads  
**Companion:** [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **End goal** | Start in **any IRL season** with a **perfect, sourced, community-linted DB**; play forward forever (procedural). |
| **How many “extra years” of data do we need *right now*?** | **Zero** for forward play from 1888 — procedural loop is proven. **2–4 additional *start-season* mods** (not calendar years) for meaningful expansion after infrastructure. |
| **What do we have today?** | **1** pickable season (1888–89): **35 clubs**, **556 named players**, **35 stadiums**, FA Cup, rules file, founding years on clubs. |
| **What blocks scaling?** | **Engineering** (IDs, validation, scripts-on-disk) before **content volume**. **Research** pipeline for sources beyond Wikipedia. |
| **CM/FM roadmap fit?** | Gameplay roadmap ([PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md)) **Phase A–B first**; data **Tier 0 lint** in parallel; **Tier 1 start mods** after DB-1–DB-7. |

---

## 2. Current database inventory (verified 2026-06-27)

### 2.1 Start seasons (pickable at new game)

| Metric | Count |
|--------|------:|
| Rows in `mods/seasons.txt` | **1** (id `18`, 1888–89) |
| Playable countries in season mod | **1** (England `1000`) |
| Clubs in `seasons/18/clubs.txt` | **35** (12 league + 23 cup/other) |
| Clubs with `foundation_year` | **35** (1874–1888 range) |
| Player files | **35** |
| **Total named players** | **556** |
| Stadium files | **35** |
| `competition_rules.txt` | **1** |
| FA Cup edition participants | **32** |

**League squad depth (players per club id):** 18–35 for ids 1–12; ~11 for many cup-only clubs (minimum squad placeholder).

### 2.2 Global registry (not yet playable)

| Metric | Count |
|--------|------:|
| Countries in `mods/countries.txt` | **~193** |
| Nations with implemented leagues | **0** (England league created by script at runtime) |
| Competitions in `competitions.txt` | **1** (FA Cup) |
| Hardcoded timeline scripts | **2** types (league + cup) in Java |

### 2.3 Procedural in-career years

| Metric | Value |
|--------|-------|
| Extra mod data per simulated year | **0** |
| Proven automated depth | **60** league champion editions |
| Wall time (league-only, quiet harness) | ~**5.4 s/season** |

---

## 3. “How many years of extra data?” — direct answer

### 3.1 If you mean “play more seasons forward from 1888”

**Answer: 0 years of extra content.**

The engine generates season 2, 3, … ∞ from save state. Sixty seasons are proven without any `mods/seasons/19/` folder.

### 3.2 If you mean “more seasons the player can *choose* as career start”

**Answer: count *start mods*, not calendar years.**

| Expansion tier | Additional **start mods** | Calendar examples | Approx. duplicate of season 18 effort |
|----------------|---------------------------|-------------------|--------------------------------------|
| **Tier 0 (now)** | 0 — polish existing | 1888–89 only | Lint + validate 556 players |
| **Tier 1 (recommended next)** | **+3** | 1899–1900, 1919–20, 1939–40 | ~**3×** content (~1,700 players if same density) |
| **Tier 2** | **+7–12** | 1892 Div2, 1905, 1925, 1963, 1989, 1992 PL, … | ~**10×** season 18 |
| **Tier 3** | **+1 country** | Scotland 1890–91 | ~**1×** season 18 per country |
| **North star** | **Master DB** or **~25–40 milestones** | “Any important era” | Community program **years**, not one sprint |

**England 1888 → 2026 = 138 calendar seasons** — we do **not** need 137 extra mod folders. Pick **milestones** or build a **year-filtered master database** ([DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md §4.2](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)).

### 3.3 Summary table for PO

| Goal | Extra “years” of mod data |
|------|---------------------------|
| Infinite forward from 1888 | **0** |
| One more English start decade (e.g. 1905) | **+1 start mod** (~550–1,500 players depending on era) |
| Three milestone English starts | **+3 start mods** |
| Scotland + England at same fidelity | **+1 country mod** (~1 start season) |
| Any season in history | **Master DB program** — not a single-year increment |

---

## 4. What still needs fleshing (proven systems → production quality)

### 4.1 Data layer (before new seasons)

| Priority | Item | Status |
|----------|------|--------|
| **P0** | Global person/club ID scheme (WD-9 / DB-1) | ❌ Local ids per club file |
| **P0** | Mod validation CI V-1–V-9 (WD-8 / DB-2) | ❌ Not in CI |
| **P0** | `region_modifiers` Lancashire `countryId` fix (WD-7) | ❌ Bug |
| **P1** | Scripts on disk per season (WD-4 / DB-3) | ❌ Hardcoded |
| **P1** | Dynamic modifier paths (WD-3) | ❌ Hardcoded to `/18/` |
| **P1** | Multi-season loader merge-by-ID (WD-5) | ❌ Duplicates if 2 rows in `seasons.txt` |
| **P1** | Source + lint docs for contributors (DB-7, DB-8) | ❌ |
| **P2** | Cup-only squads at 11 players — verify names vs placeholders | ⚠️ Many at 11 |
| **P2** | Wales (`1002`) in season countries when PO wants | Commented out |

### 4.2 Gameplay layer (parallel track — see roadmap report)

Ship **v1.0 manual QA** before large data authoring. **v1.1** (XI, ratings, unemployed) can run **parallel** to **DB-1–DB-2** — different teams.

---

## 5. Research program (historical football, not CM manuals)

CM/FM **game design** research is strong and **paused** ([CM_LEGACY_RESEARCH_PAUSE.md](CM_LEGACY_RESEARCH_PAUSE.md)) — sufficient for v1 gameplay.

**New track needed:** **Historical football data research** (parallel, World & Data owned).

### 5.1 Source hierarchy (proposed)

| Tier | Source type | Use |
|------|-------------|-----|
| **A** | Contemporary records (FA minutes, Rothmans, club histories) | Rules, dates, re-election |
| **B** | Structured web (Wikipedia season articles, RSSSF) | Squads, tables — current default |
| **C** | Specialist DB (11v11, Football Archive, Transfermarkt for modern) | Post-1900 squads |
| **D** | Community contributors with citations | Linted PRs |

### 5.2 Research deliverables (Round 2)

| ID | Deliverable | Unlocks |
|----|-------------|---------|
| **R-H1** | `HISTORICAL_DATA_SOURCE_LEDGER.md` | Source IDs per row — **stub live** |
| **R-H2** | Schema spec + example lint errors | Community PRs |
| **R-H3** | **1899–1900** research pack (clubs, league size, rules) | First Tier-1 start mod |
| **R-H4** | **Scotland 1890–91** feasibility (clubs, league founding) | Second country |
| **R-H5** | Milestone season shortlist (15–25 rows) | Long-term roadmap |
| **R-H6** | Master DB feasibility study | North star architecture |

### 5.3 CM/FM research alignment

| CM/FM lesson | Futtoboru application |
|--------------|----------------------|
| CM1 used **Rothmans / annuals** | We need **source ledger** + season mod headers |
| FM **one DB, filter by year** | North star master DB (Phase B) |
| CM **database updates** culture | **Community lint + PR** culture |
| CM1 **anonymous players** early | Optional for lower leagues — PO decision |
| FM **multiple nations** | Start mod per country × era — expensive |

**Verdict:** CM/FM research informs **process and bar quality**; **historical football research** is the new gap for data expansion.

---

## 6. Recommended phased plan (decision-ready)

### Wave 0 — Finish v1 (weeks 0–4)

- Manual QA + UI fixes ([PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md) Phase A)  
- **No new start seasons**

### Wave 1 — Data infrastructure (weeks 4–12)

| Deliverable | Owner |
|-------------|-------|
| DB-1 global player IDs + migration for season 18 | World/Data + Eng |
| DB-2 validation tests V-1–V-9 in CI | World/Data |
| DB-3 scripts on disk for season 18 | Seasons/Rules |
| DB-7 contributor `DATA_SCHEMA.md` + lint messages | World/Data |
| Fix WD-7 region modifier bug | World/Data |

**Exit:** Season 18 passes lint; safe to fork for new mods.

### Wave 2 — First expansion content (months 3–6)

| Deliverable | Effort vs season 18 |
|-------------|---------------------|
| Research pack R-H3 (1899–1900) | Research |
| **Start mod id `19` or `25`** (1899–1900) | ~**1.2×** players/clubs |
| Optional: Scotland 1890 research R-H4 | Research |
| Gameplay v1.1 (ratings, XI) if not done | Platform |

**Exit:** **2 pickable start seasons** (1888 + 1899) OR 1888 + Scotland 1890.

### Wave 3 — Milestone library (months 6–18)

- Add **3–5** English milestone starts (1919, 1939, 1992, …)  
- Second country playable end-to-end  
- Begin master DB feasibility (R-H6)

### Wave 4 — North star (18+ months)

- Master historical DB **or** 25–40 milestone mods  
- Community editor / external lint service  
- “Any season” UI picker by year + league

---

## 7. Combined priority matrix (data + gameplay)

Do **not** choose only one track — sequence matters.

| Order | Track | Rationale |
|------:|-------|-----------|
| 1 | **v1.0 ship** (gameplay QA) | Proves product |
| 2 | **DB-1, DB-2** (IDs + lint) | Unblocks all new content |
| 3 | **v1.1 gameplay** (XI, ratings) | CM1 parity — uses same squads |
| 4 | **Wave 1.5** — WE-1–WE-8 + **1892 expansion** (PO leaning) | World evolution + second division |
| 5 | **First new start mod** (1899 or Scotland) | Validates multi-start pipeline |
| 6 | **Master DB study** | Informs north star funding |

---

## 8. Risks

| Risk | Mitigation |
|------|------------|
| Author 10 mods before DB-1 | **Block** — Wave 1 gate |
| Confuse procedural years with start mods | This report + design doc |
| Wikipedia-only sourcing | Source ledger; require 2nd source for squads |
| 556-player id collision on scale | Global ID scheme first |
| Scope “any season” too early | Milestone list + master DB study |
| CM research pause = data research pause | **Separate** R-H track |

---

## 9. Metrics to track

| KPI | Today | Wave 1 target | Wave 2 target |
|-----|------:|--------------:|--------------:|
| Pickable start seasons | 1 | 1 (lint-clean) | 2–3 |
| Named players (lint-clean) | 556 | 556 validated | ~1,200+ |
| Countries playable | 1 | 1 | 1–2 |
| CI validation rules passing | 0 | 9 | 9 |
| Milestone seasons researched | 0 | 0 | 1 pack |
| Source rows with URL | ~partial | 100% season 18 | 100% new mods |

---

## 10. PO decisions requested

| # | Decision | Options |
|---|----------|---------|
| 1 | First new start mod after Wave 1? | **A)** 1899–1900 England **B)** 1890–91 Scotland **C)** Hold at 1888 only |
| 2 | Milestone vs master DB north star? | **A)** Milestones first **B)** Master DB study now |
| 3 | Minimum research source? | **A)** Wikipedia + URL **B)** Two independent sources for squads |
| 4 | Community contributions in v2? | **A)** Git PR only **B)** In-game editor later |

---

## 12. In-play world evolution (new requirement)

**Principle:** Starting at the **earliest** season, every club, competition, country, rule change, and player that appears later in real history must enter the **running `SaveGame`** on the correct date — **not** via a new mod folder or career restart.

| Today | Gap |
|-------|-----|
| League/cup scripts copy clubs into save | ✅ Pattern exists |
| All England clubs loaded at new game | ⚠️ Eager load — not evolution-friendly at scale |
| `foundation_year` on clubs | ❌ Not used for activation |
| Procedural rollover | ❌ Same participants forever |
| Script types beyond 1000/1001 | ❌ Not implemented |

**Target:** Dormant registry + timeline scripts (1002–1007) + eventual master DB projection. **Wave 1.5** engineering (WE-1–WE-8) after DB-1/DB-2, before authoring many start mods.

**Canonical design:** [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)

**UI / LibGDX:** No framework blocker for 1892 — extend competitions tables + inbox; [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md §10](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md).

**PO decisions (§9 of design doc):** auto vs script activation; lazy new-game load; first milestone (Nov 1888 rules vs 1892 Div 2 vs Scotland).

---

## 11. Document map

| Document | Purpose |
|----------|---------|
| [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) | Architecture |
| [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) | Engineering tasks |
| [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md) | Gameplay features |
| [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | Procedural loop |
| [HISTORICAL_DATA_SOURCE_LEDGER.md](HISTORICAL_DATA_SOURCE_LEDGER.md) | Source provenance (R-H1) |
| [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) | Live save incorporation |

---

*Next PM update: after PO decisions on §10 and Wave 1 kickoff.*
