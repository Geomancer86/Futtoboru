#!/usr/bin/env python3
"""
T-01 (D27): guard of the test pyramid (ported from DarkBlade tools/test_tiers.py, CAL-47). The fast tier (the default
`./mvnw verify`) leaves out @Tag("slow") (and the old "season"), so an untagged class that takes more than the limit
in the surefire reports would make the tier that gates every push slow again.

    python tools/test_tiers.py                 reads */target/surefire-reports after a Maven run
    python tools/test_tiers.py --limit 5       another limit in seconds (5 by default; tag a class over ~2 s)
    python tools/test_tiers.py --all           every class with its time

A class tagged slow/season/smoke is skipped; in a class with slow-tagged methods those methods' times are left out.
Exit 1 when an untagged class is over the limit: add @Tag("slow") to it (or to its slow methods).
"""
import argparse
import glob
import os
import re
import sys
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LIMIT = float(os.environ.get("FUTTOBORU_TIERS_LIMIT", "5.0"))  # WORKSTATION runs suites concurrently: 8
SLOW_TAGS = ("slow", "season", "smoke", "soak")
_TAG = re.compile(r'@Tag\("(%s)"\)' % "|".join(SLOW_TAGS))


def source_of(class_name):
    rel = class_name.replace(".", "/") + ".java"
    for path in glob.glob(os.path.join(ROOT, "*", "src", "test", "java", rel)):
        with open(path, encoding="utf-8", errors="replace") as handle:
            return handle.read()
    return ""


def slow_parts(source):
    """(class tagged slow, {method names tagged slow})"""
    head = re.search(r"^(?:@[^\n]*\n)*(?:public |final |abstract )*class ", source, re.M)
    class_tagged = bool(head and _TAG.search(head.group(0)))
    methods = set()
    for m in re.finditer(r"((?:[ \t]*@[^\n]*\n)+)[ \t]*(?:public |protected |private |static )*void (\w+)\(", source):
        if _TAG.search(m.group(1)):
            methods.add(m.group(2))
    return class_tagged, methods


def class_times(root=ROOT):
    """[(fast seconds, tests, class, tagged)] of every surefire report, slowest first"""
    rows = []
    for path in glob.glob(os.path.join(root, "*", "target", "surefire-reports", "TEST-*.xml")):
        suite = ET.parse(path).getroot()
        name = suite.get("name", "?")
        class_tagged, methods = slow_parts(source_of(name))
        seconds = float(suite.get("time", "0") or 0)
        for case in suite.iter("testcase"):
            if case.get("name", "").split("(")[0] in methods:
                seconds -= float(case.get("time", "0") or 0)
        rows.append((max(seconds, 0.0), int(suite.get("tests", "0")), name, class_tagged))
    return sorted(rows, reverse=True)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--limit", type=float, default=LIMIT)
    parser.add_argument("--all", action="store_true")
    args = parser.parse_args(argv)
    rows = class_times()
    if not rows:
        print("no surefire reports: run ./mvnw -B verify first")
        return 1
    bad = [r for r in rows if not r[3] and r[0] > args.limit]
    for seconds, tests, name, tagged in rows:
        if args.all:
            print("  %6.2f s %3d  %s%s" % (seconds, tests, name.rsplit(".", 1)[-1], " (slow)" if tagged else ""))
    for seconds, tests, name, _ in bad:
        print("SLOW AND UNTAGGED: %s took %.1f s (%d tests): add @Tag(\"slow\")" % (name, seconds, tests))
    fast = [r for r in rows if not r[3]]
    print("test tiers: %d classes, %.0f s summed outside @Tag(\"slow\"); %d over %.0f s"
          % (len(rows), sum(r[0] for r in fast), len(bad), args.limit))
    return 1 if bad else 0


if __name__ == "__main__":
    sys.exit(main())
