# Handoff board (WORKSTATION ⇄ BOOK3PRO)

Newest entries on top. One line per event. Format: `YYYY-MM-DD HH:MM · MACHINE · CLAIM|DONE|HANDOVER|BLOCKED · item · note`.
Claim before you start; mark DONE when merged to develop. Never claim an item that already has an open CLAIM.

## Open claims
| Item | Machine | Branch | Since |
|---|---|---|---|
| _none_ | | | |

## Ready to take (from docs/ROADMAP.md)
- **P0-4** Re-apply the code and tests from `origin/feature/competition-completion-v1` on a clean branch (drop `temp_develop.java`, the duplicate `CupBracketGeneratorTest`, the extra docs), build, merge (D4).
- **P1-2** Maven wrapper (`mvnw`) + `.sh` run scripts + `.gitattributes`.
- **P1-3** GitHub Actions: `mvn -B verify` on push and PRs.
- **P1-4** Remove dead 3D demo code, unused bullet deps, PDFs under `resources/concepts`.
- **P2-1** Fix Continue double-click advancing two days (`MainGameMenuTable`).
- **P2-2** Merge the two match-result paths in `FuttoboruGameEngine`.
- **P2-3** Headless test: simulate the full 1888-89 season end to end.
- **P2-4** Data: Burnley placeholder players (1865 birthdates), Pat Gallocher at two clubs.

## Log
- 2026-11-27 · WORKSTATION · DONE · P0-1/P0-2/P0-3/P1-1 · Imported the 4 former local-only repos into `docs/archive/` (with history), moved 62 root docs to `docs/archive/2025-12-ai/`, fixed the Maven 3.9 build, added CLAUDE.md + docs. Branch `ws/phase0-consolidation`.
