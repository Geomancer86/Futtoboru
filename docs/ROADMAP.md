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
| D10 | **Yes** (producer, 2026-11-27): Phase 2 adds P2-0 determinism, P2-6 season reset, P2-7 period details; larger P2-4/P2-5; Phase 2 becomes ~2 weeks. |
| D11 | **Stable string data ids** (DarkBlade pattern): lower-case slugs unique per kind, never changed (renames via `aliases.json` + rename log); runtime `Long` ids from one saved `IdAllocator`. See `docs/DATA_MODEL.md`. |
| D12 | UI skin stays: VisUI + Tixel + Droid Sans are Apache 2.0 (as in DarkBlade), credited in `THIRD_PARTY_NOTICES.txt`. A new own skin comes later. |
| D13 | **Yes** (producer, 2026-11-27): tag a release on `main` at the end of each phase (v0.4.1, v0.5.0, ...). |
| D14 | **All game data in JSON**, like DarkBlade: packs under `resources/gamedata/<pack>/` with a `pack.json` that lists every file; strict validator (unknown fields are errors). |
| D15 | **One people table + season squads**; seasonal facts (ground, balance, squad) per season folder. v0.4 saves are **not** migrated; saves are versioned from now on. |
| D16 | **UI/UX phase after the data redesign** and before the match engine. |
| D17 | **Real historical results are reference data only** (producer, 2026-11-27). They are used (a) by the engine calibration in test code (E-05) and (b) as history for seasons before the chosen start season (tables, champions, cup winners, records). They never decide, script or nudge a match the player plays: every played season is simulated. Season 1 keeps the **generated schedule**; real 1888-89 fixture dates and pairings are reference data too. |

## Phases
**Phase 0: consolidate git** (done 2026-11-27 except P0-4)
- P0-1 archive the 4 local-only repos ✔ · P0-2 move root docs ✔ · P0-3 CLAUDE.md + HANDOFF/BUILD/ROADMAP/STATUS ✔
- P0-4 competition-completion code + tests (D4) ✔
- Sonar stage and its dead token removed from `Jenkinsfile` ✔ · Producer: push the v0.4.0 release to `main`.

**Phase 1: builds anywhere** (2-3 days)
- P1-1 Maven 3.9 build fix ✔ · P1-6 third-party notices (skin done; 3D assets with P1-4) · P1-2 `mvnw`, `.sh` scripts, `.gitattributes` · P1-3 GitHub Actions `mvn verify` · P1-4 dead 3D code, unused deps, PDFs out of resources · P1-5 one logging style

**Phase 2: one full season, cleanly** (~2 weeks if D10)
- P2-0 one seeded, injected random source (needed by P2-3 and E-02)
- P2-1 Continue double-click (cause: 200 ms cooldown vs ~50 ms processing) · P2-2 single match-result path (only one checks the draw blocker)
- P2-3 headless seeded 1888-89 season test
- **P2-4 data model v2** (D11, D14, D15; `docs/DATA_MODEL.md`): slice 1 JSON pack + converter + validator + review list ✔ · slice 2 Java loader + `IdAllocator` ✔ · slice 3 saves v1 (with P2-5) ✔ · slice 4 data review (copied cup rosters, placeholders, duplicates)
- P2-5 saves v1 ✔: ids not copies, `saveVersion`, slots, fix load path / folder name, round-trip test, append-only `ids.json` registry
- P2-8 ✔ `AuthorityManager` kept the empty game from startup, so the daily competition check (league completion, cup draws/progression) never ran in real play
- P2-6 season rollover resets the table (empty loop in `AuthorityManager`) · P2-7 £ not $ in prize messages, cup final from the bracket

**Phase 3: UI/UX** (1-2 weeks, D16; skin unchanged, D12)
- U-01 theme tokens in JSON (palette, fonts, spacing), like DarkBlade's `UiTheme`; no literal colours or sizes in screens
- U-02 remove `setDebug(true)` and the `Color.GOLD/GREEN/CYAN` literals; consistent spacing
- U-03 UI scaling per resolution (13 px font everywhere today)
- U-04 navigation: one clear menu, hide or finish the 11 stub screens
- U-05 an interface guide (one tooltip layer, one window at a time, screenshot per UI item), as DarkBlade's `GUIA_DE_INTERFAZ.md`

**Phase 4: match engine v2** (3-5 weeks, larger because of D7)
- E-01 `Position` on players + 2-3-5 XI picker (no substitutes; injured player leaves the side)
- E-02 seeded possession/chance simulation, attack vs defence vs goalkeeper
- E-03 era rules: 3-player offside as a phase of play that kills attacks, no penalties, goalkeeper handling in own half, umpire appeals
- E-04 `MatchEvent` list (goal, chance, offside, injury) + period-style text report and match screen
- E-05 calibration against the real 1888-89 results (load the public fixture list as test data)
- E-06 replace `MatchSimulator` internals; keep the 2D prototype as a future viewer

**Phase 5: season 2 (1889-90)**: re-election vote, contract expiry and free agents, then "seasons as releases".
Marketing only once a full season plays end to end.
