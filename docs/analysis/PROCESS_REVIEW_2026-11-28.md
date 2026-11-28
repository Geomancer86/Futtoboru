# Process review, 2026-11-28: where the time goes, open items, "playable end to end every time"

Read from `origin/develop` at `f1ff890`. Analysis only: no code changed, no suite run.
**(M)** = measured (git, `tools/suite_lock.py` history, lane timings, repo files). **(E)** = estimate; do not quote as fact.
Decisions `R-FUT-1..10` are proposals; the producer's choices are `R-FUT-D1..D9` at the end.

## 1. Findings

| Topic | Finding |
|---|---|
| Lane time (WORKSTATION) | 35 lanes, **550 min (9.2 h)**, median **13.8 min**, p90 28.8. Ten lanes over 20 min hold 49 % of the time. In-lane rework 26 min (4.7 %). E-15 (player AI) has run over 60 min in one lane (M) |
| Tests after T-01 | 22 locked runs: **6.2 min running, 0.8 min waiting** in total = **1.3 % of lane time**; the gate on a merged tree took 16 s (M) |
| Tests before T-01 | About 3 suites per change plus a serial wait for CI: **6–10 min per change (E)** |
| Integration | 344 commits on develop, 131 merge commits; **62 merges (47 %) are "develop into the lane branch" re-syncs**. **69 commits (20 %) are HANDOFF-only; 95 (28 %) touch `docs/HANDOFF.md`**, which is 63 KB and conflicts on nearly every merge because both machines prepend at the same line (M) |
| Reading per lane | An engine lane's standard reading is **132 KB (about 33 k tokens) before the first edit**; 48 % of it is HANDOFF (M sizes, E tokens) |
| Model | Every WORKSTATION subagent ran on the top model, research and docs included. The repo has no subagent model rule (M) |
| Develop reds | Three: two merges without the cross-cutting guard tests, one Linux-only seed-order failure. All fixed; the gate now always runs the guards (M) |
| Rework | Sprite bake redone twice (port fidelity; 5 → 10 px/m, D29 amending D26 the same day); art prototype page reworked: **55–70 min, 10–13 % of lane time (E)** |
| Decisions | 12 producer decisions (D20–D31) in two days, three amending an earlier one within a day (M) |
| Jenkins | The job has `disableConcurrentBuilds()`: **at most 1 of 8 executors**. A develop commit runs the slow tier; **the desktop smoke test runs on develop only in the nightly** (M) |
| Maven | One surefire fork by design (4 forks measured slower: 49 s vs 37 s); one heavy run per machine. Fine for a 15–20 s fast tier (M) |
| Lanes per machine | Open claims: **BOOK3PRO 9, WORKSTATION 1** (M) |
| What WORKSTATION built | 7 of its 10 items on the 28th were viewer/art (E-21*), 3 simulation (E-13, E-14, E-16) (M) |
| Engine and game | Nothing outside `match/ecs/` references the ECS engine; the plan connects it last (E-17 flag, E-06 after E-20). Manager mode is playable end to end on v0.5.0-alpha with `MatchSimulator` (M) |
| End-to-end tests | `FullSeasonTests`, `SaveGameTests`, `AutosaveTests`, `SeededGameTests`, `NewGameFreshDataTests`, `DesktopSmokeIT`: none is in the lane gate (M) |
| Stale boards | ROADMAP lacks ticks on finished items (U-03a, U-06, U-08, U-10); STATUS still says the full season runs in `verify`. The H-01/H-02/H-03 proposals are approved (D30) but not cut into slices (M) |

## 2. Where the time goes, ranked

