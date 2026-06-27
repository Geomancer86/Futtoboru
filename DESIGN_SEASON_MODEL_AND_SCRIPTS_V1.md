# Design — Season Model, Scripts & Multi-Season Loop (v1)

**Document type:** Product / architecture design (PO-aligned)  
**Date:** 2026-06-26  
**Status:** **Active** — supersedes assumptions about “season 19 mod folders” for v1  
**Audience:** PO, PM, Seasons & Rules, World & Data, Platform & QA

**Related:** [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) · [PM_60_SEASON_GOAL_STATUS.md](PM_60_SEASON_GOAL_STATUS.md) · [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)

---

## 1. PO decision (locked 2026-06-26)

| Topic | v1 decision |
|-------|-------------|
| **Future season mod folders** | **Not required for v1.** We do **not** plan to ship `mods/seasons/19/`, `20/`, … through `77/` as hardcoded content. |
| **How season 2+ works** | **Procedural rollover** from game outcomes: same save, new `CompetitionEdition`, regenerated fixtures, participants carried from previous edition (plus script-driven changes). |
| **What is modded / scripted** | The **picked start season** mod only (e.g. 1888–89 / id `18`): squads, clubs, stadiums, **and timeline scripts that fire during that calendar window**. |
| **FM-style timeline events** | Deaths, injuries, new clubs, rule changes, cup format tweaks, etc. are **scripts dated inside the running season window**, not separate “future year” mods. |
| **Additional start dates (later)** | Post-v1: optional **new start season** mods (e.g. “begin career in 1905”) — still not one mod per simulated future year. |
| **60-season north star** | Prove the **procedural loop** for **60 in-game years** via automated tests — not by authoring 60 seasons of static data. |
| **Static player world** | Unchanged: no regens, retirement, or death simulation in v1 engine (timeline “death” scripts may be deferred). |

---

## 2. Conceptual model

### 2.1 Two different meanings of “season”

| Term | Meaning | v1 source |
|------|---------|-----------|
| **Start season (mod)** | Historical **entry point** the player selects at new game | `mods/seasons/18/` + `seasons.txt` row |
| **Simulated competition year** | One league/cup cycle inside a save (`CompetitionEdition` “1888–89”, “1889–90”, …) | **Engine-generated** after year 1 |

Only the **first** uses a full content mod. Years 2…N reuse save state and procedural generation.

### 2.2 FM-style scripting (in-season timeline)

Scripts attached to the **start season mod** run on **`executionTime` ≤ game date** while that save is active:

```
1888-04-17  LEAGUE_CREATION_SCRIPT     → create league, fixtures, mandatory draw
1888-??-??  CUP_CREATION_SCRIPT        → FA Cup participants, draw schedule
1888-11-??  RULES_AMENDMENT (future)   → e.g. points system change mid-era
1888-??-??  CLUB_EVENT (future)        → e.g. new member club (if modeled)
```

These are **not** “load season 19 folder when calendar hits 1889”. They are **events on the timeline of the career**, authored against the **start mod’s calendar** or generic script packs.

For **simulated year 1890–91**, rule changes should come from:

1. **Runtime script** scheduled when previous season ends or on a calendar date in save time, or  
2. **Default procedural rules** copied from previous edition with optional amendments, or  
3. **Authority / edition snapshot** (`CompetitionRules` on `CompetitionEdition`)

—not from loading a new mod directory per year.

### 2.3 Procedural rollover (season N → N+1)

Target behaviour (aligns with partial code in `AuthorityManager.generateNextSeason()`):

```
Season N completes (league + cup per v1 scope)
  → finalize edition (champion, runner-up, end date)
  → archive / reset per-club league counters
  → create CompetitionEdition for N+1 (name, dates, participants)
  → generate fixtures (LeagueFixtureGenerator)
  → optional mandatory fixture draw message
  → increment save season index / in-game year
  → continue daily loop
```

**No mod load step** for N+1. Participants for N+1 must reflect **current save membership** after timeline scripts (promotion, new clubs) — not a frozen copy forever ([DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)).

### 2.4 In-play world evolution

Entities that appear **after** the start date merge into **`SaveGame`** on their historical date:

- **Dormant registry** — authored in start mod, `first_active_date` / `foundation_year`  
- **Timeline scripts** — types 1002–1007  
- **Master DB projection** — north star at scale  

League/cup scripts (1000/1001) already demonstrate registry → save incorporation; rollover and new entity types are not wired yet. See [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md).

---

## 3. What exists in code today (2026-06-26)

| Component | Status | Notes |
|-----------|--------|-------|
| Start season mod `18` | ✅ | Clubs, players, scripts in `ScriptsLoader` |
| In-season scripts | ⚠️ | Types `1000` league, `1001` cup only |
| `ScriptsManager.checkGameScripts()` | ✅ | Runs on `continueGame()` |
| `checkLeagueCompletion()` | ✅ | Calls `isLeagueComplete()` → awards |
| `generateNextSeason()` | ⚠️ **Partial** | New edition + fixtures; weak stat reset; no `SeasonManager` |
| `SaveGame.currentSeasonId` | ❌ | No season identity on save |
| Timeline script types (amendments, events) | ❌ | Not implemented |
| Rules from mod file | ❌ | `LEAGUE_RULES` null; defaults 3-1-0 |
| Procedural loop tests | ❌ | No `SeasonPlaythroughIT` / soak |

