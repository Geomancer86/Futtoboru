# CM Legacy — Research Coverage Audit

**Purpose:** Honest answer to “do we have everything?” for CM1–CM4 and pre-CM1 precursors.  
**Date:** 2025-06-25 · **Round:** 14 (cmmag-01 extract; cover CD index)  
**Related:** [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) · [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md)

---

## Executive answer

**No — we do not yet have complete, primary-source documentation for every early game.** The repo is strong on **CM1**, **CM2 expansions**, **CM3**, and **CM4 patches/community**, but several dimensions are thin or missing for **CM93/94**, **CM97/98**, **spin-offs**, and **contemporary press**.

| Dimension | Early games (FM82 → CM9798) | Later (CM3–CM4) |
|-----------|----------------------------|-----------------|
| **Full manuals in repo** | CM1 ✅ · CM93 text ✅ · CM2 FR only ⚠️ · CM9798 scan ⚠️ | CM3 ✅ · CM4 ReadMe only ⚠️ |
| **Patch story documented** | CM1 N/A · CM93 SKU updates ⚠️ · CM2 SKU + cm2fix ⚠️ · CM9798 “no patches” only | CM4 EP1–5 ✅ · CM3 partial ✅ |
| **Expansion / data-disk story** | CM2 European Leagues ✅ · CM93 spin-offs listed ⚠️ · CM9798 as final SKU ⚠️ | CM3 seasonal family ✅ · CM4 successor 03/04 ✅ |
| **Era reviews (scores + themes)** | CM1 **The One #46 primary ✅** · **CM93 five UK mags primary text ✅** · **CM2 PC Zone #31 ✅** · CM9798 shallow | CM3 PC Zone ✅ · CM4 multi-outlet ✅ |
| **User feeling of the era** | CM1 modest · CM93 breakthrough narrative ⚠️ · CM9798 cult ⚠️ | CM3 golden age ✅ · CM4 launch crisis ✅ |
| **Save/tactics binaries (RE)** | CM1/CM93 saves ❌ · CM9798 saves ❌ | CM4 `.tac`/saves ❌ · CM01/02 **tactic exes + editor docs ✅** · **cmmag-04/05 catalogued ⚠️** |

**Bottom line:** Manual *content* for CM1 and CM93 is now usable for design research. **Patch/expansion chronology** and **period reception** need a dedicated pass for every SKU — see [CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md](CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md) and [CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md](CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md). **Binary save/tactics samples** for reverse-engineering are tracked in [docs/research/saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md).

---

## Coverage matrix (Game × dimension)

Depth: ★★★★★ Deep · ★★★★☆ Medium–Deep · ★★★☆☆ Medium · ★★☆☆☆ Shallow · ★☆☆☆☆ Missing

| Game | Manual | Patches | Expansions | Era reviews | User reception | Dedicated ref doc |
|------|:------:|:-------:|:----------:|:-----------:|:--------------:|:-----------------:|
| **FM 1982** | ★★★★★ | N/A | ★★★☆☆ | ★★★★☆ | ★★★★☆ | [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) |
| **FM WCE 1990** | ★★★☆☆ PDF | — | — | ★☆☆☆☆ | ★☆☆☆☆ | Precursor README only |
| **CM1 (1992)** | ★★★★★ | ★★★★☆ (none) | ★★★☆☆ | ★★★★☆ | ★★★★☆ | [CM1_REFERENCE.md](CM1_REFERENCE.md) |
| **CM93/94** | ★★★★☆ text + extract | ★★★☆☆ | ★★★★☆ | ★★★★☆ | ★★★☆☆ | [CM93_94_REFERENCE.md](CM93_94_REFERENCE.md) |
| **CM Italia / Norge / Guy Roux** | ★☆☆☆☆ | ★☆☆☆☆ | ★★★☆☆ | ★☆☆☆☆ | ★★☆☆☆ | [CM_ITALIA_SPINOFFS_REFERENCE.md](CM_ITALIA_SPINOFFS_REFERENCE.md) |
| **CM2 (1995)** | ★★★☆☆ FR only | ★★★☆☆ | ★★★★★ | ★★★☆☆ | ★★★☆☆ | [CM2_REFERENCE.md](CM2_REFERENCE.md) |
| **CM96/97 data** | ★☆☆☆☆ | ★★☆☆☆ | ★★★☆☆ | ★☆☆☆☆ | ★★☆☆☆ | CM2_REFERENCE §5 |
| **CM97/98** | ★★☆☆☆ scan | ★★☆☆☆ | ★★★☆☆ | ★★☆☆☆ | ★★★☆☆ | [CM9798_REFERENCE.md](CM9798_REFERENCE.md) **NEW** |
| **CM3** | ★★★★☆ | ★★★☆☆ | ★★★★★ | ★★★☆☆ | ★★★★★ | [CM3_REFERENCE.md](CM3_REFERENCE.md) |
| **CM4** | ★★☆☆☆ ReadMe | ★★★★★ | ★★★★☆ | ★★★★★ | ★★★★★ | [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) + community doc |

