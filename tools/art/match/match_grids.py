"""Hand-authored and small procedural match art: officials (O2), crowd figures (CR1), touchline props (T1)
and the ball (BA2). Ported from the art prototype page (producer picks, D26); everything is our own work.

Every function returns frames as `Img` (RGB tuples or None for transparent) with a pivot at the feet /
ground contact point. Deterministic: integer arithmetic and the xorshift `Rng` only.
"""
from __future__ import annotations

import hashlib
import math

from match_doll import TAU, cos, sin

PAL = {
    "ink": (34, 30, 30), "soot": (52, 47, 46), "coal": (70, 64, 62), "slate": (84, 88, 96),
    "slate_hi": (112, 116, 122), "stone": (150, 142, 128), "brick": (128, 70, 54), "brick_dk": (96, 54, 44),
    "brick_hi": (152, 92, 70), "wood": (110, 80, 52), "wood_dk": (78, 56, 38), "wood_hi": (140, 106, 70),
    "paint": (212, 204, 182), "paint_dk": (176, 168, 148), "cream": (226, 218, 196), "mud": (104, 88, 62),
    "iron": (64, 66, 70), "red": (150, 52, 46), "navy": (44, 52, 86), "claret": (110, 40, 56),
    "black": (28, 26, 28), "glass": (196, 186, 140), "rope": (200, 190, 160), "horse": (98, 66, 44),
    "canvas_red": (180, 60, 50), "sponge": (214, 190, 110),
}
OUT = (30, 26, 30)
SKIN = ((214, 174, 140), (200, 160, 126), (224, 188, 156))
HAIR = ((52, 36, 28), (86, 58, 36), (30, 26, 24), (120, 84, 48), (60, 44, 34))


def rnd(x: float) -> int:
    """JavaScript Math.round (halves go up)."""
    return int(math.floor(x + 0.5))


def mul(c, f):
    return tuple(max(0, min(255, rnd(v * f))) for v in c)


def mix(a, b, t):
    return tuple(rnd(a[i] + (b[i] - a[i]) * t) for i in range(3))


class Rng:
    """xorshift32 seeded from a string (same as tools/art/ground_art.py)."""

    def __init__(self, seed: str):
        self.s = int.from_bytes(hashlib.sha256(seed.encode("utf-8")).digest()[:4], "big") or 1

    def next(self) -> int:
        s = self.s
        s ^= (s << 13) & 0xFFFFFFFF
        s ^= s >> 17
        s ^= (s << 5) & 0xFFFFFFFF
        self.s = s
        return s

    def int(self, lo: int, hi: int) -> int:
        return lo + self.next() % (hi - lo + 1)

    def pick(self, seq):
        return seq[self.next() % len(seq)]


def hash01(*vals) -> int:
    """Small integer hash of integers (0..255)."""
    h = 2166136261
    for v in vals:
        h ^= v & 0xFFFFFFFF
        h = (h * 16777619) & 0xFFFFFFFF
    h ^= h >> 13
    h = (h * 1274126177) & 0xFFFFFFFF
    return (h >> 16) & 0xFF


class Img:
    """RGB(A) pixels: px[i] is None (transparent), (r, g, b) or (r, g, b, a). Pivot = feet / ground point."""

    def __init__(self, w: int, h: int, pivot=(0, 0)):
        self.w, self.h = w, h
        self.px = [None] * (w * h)
        self.pivot = pivot

    def set(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[y * self.w + x] = c

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y * self.w + x]
        return None

    def rect(self, x0, y0, x1, y1, c):  # inclusive
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def outline(self, c=OUT):
        add = []
        for y in range(self.h):
            for x in range(self.w):
                if self.get(x, y) is None and any(self.get(x + dx, y + dy) is not None
                                                  for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1))):
                    add.append((x, y))
        for x, y in add:
            self.set(x, y, c)
        return self


def from_grid(rows, colour, pivot, pad=1) -> Img:
    """Grid of characters -> Img with a transparent border of `pad` (for the outline)."""
    w = max(len(r) for r in rows)
    img = Img(w + 2 * pad, len(rows) + 2 * pad, (pivot[0] + pad, pivot[1] + pad))
    for y, r in enumerate(rows):
        for x, ch in enumerate(r):
            if ch != ".":
                img.set(x + pad, y + pad, colour(ch, x, y))
    return img


