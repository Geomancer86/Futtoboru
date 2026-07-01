# Player Actions — Treatment & Insurance

**Futtoboru player guide — draft**  
**Status:** Draft (synced with B5.2 Complete)  
**Build:** `analyze-ai-branch` · 2026-07-01  

**Authoritative behaviour:** [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](../planning/IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10

---

## Overview

When a player is injured, open their **player detail** screen from the squad list. If you manage the club, you can arrange treatment or take out injury insurance **before** the next knock.

This matches Championship Manager 93’s injury economy — pay for recovery, insure against wage loss, or send seriously injured players to specialist care.

---

## Rehabilitate

Available for **minor** and **moderate** injuries only.

| Severity | Effect | Typical cost (1888) |
|----------|--------|-------------------|
| Minor | About 1 week off return date | £25 |
| Moderate | About 2 weeks off return date | £75 |

- Cost is deducted from club balance.  
- If the player has **injury insurance**, rehabilitation is **free**.  
- Not available for serious injuries (use specialist options below).

---

## Injury insurance

**Insure player** — available when the player is **fit** and has a weekly wage.

- Premium is about **5% of weekly wage**, charged each week while the policy is active.  
- While **injured and insured**, the club does **not** pay their wages (the policy covers them).  
- **Cancel insurance** clears the policy; there is no refund.

Amateur players on £0/week cannot be insured.

---

## Lilleshall

For **serious** injuries only, once per injury.

Send the player to the Lilleshall National Rehabilitation Centre. Cost is about **£300** (1888 money). Recovery time is shortened (roughly 40% of remaining weeks). Insurance pays the fee if the player is insured.

---

## Surgeon abroad

For **serious** injuries when the club is **financially strong** (balance at least about **£500**).

- Specialist surgery abroad costs about **£500**.  
- On success, remaining recovery is shortened further.  
- There is a small chance the operation does not improve recovery (CM93-style risk).  
- One attempt per injury. Insurance covers the fee if active.

---

## Club physio

On the **club detail** screen, your physio’s **ability** (Fair → Superb) affects how many weeks the physio estimates when an injury occurs. Better physios mean shorter estimates. If no physio is hired, a default club standard applies.

---

## Inbox

When one of your players suffers a **moderate or serious** injury in a match, an **Injury news** message appears in your inbox with the injury name and physio estimate.

---

*Screenshots: add after human B-9 — `screenshots/player-detail-treatment.png`*
