# PM Report — Full Project Status & Forward Plan

**Document type:** PM status (all stakeholders)
**Date:** 2026-07-15
**Supersedes:** [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md) (kept for history)
**Branch:** `analyze-ai-branch`
**Release:** **Futtoboru 1888** (`R-1888` / tag `1888.0.0`)

**PO one-pager:** [PO_BRIEF.md](PO_BRIEF.md)
**Eagle view:** [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md)
**Registries:** [docs/registries/](../registries/)

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **Where are we?** | A playable **1888–89 Football League manager** with CM1 loop, league + FA Cup, procedural seasons, Victorian (ME-1) injury care, and a full CM93-grade injury **engine** underneath. |
| **How ready is R-1888?** | **~99% pre-human.** All 8 Tier A engineering blockers are technically closed. Only a sign-off decision remains. |
| **What changed since 2026-07-12?** | Closed the last blocker (A-6): fixed a real late-soak slowdown, re-ran the full 60-season soak clean, and made every long-run test always report % done + ETA so nobody waits blind again. |
| **What is the critical path now?** | **Decision, not code:** PM/PO sign off the soak evidence → invite human testers (B-9) → tag release. |
| **What must we not do?** | Do not add transfers, CM93 insurance menus, or new scope. Do not skip the human walkthrough just because automation is green. |

**Bottom line for the table:** Engineering has run out of blockers to close. The only thing standing between today and inviting human testers is a sign-off conversation.

---

## 2. What changed this sprint (2026-07-12 → 2026-07-15)

| # | Item | Why it mattered | Outcome |
|---|------|------------------|---------|
| 1 | **Soak fail-fast** | Long tests were silently swallowing errors and running to the end before failing — wasting hours per investigation | `HarnessCriticalLogCounter` now intercepts every `ERROR`/exception log and fails the test **immediately**, not after 60 simulated seasons |
| 2 | **Missing language bundle spam** | Logs were full of `missing bundle key` noise, obscuring real problems | Fallback display names + locale-tag normalization (`en_US` vs `en-US` vs `en_us`); noise gone |
| 3 | **Late-season slowdown (seasons 50–60)** | Root-caused: several places rescanned a club's *entire* match history every simulated day instead of just the current season | Added a season watermark (`playedMatchesSeasonStartIndex`); `isLeagueComplete` and related scans now only look at the current season |
| 4 | **Always-on progress reporting** | Any long test (soak, multi-season) gave zero feedback until it finished or hung | New `HarnessProgress` utility: every long-run test now prints `season X/Y`, elapsed, and ETA to the console as it runs |
| 5 | **Full soak re-verified** | Needed proof the fixes actually worked, not just theory | 60/60 seasons, **0 errors**, **69.96 s/season** (down from 87.23 s/season pre-fix — **~20% faster**), full run **~70 minutes** |

**Evidence (today, 2026-07-15):** `mvn test -pl futtoboru-core` → **141 tests, 0 failures, 0 errors**, verified fresh this session. Soak evidence is the 2026-07-12 22:51 run, on the exact commit (`fd8e742`) that is still `HEAD` today — no code has changed since, so the evidence is current.

---

## 3. Where we are (by horizon)

```
NOW     R-1888 — Victorian FL manager (CM1 loop + ME-1 injuries)     ← WE ARE HERE (~99%)
NEXT    Human QA (B-9) → R-1888.1 polish
MID     R-1892 — Div 2, reserves, basic transfers
LATER   R-1906 insurance · R-1919 discipline · R-1951 Lilleshall · R-1965 subs · R-1993 CM93 menus
FAR     Full CM + FM historical completeness
```

### 3.1 Four-stage gate (R-1888 only)

| Stage | Readiness | Blocks human QA? | Notes |
|-------|-----------|------------------|-------|
| **Analysis** | 100% | No | Locked 2026-07-03 |
| **Design** | 100% | No | ME-1 + soak-progress designs shipped |
| **Development** | 100% | No | All 8 Tier A blockers closed |
| **Automated tests** | 100% | No | 141+5 green; soak green and faster |
| **Sign-off decision** | 0% | **Yes** | Needs a PM/PO yes — not engineering work |
| **Human QA (L4)** | 0% | — | Correctly blocked until sign-off |

**Overall pre-human: ~99%.** The last 1% is a conversation, not a sprint.

### 3.2 Tier A blockers — final status

| ID | Item | Status |
|----|------|--------|
| A-1 | Victorian injury UX (ME-1) | ✅ Resolved 2026-07-12 |
| A-2 | ME-1 automated tests | ✅ Resolved 2026-07-12 |
| A-3 | Continue double-click | ✅ Resolved 2026-07-12 |
| A-4 | Match history click | ✅ Resolved 2026-07-12 |
| A-5 | Save/load UI | ✅ Resolved 2026-07-12 |
| A-6 | 60-season soak sign-off | 🟡 **Automated evidence complete 2026-07-15; awaiting PM/PO sign-off** |
| A-7 | Manual ch.6–7 | ✅ Resolved 2026-07-12 |
| A-8 | Production mod load check | ✅ Resolved 2026-07-12 |

