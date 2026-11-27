# Handoff board (WORKSTATION ⇄ BOOK3PRO)

Newest entries on top. One line per event. Format: `YYYY-MM-DD HH:MM · MACHINE · CLAIM|DONE|HANDOVER|BLOCKED · item · note`.
Claim before you start; mark DONE when merged to develop. Never claim an item that already has an open CLAIM.

## Open claims
| Item | Machine | Branch | Since |
|---|---|---|---|
| P2-4 data model v2 (JSON, string ids, people + squads) | BOOK3PRO | `b3/P2-4-data-model-v2` | 2026-11-27 |
| E-00 real 1888-89 fixtures/results (docs only) | WORKSTATION | `ws/E-00-1888-results` | 2026-11-27 |
| Phase 3 UI audit (docs only) | WORKSTATION | `ws/U-audit` | 2026-11-27 |

## Files in use (lock: don't edit these while the claim is open; ask in the log)
| Machine | Item | Paths |
|---|---|---|
| BOOK3PRO | P2-4 slices 2-3 | `futtoboru-core/src/main/resources/gamedata/**`, `mods/**/*.txt`, `tools/data/**`, `system/loaders/**`, `system/DatabaseLoader.java`, `system/SaveLoadSystem.java`, `SaveGame.java`, `ScriptsLoader.java`, `ScriptsManager.java`, the `data/*` model classes (adding `dataId`), `PersonGenerator`, `PlayerContractGenerator`, `JobManager`/`MessageManager` id counters |

## Ready to take (from docs/ROADMAP.md)
**Safe for WORKSTATION now (no overlap with the BOOK3PRO lock):**
- **P1-2** Maven wrapper (`mvnw`) + `.sh` run scripts (extend the existing `.gitattributes`).
- **P1-4** Remove dead 3D/Bullet demo code (`com/rndmodgames/graphics`, `scenes`, `org/microbasic`, `futtuboru2`, the `Bullet*` classes), unused deps (bullet, freetype, lwjgl3 in core, twitch4j, sonar plugin) and their assets; keep `match/engine/**`.
- **P2-1** Continue double-click (`MainGameMenuTable`).
- **E-00** Research the real 1888-89 Football League fixtures and results with sources (a list in a new `docs/data/` note; it becomes `seasons/1888-89/results.json` after slice 2).
- **P2-4 slice 4 (research part)** Work `docs/data/REVIEW_1888-89.md`: find sources for the copied cup rosters, the cup entrants, the duplicates. Write findings into the review file; apply them to the JSON after slice 2 lands.

**Wait for BOOK3PRO's P2-4 slice 2 (they touch the locked files):** P2-0 determinism, P2-2 single result path, P2-5 saves, P2-6 season reset, P1-5 logging.
- **P1-2** Maven wrapper (`mvnw`) + `.sh` run scripts + `.gitattributes`.
- **P1-4** Remove dead 3D demo code and unused bullet deps (concept images/PDF already gone via SEC-01).
- **P2-1** Fix Continue double-click advancing two days (`MainGameMenuTable`).
- **P2-2** Merge the two match-result paths in `FuttoboruGameEngine`.
- **P2-3** Headless test: simulate the full 1888-89 season end to end.
- **P2-4 slice 4** Data review (WORKSTATION): `docs/data/REVIEW_1888-89.md`, rebuild the copied cup rosters. Edit the JSON only after slice 2 lands (until then the JSON is generated from `mods/*.txt`; edit the .txt and rerun `tools/data/convert_legacy.py`).

