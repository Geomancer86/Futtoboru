# PM Report — Historical Mode & Agency PO Lock

**Document type:** PM report / PO decision record  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, all department leads  
**Canonical design:** [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md)  
**PO brief:** [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md)

---

## 1. Executive summary

The PO has **locked core product rules** for how Futtoboru balances **historical structure** vs **player-driven divergence**, how **agency** works across leagues, and how **news travels** in the 1888 era.

| Decision | PO lock |
|----------|---------|
| **Default new-game mode** | **Sandbox first · historical second** (`SANDBOX_FIRST`) — forks can change history |
| **Second setting** | **Historical first · not sandbox** (`HISTORICAL_FIRST`) — IRL on all forks |
| **Americas content** | **Included** — Argentina fork stays on roadmap with R-H12 pack |
| **Agency scope** | **All leagues** — not Argentina-only; shared tier engine |
| **Remote agency** | **Inbox / journalist** prompts with **realistic low weights** |
| **News speed** | **Latency by era + distance** — no instant global WORLD news |

**Impact:** Layer C / V2 content and inbox systems gain **three new engineering tracks**: fork resolution (**FLEX-***), agency tiers (**AGENCY-1**), news latency (**NEWS-8**). Layer A/B unchanged for immediate sprint.

---

## 2. What the PO decided (plain language)

### 2.1 Two settings, like Hearts of Iron

The game **supports both** play styles; the player picks at **new career** (immutable after):

| Order in UI | Setting | Meaning |
|-------------|---------|---------|
| **1 — Default** | **Alternate history** (sandbox first) | Club votes, board talks, and journalist comments **can** change structural history at fork events |
| **2** | **Historical accuracy** (history first) | Forks always follow **real-world outcome**; player sees flavour but cannot change structure |

This inverts the earlier doc draft that treated “historical first” as the default product posture. **Implementation default in code and new-game UI = `SANDBOX_FIRST`.**

### 2.2 Americas stay in scope

Question 2 in the Argentina brief (“include fork in first Americas pack?”) confused the PO wording — **intent is clear: keep Americas.** Argentina 1891–93 hiatus/restart (and fork vote) remains the **first Americas milestone** alongside V2 British Isles research.

### 2.3 Agency everywhere, weighted by distance

| Where you manage | How you participate on a fork | Power |
|------------------|------------------------------|-------|
| **Same league / country** | Club board / internal UI | High — can swing your club’s vote |
| **Same country, other league** | Inbox FA / committee letter | Medium |
| **Far away** (e.g. England during Argentina vote) | Inbox **journalist** question | **Low** — comment for the press; rarely decisive alone |

Design must keep weights **honest**: a Preston manager should not “win” an Argentine association vote from London.

### 2.4 News arrives when it could have arrived

Steamship mail and limited telegraph mean:

- Scottish news in England: **days**  
- Argentine news in England: **weeks** in 1888–1900  
- Remote agency prompts may arrive **after** local voting started → further reduced weight or expired choice  

---

## 3. Department impact

### 3.1 World & Data

| Work | ID | Notes |
|------|-----|-------|
| `HistoricalMode` on save + new-game | FLEX-1 | Default `SANDBOX_FIRST` |
| Fork script type 1008 | FLEX-2 | Branch outcomes per fork id |
| Argentina pack + alternate 1892 branch | FLEX-5, R-H12, R-H12-alt | Americas **in scope** |
| `news_latency.txt` | NEWS-8 | Era bands, region pairs |
| Agency weight table | AGENCY-1 | Shared by ARG, FL re-election, etc. |

### 3.2 Platform / UX

| Work | Notes |
|------|-------|
| New-game rules screen | Two-option HoI-style control; default highlighted |
| Inbox fork flow | Mandatory local; delayed optional journalist |
| Club internal screen stub | Layer B — board stance |
| WORLD filter + dates | Show **received** date vs **event** date where latency applied |

### 3.3 Seasons & Rules

| Work | Notes |
|------|-------|
| `ClubVoteResolution` | Tier tokens + AI club votes |
| `NewsLatencyResolver` | Shift `Message.scheduledDate` |
| `WorldEvolutionManager` | Apply winning branch only |

### 3.4 Research

| Work | Notes |
|------|-------|
| R-NEWS-2 | Argentina narrative — both outcomes |
| R-NEWS-4 *(new)* | Latency table sources per region pair |
| R-H12 | Americas pack — **confirmed in scope** |

### 3.5 QA

| Test | ID |
|------|-----|
| Historical first → Argentina 1892 hiatus always | FLEX-6a |
| Sandbox first + local board continue → 1892 active | FLEX-6b |
| Remote journalist token ≤ 15% swing on scripted case | AGENCY-1 IT |
| England career receives Argentine WORLD news ≥ 14 days after event | NEWS-8 IT |

---

## 4. Sequencing (unchanged critical path, new parallel tracks)

```
Layer A (UI bugs, QA)
    ↓
Layer B (XI, tactics, sim v2, attendance)
    ↓
WE-1 WorldEvolutionManager
    ├── NEWS-2 + NEWS-8 (news + latency)
    ├── FLEX-1–4 (mode + forks + agency UI stub)
    └── FLEX-5 Argentina + R-H12 (Americas)
    ↓
1892 England pack (membership PO still open: A/B/C/D)
    ↓
V2 Scotland (R-H4b) — same fork/latency engine
```

**Do not block Layer A/B** on FLEX-* — design is locked; implementation follows WE-1.

---

## 5. Still open (PO)

| # | Topic | Doc |
|---|-------|-----|
| 1 | England 1892 Div 1/Div 2 membership model | [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) |
| 2 | Alternate Argentina 1892 content depth (minimal vs full) | Argentina brief §8 row 4 |
| 3 | Show “you lost the vote” when IRL wins in sandbox mode | Recommended **yes** — not explicitly answered |
| 4 | V2 first country after England infra | Scotland vs Ireland — [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) |

---

## 6. Risks & mitigations

| Risk | Mitigation |
|------|------------|
| Sandbox default surprises history-focused players | Clear UI labels; tooltips; second setting one click away |
| Latency frustrates players waiting for news | Show “expected arrival” on world map stub; local news instant |
| Journalist tier feels pointless | Copy must say **press influence**; occasional swing on tight votes |
| Double content for every fork | Author **minimal alt branch** first (Argentina); full alt optional |

---

## 7. Documentation & git

| Artifact | Status |
|----------|--------|
| [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) | Updated §4.6, §11, H-8–H-10 |
| [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md) | §8 locked |
| [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) | H-6–H-10 cross-ref |
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | PM report linked |
| Local git commit | This report + doc updates — **no remote push** |

---

## 8. PO confirmation checklist

- [x] Default = **Sandbox first** (alternate history)  
- [x] Second = **Historical first** (no sandbox forks)  
- [x] **Americas included** on roadmap  
- [x] Agency on **all leagues** with distance weights  
- [x] **Journalist inbox** for remote tier  
- [x] **News latency** by era and distance  

---

*PM sign-off: decisions recorded; engineering may proceed on FLEX-1/NEWS-8/AGENCY-1 design specs. Implementation remains gated behind WE-1 and Layer B inbox UX capacity.*
