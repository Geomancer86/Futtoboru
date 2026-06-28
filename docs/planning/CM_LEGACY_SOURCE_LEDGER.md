# CM Legacy — Source Ledger

**Purpose:** Single registry of **every source consulted, acquired, blocked, or missing** for CM1–CM4 research. Prevents duplicate hunts; tracks **citation chains** (when source A cites B and B is absent, B goes on the missing queue).

**Parent:** [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md)  
**Pause point:** [CM_LEGACY_RESEARCH_PAUSE.md](CM_LEGACY_RESEARCH_PAUSE.md) — **active hunting stopped Round 15**  
**Local files:** [docs/research/sources/README.md](docs/research/sources/README.md)  
**Date opened:** 2025-06-25 · **Final active round:** 15 · **Paused:** Round 16

---

## How to use this ledger

1. **Before hunting:** search this file and [docs/research/sources/README.md](docs/research/sources/README.md) for the source ID or URL.
2. **After acquiring:** set status to `ACQUIRED`, add local path, note extraction done in session log.
3. **When a source cites another:** add cited source to § Citation chain queue if not already `ACQUIRED` or `CONSULTED`.
4. **Do not re-download** items in § Do-not-repeat (manuals with SHA256 match, duplicate magazine OCR).

### Status legend

| Status | Meaning |
|--------|---------|
| **ACQUIRED** | Full or usable copy in repo under `docs/research/sources/` or `docs/research/manuals/` |
| **CONSULTED** | Read for facts; no local archive (or summary only in ref docs) |
| **PARTIAL** | Fragment, digest, or OCR with gaps |
| **MISSING** | Identified but not in repo; actively wanted |
| **BLOCKED** | Fetch failed (403, Cloudflare, dead FTP); workaround noted |
| **DEAD** | URL/FTP confirmed gone; Wayback only path |

---

## Round 9 session log

| When | Action | Result |
|------|--------|--------|
| R9 | Created `docs/research/sources/` tree + this ledger | ✅ |
| R9 | Fetched Dobbers CM4 FAQ (`cm4.htm.html`) | ✅ ACQUIRED — rich citation chain |
| R9 | Wayback SI official CM4 FAQ | ⚠️ PARTIAL — 404 page saved; topic dead on archive |
| R9 | Wayback GameFAQs `ig1234` + `shy` CM4 FAQs | ✅ ACQUIRED |
| R9 | Live GameFAQs / MobyGames / Lemon Amiga curl | ❌ BLOCKED (403 / Cloudflare) — use Wayback or browser |
| R9 | Archive.org AF #48 Jul 1993 OCR | ✅ ACQUIRED — CM93 review body extracted (82%, Stephen Bradley) |
| R9 | Archive.org CU Amiga #40 Jun 1993 OCR | ✅ ACQUIRED — CM93 review body extracted (84%, Mark Patterson, p.66) |
| R9 | Archive.org PC Zone #31 Oct 1995 OCR | ✅ ACQUIRED — CM2 **full review** (97% index; ~pp.97) |
| R9 | Archive.org PC Zone #32 Nov 1995 OCR | ✅ ACQUIRED — CM2 digest entry **92% Classic** (roundup text only) |
| R9 | Archive.org PC Zone #35 Feb 1996 OCR | ✅ ACQUIRED — CM2 digest duplicate of #32 text; Issue 31/32 roundups |
| R9 | Archive.org Amiga Computing #62 Jul 1993 | ❌ MISSING — item has coverdisk metadata only; no `djvu.txt` on item |
| R9 | Archive.org The One Amiga #56 May 1993 | ❌ MISSING — no matching magazine item found |
| R9 | CM9798 manual OCR | ❌ BLOCKED — no Python/tesseract in environment |
| R9 | Deleted bad downloads | `CU_Amiga_040_djvu.txt` (404 HTML), failed AC062 fetch |

## Round 10 session log

