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
    shutil.copytree(ROOT.parent / "mods" / "language", tmp_path / "mods" / "language")
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


# ---------------------------------------------------------------------------------------------- G-04 contract bands
CONTRACTS = ("core", "seasons", "1888-89", "contracts.json")


def test_shipped_contract_bands_cover_every_standing_and_keep_the_sourced_1888_wages():
    bands = {b["standing"]: b for b in json.loads(ROOT.joinpath(*CONTRACTS).read_text(encoding="utf-8"))}
    assert set(bands) == {"league", "other", "amateur"}
    league = bands["league"]
    # Holt's 1888 terms (45s a week in the season, 20s in the summer) sit inside the League band
    assert league["seasonWageMin"] <= 45 <= league["seasonWageMax"]
    assert league["summerWageMin"] <= 20 <= league["summerWageMax"]
    assert bands["amateur"]["professionalShare"] == 0 and bands["amateur"]["clubs"]


def test_contract_band_typos_and_bad_ranges_are_errors(pack):
    def bad(d):
        d[0]["weeklyWage"] = 45
        d[0]["professionalShare"] = 1.5
        d[0]["seasonWageMin"] = 60
        d[1]["minimumSquad"] = 9
        d[1]["clubs"] = ["preston-north-end"]
        d[2]["clubs"].append("atlantis-fc")
        d.append(dict(d[0]))
    edit(pack.joinpath(*CONTRACTS), bad)
    out = errors(pack)
    assert "unknown field `weeklyWage`" in out
    assert "`professionalShare` 1.5 is outside 0-1" in out
    assert "`seasonWageMin` 60 is above `seasonWageMax` 50" in out
    assert "`minimumSquad` 9 cannot field eleven" in out
    assert "only the `amateur` band lists clubs" in out
    assert "unknown club `atlantis-fc`" in out
    assert "standing `league` has 2 bands" in out
FRIENDLIES = ("core", "seasons", "1888-89", "friendlies.json")


def test_shipped_friendly_terms_cover_every_standing_and_open_in_september():
    terms = {t["standing"]: t for t in json.loads(ROOT.joinpath(*FRIENDLIES).read_text(encoding="utf-8"))}
    assert set(terms) == {"league", "other", "amateur"}
    for t in terms.values():
        assert "saturday" in t["days"] and t["firstDay"] == "09-01"
    # a League club commands a larger guarantee than a club outside it
    assert terms["league"]["guarantee"] > terms["other"]["guarantee"]


def test_friendly_terms_typos_and_bad_values_are_errors(pack):
    def bad(d):
        d[0]["weekly"] = 1
        d[0]["days"].append("caturday")
        d[1]["chances"] = [1.5, 0.1]
        d[1]["gateShare"] = 2
        d[2]["leadDays"] = 1
        d[2]["firstDay"] = "13-01"
        d.append(dict(d[0]))
    edit(pack.joinpath(*FRIENDLIES), bad)
    out = errors(pack)
    assert "unknown field `weekly`" in out
    assert "not a weekday" in out
    assert "`chances` needs one chance per day of `days`" in out
    assert "`chances` 1.5 is outside 0-1" in out
    assert "`gateShare` 2 is outside 0-1" in out
    assert "`leadDays` 1 is under the two days a letter needs" in out
    assert "`firstDay` `13-01` is not a day MM-DD" in out
    assert "standing `league` has 2 records" in out
# ---- H-06: era events -------------------------------------------------------------------------------------------

def era(pack_root, name):
    return pack_root / "core" / "era" / name


def first_event(d, **fields):
    d[0].update(fields)


def test_era_files_are_shipped_and_counted():
    events = json.loads((ROOT / "core" / "era" / "events.json").read_text(encoding="utf-8"))
    base = json.loads((ROOT / "core" / "era" / "baseline.json").read_text(encoding="utf-8"))
    assert len(events) >= 30
    assert sum(len(g) for g in base["parameters"].values()) >= 40
    assert [e["effective"] for e in events] == sorted(e["effective"] for e in events)


