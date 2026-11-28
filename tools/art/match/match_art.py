"""Match sprite baker (E-21s): code-generated, deterministic match art for the V3 side-on viewer (D26).

Bakes sprite sheets and one JSON index into futtoboru-core/src/main/resources/art/match/:
players and keepers per club kit (A2 doll, G2 keeper, 8 directions), a mud overlay, officials (O2 hand grids),
touchline people (doll), crowd figures (CR1), the ball (BA2) and touchline props (T1).
Standard library only (no PIL), like tools/art/ground_art.py, so it runs in CI as it is.

    python tools/art/match/match_art.py            # bake everything
    python tools/art/match/match_art.py --check    # exit 1 if a committed file differs from a fresh bake

Scales: `1x` = 5 px/m (S1, D26); `2x` = the same art re-rendered at 10 px/m for close zoom (not an upscale).
"""
from __future__ import annotations

import json
import math
import struct
import sys
import zlib
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import match_doll as doll  # noqa: E402
import match_grids as grids  # noqa: E402
from match_grids import OUT, PAL, Rng, mix, mul, rnd  # noqa: E402

REPO = HERE.parent.parent.parent
OUT_DIR = REPO / "futtoboru-core" / "src" / "main" / "resources" / "art" / "match"
KITS = HERE / "kits.json"
PPM = 5                      # S1: pixels per metre at 1x
SCALES = (1, 2)
FORMAT = 1

PLAYER_STATES = ("idle", "run", "kick", "header", "charge", "fall", "injured", "celebrate", "throwin")
KEEPER_STATES = ("idle", "run", "kick", "fall", "catch", "punch", "throwin", "celebrate")
ANIM = {  # fps, loop
    "idle": (4, True), "run": (12, True), "walk": (8, True), "kick": (10, False), "header": (8, False),
    "charge": (12, True), "fall": (6, False), "injured": (3, True), "celebrate": (6, True),
    "throwin": (4, False), "catch": (8, False), "punch": (10, False), "flag": (2, False), "signal": (2, False),
}
PEOPLE = {  # touchline people drawn with the doll (T1); looks are period impressions, not sourced records
    "committee": {"role": "committee", "hat": "topper", "jacket": (30, 30, 34), "trousers": (40, 40, 44),
                  "hatc": (20, 18, 20), "anims": ("idle", "walk")},
    "committee-b": {"role": "committee", "hat": "bowler", "jacket": (44, 40, 38), "trousers": (52, 50, 50),
                    "hatc": (28, 26, 28), "anims": ("idle", "walk")},
    "trainer": {"role": "trainer", "hat": None, "item": "bucket", "jacket": (200, 196, 184),
                "trousers": (70, 66, 62), "anims": ("idle", "walk")},
    "press": {"role": "press", "hat": "bowler", "jacket": (70, 60, 52), "trousers": (60, 56, 52),
              "hatc": (50, 44, 40), "anims": ("idle", "walk")},
    "telegraph-boy": {"role": "boy", "hat": "cap", "body": 0.8, "jacket": (90, 70, 50), "trousers": (90, 70, 50),
                      "hatc": (60, 52, 44), "anims": ("idle", "run")},
    "constable": {"role": "police", "hat": "helmet", "jacket": (28, 32, 58), "trousers": (28, 32, 58),
                  "hatc": (28, 32, 58), "anims": ("idle", "walk")},
}
CREAM, PAD, BOOT = PAL["cream"], (192, 166, 118), (58, 40, 28)
KEEPER_CAP = (40, 40, 46)    # G2: same kit plus a dark cap
DIR4_OF = {"S": "S", "SE": "E", "E": "E", "NE": "N", "N": "N", "NW": "N", "W": "W", "SW": "W"}


# ---------------------------------------------------------------- PNG (RGBA, no timestamp/text chunks)
def png_bytes(w: int, h: int, px: list) -> bytes:
    raw = bytearray()
    for y in range(h):
        raw.append(0)
        for p in px[y * w:(y + 1) * w]:
            if p is None:
                raw += b"\0\0\0\0"
            elif len(p) == 4:
                raw += bytes(p)
            else:
                raw += bytes((p[0], p[1], p[2], 255))

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")


