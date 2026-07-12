# Player Actions — Trainer Care (1888)

**Futtoboru player guide — R-1888 slice**  
**Status:** Draft (Victorian ME-1)  
**Build:** `analyze-ai-branch` · 2026-07-12  

**Authoritative behaviour:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](../planning/DESIGN_INJURY_HISTORICAL_ERA_GATING.md) · [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10

---

## Overview

When a player is injured, open their **player detail** screen from the squad list. If you manage the club, you can ask your **Trainer** to speed recovery for **minor** and **moderate** injuries.

**Futtoboru 1888 does not include** injury insurance, Lilleshall, or surgeon abroad — those arrive in later historical releases (1906+ and 1951+). The buttons for those options are hidden in this build.

---

## Rehabilitate (Trainer)

Available for **minor** and **moderate** injuries only.

| Severity | Effect | Cost (1888) |
|----------|--------|-------------|
| Minor | About 1 week off return date | **£0.25** (3 shillings) |
| Moderate | About 1 week off return date | **£0.75** (15 shillings) |

- Cost is deducted from club balance.  
- You need enough cash in the bank; if funds are insufficient, rehabilitation is refused.  
- Not available for **serious** injuries — the player must rest until the return date.

---

## What is not in Futtoboru 1888

| Action | When it arrives | Why |
|--------|-----------------|-----|
| Injury insurance | R-1906+ | Not invented for Victorian clubs |
| Lilleshall rehab centre | R-1951+ | Modern national facility |
| Surgeon abroad | R-1993+ | CM93-era specialist care |

These remain in the long roadmap and test suite for later releases; they are **gated off** in 1888 saves.

---

## Club Trainer

On the **club detail** screen, your **Trainer** (not a modern physio) affects injury estimates when knocks occur. Better staff mean shorter estimated lay-offs. If no trainer is listed, a default club standard applies.

---

## Inbox

When one of your players suffers a **moderate or serious** injury in a match, an **Injury news** message appears in your inbox with the injury name and Trainer estimate.

---

## Save and load

Use **Save Game** from the main menu to write a named save file. **Load Game** lists your saves by name. Saves are stored under your user Futtoboru folder as JSON files.

---

*Screenshots: add after human B-9 — `screenshots/player-detail-trainer.png`*
