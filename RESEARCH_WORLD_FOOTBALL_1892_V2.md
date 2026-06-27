# Research — Worldwide Football circa 1892 (V2 Expansion Scope)

**Document type:** Historical research / product planning  
**Date:** 2026-06-27  
**Audience:** PO, PM, World & Data, Research  
**Purpose:** Answer “what other teams, leagues, and countries existed worldwide around 1892?” and plan **V2** research packs (after Layer C England 1892).

**Companion:** [PM_HISTORICAL_DATA_EXPANSION_REPORT.md](PM_HISTORICAL_DATA_EXPANSION_REPORT.md) · [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) · [HISTORICAL_DATA_SOURCE_LEDGER.md](HISTORICAL_DATA_SOURCE_LEDGER.md)

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **Was football only England in 1892?** | **No** — several **British** nations had organized league or cup football; **Netherlands** had a national league; **Argentina** had a one-year league (1891) and restarted **1893**. |
| **Best V2 candidates (playable league + researchable squads)** | **Scotland**, **Ireland (IFA / Irish League)**, then **Wales (cup + regional)**, then **Netherlands**. |
| **Exotic / milestone later** | Argentina 1893+, Uruguay 1900+, continental Europe mostly **1900+** for national leagues. |
| **Same career from 1888 England start?** | Yes — via **world evolution scripts** ([DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)), not separate careers only. |
| **Effort per country (≈ season 18 fidelity)** | **~1×** season 18 mod each for Scotland / Ireland; Wales **~0.6×** (cup-heavy, weak league); Netherlands **~1×** + rules research. |

**V2 scope (proposed):** After **England 1892 Div 2** (Layer C), add **1–2 British nations end-to-end**, research **Wales + Netherlands** packs, catalogue **Americas** for V3 milestones.

---

## 2. What “1892” means in this doc

Calendar **1892–93 season** (autumn 1892 → spring 1893):

- Aligns with English **Division Two** creation.  
- Scottish league **season 3**; Scottish **Div 2 decided July 1893** (timeline script target).  
- Irish League **season 4**.  
- Wales: **no national league** — Welsh Cup + regional leagues.  
- Argentina: **no league championship in 1892** (gap year between 1891 and 1893 associations).

---

## 3. Tier 1 — British Isles (highest priority for Futtoboru)

These share rules culture, travel distance, and source overlap (RSSSF, Wikipedia season articles, British newspapers).

### 3.1 Scotland ✅ **#1 V2 target**

| Item | Detail |
|------|--------|
| **National league** | Scottish Football League — founded **1890** (3rd oldest national league after England & Ireland) |
| **1892–93 Div 1** | **10 clubs**, 18 games each: Celtic, Rangers, St Mirren, Third Lanark, Hearts, Dumbarton, Leith Athletic, Renton, Abercorn, Clyde |
| **1892–93 champion** | **Celtic** (first league title) |
| **Cup** | Scottish Cup (ongoing); Queen’s Park strong |
| **1893 event** | Scottish **Second Division** formed **July 1893** (10 clubs incl. Hibernian, Partick Thistle, Morton, Motherwell) — mirror England 1892 pattern |
| **Re-election** | Bottom clubs re-elected / replaced (Abercorn, Clyde out 1893) |
| **Rules** | 2 pts win era; own FA; cross-border friendlies vs English clubs |
| **Est. content** | ~10–12 league clubs × ~20–25 players ≈ **400–600 named players**; ~10 stadiums |
| **Research pack** | **R-H4** (existing stub) → expand to **R-H4b 1892–93 full** |
| **Activation** | `COUNTRY_ACTIVATION_SCRIPT` 1890 or start mod Scotland 1890–91; Div 2 script 1893 |