def test_era_event_unknown_field_and_bad_enum(pack):
    edit(era(pack, "events.json"), lambda d: first_event(d, colour="red", kind="law"))
    e = errors(pack)
    assert "unknown field `colour`" in e
    assert "kind" in e and "one of" in e


def test_era_effect_param_must_exist_in_baseline(pack):
    edit(era(pack, "events.json"),
         lambda d: d[1]["effects"].append({"param": "rules.penaltyKik", "op": "set", "value": True}))
    assert "unknown parameter `rules.penaltyKik`" in errors(pack)


def test_era_effect_value_type_and_op(pack):
    edit(era(pack, "events.json"),
         lambda d: d[1]["effects"].extend([{"param": "rules.penaltyKick", "op": "set", "value": 3},
                                           {"param": "rules.officials", "op": "scale", "value": 2},
                                           {"param": "ball.massMinG", "op": "mul", "value": 2}]))
    e = errors(pack)
    assert "does not match the type of `rules.penaltyKick`" in e
    assert "`scale` needs a numeric parameter" in e
    assert "effects[4].op: expected one of" in e


def test_era_adoption_needs_number_and_seasons(pack):
    edit(era(pack, "events.json"),
         lambda d: d[1]["effects"].extend([{"param": "rules.goalNets", "op": "set", "value": True, "adoption": {"seasons": 3}},
                                           {"param": "kit.shinPadShare", "op": "set", "value": 1, "adoption": {"seasons": 0}},
                                           {"param": "kit.shinPadShare", "op": "set", "value": 1, "adoption": {}}]))
    e = errors(pack)
    assert "`adoption` needs a numeric parameter" in e
    assert "`seasons` must be at least 1" in e
    assert "missing field `seasons`" in e


def test_era_dates_are_checked(pack):
    edit(era(pack, "events.json"), lambda d: first_event(d, announced="1889-02-30", effective="1888-01-01"))
    assert "must be a calendar date" in errors(pack)
    edit(era(pack, "events.json"), lambda d: first_event(d, announced="1889-06-03", effective="1889-01-01"))
    assert "is before announced" in errors(pack)


def test_era_duplicate_id_and_unknown_requires(pack):
    edit(era(pack, "events.json"), lambda d: (d[1].update({"id": d[0]["id"]}), d[0].update({"requires": ["nope"]})))
    e = errors(pack)
    assert "duplicate event id" in e
    assert "requires unknown event `nope`" in e


def test_era_requires_must_not_take_effect_later(pack):
    def f(d):
        d[0]["requires"] = [d[-1]["id"]]
    edit(era(pack, "events.json"), f)
    assert "which takes effect later" in errors(pack)


def test_era_message_key_must_match_id_and_exist_in_every_bundle(pack):
    edit(era(pack, "events.json"), lambda d: d[1]["message"].update({"key": "era.other"}))
    assert "must be `era." in errors(pack)
    lang = pack.parent / "mods" / "language" / "BaseBundle_sp_la.properties"
    text = lang.read_text(encoding="utf-8")
    lang.write_text("".join(l for l in text.splitlines(True) if not l.startswith("era.ifab-1925-offside-two.body=")),
                    encoding="utf-8")
    e = errors(pack)
    assert "`era.ifab-1925-offside-two.body` missing or empty in BaseBundle_sp_la.properties" in e


def test_era_scope_refs_and_sources(pack):
    edit(era(pack, "events.json"), lambda d: first_event(d, scope={"competitions": ["no-such-cup"]}, sources=["not a link"]))
    e = errors(pack)
    assert "unknown competition `no-such-cup`" in e
    assert "expected an http(s) link" in e


def test_era_baseline_is_strict(pack):
    edit(era(pack, "baseline.json"), lambda d: (d["parameters"]["rules"].update({"bad": [1]}), d.update({"x": 1}),
                                               d["parameters"].update({"pitch": {"a": 1}})))
    e = errors(pack)
    assert "unknown field `x`" in e
    assert "unknown parameter group `pitch`" in e
    assert "value must be a number, true/false or text" in e


def test_every_baseline_parameter_is_used_or_documented_in_a_group():
    base = json.loads((ROOT / "core" / "era" / "baseline.json").read_text(encoding="utf-8"))
    assert set(base["parameters"]) == {"rules", "ball", "kit", "league"}
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