| When | Action | Result |
|------|--------|--------|
| R10 | Archive.org `amiga-computing-magazine-062` OCR | ✅ ACQUIRED — CM93 review body (~45870–46285); **score box missing in OCR** (92% via UVList) |
| R10 | Archive.org `theone-magazine-56` OCR | ✅ ACQUIRED — CM93 review body (~15050–15230); **score sidebar garbled** (80% via AMR) |
| R10 | ig1234 cites `geocities.com/ig12345/ig1234CM4.txt` | ❌ BLOCKED — Wayback redirects to Yahoo homepage |
| R10 | ig1234 cites `403_enhance.exe` on blueyonder | ❌ DEAD — queued; EP2 patch binary |
| R10 | ig1234 cites GameFAQs board `board=33189` | ❌ MISSING — Wayback hunt |
| R10 | shy guide cites `shy.host.sk`, `sigames.com` | ❌ MISSING — Wayback |
| R10 | The One #56 cites Gary Whitta CM1 review (July, 89%) | ❌ MISSING — `SRC-MAG-TONE-CM1-JUL1992` (issue TBD) |
| R10 | GameFAQs CM3 FAQ list (IDs 562287, 914433) | ❌ 404 on Wayback — slug hunt continues |
| R10 | GameFAQs CM3 FAQ list (IDs 562287, 914433) | ❌ 404 on Wayback — slug hunt continues |
| R10 | PC Gamer #24 Nov 1995 UK | ❌ MISSING — no `pc-gamer-magazine-uk` collection hit |

## Round 11 session log

| When | Action | Result |
|------|--------|--------|
| R11 | Archive.org `theone-magazine-46` Jul 1992 OCR | ✅ ACQUIRED — CM1 review Gary Whitta (~18334–18608, pp.92–93); **89** in score column ~27511 |
| R11 | The One #56 citation resolved | `SRC-MAG-TONE-CM1-JUL` → **same as** `SRC-MAG-TONE046` (issue #46, not separate July SKU) |
| R11 | `Amiga_Power_27_1993_Future_Publishing_GB_Jul_1993` | ❌ **Wrong item** — coverdisk ADF only, no magazine OCR; full AP #27 still **MISSING** |
| R11 | CVG #140 Jul 1993 (`cvg-magazine` collection) | ❌ MISSING — 540-issue collection has no #140 identifier match |
| R11 | PC Gamer #24 Nov 1995 UK | ❌ MISSING — no Imagine UK issue scan found |
| R11 | Wayback `belangrijk.net/mc/` (Mind Compression) | ❌ 404 (2012 snapshot) |
| R11 | Wayback `cmeuphoria.co.uk` | ❌ DEAD — 2011 snapshot is parked-domain frameset only |
| R11 | Wayback GameFAQs board 33189 / casperlab zip | ❌ 404 / redirect shell — not archived |

## Round 12 session log

| When | Action | Result |
|------|--------|--------|
| R12 | Archive.org `amiga-power-pdffromhi-000_202601` → `AmigaPowerPdffromhi027_djvu.txt` | ✅ ACQUIRED — CM93 review Tim Norris (~12404–12626); **80%** via AMR (score box OCR gap) |
| R12 | R11 false negative resolved | Full AP #27 is in **`amiga-power-pdffromhi`** collection, not `Amiga_Power_27_*` coverdisk item |
| R12 | CVG #140 Jul 1993 | ❌ MISSING — `cvg-magazine` has no issue #140 title/identifier (540 issues; #141 Aug 1993 present) |
| R12 | PC Gamer #24 Nov 1995 UK | ❌ MISSING — no Imagine UK scan on archive.org |
| R12 | Wayback `nygreen.net` homepage + `program.php?id=2` | ✅ ACQUIRED — CM4 Pregame Editor v0.04 feature list (Michael Nygreen) |
| R12 | Wayback `jonnyrivers.com` homepage | ✅ ACQUIRED — CM4 editor **v0.09b** download link cited (31/5/2003) |
| R12 | Wayback `belangrijk.net/mc/` re-check | ❌ DEAD — 404 shell (not saved) |

## Round 13 session log

