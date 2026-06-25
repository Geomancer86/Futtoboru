# CM Legacy — Deep Manual Analysis by Release

**Document type:** Manual-derived feature analysis (basic systems per release)  
**Sources:** Local archive in [docs/research/manuals/](docs/research/manuals/)  
**Companion:** [CM_MANUALS_COLLECTION.md](CM_MANUALS_COLLECTION.md) · [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md)  
**Date:** 2025-06-25  
**Round:** 5 (analysis pass — not yet full registry mapping)

---

## Short answer: do we have deep analysis of all manuals?

**No — not yet for every release.** Coverage today:

| Release | Manual in repo | Analysis depth | Primary doc |
|---------|----------------|----------------|-------------|
| **FM 1982** (precursor) | ✅ HTML | **Deep** | §1 below |
| **CM1** (1992) | ✅ HTML + extract | **Deep** | §2 + [CM1_Manual_Menu_Extract.md](docs/research/manuals/cm1/CM1_Manual_Menu_Extract.md) |
| **CM93/94** | ✅ | **Medium–Deep** (text + menu extract) | §3 |
| **CM2** (1995) | ✅ FR PDF + EN guide | **Medium** | §4 |
| **CM97/98** (1997) | ✅ PDF (image scan) | **Shallow** | §5 |
| **CM3** (1999) | ✅ PDF + OCR TOC | **Medium–deep** | §6 + [CM3_Manual_TOC_Extract.md](docs/research/manuals/cm3/CM3_Manual_TOC_Extract.md) |
| **CM4** (2003) | ⚠️ ReadMe only | **Shallow** | §7 |

**Legend:** Deep = full manual read · Medium = partial manual + supplements · Shallow = TOC/ReadMe or secondary · None = no manual

**Still needed:** OCR CM2 FR PDF + CM97/98 PDF; CM4 full printed manual; CM93/94 original PDF scan (text extract done).

---

## Master table — basic features by release

Features marked **M** = confirmed in manual/docs in repo · **S** = secondary (MobyGames, CM_*_REFERENCE, community) · **—** = absent

| Feature | FM82 | CM1 | CM93 | CM2 | CM9798 | CM3 | CM4 |
|---------|:----:|:---:|:----:|:---:|:------:|:---:|:---:|
| Season loop + promotion | M | M | S | S | S | M | S |
| FA/domestic cups | M | M | S | S | S | M | S |
| Pick XI / formation | M | M | S | M | S | M | S |
| Text match + ratings | M | M | S | M | S | M | S |
| Transfers / market | M | M | S | M | S | M | S |
| Finances / wages | M | M | S | S | S | M | S |
| Board / job pressure | — | M | S | S | S | M | S |
| Hotseat multiplayer | — | M | S | S | S | M | S |
| 0–10 match ratings | — | M | S | S | S | M | S |
| Real player names (English) | M | — | S | S | S | S | S |
| Reserve squads | — | — | S | S | S | M | S |
| Injury time | — | — | S | S | S | M | S |
| Scotland / dual nation | — | — | S | M | S | M | S |
| Audio commentary | — | — | — | M | S | — | S |
| Player attribute model (20+ attrs) | — | — | — | M | S | M | S |
| Per-player runs / drag tactics | — | — | — | M | S | M | S |
| Transfer deadlines (grey names) | — | — | — | M | S | M | S |
| Apply for vacant jobs | — | M | S | M | S | M | S |
| Data editor | — | — | — | — | S | M | S |
| Multi-league career (3 active) | — | — | — | — | S | M | S |
| Custom training schedules | — | — | — | — | — | M | S |
| Assigned scouts | — | — | — | — | — | M | S |
| Player instructions (marking etc.) | — | — | — | M | S | M | S |
| Loans | — | — | — | — | — | M | S |
| Contract clauses | — | — | — | — | — | M | S |
| Network / LAN multiplayer | — | — | — | — | — | M | S |
| National team career | — | — | — | S | S | M | S |
| 2D match engine | — | — | — | — | — | — | S |
| Training visual indicators | — | — | — | — | — | — | S |
| Media / press Q&A | — | — | — | — | — | — | S |