## Log
- 2026-11-27 · WORKSTATION · DONE · design · `docs/design/MATCH_ENGINE_V2.md`: sourced 1888-89 laws (3-opponent offside in both halves and from throw-ins, all free kicks indirect, club umpires on appeal + off-field referee, keeper handles in own half only, no nets/penalties) + possession model, data contracts, `RandomSource.fork()` for P2-0, E-01..E-06 slices. 11 producer questions in section (i). Archive drafts used 2-player offside and penalties: do not reuse.
- 2026-11-27 · WORKSTATION · DONE · P1-4 · Removed 41 dead 3D/Bullet classes (5.5k lines), 57 assets (~15 MB), gdx-bullet/freetype/sonar; core now depends on plain `gdx` (lwjgl3 only in desktop). Fat jar 44.8 to 16.6 MB. Smoke-launched OK. **BOOK3PRO: core `pom.xml` changed; expect a small conflict if slice 2 adds a dependency.**
- 2026-11-27 · WORKSTATION · DONE · P1-2 · `./mvnw` (Maven 3.9.12) + `.sh` twins of every .bat; CI now runs `./mvnw`. Use `./mvnw -B verify` from now on.
- 2026-11-27 · WORKSTATION · DONE · P2-1 · Cause: the 200 ms cooldown expired before the 2nd press of a double-click. Now a `ClickListener` that swallows tap count > 1 (6 tests). Side effect: rapid clicking advances one day per pair.
- 2026-11-27 · WORKSTATION · NOTE · producer · D10 and D13 approved (Phase 2 ~2 weeks with P2-0/P2-6/P2-7; tag a release on `main` at the end of each phase).
- 2026-11-27 · WORKSTATION · CLAIM · P2-1, P1-2, P1-4, E-00, engine v2 design, UI audit · Six parallel lanes, one `ws/` branch each; the three docs lanes write only under `docs/`. None touch the BOOK3PRO lock.
- 2026-11-27 · WORKSTATION · DONE · P2-4 slice 4 (research part) · Findings with sources in `docs/data/REVIEW_1888-89_FINDINGS.md` (hand-written, because `convert_legacy.py` regenerates `REVIEW_1888-89.md`). BOOK3PRO, for slice 2: please have `convert_legacy.py` write a pointer to the findings file at the top of the generated review (a hand-added line breaks `test_json_matches_legacy_files`, which turned develop red at 9eb5e28; fixed). Highlights: the data's FA Cup list is right (old code wrong); `witton-albion` is really Witton F.C. (Blackburn); invented players (5 Belfields, 9 Higgins); Burnley placeholders mostly from 1889-92; several cup clubs can't reach 11 sourced players (open decision in the findings). Applying to `gamedata/` waits for slice 2.
- 2026-11-27 · WORKSTATION · CLAIM · P2-4 slice 4 (research part) · Findings into `docs/data/REVIEW_1888-89.md` only; no `gamedata/` or `mods/` edits until slice 2 lands.
- 2026-11-27 · WORKSTATION · NOTE · for BOOK3PRO (locked file) · `DatabaseLoader.getInstance()` publishes `instance` before loading finishes, so a second thread (headless `create()`) sees a half-loaded loader. Linux CI hit it via `ScriptManagerTests`. Tests now wait for `create()` (`BaseTest.awaitHeadlessCreate()`); please make `getInstance()` build into a local and assign it last (or `synchronized`) in slice 2.
- 2026-11-27 · WORKSTATION · DONE · P1-3 · `.github/workflows/ci.yml`: Maven verify (JDK 17, Xvfb) + data pytest + validator on every push and PR; green on GitHub (run 45994935767). Also fixed the Linux-only test race above. Merged a36607e.
- 2026-11-27 · WORKSTATION · CLAIM · P1-3 · GitHub Actions: mvn verify + pytest tools/data + validate.py on push/PR. Touches `.github/` only.
- 2026-11-27 · BOOK3PRO · DONE · P2-4 slice 1 · JSON pack + converter + validator + review list merged to develop (inert: the game still reads mods/*.txt). Claim stays open for slices 2-3; see 'Files in use'. WORKSTATION: pick from 'Safe for WORKSTATION now'.
- 2026-11-27 · BOOK3PRO · CLAIM · P2-4 · Data model v2 per producer decisions D11, D14-D16 (all JSON like DarkBlade, stable string ids + save-local runtime ids, one people table + season squads, v0.4 saves not migrated). Slice 1: converter, JSON data, validator, review list.
- 2026-11-27 · WORKSTATION · DONE · merge · Merged `b3/SEC-01-public-hygiene` then `ws/phase0-public` into develop (1ba9722); `mvn clean package` green, 41 core tests. Jenkinsfile conflict resolved by **deleting** the Sonar stage (producer: token is dead). BOOK3PRO: `git fetch && git switch develop && git pull`; both SEC-01 and ws/phase0-public can be deleted. P1-4: concept images/PDF already removed by SEC-01; dead 3D code and unused deps still open.
- 2026-11-27 · WORKSTATION · NOTE · private · Bitbucket `Geomancer/futtoboru-private` (private) holds the 4 former local repos with full history, marketing and cost docs. Clone to `C:\git\futtoboru-private`.
- 2026-11-27 · WORKSTATION · DONE · P0-4 · Cup completion/runner-up, next season generated when the league ends, null-team guard, champion display, 17 new tests (41 core tests green). Branch `ws/P0-4-competition-completion`.
- 2026-11-27 · WORKSTATION · DONE · P0-1/P0-2/P0-3/P1-1 · Public-safe docs of the 4 former local-only repos into `docs/archive/`; all of them, history and business material included, into the private Bitbucket repo `Geomancer/futtoboru-private`; moved 62 root docs to `docs/archive/2025-12-ai/`, fixed the Maven 3.9 build, added CLAUDE.md + docs. Branch `ws/phase0-consolidation`.
