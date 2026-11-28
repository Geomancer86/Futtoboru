# Players 1888-89: fan-love tiers and bios (H-01, WORKSTATION, 2026-11-27)

Research for the game, not game data yet: the fields that would carry it are proposed in
[`docs/design/HISTORY_DATA_PROPOSAL.md`](../../design/HISTORY_DATA_PROPOSAL.md). Club histories are in
[`clubs/`](clubs/). Squad positions (H-04) and their sources: [`POSITIONS_1888-89.md`](POSITIONS_1888-89.md). Person ids are the ones in `gamedata/core/people.json` (ids never change, even where the birth year
inside the id is wrong; corrections are listed under "Data notes" at the end of each club section and in the club files).

**Short sources:** `W:Page` = `https://en.wikipedia.org/wiki/Page` (spaces as `_`). "season page" = that club's
"1888–89 <Club> F.C. season" Wikipedia article. Bios are written in our own words from the cited pages; nicknames are
listed only where a source gives them (and marked "later" when they are not period usage).

## Tier scale (fan love, per club)
| Tier | Name | Criteria for the **start value** (August 1888) |
|---|---|---|
| 0 | Unknown | name only, nothing beyond a line-up |
| 1 | Squad player | sourced 1888-89 appearances, nothing notable |
| 2 | Local favourite | regular starter, several seasons at the club, local-born mainstay, useful goals |
| 3 | Crowd idol | key man in 1888-89: captain, top scorer, a cap while at the club, sourced nickname or popularity |
| 4 | Club hero | central in the club's trophies / greatest era, several caps, named among the period's greats |
| 5 | Legend | national star of the era, still regarded among the club's greatest ever |

**Tier Aug 1888** is what the data would carry for season 1. **Legacy** is the ceiling the real player reached by about
1895: a design/balancing note only, never stored or scripted (the game must earn it, D17 spirit). Tiers are per club:
a player who moves starts again at his new club (see the proposal, §4, for how tiers could evolve in play).

Clubs in the order of the 1888-89 final table. FA Cup-only entrants are at the end.