| When | Action | Result |
|------|--------|--------|
| R13 | Opened `docs/research/saves-and-tactics/` track | ✅ README — format table + hunt queue |
| R13 | Wayback casperlab `/cm4save/` index | ✅ ACQUIRED — editor v0.02 docs player-attribute edits |
| R13 | Wayback casperlab `cm4saveeditor.zip` | ❌ BLOCKED — Wayback serves HTML wrapper (~1.7 KB), not zip |
| R13 | Archive.org `cmmag-01` … `cmmag-05` | ⚠️ REMOTE — Issue 1 ISO lists **Graeme Kelly CM3 save editor** + CM01/02 **tactics**; not extracted locally |
| R13 | `.tac` / CM4 save binary search (archive.org) | ❌ MISSING — no indexed `.tac` uploads found |
| R13 | champmanworld downloads Wayback | ❌ BLOCKED — PHP redirects only; no zip captures in CDX |
| R13 | CM4 tactics format | **CONSULTED** — `.tac` in `user data/tactics` (CM Tactics Centre FAQ, 2003) |
| R13 | CM01/02 RE docs | **CONSULTED** — Champman0102 offset index + nckstwrt GitHub (not archived locally) |
| R13 | Shell note | Round 12 parallel `curl \| Select-Object` hung ~20 min (4 terminals); use `Invoke-WebRequest -TimeoutSec` on Windows |

## Round 14 session log

| When | Action | Result |
|------|--------|--------|
| R14 | Download + mount `cmmag-01` ISO | ✅ Local `_staging/` (372 MB, gitignored) |
| R14 | Extract samples to `saves-and-tactics/samples/` | ✅ GK Save Editor FAQ+CHANGES; 3× CM0102 tactic `.exe`; CM0102 description doc |
| R14 | Disc inventory | 15× CM0102 tactic PE installers in `Tactics/`; SaveGameEditorv3.8 for CM9899–0102 |
| R14 | [CM_MAGAZINE_COVER_CD_INDEX.md](docs/research/saves-and-tactics/CM_MAGAZINE_COVER_CD_INDEX.md) | ✅ cmmag-01–05 metadata; **#4/#5 list native CM4 `.tac`/schedules** |
| R14 | CVG #140 archive identifier search | ❌ MISSING — no `*CVG*140*` item; Jul 1993 issue absent from `cvg-magazine` |
| R14 | GameFAQs CM3 Wayback CDX | ❌ BLOCKED — web.archive.org connection failed R14 |

## Round 15 session log

| When | Action | Result |
|------|--------|--------|
| R15 | Created [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md) | ✅ Master missing inventory by category + game + deferred RE |
| R15 | Extract `CMFaq/CM_Tech_FAQ.pdf` from cmmag-01 | ✅ ACQUIRED — `docs/research/sources/community/CM_Tech_FAQ_magazine_disc.pdf` |
| R15 | Catalog `cmmag-02` metadata | ✅ GK CM3 editor 3.9 + CM01/02 tactics on disc |
| R15 | PC Zone #73 CM3 review OCR hunt | ❌ MISSING — not in `pc-zone-magazine` collection by issue # |
| R15 | GameFAQs CM3 Wayback | ❌ BLOCKED — archive.org connection failed (retry when resumed) |

## Round 16 session log (research pause — no new acquisitions)

| When | Action | Result |
|------|--------|--------|
| R16 | Created [CM_LEGACY_RESEARCH_PAUSE.md](CM_LEGACY_RESEARCH_PAUSE.md) | ✅ Official stop point + early games (FM82→CM93) sufficiency audit |
| R16 | Early games gap review | ✅ **No v1 blockers** — CM1 manual/menu/ref + CM93 manual/reviews sufficient |
| R16 | Mark backlog + audit PAUSED | ✅ Resume via backlog P0/P1 when implementation needs sources |
| R16 | Transition pointer | → MVP / [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) next phase |

---

## Do-not-repeat (already researched — skip unless checksum fails)

| Asset | Location | Notes |
|-------|----------|-------|
| CM93 Lemon Amiga manual text | `docs/research/manuals/cm93-94/` | Round 6 |
| CM93 menu extract | `CM93_Manual_Menu_Extract.md` | Round 8 |
| CM9798 manual PDF (36 pp scan) | `Championship_Manager_97_98_Manual_DOS_EN.pdf` | Deduped hash Round 6–7 |
| CM1 HTML manual + menu extract | `docs/research/manuals/cm1/` | Round 4–5 |
| CM3 PDF + TOC | `docs/research/manuals/cm3/` | Round 4 |
| FM WCE PDF | `docs/research/manuals/` | Round 6 — analysis still pending |
| CM4 ReadMe | `docs/research/manuals/cm4/` | Partial only |
| MobyGames critic tables | Summarized in era refs | **CONSULTED** via web — not archived (BLOCKED curl) |
| UVList / AMR score aggregates | Era reviews doc | **CONSULTED** — primary scan text still wanted per outlet |
| End-of-Season / Collection disk archive IDs | Patches chronology | **CONSULTED** Round 7 — disk images not pulled locally |

