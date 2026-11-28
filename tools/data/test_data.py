"""Tests for the game data (P2-4). Run: python -m pytest tools/data"""
import json
import shutil
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
        d.append(dict(d[0], id=d[0]["id"].upper()))
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


# ---------------------------------------------------------------------------------------------- ids.json registry
import assign_ids  # noqa: E402


def registry(root):
    return json.loads((root / "core" / "ids.json").read_text(encoding="utf-8"))


def numbers(root):
    return validate.record_numbers(validate.scan(root))


def test_every_shipped_record_has_a_number():
    assert assign_ids.assign(ROOT, write=False) == []


def test_new_record_gets_the_next_number_and_nothing_else_changes(pack):
    before = registry(pack)
    edit(pack / "core" / "clubs.json", lambda d: d.append(dict(d[0], id="new-club")))
    assert "club `new-club` has no number" in errors(pack)
    added = assign_ids.assign(pack)
    after = registry(pack)
    assert added == [f"core club `new-club` -> {before['next']}"]
    assert after["ids"]["club"]["new-club"] == before["next"]
    assert after["next"] == before["next"] + 1
    for kind, table in before["ids"].items():
        for rid, n in table.items():
            assert after["ids"][kind][rid] == n
    assert errors(pack) == ""


def test_removed_record_keeps_its_number_reserved(pack):
    before = numbers(pack)
    def drop(d):
        d[:] = [p for p in d if p["id"] != "fred-dewhurst-1863"]
    edit(pack / "core" / "people.json", drop)
    edit(pack / "core" / "seasons" / "1888-89" / "squads.json",
         lambda d: [sq.update(players=[p for p in sq["players"] if p != "fred-dewhurst-1863"],
                              positions={k: v for k, v in sq["positions"].items() if k != "fred-dewhurst-1863"})
                    for sq in d])
    assert "person `fred-dewhurst-1863` (" in errors(pack) and "has no record" in errors(pack)
    edit(pack / "core" / "ids.json", lambda d: d["retired"].setdefault("person", []).append("fred-dewhurst-1863"))
    assert errors(pack) == ""
    after = numbers(pack)
    del before[("person", "fred-dewhurst-1863")]
    assert after == before
    # the number is not handed out again
    edit(pack / "core" / "clubs.json", lambda d: d.append(dict(d[0], id="new-club")))
    assign_ids.assign(pack)
    assert registry(pack)["ids"]["club"]["new-club"] == registry(pack)["next"] - 1
    assert registry(pack)["ids"]["club"]["new-club"] not in before.values()


def test_renamed_record_inherits_the_old_number(pack):
    old = numbers(pack)[("club", "preston-north-end")]
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"id": "preston-north-end-fc"}))
    edit(pack / "core" / "aliases.json", lambda d: d["club"].update({"preston-north-end": "preston-north-end-fc"}))
    assert errors(pack) == ""
    assert numbers(pack)[("club", "preston-north-end-fc")] == old
    assert assign_ids.assign(pack) == []


def test_duplicate_number_is_an_error(pack):
    def dup(d):
        d["ids"]["club"]["stoke"] = d["ids"]["club"]["preston-north-end"]
    edit(pack / "core" / "ids.json", dup)
    assert "is used twice" in errors(pack)


def test_retired_live_record_and_number_above_next_are_errors(pack):
    def bad(d):
        d["retired"]["club"] = ["stoke"]
        d["ids"]["club"]["everton"] = d["next"] + 5
    edit(pack / "core" / "ids.json", bad)
    out = errors(pack)
    assert "`stoke` is retired but still a live record" in out
    assert "is not below `next`" in out


def test_new_pack_gets_its_own_block_of_numbers(pack):
    (pack / "index.txt").write_text("core\nmod\n", encoding="utf-8")
    (pack / "mod").mkdir()
    (pack / "mod" / "pack.json").write_text(json.dumps({"id": "mod", "name": "A mod", "version": 1, "dataVersion": 1,
                                                        "seasons": [], "files": ["clubs.json"]}), encoding="utf-8")
    (pack / "mod" / "clubs.json").write_text(json.dumps([{"id": "someone.new-club", "name": "New", "country": "england",
                                                         "sources": []}]), encoding="utf-8")
    assert "has no number in mod/ids.json" in errors(pack)
    assert assign_ids.assign(pack) == ["mod club `someone.new-club` -> 100000"]
    assert errors(pack) == ""
    assert "ids.json" in json.loads((pack / "mod" / "pack.json").read_text(encoding="utf-8"))["files"]


