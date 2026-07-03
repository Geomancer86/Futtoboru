# Analysis — R-1888 Release Gap & Feature Roadmap

**Document type:** Release gap analysis / scheduling  
**Date:** 2026-07-03  
**Status:** **Active**  
**Release:** **R-1888** (`Futtoboru 1888`)  

**Sources:** [RESEARCH_CM93_FEATURE_CATALOGUE_V1.md](../research/RESEARCH_CM93_FEATURE_CATALOGUE_V1.md) · [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) · [DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)  

---

## 1. Executive summary

“Missing from our release” means **three different things**. Conflating them causes bad PO decisions.

| Tier | Meaning | Count (approx.) | When |
|------|---------|-----------------|------|
| **A — R-1888 blockers** | In scope for Futtoboru 1888 but **not shipped yet** | **~8 work items** | **Before human QA** |
| **B — R-1888 intentional out** | Documented in CM93 catalogue but **not in R-1888 scope** by PO design | **~55 CM93-F rows** | **Later R-YYYY** |
| **C — Full CM93 parity gaps** | Would be needed to claim “full CM93” (1993 gameplay on 1888 start) | **~66 CM93-F at None/Stub** | **R-1993 ME-6** + Layer C |

**R-1888 ships ~35 of 106 CM93 catalogue items** at Partial+ (catalogue R-1888 ✅ column). That is **correct** for a **Victorian first release**, not a failure.

---

## 2. Tier A — Missing from R-1888 (must close before human QA)

These are **in** [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md) and **block** B-9.

| ID | Gap | CM93-F | Owner | Release gate |
|----|-----|--------|-------|--------------|
| A-1 | **ME-1 injury UX** — hide CM93 insurance/Lilleshall/surgeon; Trainer labels; shilling costs | F040, F141, F143, F144–F147 hidden | Match/Sim | R-1888 |
| A-2 | ME-1 automated tests | — | Match/Sim | R-1888 |
| A-3 | Continue double-click (C-1) | — | Platform | R-1888 |
| A-4 | Match history click (PL-5) | — | Platform | R-1888 |
| A-5 | Save/load UI paths (PL-7) | F026 polish | Platform | R-1888 |
| A-6 | 60-season soak PM sign-off | F174 confidence | QA/PM | R-1888 |
| A-7 | Manual ch.6–7 R-1888 slice | — | Manual | R-1888 |
| A-8 | Production load happy-path verification | F006 | QA | R-1888 |

**Not Tier A (polish — post B-9 or R-1888.1 patch):** F030 `[IJ]` icon styling, F028 result injury line, F064–F068 tactics depth, F080 commentary.

---

## 3. Tier B — Intentionally not in R-1888 (when to add)

Grouped by **historical / product sense**, not CM93 menu order.

### 3.1 Release roadmap (recommended)

| Release | Primary driver | Ship | CM93-F examples | Rationale |
|---------|----------------|------|-----------------|-----------|
| **R-1888** | First Victorian playable season | **Now** | F006, F060–F061, F170–F171, F174, F140–F143 (ME-1) | CM1 loop + 1888 mod |
| **R-1888.1** | Post–human-QA polish | After B-9 | F030, F028, F018 polish | Low risk; no new systems |
| **R-1889** | In-season rule change | Optional milestone | Script: Nov 1888 2 pts for win | Historical rule event — not CM93 |
| **R-1892** | **World expansion** | Layer C | Div 2, F044 reserves, F175 | PO locked 1892 tranche; reserves CM93-feel |
| **R-1892+** | **Transfer market Core** | Layer C | F100–F114 (CM1 floor) | Victorian fees; no CM93 intl pool yet |
| **R-1906** | **Medical + legal era** | ME-2 | F144–F147 insurance UI, compensation framing | Workmen’s Comp Act — not before this release |
| **R-1919** | Interwar bundle | Optional | F130–F133 discipline, F120 coach, F032–F035 flags | CM93 had these; historical dressing |
| **R-1951** | National rehab era | ME-4 | F145 Lilleshall | NSC 1951 |
| **R-1965** | Football League subs | Match engine | F069 in-match subs | Rule change 1965–66 |
| **R-1993** | CM93 player-facing injury economy | ME-6 | Full B5.2 menus at CM93 costs | Only here claim “CM93 injury parity” |
| **R-1999+** | CM3 depth | Long | F120–F123 scouts, training, F064 TCT | FM/CM3 territory |

**PO rule:** Do **not** implement a row because CM93 had it — implement when **release year + historical plausibility + dependency** align ([DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)).

### 3.2 Domain scheduling table

