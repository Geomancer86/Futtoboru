# Department: Platform & UX — v1 Deep Dive

**Department expert:** _________________  
**Status:** Headless harness + 59 PR tests + soak profile ✅; manual QA pending  
**Role in v1:** **Proof gate** — E2E headless simulation blocks release tag

**Engine/UI reference:** [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md)

**Master plan:** [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)

---

## 1. Mission (v1)

Prove the three core departments work together via **automated season playthrough test** + stable **save/load** + **CI that fails on red**. Fix critical UI/engine bugs that block the loop.

---

## 2. What exists today

| Asset | Status |
|-------|--------|
| JUnit 5 + `gdx-backend-headless` | ✅ |
| `GameTestHarness` + ITs (league/cup/save/soak/10-season) | ✅ Phase 0 complete |
| GitHub Actions (`mvn test`, nightly soak) | ✅ |
| `GameTestHarness` | ✅ `harness/GameTestHarness.java` |
| Season playthrough IT | ✅ Covered by `LeagueAndCupIT`, soak ITs |
| GUI E2E | ❌ Not primary gate — headless first |

---

## 3. v1 deliverables (this department)

| ID | Deliverable | Acceptance |
|----|-------------|------------|
| PU-1 | `GameTestHarness` | ✅ Phase 0 — `harness/GameTestHarness.java` |
| PU-2 | `SeasonPlaythroughIT` | 1888 complete → rollover → 1889 rules applied |
| PU-2b | `LeagueAndCupIT` + `FaCupSeasonIT` | Full season both competitions |
| PU-2c | `MultiSeasonSoakIT` | ≥3 seasons; no mandatory-gate deadlock |
| PU-2d | `LongRunSoakIT` | 10 in-game years or 3650 days max |
| PU-2e | `MandatoryGateIT` | Draw block + acknowledge path |
| PU-3 | Save/load roundtrip test | Mid-season + post-rollover |
| PU-4 | CI fails on test failure | Remove `test.failure.ignore` |
| PU-5 | GitHub Actions workflow | Java 17 + `mvn test` on PR |
| PU-6 | Fix C-1 Continue double-click | Or document as known + harness bypass |
| PU-7 | Fix C-2/C-3/C-4 manager refresh | Authority, Job on load |
| PU-8 | Fix C-5 league ID | Draw navigation |
| PU-9 | Headless UI tests (optional, 3–5) | Draw gate blocks continue |
| PU-10 | **UI scale** (Settings + persist) | ✅ **MVP** — `UiScale` viewport + Graphics select (100–200%); match engine deferred |

**Explicitly not v1:** Sikuli/Robot full GUI E2E, every screen polished.

---

## 4. Proposed `GameTestHarness` API

```java
class GameTestHarness {
  void newGameEmployedAtClub(long clubId, long seasonId);
  void advanceDays(int n);
  void advanceUntil(LocalDate predicate);
  void clearMandatoryDrawGate();
  void simulateRemainingLeagueMatches();
  void assertSeasonComplete();
  void triggerRollover();  // or via natural SeasonManager
  void saveAndReload();
  // getters: standings, champion, currentSeasonId, rules
}
```

Refactor duplicated `@BeforeAll` blocks in 12 tests to use this.

---

## 5. v1 acceptance test (the release gate)

```text
SeasonPlaythroughIT.fullLoop1888to1889():
  1. newGameEmployedAtClub(PRESTON, 18)
  2. advanceUntil(leagueExists && fixturesGenerated)
  3. clearMandatoryDrawGate()
  4. simulateRemainingLeagueMatches()  // or advanceDays(300)
  5. assertSeasonComplete()
  6. assertRules(2, 1, 0, GOAL_AVERAGE)
  7. assertChampionNotNull()
  8. triggerRollover / natural transition
  9. assertCurrentSeasonId(19)
  10. assertRulesFromSeason19File()
  11. saveAndReload()
  12. assertStillSeason19()
```

**This test fails today** — correct TDD guardrail for v1.

---

## 6. CI configuration

### Jenkins (fix existing)

```groovy
// REMOVE: -Dmaven.test.failure.ignore=true
sh "mvn -B clean test"
```

### GitHub Actions (add)

```yaml
name: Test
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - run: mvn -B clean test
```

Headless tests need **no Xvfb**. Keep Xvfb only if LWJGL UI tests added later.

---

## 7. Critical bugs (Platform owns fix or coordinate)

| ID | Issue | Owner coord |
|----|-------|-------------|
| C-1 | Continue double-click | Platform (UI) |
| C-2 | AuthorityManager stale SaveGame | Platform + Seasons |
| C-3 | Load skips job init | Platform |
| C-4 | JobManager stale SaveGame | Platform |
| C-5 | League missing ID | Seasons (Platform verifies in test) |
| PL-7 | Save/load broken paths | Platform |

---

## 8. UI gates (minimal for v1)

| Screen | v1 requirement |
|--------|----------------|
| Continue / day advance | Works once per click (or harness bypasses UI) |
| Mandatory draw | Blocks until cleared |
| League detail / standings | Shows correct tie-break column |
| Save / load | Roundtrip in test |
| Home, tactics, jobs | **Broader** — not v1 gate |

Optional headless UI test: post synthetic touch to Continue button actor; assert exactly +1 day.

---

## 9. Dependencies

| From department | Need |
|-----------------|------|
| **Seasons & Rules** | `SeasonManager`, completion, rollover |
| **Match & Simulation** | Sim pipeline, 132 matches |
| **World & Data** | Season 18+19 data, rules loader |

Platform **does not implement** season logic — it **proves** it.

---

## 10. Implementation order

```
1. GameTestHarness (extract from BaseTestTools)
2. SeasonPlaythroughIT (failing — documents gap)
3. Fix CI ignore flag + add GHA workflow
4. Fix C-2, C-3, C-4, C-5 (coordinate)
5. Save/load roundtrip test
6. Optional: 3 headless UI gate tests
7. Re-run SeasonPlaythroughIT until green → v1 tag
```

---

## 11. Sign-off checklist (expert)

- [ ] `mvn test` fails build on regression (Jenkins + GHA)
- [ ] `SeasonPlaythroughIT` passes on `develop`
- [ ] Save/load test passes mid-season and post-rollover
- [ ] No duplicated HeadlessApplication boilerplate in tests
- [ ] README points to `DOCUMENTATION_INDEX.md`
