"""Tests for the game data (P2-4). Run: python -m pytest tools/data"""
import json
import shutil
import subprocess
import sys
from pathlib import Path

import pytest

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import validate  # noqa: E402

ROOT = validate.DEFAULT_ROOT


def test_shipped_data_has_no_errors():
    rep = validate.validate(ROOT)
    assert rep.errors == []


def test_json_matches_legacy_files():
    """Until the Java loaders switch (slice 2), the JSON is generated from mods/*.txt: they must not drift."""
    legacy = ROOT.parent / "mods" / "seasons" / "18"
    if not legacy.exists():
        pytest.skip("legacy files retired; the JSON is the only copy")
    r = subprocess.run([sys.executable, "-I", str(HERE / "convert_legacy.py"), "--check"], capture_output=True, text=True)
    assert r.returncode == 0, r.stdout + r.stderr


def test_every_person_is_in_a_squad_and_ids_are_unique():
    people = json.loads((ROOT / "core" / "people.json").read_text(encoding="utf-8"))
    squads = json.loads((ROOT / "core" / "seasons" / "1888-89" / "squads.json").read_text(encoding="utf-8"))
    ids = [p["id"] for p in people]
    assert len(ids) == len(set(ids))
    in_squads = {pid for s in squads for pid in s["players"]}
    assert set(ids) == in_squads


@pytest.fixture
def pack(tmp_path):
    dst = tmp_path / "gamedata"
    shutil.copytree(ROOT, dst)
    return dst


def edit(path: Path, fn):
    data = json.loads(path.read_text(encoding="utf-8"))
    fn(data)
    path.write_text(json.dumps(data, ensure_ascii=False), encoding="utf-8")


def errors(root):
    return "\n".join(validate.validate(root).errors)


def test_unknown_field_is_an_error(pack):
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"fundation": 1880}))
    assert "unknown field `fundation`" in errors(pack)


def test_dangling_reference_is_an_error(pack):
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"country": "atlantis"}))
    assert "unknown country `atlantis`" in errors(pack)


def test_duplicate_id_is_an_error_ignoring_case(pack):
    def dup(d):
        d.append(dict(d[0], id=d[0]["id"].upper(), legacyId=None))
    edit(pack / "core" / "clubs.json", dup)
    assert "duplicate club id" in errors(pack)


def test_bad_id_and_date_are_errors(pack):
    def bad(d):
        d[0]["id"] = "Fred Dewhurst"
        d[1]["born"] = "16-12-1863"
    edit(pack / "core" / "people.json", bad)
    out = errors(pack)
    assert "bad id `Fred Dewhurst`" in out
    assert "expected a date" in out


def test_unlisted_file_is_an_error(pack):
    (pack / "core" / "extra.json").write_text("[]", encoding="utf-8")
    assert "`extra.json` exists but is not listed" in errors(pack)


def test_alias_resolves_renamed_id(pack):
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"id": "preston-north-end-fc"}))
    assert "unknown club `preston-north-end`" in errors(pack)
    edit(pack / "core" / "aliases.json", lambda d: d["club"].update({"preston-north-end": "preston-north-end-fc"}))
    assert errors(pack) == ""


def test_alias_to_live_id_is_an_error(pack):
    edit(pack / "core" / "aliases.json", lambda d: d["club"].update({"stoke": "everton"}))
    assert "alias `stoke` is still a live id" in errors(pack)
