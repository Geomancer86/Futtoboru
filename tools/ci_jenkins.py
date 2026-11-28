#!/usr/bin/env python3
"""
CI-02 (D32): the WORKSTATION Jenkins job "Futtoboru DEV" from a script. GitHub is only git storage; Jenkins is the CI.

    python tools/ci_jenkins.py run [--branch b3/<item>] [--tier full]   Build with Parameters (BUILD_BRANCH, TEST_TIER)
    python tools/ci_jenkins.py status                                     the last builds of the job

- Needs the home LAN. Reads ~/.futtoboru/jenkins.properties (or FUTTOBORU_JENKINS_PROPERTIES): `url=`, and `user=` +
  `token=` or `credentials=<another properties file with them>`. Nothing about the server is in this public repo.
- Off the home LAN: run the level gate locally (`python tools/test_level.py --run`; a local `-Pfull` through
  tools/suite_lock.py when a change needs it); Jenkins builds develop once it is pushed (polls every 15 min).
- Exit code: 0 queued / listed, 3 no properties file or Jenkins not reachable.
"""
import argparse
import base64
import json
import os
import sys
import urllib.parse
import urllib.request

JOB = "Futtoboru DEV"
TIERS = ("auto", "fast", "slow", "full", "smoke")


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


def main(argv=None):
    parser = argparse.ArgumentParser(description="The WORKSTATION Jenkins job (CI-02)")
    sub = parser.add_subparsers(dest="action", required=True)
    p = sub.add_parser("run", help="Build with Parameters")
    p.add_argument("--branch", default="develop")
    p.add_argument("--tier", choices=TIERS, default="full")
    sub.add_parser("status", help="last builds")
    args = parser.parse_args(argv)

    path = os.environ.get("FUTTOBORU_JENKINS_PROPERTIES", "~/.futtoboru/jenkins.properties")
    try:
        conf = properties(path)
    except OSError:
        print("ci_jenkins: no %s (url=, user= + token= or credentials=)" % path)
        return 3
    root = conf["url"].rstrip("/")
    job = root + "/job/" + urllib.parse.quote(JOB)
    auth = "Basic " + base64.b64encode(("%s:%s" % (conf["user"], conf["token"])).encode()).decode()
    opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor())

    def call(url, data=None, headers=None):
        h = {"Authorization": auth}
        h.update(headers or {})
        with opener.open(urllib.request.Request(url, data=data, headers=h), timeout=20) as response:
            return response.read().decode("utf-8", "replace"), response.headers

    try:
        if args.action == "status":
            body, _ = call(job + "/api/json?tree=builds[number,result,displayName]{0,8}")
            for build in json.loads(body)["builds"]:
                print("%-55s %s" % (build["displayName"], build["result"] or "RUNNING"))
            return 0
        crumb = json.loads(call(root + "/crumbIssuer/api/json")[0])  # POSTs need the CSRF crumb (same cookie jar)
        data = urllib.parse.urlencode({"BUILD_BRANCH": args.branch, "TEST_TIER": args.tier}).encode()
        _, headers = call(job + "/buildWithParameters", data, {crumb["crumbRequestField"]: crumb["crumb"]})
        print("ci_jenkins: queued %s (%s tier): %s" % (args.branch, args.tier, headers.get("Location")))
        return 0
    except OSError as e:
        print("ci_jenkins: Jenkins not reachable (%s): off the home LAN run the gate locally" % e)
        return 3


if __name__ == "__main__":
    sys.exit(main())
