# Championship Manager 1 — Complete Reference (Round 3)

**Release:** Championship Manager (1992) — also *Championship Manager '92*  
**Predecessor context:** [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) (Soccerama → Toms FM → Collyer BBC tracker)  
**Parent:** [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) · [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md)  
**Gap vs Futtoboru:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)  
**Research status:** **Sufficient for v1** — [CM_LEGACY_RESEARCH_PAUSE.md](CM_LEGACY_RESEARCH_PAUSE.md)  
**Date:** 2025-06-25  
**Research round:** 3 (v1 benchmark — **priority over CM3 manual extraction**)

---

## 1. Identity & commercial context

| Field | Detail | Source |
|-------|--------|--------|
| **Developer** | Paul & Oliver Collyer via **Intelek** | Wikipedia, MobyGames |
| **Publisher** | **Domark** | ✅ |
| **Release** | **May 1992** (Wikipedia); **Sep 1992** Amiga/ST retail (MobyGames, reviews) | ⚠️ date conflict |
| **Platforms** | Amiga, Atari ST, MS-DOS | ✅ |
| **Genre** | Football management simulation | ✅ |
| **Working title** | **European Championship** (MobyGames SI entry) | ⚠️ |
| **Predecessor (code)** | Collyers' BBC Micro league tracker (**1983**) | Wikipedia, Shropshire Star |
| **Genre inspiration** | Kevin Toms' *Football Manager* (**1982**) — separate codebase | Wikipedia |
| **Launch sales** | ~**20,000** copies | Wikipedia ⚠️ |
| **Price (Amiga)** | **£24.99** | The One Amiga #46 |
| **Composer** | **Barry Leitch** (Amiga/ST music) | MobyGames credits |
| **Tagline** | "Make Dreams Become A Reality" | Domark marketing |

### Origin story (developers)

1. Teenage brothers in **Shropshire** (poor TV signal — results from radio, typed into BBC Micro for league tables).  
2. Expanded attributes beyond Toms' skill/pace model; built full management prototype in bedroom.  
3. **Rejected by multiple publishers** including **Electronic Arts** — "not enough live action" (text-only matches).  
4. **Domark accepted**; shipped as first *Championship Manager*.  
5. Data from publications like **Rothmans Football Yearbook** ⚠️.  
6. **Sports Interactive** formalised **July 1994** (company registered 1995).  
7. Easter egg: Collyers inserted themselves as **below-average players** in database (MobyGames tradition).

### 1992 competitive landscape

