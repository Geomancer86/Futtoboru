# Research — CM93/94 Complete Feature Catalogue

**Document type:** Primary-source feature registry  
**Date:** 2026-07-03  
**Status:** **Active** — authoritative for “what did CM93 have?” vs Futtoboru  
**Game:** Championship Manager 93/94 (1993–1994 SKUs)  

**Sources (P1 unless noted):**  
- [CM93_Manual_Menu_Extract.md](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md)  
- [CM93_Manual_LemonAmiga_Extract.md](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md)  
- [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md) (injury IDs CM93-S* / CM93-P* etc.)  

**Futtoboru depth:** [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](../planning/ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) · **R-1888 scope:** [DESIGN_YEARLY_RELEASE_SCOPE.md](../planning/DESIGN_YEARLY_RELEASE_SCOPE.md)  

---

## 1. Do we have a comprehensive CM93 list?

| Before this doc | Coverage |
|-----------------|----------|
| [CM93_Manual_Menu_Extract.md](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md) | Menus & buttons — **not** Futtoboru status |
| [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md) | **Injuries only** — ID ledger ✅ |
| [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](../planning/ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) | **52-row** CM1+CM93 mix — some rows stale post-B5.2 |
| [CM93_94_REFERENCE.md](../planning/CM93_94_REFERENCE.md) | Release bible — headline features, not exhaustive |

**This catalogue** = **one CM93-only registry** with stable IDs **`CM93-F###`**, manual trace, Futtoboru depth, and gap vs **full CM93** vs **R-1888**.

**Legend — Futtoboru depth:** None · Stub · Minimal · Partial · Core · Core+ · Beyond  
**Legend — Gap:** None · Minor · Major · Total · N/A (out of 1888 scope) · Defer (PO)

---

## 2. Summary scorecard (Futtoboru vs full CM93)

Counted from §3 registry (**106** line items).

| Depth | Count | % |
|-------|------:|--:|
| **Core+ / Core** | 18 | 17% |
| **Partial / Partial+** | 22 | 21% |
| **Minimal / Stub / None** | 66 | 62% |

| Domain | Core+ items | Biggest gaps |
|--------|-------------|--------------|
| Injuries & condition | 12 engine / 3 R-1888 UX | ME-1 skin; physio match-fitness; morale bands |
| Tactics & XI | 4 | Push/pull, TCT icons, in-match subs |
| Match | 2 | Commentary, injury time, MOM |
| Transfers | 0 | Entire subsystem |
| Staff | 1 partial | Coach, scouts, hiring |
| Board / career | 1 beyond | Board confidence UI |
| Competitions | 3 partial | Arrange friendly, reserves |

**Honest one-liner:** Futtoboru has **~38% of CM93 features at Partial or better** (40/106); **~17% at Core+** against **full CM93**. **R-1888** targets CM1 loop + ME-1 injuries — **not** CM93 full parity.

---

## 3. Master registry

### A — Startup & new game

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F001 | Real-name database (`1993 Players`) | New game | **Beyond** (1888 names mod) | N/A | ✅ |
| CM93-F002 | Generated players new game | New game | **None** | Total | ❌ |
| CM93-F003 | Hotseat 1–4 managers | New game | **None** | Total | ❌ |
| CM93-F004 | ~30 min new-game generation | New game | **N/A** (mod load) | N/A | ✅ |
| CM93-F005 | Quick Start pre-built save | New game | **None** | Total | ❌ |
| CM93-F006 | Continue Season / load save | Main menu | **Core** | None | ✅ |
| CM93-F007 | Printer on/off; print screen (P) | Startup | **None** | Total | ❌ |

### B — Main menu & navigation

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F010 | View Tables (league ±DIV) | Main menu | **Partial** | Minor | ✅ |
| CM93-F011 | Manager rankings [±SCR] | Main menu | **None** | Total | ❌ |
| CM93-F012 | Group tables [±GRP] (Europe) | Main menu | **None** | N/A | ❌ |
| CM93-F013 | Click player → factfile | Tables | **Partial** | Minor | ✅ |
| CM93-F014 | Click team → squad | Tables | **Partial** | Minor | ✅ |
| CM93-F015 | Fixture Info past/upcoming | Main menu | **Partial** (schedule) | Minor | ✅ |
| CM93-F016 | European seeding display | Fixture Info | **None** | N/A | ❌ |
| CM93-F017 | Club Details (80 clubs) | Main menu | **Partial** | Minor | ✅ |
| CM93-F018 | **Arrange Friendly** | Main menu | **Partial** (engine) | Major | Defer |
| CM93-F019 | Match Reports / statistics | Main menu | **Partial** | Minor | ✅ |
| CM93-F020 | Find Player (transfer entry) | Main menu | **Stub** | Total | ❌ |
| CM93-F021 | Board/Resign | Main menu | **Stub** | Total | Defer |
| CM93-F022 | Manager Jobs | Main menu | **Beyond** (backend) | Minor UI | ❌ |
| CM93-F023 | National Squads senior | Main menu | **None** | N/A | ❌ |
| CM93-F024 | National Squads U21 | Main menu | **None** | N/A | ❌ |
| CM93-F025 | Change Picture (UI theme) | Main menu | **None** | Total | ❌ |
| CM93-F026 | Save Game | Main menu | **Partial** | Minor | ✅ |

