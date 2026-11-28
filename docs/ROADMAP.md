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
| D18 | **v0.5.0 scope** (producer, 2026-11-27): a full 1888-89 season playable end to end, **every visible button works**, no "placeholder" / "not implemented" left, history screens; XI picker, tactics and the match screen go to v0.6 with the engine. **Minimum resolution 1280×720** (sub-720p entries dropped, default 1280×720, UI scale Auto/1×/2×). **Autosave** to one rotating slot on the 1st of each month and at season rollover. **English only in v0.5, but every label goes through the language files** (no new hard-coded text; convert each screen as it is touched) so Spanish is a pure translation job later. |
| D19 | **Engine v2 choices** (producer, 2026-11-27; answers to `docs/design/MATCH_ENGINE_V2.md` §9): (1) per-club strength factor seeded from the real 1888-89 table, sourced player attributes later (D17 holds: the factor shapes odds, never results); (2) each club's umpire leans to his club, the referee settles; (3) injuries as knock / passenger / carried off (may return), days out in a later release; (4) sendings-off a very rare event; (5) disputed goals (no nets) a rare umpire-decided event; (6) tactics: club style (dribbling / combination) **and the era's shapes** (2-3-5, 2-2-6, 1-2-7, ...), AI picks, player can set; (7) **new `dribbling` attribute** in the people data (validator, ids, saves); (8) weather seeded from month + ground; (9) **all match events saved** (compact), with FM-style filters in the match screen and report: key / extended / full; (10) **minute ticker** match screen that reveals events, then the report; (11) drawn ties and every other rule follow **the sourced rule of that season** (e.g. extra time by agreement where the rules allowed it, else replay), as historical as possible. D19 widens D6 (key events only) to full event storage. |
| D20 | **Match engine = libGDX Ashley ECS with physics** (producer, 2026-11-27; supersedes the "event/possession simulation" part of D5). Players, referee, umpires, ball and crowd are entities; movement, passes, crosses, headers and shots come from physics, not dice. D5's other demands stay: headless, seeded, fixed timestep, testable; D6/D19 outputs (event list with key/extended/full filters, minute ticker, period report) are derived from the simulation. Era physics and kit are data: the leather ball gains weight when wet over a match, the lace (tiento) can cut on headers (a rare event: sourced only anecdotally; caps were team/honour markers, not heading protection, per `docs/data/history/ERA_TIMELINE.md`), boots/shin pads/shirts/knickerbockers evolve by season; every such change is announced by the authorities / news as researched. Design: `docs/design/MATCH_ENGINE_ECS.md` (WORKSTATION), reusing what fits from the DarkBlade graphics engine. |
| D21 | **WORKSTATION focus** (producer, 2026-11-27): UI completion and polish; deep sourced history for clubs and players (fan-love tiers from local favourite to legend, with descriptions); grounds from period sources with a small generated illustration each (code-generated art, safe for this public repo); era equipment/rules research; the ECS engine design. BOOK3PRO leads gameplay. |
| D22 | **ECS engine choices** (producer, 2026-11-27; `docs/design/MATCH_ENGINE_ECS.md` §(i)): (1) **custom pure-Java 2.5D physics**, no Box2D (bit-identical replays on every platform); (3) **full physics in every match**; (4) **the ECS engine itself ships in v0.6** (no thin stand-in release; v0.6 waits for calibration E-20); (6) **crowd as 50-150 sections** with mood, encroachment and a small player effect. Other questions take the doc's recommendations (30 Hz bodies / 60 Hz ball / 10 Hz AI; saved events shown, re-simulate only on the same engine version; umpire placement a researched parameter; physique by position now, sourced later; player's match computed at kick-off and revealed by the ticker; top-down 2D viewer with code-generated sprites). |
| D23 | **DarkBlade code may be reused or re-implemented in Futtoboru** (producer, 2026-11-28), including in this public repo. Port as-is, port + adapt, or re-implement per module (plan: E-21 viewer). Still never carry over secrets, credentials, LAN addresses, personal data or business notes (`PRIVACY.md`), and third-party or AI-generated assets only after their licences are checked. |
| D24 | **Viewer port choices** (producer, 2026-11-28; `docs/design/MATCH_VIEWER_PLAN.md`): copy-port DarkBlade engine classes per class with an origin line (clearance confirmed for all DarkBlade code); keep the 30 Hz simulation with viewer interpolation; replays = ~20 s view buffer + full re-simulation from seed. View angle and pixel scale come from the art prototype checklist. |

