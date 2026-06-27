# Futtoboru — Release v1.0 Definition

**Document type:** Product scope / acceptance criteria  
**Analysis date:** 2025-06-25 · **PO update:** 2026-06-26 (season model — procedural loop)  
**Status:** **Active** — see [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)

---

## 1. Release vision (your stated goal)

> **First release must allow playing the initial season under its historical rules, then following seasons generated procedurally from outcomes — repeating indefinitely without loading new mod folders per year.**

Season **content** (squads, clubs, timeline scripts) comes from the **picked start mod** (e.g. 1888–89). Season **2+** is simulated, not pre-authored. See [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md).

This document translates that into testable acceptance criteria.

---

## 2. What v1.0 is NOT

To prevent scope creep, v1.0 explicitly **does not require**:

- Full FM feature parity (tactics, training, scouting, transfers market)
- 3D match engine or detailed match events
- All 35 clubs playable with full squads
- Multi-country leagues beyond England
- Multiplayer or modding tools
- Mobile/Android build
- **Youth regens, player retirements, or mortality** *(v1 uses static squads — see §4.7)*
- **Unemployed career start** *(v1.1)*

**In v1.0:** League **and FA Cup** full season simulation, infinite season rollover, employed start only.

---

## 3. v1.0 core user story

```
As a manager of a Football League club in 1888,
I start a new game, manage my club through the season,
see the league completed with a champion under correct rules,
and automatically (or via clear prompt) begin the next season
with that season's configured rules — repeating indefinitely.
```

**Alternate path (unemployed):** **Deferred to v1.1.** v1.0 requires starting **employed at a league club** to prove the season loop.

---

## 4. Acceptance criteria

### 4.1 New game & season start

| ID | Criterion | Current state |
|----|-----------|---------------|
| NG-1 | Player selects 1888–89 season and starts game | ✅ Works |
| NG-2 | Game date starts at season-configured date | ✅ 1 Apr 1888 |
| NG-3 | League is created on scripted date (17 Apr 1888) | ✅ Works |
| NG-4 | 12 clubs enter double round-robin (22 games each, 132 total) | ✅ Works |
| NG-5 | Mandatory fixture draw must be viewed before continuing | ✅ Works |
| NG-6 | League uses **1888 historical rules** (2-1-0, goal average) | ❌ Uses 3-1-0 modern |
| NG-7 | Season identity stored in save (`currentSeasonId`, index, or equivalent) | ❌ Missing |

### 4.2 In-season play

| ID | Criterion | Current state |
|----|-----------|---------------|
| PL-1 | Continue advances exactly one day per action | ⚠️ Double-click bug |
| PL-2 | All league matches on a date auto-simulate | ✅ Works |
| PL-3 | Standings update correctly from results | ✅ Works (wrong rules) |
| PL-4 | Player can view squad, schedule, league table, finances | ⚠️ Partial |
| PL-5 | Match results appear in inbox / history | ⚠️ History click broken |
| PL-6 | Weekly club expenses deduct including player wages | ✅ Works |
| PL-7 | Save and load restores identical game state | ⚠️ Job system broken on load |
| PL-8 | Friendly matches do not count toward league completion | ❌ `matchesPlayed` mixed |

### 4.3 Season completion

| ID | Criterion | Current state |
|----|-----------|---------------|
| SC-1 | Engine detects when all league fixtures are played | ⚠️ Wired in `AuthorityManager`; friendlies may break signal (RB-7) |
| SC-2 | Final standings frozen; champion determined | ⚠️ Wired; needs E2E proof |
| SC-3 | `CompetitionEdition` records champion, end date | ⚠️ Partial |
| SC-4 | Inbox/notification announces season end and champion | ⚠️ Partial |
| SC-5 | No duplicate fixtures generated after season ends | ❌ Latent bug |
| SC-6 | 1888 re-election (bottom 4) applied or scripted | ❌ Not implemented |
| SC-7 | Optional: mid-season rule change (Nov 1888 points) | ❌ Not implemented |

### 4.4 Season transition (season N → N+1)

