# Squad, Fitness & Injuries

**Futtoboru player guide — R-1888 slice**  
**Status:** Draft (Victorian ME-1)  
**Build:** `analyze-ai-branch` · 2026-07-12  

**Authoritative behaviour:** [DESIGN_INJURY_HISTORICAL_ERA_GATING.md](../planning/DESIGN_INJURY_HISTORICAL_ERA_GATING.md) · [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §9–§10

---

## Overview

Your squad screen tracks how ready each player is to play. **Fitness**, **sharpness**, and **morale** change after matches and rest days. **Injuries** can occur after a match and keep a player out until the return date shown.

In **Futtoboru 1888**, injury care is **Victorian-era only**: your club **Trainer** gives estimates and you can pay small shilling amounts for basic rehabilitation. Modern insurance and specialist hospitals are **not** available in this release.

For **Trainer treatment** actions, see [07-player-actions-insurance.md](07-player-actions-insurance.md).

---

## Squad screen columns

| Column | Meaning |
|--------|---------|
| **XI** | Player is in your saved starting eleven |
| **Fit** | Fitness 0–100. Drops when playing; rises on rest days |
| **Sharp** | Match sharpness 0–100. Rises when you play; falls when idle |
| **Mor** | Morale 0–100. Affected by results, goals, and ratings |
| **Injury** | Injury name, or **Fit** if available |
| **Return** | Trainer estimate, e.g. `~3 wk`, or **—** |

Sort any column by clicking its header.

---

## Fitness

- Starting a match in your **starting eleven** costs fitness.  
- **Rest days** (no match the previous day) restore fitness.  
- If **fatigue** is high, rest-day recovery is slower.  
- Very **low fitness** (below 60%) warns you on the Tactics screen and makes players less effective in matches.

---

## Sharpness

Players who play regularly build sharpness. Long gaps without matches reduce it. Sharpness affects how well a player performs in the match engine alongside their attributes and fitness.

---

## Morale

After each match, morale moves based on:

- Win or loss  
- Whether the player scored  
- Post-match rating (strong or weak performance)

Morale is shown on the squad screen; it does not yet change transfer or board behaviour.

---

## Injuries

### When they happen

Injuries are rolled **after a match** for players who were in your starting eleven. Risk increases if fitness is low, fatigue is high, or the player is **injury prone** (hidden attribute — you will not see it on the squad screen).

### Types and Trainer estimate

Injuries include knocks such as **Strain**, **Bruise**, **Twisted ankle**, **Knee injury**, and **Serious injury**. Your club **Trainer** affects how many weeks out the estimate shows.

### What you must do

- **Injured players cannot be picked** on the Tactics screen (they do not appear in the player list).  
- If a player was already assigned before getting injured, the slot shows **Injured until** with a date.  
- The **match result** screen lists anyone injured in that game with an approximate week count.  
- **Moderate or serious** injuries on your squad trigger an **inbox** message after the match.

### Treatment (1888)

Open a player from the squad to **Rehabilitate** minor or moderate injuries — see [07-player-actions-insurance.md](07-player-actions-insurance.md). Serious injuries must run their course; there is no Lilleshall or surgeon abroad in this release.

---

## Tactics screen

- Pick a **formation**, then assign players to each slot.  
- Use **Best XI** to auto-fill from your fittest available players.  
- **Injured** players are excluded from selection.  
- **Low fitness** shows a warning next to the slot.

---

## Tips

1. Rotate the squad if fitness drops below 60% before a big match.  
2. Check **Return** estimates when planning your next fixture.  
3. Keep a small reserve in club balance for Trainer rehabilitation fees.  
4. After a heavy defeat, watch morale — it recovers with better results.

---

*Screenshots: add after human B-9 — `screenshots/squad-condition.png`, `screenshots/tactics-injury-warn.png`*
