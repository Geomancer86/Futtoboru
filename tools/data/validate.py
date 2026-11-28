"""Validate Futtoboru game data packs (gamedata/<pack>/...).

Same rules as DarkBlade's book validator: a typo in a field name is an error, every
reference must resolve (after aliases), ids are stable lower-case slugs and unique per
kind across all packs, and every record has a runtime number in its pack's append-only
`ids.json` registry (see `assign_ids.py`). Suspicious but possible data (a person at two clubs, a short
squad, an odd age) is a warning: it goes to the human review, it doesn't block.

Usage:  python tools/data/validate.py [--root DIR] [--quiet]
Exit 1 when there is any error.
"""
from __future__ import annotations

import argparse
import json
import re
from datetime import date
import sys
from collections import Counter, defaultdict
from pathlib import Path

DEFAULT_ROOT = Path(__file__).resolve().parents[2] / "futtoboru-core" / "src" / "main" / "resources" / "gamedata"

ID_RE = re.compile(r"^([a-z0-9]+(-[a-z0-9]+)*\.)?[a-z0-9]+(-[a-z0-9]+)*$")  # core: slug; mods: author.slug
DATE_RE = re.compile(r"^\d{4}(-(0[1-9]|1[0-2])(-(0[1-9]|[12]\d|3[01]))?)?$")
DATETIME_RE = re.compile(r"^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$")
ATTRS = {"strength", "endurance", "stamina", "speed", "acceleration", "jumping", "dexterity", "concentration",
         "courage", "determination", "leadership", "perception", "positioning", "teamwork"}
CONFIDENCE = {"verified", "unverified", "placeholder"}
OCCUPATION_CATEGORIES = {"TEXTILE", "RAILWAY", "MINING", "CONSTRUCTION", "FACTORY", "TRANSPORT", "EDUCATION",
                         "SKILLED_TRADE", "AGRICULTURE", "SERVICE", "OTHER"}
COMPETITION_TYPES = {"LEAGUE", "CUP"}
# E-01: the slots of the 1888 2-3-5 (DATA_MODEL.md "Positions and dribbling"); a squad entry's position this season
POSITIONS = {"goalkeeper", "full-back", "half-back", "right-wing", "inside-right", "centre-forward", "inside-left",
             "left-wing"}
# where a position comes from: the slot named in docs/data/history/PLAYERS_1888-89.md, only the line named there
# (forward / winger / inside forward / defender; the slot within it assigned), or a placeholder to fill the 2-3-5.
# H-04: the club's 1888-89 Wikipedia season article (slot / line), the player's own Wikipedia article (slot / line),
# the 1889 FA Cup final line-up; docs/data/history/POSITIONS_1888-89.md lists each with its URLs
POSITION_SOURCES = {"players-1888-89", "players-1888-89-line", "placeholder", "wp-season-1888-89",
                    "wp-season-1888-89-line", "wp-player", "wp-player-line", "fa-cup-final-1889"}
ATTR_MIN, ATTR_MAX = 3, 23  # the attribute scale (3d6 plus modifiers, PlayerAttributeGenerator)
KINDS = ["continent", "country", "profession", "occupation", "competition", "club", "ground", "person", "region",
         "season", "edition"]

# H-06 era events (docs/design/ERA_EVENTS_PROPOSAL.md, D30): gamedata/<pack>/era/{baseline,events}.json
ERA_KINDS = {"rule", "equipment", "ball", "authority"}
ERA_AUTHORITIES = {"ifab", "fa", "football-league", "press"}
ERA_CONFIDENCE = {"verified", "unverified", "disputed"}
ERA_CERTAINTY = {"day", "month", "year", "season"}
ERA_OPS = {"set", "scale", "add"}
ERA_CATEGORIES = {"AUTHORITY", "LEAGUE", "SYSTEM"}
ERA_PRIORITIES = {"LOW", "NORMAL", "HIGH"}
ERA_GROUPS = ("rules", "ball", "kit", "league")
BUNDLES = ("BaseBundle_en_us", "BaseBundle_en_uk", "BaseBundle_sp_sp", "BaseBundle_sp_la")

# field spec: name -> (type, required). Types: str, int, num, bool, date, datetime, strs, attrs,
# ref:<kind>, refs:<kind>, enum:<name>, list:<record spec name>, map:<record spec name> (an object: id -> record)
ENUMS = {"era_kind": ERA_KINDS, "era_authority": ERA_AUTHORITIES, "era_confidence": ERA_CONFIDENCE,
         "era_certainty": ERA_CERTAINTY, "era_op": ERA_OPS, "era_category": ERA_CATEGORIES,
         "era_priority": ERA_PRIORITIES, "confidence": CONFIDENCE, "occupation_category": OCCUPATION_CATEGORIES, "competition_type": COMPETITION_TYPES,
         "position": POSITIONS, "position_source": POSITION_SOURCES}