def mirror_rows(rows):
    w = max(len(r) for r in rows)
    return [r.ljust(w, ".")[::-1] for r in rows]


# ======================================================================== O2: umpires and referee
# 2x set (10 px/m): the prototype's hand grids, trimmed by four rows (head, torso, two leg rows) so a
# man in a bowler stands ~22 px, in scale with the 10 px/m doll.
HS = [".hhhh.", "hhhhhh", "hssssh", "sesses", ".smms.", "..ss.."]
HN = [".hhhh.", "hhhhhh", "hhhhhh", "shhhhs", ".hhhh.", "..SS.."]
HE = [".hhhh.", "hhhhhh", "hhssss", "hhsses", ".hsmm.", "..ss.."]
HAT2 = ["..xxxx..", "..xxxx..", "xxxxxxxx"]
TF = {
    "down": (0, 0, ["..aaLLaa..", ".aaaaaaaa.", "AaaaaaaaaA", "AaaaaaaaaA", "AaaaaaaaaA", "s.aaaaaa.s"]),
    "swA": (0, 0, ["..aaLLaa..", ".aaaaaaaa.", "AaaaaaaaaA", "saaaaaaaaA", ".aaaaaaaaA", "..aaaaaa.s"]),
    "out": (-2, 0, ["....aaLLaa....", "sAAaaaaaaaaAAs", "...aaaaaaaa...", "...aaaaaaaa...", "...aaaaaaaa...",
                    "....aaaaaa...."]),
    "flag": (0, -4, ["........ts", "........tA", "........tA", "........tA", "..aaLLaa.A", ".aaaaaaaaA",
                     "AaaaaaaaaA", "Aaaaaaaaa.", "saaaaaaaa.", "..aaaaaa.."]),
}
TF["swB"] = (0, 0, mirror_rows(TF["swA"][2]))
TS = {
    "down": (0, 0, ["..aaaL..", ".aaaaaa.", ".aaAaaa.", ".aaAaaa.", ".aaAaaa.", "..asaa.."]),
    "swA": (0, 0, ["..aaaL..", ".aaaaaa.", ".aaaAaa.", ".aaaaAa.", ".aaaaaAs", "..aaaa.."]),
    "swB": (0, 0, ["..aaaL..", ".aaaaaa.", ".aAaaaa.", "Aaaaaaa.", "saaaaaa.", "..aaaa.."]),
    "out": (-1, 0, ["...aaaL...", "sAaaaaaaAs", "..aaaaaa..", "..aaaaaa..", "..aaaaaa..", "...aaaa..."]),
    "flag": (0, -4, ["......ts", "......tA", "......tA", "......tA", "..aaaLA.", ".aaaaaA.", ".aaaaaa.",
                     ".aaaaaa.", ".aaaaaa.", "..aaaa.."]),
}
LF = {
    "stand": ["kkkkkkkk", "kkkKKkkk", "kkk..kkk", ".kk..kk.", ".kk..kk.", ".ff..ff.", "fff..fff"],
    "strA": ["kkkkkkkk", "kkkKKkkk", "kkk..kkk", ".kk..kk.", ".kk..ff.", ".ff.....", "fff....."],
    "pass": ["kkkkkkkk", "kkkKKkkk", ".kkk.kk.", "..kk.kk.", "..kk.kk.", "..ff.ff.", ".fff...."],
}
LF["strB"] = mirror_rows(LF["strA"])
LS = {
    "stand": ["..kkkk..", "..kkKk..", "..kkkk..", "...kk...", "...kk...", "...ff...", "...fff.."],
    "strA": ["..kkkk..", ".kkkKkk.", "kkk..kkk", "kk....kk", "ff....kk", "......ff", ".....fff"],
    "pass": ["..kkkk..", "..kkKk..", "..kkkk..", "..kkk...", "..f.kk..", "....ff..", "....fff."],
}
LS["strB"] = LS["strA"]
BW2, BH2, BY2 = 16, 24, 4

