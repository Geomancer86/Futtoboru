# CM93/94 Manual — Menu & Feature Extract

**Source:** [CM93_Manual_LemonAmiga_Extract.md](CM93_Manual_LemonAmiga_Extract.md) (Lemon Amiga #315)  
**Derived for:** [CM93_94_REFERENCE.md](../../../CM93_94_REFERENCE.md) · [SYSTEMS_AND_FEATURES_REGISTRY.md](../../../SYSTEMS_AND_FEATURES_REGISTRY.md)  
**Date:** 2025-06-25 (Round 8)

---

## Startup

| Step | Detail |
|------|--------|
| Load | Disk 1 internal (autoboot); disk 2 in external if present |
| Printer | **Printer On/Off** at startup; **P** prints most screens |
| Quit | Always save + **Quit** when finished |

## New game paths

| Mode | Detail |
|------|--------|
| **New Game** | `1993 Players` (real names) or `Generate Players` |
| Hotseat | **1–4** managers; pick club, personality, name |
| Generation time | ~**30 minutes** on Amiga (manual) |
| **Quick Start** | Pre-generated real-data save; default club **Man Utd**; can resign and rejoin |
| **Continue Season** | Load saved game |

---

## Main menu

| Option | Function |
|--------|----------|
| **Continue Season** | Next fixtures |
| **View Tables** | League [±DIV]; manager rankings [±SCR]; group tables [±GRP]; print PRT; click player → factfile; click team → squad |
| **Fixture Info** | Past/upcoming fixtures; competition winners; **European seeding** |
| **Club Details** | Any of **80 clubs** — squad screen |
| **Arrange Friendly** | Pick week → Select Opponents → venue; cancel fixture; opponent details |
| **Match Reports** | Match statistics by competition |
| **Find Player** | Transfer search entry |
| **Board/Resign** | Board confidence; resign |
| **Manager Jobs** | Add player; managers facing axe; vacancies; management history |
| **National Squads** | England, Scotland, Wales, Ireland, NI — senior + **U21** |
| **Change Picture** | Background / colour / brightness |
| **Save Game** | Save to disk |

**NEW vs CM1:** Arrange Friendly · European seeding · U21 national squads · Change Picture.

---

## Squad details screen

### Player status (left/right of name)

| Code | Meaning |
|------|---------|
| IJ | Injured |
| SU | Suspended |
| U | Unhappy |
| L | Listed / available for loan |
| R | Transfer listed by request |
| C | Out of contract |
| 1–GK | Matchday squad pick |

### Position letters

G · D · M · A · L · R · C

Cyan names = **youth promotions** at season start.

### Squad sub-screens

| Button | Function |
|--------|----------|
| **TRNS** | Season bought/sold/loaned log |
| **STAFF** | Backroom staff; dismiss |
| **LEAG** | League position chart per match |
| **FIXL** | Fixture list (next in green); past opponent → match stats |
| **ACCS** | Weekly income/expenditure (AI clubs block) |
| **RSRV** | **Reserve team** view + selection **NEW** |
| **INFO** | Hub → PRINT / HIST (10 yr) / RECS (records) |
| **AVR** | Season average match ratings |
| **DSP** | Disciplinary points table |
| **GLS** | Goals scored |
| **MOM** | Man of the Match awards |
| **PEN** | Penalty taker order |

### Human-controlled extras

| Button | Function |
|--------|----------|
| **PRV** | Last match stats |
| **TCT** | Tactics editor |
| **OPP** | Opponent squad (pre-match) |
| **DEFS / MIDS / ATTS** | Position tables + strengths + avg ratings |
| **CLR / SWP / TACT / STYLE / CAPT / FORW / BACK / NORM** | Selection & tactics (see below) |
| **APPL** | Apply for vacant manager job |

---

## Teams and tactics

### Selection

| Action | Steps |
|--------|-------|
| Pick XI | Shirt number → player name |
| Swap shirts | Shirt → [SWP] → second shirt |
| **Reserve pick** | **[RSRV]** → shirt → player **NEW** |

### Tactics editor [TCT]

| Action | Detail |
|--------|--------|
| Assign position | Shirt → [DEF/MID/ATT] + [LEFT/RIGHT/CENTRE] icons |
| Swap positions | Shirt → [SWAP WITH] → second shirt |
| Captain | Shirt → captain assign |
| Formation | [NEW FORMATION] → sub-menu |
| Style | [STYLE] → sub-menu |
| Push/pull | [FORW] / [BACK] / [NORM] on shirt |

### In-match subs

Shirt → [SWAP WITH] → sub shirt **12, 14, or GK**

---

## Transfers (Find Player)

| Area | Detail |
|------|--------|
| **Normal Search** | Position, attributes, value, division, age filters |
| **Scout Search** | Assign scout; [RECS] for scout recommendations |
| **Foreign Players** | Overseas pool; [BUY] factfile; agent negotiates fee |
| **Shortlist** | Watch list |
| **Transfer List** | Listed players |
| **Scout reports** | Scouts → [LAST REPORT] |
| **Buying** | Multi-round fee negotiation or stalemate |

### Squad player actions (from factfile / squad)

List/Loan · Remove from list · Revalue · Not For Sale · Allow Approaches · **Fine** · **Insure / Uninsure** · Increase Wage · Renew Contract

---

## Backroom staff [STAFF]

| Role | Function |
|------|----------|
| Coach | Training |
| Physio | Fitness / injury recovery |
| **3× League Scouts** | Recommend signings **NEW** |
| **Youth Scout** | Youth recommendations **NEW** |

Sack via [SACK]; replace yourself or board appoints.

---

## Discipline (manual rules)

| Event | Points |
|-------|--------|
| Booking | 5 pts |
| Sending off | 10 pts |
| Booked + sent off same match | 10 pts total |
| **20 pts** | **2-match ban** (may be delayed if squad thin) |
| Squad discipline total | Risk of **fine** |

---

## NEW vs CM1 (extract summary)

| CM93 addition | CM1 had? |
|---------------|----------|
| Real 1993 player names | ❌ |
| Reserve squad [RSRV] | ❌ |
| Arrange friendlies | ❌ |
| Foreign player pool + scouts | ❌ (limited intl) |
| Penalty taker order [PEN] | ❌ |
| MOM table | ❌ |
| European seeding in fixtures | ❌ |
| U21 national squads | ❌ |
| Youth scout + 3 league scouts | ❌ (basic staff only) |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 8 — structured extract from Lemon Amiga manual text |