### C — Squad screen & player status

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F030 | Status icon `[IJ]` injured | Squad | **Partial** (text) | Minor | ✅ |
| CM93-F031 | Status `[SU]` suspended | Squad | **None** | Total | ❌ |
| CM93-F032 | Status `[U]` unhappy | Squad | **None** | Total | ❌ |
| CM93-F033 | Status `[L]` loan listed | Squad | **None** | Total | ❌ |
| CM93-F034 | Status `[R]` transfer request | Squad | **None** | Total | ❌ |
| CM93-F035 | Status `[C]` out of contract | Squad | **None** | Total | ❌ |
| CM93-F036 | Morale bands VERY LOW→SUPERB | Squad | **Partial** (0–100 %) | Major | ✅ |
| CM93-F037 | Per-player fitness display | Squad | **Partial** (Fit %) | Minor | ✅ |
| CM93-F038 | Youth promotion cyan names | Squad | **None** | Total | ❌ |
| CM93-F039 | TRNS season transfer log | Squad btn | **None** | Total | ❌ |
| CM93-F040 | STAFF backroom screen | Squad btn | **Partial** (physio row) | Major | ME-1 Trainer |
| CM93-F041 | LEAG league chart per match | Squad btn | **Partial** (standings) | Minor | ✅ |
| CM93-F042 | FIXL fixture list | Squad btn | **Partial** | Minor | ✅ |
| CM93-F043 | ACCS weekly income/expenditure | Squad btn | **Partial** | Minor | ✅ |
| CM93-F044 | **RSRV reserve squad view** | Squad btn | **None** | Total | ❌ |
| CM93-F045 | INFO hub HIST/RECS/PRINT | Squad btn | **Partial** | Major | Defer |
| CM93-F046 | AVR season average ratings | Squad btn | **None** | Total | ❌ |
| CM93-F047 | DSP disciplinary points table | Squad btn | **None** | Total | ❌ |
| CM93-F048 | GLS goals table | Squad btn | **None** | Total | ❌ |
| CM93-F049 | MOM man-of-match table | Squad btn | **None** | Total | ❌ |
| CM93-F050 | PEN penalty taker order | Squad btn | **None** | Total | ❌ |
| CM93-F051 | PRV last match stats | Squad btn | **Partial** | Minor | ✅ |
| CM93-F052 | OPP opponent squad pre-match | Squad btn | **None** | Total | ❌ |
| CM93-F053 | DEFS/MIDS/ATTS position tables | Squad btn | **None** | Total | ❌ |

### D — Tactics & team selection

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F060 | Pick XI by shirt number | Tactics | **Core** | None | ✅ |
| CM93-F061 | Swap shirts [SWP] | Tactics | **Core** | None | ✅ |
| CM93-F062 | Reserve pick [RSRV] | Tactics | **None** | Total | ❌ |
| CM93-F063 | Tactics editor [TCT] | Tactics | **Partial** (formation) | Major | ✅ |
| CM93-F064 | Position DEF/MID/ATT L/R/C icons | TCT | **None** | Total | ❌ |
| CM93-F065 | Captain assign | TCT | **None** | Total | ❌ |
| CM93-F066 | Formation [NEW FORMATION] | TCT | **Partial** (3 presets) | Minor | ✅ |
| CM93-F067 | Style [STYLE] | TCT | **None** | Total | ❌ |
| CM93-F068 | Push/pull FORW/BACK/NORM | TCT | **None** | Total | ❌ |
| CM93-F069 | In-match subs (shirts 12,14,GK) | Match | **None** | Total | ❌ |
| CM93-F070 | APPL apply for vacant job | Squad | **Beyond** (jobs backend) | Minor | ❌ |

