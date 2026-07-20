# PO Brief — Futtoboru

**One page. Read this.** Everything else is detail for PM, dev, or AI.

**Updated:** 2026-07-19 · **Release:** Futtoboru 1888 · **Branch:** `analyze-ai-branch`

> **PO redirect report (3 points):** [PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md](PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md) — done / missing / next for first release.

---

## Status

| | |
|-|-|
| 🟢 | Game loop works: league, cup, pick team, play matches, ratings, saves — cup competitions can now genuinely finish a full season (fixed a real bug today, see changelog) |
| 🟢 | Home screen is now a real FM/CM-style overview dashboard (league position, form, next fixture, news, injuries, finances) — was a placeholder button since the start of the project |
| 🟢 | Automated tests: **182 + 5** green on every CI run |
| 🟢 | Packaged desktop jar actually launches and New Game reaches the main game screen cleanly (fixed two real bugs today, see changelog) |
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
| 2026-07-19 (latest) | **You asked us to make sure every screen scrolls when its content is taller/wider than the window — audited all ~50 screens and fixed the 2 real gaps found**: the very first pre-game screen (choose season & manager) had no scroll fallback at all, unlike every other wizard step, so it could clip its own buttons off-screen at higher zoom levels; and the fixtures list's "scroll without clicking first" trick could accidentally disable the mouse wheel for the *entire* schedule screen after moving the mouse away from it. Every other screen already scrolled correctly (most in-game tabs get it for free from one shared wrapper). Tests unchanged at 182 + 5, all green |
| 2026-07-19 | **You asked what the empty Home screen could show, referencing Football Manager/Championship Manager — built a real overview dashboard**: club + manager identity, league position and record, a coloured recent-form strip (last 5 results), your next fixture with a smart "Play Match" or "View Schedule" button, the latest inbox news with an unread badge, injured-player list with return estimates, a finances snapshot, and quick-link shortcuts to Squad/Tactics/Schedule/Finances/Inbox/Club Info — plus a proper empty state for unemployed managers (a "Find a Job" button instead of a blank screen). No club crests/colours or board objectives are shown, because that data doesn't exist in the game yet (not faked). Verified end to end by actually building and running the desktop jar as both an unemployed and an employed manager: zero crashes, zero missing-text keys. Tests **168 + 5 → 182 + 5**, all green |
| 2026-07-19 | **You reported the cup draw soft-locked Continue, and that a full season never finished — found and fixed 3 stacked real bugs**: (1) our own test tooling had a latent bug that silently ran two independent copies of the game engine at once, which was quietly corrupting the trustworthiness of every automated test touching mandatory messages (explains why "the testing is not good" felt true even with green tests); (2) the "you've been drawn against X" cup message only ever reached the inbox for the very first tie of each round, because they all shared one duplicate-detected title; (3) the code that draws Round 2 of any cup competition after Round 1 finishes had a real, always-present bug (a never-initialized field) that silently threw an error every single time, meaning **no cup competition could ever complete a full bracket in the shipped game** — directly explaining why a full season wouldn't finish. All three fixed and covered by a new permanent test; manually re-ran 15 procedural seasons and got 15/15 league champions **and** 15/15 FA Cup champions with zero health issues. Tests **168 + 5**, all green |
| 2026-07-19 | **You clicked through the real game and hit a third real bug in the same New Game wizard — fixed it too**: Step 2 (Choose Countries) crashed immediately just from opening that screen, because of a VisUI API misuse (`Tooltip(String)` was mistakenly used for plain text — it actually looks up a named style and throws if none is registered). Fixing that revealed a second, related mistake (attaching the same tooltip twice). Both fixed; also fixed our own testing tool, which had been skipping straight past this exact screen instead of going through it like a real click does. Re-verified the entire New Game wizard end to end again after the fix: zero crashes. Tests unchanged at **167 + 5**, all green |
| 2026-07-19 | **You told us New Game still failed after the jar-startup fix — found and fixed a second real bug**: New Game actually completed and reached the main game screen without throwing, but ~36 labels across the main game screen (Squad, Authority, Personal Details, Schedule, Finances, day names, etc.) showed raw untranslated keys like `home`/`squad`/`nationality` instead of real words, because those keys were never added to any language bundle — looks exactly like "the game is broken" to a human, even though nothing crashed. Found it by actually driving the full New Game → Start Game flow (season, manager, countries, profession, club) end to end via a new debug tool, not just reading code. Fixed all 4 language files (English UK/US, Spanish Spain/Latin America — real translations, not copy-paste) and added a permanent automated test that checks every on-screen text key against every language file, so this class of bug can't silently come back. Tests **167 + 5**, all green |
| 2026-07-19 | **Found and fixed a real launch-blocking crash**: the packaged desktop jar (`build-and-run.bat`) crashed on every startup, before the main menu ever appeared, because a language file's name didn't exactly match (case) what the game asked for — invisible under our automated tests because they don't run off the packaged jar, only visible once actually launched. Confirmed it predated today's menu work (reproduced on yesterday's commit too). Fixed the filenames and added a permanent test that would catch this exact mistake again. Actually launched the built jar this time to confirm the main menu now loads. Tests **166 + 5**, all green |
| 2026-07-19 | Implemented all 7 pre-game menu fix units from the audit below: Main Menu (Continue, Quit confirm, branding), New Game wizard steps 1–3 (null-guards, manager UX, country validation + live counter, filtered cups/leagues counts, busy indicator + error dialog on Start failure), Create Manager (rejects garbage/implausible birth dates instead of silently corrupting data, overwrite confirm, Back button), Load/Save dialogs (real save metadata, delete, overwrite confirm, most-recent-first), and in-game Quit to Menu/Desktop (previously both no-ops). ~60 new translated strings added. Tests **165 + 5**, all green. Settings screen still deferred |
| 2026-07-19 (later) | Audited all 7 pre-game menu screens (main menu, new game wizard, create manager, save/load) for stubs/dead controls/missing validation — found several silent-failure and silent-data-loss issues. Turned into a per-screen fix spec, Settings screen deferred separately. Analysis + planning only so far, no code changed; dev order TBD |
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