| ID | Criterion | Current state |
|----|-----------|---------------|
| TR-1 | Club league stats reset (points, W/D/L, goals, league matches) | ❌ Missing |
| TR-2 | Season income/expenditure rolled or archived | ❌ Missing |
| TR-3 | Played matches archived; new fixture list generated | ⚠️ `generateNextSeason()` partial |
| TR-4 | `gameStartDate` / season window advances to next season | ❌ Missing |
| TR-5 | Next season rules from edition snapshot / in-save scripts (not new mod folder) | ❌ Missing |
| TR-6 | Same 12 clubs (minus re-election changes) continue | ❌ Missing |
| TR-7 | Player can continue playing without starting new game | ❌ Missing |

### 4.5 Perpetual loop (season after season forever)

| ID | Criterion | Current state |
|----|-----------|---------------|
| LP-1 | Season 1889–90 playable via **procedural rollover** (same save) | ⚠️ Partial code; no IT |
| LP-2 | Season 3+ supported **procedurally** (rules snapshot / scripts, not mod per year) | ❌ Missing proof |
| LP-3 | Script system supports in-season timeline events + rollover hooks | ⚠️ League/cup creation only |
| LP-4 | Rules can differ per season (points, tie-breakers, format) | ❌ Hardcoded defaults |
| LP-5 | Save game survives 3+ season transitions | ❌ Untested |
| LP-6 | No memory/state leak across seasons (IDs, messages, scripts) | ❌ Untested |

### 4.6 FA Cup (in v1.0)

| ID | Criterion | Current state |
|----|-----------|---------------|
| Cup-1 | FA Cup 1888–89 loaded and scheduled | ⚠️ Data exists; execution stub |
| Cup-2 | Cup draw uses mandatory inbox gate | ❌ |
| Cup-3 | Knockout rounds eliminate losers | ❌ |
| Cup-4 | Cup results do not break league completion signal | ❌ |
| Cup-5 | Cup winner on `CompetitionEdition` | ❌ |
| Cup-6 | League + cup completable same season | ❌ |

### 4.7 Player lifecycle (v1.0 — static world)

| ID | Criterion | Current state |
|----|-----------|---------------|
| PL-9 | No youth regen or intake pipeline | ✅ None implemented |
| PL-10 | No player retirement | ✅ None implemented |
| PL-11 | No player death / mortality | ✅ None implemented |
| PL-12 | `birthDate` fixed; age display may increase with calendar | ⚠️ Implicit only |
| PL-13 | Same player IDs persist across season rollovers | ❌ Rollover not implemented |
| PL-14 | Squad size stable unless season mod replaces players | ❌ Untested |

Policy detail: [V1_TESTING_AND_SIMULATION_PLAN.md §7](V1_TESTING_AND_SIMULATION_PLAN.md#7-player-lifecycle--static-world-v1-policy).

### 4.8 Automated testing (v1.0 gate)

| ID | Criterion | Current state |
|----|-----------|---------------|
| AT-1 | `GameTestHarness` headless API | ✅ Phase 0 |
| AT-2 | `MandatoryGateIT` — draw block + acknowledge | ✅ Phase 0 |
| AT-3 | `SeasonPlaythroughIT` — league complete + procedural rollover | ✅ |
| AT-4 | `LeagueAndCupIT` — full season both competitions | ✅ |
| AT-5 | `MultiSeasonSoakIT` — ≥3 procedural seasons without deadlock | ✅ **PR gate** |
| AT-5b | `LongRunSoakIT` — **60 seasons** (north star; nightly `-Psoak`) | ✅ (~10h runtime) |
| AT-5c | `SaveLoadIT` — mid-season + post-rollover JSON roundtrip | ✅ |
| AT-6 | CI fails on test failure | ✅ Phase 0 (GHA + Jenkins) |

Full plan: [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md).

---

## 5. Rules configuration model (required design)

For “configured following season rules” without hardcoding 60 future mods:

### Primary — Start season mod + edition snapshot

```
mods/seasons/18/competition_rules.txt   # Base rules for career start (1888–89)
```

Each **`CompetitionEdition`** created at rollover carries a **rules snapshot** (copy of active rules at transition). Amendments apply via scripts or authority logic — not by loading `mods/seasons/19/`.

### FM-style — In-season timeline scripts (same start mod)

Extend `BasicScript` on the **picked start season**:

- `1000` LEAGUE_CREATION (exists)
- `1001` CUP_CREATION (exists)
- `1002` RULES_AMENDMENT — mid-career rule change (e.g. Nov 1888 points)
- `1003` TIMELINE_EVENT — optional club/rule events dated in running calendar

Scripts fire on `executionTime` during the save; they are **not** “load next year’s mod folder.”

### Procedural seasons 2+

`SeasonManager.transitionToNextSeason()` copies participants, snapshots rules, generates fixtures — **no mod load**.

**Canonical reference:** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)