SPECS = {
    "era_event": {"id": ("str", True), "kind": ("enum:era_kind", True), "authority": ("enum:era_authority", True),
                  "announced": ("str", True), "effective": ("str", True),
                  "dateCertainty": ("enum:era_certainty", False), "scope": ("obj:era_scope", False),
                  "effects": ("list:era_effect", True), "message": ("obj:era_message", True),
                  "requires": ("strs", False), "confidence": ("enum:era_confidence", True),
                  "sources": ("strs", True), "notes": ("str", False)},
    "era_scope": {"countries": ("refs:country", False), "competitions": ("refs:competition", False)},
    "era_effect": {"param": ("str", True), "op": ("enum:era_op", True), "value": ("any", True), "adoption": ("obj:era_adoption", False)},
    "era_adoption": {"seasons": ("int", True), "from": ("num", False)},
    "era_message": {"key": ("str", True), "args": ("strs", False), "category": ("enum:era_category", False),
                    "priority": ("enum:era_priority", False), "mandatory": ("bool", False),
                    "newsOnly": ("bool", False)},
    "continent": {"id": ("str", True), "name": ("str", True)},
    "country": {"id": ("str", True), "name": ("str", True), "formalName": ("str", False),
                "continent": ("ref:continent", True), "selectableByDefault": ("bool", False)},
    "profession": {"id": ("str", True), "name": ("str", True), "playerSelectable": ("bool", True)},
    "occupation": {"id": ("str", True), "name": ("str", True),
                   "category": ("enum:occupation_category", True), "bonuses": ("attrs", True), "notes": ("str", False)},
    "names": {"country": ("ref:country", True), "firstNames": ("strs", True), "lastNames": ("strs", True)},
    "competition": {"id": ("str", True), "name": ("str", True), "longName": ("str", False),
                    "type": ("enum:competition_type", True), "sources": ("strs", True), "notes": ("str", False),
                    # G-08: the bottom clubs seek re-election at the annual meeting (seasons starting in From..Until)
                    "reElectionPlaces": ("int", False), "reElectionFrom": ("int", False),
                    "reElectionUntil": ("int", False), "annualMeetingAfterDays": ("int", False),
                    "annualMeetingConfidence": ("enum:confidence", False)},
    "club": {"id": ("str", True), "name": ("str", True), "fullName": ("str", False),
             "founded": ("int", False), "country": ("ref:country", True), "sources": ("strs", True),
             "notes": ("str", False)},
    "ground": {"id": ("str", True), "name": ("str", True), "opened": ("int", False),
               "capacity": ("int", False), "value": ("num", False), "description": ("str", False),
               "landValue": ("num", False), "ownedByClub": ("bool", False), "annualRent": ("num", False),
               "sources": ("strs", True), "notes": ("str", False)},
    "person": {"id": ("str", True), "firstName": ("str", True), "lastName": ("str", True), "born": ("date", True),
               "country": ("ref:country", True), "confidence": ("enum:confidence", True), "sources": ("strs", True),
               "notes": ("str", False)},
    "season": {"id": ("str", True), "name": ("str", True), "description": ("str", False),
               "start": ("datetime", True), "sources": ("strs", True), "countries": ("refs:country", True),
               "editions": ("list:edition", True)},
    "edition": {"id": ("str", True), "competition": ("ref:competition", True),
                "name": ("str", True), "participants": ("refs:club", True), "sources": ("strs", True),
                "notes": ("str", False)},
    "squad": {"club": ("ref:club", True), "ground": ("ref:ground", False), "balance": ("num", False),
              "players": ("refs:person", True), "notes": ("str", False),
              # E-01, keyed by a person id of `players`: this season's position, and a sourced dribbling value
              "positions": ("map:squad_position", False), "dribbling": ("map:squad_dribbling", False)},
    "squad_position": {"position": ("enum:position", True), "source": ("enum:position_source", True)},
    "squad_dribbling": {"value": ("int", True), "source": ("str", True)},
    "nationality_modifier": {"country": ("ref:country", True), "modifiers": ("attrs", True), "notes": ("str", False),
                             "footballingStrength": ("num", False)},
    "region": {"id": ("str", True), "name": ("str", True), "country": ("ref:country", True),
               "modifiers": ("attrs", True), "places": ("strs", False), "notes": ("str", False)},
    # one real match of a season (E-00b): reference data only (D17), never used to schedule or decide a match
    "result": {"edition": ("ref:edition", True), "round": ("str", False), "date": ("date", False),
               "home": ("ref:club", True), "away": ("ref:club", True), "homeGoals": ("int", False),
               "awayGoals": ("int", False), "walkover": ("ref:club", False), "attendance": ("int", False),
               "venue": ("str", False), "source": ("str", True), "notes": ("str", False)},
    "pack": {"id": ("str", True), "name": ("str", True), "version": ("int", True), "dataVersion": ("int", True),
             "seasons": ("strs", True), "files": ("strs", True)},
}
# pack-level file -> (record spec, id kind or None)
PACK_FILES = {
    "continents.json": ("continent", "continent"), "countries.json": ("country", "country"),
    "professions.json": ("profession", "profession"), "occupations.json": ("occupation", "occupation"),
    "names.json": ("names", None), "competitions.json": ("competition", "competition"),
    "clubs.json": ("club", "club"), "grounds.json": ("ground", "ground"), "people.json": ("person", "person"),
}
SEASON_FILES = {
    "squads.json": ("squad", None), "nationality_modifiers.json": ("nationality_modifier", None),
    "region_modifiers.json": ("region", "region"),
    "results.json": ("result", None),
}


