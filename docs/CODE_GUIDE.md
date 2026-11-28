# Code guide (read this before coding)

One page on how the code works today. Decisions: [ROADMAP.md](ROADMAP.md). Build and tiers: [BUILD.md](BUILD.md).
Data: [DATA_MODEL.md](DATA_MODEL.md). Claims: [BOARD.md](BOARD.md). State: [STATUS.md](STATUS.md).
Package paths below are under `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/` unless written in full.

## Modules and packages
- `futtoboru-desktop`: `DesktopLauncher` (the game), `MatchViewerLauncher` (2D viewer), `SmokeRun`. No game logic.
- `futtoboru-core`, package `com.rndmodgames.futtoboru`:
  - `game/` the libGDX `Futtoboru` application and `GameSession`; `screens/` top-level screens (menu, new game, `MainGameScreen`).
  - `engine/` the day loop and its managers (`season`, `temporal`, `cup`, `contracts`, `finances`, `injuries`,
    `committee`, `jobs`, `recruitment`, `era`, `messages`, `report`, `team`, `simulation`, `random`).
  - `data/` the model (Club, Player, Person, Match, Competition, Message...), saved in the game.
  - `system/` `DatabaseLoader`, `SaveGame`, `system/data` (`GameData`, `IdAllocator`), `system/save`, `system/generators`.
  - `menu/` in-game navigation; `tables/` one VisUI table per in-game screen; `dialogs/`, `ui/` (theme, scale, dialogs, gfx).
  - `match/ecs/` the event/possession ECS engine (D5; WORKSTATION's lane); `match/engine/` the old 2D prototype (viewer only).
  - `history/` history screens' data (the only main code allowed to read real results, D17).
  - `com.rndmodgames.localization` (`LanguageModLoader`, `GameFormat`); `com.rndmodgames.components`, `darkblade` (shared UI bits).

## Application, session, data (RF-01)
`Futtoboru` owns one root `RandomSource`, one `GameSession` (`getSession()`) and the database. `GameSession` gives the
screens the current `SaveGame` (`getGame()`), `database()` (`DatabaseLoader`), `navigation()` (`ScreenNavigation`) and
`progress()` (`UnsavedProgress`). Screens never reach static state; engine code still calls `DatabaseLoader.getInstance()`.
`DatabaseLoader` builds the world (clubs, people, competitions) from `GameData`, seeded by `random().fork("database")`.
`SaveGame` is the whole game state (date, clubs, messages, seed, ids allocator).

## Game data
`GameData` loads the packs listed in `gamedata/index.txt` (today `core/`: clubs, people, competitions, `seasons/<id>/`,
`era/`, `recruitment/`, `text/`...). Ids: stable string ids, renames via `aliases.json`, runtime numbers from
`tools/data/assign_ids.py` into append-only `ids.json`, a source per record (see [DATA_MODEL.md](DATA_MODEL.md)).

## The day loop: `engine/FuttoboruGameEngine.continueGame()`
`nextAction()` decides the Continue button (draw message, match preview, continue). One `continueGame()` is one day:
1. `MessageManager.deliverScheduledMessages(today)`; stop if a mandatory draw letter is unread.
2. `simulateMatchesForDate(today)`: every match of the day through `MatchResultRecorder.play`.
3. Date + 1 day, then in order: `EraEventScheduler.announce`, `InjuryManager.recoverDaily`, `ScriptsManager.checkGameScripts`,
   `AuthorityManager.checkCompetitionsSchedule`, `CommitteeManager.daily`, `JobManager.updateJobOpenings`,
   month reset (1st), `ContractManager.afterDay`, `Recruitment.afterDay`, `deliverScheduledMessages(newDate)`.
4. Daily attributes, weekly snapshots, weekly expenses; `FriendlyArranger.arrangeAiFriendlies`; per club
   `CompetitionScheduler` proposed/scheduled matches; `Autosave.afterDay`; `MainMenuManager.updateDynamicComponents()` (null in tests).
Add a daily system as a static `X.daily/afterDay(game, date, random)` call at the right place here, not inside a screen.

## Match path today
`MatchSimulator` (`engine/simulation`): `TeamSheets.kickOff` freezes both sheets (G-01), the `MatchEngine` from
`MatchEngines` gives score and scorers (`InstantMatchEngine` default; `-Dfuttoboru.match.engine=ecs` uses
`EcsMatchEngineAdapter` over `match/ecs/EcsMatchEngine`), then goal minutes and gate. `MatchResultRecorder.play/record`
does everything that follows (tables, money, injuries, letters). The ECS internals and viewer (`match/ecs/view`) are
WORKSTATION's lane: design in [design/MATCH_ENGINE_ECS.md](design/MATCH_ENGINE_ECS.md); stay out of `match/ecs/**` unless claimed.

## Screens and UI
- `menu/MainMenuManager` shows in-game screens (`openMenuEntry(GameScreenId)`); `GameScreenId` numbers never change
  (saved in `Message.actionScreen`). A new menu button = one entry in `MenuEntries` (+ `DRILL_DOWNS` for sub-pages).
- `ui/UiTheme` tokens from `ui/theme.json` (no colour/size literals); `ui/UiScale` (`new Stage(UiScale.newViewport())`,
  `updateStageViewport` in `resize`); `ui/UiDialogs` (`confirm`/`info`/`show`, Esc = cancel). UI rules: [design/INTERFACE_GUIDE.md](design/INTERFACE_GUIDE.md).
- Text: `LanguageModLoader.getValue("key")`, key in all four `resources/mods/language/BaseBundle_{en_us,en_uk,sp_sp,sp_la}.properties`.
  Dates and numbers through `GameFormat`.

## Saves
`system/save/SaveCodec.encode/decode` (JSON, `SAVE_VERSION = 3`). Format change: raise `SAVE_VERSION`, add the step in
`SaveMigrations.migrate`, keep old saves in `src/test/resources/saves/` loading. A new optional field needs no bump.
`Autosave` (1st of the month, season rollover), `SaveSlots`. v0.4 saves (no version) are not loaded (D15).

## Randomness
Never `new Random()`/`Math.random()`. Fork the root: `game.random().fork("match:" + id)`, or `Futtoboru.randomOf(game)`.
Stream names contain what makes the draw unique (club, day). Same seed = same game; saves store the seed.

## Tests and tiers (details in [BUILD.md](BUILD.md))
- Before a merge (D35) a lane runs only the affected tests (`tools/test-changed.sh` / `.ps1`) plus the fast-tier level
  gate (`python tools/test_level.py --run`, Maven under `tools/suite_lock.py`). Never slow/soak/bot seasons on the laptop.
- Tags: untagged = fast tier (`./mvnw test`); `@Tag("slow")` (`-Pslow`/`-Pfull`) and `@Tag("soak")` (`-Psoak`) run on
  Jenkins after each develop push and nightly. A test over ~2 s is slow.
- Autotests (`src/test/.../autotest`): `PlayerBot` plays as the human through the screens' calls, `GameInvariants`
  checked daily; `GameplayShortTests`/`GameplayMidTests` (slow), `GameplayLongTests` (soak).
- Seeded tests pin numbers: a change in draw order can move them; re-pin only on purpose and say so.

## Guard tests that fail the build
| Guard | Fails on |
|---|---|
| `ui/UiThemeTest` | colour/size literals in UI code; unknown theme tokens |
| `ui/LanguageBundleKeysTest` | a missing bundle key; hard-coded label/button/message text |
| `ui/GameFormatGuardTest` | locale formatting (`String.format`, `DateTimeFormatter.ofPattern`...) outside `GameFormat` |
| `system/LoggingStyleGuardTest` | `System.out/err`, `printStackTrace`, `Gdx.app.log` (use `Log`) |
| `match/ecs/JdkRandomnessGuardTest` | JDK RNGs (`SplittableRandom`, `ThreadLocalRandom`), `nextGaussian` |
| `match/ecs/EcsDeterminismGuardTest` | Gdx, clocks, hash order, `Math` transcendentals, statics in `match/ecs` |
| `history/ReferenceDataGuardTest` | main code outside `history/` reading real results (D17) |

## How to add a feature
1. Claim it in [BOARD.md](BOARD.md); branch `b3/<item>` or `ws/<item>` from `origin/develop` (own worktree).
2. Data first: JSON in `gamedata/core/` with sources, `assign_ids.py`, `validate.py`, `pytest tools/data`.
3. Model: fields in `data/` (optional, so old saves load) or a migration step if not.
4. Logic: a class in the right `engine/<area>/`, hooked into `continueGame()` or `MatchResultRecorder`; randomness by `fork`.
5. Letters: `MessageManager`, texts as bundle keys in all four bundles.
6. Screen: a table in `tables/<area>/`, a `GameScreenId` + `MenuEntries` entry, `UiTheme` tokens, `UiScale`.
7. Tests: unit tests in the same package (fast), a seeded season test if it runs daily (`@Tag("slow")`); `PlayerBot` step if the human uses it.
8. Merge `origin/develop`, affected tests + `test_level.py --run` green, merge to develop, DONE line in `docs/log/<MACHINE>.md`, `STATUS.md`.

## Where things live
| Concern | Class / file |
|---|---|
| Day advance | `engine/FuttoboruGameEngine` (`continueGame`, `nextAction`) |
| Fixtures, competitions | `engine/AuthorityManager`, `engine/ScriptsManager`, `engine/temporal/CompetitionScheduler`, `engine/cup/` |
| League tables | `engine/simulation/LeagueStandingsManager` |
| Friendlies | `engine/temporal/FriendlyArranger` |
| Team selection | `engine/team/TeamSheets`, `TeamSelector` |
| Match result | `engine/simulation/MatchSimulator`, `MatchResultRecorder`, `InstantMatchEngine` |
| Match report | `engine/report/MatchReport` |
| Era rules and events | `engine/era/`, `gamedata/core/era/` |
| Contracts, wages | `engine/contracts/ContractManager`, `seasons/<id>/contracts.json` |
| Money | `engine/finances/`, `seasons/<id>/economy.json` |
| Committee, jobs, staff | `engine/committee/CommitteeManager`, `engine/jobs/JobManager`, `ClubStaffManager` |
| Recruitment | `engine/recruitment/Recruitment`, `CandidatePools`, `gamedata/core/recruitment/` |
| Inbox letters | `engine/messages/MessageManager`, `data/Message` |
| World build | `system/DatabaseLoader`, `system/data/GameData`, `system/generators/` |
| Save / load | `system/save/SaveCodec`, `SaveMigrations`, `Autosave`, `SaveSlots` |
| Random | `engine/random/RandomSource`, `SplittableRandomSource` |
| Navigation | `menu/MainMenuManager`, `GameScreenId`, `MenuEntries`, `ScreenNavigation` |
| Theme, layout | `ui/UiTheme` + `resources/ui/theme.json`, `ui/UiScale`, `ui/UiDialogs` |
| Language | `localization/LanguageModLoader`, `GameFormat`, `resources/mods/language/` |
| Logging | `system/Log` (categories like `Log.ENGINE_GAME`) |
