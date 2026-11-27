# Roadmap and decisions (2026-11-27)

Full audit with evidence: `docs/analysis/FUTTOBORU_ANALYSIS_2026-11-27.html` (open in a browser).

## Decisions taken by the producer
| Id | Decision |
|---|---|
| D1 | Canonical WORKSTATION checkout: `G:\git\futtoboru` (fast-forwarded to develop). `G:\git\futtoboru_cursor\` is a read-only archive. |
| D2 | The 4 former local-only repos: public-safe docs in `docs/archive/` of this public repo; business material (marketing, ROI, cost quotes, personal profile) and their full history in the **private Bitbucket repo `Geomancer/futtoboru-private`** (producer, 2026-11-27, following `PRIVACY.md`). |
| D3 | The 62 AI root docs moved to `docs/archive/2025-12-ai/`; short living docs replace them. |
| D4 | `feature/competition-completion-v1`: re-apply code + tests on a clean branch, drop the junk. |
| D5 | Match results come from an **event/possession simulation** (headless, seeded, testable). 2D only as a replay viewer later. |
| D6 | Engine v1 output: **key events** (goals with minute and scorer, big chances, injuries) + a short period-style report. |
| D7 | **Fully era-accurate 1888 rules** from v1, including the 3-player offside rule, no substitutes, no penalties (until 1891), umpires + referee. |
| D8 | WORKSTATION and BOOK3PRO are **equals**: each merges its own green branches into develop. |
| D9 | Light coordination: `docs/HANDOFF.md` + branch prefixes (`ws/`, `b3/`) + CI. |

## Phases
**Phase 0: consolidate git** (done 2026-11-27 except P0-4)
- P0-1 archive the 4 local-only repos ✔ · P0-2 move root docs ✔ · P0-3 CLAUDE.md + HANDOFF/BUILD/ROADMAP/STATUS ✔
- P0-4 competition-completion code + tests (D4) ✔
- Sonar stage and its dead token removed from `Jenkinsfile` ✔ · Producer: push the v0.4.0 release to `main`.

**Phase 1: builds anywhere** (2-3 days)
- P1-1 Maven 3.9 build fix ✔ · P1-2 `mvnw`, `.sh` scripts, `.gitattributes` · P1-3 GitHub Actions `mvn verify` · P1-4 dead 3D code, unused deps, PDFs out of resources · P1-5 one logging style

**Phase 2: one full season, cleanly** (~1 week)
- P2-1 Continue double-click · P2-2 single match-result path · P2-3 headless 1888-89 season test · P2-4 data fixes (Burnley padding, duplicate player) · P2-5 save slots + integer map keys

**Phase 3: match engine v2** (3-5 weeks, larger because of D7)
- E-01 `Position` on players + 2-3-5 XI picker (no substitutes; injured player leaves the side)
- E-02 seeded possession/chance simulation, attack vs defence vs goalkeeper
- E-03 era rules: 3-player offside as a phase of play that kills attacks, no penalties, goalkeeper handling in own half, umpire appeals
- E-04 `MatchEvent` list (goal, chance, offside, injury) + period-style text report and match screen
- E-05 calibration against the real 1888-89 results (load the public fixture list as test data)
- E-06 replace `MatchSimulator` internals; keep the 2D prototype as a future viewer

**Phase 4: season 2 (1889-90)**: re-election vote, contract expiry and free agents, then "seasons as releases".
Marketing only once a full season plays end to end.
