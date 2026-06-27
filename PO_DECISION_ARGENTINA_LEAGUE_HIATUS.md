# PO Decision Brief — Argentina League Hiatus (1891–93)

**Document type:** PO decision brief  
**Date:** 2026-06-27  
**Status:** **Partially locked** — see §8  
**Context:** [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md §11](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) · [PM_REPORT_HISTORICAL_MODE_PO_LOCK.md](PM_REPORT_HISTORICAL_MODE_PO_LOCK.md) · [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) · [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md)

---

## 1. What you are deciding

Argentina is the **first designed fork event**: real history had **one league season (1891)**, **no championship in 1892**, then a **restarted Primera under AFA in 1893**.

The engine must support **both**:

| Path | 1892 | 1893 |
|------|------|------|
| **Historical (IRL)** | Hiatus — association dissolved | AFA restart |
| **Alternate (player-influenced)** | League continues (clubs vote to stay) | May differ — same body, new body, or delayed reform |

Hiatus modelling is **locked**. Fork resolution depends on **Historical Mode** (§6).

---

## 2. Real history (reference)

| Year | IRL |
|------|-----|
| **1891** | AAFL / first Argentine league — five clubs, one championship |
| **1892** | **No official league** — prior association failed to sustain |
| **1893** | **AFA** formed — Primera División restarts |

Sources: [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) · **R-H12** · **R-NEWS-2** narrative pack.

---

## 3. Hearts of Iron analogue (PO language)

| HoI concept | Futtoboru equivalent |
|-------------|----------------------|
| Game rules at start | **Historical Mode** — two settings (§6) |
| Decisions change history | **Fork events** — club votes, board, **journalist inbox** |
| Historical AI focus | **`HISTORICAL_FIRST`** — IRL branch always |
| Less rigid history | **`SANDBOX_FIRST`** (default) — votes can diverge |

---

## 4. The fork (what happens in-game)

### 4.1 Trigger window

End of **1891** season (or association annual meeting date in research pack):

- **Local tier:** mandatory inbox + club board  
- **Remote tier:** journalist inbox (after **news latency**) — low weight  
- **WORLD news:** structural outcome for all careers (delayed by distance §4.6 design doc)

### 4.2 Player agency (all leagues — PO locked)

See [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md §11.4](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) for full tier table.

| Tier | Argentina fork example |
|------|------------------------|
| **Local** | Manager at Argentine club → board stance |
| **Remote** | Manager at Preston → inbox journalist quote — **~5–15% weight** |

Same engine applies to **England 1892 re-election** and future forks.

### 4.3 Resolution outcomes

| Outcome | Engine state | News |
|---------|--------------|------|
| **A — Historical** | **HIATUS** 1892; **RESTARTED** 1893 (AFA) | “Championship not held… association dissolved” |
| **B — Continuation** | **ACTIVE** 1892 edition | “Clubs vote to continue championship” |

**PO scope for first ship:** **A** and **B** only.

---

## 5. Historical Mode options — PO locked 2026-06-27

| UI order | Enum | Label (draft) | Behaviour |
|----------|------|-----------------|-----------|
| **1 — DEFAULT** | `SANDBOX_FIRST` | Alternate history · **sandbox first**, historical fallback | Forks active; agency can diverge |
| **2** | `HISTORICAL_FIRST` | Historical accuracy · **history first**, no sandbox forks | IRL branch only |

Stored on `SaveGame` at creation — **no mid-career change**.

---

## 6. New-game settings (locked)

| Setting | Values | PO lock |
|---------|--------|---------|
| **Historical Mode** | `SANDBOX_FIRST` (default) · `HISTORICAL_FIRST` | **Locked** |
| **Americas / Argentina fork** | In roadmap with R-H12 | **Yes — included** |
| **Agency** | All leagues; journalist remote tier | **Locked** |
| **News latency** | Era + distance table | **Locked** — see design doc §4.6 |

---

## 7. Relationship to England 1892

| Event | Fork? | Doc |
|-------|-------|-----|
| **1892 Div 1/Div 2 split** | Optional | [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) |
| **Argentina 1891–93 hiatus** | **First fork template** | This brief |

**Shared engine:** `ForkEvent` + `ClubVoteResolution` + agency tiers + `NewsLatencyResolver`.

---

## 8. PO decision table

| # | Decision | PO choice | Status |
|---|----------|-----------|--------|
| 1 | Default **Historical Mode** | **`SANDBOX_FIRST`** (sandbox first, historical second) | **Locked** |
| 2 | Argentina fork in **Americas pack**? | **Yes — keep Americas on roadmap** | **Locked** |
| 3 | Player influence | **All leagues** — local board + **inbox journalist** remote; **distance weights** | **Locked** |
| 4 | Alternate **1892 continuation** content depth | Minimal / full | **Open** |
| 5 | Show “you lost the vote” in sandbox mode? | Yes (recommended) | **Open** |

---

## 9. After PO locks

Unlock engineering: **FLEX-1**, **FLEX-5**, **NEWS-7**, **NEWS-8**, **AGENCY-1** — see [PM_REPORT_HISTORICAL_MODE_PO_LOCK.md](PM_REPORT_HISTORICAL_MODE_PO_LOCK.md).

---

*Parallel to England 1892 membership brief — shared fork and agency engine.*
