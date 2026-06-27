# PM Report — Distance to the 60-Season Goal

**Document type:** PM status / north-star tracking  
**Date:** 2026-06-26  
**Branch:** `analyze-ai-branch`  
**Audience:** PM, PO, Sponsor, Tech lead, QA

**Season model (PO locked):** [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)

---

## 1. What “60 seasons” means

| | |
|--|--|
| **Goal** | A single save can simulate **60 in-game competition years** (≈60 league champions, cup winners if in scope) **without starting a new game**, using **procedural season rollover** — not 60 pre-authored mod folders. |
| **Proof required** | Automated **end-to-end** and **soak tests** — not manual playthrough alone. |
| **v1 ship bar (PO)** | Full **one-season** league + FA Cup loop with correct gates; **≥3-season** soak green on PR; **60-season** soak green nightly (stretch gate). |
| **Not in v1** | Hardcoded `mods/seasons/19…77`; youth regens; unemployed start. |

---

## 2. Executive scorecard

| Dimension | Target | Current | % of goal |
|-----------|--------|---------|-----------|
| **Automated test foundation** | Harness + CI | 49 tests green, `MandatoryGateIT` | **100%** |
| **Single season playable (employed)** | League + cup, gates | Engine + UI largely wired; rules wrong; friendlies bug | **~65%** |
| **Season completion (detect + award)** | SC-1…SC-4 | `checkLeagueCompletion()` wired; cup partial; **no E2E proof** | **~55%** |
| **Procedural rollover (1 hop)** | TR-1…TR-7 | `generateNextSeason()` partial; no stat reset; no `SeasonManager` | **~30%** |
| **Multi-season loop proven (3+)** | `MultiSeasonSoakIT` | **Not implemented** | **~0%** |
| **60-season loop proven** | `LongRunSoakIT` (60) | **Not implemented** | **~0%** |
| **Champion history over N years** | List 60 champions in test | **Impossible today** — no soak, rollover incomplete | **~0%** |

### Overall distance to 60-season goal

```
┌─────────────────────────────────────────────────────────────┐
│  PROVEN BY AUTOMATED TESTS          ~12% of 60-season goal   │
│  CODE EXISTS BUT UNPROVEN           ~35% additional (≈47%)   │
│  NOT STARTED                        ~53% remaining           │
└─────────────────────────────────────────────────────────────┘
```

**Plain language:** We can start 1888–89 and run days with mandatory draws; merged code can **detect** league completion and **attempt** next-year fixture generation. We **cannot yet certify** even 3 consecutive procedural seasons — let alone 60 — in headless E2E.

---

## 3. Testing ladder (what we need)

### Tier 0 — Done ✅

| Test | Count | Status |
|------|-------|--------|
| Loaders, generators, preferences | 44 | ✅ |
| `GameTestHarnessTest` | 3 | ✅ |
| `MandatoryGateIT` | 5 | ✅ |
| `mvn clean test` + GitHub Actions | — | ✅ |

### Tier 1 — Single season E2E (next priority)

| Test | Purpose | Status |
|------|---------|--------|
| **`SeasonPlaythroughIT`** | New game → acknowledge draws → sim full league (+ cup) → assert champion + edition finalized | ❌ Not written |
| **`LeagueCompletionIT`** | `advanceUntil(isLeagueComplete)` + inbox messages | ❌ |
| **`LeagueAndCupIT`** | Both competitions complete same simulated year | ❌ |

**Blockers before Tier 1 passes reliably:**

- RB-7: friendlies inflate `matchesPlayed` → false completion or never completes  
- NG-6: 1888 2-1-0 rules not loaded from mod  
- Cup completion path must match league gate for rollover PO decision  

### Tier 2 — Procedural rollover (3 seasons)

| Test | Purpose | Status |
|------|---------|--------|
| **`SeasonRolloverIT`** | After season 1 champion, assert edition 2 exists, fixtures generated, stats reset | ❌ |
| **`MultiSeasonSoakIT`** | 3 procedural seasons; collect 3 champions; no `SimulationDeadlockException` | ❌ |
| **`SeasonManagerTest`** | Unit tests for transition state machine | ❌ |

**Blockers:**

- No `SeasonManager` / `SaveGame` season fields (RB-2, RB-5)  
- Incomplete rollover in `generateNextSeason()` (stats, schedules, cup edition)  
- RB-6 duplicate fixture regen after completion  

### Tier 3 — 60-season north star

