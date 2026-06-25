# Championship Manager — Legacy Reference (CM1 → CM4)

**Document type:** Competitive / design reference wiki  
**Scope:** All SI-developed **Championship Manager** releases from debut through **CM Season 03/04** (final SI CM before FM2005)  
**Parent:** [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md)  
**CM4 deep dive:** [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) + [CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md)  
**Per-era deep dives:** [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM2_REFERENCE.md](CM2_REFERENCE.md) · [CM3_REFERENCE.md](CM3_REFERENCE.md)  
**Manuals index:** [CM_MANUALS_ARCHIVE_INDEX.md](CM_MANUALS_ARCHIVE_INDEX.md)  
**Gap analysis vs Futtoboru:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md)  
**PM status brief:** [PM_STAKEHOLDER_STATUS_BRIEF.md](PM_STAKEHOLDER_STATUS_BRIEF.md)  
**Date:** 2025-06-25

---

## How to use this document

Futtoboru's long-term design goal (per product direction): **implement the full feature set of legacy CM through CM4**, adapted to **historical 1888-scale data**, not FM's modern breadth.

| Symbol | Meaning |
|--------|---------|
| ✅ | Verified (multiple sources) |
| ⚠️ | Approximate or single-source |
| **NEW** | First appearance in series |
| **+** | Incremental improvement over prior release |

---

## Engine generations (summary)

```text
Gen 1 (1992–1993)  CM1, CM93/94, CM Italia     — text engine, English/Italian focus
Gen 2 (1995–1997)  CM2, CM97/98                 — text + SVGA + audio commentary; editor
Gen 3 (1999–2002)  CM3 → CM01/02, CM02/03 Xbox  — text overhaul; training; international; LAN
Gen 4 (2003)       CM4, CM Season 03/04          — 2D match engine; personalities; fan pressure
```

**After 03/04:** SI kept code → **Football Manager 2005**. Eidos kept name → **CM5** (separate, failed).

---

## Master feature debut timeline

| Feature | First appeared | Mature by |
|---------|----------------|-----------|
| Average post-match ratings (0–10) | **CM1** (1992) | CM1 |
| English pyramid + domestic/European cups | **CM1** | CM1 |
| Hotseat multiplayer | **CM1** | CM1 |
| Real player names | **CM93/94** | CM93/94 |
| FA Premier League | **CM93/94** | CM93/94 |
| Injury time | **CM93/94** | CM93/94 |
| Foreign player transfer pool | **CM93/94** | CM93/94 |
| Reserve squads | **CM93/94** | CM3 (leagues) |
| Salaries & win bonuses | **CM93/94** | CM99/00 |
| Country spin-off (Italy rules) | **CM Italia** (1993) | CM Italia |
| SVGA ground backgrounds | **CM2** (1995) | CM2 |
| Audio match commentary | **CM2** (1995) | CM2 |
| Scotland playable (PC) | **CM2** (1995) | CM2 |
| League choice at new game | **CM2** (1995) | CM3 |
| Built-in database editor | **CM97/98** (1997) | CM00/01 (standalone) |
| Multi-league concurrent world | **CM97/98** (3 leagues) | CM99/00 (16) |
| Set-piece takers | **CM97/98** | CM97/98 |
| U21 internationals | **CM97/98** | CM97/98 |
| Full engine/UI rewrite | **CM3** (1999) | CM3 |
| Custom training schedules | **CM3** | CM4 |
| Youth squads + trialists | **CM3** | CM4 |
| International management | **CM3** | CM3 |
| LAN / network multiplayer (16) | **CM3** | CM4 |
| Per-player tactics (runs, marking) | **CM3** | CM4 |
| Non-European playable leagues | **CM3** | CM4 |
| Fine / discipline players | **CM99/00** | CM4 |
| Stadium & facility upgrades | **CM99/00** | — |
| Assigned scouts | **CM99/00** | CM4 |
| Press / media Q&A | **CM00/01** | CM4 |
| Official pre-game data editor | **CM00/01** | CM03/04 |
| EU transfer windows | **CM01/02** | CM4 |
| Attribute fog of war (scout to reveal) | **CM01/02** | CM01/02 |
| Player notes & comparisons | **CM01/02** | CM4 |
| Console (Xbox) | **CM01/02** | — |
| **2D top-down match engine** | **CM4** (2003) | CM03/04 |
| Skinnable UI | **CM4** | CM4 |
| Praise / shame players (media) | **CM4** | CM4 |
| Separate wage + transfer budgets | **CM4** | CM4 |
| Fan expectations (vs board) | **CM4** | CM4 |
| Reserves & U19 in own leagues | **CM4** | CM4 |
| Match replay (saved games) | **CM03/04** | CM03/04 |
| Player-to-player relationships | **CM03/04** | CM03/04 |

