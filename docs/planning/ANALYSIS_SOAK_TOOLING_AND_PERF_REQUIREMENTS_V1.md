# Analysis — Soak Slowdown Root Cause & Long-Test Tooling Requirements

**Document type:** Analysis (Layer 4 — research archive; do not bulk-load into agent context)
**Date:** 2026-07-15
**Requested by:** PO, in two follow-ups: "why do later seasons take so much longer to simulate" and
"why does the soak process keep getting killed, and we need progress reporting for any long test"
**Status:** Updated 2026-07-19 after profiling the still-unexplained PERF-4 residual growth with JFR
(Java Flight Recorder) instead of more manual code reading. Result: found and fixed a **sixth**
O(all-time-history) hotspot (`JobManager.getAvailableJobs()`) that turned out to be the dominant one all
along — `java.time.LocalTime.compareTo` alone accounted for ~85% of all sampled CPU time in a profiled
60-season run. Fixing it took the full 60-season soak from ~74-93 s/season (~80-90 minutes total) to
**1.24 s/season (~74 seconds total)** — a ~60-75x speedup. See §3.2. All known performance hotspots are
now fixed; the tooling items remain backlog (not blocking); the policy fix that resolves the human-tester
frustration is recorded in §4.

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

### 1.1 Third pass (evening, same day): the pattern recurred three more times

A team report that "seasons are not completing / lots of errors are happening" prompted re-running the
confirmed-clean soak from scratch to double check. Result: **60/60 champions, 0 errors, BUILD SUCCESS** —
the specific claims were not reproduced. But the underlying growth curve was still real: season 1 took
~1s, season 58 took ~165s, and several individual late seasons already exceeded the `MAX_SECONDS_PER_SEASON`
gate on their own even though the run-wide *average* (74.26 s/season) still passed comfortably — the
average was masking the true worst case. That gap motivated a deeper look, which found three more
instances of the exact same "scan everything that ever existed" shape, none of which the two earlier fixes
touched:

| # | Hotspot | Called | Fix |
|---|---------|--------|-----|
| 3 | `PlayerConditionService.tickDaily()` → `clubHadMatchOnDate()` | Every simulated day, for every club in the save (not just league clubs) | Added `Club.lastMatchDate`, maintained incrementally in `FuttoboruGameEngine` wherever a match is finalized. `clubHadMatchOnDate` is now a single field comparison instead of a linear scan (with a `LocalDate` allocation per element) over the club's entire never-trimmed `playedMatches`. |
| 4 | `AttendanceCalculator.computeFormRate()` | Once per match simulated (~130+/season by late career) | The old code copied a club's *entire* `playedMatches` + `scheduledMatches` into a new list and **sorted it by date** just to read the last 5 non-friendly results — an O(n log n) cost, worse per-element than a plain scan, on top of the full-history copy. Since `playedMatches` is already chronologically ordered (matches are appended as they're finalized), the fix only looks at the tail (`RECENT_FORM_SCAN_WINDOW = 20` entries) — no copy or sort of old history. |
| 5 | `FuttoboruGameEngine.hasMandatoryUnreadDrawMessages()` and `getNextGameAction()` (two separate call sites, each with its own independent full scan of the same list) | `hasMandatoryUnreadDrawMessages()` up to 3x per simulated day (once per `continueGame()`, plus once per league match candidate considered that day); `getNextGameAction()` on essentially every UI/harness tick | Mandatory draw messages are rare (~1-2/season) but both methods linear-scanned the **entire** all-time inbox (`currentGame.getAllMessages()`, thousands of messages by late career) looking for them. Added an incremental candidate cache (`mandatoryDrawCandidates`, backed by `mandatoryDrawScanIndex`) shared by both call sites: only the slice of `allMessages` appended since the last check is scanned (usually zero new messages), and only the resulting small candidate list is checked for read/deleted status on each call — bounded by "mandatory draws ever," not "messages ever." |

All three are verified against the full 152-test L1+L2 suite (green, ~55-65s wall time each run). A clean,
uninterrupted 60-season confirmation run with all three fixes applied is still pending as of this writing —
every attempt was cut short by the orphaned-process issue in §2.1 below, not by a test failure. Partial data
before each interruption (seasons ~1-34 across attempts) showed a modestly flatter curve than the pre-fix
baseline, consistent with the fixes working as intended, but is not a substitute for a full clean run.

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

### 2.1 Confirmed root cause (evening, same day): orphaned processes, not a hostile external kill

Root-caused precisely this session: it is **not** an external actor killing the test. What happens is:

1. A long soak is launched as a backgrounded shell command. Maven forks a child JVM (`surefirebooter...jar`)
   to actually run the test — the visible "parent" process is only a thin wrapper.
