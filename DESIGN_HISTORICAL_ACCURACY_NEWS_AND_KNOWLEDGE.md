# Design — Historical Accuracy, News & World Knowledge

**Document type:** Product / architecture intentions (PO-aligned)  
**Date:** 2026-06-27  
**Status:** **Active** — core design north star  
**Audience:** PO, PM, all departments, Research, Content authors  
**Companion:** [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) · [ANALYSIS_INBOX_SYSTEM_V1.md](ANALYSIS_INBOX_SYSTEM_V1.md) · [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md)

---

## 1. PO intentions (locked)

Futtoboru is a **historical** football management simulation. These are **core product intentions**, not optional polish:

| # | Intention |
|---|-----------|
| **H-1** | The **world follows real history** in structure: leagues, hiatuses, restarts, new countries, rule changes, club founding — not a simplified “every nation has one eternal league.” |
| **H-2** | The **inbox / news layer** is how the player **learns what happened in the world** — especially events outside their club. It is a **core system**, not a debug log. |
| **H-3** | The player **does not know everything on day one**. Players, teams, managers, and staff are **discovered over time** through news, matches, competition, and scouting — aligned with era-appropriate information flow. |
| **H-4** | **Argentina-style gaps are valid:** one season → hiatus → new association → restart must be **modeled and reported**, not smoothed away. |
| **H-5** | **Historical leeway (HoI-style):** real history is the **default**, but selected **fork events** may diverge if game rules and player agency allow — e.g. Argentine clubs **vote** on hiatus vs continuation. |
| **H-6** | **Player agency is local and gated:** starting in Argentina, or taking a job there, unlocks **internal club UI** (owners, committee) that can affect votes — not global god-mode. |
| **H-7** | **Both outcomes must be playable and reported** in inbox/news — whether history matches IRL or the player changed it. |

**Distribution and git:** PO controls when/how builds are shared; project docs do not prescribe releases. **No remote git push** from automation.

---

## 2. Understanding check (for PO)

**Yes — we understand the vision as:**

> The engine holds a **historically accurate evolving world** (including dead years and restarts). The **player experiences that world through news** (inbox / newspaper voice), while **knowledge of people and clubs unlocks gradually** — like a manager in 1888 reading the paper and learning who exists, not like FM 2024 with every player pre-loaded globally.

The **simulation** and the **player’s awareness** are **two layers**:

```
[ Historical world state ]  ← scripts, world evolution, accurate timelines
         ↓ emits
[ News / inbox messages ]   ← what the player is told, when, with what detail
         ↓ reveals
[ Player knowledge graph ]  ← which clubs/people/staff are known, and how well
         ↓ gates
[ UI screens ]              ← squad, comps, transfers, staff (future)
```

---

## 3. Historical accuracy — world state model

### 3.1 Principle

**Accuracy applies to structure and timing**, not forcing every procedural season’s champion to match Wikipedia.

| Accurate (must) | Procedural (OK to diverge) |
|-----------------|------------------------------|
| League **founded / dissolved / hiatus** dates — **in Strict mode** | Your club’s exact league position after year 5 |
| **Default fork outcome** matches IRL when player absent or Strict mode | Simulated scorelines in unimportant matches |
| **New country** league begins (default path) | Cup path if you diverge from IRL draw luck |
| **Competition restart** under new body (default path) | **Fork events:** hiatus vs continuation, re-election, Div 2 split — see §11 |
| Club **founded** and enters save on date | Vote outcomes when **Flexible** mode + player agency |
| Rule changes on documented dates | — |

### 3.2 Competition lifecycle (required engine concept)

Competitions are not permanently `ACTIVE`. States:

| State | Meaning | Example |
|-------|---------|---------|
| **PLANNED** | Authored in mod; not yet in save | Argentine league 1891 pack |
| **ACTIVE** | Editions run | English FL 1888– |
| **HIATUS** | No edition this calendar year; world continues | Argentina **1892** |
| **RESTARTED** | New association or reform; new edition chain | Argentina **1893** (AFA) |
| **ENDED** | Dissolved historically | Short-lived leagues |

**Argentina case (canonical example):**

```
1891  AAFL league — 5 clubs — one championship — ACTIVE
1892  No championship — HIATUS (association dissolved)
1893  AFA formed — Primera restarts — RESTARTED (new competition id or lineage flag)
```