class Report:
    def __init__(self):
        self.errors: list[str] = []
        self.warnings: list[str] = []

    def error(self, where: str, msg: str):
        self.errors.append(f"{where}: {msg}")

    def warn(self, where: str, msg: str):
        self.warnings.append(f"{where}: {msg}")


def check_record(rec, spec_name: str, where: str, rep: Report, refs: list):
    spec = SPECS[spec_name]
    if not isinstance(rec, dict):
        rep.error(where, f"expected an object, got {type(rec).__name__}")
        return
    for k in rec:
        if k not in spec:
            rep.error(where, f"unknown field `{k}` (allowed: {', '.join(spec)})")
    for k, (typ, required) in spec.items():
        if k not in rec or rec[k] is None:
            if required:
                rep.error(where, f"missing field `{k}`")
            continue
        check_value(rec[k], typ, f"{where}.{k}", rep, refs)
    if "id" in spec and isinstance(rec.get("id"), str) and not ID_RE.match(rec["id"]):
        rep.error(where, f"bad id `{rec['id']}` (lower-case slug like `preston-north-end`; mods: `author.slug`)")


def check_value(v, typ: str, where: str, rep: Report, refs: list):
    def bad(expected):
        rep.error(where, f"expected {expected}, got {json.dumps(v, ensure_ascii=False)[:60]}")

    if typ == "str":
        isinstance(v, str) or bad("text")
    elif typ == "int":
        (isinstance(v, int) and not isinstance(v, bool)) or bad("a whole number")
    elif typ == "num":
        (isinstance(v, (int, float)) and not isinstance(v, bool)) or bad("a number")
    elif typ == "bool":
        isinstance(v, bool) or bad("true/false")
    elif typ == "date":
        (isinstance(v, str) and DATE_RE.match(v)) or bad("a date yyyy, yyyy-mm or yyyy-mm-dd")
    elif typ == "datetime":
        (isinstance(v, str) and DATETIME_RE.match(v)) or bad("a date-time yyyy-mm-ddThh:mm:ss")
    elif typ == "strs":
        (isinstance(v, list) and all(isinstance(x, str) for x in v)) or bad("a list of texts")
    elif typ == "attrs":
        if not isinstance(v, dict):
            return bad("an object of attribute numbers")
        for k, x in v.items():
            if k not in ATTRS:
                rep.error(where, f"unknown attribute `{k}`")
            elif not isinstance(x, (int, float)) or isinstance(x, bool):
                rep.error(f"{where}.{k}", "expected a number")
    elif typ.startswith("enum:"):
        v in ENUMS[typ[5:]] or bad(f"one of {sorted(ENUMS[typ[5:]])}")
    elif typ.startswith("ref:"):
        isinstance(v, str) and refs.append((typ[4:], v, where)) or (not isinstance(v, str) and bad("an id"))
    elif typ.startswith("refs:"):
        if not (isinstance(v, list) and all(isinstance(x, str) for x in v)):
            return bad("a list of ids")
        for i, x in enumerate(v):
            refs.append((typ[5:], x, f"{where}[{i}]"))
        for x, n in Counter(v).items():
            if n > 1:
                rep.error(where, f"`{x}` listed {n} times")
    elif typ == "any":
        pass
    elif typ.startswith("obj:"):
        check_record(v, typ[4:], where, rep, refs)
    elif typ.startswith("list:"):
        if not isinstance(v, list):
            return bad("a list")
        for i, x in enumerate(v):
            check_record(x, typ[5:], f"{where}[{i}]", rep, refs)
    elif typ.startswith("map:"):
        if not isinstance(v, dict):
            return bad("an object of id -> record")
        for k, x in v.items():
            check_record(x, typ[4:], f"{where}.{k}", rep, refs)