# 1x set (5 px/m): new grids in the same style, ~11 px with the bowler
HAT1 = [".xxx.", "xxxxx"]
H1 = {"S": [".sss.", ".sms."], "N": [".hhh.", ".hhh."], "E": [".hss.", ".hsm."]}
T1 = {
    "S": {"down": ["AaLaA", "AaaaA", "saaas", ".aaa."], "swA": ["AaLaA", "AaaaA", "saaaA", ".aaas"],
          "flag": ["AaLaA", "Aaaa.", "saaa.", ".aaa."], "out": ["AaLaAAs", "Aaaa...", "saaa...", ".aaa..."]},
    "N": {"down": ["AaaaA", "AaaaA", "saaas", ".aaa."], "swA": ["AaaaA", "AaaaA", "saaaA", ".aaas"],
          "flag": ["AaaaA", "Aaaa.", "saaa.", ".aaa."], "out": ["AaaaAAs", "Aaaa...", "saaa...", ".aaa..."]},
    "E": {"down": [".aaL.", ".aAa.", ".asa.", ".aa.."], "swA": [".aaL.", ".aaA.", ".aaas", ".aa.."],
          "swB": [".aaL.", ".Aaa.", "saaa.", ".aa.."], "flag": [".aaLA", ".aaa.", ".aaa.", ".aa.."],
          "out": [".aaLAAs", ".aAa...", ".asa...", ".aa...."]},
}
for _d in ("S", "N"):
    T1[_d]["swB"] = mirror_rows(T1[_d]["swA"])
L1 = {
    "S": {"stand": [".k.k.", ".k.k.", ".f.f."], "strA": [".k.k.", ".k.f.", ".f..."], "pass": [".k.k.", ".k.k.", ".f.f."]},
    "E": {"stand": ["..k..", "..k..", "..ff."], "strA": [".k.k.", "k...k.", "f...ff"], "pass": ["..k..", "..k..", "..ff."]},
}
L1["S"]["strB"] = mirror_rows(L1["S"]["strA"])
L1["E"]["strB"] = L1["E"]["strA"]
L1["N"] = L1["S"]
BW1, BH1, BY1 = 9, 14, 3

OFFICIAL_ANIMS = {  # (torso, legs, upper-body drop)
    "idle": [("down", "stand", 0), ("down", "stand", 1)],
    "walk": [("swA", "strA", 0), ("down", "pass", 0), ("swB", "strB", 0), ("down", "pass", 0)],
    "flag": [("down", "stand", 0), ("flag", "stand", 0)],
    "signal": [("down", "stand", 0), ("out", "stand", 0)],
}
OFFICIALS = {  # looks; flags are a convention (white / red), not sourced
    "umpire-a": {"jacket": (36, 36, 42), "trousers": (72, 68, 64), "hat": (28, 26, 28), "flag": (226, 218, 196),
                 "anims": ("idle", "walk", "flag")},
    "umpire-b": {"jacket": (54, 42, 36), "trousers": (60, 60, 66), "hat": (28, 26, 28), "flag": (168, 44, 40),
                 "anims": ("idle", "walk", "flag")},
    "referee": {"jacket": (28, 30, 38), "trousers": (48, 48, 54), "hat": (22, 20, 22), "flag": None,
                "anims": ("idle", "walk", "signal")},
}
DIR4 = ("S", "E", "N", "W")


def _official_colour(look, d):
    def colour(ch, x, y):
        j, t = look["jacket"], look["trousers"]
        return {"a": j, "A": mul(j, 0.78), "L": j if d == "N" else PAL["cream"], "k": t, "K": mul(t, 0.8),
                "f": (58, 40, 28), "s": look["skin"], "S": mul(look["skin"], 0.85), "e": (40, 28, 26),
                "h": look["hair"], "m": look["hair"], "x": look["hat"], "t": PAL["wood_hi"],
                "F": look["flag"] or PAL["cream"]}[ch]
    return colour