2. At some point, the agent tooling's shell-session tracking loses the connection to that backgrounded
   shell (observed as the poll tool returning `exit_code: unknown` well before the expected runtime, with
   no corresponding process-list evidence of a clean exit).
3. Critically, **the JVM child process does not die when this happens** — it keeps running, keeps
   consuming CPU, and (depending on output buffering) may or may not keep writing to its log file. It
   becomes an orphan: still alive, no longer visible to the tool that launched it.
4. Because the run *looked* dead, the natural next step was to launch another attempt — compounding the
   problem. Repeated across several attempts, this left **multiple simultaneous orphaned JVMs** competing
   for CPU/RAM, which is the direct cause of the "CPU very high" symptom reported earlier, and likely
   contributed to later attempts dying even sooner (system-wide resource pressure from the accumulated
   orphans, not the new attempt itself misbehaving).
5. `Stop-Process -Id <pid>` targeting the reported wrapper PID is **not sufficient** to clean this up on
   Windows — it does not kill the process tree, so the surefire child (the actual expensive process)
   survives. `taskkill /PID <pid> /T /F` (tree-kill) is required.

**This means the earlier "5-6 external kills in a row" framing was imprecise.** The soak test itself was
very likely completing successfully in the background in at least some of those cases; the tooling simply
lost visibility into it while it kept running (and racking up CPU) unattended. No further product-code
fix is applicable here — see TOOL-1/TOOL-5 below and the policy resolution in §4.

### 2.2 Follow-up test (same evening): a detached `Start-Process` does survive, with one wrinkle

To rule out "the agent's shell polling itself is the problem," the soak was relaunched via
`Start-Process` (a separate `cmd.exe` tree, with output redirected to a log file, no pipe tie back to the
invoking shell). Result: **it worked** — the run kept progressing well past the point earlier
agent-backgrounded attempts had died (past season 30+, still `status=ok`, zero errors, confirmed by
re-reading the growing log file over several checks). One genuine wrinkle: `Get-Process`/`Get-CimInstance`
run from the *same* shell session could not see the resulting `java.exe`/`cmd.exe` processes at all (empty
result) even while the log file was actively growing seconds apart — i.e. the process is real and
running, just outside this particular shell session's process-visibility scope. The practical takeaway:
**verify a detached long-running process's liveness by its output file growing, not by `Get-Process` in
the launching session** — the latter can give a false "it's gone" reading here.

