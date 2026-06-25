# Championship Manager 4 — Complete Feature Reference

**Document type:** Competitive deep dive (single release)  
**Parent wiki:** [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) · [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md)  
**Gap vs Futtoboru:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)  
**Date:** 2025-06-25  
**Confidence:** Mixed ✅ verified / ⚠️ community-manual / 🔲 needs manual scan

---

## 1. Release identity

| Field | Detail | Confidence |
|-------|--------|------------|
| **Full title** | Championship Manager 4 (CM4) | ✅ |
| **AKA (localised)** | *L'Entraîneur 4*, *Meistertrainer 4*, *Scudetto 4* — all “Saison 2002–2003” | ✅ MobyGames |
| **Developer** | Sports Interactive | ✅ |
| **Publisher** | Eidos Interactive (PC); Feral Interactive (Mac) | ✅ |
| **Series position** | Penultimate SI-developed CM; successor = CM Season 03/04; spiritual successor = FM 2005 | ✅ |
| **Season represented** | **2002–03** (English clubs start June/July 2002; other nations vary) | ✅ GameFAQs / IGN |
| **UK PC release** | 28 March 2003 | ✅ |
| **Mac release** | 17 May 2003 (EU) | ✅ Wikipedia |
| **Platforms** | **Windows, Mac OS X only** (no console SKU) | ✅ Wikipedia / MobyGames |
| **Engine rewrite** | **Full rewrite from scratch** — not a cosmetic annual update | ✅ MobyGames reviews |

### Commercial & awards

- Fastest-selling PC game in UK at launch (outsold nearest title ~2:1 vs *C&C Generals*) — ✅ Eurogamer / Wikipedia  
- ELSPA **Platinum** (300,000+ UK copies) — ✅  
- Golden Joystick 2003: **Best of British** — ✅ MobyGames  
- Nominated: CGW 2003 Sports GOTY; AIAS Computer Sports Game of the Year — ✅ Wikipedia  

---

## 2. What CM4 changed vs CM 02/03

CM4 was the first **major architectural refresh** since CM3, not a database patch.

### Headline new systems (SI official list — GameFAQs ig1234)

| Feature | Description |
|---------|-------------|
| **2D match engine** | First CM with real-time **top-down pitch view**; tactical feedback visually (similar in intent to real “Prozone”-style analysis) |
| **New tactic system** | Drag-and-drop pitch; team instructions; per-player match instructions; many preset formations |
| **New training system** | Weekly schedules; position-specific drills; GK/outfield split; training star feedback |
| **39 nations / 88 divisions** | Largest playable geography in series to date |
| **200,000+ players & staff** | Full researched database |
| **Reserves + U19 leagues** | Both squads **controllable** and competing in **their own leagues** |
| **Skinnable UI** | First CM with custom interface skins |
| **Praising & shaming players** | Media stories affecting morale (Enhancement Pack 1+) |
| **Player personalities** | Visible summaries + hidden mental traits driving behaviour |
| **Enhanced media interaction** | Deeper press/management dialogue than prior CM |
| **Wage + transfer budgets** | Separate board-controlled budgets |
| **Fan feedback** | Supporter expectations distinct from board |
| **News ticker** | Rolling news feed |
| **Improved transfers** | Enquiries, first options, instalments, sell-on, appearance fees |
| **Latest transfer rules** | Window rules updated for 02/03 era |
| **Enhanced player notes** | Manager note-taking on players |
| **More backroom feedback** | Staff reports to manager |
| **Right-click + keyboard shortcuts** | Full mouse/keyboard UX |
| **Enhanced multiplayer** | Network play improvements |
| **Windows XP visual refresh** | UI aligned with XP era |

### Carried forward from CM3 / 02/03 (core loop unchanged)

- Text/menu-driven management UI outside matches  
- Board expectations, sacking, job offers  
- Scouting network, shortlists, player search filters  
- International management & world qualifiers  
- European + domestic cups  
- Staff hiring (coaches, scouts, physios, assistant managers)  
- Player happiness, concerns, promises, squad status  
- Compare players, hall of fame, go on holiday  