def png_pixels(data: bytes) -> tuple[int, int, bytes]:
    """Decode a PNG written by png_bytes (filter 0, RGBA) to raw pixels, for --check and tests."""
    pos, idat, w, h = 8, b"", 0, 0
    while pos < len(data):
        (n,) = struct.unpack(">I", data[pos:pos + 4])
        tag, body = data[pos + 4:pos + 8], data[pos + 8:pos + 8 + n]
        if tag == b"IHDR":
            w, h = struct.unpack(">II", body[:8])
        elif tag == b"IDAT":
            idat += body
        pos += 12 + n
    raw = zlib.decompress(idat)
    stride = w * 4 + 1
    return w, h, b"".join(raw[y * stride + 1:(y + 1) * stride] for y in range(h))


# ---------------------------------------------------------------- colours
def hexcol(c):
    if isinstance(c, str):
        return tuple(int(c[i:i + 2], 16) for i in (1, 3, 5))
    return tuple(c)


RAMPS: dict = {}


def ramp(c):
    r = RAMPS.get(c)
    if r is None:
        r = (mix(mul(c, 0.64), (38, 42, 80), 0.2), c, mix(c, (255, 246, 222), 0.24))
        RAMPS[c] = r
    return r


def pattern_colour(kit: dict, u: float, v: float, th: float, S: int):
    """Shirt colour at surface point u (m, + = wearer's left), v (0 waist .. 1 neck), th (0..1 around)."""
    cs, p = kit["colours"], kit["pattern"]
    a = cs[0]
    b = cs[1] if len(cs) > 1 else a
    if p == "halves":
        return a if u >= 0 else b
    if p == "quarters":
        return a if (u >= 0) == (v >= 0.5) else b
    # stripes, hoops and sash: the prototype's exact formulas (A2 pick, D26)
    if p == "stripes":
        return a if math.floor((u + 0.33) / 0.066) % 2 == 1 else b
    if p == "hoops":
        if len(cs) >= 3:
            return cs[math.floor(v * 6) % 3]
        return a if math.floor(v * 5) % 2 == 1 else b
    if p == "sash":  # left shoulder to right hip
        return b if abs(u / 0.17 + (v - 0.5) * 1.7) < 0.32 else a
    if p == "spots":
        nth, nv = (8, 3) if S < 8 else (14, 5)
        i, j = int(th * nth), int(v * nv)
        fu, fv = th * nth - i, v * nv - j
        lim = 0.34 if S < 8 else 0.26
        return b if (i + j) % 2 == 0 and abs(fu - 0.5) < lim and abs(fv - 0.5) < lim else a
    return a


def sleeve_colour(kit: dict, side: int):
    cs = kit["colours"]
    if kit["pattern"] in ("halves", "quarters") and len(cs) > 1:
        return cs[0] if side > 0 else cs[1]
    return cs[0]


def kit_look(club: str, raw: dict) -> dict:
    """Resolved kit colours plus a seeded face (hair, skin, moustache) for the club's sheet."""
    r = Rng("kit:" + club)
    k = {"pattern": raw["pattern"], "colours": [hexcol(c) for c in raw["colours"]],
         "knickers": hexcol(raw["knickers"]), "socks": hexcol(raw["socks"]),
         "collar": hexcol(raw["collar"]) if raw.get("collar") else CREAM,
         "caps": bool(raw.get("caps", False)), "pads": raw.get("shinPads", True) is not False,
         "hair": grids.HAIR[r.next() % 5], "skin": grids.SKIN[r.next() % 3], "tache": r.next() % 3 != 0}
    k["cap"] = hexcol(raw["cap"]) if raw.get("cap") else k["colours"][0]
    return k


