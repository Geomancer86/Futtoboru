"""A2 "doll": a tiny 3-D figure (skeleton + capsules + ellipsoid torso) rasterised to pixels.

Ported from the art prototype page (option A2, producer pick D26) to Python for the sprite baker.
The doll is rendered once per pose, direction and scale into a *material buffer*: every pixel holds what it
shows (shirt at surface coordinates u/v/angle, sleeve, knickers, sock, pad, boot, skin, hair, cap, ...) and
one of three light tones. Kit colours are applied afterwards (`match_art.colourise`), so the slow part
(rasterising) runs once and every club sheet has exactly the same silhouettes and layout.

Determinism: only IEEE + - * / and sqrt are used (correctly rounded everywhere). Sine and cosine are our own
polynomials (`sin`, `cos` below), never the C library, so the same input gives the same pixels on every
platform.

Camera V3 (side-on broadcast, D26): heights are drawn at 1 px per px/m (`KH`), ground depth foreshortened
by `KG` (camera about 18 degrees above the pitch). Direction 0 = S (facing the camera), 2 = E (screen
right), 4 = N (away), 6 = W; odd numbers are the diagonals.
"""
from __future__ import annotations

import math

PI = 3.141592653589793
TAU = 2.0 * PI
HALF_PI = PI / 2.0

KH, KG = 1.0, 0.30  # V3 side-on: height scale, ground (depth) scale
LIGHT = (-0.55, 0.75, 0.5)
_L = math.sqrt(LIGHT[0] ** 2 + LIGHT[1] ** 2 + LIGHT[2] ** 2)
LIGHT = (LIGHT[0] / _L, LIGHT[1] / _L, LIGHT[2] / _L)

DIRS = ("S", "SE", "E", "NE", "N", "NW", "W", "SW")


def sin(x: float) -> float:
    """Sine from a fixed polynomial (|error| < 1e-10); platform independent."""
    k = math.floor(x / HALF_PI + 0.5)
    r = x - k * HALF_PI
    r2 = r * r
    s = r * (1.0 + r2 * (-1.0 / 6 + r2 * (1.0 / 120 + r2 * (-1.0 / 5040 + r2 * (1.0 / 362880
                                                                                 + r2 * (-1.0 / 39916800))))))
    c = 1.0 + r2 * (-0.5 + r2 * (1.0 / 24 + r2 * (-1.0 / 720 + r2 * (1.0 / 40320 + r2 * (-1.0 / 3628800
                                                                                       + r2 / 479001600)))))
    q = int(k) % 4
    return (s, c, -s, -c)[q]


def cos(x: float) -> float:
    return sin(x + HALF_PI)


def add(a, b):
    return (a[0] + b[0], a[1] + b[1], a[2] + b[2])


def sub(a, b):
    return (a[0] - b[0], a[1] - b[1], a[2] - b[2])


def scl(a, k):
    return (a[0] * k, a[1] * k, a[2] * k)


def dot(a, b):
    return a[0] * b[0] + a[1] * b[1] + a[2] * b[2]


def cross(a, b):
    return (a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0])


def norm(a):
    n = math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]) or 1.0
    return (a[0] / n, a[1] / n, a[2] / n)


def length(a):
    return math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2])


def rot_x(v, a):
    c, s = cos(a), sin(a)
    return (v[0], v[1] * c - v[2] * s, v[1] * s + v[2] * c)


def rot_z(v, a):
    c, s = cos(a), sin(a)
    return (v[0] * c - v[1] * s, v[0] * s + v[1] * c, v[2])


# metres; a ~1.76 m man (period players were short)
BODY = {"thigh": .43, "shin": .43, "ankle": .07, "torso": .5, "neck": .06, "headR": .135, "upper": .29,
        "fore": .27, "hipW": .12, "shW": .215, "foot": .21}

# frames per animation
FRAMES = {"idle": 4, "run": 8, "kick": 4, "header": 3, "charge": 8, "fall": 4, "injured": 4, "celebrate": 4,
          "throwin": 2, "catch": 3, "punch": 3, "walk": 8}