---

## 3. World database

### Scale

| Metric | Value |
|--------|-------|
| Nations with playable leagues | **39** |
| Divisions (total) | **88** |
| Players + staff | **200,000+** |
| Researchers | Large volunteer network (exact count not cited in Round 1 sources) 🔲 |

### Database size (new game)

Three global settings — affects **player count in save**, not just league choice:

| Setting | Effect |
|---------|--------|
| **Small** | Minimum players; recommended for 1 league / low-spec PC |
| **Medium** | Middle ground |
| **Large** | Maximum database; slow on minimum spec but supported |

- Minimum spec officially tested with **1 league + Small database** (P3 600 MHz, 64 MB RAM).  
- Expanding database size **only applies to new games** (IGN FAQ).  
- Quick Search scans all loaded database players; fewer loaded = fewer findable players.

### League selection

At new game:

1. **Quick Start** — one league, minimum database, immediate play  
2. **Start New Game** — pick nations/leagues/divisions individually  
3. **Select Recommended** — hardware benchmark suggests league count  
4. **Per-nation division toggles** — e.g. England with/without Conference; top division only  
5. **Multi-league start date** — when multiple nations selected, choose harmonised start date  

**First-time nations in CM4** (vs prior CM): Austria, Croatia, Czech Republic, Denmark, Finland, Hong Kong, India, Israel, Singapore, South Africa, South Korea, Switzerland, USA — ⚠️ en-academic.com / community lists; verify against manual for “first playable” vs “first full depth”.

### All 39 nations — divisions per country

| Nation | Divisions | Nation | Divisions |
|--------|-----------|--------|-----------|
| Argentina | 2 | Mexico | 2 |
| Australia | 1 | Northern Ireland | 2 |
| Austria | 2 | Norway | 3 |
| Belgium | 3 | Poland | 2 |
| Brazil | 3 | Portugal | 3 |
| China | 2 | Russia | 2 |
| Colombia | 2 | Scotland | 4 |
| Croatia | 2 | Singapore | 1 |
| Czech Republic | 2 | South Africa | 2 |
| Denmark | 2 | South Korea | 1 |
| England | 5 | Spain | 3 |
| Finland | 2 | Sweden | 3 |
| France | 3 | Switzerland | 2 |
| Germany | 3 | Turkey | 3 |
| Greece | 2 | USA | 3 |
| Holland | 2 | Wales | 1 |
| Hong Kong | 1 | Yugoslavia (Serbia & Montenegro) | 2 |
| India | 1 | | |
| Ireland | 2 | | |
| Israel | 1 | | |
| Italy | 4 | | |
| Japan | 2 | | |

**England (5):** Premier League, Division 1, Division 2, Division 3, Conference (+ extensive cup set).  
**Scotland (4):** SPL + lower divisions + cups.  
**Italy (4):** Serie A through lower tiers.

---

## 4. Match engine & match day

### Engine type

| Mode | Detail |
|------|--------|
| **2D top-down (default innovation)** | Real-time dots/sprites on pitch; see runs, gaps, defensive shape |
| **Text / classic flow** | Prior-style commentary-led experience still supported — purists could avoid 2D view ⚠️ Retro Replay |
| **Not 3D** | No mo-cap 3D until FM2009 / rival CM2010 |

### During a match

- Live 2D view with tactical changes taking effect in real time  
- Half-time and full-time scores  
- **Match ratings** per player (1–10 scale; sub decisions driven by <6 ratings)  
- **Possession** statistic  
- **Action zones** — heat/zone analysis post-match  
- Weather, referee, venue on fixture screen  
- Betting odds on upcoming fixtures  
- Set-piece takers identifiable via 2D + `Set Pieces` attribute  
- Substitutions: up to **5 named, 3 used** (example: English Conference rules in IGN)  
- Saves **`.pkm` replay files** for watched matches (deletable for performance)  

