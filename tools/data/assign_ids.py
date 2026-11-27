"""Give new game data records their runtime number in the pack's append-only ids.json registry.

Every record with a data id (club, person, ground...) has a number in `<pack>/ids.json`; the game uses it as the
record's runtime id, so saved games keep pointing at the same records when the data is corrected. This tool only
adds: a new record gets the next free number of its pack, an existing entry is never changed, removed or reused.
A renamed record (old id kept in aliases.json) needs nothing: it inherits the old id's number.
A record removed for good: leave its entry and add its id to `retired` (by hand, in the same commit).

Usage:  python tools/data/assign_ids.py [--root DIR] [--check]
  --check  change nothing; exit 1 when some record has no number yet (for CI and the pre-push routine)
"""
from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import validate  # noqa: E402


def new_registry(taken_ranges: list[tuple[int, int]]) -> dict:
    """A registry for a pack that has none: the first free block of numbers (1..99 999, 100 000..199 999, ...)."""
    for k in range((validate.REGISTRY_MAX + 1) // validate.BLOCK):
        first, last = max(1, k * validate.BLOCK), (k + 1) * validate.BLOCK - 1
        if not any(a <= last and first <= b for a, b in taken_ranges):
            return {"range": [first, last], "next": first, "ids": {kind: {} for kind in validate.KINDS},
                    "retired": {}}
    raise SystemExit("no free block of numbers left for a new pack")


def assign(root: Path, write: bool = True) -> list[str]:
    """Numbers every record that has none; returns `pack kind id -> number` lines. Writes only when `write`."""
    s = validate.scan(root)
    if s.rep.errors:
        raise SystemExit("fix these data errors first:\n  " + "\n  ".join(s.rep.errors))
    numbers = validate.record_numbers(s)
    taken = [tuple(reg["range"]) for reg, _w in s.registries.values()
             if isinstance(reg, dict) and isinstance(reg.get("range"), list)]
    changed: dict[str, dict] = {}
    added: list[str] = []
    for pack, kind, low, _where in s.records:
        if (kind, low) in numbers:
            continue
        if pack not in s.registries:
            reg = new_registry(taken)
            taken.append(tuple(reg["range"]))
            s.registries[pack] = (reg, f"{pack}/ids.json")
        reg = s.registries[pack][0]
        used = [n for t in reg["ids"].values() for n in t.values()]
        n = max([reg["next"]] + [u + 1 for u in used])
        if n > reg["range"][1]:
            raise SystemExit(f"pack `{pack}` has no free number left in its range {reg['range']}")
        reg["ids"].setdefault(kind, {})[low] = n
        reg["next"] = n + 1
        numbers[(kind, low)] = n
        changed[pack] = reg
        added.append(f"{pack} {kind} `{low}` -> {n}")
    if write:
        for pack, reg in changed.items():
            write_registry(root / pack, reg)
    return added


def write_registry(pdir: Path, reg: dict):
    ids = {k: dict(sorted(reg["ids"].get(k, {}).items(), key=lambda e: e[1])) for k in validate.KINDS}
    for k, t in reg["ids"].items():  # keep any other kind as it was (the validator reports it)
        ids.setdefault(k, t)
    out = {"range": reg["range"], "next": reg["next"], "ids": ids,
           "retired": {k: v for k, v in reg.get("retired", {}).items()}}
    write_json(pdir / "ids.json", out)
    manifest_path = pdir / "pack.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    files = manifest.get("files", [])
    if "ids.json" not in files:
        # the file order is the load order of the records: insert, don't sort
        at = next((i for i, f in enumerate(files) if f > "ids.json"), len(files))
        manifest["files"] = files[:at] + ["ids.json"] + files[at:]
        write_json(manifest_path, manifest)


def write_json(path: Path, data):
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8", newline="\n")


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--root", type=Path, default=validate.DEFAULT_ROOT)
    ap.add_argument("--check", action="store_true", help="change nothing, exit 1 if a record has no number")
    args = ap.parse_args()
    added = assign(args.root, write=not args.check)
    for line in added:
        print(("missing " if args.check else "added   ") + line)
    if args.check:
        print(f"{len(added)} records without a number" if added else "every record has a number")
        return 1 if added else 0
    print(f"{len(added)} numbers added")
    return 0


if __name__ == "__main__":
    sys.exit(main())