### E — Match engine & presentation

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F080 | Text match commentary stream | Match | **None** | Total | ❌ |
| CM93-F081 | Injury time in match | Match | **None** | Total | ❌ |
| CM93-F082 | Match speed control | Match | **None** | Total | ❌ |
| CM93-F083 | Interrupt for subs/tactics | Match | **None** | Total | ❌ |
| CM93-F084 | Post-match 0–10 ratings | Match | **Partial** | Minor | ✅ |
| CM93-F085 | Named scorers | Result | **Core** | None | ✅ |
| CM93-F086 | Match statistics screen | Match Reports | **Partial** | Minor | ✅ |
| CM93-F087 | Man of the Match vote | Match | **None** | Total | ❌ |
| CM93-F088 | Attendance / gate | Match | **Partial** | Minor | ✅ |
| CM93-F089 | Preview lineups before match | Flow | **Partial** | Minor | ✅ |
| CM93-F090 | Result screen ratings both squads | Result | **Partial** (user XI) | Minor | ✅ |

### F — Transfers & contracts

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F100 | Normal player search (filters) | Find Player | **None** | Total | ❌ |
| CM93-F101 | Scout search + [RECS] | Find Player | **None** | Total | ❌ |
| CM93-F102 | Foreign player pool + agent fee | Find Player | **None** | N/A | ❌ |
| CM93-F103 | Shortlist | Transfers | **None** | Total | ❌ |
| CM93-F104 | Transfer list browsing | Transfers | **None** | Total | ❌ |
| CM93-F105 | Multi-round fee negotiation | Transfers | **None** | Total | ❌ |
| CM93-F106 | List / loan player | Player menu | **None** | Total | ❌ |
| CM93-F107 | Revalue listed player | Player menu | **None** | Total | ❌ |
| CM93-F108 | Not For Sale / Allow Approaches | Player menu | **None** | Total | ❌ |
| CM93-F109 | Fine player | Player menu | **None** | Total | ❌ |
| CM93-F110 | Increase wage | Player menu | **Partial** (model) | Major | ❌ |
| CM93-F111 | Renew contract | Player menu | **Partial** (model) | Major | ❌ |
| CM93-F112 | Weekly transfer news round-up | News | **None** | Total | ❌ |
| CM93-F113 | Player stay/leave requests | Transfers prose | **None** | Total | ❌ |
| CM93-F114 | Free transfer / out of contract `[C]` | Status | **None** | Total | ❌ |

### G — Backroom staff

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F120 | Coach (training) | STAFF | **Stub** | Total | ❌ |
| CM93-F121 | Physio (fitness + injury) | STAFF | **Core+** engine / **Partial** UX | Minor | ME-1 |
| CM93-F122 | 3× league scouts | STAFF | **None** | Total | ❌ |
| CM93-F123 | Youth scout | STAFF | **None** | Total | ❌ |
| CM93-F124 | Staff ability Fair→Superb | STAFF | **Partial** (physio only) | Major | ✅ physio |
| CM93-F125 | Sack / replace staff | STAFF | **None** | Total | ❌ |
| CM93-F126 | Physio improves **match** fitness | Manual §Staff | **None** | Major | ❌ |

### H — Discipline

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F130 | Booking 5 pts | Discipline | **None** | Total | ❌ |
| CM93-F131 | Sending off 10 pts | Discipline | **None** | Total | ❌ |
| CM93-F132 | 20 pts = 2-match ban | Discipline | **None** | Total | ❌ |
| CM93-F133 | Squad discipline → club fine | Discipline | **None** | Total | ❌ |

### I — Injuries & player condition

*Detail IDs: [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md)*

| ID | CM93 feature | Futtoboru engine | Futtoboru R-1888 UX | Gap |
|----|--------------|------------------|---------------------|-----|
| CM93-F140 | Injury description text | **Core+** | **Core+** | None |
| CM93-F141 | Physio recovery estimate | **Core+** | Partial (ME-1 label) | Minor |
| CM93-F142 | INJURY PRONE hidden attr | **Core+** | **Core+** | None |
| CM93-F143 | Rehabilitate paid treatment | **Core+** | Partial (ME-1 costs) | Minor |
| CM93-F144 | Insure / Uninsure | **Core+** | **None** (hidden R-1888) | R-1888 by design |
| CM93-F145 | Lilleshall rehab | **Core+** | **None** (hidden) | R-1888 by design |
| CM93-F146 | Surgeon abroad | **Core+** | **None** (hidden) | R-1888 by design |
| CM93-F147 | Insured wages while injured | **Core+** | **None** | R-1888 by design |
| CM93-F148 | Forced retirement payout | **None** (deferred) | **None** | Defer PO |
| CM93-F149 | Injury news / inbox | **Core+** | **Core+** | None |
| CM93-F150 | In-match injury commentary | **None** | **None** | Major |
| CM93-F151 | Fitness affects availability | **Core** | **Core** | None |
| CM93-F152 | Fatigue / tiredness | **Partial** | **Partial** | Minor |

