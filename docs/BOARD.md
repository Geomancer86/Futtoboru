# Board (WORKSTATION ⇄ BOOK3PRO)

Claims, file locks and what is ready to take. **Only the PM of each machine edits this file** (R-FUT-5, D33); lanes report to their PM and never edit it. Events go to the machine logs, appended **at the bottom**: [`log/WORKSTATION.md`](log/WORKSTATION.md), [`log/BOOK3PRO.md`](log/BOOK3PRO.md) (`merge=union`, so they never conflict). Format: `YYYY-MM-DD HH:MM · CLAIM|DONE|HANDOVER|BLOCKED|NOTE · item · note`. Claim before you start; never claim an item with an open claim.


## Open claims
| Item | Machine | Branch | Since |
|---|---|---|---|
| E-DET cross-JDK determinism: own Gaussian in RandomSource, re-pin goldens once, guard | WORKSTATION | `ws/E-DET-gaussian` | 2026-11-28 |
| H-06 era events data (`era/baseline.json` + `events.json` per ERA_EVENTS_PROPOSAL, D30) | WORKSTATION | `ws/H-06-era-events` | 2026-11-28 |
| E-17a ECS engine behind `-Dfuttoboru.match.engine=instant|ecs` (default instant) + MatchEngine contract test + ECS season soak entry | WORKSTATION | `ws/E-17a-engine-flag` | 2026-11-28 |
| E-12b/E-16b realism: ball bounces off posts/bar, forwards run onto passes (offsides happen), foul rate check | WORKSTATION | `ws/E-16b-realism` | 2026-11-28 |
| H-05 history data into gamedata per HISTORY_DATA_PROPOSAL (D30): history texts, nicknames, fan tiers, kits | WORKSTATION | `ws/H-05-history-data` | 2026-11-28 |
| U-12 UI polish: Esc closes dialogs, dates/decimals/months from the game language, Authorities season, one link colour, inbox filter, debug button behind a dev flag | BOOK3PRO | `b3/U-12-ui-polish` | 2026-11-28 |
| P2-14 neutral venues on `Match` + season calendar as data (FA Cup opens/drawn in its real month) | BOOK3PRO | `b3/P2-14-venues-calendar` | 2026-11-28 |
| G-01 team sheet (XI, shape, style, captain; AI too) on the instant result **(PARKED: WIP pushed, next to finish)** | BOOK3PRO | `b3/G-01-team-sheet` | 2026-11-28 |
| G-04 contracts through the season (AI clubs too) **(PARKED: WIP pushed, after G-01)** | BOOK3PRO | `b3/G-04-contracts` | 2026-11-28 |
| R-02 + R-11a recruitment data (rules, wages, origins under `recruitment/`) + languages/literacy data and person language attributes (saves v3) | BOOK3PRO (parked) | `b3/R-02-recruitment-data` | 2026-11-28 |

## Files in use (lock: don't edit these while the claim is open; ask in the log)
| Machine | Item | Paths |
|---|---|---|
| WORKSTATION | Engine (E-10..E-22) | `match/ecs/**` (new ECS engine; BOOK3PRO: E-01 `Position`/`dribbling` data is yours, ask before editing `match/ecs`) |

## Ready to take (2026-11-28, DOC-01)
**Released:** v0.5.0-alpha (`main` = 7d7727f). **v0.5.0** is tagged when P2-13 (done), J-01 (done), U-12 and P2-14 are merged.
Model tier per D33: Sonnet for docs/data/research/art tools/merges, Opus for core sim, saves, determinism, hard bugs.

## Unblocked, unclaimed
| Item | Suggested owner | Tier | Notes |
|---|---|---|---|
| G-05 committee and the secretary's job (sacking, job seeker) | BOOK3PRO | Opus | J-01 job system is merged |
| R-03..R-07 recruitment without trials | BOOK3PRO | Opus | after R-02 data merges (D28 answered) |
| G-09 training, G-11 era events in the inbox, G-12 budget and debt | WORKSTATION (D33-7) | Sonnet/Opus | G-11 has data proposal `docs/design/ERA_EVENTS_PROPOSAL.md` |
| G-10 history / fan tiers on screen | WORKSTATION | Sonnet | after H-05 (claimed) puts the history texts into gamedata |
| G-13 AI-to-AI friendlies and away guarantees | WORKSTATION | Opus | D31: AI clubs get every system; U-08 `FriendlyArranger` is the base |
| R-08 AI recruitment/poaching | BOOK3PRO | Opus | after R-02..R-07 |
| E-17 events + `MatchResult` stats (after E-17a) | WORKSTATION | Opus | `match/ecs/**` is WORKSTATION's lock |
| E-18 crowd sections, E-19 performance (zero-alloc, spatial grid, benchmark) | WORKSTATION | Opus | E-19 needed: ~1.5-2.5 s a match estimated |
| P2-4 data review of the copied cup rosters | WORKSTATION | Sonnet | `docs/data/REVIEW_1888-89.md`; ~204 placeholder cup positions; placeholders allowed (D30 A3) |
| Source the 4 placeholder League positions (Villa, Blackburn, Burnley x2) | WORKSTATION | Sonnet | H-04 leftovers |
| E-01 follow-up: sourced positions for generated players beyond `PositionAssigner` | BOOK3PRO | Sonnet | queued in D25 |
| Weekly 10-minute producer play session on `playable-latest` | PM / producer | - | needs INF-01 `playable-latest` |

## Blocked
| Item | Blocker |
|---|---|
| E-20 calibration (= E-05) | E-17 events, E-19 performance; calibration data pack needs BOOK3PRO (1888-89 results already in `seasons/1888-89/results.json`) |
| E-22 saves with events/seed/engine version | E-17; touches saves (BOOK3PRO owns) |
| E-06 switch the game to ECS | E-20 + 7 green nightly soaks (INF-01), flipped by the producer (D33-8) |
| Trial scenes (R-09 and later) | ECS engine in the game (D28 Q4) |
| New viewer/art lanes (E-21f..m, extra art) | D33-1: a seeded season must first complete on ECS behind the engine flag |
| G-01 gate run, G-04, R-02 + R-11a merge | parked by the producer (max two code lanes on BOOK3PRO); G-04 waits for G-01 |
| AGM date for G-08 | unsourced (placeholder: one week after the league ends) |
| G1 path-aware gate, G3 nightly soak, G4 red-develop rule | INF-01 (WORKSTATION, claimed) + `test_level.py` (BOOK3PRO) |
| Phase 5 (season 2, 1889-90 as a release) | v0.5.0 tag, G-04 contracts, G-05 |
| Marketing | a full season playing end to end on the shipped build (ROADMAP Phase 5 rule) |

## Already claimed (do not take)
E-17a, INF-01, E-16b, H-05, DOC-01 (WORKSTATION); U-12, P2-14, G-01, G-04, R-02 + R-11a (BOOK3PRO).
