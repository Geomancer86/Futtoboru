# CM Legacy — Manuals Collection & Analysis

**Document type:** Research analysis (manual-derived features)  
**Local archive:** [docs/research/manuals/README.md](docs/research/manuals/README.md)  
**Index:** [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md)  
**Date:** 2025-06-25  
**Round:** 4 (manual acquisition + CM1 extraction)

---

## Executive summary

We now hold **offline copies** of official documentation for:

| Era | Title | Local file | Pages / size |
|-----|-------|------------|--------------|
| Pre-CM1 | Football Manager (1982) | HTML | Full playing instructions |
| Gen 1 | **CM1** (1992) | HTML + **menu extract** | **20 pages** — deepest v1 source |
| Gen 1 | CM93/94 | *Missing PDF* | Link only |
| Gen 2 | **CM2** (1995) | PDF (**French**) + EN guide | ~18 MB PDF |
| Gen 2 | **CM97/98** (1997) | PDF (EN) | ~6 MB |
| Gen 3 | **CM3** (1999) | PDF (EN) | **43 pages** |
| Gen 4 | **CM4** (2003) | ReadMe PDF (EN) | ~150 KB — not full manual |

**Key finding from CM1 manual:** CM1 already had board confidence, manager jobs, transfer bidding, insurance, fines, national squad views, and **1–10 ratings** — Futtoboru v1 should not assume CM1 was “tables only.”

---

## Precursor — Football Manager (1982)

**File:** `docs/research/manuals/precursors/football-manager-1982-spectrum/Football_Manager_1982_Spectrum_Manual.html`

### Game structure

| Part | Content |
|------|---------|
| Part 1 | Start — name, pick team (Div 4), difficulty |
| Part 2 | Main loop — **5 phases** per week |
| Part 3 | End of season — table, prize money, promotion/relegation (3 up/down), skill drift |

### Five phases (Part 2)

1. **Optional:** sell/list players, bank loan (by division limit), rename teams/players, save to tape  
2. **Match:** pick XI; team Energy/Morale/Defence/Midfield/Attack; 3D highlights  
3. **Results:** gate money  
4. **Weekly bills:** wages, 1% loan interest, balance  
5. **Transfer market:** bid for random player or sell to unlock market  

### Player model

- Skill **1–5**; Energy **1–20**  
- Roles: D / M / A  
- Season: 75 league + FA Cup matches → rollover  

**Futtoboru:** Proves v1 can ship with season loop + finances + table without CM3 depth.

---

## CM1 (1992) — full manual extract

**Files:** HTML manual + [CM1_Manual_Menu_Extract.md](docs/research/manuals/cm1/CM1_Manual_Menu_Extract.md)

### Main menu systems (official)

Continue · Tables · Fixtures · Club details · Match reports · Find player · Board/resign · Manager jobs · National squads · Save

### Not in earlier wiki notes — confirmed in manual

| Feature | Manual reference |
|---------|------------------|
| **Board confidence** | Main menu “Board/Resign” |
| **Manager jobs screen** | Vacancies + managers facing axe |
| **Transfer bidding rounds** | Pages 17–18 — multi-club offers, tribunal |
| **Player insurance** | STA menu |
| **Fine player (1 week wages)** | STA menu |
| **Domark Trophy** | Tables section |
| **Push/pull players in tactics** | FORW / BACK / NORM |
| **In-match tactical moves** | H/A substitution keys |

See full menu tree in extract file → map to [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) in Round 5.

---

## CM93/94 — gap

| Item | Status |
|------|--------|
| PDF manual | ❌ Not acquired |
| Lemon Amiga | Doc page exists; no stable direct PDF in crawl |
| Known additions vs CM1 | Reserve squads, injury time, international transfers (MobyGames) |

**Action:** Manual hunt via OpenRetro upload or physical scan; copy-protection score table on [StrategyWiki](https://strategywiki.org/wiki/Championship_Manager_93/Gameplay).

---

## CM2 (1995)

**File:** `Championship_Manager_2_Manual_FR.pdf` (French scan, English engine identical)

| Known from manual era + MobyGames | Notes |
|-----------------------------------|-------|
| Mouse-only UI | Text boxes |
| English + Scottish leagues | CD-ROM Laserlok |
| **Audio commentary** | Only CM2 in classic line (Clive Tyldesley) |
| Season limit ~2025 | Engine cap |
| European league add-ons | Separate products |

**Supplement:** `Championship_Manager_2_Guide_EN.txt` (12 KB community/guide from My Abandonware)

**Round 5:** OCR French PDF TOC → English feature list; cross-check [CM2_REFERENCE.md](CM2_REFERENCE.md).

---

## CM97/98 (1997)

**File:** `Championship_Manager_97_98_Manual_DOS_EN.pdf` (~6 MB)

| Manual-confirmed systems (Gen-2 finale) | |
|-------------------------------------------|--|
| **9 world leagues** | Up to **3 concurrent** on 16 MB RAM |
| **Data editor** | First in series |
| Set-piece takers | |
| Multi-league career | |
| No copy protection | Deliberate SI strategy |

Primary source for confirming Gen-2 before CM3 rewrite.

---

## CM3 (1999)

**File:** `Championship_Manager_3_Manual_Win_EN.pdf` (43 pages, 528 KB)

### Table of contents (from archive metadata + ManualMachine)

| Section | Futtoboru registry tag |
|---------|------------------------|
| Installation (Win 95/98) | platform |
| Start / restore saved game | save-load |
| **Network play** | defer |
| Hall of Fame | meta |
| Quick start | ux |
| Tactics chapter | tactics |
| Training chapter | training |
| Squad / transfer screens | transfers |

**Round 5:** Page-by-page extraction → registry (priority after CM1 extract merged).

---

## CM4 (2003)

**File:** `Championship_Manager_4_ReadMe_Win_EN.pdf` (~150 KB)

| Available | Missing |
|-----------|---------|
| Install, patches, system reqs | Full printed manual (~60+ pp per GameFAQs refs) |
| Enhancement pack notes | Tactics chapter (manual pp. 55–61 cited in FAQs) |

**Supplement:** Community FAQs reference in-box manual for player indicators — treat ReadMe as **technical doc only**.

See [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) + [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md).

---

## Acquisition matrix

| Title | PDF | HTML | In repo | Gap |
|-------|-----|------|---------|-----|
| FM 1982 | ❌ | ✅ | ✅ | PDF scan |
| CM1 | ❌ | ✅ | ✅ + extract | — |
| CM93/94 | ❌ | ⚠️ | ❌ | **Priority hunt** |
| CM2 | ✅ FR | — | ✅ | EN PDF |
| CM97/98 | ✅ EN | — | ✅ | — |
| CM3 | ✅ EN | — | ✅ | — |
| CM4 | ⚠️ ReadMe | — | ✅ ReadMe | Full manual |

---

## Research rounds

| Round | Status | Deliverable |
|-------|--------|-------------|
| 4 Manual acquisition | ✅ | This doc + `docs/research/manuals/` |
| 5 Deep analysis by release | ✅ | [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) |
| 5b Registry mapping | 🔲 | CM1 + CM3 → feature registry |
| 5b CM93 manual | 🔲 | Acquire PDF |
| 6 CM2 FR OCR / CM4 manual hunt | 🔲 | Optional |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 4 — local archive, CM1 full menu extract, acquisition matrix |
