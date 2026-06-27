# Design — In-Play World Evolution (Live Save Database)

**Document type:** Product / architecture analysis  
**Date:** 2026-06-27  
**Status:** **Active** — extends [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) and [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)  
**Audience:** PO, PM, World & Data, Seasons & Rules, Research

---

## 1. Requirement (PO-aligned)

> If the player starts in the **earliest possible season** and plays forward, every **new** club, competition, country, rule change, and player that appears in real history must be **incorporated into the live database of that save** — not by loading a new mod folder or restarting the career.

This is the **runtime world model** behind the north star in [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md):

- **Start mod** = historical entry snapshot + **timeline of future activations**  
- **Procedural rollover** = next competition year with **same save**  
- **World evolution** = dated events that **mutate `SaveGame`**, not static files per simulated year

**Player learns via news (locked):** Every structural change (new league, hiatus, restart, country) must surface in **inbox / newspaper** messages. Competitions may enter **HIATUS** or **RESTART** — e.g. Argentina 1891 → gap 1892 → AFA 1893. See [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md).

---

## 2. Canonical rule: save is truth during play

`SaveGame` already documents the intent:

```69:75:futtoboru-core/src/main/java/com/rndmodgames/futtoboru/system/SaveGame.java
     *  - TODO: when the game is being played we have to make sure we always use the most recent/updated data version and not the original static data from the DatabaseLoader
```

| Layer | Role | When used |
|-------|------|-----------|
| **`DatabaseLoader` / mod files** | Bootstrap registry at new game | New game, dev tools, **not** mid-career lookups |
| **`SaveGame.allClubs`, `allPersons`, `allPlayers`, leagues, cups, scripts** | **Live in-play database** | Every `continueGame()` tick after new game |
| **Procedural rollover** | New editions + fixtures on **existing** save entities | Season N → N+1 |

**Design lock:** Any feature that reads `DatabaseLoader.getClubById()` during simulation is a **bug** once world evolution is implemented — scripts may use the registry **once** to seed the save, then only touch save collections.

---

## 3. What happens today (gap analysis)

### 3.1 New game — partial incorporation

| Step | Behaviour | Evolution-ready? |
|------|-----------|------------------|
| Selected countries | All clubs for country copied to `SaveGame.allClubs` | ⚠️ **Eager load** — 35 clubs at once for England |
| Cup participants | Extra clubs pulled from registry into save | ✅ Pattern for “activate into save” |
| League creation script | Clubs from script → league; missing clubs pulled from registry → save | ✅ Same pattern |
| `gameScripts` | Copied from start season mod; run on date | ✅ Foundation for timeline |
| `foundation_year` on clubs | Stored on `Club.year` | ❌ **Not used** to gate visibility or activation |

### 3.2 During career — frozen world

| Event | Current behaviour | Gap |
|-------|-------------------|-----|
| Procedural season rollover | Copies **same** participant club IDs | ❌ No promotion/relegation, expansion, new members |
| New club founded (e.g. 1892) | Not modeled | ❌ No script type, no save merge |
| New division / league | Not modeled | ❌ Only one league creation script at 1888 |
| New country (Scotland) | Not selectable mid-career | ❌ Countries fixed at new game |
| Rules change (Nov 1888, 3 pts win) | Rules from start mod; snapshot on rollover | ⚠️ Amendment script type **not implemented** |
| New players | Static squads from mod | ❌ v1 policy — no regens; future signings must land in save |
| Script types | `1000` league, `1001` cup only | ❌ No founding / expansion / amendment types |

### 3.3 Existing hook that proves the pattern

`ScriptsManager.createLeague()` already **incorporates** clubs into the live save when a script fires:

```196:206:futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/ScriptsManager.java
            Club club = currentGame.getClubById(clubId);
            if (club == null) {
                club = DatabaseLoader.getClubById(clubId);
                if (club != null) {
                    currentGame.getAllClubs().add(club);
```

**Conclusion:** The engine has a **one-way incorporation pipeline** (registry → save on script execution). It is **not extended** to ongoing history (new clubs, leagues, countries, rules, players) or to **lazy** activation by calendar date.

---

## 4. Target model — three incorporation modes

All modes end in the same place: **mutate `SaveGame`**. They differ in **where** data is authored.