### After a match — Last Match screen

- Full match report  
- Player ratings  
- Action zones  
- Injuries sustained  
- Tactics used  
- Possession breakdown  

### Tactical impact (design intent)

PC GamesN: CM4 2D enabled **chess-like** human multiplayer — visual proof of full-back being beaten, prompting live formation/instruction changes rather than guessing from numeric ratings alone.

---

## 5. Tactics system (full)

### Formation construction

- **Pitch editor:** drag players with **left mouse** (position); **right mouse** (forward/back/diagonal runs)  
- **Captain** selection on tactics screen  
- **Per-player “i” button:** individual **match instructions** (manual pp. 55–61)  
- **Quick Load presets:** large library of named setups  
- **Compare Players:** up to 10 players side-by-side with colour-coded stat deltas + positional suitability  

### Preset formations (partial list — IGN FAQ)

Includes historical shapes: **2-3-5, 2-4-4, 2-5-3, 3-2-5, 3-3-4, 3-4-1-2, 3-4-2-1, 3-4-3, 3-5-2**, plus modern **4-1-2-1-2, 4-2-4, 4-3-2-1, 4-3-3, 4-4-2, 4-4-2-A, 4-4-2-VA, 4-5-1, 5-2-3, 5-3-2, 5-3-2-A, 5-3-2-D, 5-3-2-SW (sweeper), 5-4-1**.

### Team instructions

| Category | Options |
|----------|---------|
| **Passing style** | Short · Direct · Long · Mixed |
| **Focus passing** | Down both flanks · Down left · Down right · Through middle · Mixed |
| **Tackling** | Easy · Normal · Hard |
| **Closing down** | Own half · Always · Stand off |
| **Mentality** | Ultra defensive · Defensive · Normal · Attacking · Gung ho |
| **Offside trap** | Yes / No |
| **Zonal marking** | Yes / No |
| **Counter attack** | Yes / No |
| **Men behind ball** | Yes / No |

### In-match management (community meta — GameFAQs ig1234)

Documented situational playbook:

- **Winning:** tighten (extra defender), hold shape, or risk attack if lead is safe  
- **Losing:** extra striker, all-out attack, midfield push  
- **Drawing:** play for win vs protect draw  
- **Late game:** SI designer Mark Vaughan cited 3-up + 2 wingers inward when losing (~20 min left); 5-3-2 when protecting lead (~10 min left) — IGN walkthrough  

---

## 6. Training system (full)

### Structure

- **Weekly schedule:** assign session per day slot (multiple groups possible)  
- **Position schedules:** separate templates for GK, defenders, midfielders, forwards  
- **Delegation:** assistant manager can run entire training program  
- **Training report:** star rating feedback per player (buggy in early 4.0.0; fixed in patches)  
- **Pre-season:** cross-country, friendlies, overload exercises for fitness  
- **Tactical training:** includes studying opposition video, danger men, prior matches  

### All training activities

**Shared (outfield + GK where noted):**

| Activity | Primary effect |
|----------|----------------|
| 5-a-side (large pitch) | Stamina, work rate, passing, movement |
| 5-a-side (small pitch) | Marking, quick thinking, shooting; GK reflexes |
| Overloading (defensive) | Defensive line, marking, positioning |
| Overloading (attacking) | Width, crosses, striker movement/finishing |
| Sprints | Acceleration, pace, balance |
| Penalties | Composure; pre-shootout prep |
| **Tactical training** | Decision-making, team cohesion, opposition prep |
| Cross country | Stamina, weight loss; pre-season fitness multiplier |
| Pig in the middle | Anticipation, first touch |
| Technique | Ball control, passing, dribbling |
| Crosses | Crossing + timed runs |
| Set pieces (attacking) | Free-kicks, corners, penalties, long throws |
| Set pieces (defensive) | GK + outfield defending dead balls |
| Training match | Full-intensity internal friendly; high fatigue |
| Weight training | Strength; injury reduction at low intensity; pace loss if overdone |
| Rest | Recovery |

