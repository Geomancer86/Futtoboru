# Design — Yearly Release Scope & Naming

**Document type:** Product / release architecture  
**Date:** 2026-07-03  
**Status:** **Active** — PO-aligned with [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md)  
**Audience:** PO, PM, all department leads, manual authors  

**Companion:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) · [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) · [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md)

---

## 1. Core principle — document all, ship by release year

| Layer | Scope | When it grows |
|-------|--------|---------------|
| **Research & design ledger** | Full historical timeline (rules, medicine, economics, forks) | Anytime — no player-facing ship required |
| **Engine & UI implementation** | Only what the **current named release** supports | When that **release year** is in scope |
| **Start-mod content** | One primary entry season per release (minimum) | Same release milestone |

**PO rule (locked 2026-07-03):** We **do not implement** 1906 injury insurance (or any ME-2+ medical economy) while the active release is **1888** — even though research documents the 1906 Workmen’s Compensation Act. The ledger is **reference**; the **release year** is the **implementation ceiling**.

---

## 2. Three meanings of “year” (do not conflate)

| Term | Meaning | Example |
|------|---------|---------|
| **Release year** | Product / git tag name — primary start season bundled | `Futtoboru 1888` |
| **Start season** | Pickable new-game entry (`mods/seasons/{id}/`) | 1888–89 (id `18`) |
| **Simulated calendar year** | In-save date advancing day-by-day | 1888 → 1889 → … procedural |

**Procedural forward play** from 1888 does **not** require a new mod folder per simulated year ([DESIGN_HISTORICAL_DATABASE §2](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)).  
**New release years** add **pickable starts** and/or **new implemented systems** — not one mod per simulated season.

---

## 3. Release naming scheme

### 3.1 Canonical names

| Field | Convention | Example |
|-------|------------|---------|
| **Product name** | `Futtoboru {YYYY}` | `Futtoboru 1888` |
| **Git tag** | `{YYYY}.0.0` (semver patch for fixes) | `1888.0.0`, `1888.0.1` |
| **Internal release id** | `release_{YYYY}` | `release_1888` |
| **Start mod anchor** | Season id in `mods/seasons.txt` + human label | id `18` — “1888–89” |
| **Docs / PM shorthand** | **R-1888**, **R-1889**, … | R-1888 = first release tranche |

**Why season year, not “v1.0”:** Futtoboru is a **historical simulation**; the release name tells players and contributors **which era’s rules, data, and systems** are implemented. Legacy labels (`v1.0`, `V1.5 1892`) remain in older PM docs as **layer milestones** — map them to **R-1888** / **R-1892** in §3.3.

### 3.2 Cadence

| Cadence | Direction |
|---------|-----------|
| **Target** | **One named release per historical entry year** (yearly product tranche) |
| **Reality** | PO may skip thin years; milestone seasons (1892, 1906, 1919, 1939, 1993) may bundle multiple calendar gaps |
| **PO lock** | No public ship pressure — naming applies to **internal milestones** and future distribution |

### 3.3 Legacy term mapping

| Old doc label | Maps to | Notes |
|---------------|---------|-------|
| Layer A / V1.0 complete | **R-1888** engine loop | Season + cup + procedural rollover |
| Layer B / V1 MVP | **R-1888** + management depth | XI, tactics, sim, B5 injuries (ME-1 skin) |
| Layer C / V1.5 1892 | **R-1892** expansion | Div 2, world evolution — separate release tranche |
| CM93 injury parity (B5.2) | Engine reference | **Not** R-1888 player-facing claim until ME-1 skin ships |

---

## 4. Release scope matrix (systems vs release year)

**Legend:** 📚 documented in research · 🔧 implemented in release · — not in scope

### 4.1 Injury / medical (M-B5.3-HIST)

Full era catalog: [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md).

| Medical era | Calendar | Features | R-1888 | R-1906 (future) | R-1951+ (future) |
|-------------|----------|----------|--------|-----------------|------------------|
| **ME-1** | 1885–1905 | Trainer, cheap rehab, no insurance/Lilleshall | 📚 🔧 **ship** | 📚 🔧 | 📚 🔧 |
| **ME-2** | 1906–1914 | Compensation framing, insurance UI (PO) | 📚 only | 📚 🔧 **ship** | 📚 🔧 |
| **ME-4** | 1945–1970 | NSC / national rehab | 📚 only | 📚 only | 📚 🔧 **ship** |
| **ME-6** | 1993–1999 | Full CM93 menu (B5.2 reference) | 📚 only | 📚 only | 📚 only → **R-1993** |

**R-1888 injury deliverable:** M-B5.3-HIST **HIST-a + HIST-b for ME-1 only** — hide CM93 anachronisms; ME-1 costs and Trainer labels; **no** `Insure`, Lilleshall, or surgeon code paths in UI or economy.

### 4.2 Other domains (full roadmap — CM93-F cross-ref)

