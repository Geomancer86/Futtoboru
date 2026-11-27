# Futtoboru: rules for every session (WORKSTATION and BOOK3PRO)

Futtoboru is a football manager that starts with the 1888-89 Football League.
Java 17, Maven (`futtoboru-core`, `futtoboru-desktop`), libGDX 1.12 + VisUI.
Game data lives in `futtoboru-core/src/main/resources/mods/seasons/18/` (CSV-like `.txt`).

## Before you start
1. `git fetch origin` and read `docs/HANDOFF.md`: what the other machine claimed, merged or left half-done.
2. Claim your item in `docs/HANDOFF.md` (commit + push to develop) before writing code.
3. Plan and decisions: `docs/ROADMAP.md`. Build and setup: `docs/BUILD.md`.

## Git rules (both machines are equals, decision D8)
- One item = one branch from `origin/develop`: `ws/<item>` on WORKSTATION, `b3/<item>` on BOOK3PRO.
- Before every push: `git fetch origin && git merge origin/develop`, then `mvn -B verify`. Push only green builds.
- Each machine merges its own green branches into `develop` (no waiting on the other machine).
- Never force-push. Never rewrite `develop` or `main`. `main` only gets tagged releases, done by the producer.
- Parallel agents on one machine: one `git worktree` per item; never work in another session's worktree.
- Record a handover line in `docs/HANDOFF.md` when you stop mid-item.

## Content rules
- This repo is **public**: follow `PRIVACY.md`. Business-private material goes to the private Bitbucket repo
  `Geomancer/futtoboru-private`, cloned outside this clone (`C:\git\futtoboru-private`).
- `docs/archive/` holds unverified AI drafts from 2025. Do not treat them as facts or specs.
  Known errors: offside in 1888 was the **3-player rule (1866-1925)**, not 2; no penalty kicks before 1891;
  no cards; one umpire per side plus a referee off the pitch.
- Game data is JSON in `futtoboru-core/src/main/resources/gamedata/` (`docs/DATA_MODEL.md`): stable string ids that never change (renames via `aliases.json` + rename log), a source per record. Run `python tools/data/validate.py` and `python -m pytest tools/data` on every data change.
- Match results come from the event/possession engine (decision D5); the 2D prototype is only a viewer.
- Keep new docs short and few. Update `docs/STATUS.md` instead of writing new summary files.