# ---------------------------------------------------------------------------------------------- results.json (E-00b)
import importlib.util  # noqa: E402
import results_csv_to_json  # noqa: E402

RESULTS = ("core", "seasons", "1888-89", "results.json")


def test_results_json_matches_the_research_csvs():
    assert (ROOT.joinpath(*RESULTS)).read_text(encoding="utf-8") == results_csv_to_json.dump(
        results_csv_to_json.build("1888-89"))


def test_results_json_gives_the_published_1888_89_table(monkeypatch):
    path = validate.DEFAULT_ROOT.parents[4] / "docs" / "data" / "check_results_1888-89.py"
    spec = importlib.util.spec_from_file_location("check_results", path)
    check = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(check)
    rows = check.read_rows(str(ROOT.joinpath(*RESULTS)))
    assert len(rows) == 132
    monkeypatch.setattr(sys, "argv", ["check_results_1888-89.py"])
    assert check.main() == 0  # default path = the JSON


def test_results_unknown_field_and_bad_club_are_errors(pack):
    def bad(d):
        d[0]["homeGaols"] = 1
        d[1]["away"] = "atlantis-fc"
    edit(pack.joinpath(*RESULTS), bad)
    out = errors(pack)
    assert "unknown field `homeGaols`" in out
    assert "unknown club `atlantis-fc`" in out


def test_result_is_played_or_walkover_and_in_its_own_season(pack):
    def bad(d):
        del d[0]["awayGoals"]                                      # played without a score
        d[1]["walkover"] = "notts-county"                          # walkover by a third club, with goals
        d[2]["edition"] = "fa-cup-1888-89"                         # fine: an edition of this season
        d[3]["edition"] = "football-league-1889-90"                # not an edition at all
    edit(pack.joinpath(*RESULTS), bad)
    out = errors(pack)
    assert "results.json[0]: a played match needs" in out
    assert "results.json[1]: a walkover has no goals" in out
    assert "is neither the home nor the away club" in out
    assert "results.json[2]" not in out
    assert "unknown edition `football-league-1889-90`" in out


# ---------------------------------------------------------------------------------------------- E-01 positions, dribbling
SQUADS = ("core", "seasons", "1888-89", "squads.json")


def test_every_shipped_squad_player_has_a_position_of_the_2_3_5():
    squads = json.loads(ROOT.joinpath(*SQUADS).read_text(encoding="utf-8"))
    for sq in squads:
        assert set(sq["positions"]) == set(sq["players"]), sq["club"]
        lines = [e["position"] for e in sq["positions"].values()]
        # every squad can field a 2-3-5: a keeper, two backs, three halves, each forward slot
        for slot, need in {"goalkeeper": 1, "full-back": 2, "half-back": 3, "right-wing": 1, "inside-right": 1,
                           "centre-forward": 1, "inside-left": 1, "left-wing": 1}.items():
            assert lines.count(slot) >= need, (sq["club"], slot)
    assert "no position" not in "\n".join(validate.validate(ROOT).warnings)


def test_position_and_dribbling_typos_are_errors(pack):
    def bad(d):
        first = d[0]["players"][0]
        d[0]["positions"][first] = {"position": "striker", "source": "players-1888-89", "foot": "left"}
        d[0]["positions"]["someone-else-1860"] = {"position": "goalkeeper", "source": "guess"}
        d[0]["dribbling"] = {first: {"value": 30, "source": ""}}
    edit(pack.joinpath(*SQUADS), bad)
    out = errors(pack)
    assert "expected one of" in out and "striker" in out
    assert "unknown field `foot`" in out
    assert "guess" in out
    assert "`someone-else-1860` is not a player of this squad" in out
    assert "value 30 is outside 3-23" in out
    assert "empty source" in out


def test_player_without_position_is_a_warning(pack):
    def drop(d):
        d[0]["positions"].pop(d[0]["players"][0])
    edit(pack.joinpath(*SQUADS), drop)
    rep = validate.validate(pack)
    assert rep.errors == []
    assert any("no position" in w for w in rep.warnings)


# ---------------------------------------------------------------------------------------------- R-02, R-11a
REC = ("core", "recruitment")