---

# Release-by-release reference

## CM1 — *Championship Manager* (1992)

| Field | Detail |
|-------|--------|
| **Release** | Spring–Sep 1992 ✅ |
| **Platforms** | Amiga, Atari ST, MS-DOS |
| **Developer / Publisher** | Intelek / Domark |
| **Match engine** | **Text-only** commentary |
| **Database** | 4 English divisions (~80 clubs); **no real names** (No.3, No.10); 3 European competitions + domestic cups |
| **Sales** | ~20,000 launch ⚠️ |

### Features (baseline for entire series)

- Full management loop: pick team, tactics, lineups, transfers (domestic), board, cups  
- **Average player ratings** after matches — genre innovation **NEW**  
- Hotseat multiplayer **NEW**  
- No friendlies, no training depth, no scouting, no international, no editor  

---

## CM93/94 — *Championship Manager 93/94* (1993)

| Field | Detail |
|-------|--------|
| **Release** | 17 Sep 1993 |
| **Platforms** | PC (DOS), Amiga, Atari ST |
| **Developer** | Sports Interactive (company formalised 1994) |
| **Match engine** | Text + **injury time** **NEW** |
| **Database** | 4 English divisions; **real names** **NEW**; 100+ foreign players buyable (nation list, leagues not simulated) |

### NEW vs CM1

Real names · Premier League · Reserve squads · Manager history · Salaries/bonuses · International transfers · Update disks · Code rewrite BASIC→C

---

## CM Italia (1993)

| Field | Detail |
|-------|--------|
| **Release** | 1993 (Amiga 1994) |
| **Engine** | Same as CM93/94 (text) — **not** a new engine |
| **Database** | Serie A + B (~40 clubs); Coppa Italia |
| **Rules overlay** | 4 subs, mercato windows, foreign quotas ⚠️ |

First **country-specific spin-off** **NEW**. CM Italia '95 update followed.

---

## CM2 — *Championship Manager 2* (1995)

| Field | Detail |
|-------|--------|
| **Release** | 22 Sep 1995 (PC); Amiga 1997 |
| **Match engine** | Text + **SVGA ground photos** **NEW** + optional **CD audio commentary** (Clive Tyldesley) **NEW** |
| **Database** | England **or** Scotland per save; global famous players for transfers; add-on league packs (Italy, Spain, etc.) |

### NEW vs CM93/94

Scottish leagues · League at startup · Bosman (later updates) · Expanded tactics/contracts · Career to ~2025

---

## CM97/98 — *Season 97/98* (1997)

| Field | Detail |
|-------|--------|
| **Release** | Oct 1997 |
| **Engine** | Final **CM2-engine** release |
| **Database** | 9 nations; up to **3 concurrent leagues**; 32-man squads |

### NEW vs CM2

**Built-in editor** **NEW** · Portugal · Set-piece takers · U21 internationals · Signing-on fees · Updated CL/UEFA formats · #1 UK PC game 1997

---

## CM3 — *Championship Manager 3* (1999)

