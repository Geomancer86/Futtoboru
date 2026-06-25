# CM1 Manual — Menu & Feature Extract

**Source:** `Championship_Manager_1992_Amiga_Manual.html` (Domark, 20 pages)  
**Derived for:** [SYSTEMS_AND_FEATURES_REGISTRY.md](../../../SYSTEMS_AND_FEATURES_REGISTRY.md) mapping  
**Date:** 2025-06-25

---

## Startup

| Platform | Load |
|----------|------|
| Amiga | Program disk DF0; season disk DF1 optional |
| Atari ST | Drive A / B |
| PC | Type `EUROPE` at prompt |
| Printer | Optional — Printer On at startup |

## New game / save

| Step | Detail |
|------|--------|
| Hotseat | **1–4 players**; each picks club, personality, name |
| Season disk | **Blank formatted disk required**; reusing wipes prior season |
| Load | Insert season disk → resumes at main menu |

---

## Main menu (pages 3–4)

| Option | Function |
|--------|----------|
| **Continue season** | Next fixtures |
| **View tables** | League (+/- DIV), manager rankings (+/- SCR), Domark Trophy groups, print (PRT) |
| **Fixture info** | Past results, upcoming fixtures, European qualification |
| **Club details** | Any of 80 clubs — squad screen |
| **Match reports** | Stats by competition |
| **Find player** | Player search / transfers entry |
| **Board / resign** | Board confidence; resign |
| **Manager jobs** | Add player; managers facing axe; vacant jobs |
| **National squads** | England, Scotland, Ireland, Wales, NI senior + U21 |
| **Change picture** | Background / brightness |
| **Save game** | Save current state to season disk |

---

## Squad details screen (pages 5–10)

### Player status icons

| Icon | Meaning |
|------|---------|
| IJ | Injured |
| SU | Suspended |
| U | Unhappy |
| R | Transfer listed (club or request) |
| 1–14 | Picked for match day |
| C | Out of contract |

### Position letters

G, D, M, A, L, R, C (goalkeeper through centre)

### Sub-screens

| Button | Function |
|--------|----------|
| TRANS | Season bought/sold log |
| STAFF | Backroom staff; dismiss |
| LEAG | League position chart after each match |
| FIXL | Fixture list (next in green) |
| ACCS | Weekly income/expenditure |
| INFO | General info hub |
| PRINT | Print squad / transfers / results |
| HIST | 10-year club achievements |
| RECS | Club records |
| DISP | Disciplinary points |
| GOAL | Goals by player |
| TEAM / PREV / OPPS | AI club tactics; last match; opponent squad |
| DEFS / MIDS / ATTS | Position tables with strengths + **avg match rating** |
| CLR / SWP / TACT / STYLE / CAPT / FORW / BACK / NORM | Team selection & tactics |

---

## Match flow (pages 10–15)

| Feature | Detail |
|---------|--------|
| Team selection | Shirt numbers 1–14; CLR deselect; SWP swap |
| Tactics editor | TACT — formation diagram; predefined tactics; STYLE sub-menu |
| Push/pull | FORW / BACK / NORM on players (not GK/sweeper) |
| In-match subs | H/A keys or mouse buttons; tactical move or sub (shirts 12–14) |
| Speed up | Hold **SPACEBAR** when losing badly |
| Post-match | **Ratings 1–10** per player; IJ, C, SO flags; goal count suffix |
| Attendance | Shown on stats screen |

---

## Transfers (pages 16–20)

| System | Detail |
|--------|--------|
| Player search | Position, attributes, value, division filters; scout RECS recommendations |
| Shortlist | ADD / DEL |
| Transfer list | League-wide listings |
| Approach | BUY on factfile; club release + player interest |
| Multi-club bidding | Shortlisted clubs can join |
| Fee negotiation | Offer/ask rounds; tribunal if out of contract |
| Negotiations UI | +/- digits, DONE, FACTFILE |
| Player status (STA) | List, remove, revalue, not for sale, fine, insure, wage, renew contract |
| Player requests | Leave/stay requests; cannot refuse OOC leave |

---

## Futtoboru v1 mapping

| CM1 manual feature | Futtoboru status |
|--------------------|------------------|
| Continue season → champion | ❌ P0 |
| View tables / print | ⚠️ partial |
| Board confidence | ❌ stub |
| Manager jobs (multiplayer add) | ❌ |
| 1–10 match ratings | ❌ P1 |
| Transfers / shortlist / bidding | ❌ post-v1 |
| National squads display | ❌ |
| Save to season disk | ⚠️ JSON partial |

**Note:** CM1 manual describes **far more** than v1 needs — v1 gate is **season completes + rules + table**, not full transfer engine.
