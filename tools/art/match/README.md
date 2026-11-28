# Match sprites (code-generated, E-21s)

Sprite sheets for the match viewer, baked by our own code from the art picks of D26 (ROADMAP): players as the
A2 "doll" (a tiny 3-D skeleton with capsule limbs lit in three tones, flat-coloured head, hands and joints as on the prototype page, depth-buffered, 1-px outline; matches the prototype pixel for pixel at the same scale and camera) in
8 directions, keeper G2 (same kit + dark cap), ball BA2, officials O2 (hand-drawn pixel grids), crowd figures
CR1 and touchline T1 (pavilion, shed, tent, carriage, rope, railings, chairs, press table, telegraph pole,
bucket; committee men, trainer with sponge and bucket, pressmen, telegraph boy, constable). No advertising
boards and no bench. No AI images; ported from the private art prototype page (our own code).

```
python tools/art/match/match_art.py           # bake everything (~40 s), writes only files that changed
python tools/art/match/match_art.py --check   # exit 1 if a committed file differs from a fresh bake (or is stale)
python -m pytest -q tools/art                 # determinism, committed output, index consistency, kits
python tools/art/match/preview_html.py <out.html>   # local animated preview page (sheets embedded; not committed)
```

Standard library only. Determinism: our own sine/cosine polynomials (no libm), IEEE `+ - * / sqrt` only,
integer xorshift noise seeded by name, PNGs without timestamp or text chunks. `--check` compares decoded pixels.

## Scales and sizes
| | 1x (S1, 5 px/m) | 2x (10 px/m) |
|---|---|---|
| player / keeper (standing, incl. outline) | 6 x 12 px; cell 20 x 17 | 10 x 21 px; cell 38 x 31 |
| officials (O2, bowler hat) | ~7 x 13; cell 11 x 15 | ~12 x 22; cell 17 x 24 |
| touchline people (doll) | cell 10 x 14 | cell 16 x 25 |
| ball BA2 (`ground` / `high`) | 5 / 7 px | 9 / 13 px |
| crowd CR1, props T1 | 1x only (cell 6 x 14; props packed) | scale the 1x art by 2 |

The 2x sheets are re-rendered at 10 px/m, not upscaled: at 5 px/m the doll is ~10 px and stripes, sashes and
caps are only one pixel wide, so the prototype's own kit and animation boards used 10 px/m for readability. The
viewer defaults to the 2x sheets (D28: match view at 10 px/m) and keeps 1x for the wide, zoomed-out view (drawing 2x at half
size in world units), or for the near touchline under V3 perspective. Cells are uniform per layout (sized to the
widest pose: lying, charging), so every frame of a layout has the same pivot.

## Output (`futtoboru-core/src/main/resources/art/match/`)
| File | Content |
|---|---|
| `kits/<season>/<club>_{1x,2x}.png` | one sheet per club kit: `player/<anim>/<dir>/<frame>` and `keeper/<anim>/<dir>/<frame>` |
| `mud_{1x,2x}.png` | mud overlay with the kit layout: draw it over any kit frame, same rect (alpha = how dirty) |
| `officials_{1x,2x}.png` | `umpire-a`, `umpire-b` (idle, walk, flag), `referee` (idle, walk, signal); drawn in 4 directions, diagonals are aliases |
| `people_{1x,2x}.png` | `committee`, `committee-b`, `trainer`, `press`, `constable` (idle, walk), `telegraph-boy` (idle, run) |
| `ball_{1x,2x}.png` | `ball/<ground|high>/<dry|damp|soaked|muddy>/<spin 0-7>` |
| `crowd_1x.png` | `crowd/v00..v11/<calm|cheer|angry|encroach>/<frame>` (6 hats x tall/short; bonnet = woman) |
| `touchline_1x.png` | `prop/<name>/<frame>`; `rope` and `railings` are tiles (pivot at the left post) |
| `index.json` | everything the viewer needs (below) |

Player animations: idle 4, run 8, kick 4, header 3, charge 8, fall 4, injured 4, celebrate 4, throwin 2 (two
hands from behind the head). Keeper: idle, run, kick, fall, catch 3, punch 3, throwin, celebrate. The ball is
**not** drawn into player frames (the viewer draws the sim's ball).

## Index format (`index.json`, `format: 1`) - agreed contract for the viewer (E-21b)
```
{ "format": 1, "pixelsPerMetre": 5, "camera": {...}, "directions": ["S","SE","E","NE","N","NW","W","SW"],
  "sheets": [ {"id", "file", "kind": kit|overlay|officials|people|ball|crowd|touchline, "layout", "scale",
               "pixelsPerMetre", ("club", "season", "uncertain", "pattern" for kits)} ],
  "layouts": { "<layout>": { "size": [w, h],
       "animations": { "player/run": {"frames": 8, "fps": 12, "loop": true, "dirs": 8}, ... },
       "regions": { "player/run/E/3": [x, y, w, h, pivotX, pivotY], ... } } } }
```
- A region name is `<actor>/<anim>/<dir>/<frame>` for directional animations (`dirs` present) and
  `<group>/<anim>/<frame>` otherwise (props, crowd and ball use their variant as the middle part).
- Pivot = the feet / ground contact point (ball: its lowest pixel), relative to the region's top-left. Draw a
  frame at `screen - pivot`. Jumps and headers are already lifted inside the frame.
- Directions: S faces the camera, E faces screen right, N away; index 0-7 clockwise seen from above
  (S, SE, E, NE, ...). Pick by heading; frames were rendered for each direction (no mirroring), except
  officials, whose diagonals alias the nearest of S/E/N/W.
- All kit sheets of one scale share one layout (`doll_1x` / `doll_2x`), as does the mud overlay.

## Kits (`kits.json`)
Per season and club id: `pattern` (plain, halves, quarters, hoops, stripes, sash, spots), `colours` (shirt colours
in pattern order; halves/quarters: wearer's left first), `knickers`, `socks`, optional `collar`, `caps` + `cap`
(outfield caps), `shinPads` (default true, outside the stockings), `uncertain`, `guessed` (parts with no source) and
`notes`. Seeded from `docs/data/history/clubs/*.md`; every club there has a kit (35 for 1888-89). Uncertain kits
are listed in the index (`"uncertain": true`) so the game can say so. Each club sheet gets a seeded face (hair,
skin, moustache). To add a season, add a block to `kits.json` and re-bake.
