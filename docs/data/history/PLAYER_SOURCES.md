# Era player sources: assessment and pipeline (R-10, BOOK3PRO, 2026-11-28)

What D28 Q2/Q15 need: real people of 1880-1900 (England, Scotland, Wales, Ireland) for squads and candidate pools, with
a source per record. Research notes, not game data. **F** = we fetched the page, **S** = search snippet only, **X** =
the fetch failed (reason given). Confidence: **H** = the site's own terms/robots text read, **M** = one reliable page,
**L** = snippet or inference. Not legal advice: licence readings are ours. `[n]` = sources at the end (also in
[`SOURCES.md`](SOURCES.md)).

## 1. Rules that apply to every source
- **Facts are not copyright, collections can be.** A name, a birth date or a club spell is a fact. But the UK database
  right protects a database built with "substantial investment in obtaining, verifying or presenting" its contents, and
  is infringed by extracting "all or a substantial part", **including repeated extraction of insubstantial parts**
  (15 years, renewed by substantial updates) [1][2] (H). So: single checked facts from anywhere are fine; a systematic
  harvest of one compiled database (ENFA, Englandstats, a club database) is not, unless its terms allow it.
- **Text is copyright.** Bios are written in our own words (as `PLAYERS_1888-89.md` already does); no copied prose.
- **Robots and terms.** We do not fetch automatically where `robots.txt` or terms forbid it, and we do not bulk-copy
  where terms say "personal use" or "manual only".

