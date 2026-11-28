# Futtoboru: rules for every session (WORKSTATION and BOOK3PRO)

Futtoboru is a football manager that starts with the 1888-89 Football League.
Java 17, Maven (`futtoboru-core`, `futtoboru-desktop`), libGDX 1.12 + VisUI.
Game data lives in `futtoboru-core/src/main/resources/gamedata/` (JSON packs, see `docs/DATA_MODEL.md`).

## Before you start
1. `git fetch origin` and read `docs/BOARD.md` (claims, locks, ready) and the other machine's log in `docs/log/`: what it claimed, merged or left half-done.
2. Claim your item in `docs/BOARD.md` (the machine's PM does it; commit + push to develop) before writing code. Lanes never edit BOARD or the logs; they report to their PM (D33, R-FUT-5).
3. Plan and decisions: `docs/ROADMAP.md`. Build and setup: `docs/BUILD.md`.

## Git rules (both machines are equals, decision D8)
- One item = one branch from `origin/develop`: `ws/<item>` on WORKSTATION, `b3/<item>` on BOOK3PRO.
- Tests (D27, `docs/BUILD.md` "Test tiers"): while coding only the affected tests (`tools/test-changed.ps1`, through the lock like every Maven run);
  the level gate once at the end: `git fetch origin && git merge origin/develop`, then `python tools/test_level.py --run`
  (same as `tools/dev/check`). It runs only what changed since the last green run, so a merge that brought only docs
  re-runs nothing; if develop did not move since the tested SHA, merge into develop without running anything.
  The full tier (`-Pfull`) runs on WORKSTATION's Jenkins for develop, not locally; releases: `-Pfull,smoke`.
  **GitHub is git storage only (D32):** no Actions, PRs, Releases or API. A branch's full tier: `python tools/ci_jenkins.py
  run --branch <branch>` on the home LAN; off it, a local `-Pfull` through `suite_lock.py` when the change needs it.
  Never wrap `test_level.py` in `suite_lock.py` by hand (it takes the lock itself; nested runs are now safe anyway).
- Every heavy Maven run goes through `python tools/suite_lock.py run -- <command>` (`FUTTOBORU_SUITE_SLOTS` concurrent runs per machine: WORKSTATION 4, default 1; it waits
  inside and expires by itself): never your own wait loop. Long builds in the background. Push only green.
- A test class over 2 s gets `@Tag("slow")`; `python tools/test_tiers.py` fails on an untagged class over 5 s.
- Each machine merges its own green branches into `develop` (no waiting on the other machine).
- Never force-push. Never rewrite `develop` or `main`. `main` only gets tagged releases (fast-forward from a green `develop`); agents may do them (D25).
- Parallel agents on one machine: one `git worktree` per item; never work in another session's worktree.
- Record a handover line at the bottom of your machine's log (`docs/log/<MACHINE>.md`) when you stop mid-item.

## Bugs and AI clubs (D31)
- **No open bugs.** A bug you find is fixed now (this lane, or a fix lane started at once), at most one or two prompt
  rounds later, before more feature work. Never log "open, not fixed" and move on; never ask whether to fix a bug.
  A fix that changes seeded expectations re-pins them. Start every session by fixing any bug BOARD/STATUS still lists.
- **AI clubs get every system the player's club has** (friendlies, team sheet, contracts, recruitment...) unless sourced
  history says clubs of that season didn't. Each gameplay lane builds the AI side too.

## Content rules
- This repo is **public**: follow `PRIVACY.md`. Business-private material goes to the private Bitbucket repo
  `Geomancer/futtoboru-private`, cloned outside this clone (`C:\git\futtoboru-private`).
- `docs/archive/` holds unverified AI drafts from 2025. Do not treat them as facts or specs.
  Known errors: offside in 1888 was the **3-player rule (1866-1925)**, not 2; no penalty kicks before 1891;
  no cards; one umpire per side plus a referee off the pitch.
- Game data is JSON in `futtoboru-core/src/main/resources/gamedata/` (`docs/DATA_MODEL.md`): stable string ids that never change (renames via `aliases.json` + rename log), a source per record. Run `python tools/data/validate.py` and `python -m pytest tools/data` on every data change.
- A new data record gets its runtime number with `python tools/data/assign_ids.py`; `ids.json` is append-only (never
  edit or delete a number; a removed record goes to `retired`).
- Match results come from the instant result (`MatchSimulator`) until the ECS engine (D20) passes calibration; then
  the engine replaces it behind `MatchResultRecorder` (D30). Gameplay lanes never wait for the engine. Unresearched
  gameplay data may be `placeholder` (listed for review) so research never blocks a lane (D30).
- Real historical results/fixtures (`docs/data/results_*.csv`, later `seasons/*/results.json`) are reference data (D17):
  engine calibration in test code, and history for seasons before the start season. Game code must never read them
  to decide or schedule a match the player plays.
- Keep new docs short and few. Update `docs/STATUS.md` instead of writing new summary files.