Implementation: timeline scripts + `CompetitionLifecycle` metadata on `Competition` / save — see [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) script types 1005–1007.

**Research pack:** [RESEARCH_WORLD_FOOTBALL_1892_V2.md](RESEARCH_WORLD_FOOTBALL_1892_V2.md) · future **R-H12** Argentina 1893–94.

### 3.3 World evolution + accuracy

All structural changes use **dated incorporation** into `SaveGame`:

- New club, league, country, rule amendment  
- **Hiatus** (competition dormant; news explains why)  
- **Restart** (news announces new body / season)  

Never: silently keep one league running forever because procedural rollover is easier.

---

## 4. News & inbox — core player-facing channel

### 4.1 Role

The **inbox is the newspaper** for the 1888 era (text-first; no separate 3D newspaper required for MVP).

| Function | Inbox responsibility |
|----------|---------------------|
| Report **world events** | League formed, league paused, country starts football, rule change |
| Report **competition events** | Draws, fixture release, champion, re-election |
| Report **club events** | Your club + eventually others via news |
| **Gate** mandatory actions | Draw screens (existing) |
| **Link** to detail | Competitions, match result, person (when known) |

**Existing code:** `MessageManager`, `Message` (category, type, scheduledDate, mandatory), `InboxScreenTable` — extend, do not replace.

### 4.2 Message categories (target taxonomy)

| Category | Use | Era voice |
|----------|-----|-----------|
| **WORLD** | Cross-border / structural history | “Association formed in Buenos Aires…” |
| **LEAGUE** | FL, SFL, Irish League, etc. | Existing types + expansion |
| **CUP** | FA Cup, Scottish Cup, … | Existing |
| **AUTHORITY** | FA, SFA, IFA announcements | Rule changes, re-election |
| **MATCH** | Results, previews | Club-focused |
| **CLUB** | Board, finances (future) | — |
| **JOB** | Offers | Existing |
| **SYSTEM** | Welcome, tutorials | Minimal |

Add `MessageCategory.WORLD` (or use AUTHORITY + WORLD tag) for international/hiatus/restart items.

### 4.3 Message types for historical accuracy (new)

| messageType | When fired | Example headline |
|-------------|------------|------------------|
| `WORLD_LEAGUE_FOUNDED` | Script creates competition | “Scottish Football League established” |
| `WORLD_LEAGUE_HIATUS` | Edition skipped year | “Argentine championship not held this year” |
| `WORLD_LEAGUE_RESTART` | New association / reform | “Argentine Football Association reorganises Primera División” |
| `WORLD_COUNTRY_FOOTBALL` | Country activation | “Organised league football begins in the Netherlands” |
| `WORLD_CLUB_FOUNDED` | Club activation | “Small Heath admitted to Football League Division Two” |
| `AUTHORITY_RULE_CHANGE` | Rules amendment script | “Football League confirms points system” |
| `AUTHORITY_REELECTION` | Bottom-four vote | “Accrington re-elected to the League” |
| `WORLD_PERSON_NOTABLE` | Discovery hook (optional) | “Preston sign forward from Scotland” |

**Authoring:** script packs include `newsTitle` + `newsBody` + optional `sourceId` from [HISTORICAL_DATA_SOURCE_LEDGER.md](HISTORICAL_DATA_SOURCE_LEDGER.md).

### 4.4 Delivery rules

| Rule | Detail |
|------|--------|
| **Scheduled date** | News arrives on `gameDate` matching IRL event (or start-mod timeline) |
| **Relevance filter** | World news visible to all employed/unemployed managers; club news filtered (existing `InboxScreenTable` logic) |
| **No silent state changes** | Every `WorldEvolutionManager` action that changes structure → **at least one** news item |
| **Archive** | Long careers need cap/archive (BLK-4) — world news may be “newspaper archive” subset |
| **Mandatory vs info** | Only draws / critical gates block Continue; world history is **informational** unless PO adds choice |

### 4.5 Future: dedicated Newspaper screen

Optional v2 UI: **Inbox tab “Newspaper”** filtering `WORLD` + `AUTHORITY` with period styling. **Same backend** — no duplicate event pipeline.

---

## 5. Player knowledge — who you “know”

