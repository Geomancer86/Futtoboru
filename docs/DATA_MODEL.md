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
    competitions.json        football-league, fa-cup (type LEAGUE / CUP)
    clubs.json               identity only: name, fullName, founded, country, sources
    grounds.json             stadiums: capacity, value, rent, description, sources
    people.json              every real person once: firstName, lastName, born, country, confidence, sources
    aliases.json             old id -> new id, per kind, kept forever
    ids.json                 data id -> runtime number, per kind, append-only (P2-5)
    seasons/1888-89/
      season.json            start, countries, editions (league + cup, with participants)
      squads.json            per club this season: ground, balance, players (person ids)
      nationality_modifiers.json  region_modifiers.json
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

## Rename log
| Date | Kind | Old id | New id | Why | Approved by |
|---|---|---|---|---|---|
| | | | | | |