```
                    ┌─────────────────────────────────────┐
                    │         SaveGame (live DB)          │
                    │  allClubs, allPersons, leagues,     │
                    │  cups, rules snapshots, scripts     │
                    └────────────────▲────────────────────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         │                           │                           │
   Mode A │                   Mode B │                   Mode C │
 Dormant  │              Timeline    │              Master DB   │
 snapshot │              scripts     │              projection  │
         │                           │                           │
  Start   │  mods/seasons/{id}/       │  mods/master/ +         │
  mod     │  clubs w/ first_active    │  membership dates       │
  clubs   │  scripts/*.txt on date    │  query at gameDate      │
```

### Mode A — **Dormant entities in start mod** (near term)

- Start mod lists clubs/players/competitions with **`first_active_date`** (or reuse `foundation_year`).
- At new game: only entities **active on start date** enter `SaveGame`.
- On `gameDate >= first_active_date`: **`WorldEvolutionManager`** copies entity (+ squad) from mod registry into save (same as cup participant pull today).

**Example:** Club id `40` founded 1892 — present in mod files, **not** in save until April 1892 script or auto-activation.

**Fits:** England 1888 → 1910 on one start mod without loading 1892 start mod.

### Mode B — **Timeline scripts** (FM-style, already planned)

Dated scripts in start mod (or appended at rollover) with typed actions:

| Script type | ID | Incorporates into save |
|-------------|-----|------------------------|
| `LEAGUE_CREATION_SCRIPT` | 1000 | ✅ League + clubs (exists) |
| `CUP_CREATION_SCRIPT` | 1001 | ✅ Cup edition + participants (exists) |
| `RULES_AMENDMENT_SCRIPT` | 1002 | League/edition rules snapshot |
| `CLUB_ACTIVATION_SCRIPT` | 1003 | Club + players + stadium → `allClubs` |
| `LEAGUE_MEMBERSHIP_SCRIPT` | 1004 | Add/remove club from league / edition |
| `COMPETITION_CREATION_SCRIPT` | 1005 | New cup/league (Div 2, Scottish League) |
| `COUNTRY_ACTIVATION_SCRIPT` | 1006 | Nation + clubs + comps playable in save |
| `PERSON_INCORPORATION_SCRIPT` | 1007 | Named player → `allPersons` / club squad |

Scripts run via `ScriptsManager.checkGameScripts()`; **`isExecuted`** persisted on save.

### Mode C — **Master DB projection** (north star)

At each date (or on demand), query master tables for rows where `active_from <= gameDate` and `id ∉ save` → merge into save. Timeline scripts become **optional overrides** (re-election, alt history).

**Fits:** “Any season start” + centuries forward without hand-authoring every activation.

---

## 5. Entity-by-entity incorporation rules

| Entity | At earliest start | When it “appears” later | Save fields touched |
|--------|-------------------|-------------------------|---------------------|
| **Club** | In save if `first_active <= startDate` | Script or auto by `foundation_year` | `allClubs`, league membership, finances init |
| **Player** | With club at activation | Signing, youth, script “debut” | `allPersons`, `allPlayers`, `club.players` |
| **Stadium** | With club | Move script (e.g. Molineux 1889) | `club.stadium` |
| **League / division** | First league via script | Expansion script (1892 Div 2) | `mainAuthority.leagues`, new edition |
| **Cup** | FA Cup script | New competition script | `allCups`, editions |
| **Country** | Selected at new game (today) | Activation script or master DB | `selectedCountries`, clubs, comps |
| **Rules** | Start mod `competition_rules.txt` | Amendment script + edition snapshot | `league.rules`, `CompetitionEdition` snapshot |
| **Authority** | Main FA at start | Rare — documented separately | `mainAuthority` |

**Procedural rollover** must **read participants from save state** after scripts have run — not blindly copy edition N−1 forever. Re-election, promotion, and expansion update participants **before** `generateNextSeason()`.

---

## 6. Content authoring implications

### 6.1 Starting at the earliest season

| Authoring choice | Effect |
|------------------|--------|
| Put every future club in start mod | Works for Mode A only if **dormant** until activation — not all in save day 1 |
| One mod per pickable start year | Still valid for **new game** entry points — unrelated to in-career incorporation |
| Master DB | Single lint pipeline; start mod = filtered view at `startDate` |

### 6.2 Research deliverables (extends R-H track)

