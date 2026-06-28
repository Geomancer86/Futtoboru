# Football Manager Competitive Bible

**Document type:** Internal reference / competitive analysis wiki  
**Project:** Futtoboru  
**Started:** 2025-06-25  
**Maintained by:** Product / design (assign owner: _________________)

---

## Purpose

This wiki documents **Football Manager (FM)** and the **Championship Manager (CM)** lineage from the **2003 SI/Eidos split** through **today**, release by release. It is the benchmark for Futtoboru: *if the original games had a feature at a given era, that is the minimum bar we should understand before claiming parity.*

This is **not** a feature checklist to clone FM wholesale — Futtoboru targets **historical simulation from 1888** — but it defines the **industry standard** for management sim depth, data scope, and simulation layers.

---

## How to read this wiki

| Symbol | Meaning |
|--------|---------|
| ✅ | Verified from primary/secondary sources (see Sources) |
| ⚠️ | Approximate, community-reported, or varies by save settings |
| 🔲 | Not yet researched — see Round plan |
| **P** | Playable league(s) in vanilla game |
| **V** | View-only / partial data |

**Important:** From FM2015 onward, **database size is player-configurable** at new game (nations/leagues/players loaded). Official marketing numbers describe the **maximum shipped database**, not what every save uses.

---

## Series timeline (high level)

```text
1992–2003  Championship Manager (SI + Eidos) — text engine → 2D in CM4
2003       Split: Eidos keeps "Championship Manager" name; SI keeps code + database
2004       CM Season 03/04 (last SI CM) │ FM 2005 (first SI FM) │ CM5 (Eidos, failed)
2004–2008  FM annual releases; NA name "Worldwide Soccer Manager"
2009         FM 2009: first 3D match engine (PC/Mac)
2009         CM 2010 (Eidos): separate 3D engine — CM line ends ~2011
2010–2023  FM annual; Linux from 2014; data hub, dynamics, etc.
2023         FM24: record players; Japan J-League licensed; save carry-over
2024–2025  FM25 cancelled (Unity transition failed)
2025         FM26: Unity engine; women's football; Premier League license
```

---

## Match engine evolution (cross-release)

| Era | Engine | Notes |
|-----|--------|-------|
| CM1–CM3 (~1992–1999) | **Text commentary** | No visual pitch; imagination-driven |
| CM4 (2003) | **2D top-down** (first visual) | Blobs/sprites; highlights; still had text mode |
| CM5 / CM2010 (Eidos) | Separate codebase | CM2010 had **3D mo-cap** (Eidos only) |
| FM 2005–2008 | **2D top-down** | Inherited from CM 03/04 lineage |
| FM 2009+ | **3D match engine** + optional 2D | Virtua Striker mo-cap base; iteratively improved |
| FM 2014 | 3D **overhaul** | AI, lighting, animations |
| FM 2017 | 3D **1500+ mo-cap animations** | LED boards, stadium work |
| FM 2018 | Newgen **3D faces** (replacing 2D portraits) |
| FM 2020 | Redesigned human models, pitch, kits |
| FM 2022 | **New animation engine** for match sim |
| FM 25 (cancelled) | Planned **Unity** match presentation |
| FM 26 (2025) | **Unity** rendering; SI sim logic still proprietary |

**Simulation vs presentation:** FM separates **match engine logic** (tactics, decisions, outcomes) from **visual layer** (2D/3D). Futtoboru currently has **minimal logic + no presentation layer**.

---

## Database methodology (all modern FM)

At new game, player chooses:

- **Nations/leagues** to load (playable vs view-only)
- **Database size** (Small / Medium / Large / Huge — exact labels vary by version)
- **Players included** (e.g. top leagues only vs all staff)

Therefore **player counts are ranges**, not fixed integers. Official snapshots (e.g. FM 2005) describe the **full shipped dataset**.

---

## Master release table (Round 1 status)