---

## §1 — Football Manager (1982) · Kevin Toms

**Manual:** `docs/research/manuals/precursors/football-manager-1982-spectrum/Football_Manager_1982_Spectrum_Manual.html`  
**Analysis depth:** **Deep**

### Core loop

| Phase | Systems |
|-------|---------|
| **Part 1 — Start** | Manager name; pick any club (starts Div 4); beginner difficulty |
| **Part 2 — Week** | 5 phases: optional actions → match → gate money → bills → transfer market |
| **Part 3 — Season end** | Final table; prize money; **3 promoted / 3 relegated**; skill/energy drift; infinite seasons |

### Basic features (manual)

| System | Detail |
|--------|--------|
| **Squad** | 16 max; skill **1–5**; energy **1–20**; D/M/A roles |
| **Team aggregates** | Defence / Midfield / Attack totals; morale; energy average |
| **Match** | Pick XI; **3D highlight** goals (Spectrum); shock results possible |
| **Finances** | Weekly wages (division + squad skill); **bank loans** by division (£250k–£1M caps); 1% interest |
| **Transfers** | Random offers; list/sell; bid for market player |
| **Customization** | Rename all teams and players |
| **Save** | Tape save (~3¼ min) |
| **Season length** | 75 league + FA Cup |

### NEW in genre

First commercial **football management** video game — defines minimum viable product: table + fixtures + cups + multi-season.

---

## §2 — Championship Manager 1 (1992)

**Manual:** HTML 20 pp + [CM1_Manual_Menu_Extract.md](docs/research/manuals/cm1/CM1_Manual_Menu_Extract.md)  
**Analysis depth:** **Deep**

### Core loop

Weekday/weekend calendar → **Done** advances time → text match → **1–10 ratings** → between-match squad/transfer work → season completes.

### Basic features (manual)

| Category | Features |
|----------|----------|
| **World** | 4 English divisions (20 teams each); ~80 playable; ~380 cup opponents; **anonymous** league players |
| **Cups** | FA Cup, League Cup, Anglo-Italian, **Domark Trophy**, 3 European competitions |
| **Main menu** | Continue, tables, fixtures, club details, match reports, find player, **board/resign**, **manager jobs**, national squads, save |
| **Squad screen** | Staff; league chart; fixtures; finances; history; records; discipline; goals |
| **Tactics** | Shirt pick 1–14; predefined formations; STYLE; push/pull FORW/BACK; in-match subs/tactical moves |
| **Transfers** | Search, shortlist, transfer list; multi-club bidding; fee negotiation; tribunal (OOC) |
| **Player mgmt** | Fine, **insure**, list, not-for-sale, wage renew, player requests |
| **Multiplayer** | **Hotseat 1–4** |
| **Save** | Season disk (formatted blank) |

### NEW vs FM82 / genre

| NEW | Notes |
|-----|-------|
| **0–10 match ratings** | Genre innovation |
| **Hotseat 4** | |
| **European + multi-cup structure** | |
| **Board confidence** | |
| **Transfer bidding rounds** | |
| **Insurance / fines** | |

### NEW vs later CM (what CM1 lacks)

No real names (English); no reserves; no training schedules; no editor; no Scotland; no audio; no attribute spreadsheet (CM2).

---

## §3 — CM93/94 (1993–1994)

**Manual:** `CM93_Manual_LemonAmiga_Extract.md` + `CM93_Manual_Menu_Extract.md` + copy codes PDF  
**Analysis depth:** **Medium–Deep** — structured menu extract; no original PDF scan

### Main menu (manual — Lemon Amiga)

Continue Season · View Tables · Fixture Info · Club Details · **Arrange Friendly** · Match Reports · Find Player · Board/Resign · Manager Jobs · National Squads · Save/Quit