**Goalkeeper-only:**

| Activity | Effect |
|----------|--------|
| Agility (GK) | Reflexes, agility |
| Distribution | Recycling possession, goal kicks |
| Handling | Positioning, catching |
| Shot stopping | Body behind ball, powerful shots |

**Outfield-only:**

| Activity | Effect |
|----------|--------|
| Closing down session | Stamina, vision, tackling, teamwork |
| Heading | Neck strength, clearances, flick-ons, headed goals |
| Shadow play | Low-intensity pre-match walkthrough; movement, decisions |
| Shooting | Finishing, composure, weak foot, long shots |
| Agility | Acceleration, balance, coordination |

---

## 7. Squad structure

### First team

- Full squad screen with **Player Profile** + **Player Information** sub-screens  
- **Fitness states:** Superb condition → Fully fit → Match fit → Lacking match fitness → Very unfit → Very tired  
- **International instructions** when called up: play half, don’t play, etc.  
- **Discipline tracking:** yellow accumulation, red bans (league / cup / international / European separate)  
- **Statistics rows:** Non-competitive, League, Cup, Continental, International, Overall  
- **Stat columns:** Apps, Goals, Assists, MoM, Yellow, Red, Tackles/90, Pass %, Shots on target %, Fouls, Fouls against, Average rating  

### Reserves

- Separate **reserve squad** screen  
- **Reserve league** with table + fixtures  
- Own **reserve tactics**  
- Last reserve match viewable  
- **Grey players:** AI filler when reserve pool too small (not signable)  

### Under-19s

- **U19 squad** + **U19 league** + tactics + last match  
- Primary youth pipeline for wonderkids  
- Assistant can manage entirely  
- Friendly integration for testing youngsters without hurting board confidence  

---

## 8. Transfers & contracts

### Transfer market tools

| Tool | Function |
|------|----------|
| **Player search** | Filter by position, nationality, stats, age, value, etc. |
| **Scout assignments** | Scout team, country, region, competition, or **next opponent** |
| **Shortlist** | Track targets |
| **Quick search** | Name/club/nation text lookup across loaded DB |
| **All transfers screen** | Global feed filterable by nation/month/staff/players |
| **Assistant filter** | Hide unrealistic targets (e.g. Raúl to Conference club) |

### Transfer mechanics

- **Transfer list / offer to clubs / assistant recommended clubs**  
- **Bid composition:** fee, instalments, sell-on %, after-X-appearance payments  
- **Enquiry** — ask selling club’s price before bidding  
- **First option** — right of first refusal if club sells  
- **Loans:** up to 12 months or end of season; **season cap** on total loans + simultaneous loans (exact numbers in manual) 🔲  
- **Trialists** — referenced in GameFAQs TOC 🔲  
- **Wage budget + transfer budget** — board-imposed; wage structure limits offers  
- **Squad status on contract:** Indispensable, etc. — affects happiness if misassigned  
- **Contract parts:** wages, signing-on fee, length, bonuses, clauses  
- **Languages:** players without local language fluency suffer morale penalty  
- **Part-time vs full-time contracts**  

### Selling

- Set transfer status + asking price + “not needed by club” flag  
- Offer to interested clubs  
- Relegate to reserves to force move  

---

## 9. Scouting & staff

### Scout tasks

- Region / nation / competition / single team / **next opposition**  
- Youth-focused assignments (multiple scouts on same division for youth)  
- Scout report on player profile with dated notes  

### Staff roles & attributes

**Board:** Chairman, Director of Football, Managing Director, General Manager.

**Coaching staff abbreviated screen (example):** CGK · Coa · Phy · Tct · Yng · Man · Ada · Det · Jud · Jpt · Dis · Mot

