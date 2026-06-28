# Futtoboru — Project Master Analysis

> **Note (2026-06-27):** Percent-complete figures in §1 and season-lifecycle gaps in §6 are **partially stale** (procedural loop + 59-test gate landed since this audit). For current architecture and LibGDX/UI patterns use **[ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md)**.

**Document type:** Project management / technical audit  
**Analysis date:** 2025-06-25  
**Codebase analyzed:** `feature/competitions-finances-seasons` (most complete branch)  
**Version in branch:** `0.4.0-SNAPSHOT`  
**Purpose:** Full inventory of what exists, what is broken, and what is required for a first release that supports **season-after-season play with correct per-season rules**.

**Related documents:**
- [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) — **start here** for all docs
- [STAKEHOLDERS_AND_DEPARTMENTS.md](STAKEHOLDERS_AND_DEPARTMENTS.md) — org model, experts, sign-off gates
- [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) — scope and acceptance criteria for first release
- [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) — core dept deep dive
- [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md) — core dept deep dive
- [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) — core dept deep dive
- [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) — CI / harness gate
- [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) — every system/screen with status
- [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) — consolidated bug list with severity
- [PM_STAKEHOLDER_STATUS_BRIEF.md](PM_STAKEHOLDER_STATUS_BRIEF.md) — PM/stakeholder status brief (Jun 2025)
- [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) — CM1–CM03/04 feature history
- [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM2_REFERENCE.md](CM2_REFERENCE.md) · [CM3_REFERENCE.md](CM3_REFERENCE.md) — legacy deep dives
- [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) · [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md) — CM4 systems + community
- [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md) — manual PDF sources
- [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) — legacy CM target vs current codebase (~12% CM4 parity)

---

## 1. Executive summary

Futtoboru is a Java/LibGDX open-source Football Manager-style game focused on **historical football from the 1888–89 season onward**. The project has two distinct development eras:

| Era | Period | Branch | State |
|-----|--------|--------|-------|
| Manual | 2021 – Jul 2023 | `main` (v0.2.0) | Solid foundation: data loaders, UI skeleton, FA Cup structure, scheduling |
| AI-assisted | Dec 14–17, 2025 | `feature/competitions-finances-seasons` | Large expansion (~42k lines, 131 commits): match sim, finances, contracts, jobs, inbox |

**Public GitHub (`main`) does not reflect the AI work.** All recent progress lives on `feature/competitions-finances-seasons`. `develop` has planning docs only (+6 commits, no feature code).

### Can you complete a season today?

**No — not in a meaningful, repeatable way.**

You can advance time, simulate most of a 12-team double round-robin, and view standings — but the game has **no season-end detection, no champion ceremony, no rollover, and no second season**. Several bugs corrupt state during long play (fixture regeneration if AuthorityManager is fixed, wrong historical rules, stale manager references after load).

### Can you play season after season forever?

**Not implemented.** This is the primary gap for your stated first release goal. See [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md).

### Overall maturity estimate

| Layer | Maturity |
|-------|----------|
| Architecture & data models | **60%** — good separation, extensible |
| 1888–89 content (clubs, players, stadiums) | **50%** — 12/12 league squads, 35 clubs total, cups partial |
| Core gameplay loop (day → match → result) | **45%** — works for league, thin for cups |
| Competition rules (historical accuracy) | **25%** — engine exists, 1888 rules not applied |
| Season lifecycle (start → end → next) | **5%** — almost entirely missing |
| UI completeness | **40%** — many screens partial or stub |
| Save/load reliability | **35%** — works in happy path, critical gaps on load |
| Test coverage | **20%** — 12 test classes, no season/completion tests |

---

## 2. Repository & branch map

```
main (public, v0.2.0, Jul 2023)
  └── develop (+docs, +null checks, no AI code)
        └── feature/competitions-finances-seasons (+ALL AI work, v0.4.0-SNAPSHOT)
```

| Branch | Commits ahead of main | Key contents |
|--------|----------------------|--------------|
| `main` | — | Last release 0.2.0 |
| `develop` | 42 | ROADMAP, TASKS, GitFlow docs, MainGameScreen null checks |
| `feature/competitions-finances-seasons` | 169 | Full AI expansion + 50+ analysis markdown files |