def check_squad_roles(sq, where: str, rep: Report):
    """E-01: `positions` / `dribbling` keys are players of this squad; dribbling is on the attribute scale and
    sourced. A player without a position is a warning (the game then has no position for him)."""
    if not isinstance(sq, dict):
        return
    players = sq.get("players") if isinstance(sq.get("players"), list) else []
    for field in ("positions", "dribbling"):
        table = sq.get(field)
        if not isinstance(table, dict):
            continue
        for pid, x in table.items():
            if pid not in players:
                rep.error(f"{where}.{field}", f"`{pid}` is not a player of this squad")
            if field == "dribbling" and isinstance(x, dict):
                val, src = x.get("value"), x.get("source")
                if isinstance(val, int) and not isinstance(val, bool) and not ATTR_MIN <= val <= ATTR_MAX:
                    rep.error(f"{where}.dribbling.{pid}", f"value {val} is outside {ATTR_MIN}-{ATTR_MAX}")
                if isinstance(src, str) and not src.strip():
                    rep.error(f"{where}.dribbling.{pid}", "empty source (a sourced value needs its source)")
    positions = sq.get("positions") if isinstance(sq.get("positions"), dict) else {}
    for pid in players:
        if isinstance(pid, str) and pid not in positions:
            rep.warn(f"{where} `{pid}`", "no position")


def check_results(data: list, where: str, sid: str, season: dict | None, rep: Report):
    """A result is either played (both goals, a date) or a walkover (`walkover` = one of the two clubs, no goals);
    its edition belongs to its own season. Checked here because the field spec can't say "one or the other"."""
    own = {e.get("id") for e in (season or {}).get("editions") or [] if isinstance(e, dict)}
    for i, r in enumerate(data):
        if not isinstance(r, dict):
            continue
        rw = f"{where}[{i}]"
        goals = [r.get("homeGoals"), r.get("awayGoals")]
        if r.get("walkover") is not None:
            if any(g is not None for g in goals):
                rep.error(rw, "a walkover has no goals")
            if r["walkover"] not in (r.get("home"), r.get("away")):
                rep.error(rw, f"walkover `{r['walkover']}` is neither the home nor the away club")
        elif any(g is None for g in goals) or r.get("date") is None:
            rep.error(rw, "a played match needs `date`, `homeGoals` and `awayGoals` (or `walkover`)")
        if r.get("home") is not None and r.get("home") == r.get("away"):
            rep.error(rw, "home and away are the same club")
        if isinstance(r.get("edition"), str) and r["edition"] not in own:
            rep.error(rw, f"edition `{r['edition']}` is not an edition of season {sid}")


def parse_date(v) -> date | None:
    """A whole calendar date yyyy-mm-dd, or None."""
    if not isinstance(v, str) or not re.fullmatch(r"\d{4}-\d{2}-\d{2}", v):
        return None
    try:
        return date.fromisoformat(v)
    except ValueError:
        return None


def is_number(v) -> bool:
    return isinstance(v, (int, float)) and not isinstance(v, bool)


def read_bundle_keys(root: Path) -> dict[str, set[str]] | None:
    """Bundle name -> keys with a non-empty value (mods/language next to the gamedata folder), or None."""
    folder = root.parent / "mods" / "language"
    if not folder.is_dir():
        return None
    out: dict[str, set[str]] = {}
    for b in BUNDLES:
        keys: set[str] = set()
        f = folder / f"{b}.properties"
        if f.exists():
            for line in f.read_text(encoding="utf-8").splitlines():
                if "=" in line and not line.lstrip().startswith("#"):
                    k, v = line.split("=", 1)
                    if v.strip():
                        keys.add(k.strip())
        out[b] = keys
    return out


