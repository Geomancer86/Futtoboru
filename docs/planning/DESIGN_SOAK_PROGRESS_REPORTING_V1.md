# Design — Long-run soak progress meter & live reporting

**Document type:** Analysis + design (no implementation in this follow-up)  
**Date:** 2026-07-12  
**Status:** Proposal — implement in a later sprint  
**Audience:** QA, platform, agents  
**Related:** [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md) · [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) · `GameTestHarness.simulateWithReport` · `MultiSeasonSimulationReport`

---

## 1. Verdict

Long-run soaks (10 / 60 / **1000** seasons) must **never look idle** and must **abort early** when logs or health say the run is already worthless. Today we have partial progress lines and end-of-run reports; we lack a **tiered progress meter**, **live ETA**, **checkpoint artifacts**, and a **single command surface** for any duration. Design that layer once; duration becomes a parameter, not a new test class each time.

---

## 2. Analysis — what exists today

### 2.1 Inventory

| Asset | Role | Gap |
|-------|------|-----|
| `MultiSeasonSoakIT` | PR gate — 3 seasons | No progress meter (too short to need one) |
| `TenSeasonReportIT` | Nightly — 10 seasons + markdown to `target/` | Report only **at end**; progress is quiet |
| `LongRunSoakIT` | Soak profile — 60 seasons | Season progress lines only; ~87 min; no ETA / checkpoint file |
| `LongRunEconomySoakIT` / cup soaks | Nightly fragments | Same harness, no shared progress API |
| `MultiSeasonSimulationReport` | Champions, balances, health, `toMarkdown()` | Built in memory; written once at finish |
| `HarnessCriticalLogCounter` | Fail-fast on ERROR/CRITICAL | Good; not yet wired to a “progress dashboard” |
| Preflight scripts | Gate before 60-season | Not parameterized for 10 / 1000 |

### 2.2 Measured baseline (2026-07-12)

| Mode | Result |
|------|--------|
| 60 seasons, quiet, **no** per-season health scan | **~87 s/season**, **~87 min** total, green |
| Same with per-season full health scan | Slowed badly after ~season 50 (scan cost grows with match history) |
| Before fail-fast | Exceptions swallowed → could burn hours then fail at max days |
| Locale init | Fixed — no more `missing bundle key` spam on soak start |

**Projection (league-only, quiet, no mid-run health scan):**

| Seasons | Rough wall time |
|---------|-----------------|
| 10 | ~15 min |
| 60 | ~1.5 h (pre-fix avg; late seasons slower — see [ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md](ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md)) |
| 1000 | **~24 h** (until archive prune) |

Any 1000-season run without live progress + ETA + early abort is an invitation to waste a full day of CPU.

### 2.3 Why processing gets wasted

1. **Silent long loops** — quiet mode + suppressed stdout hid all feedback until recently; humans abort “hung” runs that were actually working.
2. **Late failure** — health/economy issues discovered only at the end assertion.
3. **Quadratic-ish diagnostics** — full-save health scans every season scale with accumulated matches.
4. **No checkpoint** — abort at season 59 of 60 loses all diagnostic value; restart from zero.
5. **One-off IT classes** — N seasons = copy-paste test; hard to run “just 100 seasons” without editing code.

---

## 3. Goals (design)

| # | Goal |
|---|------|
| G1 | **Any duration** — same harness API for 10, 60, 1000 (or `-Dsoak.seasons=N`) |
| G2 | **Live progress meter** — always visible even in quiet mode (dedicated channel, never filtered) |
| G3 | **Live ETA** — rolling average s/season → projected remaining time |
| G4 | **Fail fast** — first ERROR/CRITICAL / stall / hard health gate → abort + write partial report |
| G5 | **Partial report on abort** — markdown (+ optional JSON) so the run is not a total loss |
| G6 | **Cheap mid-run checks** — economy min-balance every season; full health scan **sampled**, not every season |
| G7 | **Preflight scales** — short soaks skip full 60-season preflight; ultra-long require nightly + 10-season green first |

### Non-goals (this design)

- Real VisUI / L2.5 click automation during soak  
- Parallel multi-JVM season sharding (nice later; not v1)  
- Replacing human L4  
- Changing production game loop for soak convenience  

---

## 4. Proposed architecture

