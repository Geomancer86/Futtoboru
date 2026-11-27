# Real results 1888-89 (E-00, WORKSTATION, 2026-11-27)

The 132 Football League matches of 1888-89 and the FA Cup proper rounds, with sources, for calibrating the match
engine and for `seasons/1888-89/results.json` once BOOK3PRO's data-model slice 2 lands. Club ids are the slugs in
`gamedata/core/clubs.json`.

- `results_1888-89.csv`: `date,home_id,away_id,home_goals,away_goals,attendance,source`, 132 rows.
- `facup_1888-89.csv`: the same plus `round` (R1, R1r = replay, R2r2 = second replay, R3, SF, SFr, F) and `venue`
  (only set for neutral grounds). Walkovers have no date and no goals.
- `check_results_1888-89.py`: recomputes the league table from the CSV and compares it with the published table
  (`python docs/data/check_results_1888-89.py`, exit 0 = match).

## Coverage

| | found | notes |
|---|---|---|
| League matches | **132 / 132** | date, home, away, score for every match |
| League attendances | **132 / 132** | press estimates, mostly rounded to the thousand; 38 differ between sources (below) |
| Recomputed table | **matches** the published table | every club's P W D L F A Pts, and the final order (2 points a win, goal average) |
| FA Cup proper | 38 rows: 16 R1 + 4 R1 replays (1 walkover), 8 R2 (1 walkover), 2 R2 replays, 4 R3, 2 SF + 1 replay, final | 21 attendances, from league clubs' season articles and the final's article |

## Method

1. Took each club's 1888-89 season article on Wikipedia (12 articles, one table each). Every match appears twice,
   once in the home club's article (**WH**) and once in the away club's (**WA**). These are written from Tony
   Brown, *The Football League Match by Match 1888-89 to 1892-93* (SoccerData 2010) and club histories.
2. Parsed three more full-season sources: the RSSSF cross-table (**RS**, scores only), 11v11 club fixture lists
   (**E11**, date and score) and Steve's Football Stats (**SFS**, date, score, ground, attendance).
3. Voted per match: score and date = majority of the five; attendance = value given by at least two of WH, WA,
   SFS, else the home club's article. The `source` column lists the sources that agree with the chosen date and
   score, then `att:` and the sources for the attendance.
4. Checked every disagreement by hand against a fifth, contemporary source where one existed: Liverpool Courier
   and other press reports transcribed on Blue Correspondent (**BC**), evertonresults.com (**ER**), wba.co.uk
   (**WBA**).
5. Recomputed the table (script above) and checked that no club plays twice on one date.

The sources are not fully independent: the Wikipedia results grid cites RSSSF, and 11v11 / SFS likely share the
same base data (the Association of Football Statisticians). The two cases where they all agree against the club
articles were settled by contemporary newspaper reports, not by the vote.

## Sources

| code | source |
|---|---|
| WH / WA | Wikipedia season articles, `https://en.wikipedia.org/wiki/1888%E2%80%9389_<Club>_F.C._season` for Accrington, Aston Villa, Blackburn Rovers, Bolton Wanderers, Burnley, Derby County, Everton, Notts County, Preston North End, Stoke, West Bromwich Albion, Wolverhampton Wanderers |
| RS | RSSSF, England 1888-89 cross-table and final table (Paul Felton, Barry Spencer, table by Stuart Jackson): https://www.rsssf.org/engpaul/FLA/1888-89.html |
| E11 | 11v11 (AFS) club fixture lists, e.g. https://www.11v11.com/teams/everton/tab/matches/season/1889/ |
| SFS | Steve's Football Stats, matchday tables: https://www.stevesfootballstats.uk/division_one_1888-1889.html |
| — | Published final table: https://en.wikipedia.org/wiki/1888%E2%80%9389_Football_League |
| BC | Blue Correspondent (contemporary Liverpool press transcriptions): https://bluecorrespondent.co.uk/1888-89/december1888.html and https://bluecorrespondent.co.uk/1888-89/february1889.html |
| ER | Everton results 1888-89: https://evertonresults.com/188889.htm |
| WBA | West Bromwich Albion, "On this day in 1888": https://www.wba.co.uk/news/2018/december/on-this-day-in-1888 |
| FAC | Wikipedia 1888-89 FA Cup: https://en.wikipedia.org/wiki/1888%E2%80%9389_FA_Cup |
| RSC | RSSSF FA Cup 1888-89 (Macario Reyes Padilla): https://www.rsssf.org/tablese/engcup1889.html |
| E11C | 11v11 FA Cup 1889 matches: https://www.11v11.com/competitions/fa-cup/1889/matches/ |
| FINAL | Wikipedia 1889 FA Cup final: https://en.wikipedia.org/wiki/1889_FA_Cup_final |