**Minimum for v1.0:** Start-mod rules file + edition snapshot + `SeasonPlaythroughIT` / `MultiSeasonSoakIT` proof.

---

## 6. Minimum content for v1.0

| Content | Required for v1.0? |
|---------|---------------------|
| Season 1888–89 (id 18) | ✅ Yes |
| Season 1889–90 (id 19) | ✅ Yes — proves loop |
| Season 1890–91 (id 20) | ⚠️ Strongly recommended — proves "forever" |
| 12 league clubs with squads | ✅ Yes |
| **FA Cup 1888–89 full knockout** | ✅ **Yes — in v1** |
| 23 non-league clubs | ❌ Nice to have |

---

## 7. Technical deliverables for v1.0

| Deliverable | Description |
|-------------|-------------|
| `SeasonManager` | Owns season state, completion, transition |
| `SaveGame` extensions | `currentSeasonId`, `seasonFinalized`, `seasonIndex` |
| Season completion in `continueGame()` | Calls `isLeagueComplete()`, triggers transition |
| Fixture regeneration guard | Do not regenerate if season finalized or all league matches played |
| Rules loader | Read rules from season mod or edition |
| Season 19 mod folder | Clubs, scripts, rules for 1889–90 |
| Integration test | New game → sim full season → verify rollover → play season 2 |
| **FA Cup integration test** | League + cup complete in one season headless |
| **Multi-season soak test** | ≥3 seasons without mandatory-gate deadlock |
| Bug fixes from registry | All CRITICAL items in BUGS_AND_BLOCKERS_REGISTRY.md |

---

## 8. Suggested release milestones

| Milestone | Deliverable | Exit criteria |
|-----------|-------------|---------------|
| **M0** | Branch merge + stabilize | `mvn test` green, critical bugs fixed |
| **M1** | One correct 1888 season | Historical rules, completion detected, champion shown |
| **M2** | Season rollover | Stats reset, season 2 starts from save |
| **M3** | Rules per season | 1889–90 uses different config provably |
| **M4** | v1.0 tag | 3-season automated test passes, manual QA checklist |

---

## 9. Manual QA checklist (v1.0 sign-off)

- [ ] New game → employed at Preston → league created 17 Apr 1888
- [ ] View mandatory draw → continue blocked until done
- [ ] Continue 200+ days → all 22 league games complete
- [ ] Standings show 2-1-0 points (not 3-1-0)
- [ ] Goal-average tie-break visible if tied on points
- [ ] Season end message + champion announced
- [ ] No second fixture list appears
- [ ] Season 1889–90 starts with correct start date
- [ ] Season 1889–90 rules match config file
- [ ] Save mid-season → load → state identical
- [ ] Save after season 1 → load → season 2 still works
- [ ] Play through 3 seasons without new game
- [ ] FA Cup knockout completes; does not block league completion
- [ ] No soft-lock on mandatory draw (league or cup)

---

## 10. PO decisions log

| # | Decision | **Resolved (2025-06-25)** |
|---|----------|---------------------------|
| 1 | Unemployed start in v1.0? | **No — v1.1** |
| 2 | FA Cup in v1.0? | **Yes — full knockout with league** |
| 3 | Nov 1888 mid-season points change? | **Defer** — single 2-1-0 for 1888–89 first |
| 4 | Re-election bottom 4? | **Script/message only** — full sim defer |
| 5 | Player regens / retirement / death? | **None in v1** — static squads; aging display-only |
| 6 | Merge strategy | **Open** — feature branch → develop → main TBD |
| 7 | Release tag | **Open** — `v1.0.0` when harness + QA green |

---

## 11. Summary

**v1.0 = a closed loop:** start season → play **league and FA Cup** under that season's rules → season ends → next season starts → repeat **forever**, with **automated tests** proving no mandatory-gate deadlock over multi-season runs.

Current codebase is approximately **30–40% of the way** to this definition. The missing 60% is concentrated in **season lifecycle**, **FA Cup execution**, **rules configuration**, **test harness**, and **stabilization** — not in building more UI screens.
