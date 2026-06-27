# PO Decision Brief — Argentina League Hiatus (1891–93)

**Document type:** PO decision brief  
**Date:** 2026-06-27  
**Context:** [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md §11](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) · [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) · [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md)

---

## 1. What you are deciding

Argentina is the **first designed fork event**: real history had **one league season (1891)**, **no championship in 1892**, then a **restarted Primera under AFA in 1893**.

The engine must support **both**:

| Path | 1892 | 1893 |
|------|------|------|
| **Historical (IRL)** | Hiatus — association dissolved | AFA restart |
| **Alternate (player-influenced)** | League continues (clubs vote to stay) | May differ — same body, new body, or delayed reform |

You are **not** deciding whether to model hiatus (that is locked). You are deciding:

1. **Default new-game behaviour** — strict history vs flexible forks  
2. **Who can influence** the Argentina vote (employed manager only? owner role?)  
3. **How much alternate paths may diverge** before we require a new research pack  

---

## 2. Real history (reference)

| Year | IRL |
|------|-----|
| **1891** | AAFL / first Argentine league — five clubs, one championship |
| **1892** | **No official league** — prior association failed to sustain |
| **1893** | **AFA** formed — Primera División restarts |

Sources: [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) · future **R-H12** · **R-NEWS-2** narrative pack.

---

## 3. Hearts of Iron analogue (PO language)

| HoI concept | Futtoboru equivalent |
|-------------|----------------------|
| Game rules: historical vs alternate | **Historical Mode** on new game / save (§11.2 of design doc) |
| National focuses / decisions change history | **Fork events** — club votes, board meetings, association ballots |
| AI tries to follow history unless blocked | **Default outcome = IRL** when player absent or mode **Strict** |
| Player choices in a country matter | **Local agency** — if you work (or own) an Argentine club, you can argue internally and affect your club’s vote |

We are **not** cloning HoI’s war system. We borrow only: **history is the default, divergence is opt-in and gated.**

---

## 4. The fork (what happens in-game)

### 4.1 Trigger window

End of **1891** season (or association annual meeting date in research pack):

- Inbox: **“Clubs called to vote on future of Argentine championship”** (mandatory if player eligible)  
- World news: same event for all careers (England-only players **read** it; Argentine-employed players **participate**)

### 4.2 Player agency (when eligible)

| UI channel | Purpose |
|------------|---------|
| **Inbox** | Meeting notice, vote result, world outcome |
| **Club — internal** | “Discuss with owners / committee” — your stance: **continue league**, **accept pause**, **abstain** |
| **Optional future** | Board request screen (Layer B+ finances) |

Your stance adjusts **your club’s vote weight** and **AI club votes** (rivalry, finances, owner personality — authored defaults).

### 4.3 Resolution outcomes

| Outcome | Engine state | News |
|---------|--------------|------|
| **A — Historical** | `CompetitionLifecycle` → **HIATUS** 1892; **RESTARTED** 1893 (AFA) | “Championship not held… association dissolved” |
| **B — Continuation** | **ACTIVE** 1892 edition under revised rules (same or successor body) | “Clubs vote to continue championship” |
| **C — Split (future)** | Partial participation; some clubs break away | Defer to V3 content |

**PO scope for first ship:** **A** and **B** only.

---

## 5. Options for PO

### Option 1 — **Strict historical default** (HoI “history always wins”)

- New game default: **Historical Mode = Strict**  
- Argentina fork exists in data but **always resolves to hiatus** unless player enables Flexible  
- Player in Argentina gets **flavour messages** only (no vote changes outcome)

| Pros | Cons |
|------|------|
| Simplest to ship | “Your choices matter” is weak for Argentina start |
| Easy to test | Under-sells fork system |

### Option 2 — **Flexible default, IRL-weighted** (recommended for design)

- New game default: **Historical Mode = Flexible**  
- If player **not** in Argentina: AI club vote uses **historical weights** → usually **Outcome A**  
- If player **employed at Argentine club**: internal discussion **can flip** one or more club votes → **Outcome B** possible  
- News always explains **why** (vote tally, dissenting clubs)

| Pros | Cons |
|------|------|
| Same engine for England 1892 re-election and Argentina | Needs vote sim + copy for both outcomes |
| Teaches core fantasy early | Alternate 1892 needs fixture/rules authoring |
| Respects IRL as default path | QA matrix doubles for Argentina pack |

### Option 3 — **Flexible + owner role**

- Same as Option 2, but **club owner start** (future) gets stronger vote leverage than employed manager  
- Manager: persuasion via owners  
- Owner: direct vote + board UI

| Pros | Cons |
|------|------|
| Clear role fantasy | Owner role not in Layer A/B MVP |

---

## 6. New-game settings (proposed)

| Setting | Values | Default (pending PO) |
|---------|--------|----------------------|
| **Historical Mode** | **Strict** · **Flexible** · *(future: Sandbox)* | **Flexible** (Option 2) |
| **Fork events** | On / Off | On when Flexible |
| **Show alternate outcome in codex** | On / Off | On (spoiler-safe: “paths not taken”) |

Stored on `SaveGame` at creation — mid-career change **not** allowed (same as HoI campaign rules).

---

## 7. Relationship to England 1892

| Event | Fork? | Doc |
|-------|-------|-----|
| **1892 Div 1/Div 2 split** | Optional via [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) Options B/C/D | England membership vote |
| **Argentina 1891–93 hiatus** | **First fork template** | This brief |

**Shared engine:** `ForkEvent` + `ClubVoteResolution` + inbox + internal club UI — Argentina proves the pipeline before England re-election gameplay.

---

## 8. PO decision table

| # | Decision | Your choice |
|---|----------|-------------|
| 1 | Default **Historical Mode** for new games | Strict / **Flexible** / defer |
| 2 | Argentina fork in **first playable Americas pack**? | Yes / No / defer |
| 3 | Player influence requires **employed in Argentina**? | Yes / also unemployed observer / owner only |
| 4 | Alternate **1892 continuation** — how much content? | Minimal (12-match loop) / full research / defer |
| 5 | If vote fails (Outcome A), is player agency **still shown** as “you lost the vote”? | Yes (recommended) / hide |

---

## 9. After PO locks

Update [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) §11.5 defaults → unlock **FLEX-5**, **R-H12-alt**, **NEWS-7** (fork result messages).

---

*Parallel to England 1892 membership brief — same “history default, player may diverge” product line.*