## 2. Sources
| Source | Gives | Coverage | Access / robots | Licence / reuse | Reliability | Use |
|---|---|---|---|---|---|---|
| **Wikidata** [3][4][5] | full birth and death dates and places, position (coarse: "forward"), occupations, club spells with start/end years and apps/goals qualifiers, caps; IDs of other databases (worldfootball, Transfermarkt, England Football Online, Englandstats, WikiTree) | F: SPARQL found **1,140** footballers born 1855-1875 with UK citizenship, **805** with an apps qualifier (1,730 spells); undercounts men tagged only "England" | free, SPARQL endpoint and JSON per item (F) | **CC0** ("released into the public domain") (H) | mostly "imported from English Wikipedia"; no primary sources; spells, not seasons | **bulk base** for identity and birth |
| **English Wikipedia** [6][7][8] | season articles (line-ups, apps per season), player bios (birthplace, positions, clubs), "List of X players" (position, years, apps; cites Joyce and club who's-whos) | best for the 12 League clubs of 1888-89 and later League clubs | free (F) | text CC BY-SA 4.0 with attribution [6]; we take facts, write our own words (H) | good where cited (Joyce, club books); varies | **main per-season source** (already used for 1888-89) |
| **ENFA** (English National Football Archive) [9] | match-by-match League line-ups and scorers, per-player records | League 1888-89 to 2023-24: 47,923 players, 241,661 matches | paid, one-month subscription (price not shown); robots has no Disallow (F) | "Database copyright Football DataCo Limited"; page meta "Soccerdata Limited" (H) | compiled by club statisticians (Joyce/SoccerData lineage, L) | manual checks only, if subscribed; **no bulk extraction** |
| **RSSSF** [10][11] | results, international line-ups/caps, scorer lists | England, Scotland, Ireland, Wales internationals from 1872 | free; no robots.txt (404) | "free to copy this document in whole or part provided that proper acknowledgement is given to the RSSSF" (H) | volunteer statisticians, named authors, updated 2026 | **caps and fixtures**, with credit |
| **Everton Results** [12] | every Everton match, apps, goals, dates of birth | Everton 1878- | free (F) | free to copy "provided that the use is not for commercial purposes ... and that proper acknowledgement is given" (H) | club statistician | Everton only; see choice 2 |
| **LFChistory.net** [13] | every matchday squad from 1892-93, position, apps, debut | Liverpool 1892- | free; robots only blocks /random pages (F) | "All rights reserved" (H) | good | single checks only |
| **avdb.co.uk** (Aston Villa) [14] | "every game, every player" from 1879-80, birthplaces, caps | Villa | free (F) | terms not found (L) | club statistician | single checks only |
| **playupliverpool.com** [15] | Liverpool/Everton A-Z bios (birth, position), press transcriptions | Merseyside | free (F) | no terms shown (L) | cites papers | single checks; quotes need a link |
| **bluecorrespondent.co.uk** (already used, R-01) | 1888-1891 Everton press transcriptions | Everton, opponents | free | not checked here | period text | trials, signings, line-ups |
| **londonhearts.com** [16] | Scotland caps per club, first/last game | Scotland 1872- | free (F) | "© www.londonhearts.com" only (H) | careful amateur | single checks |
| **Scottish historians** [17][18] | Scotch Professors articles; Mitchell's *The men who made Scotland* (615 caps 1872-1939, a paid book) | Scotland | free articles (F) | "All rights reserved"; "© Andy Mitchell" (H) | good, prose | leads, not data |
| **Welsh Football Data Archive** [19] | Welsh Cup results 1876-, some tables; player data "difficult to locate" | Wales | free (F) | "© 2026 Welsh Football Data Archive", no licence (H) | good, thin on players | fixtures only |
| **NI Footballing Greats** [20] | every Ireland cap since 1882: number, name, year of first cap | Ireland | free blog (F) | no notice, no sources (L) | unknown | leads for Irish men |
| **Englandstats.com** [21] | England caps and goals from 1872 | England caps | robots **disallows ClaudeBot, anthropic-ai, GPTBot, CCBot** site-wide (F) | "All Rights Reserved" (H) | good | **do not fetch**; humans may check |
| **worldfootball.net** [22] | early player pages exist (S) | wide | robots **disallows Claude bots** site-wide; "ai-train=no, use=reference"; HTTP 402 (F) | not read | unknown | **do not fetch** |
| **national-football-teams.com** [23] | per-year national squads, some club births (used once in R-01) | wide | robots disallows ClaudeBot, GPTBot, CCBot; leaves user-triggered fetchers (F) | not read | middling | humans only |
| **11v11 (AFS)** [24] | player pages incl. early Wales caps (S) | wide | 403 to us; robots blocks only forum paths (F) | not read | AFS statisticians | humans only |
| **England Football Online** | England players (Wikidata P6331 links) | England | X: connection reset | not read | good reputation (L) | humans only |
| **Transfermarkt** | stub pages for some 1880s men (via Wikidata) | | X: our fetch tool refuses it | not read | weak for the era (L) | skip |
| **British Newspaper Archive** | full period press: line-ups, "not retained" lists, trials, occupations | UK | paid; X: 403 | not read | primary | **see choice 1** |
| **Welsh Newspapers Online** (National Library of Wales) | ~120 Welsh titles to about 1910-1919 (S) | Wales | free; X: 403 | S: copyright page cites reuse under the PSI Regulations 2015; **no CC0 seen** (L) | primary | manual reading, cite page URLs |
| **Gibson & Pickford, *Association Football and the Men Who Made It*** (1905-06, 4 vols) | period bios of players, clubs, officials | 1880s-1905 | S: National Library of Scotland PDFs exist | public domain not confirmed (UK: author's life + 70; death dates not checked) (L) | period, partisan | facts only, after a PD check |
| **Joyce, *Football League Players' Records 1888 to 1939*** (SoccerData, ISBN 978-1-905891-61-0) | birth/death, League apps/goals per club (L: as usually described) | all League players 1888-1939 | paid book | copyright book; facts may be cited one by one | the standard reference, cited by Wikipedia (M) | cross-check via Wikipedia's citations |
| **FreeBMD** [25] | GRO birth/marriage/death index 1837- (quarter, district) | England and Wales | free; robots blocks /cgi/ (F) | "personal research purposes only"; "Access ... only permitted manually"; "Significant portions ... must not be reproduced" (H) | transcribed index | **manual spot checks only** |
| **FreeCEN** [26] | census transcriptions 1841-1911 (occupation, birthplace, household) | E, W, S, partial ("only counties with data") | free (F) | © Free UK Genealogy; terms not read (L) | patchy coverage | manual checks for origin and occupation |
| **FamilySearch** [27] | census, parish records | world | free, login | forbids bulk download, scraping and "any non-personal or commercial use" (H) | good | **manual only**, not cited as a data source |
| ScotlandsPeople, Ancestry, Findmypast | statutory records, census, newspapers | | paid | personal-use terms (not checked) | primary | not planned |

## 3. Which source for which field
| Field (people.json / squads.json) | First | Check against | Never |
|---|---|---|---|
| name, birth date, birth country (`born`, `country`) | Wikidata (CC0) | Wikipedia bio; a single FreeBMD lookup by a human when two sources disagree | bulk FreeBMD/FamilySearch |
| birthplace (town; for R-11 and regions) | Wikidata P19 | Wikipedia; FreeCEN by hand | |
| club per season, apps (`squads.json`) | Wikipedia season article (as 1888-89) | Wikidata spells; club list article | ENFA bulk |
| position (`positions`, E-01 codes) | Wikipedia season article / bio (existing `wp-*` codes) | period press | |
| caps (tier evidence) | RSSSF (credit), Wikipedia | NIFG (Ireland), londonhearts (Scotland) | Englandstats fetch |
| occupation, origin club (R-03 pools, Q12 backstories) | Wikipedia bio; period press via a reader | FreeCEN by hand | |
| men not in a League squad (candidate pools: Scots, Welsh, Irish, juniors) | Wikidata (spells at Scottish/Welsh clubs), Wikipedia club and cap lists | RSSSF caps; NIFG | |

## 4. Pipeline
1. **Query (tool, `tools/data/`):** a script runs a fixed SPARQL query on Wikidata (CC0) per season and club (P54 spell
   covering the season) and writes a **candidate file outside `gamedata/`** (e.g. `docs/data/candidates/<season>.csv`):
   QID, name, birth date and place, death date, position, spells, apps. The query and its date are kept with the file.
2. **Match:** against `people.json` by name + birth year + club (Wikidata QID kept in the person's `sources` as its
   URL). New men get a slug id `first-last-birthyear` (DATA_MODEL: never changed later); a number from
   `python tools/data/assign_ids.py`.
3. **Check:** each new person needs a second source for birth date and club (a Wikipedia season article or bio);
   disagreements go to the review list (`docs/data/REVIEW_<season>.md`), `confidence: unverified` until a human checks
   (`verified` only after that). No source = not added.
4. **Write:** `people.json` (identity), `seasons/<id>/squads.json` (club, position with its source code), later a
   candidate-pool file for R-03 (person id, region, origin club, source). `sources` = the Wikidata item URL plus the
   page that supports the season fact. Run `python tools/data/validate.py` and `python -m pytest tools/data`.
5. **Order:** (a) the 12 League clubs of 1888-89 (fill the 4 League placeholders and check births); (b) the same clubs
   1889-90 and 1890-91 (the next seasons' squads, and the men who join them, are the first candidate pools); (c) the
   1889-90 Football Alliance and 1892 Second Division clubs; (d) Scottish clubs that supplied the League (Renton, Vale
   of Leven, Dumbarton, Hibernian, Hearts, Queen's Park, Kilmarnock) via Wikidata spells and Scotland caps; (e) Welsh
   (Wrexham, Druids, Chirk) and Irish (Linfield, Distillery, Cliftonville) clubs via caps lists; (f) FA Cup-only clubs.
6. **Generated men** fill the rest (R-03), as decided (D28 Q2).

## 5. Producer choices
1. **British Newspaper Archive subscription** (paid; the only full source for "not retained" lists, trial reports,
   occupations and line-ups of non-League clubs). (a) no: Wikidata + Wikipedia + free sites; (b) a short
   subscription for a human researcher on a focused list (the 1888-89 gaps, Scottish imports). **Recommend (a)** now,
   (b) when R-03 pools show the gaps; agents cannot use it (403, and its terms are personal use, unverified).
2. **Non-commercial-only sources** (Everton Results: free to copy for non-commercial use with credit). The repo is
   public; if the game is ever sold, data taken from it would breach those terms. (a) use them only to check single
   facts that another source also gives; (b) copy from them with credit. **Recommend (a).**
3. **ENFA subscription** (match line-ups 1888-, Football DataCo copyright): (a) no; (b) one month for a human to
   check disputed facts by hand. **Recommend (a)**: the facts we need are in free sources; (b) only if the review list
   grows past what Wikipedia can settle.

## Sources
1. https://en.wikipedia.org/wiki/Database_right : database right, substantial part, repeated extraction, 15 years
2. https://www.gov.uk/guidance/sui-generis-database-rights : UK guidance, substantial investment
3. https://www.wikidata.org/wiki/Wikidata:Licensing : CC0 for structured data
4. https://www.wikidata.org/wiki/Special:EntityData/Q1028592.json : John Goodall item (dates, spells with apps)
5. https://www.wikidata.org/wiki/Special:EntityData/Q5494996.json : Fred Dewhurst item
6. https://en.wikipedia.org/wiki/Wikipedia:Copyrights : CC BY-SA terms
7. https://en.wikipedia.org/wiki/List_of_Preston_North_End_F.C._players : club list format
8. https://en.wikipedia.org/wiki/List_of_Doncaster_Rovers_F.C._players_(1879%E2%80%931918) : cites Joyce
9. https://www.enfa.co.uk/ : ENFA coverage, subscription, Football DataCo copyright
10. https://www.rsssf.org/ : RSSSF reuse notice
11. https://www.rsssf.org/tablese/eng-intres.html : England internationals
12. https://www.evertonresults.com/ : Everton Results, non-commercial reuse
13. https://www.lfchistory.net/ : LFChistory, all rights reserved
14. https://avdb.co.uk/ : Aston Villa database
15. https://playupliverpool.com/ : Merseyside bios and transcriptions
16. https://londonhearts.com/Scotland/clubs/playersbyclub.html : Scotland caps by club
17. https://www.thescotsfootballhistoriansgroup.org/scotch-professors : Scots historians, all rights reserved
18. https://scottishsporthistory.com/ : Andy Mitchell
19. https://www.welshsoccerarchive.co.uk/ : Welsh Football Data Archive
20. https://nifootball.blogspot.com/2023/11/northern-ireland-players-roll-of-honour.html : Ireland caps roll
21. https://www.englandstats.com/ : England caps; robots bans AI crawlers
22. https://www.worldfootball.net/robots.txt : bans Claude and other AI crawlers
23. https://www.national-football-teams.com/robots.txt : bans AI crawlers
24. https://www.11v11.com/robots.txt : forum paths only
25. https://www.freebmd.org.uk/terms.html : personal research, manual access only
26. https://www.freecen.org.uk/ : census transcriptions 1841-1911, coverage gaps
27. https://www.familysearch.org/en/legal/terms : no scraping, no non-personal use