| Attribute | Meaning |
|-----------|---------|
| Coaching GKs / Outfield | Training quality by role |
| Physiotherapy | Injury recovery |
| Tactical knowledge | Tactical training quality |
| Man management | Dressing room psychology |
| Motivating | Morale lifting |
| Level of discipline | Severity of punishment |
| Judging player ability / potential | Scout-like evaluation |
| Working with youngsters | Youth development |
| Adaptability | Foreign assignment suitability |
| Determination | Staff work ethic |

### Delegation (Manager Options)

Assistant can be assigned:

- Training schedules  
- Friendly arrangement  
- Reserve team management  
- U19 management  
- **Contract renewals**  

Recommended community pattern: manager runs first team; assistant runs reserves/youth.

---

## 10. Finances

### Finance screen

- Season **transfer budget** + **remaining**  
- **Transfer revenue %** returned to budget  
- **Sponsors**  
- **Outstanding loans** (club debt)  
- Drill-down: income, expenditure, salary breakdown, club overview  

### Economic gameplay

- Board may restrict wages if bill too high  
- Transfer profit feeds back per board policy  
- Prize money from cups/leagues  
- Loan repayments tracked  

---

## 11. Board, fans & job security

### Dual expectation system (new emphasis)

| Stakeholder | Behaviour |
|-------------|-----------|
| **Board** | Season target (finish position, cup run); monthly assessment; sack if failed badly |
| **Fans** | Often **higher** expectation than board; protests influence board; less loyal |
| **Odds** | Pre-season title/promotion odds update after over/under-performance |

### Club confidence screen

- Board current opinion + seasonal expectation  
- Supporters current opinion + seasonal expectation  
- Wage bill warnings  

### Manager career

- **Job information** table — all managers’ security (Safe / Secure / …) filterable worldwide  
- **Resign**, **retire**, **go on holiday** (AI runs club to return date)  
- **Job offers** when unemployed or poached  
- **Hall of Fame** — score, nation, club; “addictiveness rating”  
- Manager **nationality + favourite club** affect job offers and player affinity  

---

## 12. Morale, personalities & media (deep)

### Player personality system

- **Personality summary** on Information screen (derived from hidden mentals)  
- **Description tags:** e.g. “Wonderkid”, “Flamboyant winger”  
- Hundreds of mood phrases map to trait combos (professional, loyal, ambitious, temperamental, lazy, etc.)  
- **Relationships** between players — dislikes affect dressing room; young players idolise seniors  

### Happiness & concerns

| Element | Detail |
|---------|--------|
| General happiness | Overall club satisfaction |
| Slight / major concerns | Specific grievances |
| Manager promises | Tracked; breaking them worsens mood |
| Squad status | Must match ability/expectation |
| Talk to player | Multiple dialogue options per issue |
| Protect from media | Failing to shield players hurts trust |
| Discipline / fines | Context-sensitive; squad may side with or against you |
| Winning streak | Raises morale; losing runs hit performance (IGN: “concede sloppy goals”) |

### Media — Release Media Story (Enhancement Pack 1+)

- **Praise** or **criticise** recent form via player Action menu  
- Restrictions: player needs **4+ games**; **one story per week**  
- High **professionalism** players may respond well to fair criticism  
- **Temperamental / lazy** players resent criticism  
- Unfair criticism almost always backfires  
- Praise can boost player, squad, or fan morale — but may cause complacency  
- Congratulating captain, top scorer, or fan favourite has wider morale effects  

### Backroom feedback

- Assistant advises on discipline, transfer targets, contract renewals  
- Enhanced **player notes** for manager annotations  
- **News ticker** + full **News** hub (contracts, injuries, sackings, transfers)  

---

## 13. Competitions

### International (simulated worldwide)

- FIFA World Cup + qualifying (Africa, Asia, CONCACAF, Europe, Oceania, South America)  
- FIFA World Rankings  
- African Nations Cup (+ qual), Asian Cup, European Championship (+ qual)  
- CONCACAF Gold Cup, OFC Nations Cup, Copa América (+ guest nation)  
- Confederations Cup, Olympics, Intercontinental Cup  

### European clubs

