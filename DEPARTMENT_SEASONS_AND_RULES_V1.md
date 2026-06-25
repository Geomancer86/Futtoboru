# Department: Seasons & Rules — v1 Deep Dive

**Department expert:** _________________  
**Status:** ~5% of v1 season loop implemented  
**Blocks release:** Yes — primary owner of v1 goal

---

## 1. Mission (v1)

Own the **season lifecycle**: start → play under configured rules → detect completion → crown champion → rollover → next season with new rules — **forever**.

---

## 2. What exists today

| Asset | Status |
|-------|--------|
| `Season`, `CompetitionRules`, `CompetitionEdition`, `League` models | ✅ Structure |
| `createHistoricalRules()` (2-1-0, goal average) | ✅ **Never called** |
| `ScriptsLoader` → league creation script (17 Apr 1888) | ✅ Hardcoded in Java |
| `ScriptsManager.createLeague()` | ✅ Creates league, fixtures, messages |
| `LeagueStandingsManager.isLeagueComplete()` / `getChampion()` | ✅ **Never wired to daily loop** |
| `SeasonManager` | ❌ Does not exist |
| Season fields on `SaveGame` | ❌ Missing |
| Season 19 content | ❌ Missing |
| Per-season rules files | ❌ Missing |

---

## 3. Gaps not in initial analysis (new findings)

1. **Dual league architecture** — Static `mods/leagues.txt` (id 60) vs runtime `Authority.leagues` (no id). No link to `Competition` / `CompetitionEdition`.
2. **`SaveGame.allCups` / `allLeagues`** — Dead fields; FA Cup never enters active save.
3. **`MessageManager` says season starts 8 Sep 1888** — Fixtures use `gameStartDate + 9 months` from 1 Apr → inconsistent messaging.
4. **`SeasonLeague.java`** — Empty stub; `mods/seasons/18/leagues.txt` never loaded.
5. **Pre-Nov 1888 zero-points period** — Comments describe wins-only before Nov; audit only noted wrong 3-1-0.
6. **UI shows goal difference** — `LeagueDetailScreenTable` hardcodes GD column, not goal average for 1888.
7. **Contracts / finances at rollover** — No season-end handling for wages, `seasonIncome`, attribute snapshots.
8. **`ScriptsLoader` clones same 1888 script onto every season** — Adding season 19 to `seasons.txt` breaks without refactor.

---

## 4. v1 deliverables (this department)

| ID | Deliverable | Acceptance |
|----|-------------|------------|
| SR-1 | `SeasonManager` class | Owns completion, rollover, season identity |
| SR-2 | `SaveGame` season fields | `currentSeasonId`, `seasonIndex`, `seasonFinalized` |
| SR-3 | Wire completion in `continueGame()` | Calls `isLeagueComplete()` after match sim |
| SR-4 | Finalize `CompetitionEdition` | `championsId`, `endDate` set on complete |
| SR-5 | Season-end inbox message | Champion announced |
| SR-6 | Block fixture regeneration | Guard when season complete (RB-6) |
| SR-7 | Rollover routine | Reset club league stats; archive matches; bump dates |
| SR-8 | Load season 19 rules | From `competition_rules.txt` via loader |
| SR-9 | New script types | `1001` RULES_AMENDMENT, `1002` SEASON_END, `1003` NEXT_SEASON_START |
| SR-10 | Apply historical rules at league creation | 2-1-0 + goal average (min) or file-driven |

---

## 5. Key files to create

```
engine/SeasonManager.java
system/loaders/CompetitionRulesLoader.java
test/.../SeasonRolloverIntegrationTest.java
mods/seasons/18/competition_rules.txt
mods/seasons/19/  (with World & Data dept)
```

---

## 6. Key files to modify

| File | Change |
|------|--------|
| `FuttoboruGameEngine.continueGame()` | Call `SeasonManager.checkAndFinalize()` |
| `ScriptsManager.createLeague()` | Use loaded rules, set `league.setId()` |
| `AuthorityManager` | Refresh `currentGame`; skip regen if finalized |
| `LeagueStandingsManager.isLeagueComplete()` | Count league matches only |
| `LeagueDetailScreenTable` | Show GA when rules use goal average |
| `SaveGame.java` | Season identity + optional edition history |
| `BasicScript.java` | New script type constants |

---

## 7. Proposed `SeasonManager` API

```java
Season getCurrentSeason(SaveGame game);
boolean isLeagueSeasonComplete(SaveGame game, League league);
boolean isSeasonComplete(SaveGame game);           // league-only for v1
void finalizeSeason(SaveGame game);                // champion, edition, messages
void transitionToNextSeason(SaveGame game);        // rollover + load next mod
LocalDateTime getSeasonWindowStart(SaveGame game);
LocalDateTime getSeasonWindowEnd(SaveGame game);
```

---

## 8. Implementation order

```
Phase 0: Fix AuthorityManager stale SaveGame (C-2); league.setId (C-5)
Phase 1: CompetitionRulesLoader + season 18 rules file
Phase 1: Wire isLeagueComplete in daily loop; finalize edition + message
Phase 1: Fix league-only match counting (RB-7)
Phase 1: Fixture regen guard (RB-6)
Phase 2: SaveGame season fields + SeasonManager.transitionToNextSeason()
Phase 2: Season 19 scripts (NEXT_SEASON_START, not LEAGUE_CREATION)
Phase 3: Optional Nov 1888 amendment; re-election script
Phase 3: Integration test with Platform dept
```

---

## 9. Dependencies

| From department | Need |
|-----------------|------|
| **Match & Simulation** | Correct sim results, league match type, fixture counts |
| **World & Data** | `competition_rules.txt`, season 19 folder, loader refactor |
| **Platform & UX** | Harness test, save/load with season fields |
| **Economy & Club** | Reset `seasonIncome`/`seasonExpenditure` on rollover (consult) |

---

## 10. Open decisions (PO + expert)

| # | Question | Options |
|---|----------|---------|
| 1 | Nov 1888 mid-season points change? | Required / whole season 2-1-0 / defer |
| 2 | Pre-Nov zero-points period? | Model / ignore / defer |
| 3 | Bottom-4 re-election? | Script / simulate / defer |
| 4 | FA Cup in season-end gate? | League-only v1 / wait for cups |
| 5 | Calendar source | `seasons.txt` only / `season_window_*` in rules file |

---

## 11. Risks

| Risk | Mitigation |
|------|------------|
| Duplicate fixtures after season end | Regen guard + finalized flag |
| Friendlies break completion | League-only counter |
| Dual League/Competition models | v1: finalize runtime `League`; link edition later |
| Scope creep via cups | `isSeasonComplete()` = league only for v1 |

---

## 12. Sign-off checklist (expert)

- [ ] Season 1888 completes with 2-1-0 and goal average tie-break
- [ ] Champion recorded; inbox message delivered
- [ ] No second fixture list generated after completion
- [ ] Season 1889 starts with rules from season 19 file
- [ ] Club league stats reset; played matches archived
- [ ] Integration test passes (with Platform)