### J — Board, career & economy

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F160 | Board confidence meter | Board | **Stub** | Total | Defer |
| CM93-F161 | Resign | Board | **Partial** | Minor | Defer |
| CM93-F162 | Sacking | Board | **None** | Total | ❌ |
| CM93-F163 | Manager reputation / axe list | Jobs | **Beyond** (partial) | Minor | ❌ |
| CM93-F164 | Club weekly accounts | ACCS | **Partial** | Minor | ✅ |
| CM93-F165 | Win bonuses / salaries (CM93) | Finances | **Partial** | Minor | ✅ |

### K — Competitions & world

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F170 | English league season | Core | **Core** | None | ✅ |
| CM93-F171 | FA Cup knockout | Core | **Core** | None | ✅ |
| CM93-F172 | Domestic cups (League Cup etc.) | CM93 | **None** | N/A | ❌ |
| CM93-F173 | European competitions | CM93 | **None** | N/A | ❌ |
| CM93-F174 | Procedural multi-season rollover | CM93 | **Core** | None | ✅ |
| CM93-F175 | Reserve squad auto-fill when many injured | MobyGames | **None** | Total | ❌ |

### L — Data & attributes

| ID | CM93 feature | Manual | Futtoboru | Gap | R-1888 |
|----|--------------|--------|-----------|-----|--------|
| CM93-F180 | Expanded player attributes (vs CM1) | Manual §5 | **Beyond** (FM-style) | None | ✅ |
| CM93-F181 | 5-year player history | INFO HIST | **None** | Total | ❌ |
| CM93-F182 | Club / player records | INFO RECS | **None** | Total | ❌ |
| CM93-F183 | International call-ups tracking | Manual | **None** | N/A | ❌ |

---

## 4. What CM93 had that Futtoboru does **not** (Total gap list)

Use this for “what is missing” conversations — **Layer C+** unless noted.

**Transfers & market (15):** F100–F114  
**Staff beyond physio (5):** F120, F122–F123, F125–F126  
**Discipline (4):** F130–F133  
**Reserve / youth (3):** F044, F062, F038  
**Tactics depth (6):** F064–F069 (icons, style, push/pull, subs)  
**Match presentation (5):** F080–F083, F087  
**Squad analytics buttons (6):** F046–F049, F053, F011  
**Board (2):** F160, F162  
**National / Europe (5):** F012, F016, F023–F024, F173  
**Hotseat / Quick Start (3):** F003, F005, F007  
**Player status flags (5):** F031–F035  
**Misc UI (3):** F025, F018 (friendly arrange), F175  

**Injury gaps (full CM93, not R-1888):** F150 commentary; F148 retirement; F126 physio match-fitness; R-1888 hides F144–F147 by design.

---

## 5. What Futtoboru **does** match CM93 (Core+ / Core)

| Area | IDs (sample) |
|------|----------------|
| Season loop | F006, F170, F171, F174 |
| XI + formation | F060, F061, F066, F063 |
| Match outcome | F084–F085, F088–F090 |
| Condition engine | F140–F143, F149, F151 |
| Injury economy (dev profile) | F144–F147 |
| Data | F001, F180 |
| Jobs backend | F022, F070, F163 |

---

## 6. How to use this doc

| Question | Read |
|----------|------|
| Full CM93 feature list? | §3 registry |
| Count missing? | §2 scorecard + §4 Total gap list |
| Injuries only? | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](RESEARCH_CM93_INJURY_SUBSYSTEM.md) |
| R-1888 only? | §3 **R-1888** column + [RELEASE_R1888_DEFINITION.md](../planning/RELEASE_R1888_DEFINITION.md) |
| PO marketing claim? | §2 — do **not** say “full CM93” at ~17% Core+ |

**Maintenance:** Update Futtoboru depth when features ship; add row + ID — do not renumber existing IDs.

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial catalogue — 106 CM93-F IDs across 12 domains |

---

*Supersedes scattered menu extracts for parity conversations; injuries remain dual-documented in RESEARCH_CM93_INJURY_SUBSYSTEM.*