| Test | Purpose | Status |
|------|---------|--------|
| **`LongRunSoakIT`** | Headless 60 rollovers or 60 calendar years; assert 60 league champions | ❌ |
| **Performance budget** | e.g. &lt;15 min CI nightly | ❌ |
| **Memory / ID audit** | Message & job IDs don’t exhaust (BLK-4) | ❌ |

**Gate recommendation:**

| Gate | Test | Where |
|------|------|-------|
| PR | `MultiSeasonSoakIT` (3 seasons) | `mvn test` |
| Nightly | `LongRunSoakIT` (60 seasons) | Maven profile `soak` |

---

## 4. Code vs test map (merged branch)

| Capability | Code | Unit test | E2E / soak |
|------------|------|-----------|------------|
| League fixture generation | ✅ | ✅ `LeagueFixtureGeneratorTest` | — |
| Cup bracket / semi-finals | ✅ | ✅ `CupBracketGeneratorTest` | — |
| Mandatory draw gates | ✅ | — | ✅ `MandatoryGateIT` |
| League completion | ✅ wired | ❌ | ❌ |
| Cup completion | ⚠️ partial | ❌ | ❌ |
| `generateNextSeason()` | ⚠️ partial | ❌ | ❌ |
| In-season timeline scripts | ⚠️ 2 types | ✅ `ScriptManagerTests` | ❌ |
| Rules from mod | ❌ | ❌ | ❌ |
| 60-year champion list | ❌ | ❌ | ❌ |

---

## 5. Work packages to close the gap

Ordered by dependency:

| WP | Work | Dept | Unlocks |
|----|------|------|---------|
| **WP-1** | Fix RB-7 league-only match counting | Match | Reliable completion |
| **WP-2** | `competition_rules.txt` on start mod + loader | World | NG-6 |
| **WP-3** | `SeasonManager` + save season fields | Seasons | Rollover ownership |
| **WP-4** | Complete rollover (R-1…R-9 in design doc) | Seasons | Tier 2 |
| **WP-5** | `SeasonPlaythroughIT` + `LeagueAndCupIT` | Platform | Tier 1 proof |
| **WP-6** | `MultiSeasonSoakIT` (3) | Platform | PR gate |
| **WP-7** | `LongRunSoakIT` (60) + nightly profile | Platform | **60-season goal** |
| **WP-8** | `RULES_AMENDMENT_SCRIPT` (optional v1) | Seasons | FM-style mid-era events |

**Estimated sequence:** WP-1 → WP-2 → WP-3 → WP-4 → WP-5 → WP-6 → WP-7.

---

## 6. v1 release vs 60-season goal

| Milestone | Criteria | Status |
|-----------|----------|--------|
| **v1.0 shippable loop** | 1 full season league + cup + 1 procedural rollover + 3-season soak | **Not met** |
| **60-season north star** | `LongRunSoakIT` 60 champions | **Not met** |
| **Can we demo one season today?** | Yes with manual QA (see `TESTING_COMPETITION_COMPLETION.md`) | ⚠️ |
| **Can we prove 60 seasons today?** | **No** | ❌ |

---

## 7. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Treating “season 19 mod” as v1 blocker | Wrong prioritization | This report + design doc |
| Rollover without stat reset | Soak corrupts standings | R-2…R-4 + unit tests |
| Shared `Authority` / club state | Flaky multi-season IT | Harness isolation pattern (done) |
| Soak too slow for CI | Defer 60 to nightly | Maven profile `soak` |
| Cup/league completion desync | Rollover fires early | Single `SeasonManager.isSeasonComplete()` |

---

## 8. Commands

```bash
# PR gate (today)
mvn clean test

# Future nightly (not yet configured)
mvn test -Psoak -Dtest=LongRunSoakIT
```

---

## 9. Summary for stakeholders

We **correctly align** v1 with **one start-season mod** plus **procedural infinite seasons** — the same model Football Manager uses for timeline scripts within an era, not sixty hardcoded future databases.

**Automated proof of the 60-season goal is ~12% complete** (infra only). **Implementation is ~47%** of the way if we count merged completion/rollover code that is still **untested** for multi-year loops.

**Next concrete deliverable:** `SeasonPlaythroughIT` (one year) then `MultiSeasonSoakIT` (three years). **60 seasons** follows once rollover is trustworthy.

---

*Previous merge status: [PM_V1_POST_MERGE_REANALYSIS.md](PM_V1_POST_MERGE_REANALYSIS.md)*
