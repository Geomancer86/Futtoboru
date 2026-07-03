# PM Report — R-1888 Gap Analysis & Release Roadmap

**Document type:** PM status / planning  
**Date:** 2026-07-03  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, all department leads, Sponsor  

**Analysis:** [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md)  
**Feature registry:** [RESEARCH_CM93_FEATURE_CATALOGUE_V1.md](../research/RESEARCH_CM93_FEATURE_CATALOGUE_V1.md) (106 CM93-F IDs)  
**PO decision:** [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md)  

---

## 1. Executive summary

We can now state **precisely** what is missing from **Futtoboru 1888**:

| Category | Items | Action |
|----------|-------|--------|
| **Blockers (Tier A)** | 8 | Close in **1–2 sprints** → human QA |
| **Intentionally later (Tier B)** | ~55 CM93 features | Scheduled across **R-1892 → R-1993** |
| **Full CM93 parity (Tier C)** | 66 at None/Stub | **Not** R-1888 goal |

**PM recommendation:** Sign [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md), execute **Sprint A–B**, then B-9. Do **not** expand R-1888 scope with transfers or CM93 insurance.

---

## 2. What R-1888 is missing (Tier A — blockers)

| # | Deliverable | CM93-F | Sprint | Exit |
|---|-------------|--------|--------|------|
| 1 | M-B5.3-HIST ME-1 only | F040, F141–F143; hide F144–F147 | A | No CM93 menus in 1888 |
| 2 | ME-1 tests (3+) | — | A | PR suite green |
| 3 | C-1 Continue guard | — | A | M8 ready |
| 4 | PL-5 history / PL-7 save UI | F026 | B | M5/M1 reload |
| 5 | Soak preflight sign-off | F174 | B | PM signed |
| 6 | Manual R-1888 | — | B | ch.6–7 accurate |
| 7 | `@Tag("cm93")` on B5.2 economy tests | F144–F147 dev | A | Default = R-1888 |
| 8 | B-9 checklist prep | — | B | Invite testers |

**Estimate:** Sprint A = 1 week · Sprint B = 1 week · Sprint C = human QA.

---

## 3. What is missing but **correctly** not in R-1888 (Tier B)

Top domains by CM93 catalogue §4:

| Domain | CM93-F | First release | PM note |
|--------|--------|---------------|---------|
| **Transfers & market** | F100–F114 (15) | **R-1892+** design · ship with Layer C | Do not slip into R-1888 |
| **Tactics depth** | F064–F069 (6) | R-1892 partial · R-1965 subs | Subs = 1965 rule |
| **Match presentation** | F080–F083, F087 (5) | R-1888.1 polish | Commentary optional |
| **Discipline** | F130–F133 (4) | R-1919 bundle | Needs card sim |
| **Staff (coach/scouts)** | F120–F126 (5) | R-1892 coach · R-1999 scouts | CM93 scouts ≠ 1888 |
| **Reserves** | F044, F062, F175 (3) | **R-1892** | With Div 2 |
| **Board** | F160, F162 (2) | R-1889 message · R-1892 Core | Defer OK |
| **Player flags U/R/SU/C/L** | F031–F035 (5) | R-1892+ with transfers | |
| **CM93 injury economy UI** | F144–F147 | **R-1906 / R-1951 / R-1993** | PO locked |

Full schedule: [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) §3.

---

## 4. Release roadmap (PM view)

| Release | Theme | Key ship | Depends on |
|---------|-------|----------|------------|
| **R-1888** | Victorian FL management | CM1 loop + ME-1 | Tier A blockers |
| **R-1888.1** | Polish | IJ icon, result injury line | B-9 feedback |
| **R-1889** | Rules script | Nov 1888 points (optional) | PO |
| **R-1892** | Expansion | Div 2, reserves, transfers **Core** | World evolution design |
| **R-1906** | Legal/medical | ME-2 insurance UI | ME-1 + economy |
| **R-1919** | Interwar | Discipline + coach | Match events |
| **R-1951** | ME-4 | Lilleshall | Medical era |
| **R-1965** | Subs | F069 | Match engine |
| **R-1993** | CM93 skin | ME-6 full injury menus | All prior medical eras |

**Cadence:** Milestone releases over literal yearly — PO may skip thin years ([DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)).

---

## 5. Traffic lights

| Area | Status |
|------|--------|
| CM93 feature catalogue | 🟢 106 IDs |
| Gap analysis + roadmap | 🟢 This commit |
| R-1888 Tier A blockers | 🔴 8 open |
| Transfers in R-1888 scope | 🟢 Correctly **no** |
| PO decision doc | 🟡 Awaiting sign-off |
| Human QA | ⚪ Blocked (correct) |

---

## 6. Stakeholder messaging

| Say | Do not say |
|-----|------------|
| “Futtoboru 1888 — ~35 CM93-scale features at Partial+ for Victorian scope” | “Full CM93 game” |
| “Transfers ship Layer C / R-1892+” | “Transfers coming in 1888 patch” |
| “Insurance documented for 1906; not in 1888 binary” | “We have CM93 insurance” |
| “8 blockers before human QA” | “Ready for players” |

---

## 7. Next PM actions

1. PO signs [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md)  
2. Schedule Sprint A (ME-1 + C-1 + tests)  
3. Run soak preflight in Sprint B  
4. Invite human testers only after §2 checklist green  
5. Log B-9 issues → R-1888.1 vs R-1892 backlog using CM93-F IDs  

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Gap + roadmap PM report tied to CM93 catalogue |

---

*Eagle view: [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md).*
