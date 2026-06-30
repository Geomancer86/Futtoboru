# Analysis — Save Storage & Schema (Future)

**Document type:** Platform / architecture (deferred)  
**Date:** 2026-06-25  
**Status:** **Future consideration** — not Layer B blocker  
**Related:** MVP-10 (deferred) · [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md)

---

## 1. Current state

| Aspect | Implementation |
|--------|----------------|
| Format | **LibGDX `Json`** → pretty-printed `.json` files |
| Location | `%USERPROFILE%/Documents/RndModGames/Futtuboru/savegames/` |
| Model | Monolithic **`SaveGame`** graph (clubs, players, leagues, messages, …) |
| Version field | **None** (H-8 open) — MVP-10 deferred |
| Roundtrip | `SaveLoadSystem.copySaveGame()` + `SaveLoadIT` |
| Mod database | Loaded from `/mods/` at startup — separate from save file |

**Growth drivers:** more clubs (1892+), message history, per-club tactics, ratings history, world evolution state.

---

## 2. PO direction (2026-06-25)

| Topic | Decision |
|-------|----------|
| **MVP-10 now** | **No** — Layer B adds fields to existing JSON; formal versioning later |
| **MongoDB / NoSQL** | **Investigate for a later version** — embedded/local-first preferred |
| **Layer B** | Must not block on storage migration |

---

## 3. Why change might be needed (later)

| Pressure | JSON monolith today | Document DB benefit |
|----------|---------------------|---------------------|
| Save file size | Whole graph rewritten | Partial updates, indexed queries |
| Schema evolution | Manual migration scripts | Versioned documents + migration pipeline |
| Query patterns | Load everything | Inbox pagination, club slices |
| Long careers | Message/player bloat | Archival collections |
| 1892+ world state | More entities in one blob | Separate collections per domain |

**Not urgent at 1888 scale (~35 clubs, 556 players)** — reassess after Layer C or if saves exceed practical JSON size (e.g. >10–20 MB) or load time hurts QA.

---

## 4. Embedded / local-first options (research)

| Option | Embedded? | JVM fit | Notes |
|--------|-----------|---------|-------|
| **Keep JSON + MVP-10 migrations** | ✅ files | ✅ LibGDX today | Cheapest; good through Layer B/C |
| **SQLite** (JDBC / sqlite-jdbc) | ✅ single file | ✅ | Relational; good for structured queries; not NoSQL |
| **MongoDB Embedded** | ⚠️ Limited | ⚠️ | Official embedded story weak; usually server process |
| **Realm** (mobile-first) | ✅ | ⚠️ Desktop licensing | More mobile than desktop |
| **LevelDB / RocksDB** (key-value) | ✅ | ✅ | Low-level; need own serialization |
| **H2 + JSON column** | ✅ | ✅ | Hybrid SQL + document columns |
| **Embedded MongoDB alternatives** | | | e.g. **Percona**, **FerretDB** — usually overkill for desktop game |

**Recommendation for Futtoboru desktop (when the time comes):**

1. **Short term (Layer B–C):** JSON + **`saveVersion`** + incremental migrations (**MVP-10**).
2. **Medium term (V2):** Evaluate **SQLite** (single `.db` per career) *or* split JSON into **sharded files** (save manifest + clubs + inbox).
3. **MongoDB:** Only if PO wants server/cloud saves later; for **embedded local**, prefer SQLite or sharded JSON before running a Mongo daemon.

---

## 5. Migration strategy (when triggered)

```
Phase 0 (Layer B)     Add ClubTacticsSetup etc. to JSON — no version field
Phase 1 (MVP-10)      saveVersion=1; migration runner on load
Phase 2 (pre-1892)    saveVersion=2; optional inbox archive
Phase 3 (V2 storage)  Importer: JSON vN → SQLite/DB; keep JSON export for debug
```

**Rule:** Any Layer B field must serialize with LibGDX Json today so Phase 1 is additive, not a rewrite.

---

## 6. Layer B impact — none on storage engine

Layer B adds:

- `ClubTacticsSetup` on `Club`
- Optional `MatchLineup` snapshot on `Match` or match result blob
- `lastMatchRatings` map on club or players

All via existing `SaveLoadSystem` — covered by new **`ClubTacticsSetupSaveLoadIT`**.

---

## 7. Decision log

| Date | Decision |
|------|----------|
| 2026-06-25 | Defer MVP-10; note MongoDB/embedded NoSQL for future release analysis |
| TBD | PO triggers storage spike when save size or migration pain exceeds threshold |

---

*Revisit after Layer C or if save files consistently exceed agreed size budget.*
