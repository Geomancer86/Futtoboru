# Data model v2 (P2-4)

Producer decisions (2026-11-27, `docs/ROADMAP.md`): **D11** stable string ids, **D14** all data in JSON like
DarkBlade, **D15** one people table plus season squads, v0.4 saves are not migrated. The patterns come from
DarkBlade's book data (`assets/books`, `BookValidator`, `EntityIds`, `SaveMigrations`).

## Layout
```
futtoboru-core/src/main/resources/gamedata/
  index.txt                  packs in load order (a folder inside a jar cannot be listed)
  core/                      the base pack; a mod is another folder with its own pack.json
    pack.json                id, name, version, dataVersion, seasons, files (every file, explicitly)
    continents.json countries.json professions.json occupations.json names.json
    competitions.json        football-league, fa-cup (type LEAGUE / CUP); optional re-election rule (G-08): reElectionPlaces,
                             reElectionFrom/Until (season start years), annualMeetingAfterDays, annualMeetingConfidence
    clubs.json               identity only: name, fullName, founded, country, sources
    grounds.json             stadiums: capacity, value, rent, description, sources
    people.json              every real person once: firstName, lastName, born, country, confidence, sources
    aliases.json             old id -> new id, per kind, kept forever
    ids.json                 data id -> runtime number, per kind, append-only (P2-5)
    seasons/1888-89/
      season.json            start, countries, editions (league + cup, with participants), calendar (P2-14)
      squads.json            per club this season: ground, balance, players (person ids), positions, dribbling (E-01)
      nationality_modifiers.json  region_modifiers.json
      results.json           real results of the season: reference data only (D17), see below
```
What changes from season to season (ground, balance, squad) lives in the season folder; what is permanent
(a club, a person) lives once at pack level. A player who moves or plays several seasons is one person.

## Identity
- **Data ids** are lower-case slugs, unique per kind, compared ignoring case: `preston-north-end`, `deepdale`,
  `fa-cup-1888-89`. People: `first-last-birthyear` (`fred-dewhurst-1863`), with `-2` when taken.
  Mods use `author.slug` so they can't clash with the core pack.
- **An id never changes**, even if the name or birth year inside the record is corrected later (the id is a
  label, not data). If a rename is unavoidable: add `old -> new` to `aliases.json` and a row to the rename log
  below, in the same commit.
- **Runtime ids** (slice 2) are `Long`s handed out by one `IdAllocator` per game, saved with the game, never
  reused. Data records carry their `dataId`; generated people, contracts, matches, messages and jobs get only a
  runtime id. No more `currentTimeMillis`, `null` or negative ids.
- **Saves** (slice 3) store data ids for data and runtime ids for game objects, never copies of records, plus
  a `saveVersion`. The `legacyId` / `legacyIds` fields used for the switch are gone (slice 2).

## Runtime numbers: `ids.json` (P2-5)
Each pack has an append-only registry `ids.json`: `range` (the pack's block of numbers, core 1-99 999, a mod the
next free block of 100 000), `next` (the next free number, never lowered), `ids` (kind -> data id -> number) and
`retired` (kind -> ids removed for good). The game uses the number as the record's runtime id, so a save that holds
runtime ids keeps pointing at the same records when the data is corrected.
- **Adding a record:** run `python tools/data/assign_ids.py` (gives it the next free number, touches nothing else).
- **Removing a record:** leave its entry and add its id to `retired`; the number is never handed out again.
- **Renaming:** only the alias (above); the new id inherits the old id's number.
- `validate.py` checks it: every record has a number, numbers unique across kinds and packs, inside the range and
  below `next`; an entry without a record must be retired or an old (aliased) id. `assign_ids.py --check` fails when
  a record has no number. Numbers 900 000-999 999 are for objects built at load (copies, random contracts), 1 000 000+
  for play.

