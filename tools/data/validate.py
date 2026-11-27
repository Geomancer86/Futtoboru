"""Validate Futtoboru game data packs (gamedata/<pack>/...).

Same rules as DarkBlade's book validator: a typo in a field name is an error, every
reference must resolve (after aliases), ids are stable lower-case slugs and unique per
kind across all packs. Suspicious but possible data (a person at two clubs, a short
squad, an odd age) is a warning: it goes to the human review, it doesn't block.

Usage:  python tools/data/validate.py [--root DIR] [--quiet]
Exit 1 when there is any error.
"""
from __future__ import annotations

import argparse
import json
import re
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
KINDS = ["continent", "country", "profession", "occupation", "competition", "club", "ground", "person", "region",
         "season", "edition"]

# field spec: name -> (type, required). Types: str, int, num, bool, date, datetime, strs, attrs,
# ref:<kind>, refs:<kind>, enum:<name>, list:<record spec name>
ENUMS = {"confidence": CONFIDENCE, "occupation_category": OCCUPATION_CATEGORIES, "competition_type": COMPETITION_TYPES}
SPECS = {
    "continent": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True)},
    "country": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "formalName": ("str", False),
                "continent": ("ref:continent", True), "selectableByDefault": ("bool", False)},
    "profession": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True),
                   "playerSelectable": ("bool", True)},
    "occupation": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True),
                   "category": ("enum:occupation_category", True), "bonuses": ("attrs", True), "notes": ("str", False)},
    "names": {"country": ("ref:country", True), "firstNames": ("strs", True), "lastNames": ("strs", True)},
    "competition": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "longName": ("str", False),
                    "type": ("enum:competition_type", True), "sources": ("strs", True), "notes": ("str", False)},
    "club": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "fullName": ("str", False),
             "founded": ("int", False), "country": ("ref:country", True), "sources": ("strs", True),
             "notes": ("str", False)},
    "ground": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "opened": ("int", False),
               "capacity": ("int", False), "value": ("num", False), "description": ("str", False),
               "landValue": ("num", False), "ownedByClub": ("bool", False), "annualRent": ("num", False),
               "sources": ("strs", True), "notes": ("str", False)},
    "person": {"id": ("str", True), "firstName": ("str", True), "lastName": ("str", True), "born": ("date", True),
               "country": ("ref:country", True), "confidence": ("enum:confidence", True), "sources": ("strs", True),
               "legacyIds": ("strs", False), "notes": ("str", False)},
    "season": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "description": ("str", False),
               "start": ("datetime", True), "sources": ("strs", True), "countries": ("refs:country", True),
               "editions": ("list:edition", True)},
    "edition": {"id": ("str", True), "legacyId": ("int", False), "competition": ("ref:competition", True),
                "name": ("str", True), "participants": ("refs:club", True), "sources": ("strs", True),
                "notes": ("str", False)},
    "squad": {"club": ("ref:club", True), "ground": ("ref:ground", False), "balance": ("num", False),
              "players": ("refs:person", True), "notes": ("str", False)},
    "nationality_modifier": {"country": ("ref:country", True), "modifiers": ("attrs", True), "notes": ("str", False),
                             "footballingStrength": ("num", False)},
    "region": {"id": ("str", True), "legacyId": ("int", False), "name": ("str", True), "country": ("ref:country", True),
               "modifiers": ("attrs", True), "places": ("strs", False), "notes": ("str", False)},
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
    elif typ.startswith("list:"):
        if not isinstance(v, list):
            return bad("a list")
        for i, x in enumerate(v):
            check_record(x, typ[5:], f"{where}[{i}]", rep, refs)


def load_json(path: Path, rep: Report, where: str):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        rep.error(where, "file listed but missing")
    except (UnicodeDecodeError, json.JSONDecodeError) as e:
        rep.error(where, f"not valid UTF-8 JSON: {e}")
    return None


def validate(root: Path = DEFAULT_ROOT) -> Report:
    rep = Report()
    ids: dict[str, dict[str, str]] = {k: {} for k in KINDS}   # kind -> lower id -> where
    legacy: dict[str, dict[int, str]] = defaultdict(dict)
    refs: list[tuple[str, str, str]] = []
    aliases: dict[str, dict[str, str]] = {k: {} for k in KINDS}
    seasons: dict[str, dict] = {}
    squads_by_season: dict[str, list] = {}

    index = root / "index.txt"
    if not index.exists():
        rep.error("index.txt", "missing (lists the packs to load)")
        return rep
    packs = [l.strip() for l in index.read_text(encoding="utf-8").splitlines() if l.strip() and not l.startswith("#")]
    on_disk = sorted(p.name for p in root.iterdir() if p.is_dir())
    for p in on_disk:
        if p not in packs:
            rep.error("index.txt", f"pack folder `{p}` is not listed")

    def register(kind, rec, where):
        rid = rec.get("id")
        if not isinstance(rid, str):
            return
        low = rid.lower()
        if low in ids[kind]:
            rep.error(where, f"duplicate {kind} id `{rid}` (also {ids[kind][low]})")
        else:
            ids[kind][low] = where
        lid = rec.get("legacyId")
        if isinstance(lid, int):
            if lid in legacy[kind]:
                rep.error(where, f"duplicate {kind} legacyId {lid} (also {legacy[kind][lid]})")
            legacy[kind][lid] = where

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
        for s in manifest.get("seasons", []):
            if not (pdir / "seasons" / s / "season.json").exists():
                rep.error(f"{pack}/pack.json", f"season `{s}` has no seasons/{s}/season.json")

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
    for sid, squads in squads_by_season.items():
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
