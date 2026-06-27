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
| League **founded / dissolved / hiatus** dates | Your club’s exact league position after year 5 |
| **New country** league begins | Simulated scorelines in unimportant matches |
| **Competition restart** under new body | Cup path if you diverge from IRL draw luck |
| Club **founded** and enters save on date | Re-election vote outcome if PO enables choice |
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

---

## 9. PO confirmations (implicit from this message)

| Topic | Status |
|-------|--------|
| Historical structural accuracy | **Core** |
| Inbox / news as primary channel | **Core** |
| Hiatus + restart leagues | **Required** (Argentina pattern) |
| Gradual knowledge of players/clubs/staff | **Core direction** |
| Public release timing | **PO-only** — not documented as a gate |

---

## 10. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial intentions doc — accuracy, news, knowledge layers |

---

*Canonical reference for “historical world + newspaper + discovery.”*
