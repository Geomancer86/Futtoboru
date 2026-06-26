# CM Legacy — Research Backlog (What Is Still Missing)

**Purpose:** Single **honest inventory** of documents, sources, and downloads still wanted for CM1–CM4 (+ precursors and CM03/04 successor) **before** any binary reverse-engineering or decompiling work.

**Status hub:** [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md)  
**Source registry:** [CM_LEGACY_SOURCE_LEDGER.md](CM_LEGACY_SOURCE_LEDGER.md)  
**Save/tactics acquisition only:** [docs/research/saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md)  
**Last updated:** 2025-06-25 · **Round:** 15

---

## Executive answer

**No — research is not complete.** The repo is strong on **CM1/CM3/CM4 reference prose**, **CM93 UK magazine reviews (except one)**, and **CM4 community FAQs**. Gaps cluster in:

1. **Manuals** — CM2 EN PDF, CM4 printed manual, CM9798 OCR text, FM WCE analysis  
2. **Press** — CVG #140 (CM93), CM2 PC Gamer/PC Attack, CM9798/CM3 primary review OCR  
3. **Guides & GameFAQs** — CM3/CM0203/CM9798 FAQ Wayback batch; CM4 board threads  
4. **Community primary URLs** — Dobbers citation chain (patches, editors, mirrors)  
5. **Binary samples (store only)** — CM4 `.tac`, saves, cmmag-04/05 disc extracts — **no format RE yet**  
6. **Newer-era handoff** — CM Season 03/04 patch/readme depth, CM5 (Eidos) reception, official SI CM03/04 docs  

---

## Coverage snapshot

| Dimension | Strong | Partial | Missing |
|-----------|--------|---------|---------|
| **Manuals** | CM1, CM3 PDF, CM93 text | CM2 FR, CM9798 scan, CM4 ReadMe | CM2 EN, CM4 print (pp. 55–61 tactics) |
| **Reviews (primary text)** | CM93 ×5 UK, CM2 PCZ #31, CM1 TONE #46 | CM9798, CM3 (score cited, no OCR body) | **CVG #140**, PC Gamer #24, PC Attack #7 |
| **GameFAQs / guides** | CM4 ig1234 + shy | — | CM3, CM0203, CM9798, CM4 board 33189 |
| **Forums / fan sites** | Dobbers FAQ | nygreen, jonnyrivers, casperlab index | champmanworld zips, Dugout threads, cmskins |
| **Patches chronology** | CM4 EP1–5 | CM2/CM93 SKU | Per-build CM2 list, CM9798 errata |
| **Saves / tactics (files)** | GK editor docs, CM0102 tactic exes | cmmag-01 partial | CM4 `.tac`, `.sav`, cmmag-04/05 discs |
| **Post-CM4 metadata** | CM_LEGACY_REFERENCE timeline | CM03/04 in refs | CM5 reviews, 03/04 patch primary docs |

---

## 1. Manuals & official documentation

| ID | Item | Game | Priority | Hunt vector |
|----|------|------|----------|-------------|
| SRC-MAN-CM2-EN | English manual PDF | CM2 | **P1** | My Abandonware, archive.org, ManualMachine |
| SRC-MAN-CM4-PRINT | Printed manual (tactics pp. 55–61) | CM4 | **P1** | Redump, eBay scans, GameFAQs quotes |
| SRC-MAN-CM9798-OCR | Manual body as text | CM9798 | **P0** | pdftotext / archive.org djvu if uploaded |
| SRC-MAN-CM93-PDF | Full PDF scan (not Lemon text) | CM93 | **P2** | ABIME, private scans |
| SRC-MAN-FMWCE | FM World Cup Edition analysis § | Precursor | **P2** | Local PDF exists — write PRE_CM1 section |
| SRC-MAN-CM2-FR-OCR | French manual OCR | CM2 | **P2** | pdftotext on local FR PDF |
| SRC-DOC-CMTECH-FAQ | CM Technical FAQ PDF | CM4 era | ✅ **R15** | `docs/research/sources/community/CM_Tech_FAQ_magazine_disc.pdf` |

---

## 2. Contemporary reviews & press

| ID | Outlet | Game | Score | Priority | Status |
|----|--------|------|-------|----------|--------|
| SRC-MAG-CVG140 | CVG #140 Jul 1993 | CM93 | 85% | **P1** | ❌ Not in archive.org `cvg-magazine` |
| SRC-MAG-PCG024 | PC Gamer #24 Nov 1995 | CM2 | 49% | **P1** | ❌ No UK scan found |
| SRC-MAG-PCATTACK7 | PC Attack #7 Nov 1995 | CM2 | 83% | **P1** | ❌ |
| SRC-MAG-PCF-1997 | PC Format ~1997 | CM9798 | — | **P2** | ❌ |
| SRC-MAG-PCZ-1998-CM2 | PC Zone Nov 1998 retrospective | CM2 | 90%? | **P2** | ❌ Verify issue vs Oct 1995 97% review |
| SRC-MAG-PCZ073 | PC Zone #73 | CM3 | 93% | **P1** | ❌ No OCR in repo (cited in refs only) |
| SRC-MAG-AF108 | Amiga Format #108 | CM2 port | 12% | **P3** | ❌ |

**Have primary OCR:** CM93 — AF #48, CU #40, AC #62, The One #56, AP #27 · CM1 — The One #46 · CM2 — PC Zone #31 (+ digest #32/#35).

---

## 3. GameFAQs, guides & strategy docs

