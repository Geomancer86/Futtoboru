# Data review 1888-89: research findings (WORKSTATION, 2026-11-27)

Hand-written companion to the generated `REVIEW_1888-89.md`, which `tools/data/convert_legacy.py` rewrites on
every run. Section names follow that file. Apply these to `gamedata/` after P2-4 slice 2, then tick the matching
items in the generated review.

Web research against Wikipedia season/club/player articles and the sources they cite. Not applied to the data yet:
the JSON is still generated from `mods/*.txt` until P2-4 slice 2 lands, then these go into `gamedata/` and the
items above get ticked. "Not found" means no online source was found, not that the person is fake; several clubs
need offline books or newspapers (listed at the end). Many 11v11 / englishfootballleaguetables pages refused
automated access, so no full 1888-89 line-ups for the minor clubs were read.

Short sources: **FAC** = https://en.wikipedia.org/wiki/1888%E2%80%9389_FA_Cup ·
**FACQ** = https://en.wikipedia.org/wiki/1888%E2%80%9389_FA_Cup_qualifying_rounds

### FA Cup entrants (resolves the data-vs-code item)
- The data file's 32 clubs **are exactly the 1888-89 First Round Proper** (FAC). The old code was wrong:
  Bolton lost 0-4 to Linfield Athletic in the 3rd qualifying round, Everton withdrew v Ulster in the 1st
  qualifying round, Stoke lost 1-2 to Warwick County in the 1st qualifying round (FACQ). Keep the data list.
- Small Heath came through qualifying and lost 2-3 to WBA in R1; Walsall Town Swifts (exempt) beat Sheffield
  Heeley 5-1, then lost 1-6 at Wolves; Heeley came through four qualifying rounds (FAC, FACQ).
- **Wrong club:** `witton-albion` played no FA Cup until 1907-08. The "Witton" that lost 2-3 to Aston Villa on
  2 Feb 1889 is **Witton F.C. of Blackburn** (1874-1892): https://en.wikipedia.org/wiki/Witton_F.C. → new id
  `witton` (Blackburn) with an alias from `witton-albion`; its whole roster is unsourced (see below).