| Field | Detail |
|-------|--------|
| **Release** | 26 Mar 1999 |
| **Engine** | **Gen 3 rewrite** — text with action zones, rich stats, background processing |
| **Database** | **15 nations**; **25,000+** players/staff; Brazil, Japan, Argentina first non-Europe **NEW** |

### NEW vs CM97/98 (landmark)

| System | Detail |
|--------|--------|
| Training | **Custom schedules** by group **NEW** |
| Youth | Reserves + youth + **trialists** **NEW** |
| International | **Full national team career** **NEW** |
| Multiplayer | **LAN/internet up to 16** **NEW** |
| Tactics | Per-player instructions (marking, runs, pressing) **NEW** |
| Scouting | Improved assignment model **NEW** |
| UI | Vertical sidebar, multitasking **NEW** |

---

## CM99/00 — *Season 99/00* (1999)

| Field | Detail |
|-------|--------|
| **Database** | 16 nations (+ MLS); **40,000+** players; **16 concurrent leagues** |
| **NEW** | **Fine players** · **Stadium upgrades** · **Assigned scouts** · **Mac port** · Improved board (wage structure, ground improvements) |

---

## CM00/01 — *Season 00/01* (2000)

| Field | Detail |
|-------|--------|
| **Database** | **26 leagues** (+10 nations: Australia, Finland, Greece, N.Ireland, Russia, Wales, …) |
| **NEW** | **Official standalone data editor** **NEW** · **Press/media Q&A** **NEW** · Personality conflicts |

---

## CM01/02 — *Season 01/02* (2001)

| Field | Detail |
|-------|--------|
| **Platforms** | PC, Mac, **Xbox** (first console CM) **NEW** |
| **Database** | ~**100 leagues**, 27 countries |
| **NEW** | **EU transfer windows** · **Fog of war** (scout to reveal stats) · **Player notes** · Surgery option · Post-9/11 window rules |

Most-played CM ever; free release Dec 2008. Xbox: text engine, max 3 leagues.

---

## CM02/03 — *Season 02/03* (2002, Xbox only)

| Field | Detail |
|-------|--------|
| **Platform** | **Xbox exclusive** — PC users got 01/02 patch instead |
| **Engine** | CM3 **text** (not 2D) ✅ |
| **Database** | 27 leagues (+ South Korea); **100,000+** players; ~1000 researchers ⚠️ |
| **NEW** | FIFA windows for 02/03 · Pad rumble on goals · Player comparison on console |

Incremental Xbox data update; CM4 imminent on PC.

---

## CM4 — *Championship Manager 4* (2003)

| Field | Detail |
|-------|--------|
| **Release** | 28 Mar 2003 (PC); 17 May 2003 (Mac) |
| **Engine** | **Full rewrite** + **first 2D match engine** **NEW** |
| **Database** | **39 nations**, **88 divisions**, **200,000+** players/staff |

**→ Full system documentation:** [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)

### Headline NEW systems

2D engine · New tactics · New training · Skins · Personalities · Fan feedback · Praise/shame · Wage/transfer budgets · News ticker · Reserves/U19 leagues · Enhanced multiplayer · Right-click/shortcuts

Launch bugs → Enhancement Packs 4.0.3–4.0.7+.

---

## CM Season 03/04 (2003)

| Field | Detail |
|-------|--------|
| **Release** | 21 Nov 2003 |
| **Engine** | CM4 2D refined: weather, crowd audio, offside flags, pitch condition |
| **Database** | **43 countries**, **92 divisions** ⚠️ (sources vary 42/96); 200,000+ players; 2,500+ researchers |
| **NEW** | **Match replay** (watch saved games) **NEW** · Built-in pre-game editor · Player relationships · Goal of month/season · Training visual indicators on profiles |

Considered definitive pre-split CM; iterative polish on CM4 base.

---

# Cross-release systems matrix (maturity at CM4)