## Final table (recomputed from the CSV = published)

| pos | club | P | W | D | L | F | A | Pts | home W-D-L F-A |
|---|---|---|---|---|---|---|---|---|---|
| 1 | preston-north-end | 22 | 18 | 4 | 0 | 74 | 15 | 40 | 10-1-0 39-7 |
| 2 | aston-villa | 22 | 12 | 5 | 5 | 61 | 43 | 29 | 10-0-1 44-16 |
| 3 | wolverhampton-wanderers | 22 | 12 | 4 | 6 | 51 | 37 | 28 | 8-2-1 31-14 |
| 4 | blackburn-rovers | 22 | 10 | 6 | 6 | 66 | 45 | 26 | 7-4-0 44-22 |
| 5 | bolton-wanderers | 22 | 10 | 2 | 10 | 63 | 59 | 22 | 6-0-5 35-30 |
| 6 | west-bromwich-albion | 22 | 10 | 2 | 10 | 40 | 46 | 22 | 6-2-3 28-25 |
| 7 | accrington | 22 | 6 | 8 | 8 | 48 | 48 | 20 | 5-3-3 26-17 |
| 8 | everton | 22 | 9 | 2 | 11 | 35 | 47 | 20 | 8-0-3 23-14 |
| 9 | burnley | 22 | 7 | 3 | 12 | 42 | 62 | 17 | 6-3-2 21-19 |
| 10 | derby-county | 22 | 7 | 2 | 13 | 41 | 61 | 16 | 5-1-5 22-20 |
| 11 | notts-county | 22 | 5 | 2 | 15 | 40 | 73 | 12 | 4-2-5 25-32 |
| 12 | stoke | 22 | 4 | 4 | 14 | 26 | 51 | 12 | 3-4-4 15-18 |

Preston unbeaten champions (18-4-0). Bolton above WBA, Accrington above Everton and Notts County above Stoke on
goal average. Bottom four (Burnley, Derby, Notts County, Stoke) had to seek re-election; all were re-elected.
Home splits differ from RSSSF's for Wolves, WBA and Everton because of the two corrections below.

## Discrepancies and choices

**League scores and venues**