def check_era(root: Path, baselines: list, events: list, rep: Report, refs: list):
    """H-06: era/baseline.json (the 1888-89 value of every parameter) and era/events.json (dated changes)."""
    params: dict[str, object] = {}   # "rules.penaltyKick" -> baseline value
    base_date = None
    if len(baselines) > 1:
        rep.error("era/baseline.json", "more than one pack has a baseline (only core does)")
    for data, where in baselines:
        if not isinstance(data, dict):
            rep.error(where, "expected an object")
            continue
        for k in data:
            if k not in ("date", "description", "parameters"):
                rep.error(where, f"unknown field `{k}` (allowed: date, description, parameters)")
        base_date = parse_date(data.get("date"))
        if base_date is None:
            rep.error(where, "`date` must be a calendar date yyyy-mm-dd")
        if "description" in data and not isinstance(data["description"], str):
            rep.error(where, "`description` must be text")
        groups = data.get("parameters")
        if not isinstance(groups, dict):
            rep.error(where, "`parameters` must be an object of groups")
            continue
        for g, table in groups.items():
            if g not in ERA_GROUPS:
                rep.error(where, f"unknown parameter group `{g}` (allowed: {', '.join(ERA_GROUPS)})")
                continue
            if not isinstance(table, dict) or not table:
                rep.error(f"{where}.{g}", "expected a non-empty object of parameters")
                continue
            for name, val in table.items():
                if not re.fullmatch(r"[a-z][A-Za-z0-9]*", name):
                    rep.error(f"{where}.{g}.{name}", "bad parameter name (camelCase)")
                if not (isinstance(val, (bool, str)) or is_number(val)):
                    rep.error(f"{where}.{g}.{name}", "value must be a number, true/false or text")
                else:
                    params[f"{g}.{name}"] = val
        for g in ERA_GROUPS:
            if g not in groups:
                rep.error(where, f"missing parameter group `{g}`")

    bundles = read_bundle_keys(root)
    seen: dict[str, str] = {}      # event id -> where
    eff_date: dict[str, date] = {}  # event id -> effective
    pending_requires: list[tuple[str, str, str]] = []
    for data, where in events:
        if not isinstance(data, list):
            rep.error(where, "expected a list of events")
            continue
        for i, ev in enumerate(data):
            ew = f"{where}[{i}]" + (f" `{ev.get('id')}`" if isinstance(ev, dict) and ev.get("id") else "")
            check_record(ev, "era_event", ew, rep, refs)
            if not isinstance(ev, dict):
                continue
            eid = ev.get("id")
            if isinstance(eid, str):
                if eid.lower() in seen:
                    rep.error(ew, f"duplicate event id `{eid}` (also {seen[eid.lower()]})")
                seen[eid.lower()] = ew
            ann, eff = parse_date(ev.get("announced")), parse_date(ev.get("effective"))
            for fld, d in (("announced", ann), ("effective", eff)):
                if fld in ev and d is None:
                    rep.error(f"{ew}.{fld}", "must be a calendar date yyyy-mm-dd")
            if ann and eff and eff < ann:
                rep.error(ew, f"effective {eff} is before announced {ann}")
            if isinstance(eid, str) and eff:
                eff_date[eid.lower()] = eff
            # sources: links
            for j, src in enumerate(ev.get("sources") or []):
                if isinstance(src, str) and not re.match(r"https?://\S+$", src):
                    rep.error(f"{ew}.sources[{j}]", "expected an http(s) link")
            if isinstance(ev.get("sources"), list) and not ev["sources"]:
                rep.error(ew, "needs at least one source")
            for r in ev.get("requires") or []:
                pending_requires.append((ew, eid, r))
            # effects
            for j, ef in enumerate(ev.get("effects") or []):
                fw = f"{ew}.effects[{j}]"
                if not isinstance(ef, dict) or not isinstance(ef.get("param"), str):
                    continue
                p = ef["param"]
                if p not in params:
                    rep.error(fw, f"unknown parameter `{p}` (not in era/baseline.json)")
                    continue
                base, op, val = params[p], ef.get("op"), ef.get("value")
                if "value" not in ef:
                    continue
                if op == "set":
                    ok = (isinstance(base, bool) and isinstance(val, bool)) or \
                         (isinstance(base, str) and isinstance(val, str)) or \
                         (is_number(base) and is_number(val))
                    if not ok:
                        rep.error(fw, f"value {json.dumps(val)} does not match the type of `{p}` ({type(base).__name__})")
                elif op in ("scale", "add"):
                    if not (is_number(base) and is_number(val)):
                        rep.error(fw, f"`{op}` needs a numeric parameter and a number (`{p}`)")
                if "adoption" in ef:
                    if not is_number(base):
                        rep.error(fw, "`adoption` needs a numeric parameter")
                    ad = ef["adoption"]
                    if isinstance(ad, dict) and isinstance(ad.get("seasons"), int) and ad["seasons"] < 1:
                        rep.error(f"{fw}.adoption", "`seasons` must be at least 1")
            # messages
            msg = ev.get("message")
            if isinstance(msg, dict) and isinstance(msg.get("key"), str) and isinstance(eid, str):
                key = msg["key"]
                if key != f"era.{eid}":
                    rep.error(f"{ew}.message.key", f"must be `era.{eid}` (language keys `<key>.title` and `<key>.body`)")
                elif bundles is not None:
                    for b, keys in bundles.items():
                        for suffix in ("title", "body"):
                            if f"{key}.{suffix}" not in keys:
                                rep.error(f"{ew}.message", f"`{key}.{suffix}` missing or empty in {b}.properties")
                args = msg.get("args")
                if isinstance(args, list) and not args:
                    rep.error(f"{ew}.message.args", "empty list: leave it out")
    for ew, eid, r in pending_requires:
        if r.lower() not in eff_date:
            rep.error(ew, f"requires unknown event `{r}`")
        elif eid and eid.lower() in eff_date and eff_date[r.lower()] > eff_date[eid.lower()]:
            rep.error(ew, f"requires `{r}`, which takes effect later")
        elif r.lower() == (eid or "").lower():
            rep.error(ew, "requires itself")


def load_json(path: Path, rep: Report, where: str):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        rep.error(where, "file listed but missing")
    except (UnicodeDecodeError, json.JSONDecodeError) as e:
        rep.error(where, f"not valid UTF-8 JSON: {e}")
    return None


class Scan:
    """Everything read from the packs: records with ids in load order, aliases, registries."""

    def __init__(self, root: Path):
        self.root = root
        self.rep = Report()
        self.ids: dict[str, dict[str, str]] = {k: {} for k in KINDS}   # kind -> lower id -> where
        self.aliases: dict[str, dict[str, str]] = {k: {} for k in KINDS}
        self.records: list[tuple[str, str, str, str]] = []  # (pack, kind, lower id, where) in load order
        self.registries: dict[str, tuple[dict, str]] = {}  # pack -> (ids.json content, where)
        self.packs: list[str] = []
        self.squads_by_season: dict[str, list] = {}