**Sources:** [RSSSF Scotland 1892/93](https://www.rsssf.org/tabless/scot1893.html) · [Wikipedia 1892–93 Scottish football](https://en.wikipedia.org/wiki/1892%E2%80%9393_in_Scottish_football)

---

### 3.2 Ireland (IFA — Irish League, Northern Ireland today) ✅ **#2 V2 target**

| Item | Detail |
|------|--------|
| **National league** | **Irish League** founded **1890** (same week as Scotland — second-oldest national league in world) |
| **1890–91** | 8 clubs (mostly **Belfast**): Linfield, Distillery, Cliftonville, Glentoran, Ulster, Oldpark, Clarence, Milford |
| **1892 note** | **Derry Olympic** joined as second non-Belfast club (one season) |
| **Geography** | Not “all Ireland” politically today — IFA / Irish League = **Northern Ireland** focus; Republic has separate FAI history (1921 split) |
| **Cup** | Irish Cup |
| **Est. content** | ~8–10 clubs × ~15–20 players ≈ **150–200 players** (smaller than Scotland) |
| **Research pack** | **R-H7** Ireland 1890–93 (new) |
| **Futtoboru note** | PO must decide: model **IFA Irish League** only for 1892, not modern ROI league system |

**Sources:** [Wikipedia Irish League](https://en.wikipedia.org/wiki/Northern_Ireland_Football_League) · [1890–91 Irish League](https://en.wikipedia.org/wiki/1890%E2%80%9391_Irish_League)

---

### 3.3 Wales ⚠️ **#3 — cup + regional, not full national league**

| Item | Detail |
|------|--------|
| **National league in 1892?** | **No** — national Welsh league system **1902** (Welsh Football League south); **Cymru Premier 1992** |
| **What existed** | **Welsh Cup** (from 1877); **South Wales League** (~1891, turbulent membership); **North Wales Coast League** and other regionals |
| **Key clubs** | **Wrexham**, Bangor City, Rhyl, Cardiff area clubs — many entered **English FA Cup** |
| **1892–93** | Wrexham etc. in cup runs; structured league data **sparse** vs Scotland |
| **Est. content** | Welsh Cup draw + 6–10 clubs ≈ **200–350 players** if cup-focused mod |
| **Research pack** | **R-H8** Wales 1890s (cup + South Wales League snapshot) |
| **Playable model** | **Cup + friendlies** path first; full league deferred to **1902** milestone |

**Sources:** [FAW history](https://faw.cymru/news/celebrating-150-years-of-the-football-association-of-wales/) · [Welsh Football League history](https://en.wikipedia.org/wiki/Welsh_Football_League)

**Engine note:** Wales country id `1002` exists in mods but is **commented out** in season 18 — enable when pack ready.

---

### 3.4 British Home Championship (optional mini-competition)

| Item | Detail |
|------|--------|
| **What** | Annual **England vs Scotland vs Wales vs Ireland** (international) |
| **1892–93** | England won; Scotland 2nd; Ireland 3rd; Wales 0 pts |
| **Futtoboru fit** | **Optional** — `COMPETITION_CREATION_SCRIPT` for internationals; **static squads** conflict (no real international squads in v1 policy) |
| **Recommendation** | **Research only (R-H9)** for V3; **defer playable** until person pipeline / national selection design |

---

## 4. Tier 2 — Continental Europe (1890s leagues)

| Country | National league from | circa 1892 status | V2 research? |
|---------|---------------------|-------------------|--------------|
| **Netherlands** | **1890** (NBvB) | Active national competition; western clubs (Amsterdam, Haarlem, Rotterdam, Hague) | ✅ **R-H10** — first continental league model |
| **Belgium** | 1895 | Clubs exist; league **just after** 1892 window | R-H11 stub — **V3** |
| **Switzerland** | 1897 | Regional | V3+ |
| **Italy** | 1929 (Serie A) | Regional / friendly era only | Out of 1892 scope |
| **Germany** | 1903 (Bundesliga ancestor regional) | Regional | Out of 1892 scope |
| **France** | 1932 | Cup 1917+ | Out of 1892 scope |

**Netherlands detail (R-H10):**

- Founded Dec **1889**; league seasons **1890–91** onward.  
- Different association (KNVB/NBvB), travel, **Dutch squad names** — high research cost, high differentiation vs British mods.  
- **~1× season 18 effort** if done at named-player fidelity.

**Source:** [Utrecht University — Early Dutch league 1890–91](https://research-portal.uu.nl/en/publications/english-it-must-be-early-dutch-football-and-its-first-league-comp/)

---

## 5. Tier 3 — Americas (milestone / V3+)

| Country | League from | circa 1892 | Notes |
|---------|-------------|------------|-------|
| **Argentina** | **1891** (one season); **1893** stable AFA | **No 1892 championship** | Five clubs 1891; British-expat clubs; **R-H12** for **1893–94** start; **hiatus + restart** required — [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) §6 |
| **Uruguay** | **1900** (official league) | Clubs from **1891** (CURCC / Peñarol lineage) | Research stub only |
| **Paraguay** | 1906 | — | V4+ |
| **USA** | 1921 | Amateur/regional | Out of scope |

**Argentina note:** First league **outside British Isles** — strong **marketing milestone**, weak **1892 calendar** fit. Recommend **1893–94 Primera** as first Americas start mod. **Fork design:** clubs may **vote** on hiatus vs continuation in **Flexible** mode — [PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md](PO_DECISION_ARGENTINA_LEAGUE_HIATUS.md).

---

## 6. Rest of world (1892)

| Region | Organized league football circa 1892 |
|--------|--------------------------------------|
| **Africa** | None (Ghana league 1956, etc.) |
| **Asia** | None (Japan 1965, etc.) |
| **Oceania** | None (Australia 1977) |

**Conclusion:** Futtoboru’s historical wedge **1888–1900** is realistically **British Isles + Netherlands + later Argentina**.

---

## 7. Comparison to Futtoboru today

| Asset | Today |
|-------|-------|
| Countries in `mods/countries.txt` | ~**193** (registry only) |
| Playable at new game | **England** only |
| Leagues in career | **1** (English FL via script) + FA Cup |
| Wales in season 18 | Commented out in `countries.txt` |
| Cross-border | Linfield etc. not in FA Cup mod yet |

---

## 8. Proposed V2 research programme

### 8.1 Research packs (new IDs)

| ID | Pack | Priority | Unlocks | Est. effort |
|----|------|----------|---------|-------------|
| **R-H4b** | Scotland **1892–93** full (10 clubs + cup) | **P0** | Second country in same career |
| **R-H7** | Ireland **1890–93** Irish League | **P1** | Third British nation |
| **R-H8** | Wales **1890s** cup + regional | **P2** | Cup path; enable country 1002 |
| **R-H9** | British Home Championship | **P3** | Design doc only |
| **R-H10** | Netherlands **1890–91** league | **P2** | First continental nation |
| **R-H11** | Belgium **1895–96** | **P3** | V3 milestone |
| **R-H12** | Argentina **1893–94** | **P3** | First Americas start |

Each pack deliverable template:

```
research_packs/{id}/
  README.md           — sources, PO notes
  clubs.csv           — id, name, founded, city, country
  league_table.csv    — 1892–93 if applicable
  squads/             — player lists with source URLs
  competitions.md     — league + cup + dates
  timeline.md         — activation dates for world evolution
  SOURCES.md          — ledger IDs
```

### 8.2 Engineering prerequisites (same as 1892 England)

| ID | Required before multi-country V2 |
|----|----------------------------------|
| WE-1–WE-6 | World evolution + save-as-truth |
| DB-1 | Global person/club IDs |
| DB-2 | Validation CI |
| **MC-1** | `COUNTRY_ACTIVATION_SCRIPT` (1006) |
| **MC-2** | Multi-country `selectedCountries` at new game OR mid-career activation |
| **MC-3** | Cross-border cup entries (Welsh clubs in FA Cup) |

### 8.3 Suggested V2 phasing (after Layer C England 1892)

| Phase | Content | PO outcome |
|-------|---------|------------|
| **V2a** | Scotland activation (1890 or 1892 script) + Scottish Cup | Second nation playable |
| **V2b** | Ireland Irish League | Third nation |
| **V2c** | Wales Welsh Cup + regional | Cup-heavy nation |
| **V2d** | Netherlands OR Argentina milestone | First non-British pickable start |

**Parallel:** England continues — Scottish Div 2 (1893) and English Div 2 (1892) both use same world-evolution patterns.

---

## 9. Content scale estimate (V2 all Tier-1 British)

| Nation | Clubs (order) | Players (order) | New competitions |
|--------|---------------|-----------------|------------------|
| Scotland | 10 (+10 Div2 later) | 400–600 | SFL + Scottish Cup |
| Ireland | 8–10 | 150–200 | Irish League + Irish Cup |
| Wales | 6–10 (cup) | 200–350 | Welsh Cup (+ regional optional) |
| **Total incremental** | ~25–30 | ~750–1,150 | 3–5 comps |

Compare season 18 England: **35 clubs, 556 players**.

---

## 10. PO decisions (V2 scope)

| # | Question | Options |
|---|----------|---------|
| 1 | **First country after England 1892?** | **A)** Scotland **B)** Ireland **C)** Wales cup-only |
| 2 | **Same career vs new start mod per country?** | **A)** World evolution only **B)** Also separate pickable starts |
| 3 | **Netherlands in V2 or V3?** | V2 research / V3 playable |
| 4 | **Americas (Argentina) when?** | V3 milestone minimum |
| 5 | **All-Ireland vs Northern Ireland only?** | IFA 1892 historical vs modern geography |

**Defaults if silent:** 1-A Scotland, 2-A evolution + optional Scotland 1890 start mod, 3-V3 playable, 4-V3, 5-IFA Northern-focused with doc note on 1921 split.

---

## 11. Document map

| Doc | Role |
|-----|------|
| [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) | England Div 1/2 only |
| [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) | Layer A/B/C before V2 |
| [HISTORICAL_DATA_SOURCE_LEDGER.md](HISTORICAL_DATA_SOURCE_LEDGER.md) | Source IDs per pack |
| [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) | How nations enter save |

---

## 12. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial worldwide 1892 research for V2 planning |

---

*Next update: after R-H4b Scotland pack started or PO locks V2 country order.*
