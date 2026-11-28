# Status (update in place, don't add new summary files)

_Last updated 2026-11-28 (BOOK3PRO)._ Version 0.5.0-SNAPSHOT.

| System | Status | Notes |
|---|---|---|
| Day advance / game flow | working | Continue double-click fixed (P2-1) |
| Inbox / messages | working | `MessageManager` |
| League fixtures + standings | working | one result path `MatchResultRecorder` (P2-2); table counts league matches only; champion prize £500 / runner-up £250 in `AuthorityManager.LEAGUE_*_PRIZE`; Golden Boot per season (P2-3). Generated schedule runs Apr 1888-Jan 1889 |
| FA Cup | working | completion, runner-up, replays; final = the bracket's last tie (P2-7); brackets of any size with byes, next edition opens in May (P2-3); one edition per season: the creation scripts open the season.json edition (P2-9); tests in engine/cup, engine/temporal, `CupFinalAndPrizeTests` |
| Match simulation | crude | random scoreline; replaced in Phase 3 |
| Unemployed / job system | mostly working | offer and refresh bugs |
| Friendlies / Schedule | working | U-08: Schedule screen = 12-week calendar (played with score, fixed, awaiting answer) + arrange form (day from the calendar, ground, opponent). `engine/temporal/FriendlyArranger`: at least 2 days' notice; the other club answers by inbox letter the next day (accept, or decline with a reason: engaged, no side, travel cost, ground let; 80% accept when free, `fork("friendly:<id>")`); played via `MatchResultRecorder` (no table change); home club takes the gate; a friendly is called off when a league/cup match is fixed for that day. No AI-to-AI friendlies yet |
| Finances | partial | weekly expenses, gate income; money text in £ via `Money.format` (P2-7) |
| Contracts | partial | generated only; no expiry or transfers |
| New season | partial | generated when the league completes; no re-election vote yet |
| Save / load | working | v2 format (E-01 adds player position + dribbling; v1 saves migrate), v1 (P2-5): `saveVersion` + data version, named saves, data saved by id, one object per club after load, round-trip test; v0.4 saves show "older version"; a New Game after playing/loading another starts from freshly rebuilt data with the same ids (P2-10) and a fresh seed unless `-Dfuttoboru.seed` forces one (S-01, D25); autosave slot on the 1st of each month and at season rollover, listed as "Autosave"; U-06 dialogs: Delete per save (autosave too) with confirm, "Overwrite?" before writing over an existing name, empty names refused; quitting/closing waits for a running autosave (max 5 s) |
| Game data | working | JSON packs in `gamedata/` with stable string ids, strict loader + `tools/data/validate.py` (P2-4). E-01: every 1888-89 squad place has a 2-3-5 `position` (117 sourced, 47 line-only, 384 placeholder) and `dribbling` (2 sourced, rest derived); shown on the player detail screen; saves v2. E-01b: dribbling drifts and ages like the other attributes and is in the weekly snapshots; any squad player without a position (generated, old save, after the rollover) gets the 2-3-5 slot his squad is shortest of (`PositionAssigner`). Data still unverified: review list `docs/data/REVIEW_1888-89.md` (copied cup rosters) |
| Ground pictures | assets only | 22 code-generated 320x180 pictures + thumbnails in `resources/art/grounds/` (`tools/art`, H-02); not shown on any screen yet; research `docs/data/history/GROUNDS_1888-89.md`, field proposal `docs/design/GROUNDS_DATA_PROPOSAL.md` |
| Runtime ids | working | one saved `IdAllocator`; data numbers from the append-only `gamedata/core/ids.json` (never shift when data is corrected) |
| Competition schedule | fixed | daily check runs on the current game (P2-8); full-season proof comes with the P2-3 test |
| New season table | working | table reset when the season completes; final table kept on the edition (`CompetitionEdition.finalStandings`, P2-6), no screen shows it yet |
| Tactics, Training, Transfers, Scouting, Staff, Board | stub | empty screens |
| Settings | working | sound panel and the empty gameplay panel hidden until there is audio / a setting (U-08) |
| UI scale / window | working | U-03a: `ui/UiScale` on every stage (Settings: Auto (shows %) / 100% / 200%, integer steps; Auto = OS content scale rounded to 1-2, else 2x from a 1920x1080 window); window default and minimum 1280x720 from the saved preference (launcher, no double resize); screen roots scroll when they do not fit |
| Logging | working | one style (P1-5): `Log.info/debug/warn/error(Log.<CATEGORY>[, tag], msg[, e])`; info/debug/warn follow Settings > Debug Logging, errors always print with stack trace; `LoggingStyleGuardTest` blocks `System.out/err`, `printStackTrace`, `Gdx.app.log` in main code |

Full season: `engine/FullSeasonTests` plays 1888-89 headless and seeded to the 1889-90 rollover (P2-3, ~11 s, in `verify`).

Build: `./mvnw -B verify` green (Maven 3.9.12 via wrapper), 279 core + 1 desktop tests (2026-11-28). Dead 3D demo code removed (P1-4). Match engine v2 design: `docs/design/MATCH_ENGINE_V2.md`.
