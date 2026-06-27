# Futtoboru — Bugs & Blockers Registry

**Document type:** Issue backlog  
**Analysis date:** 2025-06-25 · **Automation note:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Severity:** 🔴 Critical | 🟠 High | 🟡 Medium | 🟢 Low

> **RB-1, RB-2, RB-4, RB-5, RB-7, RB-8** are **resolved in code** and covered by integration tests. Rows kept for traceability — see [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md) §3.1 for remaining v1 blockers (mostly UI: C-1, C-3, H-3, H-9).

Consolidates `ISSUES_PRIORITY_LIST.md`, code audit, and subagent analysis into one authoritative list.

---

## Release blockers (must fix for v1.0)

These prevent the stated goal: **play season under correct rules → complete → next season → repeat**.

| ID | Severity | Issue | Location | Notes |
|----|----------|-------|----------|-------|
| **RB-1** | 🔴 | **No season completion detection** | `FuttoboruGameEngine.continueGame()` | `LeagueStandingsManager.isLeagueComplete()` never called |
| **RB-2** | 🔴 | **No proven multi-season rollover** | — | Partial `generateNextSeason()`; no `SeasonManager`; **no soak IT** |
| **RB-3** | 🔴 | **Wrong 1888 rules applied** | `ScriptsManager.createLeague()` | Uses `createDefaultRules()` (3-1-0) not historical (2-1-0) |
| **RB-4** | 🔴 | **No per-season rules configuration** | `ScriptsLoader`, mod files | `LEAGUE_RULES` always null; no rules TXT |
| **RB-5** | 🔴 | **No season identity in save** | `SaveGame.java` | Cannot track which season is active |
| **RB-6** | 🟠 | **Latent duplicate fixture regeneration** | `AuthorityManager`, `LeagueFixtureGenerator.hasFixturesScheduled()` | After all matches played, empty schedule → regen same season |
| **RB-7** | 🟠 | **League completion uses wrong match count** | `LeagueStandingsManager`, `MatchSimulator` | Friendlies inflate `matchesPlayed` |
| **RB-8** | 🟠 | **Competition edition never finalized** | Engine (missing) | Champion/end date never written |

---

## Critical bugs (game-breaking)

| ID | Severity | Issue | Location | Fix direction |
|----|----------|-------|----------|---------------|
| **C-1** | 🔴 | Continue double-click advances 2 days | `MainGameMenuTable.java` | Stage-level input filter or button swap during processing |
| **C-2** | 🔴 | `AuthorityManager.currentGame` never refreshed | `AuthorityManager.java` | ⚠️ **Partial fix Phase 0** — `refreshCurrentGame()`; verify all call sites |
| **C-3** | 🔴 | Load game skips `initializeJobSystem()` | `LoadGameDialog.java` | Call job init after load |
| **C-4** | 🔴 | `JobManager` caches stale `SaveGame` | `JobManager.java` | Refresh on `setCurrentGame()` |
| **C-5** | 🟠 | League created without ID | `ScriptsManager.createLeague()` | `league.setId(...)` — breaks draw screen navigation |
| **C-6** | 🟠 | `MessageManager.nextMessageId` not persisted | `MessageManager.java` | Store in `SaveGame`; collision after reload |
| **C-7** | 🟠 | `JobManager.nextId` not persisted | `JobManager.java` | Same as C-6 |

---

## High priority bugs

| ID | Severity | Issue | Location |
|----|----------|-------|----------|
| **H-1** | 🟠 | Match simulator overwrites ticket-sales attendance | `MatchSimulator.simulateMatch()` L86-95 |
| **H-2** | 🟠 | Away club ticket sales use wrong stadium | `MatchScheduler.checkClubSheduledMatches()` |
| **H-3** | 🟠 | Match history clicks don't open selected result | `MatchHistoryScreenTable.java` — no `setSelectedMatch()` on result screen |
| **H-4** | 🟠 | My Applications screen stale after apply | `MyApplicationsScreenTable.java` — refresh not guaranteed |
| **H-5** | 🟠 | `MatchScheduler` uses `DatabaseLoader.getClubById` during play | Should use `SaveGame.getClubById()` |
| **H-6** | 🟠 | `acceptOffer` clears staff with wrong profession ID | `JobManager.java` |
| **H-7** | 🟠 | Friendly scheduling always accepted, no conflict check | `MatchScheduler.java` |
| **H-8** | 🟠 | Save file has no version field | `SaveLoadSystem.java` |
| **H-9** | 🟠 | Top menu Load/Settings/Quit not implemented | `MainGameMenuTable.java` |

---

## Medium priority bugs

| ID | Severity | Issue | Location |
|----|----------|-------|----------|
| **M-1** | 🟡 | Calendar window wrong vs real 1888–89 (Apr start + 9 months) | `ScriptsManager`, `AuthorityManager` |
| **M-2** | 🟡 | `LeagueFixtureGenerator` ignores `CompetitionRules.matchFormat` | Always double RR |
| **M-3** | 🟡 | Head-to-head tie-break falls back to alphabetical | `LeagueStandingsManager` TODO |
| **M-4** | 🟡 | Cup pipeline entirely stub | `CompetitionScheduler`, `AuthorityManager` |
| **M-5** | 🟡 | Travel/matchday expenses always zero in weekly calc | `ExpenseCalculator.java` |
| **M-6** | 🟡 | Job offer messages missing category/type | `MessageManager` / `JobManager` |
| **M-7** | 🟡 | `isSaved` gate not enforced on Continue | `SaveGame`, `MainGameMenuTable` |
| **M-8** | 🟡 | Authority screen hardcodes season index 0 | `AuthorityScreenTable.java` |
| **M-9** | 🟡 | Finances season start approximated from `gameStartDate` | `FinancesScreenTable.java` |
| **M-10** | 🟡 | Player match-day path vs auto-sim split UX | `FuttoboruGameEngine` — employed player skips auto-sim for own match? (verify) |
| **M-11** | 🟡 | Contract IDs use negative timestamps | `PlayerContractGenerator.java` — fragile |
| **M-12** | 🟡 | Heavy debug `System.out.println` in production | Multiple files, especially `JobBoardScreenTable`, `ScriptsManager` |

