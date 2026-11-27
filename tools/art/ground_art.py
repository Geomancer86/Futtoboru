"""Deterministic pixel-art illustrations of 1888-89 football grounds (own art, code-generated).

Each ground has a descriptor JSON in tools/art/grounds/<id>.json (stands, slope, surroundings, season).
The renderer draws a 320x180 picture plus a 64x36 thumbnail and writes them as PNG with the standard
library only (no PIL), so it runs anywhere Python 3 runs, including CI.

Determinism: no `random`, no trigonometry; all noise comes from a hash of the ground id and integer maths,
so the same descriptor always gives the same pixels (and, with the same zlib, the same PNG bytes).

    python tools/art/ground_art.py            # render every descriptor
    python tools/art/ground_art.py deepdale   # render one
    python tools/art/ground_art.py --check    # fail if a committed PNG no longer matches its descriptor
"""
from __future__ import annotations

import hashlib
import json
import struct
import sys
import zlib
from pathlib import Path

HERE = Path(__file__).resolve().parent
DESCRIPTORS = HERE / "grounds"
REPO = HERE.parent.parent
OUT_DIR = REPO / "futtoboru-core" / "src" / "main" / "resources" / "art" / "grounds"

W, H = 320, 180
THUMB = 5  # 320/5 x 180/5 = 64x36

# ---------------------------------------------------------------- palette (muted, Victorian)
PAL = {
    "ink": (34, 30, 30), "soot": (52, 47, 46), "coal": (70, 64, 62), "slate": (84, 88, 96),
    "slate_hi": (112, 116, 122), "stone": (150, 142, 128), "stone_hi": (178, 170, 152),
    "brick": (128, 70, 54), "brick_dk": (96, 54, 44), "brick_hi": (152, 92, 70),
    "wood": (110, 80, 52), "wood_dk": (78, 56, 38), "wood_hi": (140, 106, 70),
    "paint": (212, 204, 182), "paint_dk": (176, 168, 148), "cream": (226, 218, 196),
    "grass": (92, 120, 66), "grass_hi": (110, 138, 76), "grass_dk": (74, 100, 56), "mud": (104, 88, 62),
    "hill": (112, 124, 104), "hill_far": (140, 148, 134), "tree": (60, 82, 52), "tree_hi": (78, 102, 62),
    "twig": (86, 70, 58), "skin": (206, 168, 136), "smoke": (168, 164, 160), "smoke_dk": (128, 124, 122),
    "iron": (64, 66, 70), "red": (150, 52, 46), "navy": (44, 52, 86), "white": (232, 228, 214),
    "blue": (74, 98, 150), "claret": (110, 40, 56), "black": (28, 26, 28), "gold": (186, 150, 70),
    "green": (54, 104, 70), "skyblue": (126, 160, 196), "amber": (182, 126, 62), "glass": (196, 186, 140),
}
SKIES = {  # (top, horizon) per season; cloud colour
    "autumn": ((150, 160, 170), (214, 200, 172), (226, 220, 206)),
    "winter": ((136, 144, 154), (190, 190, 186), (206, 206, 204)),
    "spring": ((132, 164, 196), (206, 214, 210), (236, 236, 230)),
}
GRASS = {
    "autumn": ("grass", "grass_hi", "grass_dk"),
    "winter": ("grass_dk", "grass", (66, 86, 54)),
    "spring": ("grass_hi", (124, 150, 84), "grass"),
}
BAYER4 = ((0, 8, 2, 10), (12, 4, 14, 6), (3, 11, 1, 9), (15, 7, 13, 5))

HORIZON = 100      # where hills meet the town
FAR_Y = 112        # far touchline (centre), before slope
NEAR_Y = 166       # near touchline
FAR_L, FAR_R = 46, 274


def col(c):
    """A palette name, '#rrggbb' or an (r, g, b) tuple -> (r, g, b)."""
    if isinstance(c, (tuple, list)):
        return tuple(int(v) for v in c)
    if isinstance(c, str) and c.startswith("#") and len(c) == 7:
        return tuple(int(c[i:i + 2], 16) for i in (1, 3, 5))
    return PAL[c]


