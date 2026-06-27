# Historical Data Source Ledger

**Document type:** Research / provenance registry  
**Owner:** World & Data + Research  
**Status:** **Stub** — R-H1 kickoff (2026-06-27)  
**Companion:** [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) · [DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md](DESIGN_HISTORICAL_DATABASE_AND_START_SEASONS.md)

---

## 1. Purpose

Every row in a mod file (club, player, stadium, rule, competition) should cite **at least one** source ID from this ledger. Squads and founding years require **two independent sources** when PO selects option B in [PM_HISTORICAL_DATA_EXPANSION_REPORT.md §10](PM_HISTORICAL_DATA_EXPANSION_REPORT.md).

**Format in mod comments (proposed):**

```
# source: S-WIKI-EFL1888-01,S-RSSSF-ENG1888-01
```

---

## 2. Source ID scheme

| Prefix | Type | Example |
|--------|------|---------|
| `S-WIKI-` | Wikipedia season/club article | `S-WIKI-EFL1888-01` |
| `S-RSSSF-` | RSSSF archive | `S-RSSSF-ENG1888-01` |
| `S-11V11-` | 11v11.com player/club | `S-11V11-PNE-01` |
| `S-FA-` | FA / contemporary minutes | `S-FA-MINUTES-1888-01` |
| `S-BOOK-` | Published history (ISBN) | `S-BOOK-ROTHMANS-01` |
| `S-CLUB-` | Official club archive | `S-CLUB-PNE-HISTORY-01` |

Add rows below before using a new prefix in content PRs.

---

## 3. Registered sources (season 18 — partial)

| Source ID | URL / citation | Covers | Verified |
|-----------|----------------|--------|:--------:|
| `S-WIKI-EFL1888-01` | https://en.wikipedia.org/wiki/1888–89_Football_League | League table, clubs, rules context | ⚠️ Partial |
| `S-WIKI-FACUP1888-01` | https://en.wikipedia.org/wiki/1888–89_FA_Cup | FA Cup participants | ⚠️ Partial |
| `S-RSSSF-ENG1888-01` | https://www.rsssf.org/tablese/eng1889.html | Tables, dates | ❌ Not linked in files yet |

**Action:** Backfill `source:` comments in `mods/seasons/18/` during DB-2 validation pass.

---

## 4. Research packs (planned)

| Pack ID | Era | Status | Unlocks |
|---------|-----|--------|---------|
| `R-H3` | England 1899–1900 | ❌ Not started | First Tier-1 start mod candidate |
| `R-H4` | Scotland 1890–91 | ❌ Not started | First country expansion candidate |

---

## 5. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Stub created (R-H1) |

---

*Next update: after first source backfill on season 18 or PO decision on minimum source bar.*