| # | Sink | Size | Status |
|---|---|---|---|
| 1 | Critical path on one lane with coarse slices; engine wired to the game last | E-15 > 60 min in one lane; 7 of 10 items were viewer/art; E-17 unblocked and unclaimed (M) | Open → R-FUT-1 |
| 2 | Serial integration and HANDOFF churn | 47 % of merges are re-syncs; 20 % of commits are board edits; 3–6 min of PM per lane, 1.7–3.5 h (E) | Open → R-FUT-5, R-FUT-3 |
| 3 | Lane overhead (top model everywhere, 33 k tokens of reading, long briefs) | 2–4 min per lane, 70–140 of 550 min (E), plus cost | Open → R-FUT-6 |
| 4 | Rework and decision round-trips | 55–70 min, 10–13 % (E) | Open → R-FUT-8 |
| 5 | Idle hardware | Jenkins 1 of 8 by configuration; 9 claims vs 1 (M) | Open → R-FUT-4, R-FUT-7 |
| 6 | Test cycles | 6–10 min per change before (E); 1.3 % of lane time now (M) | **Fixed by T-01** |

## 3. Why the open items are not moving

| Item | Blocker |
|---|---|
| v0.5.0 tag | P2-13, J-01, U-12, P2-14: claimed by BOOK3PRO, in progress among 9 parallel claims |
| G-01, G-03, G-04, G-08, R-02 + R-11a | in progress on BOOK3PRO |
| G-02, G-05, R-03, R-04, R-05, R-07, R-09 | unclaimed: no free lane on BOOK3PRO; WORKSTATION treats gameplay as the other machine's area (D21/D30) |
| H-01 history, H-02 grounds, H-03 era events | approved (D30), never sliced or listed as ready |
| E-15 player AI | four slices in one lane |
| E-17 events + `MatchResult` + flag | **unblocked (E-14, E-16 merged), unclaimed**; needs the `TeamSheet` line-up contract with G-01 |
| E-18 crowd, E-22 saves/replay, E-04 ticker | wait for E-17 |
| E-19 performance | benchmark can start; the rest waits for E-15..E-17 |
| E-20 calibration | waits for E-19 and a data pack + validator not yet requested from BOOK3PRO |
| E-21f..m viewer | events for the HUD come from E-17 |
| E-06 engine switch | waits for E-20 |

None waits on a test. They wait on a claim, a slice or a hand-off between machines.

## 4. Decisions proposed

| Id | Decision |
|---|---|
| **R-FUT-1** | First playable ECS match: **E-17a** (`EcsMatchEngine implements MatchEngine`, `-Dfuttoboru.match.engine=instant|ecs`, default `instant`, goals-only result) starts now beside E-15. E-15 splits into a perception / b shape slots / c style / d action choice. The `match/ecs/**` lock becomes per sub-package. No new viewer/art lane until a seeded season completes on the ECS engine under the flag |
| **R-FUT-2** | "Always playable" guardrails G1–G7 (section 5) |
| **R-FUT-3** | Red develop: fixed within 30 minutes or the merge is reverted, without asking; CI moves a `green/develop` tag and archives a `playable-latest` jar after every full + smoke pass |
| **R-FUT-4** | Jenkins: concurrent builds for lane branches (full + smoke as evidence, nobody waits); develop commit = full + smoke; nightly season soak; calibration batches from E-20. Up to 4 executors by day, all 8 from 22:00 to 08:00 |
| **R-FUT-5** | Merge train (one integration branch per batch, the gate once, one push). HANDOFF split: `docs/handoff/BOARD.md` (claims and locks, PM only) + one append-only log per machine with `merge=union`; lanes stop writing the board; entries older than two days archived |
| **R-FUT-6** | Model by lane type: mid tier for research, docs, design, data, art tools, merges; top tier for core simulation code, saves, determinism, hard bugs. A lane card of at most 40 lines; at most three documents read, by section. DONE lines record model and minutes |
| **R-FUT-7** | Lane targets: WORKSTATION 4 code + 1 docs now, 6 + 2 once R-FUT-5 is in place; BOOK3PRO at most 5 claims. WORKSTATION takes H-01/H-02/H-03 and unclaimed gameplay when its engine lanes are blocked. `suite_lock`: 3 slots on WORKSTATION at 6 + 2, 1 on BOOK3PRO; one Maven fork stays |
| **R-FUT-8** | Decisions in two windows a day as one multiple-choice sheet; lanes continue with the recommended option marked provisional; one approved sample before any asset or data batch |
| **R-FUT-9** | ROADMAP ticks and STATUS brought up to date; "Ready to take" lists every unblocked item with its blocker |
| **R-FUT-10** | Measure three days (model, lane minutes, rework, minutes per merge batch, executors busy at night); review 2026-12-01 |

