# FM Competitive Bible — Round 1 Entries

**Parent:** [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md)  
**Round:** 1 of 6 (planned)  
**Date:** 2025-06-25

Each entry uses a standard template. Fields marked 🔲 need Round 2+ verification.

---

## Entry template (reference)

```text
Release │ Ship │ Platform │ Countries │ Leagues │ Teams │ Players/staff │ Researchers
Match engine │ Season start │ Key simulation features │ Licensing notes │ Sources │ Confidence
```

---

# Part A — The split and CM lineage (2003–2005)

## A1. The 2003 split (context)

| Field | Detail |
|-------|--------|
| **What happened** | Sports Interactive (SI) and publisher Eidos ended their partnership. Eidos retained the **Championship Manager** trademark. SI retained the **source code, database, and research network**. |
| **First SI FM** | **Football Manager 2005** (UK: November 2004) — effectively CM Season 04/05 under a new name |
| **Eidos response** | **Championship Manager 5** (2005), developed by Beautiful Game Studios — poor reception; franchise wound down |
| **Parallel CM line** | **Championship Manager 2010** (2009) — Eidos's last major CM; had its own **3D engine** separate from FM |
| **Confidence** | ✅ High (documented industry history) |

**Futtoboru note:** The split matters because **database + rules research** is SI's moat, not the match renderer. Futtoboru must own **historical data + season rules** the same way SI owns modern data.

**Legacy deep dives (Round 2):** [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM2_REFERENCE.md](CM2_REFERENCE.md) · [CM3_REFERENCE.md](CM3_REFERENCE.md)

---

## A2. Championship Manager 4 (CM4)

**→ Full feature bible:** [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)  
**→ Community / dev / support:** [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md)  
**→ Legacy series context:** [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md)  
**→ Futtoboru gap:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)

| Field | Value |
|-------|-------|
| **Ship date** | March 2003 (UK) |
| **Developer** | Sports Interactive |
| **Publisher** | Eidos |
| **Platforms** | PC, Mac (no console release) |

### World / database

| Metric | Value | Confidence |
|--------|-------|------------|
| Nations with data | **39** | ✅ MobyGames / official |
| Divisions | **88** | ✅ |
| Players + staff | **200,000+** | ✅ |
| Database sizes | Small / Medium / Large (adjustable) | ✅ |
| Researchers | Not cited in Round 1 | 🔲 |

### Match engine

| Aspect | Detail |
|--------|--------|
| **Type** | **First 2D top-down visual match engine** in CM series |
| **Prior** | Text-only commentary (CM1–CM3) |
| **Presentation** | 2D pitch with player blobs; text commentary retained |

### Simulation features (selected)

- Full management loop: tactics, transfers, training, staff
- Multiplayer (hotseat / network — platform dependent)
- Xbox/PS2 versions adapted UI for console

### Sources

