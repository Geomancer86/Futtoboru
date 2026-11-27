"""Write seasons/<season>/results.json (E-00b) from the research CSVs in docs/data (E-00).

Reference data only (decision D17): history screens and engine calibration tests read it; game code never uses it
to schedule or decide a match the player plays. Club ids go through aliases.json, so the JSON always holds the
current ids. Walkovers (no date, no goals in the CSV) get `walkover` = the club that went through.

Usage: python tools/data/results_csv_to_json.py [--season 1888-89] [--check]
--check exits 1 when the JSON on disk differs from what the CSVs give (used by the tests).
"""
from __future__ import annotations

import argparse
import csv
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
REPO = HERE.parents[1]
sys.path.insert(0, str(HERE))
import validate  # noqa: E402

DOCS = REPO / "docs" / "data"


def club(rid: str, aliases: dict) -> str:
    seen = 0
    while rid in aliases and seen < 16:
        rid, seen = aliases[rid], seen + 1
    return rid


def walkover_winner(row: dict) -> str:
    # "walkover (Linfield conceded the tie)" / "walkover (Swifts scratched)": the home side went through in both
    # 1888-89 cases; any other walkover needs the source text read by hand.
    src = row["source"].lower()
    if "walkover" not in src:
        raise ValueError(f"no goals and no walkover note: {row}")
    return row["home_id"]


def record(row: dict, edition: str, aliases: dict, cup: bool) -> dict:
    rec: dict = {"edition": edition}
    if cup:
        rec["round"] = row["round"]
    if row["date"]:
        rec["date"] = row["date"]
    rec["home"] = club(row["home_id"], aliases)
    rec["away"] = club(row["away_id"], aliases)
    if row["home_goals"] != "" and row["away_goals"] != "":
        rec["homeGoals"] = int(row["home_goals"])
        rec["awayGoals"] = int(row["away_goals"])
    else:
        rec["walkover"] = club(walkover_winner(row), aliases)
    if row.get("attendance"):
        rec["attendance"] = int(row["attendance"])
    if cup and row.get("venue"):
        rec["venue"] = row["venue"]
    rec["source"] = row["source"]
    return rec


def build(season: str) -> list:
    core = validate.DEFAULT_ROOT / "core"
    aliases = json.loads((core / "aliases.json").read_text(encoding="utf-8")).get("club", {})
    out = []
    with open(DOCS / f"results_{season}.csv", encoding="utf-8") as f:
        out += [record(r, f"football-league-{season}", aliases, False) for r in csv.DictReader(f)]
    with open(DOCS / f"facup_{season}.csv", encoding="utf-8") as f:
        out += [record(r, f"fa-cup-{season}", aliases, True) for r in csv.DictReader(f)]
    return out


def dump(records: list) -> str:
    return json.dumps(records, ensure_ascii=False, indent=4) + "\n"


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--season", default="1888-89")
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()
    target = validate.DEFAULT_ROOT / "core" / "seasons" / args.season / "results.json"
    text = dump(build(args.season))
    if args.check:
        same = target.exists() and target.read_text(encoding="utf-8") == text
        print("results.json matches the CSVs" if same else f"{target} differs from the CSVs: rerun without --check")
        return 0 if same else 1
    target.write_text(text, encoding="utf-8", newline="\n")
    print(f"wrote {target} ({len(json.loads(text))} records)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
