# Department: Match & Simulation — v1 Deep Dive

**Department expert:** _________________  
**Status:** ~45% of league playthrough pipeline  
**Blocks release:** Yes — feeds season completion

---

## 1. Mission (v1)

Own **league match execution**: fixtures generated correctly, all 132 games simulated, results feed standings and season completion — with **1888-accurate inputs** from Rules dept.

---

## 2. What exists today

| Component | Status |
|-----------|--------|
| `LeagueFixtureGenerator` — 12 teams, double RR, 132 matches | ✅ Works |
| `FuttoboruGameEngine.simulateMatchesForDate()` — auto-sim all clubs | ✅ Works |
| `MatchSimulator` — strength-based scores, league points | ✅ Works (wrong rules source) |
| `MatchScheduler` — ticket sales 7 days before match | ⚠️ Bugs |
| `getMatchResult()` — manual path for player club | ⚠️ Parallel to auto-sim |
| `CompetitionScheduler` | 🔲 Empty stub |
| Standings wired to daily loop | ❌ UI only |
| Season completion | ❌ Not this dept alone — feeds Seasons dept |

---

## 3. Verified numbers (12-team league)

| Metric | Value |
|--------|-------|
| Total league fixtures | **132** |
| Per club | **22** (double round-robin) |
| Match type constant | `Match.LEAGUE_MATCH = 2` |
| Matchdays | ~22 (weekly, Saturday 15:00 snap) |

---

## 4. Gaps not in initial analysis (new findings)

1. **Match day detection mismatch** — Auto-sim uses **date only**; preview uses **exact datetime** (`isEqual`). Game clock 04:00 vs fixture 15:00 → preview rarely triggers; player matches auto-sim like AI.
2. **`Match` has no `leagueId`** — Completion infers league via club membership search — fragile.
3. **`CompetitionScheduler.competitionDraw`** — RNG off-by-one; not production-ready.
4. **`initializeCompetitions()` never called** — Cups not bootstrapped on new game.
5. **No `MatchResult` entity** — Score lives on `Match` only.
6. **No player-level post-match stats** — Attributes unchanged by matches.
7. **Friendly acceptance 100%** — No schedule conflict check; corrupts `matchesPlayed`.
8. **Calendar compression** — 22 matchdays in ~9 months from Apr start ≠ historical Sep–Apr.
9. **`ANALYSIS_MATCH_SIMULATION_STATUS_V2.md` outdated** — Claims auto-sim missing; **it exists** on feature branch.
10. **`IMPLEMENTATION_SUMMARY_DAILY_SIMULATION_AND_RULES.md` claims COMPLETE** — Rules not historical; completion unwired.

---

## 5. Bug matrix (this department owns)

| ID | Bug | Fix |
|----|-----|-----|
| **H-1** | Sim overwrites ticket-sales attendance | Preserve presales or use sim-only model consistently |
| **H-2** | Away club ticket loop uses wrong stadium | Home-club-only ticket sales for league matches |
| **H-5** | `DatabaseLoader.getClubById` during play | Use `SaveGame.getClubById()` |
| **M-10** | Preview vs auto-sim datetime | Align `checkClubMatchDay` to `toLocalDate()` |
| **RB-7** | `matchesPlayed` includes friendlies | Add `leagueMatchesPlayed` or filter by type |
| **M-2** | Fixture generator ignores `matchFormat` | Read from `CompetitionRules` (with World/Rules) |

---

## 6. v1 deliverables (this department)

| ID | Deliverable | Acceptance |
|----|-------------|------------|
| MS-1 | 132 league fixtures for season window | Count test in CI |
| MS-2 | All league matches auto-sim on schedule | No stuck unplayed league games |
| MS-3 | Points from league rules (2-1-0) | 2 wins = 4 points, not 6 |
| MS-4 | League-only completion counter | Friendlies don't affect completion |
| MS-5 | Attendance/revenue consistent | H-1, H-2 fixed |
| MS-6 | Match preview datetime aligned | M-10 fixed |
| MS-7 | Feed `SeasonManager` completion signal | All league matches in `playedMatches` |

**Explicitly deferred v1:** cup sim, tactics, injuries, detailed match events.

---

## 7. Daily flow (reference)

```
continueGame()
  → simulateMatchesForDate(today)     // all clubs, date match
  → move match scheduled → played
  → MatchSimulator updates W/D/L, points (league only)
  → [Seasons dept] check isLeagueComplete
```

**Parallel path:** Match Preview → `getMatchResult()` for player club — must not double-sim (`isPlayed` guard exists).

---

## 8. Key files

| Path | Role |
|------|------|
| `engine/FuttoboruGameEngine.java` | Daily loop, sim orchestration |
| `engine/simulation/MatchSimulator.java` | Score generation, stats |
| `engine/temporal/LeagueFixtureGenerator.java` | Fixture creation |
| `engine/temporal/MatchScheduler.java` | Tickets, match day |
| `engine/simulation/LeagueStandingsManager.java` | Standings, completion math |
| `data/Match.java`, `data/Club.java` | Match lists, stats |

---

## 9. Implementation order

```
1. Fix H-1, H-2 (attendance/revenue)
2. Fix M-10 (match day datetime)
3. Fix RB-7 (league-only match count) — coordinate Seasons dept
4. Assert 132 fixtures in unit test
5. Assert points use 2-1-0 once Rules dept wires historical rules
6. Support SeasonManager signal when all league matches played
```

---

## 10. Dependencies

| From department | Need |
|-----------------|------|
| **Seasons & Rules** | Rules object on league; season window dates; completion wiring |
| **World & Data** | 12 squads with ≥11 players; stadium capacity > 0 |
| **Platform & UX** | Harness calls `continueGame()` in loop; fixture count assertions |

---

## 11. Recommended test (Platform co-own)

```java
@Test void leagueSeason132MatchesSimulated() {
  harness.newGameLeagueClub();
  harness.advanceToLeagueFixtures();
  harness.simulateAllLeagueMatches();
  assertEquals(132, harness.totalLeagueMatchesPlayed());
  assertEquals(22, harness.leagueMatchesPlayedPerClub());
}
```

---

## 12. Sign-off checklist (expert)

- [ ] 132 league fixtures generated for 12 clubs
- [ ] All league matches reach `playedMatches`
- [ ] Standings points match 2-1-0 rules
- [ ] Friendlies do not increment league completion counter
- [ ] Attendance not overwritten after ticket sales
- [ ] No double simulation on same match

---

## 13. Layer B deliverables (next track)

**Execution:** [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md)

| Phase | IDs | This department owns |
|-------|-----|-------------------|
| B1 | DATA-B1, MVP-1, MVP-3, MVP-8 | `FormationRegistry`, `MatchLineup`, `MatchSimulator v2`, balance ITs |
| B2 | MVP-4, H-1 | `AttendanceCalculator`, single attendance authority |
| B3 | MVP-5 | Post-match ratings computation |
| B4 | MVP-2 | Formation templates (sim weights) |

**Deferred:** MVP-10 save versioning; future embedded DB — [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md)

**Tests:** every MVP ships with unit + IT per [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)