| D25 | (producer, 2026-11-28) Each **new game draws a fresh seed** (saved in the game; tests and replays still pass `-Dfuttoboru.seed`). **Agents may push** what progress needs, including tagged releases to `main` (fast-forward only, never force). Old `feature/ui-auto-scale` archived as tag `archive/ui-auto-scale-2026-07` and deleted. E-01 follow-ups queued: source the placeholder positions, dribbling in ageing and snapshots, positions for generated players. |

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

**Phase 2 close-out** (before tagging v0.5.0-alpha)
- P2-9 one edition per competition and season (`ScriptsManager.createCup/createLeague` reuse the season.json edition; `RuntimeIdsTests` ~130; re-enable `FullSeasonTests.oneEditionPerCompetitionAndSeason`)
- P2-10 a new game after playing/loading another starts from fresh data (no reused played objects)
- P2-11 era calendar for the generated schedule: league September-April, FA Cup in the winter (dates generated, D17)
- Producer: push v0.4.0 to `main` (still shows 0.2.0), then tag v0.5.0-alpha

**Phase 3: UI/UX → v0.5.0** (1-2 weeks, D16, D18; skin unchanged, D12). Done = D18 (every button works, 1280×720 and 2560×1440).
- U-01 theme tokens ✔ · U-02 debug lines and literals → tokens ✔ · U-04a stubs deleted ✔
- U-03a UI scale Auto/1×/2×, one startup resolution (default 1280×720), sub-720p entries dropped
- U-04b one menu-entry list for both professions, active highlight, Back button + Esc on drill-downs
- U-04c Home dashboard (next fixture, unread messages, league position, balance)
- U-04d in-game Options: Save, Load, Settings, Quit to menu, Quit to desktop, "unsaved progress" confirm
- U-06 saves: name, overwrite confirm, delete; autosave slot monthly + season rollover (D18)
- U-07 history screens: final tables (`CompetitionEdition.finalStandings`), champions / cup winners, earlier seasons from reference data (E-00b, D17)
- U-08 friendlies finished (11 TODOs) or hidden; sound settings hidden until there is audio
- U-09 one tooltip layer (table headers Pld/W/D/L/GF/GA/Pts) + one `UiDialogs` helper (one modal at a time)
- U-10 labels into the language files, per screen as touched (D18); a guard test against new hard-coded labels
- U-05 interface guide with our own screenshots (last, after U-03/U-04)

**Phase 4: match engine v2 → v0.6.0** (4-6 weeks, larger because of D7 and D19)
- E-01 `Position` + `dribbling` attribute (data, validator, `ids.json`, saves) + XI picker with the era's shapes and club style (no substitutes)
- E-02 seeded possession/chance simulation, attack vs defence vs goalkeeper, club strength factor, weather from month + ground
- E-03 era rules per season (sourced): 3-player offside, no penalties, keeper handling in own half, partisan umpires + referee, disputed goals, rare sendings-off, drawn ties per that season's rule
- E-04 full `MatchEvent` list saved compactly; minute-ticker match screen + period-style report with key / extended / full filters
- E-05 calibration against the real 1888-89 results (test data only, D17)
- E-06 replace `MatchSimulator` internals; keep the 2D prototype as a future viewer

**Phase 5: season 2 (1889-90)**: re-election vote, contract expiry and free agents, then "seasons as releases".
Marketing only once a full season plays end to end.
