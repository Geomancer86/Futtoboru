# Status (update in place, don't add new summary files)

_Last updated 2026-11-27 (WORKSTATION)._ Version 0.4.0-SNAPSHOT.

| System | Status | Notes |
|---|---|---|
| Day advance / game flow | working | Continue double-click fixed (P2-1) |
| Inbox / messages | working | `MessageManager` |
| League fixtures + standings | working | one result path `MatchResultRecorder` (P2-2); table counts league matches only; champion prize £500 / runner-up £250 in `AuthorityManager.LEAGUE_*_PRIZE`; Golden Boot per season (P2-3). Generated schedule runs Apr 1888-Jan 1889 |
| FA Cup | working | completion, runner-up, replays; final = the bracket's last tie (P2-7); brackets of any size with byes, next edition opens in May (P2-3); tests in engine/cup, engine/temporal, `CupFinalAndPrizeTests` |
| Match simulation | crude | random scoreline; replaced in Phase 3 |
| Unemployed / job system | mostly working | offer and refresh bugs |
| Finances | partial | weekly expenses, gate income; money text in £ via `Money.format` (P2-7) |
| Contracts | partial | generated only; no expiry or transfers |
| New season | partial | generated when the league completes; no re-election vote yet |
| Save / load | working | v1 format (P2-5): `saveVersion` + data version, named saves, data saved by id, one object per club after load, round-trip test; v0.4 saves show "older version" |
| Game data | working | JSON packs in `gamedata/` with stable string ids, strict loader + `tools/data/validate.py` (P2-4). Data still unverified: review list `docs/data/REVIEW_1888-89.md` (copied cup rosters) |
| Runtime ids | working | one saved `IdAllocator`; data numbers from the append-only `gamedata/core/ids.json` (never shift when data is corrected) |
| Competition schedule | fixed | daily check runs on the current game (P2-8); full-season proof comes with the P2-3 test |
| New season table | working | table reset when the season completes; final table kept on the edition (`CompetitionEdition.finalStandings`, P2-6), no screen shows it yet |
| Tactics, Training, Transfers, Scouting, Staff, Board | stub | empty screens |
| Logging | working | one style (P1-5): `Log.info/debug/warn/error(Log.<CATEGORY>[, tag], msg[, e])`; info/debug/warn follow Settings > Debug Logging, errors always print with stack trace; `LoggingStyleGuardTest` blocks `System.out/err`, `printStackTrace`, `Gdx.app.log` in main code |

Full season: `engine/FullSeasonTests` plays 1888-89 headless and seeded to the 1889-90 rollover (P2-3, ~11 s, in `verify`).

Build: `./mvnw -B verify` green (Maven 3.9.12 via wrapper), 47 core + 1 desktop tests (2026-11-27). Dead 3D demo code removed (P1-4). Match engine v2 design: `docs/design/MATCH_ENGINE_V2.md`.