This also means the earlier working theory in §2 ("the shell/tooling loses track and orphans zombie
JVMs that then compete for CPU") is the more accurate root cause for the *originally reported* kills, not
a hard sandbox time limit — a properly detached process, checked correctly (by its file output, not
`Get-Process`), can and does survive well beyond the several-minute window earlier attempts were cut off
at.

---

## 3. Formal requirements for PM / devs

### Performance

| ID | Requirement | Status |
|----|-------------|--------|
| PERF-1 | Fix the `MessageManager` O(n) duplicate-message scan. | ✅ Done 2026-07-15 (§1, item 2). |
| PERF-1b | Fix the three more-of-the-same-pattern hotspots found in the third pass: `PlayerConditionService.tickDaily()`, `AttendanceCalculator.computeFormRate()`, `FuttoboruGameEngine` mandatory-draw-message scans. | ✅ Done 2026-07-15 evening (§1.1). Full clean 60-season re-confirmation still pending (blocked on TOOL-1, not on the fix itself). |
| PERF-2 | Add a coding-convention note (this doc + a short note in `AGENTS.md` or a code comment convention) warning against scanning an unbounded "every X that ever existed in the save" list inside a per-day/per-match hot path; prefer a season-scoped watermark or index, following the `playedMatchesSeasonStartIndex` pattern. | ✅ Done — added to `AGENTS.md` § Code conventions (this recurred 3 more times after the note was first drafted, which is itself evidence the note needed to exist and needs to stay visible). |
| PERF-3 | Add an automated performance-regression gate on a **short** (PR-gate-speed) soak test, not just the nightly 60-season one, so a future regression is caught in minutes, not during a multi-hour soak. `LongRunSoakIT` already gates on `MAX_SECONDS_PER_SEASON`; no equivalent gate exists yet on `MultiSeasonSoakIT` (the ≥3-season PR-gate test) or `MultiSeasonLeagueAndCupSoakIT` (15-season nightly test). | ⬜ Not yet built. |

### Tooling (running & monitoring long tests reliably)

| ID | Requirement | Status |
|----|-------------|--------|
| TOOL-1 | Make the 60-season soak runnable **detached** from any interactive/agent session (e.g. a CI job, a scheduled task, or a `Start-Process` with no parent tie), so it survives the session or IDE closing. | ✅ Workable today: `Start-Process` with redirected file output (§2.2) survives well past where agent-backgrounded shell attempts previously died. ⬜ Still worth formalizing as a small reusable script (e.g. `scripts/run-soak-detached.ps1`) rather than an ad-hoc one-off command, and checked via output-file growth, not `Get-Process`, in whatever session launched it. |
| TOOL-2 | Persist soak progress to a **durable status file** on disk (season, pct, ETA, last-error) that any process can poll, independent of console/stderr capture. `HarnessProgress` currently writes to stderr only — fine for an attached session, useless once detached (TOOL-1) or after a kill destroys the log buffer. | ⬜ Not built. `HarnessProgress` (added 2026-07-15) is stderr-only. |
| TOOL-3 | When asked to run or check on a long test, the agent/dev should state the **expected duration up front** and report progress **from the durable status file** (TOOL-2), not by guessing or polling blindly. | ✅ Adopted as working practice this session (progress % + ETA now reported); ⬜ still depends on TOOL-2 for full reliability once detached. |
| TOOL-4 | If a long job is killed by the environment more than **2-3 times in a row with no test-side error evidence**, stop retrying and report the pattern instead of repeating — matches the explicit PO feedback this session ("if external forces finish the test 5 or 6 times in a row you need to lighten up"). | ✅ Adopted as working practice this session (no further blind retries after the pattern was recognized); ⬜ not yet codified as an automated or documented rule anyone else would follow by default. |
| TOOL-5 | After stopping/losing track of a backgrounded long-test process, always verify with a process list that no orphaned JVM survives, and clean up with a **tree-kill** (`taskkill /PID <pid> /T /F` on Windows), not a plain `Stop-Process`/`kill` on the wrapper PID alone — see §2.1. | ✅ Root-caused and adopted as working practice 2026-07-15 evening; ⬜ not yet automated (e.g. a small wrapper script that always tree-kills and verifies before reporting a job as "stopped"). |
| TOOL-6 | **Human testers should never need to run or personally wait on a multi-season soak.** Make this an explicit, written policy (not just an informal understanding) so the frustration this caused does not recur. | ✅ Adopted 2026-07-15 evening — see policy note in §4 and `blockers.yaml` A-6. |

---

## 4. Recommendation, and the policy that actually resolves the tester frustration

None of PERF-3 or TOOL-1..3 block R-1888 sign-off — the existing soak evidence is valid and PERF-1/1b/2
are already fixed. These are backlog items for whoever owns test infrastructure next, prioritized roughly
in the order listed (PERF-3 and TOOL-2 are the two with the best remaining cost/benefit).

**But the fastest, most direct fix for "human testers are fed up with long-term soak tests" is not more
engineering on the soak itself — it is a policy correction that should have been explicit from the start:**

> **Human testers do not run, and do not wait on, the 60-season (or 15-season cup) soak.** These are
> engineering/CI-only nightly artifacts. R-1888 human QA sign-off requires only:
> 1. The fast default suite — `mvn test -pl futtoboru-core` (152 tests, ~55-65 s wall time, green as of
>    this writing).
> 2. Normal manual play of 1-3 seasons in the actual game UI (the real R-1888 commitment has always been
>    3-season stability, never 60).
>
> The 60-season soak exists so *engineering* can catch a scaling regression before it ever reaches a
> human, in the background, on nobody's critical path. If a human is watching a soak run, or is being
> asked "is it done yet," something has gone wrong with how this is being communicated/scheduled — it is
> not, and should never become, part of the human acceptance loop.

**Suggested owner/timing for the remaining backlog:** post-R-1888, alongside or before starting R-1892,
since R-1892 (second division + reserves) will make soaks longer, not shorter, and will make TOOL-1/2
more valuable, not less.

### 3.1 Live finding (2026-07-15 night): growth was not fully flat yet

A detached confirmation run (§2.2) reached season 34/60 with 0 errors, but per-season time was *still*
climbing past where it was after that day's three fixes: 69.7s (season 30) → 82.4s → 91.4s → 128.6s →
190.3s (season 34), i.e. still compounding, not flat. Recorded as PERF-4 rather than swept under the rug,
with two hypotheses: (a) a sixth O(history) scan not yet found, or (b) GC/heap pressure. Resolved below.

### 3.2 PERF-4 resolved (2026-07-19): JFR profiling found a sixth hotspot, the dominant one

Rather than keep guessing from code reading, ran the 60-season soak with Java Flight Recorder attached
(`-XX:StartFlightRecording=...settings=profile`, built into the JDK, no extra dependency) and analyzed
the resulting `.jfr` file with `jfr summary`/`jfr print --stack-depth 1`. Result was unambiguous:

```
391683 jdk.ExecutionSample events total
334727 (85.5%) -> java.time.LocalTime.compareTo(LocalTime)
```

The full stack trace for those samples:

```
java.time.LocalTime.compareTo(LocalTime)
java.time.LocalDateTime.compareTo0(LocalDateTime)
java.time.LocalDateTime.isAfter(ChronoLocalDateTime)
com.rndmodgames.futtoboru.data.jobs.JobOpening.isAcceptingApplications(LocalDateTime)
com.rndmodgames.futtoboru.engine.jobs.JobManager.lambda$getAvailableJobs$0(LocalDateTime, JobOpening)
... stream machinery ...
com.rndmodgames.futtoboru.engine.jobs.JobManager.getAvailableJobs()
```

**Root cause:** the exact same bug shape as PERF-1/1b, in a subsystem nobody had looked at yet.
`SaveGame.getActiveJobOpenings()` is actually *every job opening ever created* — when an opening expires
or is filled, `JobManager` only calls `opening.setStatus(...)`; nothing ever removes it from the list
(other screens, e.g. `MyApplicationsScreenTable`, need it for by-id history lookups). `getAvailableJobs()`
re-filtered that entire all-time list with a date comparison on every call, and `updateJobOpenings()`
(called once per simulated day) called it once per club, and separately copied and re-scanned the same
all-time list again for its own expiry check.

**Fix** (`JobManager.java`): added an incremental cache (`openJobOpeningsCache`) holding only openings
currently in `JobStatus.OPEN` — new openings are picked up by scanning only the tail appended since the
last check (openings are only ever appended), and openings that leave `OPEN` (the only two call sites:
expiry, fill) are dropped via a `removeIf` over the much smaller cache, not the all-time list. The
all-time list itself is untouched, so by-id history lookups still work. Added `JobManagerTest` (6 tests,
this class had zero prior coverage) covering: new openings appear immediately, expired/filled openings
disappear from `getAvailableJobs()` but stay in the all-time list, multi-call cache growth, per-club
filtering after mixed status changes, and cache rebuild after a simulated save load.

**Result, isolated 60-season re-run, same machine, right after the fix:**

| Metric | Before (2026-07-15/2026-07-19 morning) | After (2026-07-19) |
|---|---|---|
| Average | 74-93 s/season | **1.24 s/season** |
| Slowest season | 180-214 s | **1.93 s** |
| Total wall time | ~74-93 minutes | **~74 seconds** |
| Errors | 0 | 0 |

A ~60-75x speedup. This resolves PERF-4 conclusively as hypothesis (a) (a missed O(history) scan), not
GC/heap pressure — GC event counts in the JFR recording were unremarkable (only 5 old-gen collections in
the whole 80-minute pre-fix run, mostly cheap young-gen G1 pauses), consistent with an algorithmic cause.
`LongRunSoakIT`'s regression gates were tightened accordingly (`MAX_SECONDS_PER_SEASON` 150→10,
`MAX_SECONDS_FOR_SLOWEST_SEASON` 260→20) so a future reintroduction of this exact bug shape fails loudly
instead of hiding inside generous old headroom.