| System | CM1 | CM93/94 | CM2 | CM97/98 | CM3 | CM01/02 | CM4 |
|--------|-----|---------|-----|---------|-----|---------|-----|
| Text match engine | ✅ | ✅ | ✅ | ✅ | ✅+ | ✅ | opt |
| 2D match engine | — | — | — | — | — | — | ✅ |
| Player ratings | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Tactics / formations | basic | ✅ | ✅+ | ✅ | ✅++ | ✅++ | ✅+++ |
| Training schedules | — | ~ | ~ | ~ | ✅ | ✅ | ✅++ |
| Transfers | basic | ✅ | ✅+ | ✅+ | ✅++ | ✅+++ | ✅+++ |
| Transfer windows | — | — | — | — | — | ✅ EU | ✅ |
| Scouting | — | pool | ~ | ~ | ✅ | ✅ fog | ✅++ |
| Staff / assistant | — | ~ | ✅ | ✅ | ✅ | ✅ | ✅++ |
| Board expectations | basic | ✅ | ✅ | ✅ | ✅ | ✅+ | ✅++ |
| Fan pressure | — | — | — | — | — | — | ✅ |
| Morale / happiness | — | ~ | ~ | ~ | ✅ | ✅+ | ✅++ |
| Media / press | — | — | — | — | — | ✅ | ✅++ |
| Fines / discipline | — | — | — | — | — | ✅ | ✅ |
| Contracts / wages | — | ✅ | ✅+ | ✅+ | ✅++ | ✅++ | ✅+++ |
| Finances / budgets | ~ | ✅ | ✅ | ✅ | ✅ | ✅+ | ✅++ |
| Cups (domestic) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| European cups | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| International mgmt | — | — | ~ | U21 | ✅ | ✅ | ✅ |
| Reserves | — | ✅ | ✅ | ✅ | ✅ | ✅ | ✅+ league |
| Youth / U19 | — | — | — | — | ✅ | ✅ | ✅+ league |
| Friendlies | — | — | ✅ | ✅ | ✅ | ✅ | ✅ |
| Multiplayer | hotseat | hotseat | hotseat | hotseat | LAN 16 | LAN | network+ |
| Editor | — | disks | — | built-in | — | official | + skins |
| Player notes | — | — | — | — | — | ✅ | ✅+ |
| Compare players | — | — | — | — | — | ✅ | ✅ |
| Database size opt | — | — | — | — | — | — | ✅ S/M/L |

---

# What “full legacy CM4 features” means for Futtoboru

Not every CM4 bullet applies to **1888 England**:

| CM4 feature | Futtoboru relevance |
|-------------|---------------------|
| 39 nations / 200k players | **Scale only** — 12 clubs, ~300 players |
| 2D match engine | **Optional** — abstract sim acceptable for v1 |
| Women's football, VAR, data hub | **N/A** |
| EU transfer windows | **N/A** — adapt to 1888 signing culture |
| League + cup + season loop | **Core** |
| Historical competition rules | **Core** |
| Board + finances + contracts | **Core** |
| Training, tactics, scouting | **Legacy target** — post-v1 breadth |
| Morale, media, personalities | **Legacy target** |
| Reserves/youth leagues | **Broader** for 1888 (amateur era) |
| Multiplayer / editor | **Deferred** |

**Design principle:** Match **system depth** of CM4 at **1888 data scale**, not clone CM4's geographic scope.

---

## Sources

- [Championship Manager — Wikipedia](https://en.wikipedia.org/wiki/Championship_Manager)
- [MobyGames CM series entries](https://www.mobygames.com/)
- [PC GamesN — CM history Part 1](https://www.pcgamesn.com/championship-manager-series/championship-manager-football-manager-history-making-of)
- [Eurogamer — CM99/00, CM02/03 Xbox](https://www.eurogamer.net/)
- GameFAQs guides (CM4, CM02/03, CM3)
- [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Round 2 — links to CM1–CM3 refs, CM4 community supplement, manuals index, PM brief |
| 2025-06-25 | Initial legacy reference CM1–CM03/04 |
