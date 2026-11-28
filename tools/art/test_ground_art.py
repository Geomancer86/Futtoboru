"""Tests for the ground illustration generator. Run: python -m pytest tools/art"""
import json
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import ground_art as g  # noqa: E402

DESCRIPTORS = sorted(g.DESCRIPTORS.glob("*.json"))
GROUNDS_JSON = g.REPO / "futtoboru-core" / "src" / "main" / "resources" / "gamedata" / "core" / "grounds.json"


def test_there_are_descriptors():
    assert DESCRIPTORS


@pytest.mark.parametrize("path", DESCRIPTORS, ids=lambda p: p.stem)
def test_same_input_gives_identical_bytes(path):
    d = g.load(path)
    first, second = g.outputs(d), g.outputs(json.loads(json.dumps(d)))
    assert first == second
    big = first[f"{d['id']}.png"]
    assert g.png_pixels(big)[:2] == (g.W, g.H)
    assert g.png_pixels(first[f"{d['id']}_thumb.png"])[:2] == (g.W // g.THUMB, g.H // g.THUMB)


@pytest.mark.parametrize("path", DESCRIPTORS, ids=lambda p: p.stem)
def test_committed_png_matches_descriptor(path):
    d = g.load(path)
    for name, data in g.outputs(d).items():
        target = g.OUT_DIR / name
        assert target.exists(), f"run: python tools/art/ground_art.py {d['id']}"
        assert g.png_pixels(target.read_bytes()) == g.png_pixels(data), f"re-render {name}"


def test_descriptor_ids_are_ground_ids():
    known = {r["id"] for r in json.loads(GROUNDS_JSON.read_text(encoding="utf-8"))}
    for p in DESCRIPTORS:  # P2-14: the proposed venues (Oval, Bramall Lane, Clee Park) are ground records now
        assert p.stem in known, f"{p.stem} is not a ground id"


def test_different_grounds_differ():
    seen = {}
    for p in DESCRIPTORS:
        data = g.outputs(g.load(p))[f"{p.stem}.png"]
        assert data not in seen.values(), p.stem
        seen[p.stem] = data


def test_png_round_trip():
    cv = g.Canvas(3, 2)
    cv.set(1, 1, (10, 20, 30))
    w, h, px = g.png_pixels(g.png_bytes(cv))
    assert (w, h) == (3, 2) and px[12:15] == bytes((10, 20, 30))
