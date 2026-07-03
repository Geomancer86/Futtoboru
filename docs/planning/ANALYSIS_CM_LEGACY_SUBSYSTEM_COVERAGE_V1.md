# Analysis — CM Legacy Subsystem Documentation Coverage

**Document type:** Research inventory / gap analysis  
**Date:** 2026-07-03  
**Audience:** PO, PM, design leads  
**Question:** Do we have **full features documented** for CM93/94 and early CM — transfers, market, free players, assistants, stadium expansions, etc.? Do we have **bibles per game**?

**Related:** [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md) · [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) · [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md)

---

## 1. Executive answer

| Question | Answer |
|----------|--------|
| **Bibles per game?** | **Yes for major releases** — one dedicated ref doc each for CM1, CM93/94, CM2, CM97/98, CM3, CM4; plus master [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) and FM [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) (2003+ partial). |
| **Full feature documentation?** | **No** — release bibles + manual extracts cover **menus and headline systems**, but there is **no subsystem bible** (like [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md)) for **transfers, staff, board/stadium, training**, except injuries. |
| **CM93 transfers / market documented?** | **Partial** — primary manual text + [CM93_Manual_Menu_Extract.md](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md) §Transfers; **not** a standalone design ledger with IDs. |
| **Free agents / OOC?** | **CM1 manual** (tribunal); **CM93 manual** (list/loan/buy flow) — in extracts, not Futtoboru subsystem doc. |
| **Assistant managers?** | **Not CM93** — **CM4** delegation ([CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)); FM2016+ assistant role in FM bible. Early CM has **Coach / Physio / Scouts**, not “assistant manager”. |
| **Stadium expansions?** | **Not CM93/94** — debuts **CM99/00** ([CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) master timeline); CM4 board stadium upgrades documented shallowly. |
| **Blocks R-1888?** | **No** — transfers/staff/stadium are **Layer C+ / later R-YYYY**; research sufficient to **defer** implementation. |

---

## 2. “Bible” inventory — what exists per game

| Game | Dedicated ref (“bible”) | Manual in repo | Menu / subsystem extracts | Depth |
|------|-------------------------|----------------|------------------------------|-------|
| **FM 1982** | [PRE_CM1_GENRE_HISTORY.md](PRE_CM1_GENRE_HISTORY.md) §1 | ✅ HTML | — | Deep |
| **CM1 (1992)** | [CM1_REFERENCE.md](CM1_REFERENCE.md) | ✅ HTML + [menu extract](../research/manuals/cm1/CM1_Manual_Menu_Extract.md) | Transfers, board, jobs in extract | **Deep** |
| **CM93/94** | [CM93_94_REFERENCE.md](CM93_94_REFERENCE.md) | ✅ Lemon text + [menu extract](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md) | Transfers, staff, discipline in extract | **Medium–Deep** |
| **CM2 (1995)** | [CM2_REFERENCE.md](CM2_REFERENCE.md) | ⚠️ FR PDF + EN guide | — | Medium |
| **CM97/98** | [CM9798_REFERENCE.md](CM9798_REFERENCE.md) | ⚠️ scan only | Metadata | Shallow |
| **CM3 (1999)** | [CM3_REFERENCE.md](CM3_REFERENCE.md) | ✅ PDF + TOC extract | Training/scouts in ref | Medium–Deep |
| **CM4 (2003)** | [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) + [community doc](CM4_COMMUNITY_DEVELOPERS_AND_SUPPORT.md) | ⚠️ ReadMe only | Community FAQs | Deep (patches) |
| **FM 2005+** | [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) + [ROUND1](FM_COMPETITIVE_BIBLE_ROUND1.md) | — | Round 1 partial | Partial |
| **Cross-release** | [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) | — | Master debut timeline | Deep index |
| **Cross-release features** | [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) | — | **M/S matrix** all releases | Medium |

**Only subsystem-level “bible” today:** injuries — [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) + [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md).

---

## 3. Subsystem × CM era × documentation

Legend: **Doc** = research sufficient for design · **Partial** = manual/ref exists, no ID ledger · **Gap** = thin or missing primary · **N/A** = feature did not exist in that era