| ID | Deliverable | Purpose |
|----|-------------|---------|
| **R-H5** | **Timeline ledger** per start mod — dated club/league/comp/rule events | Author scripts / `first_active_date` |
| **R-H6** | **1892 expansion pack** — Div 2 clubs, election, new members | First LEAGUE_MEMBERSHIP + CLUB_ACTIVATION test case |
| **R-H7** | **Scotland 1890 activation** — country + league script chain | COUNTRY_ACTIVATION reference |

### 6.3 Validation (extends V-1–V-9)

| Rule | Check |
|------|-------|
| **V-10** | Every club has `foundation_year` ≤ `first_active_date` or script date |
| **V-11** | No duplicate global club/person IDs across dormant + active sets |
| **V-12** | Every post-start activation has script **or** `first_active_date` |
| **V-13** | Simulation test: start 1888 → date X → club Y in save |

---

## 7. Engineering prerequisites (before historical accuracy at scale)

**Sequence:** Wave 1 infra ([DB-1–DB-8](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)) → **Wave 1.5 world evolution** → Wave 2 content.

| ID | Task | Blocks |
|----|------|--------|
| **WE-1** | `WorldEvolutionManager` — single API: `incorporateClub`, `incorporatePerson`, `amendRules`, … | All activation |
| **WE-2** | `first_active_date` / dormant flag on mod schema + loader | Mode A |
| **WE-3** | New game: **lazy** club load (start date filter) | Scale + correctness |
| **WE-4** | Script types 1002–1007 + file format in `mods/seasons/{id}/scripts/` | Mode B |
| **WE-5** | Rollover reads **current** league membership from save, not frozen copy | Accurate seasons |
| **WE-6** | Ban `DatabaseLoader` lookups during `continueGame()` (except one-shot script seed) | Save-as-truth |
| **WE-7** | `SaveGame` schema version + migration for new entities mid-career | Long saves |
| **WE-8** | IT: `WorldEvolutionIT` — 1888 start → script date → assert club in save | Regression |

Maps to existing **SR-8+** (Seasons & Rules) and **WD-4** (scripts on disk).

---

## 7.1 Interaction with multi–start-season mods

| Scenario | Incorporation model |
|----------|---------------------|
| Start **1888**, play to 1905 | Mode A/B timeline in mod `18` — **no** mod `25` load |
| Start **1905** new game | Mod `25` snapshot — save already includes 1905 world |
| Same club id across eras | **DB-1 global IDs** — one club entity; membership dates in master DB |

**Rule:** Pickable start mods are **alternative entry snapshots**. In-career evolution always **appends to the same save**, never swaps mod folder.

---

## 8. Phased delivery (recommended)

| Phase | Scope | Proves |
|-------|-------|--------|
| **P0 (now)** | Document + WE-1 design; fix save-as-truth in new code | Alignment |
| **P1** | WE-3 lazy load + WE-4 rules amendment script (Nov 1888) | First timed incorporation |
| **P2** | WE-4 club activation + league membership (1892 expansion research) | New clubs in running save |
| **P3** | WE-4 country activation (Scotland) | Multi-nation save |
| **P4** | Master DB projection (Mode C) | North star scale |

Gameplay **v1.0** does not require P2–P4 — procedural 12-club loop is acceptable for ship. **Historical accuracy** requires P1+.

---

## 9. PO decisions requested

| # | Decision | Options |
|---|----------|---------|
| 1 | Incorporation default | **A)** Timeline scripts only **B)** Auto by `foundation_year` **C)** Both (script overrides auto) |
| 2 | New game club load | **A)** All country clubs (today) **B)** Active-on-start-date only (recommended) |
| 3 | First historical evolution milestone | **A)** Nov 1888 rules **B)** 1892 Div 2 **C)** Scotland 1890 — **PO leaning B** |
| 4 | Mid-career country add | **A)** Scripts only **B)** Also PO “expand world” UI later |
| 5 | Static players v1 | **A)** Keep static; evolution = structure only **B)** Block v1.0 on player pipeline |

**Defaults if silent:** 1-C, 2-B, 3-A, 4-A, 5-A.

---

## 10. Cross-references

| Document | Update |
|----------|--------|
| [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | §2.2 timeline + §4.2 script types |
| [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md) | §8 world evolution |
| [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) | §12 in-play evolution summary |
| [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) | WE-* tasks |
| [DEPARTMENT_SEASONS_AND_RULES_V1.md](DEPARTMENT_SEASONS_AND_RULES_V1.md) | Script types + rollover membership |
| [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) | UI/engine stack for implementers |

---

*Canonical reference for “new history enters the running save, not a new mod year.”*