def scan(root: Path = DEFAULT_ROOT) -> Scan:
    """Reads and checks every pack (records, references, aliases); the registry is checked by `validate`."""
    s = Scan(root)
    rep, ids, aliases = s.rep, s.ids, s.aliases
    refs: list[tuple[str, str, str]] = []
    seasons: dict[str, dict] = {}
    squads_by_season = s.squads_by_season
    results: list[tuple[list, str, str]] = []  # (records, where, season): checked once every season.json is read
    era_baselines: list[tuple[object, str]] = []
    era_events: list[tuple[object, str]] = []

    index = root / "index.txt"
    if not index.exists():
        rep.error("index.txt", "missing (lists the packs to load)")
        return s
    packs = [l.strip() for l in index.read_text(encoding="utf-8").splitlines() if l.strip() and not l.startswith("#")]
    s.packs = packs
    on_disk = sorted(p.name for p in root.iterdir() if p.is_dir())
    for p in on_disk:
        if p not in packs:
            rep.error("index.txt", f"pack folder `{p}` is not listed")

    pack = None

    def register(kind, rec, where):
        rid = rec.get("id")
        if not isinstance(rid, str):
            return
        low = rid.lower()
        if low in ids[kind]:
            rep.error(where, f"duplicate {kind} id `{rid}` (also {ids[kind][low]})")
        else:
            ids[kind][low] = where
            s.records.append((pack, kind, low, where))

    for pack in packs:
        pdir = root / pack
        manifest = load_json(pdir / "pack.json", rep, f"{pack}/pack.json")
        if manifest is None:
            continue
        check_record(manifest, "pack", f"{pack}/pack.json", rep, refs)
        if manifest.get("id") != pack:
            rep.error(f"{pack}/pack.json", f"id `{manifest.get('id')}` differs from its folder `{pack}`")
        listed = sorted(manifest.get("files", []))
        present = sorted(p.relative_to(pdir).as_posix() for p in pdir.rglob("*.json") if p.name != "pack.json")
        for f in sorted(set(present) - set(listed)):
            rep.error(f"{pack}/pack.json", f"`{f}` exists but is not listed in `files` (it would never load)")
        for f in sorted(set(listed) - set(present)):
            rep.error(f"{pack}/pack.json", f"`{f}` is listed but missing")

        for f in listed:
            where = f"{pack}/{f}"
            data = load_json(pdir / f, rep, where)
            if data is None:
                continue
            parts = f.split("/")
            if parts[0] == "era":
                if f == "era/baseline.json":
                    era_baselines.append((data, where))
                elif f == "era/events.json":
                    era_events.append((data, where))
                else:
                    rep.error(where, "unknown era file (baseline.json, events.json)")
                continue
            if f == "ids.json":
                s.registries[pack] = (data, where)
                continue
            if f == "aliases.json":
                if not isinstance(data, dict):
                    rep.error(where, "expected an object of kinds")
                    continue
                for kind, table in data.items():
                    if kind not in aliases:
                        rep.error(where, f"unknown kind `{kind}`")
                        continue
                    for old, new in table.items():
                        aliases[kind][old.lower()] = new
                continue
            if len(parts) == 3 and parts[0] == "seasons":
                sid, name = parts[1], parts[2]
                if name == "season.json":
                    check_record(data, "season", where, rep, refs)
                    register("season", data, where)
                    if data.get("id") != sid:
                        rep.error(where, f"season id `{data.get('id')}` differs from its folder `{sid}`")
                    for i, ed in enumerate(data.get("editions") or []):
                        if isinstance(ed, dict):
                            register("edition", ed, f"{where}.editions[{i}]")
                    seasons[sid] = data
                    continue
                spec = SEASON_FILES.get(name)
                if spec is None:
                    rep.error(where, "unknown season file")
                    continue
            else:
                spec = PACK_FILES.get(f)
                if spec is None:
                    rep.error(where, "unknown data file")
                    continue
            spec_name, kind = spec
            if not isinstance(data, list):
                rep.error(where, "expected a list of records")
                continue
            for i, rec in enumerate(data):
                rw = f"{where}[{i}]" + (f" `{rec.get('id')}`" if isinstance(rec, dict) and rec.get("id") else "")
                check_record(rec, spec_name, rw, rep, refs)
                if kind:
                    register(kind, rec, rw)
            if spec_name == "squad":
                squads_by_season[parts[1]] = data
                for i, rec in enumerate(data):
                    check_squad_roles(rec, f"{where}[{i}]", rep)
            if spec_name == "result":
                results.append((data, where, parts[1]))
        for sid in manifest.get("seasons", []):
            if not (pdir / "seasons" / sid / "season.json").exists():
                rep.error(f"{pack}/pack.json", f"season `{sid}` has no seasons/{sid}/season.json")

    for data, where, sid in results:
        check_results(data, where, sid, seasons.get(sid), rep)
    check_era(root, era_baselines, era_events, rep, refs)

    # aliases: an old id must be gone, its target must exist
    for kind, table in aliases.items():
        for old, new in table.items():
            if old in ids[kind]:
                rep.error("aliases.json", f"{kind} alias `{old}` is still a live id")
            if resolve(kind, new, aliases, ids) is None:
                rep.error("aliases.json", f"{kind} alias `{old}` -> `{new}`: target does not exist")
    for kind, rid, where in refs:
        if resolve(kind, rid, aliases, ids) is None:
            rep.error(where, f"unknown {kind} `{rid}`")

    return s


