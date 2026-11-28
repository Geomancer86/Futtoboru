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
# H-05 (HISTORY_DATA_PROPOSAL.md, D30): what kind of body a club grew out of, and the fan-love scale of a squad entry
CLUB_ORIGINS = {"church", "cricket", "works", "school", "old-boys", "rugby", "merger", "other"}
FAN_TIER_MIN, FAN_TIER_MAX = 0, 5
LANG_RE = re.compile(r"^[a-z]{2,3}(-[A-Z]{2})?$")
ATTR_MIN, ATTR_MAX = 3, 23  # the attribute scale (3d6 plus modifiers, PlayerAttributeGenerator)
# G-04: the wage band a club's men are engaged on (seasons/<id>/contracts.json): a Football League club, any other
# club, or a club of amateurs only (listed by id in that band's `clubs`)
CLUB_STANDINGS = {"league", "other", "amateur"}
# P2-14: the season calendar (season.json `calendar`) and a cup's neutral grounds (edition `neutralVenues`)
WEEKDAYS = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"}
CUP_ROUNDS = {"quarter-final", "semi-final", "final"}  # counted back from the final
NEUTRAL_GATES = {"association", "shared", "home"}  # who books the gate of a tie at a neutral ground
LATER_DRAWS = {"open-draw", "bracket"}  # each round drawn after the one before, or paired by the first draw
MONTHDAY_RE = re.compile(r"^(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$")
DAYS_IN_MONTH = [31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31]
KINDS = ["continent", "country", "profession", "occupation", "competition", "club", "ground", "person", "region",
         "season", "edition",
         # R-02 recruitment data, R-11a languages (DATA_MODEL.md "Recruitment and languages")
         "rule", "wage", "origin", "origin-club", "area", "language",
         # G-02 injury kinds (DATA_MODEL.md "Injuries")
         "injury",
         # R-03 candidate pool regions (DATA_MODEL.md "Recruitment and languages")
         "pool-region"]
# R-02 / R-11a: research grade of a rule, wage, origin, area or rate row (RECRUITMENT_1888-1900.md H/M/L; `estimate` =
# no direct source, the reasoning is in `sources`; `placeholder` = gameplay tuning, D30 A3)
DATA_CONFIDENCE = {"high", "medium", "low", "estimate", "placeholder"}
UNSOURCED_OK = {"estimate", "placeholder"}   # the only grades that may have an empty `sources`
RULE_KINDS = {"registration", "residence", "second-registration-consent", "registration-wait",
              "scottish-professional-ban", "retain-and-transfer", "max-wage"}
RULE_VALUES = {"residence": ("miles", "years"), "registration-wait": ("days",), "max-wage": ("pence",)}
RULE_BODIES = {"fa", "fa-council", "football-league", "sfa"}
APPLIES_TO = {"professional", "amateur", "all"}
WAGE_TYPES = {"example", "band"}
STANDINGS = {"leading", "middling", "small", "any"}
# docs/design/RECRUITMENT_AND_TRIALS.md §3.1 candidate source kinds
SOURCE_KINDS = {"local-junior", "works-team", "scottish-import", "league-poach", "released", "amateur", "veteran",
                "schoolboy"}
ORIGIN_CLUB_KINDS = {"church", "sunday-school", "works", "junior", "school", "scottish"}
NUDGE_MAX = 2                 # an origin's attribute nudge is small (D28 Q12): -2..+2
LITERACY_SHIFT_MAX = 1        # an origin moves literacy by at most one level
RATE_FIELDS = ("speaks", "only", "signsMen", "signsWomen", "parentsBorn", "readWriteIfSigns")
INJURY_KINDS = {"knock", "passenger", "carried-off"}  # D19-3: plays on / stays on hurt / leaves, may return
READ_WRITE_LEVELS = 4         # readWriteIfSigns: signs his name / reads slowly / reads well / writes well

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