| ID | Target | Priority | Status |
|----|--------|----------|--------|
| SRC-GFQ-CM3-FAQS | CM3 formation / massive FAQs | **P1** | ❌ Wrong slugs 404 on Wayback |
| SRC-GFQ-CM0203 | CM 02/03 guides | **P1** | ❌ |
| SRC-GFQ-CM9798 | CM97/98 guides | **P1** | ❌ |
| SRC-GFQ-CM4-BOARD | Board #33189 threads | **P1** | ❌ Wayback hunt |
| SRC-GFQ-CM4-MANUAL-PP | Manual tactics pages text | **P1** | ❌ |
| SRC-GFQ-CM4-IG1234 | ig1234 FAQ | — | ✅ Wayback |
| SRC-GFQ-CM4-SHY | shy guide | — | ✅ Wayback |
| SRC-GFQ-CM93 | CM93 walkthrough | P3 | ❌ StrategyWiki substitute only |

**Prima / official strategy:** archive.org has *Championship Manager Season 99/00* Prima guide (`championshipmana0000evan`) — not yet pulled.

---

## 4. Forums, fan sites & citation chain (from Dobbers / ig1234)

| URL / resource | Status | Priority |
|----------------|--------|----------|
| champmanworld.com downloads (EP5 mirrors) | ❌ PHP redirects | P1 |
| cmskins.com | ❌ DEAD? | P2 |
| cm4update.co.uk | ❌ | P2 |
| mitchellsclose LLM generator | ❌ | P2 |
| blueyonder FTP patches (408_enhance*) | ❌ DEAD | P1 |
| casperlab save editor zip | ❌ Wayback shell | P2 (index ✅) |
| cmeuphoria / belangrijk | ❌ DEAD | — |
| shy.host.sk, sigames.com CM4 pages | ❌ | P2 |
| SI official CM4 FAQ forum | ❌ DEAD (404 shell saved) | — |
| nygreen.net / jonnyrivers.com | ✅ Wayback R12 | — |

---

## 5. Patches, data updates & expansion primary docs

| Game | Have | Missing |
|------|------|---------|
| CM4 | EP1–5 deep in community doc | EP4 full changelog primary |
| CM3 | Seasonal family narrative | Per-build fix list |
| CM2 | Bosman / cm2fix notes | Dated patch list v1.37→v1.57 |
| CM93/94 | Mail-in update named | End-of-season v4.04 table |
| CM9798 | “No copy protection” | Official errata list |
| CM03/04 | Successor mentioned | **Readme/patch primary docs**, data update packs |

---

## 6. Save games, tactics & tools (acquire files only — RE later)

| ID | Target | Priority | Status |
|----|--------|----------|--------|
| SRC-BIN-CMMAG04 | cmmag-04 ISO → **CM4 `.tac` + training** | **P0** | REMOTE — metadata confirmed |
| SRC-BIN-CMMAG05 | cmmag-05 ISO → CM4 + CM03/04 tactics | **P1** | REMOTE |
| SRC-BIN-CMMAG02 | cmmag-02 → GK CM3 editor 3.9, CM0102 tactics | **P1** | REMOTE — catalogued R15 |
| SRC-BIN-CM4-SAV | Mid-season CM4 save folder | P1 | ❌ |
| SRC-BIN-CM0102-SAV | Uncompressed `.sav` | P1 | ❌ |
| SRC-BIN-CM93-SAV / CM1-SAV | Early saves | P2 | ❌ |
| SRC-TOOL-GK-CM3-EDITOR | SETUP.EXE from magazine CDs | P2 | FAQ only in repo |

See [CM_MAGAZINE_COVER_CD_INDEX.md](docs/research/saves-and-tactics/CM_MAGAZINE_COVER_CD_INDEX.md).

---

## 7. Newer games & post-CM4 metadata (still in scope)

These are **documentation/reception** targets, not Futtoboru v1 gameplay scope:

| Title | What we need | Have today |
|-------|--------------|------------|
| **CM Season 03/04** (2004) | Patch notes, readme, community migration guides | Timeline in CM_LEGACY_REFERENCE only |
| **CM5** (2005, Eidos) | Contemporary reviews, failure narrative | Brief in FM bible only |
| **CM 2010** (Eidos) | Separate 3D engine note | One line in FM bible |
| **CM0102 free release (2008)** | Community impact docs | Champman0102 cited, not archived |
| **CM3 seasonal updates** (99/00→01/02) | Per-SKU feature deltas primary | CM3_REFERENCE tables (secondary) |

---

## 8. Explicitly deferred (not this research phase)

| Work | Why deferred |
|------|----------------|
| Save/tactics **binary format RE** | User direction: hard analysis at end |
| Graeme Kelly editor **disassembly** | After samples + docs complete |
| CM0102 offset patching tutorials | Consulted via links only |
| MobyGames **live scrape** | Cloudflare — use AMR/UVList + primary scans |
| Full **cmmag ISO collection** in git | ISOs in `_staging/` only; samples/manifests committed |

---

## Round 15+ recommended hunt order

| Round focus | Actions |
|-------------|---------|
| **R15–16** | Pull **cmmag-04** tactics/schedules; CM Tech FAQ ✅; backlog doc ✅ |
| **R16–17** | GameFAQs Wayback when archive stable; CM3 PC Zone #73 OCR hunt |
| **R17–18** | Dobbers URL batch (champmanworld, blueyonder mirrors on archive.org) |
| **R18–19** | CM9798 manual OCR; CM2 EN manual |
| **Ongoing** | CVG #140 via ABIME / non–archive.org collections |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 15 — master backlog created; CM Tech FAQ from cmmag-01; cmmag-02 catalogued |