| match | conflict | chosen | why |
|---|---|---|---|
| WBA v Everton, 1 Dec 1888 | RS, E11, SFS: at Anfield, Everton 1-4 WBA. WH, WA: at Stoney Lane, WBA 4-1 | **WBA 4-1 Everton at West Bromwich** | Liverpool press report (BC) "This match was played at West Bromwich", WBA's own site, ER |
| Everton v WBA, 23 Feb 1889 | RS, E11, SFS: WBA 1-0 at Stoney Lane. Everton article: at Anfield, Everton 0-1. WBA article also says home (so it has both games at home) | **Everton 0-1 WBA at Anfield** | BC: "The return League fixture was played at Everton today... before 8,000 spectators"; ER |
| Wolves v Everton, 26 Jan 1889 | RS 4-0; WH, WA, E11, SFS, ER 5-0 | **5-0** | five sources; gives the published Wolves 51-37 / Everton 35-47 (RSSSF's table has 50-37 / 35-46) |
| Accrington v Notts County, 26 Jan 1889 | Notts County article lists it as a Notts home win 2-1 (so it has both meetings at home) | **Accrington 1-2 Notts County** | all other sources; score is the same, only the venue flag in that article is wrong |

No date disagreed in the league once these were settled. Source defects that are not disagreements: SFS repeats
the 6 Oct 1888 games under 13 Oct (so the six real 13 Oct games have no SFS confirmation) and has spelling slips
("Accrinton", "Browmich"); the WBA article gives Crabtree as WBA's scorer on 23 Feb, SFS gives Bayliss (not used).

**League attendances** (WH home article, WA away article, SFS). Contemporary figures are press estimates; where
two of three agree that value is used. The 600 / 500 values in SFS are probably dropped zeros.

| date | match | values | chosen |
|---|---|---|---|
| 1888-09-08 | everton v accrington | WA 8000, WH 12000 (ER also 12000), SFS 8000 | 8000 |
| 1888-09-08 | preston-north-end v burnley | WA 6000, WH 5000, SFS 5000 | 5000 |
| 1888-09-08 | stoke v west-bromwich-albion | WH 4524, WA 4500, SFS 4500 | 4500 |
| 1888-09-15 | aston-villa v stoke | WH 4000, WA 3000, SFS 4000 | 4000 |
| 1888-09-15 | derby-county v west-bromwich-albion | WH 3700, WA 3000, SFS 3000 | 3000 |
| 1888-09-15 | everton v notts-county | WH 6000, WA 7000, SFS 7000 | 7000 |
| 1888-09-22 | aston-villa v everton | WH 4000, WA 5000, SFS 4000 | 4000 |
| 1888-09-29 | stoke v accrington | WA 3000, WH 5000, SFS 3000 | 3000 |
| 1888-10-06 | accrington v wolverhampton-wanderers | WH 3000, WA 4000, SFS 4000 | 4000 |
| 1888-10-06 | burnley v bolton-wanderers | WA 4000, WH 6000, SFS 4000 | 4000 |
| 1888-10-06 | preston-north-end v stoke | WH 3000, WA 3500, SFS 3000 | 3000 |
| 1888-10-06 | west-bromwich-albion v derby-county | WA 5500, WH 2100, SFS 5500 | 5500 |
| 1888-10-13 | bolton-wanderers v stoke | WH 5000, WA 3000 | 5000 |
| 1888-10-13 | notts-county v everton | WA 4000, WH 3000 | 3000 |
| 1888-10-20 | derby-county v everton | WH 2000, WA 3000, SFS 2000 | 2000 |
| 1888-10-20 | stoke v burnley | WH 4500, SFS 3000 | 4500 |
| 1888-10-27 | aston-villa v accrington | WA 6000, WH 6000, SFS 600 | 6000 |
| 1888-10-27 | everton v derby-county | WA 5000, WH 3000, SFS 5000 | 5000 |
| 1888-10-27 | notts-county v burnley | WA 5000, WH 4000, SFS 4000 | 4000 |
| 1888-10-27 | preston-north-end v wolverhampton-wanderers | WH 6000, WA 5000, SFS 5000 | 5000 |
| 1888-11-03 | notts-county v preston-north-end | WH 4000, WA 7000, SFS 7000 | 7000 |
| 1888-11-05 | west-bromwich-albion v bolton-wanderers | WA 8000, WH 4000, SFS 4000 | 4000 |
| 1888-11-17 | bolton-wanderers v west-bromwich-albion | WH 3500, WA 3500, SFS 500 | 3500 |
| 1888-11-17 | stoke v wolverhampton-wanderers | WH 2500, WA 2000, SFS 2500 | 2500 |
| 1888-11-24 | bolton-wanderers v preston-north-end | WH 3500, WA 10000, SFS 10000 | 10000 |
| 1888-11-24 | notts-county v stoke | WH 2000, WA 2500, SFS 2000 | 2000 |
| 1888-12-01 | accrington v burnley | WH 3000, WA 5000, SFS 3000 | 3000 |
| 1888-12-01 | west-bromwich-albion v everton | WH 5000, WA 5700 (ER, E11 5700), SFS 5000 | 5000 |
| 1888-12-08 | burnley v stoke | WH 6000, WA 3000, SFS 3000 | 3000 |
| 1888-12-15 | burnley v preston-north-end | WH 10000, WA 8000, SFS 10000 | 10000 |
| 1888-12-22 | wolverhampton-wanderers v stoke | WA 2500, WH 3000, SFS 3000 | 3000 |
| 1888-12-29 | accrington v everton | WH 3000, WA 2000, SFS 3000 | 3000 |
| 1888-12-29 | west-bromwich-albion v stoke | WA 4900, WH 4900, SFS 4896 | 4900 |
| 1889-01-12 | everton v stoke | WH 7000, WA 7000, SFS 6000 | 7000 |
| 1889-01-19 | notts-county v wolverhampton-wanderers | WH 2000, WA 3000, SFS 3000 | 3000 |
| 1889-01-19 | stoke v bolton-wanderers | WA 6000, WH 4000, SFS 6000 | 6000 |
| 1889-01-26 | derby-county v stoke | WH 2500, WA 3000, SFS 2500 | 2500 |
| 1889-01-26 | wolverhampton-wanderers v everton | WA 4500, WH 3500 | 3500 |
| 1889-02-23 | everton v west-bromwich-albion | WH 2079, WA 8000, SFS 8000, BC 8000, ER 10000 | 8000 |
| 1889-03-30 | everton v blackburn-rovers | WA 7000, WH 4000, SFS 7000 | 7000 |

**FA Cup**

- R1 Halliwell v Crewe Alexandra 2-2: FAC lists Crewe first; RSC and E11C have Halliwell at home, and FAC's own
  footnote says Halliwell "ceded home advantage for the replay" (replay Crewe 1-5 Halliwell). Chose Halliwell home.
- R1 Aston Villa 3-2 "Witton": RSC says "Witton West End", E11C "Witton Albion", FAC links Witton Albion in the
  table but Witton F.C. (Blackburn) in the text. Per `REVIEW_1888-89_FINDINGS.md` it is Witton F.C. of Blackburn;
  the CSV uses the current id `witton-albion` until the planned `witton` id and alias exist.
- SF Wolves 1-1 Blackburn: the Blackburn article dates it 19 March; FAC, RSC, E11C, Wolves article: 16 March. Chose
  16 March. Both SF ties were at neutral grounds (Bramall Lane, Sheffield; Alexandra Recreation Ground, Crewe),
  so home/away order there and in the final is listing order only.
- Final attendance: 27,000 (FINAL article), 25,000 (Preston article), 22,250 (Wolves article). Chose the match
  article's 27,000; treat as uncertain.
- Chatham 1-10 WBA, 17,000: WBA article only, not confirmed elsewhere.

## Grounds (for `squads.json` / `grounds.json`)

Every league match was at the home club's usual ground (SFS and the club articles): Thorneyholme Road, Wellington
Road (Perry Barr), Leamington Road, Pikes Lane, Turf Moor, Racecourse Ground (Derby), Anfield, Trent Bridge,
Deepdale, Victoria Ground, Stoney Lane, Dudley Road. Two findings for the data:

- **Blackburn Rovers played at Leamington Road in 1888-89**, not Ewood Park (they moved in 1890); `grounds.json`
  only has `ewood-park` for them. Source: Blackburn and Burnley season articles, SFS.
- Notts County played two home games at the **Castle Cricket Ground** (5 Mar 1889 v Bolton, 16 Mar 1889 v Derby),
  the rest at Trent Bridge. SFS only; the Notts County article just says "H". `castle-ground` already exists.

## Calibration notes (132 league matches)

- **Goals:** 587, **4.45 per match** (home 2.67, away 1.77; home/away ratio 1.51). Variance of total goals 4.81,
  slightly over a Poisson with the same mean.
- **Results:** home win **59.1%** (78), draw **16.7%** (22), away win **24.2%** (32). Only two 0-0s.
- **Total goals per match:** 0: 2, 1: 6, 2: 17, 3: 29, 4: 18, 5: 19, 6: 14, 7: 14, 8: 9, 9: 1, 10: 3.
- **Home goals:** 0: 16, 1: 17, 2: 38, 3: 21, 4: 18, 5: 12, 6: 7, 7: 2, 9: 1. **Away goals:** 0: 25, 1: 41,
  2: 35, 3: 14, 4: 9, 5: 5, 6: 1, 7: 2.
- **Most common scores:** 2-1 (16), 2-2 (8), 3-0 (7), 4-1 (7), 1-1 (6), 2-0 (6), 5-2 / 0-2 / 6-2 (5 each).
- **Biggest wins:** Aston Villa 9-1 Notts County (29 Sep 1888); Preston 7-0 Stoke (6 Oct); Notts County 0-7
  Preston (3 Nov); Burnley 1-7 Blackburn (3 Nov); WBA 5-0 Derby, Preston 5-0 Derby, Wolves 5-0 Everton.
- **Highest scoring:** Villa 9-1 Notts County, Blackburn 5-5 Accrington (15 Sep), Bolton 7-3 Notts County
  (9 Mar 1889), all 10 goals.
- **Attendance:** mean 4,683, median 4,000, range 1,000 (WBA v Accrington) to 15,000 (Everton v Preston, 19 Jan
  1889). Mean by home club: Everton 7,909, Preston 6,273, Bolton 5,273, Blackburn 5,273, Villa 5,182, Burnley
  4,455, WBA 3,924, Notts County 3,909, Stoke 3,864, Wolves 3,727, Accrington 3,364, Derby 3,045.
- **Calendar:** 8 Sep 1888 to 20 Apr 1889, 36 match dates; 125 matches on Saturdays, 7 midweek (Mon 5 Nov, Mon
  12 Nov, Wed 26 Dec x2, Mon 4 Feb, Tue 5 Mar, Mon 15 Apr). Matches per month: Sep 23, Oct 23, Nov 24, Dec 27,
  Jan 20, then only 15 from February to April while the FA Cup ran. Clubs finished on different dates (Preston on
  9 Feb, Accrington and Stoke on 20 Apr).
