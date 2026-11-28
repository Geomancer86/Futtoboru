#!/usr/bin/env python3
"""
CI-01 (D30 A11): remote test runs from any network, without a login (the Jenkins box is only on the home LAN).

    python tools/ci_remote.py run [--tier full|fast|smoke] [--keep] [--no-wait]
        pushes HEAD (as committed) to ci/<tier>/<machine>-<utc stamp>; GitHub Actions runs that tier (.github/workflows/
        ci.yml); waits, prints the verdict and each failing test, then deletes the ci/ branch (unless --keep)
    python tools/ci_remote.py status [--branch develop] [-n 5]
        the last runs of a branch, and the failing tests of the newest failed one
    python tools/ci_remote.py annotate
        (inside CI, after a failed Maven step) prints a GitHub ::error annotation per failing test from the surefire /
        failsafe XML reports; annotations are public, so `run` and `status` can read them without a token
    python tools/ci_remote.py jenkins [--branch b3/<item>] [--tier full] [--status]
        on the home LAN only: the local Jenkins job "Futtoboru DEV" (Build with Parameters / last builds). Reads
        ~/.futtoboru/jenkins.properties: url=..., and user= + token= or credentials=<another properties file with them>.
        Nothing about the server is in this public repo

- Uses only git and the public GitHub API (60 requests an hour per network without a token: polls once a minute)
- Exit code: 0 green, 1 tests failed, 2 the run failed otherwise or was cancelled, 3 could not push or reach GitHub,
  4 no result within --wait-min
"""
import argparse
import base64
import datetime
import glob
import json
import os
import re
import socket
import subprocess
import sys
import time
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = "Geomancer86/Futtoboru"
API = "https://api.github.com/repos/" + REPO
TIERS = ("fast", "full", "smoke")
POLL_S = 60
MAX_ANNOTATIONS = 40


def git(*args):
    return subprocess.run(["git", "-C", ROOT] + list(args), capture_output=True, text=True)


def api(path):
    request = urllib.request.Request(API + path, headers={"Accept": "application/vnd.github+json",
                                                          "User-Agent": "futtoboru-ci-remote"})
    with urllib.request.urlopen(request, timeout=60) as response:
        return json.loads(response.read().decode("utf-8"))


# ------------------------------------------------------------------------------------------------------------ annotate

def failures(report_glob=("**/target/surefire-reports/TEST-*.xml", "**/target/failsafe-reports/TEST-*.xml")):
    """[(test name, first message line)] of every failed or errored test case in the XML reports"""
    found = []
    for pattern in report_glob:
        for path in sorted(glob.glob(os.path.join(ROOT, pattern), recursive=True)):
            try:
                suite = ET.parse(path).getroot()
            except ET.ParseError:
                continue
            for case in suite.iter("testcase"):
                for kind in ("failure", "error"):
                    node = case.find(kind)
                    if node is not None:
                        text = (node.get("message") or node.text or kind).strip().splitlines()
                        name = "%s.%s" % (case.get("classname", "?").rsplit(".", 1)[-1], case.get("name", "?"))
                        found.append((name, (node.get("type", kind).rsplit(".", 1)[-1] + ": " + (text[0] if text else ""))[:300]))
    return found


def escape(text):
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def annotate(_args):
    found = failures()
    for name, message in found[:MAX_ANNOTATIONS]:
        print("::error title=%s::%s" % (escape(name).replace(",", "%2C").replace(":", "%3A"), escape(message)))
    if len(found) > MAX_ANNOTATIONS:
        print("::error title=more failures::%d more failing tests in the test-reports artifact" % (len(found) - MAX_ANNOTATIONS))
    if not found:
        print("::error title=no failing test in the reports::the build failed outside the tests (compile, plugin, test_tiers)")
    return 0


# ------------------------------------------------------------------------------------------------------- run / status

def machine():
    """b3 or ws from the current branch prefix, else the host name (a branch word)"""
    current = git("rev-parse", "--abbrev-ref", "HEAD").stdout.strip()
    prefix = current.split("/", 1)[0] if "/" in current else ""
    word = prefix if prefix in ("b3", "ws") else socket.gethostname()
    return re.sub(r"[^a-z0-9-]+", "-", word.lower()).strip("-") or "agent"


def request_branch(tier, who, when=None):
    when = when or datetime.datetime.now(datetime.timezone.utc)
    return "ci/%s/%s-%s" % (tier, who, when.strftime("%Y%m%d-%H%M%S"))


def run_for(branch, sha=None):
    runs = api("/actions/runs?" + urllib.parse.urlencode({"branch": branch, "per_page": 10}))["workflow_runs"]
    runs = [r for r in runs if sha is None or r["head_sha"] == sha]
    return runs[0] if runs else None


def report(run):
    """prints the run's verdict and failing tests; @return the exit code"""
    print("run #%s %s @ %s: %s (%s)" % (run["run_number"], run["head_branch"], run["head_sha"][:7],
                                        run["conclusion"] or run["status"], run["html_url"]))
    failed_tests = False
    for job in api("/actions/runs/%d/jobs" % run["id"])["jobs"]:
        print("  %-60s %s" % (job["name"][:60], job["conclusion"] or job["status"]))
        if job["conclusion"] == "failure":
            for note in api("/check-runs/%d/annotations" % job["id"]):
                if note["annotation_level"] == "failure" and note["title"]:
                    failed_tests = True
                    print("    FAILED %s: %s" % (note["title"], note["message"]))
    if run["conclusion"] == "success":
        return 0
    return 1 if failed_tests else 2