- **Champions Cup** (Champions League)  
- **UEFA Cup**  
- **Intertoto Cup** (UEFA Cup entry route)  
- **Super Cup**  

### England (example depth)

- Premier League → Conference pyramid (promotion/relegation)  
- **FA Cup**, **League Cup**, **Community Shield**  
- **LDV Vans Trophy** (Div 2/3/Conference)  
- **FA Trophy** (non-league)  

Each nation in database carries domestic cups, awards (Player/Manager of month/year, Team of the week), and continental qualification slots — see in-game **League Guide** per country (Argentina dual-stage league, MLS, etc.).

### Awards system

Weekly / monthly / yearly honours per league; recorded in division history.

---

## 14. User interface & quality of life

### Navigation

- Left **toolbar:** Continue, manager menu, competitions, clubs/nations, recent items, game options  
- **F1–F12 shortcuts** (squad, staff, finances, fixtures, transfers, news, search, training, etc.)  
- **Alt+←/→**, PgUp/PgDn screen history  
- **Space** — continue / mark news read  
- **Right-click** context menus (new to CM4)  

### Skins

Shipped: **Original**, **Retro Blue**, **Ter** (by Craig Hunter); **2+ download skins day one** — GameSpot  
Custom skins: unpack to `CM4/Data/Skins`  

### Game preferences

| Setting | Purpose |
|---------|---------|
| Language / currency / wage period / temperature | Localisation |
| Database size | Small / Medium / Large |
| Autosave | Frequency (monthly recommended due to launch crashes) |
| Continue timeout | Auto-advance if AFK |
| Use real players | vs fake names |
| Play sounds | Matchday ambient |
| Full screen / windowed | Performance (fullscreen faster) |
| Background images | Context art per screen |
| Server mode | Required for network multiplayer |
| Enable background changes | Minor perf cost |

### Other meta features

- **Recent items** — screen history  
- **All notes** — manager notebook  
- **Manager chat** — in-game chat between human managers  
- **Community information** — links to CM fan sites  
- **Game status** — save metadata, leagues loaded, time played  
- **Multi-manager** — hotseat-style add manager in same world  

---

## 15. Multiplayer

| Feature | Detail |
|---------|--------|
| **Network multiplayer** | LAN + Internet — ✅ MobyGames |
| **Enhanced vs prior CM** | ✅ SI marketing |
| **Server flag** | Must enable in preferences when hosting |
| **Manager chat (F9)** | Cross-manager messaging |
| **2D engine value** | Human vs human tactical battles — PC GamesN |

Max player count 🔲 (community cites 8; not verified in primary SI doc).

---

## 16. Player attribute model (complete lists)

### Outfield — Technical

Crossing · Dribbling · Finishing · Heading · Long shots · Long throws · Marking · Passing · Penalty taking · Set pieces · Tackling · Technique

### Outfield — Mental

Aggression · Anticipation · Bravery · Creativity · Decisions · Determination · Flair · Influence · Off the ball · Positioning · Teamwork · Work rate

### Outfield — Physical

Acceleration · Agility · Balance · Jumping · Pace · Stamina · Strength

### Goalkeeper

Aerial ability · Command of area · Communication · Eccentricity · Handling · Kicking · One on ones · Reflexes · Rushing out · Tendency to punch · Throwing

### Hidden / personality-linked (CM4 era — FOFC / wiki)

Professionalism · Temperament · Loyalty · Ambition · Pressure · Consistency · Important matches · Injury proneness · Versatility · Adaptability — ⚠️ standard CM3/4 mental set; exact visible vs hidden split per manual 🔲

---

## 17. Patches & launch quality

CM4 shipped with **significant bugs** (save corruption, training stars broken, wage inflation, crashes). SI released **Enhancement Packs** (not mere bugfixes branding):