def official_frame(name: str, anim: str, f: int, d: str, scale: int) -> Img:
    """One O2 frame. d in S/E/N/W (W mirrors E)."""
    look = dict(OFFICIALS[name])
    r = Rng("official:" + name)
    look["skin"], look["hair"] = r.pick(SKIN), r.pick(HAIR)
    torso, legs, drop = OFFICIAL_ANIMS[anim][f]
    dd = "E" if d == "W" else d
    side = dd == "E"
    if scale == 2:
        BW, BH, BY = BW2, BH2, BY2
    else:
        BW, BH, BY = BW1, BH1, BY1
    grid = [["."] * BW for _ in range(BH)]

    def put(rows, x, y):
        for j, row in enumerate(rows):
            for i, ch in enumerate(row):
                if ch != "." and 0 <= x + i < BW and 0 <= y + j < BH:
                    grid[y + j][x + i] = ch
    if scale == 2:
        put((LS if side else LF)[legs], 4, BY + 13)
        dx, dy, rows = (TS if side else TF)[torso]
        put(rows, (4 if side else 3) + dx, BY + 7 + dy + drop)
        put(HS if dd == "S" else HN if dd == "N" else HE, 5, BY + 1 + drop)
        put(HAT2, 4, BY - 1 + drop)
    else:
        put(L1[dd][legs], 2, BY + 8)
        put(T1[dd][torso], 2, BY + 4 + drop)
        put(H1[dd], 2, BY + 2 + drop)
        put(HAT1, 2, BY + drop)
        if torso == "flag":  # raised arm beside the head
            put(["s", "A", "A"], 7, BY + 1)
    # the umpire's flag on its stick
    if look["flag"] is not None:
        if torso == "flag":  # stick up from the raised hand (or the prototype's stick), cloth at the top
            sticks = [(x, y) for y in range(BH) for x in range(BW) if grid[y][x] == "t"] or \
                [(x, y) for y in range(BH) for x in range(BW) if grid[y][x] == "s"]
            hx, hy = min(sticks, key=lambda p: (p[1], -p[0]))
            n = 3 if scale == 1 else 5
            for k in range(1, n + 1):
                if hy - k >= 0:
                    grid[hy - k][hx] = "t"
            cw, ch = (2, 2) if scale == 1 else (4, 3)
            for yy in range(ch):
                for xx in range(cw):
                    X, Y = hx + 1 + xx, hy - n + yy
                    if 0 <= X < BW and 0 <= Y < BH:
                        grid[Y][X] = "F"
        else:  # furled flag carried at the side
            hands = [(x, y) for y in range(BH) for x in range(BW) if grid[y][x] == "s" and y > BY + 4]
            if hands:
                hx, hy = max(hands, key=lambda p: (p[0], p[1]))
                n = 2 if scale == 1 else 4
                for k in range(1, n + 1):
                    if hy + k < BH:
                        grid[hy + k][hx] = "t"
                if scale == 2 and hy + 2 < BH and hx + 1 < BW:
                    grid[hy + 2][hx + 1] = "F"
    rows = ["".join(r) for r in grid]
    if d == "W":
        rows = mirror_rows(rows)
    pivot = (BW // 2, BH - 1)
    return from_grid(rows, _official_colour(look, dd), pivot).outline()


# ======================================================================== CR1: crowd figures (1x)
CROWD_HATS = ("bowler", "flatcap", "topper", "bonnet", "billycock", "bare")
CROWD_MOODS = {"calm": 2, "cheer": 3, "angry": 2, "encroach": 2}
HAT_COL = ((28, 26, 28), (60, 52, 44), (24, 22, 24), (90, 74, 56), (40, 40, 46), None)
COATS = (PAL["black"], PAL["soot"], PAL["navy"], PAL["wood_dk"], PAL["coal"], PAL["slate"], (90, 70, 50), (70, 40, 40))
DRESSES = ((70, 40, 40), PAL["navy"], (90, 70, 50), PAL["claret"])
CROWD_VARIANTS = [(h, tall) for tall in (True, False) for h in range(6)]  # 12 figures


def crowd_look(v: int) -> dict:
    h, tall = CROWD_VARIANTS[v]
    r = Rng(f"crowd:{v}")
    woman = CROWD_HATS[h] == "bonnet"
    return {"hat": h, "tall": tall, "woman": woman, "coat": r.pick(DRESSES if woman else COATS),
            "skin": r.pick(SKIN), "hair": r.pick(HAIR), "legs": (40, 36, 36)}


def crowd_frame(v: int, mood: str, f: int) -> Img:
    """A spectator ~8 px tall (5 px/m): hat, face, coat, legs. Moods: calm, cheer, angry, encroach."""
    lk = crowd_look(v)
    W, H = 9, 14
    img = Img(W, H, (4, 13))
    X = 4
    bob = 1 if (mood == "calm" and f == 1) or (mood == "cheer" and f == 1) else 0
    jit = 1 if mood == "angry" and f == 1 else 0
    X += jit
    coat_rows = 4 if lk["tall"] else 3
    Y = 12 - coat_rows - 2 - bob  # face top
    face = mix(lk["skin"], (200, 70, 60), 0.45) if mood == "angry" else lk["skin"]
    # legs (or a long dress)
    if lk["woman"]:
        img.rect(X - 1, Y + 2, X + 1, 13 - bob, lk["coat"])
    else:
        if mood == "encroach" and f == 0:
            img.set(X - 1, 12 - bob, lk["legs"]); img.set(X - 2, 13 - bob, lk["legs"])
            img.set(X + 1, 12 - bob, lk["legs"]); img.set(X + 2, 13 - bob, lk["legs"])
        else:
            for yy in (12, 13):
                img.set(X - 1, yy - bob, lk["legs"])
                img.set(X + 1, yy - bob, lk["legs"])
        img.rect(X - 1, Y + 2, X + 1, Y + 1 + coat_rows, lk["coat"])
    img.set(X, Y, face)
    img.set(X, Y + 1, face)
    arms = mood == "cheer" or (mood == "angry")
    if arms:
        img.set(X - 2, Y - 1, face); img.set(X - 2, Y, lk["coat"]); img.set(X - 2, Y + 1, lk["coat"])
        if mood == "cheer":
            img.set(X + 2, Y - 1, face); img.set(X + 2, Y, lk["coat"]); img.set(X + 2, Y + 1, lk["coat"])
    if mood == "encroach":  # leaning forward, one arm out
        img.set(X + 2, Y + 2, lk["coat"]); img.set(X + 3, Y + 2, face)
    toss = mood == "cheer" and f == 2 and CROWD_HATS[lk["hat"]] not in ("bonnet", "bare")
    hy = Y - 4 if toss else Y - 1
    if toss:
        img.set(X, Y - 1, lk["hair"])
    _hat(img, X, hy, lk)
    return img


def _hat(img, X, Y, lk):
    kind = CROWD_HATS[lk["hat"]]
    c = HAT_COL[lk["hat"]]
    if kind == "bare":
        img.set(X, Y, lk["hair"])
        return
    img.rect(X - 1, Y, X + 1, Y, c)
    if kind == "bowler":
        img.set(X, Y - 1, c)
    elif kind == "topper":
        img.set(X, Y - 1, c); img.set(X, Y - 2, c)
    elif kind == "bonnet":
        img.set(X - 1, Y + 1, c)
    elif kind == "billycock":
        img.rect(X - 1, Y - 1, X + 1, Y - 1, c)


# ======================================================================== BA2: ball
BALL_WET = ("dry", "damp", "soaked", "muddy")
BALL_SIZES = {1: {"ground": 2, "high": 3}, 2: {"ground": 4, "high": 6}}
BALL_SPIN = 8


def _ramp(c):
    return (mix(mul(c, 0.64), (38, 42, 80), 0.2), c, mix(c, (255, 246, 222), 0.24))


def ball_frame(scale: int, size: str, wet: int, f: int) -> Img:
    """Leather ball with a lace that turns with spin; darker and heavier-looking when wet (BA2)."""
    ri = BALL_SIZES[scale][size]
    base = mix((152, 98, 54), (66, 44, 30), wet / 3) if wet else (152, 98, 54)
    R = _ramp(base)
    img = Img(2 * ri + 1, 2 * ri + 1, (ri, 2 * ri))
    for j in range(-ri, ri + 1):
        for i in range(-ri, ri + 1):
            if i * i + j * j > ri * ri + ri * 0.6:
                continue
            l = -(i + j) / (ri * 1.6)
            c = R[2] if l > 0.35 else (R[1] if l > -0.3 else R[0])
            if wet == 3 and hash01(i, j, f, ri) < 70:
                c = PAL["mud"] if l > -0.3 else mul(PAL["mud"], 0.75)
            img.set(ri + i, ri + j, c)
    a = f / BALL_SPIN * TAU
    if cos(a) > -0.3:
        lx = rnd(cos(a) * (ri - 1) * 0.8)
        ly = rnd(sin(a * 0.5) * 0.5)
        lace = PAL["cream"] if wet < 2 else (180, 170, 150)
        lh = 1 if ri < 3 else (2 if ri < 4 else ri // 2 + 1)
        lw = 1 if ri < 4 else 2
        for yy in range(lh):
            for xx in range(lw):
                X, Y = ri + lx + xx - (lw // 2), ri + ly - 1 + yy - (lh // 3)
                if img.get(X, Y) is not None:
                    img.set(X, Y, lace)
    return img


# ======================================================================== T1: touchline props (1x, 5 px/m)
def _planks(img, x0, y0, x1, y1, c, dk, every=3):
    img.rect(x0, y0, x1, y1, c)
    for x in range(x0, x1 + 1, every):
        img.rect(x, y0, x, y1, dk)


def _roof(img, x0, x1, base, rise, c, hi):
    """Pitched roof seen from the front: column heights fall off from the middle."""
    mid = (x0 + x1) / 2
    half = (x1 - x0) / 2 or 1
    for x in range(x0, x1 + 1):
        h = rnd(rise * (1 - abs(x - mid) / half)) + 2
        img.rect(x, base - h, x, base, c)
        img.set(x, base - h, hi)


def prop_pavilion(f: int) -> Img:
    """Wooden pavilion, painted, with a verandah, balcony rail, glazed windows and a pennant (2 frames)."""
    W, H = 68, 50
    img = Img(W, H, (W // 2, H - 1))
    g = H - 1
    _roof(img, 0, W - 1, g - 26, 11, PAL["brick"], PAL["brick_hi"])
    _planks(img, 2, g - 26, W - 3, g, PAL["paint"], PAL["paint_dk"], 4)
    img.rect(2, g - 13, W - 3, g - 12, PAL["wood"])           # balcony rail
    for x in range(4, W - 4, 4):
        img.rect(x, g - 15, x, g - 13, PAL["wood_dk"])        # balusters
    for x in range(6, W - 8, 9):
        img.rect(x, g - 23, x + 3, g - 18, PAL["glass"])      # upper windows
        img.rect(x, g - 23, x + 3, g - 23, PAL["wood_dk"])
    for x in range(3, W - 2, 8):
        img.rect(x, g - 11, x, g, PAL["wood_dk"])             # verandah posts
    img.rect(W // 2 - 3, g - 8, W // 2 + 2, g, (40, 32, 26))  # doors
    img.rect(W // 2 - 6, g, W // 2 + 5, g, PAL["stone"])      # step
    px = W // 2
    img.rect(px, 0, px, g - 37, (60, 50, 40))                 # flagpole
    wave = f % 2
    img.rect(px + 1, 0 + wave, px + 4, 1 + wave, PAL["red"])
    if wave:
        img.set(px + 5, 2, PAL["red"])
    return img


def prop_shed(f: int) -> Img:
    """Wooden dressing shed with a slate roof."""
    W, H = 26, 19
    img = Img(W, H, (W // 2, H - 1))
    g = H - 1
    _planks(img, 1, g - 13, W - 2, g, PAL["wood"], PAL["wood_dk"], 3)
    img.rect(0, g - 16, W - 1, g - 14, PAL["slate"])
    img.rect(0, g - 16, W - 1, g - 16, PAL["slate_hi"])
    img.rect(W // 2 - 2, g - 8, W // 2 + 1, g, (40, 32, 26))
    img.rect(4, g - 10, 6, g - 8, PAL["glass"])
    return img


def prop_tent(f: int) -> Img:
    """Refreshment tent, striped canvas, open front."""
    W, H = 24, 18
    img = Img(W, H, (W // 2, H - 1))
    g = H - 1
    for x in range(W):
        h = rnd(14 - abs(x - 11.5) * 0.75)
        c = PAL["canvas_red"] if (x // 3) % 2 == 0 else PAL["cream"]
        img.rect(x, g - h, x, g, c)
    img.rect(9, g - 6, 14, g, (60, 50, 40))
    img.rect(11, 0, 12, 2 + (f % 2), PAL["canvas_red"])
    return img


def prop_carriage(f: int) -> Img:
    """A carriage with a horse (head bob, 2 frames)."""
    W, H = 32, 14
    img = Img(W, H, (W // 2, H - 1))
    g = H - 1
    img.rect(1, g - 10, 16, g - 4, (40, 36, 40))
    img.rect(2, g - 9, 15, g - 6, PAL["claret"])
    img.rect(5, g - 12, 12, g - 11, (40, 36, 40))
    for wx in (4, 13):
        img.rect(wx - 2, g - 4, wx + 2, g - 4, (30, 26, 26)); img.rect(wx - 2, g, wx + 2, g, (30, 26, 26))
        img.rect(wx - 2, g - 3, wx - 2, g - 1, (30, 26, 26)); img.rect(wx + 2, g - 3, wx + 2, g - 1, (30, 26, 26))
        img.set(wx, g - 2, (30, 26, 26))
    img.rect(17, g - 7, 19, g - 7, PAL["wood_dk"])  # shafts
    hx, bob = 19, f % 2
    img.rect(hx, g - 8, hx + 9, g - 5, PAL["horse"])
    img.rect(hx + 9, g - 11 - bob, hx + 11, g - 8 - bob, PAL["horse"])
    img.set(hx + 11, g - 8 - bob, PAL["black"])
    for lx in (hx + 1, hx + 3, hx + 7, hx + 9):
        img.rect(lx, g - 4, lx, g, PAL["horse"])
    img.rect(hx - 1, g - 8, hx - 1, g - 6, PAL["black"])  # tail
    return img


def prop_rope(f: int) -> Img:
    """Rope on stakes, one tile (repeat every 16 px)."""
    img = Img(16, 6, (0, 5))
    img.rect(0, 0, 0, 5, PAL["wood"])
    for x in range(1, 16):
        sag = 1 if 4 <= x <= 12 else 0
        img.set(x, 1 + sag, PAL["rope"])
    return img


def prop_railings(f: int) -> Img:
    """Painted wooden railings, one tile (repeat every 12 px)."""
    img = Img(12, 7, (0, 6))
    for x in range(0, 12, 3):
        img.rect(x, 0, x, 6, PAL["paint"])
    img.rect(0, 1, 11, 1, PAL["paint_dk"])
    img.rect(0, 4, 11, 4, PAL["paint_dk"])
    return img


def prop_chair(f: int) -> Img:
    img = Img(4, 6, (1, 5))
    img.rect(0, 0, 0, 5, PAL["wood_dk"]); img.rect(0, 3, 3, 3, PAL["wood_dk"]); img.rect(3, 3, 3, 5, PAL["wood_dk"])
    return img


def prop_press_table(f: int) -> Img:
    """Press table with papers and an ink pot."""
    img = Img(22, 7, (11, 6))
    img.rect(0, 2, 21, 3, PAL["wood"])
    img.rect(1, 4, 1, 6, PAL["wood_dk"]); img.rect(20, 4, 20, 6, PAL["wood_dk"])
    img.rect(3, 1, 5, 1, PAL["cream"]); img.rect(12, 1, 14, 1, PAL["cream"]); img.set(9, 1, PAL["ink"])
    img.set(9, 0, PAL["ink"])
    return img


def prop_bucket(f: int) -> Img:
    """The trainer's bucket and sponge, set down."""
    img = Img(4, 5, (1, 4))
    img.rect(0, 2, 2, 4, PAL["iron"]); img.rect(0, 2, 2, 2, mul(PAL["iron"], 1.3))
    img.rect(1, 1, 2, 1, PAL["sponge"])
    return img


def prop_telegraph(f: int) -> Img:
    """Telegraph pole by the press table (results went out by wire)."""
    img = Img(7, 30, (3, 29))
    img.rect(3, 0, 3, 29, PAL["wood_dk"])
    img.rect(0, 2, 6, 2, PAL["wood_dk"])
    for x in (0, 2, 4, 6):
        img.set(x, 1, PAL["cream"])
    return img


PROPS = {  # name -> (draw, frames)
    "pavilion": (prop_pavilion, 2), "shed": (prop_shed, 1), "tent": (prop_tent, 2),
    "carriage": (prop_carriage, 2), "rope": (prop_rope, 1), "railings": (prop_railings, 1),
    "chair": (prop_chair, 1), "press-table": (prop_press_table, 1), "bucket": (prop_bucket, 1),
    "telegraph-pole": (prop_telegraph, 1),
}
