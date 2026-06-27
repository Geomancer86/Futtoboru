# Design — Historical Database & Multi–Start-Season North Star

**Document type:** Product / data architecture  
**Date:** 2026-06-27  
**Status:** **Active** — extends [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md)  
**Audience:** PO, PM, World & Data, Research, Community

---

## 1. North-star vision

> **End goal:** The player can start a career in **any historically meaningful season** (eventually any year/league/country that existed in real life), using a **documented, source-cited, community-linted database** — then play **forward forever** via procedural seasons (no mod per simulated year).

This is **not** shipping 138 mod folders for 138 Football League seasons. It is:

1. **Curated start snapshots** (or a **master DB** filtered by year), and  
2. **Procedural rollover** for all following in-career years (proven — see [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md)).

---

## 2. Two meanings of “year” (avoid confusion)

| Concept | Needs mod data per year? | v1 status |
|---------|--------------------------|-----------|
| **Simulated competition year** (in save) | **No** — engine generates editions | ✅ Proven to 60 seasons |
| **Pickable start season** (new game) | **Yes** — one **start mod** per entry point | ⚠️ Only **1888–89** (id `18`) |

**Adding 10 years of forward play from 1888 costs zero extra content files.**  
**Adding the ability to start in 1905 costs one new start-mod package** (not 17 incremental year mods).

---

## 3. What one start-season mod contains

Template: `mods/seasons/{id}/` (today: id `18` = 1888–89)

| Asset | Purpose | Season 18 today |
|-------|---------|-----------------|
| `seasons.txt` row | id, name, start date, description, url | ✅ 1 row |
| `countries.txt` | Playable countries for this entry | ✅ England (`1000`) |
| `clubs.txt` | All clubs with **foundation_year**, balance, country | ✅ **35** clubs |
| `club_players/{clubId}.txt` | Named squads, birthdates, nationality | ✅ **35** files, **556** players |
| `club_stadiums/{clubId}.txt` | Ground, capacity, history notes | ✅ **35** files |
| `competition_rules.txt` | Points, tie-breakers, season window | ✅ Present |
| `competitions/` | Cup editions, participants | ✅ FA Cup 32 teams |
| `nationality_modifiers.txt` | Era attribute modifiers | ✅ |
| `region_modifiers.txt` | Regional modifiers | ⚠️ Lancashire `countryId` bug |
| Timeline scripts | League/cup creation, future amendments | ⚠️ Hardcoded in `ScriptsLoader` |
| Source URLs | Per row `url_source` / file header | ⚠️ Partial |

**Later eras scale up:** 1892+ Division Two, 1920+ three divisions, 1992+ Premier League — club and player counts rise sharply (see §6).

---

## 4. Target architecture (FM-inspired, Futtoboru-scoped)

### 4.1 Phase A — **Start-mod snapshots** (now → v2)

- Each pickable season = self-contained folder (current model).  
- Community contributes **pull requests** per mod; CI runs **validation rules V-1–V-9** ([DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md)).  
- **Milestone seasons only** — not every calendar year (see [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md)).

### 4.2 Phase B — **Master historical database** (v3+)

FM loads one database and filters by year/nation. Futtoboru equivalent:

```
mods/master/
  clubs_master.csv          # id, name, founded, dissolved, country, sources
  persons_master.csv        # global person id, name, birth, death, sources
  club_membership.csv       # club, person, from_date, to_date, sources
  competitions_master.csv
  season_snapshots/         # generated OR hand-curated views per start id
```

**Benefits:** one lint pipeline, no duplicate club IDs across eras, “any season” = generate snapshot from membership table.  
**Cost:** major loader refactor + multi-year research program.

### 4.3 Community quality bar

Every row must pass:

| Rule | Example |
|------|---------|
| **Source** | Wikipedia URL minimum; primary source preferred |
| **Lint** | Schema columns, ID uniqueness, ≥11 players per league club |
| **Review** | Two-person sign-off for squad changes (PO policy TBD) |
| **Provenance file** | `SOURCES.md` per season mod listing references |

---

## 5. Engineering prerequisites (before bulk content)

**Do not author 10 start mods until these land** — otherwise content will not load cleanly or will collide.