```
                    ┌──────────────────────────────┐
                    │  SoakRunner (test harness)   │
                    │  seasons, leagueOnly, flags  │
                    └──────────────┬───────────────┘
                                   │
           ┌───────────────────────┼───────────────────────┐
           ▼                       ▼                       ▼
 ┌─────────────────┐   ┌────────────────────┐   ┌──────────────────┐
 │ ProgressMeter   │   │ SoakCheckpoint     │   │ MultiSeason…     │
 │ console + file  │   │ every K seasons    │   │ Report (partial) │
 └─────────────────┘   └────────────────────┘   └──────────────────┘
           ▲                       ▲
           │                       │
           └──────── FailFast ─────┘
                 (HarnessCriticalLogCounter + stall + budgets)
```

Keep simulation in `GameTestHarness`; add a thin **`SoakRunner`** (or extend `simulateWithReport`) that owns progress, sampling, and artifacts — not new engine code.

---

## 5. Progress meter design

### 5.1 Channel (critical)

Progress must **bypass** quiet `System.out` suppression used for timing.

| Channel | Use |
|---------|-----|
| `System.err` **or** dedicated `PrintStream` passed into runner | Always-on progress (recommended: inject `progressOut`, default `System.err`) |
| File `target/soak-progress.log` | Append-only; survives IDE buffer loss |
| Optional heartbeat file `target/soak-heartbeat.json` | Overwritten each season for external watchers |

**Never** pipe soak through `Select-String` / `grep` that drops progress lines (already in preflight protocol).

### 5.2 Line format (human + machine)

One line per completed season (and a start banner):

```
SOAK|phase=run|season=12/60|date=1900-01-02|champ=Preston|s_last=81.2|s_avg=87.4|eta_min=70|bal=£412.50|status=ok
```

| Field | Meaning |
|-------|---------|
| `season` | Completed champions / target |
| `date` | In-game date |
| `champ` | Latest champion short name |
| `s_last` / `s_avg` | Last and rolling-average seconds per season |
| `eta_min` | `(target - done) * s_avg / 60` |
| `bal` | Player club balance (spot check) |
| `status` | `ok` / `warn` / `abort` |

Start banner once:

```
SOAK|phase=start|target=60|mode=league|log=quiet|health=sample:10|started=2026-07-12T22:00:00
```

End / abort:

```
SOAK|phase=end|result=PASS|seasons=60/60|elapsed_min=87.2|report=target/soak-report-60.md
SOAK|phase=end|result=FAIL|seasons=23/60|reason=CRITICAL ERROR: …|report=target/soak-report-partial-23.md
```

### 5.3 “Meter” without a TTY bar

Consoles in CI/Surefire are line-oriented. Prefer **structured lines** over `\r` progress bars (bars break in redirected logs). Optional ANSI bar **only** when `System.console() != null` and `-Dsoak.progress=bar`.

### 5.4 Stall detection (already partially present)

Keep **800 days without a new champion** as hard abort. Add:

| Signal | Action |
|--------|--------|
| `s_last > 3 × s_avg` after season ≥ 5 | `warn` line; after 3 consecutive → abort “season time regression” |
| Heap / optional JMX later | Out of scope v1 |
| Fail-fast log hit | Immediate abort + partial report |

---

## 6. Live & final reporting

### 6.1 Partial report every K seasons

| Duration | K (checkpoint) | Full health sample every |
|----------|----------------|--------------------------|
| 10 | 5 | every season (cheap enough) |
| 60 | 10 | every **10** seasons |
| 1000 | 25 | every **50** seasons |

Each checkpoint writes:

- `target/soak-reports/{runId}/checkpoint-{n}.md` (append summary table row)  
- Overwrite `latest.md` for “open this while it runs”  
- Optional `latest.json` for tooling  

`MultiSeasonSimulationReport.toMarkdown()` already fits; call **`writePartial()`** on abort/checkpoint, not only at end.

### 6.2 Report contents (keep current + add)

**Keep:** champions table, balances, inbox size, ms/day, projected 60.  

**Add:**

- Progress history (season → wall seconds)  
- Sampled health issues list  
- Abort reason if any  
- Config block (seasons, leagueOnly, health sample rate, Java version)  
- ETA accuracy note (projected vs actual at end)

### 6.3 Economy vs health cost

| Check | Cost | Frequency |
|-------|------|-----------|
| Fail-fast log assert | O(1) | **Every day** (already) |
| Min league balance | O(clubs) | **Every season** |
| Full `SimulationHealthMonitor.scan` | O(all matches) | **Sampled** only |
| Inbox size | O(1) | Every season |

This is the main lever that makes 1000 seasons feasible without looking hung.

---

## 7. Parameterized long-run surface

### 7.1 Single entry (recommended)