def mix(a, b, num, den):
    a, b = col(a), col(b)
    return tuple((a[i] * (den - num) + b[i] * num) // den for i in range(3))


class Rng:
    """xorshift32 seeded from a string; integer-only so it is identical everywhere."""

    def __init__(self, seed: str):
        self.s = int.from_bytes(hashlib.sha256(seed.encode("utf-8")).digest()[:4], "big") or 1

    def next(self) -> int:
        s = self.s
        s ^= (s << 13) & 0xFFFFFFFF
        s ^= s >> 17
        s ^= (s << 5) & 0xFFFFFFFF
        self.s = s
        return s

    def int(self, lo: int, hi: int) -> int:  # inclusive
        return lo + self.next() % (hi - lo + 1)

    def chance(self, num: int, den: int) -> bool:
        return self.next() % den < num


class Canvas:
    def __init__(self, w: int, h: int):
        self.w, self.h = w, h
        self.px = bytearray(w * h * 3)

    def set(self, x, y, c):
        x, y = int(x), int(y)
        if 0 <= x < self.w and 0 <= y < self.h:
            i = (y * self.w + x) * 3
            self.px[i:i + 3] = bytes(col(c))

    def get(self, x, y):
        i = (y * self.w + x) * 3
        return tuple(self.px[i:i + 3])

    def rect(self, x0, y0, x1, y1, c):  # inclusive corners
        c = col(c)
        for y in range(max(0, int(y0)), min(self.h, int(y1) + 1)):
            for x in range(max(0, int(x0)), min(self.w, int(x1) + 1)):
                self.set(x, y, c)

    def hline(self, x0, x1, y, c):
        self.rect(min(x0, x1), y, max(x0, x1), y, c)

    def vline(self, x, y0, y1, c):
        self.rect(x, min(y0, y1), x, max(y0, y1), c)

    def line(self, x0, y0, x1, y1, c):
        x0, y0, x1, y1 = int(x0), int(y0), int(x1), int(y1)
        dx, dy = abs(x1 - x0), -abs(y1 - y0)
        sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
        err = dx + dy
        while True:
            self.set(x0, y0, c)
            if x0 == x1 and y0 == y1:
                return
            e2 = 2 * err
            if e2 >= dy:
                err += dy
                x0 += sx
            if e2 <= dx:
                err += dx
                y0 += sy

    def blob(self, cx, cy, r, c):  # filled disc, integer test
        for y in range(-r, r + 1):
            for x in range(-r, r + 1):
                if x * x + y * y <= r * r + r // 2:
                    self.set(cx + x, cy + y, c)

    def haze(self, y0, y1, toward, num, den):
        """Blend a band toward a colour (mist / distance)."""
        for y in range(max(0, y0), min(self.h, y1 + 1)):
            for x in range(self.w):
                self.set(x, y, mix(self.get(x, y), toward, num, den))


# ---------------------------------------------------------------- scene geometry
class Scene:
    def __init__(self, d: dict):
        self.d = d
        self.rng = Rng(d["id"])
        self.season = d.get("season", "autumn")
        slope = d.get("slope", {}) or {}
        amt = int(slope.get("amount", 0))
        # "left-down": the left end lies lower (further down the picture) than the right end.
        sign = {"left-down": 1, "right-down": -1}.get(slope.get("direction", "none"), 0)
        self.far_yl = FAR_Y + sign * amt
        self.far_yr = FAR_Y - sign * amt

    def far_x(self, f: float) -> int:
        return int(FAR_L + f * (FAR_R - FAR_L))

    def far_y(self, x: int) -> int:
        return int(self.far_yl + (self.far_yr - self.far_yl) * (x - FAR_L) / (FAR_R - FAR_L))

    def edges(self, y):
        """Left/right pitch x at screen row y (trapezoid: far edge narrow, near edge full width)."""
        t = (y - FAR_Y) / (NEAR_Y - FAR_Y)
        return FAR_L - t * (FAR_L - 6), FAR_R + t * (W - 6 - FAR_R)


def draw_sky(cv: Canvas, sc: Scene):
    top, hor, cloud = SKIES[sc.season]
    weather = sc.d.get("weather", "clear")
    if weather == "overcast":
        top, hor = mix(top, cloud, 2, 3), mix(hor, cloud, 1, 2)
    for y in range(0, FAR_Y + 10):
        # 16-level ordered dither between two adjacent gradient steps
        level = y * 64 // (FAR_Y + 10)
        a, b = mix(top, hor, level // 4, 16), mix(top, hor, min(16, level // 4 + 1), 16)
        for x in range(W):
            cv.set(x, y, b if BAYER4[y & 3][x & 3] < (level % 4) * 4 else a)
    n = {"clear": 3, "overcast": 7, "mist": 4}.get(weather, 3)
    for _ in range(n):
        cx, cy, ln = sc.rng.int(0, W), sc.rng.int(8, 52), sc.rng.int(20, 60)
        for k in range(ln // 6):
            cv.blob(cx + k * 6 - ln // 2, cy + sc.rng.int(-2, 2), sc.rng.int(3, 6), cloud)
        cv.rect(cx - ln // 2, cy + 2, cx + ln // 2, cy + 4, cloud)


def draw_hills(cv: Canvas, sc: Scene):
    hills = sc.d.get("hills")
    if not hills:
        return
    height = int(hills.get("height", 14))
    for layer, colour, hh in ((0, "hill_far", height), (1, "hill", height * 2 // 3)):
        y = HORIZON - hh
        drift = 0
        for x in range(W):
            if x % 8 == 0:
                drift = max(-hh // 2, min(hh // 2, drift + sc.rng.int(-2, 2)))
            top = y + drift + (layer * 4)
            cv.vline(x, top, HORIZON + 4, colour)


def smoke(cv: Canvas, sc: Scene, x, y, wind):
    for k in range(7):
        r = 2 + k // 2
        cv.blob(x + wind * k * 4 + sc.rng.int(-1, 1), y - k * 3, r, "smoke_dk" if k < 2 else "smoke")


def draw_ground_band(cv: Canvas, sc: Scene):
    """The flat land between the town and the far touchline (paths, cinders, rough grass)."""
    g = col(GRASS[sc.season][2])
    for y in range(HORIZON + 4, NEAR_Y + 1):
        for x in range(W):
            cv.set(x, y, mix(g, "mud", 1, 3) if (x * 7 + y * 13) % 11 else mix(g, "mud", 2, 3))


def draw_skyline(cv: Canvas, sc: Scene):
    wind = int(sc.d.get("wind", 1))
    base = HORIZON + 6
    for item in sc.d.get("skyline", []):
        t = item["type"]
        x = int(item.get("x", 0.5) * W)
        h = int(item.get("height", 30))
        if t == "chimney":
            cv.rect(x, base - h, x + 3, base, "brick_dk")
            cv.rect(x - 1, base - h, x + 4, base - h + 1, "soot")
            if item.get("smoke", True):
                smoke(cv, sc, x + 1, base - h - 2, wind)
        elif t == "mill":
            w = int(item.get("width", 40))
            cv.rect(x, base - h, x + w, base, "brick_dk")
            for wy in range(base - h + 3, base - 2, 5):
                for wx in range(x + 2, x + w - 1, 4):
                    cv.rect(wx, wy, wx + 1, wy + 2, "glass" if sc.rng.chance(1, 5) else "soot")
            cv.rect(x - 1, base - h - 1, x + w + 1, base - h, "slate")
            if item.get("chimney", True):
                cx = x + w + 3
                cv.rect(cx, base - h - 22, cx + 3, base, "brick")
                smoke(cv, sc, cx + 1, base - h - 24, wind)
        elif t == "spire":
            cv.rect(x - 4, base - h // 3, x + 4, base, "stone")
            cv.rect(x - 2, base - h // 2, x + 2, base - h // 3, "stone")
            for k in range(h // 2):
                half = 2 - (k * 3) // max(1, h // 2)
                cv.hline(x - max(0, half), x + max(0, half), base - h // 2 - k, "slate")
        elif t == "tower":
            cv.rect(x - 4, base - h, x + 4, base, "stone")
            for k in range(-4, 5, 2):
                cv.set(x + k, base - h - 1, "stone")
        elif t == "kiln":  # bottle oven
            for k in range(h):
                t = k * 100 // max(1, h)  # bottle profile: foot, belly, shoulder, neck
                half = 6 if t < 12 else 7 if t < 50 else 7 - (t - 50) * 5 // 30 if t < 80 else 2
                cv.hline(x - half, x + half, base - k, "brick" if k % 5 else "brick_dk")
            cv.hline(x - 3, x + 3, base - h, "brick_dk")
            smoke(cv, sc, x, base - h - 2, wind)
        elif t == "gasholder":
            w = int(item.get("width", 30))
            cv.rect(x, base - h + 6, x + w, base, "iron")
            for k in range(x, x + w + 1, 6):
                cv.vline(k, base - h, base, "slate")
            cv.hline(x, x + w, base - h, "slate")
        elif t == "houses":
            w = int(item.get("width", 60))
            hx = x
            while hx < x + w:
                hw = sc.rng.int(8, 12)
                hh = h + sc.rng.int(-2, 2)
                cv.rect(hx, base - hh, hx + hw, base, "brick" if sc.rng.chance(1, 2) else "brick_dk")
                for k in range(hw // 2):
                    cv.hline(hx + k, hx + hw - k, base - hh - k // 1 - 1, "slate")
                cv.rect(hx + hw - 3, base - hh - 6, hx + hw - 2, base - hh - 1, "brick_dk")
                hx += hw + 1
        elif t == "trees":
            w = int(item.get("width", 40))
            draw_trees(cv, sc, x, x + w, base, h)
        elif t == "railway":
            y = base - int(item.get("embankment", 6))
            cv.rect(0, y, W, base, "hill")
            cv.hline(0, W, y - 1, "iron")
            if item.get("train", True):
                tx = x
                cv.rect(tx, y - 7, tx + 14, y - 2, "soot")
                cv.rect(tx + 10, y - 11, tx + 14, y - 7, "soot")
                cv.rect(tx + 2, y - 10, tx + 3, y - 7, "ink")
                for c in range(3):
                    cx = tx + 16 + c * 15
                    cv.rect(cx, y - 8, cx + 13, y - 2, "claret")
                    for wx in range(cx + 2, cx + 12, 3):
                        cv.set(wx, y - 6, "glass")
                for k in range(5):
                    cv.blob(tx + 2 - wind * k * 4, y - 13 - k * 2, 1 + k // 2, "smoke")
        elif t == "bridge":
            w = int(item.get("width", 50))
            cv.rect(x, base - h, x + w, base - h + 3, "stone")
            for k in range(x + 4, x + w, 12):
                cv.rect(k, base - h + 3, k + 3, base, "stone")
        elif t == "river":
            cv.rect(0, base - 2, W, base, mix("skyblue", "slate", 1, 2))
        elif t == "headgear":  # colliery winding gear and spoil heap
            cv.line(x - 6, base, x, base - h, "iron")
            cv.line(x + 6, base, x, base - h, "iron")
            cv.line(x, base - h, x + 10, base, "iron")
            cv.blob(x, base - h + 1, 2, "soot")
            for k in range(h // 3):
                cv.hline(x + 8 + k, x + 8 + 2 * (h // 3) - k, base - k, "coal")


def draw_trees(cv: Canvas, sc: Scene, x0, x1, base, h):
    bare = sc.season == "winter"
    x = x0
    while x < x1:
        r = sc.rng.int(max(3, h // 4), max(4, h // 3))
        cy = base - h + r
        cv.vline(x, cy, base, "twig")
        if bare:
            for k in range(4):
                cv.line(x, cy + 2, x + sc.rng.int(-r, r), cy - sc.rng.int(0, r), "twig")
        else:
            cv.blob(x, cy, r, "tree")
            cv.blob(x - 1, cy - 1, max(1, r - 2), "tree_hi")
        x += r + sc.rng.int(1, 4)


def crowd_row(cv: Canvas, sc: Scene, x0, x1, y, density):
    """Spectators 2px wide: hat, face, coat. density 0..100 (%)."""
    coats = ("ink", "soot", "coal", "brick_dk", "wood_dk", "navy")
    for x in range(int(x0), int(x1), 2):
        if sc.rng.int(0, 99) >= density:
            continue
        cv.set(x, y - 2, "ink" if sc.rng.chance(3, 4) else "stone")
        cv.set(x, y - 1, "skin")
        cv.set(x, y, coats[sc.rng.int(0, len(coats) - 1)])


def draw_far_side(cv: Canvas, sc: Scene):
    density = int(sc.d.get("crowd", 60))
    items = sorted(sc.d.get("farSide", []), key=lambda i: {"trees": 0, "houses": 0}.get(i["type"], 1))
    for item in items:
        t = item["type"]
        x0, x1 = sc.far_x(item.get("from", 0.0)), sc.far_x(item.get("to", 1.0))
        base = min(sc.far_y(x0), sc.far_y(x1)) - 2
        h = int(item.get("height", {"grandstand": 20, "stand": 10, "bank": 8, "pavilion": 22,
                                    "fence": 4, "trees": 26, "houses": 18, "shed": 9}.get(t, 10)))
        if t == "grandstand":
            covered = item.get("covered", True)
            roof = "slate" if item.get("roof", "slate") == "slate" else "wood_dk"
            cv.rect(x0, base - h, x1, base, "wood_dk")
            rows = (h - 6) // 3
            for r in range(rows):
                y = base - 4 - r * 3
                cv.hline(x0 + 1, x1 - 1, y + 1, "wood")
                crowd_row(cv, sc, x0 + 1, x1 - 1, y, density)
            cv.rect(x0, base - 3, x1, base, item.get("front", "paint_dk"))
            cv.hline(x0, x1, base - 3, "paint")
            if covered:
                ry = base - h - 4
                cv.rect(x0 - 2, ry, x1 + 2, ry + 3, roof)
                cv.hline(x0 - 2, x1 + 2, ry + 3, "slate_hi")
                for px in range(x0, x1 + 1, max(8, (x1 - x0) // 6)):
                    cv.vline(px, ry + 4, base - 3, "paint_dk")
                if item.get("gable"):
                    gx = (x0 + x1) // 2
                    for k in range(7):
                        cv.hline(gx - 7 + k, gx + 7 - k, ry - k, roof)
                    cv.vline(gx, ry - 12, ry - 7, "ink")
                    cv.rect(gx + 1, ry - 12, gx + 4, ry - 10, item.get("flag", "red"))
        elif t == "stand":  # open wooden stand or terrace steps
            steps = max(2, h // 3)
            for s in range(steps):
                y = base - s * 3
                inset = s * 2
                cv.rect(x0 + inset, y - 2, x1 - inset, y, "wood" if s % 2 == 0 else "wood_dk")
                crowd_row(cv, sc, x0 + inset + 1, x1 - inset - 1, y - 1, density)
        elif t == "bank":
            for k in range(h):
                inset = k * 2
                cv.hline(x0 + inset, x1 - inset, base - k, "grass_dk" if k % 3 else "mud")
            for k in range(0, h, 2):
                crowd_row(cv, sc, x0 + k * 2 + 1, x1 - k * 2 - 1, base - k, density * 3 // 4)
        elif t == "pavilion":
            wall = item.get("wall", "paint")
            cv.rect(x0, base - h + 8, x1, base, wall)
            for k in range(8):
                cv.hline(x0 - 2 + k, x1 + 2 - k, base - h + 8 - k, "slate" if k < 7 else "slate_hi")
            cv.rect(x0, base - 6, x1, base - 5, "wood_dk")  # verandah rail
            for px in range(x0 + 2, x1, 5):
                cv.vline(px, base - h + 9, base, "paint_dk")
                cv.rect(px + 1, base - h + 11, px + 2, base - h + 13, "soot")
            cx = (x0 + x1) // 2
            cv.vline(cx, base - h - 8, base - h + 1, "ink")
            cv.rect(cx + 1, base - h - 8, cx + 5, base - h - 6, item.get("flag", "red"))
            crowd_row(cv, sc, x0 + 1, x1 - 1, base - 1, density // 2)
        elif t == "shed":  # small covered shelter, e.g. a press or dressing shed
            cv.rect(x0, base - h + 2, x1, base, "wood")
            cv.rect(x0 - 1, base - h, x1 + 1, base - h + 1, "slate")
            cv.rect((x0 + x1) // 2 - 1, base - 5, (x0 + x1) // 2 + 1, base, "ink")
        elif t == "fence":
            cv.rect(x0, base - h, x1, base, "wood_dk")
            for px in range(x0, x1 + 1, 3):
                cv.vline(px, base - h, base, "wood")
            crowd_row(cv, sc, x0, x1, base - h - 1, density // 2)
        elif t == "trees":
            draw_trees(cv, sc, x0, x1, base, h)
        elif t == "houses":
            hx = x0
            while hx < x1:
                hw = sc.rng.int(9, 12)
                cv.rect(hx, base - h, hx + hw, base, "brick")
                cv.rect(hx + 2, base - h + 4, hx + 4, base - h + 7, "glass")
                cv.rect(hx + hw - 4, base - h + 4, hx + hw - 2, base - h + 7, "soot")
                for k in range(hw // 2 + 1):
                    cv.hline(hx + k, hx + hw - k, base - h - k, "slate")
                hx += hw + 1
        elif t == "inn":  # a public house: brick, sign board, chimneys
            cv.rect(x0, base - h, x1, base, "brick")
            for k in range((x1 - x0) // 2 + 2):
                cv.hline(x0 - 1 + k, x1 + 1 - k, base - h - k // 2, "slate")
            for wx in range(x0 + 2, x1 - 1, 4):
                cv.rect(wx, base - h + 3, wx + 1, base - h + 5, "glass")
            cv.rect((x0 + x1) // 2 - 1, base - 4, (x0 + x1) // 2 + 1, base, "wood_dk")
            cv.rect(x0 - 5, base - h + 1, x0 - 1, base - h + 4, item.get("sign", "gold"))
            cv.vline(x0 - 3, base - h + 5, base, "wood_dk")
            cv.rect(x0 + 1, base - h - (x1 - x0) // 4 - 3, x0 + 2, base - h, "brick_dk")
            cv.rect(x1 - 2, base - h - (x1 - x0) // 4 - 3, x1 - 1, base - h, "brick_dk")
        elif t == "haystack":
            for k in range(h):
                half = (x1 - x0) // 2 - (k * k) // max(1, h)
                cv.hline((x0 + x1) // 2 - half, (x0 + x1) // 2 + half, base - k, "amber" if k % 3 else "gold")
        elif t == "arch":  # ornate entrance arch with a name board
            cv.rect(x0, base - h, x0 + 2, base, "wood_dk")
            cv.rect(x1 - 2, base - h, x1, base, "wood_dk")
            cv.rect(x0, base - h - 4, x1, base - h, "paint")
            cv.hline(x0 + 2, x1 - 2, base - h - 2, "navy")
            cv.rect((x0 + x1) // 2 - 1, base - h - 7, (x0 + x1) // 2 + 1, base - h - 4, "paint")
        elif t == "rails":  # white racecourse or cricket rails
            cv.hline(x0, x1, base - 3, "white")
            cv.hline(x0, x1, base - 1, "paint_dk")
            for px in range(x0, x1 + 1, 6):
                cv.vline(px, base - 4, base, "paint")
        elif t == "crowd":  # standing spectators along the rope
            crowd_row(cv, sc, x0, x1, base, density)
            crowd_row(cv, sc, x0, x1, base - 3, density // 2)


def draw_pitch(cv: Canvas, sc: Scene):
    g, g_hi, g_dk = (col(c) for c in GRASS[sc.season])
    cond = sc.d.get("pitch", {}).get("condition", "good")
    top = min(sc.far_yl, sc.far_yr)
    for y in range(top - 1, NEAR_Y + 1):
        lx, rx = sc.edges(y)
        for x in range(0, W):
            fy = sc.far_y(x) if FAR_L <= x <= FAR_R else (sc.far_yl if x < FAR_L else sc.far_yr)
            if y < fy - 1:
                continue
            # grass with gentle bands (not modern stripes: a scythe/horse-mower texture)
            c = mix(g, g_hi, 1, 2) if ((x + y * 3) // 23) % 2 == 0 else g
            if sc.rng.chance(1, 9):
                c = g_dk
            # worn goalmouths
            if cond in ("worn", "muddy"):
                for gx in (lx + 14, rx - 14):
                    if abs(x - gx) < 10 and sc.rng.chance(1 if cond == "worn" else 2, 3):
                        c = col("mud")
            if y >= fy:
                cv.set(x, y, c)
            else:
                cv.set(x, y, g_dk)
    track = sc.d.get("pitch", {}).get("track")
    if track:
        tc = col({"cinder": mix("coal", "brick_dk", 1, 2), "grass": mix(g, "stone", 1, 3)}.get(track, "mud"))
        for y in range(top - 1, NEAR_Y + 1):
            lx, rx = sc.edges(y)
            for x in range(W):
                fy = sc.far_y(x) if FAR_L <= x <= FAR_R else (sc.far_yl if x < FAR_L else sc.far_yr)
                if y < fy - 1:
                    continue
                if lx - 8 <= x < lx - 1 or rx + 1 < x <= rx + 8 or y <= fy or y >= NEAR_Y - 1:
                    cv.set(x, y, tc if (x + y) % 7 else mix(tc, "stone", 1, 3))
    line = "cream"
    # touchlines and goal lines
    for x in range(FAR_L, FAR_R + 1):
        cv.set(x, sc.far_y(x) + 1, line)
    cv.hline(6, W - 7, NEAR_Y - 2, line)
    cv.line(FAR_L, sc.far_yl + 1, 6, NEAR_Y - 2, line)
    cv.line(FAR_R, sc.far_yr + 1, W - 7, NEAR_Y - 2, line)
    # halfway line (1888 pitches had one; no penalty area before 1891)
    mx = (FAR_L + FAR_R) // 2
    cv.line(mx, sc.far_y(mx) + 1, W // 2, NEAR_Y - 2, line)
    # goals: posts, tape-or-bar crossbar, no nets (nets came in 1891)
    for side in (0, 1):
        fx, fy = (FAR_L, sc.far_yl) if side == 0 else (FAR_R, sc.far_yr)
        nx = 6 if side == 0 else W - 7
        gx = (fx + nx) // 2
        gy = (fy + NEAR_Y) // 2
        w = 7
        for dy in (-w, w):
            px = gx + (dy * (nx - fx)) // (NEAR_Y - fy)
            cv.vline(px, gy + dy // 2 - 11, gy + dy // 2, "white")
        p0 = gx + (-w * (nx - fx)) // (NEAR_Y - fy)
        p1 = gx + (w * (nx - fx)) // (NEAR_Y - fy)
        cv.line(p0, gy - w // 2 - 11, p1, gy + w // 2 - 11, "white")
    # corner flags
    for x, y in ((FAR_L, sc.far_yl), (FAR_R, sc.far_yr), (6, NEAR_Y - 2), (W - 7, NEAR_Y - 2)):
        cv.vline(x, y - 5, y, "ink")
        cv.rect(x + 1, y - 5, x + 2, y - 4, "red")


def draw_players(cv: Canvas, sc: Scene):
    kits = sc.d.get("kits")
    if not kits:
        return
    home, away = kits.get("home", ["white", "navy"]), kits.get("away", ["red", "white"])
    figs = []
    for team, kit in ((0, home), (1, away)):
        for _ in range(11):
            y = sc.rng.int(FAR_Y + 6, NEAR_Y - 6)
            lx, rx = sc.edges(y)
            x = sc.rng.int(int(lx) + 8, int(rx) - 8)
            figs.append((y, x, kit))
    for y, x, kit in sorted(figs):
        tall = 4 + (y - FAR_Y) * 4 // (NEAR_Y - FAR_Y)
        cv.set(x, y - tall, "skin")
        cv.rect(x, y - tall + 1, x + 1, y - tall // 2, kit[0])
        cv.rect(x, y - tall // 2 + 1, x + 1, y - tall // 2 + 1, kit[1] if len(kit) > 1 else "white")
        cv.rect(x, y - tall // 2 + 2, x + 1, y, "soot")  # dark stockings and boots
    by = sc.rng.int(FAR_Y + 14, NEAR_Y - 14)
    lx, rx = sc.edges(by)
    cv.set(sc.rng.int(int(lx) + 20, int(rx) - 20), by, "wood_hi")  # the leather ball


def draw_near_side(cv: Canvas, sc: Scene):
    near = sc.d.get("nearSide", {}) or {}
    kind = near.get("type", "rope")
    density = int(near.get("crowd", sc.d.get("crowd", 60)))
    cv.rect(0, NEAR_Y + 1, W - 1, H - 1, "grass_dk" if kind != "bank" else "mud")
    if kind == "rope":
        for x in range(0, W, 16):
            cv.vline(x, NEAR_Y - 2, NEAR_Y + 4, "wood_dk")
        cv.hline(0, W - 1, NEAR_Y, "paint_dk")
    elif kind == "fence":
        cv.rect(0, NEAR_Y, W - 1, NEAR_Y + 3, "wood_dk")
        for x in range(0, W, 3):
            cv.vline(x, NEAR_Y, NEAR_Y + 3, "wood")
    for row, y in enumerate(range(NEAR_Y + 6, H + 3, 4)):
        coats = ("ink", "soot", "coal", "brick_dk", "wood_dk", "navy")
        for x in range(row % 2, W, 4):
            if sc.rng.int(0, 99) >= density:
                continue
            c = coats[sc.rng.int(0, len(coats) - 1)]
            cv.rect(x, y - 3, x + 2, y - 3, "ink" if sc.rng.chance(4, 5) else "stone")  # hat brim
            cv.rect(x + 1, y - 4, x + 1, y - 4, "ink")
            cv.rect(x, y - 2, x + 2, y + 2, c)


def draw_frame(cv: Canvas):
    for x in range(W):
        cv.set(x, 0, "ink")
        cv.set(x, H - 1, "ink")
    for y in range(H):
        cv.set(0, y, "ink")
        cv.set(W - 1, y, "ink")


def render(d: dict) -> Canvas:
    sc = Scene(d)
    cv = Canvas(W, H)
    draw_sky(cv, sc)
    draw_hills(cv, sc)
    draw_ground_band(cv, sc)
    draw_skyline(cv, sc)
    if d.get("weather") == "mist":
        top, hor, cloud = SKIES[sc.season]
        cv.haze(0, FAR_Y - 6, hor, 2, 5)
    draw_far_side(cv, sc)
    draw_pitch(cv, sc)
    draw_players(cv, sc)
    draw_near_side(cv, sc)
    draw_frame(cv)
    return cv


def thumbnail(cv: Canvas) -> Canvas:
    tw, th = cv.w // THUMB, cv.h // THUMB
    out = Canvas(tw, th)
    n = THUMB * THUMB
    for ty in range(th):
        for tx in range(tw):
            acc = [0, 0, 0]
            for y in range(ty * THUMB, ty * THUMB + THUMB):
                for x in range(tx * THUMB, tx * THUMB + THUMB):
                    p = cv.get(x, y)
                    acc[0] += p[0]
                    acc[1] += p[1]
                    acc[2] += p[2]
            out.set(tx, ty, (acc[0] // n, acc[1] // n, acc[2] // n))
    return out


# ---------------------------------------------------------------- PNG (stdlib only)
def png_bytes(cv: Canvas) -> bytes:
    raw = bytearray()
    stride = cv.w * 3
    for y in range(cv.h):
        raw.append(0)  # filter: none
        raw += cv.px[y * stride:(y + 1) * stride]

    def chunk(tag: bytes, data: bytes) -> bytes:
        return struct.pack(">I", len(data)) + tag + data + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    ihdr = struct.pack(">IIBBBBB", cv.w, cv.h, 8, 2, 0, 0, 0)  # 8-bit RGB, no timestamp or text chunks
    return b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")


def png_pixels(data: bytes) -> tuple[int, int, bytes]:
    """Decode a PNG written by png_bytes (filter 0, RGB) back to raw pixels, for --check."""
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
    stride = w * 3 + 1
    return w, h, b"".join(raw[y * stride + 1:(y + 1) * stride] for y in range(h))


def load(path: Path) -> dict:
    d = json.loads(path.read_text(encoding="utf-8"))
    if d.get("id") != path.stem:
        raise ValueError(f"{path.name}: id '{d.get('id')}' must match the file name")
    return d


def outputs(d: dict) -> dict[str, bytes]:
    cv = render(d)
    return {f"{d['id']}.png": png_bytes(cv), f"{d['id']}_thumb.png": png_bytes(thumbnail(cv))}


def main(argv: list[str]) -> int:
    check = "--check" in argv
    names = [a for a in argv if not a.startswith("--")]
    paths = sorted(DESCRIPTORS.glob("*.json"))
    if names:
        paths = [p for p in paths if p.stem in names]
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    bad = 0
    for p in paths:
        for name, data in outputs(load(p)).items():
            target = OUT_DIR / name
            if check:
                if not target.exists() or png_pixels(target.read_bytes()) != png_pixels(data):
                    print(f"out of date: {target.relative_to(REPO)}")
                    bad += 1
            else:
                target.write_bytes(data)
                print(f"wrote {target.relative_to(REPO)}")
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