**Recommendation before any release work:** merge or rebase `feature/competitions-finances-seasons` → `develop`, not directly to `main`.

---

## 3. Technology stack

| Component | Version / choice |
|-----------|------------------|
| Language | Java 17 |
| Game framework | LibGDX 1.12.0 |
| UI | VisUI 1.5.2 |
| Build | Maven (multi-module: `futtoboru-core`, `futtoboru-desktop`) |
| Persistence | LibGDX JSON → `~/Documents/RndModGames/Futtuboru/savegames/` |
| Tests | JUnit 5, JaCoCo |

---

## 4. Architecture overview

```
futtoboru/
├── futtoboru-core/
│   ├── data/           — Domain models (Club, Player, Match, League, CompetitionRules, etc.)
│   ├── engine/         — Game loop, simulation, finances, jobs, messages, scripts
│   ├── screens/        — LibGDX screens (menu, new game, main game)
│   ├── tables/         — In-game UI panels (squad, finances, competitions, jobs, etc.)
│   ├── menu/           — Sidebar navigation, top bar (Continue)
│   └── system/         — DatabaseLoader, SaveGame, loaders, generators
├── futtoboru-desktop/  — Desktop launcher JAR
└── resources/mods/     — External data (seasons, clubs, players, competitions)
```

### Design strengths

- **Data-driven seasons** via `mods/seasons/{id}/` folders
- **Script system** for timed historical events (`BasicScript` + `ScriptsManager`)
- **Separation** of static DB (`DatabaseLoader`) vs mutable save state (`SaveGame`)
- **CompetitionRules** model supports points, tie-breakers, match format per league

### Design weaknesses

- **Managers cache stale `SaveGame`** — created at app startup before a game exists
- **Magic screen IDs** instead of enums
- **Heavy `System.out.println` debug** left in production paths
- **50+ overlapping analysis markdown files** from AI sessions — no single source of truth until this audit

---

## 5. The daily game loop (what actually runs)

**Entry:** `MainGameMenuTable.continueGame()` → `FuttoboruGameEngine.continueGame()`

### Pre-conditions (blocks Continue)

1. Unread **mandatory draw message** → forces Inbox → League Draw screen
2. Employed player has match **today** → forces Match Preview (manual sim path)

### Pipeline (each Continue = +1 day)

| Step | Component | Notes |
|------|-----------|-------|
| 1 | `MessageManager.deliverScheduledMessages` | Inbox delivery |
| 2 | `simulateMatchesForDate` | Auto-sim **all clubs'** matches today |
| 3 | `gameDate += 1 day` | No season boundary check |
| 4 | `ScriptsManager.checkGameScripts` | Only `LEAGUE_CREATION_SCRIPT` implemented |
| 5 | `AuthorityManager.checkCompetitionsSchedule` | **Broken:** `currentGame` never refreshed → no-op |
| 6 | `JobManager.updateJobOpenings` | Jobs expire/renew daily |
| 7 | Messages for new date | |
| 8 | Daily attribute changes | Placeholder until training system |
| 9 | Weekly snapshots (every 7 days) | Attribute history |
| 10 | Weekly expenses (every 7 days) | `ExpenseCalculator` + wage deduction |
| 11 | Per-club `MatchScheduler` | Friendly proposals, ticket sales |
| 12 | UI refresh | |

**Not in daily loop:** season completion, cup rounds, season rollover, `CompetitionScheduler.initializeCompetitions()`.

---

## 6. Season lifecycle — critical gap analysis

This section is the heart of your first-release requirement.

### 6.1 What exists