| Domain | Missing from R-1888? | First sensible release | Why then |
|--------|----------------------|------------------------|----------|
| **Transfers / market** | Yes — all F100–F114 | **R-1892+** (design) · **ship R-1892 or R-1906** | Needs economy + squad churn; 1888 static cohort OK for first release |
| **Free agents / contracts expiry** | Yes — F035, F114 | **R-1892+** with transfers | Depends on transfer engine |
| **Coach / scouts** | Yes — F120, F122–F123 | **R-1892** (coach stub) · **R-1999** (scouts CM93) | CM93 scouts are 1993 product; coach simpler |
| **Assistant manager** | N/A in CM93 | **Never for CM93 parity** · **CM4 tranche** | CM4 feature — see [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) |
| **Stadium expansion** | N/A in CM93 | **R-1999+** (CM99/00) | Not CM93; historical stadium in mod is static 1888 |
| **Discipline / bookings** | Yes — F130–F133 | **R-1919** or **R-1892** if cards added to sim | Needs match event model |
| **Reserve squad** | Yes — F044, F062, F175 | **R-1892** | Div 2 + squad depth |
| **Board / sacking** | Yes — F160, F162 | **R-1889** message-only · **R-1892** Core | R-1888 defer OK |
| **Unemployed / jobs UI** | Partial — F022 backend | **R-1889** or post-R-1888 polish | Backend exists |
| **National / European** | Yes — F012, F023–F024, F173 | **Out of scope** until multi-country mod | N/A 1888 England focus |
| **Hotseat / Quick Start** | Yes — F003, F005 | **P3** — no release year | Not historical sim priority |
| **CM93 insurance UI** | Hidden by design | **R-1906** (ME-2) | PO locked |
| **Lilleshall / surgeon** | Hidden by design | **R-1951** / **R-1993** | Medical eras |
| **Match commentary** | Yes — F080–F083 | **R-1888.1** flavour · **R-1993** depth | Polish vs product |
| **Tactics push/pull / subs** | Yes — F064–F069 | **R-1965** (subs) · tactics **R-1892** partial | Subs = rule change |

---

## 4. Tier C — Full CM93 parity (not a release goal for R-1888)

From catalogue §2: **66/106** items at None/Stub/Minimal vs full CM93.

| Claim | Valid when |
|-------|------------|
| “Futtoboru 1888 — Victorian Football League management” | **R-1888** after Tier A |
| “CM1-minimum loop” | **R-1888** today (automated) |
| “CM93-style injuries” | **R-1993** ME-6 profile only |
| “Full CM93 feature parity” | **Not scheduled** as single release — multi-decade roadmap |

---

## 5. R-1888 in / out checklist (PO audit)

### In scope ✅ (ship or shipped)

- Procedural league + FA Cup + rollover (F170, F171, F174)
- Pick XI, 3 formations, preview, kick off, result, ratings (F060–F061, F063, F066, F084–F090)
- Fitness, morale, injury roll, squad columns (F037, F036, F140–F143, F151)
- **ME-1** Trainer injury care (Tier A-1)
- 1888 wages, gate receipts, weekly accounts (F043, F088, F164)
- 1888 named player database (F001, F180 Beyond)
- Employed start (not CM93 unemployed menu)

### Out of scope ❌ (document only until later R-YYYY)

- Entire transfer subsystem (F100–F114)
- CM93 insurance / Lilleshall / surgeon in UI (F144–F147) until R-1906 / R-1951 / R-1993
- Scouts, coach hiring, discipline, reserves, national squads
- European cups, hotseat, printer, Quick Start
- Full CM93 tactics (style, push/pull, in-match subs)

---

## 6. Dependency graph (simplified)

```
R-1888 (CM1 loop + ME-1)
    → B-9 human QA
    → R-1888.1 polish
    → R-1892 (Div 2 + reserves + transfers Core design)
         → R-1906 (ME-2 insurance + transfer economy depth)
              → R-1919 (discipline bundle)
                   → R-1951 (ME-4 Lilleshall)
                        → R-1965 (subs)
                             → R-1993 (ME-6 CM93 menus)
```

Transfers **can** start at R-1892 with CM1-simple buy/sell; **insurance** must not precede R-1906.

---

## 7. Document chain

```
RESEARCH_CM93_FEATURE_CATALOGUE_V1.md
    → ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md (this doc)
    → PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md
    → PM_EAGLE_VIEW_REPORT.md
    → PO_DECISION_R1888_RELEASE_ROADMAP.md
    → DESIGN_YEARLY_RELEASE_SCOPE.md §4.3
```

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial gap tiers + release scheduling matrix |

---

*Update catalogue Futtoboru depth when a release tranche ships; add release column notes here.*
