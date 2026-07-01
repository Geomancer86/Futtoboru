# Futtoboru — Player Manual (draft scaffold)

**Status:** **Not for players yet** — internal scaffold for eventual wiki / PDF manual at release.  
**Last updated:** 2026-07-01  
**Maintainer:** PO + Platform/UX  

Do **not** publish this folder until chapters are marked **Player-ready** and synced with shipped build + i18n.

---

## How this folder relates to dev docs

| Dev doc type | Location | Becomes manual… |
|--------------|----------|-----------------|
| Research | `docs/research/` | Background / historical appendix only |
| Analysis | `docs/planning/ANALYSIS_*` | Source for accuracy checks — not copied verbatim |
| Design | `docs/planning/DESIGN_*` | Mechanics appendix (simplified) |
| Implementation | `docs/planning/IMPLEMENTATION_*` | **Authoritative behaviour** for manual text |
| This folder | `docs/manual/` | Player language + screenshots |

See [ANALYSIS_DOCUMENTATION_PIPELINE_READINESS_V1.md](../planning/ANALYSIS_DOCUMENTATION_PIPELINE_READINESS_V1.md).

---

## Planned table of contents

| § | Chapter file | Shipped? | Source implementation doc |
|---|--------------|----------|---------------------------|
| 1 | `01-getting-started.md` | 🔲 | Save/load, new game — TBD |
| 2 | `02-main-menu-and-calendar.md` | 🔲 | Continue, schedule — TBD |
| 3 | `03-competitions-and-season.md` | 🔲 | `IMPLEMENTATION_PLAN_SEASON_CUP_V1.md` (partial) |
| 4 | `04-tactics-and-team-selection.md` | ⚠️ Partial | [IMPLEMENTATION_TACTICS_B4_V1.md](../planning/IMPLEMENTATION_TACTICS_B4_V1.md) |
| 5 | `05-match-day.md` | ⚠️ Partial | [IMPLEMENTATION_MATCH_RATINGS_B3_V1.md](../planning/IMPLEMENTATION_MATCH_RATINGS_B3_V1.md) |
| 6 | `06-squad-and-condition.md` | ⚠️ Draft | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10 |
| 7 | `07-player-actions-insurance.md` | ⚠️ Draft | [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10 |
| A | `appendix-glossary.md` | ⚠️ Draft | Condition terms |
| 8 | `08-finances.md` | ⚠️ Partial | Expenses analysis docs |
| 9 | `09-inbox-and-news.md` | 🔲 | Message system — TBD |
| 10 | `10-manager-career-and-jobs.md` | ⚠️ Partial | Job system design |
| 11 | `11-settings-and-accessibility.md` | ⚠️ Partial | UI scale — PU-10 |
| A | `appendix-glossary.md` | 🔲 | [GLOSSARY.md](GLOSSARY.md) (TBD) |
| B | `appendix-1888-rules.md` | 🔲 | Competition rules mod |
| C | `appendix-attributes.md` | 🔲 | Player attribute model |

**Legend:** 🔲 not started · ⚠️ dev docs exist, player chapter not written · ✅ player-ready

---

## Writing rules (when chapters begin)

1. **Second person**, present tense — “Select Tactics from the sidebar.”  
2. **No Java class names** in player text.  
3. **Match i18n** — long help strings live in `mods/language/`; manual repeats or links same wording.  
4. **State era** — 1888 rules when they differ from modern football or CM93.  
5. **Screenshot placeholder** — `![Squad screen](screenshots/squad-fit-morale-injury.png)` after human B-9.  
6. **Version stamp** — each chapter footer: `Futtoboru x.y.z · chapter rev date`.

---

## Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Scaffold created |