| Subsystem | CM1 | CM93/94 | CM2 | CM3 | CM4 | Doc status | Futtoboru (R-1888) |
|-----------|-----|---------|-----|-----|-----|------------|----------------------|
| **Transfer search / shortlist** | ✅ | ✅ | ✅ | ✅ | ✅ | **Partial** (manual extracts) | ❌ Layer C+ |
| **Transfer list / sell** | ✅ | ✅ | ✅ | ✅ | ✅ | **Partial** | ❌ |
| **Multi-club bidding / negotiation** | ✅ | ✅ | ✅ | ✅ | ✅ | **Partial** | ❌ |
| **Loans** | ⚠️ | ✅ manual | S | ✅ | ✅ | **Partial** | ❌ |
| **Foreign / intl pool** | ⚠️ | ✅ | ✅ | ✅ | ✅ | **Partial** | ❌ (1888 scope) |
| **Free transfer / OOC / tribunal** | ✅ CM1 | ⚠️ manual | S | ✅ | ✅ | **Partial** | ❌ |
| **Transfer deadline / windows** | ❌ | ❌ | ✅ grey names | ✅ EU | ✅ | **Partial** (CM2+ ref) | ❌ |
| **Weekly transfer news** | ⚠️ | ✅ manual | S | ✅ | ✅ | **Partial** | ❌ |
| **Scouts (assigned)** | ⚠️ staff DB | ✅ 3 league + youth | S | ✅ | ✅ | **Partial** | ❌ |
| **Coach / trainer** | ✅ staff | ✅ | ✅ | ✅ | ✅ | **Partial** | ⚠️ Trainer ME-1 (injury only) |
| **Physio** | ✅ staff | ✅ | ✅ | ✅ | ✅ | **Deep** (injury ledger) | B5.2 engine |
| **Assistant manager (delegation)** | ❌ | ❌ | ❌ | ❌ | ✅ | **Partial** (CM4 ref) | ❌ |
| **Board confidence / resign** | ✅ | ✅ | ✅ | ✅ | ✅ | **Partial** | ⚠️ comments only |
| **Manager jobs / unemployed** | ✅ | ✅ | ✅ | ✅ | ✅ | **Partial** + Futtoboru backend | Stub UI |
| **Fines / discipline** | ✅ | ✅ expanded | S | ✅ | ✅ | **Partial** | ❌ |
| **Insurance (player)** | ✅ CM1 | ✅ CM93 | S | ✅ | ✅ | **Deep** (injury) | ME-1 hidden |
| **Stadium expansion / upgrades** | ❌ | ❌ | ❌ | ✅ CM99/00 | ✅ board | **Gap** (timeline only) | ❌ |
| **Separate transfer/wage budget** | ❌ | ❌ | ❌ | ⚠️ | ✅ CM4 | **Partial** | ❌ |
| **Training schedules** | ❌ | ❌ | ⚠️ | ✅ | ✅ | **Partial** (CM3+) | Stub screen |
| **Reserve squad [RSRV]** | ❌ | ✅ | ✅ | ✅ | ✅ leagues | **Partial** | ❌ |
| **Arrange friendly** | ❌ | ✅ | ✅ | ✅ | ✅ | **Partial** | ⚠️ engine exists |

---

## 4. CM93/94 — what the manual already documents (not yet ledgered)

Source: [CM93_Manual_LemonAmiga_Extract.md](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md) · [CM93_Manual_Menu_Extract.md](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md)

| CM93 feature | Manual evidence | Futtoboru subsystem doc? |
|--------------|-----------------|--------------------------|
| Find Player → Normal / Scout / Foreign search | ✅ P1 | ❌ no `RESEARCH_CM93_TRANSFER_*` |
| Shortlist, transfer list, scout [RECS] | ✅ P1 | ❌ |
| List / loan / revalue / not for sale / approaches | ✅ P1 | ❌ |
| Fine, insure, wage, renew on player menu | ✅ P1 | ✅ injury economy only |
| Backroom: Coach, Physio, 3× scouts, youth scout | ✅ P1 | ❌ |
| Transfer news round-up | ✅ P1 | ❌ |
| Player requests (stay/leave) | ✅ P1 | ❌ |
| No assistant manager role | ✅ (roles list) | N/A |