## 5. Guardrails: playable end to end every time

| # | Guardrail | Where and when | Owner |
|---|---|---|---|
| G1 | Playable check in the lane gate, path-aware: simulation or data change → `FullSeasonTests` + `SaveGameTests`; startup, screen wiring, launcher, POM or resource loading → `DesktopSmokeIT`; guard tests always. Budget 90 s, else the check returns to post-merge | pre-merge, local (`tools/test_level.py --run`) | BOOK3PRO |
| G2 | Full + smoke on every develop commit and every pushed lane branch | Jenkins, asynchronous | WORKSTATION |
| G3 | Nightly soak: 16 seeds × 3 seasons; invariants (no exception, every fixture played, table sums, one champion, one cup winner, finite money, save → load → continue equal at each rollover, next season created); report names the first failing seed | 22:00–08:00, EXECUTOR_BACKUP | WORKSTATION runs, BOOK3PRO fixes the season loop |
| G4 | Engine flag: `instant` stays the default and release path until E-20 passes and the ECS soak is green seven nights; one `MatchEngine` contract test for both engines; the ECS soak runs nightly from E-17a | code + nightly | WORKSTATION |
| G5 | Red develop = revert first; `green/develop` + `playable-latest` | Jenkins | the machine that merged |
| G6 | Every new feature or season brings its proof: season pack added to the soak list, new screen added to `ScreenshotTour`, save change with its migration test, AI clubs on the same system (D31) | in the lane | both |
| G7 | The producer plays `playable-latest` ten minutes a week; notes become items | weekly | producer + PM |

**Each machine, starting now.** WORKSTATION: E-15a–d and E-17a, then E-19/E-20; the Jenkins jobs, soak and morning triage; the merge train for `ws/*`; spare lanes on H-01/H-02/H-03. BOOK3PRO: the v0.5.0 quick fixes first, then G-01/G-03/G-04/G-08 and recruitment; saves; the gate tool (G1); release tags from `green/develop`; at most 5 claims.

## 6. For the producer

| Id | Question | A | B | Recommended |
|---|---|---|---|---|
| R-FUT-D1 | Pause new viewer/art lanes until a season completes on the ECS engine under the flag? | Yes | Keep one viewer lane | A |
| R-FUT-D2 | Model per lane type (R-FUT-6), measured for ten lanes? | Yes | All lanes on the top tier | A |
| R-FUT-D3 | EXECUTOR_BACKUP by day? | 4 executors by day, 8 at night | Night only | A |
| R-FUT-D4 | Red develop? | Revert first after 30 min | Always fix forward | A |
| R-FUT-D5 | Decisions? | Two windows a day, provisional defaults, one sample before a batch | As they arise | A |
| R-FUT-D6 | WORKSTATION lanes? | 4 + 1 now, 6 + 2 later; BOOK3PRO at most 5 claims | Stay at 3 + 1 | A |
| R-FUT-D7 | May WORKSTATION take gameplay and history items (amends D21)? | Yes, after a claim | No | A |
| R-FUT-D8 | When does the ECS engine become the default? | After E-20 and seven green soak nights; the producer flips it | Earlier, as an opt-in setting | A |
| R-FUT-D9 | A weekly ten-minute play of `playable-latest`? | Yes, fixed day | When there is time | A |

**Not verified:** BOOK3PRO's lane timings and hardware, the live Jenkins job configuration (only the `Jenkinsfile` was read), the PM's minutes per merge, and the current duration of `FullSeasonTests` (the ~11 s in STATUS predates T-01).

## Producer decision (2026-11-28)
All nine approved as recommended (option A for R-FUT-D1..D9); recorded as **D33** in `docs/ROADMAP.md`.