- [Championship Manager 4 — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager_4)
- [MobyGames CM4](https://www.mobygames.com/game/championship-manager-4)

---

## A3. Championship Manager Season 03/04

| Field | Value |
|-------|-------|
| **Ship date** | 2003 |
| **Note** | Last CM branded release before FM2005; direct predecessor to FM2005 codebase |
| **Database** | 🔲 Round 3 — likely similar scale to CM4 |
| **Match engine** | 🔲 Text + transitional; verify vs CM4 |
| **Confidence** | ⚠️ Partial |

---

## A4. Championship Manager 5 (Eidos)

| Field | Value |
|-------|-------|
| **Ship date** | 2005 |
| **Developer** | Beautiful Game Studios |
| **Publisher** | Eidos |
| **Reception** | Widely panned; contributed to CM brand decline |
| **Database / leagues** | 🔲 Round 3 |
| **Match engine** | 🔲 Separate from SI codebase |
| **Confidence** | ⚠️ Partial (historical context only) |

---

## A5. Championship Manager 2010 (Eidos — parallel line)

| Field | Value |
|-------|-------|
| **Ship date** | September 2009 (UK) |
| **Developer** | Beautiful Game Studios |
| **Publisher** | Eidos |
| **Match engine** | **3D motion-capture** (Eidos engine, not FM's) |
| **Notable** | Dutch leagues expansion; separate product from FM 2010 |
| **Database** | 🔲 Round 3 |
| **End of line** | CM franchise effectively discontinued after poor sales |
| **Confidence** | ⚠️ Partial |

---

# Part B — Football Manager 2005–2010 (SI line, detailed)

## B1. Football Manager 2005 (FM 2005)

| Field | Value |
|-------|-------|
| **Ship date** | 5 November 2004 (UK) |
| **Developer** | Sports Interactive |
| **Publisher** | Sega (first FM under Sega) |
| **Platforms** | PC, Mac |
| **NA title** | **Worldwide Soccer Manager 2005** |
| **Season represented** | 2004–05 European season start |

### World / database ✅

| Metric | Value | Source |
|--------|-------|--------|
| Countries | **51** | GamesIndustry.biz / Wikipedia |
| First-team divisions | **158** | GamesIndustry.biz |
| League teams | **5,002** | GamesIndustry.biz |
| Players + staff | **250,000+** | Official marketing |
| Researchers | **~2,500** | GamesIndustry.biz |

### Match engine

| Aspect | Detail |
|--------|--------|
| **Type** | **2D top-down** (carried from CM 03/04 lineage) |
| **3D** | No |

### Key simulation features

- Full club management: tactics, training, transfers, staff hiring
- Board expectations, job security
- Improved media / news vs CM 03/04
- Network multiplayer (PC)
- **Same core loop FM still uses:** squad → tactics → match → table → transfers → repeat

### Licensing (selected)

- Major European leagues partially licensed; many unlicensed with real player names (pre-DFB etc. restrictions in later years)

### Sources

- [Football Manager 2005 — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager_2005)
- [GamesIndustry.biz numbers](https://www.gamesindustry.biz/numbers-up-for-football-manager-2005)

**Futtoboru benchmark:** FM2005 proves **season loop + rules + researched database** is the product — not 3D. A 12-club 1888 sim needs the **discipline** of this release, not its breadth.

---

## B2. Football Manager 2006

| Field | Value |
|-------|-------|
| **Ship date** | October 2005 (UK) |
| **Season** | 2005–06 |
| **Platforms** | PC, Mac, **Xbox 360** (first console FM) |

### World / database ⚠️

| Metric | Value | Confidence |
|--------|-------|------------|
| Countries | ~**50** (Xbox marketing: 50 league systems) | ⚠️ |
| Players | ~**250,000** | ⚠️ Console box / press |
| Leagues / teams | Similar to FM2005 + incremental | ⚠️ |

### Match engine

- **2D top-down** on PC
- Xbox 360: adapted UI; same underlying sim

### Key simulation features

- **Assistant Manager** role introduced — delegate match touchline
- **Finances** expanded (cash flow, budgets)
- **Competition rules** per nation (varies by league)
- Improved **non-league** and lower-league depth (incremental)

### Sources

- Wikipedia FM2006; Xbox 360 press materials

---

## B3. Football Manager 2007

| Field | Value |
|-------|-------|
| **Ship date** | October 2006 |
| **Match engine** | 2D |
| **Database** | 🔲 Round 3 — incremental over FM2006 |
| **Notable features** | 🔲 Round 2 — verify: scouting overhaul, feeder clubs, etc. |
| **Confidence** | 🔲 |

---

## B4. Football Manager 2008

| Field | Value |
|-------|-------|
| **Ship date** | October 2007 |
| **Platforms** | PC, Mac |

### World / database ⚠️

| Metric | Value | Confidence |
|--------|-------|------------|
| Countries | **50+** | ⚠️ Giant Bomb / press |
| Teams | **5,000+** | ⚠️ Giant Bomb wiki |
| Players | 🔲 | 🔲 |

### Match engine

- **2D top-down** (last generation before 3D default)

### Key simulation features (verify in Round 2)

- **International management** expanded
- **Transfer window** and negotiation iterations
- **Match preparation** and opposition reports (era-typical)

### Sources

- [Giant Bomb FM2008](https://giantbomb.com/wiki/Games/Football_Manager_2008)

---

## B5. Football Manager 2009 — **3D engine debut**

| Field | Value |
|-------|-------|
| **Ship date** | November 2008 (UK) |
| **Season** | 2008–09 |
| **Platforms** | PC, Mac |

### Match engine ✅

| Aspect | Detail |
|--------|--------|
| **3D** | **First FM with 3D match engine** (PC/Mac) |
| **Technology** | Motion capture; influenced by **Virtua Striker** heritage (SI/Sega) |
| **2D** | **Retained** — player could switch 2D/3D |
| **Significance** | Presentation leap; **sim logic still separate** from visuals |

### World / database

| Metric | Value | Confidence |
|--------|-------|------------|
| Countries / leagues | Similar to FM2008 + updates | ⚠️ |
| Players | 🔲 | 🔲 |

### Key simulation features

- 3D highlights and full match view
- Tactical instructions refined for 3D readability
- **FM Live** / online features era (verify Round 2)

### Sources

- [Eurogamer FM2009 preview](https://www.eurogamer.net/football-manager-2009-preview)
- Wikipedia FM2009

---

## B6. Football Manager 2010

| Field | Value |
|-------|-------|
| **Ship date** | October 2009 |
| **Match engine** | 3D + 2D |
| **Notable** | **Scottish Challenge Cup** and lower-league cup coverage expanded (community notes) |
| **Database** | 🔲 Round 2 |
| **Features** | 🔲 Press conferences iteration; verify vs FM2009 |
| **Confidence** | ⚠️ Partial |

**Note:** Do not confuse with **Championship Manager 2010** (Eidos, separate product).

---

# Part C — FM 2011–2018 (summary stubs for Round 2)

## C1. Football Manager 2011–2013

| Release | Ship | Engine | Round 2 priorities |
|---------|------|--------|-------------------|
| FM 2011 | Nov 2010 | 3D | Nations count; classic "1-1 draw bug" engine note |
| FM 2012 | Oct 2011 | 3D | **Polish league** added; Linux 🔲 |
| FM 2013 | Nov 2012 | 3D | **Football Associations** feature; NFC / Steam Workshop era |

---

## C2. Football Manager 2014

| Field | Value |
|-------|-------|
| **Ship** | Oct 2013 |
| **Engine** | **Major 3D overhaul** — AI, animations, lighting |
| **Database** | 🔲 Round 2 |
| **Features** | Redesigned UI; improved set pieces in 3D |

---

## C3. Football Manager 2015

| Field | Value |
|-------|-------|
| **Ship** | Nov 2014 |
| **Engine** | 3D |
| **Database** | **Configurable at new game** — nations/leagues/players (modern pattern begins) |
| **Features** | 🔲 Round 2 — verify Data Analysis introduction timeline |

---

## C4. Football Manager 2016 ✅ (partial)

| Field | Value |
|-------|-------|
| **Ship** | Nov 2015 |
| **Countries** | **50** (MobyGames) |
| **Players** | **~300,000** (MobyGames) |
| **Engine** | 3D; **manager touchline mo-cap** on 3D bench |
| **Features** | Improved set pieces; custom kits; **FM Touch** spin-off |

---

## C5. Football Manager 2017–2018

| Release | Engine / feature headline |
|---------|---------------------------|
| **FM 2017** | **1500+ mo-cap animations**; LED advertising boards; improved stadium rendering |
| **FM 2018** | **Dynamics** (player relationships, social groups); **3D newgen faces**; **Sports Science** medical tab; **Football Manager Mobile 2018** |

🔲 Full database counts — Round 2

---

# Part D — FM 2019–2026 (modern era)

## D1. Football Manager 2019 ✅ (partial)

| Field | Value |
|-------|-------|
| **Ship** | 2 November 2018 |
| **Countries with teams** | **52** (Wikipedia; 34 European with playable leagues) |
| **Licensed leagues** | **25** fully licensed + KNVB Team Holland |
| **Bundesliga** | **First time all 3 German divisions licensed** |
| **Germany NT** | **Not included** (licensing) |
| **Engine** | 3D + 2D |
| **Simulation** | **VAR** in competitions that use it; **training overhaul** |
| **Linux** | **No Linux build** (first FM since 2013 without Linux) |
| **Players count** | 🔲 Round 2 |

---

## D2. Football Manager 2020 ✅ (partial)

| Field | Value |
|-------|-------|
| **Ship** | November 2019 |
| **Nations (playable leagues)** | **51** |
| **Leagues** | **118** (Wikipedia) |
| **Players** | ⚠️ Community full export ~280k+; official configurable |
| **Engine** | 3D visual refresh — models, pitch, kits |
| **Features** | **Development Centre**; improved board interactions; **no Brexit** in base (later patches / FM21) |
| **Platforms** | PC, Mac, Mobile, Touch, Stadia (historical) |

---

## D3. Football Manager 2021–2023

| Release | Headlines (Round 2 expand) |
|---------|----------------------------|
| **FM 2021** | Brexit rules; **Quick Match**; compressed COVID-era schedules |
| **FM 2022** | **New animation engine**; **Data Hub**; **Transfer Deadline Day** experience |
| **FM 2023** | **FM23** — 🔲 verify: tactical presets, data improvements |

---

## D4. Football Manager 2024 ✅ (partial)

| Field | Value |
|-------|-------|
| **Ship** | 6 November 2023 |
| **Japan** | **First release in Japan**; **J1, J2, J3 fully licensed** |
| **Save carry-over** | **First cross-edition save import** (FM23 → FM24; planned ongoing) |
| **Players** | ⚠️ Full database export cited ~**475,000** players (community tools; varies by setup) |
| **Records** | Most-played FM ever (7M players in 100 days; later 19M+ cited) |
| **Engine** | 3D (pre-Unity) |
| **Platforms** | PC, Mac, consoles (Xbox, PS5), Switch, Mobile (Netflix) |

---

## D5. Football Manager 25 — **cancelled**

| Field | Value |
|-------|-------|
| **Planned** | November 2024 → delayed to March 2025 |
| **Cancelled** | February 2025 |
| **Reason** | Quality bar not met; **Unity transition** troubled |
| **Engine** | Unity (planned) |
| **Impact** | Forced **FM26** as next shipping title; 18-month gap in annual cycle |

---

## D6. Football Manager 26 ✅ (partial)

| Field | Value |
|-------|-------|
| **Ship** | 4 November 2025 (Switch: Dec 2025) |
| **Engine** | **Unity** (presentation/UI); **SI proprietary sim** for logic |
| **Women's football** | **First full integration** — **14 playable women's leagues**, **35,000+ licensed women players** |
| **Women's leagues (licensed examples)** | WSL, WSL2, NWSL, Frauen-Bundesliga, Damallsvenskan, A-League Women, UWCL |
| **Men's licensing** | **Full Premier League**; EFL extended; SPFL full license (2025) |
| **New nations (examples)** | **Egypt, Lithuania, UAE** (A Lyga, ADNOC Pro League) |
| **International mgmt** | **Not at launch** — promised post-launch (FIFA partnership; World Cup 2026 update) |
| **Reception** | Mixed reviews; UI criticism; Steam mostly negative at launch |
| **Post-launch** | International management update (May 2026 per Wikipedia) |

🔲 Full men's database counts — Round 2

---

# Part E — Cross-release comparison tables

## E1. Match engine by release

| Release | 2D | 3D | Notes |
|---------|----|----|-------|
| CM4 | ✅ | — | First visual engine |
| FM2005–2008 | ✅ | — | |
| FM2009+ | ✅ opt | ✅ | 3D default over time |
| FM2014 | ✅ | ✅ major | Overhaul |
| FM2017 | ✅ | ✅ | 1500+ anims |
| FM2022 | ✅ | ✅ | New animation engine |
| FM26 | ✅? | ✅ Unity | UI rebuilt |

## E2. Database size trend (⚠️ approximate)

| Era | Typical max players+staff |
|-----|---------------------------|
| CM4 | 200,000+ |
| FM2005 | 250,000+ |
| FM2016 | ~300,000 |
| FM2020 | ~280k–350k (config dependent) |
| FM2024 | up to ~475k (full export) |
| FM26 | +35k women (additional) |

## E3. Countries trend

| Era | Playable nations (typical) |
|-----|----------------------------|
| CM4 | 39 |
| FM2005 | 51 |
| FM2016 | 50 |
| FM2019 | 52 |
| FM2020 | 51 (118 leagues) |
| FM26 | 🔲 Round 2 full count |

---

# Part F — Simulation systems inventory (Round 4 preview)

Systems to track per release in future rounds:

| System | First appeared (approx) | Futtoboru v1 |
|--------|---------------------------|--------------|
| League fixtures + table | CM1 / FM always | ✅ Partial |
| Cup competitions | Early CM | ⚠️ Partial |
| Transfer market | Early CM | ❌ Broader |
| Contracts / wages | Early CM | ✅ Partial |
| Training | Early CM | ❌ |
| Scouting | CM3+ | ❌ |
| Youth / newgens | CM3+ | ❌ |
| Staff hiring | CM3+ | ⚠️ Jobs exist |
| Board / job security | FM2005+ polish | ⚠️ Partial |
| Press / media | FM2007+ | ❌ |
| Tactics / formations | Always | ❌ |
| 2D match | CM4 | ❌ |
| 3D match | FM2009 | ❌ |
| VAR | FM2019 | ❌ N/A 1888 |
| Women's game | FM26 | ❌ N/A v1 |
| International mgmt | CM/FM always (except FM26 launch) | ❌ |
| Multiplayer | CM4+ | ❌ |
| Editor | FM2005+ community | ❌ |
| Cross-save | FM2024+ | ❌ |

---

## Round 1 completion checklist

- [x] Split story documented
- [x] CM4 full entry
- [x] FM2005 full entry
- [x] FM2006, FM2008, FM2009 entries
- [x] FM2016, FM2019, FM2020, FM2024, FM25, FM26 partial entries
- [x] CM5, CM 03/04, CM2010 stubs
- [x] Engine evolution table
- [ ] FM2007, FM2010 full verification — Round 2/3
- [ ] FM2011–2015, FM2021–2023 full entries — Round 2
- [ ] Per-release official fact sheets — Round 2
- [ ] Simulation systems matrix — Round 4

**Next session:** Start Round 2 with FM2011 official feature list + FM2007 database snapshot.