### 5.1 Principle

The save stores **full world data** (for sim). The player sees **only what they have discovered**.

This is **not** anti-historical: in 1888 you do not know every Football League squad in detail on day one unless you research or read about them.

### 5.2 Entity types

| Entity | Discovery examples |
|--------|-------------------|
| **Club** | In your league; opponent in cup; mentioned in WORLD news; scout report |
| **Player** | In your squad; opponent lineups after match; named in news; scout |
| **Manager / staff** | Hired at your club; named in news; opponent dugout (future) |
| **Competition** | Exists when you enter it or news announces it |
| **Country football** | News when league starts abroad |

### 5.3 Knowledge levels (proposed)

| Level | Player sees |
|-------|-------------|
| **UNKNOWN** | Name may appear in fixtures as “?” or club name only |
| **AWARE** | Club/person name from news or table; no attributes |
| **PARTIAL** | Position, age, rough ability band (opponent after one match) |
| **FULL** | Full attributes (your squad, long-scouted, signed) |

Store on `SaveGame`:

```java
// Conceptual — implement as KNOW-1
Map<Long, EntityKnowledge> knowledgeByEntityId;  // person, club, comp keys
```

**MVP path:**  
- Layer A/B: all league opponents **AWARE** (names on fixture list); attributes **PARTIAL** after match.  
- V2+: true fog for foreign countries until news/scout.

**Existing hint:** `SquadScreenTable` comment — “Design for Fog of War on Attributes should be done ASAP” — this design **is** that system.

### 5.4 Discovery triggers

| Trigger | Effect |
|---------|--------|
| **WORLD news message** | AWARE for entities in `actionData` / linked ids |
| **Match played** | PARTIAL for opponents in lineup |
| **Player joins your club** | FULL for that player |
| **Competition draw** | AWARE for all participants |
| **Scouting (future)** | PARTIAL → FULL |
| **World evolution script** | AWARE for new club; news mandatory |

### 5.5 Managers & staff

Same pipeline as players:

- **Your club:** FULL from hire.  
- **Other clubs:** AWARE via news (“Mr. X appointed secretary at…”) when research pack includes appointments; else name only after match programme (future).  

Staff data model exists (`ClubStaffManager`) — tie discovery to job/news events.

---

## 6. End-to-end example: Argentina hiatus

**Timeline in save starting England 1888:**

| Date | Engine | Inbox (player reads) | Knowledge |
|------|--------|----------------------|-----------|
| 1891 | Script: Argentine league edition 1 runs (if country active or world news only) | “Championship organised in Buenos Aires — St Andrew’s share title” | AWARE: 5 Argentine clubs |
| 1892 | Competition → **HIATUS**; no edition sim | “No official championship held in Argentina this season; earlier league dissolved” | Clubs remain AWARE; comp marked dormant |
| 1893 | Script: AFA + Primera restart | “Argentine Football Association launches new Primera División campaign” | AWARE: restarted competition id |
| 1893+ | Editions simulate if PO enables playable Argentina | Match results in news if followed | PARTIAL/FULL via matches |

If player’s career is **England-only**, they still **read** Argentine news (WORLD category) — immersion without playable Argentina required.

**Flexible mode:** if the player is **employed at an Argentine club**, they may participate in the **1891 association vote** (continue vs hiatus). Outcome A = table above; Outcome B = **no hiatus in 1892**, alternate news and edition chain — see §11 and [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md).

---

## 11. Historical flexibility & player agency (HoI-style leeway)

### 11.1 Principle

Futtoboru is **historical first**, not a sandbox. Like **Hearts of Iron** campaign rules:

- **Default:** the world follows **real structural history** (hiatuses, restarts, new leagues).  
- **Leeway:** at authored **fork nodes**, outcomes may **diverge** if the save is in **Flexible** mode and the player (or vote simulation) chooses another path.  
- **No silent divergence:** alternate paths still emit **inbox / news** and update `CompetitionLifecycle` — same pipeline as IRL.

```
Timeline script (IRL default)
        ↓
   ForkEvent? ──no──→ apply default (Strict always takes this arm)
        │
       yes (Flexible + fork enabled)
        ↓
   Eligibility check (country, club, role)
        ↓
   Player agency UI (optional) + AI club votes
        ↓
   Resolved outcome → WorldEvolutionManager → news
```

