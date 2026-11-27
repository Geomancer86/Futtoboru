# Handoff board (WORKSTATION ⇄ BOOK3PRO)

Newest entries on top. One line per event. Format: `YYYY-MM-DD HH:MM · MACHINE · CLAIM|DONE|HANDOVER|BLOCKED · item · note`.
Claim before you start; mark DONE when merged to develop. Never claim an item that already has an open CLAIM.

## Open claims
| Item | Machine | Branch | Since |
|---|---|---|---|
| P2-4 data model v2 (JSON, string ids, people + squads) | BOOK3PRO | `b3/P2-4-data-model-v2` | 2026-11-27 |
| P1-3 GitHub Actions CI (`.github/` only) | WORKSTATION | `ws/P1-3-ci` | 2026-11-27 |

## Files in use (lock: don't edit these while the claim is open; ask in the log)
| Machine | Item | Paths |
|---|---|---|
| BOOK3PRO | P2-4 slices 2-3 | `futtoboru-core/src/main/resources/gamedata/**`, `mods/**/*.txt`, `tools/data/**`, `system/loaders/**`, `system/DatabaseLoader.java`, `system/SaveLoadSystem.java`, `SaveGame.java`, `ScriptsLoader.java`, `ScriptsManager.java`, the `data/*` model classes (adding `dataId`), `PersonGenerator`, `PlayerContractGenerator`, `JobManager`/`MessageManager` id counters |

## Ready to take (from docs/ROADMAP.md)
**Safe for WORKSTATION now (no overlap with the BOOK3PRO lock):**
- **P1-3** GitHub Actions: `mvn -B verify` + `python -m pytest tools/data` + `python tools/data/validate.py` on push and PRs (`.github/` only).
- **P1-2** Maven wrapper (`mvnw`) + `.sh` run scripts (extend the existing `.gitattributes`).
- **P1-4** Remove dead 3D/Bullet demo code (`com/rndmodgames/graphics`, `scenes`, `org/microbasic`, `futtuboru2`, the `Bullet*` classes), unused deps (bullet, freetype, lwjgl3 in core, twitch4j, sonar plugin) and their assets; keep `match/engine/**`.
- **P2-1** Continue double-click (`MainGameMenuTable`).
- **E-00** Research the real 1888-89 Football League fixtures and results with sources (a list in a new `docs/data/` note; it becomes `seasons/1888-89/results.json` after slice 2).
- **P2-4 slice 4 (research part)** Work `docs/data/REVIEW_1888-89.md`: find sources for the copied cup rosters, the cup entrants, the duplicates. Write findings into the review file; apply them to the JSON after slice 2 lands.

**Wait for BOOK3PRO's P2-4 slice 2 (they touch the locked files):** P2-0 determinism, P2-2 single result path, P2-5 saves, P2-6 season reset, P1-5 logging.
- **P1-2** Maven wrapper (`mvnw`) + `.sh` run scripts + `.gitattributes`.
- **P1-3** GitHub Actions: `mvn -B verify` on push and PRs.
- **P1-4** Remove dead 3D demo code and unused bullet deps (concept images/PDF already gone via SEC-01).
- **P2-1** Fix Continue double-click advancing two days (`MainGameMenuTable`).
- **P2-2** Merge the two match-result paths in `FuttoboruGameEngine`.
- **P2-3** Headless test: simulate the full 1888-89 season end to end.
- **P2-4 slice 4** Data review (WORKSTATION): `docs/data/REVIEW_1888-89.md`, rebuild the copied cup rosters. Edit the JSON only after slice 2 lands (until then the JSON is generated from `mods/*.txt`; edit the .txt and rerun `tools/data/convert_legacy.py`).

## Log
- 2026-11-27 · WORKSTATION · CLAIM · P1-3 · GitHub Actions: mvn verify + pytest tools/data + validate.py on push/PR. Touches `.github/` only.
- 2026-11-27 · BOOK3PRO · DONE · P2-4 slice 1 · JSON pack + converter + validator + review list merged to develop (inert: the game still reads mods/*.txt). Claim stays open for slices 2-3; see 'Files in use'. WORKSTATION: pick from 'Safe for WORKSTATION now'.
- 2026-11-27 · BOOK3PRO · CLAIM · P2-4 · Data model v2 per producer decisions D11, D14-D16 (all JSON like DarkBlade, stable string ids + save-local runtime ids, one people table + season squads, v0.4 saves not migrated). Slice 1: converter, JSON data, validator, review list.
- 2026-11-27 · WORKSTATION · DONE · merge · Merged `b3/SEC-01-public-hygiene` then `ws/phase0-public` into develop (1ba9722); `mvn clean package` green, 41 core tests. Jenkinsfile conflict resolved by **deleting** the Sonar stage (producer: token is dead). BOOK3PRO: `git fetch && git switch develop && git pull`; both SEC-01 and ws/phase0-public can be deleted. P1-4: concept images/PDF already removed by SEC-01; dead 3D code and unused deps still open.
- 2026-11-27 · WORKSTATION · NOTE · private · Bitbucket `Geomancer/futtoboru-private` (private) holds the 4 former local repos with full history, marketing and cost docs. Clone to `C:\git\futtoboru-private`.
- 2026-11-27 · WORKSTATION · DONE · P0-4 · Cup completion/runner-up, next season generated when the league ends, null-team guard, champion display, 17 new tests (41 core tests green). Branch `ws/P0-4-competition-completion`.
- 2026-11-27 · WORKSTATION · DONE · P0-1/P0-2/P0-3/P1-1 · Public-safe docs of the 4 former local-only repos into `docs/archive/`; all of them, history and business material included, into the private Bitbucket repo `Geomancer/futtoboru-private`; moved 62 root docs to `docs/archive/2025-12-ai/`, fixed the Maven 3.9 build, added CLAUDE.md + docs. Branch `ws/phase0-consolidation`.