# field spec: name -> (type, required). Types: str, int, num, bool, date, datetime, strs, nums, attrs,
# monthday (MM-DD), monthdays, ref:<kind>, refs:<kind>, enum:<name>, enums:<name> (a list), list:<record spec
# name>, map:<record spec name> (an object: id -> record), obj:<record spec name> (one nested object),
# shares:<kind> (an object: id of that kind -> a share 0..1)
ENUMS = {"era_kind": ERA_KINDS, "era_authority": ERA_AUTHORITIES, "era_confidence": ERA_CONFIDENCE,
         "era_certainty": ERA_CERTAINTY, "era_op": ERA_OPS, "era_category": ERA_CATEGORIES,
         "era_priority": ERA_PRIORITIES, "confidence": CONFIDENCE, "occupation_category": OCCUPATION_CATEGORIES, "competition_type": COMPETITION_TYPES,
         "position": POSITIONS, "position_source": POSITION_SOURCES, "data_confidence": DATA_CONFIDENCE,
         "rule_kind": RULE_KINDS, "rule_body": RULE_BODIES, "applies_to": APPLIES_TO, "wage_type": WAGE_TYPES,
         "standing": STANDINGS, "source_kind": SOURCE_KINDS, "origin_club_kind": ORIGIN_CLUB_KINDS,
         "weekday": WEEKDAYS, "cup_round": CUP_ROUNDS, "neutral_gate": NEUTRAL_GATES, "later_draws": LATER_DRAWS,
         "club_origin": CLUB_ORIGINS, "injury_kind": INJURY_KINDS,
         "club_standing": CLUB_STANDINGS}
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
             "notes": ("str", False),
             # H-05: identity facts (the prose is in text/<lang>/history.json)
             "origin": ("enum:club_origin", False), "nicknames": ("list:nickname", False),
             "rivals": ("refs:club", False), "nameHistory": ("list:club_name", False)},
    # period: true = sourced as used at the time (the game shows only these in-season), false = a later name
    "nickname": {"name": ("str", True), "from": ("int", False), "to": ("int", False), "period": ("bool", False),
                 "sources": ("strs", True)},
    "club_name": {"name": ("str", True), "from": ("int", True), "to": ("int", False), "sources": ("strs", True)},
    # text/<lang>/history.json: prose keyed by data id, per kind (the structured facts stay in the records)
    "history_club": {"hook": ("str", True), "history": ("strs", True), "seasons": ("seasontexts", False),
                     "sources": ("strs", True)},
    "history_person": {"bio": ("str", True), "sources": ("strs", True)},
    "ground": {"id": ("str", True), "name": ("str", True), "opened": ("int", False),
               "capacity": ("int", False), "value": ("num", False), "description": ("str", False),
               "landValue": ("num", False), "ownedByClub": ("bool", False), "annualRent": ("num", False),
               "sources": ("strs", True), "notes": ("str", False)},
    "person": {"id": ("str", True), "firstName": ("str", True), "lastName": ("str", True), "born": ("date", True),
               "country": ("ref:country", True), "confidence": ("enum:confidence", True), "sources": ("strs", True),
               "notes": ("str", False),
               # H-05: nicknames (same shape as a club's) and the display name where it differs
               "nicknames": ("list:nickname", False), "knownAs": ("str", False),
               # R-11a: his birth area, only when a source says so (else drawn from the area weights)
               "birthArea": ("ref:area", False)},
    "season": {"id": ("str", True), "name": ("str", True), "description": ("str", False),
               "start": ("datetime", True), "sources": ("strs", True), "countries": ("refs:country", True),
               "editions": ("list:edition", True), "calendar": ("obj:calendar", False)},
    "edition": {"id": ("str", True), "competition": ("ref:competition", True),
                "name": ("str", True), "participants": ("refs:club", True), "sources": ("strs", True),
                "notes": ("str", False), "neutralVenues": ("obj:neutral_venues", False)},
    # P2-14 (D30 A9): the season's calendar rules; a later season without one keeps the latest earlier one's rules.
    # A date rule is a month-day and a weekday: the first such weekday on or after it, or the last on or before it;
    # months from July on fall in the season's first year, January to June in the next.
    "calendar": {"league": ("obj:calendar_league", False), "cups": ("list:calendar_cup", False),
                 "notes": ("str", False)},
    "calendar_league": {"opens": ("obj:day_rule", True), "closes": ("obj:day_rule", True),
                        "midweek": ("enum:weekday", False), "noMatchOn": ("monthdays", False),
                        "sources": ("strs", True), "notes": ("str", False)},
    "calendar_cup": {"competition": ("ref:competition", True), "opens": ("obj:day_rule", True),
                     "firstDraw": ("obj:day_rule", True), "laterDraws": ("enum:later_draws", True),
                     "drawDaysAfterRound": ("int", False), "final": ("obj:day_rule", True),
                     "properFrom": ("monthday", True), "daysBetweenRounds": ("int", True),
                     "earlyRoundsOn": ("enum:weekday", False), "maxEarlyRounds": ("int", False),
                     "sources": ("strs", True), "notes": ("str", False)},
    "day_rule": {"onOrAfter": ("monthday", False), "onOrBefore": ("monthday", False), "weekday": ("enum:weekday", True),
                 "confidence": ("enum:confidence", True), "source": ("str", False), "notes": ("str", False)},
    # the cup ties of this season played on a neutral ground (P2-14): candidate grounds per round, the gate rule
    "neutral_venues": {"rounds": ("list:neutral_round", True), "gate": ("enum:neutral_gate", True),
                       "gateConfidence": ("enum:confidence", True), "sources": ("strs", True),
                       "notes": ("str", False)},
    "neutral_round": {"round": ("enum:cup_round", True), "grounds": ("refs:ground", True),
                      "confidence": ("enum:confidence", True), "notes": ("str", False)},
    "squad": {"club": ("ref:club", True), "ground": ("ref:ground", False), "balance": ("num", False),
              "players": ("refs:person", True), "notes": ("str", False),
              # E-01, keyed by a person id of `players`: this season's position, and a sourced dribbling value
              "positions": ("map:squad_position", False), "dribbling": ("map:squad_dribbling", False),
              # H-05: the club's fans' tier (0-5) of a player at the start of the season; missing = derived default
              "fanTiers": ("tiers:person", False)},
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
    # G-04: one-season engagements of a season, per club standing; money in shillings a week (docs/DATA_MODEL.md)
    "contract_band": {"standing": ("enum:club_standing", True), "clubs": ("refs:club", False),
                      "professionalShare": ("num", True), "seasonWageMin": ("num", True),
                      "seasonWageMax": ("num", True), "summerWageMin": ("num", True), "summerWageMax": ("num", True),
                      "summerPayShare": ("num", True), "amateurExpenses": ("num", True),
                      "minimumSquad": ("int", True), "squadSize": ("int", True),
                      "confidence": ("enum:confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    # R-02 (docs/design/RECRUITMENT_AND_TRIALS.md §6): registration rules by date, wages, origin backstories and
    # clubs. Money in pence (240 to the pound).
    "rule": {"id": ("str", True), "rule": ("enum:rule_kind", True), "from": ("date", True), "until": ("date", False),
             "body": ("enum:rule_body", True), "appliesTo": ("enum:applies_to", True), "days": ("int", False),
             "miles": ("int", False), "years": ("int", False), "pence": ("int", False),
             "confidence": ("enum:data_confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    "wage": {"id": ("str", True), "type": ("enum:wage_type", True), "from": ("date", True), "until": ("date", False),
             "standing": ("enum:standing", True), "club": ("ref:club", False), "person": ("ref:person", False),
             "weeklyPence": ("int", False), "summerWeeklyPence": ("int", False), "minWeeklyPence": ("int", False),
             "maxWeeklyPence": ("int", False), "confidence": ("enum:data_confidence", True),
             "sources": ("strs", True), "notes": ("str", False)},
    "origin": {"id": ("str", True), "name": ("str", True), "occupations": ("refs:occupation", True),
               "sourceKinds": ("enums:source_kind", True), "nudges": ("attrs", True), "literacyShift": ("int", True),
               "confidence": ("enum:data_confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    "origin_club": {"id": ("str", True), "name": ("str", True), "kind": ("enum:origin_club_kind", True),
                    "place": ("str", True), "country": ("ref:country", True), "area": ("ref:area", False),
                    "founded": ("int", False), "club": ("ref:club", False), "origins": ("refs:origin", False),
                    "confidence": ("enum:confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    # R-11a (design §10): languages, rate areas (weight = share of the country's people) and rate rows
    "language": {"id": ("str", True), "name": ("str", True), "countries": ("refs:country", True),
                 "sources": ("strs", True), "notes": ("str", False)},
    "area": {"id": ("str", True), "name": ("str", True), "country": ("ref:country", True), "weight": ("num", True),
             "places": ("strs", False), "confidence": ("enum:data_confidence", True), "sources": ("strs", True),
             "notes": ("str", False)},
    "language_rate": {"area": ("ref:area", False), "countries": ("refs:country", False), "bornFrom": ("int", True),
                      "bornUntil": ("int", True), "speaks": ("shares:language", False),
                      "only": ("shares:language", False), "signsMen": ("num", False), "signsWomen": ("num", False),
                      "parentsBorn": ("shares:country", False), "readWriteIfSigns": ("nums", False),
                      "confidence": ("enum:data_confidence", True), "sources": ("strs", True),
                      "notes": ("str", False)},
    # G-02 (D19-3): one record per injury kind: chance per man per match, days out after it, in-match effect
    "injury": {"id": ("str", True), "kind": ("enum:injury_kind", True), "chancePerMatch": ("num", True),
               "minDays": ("int", True), "maxDays": ("int", True), "returnChance": ("num", False),
               "strengthWhileHurt": ("num", False), "confidence": ("enum:data_confidence", True),
               "sources": ("strs", True), "notes": ("str", False)},
    # R-03 (RECRUITMENT_AND_TRIALS.md 3.2): a candidate pool region; `foreignBorn` = share of its men born in that
    # country (D28 Q13), `upbringing` = the football culture he learned the game in (Q14), `people` = real men first (Q2)
    "pool_region": {"id": ("str", True), "name": ("str", True), "country": ("ref:country", True),
                    "area": ("ref:area", False), "places": ("strs", True), "clubs": ("refs:club", True),
                    "size": ("int", True), "monthly": ("int", True), "sourceKinds": ("enums:source_kind", True),
                    "originClubs": ("refs:origin-club", False), "localClubs": ("strs", True),
                    "foreignBorn": ("shares:country", False), "upbringing": ("obj:upbringing", True),
                    "people": ("list:pool_person", False), "confidence": ("enum:data_confidence", True),
                    "sources": ("strs", True), "notes": ("str", False)},
    "upbringing": {"style": ("str", True), "tendencies": ("attrs", True), "chance": ("num", True),
                   "confidence": ("enum:data_confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    "pool_person": {"person": ("ref:person", True), "from": ("int", True), "until": ("int", False),
                    "kind": ("enum:source_kind", True), "playsFor": ("str", True), "position": ("enum:position", False),
                    "confidence": ("enum:confidence", True), "sources": ("strs", True), "notes": ("str", False)},
    "pack": {"id": ("str", True), "name": ("str", True), "version": ("int", True), "dataVersion": ("int", True),
             "seasons": ("strs", True), "files": ("strs", True)},
}
# pack-level file -> (record spec, id kind or None)
PACK_FILES = {
    "continents.json": ("continent", "continent"), "countries.json": ("country", "country"),
    "professions.json": ("profession", "profession"), "occupations.json": ("occupation", "occupation"),
    "names.json": ("names", None), "competitions.json": ("competition", "competition"),
    "clubs.json": ("club", "club"), "grounds.json": ("ground", "ground"), "people.json": ("person", "person"),
    "recruitment/rules.json": ("rule", "rule"), "recruitment/wages.json": ("wage", "wage"),
    "recruitment/origins.json": ("origin", "origin"), "recruitment/origin_clubs.json": ("origin_club", "origin-club"),
    "languages.json": ("language", "language"), "areas.json": ("area", "area"),
    "language_rates.json": ("language_rate", None),
    "injuries.json": ("injury", "injury"),
    "recruitment/pools.json": ("pool_region", "pool-region"),
}
SEASON_FILES = {
    "squads.json": ("squad", None), "nationality_modifiers.json": ("nationality_modifier", None),
    "region_modifiers.json": ("region", "region"),
    "results.json": ("result", None),
    "contracts.json": ("contract_band", None),
}


def is_int(n) -> bool:
    return isinstance(n, int) and not isinstance(n, bool)


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
    if spec_name in SOURCED:
        check_history_sourced(rec, spec_name, where, rep)
    check_rules(rec, spec_name, where, rep)


SOURCED = {"nickname", "club_name", "history_club", "history_person"}


def check_history_sourced(rec: dict, spec_name: str, where: str, rep: Report):
    """H-05: a history fact or text is only as good as its source: `sources` is a non-empty list of http(s) links;
    names and prose are not empty; a period runs forwards."""
    src = rec.get("sources")
    if isinstance(src, list):
        if not src:
            rep.error(f"{where}.sources", "empty (every history fact and text needs a source)")
        for i, u in enumerate(src):
            if isinstance(u, str) and not u.startswith(("http://", "https://")):
                rep.error(f"{where}.sources[{i}]", f"`{u[:40]}` is not a link")
    for field in ("name", "hook", "bio"):
        if isinstance(rec.get(field), str) and not rec[field].strip():
            rep.error(f"{where}.{field}", "empty")
    if spec_name == "history_club" and isinstance(rec.get("history"), list):
        if not rec["history"] or any(isinstance(x, str) and not x.strip() for x in rec["history"]):
            rep.error(f"{where}.history", "needs at least one non-empty paragraph")
    a, b = rec.get("from"), rec.get("to")
    if is_int(a) and is_int(b) and a > b:
        rep.error(where, f"`from` {a} is after `to` {b}")


def check_rules(rec: dict, spec_name: str, where: str, rep: Report):
    """P2-14: what a field spec can't say about the calendar and neutral grounds (same checks in GameData)."""
    if spec_name == "day_rule":
        anchors = [k for k in ("onOrAfter", "onOrBefore") if rec.get(k) is not None]
        if len(anchors) != 1:
            rep.error(where, "a date rule needs exactly one of `onOrAfter` / `onOrBefore`")
    elif spec_name == "calendar_cup":
        for k, low in (("daysBetweenRounds", 1), ("maxEarlyRounds", 0), ("drawDaysAfterRound", 0)):
            v = rec.get(k)
            if isinstance(v, int) and not isinstance(v, bool) and not low <= v <= 60:
                rep.error(f"{where}.{k}", f"{v} is outside {low}-60")
    elif spec_name == "calendar":
        cups = [c.get("competition") for c in rec.get("cups") or [] if isinstance(c, dict)]
        for c, n in Counter(cups).items():
            if n > 1:
                rep.error(f"{where}.cups", f"`{c}` has {n} calendars")
    elif spec_name == "neutral_venues":
        rounds = [r.get("round") for r in rec.get("rounds") or [] if isinstance(r, dict)]
        for r, n in Counter(rounds).items():
            if n > 1:
                rep.error(f"{where}.rounds", f"`{r}` listed {n} times")
        for i, r in enumerate(rec.get("rounds") or []):
            if isinstance(r, dict) and isinstance(r.get("grounds"), list) and not r["grounds"]:
                rep.error(f"{where}.rounds[{i}].grounds", "a neutral round needs at least one ground")


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
    elif typ == "nums":
        (isinstance(v, list) and all(is_num(x) for x in v)) or bad("a list of numbers")
    elif typ.startswith("enums:"):
        if not (isinstance(v, list) and all(isinstance(x, str) for x in v)):
            return bad("a list of texts")
        for x in v:
            if x not in ENUMS[typ[6:]]:
                rep.error(where, f"`{x}` is not one of {sorted(ENUMS[typ[6:]])}")
    elif typ.startswith("shares:"):
        if not isinstance(v, dict):
            return bad("an object of id -> share")
        for k, x in v.items():
            refs.append((typ[7:], k, f"{where}.{k}"))
            if not is_num(x) or not 0 <= x <= 1:
                rep.error(f"{where}.{k}", "expected a share between 0 and 1")
    elif typ == "monthday":
        is_monthday(v) or bad("a month-day MM-DD")
    elif typ == "monthdays":
        (isinstance(v, list) and all(is_monthday(x) for x in v)) or bad("a list of month-days MM-DD")
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
    elif typ.startswith("tiers:"):
        if not isinstance(v, dict):
            return bad("an object of id -> tier")
        for k, x in v.items():
            refs.append((typ[6:], k, f"{where}.{k}"))
            if not is_int(x) or not FAN_TIER_MIN <= x <= FAN_TIER_MAX:
                rep.error(f"{where}.{k}", f"tier must be a whole number {FAN_TIER_MIN}-{FAN_TIER_MAX}")
    elif typ == "seasontexts":
        if not isinstance(v, dict):
            return bad("an object of season id -> text")
        for k, x in v.items():
            refs.append(("season", k, f"{where}.{k}"))
            if not isinstance(x, str) or not x.strip():
                rep.error(f"{where}.{k}", "expected a non-empty text")
    elif typ.startswith("obj:"):
        check_record(v, typ[4:], where, rep, refs)


def check_text_file(data, where: str, lang: str, name: str, s: "Scan", refs: list):
    """H-05: `text/<lang>/history.json` = {"club": {id: history_club}, "person": {id: history_person}}. Keys are
    data ids and must resolve (aliases followed); unknown fields are errors like in any record."""
    rep = s.rep
    if name != "history.json":
        rep.error(where, "unknown text file (only history.json)")
        return
    if not LANG_RE.match(lang):
        rep.error(where, f"`{lang}` is not a language code (like `en`, `es`, `pt-BR`)")
    if not isinstance(data, dict):
        rep.error(where, "expected an object of kinds (club, person)")
        return
    spec_of = {"club": "history_club", "person": "history_person"}
    keys = s.texts.setdefault(lang, {})
    for kind, table in data.items():
        if kind not in spec_of:
            rep.error(where, f"unknown kind `{kind}` (allowed: {', '.join(spec_of)})")
            continue
        if not isinstance(table, dict):
            rep.error(f"{where}.{kind}", "expected an object of data id -> entry")
            continue
        for rid, entry in table.items():
            refs.append((kind, rid, f"{where}.{kind}.{rid}"))
            keys.setdefault(kind, set()).add(rid.lower())
            check_record(entry, spec_of[kind], f"{where}.{kind}.{rid}", rep, refs)


def check_rivals(s: "Scan"):
    """H-05: rivals are other clubs, and symmetric by convention (a one-sided rivalry is a warning)."""
    table = {}
    for c in s.clubs:
        if isinstance(c.get("id"), str) and isinstance(c.get("rivals"), list):
            table[c["id"].lower()] = {resolve("club", r, s.aliases, s.ids) for r in c["rivals"] if isinstance(r, str)}
    for cid, rs in table.items():
        if cid in rs:
            s.rep.error(f"club `{cid}`.rivals", "a club is not its own rival")
        for r in sorted(x for x in rs if x):
            if cid not in table.get(r, set()):
                s.rep.warn(f"club `{cid}`.rivals", f"`{r}` does not list `{cid}` back (rivalries are symmetric)")


def check_translations(s: "Scan"):
    """H-05: `en` is the fallback for every language: another language may only translate keys that exist in `en`
    (an orphan is an error); an untranslated key is a warning."""
    base = s.texts.get("en", {})
    for lang, kinds in s.texts.items():
        if lang == "en":
            continue
        for kind, keys in kinds.items():
            for k in sorted(keys - base.get(kind, set())):
                s.rep.error(f"text/{lang}/history.json.{kind}", f"`{k}` has no entry in text/en (orphan translation)")
        for kind, keys in base.items():
            missing = keys - kinds.get(kind, set())
            if missing:
                s.rep.warn(f"text/{lang}/history.json.{kind}", f"{len(missing)} entries not translated (en is used)")


def is_monthday(v) -> bool:
    if not isinstance(v, str) or not MONTHDAY_RE.match(v):
        return False
    month, day = int(v[:2]), int(v[3:])
    return day <= DAYS_IN_MONTH[month - 1]


def check_squad_roles(sq, where: str, rep: Report):
    """E-01: `positions` / `dribbling` keys are players of this squad; dribbling is on the attribute scale and
    sourced. A player without a position is a warning (the game then has no position for him)."""
    if not isinstance(sq, dict):
        return
    players = sq.get("players") if isinstance(sq.get("players"), list) else []
    for field in ("positions", "dribbling", "fanTiers"):
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


def is_num(x) -> bool:
    return isinstance(x, (int, float)) and not isinstance(x, bool)


def date_key(d) -> tuple:
    """yyyy / yyyy-mm / yyyy-mm-dd -> a tuple that sorts by date (a missing month or day counts as 0)."""
    parts = [int(p) for p in d.split("-")] if isinstance(d, str) and DATE_RE.match(d) else [0]
    return tuple(parts + [0] * (3 - len(parts)))


def recs(data, where: str):
    """(record, where) of every object of a list file."""
    for i, r in enumerate(data if isinstance(data, list) else []):
        if isinstance(r, dict):
            yield r, f"{where}[{i}]" + (f" `{r['id']}`" if isinstance(r.get("id"), str) else "")


def check_sourced(r: dict, w: str, rep: Report):
    """A record needs a source unless it is an `estimate` or `placeholder` (then `sources` says why, or is empty)."""
    src = r.get("sources")
    if isinstance(src, list) and not [x for x in src if isinstance(x, str) and x.strip()] \
            and r.get("confidence") not in UNSOURCED_OK:
        rep.error(w, "no source (only an `estimate` or `placeholder` may have none)")


def check_span(r: dict, w: str, rep: Report):
    if isinstance(r.get("until"), str) and date_key(r["until"]) < date_key(r.get("from")):
        rep.error(w, f"`until` {r['until']} is before `from` {r.get('from')}")


def check_rule_records(data, where: str, rep: Report):
    """R-02 rules: a rule kind's value fields are present and positive; a dated span; a source."""
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        check_span(r, w, rep)
        for f in RULE_VALUES.get(r.get("rule"), ()):
            if r.get(f) is None:
                rep.error(w, f"a `{r.get('rule')}` rule needs `{f}`")
        for f in ("days", "miles", "years", "pence"):
            if is_num(r.get(f)) and r[f] <= 0:
                rep.error(f"{w}.{f}", "must be above 0")


def check_wages(data, where: str, rep: Report):
    """R-02 wages: an example has a weekly wage, a band a min <= max; amounts positive; a source or `estimate`."""
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        check_span(r, w, rep)
        if r.get("type") == "example" and r.get("weeklyPence") is None:
            rep.error(w, "an `example` needs `weeklyPence`")
        if r.get("type") == "band":
            lo, hi = r.get("minWeeklyPence"), r.get("maxWeeklyPence")
            if lo is None or hi is None:
                rep.error(w, "a `band` needs `minWeeklyPence` and `maxWeeklyPence`")
            elif is_num(lo) and is_num(hi) and lo > hi:
                rep.error(w, f"minWeeklyPence {lo} is above maxWeeklyPence {hi}")
        for f in ("weeklyPence", "summerWeeklyPence", "minWeeklyPence", "maxWeeklyPence"):
            if is_num(r.get(f)) and r[f] < 0:
                rep.error(f"{w}.{f}", "must not be negative")


def check_origins(data, where: str, rep: Report):
    """R-02 origins (D28 Q12): small whole-number nudges, literacy moved by one level at most."""
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        for k, x in (r.get("nudges") or {}).items() if isinstance(r.get("nudges"), dict) else ():
            if is_num(x) and (x != int(x) or abs(x) > NUDGE_MAX):
                rep.error(f"{w}.nudges.{k}", f"a nudge is a whole number from -{NUDGE_MAX} to {NUDGE_MAX}")
        ls = r.get("literacyShift")
        if is_num(ls) and abs(ls) > LITERACY_SHIFT_MAX:
            rep.error(f"{w}.literacyShift", f"from -{LITERACY_SHIFT_MAX} to {LITERACY_SHIFT_MAX}")


def check_sources_listed(data, where: str, rep: Report):
    for r, w in recs(data, where):
        if not [x for x in r.get("sources") or [] if isinstance(x, str) and x.strip()]:
            rep.error(w, "no source")


def check_areas(data, where: str, rep: Report):
    """R-11a areas: a weight in (0, 1]; the weights of one country add up to 1."""
    total: dict[str, float] = defaultdict(float)
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        x = r.get("weight")
        if is_num(x):
            if not 0 < x <= 1:
                rep.error(f"{w}.weight", "expected a share above 0 and at most 1")
            if isinstance(r.get("country"), str):
                total[r["country"]] += x
    for c, t in sorted(total.items()):
        if abs(t - 1) > 0.005:
            rep.error(where, f"the area weights of `{c}` add up to {t:.3f}, not 1")


def check_rates(data, where: str, rep: Report):
    """R-11a rate rows: a target (an area, countries, or none = the default), a birth-year band, at least one rate;
    `only` within `speaks`; English is the default language and never listed; readWriteIfSigns is 4 shares adding
    to 1. For one target and field the bands never overlap (an area row wins over a country row, which wins over
    the default, so a lookup is never ambiguous)."""
    spans: dict[tuple, list] = defaultdict(list)
    has_default = False
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        lo, hi = r.get("bornFrom"), r.get("bornUntil")
        if is_num(lo) and is_num(hi) and lo > hi:
            rep.error(w, f"bornFrom {lo} is after bornUntil {hi}")
        if r.get("area") is not None and r.get("countries") is not None:
            rep.error(w, "a row has an `area` or `countries`, not both")
        fields = [f for f in RATE_FIELDS if r.get(f) is not None]
        if not fields:
            rep.error(w, f"no rate (one of {', '.join(RATE_FIELDS)})")
        speaks = r.get("speaks") if isinstance(r.get("speaks"), dict) else {}
        only = r.get("only") if isinstance(r.get("only"), dict) else {}
        for table, name in ((speaks, "speaks"), (only, "only")):
            if "english" in table:
                rep.error(f"{w}.{name}", "English is the default language: list only the others")
        if sum(x for x in speaks.values() if is_num(x)) > 1 + 1e-9:
            rep.error(f"{w}.speaks", "the shares add up to more than 1 (one home language besides English)")
        for k, x in only.items():
            if k not in speaks:
                rep.error(f"{w}.only.{k}", "a language in `only` must be in `speaks`")
            elif is_num(x) and is_num(speaks[k]) and x > speaks[k]:
                rep.error(f"{w}.only.{k}", f"{x} speak only it but {speaks[k]} speak it")
        pb = r.get("parentsBorn") if isinstance(r.get("parentsBorn"), dict) else {}
        if sum(x for x in pb.values() if is_num(x)) > 1 + 1e-9:
            rep.error(f"{w}.parentsBorn", "the shares add up to more than 1")
        for f in ("signsMen", "signsWomen"):
            if is_num(r.get(f)) and not 0 <= r[f] <= 1:
                rep.error(f"{w}.{f}", "expected a share between 0 and 1")
        rw = r.get("readWriteIfSigns")
        if isinstance(rw, list) and all(is_num(x) for x in rw):
            if len(rw) != READ_WRITE_LEVELS or any(x < 0 for x in rw) or abs(sum(rw) - 1) > 0.001:
                rep.error(f"{w}.readWriteIfSigns", f"expected {READ_WRITE_LEVELS} shares (signs his name, reads "
                                                   "slowly, reads well, writes well) adding up to 1")
        if isinstance(r.get("area"), str):
            targets = [("area", r["area"])]
        elif isinstance(r.get("countries"), list):
            targets = [("country", c) for c in r["countries"] if isinstance(c, str)]
        else:
            targets = [("default",)]
            has_default = has_default or "signsMen" in fields
        if is_num(lo) and is_num(hi):
            for t in targets:
                for f in fields:
                    for (a, b, w2) in spans[(t, f)]:
                        if lo <= b and a <= hi:
                            rep.error(w, f"`{f}` for {' '.join(t)} overlaps {w2} (born {a}-{b})")
                    spans[(t, f)].append((lo, hi, w))
    if isinstance(data, list) and data and not has_default:
        rep.error(where, "no default row (no area, no countries) with `signsMen`: a man born abroad has no rate")


def check_injuries(data, where: str, rep: Report):
    """G-02: each kind once; 0 <= minDays <= maxDays; chances and strengthWhileHurt in 0..1; a source or `estimate`."""
    seen = {}
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        k = r.get("kind")
        if k in seen:
            rep.error(w, f"injury kind `{k}` is listed twice (also {seen[k]})")
        seen[k] = w
        lo, hi = r.get("minDays"), r.get("maxDays")
        if is_num(lo) and lo < 0:
            rep.error(f"{w}.minDays", "must not be negative")
        if is_num(lo) and is_num(hi) and lo > hi:
            rep.error(w, f"minDays {lo} is above maxDays {hi}")
        for f in ("chancePerMatch", "returnChance", "strengthWhileHurt"):
            if is_num(r.get(f)) and not 0 <= r[f] <= 1:
                rep.error(f"{w}.{f}", "must be from 0 to 1")


def check_pools(data, where: str, rep: Report):
    """R-03 pools: a size, foreign-born shares under 1, small whole tendencies with a chance 0-1, a club in one region,
    a real man listed once with `from` <= `until`; sources unless an estimate or placeholder."""
    clubs: dict[str, str] = {}
    people: dict[str, str] = {}
    for r, w in recs(data, where):
        check_sourced(r, w, rep)
        if is_int(r.get("size")) and r["size"] < 1:
            rep.error(f"{w}.size", "must be at least 1")
        if is_int(r.get("monthly")) and r["monthly"] < 0:
            rep.error(f"{w}.monthly", "must not be negative")
        fb = r.get("foreignBorn")
        if isinstance(fb, dict) and sum(x for x in fb.values() if is_num(x)) >= 1:
            rep.error(f"{w}.foreignBorn", "the shares must add up to less than 1")
        up = r.get("upbringing")
        if isinstance(up, dict):
            check_sourced(up, f"{w}.upbringing", rep)
            if is_num(up.get("chance")) and not 0 <= up["chance"] <= 1:
                rep.error(f"{w}.upbringing.chance", "must be from 0 to 1")
            t = up.get("tendencies")
            for k, x in (t.items() if isinstance(t, dict) else ()):
                if is_num(x) and (x != int(x) or abs(x) > NUDGE_MAX):
                    rep.error(f"{w}.upbringing.tendencies.{k}",
                              f"a tendency is a whole number from -{NUDGE_MAX} to {NUDGE_MAX}")
        for c in r.get("clubs") or []:
            if isinstance(c, str):
                if c in clubs:
                    rep.error(w, f"club `{c}` is already in pool region `{clubs[c]}`")
                clubs[c] = r.get("id")
        for i, p in enumerate(r.get("people") or []):
            if not isinstance(p, dict):
                continue
            pw = f"{w}.people[{i}]"
            check_sourced(p, pw, rep)
            if is_int(p.get("until")) and is_int(p.get("from")) and p["until"] < p["from"]:
                rep.error(pw, f"`until` {p['until']} is before `from` {p['from']}")
            pid = p.get("person")
            if isinstance(pid, str):
                if pid in people:
                    rep.error(pw, f"`{pid}` is already in pool region `{people[pid]}`")
                people[pid] = r.get("id")


# pack file -> its extra checks (beyond the field spec)
FILE_CHECKS = {"recruitment/rules.json": check_rule_records, "recruitment/wages.json": check_wages,
               "recruitment/origins.json": check_origins, "recruitment/origin_clubs.json": check_sources_listed,
               "languages.json": check_sources_listed, "areas.json": check_areas, "language_rates.json": check_rates,
               "injuries.json": check_injuries, "recruitment/pools.json": check_pools}


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


def check_contract_bands(data: list, where: str, rep: Report):
    """G-04: shares are 0-1, money is not negative and min <= max, squads can field eleven, one band per standing;
    only the `amateur` band lists clubs."""
    seen = Counter()
    for i, b in enumerate(data):
        if not isinstance(b, dict):
            continue
        rw = f"{where}[{i}]"
        seen[b.get("standing")] += 1
        for f in ("professionalShare", "summerPayShare"):
            v = b.get(f)
            if isinstance(v, (int, float)) and not isinstance(v, bool) and not 0 <= v <= 1:
                rep.error(rw, f"`{f}` {v} is outside 0-1")
        for f in ("seasonWageMin", "seasonWageMax", "summerWageMin", "summerWageMax", "amateurExpenses"):
            v = b.get(f)
            if isinstance(v, (int, float)) and not isinstance(v, bool) and v < 0:
                rep.error(rw, f"`{f}` {v} is negative")
        for lo, hi in (("seasonWageMin", "seasonWageMax"), ("summerWageMin", "summerWageMax")):
            a, z = b.get(lo), b.get(hi)
            if all(isinstance(x, (int, float)) and not isinstance(x, bool) for x in (a, z)) and a > z:
                rep.error(rw, f"`{lo}` {a} is above `{hi}` {z}")
        m, s = b.get("minimumSquad"), b.get("squadSize")
        if isinstance(m, int) and not isinstance(m, bool) and m < 11:
            rep.error(rw, f"`minimumSquad` {m} cannot field eleven")
        if all(isinstance(x, int) and not isinstance(x, bool) for x in (m, s)) and m > s:
            rep.error(rw, f"`minimumSquad` {m} is above `squadSize` {s}")
        if b.get("clubs") and b.get("standing") != "amateur":
            rep.error(rw, "only the `amateur` band lists clubs")
    for standing, n in seen.items():
        if n > 1:
            rep.error(where, f"standing `{standing}` has {n} bands")
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
        self.texts: dict[str, dict[str, set]] = {}   # language -> kind -> keys (text/<lang>/history.json)
        self.clubs: list[dict] = []


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
            if len(parts) == 3 and parts[0] == "text":
                check_text_file(data, where, parts[1], parts[2], s, refs)
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
            if spec_name == "club":
                s.clubs.extend(r for r in data if isinstance(r, dict))
            if spec_name == "squad":
                squads_by_season[parts[1]] = data
                for i, rec in enumerate(data):
                    check_squad_roles(rec, f"{where}[{i}]", rep)
            if spec_name == "result":
                results.append((data, where, parts[1]))
            if spec_name == "contract_band":
                check_contract_bands(data, where, rep)
            if f in FILE_CHECKS:
                FILE_CHECKS[f](data, where, rep)
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
    check_rivals(s)
    check_translations(s)

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