| Domain | CM93-F | R-1888 | Documented ahead | Implement when |
|--------|--------|--------|------------------|----------------|
| **Transfers & market** | F100–F114 | — | 📚 CM1 floor | **R-1892+** design · ship Layer C |
| **Transfer flags (U/R/SU/C/L)** | F031–F035 | — | 📚 | **R-1892+** with transfers |
| **Reserve squad** | F044, F062, F175 | — | 📚 | **R-1892** with Div 2 |
| **Coach / scouts** | F120–F126 | — | 📚 | Coach **R-1892** stub · scouts **R-1999** |
| **Discipline / bookings** | F130–F133 | — | 📚 | **R-1919** bundle |
| **Board / sacking** | F160, F162 | message | 📚 | **R-1889** message · **R-1892** Core |
| **Tactics depth (style, push/pull)** | F064–F068 | partial | 📚 | **R-1892** partial |
| **In-match substitutes** | F069 | — | 📚 1965 rule | **R-1965** |
| **Match commentary** | F080–F083 | — | 📚 | **R-1888.1** polish · depth **R-1993** |
| **League rules (2-1-0, goal avg)** | — | 🔧 | 📚 Nov 1888 points | **R-1889** script (optional) |
| **Division Two** | — | — | 📚 1892 | **R-1892** |
| **Player insurance (injury)** | F144–F147 | hidden | 📚 1906 Act | **R-1906** ME-2 |
| **Lilleshall / surgeon abroad** | F145–F147 | hidden | 📚 | **R-1951** ME-4 · **R-1993** ME-6 |
| **Unemployed career** | F022 | backend | 📚 CM2+ | **R-1889** polish |
| **National / European** | F012, F023–F024, F173 | — | 📚 | Multi-country mod (TBD) |
| **Hotseat / Quick Start** | F003, F005 | — | 📚 | **P3** — no release year |
| **Assistant manager** | — (CM4) | — | 📚 CM4 | **R-2003+** tranche |
| **Stadium expansion** | — (CM99) | — | 📚 | **R-1999+** tranche |

**Gap analysis:** [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) · **PO lock:** [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md).

### 4.3 Milestone release sequence (PO-approved)

| Release | Theme | Minimum ship |
|---------|-------|--------------|
| **R-1888** | Victorian FL + CM1 loop | ME-1 injury, Tier A blockers, B-9 |
| **R-1888.1** | Post–human-QA polish | F030, F028, UX fixes |
| **R-1889** | Rules / jobs | Nov 1888 points script (optional) |
| **R-1892** | World expansion | Div 2, reserves, transfers Core |
| **R-1906** | Legal / medical | ME-2 insurance UI |
| **R-1919** | Interwar bundle | Discipline + coach |
| **R-1951** | ME-4 | Lilleshall |
| **R-1965** | Match rules | In-match subs |
| **R-1993** | CM93 skin | ME-6 full injury menus |

**Cadence:** Milestone releases over literal every calendar year — PO may skip thin years.

---

## 5. Implementation ceiling pattern

Each release ships a **`ReleaseScopeRegistry`** (or equivalent mod header) — proposed:

```
# release_manifest.txt (concept)
release_id=release_1888
display_name=Futtoboru 1888
start_season_ids=18
max_implemented_medical_era=ME-1
max_implemented_rules_year=1888
```

**Runtime rule:** `MedicalEraRegistry.resolve(date)` returns era from calendar **but** features above `max_implemented_medical_era` stay **disabled** until a later release raises the ceiling — even if the save simulates past 1905.

**Alternative (deferred PO):** Calendar unlock within one install — only if PO later drops yearly ceiling in favour of “complete sim in one binary.” **Default for R-1888: hard ceiling at ME-1.**

---

## 6. Documentation rules for contributors

1. **Research docs** may describe any era — cite sources; mark **“Ship: R-YYYY”** on each feature row when known.  
2. **Design docs** separate **§ Ledger** (full timeline) from **§ Release slice** (what this tranche builds).  
3. **Implementation / manual** describe **only** what the current release ships — footnote future R-YYYY for ledger features.  
4. **PM reports** use **R-YYYY** in traffic lights and next steps.  
5. **Traceability matrix** links feature → **minimum release id**.

---

## 7. Relationship to procedural multi-decade saves

| Question | R-1888 answer |
|----------|---------------|
| Can I play 60 seasons from 1888 in one save? | Yes — engine proven |
| Will 1906 insurance appear when calendar hits 1906? | **No** — not implemented until **R-1906** raises ceiling |
| Is that historically wrong for a 1888-only binary? | **No** — release scope honesty beats silent anachronism |
| What do we tell players? | Manual: “Futtoboru 1888 implements Victorian injury care; later releases add later medicine.” |

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial yearly release naming + document-all / ship-by-year principle |
| 2026-07-03 | §4.2 full domain roadmap + §4.3 milestone sequence (PO lock) |

---

*Gap analysis: [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md). **R-1888 wrap-up:** [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) · [RELEASE_R1888_DEFINITION.md](RELEASE_R1888_DEFINITION.md).*
