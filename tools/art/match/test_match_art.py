"""Tests for the match sprite baker. Run: python -m pytest tools/art"""
import json
import math
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import match_art as m  # noqa: E402
import match_doll as doll  # noqa: E402
import match_grids as grids  # noqa: E402

SEASON_JSON = m.REPO / "futtoboru-core" / "src" / "main" / "resources" / "gamedata" / "core" / "seasons" / "1888-89" / "season.json"
CLUBS_JSON = m.REPO / "futtoboru-core" / "src" / "main" / "resources" / "gamedata" / "core" / "clubs.json"


@pytest.fixture(scope="module")
def baked():
    return m.outputs()


@pytest.fixture(scope="module")
def index(baked):
    return json.loads(baked["index.json"])


def test_own_sine_matches_libm_closely():
    for i in range(-400, 400):
        x = i * 0.0371
        assert abs(doll.sin(x) - math.sin(x)) < 1e-10
        assert abs(doll.cos(x) - math.cos(x)) < 1e-10


def test_same_input_gives_identical_pixels():
    look = {"role": "player"}
    for st, f, d in (("run", 3, 1), ("kick", 2, 6), ("catch", 0, 2)):
        a, b = doll.render(st, f, d, 10, look), doll.render(st, f, d, 10, dict(look))
        assert a.px == b.px
    lay = m.doll_layout(1)
    raw = m.load_kits()["1888-89"]["derby-county"]
    one = m.png_bytes(lay.w, lay.h, m.kit_sheet(lay, m.kit_look("derby-county", raw), 1))
    two = m.png_bytes(lay.w, lay.h, m.kit_sheet(lay, m.kit_look("derby-county", json.loads(json.dumps(raw))), 1))
    assert one == two
    assert grids.official_frame("umpire-a", "flag", 1, "E", 2).px == grids.official_frame("umpire-a", "flag", 1, "E", 2).px


def test_committed_files_match_a_fresh_bake(baked):
    for rel, data in baked.items():
        target = m.OUT_DIR / rel
        assert target.exists(), f"run: python tools/art/match/match_art.py ({rel} missing)"
        assert m.same(target, data), f"re-bake: python tools/art/match/match_art.py ({rel} differs)"
    produced = {(m.OUT_DIR / r).resolve() for r in baked}
    stale = [p for p in m.OUT_DIR.rglob("*") if p.is_file() and p.resolve() not in produced]
    assert not stale, f"files not produced by the baker: {stale}"


def test_index_regions_fit_their_sheets(index, baked):
    layouts = index["layouts"]
    for sh in index["sheets"]:
        w, h, px = m.png_pixels(baked[sh["file"]])
        lay = layouts[sh["layout"]]
        assert [w, h] == lay["size"], sh["file"]
        for name, (x, y, rw, rh, pvx, pvy) in lay["regions"].items():
            assert 0 <= x and 0 <= y and x + rw <= w and y + rh <= h, (sh["file"], name)
            assert 0 <= pvx <= rw and 0 <= pvy <= rh, (sh["file"], name)


def test_animations_have_all_frames_and_directions(index):
    for lname, lay in index["layouts"].items():
        regs = lay["regions"]
        for anim, a in lay["animations"].items():
            for f in range(a["frames"]):
                if "dirs" in a:
                    for d in index["directions"]:
                        assert f"{anim}/{d}/{f}" in regs, (lname, anim, d, f)
                else:
                    assert f"{anim}/{f}" in regs, (lname, anim, f)


def test_kit_frames_are_not_empty_and_share_one_layout(index, baked):
    kits = [s for s in index["sheets"] if s["kind"] == "kit"]
    for sh in kits:
        w, h, px = m.png_pixels(baked[sh["file"]])
        x, y, rw, rh, *_ = index["layouts"][sh["layout"]]["regions"]["player/run/E/0"]
        alpha = [px[((y + j) * w + x + i) * 4 + 3] for j in range(rh) for i in range(rw)]
        assert sum(1 for a in alpha if a) > 20, sh["file"]
    assert {s["layout"] for s in kits} == {"doll_1x", "doll_2x"}


def test_every_league_club_has_a_kit_sheet_at_both_scales(index):
    season = json.loads(SEASON_JSON.read_text(encoding="utf-8"))
    league = next(e for e in season["editions"] if e["competition"] == "football-league")["participants"]
    have = {(s["club"], s["scale"]) for s in index["sheets"] if s["kind"] == "kit" and s["season"] == "1888-89"}
    for club in league:
        for scale in m.SCALES:
            assert (club, scale) in have, club


def test_kit_descriptors_are_valid():
    clubs = {c["id"] for c in json.loads(CLUBS_JSON.read_text(encoding="utf-8"))}
    for season, kits in m.load_kits().items():
        for club, k in kits.items():
            assert club in clubs, club
            assert k["pattern"] in ("plain", "halves", "quarters", "hoops", "stripes", "sash", "spots"), club
            assert k["colours"] and all(len(c) == 7 and c[0] == "#" for c in k["colours"]), club
            assert isinstance(k["uncertain"], bool) and isinstance(k["guessed"], list) and k["notes"], club
            if k["pattern"] in ("halves", "quarters", "stripes", "sash", "spots"):
                assert len(k["colours"]) >= 2, club


def test_sizes_follow_s1_scale():
    """A standing player is ~9-10 px tall at 5 px/m (plus a 1-px outline), twice that at 2x."""
    for scale, lo, hi in ((1, 9, 13), (2, 18, 23)):
        g = doll.render("idle", 0, 0, m.PPM * scale, {"role": "player"})
        x0, y0, x1, y1 = g.bbox()
        assert lo <= y1 - y0 + 1 <= hi, (scale, y1 - y0 + 1)
