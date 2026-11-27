"""One-shot converter: legacy mods/*.txt -> gamedata/core/*.json (P2-4, data model v2).

Producer decisions (docs/ROADMAP.md): D11 stable string ids, D14 all JSON, D15 one
people table + season squads. The legacy files stay untouched; this writes the new
pack and a review list of everything suspicious for a human to check.

Rules:
- Faithful copy. Only mechanical, unambiguous repairs are applied (encoding, an exact
  duplicate row, an unquoted comma inside a URL, a column shift from a missing first
  name, region modifiers pointing at country 1). Each repair is listed in the review.
- Anything that needs judgement (possible duplicate people, continent values, cup
  entrants) is flagged, never changed.
- Every record keeps its legacy id ("legacyId") so the Java switch (slice 2) can map
  old numeric ids and so nothing is lost.

Usage:  python tools/data/convert_legacy.py            (writes files)
        python tools/data/convert_legacy.py --check    (exit 1 if output would change)
"""
from __future__ import annotations

import argparse
import functools
import json
import re
import sys
import unicodedata
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / "futtoboru-core" / "src" / "main" / "resources"
LEGACY = RES / "mods"
S18 = LEGACY / "seasons" / "18"
OUT = RES / "gamedata"
PACK = OUT / "core"
SEASON_ID = "1888-89"
REVIEW = ROOT / "docs" / "data" / f"REVIEW_{SEASON_ID}.md"

QUOTED_SPLIT = re.compile(r',(?=(?:[^"]*"[^"]*")*[^"]*$)')
ATTRS = ["strength", "endurance", "stamina", "speed", "acceleration", "jumping", "dexterity",
         "concentration", "courage", "determination", "leadership", "perception", "positioning",
         "teamwork"]

# Burnley rows 11-31 were added in one batch (commit fd35aaa, 2025-12-16) with a filler
# birth year of 1865 to reach a squad size; some may belong to other seasons.
PLACEHOLDER_ROWS = {9: set(range(11, 32))}
# Hard-coded in ScriptsLoader today (league founders, cup entrants) - compared, not used.
CODE_LEAGUE_FOUNDERS = list(range(1, 13))
CODE_CUP_ENTRANTS = list(range(1, 33))

review: dict[str, list[str]] = defaultdict(list)


def flag(section: str, text: str) -> None:
    review[section].append(text)


def slug(text: str) -> str:
    t = unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")
    t = t.lower().replace("'", "").replace(".", "")
    return re.sub(r"[^a-z0-9]+", "-", t).strip("-")


@functools.lru_cache(maxsize=None)
def read_text(path: Path) -> str:
    raw = path.read_bytes()
    try:
        text = raw.decode("utf-8")
    except UnicodeDecodeError:
        text = raw.decode("cp1252")
        flag("Repairs applied", f"`{rel(path)}` was CP1252, re-read as such (now UTF-8 JSON).")
    if "�" in text:
        n = len(re.findall("�(?=[0-9])", text))
        text = re.sub("�(?=[0-9])", "£", text)
        if n:
            flag("Repairs applied", f"`{rel(path)}`: {n} corrupted pound sign(s) restored as `£`.")
        if "�" in text:
            flag("Needs review", f"`{rel(path)}` still has unreadable characters after repair.")
    return text


def rel(path: Path) -> str:
    return path.relative_to(ROOT).as_posix()


def rows(path: Path, quoted: bool = False) -> list[tuple[int, list[str]]]:
    out = []
    for n, line in enumerate(read_text(path).splitlines(), 1):
        s = line.strip()
        if not s or s.startswith("#"):
            continue
        parts = QUOTED_SPLIT.split(s) if quoted else s.split(",")
        parts = [p.strip() for p in parts]
        while parts and parts[-1] == "":
            parts.pop()
        out.append((n, parts))
    return out


def unquote(v: str) -> str:
    return v[1:-1] if len(v) >= 2 and v[0] == '"' and v[-1] == '"' else v


def header_sources(path: Path) -> list[str]:
    urls = []
    for line in read_text(path).splitlines():
        if line.lstrip().startswith("#"):
            urls += re.findall(r"https?://\S+", line)
    seen, out = set(), []
    for u in urls:
        if u not in seen:
            seen.add(u)
            out.append(u)
    return out


def num(v: str):
    if v == "":
        return None
    f = float(v)
    return int(f) if f.is_integer() else f