## Quality: confidence and sources
Every person has `confidence`: `unverified` (typed by hand from the cited pages, may have errors; the default),
`placeholder` (filler, e.g. Burnley's 21 players with a made-up 1865 birth year) or `verified` (checked against a
source). Records keep their `sources` (the URLs from the old file headers). The open questions are in
[data/REVIEW_1888-89.md](data/REVIEW_1888-89.md); the biggest one: several 11-player cup rosters are copies of
each other (Sheffield Wednesday / Heeley, Sunderland Albion / Notts Rangers / Walsall, ...) and must be rebuilt.

## Validation (run on every data change)
```
python tools/data/validate.py          # errors block, warnings feed the review
python -m pytest tools/data            # validator tests
```
Errors: invalid JSON, an **unknown field** (catches typos), a missing field, a bad id or date, a duplicate id or
legacy id, a reference that doesn't resolve (after aliases), an alias that points nowhere or shadows a live id,
a file present but not listed in `pack.json` (it would never load) or listed but missing.
Warnings: a person in several squads, a squad under 11, no first name, an age outside 15-45, placeholders.

## Slices
1. **JSON pack, converter, validator, review list** (this). `tools/data/convert_legacy.py` writes the pack from
   `mods/*.txt`; the game still reads the `.txt` files, so nothing changes in play.
2. **Java loader**: `GameData` reads the packs (libGDX `Json`) with the same strict checks, `IdAllocator`,
   `dataId` on the models, editions from `season.json` instead of the lists hard-coded in `ScriptsLoader`, the
   hard-coded `mods/seasons/18/` paths gone, files read as UTF-8. Then the legacy loaders, `mods/**/*.txt`
   (except language bundles) and `convert_legacy.py` are deleted and the JSON is the only copy.
3. **Saves v1** (with P2-5): ids instead of copies, `saveVersion`, numbered slots, folder fixes, a save -> load
   round-trip test and a fixture save kept for future migrations.
4. **Data review** (WORKSTATION lane): work through the review list, rebuild the copied cup rosters from
   sources, mark checked people `verified`.

### How slice 2 is built (notes where the code differs from the plan above)
- `system/data/GameData` reads the packs from the classpath (`gamedata/`) with the rules of `validate.py`; a
  broken record is skipped and reported in a `DataReport` (logged at startup), never a crash. An unresolved
  entry of a *list* of references (a squad player, an edition participant) is dropped from the list instead of
  skipping the whole squad or season. "Present but not listed" is checked only when the folder can be listed
  (not inside the jar); `validate.py` stays the full check.
