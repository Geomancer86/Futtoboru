# CM Legacy Research — Pause Point

**Status:** **PAUSED** (sufficient for v1 design; resume when implementation needs deeper sources)  
**Pause date:** 2025-06-25  
**Final research round:** **15** (`268af40`)  
**Branch:** `analyze-ai-branch`  
**Next project phase:** **MVP / v1 feature definition & implementation** — see [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) · [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)

---

## Why we stop here

Document acquisition for CM legacy ran **15 rounds** (FM bible R1 → CM refs R2–8 → source hunts R9–15). For **Futtoboru v1** — a historical season loop benchmarked on **CM1 (1992)**, not CM4 depth — we now have:

| Need for v1 | Research state |
|-------------|----------------|
| **Genre minimum loop** (Toms 1982) | ✅ FM1982 manual + [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) |
| **v1 feature benchmark** (CM1) | ✅ Manual HTML + menu extract + [CM1_REFERENCE.md](CM1_REFERENCE.md) + The One #46 review OCR |
| **Sequel evolution context** (CM93) | ✅ Manual text + menu extract + five UK magazine reviews |
| **Later-era scope guard** (CM2–CM4) | ✅ Reference docs + gap analysis — **out of v1 scope** |
| **Binary save/tactics RE** | ⏸️ **Explicitly deferred** — catalogued in backlog, not started |

**Decision:** Pause **active hunting** (magazines, GameFAQs, cmmag ISOs, citation-chain mirrors). Remaining gaps are logged in [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md) for a future **R16+** session — not blockers for MVP work.

---

## What was delivered (Rounds 1–15)

| Phase | Deliverables |
|-------|--------------|
| **FM Bible R1** | FM2005+ competitive index |
| **CM Legacy R2–8** | CM1–CM4 reference wiki, manuals archive, patch chronology, era reviews, CM93/9798/Italia refs |
| **Pre-CM1 R3** | Genre history, CM1 v1 benchmark |
| **Source hunts R9–12** | Magazine OCR (CM93 ×5, CM1 TONE #46, CM2 PC Zone #31, AP #27), Wayback community captures |
| **Saves/tactics R13–14** | Format hunt track, cmmag-01 extract, magazine CD index |
| **Backlog R15** | Master missing inventory, CM Tech FAQ from cmmag-01 |

**Local source tree:** [docs/research/sources/README.md](docs/research/sources/README.md) · **Manuals:** [docs/research/manuals/README.md](docs/research/manuals/README.md)

---

## Early games sufficiency audit (FM82 → CM93)

**Question:** Do we still need anything from the **very early** titles before moving to MVP?

**Answer:** **No — not for v1.** Early-game research is **complete enough** to design and implement the first release. Remaining early-game gaps are **P2–P3 polish** or **binary artifacts**, not design blockers.

### Per-title verdict

| Title | v1 relevance | Have in repo | Still missing | Need before v1? |
|-------|--------------|--------------|---------------|-----------------|
| **Football Manager (1982)** | Genre floor — text + table + cups | HTML manual, PRE_CM1 §2 | PDF scan only | **No** |
| **FM World Cup Edition (1990)** | Precursor context only | PDF in `precursors/` | Deep manual analysis § | **No** — defer |
| **CM1 (1992)** | **Primary v1 benchmark** | HTML manual, menu extract, CM1_REFERENCE, TONE #46 OCR | Low-score review OCR, `.sav` samples | **No** |
| **CM93/94 (1993–94)** | Evolution reference (real names, tactics screen) | Lemon text manual, menu extract, 5 UK review OCRs, CM93_94_REFERENCE | CVG #140 (1 review), PDF scan, saves | **No** |
| **CM Italia / Norge / Guy Roux** | Spin-offs — not v1 scope | Listed in CM_ITALIA_SPINOFFS_REFERENCE | Manuals, reviews | **No** |

### Early-game gaps — classify at pause

| Gap | Blocks v1 design? | Resume when |
|-----|-------------------|-------------|
| CVG #140 CM93 review | No (5 other UK primaries) | Completing CM93 reception anthology |
| CM93 PDF scan | No (full text extract) | Archival completeness |
| CM1 / CM93 save files | No (no save-format RE in v1) | Save editor or import feature |
| FM WCE manual analysis | No | Precursor deep-dive or FM lineage doc |
| Polarized CM1 low-score press | No (TONE #46 + CM1_REFERENCE narrative) | Reception polish |

### Design inputs already extracted for v1

From early-game manuals and refs — **ready for MVP spec**:

1. **Season loop:** calendar week → match → ratings → tables (CM1 manual)  
2. **Menus/screens:** CM1 menu extract maps to UI inventory  
3. **Rules scope:** 4 divisions, cups, Europe — Futtoboru scopes to **1888 league** (subset)  
4. **Multiplayer:** CM1 hotseat — optional v1.1  
5. **CM93 delta:** real names, `[TCT]` tactics screen, discipline — **post-v1** unless PO expands  

See [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) § CM1 for feature-level manual mapping.

---

## What remains open (not blocking pause)

Full inventory: [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md)

| Category | Examples | Priority when resumed |
|----------|----------|------------------------|
| **Mid-era manuals** | CM2 EN PDF, CM9798 OCR | CM2/9798 feature work |
| **Mid/late press** | PC Gamer CM2, PC Zone #73 CM3 | Reception docs |
| **CM4 depth** | Printed manual pp. 55–61, GameFAQs board | Post-v1 CM4 parity |
| **Binary samples** | cmmag-04 `.tac`, CM4 saves | Save/tactics RE phase |
| **Post-CM4** | CM03/04 patches, CM5 reception | Successor / FM handoff doc |
| **Citation chain** | champmanworld, blueyonder mirrors | Community patch archaeology |

**Binary reverse-engineering and decompiling** remain **last** — unchanged from user direction.

---

## How to resume research

1. Open [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md) — pick highest **P0/P1** row for the feature you are implementing.  
2. Log session in [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md) (Round N+1).  
3. Update [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md) and this file’s **Pause date / Final round** if un-pausing.  
4. For save/tactics binaries: [docs/research/saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md).

**Suggested first resume targets** (when needed, not now):

- **cmmag-04 extract** — if implementing CM4-style tactics import  
- **CM9798 manual OCR** — if implementing editor or Gen-2 engine features  
- **GameFAQs Wayback batch** — if validating CM3/CM4 community mechanics  

---

## Transition to MVP / v1 work

Research **informs** scope; it does **not** expand v1 by default.

| Document | Role in next phase |
|----------|-------------------|
| [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) | Acceptance criteria NG/PL/SC/SR |
| [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) | Code vs CM1/CM4 — what to build |
| [DEPARTMENT_*_V1.md](DOCUMENTATION_INDEX.md) | Department gates G1–G4 |
| [MVP_PRIORITY_ROADMAP.md](MVP_PRIORITY_ROADMAP.md) | **Stale** — refresh against gap analysis + RELEASE_V1 in next sprint |
| [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) | Screen/system status for MVP cut |

**Next phase:** **v1 implementation** — [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) · [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) (PO decisions locked 2025-06-25)

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Research **PAUSED** at Round 15; early games (FM82→CM93) sufficiency audit; MVP transition pointers |