---

## Local archive inventory (Round 9 additions)

See full file list: [docs/research/sources/README.md](docs/research/sources/README.md)

| Source ID | File | Size | Games |
|-----------|------|------|-------|
| SRC-COMM-DOBBERS-CM4 | `community/Dobbers_CM4_FAQ_cm4.htm.html` | ~14 KB | CM4 |
| SRC-FORUM-SI-CM4-FAQ | `forums/SIGames_Official_CM4_FAQ_wayback.html` | Wayback shell | CM4 |
| SRC-GFQ-CM4-IG1234 | `gamefaqs/CM4_FAQ_ig1234_wayback.html` | ~80 KB | CM4 |
| SRC-GFQ-CM4-SHY | `gamefaqs/CM4_shy_guide_wayback.html` | ~112 KB | CM4 |
| SRC-MAG-AF048 | `magazines/Amiga_Format_048_1993_07_djvu.txt` | ~1.1 MB | CM93 |
| SRC-MAG-CU040 | `magazines/CU_Amiga_040_Jun_1993_djvu.txt` | ~1.0 MB | CM93 |
| SRC-MAG-PCZ031 | `magazines/PC_Zone_31_Oct_1995_djvu.txt` | ~553 KB | CM2 |
| SRC-MAG-PCZ032 | `magazines/PC_Zone_32_Nov_1995_djvu.txt` | ~642 KB | CM2 |
| SRC-MAG-PCZ035 | `magazines/PC_Zone_35_1996_02_djvu.txt` | (prior) | CM2 |
| SRC-MAG-AC062 | `magazines/Amiga_Computing_062_Jul_1993_djvu.txt` | ~1.0 MB | CM93 |
| SRC-MAG-TONE056 | `magazines/The_One_056_May_1993_djvu.txt` | ~481 KB | CM93 |
| SRC-MAG-TONE046 | `magazines/The_One_046_Jul_1992_djvu.txt` | ~472 KB | CM1 |
| SRC-MAG-AP027 | `magazines/Amiga_Power_027_Jul_1993_djvu.txt` | ~449 KB | CM93 |
| SRC-COMM-NYGREEN | `community/nygreen_home_wayback.html` + `nygreen_cm4_pregame_editor_wayback.html` | Wayback May 2003 | CM4 |
| SRC-COMM-JONNYRIVERS | `community/jonnyrivers_home_wayback.html` | Wayback Jun 2003 | CM4 |

---

## Primary review text now in repo

