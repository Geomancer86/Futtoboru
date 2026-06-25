# Futtoboru — Release v1.0 Definition

**Document type:** Product scope / acceptance criteria  
**Analysis date:** 2025-06-25  
**Status:** Draft for team decision — analysis only, no implementation committed

---

## 1. Release vision (your stated goal)

> **First release must allow playing the initial season under its historical rules, then following seasons under their configured rules — forever, season after season.**

This document translates that into testable acceptance criteria.

---

## 2. What v1.0 is NOT

To prevent scope creep, v1.0 explicitly **does not require**:

- Full FM feature parity (tactics, training, scouting, transfers market)
- 3D match engine or detailed match events
- All 35 clubs playable with full squads
- FA Cup full knockout simulation *(may be Phase 1.1 if league loop ships first)*
- Multi-country leagues beyond England
- Multiplayer or modding tools
- Mobile/Android build

These can follow v1.0 if the season loop is solid.

---

## 3. v1.0 core user story

```
As a manager of a Football League club in 1888,
I start a new game, manage my club through the season,
see the league completed with a champion under correct rules,
and automatically (or via clear prompt) begin the next season
with that season's configured rules — repeating indefinitely.
```

**Alternate path (unemployed):** v1.0 may require starting **employed at a league club** for the season loop. Unemployed → job → first season can be v1.1 if it delays the core loop.

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
| SC-1 | Engine detects when all league fixtures are played | ❌ Not wired |
| SC-2 | Final standings frozen; champion determined | ❌ Not wired |
| SC-3 | `CompetitionEdition` records champion, end date | ❌ Not wired |
| SC-4 | Inbox/notification announces season end and champion | ❌ Not wired |
| SC-5 | No duplicate fixtures generated after season ends | ❌ Latent bug |
| SC-6 | 1888 re-election (bottom 4) applied or scripted | ❌ Not implemented |
| SC-7 | Optional: mid-season rule change (Nov 1888 points) | ❌ Not implemented |

### 4.4 Season transition (season N → N+1)

| ID | Criterion | Current state |
|----|-----------|---------------|
| TR-1 | Club league stats reset (points, W/D/L, goals, league matches) | ❌ Missing |
| TR-2 | Season income/expenditure rolled or archived | ❌ Missing |
| TR-3 | Played matches archived; new fixture list generated | ❌ Missing |
| TR-4 | `gameStartDate` / season window advances to next season | ❌ Missing |
| TR-5 | Next season rules loaded from configuration | ❌ Missing |
| TR-6 | Same 12 clubs (minus re-election changes) continue | ❌ Missing |
| TR-7 | Player can continue playing without starting new game | ❌ Missing |

### 4.5 Perpetual loop (season after season forever)

| ID | Criterion | Current state |
|----|-----------|---------------|
| LP-1 | Season 1889–90 playable with its own rules config | ❌ No season 2 data |
| LP-2 | Season 1890–91+ supported via data (not hardcoded) | ❌ Missing |
| LP-3 | Script system supports season-end and season-start events | ❌ One script type only |
| LP-4 | Rules can differ per season (points, tie-breakers, format) | ❌ Hardcoded defaults |
| LP-5 | Save game survives 3+ season transitions | ❌ Untested |
| LP-6 | No memory/state leak across seasons (IDs, messages, scripts) | ❌ Untested |

---

## 5. Rules configuration model (required design)

For "configured following season rules," v1.0 needs a **data-driven rules source**. Recommended approach:

### Option A — Per-season file (recommended)

```
mods/seasons/18/competition_rules.txt   # 1888-89
mods/seasons/19/competition_rules.txt   # 1889-90
```

Fields: points for win/draw/loss, tie-breaker order, match format, optional mid-season amendments with effective date.

### Option B — Script-driven amendments

Extend `BasicScript` with types:
- `1000` LEAGUE_CREATION (exists)
- `1001` RULES_AMENDMENT (new)
- `1002` SEASON_END (new)
- `1003` NEXT_SEASON_START (new)

Scripts reference rule sets or inline values.

### Option C — Per-edition on `CompetitionEdition`

Each year's edition carries its own `CompetitionRules` snapshot.

**Minimum for v1.0:** Option A or C plus script hooks for timed amendments (1888 Nov points change).

---

## 6. Minimum content for v1.0

| Content | Required for v1.0? |
|---------|---------------------|
| Season 1888–89 (id 18) | ✅ Yes |
| Season 1889–90 (id 19) | ✅ Yes — proves loop |
| Season 1890–91 (id 20) | ⚠️ Strongly recommended — proves "forever" |
| 12 league clubs with squads | ✅ Yes |
| FA Cup full simulation | ❌ Defer to v1.1 unless trivial |
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

---

## 10. Open decisions for the team

1. **Unemployed start in v1.0?** Yes / No / v1.1
2. **FA Cup in v1.0?** Yes / No / simplified
3. **Nov 1888 mid-season points change?** Required / simplified / defer
4. **Re-election bottom 4?** Script only / full simulation / defer
5. **Merge strategy:** feature branch → develop → main, or squash?
6. **Release tag:** v1.0.0 vs v0.5.0 incremental?

Document decisions here when made.

---

## 11. Summary

**v1.0 = a closed loop:** start season → play under that season's rules → season ends → next season starts under new rules → repeat.

Current codebase is approximately **30–40% of the way** to this definition. The missing 60% is concentrated in **season lifecycle**, **rules configuration**, and **stabilization** — not in building more UI screens.
