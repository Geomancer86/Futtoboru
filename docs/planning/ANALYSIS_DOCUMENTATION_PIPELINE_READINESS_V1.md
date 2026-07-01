# Analysis — Documentation Pipeline Readiness (Wiki / Game Manual)

**Document type:** Meta-audit — can we ship a player wiki or printed-style manual at release?  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, all leads  

**Related:** [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) · [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md)

---

## 1. Executive answer

**For internal development:** yes — research, analysis, and design coverage is **strong** for Layer B and historical data. Engineers can reconstruct intent from `docs/planning/` and `docs/research/`.

**For a release wiki or game manual:** **not yet.** We have excellent **competitive research** (CM manuals) and **internal design**, but almost no **player-facing pipeline**, no **feature traceability ledger**, and many **stale registries** that would mislead a manual author. A manual writer today would need to read Java and reconcile conflicting docs.

**Rough readiness score (release manual):** **~35%**

| Pipeline stage | Internal use | Manual/wiki use |
|----------------|--------------|-----------------|
| Researched | ✅ ~80% (CM legacy paused; 1888 strong) | ⚠️ 40% — competitor docs, not Futtoboru player text |
| Analysed | ✅ ~75% | ⚠️ 30% — dev-oriented; stale rows remain |
| Designed | ✅ ~70% (Layer B) | ⚠️ 25% — no UI copy, no screen walkthroughs |
| Implemented (documented) | ⚠️ ~40% | ❌ ~15% — few `IMPLEMENTATION_*` docs |
| Player-facing draft | ❌ ~5% | ❌ ~5% — i18n keys only; no manual folder |

---

## 2. What we have today (inventory)

### 2.1 Research (`docs/research/`)

| Asset | Count / quality | Manual value |
|-------|-----------------|--------------|
| CM manual extracts (CM1, CM93, …) | Primary sources archived | **Template** for manual structure — not Futtoboru content |
| Magazine / review extracts | Good for tone & expectations | Background only |
| `RESEARCH_EARLY_CM_INJURIES_VS_FM.md` | Closed note | Informs mechanics wording |
| Historical ledgers (RSSSF, 1888 data) | Strong for World/Data | **Appendix** material (historical notes) |
| Save/tactics samples | Niche | Dev only |

**Verdict:** Research answers “what did CM do?” — not “how does Futtoboru work for the player?”

### 2.2 Analysis (`docs/planning/ANALYSIS_*`)

| Strength | Weakness |
|----------|----------|
| Deep dives per subsystem (match sim, finances, UI scale, …) | ~168 planning files — **no single feature index with lifecycle status** |
| [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) — 52 features × depth | Competitive framing; not player tutorial text |
| Department briefs (Seasons, Match, World, Platform) | Several **outdated** (e.g. `DEPARTMENT_MATCH_AND_SIMULATION_V1.md` pre–Layer B) |

### 2.3 Design (`docs/planning/DESIGN_*`)

| Strength | Weakness |
|----------|----------|
| Layer B designs: tactics, ratings, condition, core loop | Design ≠ shipped — **no mandatory “implementation status” footer** |
| PO locks and scope boundaries clear | UI copy / help strings not specified |
| Test IDs (MVP-*, COND-*) | Not mapped to manual sections |

### 2.4 Implementation docs (`docs/planning/IMPLEMENTATION_*`)

Only **5 files** exist — mostly debug UI and season/cup plans. **Layer B has no implementation closure docs** (tactics, ratings, condition).

| Exists | Missing for Layer B |
|--------|---------------------|
| `IMPLEMENTATION_MATCH_ENGINE_DEBUG_UI_V1.md` | `IMPLEMENTATION_TACTICS_AND_XI_B4.md` |
| `IMPLEMENTATION_SUMMARY_DAILY_SIMULATION_AND_RULES.md` | `IMPLEMENTATION_MATCH_RATINGS_B3.md` |
| Season/cup/debug logging plans | `IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md` ✅ **added 2026-06-30** |

### 2.5 Registries & indexes

| Doc | Problem |
|-----|---------|
| [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) | §0.1 still lists MVP-1–5 as ❌ — **false** post Layer B |
| [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) | §3.2–3.4 superseded — banner added |
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | Good hub; no **manual** section |
| [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) | Stale traffic lights |

### 2.6 Player-facing artefacts

| Artefact | Status |
|----------|--------|
| `docs/manual/` | **Does not exist** — scaffold added 2026-06-30 |
| In-game help / tutorial | None |
| i18n (`mods/language/BaseBundle_*.properties`) | Partial — screen labels, not explanatory prose |
| Wiki / GitHub Pages | Not started |

---

## 3. What a release manual needs (CM manual as model)

CM93 manual structure (from [CM93_Manual_Menu_Extract.md](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md)):

1. Starting up / new game / save disks  
2. Main menu tour  
3. Squad screen (columns, icons, staff)  
4. Tactics / team selection  
5. Match day flow  
6. Transfers  
7. Training  
8. Board / jobs  
9. **Player injuries & insurance** — prose + manager choices  
10. Attributes glossary  
11. Competitions / cups  

Futtoboru manual at release would need **parallel chapters** — but only for **shipped** features, with **1888 historical notes** where rules differ from CM.

---