**7/8 formally resolved. A-6 has clean, reproducible automated evidence — the remaining step is a human decision, recorded in `blockers.yaml`, not more engineering.**

---

## 4. Traffic lights (all departments)

| Area | Light | Comment |
|------|-------|---------|
| CM1 management loop (automated) | 🟢 | Proven in harness |
| CM1 loop (human feel) | ⚪ | Waiting on B-9 |
| Victorian injury (ME-1) | 🟢 | Shipped + tested |
| Transfers / market | 🟢 Out of scope | Correctly deferred to R-1892+ |
| League + cup + rollover | 🟢 | Shipped |
| UI regressions (Continue, history, save) | 🟢 | Closed |
| L1+L2 tests (141) | 🟢 | Green — verified today |
| L3 nightly (5) | 🟢 | Green |
| L3 soak (60 seasons) | 🟢 | Green, faster, fail-fast, progress-reported — **awaiting sign-off, not fixes** |
| Human QA | ⚪ | Blocked by policy until sign-off |
| Public release | ⚪ | No — local only (PO lock) |
| Documentation / governance | 🟢 | PO_BRIEF + registries current |
| PO decisions signed | 🟡 | 3 decisions in PO_BRIEF still await initials |
| Branch hygiene | 🟡 | `analyze-ai-branch` is truth; `main` is stale |
| Test observability | 🟢 **New** | Long tests now self-report % done + ETA on stderr |

---

## 5. How to finish the current plan — recommended path

This is the direct answer to "what's the best way to finish": there is no more Tier A engineering work. The plan finishes in four short steps:

| # | Action | Owner | Exit criterion |
|---|--------|-------|-----------------|
| 1 | **Sign off A-6** using the 2026-07-15 soak evidence in §2–3 (or re-run soak once more if you want a same-day confirmation run — optional, not required) | PM/PO | `blockers.yaml` A-6 moved to resolved |
| 2 | **Sign the 3 open PO decisions** on [PO_BRIEF.md](PO_BRIEF.md) (scope lock, testers after blockers, 1892 next) | PO | `decisions.yaml` updated |
| 3 | **Invite human testers (B-9)** with the existing checklist; log every issue as R-1888.1 (polish) vs R-1892 (new scope) — do not fix mid-walkthrough by reopening blockers | QA/PM | Walkthrough complete, backlog sorted |
| 4 | **Tag `1888.0.0`** once PO accepts the walkthrough result | PM | Release milestone; branch merge decision made separately (ask-first, per `AGENTS.md`) |

**Nothing else needs to be built before human eyes see the game.** Anything raised during the human walkthrough becomes new, explicitly-scoped work (R-1888.1 or R-1892) — it should not reopen A-1…A-8.

---

## 6. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Sign-off stalls on "just one more soak run" indefinitely | Delays human QA with no new information | Evidence is already reproducible (2 clean runs, before/after fix); treat further re-runs as optional confidence, not a requirement |
| Human QA surfaces real bugs | Expected, not a failure | Triage into R-1888.1 immediately; do not scope-creep into R-1892 |
| Scope creep before B-9 ("just one more feature") | Delays testing further with no PO ask | PO_BRIEF + ask-first policy in `AGENTS.md` |
| Branch never merges to `main`/`develop` | Docs/code truth diverges from tracked history | Explicit ask-first gate already documented; decide after B-9 |

---

## 7. Messaging (say / do not say)

| Say | Do not say |
|-----|------------|
| All engineering blockers for Futtoboru 1888 are closed; one sign-off remains | Fully tested by humans / ready for players |
| Soak is green and faster than before, with fail-fast and live progress reporting | The 60-season soak "might still have issues" |
| Next concrete step is inviting human testers | More development is needed before testing |

---

## 8. Document & registry pointers

| Audience | Doc |
|----------|-----|
| PO | [PO_BRIEF.md](PO_BRIEF.md) |
| PM / Sponsor | This report · [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) |
| Agents | [AGENTS.md](../../AGENTS.md) |
| Features / blockers / roadmap | [docs/registries/](../registries/) |
| Late-soak slowdown root cause | [ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md](ANALYSIS_SOAK_LATE_SEASON_SLOWDOWN_V1.md) |
| Progress-reporting design | [DESIGN_SOAK_PROGRESS_REPORTING_V1.md](DESIGN_SOAK_PROGRESS_REPORTING_V1.md) |
| Previous full report | [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md) |

---

## 9. Changelog

| Date | Change |
|------|--------|
| 2026-07-15 | All 8 Tier A blockers technically closed; soak re-verified faster and clean; recommended 4-step path to finish R-1888 |
| 2026-07-12 | Full-project PM report — all stakeholders, critical path, four phases |

---

*Refresh when the sign-off decision is made or human QA (B-9) completes. Do not create a parallel essay for routine sprint updates — update PO_BRIEF + blockers.yaml instead.*
