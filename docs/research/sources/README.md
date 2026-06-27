# CM Legacy — Local Source Archive

**Parent ledger:** [CM_LEGACY_SOURCE_LEDGER.md](../../CM_LEGACY_SOURCE_LEDGER.md)  
**Research status:** **PAUSED** at Round 15 — [CM_LEGACY_RESEARCH_PAUSE.md](../../CM_LEGACY_RESEARCH_PAUSE.md)  
**Manuals (separate tree):** [../manuals/README.md](../manuals/README.md)

This directory holds **primary and community sources** acquired for CM research. Check here before downloading anything new.

---

## Directory layout

```
sources/
├── community/     Third-party FAQs, fan sites (HTML captures)
├── forums/        Official forum / BBS Wayback captures
├── gamefaqs/      GameFAQs guides (Wayback HTML)
├── magazines/     Archive.org magazine OCR (djvu.txt)
│   └── MAGAZINE_REVIEW_EXTRACTS.md   Key review quotes + line refs
└── README.md      (this file)
```

---

## File inventory (Round 9)

| File | Source ID | Acquired | Method | SHA256 note |
|------|-----------|----------|--------|-------------|
| `community/Dobbers_CM4_FAQ_cm4.htm.html` | SRC-COMM-DOBBERS-CM4 | 2025-06-25 | Live HTTP | ~14 KB plain HTML |
| `forums/SIGames_Official_CM4_FAQ_wayback.html` | SRC-FORUM-SI-CM4-FAQ | 2025-06-25 | Wayback | 404 page — topic not archived |
| `gamefaqs/CM4_FAQ_ig1234_wayback.html` | SRC-GFQ-CM4-IG1234 | 2025-06-25 | Wayback 2019-05 | SI official quotes |
| `gamefaqs/CM4_shy_guide_wayback.html` | SRC-GFQ-CM4-SHY | 2025-06-25 | Wayback 2019-05 | Attributes / UI |
| `magazines/Amiga_Format_048_1993_07_djvu.txt` | SRC-MAG-AF048 | Round 9 | archive.org | CM93 82% review |
| `magazines/CU_Amiga_040_Jun_1993_djvu.txt` | SRC-MAG-CU040 | Round 9 | `cuamiga-magazine-040` | CM93 84% review |
| `magazines/PC_Zone_31_Oct_1995_djvu.txt` | SRC-MAG-PCZ031 | Round 9 | `PC_Zone_31_October_1995` | CM2 97% full review |
| `magazines/PC_Zone_32_Nov_1995_djvu.txt` | SRC-MAG-PCZ032 | Round 9 | `PC_Zone_32_November_1995` | CM2 92% digest |
| `magazines/PC_Zone_35_1996_02_djvu.txt` | SRC-MAG-PCZ035 | Round 9 | prior session | Roundups + CM2 letters |
| `magazines/Amiga_Computing_062_Jul_1993_djvu.txt` | SRC-MAG-AC062 | Round 10 | `amiga-computing-magazine-062` | CM93 92% review body |
| `magazines/The_One_056_May_1993_djvu.txt` | SRC-MAG-TONE056 | Round 10 | `theone-magazine-56` | CM93 80% review body |
| `magazines/The_One_046_Jul_1992_djvu.txt` | SRC-MAG-TONE046 | Round 11 | `theone-magazine-46` | CM1 89% review (Gary Whitta) |
| `magazines/Amiga_Power_027_Jul_1993_djvu.txt` | SRC-MAG-AP027 | Round 12 | `amiga-power-pdffromhi-000_202601` | CM93 80% review (Tim Norris) |
| `community/nygreen_home_wayback.html` | SRC-COMM-NYGREEN | Round 12 | Wayback 2003-05-26 | CM4 Pregame Editor site homepage |
| `community/nygreen_cm4_pregame_editor_wayback.html` | SRC-COMM-NYGREEN | Round 12 | Wayback 2003-05-22 | CM4 editor feature list v0.04 |
| `community/jonnyrivers_home_wayback.html` | SRC-COMM-JONNYRIVERS | Round 12 | Wayback 2003-06-04 | CM4 editor v0.09b download page |
| `community/casperlab_cm4save_index_wayback.html` | SRC-COMM-CASPERLAB-INDEX | Round 13 | Wayback 2003-06-08 | CM4 save editor v0.02 docs |
| `community/CM_Tech_FAQ_magazine_disc.pdf` | SRC-DOC-CMTECH-FAQ | Round 15 | cmmag-01 disc | CM technical FAQ (magazine cover CD) |

---

## Archive.org download URLs (canonical)

Use these exact paths — item-level `*_djvu.txt` names vary by upload.

| Local file | archive.org item | OCR file |
|------------|------------------|----------|
| AF #48 | Search `AmigaFormatMagazine` issue 48 | `*_djvu.txt` |
| CU #40 | `cuamiga-magazine-040` | `CUAmiga_040_Jun_1993_djvu.txt` |
| AC #62 | `amiga-computing-magazine-062` | `AmigaComputing_062_Jul_1993_djvu.txt` |
| The One #56 | `theone-magazine-56` | `TheOne_56_May_1993_djvu.txt` |
| The One #46 | `theone-magazine-46` | `TheOne_46_Jul_1992_djvu.txt` |
| Amiga Power #27 | `amiga-power-pdffromhi-000_202601` | `AmigaPowerPdffromhi027_djvu.txt` |
| PC Zone #31 | `PC_Zone_31_October_1995` | `PC_Zone_31_October_1995_djvu.txt` |
| PC Zone #32 | `PC_Zone_32_November_1995` | `PC_Zone_32_November_1995_djvu.txt` |
| PC Zone #35 | (existing identifier in repo) | `PC_Zone_35_1996_02_djvu.txt` |

---

## Acquisition rules

1. **No duplicates** — compare with [manuals/README.md](../manuals/README.md) and ledger § Do-not-repeat.
2. **Prefer OCR text** over PDF when available (`*_djvu.txt` from archive.org).
3. **Wayback for GameFAQs** — live site blocks bots; snapshot date in filename.
4. **Delete failed fetches** — 404 HTML pages must not stay named as `.txt` (see Round 9 cleanup).
5. **Cite line numbers** — when extracting, update `magazines/MAGAZINE_REVIEW_EXTRACTS.md` and the ledger.
6. **Windows HTTP** — use `Invoke-WebRequest -TimeoutSec 60 -UseBasicParsing`; avoid `curl -s … | Select-Object` (can hang with no output).

---

## Still missing (not in this folder)

See [CM_LEGACY_SOURCE_LEDGER.md](../../CM_LEGACY_SOURCE_LEDGER.md) § Citation chain queue, § Magazine PDF/OCR matrix, and [saves-and-tactics/README.md](../saves-and-tactics/README.md).