CM1 launched into a **crowded year** — see [PRE_CM1_GENRE_HISTORY.md §5](PRE_CM1_GENRE_HISTORY.md#part-5--the-1992-market-cm1-launch-year):

| Rival | Publisher | CM1 comparison |
|-------|-----------|----------------|
| **Premier Manager** | Gremlin | Real 92/93 names; Conference; training/tactics depth |
| **The Manager** | US Gold | Real 91/92 names; Bundesliga Manager reskin |
| **Football Manager 3** | Addictive/Prism | Last Toms line; poor sales — genre baton passed |
| **Super League Manager** | Audiogenic | Cited in Amiga reviews as alternative |

**Historical irony:** CM1 lost 1992 realism wars (anonymous players, 20-team divisions vs real 22–24) but won long-term on **depth, updates, and database culture** (CM2 → CM97/98 → CM3).

---

## 2. Match engine & presentation

| Aspect | CM1 implementation |
|--------|-------------------|
| **Engine type** | **Text-only** scrolling match commentary |
| **Visual pitch** | None — no 2D/3D action |
| **Backgrounds** | Digitised ground photos on menus (Amiga review: "attractive backdrops") |
| **Player ratings** | **0–10 average after each match** — **first in genre** ✅ |
| **Match flow** | Calendar week: weekday actions → weekend match → ratings |
| **Advance time** | Press **Done** to progress |
| **In-match control** | Interrupt commentary for **subs/tactics** (limited vs CM3+) |
| **Printer output** | Optional **video printer** match transcript (Amiga review) ✅ |

---

## 3. Database & world scope

| Metric | Value | Notes |
|--------|-------|-------|
| English divisions | **4** (Divisions 1–4) | Pre-Premier League naming |
| Teams per division | **20 each** | ⚠️ Real 1992 had 22–24 — review criticism |
| Playable clubs | **~80** | |
| Cup / European opponents | **~380** | Playable in cups; not full squads |
| Named players (English leagues) | **No** — "No.3", "No.10" | Foreign stars separate mechanic |
| Staff in DB | **~650** managers, coaches, scouts, physios | |
| Player records | **~1,500** | Skills, fitness, age, morale, attitude, **5-year history** |

### Competitions

| Competition | In CM1 |
|-------------|--------|
| **FA Cup** | ✅ |
| **League Cup** | ✅ |
| **Anglo-Italian Cup** | ✅ |
| **Domark Trophy** | ✅ (promotional — review criticism) |
| **European cups (3)** | ✅ incl. **Cup Winners' Cup** |
| **Full English pyramid** | ✅ structure |
| **International management** | ❌ not full career mode |

---

## 4. Core gameplay systems

### 4.1 Management loop

1. New game or load season disk  
2. Pick club (**hotseat up to 4** managers)  
3. Set tactics / lineup  
4. Advance week → match text → **0–10 ratings**  
5. Transfers, squad management between matches  
6. **Season ends with champion** — full loop ✅ (Futtoboru P0 gap)

### 4.2 Tactics

- Formations and team selection via menus  
- Mouse-driven large text boxes  
- No per-player instruction depth of CM3+

### 4.3 Transfers

- Approach players; club acceptance; wage/contract negotiation  
- Domestic focus; **overseas players** handled separately  
- No formal transfer windows

### 4.4 Training

- **Basic training options exist** (Amiga Reviews 1992 — not absent)  
- No CM3-style weekly schedules or individual focus  
- Fitness and morale tracked

### 4.5 Board & job security

- Basic board expectations  
- **Resign** from main menu  
- Sacking for poor performance (basic)

### 4.6 Multiplayer

- **Hotseat up to 4 players** on one machine ✅ (manual confirms)

### 4.7 Save system

- Requires **blank formatted season disk** (Amiga/ST)  
- Reusing disk **wipes prior season** — manual warns explicitly  
- MS-DOS: run `EUROPE` from season disk

---

## 5. UI & manual (official)

**Primary manual:** Domark Amiga manual — [GamesDatabase HTML scan](https://www.gamesdatabase.org/Media/SYSTEM/Commodore_Amiga/Manual/formated/Championship_Manager_-_1992_-_Domark_Software.htm)

| Manual section | Content |
|----------------|---------|
| Starting up | DF0 program disk; DF1 season disk; PC `EUROPE` command |
| New game | Player count (max 4); club; personality; manager name |
| Season disk | Format blank disk (Amiga Workbench / ST TOS) |
| Main menu | Continue, tactics, transfers, tables, cups, resign |
| Printer | Optional match printout |

**Round 4 planned:** ~~Full menu tree extraction from HTML manual~~ → Done: [CM1_Manual_Menu_Extract.md](docs/research/manuals/cm1/CM1_Manual_Menu_Extract.md)

---

## 6. Localized & derivative releases

CM1 engine reused for non-English markets with **real player names** where licensing allowed:

| Title | Year | Market | Publisher | Notes |
|-------|------|--------|-----------|-------|
| **Guy Roux Manager** | 1993 | France | Ubisoft / Intelek | Same Collyer engine; French league |
| **Championship Manager (NO/IT)** | ~1993 | Norway, Italy | — | Localized with real names ⚠️ verify per market |

See [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md) for manual links.

---

## 7. Expansions & updates

| Product | Notes |
|---------|-------|
| **CM93/94** | Separate sequel (1993), not in-place patch |
| **Season data updates** | No official CM1 disk patches |
| **Add-on discs** | None for CM1 |

CM1 superseded by **CM93/94** rather than patched.

---

## 8. Community & preservation (modern)

| Resource | Role |
|----------|------|
| [MobyGames CM1](https://www.mobygames.com/game/469/championship-manager/) | Credits, description, Barry Leitch |
| [cm0001.free.fr](http://cm0001.free.fr/) | French CM archive; historical interviews |
| [championshipmanager.co.uk](http://www.championshipmanager.co.uk/) | Fan site; claimed legal CM1 download ⚠️ verify |
| [OpenRetro](https://openretro.org/) | Preservation uploads |
| **cm9798.co.uk podcast** | Retrospective CM1 coverage |

**Active modding:** Minimal — emulation (Amiga/DOSBox) and preservation only.

---

## 9. Media & press (1992)

### Marketing

| Source | Detail |
|--------|--------|
| **Daily Mirror** | **Graeme Souness** endorsement quote (May 1992 launch) | Wikipedia |
| Domark | "Make Dreams Become A Reality" |

### Review scores (Amiga + multi-platform)

**Curated high scores (design research focus):**

| Publication | Score | Notes |
|-------------|-------|-------|
| **The One Amiga** #46 | **89%** | £24.99; depth praised; vs Super League Manager — **primary text in repo** ([extract](docs/research/sources/magazines/MAGAZINE_REVIEW_EXTRACTS.md)) |
| **CU Amiga** | **84%** | |
| **Amiga Reviews** (Level Up) | Positive | vs **The Manager** (US Gold); wrong div sizes; fake names |
| **Amiga Format** | Listed | See [Amiga Magazine Rack](http://amr.abime.net/review_418) |

**Full MobyGames critic table (shows era polarization):**

| Publication | Platform | Score |
|-------------|----------|-------|
| **ST Action** | Atari ST | **93%** |
| **Amiga Mania** | Amiga | **88%** |
| **The One Amiga** #46 | Amiga | **89%** |
| **CU Amiga** | Amiga | **84%** |
| **Zero** | Amiga | **82%** |
| **Press Play On Tape** | DOS | **80%** |
| **Amiga Format** | Amiga | **41%** |
| **Amiga Power** | Amiga | **38%** |
| **Amiga Joker** | Amiga | **27%** |

MobyGames critic average: **67%** (8 ratings). See [CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md](CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md) for themes and Round 8 primary-scan backlog.

**Review themes:**

| Praise | Criticism |
|--------|-----------|
| Stat depth (~1500 players) | Anonymous English players |
| Cups + Europe | 20-team divisions (not 22–24) |
| Mouse menus | Text-only (expected for genre) |
| Video printer | Domark Trophy self-promotion |
| Hotseat 4-player | Complexity for casual players |

No ELSPA blockbuster at CM1 scale — breakthrough sales **CM2 / CM97/98**.

---

## 10. Developers & key people (CM1 era)

| Person | Role |
|--------|------|
| **Paul Collyer** | Lead programming |
| **Oliver Collyer** | Programming; attribute design |
| **Barry Leitch** | Music (Amiga/ST) |
| **Domark** | Publisher → Eidos chain |

**Not on CM1:** Ray Houghton (consultant CM4), Marc Vaughan (CM3+ database lead).

---

## 11. Futtoboru comparison — CM1 v1 benchmark

CM1 is the **minimum shippable management sim** Futtoboru **V1 MVP (Layer B)** should match — not CM3/CM4 depth.

**Canonical Layer B design:** [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md)

| CM1 feature | Futtoboru (2026-06-27) | Horizon |
|-------------|-------------------------|---------|
| Full season completes + rollover | ✅🧪 Proven headless | H1 ✅ |
| Correct league rules (1888 2-1-0) | ✅🧪 Loader + IT; **UI verify open** (A-5) | H1 |
| League table | ✅ | H1 |
| Champion declared | ✅🧪 | H1 |
| Text/abstract match uses **your XI** | ❌ Squad avg + RNG | **H2 — MVP-3** |
| Player ratings 0–10 | ❌ | **H2 — MVP-5** |
| Formations + pick 11 | 🔲 Stub | **H2 — MVP-1/2/9** |
| Credible attendance | ❌ Random | **H2 — MVP-4** |
| FA Cup / cups | ✅🧪 headless | H1 |
| Transfers | ❌ | Post–Layer B |
| Real player names | ✅ **1888 researched** (ahead of CM1) | — |
| Save game | ✅🧪 JSON roundtrip | H1 |

**Critical path:** Layer A manual QA → **Layer B** per [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) → 1892 → world/V2.

---

## 12. Round 4 research backlog

| Item | Source | Output |
|------|--------|--------|
| Full manual menu tree | GamesDatabase HTML | Registry update |
| The One #46 pages 92–93 | ✅ `The_One_046_Jul_1992_djvu.txt` | Press appendix — Gary Whitta 89% |
| Guy Roux Manager feature diff | MobyGames / cm0001 | CM1 supplement |
| European Championship prototype | MobyGames SI | Pre-CM1 doc |
| DOS vs Amiga feature parity | Emulator testing | Platform matrix |

---

## 13. Sources

- [Championship Manager (1992) — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager_(video_game))
- [Championship Manager — MobyGames](https://www.mobygames.com/game/469/championship-manager/)
- [Collyer brothers — Wikipedia](https://en.wikipedia.org/wiki/Collyer_brothers_(game_designers))
- [Amiga Reviews — CM92](https://www.amigareviews.leveluphost.com/champi92.htm)
- [Amiga Magazine Rack — The One #46](http://amr.abime.net/review_418)
- GamesDatabase Amiga manual HTML
- [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md)

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 16 — research paused; CM1 sources sufficient for v1 MVP |
| 2025-06-25 | Round 11 — The One #46 primary review text in repo; press backlog closed |
| 2025-06-25 | Round 3 — press table, 1992 competitors, localized versions, EA rejection, training correction, v1 benchmark |
| 2025-06-25 | Round 2 deep reference created |