- Runtime ids: data records get their `ids.json` number (slice 3; the first registry kept the load-order ids of
  slice 2); objects built at load from the data (players' random contracts, the copies below, the IFAB,
  nationality modifiers) get 900 000+, so every data-side id is below 1 000 000. The game's `IdAllocator` lives in `SaveGame` and starts at 1 000 000.
  A `Player` keeps the id of its `Person` (it is the person's football role, not a second object).
- A person listed in several squads of a season (the copied rosters) still plays for each: the first squad gets
  the canonical `Person`, later squads a runtime copy with the same `dataId`; lookups by data id return the
  canonical one. Slice 4 fixes the data.
- League founders and cup entrants come from the editions in `season.json`; code compares professions by data id
  (`Profession.MANAGER`...), never by number.

### How slice 3 (saves v1, P2-5) is built
- `system/save/SaveCodec`: a header (`saveVersion`, game and data versions, packs, `savedAt`, a `summary` for the
  load list), the `game` fields, then one table per kind of game object (authorities, competitions, editions,
  leagues, clubs, persons, players, matches), each object once, sorted by runtime id; anywhere else a game object is
  its runtime id. Countries, continents, professions, occupations and a ground's facts are their data id, re-linked
  on load to the database's objects. A reference that doesn't resolve is reported and left out, never a crash.
- Loading rebuilds the database objects from the data, reads the save, then binds the lookups
  (`DatabaseLoader.getClubById` and the rest) to the game's objects: one object per club, person, competition.
- `SaveMigrations` holds one step per version (1 -> 2: E-01 position + dribbling); a file without `saveVersion` (v0.4) is
  listed as "older version" and not loaded. The first real v1 save is kept in `src/test/resources/saves/`.

## Real results: `seasons/<id>/results.json` (E-00b)
A list of matches, one record each: `edition` (an edition id of that season), `round` (cups: R1, R1r = replay,
R2r2, SF, F...), `date`, `home`, `away`, `homeGoals`, `awayGoals`, `attendance`, `venue` (neutral grounds only),
`source` (the source codes of `docs/data/RESULTS_<season>.md`), `notes`. A walkover has no date and no goals and sets
`walkover` to the club that went through. No ids, so no `ids.json` numbers. Written from the research CSVs by
`python tools/data/results_csv_to_json.py` (`--check` in the tests); `docs/data/check_results_1888-89.py` recomputes
the table from it. **Decision D17: reference data only.** Read by the history screens (`history/`, seasons before the
game's start season) and by calibration tests; never to schedule or decide a match (`ReferenceDataGuardTest`).

## Positions and dribbling (E-01)
Two optional objects on a **squad entry** (`seasons/<id>/squads.json`), keyed by a person id of that squad's `players`:
```
"positions": { "john-goodall-1863": { "position": "centre-forward", "source": "players-1888-89" }, ... },
"dribbling": { "jack-southworth-1866": { "value": 18, "source": "<doc / URL that says so>" } }
```
- **Why the squad entry, not the person:** a position is a fact of one season (D15: what changes per season lives in
  the season folder). Players changed lines over a career (Drummond "utility man", Weir "centre-half/forward",
  Hendry a centre-forward at WBA and a defender at Stoke in the same season), and a person in two squads can play
  different roles in each. The person record stays identity only. Dribbling is a skill as of that season too.
- **`position`**, the slots of the 2-3-5 (Java `data/Position`, with a `Line` GOAL/BACK/HALF/FORWARD):
  `goalkeeper`, `full-back`, `half-back`, `right-wing`, `inside-right`, `centre-forward`, `inside-left`, `left-wing`.
  Backs and halves carry no side (the sources rarely give one); the five forward slots do.
- **`source`** of a position: `players-1888-89` = the slot is named in `docs/data/history/PLAYERS_1888-89.md` (its
  bios cite their own pages); `players-1888-89-line` = only the line is named there ("forward", "winger", "inside
  forward", "defender"), the slot inside it was assigned to complete that squad's 2-3-5; `placeholder` = not in the
  doc, assigned in squad order to the slot the squad is shortest of (so every squad can field a 2-3-5). H-04 added
  `wp-season-1888-89` / `wp-season-1888-89-line` (the club's 1888-89 Wikipedia season article), `wp-player` /
  `wp-player-line` (the player's own Wikipedia article) and `fa-cup-final-1889`; their meaning and every URL are in
  `docs/data/history/POSITIONS_1888-89.md`. A new code goes in both `tools/data/validate.py` and `GameData`.
  1888-89 now: 117 / 47 / 81 / 40 / 41 / 12 / 2 sourced, 208 `placeholder` of 548 squad places (League clubs: 4).
  Replace placeholders as sources are found (keep the source honest).
- **`dribbling`** (D19-7): same 3-23 scale as every attribute. Most players have no source: the game **derives** it
  from his other attributes, `round(0.35 dexterity + 0.25 trickShots + 0.25 acceleration + 0.15 speed)` clamped to
  3-23 (`PlayerAttributeGenerator.deriveDribbling`, integer maths, no dice: the random stream is unchanged). A squad
  value overrides that and must cite its source (1888-89: Southworth "Prince of Dribblers", Archie Hunter "a prince
  of dribblers", both 18).
- Errors: an unknown field, a bad enum, a key that is not a player of the squad, a dribbling value outside 3-23 or
  with an empty source. A squad player without a position is a warning. No new ids (no `ids.json` change).
- **No position in the data** (generated players, a squad player without an entry, an old save): the game assigns
  one without dice (`PositionAssigner`): a squad gap takes the line, then slot, the squad is shortest of; a generated
  player the line his seeded attributes suit best. Dribbling then drifts and ages like every other attribute.
- **Saves v2:** `Player.position` (enum name) and `Player.dribbling`. The v1 -> v2 step in `SaveMigrations` adds
  dribbling (sourced, else derived from the saved attributes) and the position from the club's squad entry.

## History facts and texts (H-05, D30)
Approved shape of `docs/design/HISTORY_DATA_PROPOSAL.md`; the research is in `docs/data/history/`. Nothing here is
used by gameplay yet; `GameData` only loads and exposes it (`nicknames`, `nameHistory`, `squadFanTiers`,
`historyText`, `DataRecord.refs("rivals")`).
- **`clubs.json`** (all optional): `origin` (`church cricket works school old-boys rugby merger other`), `nicknames`
  (`{name, from?, to?, period?, sources}`; `period: true` = sourced as used at the time, else a later name; the game
  shows only period names in-season), `rivals` (club ids; symmetric by convention, one-sided = warning),
  `nameHistory` (`{name, from, to?, sources}`).
- **`people.json`**: `nicknames` (same shape), `knownAs` (display name where it differs).
- **`seasons/<id>/squads.json`**: `fanTiers` `{person-id: 0-5}`, the club's fans' tier of a player at the season start;
  keys must be players of the squad; missing = derived default (1 sourced, 0 placeholder). Scale: 0 unknown, 1 squad
  player, 2 local favourite, 3 crowd idol, 4 club hero, 5 legend (`PLAYERS_1888-89.md`).
- **`text/<lang>/history.json`** (listed in `pack.json`, UTF-8; `en` is the fallback): `{"club": {id: {hook, history[],
  seasons{season: text}, sources[]}}, "person": {id: {bio, sources[]}}}`. Keys are data ids (aliases followed); season
  keys must be seasons; `sources` is required and must be http(s) links; another language may only hold keys that exist
  in `en` (orphan = error, missing = warning).
- **Not done:** `kit` (colours and patterns per season squad): its shape is to be agreed with H-03 (era kit) first.
  Saves do not store a runtime fan meter yet.
- Data filled so far: the 12 League clubs of 1888-89 (origin, nicknames, rivals, nameHistory), every tiered player,
  club and season text for all 35 clubs, 178 player bios. Origin, nicknames and rivals of the FA Cup clubs are not done.
## Recruitment and languages (R-02, R-11a)
Pack-level files, each record with a stable id and an `ids.json` number (kinds `rule`, `wage`, `origin`,
`origin-club`, `area`, `language`), except the rate rows. Design: `docs/design/RECRUITMENT_AND_TRIALS.md` §6, §10;
facts: `docs/data/history/RECRUITMENT_1888-1900.md`, `LANGUAGES_LITERACY_1880-1914.md`.
- **`confidence`** of rules, wages, origins, areas and rate rows: `high` / `medium` / `low` (the research grades),
  `estimate` (no direct source; `sources` says what it is anchored on) or `placeholder` (gameplay tuning, D30 A3). Only
  `estimate` and `placeholder` may have an empty `sources`. Money is in **pence** (240 to the pound).
- `recruitment/rules.json`: `rule` (registration, residence, second-registration-consent, registration-wait,
  scottish-professional-ban, retain-and-transfer, max-wage), `from` / `until` (`until` = first day no longer in force),
  `body`, `appliesTo`, and the value the kind needs (`miles` + `years`, `days`, `pence`).
- `recruitment/wages.json`: `type` `example` (one man's sourced terms: `weeklyPence`, `summerWeeklyPence`, `club`,
  `person`) or `band` (`minWeeklyPence`..`maxWeeklyPence` for a `standing` leading / middling / small / any).
- `recruitment/origins.json` (D28 Q12): backstories with `occupations`, `sourceKinds` (design §3.1), `nudges`
  (attributes, whole numbers -2..2) and `literacyShift` (-1..1). `recruitment/origin_clubs.json`: period works, church
  and Scottish clubs (`kind`, `place`, `area`, optional `club` it became, `origins`).
- `languages.json` (`countries` = where it is the school language), `areas.json` (rate areas of England, Wales,
  Scotland, Ireland; `weight` = share of the country's 1881 people, adding up to 1 per country), `language_rates.json`
  (rows for an `area`, a list of `countries` or nobody = the default row, a `bornFrom`..`bornUntil` band, and some of
  `speaks` / `only` (non-English languages), `signsMen` / `signsWomen`, `parentsBorn` (country shares),
  `readWriteIfSigns` (levels 1-4, adding to 1)). A lookup takes, per field, the area row, else the country row, else
  the default; for one target and field the bands never overlap. `people.json` may give a sourced `birthArea`.
- **Person languages (saves v3):** `languages` = `[{language, speak, understand, readWrite}]` (levels 0-4, home
  language first), `homeLanguage`, `birthArea`, `parentsOrigin` (country id or absent). Generated by
  `LanguageGenerator` on `fork("database").fork("lang:" + dataId)` (generated people: `lang:id:<runtime id>` or the
  people stream), never from football attributes. The v2 -> v3 step in `SaveMigrations` runs the same generator.
- Java: `GameData` checks the same rules; `RecruitmentData` and `LanguageData` (from `DatabaseLoader`) are the
  read-only lookups.

## Season calendar and neutral grounds (P2-14, D30 A9)
Two optional blocks in `seasons/<id>/season.json`; the rules the generated schedule follows (D17: never the real
fixture list). A season without them keeps the latest earlier season's, one year on; no data at all = the code
defaults in `CalendarRules.DEFAULTS` (the 1888-89 rules).
```
"calendar": {
  "league": { "opens": {"onOrAfter": "09-08", "weekday": "saturday", "confidence": "unverified", "source": url},
              "closes": {"onOrBefore": "04-20", ...}, "midweek": "wednesday", "noMatchOn": ["12-25"], "sources": [...] },
  "cups": [ { "competition": "fa-cup", "opens": {...10-01 saturday...}, "firstDraw": {...01-01 monday, placeholder...},
              "laterDraws": "open-draw", "drawDaysAfterRound": 2, "final": {"onOrBefore": "03-31", ...},
              "properFrom": "02-01", "daysBetweenRounds": 14, "earlyRoundsOn": "wednesday", "maxEarlyRounds": 8,
              "sources": [...] } ] }
editions[fa-cup-1888-89].neutralVenues: { "rounds": [ {"round": "semi-final", "grounds": [...], "confidence": ...},
              {"round": "final", "grounds": ["kennington-oval"], ...} ], "gate": "association", "gateConfidence": ...,
              "sources": [...] }
```
- A **date rule** is a month-day (`MM-DD`) and a weekday: the first such weekday on or after it (`onOrAfter`) or the
  last on or before it (`onOrBefore`), exactly one; July-December fall in the season's first year, January-June in
  the next. Each rule has its own `confidence` (and `source`), so a placeholder day is marked where it is.
- **Cup:** `opens` = the edition starts and its creation script runs (1888-89: 6 Oct, the first qualifying round);
  `firstDraw` = the first round is drawn on or after it (7 Jan 1889, placeholder); `laterDraws` `open-draw` = each
  later round is drawn once the round before is decided, `drawDaysAfterRound` days after its date at the earliest
  (`bracket` = paired by the first draw); round dates as P2-11 (fortnightly Saturdays back from `final`, from
  `properFrom`; extra rounds on `earlyRoundsOn`).
- **Neutral grounds:** the cup rounds (`final`, `semi-final`, `quarter-final`, counted back from the final) played
  at a neutral ground, with the candidate grounds; the game draws one per tie when its clubs are known
  (`fork("venue:<match>")`), never a club's own nor one another tie uses that day; a replay stays there. `gate`:
  `association` (the FA keeps it: 1888-89), `shared` (equal halves), `home` (the club drawn at home). The match
  stores the ground id as `Match.venue` (saved; empty = the home club's ground).
- Errors (both validators): an unknown field or weekday, a bad month-day, a rule with no or two anchors, two
  calendars for one cup, a round listed twice, a round with no ground, an unknown ground or competition.

## Injuries (G-02, D19-3)
`injuries.json` (pack level, kind `injury`, one record per kind): `kind` `knock` (plays on) / `passenger` (stays on,
worth `strengthWhileHurt` of a fit man; no substitutes in 1888) / `carried-off` (leaves, the side plays short; comes
back on as a passenger with `returnChance`), `chancePerMatch` (per man per match, 0..1), `minDays`..`maxDays` (days
out after the match; 0 = fit for the next one), `confidence` + `sources` as above (all `estimate` now: no injury
rates for the era were found). Both validators: each kind once, 0 <= minDays <= maxDays, chances 0..1. A kind left
out keeps the code default (`InjuryRules`). Saves: `Player.injury` (kind, since, until, daysOut, matchId) and
`Match.home/awayInjuries` (person, kind, minute, returned minute, days out) are optional fields (no saveVersion bump).

## Rename log
| Date | Kind | Old id | New id | Why | Approved by |
|---|---|---|---|---|---|
| 2026-11-27 | club | witton-albion | witton | Wrong club: the 1888-89 FA Cup entrant is Witton F.C. of Blackburn, not Witton Albion (Northwich, no FA Cup before 1907-08). [Witton F.C.](https://en.wikipedia.org/wiki/Witton_F.C.) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | james-duerden-1865 | james-duerden-1866 | Same person; Blackburn in 1888-89, Burnley only 1890-91. [source](https://en.wikipedia.org/wiki/James_Duerden) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | robert-haresnape-1865 | robert-haresnape-1866 | Same person; Blackburn in 1888-89, Burnley from 1889. [source](https://en.wikipedia.org/wiki/Robert_Haresnape) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | joe-lofthouse-1865 | joe-lofthouse-1864 | Same person (b. 1865-04-14), Accrington all season. [source](https://en.wikipedia.org/wiki/Joe_Lofthouse) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | pat-gallocher-1865-2 | pat-gallocher-1865 | Same person (b. 1865-04-25), Burnley then Accrington. [source](https://en.wikipedia.org/wiki/Pat_Gallocher) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | william-chatterton-1865 | william-chatterton-1861 | Same person (b. 1861-12-27), Derby County. [source](https://en.wikipedia.org/wiki/William_Chatterton) | PM, P2-4 slice 4 brief |
| 2026-11-27 | person | arthur-brown-1858 | albert-brown-1862 | Wrong person: Arthur Brown had retired by 1886; Villa's 1888-89 right winger is his brother Albert Alfred Brown. [source](https://en.wikipedia.org/wiki/Albert_Brown_(footballer,_born_1862)) | PM, H-01b brief |
| 2026-11-27 | person | james-weir-1864 | john-weir-1865 | Wrong person: Everton's half-back is John Weir (b. 1865-01-10, Scotland), listed as "James" in one summary; Jimmy Weir (b. 1864) is someone else. [source](https://en.wikipedia.org/wiki/John_Weir_(footballer)) | PM, H-01b brief |
| 2026-11-27 | person | harry-cursham-1859 | henry-cursham-1863 | Same person (Harry Cursham, b. 1859-11-27), Notts County, not Swifts. [source](https://en.wikipedia.org/wiki/Harry_Cursham) | PM, H-01b brief |
| 2026-11-27 | person | george-cotterill-1868 | gh-cotterill-1868 | Same person (George Huth Cotterill, same birth date), capped as an Old Brightonian; dropped from Swifts. [source](https://en.wikipedia.org/wiki/Old_Brightonians_F.C.) | PM, H-01b brief |