def player_colour(key: tuple, tone: int, kit: dict, keeper: bool, S: int):
    m = key[0]
    if m == "out":
        return OUT
    if m == "shirt":
        c = pattern_colour(kit, key[1], key[2], key[3], S)
    elif m == "sleeve":
        c = sleeve_colour(kit, key[1])
    elif m == "collar":
        c = kit["collar"]
    elif m == "knick":
        c = kit["knickers"]
    elif m == "sock":
        c = kit["socks"]
    elif m == "pad":
        c = PAD if kit["pads"] else kit["socks"]
    elif m in ("boot", "foot"):
        c = BOOT
    elif m == "skin":
        c = kit["skin"]
    elif m == "head":
        cap, base = key[1], key[2]
        wears = keeper or kit["caps"]
        capc = KEEPER_CAP if keeper else kit["cap"]
        if wears and cap:
            c = capc if cap == "cap" else mul(capc, 0.8)
        elif base == "hair":
            c = kit["hair"]
        elif base == "tache":
            c = kit["hair"] if kit["tache"] else kit["skin"]
        else:
            c = kit["skin"]
    else:
        raise ValueError(f"unknown player material {key}")
    return ramp(c)[tone]


def mud_level(key: tuple) -> float:
    m = key[0]
    if m == "knick":
        return 0.25
    if m == "sock":
        return 0.4 * (key[1] + 0.5) / 4
    if m in ("pad", "foot"):
        return 0.3
    if m == "boot":
        return 0.15
    if m == "shirt" and key[2] < 0.3:
        return 0.2
    return 0.0


def people_colour(key: tuple, tone: int, look: dict):
    m = key[0]
    if m == "out":
        return OUT
    c = {"jacket": look["jacket"], "front": CREAM, "trousers": look["trousers"], "boot": BOOT, "foot": BOOT,
         "skin": look["skin"], "hair": look["hair"], "tache": look["hair"] if look["tache"] else look["skin"],
         "hat": look.get("hatc", (30, 30, 30)), "cap": look.get("hatc", (30, 30, 30)),
         "peak": mul(look.get("hatc", (30, 30, 30)), 0.8), "bucket": PAL["iron"], "sponge": PAL["sponge"]}.get(m)
    if c is None:
        raise ValueError(f"unknown material {key}")
    return ramp(c)[tone]


# ---------------------------------------------------------------- layouts
class Layout:
    """Named regions on one sheet: name -> [x, y, w, h, pivotX, pivotY] (pivot relative to the region)."""

    def __init__(self, name: str):
        self.name = name
        self.w = self.h = 0
        self.regions: dict[str, list] = {}
        self.place: dict[str, tuple] = {}   # name -> (frame, dx, dy): pixel (x, y) of frame goes to (x+dx, y+dy)
        self.animations: dict[str, dict] = {}