def pose(state: str, t: float) -> dict:
    """Joint angles for `state` at phase t in [0, 1). Legs: (hip swing, knee bend); arms: (swing, elbow, abduct)."""
    S, C = sin, cos
    P = {"legL": (0.03, 0.06), "legR": (-0.03, 0.06), "armL": (0.05, 0.2, 0.2), "armR": (0.05, 0.2, 0.2),
         "lean": 0.0, "roll": 0.0, "pitch": 0.0, "jump": 0.0, "ground": True}

    def f(n):
        return int(math.floor(t * n + 1e-6))

    if state == "idle":
        b = S(t * TAU)
        P["armL"] = (0.05 + 0.03 * b, 0.2, 0.2)
        P["armR"] = (0.05 - 0.03 * b, 0.2, 0.2)
    elif state == "run":
        ph = t * TAU

        def leg(p):
            return (0.8 * S(p), 0.25 + 1.25 * max(0.0, C(p)))
        P["legL"], P["legR"] = leg(ph), leg(ph + PI)
        P["armL"] = (-0.8 * S(ph), 1.3, 0.12)
        P["armR"] = (0.8 * S(ph), 1.3, 0.12)
        P["lean"] = 0.2
    elif state == "walk":
        ph = t * TAU

        def leg(p):
            return (0.35 * S(p), 0.1 + 0.5 * max(0.0, C(p)))
        P["legL"], P["legR"] = leg(ph), leg(ph + PI)
        P["armL"] = (-0.3 * S(ph), 0.3, 0.1)
        P["armR"] = (0.3 * S(ph), 0.3, 0.1)
    elif state == "kick":
        k = f(4)
        if k == 0:
            P.update(legR=(-0.85, 1.45), legL=(0.15, 0.15), armL=(0.5, 0.4, 0.7), armR=(-0.4, 0.4, 0.55))
        elif k == 1:
            P.update(legR=(0.4, 0.3), legL=(0.1, 0.15), armL=(-0.3, 0.4, 0.75), armR=(0.3, 0.4, 0.6), lean=-0.05)
        else:
            P.update(legR=(1.25, 0.1), legL=(0.0, 0.2), armL=(-0.6, 0.3, 0.85), armR=(0.5, 0.3, 0.7), lean=-0.18)
    elif state == "header":
        k = f(3)
        if k == 0:
            P.update(legL=(0.4, 0.85), legR=(0.4, 0.85), armL=(-0.5, 0.6, 0.2), armR=(-0.5, 0.6, 0.2), lean=0.3)
        elif k == 1:
            P.update(jump=0.36, ground=False, legL=(-0.15, 0.9), legR=(0.05, 0.7), armL=(0.4, 0.5, 0.7),
                     armR=(0.4, 0.5, 0.7), lean=-0.12)
        else:
            P.update(jump=0.42, ground=False, legL=(-0.25, 0.95), legR=(0.0, 0.7), armL=(0.2, 0.5, 0.9),
                     armR=(0.2, 0.5, 0.9), lean=0.32)
    elif state == "charge":  # shoulder charge (fair in 1888)
        ph = t * TAU
        P["legL"] = (0.6 * S(ph), 0.3 + 0.9 * max(0.0, C(ph)))
        P["legR"] = (0.6 * S(ph + PI), 0.3 + 0.9 * max(0.0, C(ph + PI)))
        P.update(armL=(-0.2, 1.6, 0.05), armR=(0.3, 1.0, 0.35), lean=0.25, roll=0.34)
    elif state == "fall":
        k = f(4)
        if k == 0:
            P.update(legL=(0.6, 0.4), legR=(-0.6, 1.0), armL=(0.7, 0.3, 0.5), armR=(0.5, 0.3, 0.6), lean=0.5)
        elif k == 1:
            P.update(legL=(0.2, 0.5), legR=(-0.3, 0.6), armL=(1.3, 0.2, 0.4), armR=(1.2, 0.2, 0.4), pitch=0.85)
        else:
            P.update(legL=(0.1, 0.3), legR=(-0.1 + 0.1 * (k - 2), 0.2), armL=(1.6, 0.1, 0.75),
                     armR=(1.5, 0.1, 0.6 + 0.1 * (k - 2)), pitch=1.52)
    elif state == "injured":  # on his back, clutching a knee
        w = S(t * TAU) * 0.15
        P.update(pitch=-1.5, legL=(1.1 + w, 1.8 + w), legR=(0.1, 0.1), armL=(1.25 + w, 0.7, 0.12),
                 armR=(1.2, 0.8, 0.12))
    elif state == "celebrate":
        b = abs(S(t * TAU))
        P.update(armL=(2.85, 0.1, 0.38), armR=(2.85, 0.1, 0.38), jump=0.14 * b, ground=False,
                 legL=(0.1, 0.5 * b), legR=(-0.05, 0.5 * b))
    elif state == "throwin":  # two-handed, from behind the head (1882 law)
        k = f(2)
        if k == 0:
            P.update(armL=(2.75, 0.95, 0.18), armR=(2.75, 0.95, 0.18), lean=-0.15)
        else:
            P.update(armL=(1.65, 0.1, 0.15), armR=(1.65, 0.1, 0.15), lean=0.15)
        P.update(legL=(0.25, 0.1), legR=(-0.2, 0.1))
    elif state == "catch":  # keeper: reach, take, clutch to the chest
        k = f(3)
        if k == 0:
            P.update(armL=(1.9, 0.2, 0.25), armR=(1.9, 0.2, 0.25), legL=(0.2, 0.3), legR=(-0.1, 0.3), lean=0.1)
        elif k == 1:
            P.update(armL=(1.45, 0.6, 0.2), armR=(1.45, 0.6, 0.2), legL=(0.15, 0.25), legR=(-0.05, 0.25))
        else:
            P.update(armL=(0.75, 1.75, 0.22), armR=(0.75, 1.75, 0.22), legL=(0.1, 0.2), legR=(-0.05, 0.2),
                     lean=0.18)
    elif state == "punch":  # keeper: two-fisted punch clear (keepers could be charged, so they punched)
        k = f(3)
        if k == 0:
            P.update(armL=(0.7, 1.9, 0.15), armR=(0.7, 1.9, 0.15), legL=(0.4, 0.8), legR=(0.3, 0.8), lean=0.1)
        elif k == 1:
            P.update(armL=(2.55, 0.05, 0.12), armR=(2.55, 0.05, 0.12), jump=0.28, ground=False,
                     legL=(-0.1, 0.7), legR=(0.1, 0.5), lean=-0.1)
        else:
            P.update(armL=(2.2, 0.3, 0.3), armR=(2.2, 0.3, 0.3), jump=0.12, ground=False, legL=(0.0, 0.4),
                     legR=(0.1, 0.3), lean=0.05)
    return P


