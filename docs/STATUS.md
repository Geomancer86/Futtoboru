# Status (update in place, don't add new summary files)

_Last updated 2026-11-27 (WORKSTATION)._ Version 0.4.0-SNAPSHOT.

| System | Status | Notes |
|---|---|---|
| Day advance / game flow | working | Continue double-click fixed (P2-1) |
| Inbox / messages | working | `MessageManager` |
| League fixtures + standings | working | champion prize hard-coded £500 |
| FA Cup | working | completion, runner-up, replays; tests in engine/cup and engine/temporal |
| Match simulation | crude | random scoreline; replaced in Phase 3 |
| Unemployed / job system | mostly working | offer and refresh bugs |
| Finances | partial | weekly expenses, gate income |
| Contracts | partial | generated only; no expiry or transfers |
| New season | partial | generated when the league completes; no re-election vote yet |
| Save / load | partial | single slot; load path probably broken; no version (P2-5) |
| Game data | converting | JSON pack + validator ready (P2-4 slice 1); game still reads `mods/*.txt` until slice 2. Review list: `docs/data/REVIEW_1888-89.md` |
| New season table | broken | points are never reset (P2-6) |
| Tactics, Training, Transfers, Scouting, Staff, Board | stub | empty screens |

Build: `./mvnw -B verify` green (Maven 3.9.12 via wrapper), 47 core + 1 desktop tests (2026-11-27). Dead 3D demo code removed (P1-4). Match engine v2 design: `docs/design/MATCH_ENGINE_V2.md`.
