# Futtoboru v1 — Testing, Simulation & Product Plan

**Document type:** MVP implementation plan (testing + long-run simulation + scope)  
**Date:** 2025-06-25  
**Branch target:** `feature/competitions-finances-seasons`  
**Status:** **Active plan** — supersedes stale priorities in [MVP_PRIORITY_ROADMAP.md](MVP_PRIORITY_ROADMAP.md) for v1 work  
**Related:** [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) · [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) · [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md)

---

## 1. Executive summary

v1 must prove a **closed, infinite loop**: league **and FA Cup** in the same career, correct historical rules, season completion, rollover, and **repeat forever** — without the game **blocking** on missing mandatory prompts.

Testing cannot rely on **unit tests alone**. We need a **headless end-to-end harness** that simulates months and years of `continueGame()` the same way production does, plus **soak tests** that fail if mandatory gates deadlock.

**Player world for v1:** squads are **static** — no regens, no retirements, no deaths. Ages may advance with the calendar (derived from fixed `birthDate`); that is cosmetic only until a later feature phase.

---

## 2. PO decisions locked (2025-06-25)

| Decision | v1 choice | Notes |
|----------|-----------|-------|
| **FA Cup** | ✅ **In v1** alongside league | Full knockout progression required for sign-off |
| **Unemployed start** | ❌ **Defer to v1.1** | v1 starts **employed** at a league club |
| **Season loop** | ✅ **Forever** | 1888–89 → 1889–90 → … via data-driven seasons |
| **Youth regens** | ❌ **Not in v1** | No intake, no regen pipeline |
| **Retirements** | ❌ **Not in v1** | Players never retire |
| **Death** | ❌ **Not in v1** | No mortality system |
| **Player aging** | ⚠️ **Display-only** | `birthDate` fixed; age = f(game date); no attribute decay from age |
| **Nov 1888 points change** | **Defer** | Ship single 2-1-0 rule set for 1888–89 first |
| **Re-election (bottom 4)** | **Script/message only** | Full AGM simulation defer |
| **Release tag** | **TBD** | `v1.0.0` when harness green + manual QA checklist |