def validate(root: Path = DEFAULT_ROOT) -> Report:
    s = scan(root)
    rep = s.rep
    if not s.packs:
        return rep
    check_registry(s)

    # warnings for the human review
    people = {}
    pfile = root / "core" / "people.json"
    if pfile.exists():
        try:
            people = {p["id"]: p for p in json.loads(pfile.read_text(encoding="utf-8")) if isinstance(p, dict)}
        except (json.JSONDecodeError, KeyError, TypeError):
            pass
    for pid, p in people.items():
        if p.get("firstName") == "":
            rep.warn(f"person `{pid}`", "no first name")
    for sid, squads in s.squads_by_season.items():
        year = int(sid[:4]) if sid[:4].isdigit() else None
        seen: dict[str, list[str]] = defaultdict(list)
        for sq in squads:
            if not isinstance(sq, dict):
                continue
            players = sq.get("players") or []
            if len(players) < 11:
                rep.warn(f"season {sid} `{sq.get('club')}`", f"only {len(players)} players (an XI needs 11)")
            for pid in players:
                seen[pid].append(sq.get("club"))
                born = people.get(pid, {}).get("born", "")
                if year and born[:4].isdigit() and not 15 <= year - int(born[:4]) <= 45:
                    rep.warn(f"season {sid} `{pid}`", f"age {year - int(born[:4])} at the season start")
        for pid, clubs in seen.items():
            if len(clubs) > 1:
                rep.warn(f"season {sid} `{pid}`", f"in {len(clubs)} squads: {', '.join(clubs)}")
        n_ph = sum(1 for p in people.values() if p.get("confidence") == "placeholder")
        if n_ph:
            rep.warn(f"season {sid}", f"{n_ph} people are placeholders")
    return rep


def resolve(kind: str, rid: str, aliases, ids, depth: int = 0):
    low = rid.lower()
    if low in ids[kind]:
        return low
    if low in aliases[kind] and depth < 16:
        return resolve(kind, aliases[kind][low], aliases, ids, depth + 1)
    return None


# ---------------------------------------------------------------------------------------------- ids.json registry
# Each pack has an append-only `ids.json`: kind -> data id -> runtime number. Numbers are unique across all kinds
# and packs, lie in the pack's `range`, stay below `next`, and are never reused or changed. A removed record keeps
# its entry (listed in `retired`); a renamed record (old id in aliases.json) keeps the old entry and its new id
# inherits that number, so runtime ids saved in games never shift. The game reads the same file (GameData).
REGISTRY_MAX = 899_999       # numbers 900 000+ are for objects the game derives at load; 1 000 000+ for play
BLOCK = 100_000              # a new pack gets the next free block of numbers
REGISTRY_FIELDS = {"range", "next", "ids", "retired"}


def is_int(n) -> bool:
    return isinstance(n, int) and not isinstance(n, bool)


def registry_entries(s: Scan) -> dict:
    """(kind, lower id) -> (number, pack) for every well-formed entry of every registry (first one wins)."""
    out = {}
    for pack, (reg, _where) in s.registries.items():
        if not isinstance(reg, dict) or not isinstance(reg.get("ids"), dict):
            continue
        for kind, table in reg["ids"].items():
            if kind not in KINDS or not isinstance(table, dict):
                continue
            for rid, n in table.items():
                if is_int(n):
                    out.setdefault((kind, rid.lower()), (n, pack))
    return out


def record_numbers(s: Scan, entries: dict | None = None) -> dict:
    """(kind, lower id) -> runtime number of every record: its own entry, else the lowest number of the old ids
    (aliases) that now resolve to it. Records without a number are left out."""
    entries = registry_entries(s) if entries is None else entries
    out = {}
    for _pack, kind, low, _where in s.records:
        if (kind, low) in entries:
            out[(kind, low)] = entries[(kind, low)][0]
    for (kind, rid), (n, _pack) in sorted(entries.items(), key=lambda e: e[1][0]):
        if rid in s.ids[kind]:
            continue
        target = resolve(kind, rid, s.aliases, s.ids)
        if target is not None and (kind, target) not in out:
            out[(kind, target)] = n
    return out


