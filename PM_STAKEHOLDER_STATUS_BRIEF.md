# PM & Stakeholder Status Brief

**Document type:** Project status / stakeholder communication  
**Audience:** PM, PO, Sponsor, department leads, expert sign-off owners  
**Branch:** `analyze-ai-branch` (merged `origin/develop` + `origin/feature/competition-completion-v1`)  
**Date:** 2026-06-27  
**Next update:** After manual QA sign-off

---

## 1. One-page executive summary

**Futtoboru** is an open-source historical football management sim (1888–89 onward). The branch now has a **complete automated pre-QA gate**: league + FA Cup E2E, save/load roundtrip, 3-season PR soak, and **60-season north-star soak**.

| Question | Answer |
|----------|--------|
| **Can we ship v1 today?** | **Not yet** — manual QA + UI fixes (load, double-click) |
| **How far are we?** | **~85% CM1 loop** automated · **~45% CM1 features** · **~12% CM4 depth** |
| **Biggest risk** | Doc/code drift on UI bugs (C-1, C-3); inbox growth at long careers |
| **Biggest asset** | Proven infinite season loop + CM research corpus + 59-test gate |
| **Recommended focus** | Manual QA → v1.0 tag → v1.1 (XI, ratings, unemployed) per roadmap report |

**Season model (PO):** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) — **no future season mod folders for v1.**

**60-season goal:** [PM_60_SEASON_GOAL_STATUS.md](PM_60_SEASON_GOAL_STATUS.md)

**Features & roadmap (CM/FM aligned):** [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md)

**Historical data expansion (round 2):** [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) — **0 extra years** for forward play; **+3 start mods** recommended after Wave 1 infra.

**In-play evolution:** [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) — new clubs/comps/countries must enter **`SaveGame`** on historical dates when playing from the earliest season.

**Post-merge analysis:** [PM_V1_POST_MERGE_REANALYSIS.md](PM_V1_POST_MERGE_REANALYSIS.md)

**Automated suite:** ✅ **59 PR tests** + nightly **`LongRunSoakIT`** (~5 min) + **`TenSeasonReportIT`** report.

**v1 plan:** [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)

---

## 2. Documentation inventory (what stakeholders can read)

**Start here:** [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)

### Project & governance

| Document | For whom | Purpose |
|----------|----------|---------|
| [PROJECT_MASTER_ANALYSIS.md](PROJECT_MASTER_ANALYSIS.md) | PM, PO, Tech lead | Technical audit, branch reality |
| [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) | PO, QA | v1 acceptance criteria NG/PL/SC |
| [STAKEHOLDERS_AND_DEPARTMENTS.md](STAKEHOLDERS_AND_DEPARTMENTS.md) | PM, PO | 6 departments, sign-off gates G1–G6 |
| [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) | All eng | Prioritized RB/C/H backlog |
| [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) | Eng, QA | Screen/system status matrix |

### Department deep dives (v1 core)

| Document | Expert slot | Gate |
|----------|-------------|------|
| [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) | _____________ | G1 |
| [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md) | _____________ | G2 |
| [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) | _____________ | G3 |
| [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) | _____________ | G4 |

### Competitive / design reference (Jun 2025 research rounds)

| Document | Content |
|----------|---------|
| [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) | **Round 3** — Soccerama, Toms FM, Collyer, 1992 rivals |
| [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) | CM1–CM03/04 timeline |
| [CM1_REFERENCE.md](CM1_REFERENCE.md) | **Round 3** CM1 v1 benchmark + press + manual |
| [CM2_REFERENCE.md](CM2_REFERENCE.md) | CM2 + European expansions |
| [CM3_REFERENCE.md](CM3_REFERENCE.md) | CM3 + 01/02 community golden age |
| [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) | CM4 all game systems |
| [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md) | CM4 devs, patches, media, fansites |
| [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md) | Manual PDF sources (CM3, CM97/98, CM1) |
| [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) | **Legacy target vs codebase** |
| [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) | FM2005+ index (Round 1) |

---

## 3. Project status by department

### Core v1 departments

