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
                              positions={k: v for k, v in sq["positions"].items() if k != "fred-dewhurst-1863"},
                              fanTiers={k: v for k, v in sq.get("fanTiers", {}).items() if k != "fred-dewhurst-1863"})
                    for sq in d])
    edit(pack / "core" / "text" / "en" / "history.json", lambda d: d["person"].pop("fred-dewhurst-1863", None))
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


# ---------------------------------------------------------------------------------------------- history data (H-05)
TEXT_EN = ("core", "text", "en", "history.json")
SQUADS = ("core", "seasons", "1888-89", "squads.json")


def add_lang(pack, lang="es", club_keys=("stoke",)):
    src = json.loads((pack / "core" / "text" / "en" / "history.json").read_text(encoding="utf-8"))
    out = {"club": {k: src["club"][k] for k in club_keys if k in src["club"]}, "person": {}}
    (pack / "core" / "text" / lang).mkdir(parents=True)
    (pack / "core" / "text" / lang / "history.json").write_text(json.dumps(out), encoding="utf-8")
    edit(pack / "core" / "pack.json", lambda d: d["files"].append(f"text/{lang}/history.json"))
    return out


def test_shipped_history_covers_the_league_clubs_and_tiers():
    text = json.loads(ROOT.joinpath(*TEXT_EN).read_text(encoding="utf-8"))
    league = ["preston-north-end", "aston-villa", "wolverhampton-wanderers", "blackburn-rovers", "bolton-wanderers",
              "west-bromwich-albion", "accrington", "everton", "burnley", "derby-county", "notts-county", "stoke"]
    clubs = {c["id"]: c for c in json.loads((ROOT / "core" / "clubs.json").read_text(encoding="utf-8"))}
    squads = {s["club"]: s for s in json.loads(ROOT.joinpath(*SQUADS).read_text(encoding="utf-8"))}
    for cid in league:
        assert text["club"][cid]["hook"] and text["club"][cid]["history"] and "1888-89" in text["club"][cid]["seasons"]
        assert "origin" in clubs[cid]
        assert squads[cid]["fanTiers"], cid
    assert squads["aston-villa"]["fanTiers"]["archie-hunter-1859"] == 5


def test_history_text_key_must_resolve(pack):
    edit(pack / "core" / "text" / "en" / "history.json", lambda d: d["club"].update({"atlantis-fc": d["club"]["stoke"]}))
    assert "unknown club `atlantis-fc`" in errors(pack)


def test_history_text_follows_aliases(pack):
    edit(pack / "core" / "clubs.json", lambda d: [c.update({"id": "stoke-fc"}) for c in d if c["id"] == "stoke"])
    edit(pack / "core" / "aliases.json", lambda d: d["club"].update({"stoke": "stoke-fc"}))
    out = errors(pack)
    assert "unknown club" not in out and "history.json" not in out


def test_history_entry_needs_sources_and_has_no_unknown_fields(pack):
    def bad(d):
        d["club"]["stoke"]["sources"] = []
        d["club"]["everton"]["motto"] = "x"
        d["person"]["john-goodall-1863"]["sources"] = ["not a link"]
        del d["club"]["burnley"]["hook"]
    edit(pack / "core" / "text" / "en" / "history.json", bad)
    out = errors(pack)
    assert "club.stoke.sources: empty" in out
    assert "unknown field `motto`" in out
    assert "is not a link" in out
    assert "missing field `hook`" in out


def test_history_season_key_must_be_a_season(pack):
    edit(pack / "core" / "text" / "en" / "history.json", lambda d: d["club"]["stoke"]["seasons"].update({"1066-67": "x"}))
    assert "unknown season `1066-67`" in errors(pack)


def test_translation_may_only_hold_keys_of_en(pack):
    add_lang(pack, "es", ("stoke",))
    rep = validate.validate(pack)
    assert rep.errors == [] and any("not translated" in w for w in rep.warnings)
    edit(pack / "core" / "text" / "es" / "history.json", lambda d: d["club"].update({"everton-9": d["club"]["stoke"]}))
    assert "unknown club `everton-9`" in errors(pack)
    # a key that resolves but has no English entry is an orphan
    edit(pack / "core" / "text" / "es" / "history.json",
         lambda d: (d["club"].pop("everton-9"), d["club"].update({"swifts": d["club"]["stoke"]})))
    edit(pack / "core" / "text" / "en" / "history.json", lambda d: d["club"].pop("swifts", None))
    assert "`swifts` has no entry in text/en (orphan translation)" in errors(pack)


def test_language_code(pack):
    add_lang(pack, "english", ())
    assert "is not a language code" in errors(pack)


def test_fan_tier_rules(pack):
    def bad(d):
        sq = d[0]
        sq["fanTiers"]["john-goodall-1863"] = 6
        sq["fanTiers"]["archie-hunter-1859"] = 3      # a Villa player, not in Preston's squad
    edit(pack / "core" / "seasons" / "1888-89" / "squads.json", bad)
    out = errors(pack)
    assert "tier must be a whole number 0-5" in out
    assert "`archie-hunter-1859` is not a player of this squad" in out


def test_fan_tier_must_be_a_known_person(pack):
    edit(pack / "core" / "seasons" / "1888-89" / "squads.json",
         lambda d: d[0]["fanTiers"].update({"nobody-1900": 1}))
    assert "unknown person `nobody-1900`" in errors(pack)


def test_nickname_needs_sources_and_a_forward_period(pack):
    def bad(d):
        d[0]["nicknames"] = [{"name": "The Invincibles", "from": 1890, "to": 1883, "sources": []},
                             {"name": "", "sources": ["https://example.org"], "colour": 1}]
        d[0]["origin"] = "pub"
    edit(pack / "core" / "clubs.json", bad)
    out = errors(pack)
    assert "nicknames[0].sources: empty" in out
    assert "`from` 1890 is after `to` 1883" in out
    assert "unknown field `colour`" in out
    assert ".name: empty" in out
    assert "origin" in out and "one of" in out


def test_person_nicknames_and_known_as(pack):
    def add(d):
        d[0]["nicknames"] = [{"name": "Dewy", "period": True, "sources": ["https://example.org/x"]}]
        d[0]["knownAs"] = "Freddie Dewhurst"
    edit(pack / "core" / "people.json", add)
    assert errors(pack) == ""


def test_rivals_resolve_and_one_sided_rivalry_is_a_warning(pack):
    edit(pack / "core" / "clubs.json", lambda d: [c.update({"rivals": []}) for c in d if c["id"] == "blackburn-rovers"])
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"rivals": ["stoke"]}))
    rep = validate.validate(pack)
    assert rep.errors == [] and any("does not list `preston-north-end` back" in w for w in rep.warnings)
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"rivals": ["preston-north-end", "atlantis"]}))
    out = errors(pack)
    assert "unknown club `atlantis`" in out and "not its own rival" in out


def test_name_history_needs_from_and_sources(pack):
    edit(pack / "core" / "clubs.json", lambda d: d[0].update({"nameHistory": [{"name": "North End"}]}))
    out = errors(pack)
    assert "missing field `from`" in out and "missing field `sources`" in out
