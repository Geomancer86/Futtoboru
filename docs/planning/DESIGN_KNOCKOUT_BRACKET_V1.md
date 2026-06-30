# Knockout Bracket Scheduler — Design v1

**Status:** Implemented (2026-06-30)  
**Branch:** `analyze-ai-branch`

## Historical context (1888–89 FA Cup)

- The **Football League 1888–89 had 12 clubs** — that is correct for the league mod.
- The **FA Cup first round proper 1888–89 had 32 teams** (16 ties), mixing league clubs and invited non-league sides after qualifying rounds (qualifying not modelled in v1).
- Mod data: `mods/seasons/18/competitions/1/competition_edition.txt` lists **32 participant club IDs**.
- `ScriptsLoader` now uses the same 32 IDs as the mod file (not a naive `1..32` loop).

## Generic bracket generator

`CupBracketGenerator` is the single generator for any knockout size:

| Input | Behaviour |
|-------|-----------|
| `n` teams (≥ 2) | Pad to next power of two with first-round byes |
| Output | Exactly `n - 1` matches across `log₂(bracketSize)` rounds |
| Round 1 | Paired teams from non-bye pool |
| Round 2+ | Pair consecutive parent matches; round 2 with byes pairs each R1 winner with a bye team |
| Later rounds | TBD home/away until `CupBracketManager.advanceWinner()` fills slots |

Unit tests cover 8, 12, 16, and 32 teams.

## Runtime pipeline (one path)

```
May 1 script / mod edition
        ↓
ScriptsManager.createCup() — reuse mod edition if season name matches
        ↓
AuthorityManager.checkAndScheduleCupDraws() — one draw per edition (needsCupDraw guard)
        ↓
CupBracketGenerator.generateCompleteBracket()
        ↓
MatchSimulator + CupBracketManager.advanceWinner() — fill parent slots, complete final
```

**Do not** call `CompetitionScheduler.competitionDraw()` for pre-generated brackets — `AuthorityManager.checkCupRoundProgression()` skips editions with parent-link matches.

## Edition lifecycle fixes (CUP-B2)

| Issue | Fix |
|-------|-----|
| Duplicate 1888–89 editions (mod + script) | `ScriptsManager` reuses existing edition by season fragment |
| Staging drew full bracket months early | `generateNextCupStaging()` defers draw; `checkAndScheduleCupDraws()` draws when edition is active |
| Stale cup fixtures on rollover | `SeasonManager.archiveAndClearCupFixtures()` before new edition |

## Health monitoring

`SimulationHealthMonitor` cup checks:

- Semi-final round must have exactly **2** matches (excluding replay suffix `…R`).
- Only validates editions with **played activity** or a recorded champion (ignores future staging).
- Cup TBD placeholders are **not** counted in the ticket-window gate (expected until parent ties finish).

## Logging profiles (tests)

| Profile | Maven | Use |
|---------|-------|-----|
| Quiet (default) | `mvn test` | Fast PR feedback; CRITICAL still fails via `HarnessCriticalLogCounter` |
| Nightly | `-Pnightly` | More engine categories |
| Soak | `-Psoak` | Full `DebugLogManager` verbosity for long-run debugging |

Override: `-Dfuttoboru.test.logProfile=quiet|verbose`

## Related docs

- [SIMULATION_TEST_SUITE_V2.md](SIMULATION_TEST_SUITE_V2.md)
- [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) — CUP-B1, CUP-B2, H-10, SCHED-1
- [DESIGN_CUP_BRACKET_SYSTEM_V1.md](DESIGN_CUP_BRACKET_SYSTEM_V1.md)