def unique(records: list[dict], kind: str) -> None:
    seen: dict[str, int] = {}
    for r in records:
        base = r["id"]
        if base in seen:
            seen[base] += 1
            r["id"] = f"{base}-{seen[base]}"
            label = r.get("name") or f"{r.get('firstName', '')} {r.get('lastName', '')}".strip()
            flag("Needs review", f"{kind} id `{base}` was taken; this one became `{r['id']}` ({label}).")
        else:
            seen[base] = 1


# ---------------------------------------------------------------- world tables
def convert_continents():
    recs = [{"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1]} for _, p in rows(LEGACY / "continents.txt")]
    return recs, {r["legacyId"]: r["id"] for r in recs}


def convert_countries(cont):
    recs = []
    for n, p in rows(LEGACY / "countries.txt"):
        r = {"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1]}
        if len(p) > 2 and p[2]:
            r["formalName"] = p[2]
        r["continent"] = cont[int(p[3])]
        if len(p) > 4 and p[4]:
            r["selectableByDefault"] = p[4].lower() == "true"
        recs.append(r)
    unique(recs, "country")
    by_legacy = {r["legacyId"]: r["id"] for r in recs}
    for r in recs:
        if r["id"] == "scotland" and r["continent"] != "europe":
            flag("Needs review", f"Scotland's continent is `{r['continent']}` (legacy value). Europe expected; "
                 "countries.txt says continents follow the football association's origin. Left as is.")
    return recs, by_legacy


def convert_professions():
    recs = [{"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1], "playerSelectable": p[2].lower() == "true"}
            for _, p in rows(LEGACY / "professions.txt")]
    return recs


def bonuses(values: list[str]) -> dict:
    return {a: num(v) for a, v in zip(ATTRS, values) if num(v)}


def convert_occupations():
    recs = []
    for _, p in rows(S18 / "player_professions.txt", quoted=True):
        r = {"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1], "category": p[2],
             "bonuses": bonuses(p[3:17])}
        if len(p) > 17:
            r["notes"] = unquote(p[17])
        recs.append(r)
    unique(recs, "occupation")
    return recs


def convert_nationality_modifiers(countries):
    recs = []
    for _, p in rows(S18 / "nationality_modifiers.txt", quoted=True):
        r = {"country": countries[int(p[0])], "modifiers": bonuses(p[2:16])}
        if len(p) > 16:
            r["notes"] = unquote(p[16])
        if len(p) > 17:
            r["footballingStrength"] = num(p[17])
        recs.append(r)
    return recs


def convert_region_modifiers(countries):
    recs = []
    for n, p in rows(S18 / "region_modifiers.txt", quoted=True):
        country = countries.get(int(p[2]))
        if int(p[2]) == 1:
            country = "england"
            flag("Repairs applied", f"region `{p[1]}` pointed at country 1 ({countries.get(1)}); the file is "
                 "'Region Modifiers ... (England)', so it now points at `england`.")
        r = {"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1].title(), "country": country,
             "modifiers": bonuses(p[3:17])}
        if len(p) > 17:
            r["places"] = [c.strip() for c in unquote(p[17]).split(",") if c.strip()]
        if len(p) > 18:
            r["notes"] = unquote(p[18])
        recs.append(r)
    return recs


def convert_names():
    out = []
    for d in sorted((LEGACY / "names").iterdir()):
        if d.is_dir():
            def lines(f):
                return [l.strip() for l in read_text(d / f).splitlines() if l.strip() and not l.startswith("#")]
            out.append({"country": slug(d.name), "firstNames": lines("names.txt"), "lastNames": lines("lastnames.txt")})
    return out


# ---------------------------------------------------------------- clubs, grounds, people
def convert_clubs(countries):
    clubs, season_extra = [], {}
    for n, p in rows(S18 / "clubs.txt"):
        lid = int(p[0])
        r = {"id": slug(p[1]), "legacyId": lid, "name": p[1], "fullName": p[2]}
        if len(p) > 5 and p[5]:
            r["founded"] = int(p[5])
        r["country"] = countries[int(p[6])]
        r["sources"] = [p[4]] if p[4] else []
        clubs.append(r)
        season_extra[lid] = {"balance": num(p[3])}
    unique(clubs, "club")
    return clubs, {c["legacyId"]: c["id"] for c in clubs}, season_extra


def convert_grounds(club_ids):
    grounds, ground_of = [], {}
    for f in sorted((S18 / "club_stadiums").glob("*.txt"), key=lambda x: int(x.stem)):
        lid = int(f.stem)
        file_sources = header_sources(f)
        for n, p in rows(f, quoted=True):
            if len(p) == 11 and p[6].startswith("http") and not p[7].lstrip("-").isdigit():
                p = p[:6] + [p[6] + "," + p[7]] + p[8:]
                flag("Repairs applied", f"`{rel(f)}` line {n}: URL contained an unquoted comma; rejoined.")
            if len(p) != 10:
                flag("Needs review", f"`{rel(f)}` line {n}: {len(p)} columns, skipped.")
                continue
            sources = [p[6]] + [u for u in file_sources if u != p[6]]
            g = {"id": slug(p[1]), "legacyId": int(p[0]), "name": p[1], "opened": num(p[2]),
                 "capacity": num(p[3]), "value": num(p[4]), "description": unquote(p[5]),
                 "landValue": num(p[7]), "ownedByClub": p[8].lower() == "true", "annualRent": num(p[9]),
                 "sources": [s for s in sources if s]}
            if int(p[0]) != lid:
                flag("Needs review", f"`{rel(f)}`: stadium id {p[0]} differs from the file's club {lid}.")
            grounds.append(g)
            ground_of[lid] = g
    names = defaultdict(list)
    for lid, g in ground_of.items():
        names[g["id"]].append(lid)
    for gid, lids in names.items():
        if len(lids) > 1:
            for lid in lids[1:]:
                ground_of[lid]["id"] = f"{gid}-{club_ids[lid]}"
                flag("Needs review", f"two grounds are called `{gid}`; the one at `{club_ids[lid]}` became "
                     f"`{gid}-{club_ids[lid]}`. Check its name (see its description).")
    unique(grounds, "ground")
    return grounds, {k: v["id"] for k, v in ground_of.items()}


def convert_people(countries, club_ids):
    people, squads = [], defaultdict(list)
    by_key: dict[tuple, dict] = {}
    for f in sorted((S18 / "club_players").glob("*.txt"), key=lambda x: int(x.stem)):
        club_lid = int(f.stem)
        file_sources = header_sources(f)
        seen_rows = set()
        for n, p in rows(f):
            if tuple(p) in seen_rows:
                flag("Repairs applied", f"`{rel(f)}` line {n}: exact duplicate row `{','.join(p)}` dropped.")
                continue
            seen_rows.add(tuple(p))
            if len(p) == 4 and p[2].isdigit():
                flag("Needs review", f"`{rel(f)}` line {n}: no first name (`{','.join(p)}`); kept with an empty "
                     "first name. Find the first name.")
                p = [p[0], "", p[1], p[2], p[3]]
            if len(p) != 5:
                flag("Needs review", f"`{rel(f)}` line {n}: {len(p)} columns, skipped: `{','.join(p)}`.")
                continue
            row_id, first, last, country_lid, born = p
            if not re.fullmatch(r"\d{4}(-\d{2}(-\d{2})?)?", born):
                flag("Needs review", f"`{rel(f)}` line {n}: birth date `{born}` not understood; skipped.")
                continue
            country = countries.get(int(country_lid))
            if country is None:
                flag("Needs review", f"`{rel(f)}` line {n}: unknown country {country_lid}.")
            placeholder = int(row_id) in PLACEHOLDER_ROWS.get(club_lid, set())
            key = (first.lower(), last.lower(), born, country)
            person = by_key.get(key)
            if person is None:
                person = {"id": slug(f"{first} {last} {born[:4]}"), "firstName": first, "lastName": last,
                          "born": born, "country": country,
                          "confidence": "placeholder" if placeholder else "unverified",
                          "sources": list(file_sources),
                          "legacyIds": []}
                people.append(person)
                by_key[key] = person
            else:
                for s in file_sources:
                    if s not in person["sources"]:
                        person["sources"].append(s)
            if any(x is person for x in squads[club_lid]):
                flag("Repairs applied", f"`{rel(f)}` line {n}: `{person['id']}` appears twice in this file "
                     f"(row ids differ, data identical); second row `{row_id}` dropped.")
                continue
            person["legacyIds"].append(f"{club_lid}:{row_id}")
            squads[club_lid].append(person)
    unique(people, "person")

    # review: same person at several clubs, same name with different data
    by_name = defaultdict(list)
    for pr in people:
        by_name[(pr["firstName"].lower(), pr["lastName"].lower())].append(pr)
    for pr in people:
        clubs_of = sorted({int(x.split(":")[0]) for x in pr["legacyIds"]})
        if len(clubs_of) > 1:
            flag("Same person listed at several clubs",
                 f"`{pr['id']}` at {', '.join(club_ids[c] for c in clubs_of)}: a real move during 1888-89, or a typo?")
    for (fn, ln), group in sorted(by_name.items()):
        if len(group) > 1:
            desc = "; ".join(f"`{g['id']}` born {g['born']} ({', '.join(club_ids[int(x.split(':')[0])] for x in g['legacyIds'])})"
                             for g in group)
            flag("Same name, different birth data", f"{desc}: the same person (merge) or different people?")
    members = {c: {pr["id"] for pr in squads[c]} for c in squads}
    for a in sorted(members):
        for b in sorted(members):
            if a < b:
                shared = members[a] & members[b]
                small = min(len(members[a]), len(members[b]))
                if len(shared) >= 3:
                    flag("Shared rosters (likely copied)",
                         f"`{club_ids[a]}` and `{club_ids[b]}` share {len(shared)} of {small} players. "
                         "Probably one roster was copied to fill the other; rebuild the smaller one from a source.")
    n_ph = sum(1 for pr in people if pr["confidence"] == "placeholder")
    if n_ph:
        flag("Placeholders", f"{n_ph} Burnley players added in one batch with a filler birth year of 1865 "
             "(commit fd35aaa) are marked `confidence: placeholder`. Check each one against a 1888-89 source.")
    partial = [pr["id"] for pr in people if len(pr["born"]) == 7]
    if partial:
        flag("Placeholders", f"Year-month birth dates kept as is: {', '.join(f'`{x}`' for x in partial)}.")
    return people, squads


# ---------------------------------------------------------------- season
def convert_season(club_ids, ground_of, season_extra, squads):
    (_, s), = rows(LEGACY / "seasons.txt", quoted=True)
    m = re.fullmatch(r"(\d{2})-(\d{2})-(\d{4}) (\d{2}):(\d{2}):(\d{2}) (AM|PM)", s[4])
    hh = int(m.group(4)) % 12 + (12 if m.group(7) == "PM" else 0)
    start = f"{m.group(3)}-{m.group(2)}-{m.group(1)}T{hh:02d}:{m.group(5)}:{m.group(6)}"
    countries = [c for _, (c, *rest) in rows(S18 / "countries.txt")]
    (cup_row,) = [p for _, p in rows(S18 / "competitions" / "1" / "competition_edition.txt", quoted=True)]
    cup_entrants = [int(x) for x in unquote(cup_row[3]).split(",")]
    missing = sorted(set(CODE_CUP_ENTRANTS) - set(cup_entrants))
    extra = sorted(set(cup_entrants) - set(CODE_CUP_ENTRANTS))
    if missing or extra:
        flag("Needs review", "FA Cup entrants: the data file and the code (ScriptsLoader, clubs 1-32) disagree. "
             f"Only in the code: {', '.join(club_ids[c] for c in missing)}. Only in the data: "
             f"{', '.join(club_ids[c] for c in extra)}. The new data keeps the data file's list; check it "
             "against the 1888-89 FA Cup.")
    season = {
        "id": SEASON_ID, "legacyId": int(s[0]), "name": s[1], "description": unquote(s[2]),
        "start": start, "sources": [s[3]],
        "countries": ["england" if c == "1000" else c for c in countries],
        "editions": [
            {"id": "football-league-1888-89", "competition": "football-league", "name": "1888-89 Football League",
             "participants": [club_ids[c] for c in CODE_LEAGUE_FOUNDERS],
             "sources": ["https://en.wikipedia.org/wiki/1888%E2%80%9389_Football_League"],
             "notes": "The twelve founder members (moved here from ScriptsLoader, where they were hard-coded)."},
            {"id": "fa-cup-1888-89", "legacyId": int(cup_row[0]), "competition": "fa-cup", "name": cup_row[1],
             "participants": [club_ids[c] for c in cup_entrants], "sources": [cup_row[2]]},
        ],
    }
    squad_recs = []
    for lid in sorted(club_ids):
        squad_recs.append({"club": club_ids[lid], "ground": ground_of.get(lid), **season_extra[lid],
                           "players": [p["id"] for p in squads.get(lid, [])]})
        if len(squads.get(lid, [])) < 11:
            flag("Needs review", f"`{club_ids[lid]}` has only {len(squads.get(lid, []))} players.")
        if lid not in ground_of:
            flag("Needs review", f"`{club_ids[lid]}` has no ground.")
    return season, squad_recs


def convert_competitions():
    (p,) = [p for _, p in rows(LEGACY / "competitions.txt")]
    return [
        {"id": "fa-cup", "legacyId": int(p[0]), "name": p[1], "longName": p[2], "type": "CUP", "sources": [p[3]]},
        {"id": "football-league", "name": "Football League", "longName": "The Football League", "type": "LEAGUE",
         "sources": ["https://en.wikipedia.org/wiki/English_Football_League"],
         "notes": "Not in the legacy competitions file; the league was created in code (ScriptsLoader)."},
    ]


# ---------------------------------------------------------------- write
def dump(obj) -> str:
    return json.dumps(obj, indent=2, ensure_ascii=False) + "\n"


def build() -> dict[Path, str]:
    continents, cont = convert_continents()
    countries, country_ids = convert_countries(cont)
    clubs, club_ids, extra = convert_clubs(country_ids)
    grounds, ground_of = convert_grounds(club_ids)
    people, squads = convert_people(country_ids, club_ids)
    season, squad_recs = convert_season(club_ids, ground_of, extra, squads)

    season_dir = f"seasons/{SEASON_ID}"
    files = {
        "continents.json": continents,
        "countries.json": countries,
        "professions.json": convert_professions(),
        "occupations.json": convert_occupations(),
        "names.json": convert_names(),
        "competitions.json": convert_competitions(),
        "clubs.json": clubs,
        "grounds.json": grounds,
        "people.json": people,
        "aliases.json": {k: {} for k in ["continent", "country", "profession", "occupation", "competition",
                                          "club", "ground", "person", "region", "season", "edition"]},
        f"{season_dir}/season.json": season,
        f"{season_dir}/squads.json": squad_recs,
        f"{season_dir}/nationality_modifiers.json": convert_nationality_modifiers(country_ids),
        f"{season_dir}/region_modifiers.json": convert_region_modifiers(country_ids),
    }
    pack = {"id": "core", "name": "Futtoboru base data", "version": 1, "dataVersion": 1,
            "seasons": [SEASON_ID], "files": sorted(files)}
    out = {PACK / k: dump(v) for k, v in files.items()}
    out[PACK / "pack.json"] = dump(pack)
    out[OUT / "index.txt"] = "# Data packs, loaded in this order (a jar folder cannot be listed)\ncore\n"
    out[REVIEW] = review_markdown(people, clubs)
    return out


def review_markdown(people, clubs) -> str:
    lines = [f"# Data review: {SEASON_ID}", "",
             "Generated by `tools/data/convert_legacy.py` from the legacy `mods/` files. Every person starts as "
             "`confidence: unverified` (typed by hand, may have errors). When a record is checked against a source, "
             "set `confidence: verified` and fix it in the JSON; once the Java loaders switch (P2-4 slice 2) the JSON "
             "is the only copy.", "",
             f"Totals: {len(clubs)} clubs, {len(people)} people.", ""]
    order = ["Needs review", "Shared rosters (likely copied)", "Same person listed at several clubs", "Same name, different birth data",
             "Placeholders", "Repairs applied"]
    for sec in order:
        if review.get(sec):
            lines += [f"## {sec} ({len(review[sec])})", ""] + [f"- [ ] {t}" if sec != "Repairs applied" else f"- {t}"
                                                              for t in review[sec]] + [""]
    return "\n".join(lines)


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--check", action="store_true", help="exit 1 if the output on disk is out of date")
    args = ap.parse_args()
    out = build()
    stale = [p for p, text in out.items() if not p.exists() or p.read_text(encoding="utf-8") != text]
    if args.check:
        for p in stale:
            print(f"out of date: {rel(p)}")
        return 1 if stale else 0
    for p, text in out.items():
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(text, encoding="utf-8", newline="\n")
    print(f"wrote {len(out)} files ({len(stale)} changed); review items: "
          + ", ".join(f"{k}={len(v)}" for k, v in review.items()))
    return 0


if __name__ == "__main__":
    sys.exit(main())