---

## What we have vs what “complete” means

### Manuals (local archive)

See [docs/research/manuals/README.md](docs/research/manuals/README.md).

| Have full or near-full manual body | Partial / scan only | Not in repo |
|--------------------------------------|---------------------|-------------|
| FM 1982 HTML · CM1 HTML + extract · CM93 text extract · CM3 PDF + TOC · CM9798 image PDF · CM4 ReadMe | CM2 French PDF (not OCR’d) · FM WCE PDF (not analysed) | CM2 EN PDF · CM4 printed manual · CM93 PDF scan |

### Patch story (every game)

| Game | Documented? | Gap |
|------|-------------|-----|
| CM1 | ✅ No patches; superseded by CM93 | — |
| CM93/94 | ⚠️ Update disks named; no version table | End-of-season edition versioning (v4.04) |
| CM2 | ⚠️ Bosman in later SKUs; LaserLok + cm2fix | No dated patch list per v1.37 / v1.56 / v1.57 |
| CM9798 | ⚠️ “No copy protection” only | No official errata list |
| CM3 | ✅ Culture vs CM4; covermounts | Per-build fix list incomplete |
| CM4 | ✅ EP1–5 deep | EP4 changelog thin |

**Unified chronology:** [CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md](CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md)

### Expansion / data-disk story

| Well documented | Listed only | Missing |
|-----------------|-------------|---------|
| CM2 European Leagues CD · CM3 seasonal family · CM4 EPs + 03/04 successor | CM Italia/Norge/Guy Roux · CM93 collection/update disks · CM96/97 | Per-SKU install sheets · Italian foreign-player rules primary source |

### Era reviews & user feeling

| Well documented | Partial | Missing |
|-----------------|---------|---------|
| CM4 launch press · CM3 PC Zone · **CM93 AF+CU+AC+The One+AP OCR (R9–12)** · **CM1 The One #46 OCR (R11)** | CM1 polarized low scores · CM9798 “#1 UK” claim | CM2 PC Gamer/PC Attack · **CVG #140 CM93** |

**Unified table:** [CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md](CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md)

---

## Round 7 deliverables (this commit)

| Doc | Purpose |
|-----|---------|
| This audit | Honest matrix + priorities |
| [CM93_94_REFERENCE.md](CM93_94_REFERENCE.md) | Dedicated Gen-1 sequel reference |
| [CM9798_REFERENCE.md](CM9798_REFERENCE.md) | Dedicated CM2-engine finale reference |
| [CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md](CM_LEGACY_PATCHES_AND_EXPANSIONS_CHRONOLOGY.md) | Patch + expansion timeline all titles |
| [CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md](CM_LEGACY_ERA_REVIEWS_AND_RECEPTION.md) | Contemporary scores + player sentiment |

---

## Round 8 deliverables (this commit)

| Doc | Purpose |
|-----|---------|
| [CM93_Manual_Menu_Extract.md](docs/research/manuals/cm93-94/CM93_Manual_Menu_Extract.md) | Structured CM93 menu tree (like CM1) |
| [CM9798_Manual_Metadata.md](docs/research/manuals/cm97-98/CM9798_Manual_Metadata.md) | 36-page scan metadata; OCR backlog |
| [CM_ITALIA_SPINOFFS_REFERENCE.md](CM_ITALIA_SPINOFFS_REFERENCE.md) | Italia / Norge / Guy Roux |
| CM2 + CM9798 review tables | Updated in era reviews + CM2/CM9798 refs |
| CM '94 mail-in update | Patches chronology + CM93_94_REFERENCE |

---

## Round 14 deliverables (this commit)

| Doc / asset | Purpose |
|-------------|---------|
| `saves-and-tactics/samples/` | GK Save Editor FAQ+CHANGES; 3× CM0102 tactic PE installers; CM0102 doc |
| [CM_MAGAZINE_COVER_CD_INDEX.md](docs/research/saves-and-tactics/CM_MAGAZINE_COVER_CD_INDEX.md) | cmmag-01–05 metadata; #4/#5 = CM4 `.tac` source |
| `_staging/cmmag-01/CMMag01.iso` | Local mount source (gitignored) |
| Ledger + saves README | SRC-BIN-* updates; CVG140 still missing |

