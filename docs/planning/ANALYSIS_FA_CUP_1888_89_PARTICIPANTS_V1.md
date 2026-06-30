# Analysis — 1888–89 FA Cup First-Round Participants vs Game Data

**Document type:** Historical data audit (analysis only)  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Sources:** [Wikipedia 1888–89 FA Cup](https://en.wikipedia.org/wiki/1888%E2%80%9389_FA_Cup), `mods/seasons/18/clubs.txt`, `competition_edition.txt`, `ScriptsLoader.java`

---

## Executive summary

**Yes — the first FA Cup edition uses the correct 32 clubs for the first round proper.**

| Check | Result |
|-------|--------|
| Participant count | **32** (mod, script, runtime after May 1 reuse) |
| All IDs resolve in `clubs.txt` | **Yes** (`CompetitionsLoaderTests.loadCompetitionParticipantClubsTest`) |
| Match Wikipedia first round proper (16 ties) | **32/32** |
| Missing clubs blocking v1 first round | **None** |
| Qualifying-only clubs in DB | **Not required** for current scope (qualifying not modelled) |

No new club records are needed **for the first round proper** in season 18. Optional future work is qualifying-round clubs (e.g. Warwick County) if we model pre–first-round ties.

---

## Runtime path (which list wins?)

1. **`CompetitionsLoader`** loads edition id `1` from `mods/seasons/18/competitions/1/competition_edition.txt` with **32 club IDs**.
2. **`ScriptsManager.createCup()`** (May 1) **reuses** that edition by season name (`1888-89`) and **does not overwrite** participants when the mod list is already populated.
3. **`ScriptsLoader`** cup script uses the **same 32 IDs** as the mod file (aligned in commit `e89887a`).
4. **`CupBracketGenerator`** draws from `edition.getParticipantClubsIds()` → **32-team bracket**.

The old bug (12 league clubs only, or duplicate editions) is fixed; the **data** was largely correct before scheduling fixes.

---

## Historical first round proper (2 Feb 1889) — full mapping

Wikipedia lists **16 ties = 32 teams**. Game id → `clubs.txt` name:

| Tie | Home (hist.) | Away (hist.) | Game ID | Game name |
|-----|--------------|--------------|---------|-----------|
| 1 | Burnley | Old Westminsters | 9, 13 | Burnley, Old Westminsters |
| 2 | Swifts | Wrexham | 14, 16 | Swifts, Wrexham |
| 3 | Notts County | Old Brightonians | 11, 15 | Notts County, Old Brightonians |
| 4 | Nottingham Forest | Linfield Athletic | 17, 18 | Nottingham Forest, Linfield |
| 5 | Aston Villa | Witton | 2, 19 | Aston Villa, Witton Albion |
| 6 | Bootle | Preston North End | 20, 1 | Bootle, Preston North End |
| 7 | Accrington | Blackburn Rovers | 7, 4 | Accrington, Blackburn Rovers |
| 8 | Chatham | South Shore | 21, 22 | Chatham Town, South Shore |
| 9 | Grimsby Town | Sunderland Albion | 23, 24 | Grimsby Town, Sunderland Albion |
| 10 | Crewe Alexandra | Halliwell | 26, 25 | Crewe Alexandra, Halliwell |
| 11 | Wolverhampton Wanderers | Old Carthusians | 3, 27 | Wolverhampton Wanderers, Old Carthusians |
| 12 | Derby County | Derby Junction | 10, 28 | Derby County, Derby Junction |
| 13 | Notts Rangers | The Wednesday | 29, 30 | Notts Rangers, Sheffield Wednesday |
| 14 | Birmingham St George's | Long Eaton Rangers | 31, 32 | Birmingham St George's, Long Eaton Rangers |
| 15 | Small Heath | West Bromwich Albion | 33, 6 | Small Heath, West Bromwich Albion |
| 16 | Walsall Town Swifts | Sheffield Heeley | 34, 35 | Walsall, Heeley |

**Mod/script ID order** (same set):  
`9,13,14,16,11,15,17,18,2,19,20,1,7,4,21,22,23,24,25,26,3,27,10,28,29,30,31,32,33,6,34,35`

---

## Football League clubs — in / out of first round

| ID | Club | In 1888–89 FL | In FA Cup 1st round proper | Historical reason |
|----|------|---------------|----------------------------|-------------------|
| 1 | Preston | Yes | Yes | Invited / played Bootle |
| 2 | Aston Villa | Yes | Yes | |
| 3 | Wolves | Yes | Yes | |
| 4 | Blackburn | Yes | Yes | |
| 5 | Bolton | Yes | **No** | Lost qualifying (Linfield, Belfast) |
| 6 | West Brom | Yes | Yes | |
| 7 | Accrington | Yes | Yes | |
| 8 | Everton | Yes | **No** | Withdrew (drawn vs Ulster, Belfast) |
| 9 | Burnley | Yes | Yes | |
| 10 | Derby County | Yes | Yes | |
| 11 | Notts County | Yes | Yes | Only FL club to qualify via qualifying |
| 12 | Stoke | Yes | **No** | Lost qualifying (Warwick County) |

**Correct:** Bolton, Everton, and Stoke are in `clubs.txt` but **not** in the cup participant list — matching history.

---

## Naming approximations (acceptable for v1)

| Historical name | Game name | Notes |
|-------------------|-----------|--------|
| Witton | Witton Albion (19) | Same club; Wikipedia short name |
| Walsall Town Swifts | Walsall (34) | Lineage to Walsall FC; mod founded 1888 |
| Sheffield Heeley | Heeley (35) | Same club |
| The Wednesday | Sheffield Wednesday (30) | |
| Linfield Athletic | Linfield (18) | State id `83` (Ireland) in mod |

No separate mod entries required unless we want exact historical display names in UI.

---

## Database coverage (season 18)

| Asset | Scope |
|-------|--------|
| `clubs.txt` | **35** clubs (12 FL + 23 cup-only/non-FL) |
| Cup participants | **32** of those 35 |
| Not in cup | **5** Bolton, **8** Everton, **12** Stoke (see above) |
| `club_players/` + `club_stadiums/` | Present for all **35** ids (including all 32 cup sides) |

---

## Not in game data (qualifying only — out of v1 scope)

If we later model **qualifying rounds**, these historical clubs are **not** in season 18 data today:

| Club | Role 1888–89 |
|------|----------------|
| Warwick County | Beat Stoke (1st qualifying) |
| Ulster | Everton withdrew rather than travel |
| Others | Further qualifying ties (see Wikipedia qualifying rounds page) |

Adding them would be **enhancement**, not a blocker for the current “32-team first round proper” design.

---

## Conclusion & recommendations

| Priority | Action |
|----------|--------|
| **None (blocker)** | First cup edition already uses proper 32 teams with full squads/stadiums |
| **Low** | UI display names for Witton / Walsall Town Swifts / Sheffield Heeley if desired |
| **Future** | Qualifying round mod + scripts; add Warwick County, Ulster, etc. |
| **Done** | Keep `ScriptsLoader` IDs synced with `competition_edition.txt` (regression guard) |

---

## Related docs

- [DESIGN_KNOCKOUT_BRACKET_V1.md](DESIGN_KNOCKOUT_BRACKET_V1.md) — bracket / scheduling
- [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) — season 18 snapshot
- [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) — data ownership
