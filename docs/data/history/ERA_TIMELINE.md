# Era timeline 1863 to ~1939: everything that changes how football plays

_Lane H-03 (WORKSTATION), 2026-11-27. Research for D7, D19, D20, D21. Builds on `docs/design/MATCH_ENGINE_V2.md` §1
(the 1888-89 Laws, not repeated here). Physics numbers: `BALL_AND_KIT_PHYSICS.md`. Data shape:
`docs/design/ERA_EVENTS_PROPOSAL.md`._

How to read it: **Date** is when the change was decided or happened; most Laws changes were voted at the
International FA Board (IFAB) Annual General Meeting in **June** and applied from the **next season** (1 September).
**Who announces** is the authority that would send the in-game message (`IFAB`, `FA`, `LEAGUE` = Football League,
`PRESS` = news / makers / clubs). **Kind** maps to the proposal: `rule`, `equipment`, `ball`, `authority`.
**Conf.**: H = two or more agreeing sources or a primary text; M = one good secondary source; L = conflicting or
weak sources (see notes). Sources are the keys in the list at the end.

## 1. Dated table

### Before the League (background, already true on 8 September 1888)
| Date | Kind | Who | Change | Game effect | Conf. | Src |
|---|---|---|---|---|---|---|
| 1858 | equipment | PRESS | Sheffield FC prescribes red or blue caps to tell its two sides apart. Caps are for **identification**. | caps = team colour, not protection | M | NSZ-4 |
| c.1862 | ball | PRESS | Richard Lindon (Rugby) replaces the pig's bladder with an India-rubber bladder and a brass pump; pig bladders were blown up by mouth, could be diseased, and set an uneven shape. | rounder, more consistent ball | M | W-Lindon |
| 1863 | equipment | FA | FA laws ban projecting nails, iron plates and gutta-percha on boots. | boot hazard rules exist | M | NSZ-4, W-Kit |
| 1863 | ball | FA | First FA ball specification (details not verified). | - | M | W-Ball |
| 1866 | rule | FA | Offside: onside with **3 opponents** between player and goal line. Tape between posts. | 3-player offside (D7) | H | W-LotG, W-Off |
| Jan 1871 | rule | PRESS | Earliest recorded **heading** (Sheffield v Derby), "catching being disallowed, the players naturally headed". | heading exists from the start | M | SHF-Head |
| 1871 | rule | FA | Goalkeeper position: may handle "for the protection of his goal". | keeper | H | W-LotG |
| 1872 | ball | FA | Ball circumference **27-28 in** (68.6-71.1 cm), the size still used. (One table gives 1883.) | ball radius | M | W-Ball, NSZ-2 |
| 1872 | rule | FA | Corner kick; indirect free kick for handball. | set pieces | H | W-LotG |
| 1874 | equipment | PRESS | Sam Weller Widdowson (Nottingham Forest) straps cut-down cricket pads **outside** his stockings: the first shin guards, laughed at, then copied. | shin-pad option | H | W-Shin, W-Kit |
| 1874 | rule | FA | First umpires (one per side) in the laws; they also check boots. | officials | H | W-LotG, NSZ-5 |
| 1881 | rule | FA | Referee added to settle disputes between umpires; caution and sending-off appear. Shin guards mentioned: not to be made of metal. | officials; pads | H | W-LotG, NSZ-5, NSZ-4 |
| 1882-83 | rule | FA | Crossbar compulsory; two-handed throw from above the head; no offside from corners; keeper two steps. | (in V2 §1) | H | W-LotG |
| 20 Jul 1885 | authority | FA | **Professionalism legalised**; player registration follows. | wages exist | H | W-EFL, W-RT |
| 10 May 1886 | authority | FA | International **caps** awarded to England players (N. L. Jackson's proposal): the cap becomes an honour. | flavour | M | W-Cap |
| 1886 | authority | IFAB | International FA Board founded (FA, SFA, FAW, IFA); later Laws changes come from its June AGM. | announcer | H | W-LotG |
| 1887 | rule | IFAB | Keeper may not handle in the opponents' half. Bars only on soles of boots. | keeper own half | H | W-LotG, NSZ-4 |
| 1888 | equipment | IFAB | Boots: bars flat, at least 1.5 in long and 0.5 in wide; studs round, at least 0.5 in diameter, never conical or pointed; **neither may project more than 0.5 in**; fastenings flush with the leather. (SATRA dates this 1891; the 1890 Laws text already has it.) | stud grip cap | M | NSZ-4, WS-1890; SATRA (1891) |
| 1888 | rule | IFAB | Dropped ball introduced; referee may stop the game for spectator interference. | restart | H | W-LotG, NSZ-5 |
| 17 Apr 1888 | authority | LEAGUE | Football League founded (12 clubs); season starts 8 Sep 1888. | (in game) | H | W-EFL |

### The game era (events the player can live through)
| Date | Kind | Who | Change | Game effect | Conf. | Src |
|---|---|---|---|---|---|---|
| late Nov 1888 | authority | LEAGUE | **2 points for a win, 1 for a draw**, goal average splits ties (decided during the season). | table rules | H | W-1888 |
| Jun 1889 | ball | IFAB | Ball weight **13-15 oz (369-425 g) at the start of play**; the 1890 text applies it to **international matches**. | ball mass at kick-off | H | NSZ-2, WS-1890 |
| Jun 1889 | rule | IFAB | Referee may give a free kick **without appeal** for dangerous play, stop for darkness, keep time, caution; send off for repeated misconduct. | referee power grows | H | NSZ-5, W-LotG |
| May-Jun 1889 | authority | LEAGUE | Bottom four (Stoke, Burnley, Derby, Notts County) must retire and seek **re-election**: all re-elected. | re-election vote (Phase 5) | H | W-1888 |
| 1889 | equipment | PRESS | J. A. Brodie (Liverpool) patents **goal nets**. | news | H | Brodie |
| 1890 | rule | IFAB | No goal directly from a goal kick. | - | H | W-LotG |
| 1890 | authority | LEAGUE | Stoke not re-elected; Sunderland elected. League bans similar registered colours (later: second set of shirts). | colours clash rule | H | W-EFL, W-Kit |
| Jan 1891 | equipment | PRESS | First official use of goal nets (North v South, Nottingham). | news | H | Brodie |
| 2 Jun 1891 | rule | IFAB | **Penalty kick** for handball or foul within 12 yd of the goal line; **referee moves onto the field** with full power; **umpires become linesmen**; goal area, penalty line, centre spot and circle marked. | penalties on; officials model changes | H | W-LotG, W-Pen, NSZ-5 |
| 1891-92 | equipment | LEAGUE | Goal **nets** adopted League-wide; disputed "was it in?" goals mostly end. | disputed goals rarer | H | Brodie, V2 |
| 14 Sep 1891 | rule | PRESS | First Football League penalty (Wolves v Accrington). | news | H | W-Pen |
| 1891-92 | authority | LEAGUE | League grows to 14 clubs (Stoke back, Darwen elected). | league size | H | W-EFL |
| 1892 | authority | LEAGUE | **Second Division** formed by absorbing the 12-club Football Alliance; promotion/relegation by **test matches**. | 2 divisions | H | W-EFL |
| 1892 / 1894 | rule | IFAB / FA | Keeper may be charged only when playing the ball or obstructing (date conflict). | keeper charge risk drops | L | NSZ-12, Spart |
| 1893 | rule | IFAB | Referee's decision is final. | appeals end | H | NSZ-5 |
| 1893-94 | authority | FA / LEAGUE | Annual player **registration** with the FA; **retain and transfer** begins: a registered player cannot join another League club without his club's consent, even after his contract ends. | transfer rules | H | W-RT, Spart-T |
| Feb 1898 | authority | PRESS | Association Footballers' Union formed (about 250 members). | news | M | Spart-T |
| 1897 | rule | IFAB | Laws fix **11 players** and **90 minutes** (unless agreed); half-way line; max length 130 yd. | - | H | W-LotG |
| 1898 | rule | IFAB | Referee may add time lost; bars must be transverse; referee checks boots before the match. | stoppage time | H | NSZ-5, NSZ-4 |
| 30 Apr 1898 | authority | LEAGUE | Stoke v Burnley 0-0 "arranged" test match; **test matches scrapped**: automatic promotion/relegation of two clubs, both divisions to 18. | promotion rule | H | W-EFL |
| 1901 | rule | IFAB | Keeper may handle **for any purpose** (still own half). | keeper | H | W-LotG |
| 1901 | authority | LEAGUE | **Maximum wage £4 a week**; AFU dissolved. | wage cap | H | W-RT |
| 1902 | rule | IFAB | Modern goal area and penalty area (18 yd box), penalty spot. | pitch markings | H | W-LotG |
| 1903 | rule | IFAB | **Direct free kicks** for handball and foul play; **advantage** rule. | set pieces | H | W-LotG |
| 1904 | equipment | FA | FA drops the rule that knickerbockers must cover the knee; shorts get shorter. | kit mass / speed (small) | M | W-Kit |
| 1904 | equipment | IFAB | Gutta-percha clause deleted from the boot law (one source). | - | L | NSZ-4 |
| Feb 1905 | authority | PRESS | Alf Common: first **£1,000** transfer (Sunderland to Middlesbrough). | transfer market | M | Spart-T |
| 1905 | ball | IFAB | Ball "must not have anything on it that can injure a player". | lace hazard is a rules issue | M | NSZ-2 |
| 1905 | authority | LEAGUE | Both divisions to 20 clubs. | league size | H | W-EFL |
| 1907 | rule | IFAB | **No offside in own half**. | offside | H | W-LotG |
| Jan 1908 | authority | LEAGUE | **£350 transfer fee limit**; evaded (players sold in bundles), withdrawn after about a year. | transfer cap (temporary) | M | Spart-T |
| 1909 | equipment | IFAB | **Goalkeeper must wear a different colour**: scarlet or royal blue. | keeper visible | M | W-Kit |
| 1909 | ball | PRESS | Press reports a ball gaining **47 % mass over 90 min**. | wet-ball news | M | PH-25 [32] |
| 1910 | authority | LEAGUE | Maximum wage £5 (with bonuses); Southern League recognises retain and transfer. | wage cap | M | 1888L, W-RT |
| 1910 | equipment | IFAB | Studs need not be concealed but not conical; referee may check boots during the match. | - | M | NSZ-4 |
| 1912 | rule | IFAB | **Keeper handles only inside the penalty area**. | keeper | H | W-LotG |
| 1912 | equipment | IFAB | **Green** added for goalkeepers (soon almost universal). | keeper kit | M | W-Kit |
| 1912 | authority | PRESS | Kingaby v Aston Villa: court challenge to retain and transfer fails (year from secondary sources). | news | L | W-RT |
| 1913 | rule | IFAB | Opponents 10 yd from free kicks (not verified here; V2 flagged it). | set pieces | L | V2 |
| 1915-1919 | authority | LEAGUE | League suspended after 1914-15 for the First World War; resumes 1919 with 22-club divisions. | no league seasons | H | W-EFL |
| 1920 | rule | IFAB | No offside from a throw-in. | offside | H | W-LotG |
| 1920 | authority | LEAGUE | Third Division (Southern League clubs); maximum wage £9. | league size; wage | H / M | W-EFL, 1888L |
| 1921 | authority | LEAGUE | Third Division North added; away team changes colours in a clash. | league size | H | W-EFL, W-Kit |
| 1922 | authority | LEAGUE | Maximum wage cut to **£8** (season) and **£6** (summer). | wage cut | M | 1888L, MM |
| 1924 | rule | IFAB | Goal direct from a corner allowed. | set pieces | H | W-LotG |
| 1925 | rule | IFAB | **Offside: two opponents** instead of three. Goals jump. | offside | H | W-LotG, W-EFL |
| 1925 | equipment | PRESS | Dassler brothers make boots with 6-7 replaceable nailed studs. | - | M | SATRA |
| 1929 | rule | IFAB | Player who changed illegal boots needs the referee's permission to return. | - | M | NSZ-4 |
| 1931 | rule | IFAB | Keeper may take four steps carrying the ball. | keeper | H | W-LotG |
| 1931-32 | ball | PRESS | **Laceless, valve ball** ("Superball") patented in Bell Ville, Argentina (1931); AFA official ball 1932. | lace-less ball (not in England yet) | H | W-Ball |
| 1933 | equipment | FA | FA Cup final: Everton numbered 1-11, Manchester City 12-22. | numbers (one-off) | H | W-Kit |
| 1936 | equipment | PRESS | Continental boots weigh about a third of the rigid boots of a decade before. | boot mass (abroad) | M | W-Kit |
| 1937 | ball | IFAB | Ball weight **14-16 oz (397-454 g)**; the dry weight has not changed since. Boot stud specs deleted; combined stud and bar allowed. | ball mass | H | NSZ-2, PH-25, NSZ-4 |
| 1938 | rule | IFAB | Laws rewritten into 17 Laws (Rous); kit listed: jersey, shorts, stockings, boots, keeper colours; bars/studs of leather or rubber. | - | H | W-LotG, NSZ-4 |
| 1939 | equipment | LEAGUE | **Numbered shirts** compulsory in the Football League. | numbers | H | W-EFL |
| 1950s | ball | PRESS | English makers sell **laceless** and **coated** leather balls; FA 1953 tests: uncoated ball +19 % in 45 min on a wet day, coated +3 %. | wet gain drops | H | PH-25 |

**Count:** 76 dated rows (20 background, 56 in the playable era). The ball rows feed `BALL_AND_KIT_PHYSICS.md`.

## 2. Narrative (what a manager in 1888 would notice change)

**1888-89, the starting season.** Umpires with flags, one from each club, decide only on appeal; the referee
watches from the touchline and settles their disagreements (V2 §1). No penalties, no nets, 3-player offside in both
halves, keepers handle anywhere in their own half and can be bundled over the line. The ball is a rubber bladder in a
hand-stitched leather case, closed with a leather lace; its size has been fixed since 1872, but **no weight is laid
down yet**. Boots are heavy, ankle-high leather with hard toecaps; leather bars or studs are allowed up to half an
inch. Shin pads exist (since 1874) and by now are worn by many under or over the stockings. Caps, the identity
marker of the 1860s-70s, have given way to club jerseys; the cap is now an honour (1886).

**1889-1891, the referee takes over.** The ball gets a weight (13-15 oz, 1889). The referee gains power to act
without appeal (1889). Brodie's nets appear (1889 patent, January 1891 first use). Then the big June 1891 meeting:
the penalty kick, the referee on the field, linesmen instead of partisan umpires, pitch markings. This is the
single largest rules event of the game's first decade and deserves a mandatory inbox message.

**1892-1898, a bigger League.** Second Division (1892), test matches, re-election votes; retain and transfer
(1893) ties players to clubs; the 1898 Stoke-Burnley draw kills test matches and brings automatic promotion and
relegation. Keepers gain protection from charging (1892 or 1894).

**1899-1914, money and the modern box.** Keepers may handle for any purpose (1901), the maximum wage (£4, 1901)
caps what clubs can pay; the 18-yard box and penalty spot (1902); direct free kicks and advantage (1903); knees may
show (1904); the £1,000 transfer (1905); no offside in your own half (1907); keepers in their own colour (1909,
green from 1912) and handling only in the box (1912). Wet balls stay a talking point (1909: 47 %).

**1915-1939.** War suspends the League (1915-1919); three divisions by 1921; wage cut (1922); the 1925 offside
change (2 opponents) is the second giant rules event; laceless valve balls in Argentina (1931-32) while England
keeps laced leather into the 1950s; ball weight raised to 14-16 oz (1937); numbers compulsory (1939).

## 3. Myths and claims checked

| Claim | Verdict | Why |
|---|---|---|
| "Players wore caps to protect their heads when heading the laced ball." | **Not supported (likely myth).** | Caps were prescribed for team identification in 1858 (Sheffield), before heading is recorded (1871); 1860s-70s players were told apart by cap colour in reports and programmes (NSZ-4, W-Kit, W-Cap). No source found linking caps to heading protection. D20's "caps worn" for lace cuts should be optional flavour for the 1860s-70s, not a protection mechanic in 1888. |
| "The lace cut players' foreheads on headers." | **Plausible, anecdotal, not quantified.** | Club-history oral accounts say heading onto the laces "added to the problem" (LFCH); IFAB 1905 says the ball must have nothing on it that can injure (NSZ-2). No injury rate found. Model as a rare cut/graze event, more likely with a wet ball. |
| "A wet leather ball doubled (or tripled) in weight." | **False.** | Lab saturation of a laced, uncoated leather ball: **+55.8 %**; FA 1953 match test: +19 % in 45 min; 1909 report: +47 % in 90 min (PH-25). |
| "Old balls were heavier." | **Only when wet.** | Dry weight 13-15 oz (1889) is lighter than today's 14-16 oz (1937 onward, 410-450 g now). |
| "Early boots doubled in weight when wet." | **Unverified, repeated.** | SATRA and others repeat it without a measurement; ~500 g dry per boot (W-Boot) is the only figure. Low confidence. |
| "Penalties / nets / linesmen in 1888-89." | **False** (V2). | All three from 1891. |
| "Shin pads were compulsory early." | **False.** | Optional until 1990 (NSZ-4, press column). |

## 4. Open questions
1. Exact 1872 vs 1883 date for the 27-28 in circumference (the 1890 Laws text has it; both dates precede 1888).
2. Keeper-charge protection 1892 (IFAB) vs 1894 (FA).
3. Whether the 1889 13-15 oz weight applied to League matches or only internationals (the 1890 wording says
   "International matches"). Proposal: treat it as practice for all matches, flag confidence M.
4. 1910 wage (£5 with bonuses) and the Kingaby year (1912) rest on secondary sources.
5. When English League balls went laceless: the 1950s per PH-25; no exact first League match found.
6. Free-kick distance 6 yd vs 10 yd (1913) unverified.

## Sources
- [W-LotG] Laws of the Game, history: https://en.wikipedia.org/wiki/Laws_of_the_Game_(association_football)
- [W-Off] Offside history: https://en.wikipedia.org/wiki/Offside_(association_football)
- [W-Pen] Penalty kick: https://en.wikipedia.org/wiki/Penalty_kick_(association_football)
- [W-Ball] Ball (association football): https://en.wikipedia.org/wiki/Ball_(association_football)
- [W-Lindon] Richard Lindon: https://en.wikipedia.org/wiki/Richard_Lindon
- [W-Kit] Kit (association football): https://en.wikipedia.org/wiki/Kit_(association_football)
- [W-Boot] Football boot: https://en.wikipedia.org/wiki/Football_boot
- [W-Shin] Shin guard: https://en.wikipedia.org/wiki/Shin_guard
- [W-Cap] Cap (sport): https://en.wikipedia.org/wiki/Cap_(sport)
- [W-EFL] English Football League, history: https://en.wikipedia.org/wiki/English_Football_League
- [W-1888] 1888-89 Football League: https://en.wikipedia.org/wiki/1888%E2%80%9389_Football_League
- [W-RT] Retain and transfer system: https://en.wikipedia.org/wiki/Retain_and_transfer_system
- [WS-1890] Laws of the Game (1890), IFAB, primary text on Wikisource: https://en.wikisource.org/wiki/Laws_of_the_Game_(1890)
- [NSZ-2] P. Tabarelli, Law 2 history: https://nachspielzeiten.de/law-2-the-ball-en/
- [NSZ-4] P. Tabarelli, Law 4 history (equipment): https://nachspielzeiten.de/law-4-the-players-equipment/
- [NSZ-5] P. Tabarelli, Law 5 history (referee, umpires): https://nachspielzeiten.de/law-5-the-referee-en/
- [NSZ-12] P. Tabarelli, Law 12 history: https://nachspielzeiten.de/law-12-fouls-and-misconduct-en
- [Spart] Spartacus, goalkeeper: https://spartacus-educational.com/Fgoalkeeper.htm
- [Spart-T] Spartacus, transfer system: https://spartacus-educational.com/Ftransfer.htm
- [1888L] A short history of wages in English football: https://the1888letter.com/a-short-history-of-wages-in-english-football/
- [MM] Mark Metcalf, 1947 wages article (1922 cut): https://markmetcalfwfb.blogspot.com/2023/03/1947-when-football-stars-then-being.html
- [Brodie] Goal nets: https://en.wikipedia.org/wiki/John_Alexander_Brodie and https://markmetcalfwfb.blogspot.com/2023/01/goal-nets-were-first-officially-used-on.html
- [SHF-Head] Sheffield Home of Football, heading the ball (S. Wood): https://sheffieldhomeoffootball.org/museum/heading-the-ball
- [SATRA] SATRA bulletin, football boots: https://satra.com/bulletin/article.php?id=2311
- [PH-25] Phillips, Harland, Mitchell, Lepper (2025), "Investigating static water uptake characteristics and mechanisms
  for leather and synthetic association footballs", Sports Engineering 28:40, open access:
  https://doi.org/10.1007/s12283-025-00521-8 (cites FA News 1953 and Sussex Express 1909 [32]).
- [LFCH] LFC History, leather-ball era and heading: https://lfchistory.net/articles/4667
- [V2] `docs/design/MATCH_ENGINE_V2.md` §1 and its sources.
