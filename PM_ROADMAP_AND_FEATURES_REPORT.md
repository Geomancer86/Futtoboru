# PM Report — Features, Gaps & Roadmap (CM/FM Aligned)

**Document type:** PM status / product roadmap  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Audience:** PM, PO, Sponsor, department leads, QA  
**Evidence base:** 59 automated tests · [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md) · [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **What is proven today?** | The **core season loop** (league + FA Cup headless, procedural rollover, finances, save JSON roundtrip, mandatory gates) — validated by **59 PR tests** and a **10-season simulation report**. |
| **What still needs fleshing?** | **UI/production polish**, **historical authenticity** (rules display, sim balance), **inbox scaling**, and almost all **CM management depth** (tactics, training, transfers, board). |
| **Is the CM/FM roadmap good?** | **Research: excellent.** **Execution tracking: was poor** (stale `ROADMAP_V1.0.md`, outdated registry). This report + updated registry **replace** those as the living roadmap. |
| **Where are we vs CM1?** | **Season loop ~85%** (automated) · **CM1 full feature set ~45%** · **CM4 depth ~12%** (unchanged for management systems). |
| **Next gate** | **Layer A** complete → **Layer B MVP** → **Layer C 1892** |

**Master plan:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md)

---

## 2. Proven systems (automated or strongly evidenced)

These are **not theoretical** — headless integration tests or the 10-season report demonstrate behaviour.

| System | Evidence | v1 PO scope |
|--------|----------|-------------|
| **Procedural season loop** | `SeasonPlaythroughIT`, `MultiSeasonSoakIT`, `TenSeasonReportIT`, `LongRunSoakIT` | ✅ In scope |
| **League creation + mandatory draw** | `MandatoryGateIT`, league scripts | ✅ |
| **League completion (league-only stats)** | RB-7 fix, `LeagueStandingsManagerTest` | ✅ |
| **Champion edition finalization** | 10 editions in report; rollover | ✅ |
| **FA Cup knockout (headless)** | `LeagueAndCupIT`, cup unit tests | ✅ |
| **Competition rules from start mod** | `competition_rules.txt`, `CompetitionRulesLoaderTest` | ✅ |
| **SeasonManager + save fields** | `startSeasonId`, `simulatedSeasonIndex` | ✅ |
| **Day-by-day engine pipeline** | All E2E ITs | ✅ |
| **Match simulation (strength RNG)** | Seasons complete; goals/points update | ✅ Basic |
| **Standings / points** | League completes; 2-1-0 in mod rules | ⚠️ Verify UI |
| **Weekly expenses + wages** | 10-season PNE balance/income/spend | ✅ |
| **Inbox + mandatory gates** | `MandatoryGateIT`; draw ack | ✅ |
| **Save/load JSON roundtrip** | `SaveLoadIT` | ⚠️ UI load still gaps |
| **Static squads (no regen/retire)** | PO locked; no lifecycle code | ✅ |
| **Test harness + CI** | 59 tests, GHA, `-Psoak` | ✅ |
| **Mod/data pipeline** | Loader tests, season 18 content | ✅ |

**Simulation speed (corrected):** ~**5.4 s/season** league-only when test log spam is suppressed — **not** CPU-bound. See [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md).

---

## 3. Working skeleton — needs fleshing (priority order)

These **run in code** but are **thin, wrong in UI, or untested manually**.

### 3.1 v1 ship blockers (before tag)

| Area | State | Gap | Owner dept |
|------|-------|-----|------------|
| **Manual QA pass** | Checklist open | No human sign-off on UI flows | QA |
| **Continue double-click** | C-1 | Two days per click | Platform/UX |
| **Production save/load** | C-3, C-4, H-9 | Load skips job init; top menu load stub | Platform |
| **2-1-0 in UI** | RB-3 partial | Rules file exists; UI/table may still show 3-1-0 | Seasons/Rules |
| **Match history → result** | H-3 | Clicks broken | Platform/UX |
| **Inbox growth** | 763 msgs @ 10 seasons | BLK-4 risk; scan cost | Platform |
| **Debug log noise** | M-12 | `System.out` every day slowed old soak | Platform |

### 3.2 v1 “complete but shallow”