| Capability | Status | Location |
|------------|--------|----------|
| Season data file (1888–89 only) | ✅ | `mods/seasons.txt`, `mods/seasons/18/` |
| New game season selection | ✅ | `NewGameSeasonScreen` |
| Game start date from season | ✅ | `NewGameOverviewScreen` |
| Season scripts copied to save | ✅ | `SaveGame.gameScripts` |
| League creation script (17 Apr 1888) | ✅ | `ScriptsLoader` → `ScriptsManager.createLeague()` |
| Fixture generation (double RR, 132 matches) | ✅ | `LeagueFixtureGenerator` |
| Match auto-simulation | ✅ | `FuttoboruGameEngine.simulateMatchesForDate` |
| Standings calculation | ✅ | `LeagueStandingsManager.calculateStandings()` |
| League completion detection | ⚠️ **Exists but unwired** | `LeagueStandingsManager.isLeagueComplete()` |
| Champion detection | ⚠️ **Exists but unwired** | `LeagueStandingsManager.getChampion()` |

### 6.2 What is missing (blockers for season-after-season)

| # | Gap | Impact |
|---|-----|--------|
| 1 | **No `SeasonManager` / season state in `SaveGame`** | Cannot track which season you're in |
| 2 | **No season-end hook in `continueGame()`** | Game runs forever with no closure |
| 3 | **No season rollover** | Club stats, finances, fixtures never reset for new season |
| 4 | **No second season content** | Only season id `18` in `seasons.txt` |
| 5 | **No next-season script types** | Only `LEAGUE_CREATION_SCRIPT` (1000) exists |
| 6 | **`CompetitionEdition` never finalized** | `championsId`, `endDate` never set by engine |
| 7 | **Cup lifecycle empty** | FA Cup data loads but doesn't play through rounds |
| 8 | **Fixture regeneration bug (latent)** | When `AuthorityManager` is fixed, empty `scheduledMatches` after all matches played would trigger **duplicate full fixture set** on same calendar window |
| 9 | **`isLeagueComplete()` counts all matches** | Friendlies inflate `matchesPlayed` — wrong completion signal |
| 10 | **No re-election / AGM (1888 rule)** | Documented in comments only |

### 6.3 Season calendar accuracy (1888–89)

| Intended (historical) | Current runtime |
|----------------------|-----------------|
| Season start ~Sep 1888 | Game starts **1 Apr 1888** |
| League founded 17 Apr 1888 | Script fires 17 Apr ✅ |
| Fixtures Sep 1888 – Apr 1889 | Window = `gameStartDate` + 9 months ≈ **Jan 1889** |
| 2 pts win / 1 draw (from Nov 1888) | **3-1-0 from day one** (modern default) |
| Goal average tie-break | Goal difference used (modern default) |

---

## 7. Competition rules — critical gap analysis

### Rules engine (works)

- `CompetitionRules` model: points, tie-breakers, match format
- `MatchSimulator` awards points from league rules
- `LeagueStandingsManager` sorts by rule chain
- UI displays rules on `LeagueDetailScreenTable`

### Rules configuration (broken for historical play)

| Issue | Detail |
|-------|--------|
| `createHistoricalRules()` never called | 2-1-0 + goal average exist but unused |
| `ScriptsLoader` sets `LEAGUE_RULES = null` | Script parameter ignored |
| No rules in mod TXT files | Cannot configure per season without code |
| No mid-season rule change | Nov 1888 points introduction not modelled |
| `LeagueFixtureGenerator` ignores `matchFormat` | Always double round-robin |
| Cups have no `CompetitionRules` | Knockout/replay rules absent |

**For your release goal:** each season needs rules defined in data (or scripts) and applied at league/competition creation — not hardcoded defaults.

---

## 8. Content inventory (1888–89 season)

| Asset | Count | Notes |
|-------|-------|-------|
| Clubs in `clubs.txt` | 35 | 12 league + 23 cup/other |
| League player files | 12/12 | One per founding club |
| Stadium files | 12+ | League clubs covered |
| Competitions | FA Cup edition data | Participants load; execution stub |
| Seasons defined | **1** | id `18` only — no 1889–90 |
| Professions | 80 | Amateur era job system |
| Nationality/region modifiers | ✅ | For attribute generation |

---

## 9. Existing documentation landscape

The AI era produced **50+ markdown files** (ANALYSIS_*, DESIGN_*, ROADMAP_*, etc.). These are valuable but:

- Overlap heavily (same topics, different versions)
- Some are **outdated vs code** (e.g. MVP roadmap says match sim "0%" after `MatchSimulator` was added)
- No unified bug registry until [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md)
- Mix of "Analyst: AI Assistant" and human intent

