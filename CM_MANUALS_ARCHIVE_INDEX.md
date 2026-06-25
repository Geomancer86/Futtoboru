# Championship Manager — Manuals & Archive Index

**Purpose:** Curated reference list of **official manuals**, READMEs, and preservation archives for CM1–CM4 research.  
**Note:** Futtoboru repo does **not** redistribute copyrighted PDFs. Use links for internal research only.

**Last updated:** 2025-06-25

---

## Official / original documentation by title

| Game | Manual type | Format | Archive source | Confidence |
|------|-------------|--------|----------------|------------|
| **CM1** (1992) | Printed Amiga/ST/PC manual | HTML scan | [GamesDatabase Amiga manual](https://www.gamesdatabase.org/Media/SYSTEM/Commodore_Amiga/Manual/formated/Championship_Manager_-_1992_-_Domark_Software.htm) | ✅ |
| **CM93/94** | Amiga manual | PDF/docs | [Lemon Amiga docs](https://www.lemonamiga.com/games/docs.php?id=315) | ✅ |
| **CM2** | PC manual (DOS) | Printed / partial scans | Community scans via [MobyGames](https://www.mobygames.com/) | ⚠️ |
| **CM97/98** | PC manual | **PDF ~6 MB** | [My Abandonware CM97/98](https://www.myabandonware.com/game/championship-manager-season-97-98-2db) | ✅ |
| **CM97/98** | Manual mirror | PDF | [BD Jogos manual id=910](https://en.bdjogos.com.br/manual.php?id=910) | ✅ |
| **CM3** | User manual | **PDF ~540 KB, 43 pages** | [ManualMachine CM3](https://manualmachine.com/gamespc/championshipmanager3/1119318-user-manual/) | ✅ |
| **CM4** | Manual + README | PDF / in-box | Listed on [MobyGames CM4 releases](https://www.mobygames.com/game/8910/championship-manager-4/releases/) | ⚠️ |
| **CM4** | Patch READMEs | Text in EP installers | Enhancement Pack 1–5 executables | ✅ |

---

## Manual contents summary (research value for Futtoboru)

### CM1 manual (Domark, Amiga)

Documented in HTML scan:

- **Startup:** Program disk DF0; optional season disk DF1; PC typed `EUROPE` at prompt  
- **Hotseat:** Up to **4 players**; each picks club, personality, name  
- **Season disk:** Blank formatted disk required to save — data erased if reused  
- **Main menu:** Continue, tactics, transfers, results, fixtures, cups, resign  
- **Printer option** for match output  

### CM3 manual (43 pages — ManualMachine)

Table of contents includes:

- Installation (Windows 95/98)  
- Start New Game / Restore Saved Game  
- **Network Play**  
- Hall of Fame  
- Quick Start Game  
- Epilepsy / ergonomics warnings (standard Eidos era)  

Full PDF recommended for tactics/training screen enumeration — **Round 3 action:** download to `docs/research/manuals/` (local only, gitignored) if team obtains license clearance.

### CM97/98 manual

Covers editor, multi-league setup, set-piece takers — primary source for Gen-2 feature confirmation.

---

## Official patch / enhancement documentation

### CM4 Enhancement Packs (Sports Interactive)

| Pack | Version | Date | Notes |
|------|---------|------|-------|
| EP1 | 4.0.3 | 29 Mar 2003 | Launch day; wages, Swedish stadia, praise/criticise media |
| EP2 | 4.0.4 | 11 Apr 2003 | Cumulative |
| EP3 | 4.0.5 | ~May 2003 | Injury fixes; optional data update |
| EP5 | **4.0.8** | 24 Jul 2003 | **Final** CM4 patch; NO DATA + Updated Data variants |

**Mirror list (historical — may be dead):** Documented in [CM4 FAQ (Dobbers)](http://www.dobbers.f9.co.uk/agcm/cm4.htm):

- `ftp://gamefiles.blueyonder.co.uk/blueyondergames/championshipmanager4/patches/408_enhance_nodata.exe`
- `ftp://gamefiles.blueyonder.co.uk/blueyondergames/championshipmanager4/patches/408_enhance.exe`
- Mac: `cm_408_updater.sit`

**Community guidance:** EP2 minimum; EP5 recommended; cumulative (no need to install EP1–4 first).

### CM 03/04 patches (post-CM4)

- SI patches through **v4.1.5+** documented on archived SIGames downloads / Soccer Gaming threads  
- Separate from CM4 EP line after split

---

## Preservation & community archive sites

| Site | Era | Content |
|------|-----|---------|
| [My Abandonware](https://www.myabandonware.com/) | CM1–CM3 | Games + some manuals |
| [ManualMachine](https://manualmachine.com/) | CM3 | Full manual PDF |
| [Lemon Amiga](https://www.lemonamiga.com/) | CM1, CM93 | Amiga docs |
| [GamesDatabase](https://www.gamesdatabase.org/) | CM1 | Manual HTML |
| [MobyGames](https://www.mobygames.com/) | All | Box art, credits, patches list |
| [cm9798.co.uk](https://cm9798.co.uk/) | CM2–CM3 | Podcast, DOSBox guides, disc-free installs |
| [Champman0102.net](https://champman0102.net/) | CM2–CM01/02 | Forums, CM2 European leagues, DOSBox |
| [Champman Fans WordPress](https://champmanfans.wordpress.com/) | CM2 | European leagues install guide |
| [The Dugout](https://www.champman0102.com/) (successor community) | CM01/02–FM | Tactics, downloads, forums |
| [CM Revolution / cmrev.com](https://www.cmrev.com/) | CM4–FM | Polish/EN patch mirrors |
| [Soccer Gaming forums](https://soccergaming.com/) | CM4, 03/04 | Data updates, Nygreen editor |
| [VOGONS](https://www.vogons.org/) | CM2 DOS | LaserLok fix (`cm2fix.com`) |
| [community.sports-interactive.com](https://community.sports-interactive.com/) | CM4→FM | Successor to SIGames forums |
| [Wayback: sigames.com](https://web.archive.org/web/*/sigames.com) | CM3–CM4 | Official news, patches |

---

## Recommended local research workflow

1. Download CM3 PDF from ManualMachine → extract UI/feature terminology  
2. Download CM97/98 PDF from My Abandonware → editor + league setup  
3. Archive CM4 EP5 changelog from Dobbers FAQ / SI forum Wayback  
4. Cross-check feature claims against [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md)  
5. Map gaps via [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)

**Do not commit copyrighted PDFs to git** without explicit permission. Add `docs/research/manuals/` to `.gitignore` if storing locally.

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 2 — initial archive index |