class Geo:
    """A rendered material buffer: `px[i]` is None or (material-key, tone) with tone 0 dark .. 2 light,
    -1 for flat colours (outline). (ox, oy) is the pivot: the ground point under the figure."""

    __slots__ = ("w", "h", "ox", "oy", "px")

    def __init__(self, w, h, ox, oy):
        self.w, self.h, self.ox, self.oy = w, h, ox, oy
        self.px = [None] * (w * h)

    def bbox(self):
        x0 = y0 = 10 ** 9
        x1 = y1 = -1
        w = self.w
        for i, p in enumerate(self.px):
            if p is not None:
                x, y = i % w, i // w
                x0, x1 = min(x0, x), max(x1, x)
                y0, y1 = min(y0, y), max(y1, y)
        return x0, y0, x1, y1


def render(state: str, frame: int, direction: int, S: int, look: dict) -> Geo:
    """Rasterise one doll pose. `look`: role ('player' or a clothed role), hat (None/'bowler'/'topper'/
    'helmet'/'cap'), item ('bucket' or None), body (height factor, 1.0 = BODY)."""
    n = FRAMES[state]
    P = pose(state, (frame % n) / n)
    k = look.get("body", 1.0)
    B = {a: v * k for a, v in BODY.items()}
    player = look.get("role", "player") == "player"
    hat = look.get("hat")
    hip_y = B["thigh"] + B["shin"] + B["ankle"]

    legs, arms = {}, {}
    for nm, side, (a, kk) in (("L", 1, P["legL"]), ("R", -1, P["legR"])):
        hx = side * B["hipW"]
        hip = (hx, hip_y, 0.0)
        knee = (hx, hip_y - B["thigh"] * cos(a), B["thigh"] * sin(a))
        b = a - kk
        ank = (hx, knee[1] - B["shin"] * cos(b), knee[2] + B["shin"] * sin(b))
        toe = (hx, ank[1] + B["foot"] * sin(b) * 0.7 - 0.02, ank[2] + B["foot"] * cos(b))
        legs[nm] = {"hip": hip, "knee": knee, "ank": ank, "toe": toe}
    lean = P["lean"]
    hip_c = (0.0, hip_y, 0.0)
    up = (0.0, cos(lean), sin(lean))
    neck = add(hip_c, scl(up, B["torso"]))
    head_c = add(neck, scl(up, B["neck"] + B["headR"]))
    for nm, side, (sw, e, ab) in (("L", 1, P["armL"]), ("R", -1, P["armR"])):
        sh = add(add(hip_c, scl(up, B["torso"] - 0.05)), (side * B["shW"], 0.0, 0.0))
        d1 = rot_x((side * sin(ab), -cos(sw) * cos(ab), sin(sw) * cos(ab)), lean)
        el = add(sh, scl(d1, B["upper"]))
        d2 = rot_x((side * sin(ab) * 0.8, -cos(sw + e) * cos(ab), sin(sw + e) * cos(ab)), lean)
        arms[nm] = {"sh": sh, "el": el, "hand": add(el, scl(d2, B["fore"]))}

    pitch, roll = P["pitch"], P["roll"]

    def G(v):
        return rot_z(rot_x(v, pitch), roll)
    for limb in (legs["L"], legs["R"], arms["L"], arms["R"]):
        for kk in limb:
            limb[kk] = G(limb[kk])
    hip_t, neck_t, head_t = G(hip_c), G(neck), G(head_c)
    fwd_b = G(rot_x((0.0, 0.0, 1.0), lean))
    up_b = norm(sub(neck_t, hip_t))

    miny = 1e9

    def consider(p, r):
        nonlocal miny
        miny = min(miny, p[1] - r)
    for L in (legs["L"], legs["R"]):
        consider(L["ank"], 0.05)
        consider(L["toe"], 0.04)
    if abs(pitch) > 0.3:
        for A in (arms["L"], arms["R"]):
            consider(A["hand"], 0.045)
            consider(A["el"], 0.05)
        consider(head_t, B["headR"])
        consider(neck_t, 0.12)
        consider(hip_t, 0.12)
        for L in (legs["L"], legs["R"]):
            consider(L["knee"], 0.08)
    dy = -miny + (0.0 if P["ground"] else P["jump"])

    yaw = direction * PI / 4
    cy, sy = cos(yaw), sin(yaw)
    W = int(math.ceil(4.6 * S)) + 8
    H = int(math.ceil(S * (2.7 * KH + 4.4 * KG))) + 10
    ox = W // 2
    oy = int(math.ceil(S * (2.6 * KH + 2.2 * KG))) + 5
    geo = Geo(W, H, ox, oy)
    px = geo.px
    zb = [-1e9] * (W * H)
    step = 0.42 / S
    lx, ly, lz = LIGHT
    floor = math.floor

    def plot(p, nn, key):
        x, y, z = p[0], p[1] + dy, p[2]
        wx = x * cy + z * sy
        wz = -x * sy + z * cy
        nx = nn[0] * cy + nn[2] * sy
        nz = -nn[0] * sy + nn[2] * cy
        ny = nn[1]
        if ny * KG + nz * KH < -0.08:
            return
        sx = int(floor(ox + wx * S))
        syy = int(floor(oy - y * S * KH + wz * S * KG))
        if sx < 0 or syy < 0 or sx >= W or syy >= H:
            return
        d = wz * KH + y * KG
        i = syy * W + sx
        if d <= zb[i]:
            return
        zb[i] = d
        lv = nx * lx + ny * ly + nz * lz
        px[i] = (key, 2 if lv > 0.42 else (1 if lv > -0.18 else 0))

    def sphere(c, r, mat):
        nl = max(5, int(math.ceil(PI * r / step)) + 2)
        for i in range(nl + 1):
            ph = i / nl * PI
            yy, rr = cos(ph), sin(ph)
            na = max(5, int(math.ceil(TAU * r * rr / step)) + 2)
            for j in range(na):
                th = j / na * TAU
                nn = (rr * cos(th), yy, rr * sin(th))
                plot(add(c, scl(nn, r)), nn, mat(nn))

    def capsule(a, b, r, mat):
        ax = sub(b, a)
        L = length(ax) or 1e-6
        u = scl(ax, 1 / L)
        p = (0.0, 1.0, 0.0) if abs(u[1]) < 0.9 else (1.0, 0.0, 0.0)
        v1 = norm(cross(u, p))
        v2 = cross(u, v1)
        nt = max(2, int(math.ceil(L / step)) + 1)
        na = max(6, int(math.ceil(TAU * r / step)) + 2)
        ring = [(cos(j / na * TAU), sin(j / na * TAU)) for j in range(na)]
        for i in range(nt + 1):
            t = i / nt
            c = add(a, scl(ax, t))
            for ca, sa in ring:
                nn = add(scl(v1, ca), scl(v2, sa))
                plot(add(c, scl(nn, r)), nn, mat(t, nn))
        sphere(a, r, lambda nn: mat(0.0, nn))
        sphere(b, r, lambda nn: mat(1.0, nn))

    # legs
    for nm in ("L", "R"):
        L = legs[nm]
        sd = norm(sub(L["ank"], L["knee"]))
        fr = norm(sub(fwd_b, scl(sd, dot(fwd_b, sd))))
        if player:
            k_end = add(L["knee"], scl(sd, 0.05))
            capsule(L["hip"], k_end, 0.098, lambda t, nn: ("knick",))

            def shin(t, nn, fr=fr):
                if t > 0.84:
                    return ("boot",)
                if 0.12 < t < 0.76 and dot(nn, fr) > 0.3:
                    return ("pad",)  # shin pads worn outside the stockings
                return ("sock", int(t * 4))
            capsule(L["knee"], L["ank"], 0.06, shin)
        else:
            capsule(L["hip"], L["knee"], 0.075, lambda t, nn: ("trousers",))
            capsule(L["knee"], L["ank"], 0.062, lambda t, nn: ("boot",) if t > 0.9 else ("trousers",))
        capsule(L["ank"], L["toe"], 0.056, lambda t, nn: ("foot",))

    # torso: an ellipse swept from hips to neck; shirt keys carry surface coordinates for the kit pattern
    tU = up_b
    tF = norm(sub(fwd_b, scl(tU, dot(fwd_b, tU))))
    tR = cross(tU, tF)
    tlen = length(sub(neck_t, hip_t))
    nv = int(math.ceil(tlen / step)) + 1
    na = int(math.ceil(TAU * 0.19 / step)) + 4
    ring = [(cos(j / na * TAU), sin(j / na * TAU), j / na) for j in range(na)]
    for i in range(nv + 1):
        v = i / nv
        rx, rz = 0.17 + 0.05 * v, 0.115
        base = add(hip_t, scl(tU, v * tlen))
        for cx, sz, th in ring:
            nn = norm(add(scl(tR, cx / rx), scl(tF, sz / rz)))
            p = add(base, add(scl(tR, rx * cx), scl(tF, rz * sz)))
            if player:
                key = ("collar",) if (sz > 0.82 and abs(cx) < 0.14 and v > 0.8) else \
                    ("shirt", round(-rx * cx, 3), round(v, 3), round(th, 3))
            else:
                key = ("front",) if (sz > 0.8 and abs(cx) < 0.2 and v > 0.72) else ("jacket",)
            plot(p, nn, key)
    rr = 0.0
    while rr <= 1.0:  # shoulders (top of the torso ellipse)
        for cx, sz, th in ring:
            p = add(neck_t, add(scl(tR, 0.22 * rr * cx), scl(tF, 0.115 * rr * sz)))
            plot(p, tU, ("shirt", round(-0.22 * rr * cx, 3), 1.0, round(th, 3)) if player else ("jacket",))
        rr += step / 0.19
    if not player:  # coat skirt below the waist
        for i in range(7):
            v = -i / 6 * 0.22
            for cx, sz, th in ring:
                nn = norm(add(scl(tR, cx), scl(tF, sz)))
                p = add(add(hip_t, scl(tU, v)), add(scl(tR, 0.16 * cx), scl(tF, 0.115 * sz)))
                plot(p, nn, ("jacket",))

    # arms: long sleeves to the wrist
    for nm, side in (("L", 1), ("R", -1)):
        A = arms[nm]
        sk = ("sleeve", side) if player else ("jacket",)
        capsule(A["sh"], A["el"], 0.056, lambda t, nn, sk=sk: sk)
        capsule(A["el"], A["hand"], 0.05, lambda t, nn, sk=sk: ("skin",) if t > 0.92 else sk)
        sphere(A["hand"], 0.042, lambda nn: ("skin",))

    capsule(neck_t, add(neck_t, scl(up_b, 0.07)), 0.048, lambda t, nn: ("skin",))
    h_r = norm(cross(up_b, fwd_b))

    def head(nn):
        u, f, s = dot(nn, up_b), dot(nn, fwd_b), dot(nn, h_r)
        if hat in ("bowler", "topper") and u > 0.15:
            return ("hat",)
        if hat == "helmet" and u > -0.05:
            return ("hat",)
        cap = "cap" if u > 0.2 else ("peak" if (f > 0.55 and -0.05 < u < 0.35) else "")
        if u > 0.38 or (f < -0.25 and u > -0.55):
            base = "hair"
        elif f > 0.8 and -0.48 < u < -0.15 and abs(s) < 0.48:
            base = "tache"
        else:
            base = "skin"
        if hat == "cap":
            return ("cap",) if cap == "cap" else (("peak",) if cap == "peak" else (base,))
        return ("head", cap, base) if player else (base,)
    sphere(head_t, B["headR"], head)

    if hat in ("bowler", "topper"):  # brim
        base = add(head_t, scl(up_b, 0.035))
        for j in range(40):
            th = j / 40 * TAU
            r = 0.11
            while r <= 0.16:
                plot(add(base, add(scl(h_r, r * cos(th)), scl(fwd_b, r * sin(th)))), up_b, ("hat",))
                r += step
    if hat == "topper":
        capsule(add(head_t, scl(up_b, 0.09)), add(head_t, scl(up_b, 0.30)), 0.10, lambda t, nn: ("hat",))
    if hat == "helmet":  # custodian helmet
        sphere(add(head_t, scl(up_b, 0.1)), 0.12, lambda nn: ("hat",))
    item = look.get("item")
    if item == "bucket":  # trainer: bucket in the right hand, sponge in the left
        hand = arms["R"]["hand"]
        capsule(add(hand, (0.0, -0.08, 0.0)), add(hand, (0.0, -0.26, 0.0)), 0.08, lambda t, nn: ("bucket",))
        sphere(arms["L"]["hand"], 0.06, lambda nn: ("sponge",))

    # 1-px outline around the silhouette
    add_px = []
    for i in range(W * H):
        if px[i] is not None:
            continue
        x, y = i % W, i // W
        if (x > 0 and px[i - 1] is not None) or (x < W - 1 and px[i + 1] is not None) or \
                (y > 0 and px[i - W] is not None) or (y < H - 1 and px[i + W] is not None):
            add_px.append(i)
    for i in add_px:
        px[i] = (("out",), -1)
    return geo