def test_shipped_recruitment_and_language_data_is_sourced():
    for f, field in (("rules.json", "rule"), ("wages.json", "type"), ("origins.json", "nudges")):
        rows = json.loads(ROOT.joinpath(*REC, f).read_text(encoding="utf-8"))
        assert rows and all(field in r for r in rows), f
    rules = {r["rule"] for r in json.loads(ROOT.joinpath(*REC, "rules.json").read_text(encoding="utf-8"))}
    # the dated rules of the brief: 1885 registration + residence, 1888 consent, 1890 wait, 1893 retain, 1901 cap
    assert {"registration", "residence", "second-registration-consent", "registration-wait", "retain-and-transfer",
            "max-wage"} <= rules
    rates = json.loads((ROOT / "core" / "language_rates.json").read_text(encoding="utf-8"))
    assert all(r["sources"] or r["confidence"] in validate.UNSOURCED_OK for r in rates)


def test_rule_needs_its_value_and_a_source(pack):
    def bad(d):
        d[0]["sources"] = []
        d[0]["rule"] = "registration-wait"
        d[1]["until"] = "1880"
        d.append({"id": "made-up", "rule": "bribery", "from": "1888", "body": "fa", "appliesTo": "all",
                  "confidence": "high", "sources": ["x"]})
    edit(pack.joinpath(*REC, "rules.json"), bad)
    out = errors(pack)
    assert "no source" in out
    assert "a `registration-wait` rule needs `days`" in out
    assert "`until` 1880 is before `from`" in out
    assert "bribery" in out


def test_wage_band_and_example_checks(pack):
    def bad(d):
        d[0].pop("weeklyPence")
        band = next(r for r in d if r["type"] == "band")
        band["minWeeklyPence"], band["maxWeeklyPence"] = 600, 300
        d[1]["club"] = "real-madrid"
    edit(pack.joinpath(*REC, "wages.json"), bad)
    out = errors(pack)
    assert "an `example` needs `weeklyPence`" in out
    assert "minWeeklyPence 600 is above maxWeeklyPence 300" in out
    assert "unknown club `real-madrid`" in out


def test_origin_nudges_are_small_and_refs_resolve(pack):
    def bad(d):
        d[0]["nudges"]["strength"] = 5
        d[0]["literacyShift"] = 2
        d[0]["sourceKinds"].append("academy")
        d[0]["occupations"].append("astronaut")
    edit(pack.joinpath(*REC, "origins.json"), bad)
    out = errors(pack)
    assert "a nudge is a whole number from -2 to 2" in out
    assert "literacyShift" in out
    assert "`academy` is not one of" in out
    assert "unknown occupation `astronaut`" in out


def test_new_records_need_numbers(pack):
    edit(pack.joinpath(*REC, "origin_clubs.json"),
         lambda d: d.append(dict(d[0], id="st-marys-junior", name="St Mary's")))
    assert "origin-club `st-marys-junior` has no number" in errors(pack)


def test_area_weights_add_up_per_country(pack):
    edit(pack / "core" / "areas.json", lambda d: d[0].update({"weight": 0.5}))
    assert "the area weights of `england` add up to" in errors(pack)


def test_rate_rows_are_consistent_and_never_overlap(pack):
    def bad(d):
        glam = next(r for r in d if r.get("area") == "glamorgan")
        glam["only"] = {"welsh": 0.9, "irish": 0.1}
        glam["speaks"]["english"] = 1.0
        d.append({"countries": ["england"], "bornFrom": 1860, "bornUntil": 1862, "signsMen": 0.9,
                  "readWriteIfSigns": [0.5, 0.5], "parentsBorn": {"atlantis": 0.1},
                  "confidence": "high", "sources": ["x"]})
        d.append({"area": "glamorgan", "countries": ["wales"], "bornFrom": 1900, "bornUntil": 1890,
                  "confidence": "estimate", "sources": []})
    edit(pack / "core" / "language_rates.json", bad)
    out = errors(pack)
    assert "0.9 speak only it but 0.495 speak it" in out
    assert "a language in `only` must be in `speaks`" in out
    assert "English is the default language" in out
    assert "`signsMen` for country england overlaps" in out
    assert "readWriteIfSigns" in out
    assert "unknown country `atlantis`" in out
    assert "an `area` or `countries`, not both" in out
    assert "bornFrom 1900 is after bornUntil 1890" in out
    assert "no rate" in out


def test_rates_need_a_default_row(pack):
    edit(pack / "core" / "language_rates.json",
         lambda d: d.remove(next(r for r in d if "area" not in r and "countries" not in r)))
    assert "no default row" in errors(pack)