- Names in 1888-89: `linfield` = Linfield Athletic; `walsall` = Walsall Town Swifts
  (https://en.wikipedia.org/wiki/Walsall_F.C.); `sheffield-wednesday` = The Wednesday; `heeley` = Sheffield
  Heeley; `small-heath` = Small Heath F.C. (Ltd), renamed from Small Heath Alliance in 1888
  (https://en.wikipedia.org/wiki/Birmingham_City_F.C.); `birmingham-st-georges` dropped "Mitchell" in 1888
  (https://en.wikipedia.org/wiki/Birmingham_St_George%27s_F.C.); `chatham-town` played as Chatham
  (https://en.wikipedia.org/wiki/Chatham_F.C. redirects to Chatham Town; lineage not checked on fchd.info).
- R1 results for `results.json` (E-00): Burnley 4-3 Old Westminsters; Swifts 3-1 Wrexham; Notts County 2-0 Old
  Brightonians; Nottingham Forest 2-2 Linfield Athletic (Linfield conceded the tie for an ineligible player,
  William Johnston; Forest through); Aston Villa 3-2 Witton; Bootle 0-3 Preston; Accrington 1-1 Blackburn
  (replay 0-5); Chatham 2-1 South Shore; Grimsby 3-1 Sunderland Albion; Crewe 2-2 Halliwell (replay 1-5);
  Wolves 4-3 Old Carthusians; Derby County 1-0 Derby Junction; Notts Rangers 1-1 The Wednesday (replay 0-3);
  Birmingham St George's 3-2 Long Eaton Rangers; Small Heath 2-3 WBA; Walsall Town Swifts 5-1 Sheffield Heeley.
  All 2 Feb 1889, replays 9 Feb (FAC).

### Needs review
- **Scotland's continent:** Europe. Plain fact; the legacy `africa` is a data error.
- **`racecourse-ground-wrexham`:** correct name is the Racecourse Ground, Wrexham (Wrexham's ground since 1872:
  https://en.wikipedia.org/wiki/Racecourse_Ground). Keep the id; fix the description if it says otherwise.
- **Everton "Roberts" (8.txt line 49):** Isaac "Ike" Roberts, centre-half, 1 league game (9 Feb 1889 v Wolves),
  born c.1865 Liverpool: https://en.wikipedia.org/wiki/Roberts_(footballer)
- **Notts County "Galbraith" (11.txt line 34):** a forward with 1 league game, surname only in
  https://en.wikipedia.org/wiki/1888%E2%80%9389_Notts_County_F.C._season. First name **not found**. Do not merge
  with Hugh Galbraith (b. 1868-12-22, Bootle/Accrington 1888-89).
- **`derby-junction` (10 players):** all nine "Higgins" look invented (in no source); William Chatterton was at
  Derby County. Only nearby-season names found (see rosters).

### Same name, different birth data
| ids | verdict | evidence |
|---|---|---|
| bob-roberts-1864 / -1859 / -1859-2 | **three different people**. Bolton: Robert Roberts b. Jul 1864 Penycae. WBA: goalkeeper b. 1859-04-09. Wrexham: Bob Roberts b. **1863** (not 1859) | [1864](https://en.wikipedia.org/wiki/Robert_Roberts_(footballer,_born_1864)) · [1859](https://en.wikipedia.org/wiki/Bob_Roberts_(footballer,_born_1859)) · [1863](https://en.wikipedia.org/wiki/Bob_Roberts_(footballer,_born_1863)) |
| james-duerden-1866 / -1865 | same person; Blackburn Rovers in 1888-89 (Burnley only 1890-91) → drop the Burnley entry | https://en.wikipedia.org/wiki/James_Duerden |
| joe-lofthouse-1864 / -1865 | same person, b. 1865-04-14, **Accrington** all season (21 league + 2 cup); not Bootle, Crewe or Long Eaton | https://en.wikipedia.org/wiki/Joe_Lofthouse |
| john-brodie-1862 / -1865 | different people. Wolves: b. 1862-08-30. Burnley's John C. Brodie signed Nov 1890 → drop from 1888-89 | [1862](https://en.wikipedia.org/wiki/John_Brodie_(footballer,_born_1862)) · [d.1901](https://en.wikipedia.org/wiki/John_Brodie_(footballer,_died_1901)) |
| pat-gallocher-1865 / -1865-2 | same person, b. 1865-04-25; Burnley (20 apps) then Accrington from March 1889 (1 app): a real mid-season move → merge | https://en.wikipedia.org/wiki/Pat_Gallocher |
| robert-haresnape-1866 / -1865 | same person, b. 1866-07-14; Blackburn in 1888-89, Burnley from 1889 → merge, drop Burnley | https://en.wikipedia.org/wiki/Robert_Haresnape |
| william-chatterton-1865 / -1861 | same person, b. 1861-12-27 (also the Derbyshire cricketer); Derby County (5 apps) → merge, drop Derby Junction / Long Eaton | https://en.wikipedia.org/wiki/William_Chatterton |
| william-ingram-1867 / -1867-2 | probably different. Wednesday's Billy Ingram b. **1865-12-11** (fix); the Old Westminsters Ingram not found | https://en.wikipedia.org/wiki/Billy_Ingram |

### Same person at several clubs (league players)
| person | 1888-89 club(s) | source |
|---|---|---|
| Archie Goodall (b. 1865-01-03) | Preston to 30 Sep 1888, Aston Villa from 1 Oct 1888: a real mid-season transfer | https://en.wikipedia.org/wiki/Archie_Goodall |
| Bob Howarth (b. 1865-06-20) | Preston only; drop Bootle | https://en.wikipedia.org/wiki/Bob_Howarth |
| Dennis Hodgetts (b. 1863-11-28) | Aston Villa only (left St George's 1886); drop Birmingham St George's | https://en.wikipedia.org/wiki/Dennis_Hodgetts |
| Billy Hendry (b. **1869-06-20**) | WBA, then Stoke in April 1889: real move | https://en.wikipedia.org/wiki/Billy_Hendry |
| Henry Parkinson (b. 1864-12-03) | Everton (1 app, Dec 1888), then Accrington (1 app, Mar 1889): real move | https://en.wikipedia.org/wiki/Henry_Parkinson_(footballer,_born_1864) |
| Jack Yates (b. 1861-01-03) | Burnley only; drop Swifts | https://en.wikipedia.org/wiki/Jack_Yates_(footballer,_born_1861) |
| Haydn Morley (b. 1860-11-26) | Notts County, then Derby County from Mar 1889 (single source also mentions Forest) | https://en.wikipedia.org/wiki/Haydn_Morley |
| Billy Fletcher | not found at Forest or Notts Rangers | — |
| Billy Ollis (b. 1871-08-12) | at Warwick County until 1891; drop Crewe and Small Heath | https://en.wikipedia.org/wiki/William_Ollis |
| Fred Geary (b. 1868-01-23) | Notts Rangers in 1888-89 (snapshot puts him at Bootle) | https://en.wikipedia.org/wiki/Fred_Geary |

### Burnley placeholders (21) and missing players
- **Played 1888-89:** Friel, Yates, Gallocher, Sandy Lang (b. 1864), William Bury, Jack Keenan (b. 1863), Bill
  McFettridge (b. 1864-04-22), Abrahams, Walter Cox (b. 1863), William Tait (left Oct 1888), Ross McMahon,
  Thomas Midgley, Alec Brady (b. 1870-02-09; some apps belong to William Brady; left Mar 1889).
- **Later seasons, drop from 1888-89:** Archibald Kaye, Thomas White, Jimmy Hill, Claude Lambie, Daniel Spiers,
  Haresnape (1889-); Duerden, John C. Brodie, Thomas Crossley, Zacharias Carr, John Walker (1890-91); Billy
  Bowes (1890-); Tom Nicol (1891-); John Oswald (after 1890-91); Alexander McLardie (1892).
- **Not found as Burnley players:** James McFettridge, John Haworth (the one found is the later manager, b. 1876),
  Joseph Nash.
- **Missing real 1888-89 players to add:** Fred Poland (9 apps), William McKay (14), William Brady, Robert Kay
  (GK, 5), William Smith b. 1865 (3), Robert Duckworth (1). Player articles linked from
  https://en.wikipedia.org/wiki/List_of_Burnley_F.C._players ; list may be incomplete.

### Copied rosters: what each club really had
Every shared name in the nine "shared roster" items is wrong for at least one of the clubs. Proposed rebuilds
(confirmed = a source puts him there in 1888-89; nearby = adjacent season, use only to fill to 11):

- **linfield** (Linfield Athletic): confirmed Jack Peden (b. 1863-07-12), Samuel Torrans (1869-02-04), George
  Gaffikin (1868-05-17), Sam Johnston (birth not found; not the 1866 Distillery player); nearby William Dalton
  (1870-03-16), Robert Milne (**1870-10-01**). Remove Gillespie (Queen's Park GK, b. 1859-06-22), Hannah, Watson,
  and the duplicate "John Peden".
- **sunderland-albion**: confirmed James Allan (1857-10-09), John Angus (GK, 1867), Bob Brand (1865-08-03, from
  Jan 1889); first line-up 5 May 1888 by surname only (Stewart, Oliver, Gilmartin, Richardson, Moore, Baxter,
  Scott, Kilpatrick, Melville, Monaghan, Nugent); nearby Jimmy Hannah (b. **1869-03-17**, from 1889), Jimmy Weir
  (1864, from 1889). https://en.wikipedia.org/wiki/Sunderland_Albion_F.C.
- **notts-rangers**: confirmed Fred Geary; likely Arthur Elliott (1870); nearby William Brown (GK, 1889-92).
  Remove all 11 current names. https://en.wikipedia.org/wiki/Notts_Rangers_F.C.
- **walsall** (Walsall Town Swifts): confirmed Albert Aldridge (b. 1863-08-04 or 1864-04-13, sources differ),
  Alf Jones (c. Feb 1861, ever-present), Charlie Shaw (1862-11-18, from Oct 1888); nearby Tom Bayley (Aug 1868);
  a "Gray" scored v Wolves 16 Feb 1889 (maybe the current David Grey; unverified). Remove the rest.
- **witton-albion → witton (Blackburn)**: no 1888-89 players found for either club; all 10 current names unsourced.
- **sheffield-wednesday** (The Wednesday): confirmed XI v Wolves, 2 Mar 1889 (FA Cup R3): Jim Smith (GK), Teddy
  Brayshaw (1863-10-06), Thompson, Jack Dungworth (c.1866), Billy Betts (1864-03-26), George Waller (1864-12-03),
  Winterbottom, Billy Ingram (1865-12-11), Harry Woolhouse (1867), Tom Cawley, Albert Mumford (1865-06-07).
  Remove Andrew Amos (Corinthians), James Lang (retired c.1884), Tom Brandon (St Mirren 1887-89), John Hudson
  (Wednesday only 1880-85), George Chaloner (not found). Whitham uncertain (1 app in 1887-88).
  Line-up source read via a search summary of https://wolvescompletehistory.co.uk/?p=1583 (page refused direct access).
- **heeley** (Sheffield Heeley): **no 1888-89 player found**; all 11 are Wednesday copies (Betts left Heeley 1883,
  Hudson 1884). Needs an offline source.
- **small-heath**: confirmed or at the club across 1888-89: Chris Charsley (GK, 1864-11-07), Fred Speller (1863),
  Caesar Jenkyns (1866-08-24), Harry Morris (1866-04-11), Ted Devey (1871-03-19), Charlie Simms, Will Devey
  (1865-04-12), Eddy Stanley (1860), George Short (1866), Ted Hill. Remove Fred Wheldon (from 1890), Charlie Hare
  (b. 1870-03-16, from 1891), Billy Ollis, Jack Hallissey (not found), Tom Green (Aston Villa 1887-89).
- **birmingham-st-georges**: confirmed Billy Siddons (from Aston Villa for 1888-89); nearby Jack Devey
  (1866-12-26, 1889-91). Remove Hodgetts, Green and the copied Small Heath players. Mostly not found.
- **crewe-alexandra**: confirmed William Bell (1860-08-22, 6 Combination games), John Pearson (1868-01-25, Crewe
  1886-95); nearby Alfred Davies (1863), Richard Jones (1867), Billy Lewis (1864). Remove the five Belfields
  (invented), Lofthouse, Ollis; Whittaker, Sagar, Hill not found.
- **long-eaton-rangers**: confirmed William "Tich" Smith (1868-11-10); nearby George Clifton (1865-08-17, Derby
  County in 1888-89). Remove Chatterton, the Belfields, Pearson, Lofthouse, Whittaker, Sagar, Hill.
- **derby-junction**: nearby only: Joseph Marshall (GK, 1862), Enos Bromage (GK, 1864-04-30), S. Smith,
  W. Hopkins (1887-88 cup scorers). Remove Chatterton and the nine Higgins.
- **bootle**: Andrew Watson (1856-05-24, 1887-92), Bob McFarlane (c.1866, 1888-90), Job Wilding (1862, from
  1888), David Jardine (GK, 1867-11-03), Hugh Galbraith (1868-12-22, 1 app); nearby Walter Evans (1867). Remove
  Howarth, Lofthouse, Geary, Billy Hughes (Bootle 1892-93); Veitch, Jackson, Woods, Sissons, Gillam not found;
  John Campbell's birth date matches the Celtic/Villa player, not verified.

### Decision: short squads (producer, 2026-11-27)
Several minor clubs cannot reach 11 sourced 1888-89 players (Heeley, Witton, Derby Junction, Birmingham St
George's, Notts Rangers, Long Eaton). Fill them in this order: (1) players sourced to the club in 1888-89;
(2) players sourced to the club in 1887-88 or 1889-90, `confidence: unverified` with a note naming the season;
(3) generated period-appropriate names marked `confidence: placeholder` up to 11. Invented duplicates
(the Belfields, the Higgins) are removed, not kept as fillers.

### Offline sources for the gaps
Tony Onslow, *The Forgotten Rivals* (Bootle); Marco Crisp, *Crewe Alexandra Match by Match*; the Football
Alliance/Combination match-by-match book cited on the Long Eaton Rangers article; Sheffield and Birmingham
newspaper reports of the FA Cup ties (British Newspaper Archive).
