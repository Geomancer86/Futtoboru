# PO Decision — R-1888 Release Scope & Feature Roadmap

> **Superseded for PO routine reading → [PO_BRIEF.md](PO_BRIEF.md).** Machine roadmap: [docs/registries/release-roadmap.yaml](../registries/release-roadmap.yaml).

**Document type:** PO decision / lock  
**Date:** 2026-07-03  
**Status:** **Active** — pending PO initials on §6  
**Audience:** PO (sign) · PM · all leads  

**Inputs:** [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) · [RESEARCH_CM93_FEATURE_CATALOGUE_V1.md](../research/RESEARCH_CM93_FEATURE_CATALOGUE_V1.md) · [PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md](PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md)  

---

## PO reply (decision record)

> **We ship Futtoboru 1888 as a Victorian Football League management sim — CM1 loop plus ME-1 injury care — not as a CM93 clone.**
>
> **Tier A blockers must close before any human tester runs B-9.** Nothing from Tier B enters R-1888 scope without a new PO decision and release rename.
>
> **Transfers, scouts, discipline, reserves, and CM93 insurance stay out of R-1888.** Transfers begin design at R-1892 Layer C; injury insurance ships no earlier than **R-1906**; full CM93 injury menus no earlier than **R-1993**.
>
> **Milestone releases** (1892, 1906, 1951, 1965, 1993) take precedence over shipping every calendar year. Research may document everything; implementation follows release year.
>
> **Marketing:** “Futtoboru 1888” / “Victorian injury care” — never “full CM93” for this tranche.

— PO direction lock, 2026-07-03 (documented by PM from product conversation)

---

## 1. Decisions locked

| # | Decision | Answer |
|---|----------|--------|
| D-1 | **R-1888 product claim** | Victorian 1888–89 FL + CM1 management loop + ME-1 injuries |
| D-2 | **CM93 full parity in R-1888?** | **No** — ~17% Core+ vs full CM93 catalogue is expected |
| D-3 | **Hard medical ceiling R-1888** | **ME-1 only** — no calendar unlock of 1906+ in same binary |
| D-4 | **Transfers in R-1888?** | **No** — first design **R-1892** Layer C; CM1-simple market |
| D-5 | **Insurance / Lilleshall in R-1888?** | **No** — **R-1906** / **R-1951** / **R-1993** per medical eras |
| D-6 | **Human QA gate** | **After** Tier A (8 blockers) + L1–L3 automated green |
| D-7 | **Release cadence** | Milestone **R-YYYY** tranches; skip thin years OK |
| D-8 | **Assistant manager / stadium expansion** | **Out of CM93 scope** — defer to CM4/CM99 tranche (~R-2003+) |
| D-9 | **Nov 1888 points rule** | **Defer** to R-1889 script or message-only — not R-1888 blocker |
| D-10 | **Re-election bottom 4 (1888)** | **Defer** — message-only acceptable for R-1888 |

---

## 2. Tier A — approved R-1888 work (before B-9)

PM is authorized to schedule without further PO review:

1. M-B5.3-HIST **ME-1 only** (hide F144–F147; Trainer; shilling costs)  
2. ME-1 automated tests  
3. C-1 Continue double-click fix  
4. PL-5 / PL-7 UI fixes (or documented B-9 waiver — **PO prefers fix**)  
5. Soak preflight + PM sign-off  
6. Manual ch.6–7 R-1888  
7. Tag CM93 economy tests `@Tag("cm93")`  

---

## 3. Tier B — approved deferral schedule

| Release | Approved scope (minimum) |
|---------|--------------------------|
| **R-1888.1** | Polish: F030 IJ styling, result injury line, friendly UX |
| **R-1889** | Optional Nov 1888 points script; jobs UI polish |
| **R-1892** | Div 2, reserves (F044), transfers **Core** (F100–F111 subset) |
| **R-1906** | ME-2 injury insurance UI |
| **R-1919** | Discipline F130–F133 + coach F120 (bundle) |
| **R-1951** | ME-4 Lilleshall |
| **R-1965** | In-match substitutes F069 |
| **R-1993** | ME-6 CM93 player-facing injury economy |

PO may reprioritize **between** milestones; PO may **not** pull Tier B into R-1888 without amending this doc.

---

## 4. Explicit non-goals (R-1888)

- Full CM93 feature parity (106/106)  
- Transfers, scouts, youth scout, transfer news  
- CM93 insurance, Lilleshall, surgeon abroad in player UI  
- Discipline points, unhappy/transfer-request flags  
- Reserve squad management, national squads, European cups  
- Hotseat, Quick Start, printer  
- Public distribution (unchanged — local dev until ready)  

---

## 5. Claims matrix (PO-approved language)

| OK to say | Do not say |
|-----------|------------|
| Futtoboru 1888 — 1888–89 Football League | Full Championship Manager 93 |
| CM1-style management loop (automated proof) | Complete CM93 feature set |
| Victorian Trainer injury care (after ME-1 ships) | CM93 insurance and Lilleshall |
| 106 CM93 features catalogued; ~35 in R-1888 scope | We match CM93 |
| Roadmap: 1892 transfers, 1906 insurance, 1993 CM93 menus | Everything documented is in the game |

---

## 6. Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| PO | _________________ | ______ | _________ |
| PM | _________________ | ______ | _________ |

**On sign:** PM executes [PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md](PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md) §7.

---

## 7. Document chain

```
RESEARCH_CM93_FEATURE_CATALOGUE_V1.md
ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md
PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md
PM_EAGLE_VIEW_REPORT.md
PO_DECISION_R1888_RELEASE_ROADMAP.md (this doc)
DESIGN_YEARLY_RELEASE_SCOPE.md §4.3
```

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial PO lock — R-1888 scope, Tier A/B, release roadmap |

---

*Amend via new PO decision doc revision — do not silently expand R-1888 in engineering.*
