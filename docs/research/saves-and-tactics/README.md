# CM Legacy — Saves, Tactics & Format Artifacts

**Purpose:** Track **binary samples** and **format documentation** for later reverse-engineering of OG Championship Manager data structures (CM1–CM4 and CM9798/CM3 family).

**Research status:** **PAUSED** — acquisition and RE deferred until post-v1 or explicit feature need. See [CM_LEGACY_RESEARCH_PAUSE.md](../../../CM_LEGACY_RESEARCH_PAUSE.md).

**Parent ledger:** [CM_LEGACY_SOURCE_LEDGER.md](../../../CM_LEGACY_SOURCE_LEDGER.md) § Save games & tactics  
**Cover CDs:** [CM_MAGAZINE_COVER_CD_INDEX.md](CM_MAGAZINE_COVER_CD_INDEX.md)  
**Local samples:** [samples/MANIFEST.md](samples/MANIFEST.md)  
**Community context:** [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](../../../CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md)

---

## Known file types (community consensus)

| Era | Artifact | Typical path / extension | RE value |
|-----|----------|--------------------------|----------|
| **CM4 / CM03/04** | Tactics preset | `user data/tactics/*.tac` | Formation, team instructions, player roles |
| **CM4 / CM03/04** | Training schedule | `user data/` (`.sch` or similar — verify per install) | Session layout ↔ player growth |
| **CM4 / CM03/04** | Pregame database | `server_db.dat`, `client_db.dat`, `people_db` | Static world schema; editors document field layout |
| **CM01/02** | Season save | `*.sav` (uncompressed for editors) | Full runtime state; offset maps on Champman0102 |
| **CM01/02** | Tactics (magazine CD) | `Tactics/*.exe` on cmmag-01 | **PE installers** (29,184 B) — deploy tactic into game folder |
| **CM01/02** | Safe-save temp | `temp_save_file` | Two-phase write pattern |
| **CM3 family** | Save game | `.sav` + Graeme Kelly editor | Raw attrs in `*.sav`; editor uses temp `*.dat` until Save |
| **CM93/94** | Save game | “Save to disk” (manual); extension TBD | Early text-engine binary |
| **CM1** | Season save | Season disk (Amiga/ST/DOS) | Pre-real-names schema |
| **CM9798** | Save game | Multiple slots (manual) | Editor-era finale |

**Load path (CM4 tactics):** copy `.tac` into tactics folder → Squad screen → Tactics → **Load** dropdown ([CM Tactics Centre FAQ](https://cmtacticscentre.proboards.com/thread/87/load-downloaded-tactic-cm4), 2003).

**Graeme Kelly editor (from FAQ on disc):** games must be **uncompressed**; left column = raw `*.sav` values, right = displayed 1–20; also reads `*.pls` shortlists; `regentracking.dat` per save folder.

---

## Local inventory

| Source ID | File | Status | Notes |
|-----------|------|--------|-------|
| SRC-BIN-CMMAG01 | `_staging/cmmag-01/CMMag01.iso` | **PARTIAL** | Mounted R14; ISO gitignored |
| SRC-BIN-CMMAG04 | — | **REMOTE** | **P0** — CM4 `.tac` + training per metadata |
| SRC-TOOL-GK-CM3-EDITOR | `samples/cm3/tools/GraemeKelly_*` | **PARTIAL** | FAQ + CHANGES from cmmag-01 |
| SRC-BIN-CM0102-TACT-EXE | `samples/cm0102/tactics/*.exe` | **ACQUIRED** | 3 of 15 tactic installers sampled |
| SRC-BIN-CM4-TAC | — | **MISSING** | Hunt cmmag-04/05 next |
| SRC-BIN-CM4-SAV | — | **MISSING** | Mid-season CM4 save folder |
| SRC-COMM-CASPERLAB-INDEX | casperlab Wayback | **ACQUIRED** | Save editor v0.02 docs |
| SRC-COMM-NYGREEN / JONNYRIVERS | Wayback | **ACQUIRED** | Pregame editor pages |
| SRC-GFQ-CM4-IG1234 | ig1234 FAQ | **ACQUIRED** | Text-only tactics |
| SRC-TOOL-CASPERLAB-ZIP | — | **BLOCKED** | Wayback zip = HTML shell |

---

## Archive.org — cover CD bundles

See [CM_MAGAZINE_COVER_CD_INDEX.md](CM_MAGAZINE_COVER_CD_INDEX.md) for issues 1–5.

---

## External format documentation (consulted, not archived)

| Resource | URL | Use |
|----------|-----|-----|
| Champman0102 offset index | [viewtopic.php?f=35&t=2802](https://champman0102.net/viewtopic.php?f=35&t=2802) | CM01/02 save/staff/player offsets |
| CM0102 structures | [nic.hopto.org/open/cm010](https://nic.hopto.org/open/cm010) | C headers / memory layout |
| CM0102Patch source | [github.com/nckstwrt/CM0102Patch](https://github.com/nckstwrt/CM0102Patch) | Save-load code paths |
| Tool-making thread | [viewtopic.php?t=6259](https://champman0102.net/viewtopic.php?t=6259) | `server_db.dat` tables |
| Graeme Kelly editor | [viewtopic.php?f=43&t=1554](https://champman0102.net/viewtopic.php?f=43&t=1554) | Also on cmmag-01 |

---

## Hunt queue

| Priority | Target | Hunt vector |
|----------|--------|-------------|
| **P0** | Extract **`cmmag-04`** → CM4 `.tac` + training schedules | Mount ISO; grep `*.tac`, `*.sch` |
| **P1** | CM4 mid-season save directory | Community uploads |
| **P1** | CM01/02 `.sav` (uncompressed) | Generate from cmmag-01 CM0102 demo + editor |
| **P2** | CM93/CM1 Amiga save | ABIME / game bundles |
| **P2** | Copy GK Save Editor `SETUP.EXE` from cmmag-01 | If live RE session needed |

---

## Acquisition rules

1. **Do not commit** full magazine ISOs — `_staging/*.iso` is gitignored.
2. Store binaries under `samples/<game>/` with [MANIFEST.md](samples/MANIFEST.md).
3. Pair every sample with **game version + patch**.
4. Prefer **uncompressed** CM01/02 saves for editor RE.

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 16 — hunt queue **paused** at research stop point |
| 2025-06-25 | Round 14 — cmmag-01 extract; cover CD index; GK FAQ/CHANGES + CM0102 tactic exes |
| 2025-06-25 | Round 13 — track opened; casperlab index; cmmag-01 catalogued |