---

## 4. Required architecture (v1)

### 4.1 `SeasonManager` (new)

Single owner for:

- `currentSimulatedSeasonIndex` / `inGameSeasonLabel` on `SaveGame`
- `isSeasonComplete(save)` — league + cup per v1 gate
- `finalizeSeason(save)` — champions, editions, messages
- `transitionToNextSeason(save)` — calls rollover pipeline (not mod loader)
- `applyScheduledScripts(save, date)` — delegates to `ScriptsManager` for dated events

`AuthorityManager` should **call** `SeasonManager`, not duplicate rollover logic long-term.

### 4.2 Script system extensions

| Type | ID | Purpose |
|------|-----|---------|
| `LEAGUE_CREATION_SCRIPT` | 1000 | ✅ Exists |
| `CUP_CREATION_SCRIPT` | 1001 | ✅ Exists |
| `RULES_AMENDMENT_SCRIPT` | 1002 | **New** — change points/tie-break mid-career |
| `CLUB_ACTIVATION_SCRIPT` | 1003 | **New** — club + squad → `SaveGame.allClubs` |
| `LEAGUE_MEMBERSHIP_SCRIPT` | 1004 | **New** — add/remove league participant |
| `COMPETITION_CREATION_SCRIPT` | 1005 | **New** — new cup/league/division |
| `COUNTRY_ACTIVATION_SCRIPT` | 1006 | **New** — nation playable in save |
| `PERSON_INCORPORATION_SCRIPT` | 1007 | **New** — named player enters save |

Scripts are **copied into `SaveGame.gameScripts` at new game** (with `isExecuted` per save). Optional: append dynamically generated scripts at rollover (e.g. “re-election result”) without a mod folder.

### 4.3 Rules configuration (revised)

**v1 minimum:**

- **Base rules** loaded from **start season mod** (`competition_rules.txt` under `mods/seasons/18/`)
- **Amendments** via dated scripts during the save (1888 November points change = script, not season 19 mod)
- Each **`CompetitionEdition`** stores a **rules snapshot** at creation (procedural year 2 copies year 1 unless amended)

**Explicitly out of scope for v1:** one rules file per future simulated year in separate mod folders.

### 4.4 Rollover checklist (must be implemented + tested)

| Step | Requirement |
|------|-------------|
| R-1 | League-only match counting for completion (exclude friendlies) |
| R-2 | Reset `matchesPlayed`, points, W/D/L, goals for league scope |
| R-3 | Move played league matches to history / clear active schedule |
| R-4 | New fixtures only for new edition |
| R-5 | Cup new edition + draw (procedural) |
| R-6 | Financial season counters archived |
| R-7 | No duplicate fixture regen (RB-6) |
| R-8 | Mandatory draw gate for new season fixtures if UI requires it |
| R-9 | Save/load preserves season index + active edition |

---

## 5. Testing implications

See [PM_60_SEASON_GOAL_STATUS.md](PM_60_SEASON_GOAL_STATUS.md) for the full matrix.

| Test | Proves |
|------|--------|
| `SeasonPlaythroughIT` | One full simulated year: start mod → completion → **one** procedural rollover |
| `MultiSeasonSoakIT` (≥3) | Loop stable; champions recorded per edition |
| `LongRunSoakIT` (60) | **60-season goal** — no deadlock, memory, ID leaks |
| `SeasonManagerTest` | Rollover state machine unit tests |
| `RulesAmendmentScriptTest` | Mid-career rule change applies to standings |

**Success criterion for 60-season goal:** headless run lists **60 league champions** (and cup if in scope) from **procedural editions**, not from 60 mod loads.

---

## 6. Documentation drift to avoid

| Old assumption | Correct v1 stance |
|----------------|-------------------|
| “Ship `mods/seasons/19/` for v1 proof” | **Wrong** — proof is procedural rollover + tests |
| “LP-2 = season 1890+ data files” | **Wrong** — LP-2 = procedural seasons with rules snapshot / scripts |
| “Load next season mod on rollover” | **Wrong for v1** — optional only for **new game start date** later |
| “Script every future year by hand” | **Wrong** — script **timeline** in start mod + rare dynamic hooks |

---

## 7. Cross-references to update

Documents updated in this pass:

- [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) — §4.4–4.5, §5  
- [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) — Phases 2–4, soak targets  
- [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) — Phase 2 wording  
- [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) — start mod vs procedural  
- [PM_STAKEHOLDER_STATUS_BRIEF.md](PM_STAKEHOLDER_STATUS_BRIEF.md)  
- [PM_V1_POST_MERGE_REANALYSIS.md](PM_V1_POST_MERGE_REANALYSIS.md)  
- [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) — RB-2 description  

---

## 8. Open questions (PO)

| # | Question | Default if silent |
|---|----------|-------------------|
| 1 | Cup required in “season complete” gate for rollover? | Yes for v1 (FA Cup in scope) |
| 2 | First procedural season keeps same 12 clubs always? | Yes until re-election script |
| 3 | `LongRunSoakIT` at 60 seasons in CI or nightly only? | Nightly; PR gate at 3 seasons |
| 4 | Mid-season 1888 rules script in v1 or defer? | Defer unless standings wrong without it |

---

*This document is the canonical reference for “what is a season” in Futtoboru v1.*