| ID | Prerequisite | Blocks |
|----|--------------|--------|
| **DB-1** | Global **player/person ID** scheme | Multi-club careers, save/load |
| **DB-2** | **Validation CI** V-1–V-9 | Community lint |
| **DB-3** | **Scripts from files** (not `ScriptsLoader` hardcode) | Per-era league/cup scripts |
| **DB-4** | **Dynamic modifier paths** `{seasonId}` | Season 19+ modifiers |
| **DB-5** | **ClubsLoader merge-by-ID** | Multiple start mods in dev build |
| **DB-6** | **Save schema version** + migration | Long careers across game versions |
| **DB-7** | **Data lint CLI** (`mvn validate-mods`) | Contributor workflow |
| **DB-8** | **Source registry** row format | Audit trail |

Most map to existing **WD-1–WD-9** in World & Data dept doc.

---

## 6. Scale by era (England-centric)

Approximate **content volume per start-mod snapshot** if authored at today’s 1888 fidelity:

| Era | Example start | League clubs (order of) | Players (order of) | Notes |
|-----|---------------|---------------------------|--------------------|-------|
| **1888–89** | id `18` ✅ | 12 + 23 cup | **556** ✅ | Baseline |
| **1892–93** | Div 1+2 | ~24 | ~900 | First expansion |
| **1905–06** | Edwardian | ~40 | ~1,500 | More cup depth |
| **1914–15** | Pre-WWI | ~40 | ~1,500 | Last before war break |
| **1919–20** | Post-WWI | ~44 | ~1,700 | Restart snapshot |
| **1939–40** | Pre-WWII | ~88 | ~3,500 | Three divisions |
| **1963–64** | Post-max wage | ~92 | ~4,000 | |
| **1992–93** | PL birth | ~92 + FL | ~4,500+ | Different rules |
| **2024–25** | Modern | 100+ leagues | 10,000+ | FM-scale — different product tier |

**Full “any season 1888–2026”** for England alone ≈ **138 calendar seasons** — impractical as hand mods. Use **~15–25 milestone starts** or **master DB**.

---

## 7. Countries & leagues expansion

| Today | Gap |
|-------|-----|
| **~193** countries in `mods/countries.txt` | Registry only — no leagues |
| **1** playable nation (England) at new game | Scotland, Wales, Ireland, etc. missing |
| **1** league (via script) + FA Cup | Scottish League (1890), Welsh cup, etc. |

**Per new country (minimum):**

1. Start-season mod(s) with `countries.txt` including that nation  
2. `clubs.txt` + players + stadiums for era-appropriate clubs  
3. `competition_rules.txt` + scripts for league/cup creation  
4. Modifiers if not reusing England templates  
5. Research pack (sources, league structure by year)

**Scotland 1890–91** is the natural **second country** (~10–12 clubs, ~400–600 players) — similar effort to one England milestone mod.

---

## 8. Relationship to procedural loop

```
New game: pick start mod (year + country)
    → load snapshot (clubs, squads, rules, scripts)
    → play season 1 (real historical participants)
    → rollover → procedural season 2, 3, … ∞
```

Optional **timeline scripts** in the start mod fire during the calendar (deaths, rule changes, new clubs) — FM-style on the **start mod timeline**, not new mod folders.

---

## 9. Open PO questions

| # | Question | Default |
|---|----------|---------|
| 1 | Milestone vs every-year starts? | Milestones + master DB long term |
| 2 | Minimum squad quality for cup-only clubs? | ≥11 named players |
| 3 | Anonymous players (CM1 style) for lower leagues? | Defer — named where sources exist |
| 4 | Community mod repo vs in-tree only? | In-tree + PR; external editor later |
| 5 | Second country before second English era? | PO call — Scotland historically adjacent |

---

## 10. Cross-references

| Doc | Role |
|-----|------|
| [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) | PM numbers, phases, research |
| [DEPARTMENT_WORLD_AND_DATA_V1.md](DEPARTMENT_WORLD_AND_DATA_V1.md) | Loader tasks, validation |
| [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md) | Gameplay features first |
| [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | Procedural vs start mod |

---

*Canonical reference for “start any season in history” data strategy.*