**Gap type:** **Organisation**, not **missing primary source**. Content is in manual extracts; not distilled into traceable IDs for PO/eng like injuries.

---

## 5. What research is still missing (honest)

From [CM_LEGACY_RESEARCH_BACKLOG.md](CM_LEGACY_RESEARCH_BACKLOG.md) — **research paused Round 16**; does **not** block R-1888.

| Category | Still missing | Blocks transfers design? |
|----------|---------------|---------------------------|
| **Manuals** | CM2 EN PDF, CM9798 OCR, CM4 printed manual | No for CM93 transfers |
| **Press** | CVG #140 CM93 (one UK review) | No |
| **Subsystem ledgers** | Transfers, staff, board, training (all eras) | **Yes for Layer C PO sign-off** |
| **Primary deep dives** | Stadium upgrades (CM99/00 manual pages) | Yes for R-1999+ tranche |
| **Assistant manager** | CM4 manual tactics delegation pp. 55–61 | Yes for CM4 parity only |
| **Free agent rules by era** | Cross-manual synthesis | Yes for transfer milestone |
| **Binary / saves** | CM93 saves, CM4 `.tac` | No (RE deferred) |

---

## 6. Futtoboru vs CM93 — what we intentionally have not built

From [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) §4.6:

| CM93 Core feature | Futtoboru depth | Planned release |
|-------------------|-----------------|-----------------|
| Transfers / market | **None** (stub UI) | **Layer C+** — not R-1888 |
| Staff (coach/scout/physio) | **Partial** (physio injury only) | R-1888 ME-1 Trainer |
| Board / stadium | **None** | R-1999+ / CM3 tranche |
| Unhappy / transfer request flags | **None** | Layer C+ |
| Reserve squad auto-fill | **None** | Layer C |

**R-1888 does not require CM93 transfer parity** — PO locked yearly release scope ([DESIGN_YEARLY_RELEASE_SCOPE.md](DESIGN_YEARLY_RELEASE_SCOPE.md)).

---

## 7. Recommended documentation backlog (when Layer C starts)

Mirror the injury pattern — one **research ledger** per subsystem before PO claims parity:

| Priority | Proposed doc | Primary sources |
|----------|--------------|-----------------|
| **P1** | `RESEARCH_CM93_TRANSFER_SUBSYSTEM.md` | CM93 manual §Transfers + CM1 §4.3 + [CM93_Manual_Menu_Extract](../research/manuals/cm93-94/CM93_Manual_Menu_Extract.md) |
| **P1** | `RESEARCH_CM93_STAFF_SUBSYSTEM.md` | CM93 [STAFF] section; CM1 staff DB note |
| **P2** | `RESEARCH_CM_TRANSFER_EVOLUTION.md` | CM_MANUALS_DEEP_ANALYSIS master table + CM2 deadline + CM3 loans |
| **P2** | `RESEARCH_CM_STADIUM_AND_BOARD.md` | CM3/CM99/00 refs; CM4 board § |
| **P3** | `RESEARCH_CM4_ASSISTANT_DELEGATION.md` | CM4_COMPLETE_REFERENCE + printed manual hunt |
| **P3** | `DESIGN_TRANSFERS_LAYER_C_V1.md` | After ledgers + R-1888 ship |

**Not recommended:** One giant “CM93 everything bible” — keep **release refs** + **subsystem ledgers** (injury model already proves this works).

---

## 8. Quick reference — where to read today

| I need… | Read |
|---------|------|
| Per-game overview | [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) |
| CM93 transfers (primary) | [CM93 manual extract](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md) · menu extract §Transfers |
| CM93 staff | Menu extract §Backroom staff |
| Feature matrix all releases | [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) |
| Honest “do we have everything?” | [CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md](CM_LEGACY_RESEARCH_COVERAGE_AUDIT.md) |
| Futtoboru depth vs CM | [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md) |
| Injuries only (full ledger) | [RESEARCH_CM93_INJURY_SUBSYSTEM.md](../research/RESEARCH_CM93_INJURY_SUBSYSTEM.md) |
| FM modern benchmark | [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) |

---

## 9. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial subsystem coverage analysis |

---

*Resume CM research rounds only when Layer C PO scopes transfers or a specific primary source is needed.*