def wait_for(branch, sha, wait_min):
    deadline = time.time() + wait_min * 60
    run = None
    while time.time() < deadline:
        run = run_for(branch, sha)
        if run and run["status"] == "completed":
            return run
        time.sleep(POLL_S)
    return run if run and run["status"] == "completed" else None


def run(args):
    if git("status", "--porcelain", "--untracked-files=no").stdout.strip():
        print("ci_remote: uncommitted changes are not sent; HEAD as committed is tested")
    sha = git("rev-parse", "HEAD").stdout.strip()
    branch = request_branch(args.tier, machine())
    pushed = git("push", "-q", "origin", "HEAD:refs/heads/" + branch)
    if pushed.returncode != 0:
        print("ci_remote: push failed: " + pushed.stderr.strip())
        return 3
    print("ci_remote: %s @ %s pushed; tier %s on GitHub Actions" % (branch, sha[:7], args.tier), flush=True)
    if args.no_wait:
        print("ci_remote: later: python tools/ci_remote.py status --branch %s" % branch)
        return 0
    try:
        finished = wait_for(branch, sha, args.wait_min)
    except OSError as e:
        print("ci_remote: GitHub not reachable: %s" % e)
        return 3
    try:
        if finished is None:
            print("ci_remote: no result within %d min; python tools/ci_remote.py status --branch %s" % (args.wait_min, branch))
            return 4
        return report(finished)
    finally:
        if finished is not None and not args.keep:
            git("push", "-q", "origin", "--delete", branch)


def status(args):
    try:
        runs = api("/actions/runs?" + urllib.parse.urlencode({"branch": args.branch, "per_page": args.n}))["workflow_runs"]
    except OSError as e:
        print("ci_remote: GitHub not reachable: %s" % e)
        return 3
    if not runs:
        print("no runs for %s" % args.branch)
        return 4
    for r in runs:
        print("#%-5s %-10s %-10s %s %s %s" % (r["run_number"], r["event"], r["conclusion"] or r["status"],
                                             r["created_at"], r["head_sha"][:7], r["display_title"][:60]))
    failed = next((r for r in runs if r["conclusion"] == "failure"), None)
    if failed:
        print()
        report(failed)
    return 0 if runs[0]["conclusion"] == "success" else (2 if runs[0]["status"] == "completed" else 0)


# ------------------------------------------------------------------------------------------------------------- jenkins

def properties(path):
    values = {}
    with open(os.path.expanduser(path), encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if line and not line.startswith("#") and "=" in line:
                key, value = line.split("=", 1)
                values[key.strip()] = value.strip()
    if "credentials" in values and "token" not in values:
        values.update({k: v for k, v in properties(values["credentials"]).items() if k in ("user", "token")})
    return values


def jenkins(args):
    path = os.environ.get("FUTTOBORU_JENKINS_PROPERTIES", "~/.futtoboru/jenkins.properties")
    try:
        conf = properties(path)
    except OSError:
        print("ci_remote: no %s (url=, user= + token= or credentials=); use `run` (GitHub) instead" % path)
        return 3
    base = conf["url"].rstrip("/") + "/job/" + urllib.parse.quote("Futtoboru DEV")
    auth = "Basic " + base64.b64encode(("%s:%s" % (conf["user"], conf["token"])).encode()).decode()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor())

    def call(url, data=None, headers=None):
        h = {"Authorization": auth}
        h.update(headers or {})
        with opener.open(urllib.request.Request(url, data=data, headers=h), timeout=20) as response:
            return response.read().decode("utf-8", "replace"), response.headers

    try:
        if args.status:
            body, _ = call(base + "/api/json?tree=builds[number,result,displayName]{0,8}")
            for b in json.loads(body)["builds"]:
                print("%-50s %s" % (b["displayName"], b["result"] or "RUNNING"))
            return 0
        crumb = json.loads(call(conf["url"].rstrip("/") + "/crumbIssuer/api/json")[0])
        data = urllib.parse.urlencode({"BUILD_BRANCH": args.branch, "TEST_TIER": args.tier}).encode()
        _, headers = call(base + "/buildWithParameters", data, {crumb["crumbRequestField"]: crumb["crumb"]})
        print("ci_remote: Jenkins queued %s (%s tier): %s" % (args.branch, args.tier, headers.get("Location")))
        return 0
    except OSError as e:
        print("ci_remote: Jenkins not reachable (%s); off the home LAN use `run` (GitHub) instead" % e)
        return 3


def main(argv=None):
    parser = argparse.ArgumentParser(description="Remote test runs (CI-01)")
    sub = parser.add_subparsers(dest="action", required=True)
    p = sub.add_parser("run", help="push HEAD as ci/<tier>/..., wait, print the verdict")
    p.add_argument("--tier", choices=TIERS, default="full")
    p.add_argument("--keep", action="store_true", help="keep the ci/ branch afterwards")
    p.add_argument("--no-wait", action="store_true")
    p.add_argument("--wait-min", type=float, default=45)
    p = sub.add_parser("status", help="last runs of a branch")
    p.add_argument("--branch", default="develop")
    p.add_argument("-n", type=int, default=5)
    sub.add_parser("annotate", help="(in CI) failing tests as ::error annotations")
    p = sub.add_parser("jenkins", help="the home Jenkins job (home LAN only)")
    p.add_argument("--branch", default="develop")
    p.add_argument("--tier", choices=("auto", "fast", "slow", "full", "smoke"), default="full")
    p.add_argument("--status", action="store_true")
    args = parser.parse_args(argv)
    return {"run": run, "status": status, "annotate": annotate, "jenkins": jenkins}[args.action](args)


if __name__ == "__main__":
    sys.exit(main())