| Version | Date (MobyGames) | Notable fixes/features |
|---------|------------------|------------------------|
| **4.0.3** Enhancement Pack 1 | 29 Mar 2003 | Wage bug, rehab timeout, Swedish derby stadia, Euro symbol in Ter skin; **praise/criticise media** |
| **4.0.4** Enhancement Pack 2 | 11 Apr 2003 | 🔲 |
| **4.0.8** Enhancement Pack 5 | 1 May 2003 | 🔲 |
| **4.0.5 – 4.0.7** | subsequent | League rollover (Argentina, Denmark), cup fixes, Mac/PC match sync, loan youth tweak, morale fixes |

ActiveGaming review: **“Get Enhancement Pack or punch desk.”** Many fans migrated to **CM 03/04** (iterative fix on same codebase) which added **match replay** — feature CM4 lacked until successor.

---

## 18. System requirements

| Tier | Spec |
|------|------|
| **Minimum** (1 league, small DB) | P3 600 MHz; Win 98/XP; 64 MB RAM (128 MB XP); 1024×768 32-bit colour; DirectX sound |
| **Recommended** | P4 1 GHz; 256 MB RAM; “optimised for P4” |
| **Mac** | Separate Feral port — May 2003 |

---

## 19. Futtoboru relevance (1888 benchmark)

CM4 is the **first “modern” management sim layer** in the SI lineage. For Futtoboru v1, CM4 defines patterns worth understanding — not copying at FM scale:

| CM4 capability | Futtoboru v1 lesson |
|----------------|---------------------|
| Season loop + board expectation | ✅ Need season end + rules + rollover |
| League + optional cups + table | ✅ Core |
| 2D match presentation | ❌ Not v1 |
| Full training/schedules | ❌ Broader |
| Transfers/scouting | ❌ Broader |
| Personalities/morale/media | ❌ Broader |
| Reserves/youth leagues | ❌ Broader |
| 39 nations / 200k players | ❌ — scope is 12 clubs, ~500 humans |
| Configurable database size | ⚠️ Analog: load only season 18/19 data |
| Dual board + fan pressure | ⚠️ Optional later |
| Researcher network | ✅ Analog: sourced 1888 roster research |

**Minimum bar:** CM4 proves the product is **rules + data + season loop**; the 2D engine was marketing breakthrough but not the economic moat.

---

## 20. Sources

- [Championship Manager 4 — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager_4)
- [MobyGames — CM4](https://www.mobygames.com/game/8910/championship-manager-4/) + [patches](https://www.mobygames.com/game/8910/championship-manager-4/patches/)
- [GameFAQs — ig1234 CM4 FAQ](https://gamefaqs.gamespot.com/pc/562286-championship-manager-4/faqs/23154) (SI official feature quote)
- [GameFAQs — shy CM4 guide](https://gamefaqs.gamespot.com/pc/562286-championship-manager-4/faqs/23115) (attributes, screens)
- [IGN — CM4 walkthrough/FAQ](https://www.ign.com/articles/2003/06/05/championship-manager-4-walkthroughfaq-422670) (tactics, training, transfers, prefs)
- [IGN — CM4 walkthrough (toolbar)](https://www.ign.com/articles/2004/03/11/championship-manager-4-walkthrough-497914)
- [GameSpot — CM4 update (skins, 39 leagues)](https://www.gamespot.com/articles/championship-manager-4-update/1100-2910603/)
- [PC GamesN — CM history / CM4 2D](https://www.pcgamesn.com/championship-manager-series/championship-manager-football-manager-history-making-of)
- [ActiveGaming — CM4 review](http://www.activewin.com/reviews/software/games/c/champ_man4/index.shtml)
- [FOFC — CM03/04 hints (CM4 morale/media)](https://forums.operationsports.com/fofc/showthread.php?t=24495)
- [Retro Replay — CM4 feature summary](https://retro-replay.com/db/macintosh/championship-manager-4/)

---

## 21. Document changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Initial deep reference created from online/manual/community sources |

**Gaps for Round 2:** scan original PDF manual for exact loan caps, trialist rules, full player instruction enum, researcher count, complete Enhancement Pack changelogs.
