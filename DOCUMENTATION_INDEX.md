# Futtoboru — Documentation Index

**Last updated:** 2025-06-25 (Round 11 source hunt)

Use this index to navigate project documentation. **Start here** after cloning.

---

## For PM & PO (brief)

| Document | Audience | Purpose |
|----------|----------|---------|
| [PM_STAKEHOLDER_STATUS_BRIEF.md](PM_STAKEHOLDER_STATUS_BRIEF.md) | **PM, all stakeholders** | **Status brief** — progress, docs hub, critical path |
| [STAKEHOLDERS_AND_DEPARTMENTS.md](STAKEHOLDERS_AND_DEPARTMENTS.md) | PM, PO | Who owns what; core vs broader; sign-off gates |
| [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) | PO | v1 acceptance criteria |
| [PROJECT_MASTER_ANALYSIS.md](PROJECT_MASTER_ANALYSIS.md) | PM, PO | Executive technical status |

---

## v1 core departments (deep dives)

| Document | Expert owns |
|----------|-------------|
| [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) | Season lifecycle, rules, scripts, rollover |
| [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md) | Fixtures, match engine, standings input |
| [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) | Mods, loaders, season 18/19 content |
| [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) | CI, harness tests, save/load, UI gates |

---

## Registries

| Document | Purpose |
|----------|---------|
| [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) | Every system/screen status |
| [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) | Prioritized bug backlog |

---

## Competitive reference (CM legacy + FM bible)

Industry benchmark wiki for Futtoboru design targets.

### CM legacy (Round 2–3 — Jun 2025)

| Document | Purpose |
|----------|---------|
| [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md) | **Round 11** — every source acquired/missing; citation-chain hunt queue |
| [docs/research/sources/README.md](docs/research/sources/README.md) | Local GameFAQs, magazine OCR, forum captures |
| [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md) | Honest manual/patch/review coverage matrix + backlog |
| [CM93_94_REFERENCE.md](CM93_94_REFERENCE.md) | **Round 7** — CM93/94 manual, expansions, era reviews |
| [CM9798_REFERENCE.md](CM9798_REFERENCE.md) | **Round 7** — CM97/98 engine finale, editor, reception |
| [CM_ITALIA_SPINOFFS_REFERENCE.md](CM_ITALIA_SPINOFFS_REFERENCE.md) | **Round 8** — CM Italia / Norge / Guy Roux spin-offs |
| [CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md](CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md) | Patch + data-disk + EP timeline all titles |
| [CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md](CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md) | Contemporary scores + user sentiment index |
| [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) | **Round 3** — Soccerama, Toms FM 1982, Collyer BBC tracker, 1992 market |
| [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) | CM1–CM03/04 timeline & feature debut table |
| [CM1_REFERENCE.md](CM1_REFERENCE.md) | **Round 3** — CM1 v1 benchmark: press, competitors, manual, Futtoboru map |
| [CM2_REFERENCE.md](CM2_REFERENCE.md) | CM2 + European Leagues expansions |
| [CM3_REFERENCE.md](CM3_REFERENCE.md) | CM3 + 99/00–01/02, community golden age |
| [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) | CM4 all game systems |
| [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md) | CM4 patches, media, developers, fan sites |
| [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md) | Manual sources + **local archive** (`docs/research/manuals/`) |
| [CM_MANUALS_COLLECTION.md](CM_MANUALS_COLLECTION.md) | Round 4 — manual acquisition summary |
| [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) | **Round 5** — basic features by release (manual-derived) |
| [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) | Legacy target vs codebase (~12% CM4 parity) |

### FM bible (Round 1)

| Document | Purpose |
|----------|---------|
| [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) | FM2005+ master index |
| [FM_COMPETITIVE_BIBLE_ROUND1.md](FM_COMPETITIVE_BIBLE_ROUND1.md) | FM Round 1 entries |

**PM / status:** [PM_STAKEHOLDER_STATUS_BRIEF.md](PM_STAKEHOLDER_STATUS_BRIEF.md)  
**Design gaps:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)  
**FM Round 2 (planned):** FM2011–2015, FM2021–2023

---

## Legacy / reference (AI era — may be outdated)

Older analysis files (`ANALYSIS_*.md`, `ROADMAP_V1.0.md`, `TASKS_V1.0.md`, etc.) remain for history. **Prefer the documents above** for decisions. Known drift: some claim match sim is "0% implemented" — it exists on the feature branch but season loop does not.

---

## Branch note

Most accurate code state: **`feature/competitions-finances-seasons`**. Public `main` is Release 0.2.0 (July 2023).
