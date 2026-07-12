# Eagle View — Project Status (All Stakeholders)

**Date:** 2026-07-12 · **Audience:** PO (primary) · Sponsor · PM  

**PO reads:** [PO_BRIEF.md](PO_BRIEF.md)  
**Full PM report:** [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md)

---

## One sentence

**Futtoboru 1888 is ~92% ready for human playtesters** — the game loop works and automated tests are green; **eight blockers** (Victorian injury screens + UI + soak + manual) stand between us and inviting people to play.

---

## Where we are

```
████████████████████████████░░░░  ~92% pre-human QA
│ analysis ✓ │ design ✓ │ code ⚠ │ tests ⚠ │ humans ⏸
```

| Done | Not done yet |
|------|----------------|
| League, cup, seasons forever | Victorian injury screens (hide modern insurance) |
| Pick team, tactics, match, ratings | Continue / history / save UI fixes |
| 131 + 5 automated tests green | 60-season soak signed by PM |
| Roadmap & governance for PO | Human playtest (B-9) |
| CM93 injury **engine** (hidden for 1888) | Player manual chapters for 1888 |

---

## Critical path (next ~2 weeks)

1. **Sprint A** — Victorian injuries + Continue fix + tests  
2. **Sprint B** — History / save UI + soak + manual + load check  
3. **Invite human testers**  
4. Polish → then **1892** (second division, reserves, transfers)

---

## Decisions needed (PO)

| # | Question | Rec |
|---|----------|-----|
| 1 | Lock 1888 scope (no transfers, no modern insurance)? | Yes |
| 2 | Testers only after the 8 blockers? | Yes |
| 3 | Next big release = **1892**? | Yes |

→ Answer on [PO_BRIEF.md](PO_BRIEF.md) or in chat.

---

## Horizons

| When | What |
|------|------|
| **Now** | Finish R-1888 blockers → human QA |
| **Next** | R-1888.1 polish |
| **Mid** | R-1892 expansion + transfers |
| **Later** | 1906 insurance → 1993 CM93 menus |
| **Far** | Full CM + FM history |

---

## Traffic lights

| | |
|-|-|
| 🟢 | Loop, league/cup, automated tests, governance |
| 🟡 | Soak unsigned · PO decisions unsigned · branch not on `main` |
| 🔴 | ME-1 injury UX · Continue / history / save UI |
| ⚪ | Human QA · public release |

---

*Detail for PM/Sponsor: [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md)*