---

## Round 13 deliverables

| Doc / asset | Purpose |
|-------------|---------|
| [docs/research/saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md) | Save/tactics format table + RE hunt queue |
| `casperlab_cm4save_index_wayback.html` | CM4 save editor v0.02 documentation |
| Ledger § Save games & tactics | SRC-BIN-* registry; cmmag-01 remote catalog |
| Coverage audit | Save/tactics dimension added to executive matrix |

---

## Round 12 deliverables

| Doc / asset | Purpose |
|-------------|---------|
| `Amiga_Power_027_Jul_1993_djvu.txt` | CM93 primary review (Tim Norris); fifth UK Amiga outlet |
| `nygreen_*` + `jonnyrivers_home_wayback.html` | CM4 pre-game editor community captures |
| Ledger + extracts updated | AP27 found in `amiga-power-pdffromhi` collection; CVG140 still absent |

---

## Round 11 deliverables

| Doc / asset | Purpose |
|-------------|---------|
| `The_One_046_Jul_1992_djvu.txt` | CM1 primary review (Gary Whitta, 89%); resolves TONE056 citation |
| Ledger + extracts updated | AP27 coverdisk false positive; CVG140 not in cvg-magazine collection |
| Dobbers URL status | cmeuphoria DEAD; belangrijk 404 |

---

## Round 10 deliverables

| Doc / asset | Purpose |
|-------------|---------|
| `Amiga_Computing_062_Jul_1993_djvu.txt` | CM93 primary review (Jonathan Maddock) |
| `The_One_056_May_1993_djvu.txt` | CM93 primary review (Simon Byron); cites CM1 Gary Whitta review |
| Ledger + extracts updated | ig1234/shy citation chain; score-box OCR gaps noted |

---

## Round 9 deliverables

| Doc / asset | Purpose |
|-------------|---------|
| [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md) | Master source registry + citation-chain missing queue |
| [docs/research/sources/README.md](docs/research/sources/README.md) | Local source file inventory |
| [MAGAZINE_REVIEW_EXTRACTS.md](docs/research/sources/magazines/MAGAZINE_REVIEW_EXTRACTS.md) | CM93 AF+CU, CM2 PC Zone primary quotes |
| Magazine OCR ×5 | AF48, CU040, PCZ031/032/035 |
| GameFAQs Wayback ×2 | CM4 ig1234 + shy |
| Dobbers CM4 FAQ | Community source + 15+ cited URLs queued as MISSING |

---

## Round 10+ backlog

See [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md) § Round 10+ priorities. Summary:

| Priority | Task | Primary sources |
|----------|------|-----------------|
| **P0** | Extract **`cmmag-04`** → CM4 `.tac` + training | [saves-and-tactics](docs/research/saves-and-tactics/README.md) |
| **P0** | OCR CM9798 + CM2 FR manuals | Archive.org djvu / pdftotext |
| **P1** | CM4 `.tac` + mid-season save samples | SRC-BIN-CM4-* |
| **P1** | CVG #140 Jul 1993 CM93 review (ABIME / alternate scan) | SRC-MAG-CVG140 |
| **P1** | PC Gamer #24 CM2 review scan | SRC-MAG-PCG024 |
| **P1** | GameFAQs Wayback batch (CM3, CM0203, CM9798) | SRC-GFQ-* |
| **P1** | Dobbers citation chain (editors, patches, cmskins) | Wayback |
| **P2** | CM4 printed manual (tactics pp. 55–61) | Redump, GameFAQs |
| **P2** | FM WCE manual analysis § in PRE_CM1 | Local PDF |
| **P3** | Resolve PC Zone CM2 97% vs 92% | PCZ031 PDF score box |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 14 — cmmag-01 samples; CM Magazine CD index |
| 2025-06-25 | Round 13 — saves/tactics RE track; coverage audit sync |
| 2025-06-25 | Round 12 — AP #27 CM93 OCR; nygreen/jonnyrivers Wayback |
| 2025-06-25 | Round 11 — The One #46 CM1 OCR; AP27/CVG140 hunt notes |
| 2025-06-25 | Round 10 — AC062 + The One #56 OCR; CM1 review citation chain |
| 2025-06-25 | Round 9 — source ledger; magazine OCR; GameFAQs Wayback |