# ---- P2-14: the season calendar and the cup's neutral grounds (season.json) -------------------------------------
SEASON = ("core", "seasons", "1888-89", "season.json")


def fa_cup(d):
    return next(e for e in d["editions"] if e["competition"] == "fa-cup")


def test_shipped_calendar_and_neutral_grounds():
    season = json.loads(ROOT.joinpath(*SEASON).read_text(encoding="utf-8"))
    cal = season["calendar"]
    assert cal["league"]["opens"]["onOrAfter"] == "09-08" and cal["league"]["opens"]["weekday"] == "saturday"
    cup = cal["cups"][0]
    assert cup["competition"] == "fa-cup" and cup["laterDraws"] == "open-draw"
    assert cup["opens"]["onOrAfter"][:2] == "10", "the cup opens with the autumn qualifying rounds, not in May"
    assert cup["firstDraw"]["onOrAfter"][:2] == "01" and cup["firstDraw"]["confidence"] == "placeholder"
    neutral = fa_cup(season)["neutralVenues"]
    assert {r["round"]: r["grounds"] for r in neutral["rounds"]}["final"] == ["kennington-oval"]
    assert neutral["gate"] == "association"


def test_calendar_typos_are_errors(pack):
    def bad(d):
        cal = d["calendar"]
        cal["league"]["opens"]["weekday"] = "caturday"
        cal["league"]["closes"]["onOrAfter"] = "04-01"  # both anchors
        cal["league"]["noMatchOn"] = ["12-32"]
        cup = cal["cups"][0]
        cup["firstDraw"].pop("onOrAfter")  # no anchor
        cup["laterDraws"] = "lottery"
        cup["daysBetweenRounds"] = 0
        cup["kickOff"] = "15:00"
        cal["cups"].append(dict(cup))  # a second fa-cup calendar
    edit(pack.joinpath(*SEASON), bad)
    out = errors(pack)
    assert "caturday" in out
    assert out.count("exactly one of `onOrAfter` / `onOrBefore`") >= 2
    assert "12-32" in out
    assert "lottery" in out
    assert "daysBetweenRounds: 0 is outside 1-60" in out
    assert "unknown field `kickOff`" in out
    assert "`fa-cup` has 2 calendars" in out


def test_neutral_grounds_typos_are_errors(pack):
    def bad(d):
        neutral = fa_cup(d)["neutralVenues"]
        neutral["rounds"][0]["grounds"] = ["wembley"]
        neutral["rounds"][1]["round"] = "semi-final"  # listed twice
        neutral["gate"] = "visitors"
        neutral["rounds"].append({"round": "final", "grounds": [], "confidence": "unverified"})
    edit(pack.joinpath(*SEASON), bad)
    out = errors(pack)
    assert "unknown ground `wembley`" in out
    assert "`semi-final` listed 2 times" in out
    assert "visitors" in out
    assert "needs at least one ground" in out


def test_a_season_without_calendar_is_valid(pack):
    def drop(d):
        d.pop("calendar")
        fa_cup(d).pop("neutralVenues")
    edit(pack.joinpath(*SEASON), drop)
    assert errors(pack) == ""


# G-02 injuries (DATA_MODEL.md "Injuries")
def test_shipped_injuries_have_the_three_kinds():
    rep = validate.validate(ROOT)
    assert rep.errors == []
    data = json.loads((ROOT / "core" / "injuries.json").read_text(encoding="utf-8"))
    assert sorted(r["kind"] for r in data) == ["carried-off", "knock", "passenger"]


def test_injury_typos_are_errors(pack):
    def bad(d):
        d[0]["minDays"], d[0]["maxDays"] = 9, 3
        d[1]["chancePerMatch"] = 1.5
        d[2]["kind"] = "knock"
        d[2]["sources"] = []
        d[2]["confidence"] = "high"
    edit(pack.joinpath("core", "injuries.json"), bad)
    out = errors(pack)
    assert "minDays 9 is above maxDays 3" in out
    assert "must be from 0 to 1" in out
    assert "injury kind `knock` is listed twice" in out
    assert "no source" in out