| Game | Outlet | Score | Reviewer | Local OCR | Lines / pages |
|------|--------|-------|----------|-----------|---------------|
| CM93 | Amiga Format #48 Jul 1993 | **82%** | Stephen Bradley | SRC-MAG-AF048 | ~16310–16387; adv p.73 |
| CM93 | CU Amiga #40 Jun 1993 | **84%** | Mark Patterson | SRC-MAG-CU040 | ~21797–22075; p.66 |
| CM93 | Amiga Computing #62 Jul 1993 | **92%** ⚠️ | Jonathan Maddock | SRC-MAG-AC062 | ~45870–46285; p.118 — score box not in OCR |
| CM93 | The One #56 May 1993 | **80%** ⚠️ | Simon Byron | SRC-MAG-TONE056 | ~15050–15230 — score sidebar garbled in OCR |
| CM2 | PC Zone #31 Oct 1995 | **97%** (review index) | — | SRC-MAG-PCZ031 | Index ~7843–7846; body ~23180–24149 |
| CM2 | PC Zone digest (#32/#35) | **92% Classic** | — | SRC-MAG-PCZ032 / PCZ035 | ~27537–27543 — **conflicts with 97%**; treat #31 as primary |
| CM1 | The One #46 Jul 1992 | **89%** | Gary Whitta | SRC-MAG-TONE046 | ~18334–18608; pp.92–93; score col ~27511 |
| CM93 | Amiga Power #27 Jul 1993 | **80%** ⚠️ | Tim Norris | SRC-MAG-AP027 | ~12404–12626 — score box not in OCR |

**Extract summary:** [docs/research/sources/magazines/MAGAZINE_REVIEW_EXTRACTS.md](docs/research/sources/magazines/MAGAZINE_REVIEW_EXTRACTS.md)

---

## GameFAQs registry

Live `gamefaqs.gamespot.com` returns **403** to automated fetch. **Wayback** works for known FAQ URLs.

| Source ID | Title / author | URL | Status | Local |
|-----------|----------------|-----|--------|-------|
| SRC-GFQ-CM4-IG1234 | CM4 FAQ — ig1234 (SI feature quotes) | `/pc/562286-.../faqs/23154` | **ACQUIRED** | `gamefaqs/CM4_FAQ_ig1234_wayback.html` |
| SRC-GFQ-CM4-SHY | CM4 guide — shy (attributes, screens) | `/faqs/23115` | **ACQUIRED** | `gamefaqs/CM4_shy_guide_wayback.html` |
| SRC-GFQ-CM4-MANUAL-PP | Printed manual pp. 55–61 (tactics) | Cited in CM4 docs | **MISSING** | Need scan or FAQ citing page text |
| SRC-GFQ-CM3-FAQS | CM3 formation / massive FAQs | CM3_REFERENCE cites | **MISSING** | Wayback 404 for IDs 562287, 914433 — need correct slug |
| SRC-GFQ-CM4-BOARD | CM4 GameFAQs board #33189 | ig1234 FAQ cites | **MISSING** | `s2.cgi.gamefaqs.com/boards/gentopic.asp?board=33189` |
| SRC-COMM-IG1234-GEO | ig1234 provisional FAQ text | ig1234 cites | **BLOCKED** | `geocities.com/ig12345/ig1234CM4.txt` — Wayback → Yahoo |
| SRC-GFQ-CM0203 | CM 02/03 guides | CM_LEGACY_REFERENCE | **MISSING** | Wayback |
| SRC-GFQ-CM9798 | CM97/98 guides | — | **MISSING** | Wayback |
| SRC-GFQ-CM93 | CM93 walkthrough | StrategyWiki used instead | **MISSING** | Low priority |

---

## Community documents (non-GameFAQs)

| Source ID | Title | Status | Local |
|-----------|-------|--------|-------|
| SRC-DOC-CMTECH-FAQ | CM Technical FAQ PDF (magazine disc) | **ACQUIRED** | `community/CM_Tech_FAQ_magazine_disc.pdf` (cmmag-01) |

---

## Forums & official community

| Source ID | Description | Status | Notes |
|-----------|-------------|--------|-------|
| SRC-FORUM-SI-CM4-FAQ | SI community CM4 technical FAQ | **DEAD** | Dobbers cites; Wayback `OpenTopic?...5571901826` → 404 page archived |
| SRC-FORUM-DUGOUT | CM community forums (Dugout era) | **MISSING** | Mentioned in CM4 community doc — Wayback by thread URL |
| SRC-FORUM-CHAMPMANWORLD | champmanworld.com downloads | **DEAD/MISSING** | EP5 mirrors cited by Dobbers |

---

## Citation chain queue (MISSING — cited by something we have)

When an **ACQUIRED** source cites another, the cited source is listed here until acquired.

### From Dobbers CM4 FAQ (SRC-COMM-DOBBERS-CM4)

| Cited URL / resource | Cited for | Status | Hunt notes |
|----------------------|-----------|--------|------------|
| `community.sigames.com/.../5571901826` | Official technical FAQ | **DEAD** | Wayback 404; try other SI forum snapshots |
| `www.cmskins.com` | Official skins | **DEAD?** | Wayback; registration was required |
| `www.nygreen.net` | Pre-game editor | **ACQUIRED** | Wayback May 2003 — homepage + program page |
| `www.jonnyrivers.com` | Pre-game editor | **ACQUIRED** | Wayback Jun 2003 — v0.09b zip link on homepage |
| `www.cmeuphoria.co.uk` | Independent data updates | **DEAD** | Wayback 2011 — parked domain frameset only |
| `www.cm4update.co.uk` | Independent data updates | **MISSING** | Wayback |
| `www.champmanworld.com/.../cid=2` | EP5 patch mirrors | **MISSING** | Wayback downloads section |
| `www.cm-hq.co.uk/408_enhance_nodata.exe` | EP5 patch HTTP | **DEAD** | Mirror on blueyonder FTP below |
| `ftp://gamefiles.blueyonder.co.uk/.../408_enhance.exe` | EP5 patch FTP | **DEAD** | Try archive.org or patch collections |
| `ftp://.../408_enhance_nodata.exe` | EP5 patch (no data) | **DEAD** | Same |
| `ftp://.../cm_408_updater.sit` | Mac updater | **DEAD** | Same |
| `www.mitchellsclose.co.uk/LLM/LL-Team-Generator.html` | LLM team generator | **MISSING** | Wayback |
| `www.belangrijk.net/mc/` | Mind Compression editor | **DEAD** | Wayback 2012 → 404 |
| `www.casperlab.com/cm4save/cm4saveeditor.zip` | Save editor | **BLOCKED** | Index page ✅; zip Wayback = HTML shell only |
| `www.iebeta.net/files/Q815411_WXP_SP2_x86_ENU.exe` | XP perf hotfix for CM4 | **MISSING** | Microsoft KB Q815411 — archive.org |
| `www.eidosinteractive.co.uk/support/` | CM4 support | **MISSING** | Wayback Eidos support pages |

### From magazine / review cross-refs

| Cited source | Cited by | Status | Hunt notes |
|--------------|----------|--------|------------|
| The One Amiga #56 May 1993 (CM93 80%) | CM93_94_REFERENCE, AMR | **ACQUIRED** | `SRC-MAG-TONE056` |
| The One Amiga #46 Jul 1992 (CM1 89%) | CM1_REFERENCE, TONE056 cites | **ACQUIRED** | `SRC-MAG-TONE046` |
| Amiga Computing #62 Jul 1993 (CM93 92%) | CM93_94_REFERENCE, UVList | **ACQUIRED** | `SRC-MAG-AC062` — score box OCR gap |
| Computer & Video Games #140 Jul 1993 (85%) | CM93_94_REFERENCE | **MISSING** | Not in archive.org `cvg-magazine` (R11) |
| Amiga Power #27 Jul 1993 (CM93 80%) | CM93_94_REFERENCE, AMR | **ACQUIRED** | `SRC-MAG-AP027` via `amiga-power-pdffromhi-000_202601` |
| PC Gamer #24 Nov 1995 (CM2 49%) | Wikipedia / era reviews | **MISSING** | PC Gamer archive |
| PC Attack #7 Nov 1995 (CM2 83%) | UVList | **MISSING** | Magazine rack |
| PC Format 1997 CM9798 review | CM9798_REFERENCE gap | **MISSING** | Archive.org |
| PC Zone Nov 1998 CM2 **90%** retrospective | Wikipedia | **MISSING** | Distinct from Oct 1995 97% review — verify issue |

### From GameFAQs ig1234 (ACQUIRED)

| Cited in FAQ | Status | Notes |
|--------------|--------|-------|
| SI official CM4 feature list (quoted) | **CONSULTED** via ig1234 | Primary SI press release PDF **MISSING** |
| Printed manual tactics section | **MISSING** | pp. 55–61 per multiple docs |
| `geocities.com/ig12345/ig1234CM4.txt` | **BLOCKED** | Wayback redirect; GameFAQs HTML is canonical |
| `gamefiles.blueyonder.co.uk/.../403_enhance.exe` | **DEAD** | EP2 patch; archive.org file search |
| GameFAQs board `board=33189` | **MISSING** | CM4 community threads |
| `www.shy.host.sk` | **MISSING** | shy guide author homepage |
| `www.sigames.com` (CM4 homepage per shy) | **MISSING** | Wayback SI site snapshots |

### From The One #56 CM93 review (SRC-MAG-TONE056)

| Cited source | Cited for | Status | Hunt notes |
|--------------|-----------|--------|------------|
| Gary Whitta CM1 review (July, **89%**) | Benchmark vs CM93 sequel | **ACQUIRED** | `SRC-MAG-TONE046` — resolved R11 |

---

## Magazine PDF/OCR — full hunt matrix

| Source ID | Issue | Game | Score (secondary) | OCR in repo | Primary review text |
|-----------|-------|------|-------------------|-------------|---------------------|
| SRC-MAG-AF048 | AF #48 Jul 1993 | CM93 | 82% | ✅ | ✅ |
| SRC-MAG-CU040 | CU #40 Jun 1993 | CM93 | 84% | ✅ | ✅ |
| SRC-MAG-AC062 | Amiga Computing #62 Jul 1993 | CM93 | 92% | ✅ | ✅ body; score box OCR gap |
| SRC-MAG-TONE056 | The One #56 May 1993 | CM93 | 80% | ✅ | ✅ body; score sidebar OCR gap |
| SRC-MAG-TONE046 | The One #46 Jul 1992 | CM1 | 89% | ✅ | ✅ body; score column ~27511 |
| SRC-MAG-TONE-CM1-JUL | (alias → TONE046) | CM1 | 89% | ✅ | Resolved R11 |
| SRC-MAG-AP027 | Amiga Power #27 Jul 1993 | CM93 | 80% | ✅ | ✅ body; score box OCR gap |
| SRC-MAG-AP027-FULL | (alias → AP027) | CM93 | 80% | ✅ | R11 coverdisk item was wrong archive ID |
| SRC-MAG-CVG140 | CVG #140 Jul 1993 | CM93 | 85% | ❌ | ❌ — not in `cvg-magazine` collection |
| SRC-MAG-PCZ031 | PC Zone #31 Oct 1995 | CM2 | 97% | ✅ | ✅ |
| SRC-MAG-PCZ032 | PC Zone #32 Nov 1995 | CM2 | 92% digest | ✅ | PARTIAL digest only |
| SRC-MAG-PCG024 | PC Gamer #24 Nov 1995 | CM2 | 49% | ❌ | ❌ |
| SRC-MAG-PCF-1997 | PC Format ~1997 | CM9798 | — | ❌ | ❌ |
| SRC-MAG-AF108 | AF #108 | CM2 Amiga port | 12% | ❌ | ❌ |

Archive.org collections to search: `AmigaFormatMagazine`, `cuamiga-magazine`, `PC_Zone_*`, `amiga-computing-magazine`, `theone-magazine-*`, **`amiga-power-pdffromhi-000_202601`**.

---

## Per-game source coverage (quick matrix)

Depth: ✅ in repo · ⚠️ partial · ❌ missing

| Game | Manuals | Magazines (primary) | GameFAQs | Forums/community | Patches primary |
|------|---------|---------------------|----------|------------------|-----------------|
| CM1 | ✅ HTML | ⚠️ The One #46 ✅ | ❌ | ❌ | N/A |
| CM93 | ✅ text | ⚠️ **AF+CU+AC+The One+AP ✅**; CVG ❌ | ❌ | ❌ | ⚠️ Intelek ads in AF + AC OCR |
| CM2 | ⚠️ FR PDF | ⚠️ PCZ ✅; PCG/PC Attack ❌ | ❌ | ❌ | ⚠️ chronology only |
| CM9798 | ⚠️ scan | ❌ | ❌ | ❌ | ⚠️ |
| CM3 | ✅ PDF | ❌ | ❌ | ❌ | ⚠️ |
| CM4 | ⚠️ ReadMe | ✅ era reviews elsewhere | ⚠️ 2 FAQs | ⚠️ Dobbers ✅ + nygreen/jonnyrivers Wayback | ✅ EP deep |

---

## Save games, tactics & RE artifacts

Full hunt matrix: [docs/research/saves-and-tactics/README.md](docs/research/saves-and-tactics/README.md)

| Source ID | Artifact | Game | Status | Notes |
|-----------|----------|------|--------|-------|
| SRC-BIN-CMMAG01 | Magazine cover CD ISO | CM2–CM4 demos + tools | **PARTIAL** | ISO `_staging/`; [samples/MANIFEST](docs/research/saves-and-tactics/samples/MANIFEST.md) |
| SRC-BIN-CMMAG04 | [cmmag-04](https://archive.org/details/cmmag-04) | CM4 `.tac` + training | **REMOTE** | Metadata confirms; **P0 extract R15** |
| SRC-BIN-CMMAG05 | [cmmag-05](https://archive.org/details/cmmag-05) | CM4 + CM03/04 tactics | **REMOTE** | Not downloaded R14 |
| SRC-BIN-CM0102-TACT-EXE | `samples/cm0102/tactics/*.exe` | CM01/02 | **ACQUIRED** | PE installers from cmmag-01 (15 on disc) |
| SRC-BIN-CM4-TAC | `.tac` tactics presets | CM4 | **MISSING** | Expected on cmmag-04/05 |
| SRC-BIN-CM4-SAV | Mid-season save folder | CM4 | **MISSING** | Pair with EP/patch version |
| SRC-BIN-CM0102-SAV | `.sav` uncompressed | CM01/02 | **MISSING** | Editors require uncompressed saves |
| SRC-BIN-CM93-SAV | Save file | CM93 | **MISSING** | Manual: “save to disk” |
| SRC-BIN-CM1-SAV | Season disk save | CM1 | **MISSING** | Amiga/ST/DOS variants differ |
| SRC-COMM-CASPERLAB-INDEX | Save editor docs | CM4 | **ACQUIRED** | `casperlab_cm4save_index_wayback.html` |
| SRC-TOOL-CASPERLAB-ZIP | Save editor binary | CM4 | **BLOCKED** | Wayback zip not preserved |
| SRC-TOOL-GK-CM3-EDITOR | `samples/cm3/tools/GraemeKelly_*` | CM9899–0102 | **PARTIAL** | FAQ+CHANGES; SETUP.EXE on disc not copied |
| SRC-FORMAT-CM0102-OFFSETS | Offset index thread | CM01/02 | **CONSULTED** | champman0102.net f=35 t=2802 |
| SRC-FORMAT-NCKSTWRT-GH | CM0102Patch / Loader | CM01/02 | **CONSULTED** | GitHub save-load paths |

---

## Blocked sources & workarounds

| Source | Block reason | Workaround |
|--------|--------------|------------|
| gamefaqs.gamespot.com (live) | HTTP 403 | Wayback `web.archive.org/web/2020/https://gamefaqs...` |
| mobygames.com | Cloudflare | Manual browser save; or rely on AMR/UVList |
| lemonamiga.com | HTTP 403 | Wayback; manual text already archived R6 |
| CM9798 PDF OCR | No Python/tesseract on host | Install Python or use archive.org `djvu.txt` if uploaded |
| Dead FTP blueyonder | FTP timeout / gone | `cm4patch`, Dobbers mirrors, archive.org file search |

---

## Round 10+ priorities (from missing queue)

| P | Task | Source IDs |
|---|------|------------|
| P0 | Extract **`cmmag-04`** ISO → CM4 `.tac` + training schedules | SRC-BIN-CMMAG04 |
| P0 | Extract **`cmmag-01`** SETUP.EXE (GK editor) if needed for live RE | SRC-TOOL-GK-CM3-EDITOR |
| P0 | OCR CM9798 + CM2 FR manuals | Manuals backlog |
| P1 | CM4 `.tac` + mid-season save samples | SRC-BIN-CM4-* |
| P1 | CVG #140 Jul 1993 CM93 review | SRC-MAG-CVG140 |
| P1 | PC Gamer #24 CM2 review | SRC-MAG-PCG024 |
| P1 | GameFAQs CM3 + CM0203 Wayback batch | SRC-GFQ-CM3-* |
| P2 | Dobbers citation chain Wayback (editors, patches, cmskins) | § Citation chain |
| P2 | CM4 printed manual scan | SRC-GFQ-CM4-MANUAL-PP |
| P2 | PC Format CM9798 | SRC-MAG-PCF-1997 |
| P3 | Resolve PC Zone CM2 **97% vs 92%** | Compare #31 full review scoring box |

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 16 — research **PAUSED**; early games audit |
| 2025-06-25 | Round 15 — master backlog; CM Tech FAQ; cmmag-02 catalog |
| 2025-06-25 | Round 14 — cmmag-01 mount/extract; CM Magazine CD index; GK editor docs |
| 2025-06-25 | Round 13 sync | Coverage audit + PM brief; shell hunt note |
| 2025-06-25 | Round 13 — saves/tactics RE track; casperlab index; cmmag-01 catalogued |
| 2025-06-25 | Round 12 — AP #27 CM93 OCR; nygreen/jonnyrivers Wayback; CVG140 still missing |
| 2025-06-25 | Round 11 — The One #46 CM1 OCR; AP27/CVG140 hunt notes; Dobbers URL status updates |
| 2025-06-25 | Round 10 — AC062 + The One #56 OCR; ig1234/shy citation additions; CM1 review chain |
| 2025-06-25 | Round 9 — ledger created; 5 magazine OCRs; 2 GameFAQs; Dobbers FAQ; citation queue seeded |
