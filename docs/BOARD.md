# Board (WORKSTATION ⇄ BOOK3PRO)

Claims, file locks and what is ready to take. **Only the PM of each machine edits this file** (R-FUT-5, D33); lanes report to their PM and never edit it. Events go to the machine logs, appended **at the bottom**: [`log/WORKSTATION.md`](log/WORKSTATION.md), [`log/BOOK3PRO.md`](log/BOOK3PRO.md) (`merge=union`, so they never conflict). Format: `YYYY-MM-DD HH:MM · CLAIM|DONE|HANDOVER|BLOCKED|NOTE · item · note`. Claim before you start; never claim an item with an open claim.


## Open claims
| Item | Machine | Branch | Since |
|---|---|---|---|
| E-17a ECS engine behind `-Dfuttoboru.match.engine=instant|ecs` (default instant) + MatchEngine contract test + ECS season soak entry | WORKSTATION | `ws/E-17a-engine-flag` | 2026-11-28 |
| INF-01 Jenkins: concurrent + branch builds, full+smoke per develop commit, nightly soak (G3), `green/develop` + `playable-latest`, executors 4 day / 8 night | WORKSTATION | `ws/INF-01-jenkins` | 2026-11-28 |
| E-12b/E-16b realism: ball bounces off posts/bar, forwards run onto passes (offsides happen), foul rate check | WORKSTATION | `ws/E-16b-realism` | 2026-11-28 |
| H-05 history data into gamedata per HISTORY_DATA_PROPOSAL (D30): history texts, nicknames, fan tiers, kits | WORKSTATION | `ws/H-05-history-data` | 2026-11-28 |
| DOC-01 ROADMAP ticks + STATUS refresh + Ready-to-take with blockers (R-FUT-9) | WORKSTATION | `ws/DOC-01-boards` | 2026-11-28 |
| U-12 UI polish: Esc closes dialogs, dates/decimals/months from the game language, Authorities season, one link colour, inbox filter, debug button behind a dev flag | BOOK3PRO | `b3/U-12-ui-polish` | 2026-11-28 |
| P2-14 neutral venues on `Match` + season calendar as data (FA Cup opens/drawn in its real month) | BOOK3PRO | `b3/P2-14-venues-calendar` | 2026-11-28 |
| G-01 team sheet (XI, shape, style, captain; AI too) on the instant result **(PARKED: WIP pushed, next to finish)** | BOOK3PRO | `b3/G-01-team-sheet` | 2026-11-28 |
| G-04 contracts through the season (AI clubs too) **(PARKED: WIP pushed, after G-01)** | BOOK3PRO | `b3/G-04-contracts` | 2026-11-28 |
| R-02 + R-11a recruitment data (rules, wages, origins under `recruitment/`) + languages/literacy data and person language attributes (saves v3) | BOOK3PRO (parked) | `b3/R-02-recruitment-data` | 2026-11-28 |

## Files in use (lock: don't edit these while the claim is open; ask in the log)
| Machine | Item | Paths |
|---|---|---|
| WORKSTATION | Engine (E-10..E-22) | `match/ecs/**` (new ECS engine; BOOK3PRO: E-01 `Position`/`dribbling` data is yours, ask before editing `match/ecs`) |

## Ready to take (from docs/ROADMAP.md)
Target: **v0.5.0** = D18 (full season playable, every button works, 1280×720 minimum, autosave, labels in language files). Details in `docs/ROADMAP.md` Phase 2 close-out + Phase 3.

**v0.5.0-alpha released** (tag on `main`, 7d7727f). **v0.5.0** is tagged once P2-13, J-01, U-12 and P2-14 are merged (D30 A10).
**Phase 3b (D30):** gameplay on the instant result; order in `docs/ROADMAP.md`. Next after the claimed lanes: G-02 injuries, G-05 committee, recruitment R-02..R-07, G-08 re-election (claimed, BOOK3PRO).
**Unclaimed for WORKSTATION:** Phase 4 engine lanes per `docs/design/MATCH_ENGINE_ECS.md`. The ECS engine will read G-01's team sheet (`TeamSheet`, outside `match/ecs`) as its line-up input.
