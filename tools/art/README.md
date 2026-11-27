# Ground illustrations (code-generated)

Small pixel-art pictures of the 1888-89 grounds, drawn by our own code from a descriptor per ground. No AI
images, photos, scans or traced drawings are used, so the PNGs are our own work and safe for this public repo
(PRIVACY.md). The research behind each descriptor is in `docs/data/history/GROUNDS_1888-89.md`.

```
python tools/art/ground_art.py             # render every descriptor
python tools/art/ground_art.py anfield     # render one
python tools/art/ground_art.py --check     # exit 1 if a committed PNG no longer matches its descriptor
python -m pytest -q tools/art              # determinism + committed-output tests
python tools/art/preview.py anfield out.png  # 3x enlargement for eyeballing (not committed)
```

Output: `futtoboru-core/src/main/resources/art/grounds/<ground-id>.png` (320x180) and `<ground-id>_thumb.png`
(64x36, a 5x5 box average). Standard library only (no PIL), so it runs in CI as it is.

## How it draws
One fixed view for every ground: from the near touchline, looking across the pitch at the far side.
Layers, back to front: dithered sky (season palette, clouds) > hills > ground band > skyline (chimneys, mills,
spires, bottle ovens, gasholders, houses, trees, railway with a train, colliery headgear) > far-side
structures (covered grandstand, open wooden stand, earth bank, pavilion, shed, fence, inn, haystack, entrance
arch, racecourse rails, standing crowd) > pitch (slope, wear, optional cinder/grass track) > 1888 markings
(touch and goal lines, halfway line, goals with crossbar and **no nets**, corner flags; no penalty area before
1891) > 22 players in the two kits and the ball > near-side crowd behind a rope, fence or bank > frame.

Determinism: no `random`, no trigonometry. Noise is an integer xorshift seeded from the ground id, geometry is
integer or plain IEEE arithmetic, and the PNG has no timestamp or text chunks, so the same descriptor gives the
same pixels everywhere and the same bytes with the same zlib. `--check` and the tests compare decoded pixels,
so a different zlib build cannot cause a false failure.

## Descriptor (`grounds/<ground-id>.json`)
| Field | Meaning |
|---|---|
| `id`, `name` | ground id (matches `grounds.json`, or a venue proposed in `docs/design/GROUNDS_DATA_PROPOSAL.md`) |
| `season` | `autumn` / `winter` / `spring`: sky, grass, bare trees in winter |
| `weather` | `clear` / `overcast` / `mist` |
| `crowd` | 0-100, how full the far side is |
| `wind` | `1` or `-1`, smoke direction |
| `slope` | `{direction: left-down / right-down / none, amount: px}` |
| `hills` | `{height}` for a hill line behind the town |
| `skyline[]` | `{type, x (0-1 of width), height, width}`; types above |
| `farSide[]` | `{type, from, to (0-1 along the far touchline, may go past 0/1), height, covered, gable, roof, flag, wall, sign}` |
| `nearSide` | `{type: rope / fence / bank, crowd}` |
| `pitch` | `{condition: good / worn / muddy, track: cinder / grass}` |
| `kits` | `{home: [shirt, knickers], away: [...]}` palette names or `#rrggbb`; approximate, not a sourced kit record |
| `view` | one line: what the picture shows and why (from the research) |
| `research` | link to the ground's section in the history doc |

Pictures are impressions: where a source fixes a stand, bank, track, slope or neighbour it is drawn; where it is
silent the generic parts (skyline, crowd, which side a stand is drawn on) are choices, listed per ground in the
history doc.