## 4. Gap analysis — what is still needed

### 4.1 Process & governance (P0 — before manual writing)

| ID | Deliverable | Purpose |
|----|-------------|---------|
| **DOC-1** | **Feature traceability matrix** (CSV or markdown) | Columns: Feature ID · Research doc · Analysis · Design · Implementation · Code paths · Tests · Manual § · i18n keys · Status |
| **DOC-2** | **Doc lifecycle template** | Every feature: `RESEARCH → ANALYSIS → DESIGN → IMPLEMENTATION → MANUAL_EXCERPT` |
| **DOC-3** | **Registry refresh sprint** | Update `SYSTEMS_AND_FEATURES_REGISTRY`, department briefs, PM eagle view to Layer B truth |
| **DOC-4** | **Stale doc policy** | Banner + supersede link; quarterly audit |
| **DOC-5** | **Design doc footer standard** | Required sections: Status · Shipped in · Code · Tests · Manual § (TBD) |

### 4.2 Player-facing pipeline (P1 — parallel to feature work)

| ID | Deliverable | Purpose |
|----|-------------|---------|
| **MAN-1** | [docs/manual/README.md](../manual/README.md) outline | Table of contents + links to source design docs |
| **MAN-2** | **Glossary** (`docs/manual/GLOSSARY.md`) | Fit%, sharpness, morale, re-election, etc. |
| **MAN-3** | **Screen inventory** | One subsection per screen: purpose, controls, columns |
| **MAN-4** | **Mechanics appendix (player language)** | Simplified formulas — no Java |
| **MAN-5** | **i18n ↔ manual sync** | Long help strings in properties; manual imports same text |
| **MAN-6** | Screenshot / capture checklist | After human B-9 — tied to manual sections |

### 4.3 Per shipped feature (P1 — rolling)

For each **Core** or **Partial** feature in [depth audit](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md):

| Required artefact | Example (injuries) |
|-------------------|-------------------|
| Research note | `RESEARCH_EARLY_CM_INJURIES_VS_FM.md` ✅ |
| Analysis | `ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md` ✅ |
| Design (target) | `DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md` ✅ |
| Design (baseline) | `DESIGN_PLAYER_CONDITION_V1.md` ✅ |
| Implementation record | `IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md` ✅ |
| Manual excerpt | `docs/manual/06-squad-and-condition.md` 🔲 |
| Tests cited | `InjuryGeneratorTest`, `InjuredPlayerLineupIT` |

### 4.4 Content gaps by game area (manual completeness)

| Game area | Research | Design | Impl doc | Manual-ready? |
|-----------|----------|--------|----------|---------------|
| New game / save | Partial | Partial | ❌ | ❌ |
| Continue / calendar | Partial | Partial | Partial | ❌ |
| Squad / attributes | ✅ | Partial | ❌ | ❌ |
| **Fitness / morale / injury** | ✅ | ✅ B5 + B5.1 proposal | ✅ B5 | ⚠️ After B5.1 + excerpt |
| Tactics / XI | ✅ | ✅ | ❌ | ❌ |
| Match preview / result | ✅ | ✅ | ❌ | ❌ |
| Ratings | ✅ | ✅ | ❌ | ❌ |
| Attendance / finances | ✅ | Partial | Partial | ❌ |
| Inbox / messages | ✅ | ✅ | ❌ | ❌ |
| Competitions / FA Cup | ✅ | ✅ | Partial | ❌ |
| Season rollover | ✅ | ✅ | Partial | ❌ |
| Transfers | ✅ CM | Stub | ❌ | N/A until shipped |
| Training | ✅ CM | Stub | ❌ | N/A until shipped |
| Board / jobs | Partial | ✅ jobs | Partial | ❌ |
| Settings / UI scale | ✅ | ✅ | ✅ PU-10 | ⚠️ |

---

## 5. Recommended documentation order (next 3 sprints)

```
Sprint doc-A   DOC-1 traceability matrix + registry refresh (Layer B rows)
Sprint doc-B   IMPLEMENTATION_* for B3/B4/B5 + MAN-2 glossary + MAN-3 screen inventory
Sprint doc-C   First manual chapters from shipped features (continue, squad, tactics, match)
               — only after human B-9 screenshots
```

Injury/fitness track (this request):

1. ✅ `ANALYSIS_PLAYER_CONDITION_AND_INJURIES_V1.md`  
2. ✅ `DESIGN_PLAYER_CONDITION_B5_1_PROPOSAL.md`  
3. ✅ `IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md` (as-built B5)  
4. 🔲 PO decision on B5.1 scope  
5. 🔲 `docs/manual/06-squad-and-condition.md` after design lock  

---

## 6. Risks if we skip this

| Risk | Impact |
|------|--------|
| Manual author reads stale registry | Documents features that do not exist |
| No implementation docs | Wiki describes **design intent**, not **behaviour** |
| i18n and manual diverge | In-game text contradicts wiki |
| Competitive docs copied verbatim | Players expect CM93 insurance when we only have strain stub |
| No glossary | “Fit%” vs “sharpness” vs attribute “form” confusion |

---

## 7. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial pipeline readiness audit; manual scaffold; injury doc chain started |

---

*Next: maintain [docs/manual/README.md](../manual/README.md) as the manual table of contents grows.*
