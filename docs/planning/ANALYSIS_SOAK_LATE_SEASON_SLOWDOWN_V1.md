# Analysis — Why late soak seasons get slow (S50–S60)

**Document type:** Debug analysis / follow-up backlog  
**Date:** 2026-07-12  
**Status:** Root causes identified; primary fix shipped with this note  
**Related:** [DESIGN_SOAK_PROGRESS_REPORTING_V1.md](DESIGN_SOAK_PROGRESS_REPORTING_V1.md) · [SOAK_PREFLIGHT_PROTOCOL.md](SOAK_PREFLIGHT_PROTOCOL.md)

---

## 1. Do we have useful logs?

**Partial only.** The successful 60-season run (`soak-output.log`, 2026-07-12) has:

```
Soak progress: season N/60 @ <game-date>
…
LongRunSoakIT timing: 5233741 ms, 87.23 s/season
```

What it **does not** have:

- Wall-clock delta per season (`s_last`)
- Match archive / inbox sizes
- Hotspot attribution

So we could see that the run finished (~87 min avg) and that humans perceived S50–S60 as “too long,” but we could **not** reconstruct a per-season curve from that log alone. Progress lines now include timing + size metrics so the next soak is diagnostic.

---

## 2. Verdict

Late seasons are slow because **daily engine work rescans unbounded history**. Season rollover **resets counters** (`matchesPlayed`, points) but **never clears** `club.playedMatches` or the inbox. Cost grows ~**linearly with season index** (often worse on match days).

At 12 Football League clubs × 22 league matches/club/season:

| Season | `playedMatches` per club (league alone) | Daily `isLeagueComplete` inspections (order) |
|--------|------------------------------------------|-----------------------------------------------|
| 1 | ~22 | ~260 |
| 30 | ~660 | ~8,000 |
| 60 | ~1,320 | ~16,000 |

× ~365 days/season → season 60 pays on the order of **~60×** the completion-check work of season 1 for that path alone.

---

### R0 — Smoking gun for “fix breaks first season”

`checkNewSeasonGeneration()` called `generateNextSeason` whenever the calendar year fragment (e.g. `1889-90`) was missing from edition names — **even while 1888-89 was still in progress**. That **archived and cleared fixtures daily from January**, then regenerated.

Without a season watermark, `playedMatches` still grew until `isLeagueComplete` eventually returned true (slow + sloppy).  
With a watermark, each mid-season regen advanced `playedMatchesSeasonStartIndex` to “now,” so the current-season window stayed empty → **0 champions / deadlock**.

**Fix:** `needsNextSeasonGeneration` returns false while the active edition has `championsId == null`.

---

## 3. Root causes (ranked)

### R1 — Primary: `isLeagueComplete` scans all `playedMatches` every day

**Call chain:** `continueGame` → `AuthorityManager.checkCompetitionsSchedule` → `checkLeagueCompletion` → `LeagueStandingsManager.isLeagueComplete` → `countCompletedLeagueMatches`.

That helper walked **scheduled + played** lists and counted every `LEAGUE_MATCH` ever played — **no edition filter**. After season 1, history already exceeds “22 matches,” so the method still walks the full archive every day (then exits early via `isLeagueAlreadyCompleted`).

Matches are **moved** from `scheduledMatches` → `playedMatches` as soon as they are simulated (`FuttoboruGameEngine.finalizePlayedMatch`). So completion must read the archive — but only the **current season slice**.

**Fix:** `Club.playedMatchesSeasonStartIndex` watermark, advanced in `archiveAndClearLeagueFixtures`. `isLeagueComplete` counts only from that index (+ any rare still-scheduled played fixtures).

### R2 — Archive never pruned on rollover

`SeasonManager.archiveAndClearLeagueFixtures` moves finished fixtures **into** `playedMatches`. `resetLeagueClubStats` zeroes counters only. Live club lists keep growing forever.

### R3 — `ArrayList.contains` when archiving a played match

`FuttoboruGameEngine` uses `playedMatches.contains(match)` before add → **O(n)** per finalize; worse each season.

### R4 — Unbounded inbox

`MessageManager.deliverMessage` scans `allMessages` for duplicates; `hasMandatoryUnreadDrawMessages` walks the whole inbox (multiple times per day). Inbox grows every season.

### R5 — Cup edition scans (when cups enabled)

`AuthorityManager.getMatchesForEdition` scans all clubs’ scheduled+played history. League-only soak avoids most of this; cup soaks are worse.

### R6 — Wrong seasonal awards if re-scanned

`calculateTopScorer` iterates **all** historical league matches on a club (not current edition). Not the main soak CPU hog (runs at completion), but incorrect for multi-season awards.

### Diagnostic evidence (2026-07-12)

After ~400 days with FA Cup still attached:

```
DATE=1889-05-06 cups=1
Preston … full=22 cur=0 startIdx=35 playedSz=36 schedSz=53
… all 12 clubs full=22 cur=0 …
isComplete=false
```

Interpretation: first season’s 22 league matches exist in history (`full`), but rollover advanced `startIdx` so the current window is empty (`cur=0`). Old completion treated `full>=22` as forever-complete (cheap false positive after S1; expensive scan). New completion is correct and O(current season).

---

| Change | Why |
|--------|-----|
| `playedMatchesSeasonStartIndex` + current-season-only completion count | O(current season) per day instead of O(history) |
| `needsNextSeasonGeneration` blocked while active edition has no champion | Stops mid-season fixture wipe from January year-fragment bug |
| Progress lines include `s_last`, `played_avg`, `inbox` | Next soak log is diagnostic |
| Harness `isPrimaryLeagueComplete` treats recorded champion as complete | Waits survive post-rollover empty current-season window |
| Soak progress prints `s_last`, avg played-list size, inbox size | Next log answers “is S50 slower?” without guessing |
| This analysis doc | So the finding is not lost |

**Still open (backlog — see design doc P1+):**

- Prune/archive `playedMatches` off the live club list after rollover  
- `Set<Long>` match-id membership instead of `List.contains`  
- Inbox soft-cap / O(1) mandatory-draw flag  
- Set `competitionEditionId` on league fixtures; edition-scope scorers  
- Sampled health scans only (already partly done for 60-season soak)

---

## 5. Expected effect

Removing the daily full-history completion scan should flatten most of the **per-day** linear growth. Remaining growth (R3–R5) still matters for **1000** seasons and for cup mode — address via archive prune + indexes before `soak-ultra`.

Re-run after fix:

```powershell
mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT -Dfuttoboru.test.logProfile=quiet
```

Compare `s_last` for seasons 10 vs 50 vs 60 in the progress lines.

---

## 6. Follow-ups (do not lose)

1. **Measure** — keep `s_last` + size metrics on every long soak; attach log snippet to A-6 sign-off.  
2. **Prune archives** — highest leverage for 1000-season.  
3. **Edition id on league fixtures** — enables correct edition-scoped stats/UI.  
4. **Link** from DESIGN_SOAK_PROGRESS_REPORTING §2 (measured baseline) when P0 lands.

---

*Logged for QA/platform. Primary completion-scan fix accompanies this analysis.*
