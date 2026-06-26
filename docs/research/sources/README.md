# CM Legacy — Local Source Archive

**Parent ledger:** [CM_LEGACY_SOURCE_LEDGER.md](../../CM_LEGACY_SOURCE_LEDGER.md)  
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

---

## Archive.org download URLs (canonical)

Use these exact paths — item-level `*_djvu.txt` names vary by upload.

| Local file | archive.org item | OCR file |
|------------|------------------|----------|
| AF #48 | Search `AmigaFormatMagazine` issue 48 | `*_djvu.txt` |
| CU #40 | `cuamiga-magazine-040` | `CUAmiga_040_Jun_1993_djvu.txt` |
| AC #62 | `amiga-computing-magazine-062` | `AmigaComputing_062_Jul_1993_djvu.txt` |
| The One #56 | `theone-magazine-56` | `TheOne_56_May_1993_djvu.txt` |
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

---

## Still missing (not in this folder)

See [CM_LEGACY_SOURCE_LEDGER.md](../../CM_LEGACY_SOURCE_LEDGER.md) § Citation chain queue and § Magazine PDF/OCR matrix.