---

## Low priority / tech debt

| ID | Severity | Issue | Location |
|----|----------|-------|----------|
| **L-1** | 🟢 | Magic screen ID numbers | `Futtoboru.java`, `MainMenuManager` |
| **L-2** | 🟢 | i18n TODOs throughout UI | All tables |
| **L-3** | 🟢 | Home screen placeholder | `HomeScreenTable.java` |
| **L-4** | 🟢 | Orphaned `tables/main/*` stubs | 10 files unwired |
| **L-5** | 🟢 | Empty `LeagueTable`, `ConfederationsTable` | `tables/authority/` |
| **L-6** | 🟢 | Empty `SeasonLeague` class | `data/SeasonLeague.java` |
| **L-7** | 🟢 | Unused `LeagueCreationScript` class | Parallel to `BasicScript` |
| **L-8** | 🟢 | 3D terrain / shader demo code in repo | `org/microbasic/`, `graphics/` — not game |
| **L-9** | 🟢 | 50+ overlapping AI analysis markdown files | Repo root — superseded by this audit |
| **L-10** | 🟢 | Hardcoded save filename `"savegame"` | `SaveLoadSystem.java` |

---

## Design gaps (not bugs — missing features)

| ID | Gap | Blocks v1.0? |
|----|-----|--------------|
| **DG-1** | `SeasonManager` class | Yes |
| **DG-2** | Script types: SEASON_END, NEXT_SEASON, RULES_AMENDMENT | Yes |
| **DG-3** | Season 1889–90 mod data | Yes |
| **DG-4** | Mid-season Nov 1888 points rule change | Optional |
| **DG-5** | Bottom-4 re-election AGM | Optional |
| **DG-6** | FA Cup round progression | **Yes** — PO locked 2025-06-25 |
| **DG-7** | Training / tactics / transfers screens | No |
| **DG-8** | Match preview detail (lineups, form) | No |
| **DG-9** | Promotion/relegation system | Later seasons |
| **DG-10** | Youth regens / retirement / mortality | **No in v1** — static cohort policy |
| **DG-11** | `GameTestHarness` + season/cup/soak ITs | **Yes** — release gate |

---

## Simulation / blocking issues (test targets)

| ID | Severity | Issue | Test |
|----|----------|-------|------|
| **BLK-1** | 🔴 | Stuck on mandatory draw not acknowledged | `MandatoryGateIT` |
| **BLK-2** | 🔴 | Draw viewed but `isRead` not set | Gate IT + UI test |
| **BLK-3** | 🟠 | Scheduled draw not delivered | Message delivery IT |
| **BLK-4** | 🟠 | Long-run ID collision (messages/jobs) | `LongRunSoakIT` |

See [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) §3.

---

## Bug dependency graph (fix order)

```
RB-5 (season in save)
  └── RB-1 (completion detection)
        └── RB-8 (finalize edition)
              └── RB-2 (rollover)
                    └── RB-4 (per-season rules)
                          └── RB-3 (1888 rules)

C-2 (AuthorityManager refresh)
  └── RB-6 (fixture regen guard)

C-3, C-4 (load/job init)
  └── H-4 (applications UI)

C-5 (league ID)
  └── Mandatory draw navigation
```

---

## Mapping to old ISSUES_PRIORITY_LIST.md

| Old # | New ID | Status |
|-------|--------|--------|
| #0 Continue double-click | C-1 | Still open |
| #1 Button listeners | Partially fixed (`addCaptureListener`) — if still broken, check C-3/C-4 | |
| #2 My Applications | H-4 | Still open |
| #5 Job offers | Likely H-4 + C-3/C-4 | |

---

## Verification checklist (post-fix)

For each critical fix, verify:

- [ ] **RB-1:** Unit test: all league matches played → `isLeagueComplete` true → engine triggers end event
- [ ] **RB-2:** Integration test: season 1 → rollover → season 2 fixtures exist
- [ ] **RB-3:** Standings after 2 wins = 4 points (not 6)
- [ ] **C-1:** Rapid 10 clicks on Continue → exactly 10 days advanced
- [ ] **C-3:** Load save → job board apply works
- [ ] **C-5:** Draw message → league draw screen shows correct fixtures
- [ ] **RB-6:** Complete season → Continue 30 days → fixture count unchanged
- [ ] **AT-2:** Mandatory draw blocks → acknowledge → date advances
- [ ] **Cup-6:** League + FA Cup complete headless in one season
- [ ] **LP-5:** 3-season soak without deadlock

---

## Summary

| Severity | Count |
|----------|-------|
| 🔴 Release blockers | 8 |
| 🔴 Critical bugs | 7 |
| 🟠 High | 9 |
| 🟡 Medium | 12 |
| 🟢 Low | 10 |
| Design gaps (v1) | 5 required, 3 optional |

**Minimum to ship v1.0:** fix all **RB-*** + **C-1** through **C-5** + season 2 content + **FA Cup** + **`GameTestHarness` / `SeasonPlaythroughIT` green**.
