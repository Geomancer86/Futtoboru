# CM Legacy — Saves, Tactics & Format Artifacts

**Purpose:** Track **binary samples** and **format documentation** for later reverse-engineering of OG Championship Manager data structures (CM1–CM4 and CM9798/CM3 family).

**Parent ledger:** [CM_LEGACY_SOURCE_LEDGER.md](../../../CM_LEGACY_SOURCE_LEDGER.md) § Save games & tactics  
**Community context:** [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](../../../CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md)

---

## Known file types (community consensus)

| Era | Artifact | Typical path / extension | RE value |
|-----|----------|--------------------------|----------|
| **CM4 / CM03/04** | Tactics preset | `user data/tactics/*.tac` | Formation, team instructions, player roles |
| **CM4 / CM03/04** | Training schedule | `user data/` (`.sch` or similar — verify per install) | Session layout ↔ player growth |
| **CM4 / CM03/04** | Pregame database | `server_db.dat`, `client_db.dat`, `people_db` | Static world schema; editors document field layout |
| **CM01/02** | Season save | `*.sav` (uncompressed for editors) | Full runtime state; offset maps on Champman0102 |
| **CM01/02** | Safe-save temp | `temp_save_file` | Two-phase write pattern |
| **CM3** | Save game | `.sav` + Graeme Kelly editor | Magazine CD editor implies parseable layout |
| **CM93/94** | Save game | “Save to disk” (manual); extension TBD | Early text-engine binary |
| **CM1** | Season save | Season disk (Amiga/ST/DOS) | Pre-real-names schema |
| **CM9798** | Save game | Multiple slots (manual) | Editor-era finale |

**Load path (CM4 tactics):** copy `.tac` into tactics folder → Squad screen → Tactics → **Load** dropdown ([CM Tactics Centre FAQ](https://cmtacticscentre.proboards.com/thread/87/load-downloaded-tactic-cm4), 2003).

---

## Local inventory

| Source ID | File | Status | Notes |
|-----------|------|--------|-------|
| SRC-COMM-CASPERLAB-INDEX | [../sources/community/casperlab_cm4save_index_wayback.html](../sources/community/casperlab_cm4save_index_wayback.html) | **ACQUIRED** | CM4 save editor v0.02 page — edits **player attributes**; zip not archived |
| SRC-COMM-NYGREEN | nygreen Wayback pages | **ACQUIRED** | Pregame editor; references `server_db.dat` |
| SRC-COMM-JONNYRIVERS | jonnyrivers Wayback | **ACQUIRED** | CM4 editor v0.09b zip link — **zip not in Wayback** |
| SRC-GFQ-CM4-IG1234 | ig1234 FAQ | **ACQUIRED** | **Text-only** tactics (formations 4-4-2 variants); no `.tac` binaries |
| SRC-BIN-CMMAG01 | — | **REMOTE** | See archive.org below — not extracted locally yet |
| SRC-BIN-CM4-TAC | — | **MISSING** | Need `.tac` + training schedule samples |
| SRC-BIN-CM4-SAV | — | **MISSING** | Mid-season CM4 save folder |
| SRC-BIN-CM1-SAV | — | **MISSING** | Amiga/ST season-disk save |
| SRC-BIN-CM93-SAV | — | **MISSING** | CM93 save file |
| SRC-TOOL-CASPERLAB-ZIP | `cm4saveeditor.zip` | **BLOCKED** | Wayback returns HTML wrapper only (~1.7 KB) |
| SRC-TOOL-GK-CM3-EDITOR | On `cmmag-01` ISO | **REMOTE** | Graeme Kelly CM3 Save Game Editor |

---

## Archive.org — high-value bundles (not yet mounted locally)

| Item | URL | Contents (from metadata) |
|------|-----|----------------------------|
| **cmmag-01** | [archive.org/details/cmmag-01](https://archive.org/details/cmmag-01) | CM2/CM3 demos; **Graeme Kelly CM3 Save Game Editor**; CM01/02 demo + **tactics**; CM01/02 data update 3.9.67; CM4 rolling demo |
| **cmmag-02 … cmmag-05** | `cmmag-02` … `cmmag-05` | Future CM Magazine cover CDs — extract file lists when hunting |

**Next step:** mount `CMMag01.iso` and inventory `*.tac`, `*.sav`, editor `.exe` + any readme describing offsets.

---

## External format documentation (consulted, not archived)

| Resource | URL | Use |
|----------|-----|-----|
| Champman0102 offset index | [viewtopic.php?f=35&t=2802](https://champman0102.net/viewtopic.php?f=35&t=2802) | CM01/02 save/staff/player offsets |
| CM0102 structures | [nic.hopto.org/open/cm010](https://nic.hopto.org/open/cm010) | C headers / memory layout |
| CM0102Patch source | [github.com/nckstwrt/CM0102Patch](https://github.com/nckstwrt/CM0102Patch) | Save-load code paths |
| Tool-making thread | [viewtopic.php?t=6259](https://champman0102.net/viewtopic.php?t=6259) | Translating pregame editor Pascal → Java; `server_db.dat` tables |
| Graeme Kelly editor | [viewtopic.php?f=43&t=1554](https://champman0102.net/viewtopic.php?f=43&t=1554) | CM3 save editor usage; also on cmmag-01 |

---

## Hunt queue (P1 for RE)

| Priority | Target | Hunt vector |
|----------|--------|-------------|
| **P0** | Extract `cmmag-01` ISO → tactics + GK CM3 editor + any `.sav` | Local mount or `7z x CMMag01.iso` |
| **P1** | CM4 `.tac` files (3–5 presets) | Wayback `champmanworld.com` downloads cid=2; CM Tactics Centre; boards.ie attachments |
| **P1** | CM4 mid-season save directory | Community uploads; casperlab editor test saves |
| **P1** | CM01/02 `.sav` (uncompressed) + `server_db.dat` | Champman0102 downloads; cmmag ISO |
| **P2** | CM93/CM1 Amiga save | ABIME / Lemon / archive.org `msdos_Championship_Manager_1992` bundle |
| **P2** | CM9798 save slot sample | Editor community; archive.org CM96-97 family items |
| **P2** | Jonny Rivers + casperlab **binaries** | Alternate mirrors; `cm4patch` collections |
| **P3** | Training schedule files (CM03/04) | boards.ie “Super Training Schedule” threads |

---

## Acquisition rules

1. **Do not commit** full magazine ISOs (372 MB+) — record archive.org ID + extracted filenames only.
2. Store binaries under `samples/<game>/` with a one-line `MANIFEST.md` per folder (game version, patch level, source URL).
3. Pair every sample with **game version + patch** (e.g. CM4 EP5, CM01/02 v3.9.68).
4. Prefer **uncompressed** CM01/02 saves when editors are the goal (see Graeme Kelly FAQ).

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 13 — saves/tactics track opened; casperlab index; cmmag-01 catalogued; format table |