def retired_entries(s: Scan) -> set:
    out = set()
    for reg, _where in s.registries.values():
        if isinstance(reg, dict) and isinstance(reg.get("retired"), dict):
            for kind, lst in reg["retired"].items():
                if isinstance(lst, list):
                    out.update((kind, rid.lower()) for rid in lst if isinstance(rid, str))
    return out


def check_registry(s: Scan):
    rep = s.rep
    ranges = []
    seen_numbers: dict[int, str] = {}
    seen_keys: dict[tuple, str] = {}
    for pack, (reg, where) in s.registries.items():
        if not isinstance(reg, dict):
            rep.error(where, "expected an object with range, next, ids, retired")
            continue
        for k in reg:
            if k not in REGISTRY_FIELDS:
                rep.error(where, f"unknown field `{k}` (allowed: {', '.join(sorted(REGISTRY_FIELDS))})")
        rng, nxt = reg.get("range"), reg.get("next")
        if not (isinstance(rng, list) and len(rng) == 2 and all(is_int(x) for x in rng)
                and 1 <= rng[0] <= rng[1] <= REGISTRY_MAX):
            rep.error(where, f"`range` must be [first, last] with 1 <= first <= last <= {REGISTRY_MAX}")
            rng = None
        else:
            ranges.append((rng[0], rng[1], pack))
        if not is_int(nxt):
            rep.error(where, "`next` must be a whole number (the next free number, never lowered)")
            nxt = None
        if not isinstance(reg.get("ids"), dict):
            rep.error(where, "`ids` must be an object of kind -> {data id: number}")
            continue
        for kind, table in reg["ids"].items():
            if kind not in KINDS:
                rep.error(where, f"unknown kind `{kind}`")
                continue
            if not isinstance(table, dict):
                rep.error(f"{where}.ids.{kind}", "expected an object of data id -> number")
                continue
            for rid, n in table.items():
                label = f"{pack} {kind} `{rid}`"
                if (kind, rid.lower()) in seen_keys:
                    rep.error(where, f"{kind} `{rid}` has two entries ({seen_keys[(kind, rid.lower())]} and {label})")
                seen_keys.setdefault((kind, rid.lower()), label)
                if not is_int(n):
                    rep.error(f"{where}.ids.{kind}.{rid}", "expected a whole number")
                    continue
                if n in seen_numbers:
                    rep.error(where, f"number {n} is used twice ({seen_numbers[n]} and {label})")
                seen_numbers.setdefault(n, label)
                if rng and not rng[0] <= n <= rng[1]:
                    rep.error(f"{where}.ids.{kind}.{rid}", f"number {n} is outside the pack's range {rng}")
                elif nxt is not None and n >= nxt:
                    rep.error(f"{where}.ids.{kind}.{rid}", f"number {n} is not below `next` ({nxt})")
        retired = reg.get("retired", {})
        if not isinstance(retired, dict):
            rep.error(where, "`retired` must be an object of kind -> [data ids]")
            continue
        for kind, lst in retired.items():
            if kind not in KINDS or not isinstance(lst, list):
                rep.error(f"{where}.retired", f"unknown kind `{kind}` or not a list")
                continue
            for rid in lst:
                if not isinstance(rid, str) or rid not in (reg["ids"].get(kind) or {}):
                    rep.error(f"{where}.retired.{kind}", f"`{rid}` has no entry in `ids` (entries are never deleted)")
                elif rid.lower() in s.ids[kind]:
                    rep.error(f"{where}.retired.{kind}", f"`{rid}` is retired but still a live record")
    ranges.sort()
    for (_a1, b1, p1), (a2, _b2, p2) in zip(ranges, ranges[1:]):
        if a2 <= b1:
            rep.error("ids.json", f"the number ranges of packs `{p1}` and `{p2}` overlap")

    # every record has a number; every entry has a record, is retired or is an old (aliased) id
    entries = registry_entries(s)
    numbers = record_numbers(s, entries)
    for pack, kind, low, where in s.records:
        if (kind, low) not in numbers:
            rep.error(where, f"{kind} `{low}` has no number in {pack}/ids.json (run python tools/data/assign_ids.py)")
    retired = retired_entries(s)
    for (kind, rid), (n, pack) in entries.items():
        if rid in s.ids[kind] or (kind, rid) in retired:
            continue
        if resolve(kind, rid, s.aliases, s.ids) is None:
            rep.error(f"{pack}/ids.json", f"{kind} `{rid}` ({n}) has no record: list it in `retired` or alias it")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--root", type=Path, default=DEFAULT_ROOT)
    ap.add_argument("--quiet", action="store_true", help="print only errors and the summary")
    args = ap.parse_args()
    rep = validate(args.root)
    for e in rep.errors:
        print(f"ERROR   {e}")
    if not args.quiet:
        for w in rep.warnings:
            print(f"warning {w}")
    print(f"{len(rep.errors)} errors, {len(rep.warnings)} warnings")
    return 1 if rep.errors else 0


if __name__ == "__main__":
    sys.exit(main())