| Release | Ship date (UK/EU typical) | Publisher / Dev | Countries (P) ⚠️ | Leagues (P) ⚠️ | Players/staff ⚠️ | Match engine | Round |
|---------|---------------------------|-----------------|------------------|------------------|------------------|--------------|-------|
| **CM Season 03/04** | 2003 | Eidos / SI | 🔲 | 🔲 | 🔲 | Text + ? | 1 partial |
| **Championship Manager 4** | Mar 2003 | Eidos / SI | **39** | **88 div** | **200k+** | 2D (first CM) | **1 ✅** |
| **Championship Manager 5** | 2005 | Eidos / BGS | 🔲 | 🔲 | 🔲 | 🔲 | 1 partial |
| **Football Manager 2005** | Nov 2004 | Sega / SI | **51** | **158** (1st team) | **250k+** | 2D | **1 ✅** |
| **FM 2006** | Oct 2005 | Sega / SI | ~50 | ~same+ | ~250k | 2D | **1 ✅** |
| **FM 2007** | Oct 2006 | Sega / SI | 🔲 | 🔲 | 🔲 | 2D | 1 partial |
| **FM 2008** | Oct 2007 | Sega / SI | **50+** | **5000+ teams** | 🔲 | 2D | **1 ✅** |
| **FM 2009** | Nov 2008 | Sega / SI | 🔲 | 🔲 | 🔲 | **3D** (+2D) | **1 ✅** |
| **FM 2010** | Oct 2009 | Sega / SI | 🔲 | +Challenge Cup etc. | 🔲 | 3D + 2D | 1 partial |
| **FM 2011** | Nov 2010 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D | 2 |
| **FM 2012** | Oct 2011 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D | 2 |
| **FM 2013** | Nov 2012 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D | 2 |
| **FM 2014** | Oct 2013 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D overhauled | 2 |
| **FM 2015** | Nov 2014 | Sega / SI | 🔲 | 🔲 | Configurable | 3D | 2 |
| **FM 2016** | Nov 2015 | Sega / SI | **50** | 🔲 | **300k** (MobyGames) | 3D + mo-cap mgr | **1 ✅** |
| **FM 2017** | Nov 2016 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D major | 2 |
| **FM 2018** | Nov 2017 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D + Dynamics | 2 |
| **FM 2019** | Nov 2018 | Sega / SI | **52** (53 teams) | **25 licensed** | 🔲 | 3D; VAR | **1 ✅** |
| **FM 2020** | Nov 2019 | Sega / SI | **51** | **118** | ~280k+ community | 3D | **1 ✅** |
| **FM 2021** | Nov 2020 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D | 2 |
| **FM 2022** | Nov 2021 | Sega / SI | **17** (quick cite) | 🔲 | 🔲 | New anim engine | 2 |
| **FM 2023** | Nov 2022 | Sega / SI | 🔲 | 🔲 | 🔲 | 3D | 2 |
| **FM 2024** | Nov 2023 | Sega / SI | 🔲 | +J1/J2/J3 | **~475k** export ⚠️ | 3D | **1 ✅** |
| **FM 25** | *Cancelled* | Sega / SI | — | — | — | Unity (planned) | **1 ✅** |
| **FM 26** | Nov 2025 | Sega / SI | 🔲 | **+women 14 leagues** | **35k+ women** | Unity | **1 ✅** |
| **CM 2010** | Sep 2009 | Eidos / BGS | 🔲 | +NL leagues | 🔲 | **3D** (CM line) | 1 partial |

---

## Research rounds (plan)

| Round | Scope | Status |
|-------|-------|--------|
| **Round 1** | Split story; CM4; FM2005–2010; FM2016/2019/2020/2024/25/26 highlights; engine timeline | ✅ This commit |
| **Round 2** | FM2011–2015, FM2021–2023 full entries; official fact sheets | 🔲 Planned |
| **Round 3** | FM2007, FM2008 database snapshots; CM5, CM 03/04, CM2010 | 🔲 Planned |
| **Round 4** | Simulation systems matrix (training, scouting, press, finances) per era | 🔲 Planned |
| **Round 5** | Mobile/Touch/Live spin-offs; editor ecosystem | 🔲 Planned |
| **Round 6** | Futtoboru gap matrix vs FM1888-equivalent scope | 🔲 Planned |

**Detailed Round 1 entries:** [FM_COMPETITIVE_BIBLE_ROUND1.md](FM_COMPETITIVE_BIBLE_ROUND1.md)

---

## Futtoboru implications (1888 scope)

FM at any era exceeds Futtoboru's current scope. For **historical v1**, compare against:

| FM capability (any modern year) | Futtoboru v1 need? |
|--------------------------------|-------------------|
| 50+ nations, 100+ leagues | **No** — 1–2 seasons, 12 clubs |
| 250k+ players | **No** — ~300–500 researched players |
| 3D match engine | **No** — abstract sim acceptable |
| Configurable competition rules per season | **Yes** |
| Season completes and rolls over | **Yes** |
| League table, fixtures, cup structure | **Yes** (league min; cup optional) |
| Finances, contracts, wages | **Yes** (1888-scale) |
| Scouting, training, press, VAR, etc. | **Broader** — post-v1 |

**Minimum bar:** FM 2005-level **season loop + rules + database discipline** at **1888 data scale**, not FM 2024 breadth.

---

## Sources (Round 1)

- [Football Manager — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager)
- [Championship Manager — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager)
- [Championship Manager 4 — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager_4)
- [Football Manager 2005 — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager_2005)
- [GamesIndustry.biz FM2005 statistics snapshot](https://www.gamesindustry.biz/numbers-up-for-football-manager-2005)
- [MobyGames FM2005 / FM2016 / CM4](https://www.mobygames.com/)
- [Giant Bomb FM2008 league list](https://giantbomb.com/wiki/Games/Football_Manager_2008)
- [Eurogamer FM2009 3D preview](https://www.eurogamer.net/football-manager-2009-preview)
- [Football Manager 2020 — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager_2020)
- [Football Manager 2024 — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager_2024)
- [Football Manager 26 — Wikipedia](https://en.wikipedia.org/wiki/Football_Manager_26)
- [Sports Interactive FM2022](https://www.sports-interactive.com/games/football-manager-2022)
- PCGamesN / Eurogamer history articles (CM split)

---

## Change log

| Date | Change |
|------|--------|
| 2025-06-25 | Round 1: master index, engine timeline, master table, prelude entries |