| ID | Requirement | Status |
|----|-------------|--------|
| PERF-4 | Investigate whether the still-climbing per-season cost past season ~30 (§3.1) is a further O(history) scan or GC/heap pressure. | ✅ Resolved 2026-07-19 — sixth hotspot found via JFR and fixed (§3.2). |

### 4.1 Getting a fully clean 60-season confirmation run

`Start-Process` with file-redirected output (§2.2) reliably survives long enough to finish a 60-season
soak; the failure mode in earlier attempts was orphaned/zombie processes and mis-checking liveness with
`Get-Process` rather than the output file, not a hard time limit. This is **not required** for R-1888
sign-off (see policy above — human testers never run this at all) but the evidence is being generated
this way going forward: launch detached, poll only the log file, never `Get-Process` in the same session.
A durable script wrapping this (TOOL-1) is recommended so it doesn't need to be re-derived each time.

**Largely moot after §3.2:** at ~74 seconds total, the 60-season soak is no longer a "long-running
background job" in any meaningful sense — it now runs faster than the nightly suite used to. TOOL-1/2's
main remaining value is for whatever comes after R-1892 (second division, reserves) makes a season more
expensive to simulate again.

---

*Cross-references: [ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md](ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md) (original root-cause writeup for hotspot #1) · [DESIGN_SOAK_PROGRESS_REPORTING_V1.md](DESIGN_SOAK_PROGRESS_REPORTING_V1.md) (progress-reporting design, TOOL-3 partial implementation) · [PM_REPORT_PROJECT_STATUS_2026_07_15.md](PM_REPORT_PROJECT_STATUS_2026_07_15.md) §10-11.*