**Not in scope:** rewriting all of history freely. Early design targets **named fork events** only (Argentina 1891 vote first; England 1892 re-election / Div 2 parallel).

### 11.2 Historical Mode (new-game setting)

| Mode | Behaviour |
|------|-----------|
| **Strict** | All fork events resolve to **IRL default**; player may see flavour text but **cannot change** structural outcome. |
| **Flexible** | Fork events run **vote / choice resolution**; IRL default remains **likely** when player is absent or opts out. |
| **Sandbox** *(future)* | Wider player-driven structural changes — not V1.5 scope. |

Stored on `SaveGame` at career start (`historicalMode` enum). **Immutable mid-career** (precedent: HoI ironman / rules at start).

PO default pending: see [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md) §6.

### 11.3 Fork events (engine concept)

Extend timeline scripts with optional fork metadata:

```text
# Conceptual script row — type 1008 FORK_EVENT
forkId=ARG_ASSOCIATION_VOTE_1891
date=1891-12-01
defaultOutcome=HIATUS_1892_AFA_1893
eligibleCountries=AR
playerAgency=EMPLOYED_AT_ELIGIBLE_CLUB
resolution=CLUB_VOTE
newsMandate=true
```

| Field | Meaning |
|-------|---------|
| `defaultOutcome` | IRL branch — always applied in **Strict** or if vote fails |
| `eligibleCountries` | Who can participate in agency UI |
| `playerAgency` | `NONE` · `OBSERVER` · `EMPLOYED_AT_ELIGIBLE_CLUB` · `CLUB_OWNER` |
| `resolution` | `AUTO` · `CLUB_VOTE` · `BOARD_CHOICE` · `LEAGUE_BALLOT` |

**WorldEvolutionManager** applies the **winning branch** (competition states, new bodies, editions) — never both branches.

### 11.4 Player agency UI (early design)

| Surface | When | Actions |
|---------|------|---------|
| **Inbox (mandatory)** | Fork opens | Read meeting notice; link to club discussion |
| **Club → Internal / Board** | Player eligible | Discuss with **owners / committee** — stance affects club vote |
| **Inbox (result)** | Vote closes | Tally, your club’s position, world structural outcome |
| **WORLD news** | Always | Same result for all careers (wording differs if player involved) |

**Voice:** period-appropriate — letters, committee minutes, not modern push notifications.

**Layer placement:** minimal stub acceptable in **P2** (message + one choice table); full owner dialogue in **Layer B+** when club finances / staff exist.

### 11.5 Argentina — canonical first fork

| Step | Strict | Flexible (player in Argentina, pushes continue) |
|------|--------|--------------------------------------------------|
| 1891 season ends | Vote scheduled | Same |
| Association meeting | Auto → **hiatus** | Club vote — player stance via owners |
| 1892 | **HIATUS** | **ACTIVE** (alternate edition) OR hiatus if vote lost |
| 1893 | **AFA RESTARTED** (IRL) | IRL restart **or** continued body **or** delayed reform (PO picks alt depth) |

**Design intent (PO message):** clubs **vote** on whether to accept the hiatus — history says they effectively disbanded; the game **allows** the player to fight for continuation through **internal club politics**, but **does not guarantee** success.

Research: **R-H12** (IRL path) + **R-H12-alt** (1892 continuation fixtures/rules) when PO approves Option 2 in Argentina brief.

### 11.6 Parallel forks (roadmap)

| Fork | Region | Mechanism | Doc |
|------|--------|-----------|-----|
| **ARG_ASSOCIATION_VOTE_1891** | Argentina | Club vote | [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md) |
| **FL_REELECTION_1888_91** | England | League ballot | [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) Option B |
| **FL_DIV2_SPLIT_1892** | England | Script + optional save-driven | Same brief Options C/D |
| **SFL_FOUNDING_1890** | Scotland | Authority event | V2 — observer only at first |

Shared implementation: **FLEX-2** `ForkEvent` + **FLEX-3** `ClubVoteResolution`.

### 11.7 News for alternate history