```text
mvn test -pl futtoboru-core -Psoak -Dtest=ParameterizedSoakIT \
  -Dsoak.seasons=60 \
  -Dsoak.leagueOnly=true \
  -Dsoak.healthSample=10 \
  -Dsoak.checkpoint=10 \
  -Dfuttoboru.test.logProfile=quiet
```

| Property | Default | Notes |
|----------|---------|-------|
| `soak.seasons` | 60 | 10 / 60 / 1000 |
| `soak.leagueOnly` | true | false = league + cup (much slower) |
| `soak.healthSample` | auto from seasons | 1 / 10 / 50 |
| `soak.checkpoint` | auto | write partial report |
| `soak.failFast` | true | |
| `soak.progress` | lines | `lines` \| `bar` \| `off` |

### 7.2 Keep thin named ITs as aliases

| Class | Tag | Fixed seasons | Purpose |
|-------|-----|---------------|---------|
| `MultiSeasonSoakIT` | (default) | 3 | PR gate |
| `TenSeasonReportIT` | nightly | 10 | Nightly report |
| `LongRunSoakIT` | soak | 60 | North-star gate |
| `UltraLongSoakIT` (new) | soak-ultra | 1000 | Manual / scheduled weekend only |

Aliases call the same `SoakRunner`; no duplicated loop logic.

### 7.3 Maven profiles

| Profile | When | Seasons |
|---------|------|---------|
| (default) | every commit | exclude soak |
| `nightly` | CI night | 10 (+ existing cup/economy) |
| `soak` | PM / release | 60 |
| `soak-ultra` | explicit only | 1000 — **never** default CI |

---

## 8. Preflight matrix (avoid starting doomed long runs)

| Next run | Required green first |
|----------|----------------------|
| 10 seasons | L1+L2 suite (or existing short preflight) |
| 60 seasons | Current `preflight-before-soak` + 10-season IT |
| 1000 seasons | Preflight + **60-season green within last N days** (artifact or CI badge) + free disk for reports |

If 60 is red, **do not** start 1000. Script should refuse with a clear message.

---

## 9. Implementation phases (later — not this follow-up)

| Phase | Work | Exit criteria |
|-------|------|---------------|
| **P0** | Extract `SoakProgress` + inject `progressOut`; ETA fields; partial `writeTo` on abort | 60-season shows ETA; abort writes partial md |
| **P1** | Sampled health; checkpoint every K; heartbeat file | 60-season ≤ ~90 min on reference machine; mid-run `latest.md` usable |
| **P2** | `-Dsoak.seasons=` ParameterizedSoakIT; profiles `soak` / `soak-ultra` | 10/60/1000 via property only |
| **P3** | Ultra-long ops: refuse without 60 green; optional resume-from-checkpoint (stretch) | 1000 documented weekend runbook |

**Do not** implement resume-from-save in P0–P2 unless 1000 is scheduled — high complexity.

---

## 10. Acceptance criteria (when implemented)

1. Quiet 60-season run prints a **progress line every season** with **ETA**.  
2. Injected ERROR during season 2 aborts **before** season 3 and writes **partial report**.  
3. Full health scan does **not** run every season when `soak.seasons ≥ 60`.  
4. `soak.seasons=10|60|1000` works without editing Java constants.  
5. Preflight for 1000 refuses if 60 soak artifact / last known green missing.  
6. Docs: update SOAK_PREFLIGHT_PROTOCOL + pyramid L3 row; no PO essay required beyond one PO_BRIEF line when 1000 is first scheduled.

---

## 11. Risks & decisions for PO/PM

| Decision | Recommendation |
|----------|----------------|
| Is **1000 seasons** an R-1888 gate? | **No** — post-1888 / platform hardening. R-1888 stays **60 + PM sign-off**. |
| Where do reports live? | `futtoboru-core/target/soak-reports/` (gitignored via `target/`) |
| Progress on `stderr` vs file? | **Both** — stderr for live watch; file for post-mortem |
| Cup in long soaks? | Default **league-only**; cup at 2–3 seasons nightly only |

---

## 12. Summary for implementers

Build a **parameterized SoakRunner** with always-on progress + ETA, **sampled** expensive checks, **checkpoint/partial reports**, and fail-fast already in place. Duration (10 / 60 / 1000) is configuration. Until that lands, the interim practice remains: quiet logs, season progress lines, fail-fast, skip per-season full health on 60+, and never pipe soak output.

---

*Design only — no code in this follow-up. Next step when scheduled: P0 in harness + update SOAK_PREFLIGHT_PROTOCOL.*