| Department | Status | % to v1 gate | Top blocker |
|------------|--------|--------------|-------------|
| **Seasons & Rules** | 🟡 Amber | ~70% | `SeasonManager` + 3-season soak IT green |
| **Match & Simulation** | 🟡 Amber | ~55% | Sim + completion wired; cup path landed |
| **World & Data** | 🟡 Amber | ~55% | Start mod 18 complete; **no per-year mod folders required** |

### Supporting

| Department | Status | Notes |
|------------|--------|-------|
| **Platform & UX** | 🟡 Amber | Save/load gaps; no CI season test; Continue double-click bug |
| **Economy & Club** | 🟢 Green-ish | Wages/expenses work; no transfer market |
| **Roster & People** | 🟡 Amber | Attributes + contracts; no training/morale |

### v1 acceptance criteria snapshot

| Section | Pass | Partial | Fail |
|---------|------|---------|------|
| New game & season start (NG-*) | 4 | 1 | 2 |
| In-season play (PL-*) | 3 | 4 | 1 |
| Season completion (SC-*) | 0 | 0 | **6** |
| Season rollover (SR-*) | 0 | 0 | **all** |

Full criteria: [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §4.

---

## 4. Competitive research conclusions (for scope control)

### What "legacy CM4 parity" means for Futtoboru

- **Not** 39 nations or 200k players  
- **Yes** season loop, rules, league/cup structure, finances, contracts  
- **Later** training, tactics, scouting, morale, media (CM3–CM4 depth)  
- **Optional** 2D match engine (CM4) — abstract sim acceptable for v1  

### Gap summary (feature branch vs CM4 systems)

| Category | Futtoboru | CM4 had |
|----------|-----------|---------|
| Season completes + rolls | ❌ | ✅ |
| Historical rules | ❌ | ✅ (modern rules) |
| League + table | ⚠️ | ✅ |
| Training / tactics / scouting | ❌ stubs | ✅ |
| Transfers | ❌ | ✅ |
| Board / fans / media | ❌ | ✅ |
| 2D engine | ❌ | ✅ |
| Mod/editor culture | ⚠️ txt mods | ✅ |

Detail: [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)

### Research rounds complete / planned

| Round | Status | Deliverable |
|-------|--------|-------------|
| FM Bible Round 1 | ✅ | FM2005–2010 + CM prelude |
| CM Legacy Round 2 | ✅ | CM1–CM4 massive refs + manuals index + gap |
| FM Bible Round 2 | 🔲 | FM2011–2015, FM2021–2023 |
| CM1 + pre-CM1 Round 3 | ✅ | [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) + [CM1_REFERENCE.md](CM1_REFERENCE.md) Round 3 |
| CM Manual acquisition Round 4 | ✅ | Local archive + [CM_MANUALS_COLLECTION.md](CM_MANUALS_COLLECTION.md) + CM1 menu extract |
| CM Manual → registry Round 5 | 🔲 | CM1 extract + CM3 PDF TOC → feature registry |
| CM Manual deep analysis Round 5 | ✅ | [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) |
| CM source hunt Rounds 9–12 | ✅ | [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md) — magazine OCR, Wayback, AP27/TONE046 |
| CM saves/tactics RE Round 13 | ⚠️ | [saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md) — formats catalogued; binaries pending |
| CM Magazine CD extract Round 14 | ⚠️ | cmmag-01 samples only; **cmmag-04 deferred** at pause |
| CM research backlog Round 15 | ✅ | [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md) — full missing inventory |
| **CM research pause Round 16** | ✅ | [CM_LEGACY_RESEARCH_PAUSE.md](CM_LEGACY_RESEARCH_PAUSE.md) — **stopped**; early games OK for v1 |
| **MVP / v1 feature sprint** | ⚠️ **Phase 0 done** | [PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md](PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md) |
| **Phase 1 season loop** | 🔲 **NEXT** | `SeasonPlaythroughIT` + RB fixes |
| CM03/04 supplement | 🔲 | Optional — resume research if needed |

---

## 5. Critical path (PM schedule input)

### Phase A — v1 blockers (estimate: 4–8 eng-weeks, TBD by experts)

| # | Work item | Owner dept | Gate |
|---|-----------|------------|------|
| A1 | Wire `isLeagueComplete()` + champion | Seasons & Rules | G1 |
| A2 | Apply 1888 `CompetitionRules` from mod | World & Data | G3 |
| A3 | Implement `SeasonManager` + rollover | Seasons & Rules | G1 |
| A4 | Season 19 mod folder + scripts | World & Data | G3 |
| A5 | Fix friendly `matchesPlayed`, AuthorityManager, load/job init | Match + Platform | G2, G4 |
| A6 | `SeasonPlaythroughIT` headless test | Platform & UX | G4 |

### Phase B — CM1 parity polish (post-A)

- Player match ratings · pick XI · board expectation · FA Cup decision  

### Phase C — legacy CM3–CM4 depth (post-v1)

- Tactics → training → transfers → scouting → morale/media  

**PO decisions still open:** Merge strategy · release tag only (gameplay locked in RELEASE_V1 §10)

Document: [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §8.

---

## 6. Branch & release reality

| Branch | What it is |
|--------|------------|
| `main` | Public 0.2.0 (Jul 2023) — manual era only |
| `develop` | Planning docs; no feature code |
| **`feature/competitions-finances-seasons`** | **Source of truth** — v0.4.0-SNAPSHOT AI-era work |
| `analyze-ai-branch` | Local docs branch (this documentation commit) |

**Public GitHub does not show feature branch work** unless merged or pushed.

---

## 7. Stakeholder briefing guide

### For **Sponsor / Business**

- v1 is **not shippable**; core loop incomplete  
- Investment thesis: historical niche + mod pipeline, not FM clone  
- Ask: confirm v1 scope (league-only vs FA Cup; employed-only start)

### For **Product Owner**

- Use [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) for in/out  
- Use [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) to reject scope creep ("we need CM4 transfers" ≠ v1)  
- Fill expert names in [STAKEHOLDERS_AND_DEPARTMENTS.md](STAKEHOLDERS_AND_DEPARTMENTS.md) §10

### For **PM**

- Track Phase A on board; gates G1–G4 block release  
- Jenkins: `maven.test.failure.ignore=true` — fix before calling v1 stable  
- Documentation hub ready for onboarding new experts

### For **Department experts**

| Expert | Read first | Sign-off |
|--------|------------|----------|
| Seasons & Rules | Dept doc + gap §3.2 | G1 |
| Match & Sim | Dept doc + CM4 match § | G2 |
| World & Data | Dept doc + CM_MANUALS | G3 |
| Platform | Dept doc + BUGS registry | G4 |

---

## 8. Risks & issues (top 10)

| ID | Risk | Severity | Mitigation |
|----|------|----------|------------|
| R1 | Season never completes in prod | Critical | Phase A1 |
| R2 | Wrong 1888 rules shipped | Critical | Phase A2 |
| R3 | No season 2 data | Critical | Phase A4 |
| R4 | Save/load corrupts career | High | Phase A5 |
| R5 | Scope creep to CM4 full parity | High | This brief §4 |
| R6 | Expert slots unfilled | Medium | PO assigns names |
| R7 | Dual league architecture confusion | Medium | World dept doc |
| R8 | FA Cup half-built | Medium | PO decision |
| R9 | CI ignores test failures | Medium | Platform gate |
| R10 | Public repo stale vs feature branch | Low | Comms / merge plan |

Full list: [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md)

---

## 9. Recommended communications

| Audience | Channel | Message |
|----------|---------|---------|
| All stakeholders | Share `DOCUMENTATION_INDEX.md` link | "Single doc hub live" |
| PO + PM | This brief | Status + Phase A |
| Experts | Dept deep dive + gap matrix | Own gate criteria |
| Community (future) | After v1 | Feature branch merge announcement |

---

## 10. Document changelog

| Date | Author | Change |
|------|--------|--------|
| 2025-06-25 | Research/doc sprint | Initial PM brief after CM Round 2 + gap analysis |
| 2025-06-25 | Round 16 | CM legacy research **paused**; next phase MVP/v1 |
| 2025-06-25 | v1 plan | [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) — E2E, FA Cup, static players |
| 2025-06-25 | Phase 0 | [PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md](PM_PHASE0_TEST_INFRASTRUCTURE_REPORT.md) — harness + ITs + CI |

**Distribution:** PM, PO, Sponsor, department leads. Update when Phase A starts or PO resolves open decisions.
