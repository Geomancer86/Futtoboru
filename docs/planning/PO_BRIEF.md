# PO Brief — Futtoboru

**One page. Read this.** Everything else is detail for PM, dev, or AI.

**Updated:** 2026-07-19 · **Release:** Futtoboru 1888 · **Branch:** `analyze-ai-branch`

> **PO redirect report (3 points):** [PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md](PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md) — done / missing / next for first release.

---

## Status

| | |
|-|-|
| 🟢 | Game loop works: league, cup, pick team, play matches, ratings, saves |
| 🟢 | Automated tests: **158 + 5** green on every CI run |
| 🟢 | Tier A engineering blockers **8/8 closed** — ME-1, C-1, PL-5, PL-7, manual, mod load, soak |
| 🟡 | **1 decision** left before human playtesters: your sign-off on the soak evidence below |
| 🔴 | Human QA not started — **correct**; we finish sign-off first |
| ⚪ | No public release — local dev only |

---

## What Futtoboru 1888 is

A **Victorian Football League manager** starting **1888–89**. You pick your team, set tactics, play matches, manage fitness and **Trainer-style injury care**, and keep playing season after season.

**It is not** Championship Manager 93. We are building toward that horizon over many releases — not in this one.

---

## Decisions needed from you

| # | Question | PM recommendation | Your call |
|---|----------|-------------------|-----------|
| 1 | Approve R-1888 scope as locked? (no transfers, no modern injury insurance in this release) | **Yes** | ☐ |
| 2 | Human testers after soak sign-off? | **Yes** | ☐ |
| 3 | Next big tranche after 1888: **1892 expansion** (second division, reserves, basic transfers)? | **Yes** | ☐ |

Reply in chat or initials here — PM updates the decision log. You do **not** need to open other files.

---

## Blockers before human playtest (plain English)

| # | What | Status |
|---|------|--------|
| 1 | **Long-run stability** — 60-season automated run | 🟢 Green (fixed a real slowdown, now ~20% faster, zero errors, re-verified 2026-07-15) |

**All engineering blockers are closed.** The only thing left before inviting human testers is **your sign-off** on the soak evidence — see [PM_REPORT_PROJECT_STATUS_2026_07_15.md](PM_REPORT_PROJECT_STATUS_2026_07_15.md) §2–3 for the numbers.

**Closed this sprint (2026-07-12):** Victorian injury screens (ME-1), injury tests, Continue button, match history click, save/load UI, player manual ch.6–7, production mod load check.
**Closed this sprint (2026-07-15):** 60-season soak — root-caused and fixed a late-season slowdown (found and fixed **5** separate causes over the day, all the same "scanning full history instead of current season" shape), soak now runs clean and faster, long tests always show progress + ETA.

**Important clarification (2026-07-15 evening):** the 60-season soak is an **engineering-only** background check — human testers never run it and never wait on it. A team report that it was failing turned out, on re-run, to be a false alarm (0 errors, all 60 seasons completed) plus a real-but-separate performance smell (fixed) and a tooling issue where a background test process got orphaned and looked "dead" while still quietly running (also fixed — see [ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md](ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md)). Human sign-off criteria are unchanged: the fast test suite (~1 min) + normal manual play.

**Update (2026-07-19):** actually profiled the run (Java Flight Recorder, not just log reading) and found a sixth slowdown cause — a "job openings" list that quietly kept every job posting ever created and re-checked all of them every day. Fixing it took the full 60-season run from ~80-90 minutes to **~74 seconds**. This was the dominant cause all along; the soak is no longer a "long test" by any definition. Tests **158+5**, all green.

**Estimate:** Your sign-off → invite testers. No more code work is required to reach that gate.

---

## Not in Futtoboru 1888 (on purpose)

| Feature | When (roughly) | Why wait |
|---------|----------------|----------|
| Buying/selling players | **1892+** release | Needs economy + squad rules |
| Injury insurance menus | **1906+** release | Historically wrong for 1888 |
| Lilleshall / surgeon abroad | **1951–1993** releases | Modern medicine |
| Bookings / discipline | **1919+** release | Needs match card system |
| Reserve squads | **1892+** with 2nd division | Squad depth feature |
| Scouts / coaching staff | **1892+** stub, full later | CM93 depth is 1990s product |

We **document** all of this for the long roadmap. We **ship** only what fits 1888.

---

## How we avoid slowing down

| Old way (pain) | New way |
|----------------|---------|
| PO reads 175 markdown files | PO reads **this file** |
| Every feature = new ANALYSIS/DESIGN/PM essay | Feature spec **only while building**, then registry row |
| IDs like F144–F147 in PO face | Plain English here; IDs live in `docs/registries/` for dev/AI |
| Docs drift from code | Agents update registries + this brief when shipping |

**Governance detail (optional):** [GOVERNANCE_MODEL_V1.md](GOVERNANCE_MODEL_V1.md)

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-19 | Profiled the soak with JFR and found a sixth, dominant slowdown cause (JobManager re-scanning every job opening ever created, ~85% of sampled CPU time). Fixed; 60-season soak now ~74 seconds total (was ~80-90 minutes). Tests **158+5**, all green |
| 2026-07-15 (evening) | Investigated a team report of soak failures: not reproduced (0 errors, 60/60 seasons complete). Found and fixed 3 more of the same "scan full history" perf pattern (fitness tick, attendance form, mandatory-draw-message check) and root-caused the "process keeps getting killed" mystery to an orphaned/zombie background process, not a real external kill. Clarified policy: humans never run/wait on the soak. Tests **152+5**, all green |
| 2026-07-15 | QA hardening pass: fixed a second slowdown source (inbox scan), added cup-replay tests, hardened save/load against corrupt files, extended cup soak 2→15 seasons (found non-blocking A-9 for R-1892). Tests **152+5**, all green |
| 2026-07-15 | Soak blocker closed (fixed slowdown, faster, zero errors); **8/8** Tier A blockers closed; 141+5 tests green; only your sign-off remains |
| 2026-07-12 | ME-1 Victorian injury UX shipped; 7/8 Tier A blockers closed; 135+5 tests green |
| 2026-07-12 | Linked full PM status report; status unchanged (8 blockers still open) |
| 2026-07-12 | Note: automated E2E ≠ screen clicks — see ANALYSIS_AGENT_PLAYER_E2E_V1 (post-1888) |
| 2026-07-03 | Created — single PO dashboard; replaces multi-doc PO reading |

---

*PM full status: [PM_REPORT_PROJECT_STATUS_2026_07_15.md](PM_REPORT_PROJECT_STATUS_2026_07_15.md) · Eagle: [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) · Agent E2E analysis: [ANALYSIS_AGENT_PLAYER_E2E_V1.md](ANALYSIS_AGENT_PLAYER_E2E_V1.md)*