def frame_box(fr):
    """(x0, y0, x1, y1, pivotX, pivotY) of a Geo or Img (bbox of opaque pixels)."""
    w = fr.w
    xs = [i % w for i, p in enumerate(fr.px) if p is not None]
    ys = [i // w for i, p in enumerate(fr.px) if p is not None]
    px, py = (fr.ox, fr.oy) if isinstance(fr, doll.Geo) else fr.pivot
    if not xs:
        return px, py, px, py, px, py
    return min(xs), min(ys), max(xs), max(ys), px, py


def uniform_layout(name: str, rows: list, aliases=None) -> Layout:
    """Every frame in a cell of one size with the same pivot (feet). rows: [[(region-name, frame), ...], ...]."""
    lay = Layout(name)
    boxes = {n: frame_box(f) for row in rows for n, f in row}
    L = max(b[4] - b[0] for b in boxes.values())
    R = max(b[2] - b[4] for b in boxes.values()) + 1
    T = max(b[5] - b[1] for b in boxes.values())
    B = max(b[3] - b[5] for b in boxes.values()) + 1
    cw, ch = L + R, T + B
    for r, row in enumerate(rows):
        for c, (n, f) in enumerate(row):
            x, y = c * cw, r * ch
            b = boxes[n]
            lay.regions[n] = [x, y, cw, ch, L, T]
            lay.place[n] = (f, x + L - b[4], y + T - b[5])
    lay.w = max(len(row) for row in rows) * cw
    lay.h = len(rows) * ch
    for alias, target in (aliases or {}).items():
        lay.regions[alias] = list(lay.regions[target])
    return lay


def packed_layout(name: str, rows: list) -> Layout:
    """Frames trimmed to their own size, packed left to right in rows (props)."""
    lay = Layout(name)
    y = 0
    for row in rows:
        x, rh = 0, 0
        for n, f in row:
            x0, y0, x1, y1, px, py = frame_box(f)
            w, h = x1 - x0 + 1, y1 - y0 + 1
            lay.regions[n] = [x, y, w, h, px - x0, py - y0]
            lay.place[n] = (f, x - x0, y - y0)
            x += w + 1
            rh = max(rh, h)
        lay.w = max(lay.w, x - 1)
        y += rh + 1
    lay.h = y - 1
    return lay


def paint(lay: Layout, colour) -> list:
    """Sheet pixels; colour(name, frame, pixel, x, y) -> RGB/RGBA or None."""
    out = [None] * (lay.w * lay.h)
    for n, (f, dx, dy) in lay.place.items():
        w = f.w
        for i, p in enumerate(f.px):
            if p is None:
                continue
            x, y = i % w + dx, i // w + dy
            c = colour(n, f, p, x, y)
            if c is not None:
                out[y * lay.w + x] = c
    return out


# ---------------------------------------------------------------- the bake
_GEO: dict = {}


def geos(scale: int) -> dict:
    """Material buffers of every player/keeper pose at this scale (kit-independent; cached)."""
    if scale not in _GEO:
        S = PPM * scale
        need = {}
        for st in PLAYER_STATES + KEEPER_STATES:
            for f in range(doll.FRAMES[st]):
                for d in range(8):
                    need[(st, f, d)] = None
        _GEO[scale] = {k: doll.render(k[0], k[1], k[2], S, {"role": "player"}) for k in need}
    return _GEO[scale]


def doll_layout(scale: int) -> Layout:
    g = geos(scale)
    rows = []
    for actor, states in (("player", PLAYER_STATES), ("keeper", KEEPER_STATES)):
        for st in states:
            for f in range(doll.FRAMES[st]):
                rows.append([(f"{actor}/{st}/{doll.DIRS[d]}/{f}", g[(st, f, d)]) for d in range(8)])
    lay = uniform_layout(f"doll_{scale}x", rows)
    for actor, states in (("player", PLAYER_STATES), ("keeper", KEEPER_STATES)):
        for st in states:
            fps, loop = ANIM[st]
            lay.animations[f"{actor}/{st}"] = {"frames": doll.FRAMES[st], "fps": fps, "loop": loop, "dirs": 8}
    return lay


def kit_sheet(lay: Layout, kit: dict, scale: int) -> list:
    S = PPM * scale
    cache: dict = {}

    def colour(n, f, p, x, y):
        keeper = n.startswith("keeper/")
        k = (p, keeper)
        c = cache.get(k)
        if c is None:
            c = player_colour(p[0], p[1], kit, keeper, S)
            cache[k] = c
        return c
    return paint(lay, colour)


def mud_sheet(lay: Layout) -> list:
    """Kit-independent overlay: mud colour with alpha = how dirty that part gets, plus splashes."""
    mud = ramp(PAL["mud"])

    def colour(n, f, p, x, y):
        key, tone = p
        if key[0] == "out":
            return None
        lvl = mud_level(key)
        if key[0] in ("knick", "sock", "pad", "foot", "boot") and grids.hash01(x, y, 7) < 72:
            lvl = min(0.85, lvl + 0.3)
        if lvl <= 0:
            return None
        c = mud[max(0, tone)]
        return (c[0], c[1], c[2], rnd(lvl * 255))
    return paint(lay, colour)


def officials_layout(scale: int) -> Layout:
    rows, aliases = [], {}
    lay_anims = {}
    for name, o in grids.OFFICIALS.items():
        for anim in o["anims"]:
            n = len(grids.OFFICIAL_ANIMS[anim])
            fps, loop = ANIM[anim]
            lay_anims[f"{name}/{anim}"] = {"frames": n, "fps": fps, "loop": loop, "dirs": 8, "drawnDirs": 4}
            for f in range(n):
                rows.append([(f"{name}/{anim}/{d}/{f}", grids.official_frame(name, anim, f, d, scale))
                             for d in grids.DIR4])
                for d8, d4 in DIR4_OF.items():
                    if d8 != d4:
                        aliases[f"{name}/{anim}/{d8}/{f}"] = f"{name}/{anim}/{d4}/{f}"
    lay = uniform_layout(f"officials_{scale}x", rows, aliases)
    lay.animations = lay_anims
    return lay


_PEOPLE: dict = {}


def people_layout(scale: int) -> Layout:
    if scale not in _PEOPLE:
        S = PPM * scale
        rows = []
        anims = {}
        for name, lk in PEOPLE.items():
            for st in lk["anims"]:
                fps, loop = ANIM[st]
                anims[f"{name}/{st}"] = {"frames": doll.FRAMES[st], "fps": fps, "loop": loop, "dirs": 8}
                for f in range(doll.FRAMES[st]):
                    rows.append([(f"{name}/{st}/{doll.DIRS[d]}/{f}", doll.render(st, f, d, S, lk)) for d in range(8)])
        lay = uniform_layout(f"people_{scale}x", rows)
        lay.animations = anims
        _PEOPLE[scale] = lay
    return _PEOPLE[scale]


def people_sheet(lay: Layout) -> list:
    looks = {}
    for name, lk in PEOPLE.items():
        r = Rng("people:" + name)
        looks[name] = dict(lk, skin=r.pick(grids.SKIN), hair=r.pick(grids.HAIR), tache=r.next() % 3 != 0)

    def colour(n, f, p, x, y):
        return people_colour(p[0], p[1], looks[n.split("/")[0]])
    return paint(lay, colour)


def crowd_layout() -> Layout:
    rows, anims = [], {}
    for v in range(len(grids.CROWD_VARIANTS)):
        row = []
        for mood, n in grids.CROWD_MOODS.items():
            anims[f"crowd/v{v:02d}/{mood}"] = {"frames": n, "fps": {"calm": 1, "cheer": 6, "angry": 4,
                                                                     "encroach": 4}[mood], "loop": True}
            row += [(f"crowd/v{v:02d}/{mood}/{f}", grids.crowd_frame(v, mood, f)) for f in range(n)]
        rows.append(row)
    lay = uniform_layout("crowd_1x", rows)
    lay.animations = anims
    return lay


def ball_layout(scale: int) -> Layout:
    rows, anims = [], {}
    for size in ("ground", "high"):
        for w, wet in enumerate(grids.BALL_WET):
            anims[f"ball/{size}/{wet}"] = {"frames": grids.BALL_SPIN, "fps": 12, "loop": True}
            rows.append([(f"ball/{size}/{wet}/{f}", grids.ball_frame(scale, size, w, f))
                         for f in range(grids.BALL_SPIN)])
    lay = packed_layout(f"ball_{scale}x", rows)
    lay.animations = anims
    return lay


def touchline_layout() -> Layout:
    rows, anims = [], {}
    big = ("pavilion", "shed", "tent", "carriage")
    for group in (big, tuple(k for k in grids.PROPS if k not in big)):
        row = []
        for name in group:
            draw, n = grids.PROPS[name]
            anims[f"prop/{name}"] = {"frames": n, "fps": 2, "loop": True}
            row += [(f"prop/{name}/{f}", draw(f)) for f in range(n)]
        rows.append(row)
    lay = packed_layout("touchline_1x", rows)
    lay.animations = anims
    return lay


def img_sheet(lay: Layout) -> list:
    return paint(lay, lambda n, f, p, x, y: p)


def load_kits() -> dict:
    return json.loads(KITS.read_text(encoding="utf-8"))["seasons"]


def dump_json(obj, ind=0) -> str:
    """Stable, compact JSON: one region / animation per line."""
    pad = " " * ind
    if isinstance(obj, dict):
        if not obj:
            return "{}"
        if all(not isinstance(v, (dict, list)) or (isinstance(v, list) and all(not isinstance(x, (dict, list))
                                                                                for x in v)) for v in obj.values()) \
                and len(json.dumps(obj)) < 110:
            return json.dumps(obj, ensure_ascii=False)
        items = [f'{pad} {json.dumps(k, ensure_ascii=False)}: {dump_json(v, ind + 1)}' for k, v in obj.items()]
        return "{\n" + ",\n".join(items) + "\n" + pad + "}"
    if isinstance(obj, list):
        if all(not isinstance(x, (dict, list)) for x in obj):
            return json.dumps(obj, ensure_ascii=False)
        return "[\n" + ",\n".join(pad + " " + dump_json(x, ind + 1) for x in obj) + "\n" + pad + "]"
    return json.dumps(obj, ensure_ascii=False)


def outputs() -> dict[str, bytes]:
    """Every baked file: relative path under art/match -> bytes."""
    out: dict[str, bytes] = {}
    layouts: dict[str, Layout] = {}
    sheets = []

    def add(lay: Layout, file: str, pixels: list, kind: str, scale: int, **extra):
        layouts[lay.name] = lay
        out[file] = png_bytes(lay.w, lay.h, pixels)
        sheets.append(dict({"id": file[:-4], "file": file, "kind": kind, "layout": lay.name, "scale": scale,
                            "pixelsPerMetre": PPM * scale}, **extra))

    seasons = load_kits()
    for scale in SCALES:
        lay = doll_layout(scale)
        for season, clubs in seasons.items():
            for club in sorted(clubs):
                raw = clubs[club]
                add(lay, f"kits/{season}/{club}_{scale}x.png", kit_sheet(lay, kit_look(club, raw), scale), "kit",
                    scale, club=club, season=season, uncertain=bool(raw.get("uncertain")),
                    pattern=raw["pattern"])
        add(lay, f"mud_{scale}x.png", mud_sheet(lay), "overlay", scale, overlayFor="kit")
        ol = officials_layout(scale)
        add(ol, f"officials_{scale}x.png", img_sheet(ol), "officials", scale)
        pl = people_layout(scale)
        add(pl, f"people_{scale}x.png", people_sheet(pl), "people", scale)
        bl = ball_layout(scale)
        add(bl, f"ball_{scale}x.png", img_sheet(bl), "ball", scale)
    cl = crowd_layout()
    add(cl, "crowd_1x.png", img_sheet(cl), "crowd", 1)
    tl = touchline_layout()
    add(tl, "touchline_1x.png", img_sheet(tl), "touchline", 1)

    index = {
        "format": FORMAT,
        "generator": "tools/art/match/match_art.py (see tools/art/match/README.md)",
        "pixelsPerMetre": PPM,
        "camera": {"view": "V3 side-on", "heightScale": doll.KH, "groundScale": doll.KG},
        "directions": list(doll.DIRS),
        "regionFormat": "[x, y, w, h, pivotX, pivotY]; pivot = feet / ground contact, relative to the region",
        "sheets": sheets,
        "layouts": {name: {"size": [l.w, l.h], "animations": l.animations, "regions": l.regions}
                    for name, l in layouts.items()},
    }
    out["index.json"] = (dump_json(index) + "\n").encode("utf-8")
    return out


def same(path: Path, data: bytes) -> bool:
    if not path.exists():
        return False
    old = path.read_bytes()
    if path.suffix == ".png":
        return png_pixels(old) == png_pixels(data)
    return old.replace(b"\r\n", b"\n") == data


def main(argv: list[str]) -> int:
    check = "--check" in argv
    files = outputs()
    bad = 0
    for rel, data in files.items():
        target = OUT_DIR / rel
        if check:
            if not same(target, data):
                print(f"out of date: {target.relative_to(REPO).as_posix()}")
                bad += 1
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            if not same(target, data):
                target.write_bytes(data)
                print(f"wrote {target.relative_to(REPO).as_posix()}")
    produced = {(OUT_DIR / r).resolve() for r in files}
    for p in sorted(OUT_DIR.rglob("*")) if OUT_DIR.exists() else []:
        if p.is_file() and p.resolve() not in produced:
            print(f"{'stale' if check else 'not baked (delete?)'}: {p.relative_to(REPO).as_posix()}")
            bad += check
    if not check:
        print(f"{len(files)} files in {OUT_DIR.relative_to(REPO).as_posix()}")
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