| messageType | When |
|-------------|------|
| `WORLD_FORK_NOTICE` | Fork opens — “Clubs summoned…” |
| `WORLD_FORK_RESULT_HISTORICAL` | IRL branch won |
| `WORLD_FORK_RESULT_ALTERNATE` | Player/vote diverged |
| `CLUB_INTERNAL_STANCE` | Your board backs continue / pause (club category) |

Alternate headlines must **not** pretend IRL happened — clear “clubs vote to continue championship” vs hiatus wording.

### 11.8 Engineering — flexibility pipeline

| ID | Task | Depends on |
|----|------|------------|
| **FLEX-1** | `HistoricalMode` on `SaveGame` + new-game UI | Save schema |
| **FLEX-2** | Script type **1008** `FORK_EVENT` + branch outcomes | WE-4 |
| **FLEX-3** | `ClubVoteResolution` — weights, tally, AI clubs | WE-1 |
| **FLEX-4** | Club internal UI — owner / committee stance | Platform, Layer B club screen |
| **FLEX-5** | Argentina **ARG_ASSOCIATION_VOTE_1891** content pack | R-H12, NEWS-2 |
| **FLEX-6** | IT: Strict → always hiatus; Flexible + forced votes → continuation | Tests |
| **NEWS-7** | Fork notice + result message types | NEWS-1 |

### 11.9 What we are **not** promising (early design)

- Arbitrary “what if” for every country from day one  
- Mid-career toggle of Historical Mode  
- Guaranteed player wins — **agency is influence**, not scripted victory  
- Full alternate-history research for every fork until PO approves alt packs  

---

## 7. Engineering roadmap

### 7.1 News pipeline

| ID | Task | Depends on |
|----|------|------------|
| **NEWS-1** | `MessageCategory.WORLD` + types §4.3 | Message model |
| **NEWS-2** | `WorldEvolutionManager` → `MessageManager` hook (no silent changes) | WE-1 |
| **NEWS-3** | Script file fields: `newsTitle`, `newsBody`, `sourceId` | DB-3 |
| **NEWS-4** | `CompetitionLifecycle` on save (ACTIVE/HIATUS/RESTARTED) | WE-4 |
| **NEWS-5** | Inbox UI: WORLD filter + newspaper-style grouping | Platform |
| **NEWS-6** | IT: Argentina 1891→hiatus→1893 messages fire on dates | Tests |

### 7.2 Knowledge pipeline

| ID | Task | Phase |
|----|------|-------|
| **KNOW-1** | `EntityKnowledge` on `SaveGame` + migration | Layer B / V2 |
| **KNOW-2** | Match → PARTIAL opponent attributes | Layer B |
| **KNOW-3** | News → AWARE linked entities | V2 |
| **KNOW-4** | Squad UI respects knowledge level | V2 |
| **KNOW-5** | Scout system → FULL (future) | V3 |

### 7.3 Research / content

| ID | Task |
|----|------|
| **R-NEWS-1** | Timeline ledger: every structural event → news copy + date |
| **R-NEWS-2** | Argentina 1891–93 narrative pack (hiatus wording sourced) |
| **R-NEWS-3** | England 1892 Div 2 announcement texts |

---

## 8. Relationship to other docs

| Doc | Link |
|-----|------|
| [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) | **What** enters save |
| **This doc** | **How player learns** it + accuracy bar |
| [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) | Layer A/B/C sequencing |
| [ANALYSIS_INBOX_SYSTEM_V1.md](ANALYSIS_INBOX_SYSTEM_V1.md) | UI/feature detail (update pending) |
| [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) | England-only membership choice |
| [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md) | **Argentina fork — vote on hiatus vs continuation** |

---

## 9. PO confirmations (implicit from this message)

| Topic | Status |
|-------|--------|
| Historical structural accuracy | **Core** |
| Inbox / news as primary channel | **Core** |
| Hiatus + restart leagues | **Required** (Argentina pattern) |
| Gradual knowledge of players/clubs/staff | **Core direction** |
| Historical leeway (HoI-style forks) | **Core direction** — §11 |
| Argentina vote / internal club agency | **First fork template** — PO brief |
| Public release timing | **PO-only** — not documented as a gate |

---

## 10. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | §11 — Historical Mode, fork events, Argentina vote agency (HoI-style leeway) |
| 2026-06-27 | Initial intentions doc — accuracy, news, knowledge layers |

---

*Canonical reference for “historical world + newspaper + discovery.”*