### Basic features (manual-verified)

| vs CM1 | Addition |
|--------|----------|
| **1993 real player names** | New Game → `1993 Players` |
| **Generated players option** | New Game → `Generate Players` |
| **Quick Start** | Pre-built save (Man Utd default) |
| **Reserve squad selection** | `[RSRV]` on squad screen |
| **Tactics editor `[TCT]`** | Position icons DEF/MID/ATT + L/R |
| **European seeding** | Fixture Info |
| **Scouts** | Transfers guidance section |
| **Disciplinary system** | 5 pts booking · 10 sending off · 20 pts = 2-match ban |
| **Copy protection** | Manual score table (PDF acquired) |

**Gap:** Original PDF scan; optional structured menu extract like CM1.

---

## §4 — Championship Manager 2 (1995)

**Manual:** `Championship_Manager_2_Manual_FR.pdf` (not OCR’d) + `Championship_Manager_2_Guide_EN.txt`  
**Analysis depth:** **Medium** (guide confirms CM2 engine features; French PDF pending OCR)

### Basic features (manual-adjacent — EN guide + CM2_REFERENCE)

| Category | Features |
|----------|----------|
| **Presentation** | Text engine; **SVGA ground photos**; optional **CD audio commentary** (Clive Tyldesley) |
| **Leagues** | **England OR Scotland** selectable at new game |
| **Tactics** | **15 presets** (Ultra Defensive → All Out Attack); custom; **5 horizontal slots** per row (sweeper→attack); **drag player runs** (right mouse arrows) |
| **Styles** | Long Ball · Passing · Direct |
| **In-match** | Change formation anytime; warned when opponent changes; **spacebar** speed |
| **Attributes** | **20+ visible categories** — footballing, GK, physical, mental, hidden (ability/potential/consistency/dirtiness) |
| **Transfers** | World market; **deadline** (grey names in search = closed window) |
| **Career** | Apply for vacant jobs (Manager Info → Job News); reputation after ~2 seasons |
| **Friendlies** | Full calendar arrange/cancel |
| **UI tricks** | Right-click Done = advance by **day**; home/away table toggle; match stats on score click |
| **Limit** | Career sim caps ~**2025** |

### NEW vs CM93/94

England/Scotland choice; SVGA + commentary; deeper tactics (runs); full attribute model; Bosman in later updates (secondary).

---

## §5 — CM97/98 (1997)

**Manual:** `Championship_Manager_97_98_Manual_DOS_EN.pdf` (~6 MB, **image scan — not text extracted**)  
**Analysis depth:** **Shallow** (pending OCR; bullets from [CM2_REFERENCE.md](CM2_REFERENCE.md) + SI history)

### Basic features (secondary until OCR)

| Category | Features |
|----------|----------|
| **Engine** | Final **CM2 engine** release |
| **Leagues** | **9 nations**; up to **3 concurrent** on 16 MB RAM |
| **Editor** | **First bundled data editor** |
| **Set pieces** | Designated takers |
| **U21 internationals** | |
| **Signing-on fees** | |
| **Distribution** | **No copy protection** (deliberate) |
| **Multiplayer** | Hotseat retained |

### NEW vs CM2

Editor; multi-nation concurrent career; 97/98 season data refresh; AI/debug polish.

**Round 6 action:** OCR PDF → replace S markers with M in master table.

---

## §6 — Championship Manager 3 (1999)

**Manual:** PDF + [CM3_Manual_TOC_Extract.md](docs/research/manuals/cm3/CM3_Manual_TOC_Extract.md) (Archive.org OCR)  
**Analysis depth:** **Medium–deep** (full TOC + partial body; scanned PDF not fully read page-by-page)

### Core loop (manual)

Main Menu → Start/Restore → **Menu Bar** (Continue · Manager · Competitions · Nations/Clubs · Find · Options) → First Eleven → Training → Tactics → Match (dressing room → watch → stats → subs) → Transfers/scouting → season rollover.

### Basic features (manual TOC + OCR body)

