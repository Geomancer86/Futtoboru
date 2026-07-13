# Soak Preflight Protocol

**Document type:** QA runbook / post-mortem  
**Date:** 2026-06-30  
**Trigger:** 60-season soak smoke aborted after **~41 minutes** with no pass/fail result and no useful log artifact.

---

## 1. What happened (2026-06-30)

| Item | Detail |
|------|--------|
| **Command** | Chained `mvn test` (full PR suite) **then** `mvn test -Psoak -Dtest=LongRunSoakIT`, stdout piped through `Select-String` |
| **PR suite** | Completed — **100/100 green** (~60s) |
| **Soak** | Started, then **user-aborted at ~41 min** — no `LongRunSoakIT timing:` line, no Surefire result |
| **Log artifact** | **Not usable.** Terminal capture only kept filtered PR lines + a few verbose `PlayerProfessionAssigner` lines. Soak profile sets `redirectTestOutputToFile=false`, so full output was discarded by the pipe filter. |
| **Surefire report** | No `LongRunSoakIT` report (test never finished) |

**Why ~41 minutes instead of ~5?**

1. **`-Psoak` forced `futtoboru.test.logProfile=verbose`** — enables every `DebugLogManager` category for ~42,000 day-advances (60 × 700 days). Documented ~5 min runs use **quiet** logs + suppressed stdout.
2. **No preflight gate** — soak was started without confirming the composing E2E tests in the same JVM session policy; PR suite passed but that does not guarantee soak config is fast or that cup paths were validated first.
3. **Piping stdout through `Select-String`** — hides progress, buffers output, and destroys the log you need when debugging a long run.

**Lesson:** Never start `-Psoak` until preflight passes. Never pipe soak output through filters. Use **quiet** for timing runs; **verbose** only when debugging a known failure.

---

## 2. Mandatory preflight (before any long soak)

Run **all** of the following. Total **~30–90 seconds**. If any step fails, **do not** start `LongRunSoakIT`.

### Windows

```powershell
.\scripts\preflight-before-soak.ps1
```

### Unix

```bash
bash scripts/preflight-before-soak.sh
```

### Manual equivalent

```bash
# 1) Core E2E (~15s)
mvn test -pl futtoboru-core -Dtest=LeagueAndCupIT,MultiSeasonSoakIT -Dfuttoboru.test.logProfile=quiet

# 2) Cup multi-season (~5s)
mvn test -pl futtoboru-core -Pnightly -Dtest=MultiSeasonLeagueAndCupSoakIT -Dfuttoboru.test.logProfile=quiet
```

### What each step proves

| Step | Test | Proves |
|------|------|--------|
| 1a | `LeagueAndCupIT` | One full 1888–89 league + FA Cup, health + CRITICAL gates |
| 1b | `MultiSeasonSoakIT` | 3 procedural league seasons, economy + health |
| 2 | `MultiSeasonLeagueAndCupSoakIT` | 2 seasons league **and** cup, bracket health |

Only after **all green** → run north-star soak.

---

## 3. Running the 60-season soak (after preflight)

### Recommended (preflight + soak)

```powershell
.\scripts\run-soak-with-preflight.ps1
```

### Manual (quiet — realistic ~5 min)

```bash
mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT -Dfuttoboru.test.logProfile=quiet
```

### Debug only (slow — expect 30–60+ min)

```bash
mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT -Dfuttoboru.test.logProfile=verbose
```

**Do not** filter stdout/stderr. Watch for:

```
PROGRESS|soak-league|done=12/60|pct=20.0|elapsed=17m|eta=1h8m|last=87.2s|...
LongRunSoakIT timing: ... ms, ... s/season
```

---

## 4. Log locations

| Run type | Where output goes |
|----------|-------------------|
| Default PR (`redirectTestOutputToFile=true`) | `futtoboru-core/target/surefire-reports/*.txt` |
| `-Psoak` / `-Pnightly` | Console (redirect off) — **do not pipe** |
| CRITICAL failures | Fails test via `HarnessCriticalLogCounter` |

If a soak is interrupted, check:

- Console scrollback (if unfiltered)
- `futtoboru-core/target/surefire-reports/` (only if the JVM exited cleanly)
- Re-run preflight first — faster than guessing from a partial soak.

---

## 5. Policy (agents & humans)

1. **Preflight first** — always `preflight-before-soak` before `-Psoak`.
2. **Quiet for timing** — default soak timing runs use `logProfile=quiet`.
3. **Verbose for diagnosis** — only after a quiet run fails or when investigating a specific defect.
4. **No stdout pipes** on soak/nightly commands.
5. **Do not chain** full PR suite + soak in one piped command — run preflight script instead (targeted tests, clear exit codes).

---

## 6. Related docs

- [SIMULATION_TEST_SUITE_V2.md](SIMULATION_TEST_SUITE_V2.md)
- [PM_V1_STATUS_AND_NEXT_STEPS.md](PM_V1_STATUS_AND_NEXT_STEPS.md)