| Area | State | Flesh-out needed |
|------|-------|------------------|
| **Match engine** | Strength RNG, no tactics | Post-match ratings (CM1), home/away tuning, less Accrington dominance |
| **Match preview / result UI** | Stubs | Lineups, scorers, attendance display |
| **Finances screen** | Backend ticks | Season boundary display, charts from `financialHistory` |
| **Schedule / fixtures UI** | Partial windows | Full season calendar, cup ties visible |
| **Competitions UI** | League good; cup WIP | Bracket view, cup detail polish |
| **Friendlies** | Hardcoded | Must not break completion (RB-7 guard exists; UX conflict check) |
| **Authority / re-election** | Comments only | 1888 bottom-4 script/message (PO: message-only OK for v1) |
| **Cup + league same career (UI)** | Headless proven | Player-facing verification in manual QA |

### 3.3 Backend exists, UI stub (v1.1+)

| System | CM era | Futtoboru files |
|--------|--------|-----------------|
| **Tactics / XI** | CM1 pick team | `TacticsScreenTable` stub — sim ignores picked XI |
| **Training** | CM3 | `TrainingScreenTable` stub |
| **Transfers** | CM93/94 | `TransfersScreenTable` stub |
| **Scouting** | CM3 | `ScoutingScreenTable` stub |
| **Board / expectations** | CM1 sack | `BoardScreenTable` stub |
| **Staff** | CM1 | `StaffScreenTable` stub |
| **Unemployed career** | CM2+ | Job backend ✅; v1 starts employed only |

---

## 4. Not started / missing (by design or backlog)

| Feature | v1? | CM reference | Notes |
|---------|-----|--------------|-------|
| Youth / regen / retirement | ❌ v1 | CM3+ | PO locked static world |
| Injuries / fitness model | ❌ v1 | CM93/94 | No sim impact |
| International | ❌ | CM3 | Out of 1888 scope |
| Promotion / relegation | ❌ v1 | CM1 | Single division 1888 |
| 2D / 3D match view | ❌ | CM4 / FM09 | Text/abstract OK for v1 |
| Fog of war / scouting reveal | ❌ | CM01/02 | All stats visible |
| Multi-country leagues | ❌ | CM97/98 | England only |
| Nov 1888 points rule change | Defer | Historic | In-season script future |
| Per-year mod folders | ❌ | N/A | Procedural rollover instead |

---

## 5. Recommended roadmap (aligned to CM/FM research)

Research corpus is **strong**: [CM1_REFERENCE.md](CM1_REFERENCE.md), [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md), [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md), [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md), manual extracts under `docs/research/`. **Gap:** execution docs (`ROADMAP_V1.0.md`, registry) lagged code by ~6 months — corrected below.

### Phase A — Ship v1.0 (now → 2 weeks)

| ID | Deliverable | CM analogue | Exit |
|----|-------------|-------------|------|
| A-1 | Manual QA checklist green | CM1 “finish a season” | [TESTING_COMPETITION_COMPLETION.md](TESTING_COMPETITION_COMPLETION.md) |
| A-2 | Fix C-1, C-3, H-3, H-9 | CM1 Done button reliability | Playable UI loop |
| A-3 | Verify 2-1-0 + goal avg in league UI | CM1 rules | Table matches mod |
| A-4 | Tag `v1.0.0` | — | PO sign-off |

### Phase B — v1.1 “CM1 completeness” (1–2 months)

| ID | Deliverable | CM analogue | Research doc |
|----|-------------|-------------|--------------|
| B-1 | **Unemployed start** | CM2 career | [DESIGN_UNEMPLOYED_JOB_SYSTEM_V1.md](DESIGN_UNEMPLOYED_JOB_SYSTEM_V1.md) |
| B-2 | **Pick XI / basic formation → sim** | CM1 team selection | CM1 §3 tactics |
| B-3 | **Post-match player ratings (0–10)** | CM1 first in genre | CM1 §2 |
| B-4 | **Sim balance tuning** | — | Ten-season Accrington issue |
| B-5 | **Inbox archive / cap** | CM4 message load | BLK-4 |
| B-6 | **Production save/load hardening** | CM1 save | C-6, C-7, H-8 |
| B-7 | **Board expectation + sack** | CM1 | CM1 §6 |