**This audit supersedes scattered docs for decision-making.** Keep historical analysis files as reference; use the four new registry documents as the working set.

---

## 10. Test coverage

| Test class | Covers |
|------------|--------|
| `DatabaseLoaderTests` | Static DB loading |
| `CompetitionsLoaderTests` | FA Cup load, draw, scheduling walk |
| `ScriptManagerTests` | League creation after ~30 days |
| `CompetitionSchedulerTests` | Draw skeleton |
| `SeasonLeaguesAndTeamsTests` | 1 league, 12 teams in mod |
| `PlayersLoaderTests`, `StadiumsLoaderTests`, etc. | Loaders |
| **None** | Season completion, rollover, multi-season, save/load integration, match sim accuracy |

---

## 11. Decision framework — three paths

### Path A: Minimum viable "one season playable" (4–6 weeks)

Fix critical bugs, apply 1888 rules, wire season completion for league only, **stop before** multi-season. Good milestone, **not** your stated goal.

### Path B: First release = season loop forever (8–12 weeks) ⭐ Recommended target

Implement `SeasonManager`, season-end detection, rollover, season 2 content, per-season rules from data/scripts, fix save/load and AuthorityManager. Cups optional for v1 if league loop is solid.

### Path C: Full FM-style v1 (6+ months)

Everything in Path B plus cups, tactics, training, transfers, full unemployed career, all screens.

---

## 12. Recommended implementation order (when you decide to build)

### Phase 0 — Stabilize (1 week)
- Fix stale `SaveGame` in all managers
- Fix load → re-init job system
- Fix Continue double-click
- Remove debug logging
- Assign league IDs at creation

### Phase 1 — One correct season (2–3 weeks)
- Apply `createHistoricalRules()` for 1888
- Separate league `matchesPlayed` from total
- Wire `isLeagueComplete()` in daily loop
- Season-end messages + champion + edition finalization
- Block fixture regeneration after season complete

### Phase 2 — Season rollover (2–3 weeks)
- `SeasonManager` + `currentSeasonId` in `SaveGame`
- Rollover: reset stats, archive matches, bump dates
- Season 1889–90 mod folder + scripts
- Per-season rules from mod/script config

### Phase 3 — Perpetual loop (2 weeks)
- Script types: `SEASON_END`, `NEXT_SEASON_START`, `RULES_AMENDMENT`
- Automated next-season fixture generation
- Save/load validation + version field
- Integration tests for 2+ seasons

### Phase 4 — Polish for release (ongoing)
- Cup pipeline OR explicitly defer
- Match history → result navigation
- Home screen, top menu load/settings
- CI with `mvn test`

---

## 13. Key file reference

| Concern | Primary files |
|---------|---------------|
| Daily loop | `engine/FuttoboruGameEngine.java` |
| Season scripts | `engine/ScriptsManager.java`, `system/loaders/ScriptsLoader.java` |
| Authority / fixtures | `engine/AuthorityManager.java`, `engine/temporal/LeagueFixtureGenerator.java` |
| Rules | `data/CompetitionRules.java` |
| Standings / completion | `engine/simulation/LeagueStandingsManager.java` |
| Match sim | `engine/simulation/MatchSimulator.java` |
| Save state | `system/SaveGame.java`, `system/SaveLoadSystem.java` |
| Season data | `resources/mods/seasons.txt`, `resources/mods/seasons/18/` |
| UI router | `menu/MainMenuManager.java` |
| Continue button | `menu/topmenu/MainGameMenuTable.java` |

---

## 14. Conclusion

Futtoboru has a **credible foundation** and a **large AI-assisted prototype** that added many systems quickly — but **the core promise of historical season-after-season management is not yet implemented**. The gap is not more features; it is **season lifecycle wiring**, **rules configuration**, and **stabilization** of managers and save/load.

Use [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) for exact acceptance criteria, [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) for feature-level status, and [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) for the fix backlog.

**Next step for the team:** review these four documents, choose Path B (or adjust scope), then decide merge strategy for `feature/competitions-finances-seasons`.
