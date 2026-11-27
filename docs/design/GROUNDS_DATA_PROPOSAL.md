# Grounds data proposal (H-02)

A proposal only. `validate.py`, `ids.json`, `grounds.json` and the Java data classes belong to the data lane
(BOOK3PRO), and this lane did not change them. The research is in `docs/data/history/GROUNDS_1888-89.md` and the
pictures are in `tools/art`.

## 1. New optional fields on a `ground` record
All fields are optional, so the current records stay valid. Each new fact should also have its URL in
`sources`.

| Field | Type | Meaning / example |
|---|---|---|
| `location` | str | "Off Anfield Road, by Stanley Park, Liverpool" |
| `closed` | int | last year of football use, e.g. Dudley Road 1889 |
| `recordCrowd` | object | `{"value": 10000, "date": "1889-03-02", "match": "Wolves v The Wednesday, FA Cup", "source": url}`. This replaces using `capacity` for the record: `wellington-road` currently has `capacity` 26,849, which is the 1888 record crowd |
| `capacityNote` | str | how the capacity was reached (estimate, press figure, "about 12,000 under cover plus 6,000 standing") |
| `stands` | list | `[{"name": "Oakfield Road stand", "side": "north/south/east/west/end", "kind": "grandstand/open/bank/terrace/pavilion/shelter", "covered": true, "seats": 600, "built": 1886, "material": "wood"}]`; unknown parts are left out |
| `pitch` | object | `{"length": 110, "width": 70, "slope": "away from one goal", "surface": "grazed, uneven", "track": "cinder"}` (yards) |
| `sharedWith` | strs | `["cricket", "athletics", "cycling", "racing"]` |
| `changing` | str | "Sandon Hotel", "blacksmith's shed, later the Crown & Cushion" |
| `admission` | object | `{"ground": "3d", "stand": "6d", "season": 1888, "source": url}` (old money as text) |
| `landlord` | str | "Colonel Hargreaves of Broad Oak (1s a year)" |
| `illustration` | str | resource path without extension: `art/grounds/anfield` (the game adds `.png` or `_thumb.png`) |
| `confidence` | enum | the same `unverified / checked / sourced` scale as people |

Seasonal facts (stands, admission, capacity) can differ from year to year. Under D15 they could go in
`seasons/<season>/grounds.json` overrides later; for now one value per record, describing 1888-89, is enough.

## 2. Corrections to existing records
| Record | Problem | Proposed fix |
|---|---|---|
| `racecourse-ground` | `description` describes the Baseball Ground (1895+) | Replace with the County Ground / racecourse text from the history doc. Consider renaming it to `county-ground-derby` (alias plus rename log) |
| `wellington-road` | `capacity` 26,849 is the 7 Jan 1888 record crowd | Move it to `recordCrowd`; leave `capacity` unset (not found) |
| `notts-rangers` squad | ground `castle-ground`, but Rangers moved to Meadow Lane in Sep 1886 | New ground `meadow-lane-1886` (or similar), or leave the field unset with a note |
| `grimsby-town` squad | `abbey-park` (first match 30 Aug 1889) | New ground `clee-park` for 1888-89 |
| `chatham-town` squad | `maidstone-road-ground`, but the 1889 ties were on the Army's open "Lines" | New ground `great-lines-chatham` (unverified); Maidstone Road came after the cup run |
| `ewood-park` | its `description` says Rovers used it in 1882 | Fine; it is not used in 1888-89 (`leamington-road` is) |
| `stoney-lane` | 600 seats or over 2,000 | Note both sources in `capacityNote` |

## 3. New venue records
`kennington-oval` (1889 final, Chatham v Forest replay), `bramall-lane` (semi-final) and `clee-park`. The
Alexandra Recreation Ground (other semi-final) already exists. Descriptors and pictures for the first three
already exist under these ids. `test_ground_art.py` accepts them until the records exist.

## 4. Pictures in the game
- Files: `futtoboru-core/src/main/resources/art/grounds/<id>.png` (320x180) and `<id>_thumb.png` (64x36), for 22
  grounds. Load with `Gdx.files.internal("art/grounds/" + id + ".png")` and scale by whole numbers (2x at
  1280x720, 4x at 2560x1440) with `TextureFilter.Nearest` to keep the pixels crisp.
- Where they should show (UI lanes): the **club screen** (ground panel: picture, name, opened, capacity, short
  description), a **ground detail view** opened from it (picture, facts table, stands, record crowd, sources
  link), the **match screen / pre-match header** (thumbnail or banner of the venue, including neutral FA Cup
  venues), the **new game club picker** (thumbnail next to each club) and **history screens** (the venue of past
  finals). None of this is wired up; there is no ground screen today.
- A missing picture must fall back to a plain panel (mods may add grounds without art).
- Regenerating: `python tools/art/ground_art.py`; `python -m pytest tools/art` fails if a committed PNG is stale.
  CI runs only `tools/data` today. Adding `python -m pytest -q tools/art` to the data job is suggested.