## Preston North End (`preston-north-end`)
Apps/goals from the season page (W:1888–89_Preston_North_End_F.C._season). Club file: [clubs/preston-north-end.md](clubs/preston-north-end.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| john-goodall-1863 | John Goodall | — | 4 | 5 | Centre-forward born in London to Scottish parents, raised in Kilmarnock; at Preston since 1885, England cap 1888 (14 caps, 12 goals by 1898). Top scorer of the first League season (21 League goals per the season page, 20 per his article). Left for Derby in 1889. | W:John_Goodall |
| fred-dewhurst-1863 | Fred Dewhurst | — | 4 | 4 | Amateur inside forward from Fulwood, a schoolmaster at Preston Catholic College; 9 England caps, 11 goals (1886-89). 12 League goals; captain in the 1889 Final and scorer of its first goal. Died 1895, aged 31. | W:Fred_Dewhurst, W:1889_FA_Cup_final |
| jimmy-ross-1866 | Jimmy Ross | "Little Demon" | 3 | 4 | Edinburgh inside forward, Nick Ross's younger brother, scorer of 7 in the 26-0 over Hyde (1887). 19 League goals and a goal in the Final; champion again in 1890. | W:Jimmy_Ross_(footballer,_born_1866) |
| james-trainer-1863 | James Trainer | — | 3 | 4 | Wrexham-born goalkeeper, 20 Wales caps (1887-99); kept goal in 20 League games and won four titles with Preston. ("Prince of Goalkeepers" is often attached to him but was not found in a source; not used.) | W:James_Trainer |
| robert-mills-roberts-1862 | Robert Mills-Roberts | — | 3 | 3 | Welsh amateur goalkeeper and surgeon, 8 Wales caps; kept goal in all five Cup ties without conceding. | W:Robert_Mills-Roberts |
| bob-howarth-1865 | Bob Howarth | — | 3 | 3 | Preston-born full-back and solicitor, 4 England caps (1886-94); 18 League and 5 Cup apps. To Everton 1891. | W:Bob_Howarth |
| robert-holmes-1867 | Bob Holmes | "last of the Invincibles" (later) | 2 | 4 | Preston-born left-back, ever-present (22); 7 England caps 1888-95, at the club until 1902 (300 League games). | W:Bob_Holmes_(footballer) |
| jack-gordon-1861 | Jack Gordon | — | 2 | 3 | Winger from Bridge of Weir, at Preston from 1884; 10 League goals incl. a hat-trick in the 7-0 at Notts County and a goal on day one. | W:Jack_Gordon_(footballer,_born_1863) |
| johnny-graham-1857 | Johnny Graham | — | 2 | 3 | Wing-half from Coylton, Scotland cap 1884; ever-present in the Double season. | W:Johnny_Graham_(footballer,_born_1857) |
| david-russel-1863 | David Russell | — | 2 | 3 | Tough-tackling centre-half from Beith (18 apps); scored the semi-final winner v WBA. To Forest 1890. | W:David_Russell_(Scottish_footballer) |
| sam-thomson-1862 | Sammy Thomson | — | 2 | 3 | Winger from Lugar, 2 Scotland caps (1884); scored Preston's third in the Final. | W:Sammy_Thomson |
| george-drummond-1865 | Geordie Drummond | "Geordie" | 2 | 3 | Edinburgh utility man and left winger, at Preston 1883-99 (12 League apps plus 4 in the Cup). | W:George_Drummond_(footballer,_born_1865) |
| sandy-robertson-1860 | Sandy Robertson | — | 1 | 2 | Edinburgh half-back, 21 of 22 League games (3 goals); to Royal Arsenal 1890. | W:Sandy_Robertson_(footballer,_born_1860) |
| archie-goodall-1865 | Archie Goodall | — | 1 | 3 | John's Belfast-born brother: 2 apps, 1 goal, then to Villa on 1 Oct 1888 (first in-season League transfer). | W:Archie_Goodall |
| willie-graham-1866, jack-edwards-1867, jock-inglis-1859, richard-whittle-1864 | W. Graham (5 apps), Edwards (4 apps, 3 goals), Inglis (1-3 apps, sources differ), Whittle (1 app, a long-range goal v Stoke) | — | 1 | 1 | Bit-part players. | W:Willie_Graham_(footballer,_born_1866), W:Jack_Edwards_(footballer,_born_1867), W:Jock_Inglis, W:Richard_Whittle |

Not in the data: **William Sudell**, chairman and de facto manager (W:William_Sudell). **Nick Ross** was at Everton in
1888-89 (see Everton): he is an Invincible of 1887-88 and 1889-90, not of the Double season.
Data notes: `david-russel-1863` is David Russell (b. 1863-07-06); `jack-edwards-1867` is English (b. Preston 1867-08-25), stored as `scotland`.

## Aston Villa (`aston-villa`)
Season page: W:1888–89_Aston_Villa_F.C._season. Club file: [clubs/aston-villa.md](clubs/aston-villa.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| archie-hunter-1859 | Archie Hunter | "The Old Warhorse" | 5 | 5 | Ayrshire forward at Villa since 1878, captain of the 1887 Cup winners and a scorer in every round; his 36 FA Cup goals are still a club record. Never capped (Scotland did not pick England-based men). Collapsed during a match in 1890, died 1894; a Football League 100 Legend. | W:Archie_Hunter |
| dennis-hodgetts-1863 | Dennis Hodgetts | "Denny" | 4 | 5 | Birmingham winger who scored the first goal of the 1887 Final; 6 England caps (1888-94), 10 goals in 1888-89; League champion 1894 and 1896, Cup winner 1895. | W:Dennis_Hodgetts |
| albert-allen-1867 | Albert Allen | — | 3 | 3 | Aston-born inside forward, hat-trick on his only England cap (1888); top scorer (19) incl. the club's first League hat-trick (9-1 v Notts County). Died 1899. | W:Albert_Allen |
| jimmy-warner-1865 | Jimmy Warner | — | 3 | 3 | Lozells-born goalkeeper, 1887 Cup winner (21 League apps). | W:Jimmy_Warner |
| arthur-brown-1858 | **Albert** Brown (see note) | — | 2 | 3 | Right winger and 1887 Cup winner, ever-present in 1888-89 (7 League goals); at Villa 1884-94. | W:Albert_Brown_(footballer,_born_1862) |
| tommy-green-1863 | Tommy Green | — | 2 | 2 | Worcester-born forward, ex-WBA; scored Villa's first League goal and played all 22 games (15 goals). | W:Tommy_Green_(footballer,_born_1863) |
| gershom-cox-1863 | Gershom Cox | — | 2 | 2 | Birmingham full-back, ever-present; his own goal at Wolves on 8 Sep 1888 was long listed as the League's first goal. | W:Gershom_Cox |
| harry-devey-1864 | Harry Devey | — | 2 | 2 | Birmingham centre-half who missed one game. | W:Harry_Devey |
| frank-coulton-1866 | Frank Coulton | — | 2 | 2 | Stylish full-back (19 League, 2 Cup apps). | W:Frank_Coulton |
| john-burton-1863 | John Burton | — | 2 | 2 | Handsworth wing-half, 1887 Cup finalist. | W:John_Burton_(footballer,_born_1863) |
| harry-yates-1861 | Harry Yates | "Tubby" | 2 | 2 | Walsall-born defensive wing-half, 1887 Cup winner (13 apps). | W:Harry_Yates_(footballer,_born_1861) |
| archie-goodall-1865 | Archie Goodall | — | 1 | 3 | Arrived 1 Oct 1888 from Preston; 7 goals in 14 games, to Derby May 1889; later 10 Ireland caps. | W:Archie_Goodall |
| arthur-wollaston-1865, frankie-dawson-1862, arthur-dixon-1867, batty-garvey-1865, thomas-harrison-1866, walter-ashmore-1861 | Wollaston (4+1), Dawson (3), Dixon (3, 1 goal), Garvey (2), Harrison (1), Ashmore (1) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: **George Ramsay** ("Scotty"), secretary-manager from 1886 (W:George_Ramsay_(footballer,_born_1855));
**William McGregor**, committee man and founder of the Football League (W:William_McGregor_(football)); Frederick Rinder;
Bob Thomas (debut 1888-89, missing from the squad).
Data notes: **`arthur-brown-1858` is the wrong person**: Arthur "Digger" Brown had retired by 1886; the 1888-89 man is his
brother Albert Alfred Brown (born Coventry, 1862-01-07 or 1865-01-06, the article contradicts itself) → a new person record.

## Wolverhampton Wanderers (`wolverhampton-wanderers`)
Season page: W:1888–89_Wolverhampton_Wanderers_F.C._season. Club file: [clubs/wolverhampton-wanderers.md](clubs/wolverhampton-wanderers.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| john-brodie-1862 | John Brodie | — | 3 | 4 | Co-founded St Luke's at 15, a schoolteacher; captain in the 1889 Final and England captain on his 1889 debut (3 caps). 11 League goals. | W:John_Brodie_(footballer,_born_1862) |
| harry-allen-1866 | Harry Allen | — | 3 | 4 | Walsall-born centre-half, 5 England caps 1888-90, ever-present; later captain and scorer of the 1893 Final winner. Died 1895. | W:Harry_Allen_(footballer,_born_1866) |
| dickie-baugh-1864 | Dickie Baugh | — | 3 | 4 | Right-back from Staveley, 2 England caps, ever-present; three Finals (winner 1893, captain 1896). | W:Dickie_Baugh |
| charlie-mason-1863 | Charlie Mason | — | 3 | 3 | Wolverhampton-born full-back and founder member, nearly 300 games, 3 England caps (1887-90). | W:Charlie_Mason_(footballer,_born_1863) |
| jack-baynton-1859 | Jack Baynton | — | 3 | 3 | Founder-era player and early secretary, Wrekin Cup captain 1884, now goalkeeper (18 apps, played in the Final). Left April 1889. | W:Jack_Baynton |
| harry-wood-1868 | Harry Wood | "the wolf" (later, at Southampton) | 2 | 4 | Walsall-born inside forward, top scorer (13); 3 England caps (1890-96), 1893 Cup winner. | W:Harry_Wood_(footballer,_born_1868) |
| arthur-lowder-1863 | Arthur Lowder | — | 2 | 2 | Local left-half from St Luke's (1882); England cap Feb 1889. | W:Arthur_Lowder |
| albert-fletcher-1867 | Albert Fletcher | — | 2 | 3 | Right-half, 2 England caps (1889, 1890); later Wolves trainer until 1920. | W:Albert_Fletcher_(footballer,_born_1867) |
| tommy-hunter-1863 | Tommy Hunter | — | 2 | 2 | Walsall-born winger (20 apps, 4 goals), played in the Final. | W:Tommy_Hunter_(footballer) |
| joe-cooper-1865 | Joe Cooper | — | 2 | 2 | Local inside forward (21 apps, 6 goals). | W:Joe_Cooper_(footballer,_born_1865) |
| david-wykes-1867 | David Wykes | — | 1 | 3 | Joined Aug 1888 (4 League + 3 Cup goals); 1893 Cup winner, died 1895. | W:David_Wykes |
| tommy-knight-1865 | Tommy Knight | — | 1 | 2 | Local inside-right, 7 League and 5 Cup goals. | W:Tommy_Knight_(footballer) |
| alec-cannon-1866, william-rose-1865, walter-white-1866, nick-anderson-1867, john-benton-1866, r-dudley-1865, t-tomkyes-1866 | Cannon (9), Rose (GK, 4), White (4, 2 goals), Anderson (2), Benton, Dudley, Tomkyes (1 each) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: **Jack Addenbrooke**, secretary-manager from 1885 (W:Jack_Addenbrooke).

## Blackburn Rovers (`blackburn-rovers`)
Season page: W:1888–89_Blackburn_Rovers_F.C._season. Club file: [clubs/blackburn-rovers.md](clubs/blackburn-rovers.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| jimmy-forrest-1864 | Jimmy Forrest | — | 4 | 5 | Blackburn-born half-back, the first professional to play for England (11 caps 1884-90) and a five-time FA Cup winner (1884-86, 1890, 1891); later a director. | W:Jimmy_Forrest_(footballer), https://www.brfcs.com/player-archive/james-henry-forrest/ |
| herby-arthur-1863 | Herby Arthur | — | 4 | 4 | Blackburn-born goalkeeper, 7 England caps (1885-87), four FA Cups; remembered as the man left alone on the pitch at Burnley in 1891. | W:Herby_Arthur |
| fergus-suter-1857 | Fergus Suter | — | 3 | 4 | Glasgow stonemason often called the first professional footballer; four Finals, three wins. One League game in 1888-89 (22 Dec). | W:Fergus_Suter |
| john-forbes-1862 | John Forbes | — | 3 | 3 | Vale of Leven full-back, 5 Scotland caps; Cup winner 1890 and 1891. | https://spartacus-educational.com/BLACKBforbes.htm |
| jack-southworth-1866 | Jack Southworth | "Skimmy", "Prince of Dribblers" | 2 | 5 | Blackburn centre-forward and professional violinist; top scorer (17 League + 4 Cup), four v Villa. 3 England caps, a goal in each; later the first to 100 League goals. | W:Jack_Southworth |
| william-townley-1866 | William Townley | — | 2 | 4 | Blackburn-born left winger (8 League goals); 2 England caps, first Cup Final hat-trick (1890); later a coach in Germany. | W:William_Townley |
| nat-walton-1867 | Nat Walton | — | 2 | 3 | Preston-born inside forward, 1886 Cup winner (10 League goals); England cap 1890. | W:Nat_Walton |
| jimmy-douglas-1859 | Jimmy Douglas | — | 2 | 3 | Renfrew half-back, Scotland cap 1880, at Rovers since 1880. | W:Jimmy_Douglas_(Scottish_footballer) |
| harry-fecitt-1864 | Harry Fecitt | — | 2 | 2 | Blackburn forward, 1885/86 Cup winner, back from Accrington (12 League goals). | W:Harry_Fecitt |
| joe-beverley-1856 | Joe Beverley | — | 2 | 2 | Blackburn full-back, ex-Olympic, 3 England caps (1884); briefly the League's oldest player. | W:Joe_Beverley |
| robert-haresnape-1866 | Robert Haresnape | — | 1 | 2 | Winger, Cup hero of the season (hat-trick in the 8-1 v Villa). To Burnley 1889. | W:Robert_Haresnape |
| willie-almond-1868 | Willie Almond | — | 1 | 2 | Blackburn centre-half from Witton (21 apps). | W:Willie_Almond |
| james-southworth-1864 | James Southworth | — | 1 | 2 | Left-back, Jack's brother, ex-Olympic (19 apps). | W:James_Southworth |
| james-beresford-1860, john-barton-1866, billy-mcowen-1871, bernard-whittaker-1865, james-brown-1865, james-duerden-1866, james-stothert-1870, w-mitchell-1866, william-holden-1860, robert-porter-1867 | Beresford (12, 4 goals), Barton (5+1), McOwen (5), Whittaker (4), Brown (4), Duerden (2), Stothert, Mitchell (2 goals), Holden, Porter (1 each) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: **Thomas Mitchell**, secretary-manager 1887-96 (W:Thomas_Mitchell_(football_manager)).

## Bolton Wanderers (`bolton-wanderers`)
Season page: W:1888–89_Bolton_Wanderers_F.C._season. Club file: [clubs/bolton-wanderers.md](clubs/bolton-wanderers.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| kenny-davenport-1862 | Kenny Davenport | — | 3 | 4 | Bolton-born inside right at the club since 1883, England caps 1885 and 1890; ever-present (11 goals). Since 2013 credited with the first Football League goal (8 Sep 1888, 3:47 pm, Pikes Lane; plaque 2016). | W:Kenny_Davenport |
| bob-roberts-1864 | Bob Roberts | — | 3 | 3 | Welsh wing-half from Penycae, at Bolton since 1884-85, 9 Wales caps; ever-present. | W:Robert_Roberts_(footballer,_born_1864) |
| alexander-barbour-1862 | Alexander Barbour | — | 2 | 2 | Dumbarton striker, 1885 Scottish Cup winner with Renton, 1 Scotland cap; 9 goals in 14. | W:Alexander_Barbour |
| james-brogan-1864 | James Brogan | — | 1 | 2 | Scottish forward from Beith, new in 1888; ever-present and top scorer (13). | W:James_Brogan_(footballer,_born_1865) |
| david-weir-1863 | Davie Weir | — | 1 | 3 | Aldershot-born centre-half/forward from Halliwell (1888); ever-present, 10 goals incl. a hat-trick v Accrington; 2 England caps 1889. | W:Davie_Weir_(footballer) |
| di-jones-1867 | Di Jones | — | 1 | 4 | Welsh left-back from Chirk (two Welsh Cups); ten years and 228 games at Bolton, captain in the 1894 Final, 14 Wales caps. | W:Di_Jones |
| john-milne-1866 | John Milne | — | 1 | 2 | Ever-present forward/centre-half (6 goals); little else is known. | W:John_Milne_(English_footballer) |
| charlie-harrison-1861 | Charlie Harrison | — | 1 | 2 | Goalkeeper from Newton Heath (19 apps); went back there in 1889. | W:Charlie_Harrison_(footballer,_born_1861) |
| bethel-robinson-1861 | Bethel Robinson | — | 1 | 2 | Fleetwood full-back, ex-Preston (16-18 apps). | W:Bethel_Robinson |
| peter-bullough-1865 | Peter Bullough | — | 1 | 2 | Right-half from Lostock (15 apps). | W:Peter_Bullough |
| harry-tyrer-1868 | Harry Tyrer | — | 1 | 1 | Winger from Halsall, both goals in the 6-2 over Everton. | W:Harry_Tyrer_(footballer,_born_1868) |
| j-scowcroft-1861, w-flitcroft-1864, g-owen-1865, thomas-coupar-1865, sam-gillam-1865, john-mitchell-1861, jim-parkinson-1865, jimmy-turner-1865, w-simmers-1865, d-mercer-1865, e-siddons-1866, frank-dyer-1867, h-mcguinness-1866 | Scowcroft (9), Flitcroft (7), Owen (7, 3 goals), Coupar (3), Gillam (GK, 2), Mitchell, Parkinson (2), Turner (2 + a cup hat-trick), Simmers (2+2), Mercer, Siddons, Dyer, McGuinness (1 each) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: **J. J. Bentley**, secretary (manager 1885-86, 1887-95), founding League committee man, League president
1894-1910 (W:J._J._Bentley).

## West Bromwich Albion (`west-bromwich-albion`)
Season page: W:1888–89_West_Bromwich_Albion_F.C._season. Club file: [clubs/west-bromwich-albion.md](clubs/west-bromwich-albion.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| billy-bassett-1869 | Billy Bassett | — | 4 | 5 | Local outside right, 19, the star of the 1888 Cup final and an England international from 1888 (16 caps, 8 goals to 1896); joint top scorer (11). Cup winner again 1892, later chairman; a Football League 100 Legend. | W:Billy_Bassett |
| bob-roberts-1859 | Bob Roberts | "Long Bob", "The Prince of Goalkeepers" | 3 | 4 | Local goalkeeper, 6 ft 4 in, the club's first international (England 1887, 1888, 1890), 1888 Cup winner; ever-present. | W:Bob_Roberts_(footballer,_born_1859) |
| jem-bayliss-1865 | Jem Bayliss | — | 3 | 4 | Tipton wing-half/forward who captained the 1888 Cup winners and opened the scoring in the Final; England cap 1891. | W:Jem_Bayliss, https://spartacus-educational.com/Fwba.htm |
| george-woodhall-1864 | George Woodhall | "Spry" | 3 | 3 | Local forward who scored the 1888 Final winner; 2 England caps (1888). | W:George_Woodhall |
| ezra-horton-1864 | Ezra Horton | "Ironsides" | 3 | 3 | Defensive right-half, 350+ games, three Finals (winner 1888). | W:Ezra_Horton |
| tom-pearson-1866 | Tom Pearson | — | 2 | 4 | Local inside left, in the 1887, 1888 and 1892 Finals; ever-present and joint top scorer (11). 72 goals in 138 games. | W:Tom_Pearson_(footballer) |
| charlie-perry-1864 | Charlie Perry | — | 2 | 4 | Local centre-half; Cup winner 1888 and 1892, 3 England caps, captain from 1891, later a director. | W:Charlie_Perry_(footballer,_born_1866) |
| george-timmins-1863 | George Timmins | — | 2 | 3 | Left-half at the club from 1880, three Finals; ever-present; called "a hard, fearless man". | W:George_Timmins_(footballer) |
| jack-horton-1863 | Jack Horton | — | 2 | 3 | Ezra's younger brother, full-back 1882-97 (19 apps). | W:Jack_Horton_(footballer,_born_1866) |
| joe-wilson-1865 | Joe Wilson | — | 2 | 3 | Handsworth outside left, 1888 Cup winner, scorer of the club's first League goal (at Stoke); later a League referee and a goldsmith. | W:Joe_Wilson_(footballer,_born_1861) |
| walter-perry-1865 | Walter Perry | — | 1 | 2 | Local inside forward, 4 League goals and 6 in the Cup (hat-trick v Burnley). | W:Walter_Perry_(footballer) |
| billy-hendry-1865 | Billy Hendry | — | 1 | 1 | Scottish centre-forward, 19 (16 apps); to Stoke April 1889. | W:Billy_Hendry |
| harry-green-1865, luther-walker-1862 | Green (9 + 4 Cup), Walker (12) | — | 1 | 1 | Full-backs. | season page |
| george-haynes-1866, fredrick-crabtree-1866, albert-millard-1866, harold-oliver-1865, alexander-ramsey-1865, charlie-shaw-1866 | Haynes (5), Crabtree (1, the winner v Everton), Millard, Oliver, Ramsey, Shaw (1 each) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: chairmen Henry Jackson and Edward W. Heelis (changed during 1888, season page).

## Accrington (`accrington`)
Season page: W:1888–89_Accrington_F.C._season. Club file: [clubs/accrington.md](clubs/accrington.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| george-haworth-1863 | George Haworth | — | 3 | 4 | Accrington-born half-back at the club 1882-92, 5 England caps (1887-90), 1885 FA Cup winner on loan to Blackburn; captain in 1889-90. | W:George_Haworth |
| joe-lofthouse-1864 | Joe Lofthouse | — | 2 | 2 | Blackburn-born winger, four FA Cups with Blackburn and 7 England caps (1885-87); one season here (21 apps). Nationally a tier-4 name, at this club a newcomer. | W:Joe_Lofthouse |
| johnny-horne-1862 | Johnny Horne | — | 2 | 2 | Amateur goalkeeper from Huncoat, a chemist; ever-present, held Preston to their only goalless League game of the season. | W:Johnny_Horne |
| jim-bonar-1866 | Jim Bonar | — | 2 | 2 | Glasgow-born forward at the club since 1884 (17 apps). | W:Jim_Bonar |
| john-kirkham-1869 | Johnny Kirkham | — | 1 | 4 | Forward, 19, two on debut in the 5-5 at Blackburn, 12 goals in 19; became the club's record appearance maker and scorer. | W:John_B._Kirkham |
| billy-barbour-1865 | Billy Barbour | — | 1 | 3 | Kilmarnock centre-forward, top scorer (13); his move from Queen of the South was reportedly paid in groceries. | W:Billy_Barbour |
| bob-brand-1865 | Bob Brand | — | 1 | 2 | Scottish forward, 11 goals in 16, the club's first League hat-trick (6-2 v Derby). | W:Bob_Brand |
| john-stevenson-1862 | John Stevenson | — | 1 | 2 | Glasgow full-back, ever-present; 81 League apps to 1893. | W:John_Stevenson_(footballer,_born_1862) |
| john-mclellan-1863 | John McLellan | — | 1 | 2 | Edinburgh full-back from Hearts, at the club 1888-93. | W:John_McLellan_(footballer) |
| james-tattersall-1864 | James Tattersall | — | 1 | 2 | Accrington-born defender, about 100 League apps to 1893. | W:James_Tattersall |
| luther-pemberton-1864 | Luther Pemberton | — | 1 | 1 | Half-back, 16 apps. | season page |
| j-holden-1865, peter-chippendale-1864, arthur-wilkinson-1865, jonathan-wilkinson-1864, stephen-smith-singleton-1863, j-robertson-1865, j-woods-1865, robert-macbeth-1866, hugh-galbraith-1866, henry-parkinson-1865, pat-gallocher-1865 | Holden (6, 4 goals, the club's first League goal), Chippendale (6), A. Wilkinson (4+2, the Cup goal v Blackburn), J. Wilkinson, Smith-Singleton (4), Robertson (3), Woods (2), Macbeth, Galbraith, Parkinson, Gallocher (1 each) | — | 1 | 1 | Bit-part players. | season page |

Not in the data: club officials not found.

## Everton (`everton`)
Season page: W:1888–89_Everton_F.C._season; goals also https://www.evertonresults.com/188889.htm. All 33 squad ids played. Club file: [clubs/everton.md](clubs/everton.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| nick-ross-1863 | Nick Ross | — | 3 | 3 | Edinburgh-born full-back, Preston's captain and star, lured to Everton for a reported £10 a month; the first full-back to score a League goal. Back to Preston in 1889; called a "footballing genius" by William Pickford (1906); died of TB in 1894. At Preston his tier would be 5. | W:Nick_Ross_(footballer,_born_1862) |
| george-dobson-1862 | George Dobson | — | 3 | 3 | Bolton-born right-back, one of Everton's first professionals (about 130 pre-League games); injury ended his career in 1889. In the Gwladys Street Hall of Fame. | W:George_Dobson_(footballer,_born_1862) |
| george-farmer-1865 | George Farmer | — | 3 | 3 | Oswestry-born wing-half with 2 Wales caps, at the club since 1885 (81 pre-League goals). | W:George_Farmer_(Welsh_footballer) |
| robert-smalley-1865 | Robert Smalley | — | 2 | 2 | Darwen goalkeeper, first choice since Oct 1887. | W:Robert_Smalley |
| alec-dick-1865 | Alec Dick | — | 2 | 2 | Kilmarnock full-back, the first Scottish professional Everton signed; suspended after striking an opponent at Notts County, Nov 1888. | W:Alec_Dick_(footballer) |
| james-weir-1864 | **John** Weir | — | 2 | 2 | Crossmyloof half-back, suspended in 1887 over professionalism, then reinstated (16 apps). | W:John_Weir_(footballer) |
| johnny-holt-1865 | Johnny Holt | "Little Everton Devil" | 1 | 5 | 5 ft 5 in centre-half from Church, signed from Bootle 1888; Everton's first England cap (1890, 10 in all), 1891 champion, Gwladys Street Hall of Fame; known for crafty fouls. | W:Johnny_Holt |
| edgar-chadwick-1869 | Edgar Chadwick | "Hooky" | 1 | 4 | Inside left, 19, from Blackburn (1 Jul 1888); ever-present and top scorer (6). 270 games, 97 goals, 1891 champion, 7 England caps; later coached the Netherlands. | W:Edgar_Chadwick |
| alf-milward-1865 | Alf Milward | — | 1 | 4 | Outside left from Great Marlow, 18, debut Nov 1888; later 201 games, 84 goals, 1891 champion, 4 England caps. | W:Alf_Milward |
| robert-watson-1864 | Robert Watson | — | 1 | 2 | Liverpool-born inside right (18 apps, 4-5 goals). | W:Robert_Watson_(footballer) |
| george-fleming-1864, alex-mckinnon-1864, james-costley-1865, david-waugh-1864, william-brown-1865, jack-angus-1865, charles-jolliffe-1865, william-lewis-1865, william-briscoe-1864, albert-chadwick-1866, jas-coyne-1865, george-davie-1865 | Fleming (4, both goals in the opener: Everton's first League goals), McKinnon (6, hat-trick v Derby), Costley (6, 2), Waugh (7, 2), Brown (6, 2), Angus (5), Jolliffe (4, former first-choice GK), Lewis (3, 1), Briscoe (3), A. Chadwick, Coyne, Davie (2 each) | — | 1 | 1 | Bit-part players. | season page, evertonresults |
| mike-higgins-1865, robert-jones-1864, bob-kelso-1863, jack-keys-1865, r-morris-1865, henry-parkinson-1865, hugh-pollock-1864, roberts-1865, george-stephenson-1864, harry-warmby-1864, walter-wilson-1864 | One League game each (roberts-1865 = Isaac "Ike" Roberts) | — | 1 | 1 | Bit-part players. | season page |

Missing from the squad: **Frank Sugg** (b. 1862-01-11 Ilkeston; centre-forward, 9 apps; England Test cricketer 1888: tier 3,
legacy 1; W:Frank_Sugg) and **Joe Davies** (Chirk, outside right, 8 apps, 2 goals; 11 Wales caps from 1889: tier 1, legacy 2;
W:Joe_Davies_(footballer,_born_1870), https://efcheritagesociety.com/?p=482).
Not in the data: W. E. Barclay, first secretary-manager (W:William_Edward_Barclay); John Houlding, president and landlord of Anfield.
Data notes: `james-weir-1864` is John Weir (b. 1865-01-10, Scotland); `james-costley-1865` first name unresolved (James vs T.).

## Burnley (`burnley`)
League appearance counts from the player articles (the season page has no squad table). Club file: [clubs/burnley.md](clubs/burnley.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| dan-friel-1860 | Dan Friel | — | 3 | 3 | Scottish centre-half from Bonhill, captain since 1883 with 300+ games for the club. The only ever-present of 1888-89 (22); left late 1889. | W:Dan_Friel |
| sandy-lang-1865 | Sandy Lang | — | 3 | 4 | Scottish full-back from Bridge of Weir, captain when League football came to Turf Moor (21 apps). First Burnley man to 100 games (1893), scorer of the club's first League penalty (1891); stayed to 1895. | W:Sandy_Lang |
| john-yates-1861 | Jack Yates | — | 2 | 4 | Blackburn-born left winger, an 1883 FA Cup winner with Blackburn Olympic. 21 apps, 5 goals; Burnley's first England player, hat-trick on his only cap (v Ireland, 2 Mar 1889). | W:Jack_Yates_(footballer,_born_1861) |
| pat-gallocher-1865 | Pat Gallocher | "The Artful Dodger" | 3 | 3 | Quick, tricky Scottish inside forward from Johnstone; scored Burnley's first League goal and was the club's top scorer in all competitions (9). Left for Accrington in March 1889. | W:Pat_Gallocher |
| jack-keenan-1865 | Jack Keenan | — | 2 | 3 | Strong half-back from Clitheroe, a local man in a mostly Scottish side (20 apps); at Burnley 1884-93 and close to an England cap. | W:Jack_Keenan_(footballer) |
| william-bury-1865 | William Bury | — | 2 | 2 | Hard-tackling full-back from Darwen, Lang's partner (20 apps). | W:William_Bury_(footballer) |
| bill-mcfettridge-1865 | Bill McFettridge | — | 2 | 3 | Robust Govan half-back who played a Scottish Cup final at 16 (1880); 16 apps, at Burnley 1886-93. | W:Bill_McFettridge |
| jack-abrahams-1865 | Jack Abrahams | — | 2 | 2 | Barrhead wing-half at the club since 1885 (13 apps); scored the winner v Notts County, 29 Dec 1888. | W:Jack_Abrahams |
| fred-poland-1865 | Fred Poland | — | 2 | 2 | Dundee centre forward, scorer of the club's second League goal (9 apps, 5 goals). Single source: kept goal in the 1-7 at Blackburn. | W:Fred_Poland |
| william-tait-1864 | William Tait | — | 2 | 2 | Glasgow inside forward who scored the first Football League hat-trick (15 Sep 1888, at Bolton; one fan source says two goals). Suspended for drinking after it and gone by late October. | W:William_Tait_(footballer) |
| alec-brady-1865 | Alec Brady | — | 1 | 4 | Scottish inside forward from Cathcart, credited with 7 League goals (some apps may be William Brady's). Later League champion with Everton (1891), Scottish Cup with Celtic (1892), FA Cup with Wednesday (1896). | W:Alec_Brady |
| william-mckay-1865 | Willie McKay | — | 1 | 2 | Scottish inside forward from Hearts (Nov 1888); 7 goals in 14 apps, the team's best League tally. | W:William_McKay_(footballer) |
| walter-cox-1865 | Walter Cox | — | 1 | 2 | Scottish goalkeeper from Leith Harp (Nov 1888): 13 League games and both cup ties. | W:Walter_Cox_(footballer,_born_1863) |
| william-brady-1870 | William Brady | — | 1 | 1 | Forward, scored in the cup v Old Westminsters; records tangled with Alec Brady's. | W:William_Brady_(footballer) |
| robert-kay-1865 | Robert Kay | — | 1 | 1 | Goalkeeper at the club since 1884; 5 apps, dropped after heavy defeats. | W:Robert_Kay_(footballer) |
| william-smith-1865 | William Smith | — | 1 | 1 | Goalkeeper from Haggate, 3 apps including the opening day. | W:William_Smith_(footballer,_born_1865) |
| ross-mcmahon-1865 | Ross McMahon | — | 1 | 1 | Scottish centre forward; 2 League apps (injured in both) and both cup ties. | W:Ross_McMahon |
| thomas-midgley-1860 | Thomas Midgley | — | 1 | 2 | Captain of Burnley Rovers at the 1882 switch from rugby; 1 app (at Stoke) aged about 32. | W:Thomas_Midgley_(footballer) |
| robert-duckworth-1865 | Robert Duckworth | — | 1 | 1 | 1 app. | REVIEW_1888-89_FINDINGS.md |

Not in the data: B. Wyatt Granger (chairman), Harry Bradshaw (committee man from 1887, secretary-manager 1894), Charles
Massey (benefactor); the 1888-89 secretary was not found (see club file).
Data notes: born in Scotland, stored as `england`: sandy-lang, bill-mcfettridge, walter-cox, ross-mcmahon. thomas-midgley born 1856.

## Derby County (`derby-county`)
All 27 ids appear in the season table. Club file: [clubs/derby-county.md](clubs/derby-county.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| alex-higgins-1863 | Alex Higgins | — | 3 | 3 | Kilmarnock centre-forward and former miner, Scotland cap in 1885 (hat-trick on debut). Top scorer with 11 League goals in 21 apps, including Derby's first four-goal game (v Villa). | W:Alex_Higgins_(footballer,_born_1863) |
| george-bakewell-1865 | George Bakewell | — | 3 | 3 | Derby-born winger, thin and fast, the club's second-ever signing; scored Derby's first League goal (16 apps, 6 goals). | W:George_Bakewell |
| benjamin-spilsbury-1865 | Ben Spilsbury | — | 3 | 3 | Repton and Cambridge amateur, 3 England caps (5 goals), scorer of the club's first recorded goal (1884). One League game (1 goal) and the cup tie in 1888-89. | W:Benjamin_Spilsbury |
| william-chatterton-1861 | William Chatterton | — | 3 | 3 | Derbyshire cricket captain, later an England Test player; in the club's first FA Cup tie (1884). 5 apps, 1 goal. | W:William_Chatterton |
| lawrence-plackett-1869 | Lol Plackett | "Lol" | 2 | 2 | Forward from Breaston, the only ever-present (22 apps, 7 goals); two on the opening day. Released 1889. | W:Lawrence_Plackett |
| lewis-cooper-1864 | Lewis Cooper | "Kid" | 2 | 2 | Belper forward, 8 goals in 15 apps; 23 goals in 50 apps over 1888-91. | W:Lewis_Cooper |
| walter-roulstone-1864 | Walter Roulstone | — | 2 | 3 | Left half from Castle Donington (21 apps); first Derby player to 100 League games (118 by 1894). | W:Walter_Roulstone |
| arthur-latham-1863 | Arthur Latham | — | 2 | 3 | Coventry-born full-back, an automatic choice (20 apps); at Derby 1886-1901. | W:Arthur_Latham_(footballer) |
| albert-williamson-1864 | Albert Williamson | — | 2 | 2 | Right half from Sawley, a regular (19 apps). | W:Albert_Williamson |
| haydn-morley-1864 | Haydn Morley | — | 2 | 3 | Derby-born left-back, cricketer and club pioneer; Notts County then Derby (4 apps, 2 cup ties). Captained The Wednesday in the 1890 Cup final. | W:Haydn_Morley |
| levi-wright-1864 | Levi Wright | — | 2 | 2 | Oxford-born teacher and Derbyshire batsman, later a Wisden Cricketer of the Year; 4 apps, 1 goal. | W:Levi_Wright |
| archibald-ferguson-1863 | Archibald Ferguson | — | 1 | 2 | Edinburgh full-back from Hearts, a long kicker (16 apps); later emigrated to the USA. | W:Archibald_Ferguson |
| joseph-marshall-1863 | Joseph Marshall | — | 1 | 1 | Goalkeeper from Ripley, also a Derbyshire cricketer (16 apps). | W:Joseph_Marshall_(footballer) |
| harry-plackett-1866 | Harry Plackett | — | 1 | 1 | Lol's brother, 17 years 147 days on the opening day: the youngest player in the League (16 apps, 2 goals). | W:Harry_Plackett |
| j-smith-1864 | J. Smith | — | 1 | 1 | Half-back, 10 apps. | season page |
| thomas-needham-1865 | Thomas Needham | — | 1 | 1 | Forward, 9 apps, 1 goal. | season page |
| john-lees-1865, william-hopewell-1864, isaac-monks-1864, ruben-pitman-1864 | Lees (5 apps, 2 goals), Hopewell (5), Monks (3), Pitman (GK, 3) | — | 1 | 1 | Bit-part players. | season page |

Tier 0 (1-2 apps, nothing else known): ew-bellhouse-1865, enos-bromage-1864, harold-bestwick-1865, g-clifton-1865,
h-harbour-1865, ernest-hickinbottom-1864, frank-roulstone-1865.
Not in the data: William Morley (proposed and founded the club, W:History_of_Derby_County_F.C._(1884–1967)).
Data notes: harry-plackett born 1871-04-14 (not 1866); archibald-ferguson Scottish; further birth dates in the club file.

## Notts County (`notts-county`)
All 35 ids appear in the season table (six cup ties and goalkeeper changes, not padding). Club file: [clubs/notts-county.md](clubs/notts-county.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| henry-cursham-1863 | Harry Cursham | — | 4 | 5 | Amateur forward with 8 England caps (1880-84) and the all-time FA Cup scoring record (49); a voice for dribbling over combination. 8 League apps plus cup ties, retired January 1889. | W:Harry_Cursham |
| mordecai-sherwin-1862 | Mordecai Sherwin | — | 4 | 4 | Notts cricket captain and England Test wicket-keeper; a goalkeeper of about 17 stone, called "the idol of the crowd", who once turned a cartwheel during a match. 3 cup ties and 1 League game, last on 8 Dec 1888. | W:Mordecai_Sherwin |
| harry-daft-1866 | Harry Daft | — | 3 | 4 | Outside left from Radcliffe-on-Trent, son of Richard Daft and a Notts cricketer; joint top scorer (9). First England cap 2 Mar 1889 (5 in all), 1894 FA Cup winner. | W:Harry_Daft |
| charlie-shelton-1864 | Charlie Shelton | — | 3 | 3 | Left half, England cap in 1888; 15 League apps, 1 goal. | W:Charlie_Shelton |
| william-gunn-1865 | William Gunn | — | 3 | 3 | England Test batsman with 2 football caps (1884), co-founder of Gunn & Moore, known for long one-handed throw-ins; 1 League app, director from 1890. | W:William_Gunn_(cricketer) |
| alf-shelton-1865 | Alf Shelton | — | 2 | 4 | Nottingham left half known for stamina and heading, signed August 1888; 21 apps, the club's most that season. 6 England caps 1889-92, two Cup finals (1891, 1894). | W:Alf_Shelton |
| albert-moore-1865 | Albert Moore | — | 2 | 2 | Nottingham forward with the club since 1883; scored Notts' first League goal on his birthday. Quit in 1889 when his hosiery employer made him choose. | W:Albert_Moore_(footballer,_born_1863) |
| jack-holland-1861 | Jack Holland | — | 2 | 2 | Bulwell tram conductor, first-choice goalkeeper 1886-88 (9 apps). | W:Jack_Holland_(footballer,_born_1861) |
| bob-jardine-1864 | Bob Jardine | — | 1 | 3 | Forward, joint top scorer (9); first man to score five in a League game (v Burnley, 27 Oct 1888). Nationality disputed (Scottish; one source: born Newcastle). | W:Bob_Jardine |
| george-brown-1863 | George H. Brown | — | 1 | 1 | Nottingham wing half from Forest; 19 apps, 1 goal. | W:George_H._Brown_(footballer) |
| bill-hodder-1864 | Bill Hodder | — | 1 | 1 | Forward from Stroud; 20 apps, 3 goals. | W:Bill_Hodder |
| frank-guttridge-1863 | Frank Guttridge | — | 1 | 2 | Nottingham full-back and Notts/Sussex cricketer (17 apps). | W:Frank_Guttridge |
| john-clements-1864 | John Clements | — | 1 | 1 | Full-back from East Markham, signed Oct 1888 (12 League apps plus cup). | W:John_Clements_(footballer) |
| tom-widdowson-1863 | Tom Widdowson | — | 1 | 1 | Hucknall goalkeeper from Forest, Dec 1888 (12 apps). | W:Tom_Widdowson |
| edward-may-1865 | Teddy May | "Teddy" | 1 | 1 | Hull-born left winger, ex-Port Vale (11 apps, 3 goals). | W:Teddy_May |
| tom-mclean-1863 | Tom McLean | — | 1 | 1 | Full-back, 12 apps (identity not confirmed). | season page |
| harry-jackson-1865 | Harry Jackson | — | 1 | 1 | Forward at the club 1884-90 (5 League apps, 3 goals). | W:Harry_Jackson_(footballer,_born_1864) |
| thomas-allin-1865, herbert-emmitt-1864, ted-harker-1865, herbert-snook-1865, billy-may-1864, ben-warburton-1864, edwin-wardle-1864 | Allin (6, 2 goals), Emmitt (7), Harker (5, 2 goals), Snook (6), B. May (4), Warburton (4), Wardle (4) | — | 1 | 1 | Bit-part players (apps incl. cup). | season page |

Tier 0 (1-2 apps): lf-bailey-1865, john-brown-1865, charles-dobson-1865, galbraith-1865, hugh-owen-1864, albert-shaw-1865,
fred-weightman-1865, hh-brown-1865, jt-marshall-1864, h-stevens-1865.
Data notes: mordecai-sherwin born 1851, william-gunn 1858-12-04, henry-cursham 1859-11-27 (ids keep the old years).

## Stoke (`stoke`)
All 32 ids appear in the season table (it includes the reserve XI of the Warwick County cup tie and two Preston loanees). Club file: [clubs/stoke.md](clubs/stoke.md).

| person-id | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|
| tommy-clare-1865 | Tommy Clare | — | 3 | 4 | Right-back from Congleton and captain in 1888 (21 apps). 4 England caps (1889-94), Alliance champion 1891, 252 Stoke games; later Port Vale manager. | W:Tommy_Clare |
| bill-rowley-1863 | Bill Rowley | — | 3 | 4 | Hanley goalkeeper who came from Port Vale after a court fight (1886); 21 apps, England caps 1889 and 1892. Later Stoke's secretary-manager and a noted dealmaker. | W:Bill_Rowley |
| bob-mcskimming-1864 | Bob McSkimming | — | 3 | 3 | Kilmarnock forward and sprinter, called Stoke's first Scottish signing; ever-present and top scorer (6). | W:Bob_McSkimming |
| george-shutt-1864 | George Shutt | — | 3 | 3 | Fenton half-back, one of Stoke's first professionals, England cap 1886 (21 apps); later a referee. | W:George_Shutt |
| jimmy-sayer-1865 | Jimmy Sayer | "Greyhound" | 3 | 3 | Outside right from Mexborough, England cap 1887, named by fans for his pace (7 apps, 1 goal). | W:Jimmy_Sayer |
| alf-underwood-1869 | Alf Underwood | — | 2 | 4 | Hanley left-back, 19, ever-present (22); England caps 1891 and 1892, a famous pairing with Clare until a knee infection ended his career (1893). | W:Alf_Underwood |
| elijah-smith-1863 | Elijah Smith | "Father Smith" | 2 | 2 | Stoke-born half-back at the club since 1883, a clergyman, hence the nickname (21 apps). | W:Elijah_Smith_(footballer) |
| bob-ramsay-1863 | Bob Ramsay | — | 2 | 2 | Liverpool-born full-back/half-back from Port Vale (21 apps). | W:Bob_Ramsay_(footballer) |
| alf-edge-1865 | Alf Edge | — | 2 | 3 | Quick, versatile inside forward from Stone (20 apps, 3 goals); 12 goals in the 1890-91 Alliance title season. | W:Alf_Edge |
| george-lawton-1864 | George Lawton | — | 2 | 2 | Local right winger (13 apps), scorer of the winner v Burnley. | W:George_Lawton_(footballer,_born_1862) |
| bob-milarvie-1864 | Bob Milarvie | — | 1 | 2 | Glasgow forward from Hibernian, Nov 1888 (15 apps, 5 goals; scored on debut). | W:Bob_Milarvie |
| jimmy-sloane-1864 | Jimmy Sloane | — | 1 | 1 | Govan centre forward on a spell away from Rangers (11 apps, 2 goals). | W:Jimmy_Sloane |
| billy-tunnicliffe-1865 | Billy Tunnicliffe | — | 1 | 1 | Hanley left winger, both goals in Stoke's first League win (8 apps, 4 goals). | W:Billy_Tunnicliffe_(footballer,_born_1864) |
| frank-staton-1865 | Frank Staton | — | 1 | 1 | Scored Stoke's first League goal (4 apps, 2 goals). | W:Frank_Staton |
| billy-hendry-1865 | Billy Hendry | — | 1 | 3 | Scottish defender from WBA, April 1889 (2 apps); later Sheffield United captain. | W:Billy_Hendry |
| bill-smalley-1865 | Bill Smalley | — | 1 | 1 | Preston reserve lent for the 0-7 at Deepdale. | W:Bill_Smalley |
| wilf-merritt-1865, george-slater-1865, bob-barr-1864, harry-montford-1865, tom-wainwright-1865, alfred-dempsey-1865 | Merritt (GK, 1), Slater (2, 1 goal), Barr (3), H. Montford (1 + cup), Wainwright (1 + cup goal), Dempsey (Preston loanee) | — | 1 | 1 | Bit-part players. | season page |

Tier 0: charlie-hassall-1864, albert-bourne-1865, edgar-montford-1864, albert-farmer-1864, will-holford-1865,
tom-forrester-1865 (Warwick County cup tie only); charlie-baker-1865, freddie-gee-1865, billy-hutchinson-1865,
thomas-moore-1865 (1 League app each).
Not in the data: Harry Lockett, secretary-manager and first Football League secretary (W:Harry_Lockett).
Data notes: Scottish, stored as `england`: bob-milarvie, jimmy-sloane, billy-hendry. Birth years: bill-rowley 1865-09-11,
george-shutt 1861-12-18, elijah-smith 1860, jimmy-sayer 1862 (more in the club file).

## FA Cup-only entrants (notable players only)
Only the genuinely notable men in these squads were tiered; everyone else defaults to 1 (sourced) or 0 (placeholder).
Several of these squads are still placeholders or doubtful (see each club file and `REVIEW_1888-89_FINDINGS.md`).

| person-id | Club (data) | Name | Nickname(s) | Aug 1888 | Legacy | Bio | Source |
|---|---|---|---|---|---|---|---|
| tinsley-lindley-1865 | nottingham-forest | Tinsley Lindley | — | 4 | 5 | Amateur England centre forward (14 goals in 13 internationals), back at Forest in 1888 after Cambridge. | W:Tinsley_Lindley |
| billy-betts-1864 | sheffield-wednesday | Billy Betts | — | 3 | 3 | Wednesday centre-half and England international, 143 games over two spells. | W:Billy_Betts |
| teddy-brayshaw-1864 | sheffield-wednesday | Teddy Brayshaw | — | 3 | 3 | Wednesday centre-half, England cap 1887. | W:Teddy_Brayshaw |
| albert-mumford-1865 | sheffield-wednesday | Albert Mumford | — | 2 | 3 | Scored Wednesday's goal in the 1890 FA Cup final. | W:Albert_Mumford |
| fred-geary-1868 | notts-rangers | Fred Geary | — | 2 | 4 | Fast centre forward; later Everton's star (86 goals in 99 games) and an England hat-trick on debut. | W:Fred_Geary |
| chris-charsley-1864 | small-heath | Chris Charsley | — | 2 | 4 | Amateur goalkeeper and police officer; England cap 1893. | W:Chris_Charsley |
| caesar-jenkyns-1866 | small-heath | Caesar Jenkyns | "Jumbo", "The Mighty Caesar" | 1 | 4 | Rugged defender who joined in 1888; later 8 Wales caps; released in 1895 after going for spectators. | W:Caesar_Jenkyns |
| andrew-watson-1856 | bootle | Andrew Watson | — | 3 | 3 | Three Scotland caps (1881-82), widely regarded as the first black international footballer; probably paid by Bootle from 1887. | W:Andrew_Watson_(footballer,_born_1856) |
| john-pearson-1868 | crewe-alexandra | John Pearson | — | 2 | 4 | One-club Crewe man, their only England cap (1892); later refereed the 1911 Cup final. | W:John_Pearson_(footballer,_born_1868) |
| william-bell-1860 | crewe-alexandra | William Bell | — | 3 | 2 | Crewe's first international (Wales, 5 caps 1881-86). | W:William_Bell_(Welsh_footballer) |
| james-allan-1857 | sunderland-albion | James Allan | — | 4 | 4 | Founder of both Sunderland AFC and the breakaway Sunderland Albion, and a forward for them. | W:James_Allan_(footballer,_born_1857) |
| jack-peden-1863 | linfield | Jack Peden | — | 3 | 4 | Linfield outside left from 1886, 24 Ireland caps. | W:Jack_Peden |
| charles-wreford-brown-1866 | old-carthusians | Charles Wreford-Brown | — | 3 | 4 | Centre-half, later England captain; credited with coining the word "soccer". | W:Charles_Wreford-Brown |
| william-moon-1868 | old-westminsters | Billy Moon | — | 3 | 4 | Goalkeeper, first capped 1888, 7 caps and once England captain. | W:Billy_Moon |
| percy-walters-1863, arthur-walters-1864 | old-westminsters (wrong, see note) | Percy and Arthur Walters | "Morning" and "Afternoon" | 4 | 4 | England's leading full-back pair (Percy 13 caps, 5 as captain). | W:Percy_Walters |
| bambridge-charles-1858 | swifts | Charles Bambridge | "Charlie Bam" | 3 | 3 | Very fast winger, 18 England caps (1879-87), twice captain; near the end of his career. | W:Charles_Bambridge |
| albert-aldridge-1863 | walsall | Albert Aldridge | — | 3 | 3 | Walsall-born England full-back, 1888 FA Cup winner (with WBA). | W:Albert_Aldridge |
| alf-jones-1861 | walsall | Alf Jones | — | 2 | 2 | Walsall-born England full-back. | W:Albert_Aldridge |

Data notes (detail in the club files): the Walters brothers were Old Carthusians, not Old Westminsters; `arthur-dunn-1860`
and `norman-pares-1854` (b. 1857) were Old Etonians; `harry-cursham-1859` at Swifts looks like Notts County's Harry
Cursham (`henry-cursham-1863`); George Cotterill is in two squads (Swifts, Old Brightonians); Forest's `harry-linacre-1869`
is the 1880-born man who joined in 1899 and `john-mcpherson-1867` was at Hearts 1888-91; `evelyn-lintott-1883` (Old
Carthusians) is impossible.

## Tier counts (start values, August 1888)
Counted from the tables above; group rows count once per person. Frank Sugg and Joe Davies (Everton, missing from the
data) are not counted.

| Tier | 0 | 1 | 2 | 3 | 4 | 5 | Total |
|---|---|---|---|---|---|---|---|
| Aug 1888 | 27 | 158 | 63 | 47 | 12 | 1 | 308 |
| Legacy (~1895, design note) | 27 | 122 | 55 | 55 | 40 | 9 | 308 |

The only tier 5 at the start is Archie Hunter (Villa). The legacy 5s: Goodall (Preston), Hunter and Hodgetts (Villa),
Forrest and Jack Southworth (Blackburn), Bassett (WBA), Holt (Everton), Cursham (Notts County), Lindley (Forest).
Nick Ross would be a 5 at Preston but starts at Everton as a 3.
