# Analysis — Soak Slowdown Root Cause & Long-Test Tooling Requirements

**Document type:** Analysis (Layer 4 — research archive; do not bulk-load into agent context)
**Date:** 2026-07-15
**Requested by:** PO, in two follow-ups: "why do later seasons take so much longer to simulate" and
"why does the soak process keep getting killed, and we need progress reporting for any long test"
**Status:** Analysis only — turned into requirements below for PM/devs to plan. Two of the three
performance items are already fixed as of this document; the tooling items are not yet built.

---

## 1. Why later seasons took longer (root cause, both fixed 2026-07-15)

Two independent O(n)-in-history hotspots compounded across a 60-season soak, each re-scanning data
that only grows, never shrinks, across the run:

| # | Hotspot | Symptom | Fix | Status |
|---|---------|---------|-----|--------|
| 1 | `LeagueStandingsManager.isLeagueComplete()` (and related scans) iterated the club's **entire** `playedMatches` history every simulated day to check season completion, instead of only the current season's matches. | Seasons 50-60 took ~2-3x longer per season than seasons 1-10 in the first diagnostic run. | Added `Club.playedMatchesSeasonStartIndex` watermark; completion checks now only scan matches from the current season forward. | ✅ Fixed, verified: 87.23 s/season → ~70 s/season average in the 2026-07-12 full re-run. |
| 2 | `MessageManager` duplicate-message checks scanned the **entire** inbox (`currentGame.getAllMessages()`, all seasons) on every message delivery, looking for an existing message with the same id or same type+title. | Smaller, secondary contributor to the same late-season slowdown pattern; not the dominant cause but additive with #1. | Added O(1) `HashSet` dedup caches (`deliveredMessageIds`, `deliveredTypeTitleKeys`), lazily rebuilt only when the underlying message list reference changes (e.g. after a load). | ✅ Fixed 2026-07-15 (later same day), covered by existing message-delivery tests; no new perf test added for this specific hotspot (see PERF-3 below). |

**Pattern to watch for:** both bugs share the same shape — *"iterate every X that ever existed"* inside a
loop that only cares about *"X in the current season/window."* This is a natural trap in a game that is
explicitly designed to run forever (procedural seasons), so it will recur unless guarded against (see
PERF-2).

---

## 2. Why the soak process kept getting killed (this session)

Across this session, attempts to run the full 60-season soak (`mvn test -Psoak -Dtest=LongRunSoakIT`,
expected ~60-70 min) as a background shell job were repeatedly terminated before completion — 5-6 times
in a row — **not by a test assertion failing**, but by the process being killed externally (by the
IDE/agent tooling's background-shell lifecycle, not by the JVM or Maven itself). Evidence:

- Each time, checking for `java`/`mvn` processes afterward found either nothing, or **orphaned zombie
  processes** still consuming CPU (see incident below) — i.e. the parent shell session was torn down
  without the child JVM being told to stop, or vice versa.
- One incident produced **9 leftover Java processes**, 4 of them separate `surefirebooter` test forks
  from 4 different killed attempts, one of which had accumulated **1074 CPU-seconds** — this is the
  direct cause of the "CPU very high" symptom the PO reported, and it was pure waste: no test evidence
  was produced by any of those 4 abandoned runs.
- The log output itself (when captured) showed clean `PROGRESS|season=N|pct=...|eta=...` lines with no
  errors up to the point of termination — reinforcing that the kills were environmental, not test bugs.

**Conclusion:** this is a **tooling reliability problem**, not a product-code problem. Retrying the same
command the same way is unlikely to succeed more often; it needs either a different execution mode
(detached process, not a supervised background shell) or an explicit stop-and-report rule after N
consecutive environmental kills, so time and CPU aren't spent on repeat attempts that provide no new
information.

---

## 3. Formal requirements for PM / devs

### Performance

| ID | Requirement | Status |
|----|-------------|--------|
| PERF-1 | Fix the `MessageManager` O(n) duplicate-message scan. | ✅ Done 2026-07-15 (§1, item 2). |
| PERF-2 | Add a coding-convention note (this doc + a short note in `AGENTS.md` or a code comment convention) warning against scanning an unbounded "every X that ever existed in the save" list inside a per-day/per-match hot path; prefer a season-scoped watermark or index, following the `playedMatchesSeasonStartIndex` pattern. | ⬜ Not yet written into `AGENTS.md`; recommended next small doc change. |
| PERF-3 | Add an automated performance-regression gate on a **short** (PR-gate-speed) soak test, not just the nightly 60-season one, so a future regression is caught in minutes, not during a multi-hour soak. `LongRunSoakIT` already gates on `MAX_SECONDS_PER_SEASON`; no equivalent gate exists yet on `MultiSeasonSoakIT` (the ≥3-season PR-gate test) or `MultiSeasonLeagueAndCupSoakIT` (15-season nightly test). | ⬜ Not yet built. |

### Tooling (running & monitoring long tests reliably)

| ID | Requirement | Status |
|----|-------------|--------|
| TOOL-1 | Make the 60-season soak runnable **detached** from any interactive/agent session (e.g. a CI job, a scheduled task, or a `Start-Process` with no parent tie), so it survives the session or IDE closing. | ⬜ Not built. Today it only runs as a foreground or agent-backgrounded Maven process. |
| TOOL-2 | Persist soak progress to a **durable status file** on disk (season, pct, ETA, last-error) that any process can poll, independent of console/stderr capture. `HarnessProgress` currently writes to stderr only — fine for an attached session, useless once detached (TOOL-1) or after a kill destroys the log buffer. | ⬜ Not built. `HarnessProgress` (added 2026-07-15) is stderr-only. |
| TOOL-3 | When asked to run or check on a long test, the agent/dev should state the **expected duration up front** and report progress **from the durable status file** (TOOL-2), not by guessing or polling blindly. | ✅ Adopted as working practice this session (progress % + ETA now reported); ⬜ still depends on TOOL-2 for full reliability once detached. |
| TOOL-4 | If a long job is killed by the environment more than **2-3 times in a row with no test-side error evidence**, stop retrying and report the pattern instead of repeating — matches the explicit PO feedback this session ("if external forces finish the test 5 or 6 times in a row you need to lighten up"). | ✅ Adopted as working practice this session (no further blind retries after the pattern was recognized); ⬜ not yet codified as an automated or documented rule anyone else would follow by default. |

---

## 4. Recommendation

None of PERF-2/3 or TOOL-1..4 block R-1888 sign-off — the existing 2026-07-12 full soak evidence is
still valid (same commit, `fd8e742`, still `HEAD`) and PERF-1 is already fixed. These are backlog items
for whoever owns test infrastructure next, prioritized roughly in the order listed (PERF-3 and TOOL-2 are
the two with the best cost/benefit — they would have prevented most of the pain in this session).

**Suggested owner/timing:** post-R-1888, alongside or before starting R-1892, since R-1892 (second
division + reserves) will make soaks longer, not shorter, and will make TOOL-1/2 more valuable, not less.

---

*Cross-references: [ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md](ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md) (original root-cause writeup for hotspot #1) · [DESIGN_SOAK_PROGRESS_REPORTING_V1.md](DESIGN_SOAK_PROGRESS_REPORTING_V1.md) (progress-reporting design, TOOL-3 partial implementation) · [PM_REPORT_PROJECT_STATUS_2026_07_15.md](PM_REPORT_PROJECT_STATUS_2026_07_15.md) §10-11.*