| Category | Features |
|----------|----------|
| **Setup** | Win 95/98 install; game settings; Hall of Fame; Quick Start; **Network play** |
| **World** | **Selected leagues** + **background leagues**; nations & clubs browser |
| **Squad** | First eleven; squad numbering; reserves screen; **control reserve team** |
| **Training** | **Squad training** + individual **training** screens |
| **Tactics** | Team instructions; **per-player instructions**; create/modify tactics |
| **Match** | Dressing room; overview; stats; **action zones**; team ratings; commentary speed; subs |
| **Scouting** | Quick search; **assigning scouts** |
| **Transfers** | Buy/sell/**loan**; fee negotiation; **contract negotiation**; **clauses** (fee after apps) |
| **Finance** | Club finances & info screen |
| **Fixtures** | Arrange friendlies |
| **International** | World Cup etc. referenced |

### NEW vs CM97/98 (landmark — CM_LEGACY + manual)

| NEW | Manual |
|-----|--------|
| **Engine rewrite** | — |
| **15 nations / 25k+ database** | Nations & Clubs |
| **Custom training schedules** | Training chapter |
| **Reserves + youth + trialists** | Reserves |
| **Full NT manager career** | World Cup text |
| **LAN/internet up to 16** | Network Play ×2 in TOC |
| **Per-player instructions** | Player Instructions |
| **Sidebar UI / multitasking** | Menu Bar structure |

---

## §7 — Championship Manager 4 (2003)

**Manual:** `Championship_Manager_4_ReadMe_Win_EN.pdf` only (**not** gameplay manual)  
**Analysis depth:** **Shallow** for manual · **Deep** for gameplay in [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)

### ReadMe covers (technical)

Install; system requirements; patches; www.championshipmanager.co.uk; enhancement pack pointers.

### Basic features (CM4_COMPLETE_REFERENCE — not from ReadMe)

| Category | Features |
|----------|----------|
| **Engine** | **First 2D match engine** + text |
| **World** | **39 nations**; ~200k database |
| **Training** | Schedules + **profile visual indicators** |
| **Board** | Wage structure; stadium upgrades |
| **Transfers** | EU windows; fog of war; player notes |
| **Media** | Press conferences (01/02+) refined; praise/criticise (EP) |
| **Other** | Surgery option; relationships; replay saved matches; pre-game editor |

### NEW vs CM3

2D engine; massive DB scale; fog of war; EU windows; board stadium; media depth.

**Gap:** Full printed manual (GameFAQs cite pp. 55–61 for tactics/indicators).

---

## Feature accumulation chart (by generation)

```text
1982 FM     ── season + cups + transfers + loans + 5-phase week
1992 CM1    ── + ratings + hotseat + Europe + board + bidding + insurance
1993 CM93   ── + real names + reserves + injury time + intl transfers
1995 CM2    ── + Scotland + SVGA + commentary + attribute model + run arrows
1997 CM9798 ── + 9 leagues + editor + 3 concurrent + set pieces
1999 CM3    ── + rewrite + training + scouts + loans + NT career + LAN
2003 CM4    ── + 2D + 39 nations + fog of war + windows + media + board depth
```

---

## Round 6 backlog (to reach “deep” everywhere)

| Priority | Task | Output |
|----------|------|--------|
| P0 | OCR `Championship_Manager_97_98_Manual_DOS_EN.pdf` | CM9798 section → all **M** |
| P1 | OCR `Championship_Manager_2_Manual_FR.pdf` (or find EN PDF) | CM2 menu tree |
| P1 | CM93/94 PDF scan (text extract done) | §3 optional menu extract |
| P2 | CM3 page-by-page OCR body (not just TOC) | Registry mapping |
| P2 | Hunt CM4 printed manual scan | Tactics/indicators from primary source |
| P3 | Map all **M** rows → [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) | Implementation traceability |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 5 — first deep-by-release analysis; honest coverage matrix; CM3 TOC extract |
