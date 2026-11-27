# Status (update in place, don't add new summary files)

_Last updated 2026-11-27 (WORKSTATION)._ Version 0.4.0-SNAPSHOT.

| System | Status | Notes |
|---|---|---|
| Day advance / game flow | working | Continue double-click advances two days (P2-1) |
| Inbox / messages | working | `MessageManager` |
| League fixtures + standings | working | champion prize hard-coded £500 |
| FA Cup | working | completion, runner-up, replays; tests in engine/cup and engine/temporal |
| Match simulation | crude | random scoreline; replaced in Phase 3 |
| Unemployed / job system | mostly working | offer and refresh bugs |
| Finances | partial | weekly expenses, gate income |
| Contracts | partial | generated only; no expiry or transfers |
| New season | partial | generated when the league completes; no re-election vote yet |
| Save / load | partial | single slot |
| Tactics, Training, Transfers, Scouting, Staff, Board | stub | empty screens |

Build: `mvn -B verify` green on Maven 3.9.12 / JDK 21 (2026-11-27).