### Phase C — v1.2 “CM93/94 management core” (3–6 months)

| ID | Deliverable | CM analogue |
|----|-------------|-------------|
| C-1 | Transfer engine (basic buy/sell) | CM93/94 |
| C-2 | Wage / squad budgets separate from balance | CM4 |
| C-3 | Friendlies rework (optional, safe) | CM2 |
| C-4 | FA Cup UI bracket + history | CM1 cup |
| C-5 | In-season timeline scripts (Nov 1888 points) | FM-style scripts on start mod |

### Phase D — v2 “CM3 depth” (6–12 months)

Ordered by [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md §6 P2](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md):

1. Training → attribute development (replace random drift)  
2. Scouting + shortlist  
3. Morale / personalities  
4. Staff hiring + assistant delegation  
5. Media / praise–shame (1888 flavour)  

### Phase E — Differentiation (ongoing)

| Futtoboru-only | vs CM/FM |
|----------------|----------|
| Historical 1888 mod pipeline | FM has no 19th-century loop |
| Professions + nationality modifiers | Era authenticity |
| Procedural infinite seasons | FM uses yearly DB updates |
| Script-driven timeline events | FM pre-game editor vs in-career scripts |

---

## 6. CM/FM research vs roadmap — honest assessment

| Dimension | Grade | Comment |
|-----------|-------|---------|
| **Research depth** | **A** | CM1–CM4 + FM bible + manuals + gap matrix — rare for an indie project |
| **v1 scope clarity** | **A** | PO locked: employed, static squads, league+cup, procedural loop |
| **Execution tracking** | **B−** (was **D**) | Old `ROADMAP_V1.0.md` still said “match sim missing”; now superseded |
| **Test strategy** | **A** | Harness + soak + 10-season report — matches CM-style long-save proof |
| **CM1 season-loop parity** | **~85% automated** | Missing UI polish + ratings + pick XI |
| **CM4 management parity** | **~12%** | Expected; not v1 goal |
| **Roadmap continuity** | **B+** | P0 from gap analysis **largely done**; P1–P2 still valid forward plan |

**Verdict:** You have **excellent CM/FM research** and a **correct v1 wedge** (infinite historical season loop). The weakness was **documentation drift**, not missing research. Use this report + updated [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) as the single forward roadmap.

---

## 7. Department priorities (next quarter)

| Department | Now | Next |
|------------|-----|------|
| **Platform / UX** | Manual QA support | C-1, load/save UI, inbox cap |
| **Seasons / Rules** | Procedural loop done | UI rules display, re-election script |
| **Match / Sim** | Headless complete | Ratings, XI input, balance |
| **World / Data** | Season 18 solid (556 players) | **Wave 1:** DB-1–DB-2 (IDs + lint); **Wave 2:** first new start mod |
| **QA** | Automated gate green | Manual checklist, regression on 10-season report |
| **Design** | CM1 benchmark locked | v1.1 tactics/ratings spec from CM1 manual |

---

## 8. Key metrics

| Metric | Value |
|--------|-------|
| PR automated tests | **59** (~61 s) |
| Proven procedural seasons (automated) | **60** (`LongRunSoakIT`) |
| 10-season wall time | **~55 s** (league only) |
| CM1 loop (automated) | **~85%** |
| CM1 full features | **~45%** |
| CM4 depth | **~12%** |
| Manual QA | **Not started** |

---

## 9. Document map

| Need | Read |
|------|------|
| **V1 finish + MVP + 1892 plan** | [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |
| 10-season champions + finances | [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md) |
| v1 acceptance criteria | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |
| CM feature checklist | [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) |
| System inventory (updated) | [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) |
| Historical data / start seasons | [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) |
| North star DB design | [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) |
| Test plan | [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) |
| Bugs | [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) |

---

## 10. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial report post 59-test gate + 10-season analysis |
| 2026-06-27 | Cross-link historical data expansion report |
| 2026-06-27 | Link V1 MVP + 1892 master plan |
| 2026-06-27 | Supersedes stale execution sections of `ROADMAP_V1.0.md` |

---

*Next PM update: after PO locks 1892 membership (Option A/B/C/D) or Layer A manual QA.*