Document amendments: [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §2, §10, new cup + lifecycle sections.

---

## 3. Problem statement: blocking and long-run failures

### 3.1 Mandatory input gates

The engine uses **mandatory unread messages** to block time advancement until the player views draws:

```112:142:futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/FuttoboruGameEngine.java
    public int getNextGameAction() {
        // ...
        if (isMandatory && isUnread && isNotDeleted) {
            if (messageType.equals("LEAGUE_DRAW") || messageType.equals("CUP_DRAW") || messageType.equals("FIXTURE_DRAW")) {
                return DRAW_ACTION;
            }
        }
```

**Failure modes observed / documented:**

| ID | Symptom | Likely cause | Test that must catch it |
|----|---------|--------------|-------------------------|
| **BLK-1** | Continue does nothing / stuck on date | Unread mandatory draw; UI never opened draw screen | Harness: `getNextGameAction() == DRAW_ACTION` with unread mandatory |
| **BLK-2** | Draw viewed but still blocked | `isRead` not set when leaving draw screen | Gate test: open draw → mark read → continue advances |
| **BLK-3** | Scheduled draw never delivered | `deliverScheduledMessages` / date mismatch | Advance to draw date → message in inbox |
| **BLK-4** | Double-click advances 2 days | `MainGameMenuTable` event queue (C-1) | Harness bypasses UI; separate UI test for 10 clicks = 10 days |
| **BLK-5** | Authority path no-op | `AuthorityManager.currentGame` stale (C-2) | Daily IT: fixtures/cups scheduled after league creation |
| **BLK-6** | Season never ends | `isLeagueComplete()` unwired (RB-1) | IT: all league fixtures played → completion event |
| **BLK-7** | Infinite duplicate fixtures | Regen after season complete (RB-6) | Soak: 30 days post-completion → fixture count stable |

Existing debug notes: [DEBUG_DRAW_MESSAGE_ISSUES.md](DEBUG_DRAW_MESSAGE_ISSUES.md), [STEP_BY_STEP_DEBUG_GUIDE.md](STEP_BY_STEP_DEBUG_GUIDE.md).

### 3.2 Why unit tests are insufficient today

| Gap | Detail |
|-----|--------|
| **12 shallow test classes** | Loaders + script smoke only; no full engine loop |
| **No `GameTestHarness`** | Each test duplicates setup; no shared advance API |
| **No season IT** | `SeasonPlaythroughIT` planned but missing |
| **CI does not gate** | `maven.test.failure.ignore=true` — red builds still “pass” |
| **UI-only gates** | Mandatory draw enforced in engine + UI; headless must simulate “player acknowledged draw” |
| **Long-run untested** | Message ID collisions (C-6), job IDs (C-7), save growth — only appear after many days |

---

## 4. Testing strategy (pyramid)

```
                    ┌─────────────────────┐
                    │  Manual QA checklist │  RELEASE_V1 §9
                    └──────────┬──────────┘
              ┌────────────────┴────────────────┐
              │   Soak / multi-season IT       │  10–30 seasons headless
              └────────────────┬────────────────┘
        ┌──────────────────────┴──────────────────────┐
        │  Integration / E2E (headless gdx)          │  SeasonPlaythroughIT, Cup+LeagueIT
        └──────────────────────┬──────────────────────┘
  ┌───────────────────────────┴───────────────────────────┐
  │  Component tests (engine slices)                       │  standings, sim, messages, rollover
  └───────────────────────────┬───────────────────────────┘
┌─────────────────────────────┴─────────────────────────────┐
│  Unit tests (loaders, rules, pure functions)                 │  existing 12 classes + expand
└─────────────────────────────────────────────────────────────┘
```

**Primary release gate:** headless **integration + soak**, not GUI robot tests.

**Optional later:** LibGDX scene tests for draw screen + Continue button (3–5 tests), Sikuli/Robot **not** v1 gate.

---

## 5. `GameTestHarness` (required infrastructure)

Single API used by all integration tests. Lives in `futtoboru-core/src/test/java/.../harness/`.

```java
public final class GameTestHarness {
  // Setup
  void newGameEmployedAtClub(long clubId, long seasonId);
  SaveGame game();

  // Time — bypasses UI double-click; calls engine directly
  void continueOneDay();
  void continueDays(int n);
  LocalDateTime advanceUntil(Predicate<SaveGame> condition, int maxDays);

  // Mandatory gates — simulates player viewing draw
  void deliverPendingMessages();
  void acknowledgeMandatoryDraws();  // marks LEAGUE_DRAW/CUP_DRAW read + runs draw logic if needed
  int getNextGameAction();           // mirrors FuttoboruGameEngine

  // Simulation helpers
  void simulateAllMatchesOnDate(LocalDateTime date);
  void fastForwardSeason();          // loop: gate check → continue → sim until league+cup complete

  // Assertions
  void assertNotBlocked();
  void assertSeasonComplete(long leagueId);
  void assertCupRoundComplete(String competitionId, int round);
  void triggerAndAssertRollover();

  // Persistence
  void saveToTemp();
  void reloadFromTemp();
}
```

**Design rules:**

1. **`continueOneDay()`** always calls `FuttoboruGameEngine.continueGame()` — never UI.
2. **`advanceUntil`** throws `SimulationDeadlockException` if `maxDays` exceeded while blocked or predicate false — **this is the soak-test failure mode**.
3. **`acknowledgeMandatoryDraws()`** is the headless stand-in for draw screens — must stay in sync with `LeagueDrawScreenTable` / cup draw mark-read behaviour.

Refactor existing `BaseTest` / `BaseTestTools` to delegate here.

---

## 6. Test catalog

### 6.1 Unit tests (expand coverage)

| Suite | Targets | Priority |
|-------|---------|----------|
| `LeagueStandingsManagerTest` | 2-1-0 points, goal average, completion excludes friendlies | P0 |
| `CompetitionRulesLoaderTest` | Parse `competition_rules.txt` per season | P0 |
| `MessageManagerTest` | Mandatory flag, delivery, mark read, ID persistence | P0 |
| `LeagueFixtureGeneratorTest` | 132 league matches, double RR | P1 |
| `CupFixtureGeneratorTest` | FA Cup 1888–89 round structure | P0 |
| `SeasonManagerTest` | Rollover state machine (when implemented) | P0 |
| Existing loader tests | Keep | — |

**Coverage goal:** engine packages `engine.simulation`, `engine.messages`, `engine.temporal` **>70% line** on P0 paths (measure with JaCoCo in CI).

### 6.2 Integration / E2E tests (headless — **release gate**)

| Test class | Scenario | Exit criteria |
|------------|----------|---------------|
| **`MandatoryGateIT`** | New game → league draw mandatory → blocked → acknowledge → continue | Date advances |
| **`LeagueSeasonIT`** | 1888–89 league only → champion → edition finalized | SC-1–SC-5 |
| **`FaCupSeasonIT`** | FA Cup rounds scheduled, played, winner | Cup-1–Cup-6 |
| **`LeagueAndCupIT`** | Parallel league + cup fixtures; no schedule conflicts | PL-2, Cup-4 |
| **`SeasonPlaythroughIT`** | 1888 complete → rollover → 1889 starts with new rules file | TR-*, LP-1 |
| **`MultiSeasonSoakIT`** | **3 seasons** minimum headless; save/load between each | LP-5 |
| **`LongRunSoakIT`** | **10 in-game years** (or 3,650 days) without deadlock | No BLK-*; memory stable |
| **`SaveLoadIT`** | Mid-season + post-rollover roundtrip | PL-7 |

### 6.3 FA Cup acceptance (new criteria)

| ID | Criterion |
|----|-----------|
| Cup-1 | FA Cup 1888–89 competition loaded from mod data |
| Cup-2 | Cup draw(s) use mandatory gate same as league |
| Cup-3 | Knockout rounds advance; losers eliminated |
| Cup-4 | Cup fixtures do not corrupt league `matchesPlayed` / completion |
| Cup-5 | Cup winner recorded on `CompetitionEdition` |
| Cup-6 | Cup + league both completable in one season |

Implementation reference: [ANALYSIS_LEAGUE_CUP_COMPLETE_V1.md](ANALYSIS_LEAGUE_CUP_COMPLETE_V1.md), [ANALYSIS_INBOX_SYSTEM_V1.md](ANALYSIS_INBOX_SYSTEM_V1.md) (cup message types).

### 6.4 Manual QA (human)

Keep [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §9 checklist. Add:

- [ ] FA Cup draw mandatory gate same behaviour as league draw  
- [ ] Full season league + cup without soft-lock  
- [ ] 3 seasons in UI without new game  

---

## 7. Player lifecycle — static world (v1 policy)

### 7.1 Current code reality

| Mechanism | Status |
|-----------|--------|
| `Person.birthDate` | Set at generation / load from mod files |
| Age display | UI derives from `birthDate` vs game date (label bug: shows date not age in places) |
| Daily aging tick | **None** — age increases implicitly as calendar advances |
| Regens | **None** |
| Retirement | **None** |
| Death | **None** |
| Mid-career player generation | `PersonGenerator` exists but not wired to season rollover |

### 7.2 v1 design policy: **Static cohort**

For **infinite seasons without regens**:

1. **Roster continuity:** Same `Person` objects persist across season rollovers unless a **season mod** explicitly replaces `players/{clubId}.txt` on load.
2. **No roster shrink:** Without retirements, squad size is constant — acceptable for v1.
3. **Aging:** Allowed for display; **must not** trigger retirement, death, or attribute collapse in v1.
4. **Contracts:** May expire — PO decision: **auto-renew 1-year** at same wage for v1 (simplest) OR freeze contracts across rollover (document in season dept).
5. **Future (post-v1):** Regen/youth/intake, retirement, mortality — separate feature flags.

### 7.3 Test implications

| Test | Assert |
|------|--------|
| `PlayerLifecycleIT` | Same player IDs season 1 → 2 → 3 |
| `PlayerLifecycleIT` | `birthDate` unchanged; computed age increases |
| `PlayerLifecycleIT` | Squad count unchanged after rollover |
| `LongRunSoakIT` | No code path removes players |

### 7.4 Documentation for modders

Add to season mod README (when season 19 lands): *v1 does not spawn new players; edit `players/*.txt` per season for historical squad changes.*

---

## 8. Implementation phases

### Phase 0 — Test infrastructure (1–2 weeks)

| ID | Work |
|----|------|
| T-0 | Implement `GameTestHarness` |
| T-1 | `MandatoryGateIT` + `SimulationDeadlockException` |
| T-2 | Remove `maven.test.failure.ignore`; add GitHub Actions `mvn test` |
| T-3 | JaCoCo report on `futtoboru-core` |

### Phase 1 — Unblock simulation (parallel with RB fixes)

| ID | Work | Registry |
|----|------|----------|
| T-4 | Fix C-2 AuthorityManager refresh | C-2 |
| T-5 | Fix C-5 league ID | C-5 |
| T-6 | Wire `isLeagueComplete` + friendlies fix | RB-1, RB-7 |
| T-7 | `acknowledgeMandatoryDraws()` matches UI | BLK-1, BLK-2 |

### Phase 2 — Season loop (release core)

| ID | Work | Registry |
|----|------|----------|
| T-8 | `SeasonManager` + save fields | RB-2, RB-5 |
| T-9 | Historical 2-1-0 rules from mod | RB-3, RB-4 |
| T-10 | Season 19 mod + rollover scripts | DG-3 |
| T-11 | `SeasonPlaythroughIT` green | LP-1, LP-5 |

### Phase 3 — FA Cup in v1

| ID | Work | Registry |
|----|------|----------|
| T-12 | Cup scheduling + draws + mandatory messages | DG-6, M-4 |
| T-13 | `LeagueAndCupIT` + `FaCupSeasonIT` | Cup-* |

### Phase 4 — Soak & ship

| ID | Work |
|----|------|
| T-14 | `LongRunSoakIT` (10 years or 3 seasons minimum) |
| T-15 | Manual QA checklist |
| T-16 | Tag release |

---

## 9. CI / quality gates

| Gate | Requirement |
|------|-------------|
| **G-TEST-1** | `mvn test` fails build on any failure |
| **G-TEST-2** | `SeasonPlaythroughIT` + `MandatoryGateIT` required on PR |
| **G-TEST-3** | `LongRunSoakIT` nightly (may be slower profile) |
| **G-TEST-4** | JaCoCo: no decrease on `engine.*` without PO waiver |

Parent POM: remove or override `maven.test.failure.ignore` in root `pom.xml` when implementing T-2.

---

## 10. Cross-reference map

| Topic | Document |
|-------|----------|
| Acceptance criteria | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |
| Bugs / blockers | [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) |
| Platform dept | [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) |
| Season / rules | [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) |
| Match / cup | [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md) |
| Gap vs CM | [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) |
| Draw debug | [DEBUG_DRAW_MESSAGE_ISSUES.md](DEBUG_DRAW_MESSAGE_ISSUES.md) |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Initial plan — PO decisions, harness design, test catalog, static player lifecycle |
