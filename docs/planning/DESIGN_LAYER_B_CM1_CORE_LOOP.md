# Design — Layer B CM1 Core Loop (Critical Path)

**Document type:** Product / gap analysis / implementation design  
**Date:** 2026-06-27  
**Status:** **Active** — PO adopted **default critical path**; fork UI **deferred**  
**Audience:** PO, PM, Match/Sim, Platform/UX, QA  
**Research base:** [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) · [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md) (attendance only)

**Execution:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md §5](PM_V1_MVP_AND_1892_MASTER_PLAN.md) · [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) · [ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md](ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md) · [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md)

---

## 1. PO direction (locked 2026-06-27)

| Decision | Choice |
|----------|--------|
| **Sequence** | **Default critical path** — H1 QA → **H2 Layer B** → H3 1892 → H4 world/V2 |
| **Management benchmark** | **CM1 (1992) minimum loop** — not CM3/CM4 depth |
| **FM borrow for Layer B** | **Attendance model only** (table + opponent + form + derbies) — not FM tactics/transfers/scouting |
| **Fork UI** | **Deferred** — design kept; implement after Layer B + WE-1 |
| **Historical world (news/knowledge)** | **Deferred to H3/H4** — do not block Layer B |

---

## 2. CM1 core loop — what we must match (Layer B)

From [CM1_REFERENCE.md §4.1](CM1_REFERENCE.md) — the **weekly management rhythm**:

```
Pick club → Set lineup / formation → Advance time → Match resolves → Player ratings (0–10)
     ↑___________________________________________|
              (repeat until season ends)
```

| CM1 step | CM1 had | Futtoboru Layer B target | ID |
|----------|---------|--------------------------|-----|
| Employed at club | ✅ | ✅ (today) | — |
| **Pick XI (11)** | ✅ menus | ❌ sim uses full squad | **MVP-1** |
| **Formation / tactics** | ✅ basic presets | 🔲 stub screen | **MVP-2**, **MVP-9** |
| Advance time (Done) | ✅ | ✅ Continue (Layer A QA) | A-2 |
| **Abstract match** | ✅ text strength | ⚠️ heavy RNG, no XI | **MVP-3** |
| **Post-match ratings** | ✅ **genre first** | ❌ | **MVP-5** |
| **Result feedback** | ✅ score + names | ⚠️ latest match only | **MVP-6** |
| **Preview before match** | ✅ | 🔲 stub | **MVP-7** |
| Season completes + rolls | ✅ | ✅ **proven headless** | — |
| Transfers between matches | ✅ basic | ❌ **post–Layer B** | defer |
| Training | ✅ basic | ❌ stub | defer |
| Match commentary text | ✅ | ❌ | defer (P3) |

**Layer B exit:** Player feels the **CM1 loop** — your XI and formation **matter**, crowds **make sense**, you see **ratings** after matches. Season engine unchanged.

---

## 3. FM vs CM — what we take for Layer B only

Full FM depth is **out of scope** for Layer B. One FM-derived slice is **PO locked**:

| FM idea | Use in Futtoboru | Layer |
|---------|------------------|-------|
| Attendance ↔ league position | ✅ | **B2 — MVP-4** |
| Attendance ↔ opponent quality | ✅ | **B2** |
| Attendance ↔ recent form | ✅ | **B2** |
| Derby boost | ✅ (mod file) | **B2** |
| Expected goals / xG | ❌ | never MVP |
| Press conferences | ❌ | H4+ |
| Transfer market / scouting | ❌ | post–Layer B |
| Dynamic reputation / media | ❌ | CM4 / H4 |

Formula: [PM_V1_MVP_AND_1892_MASTER_PLAN.md §5.3](PM_V1_MVP_AND_1892_MASTER_PLAN.md).

---

## 4. Gap analysis — current truth (2026-06-27)

### 4.1 Already ahead of or equal to CM1 (do not rebuild)

| Area | Status | Evidence |
|------|--------|----------|
| Season completes + champion | ✅🧪 | `SeasonPlaythroughIT`, `TenSeasonReportIT` |
| Procedural rollover | ✅🧪 | `MultiSeasonSoakIT`, `LongRunSoakIT` |
| 1888 competition rules loader | ✅🧪 | `CompetitionRulesLoaderTest`, mod `competition_rules.txt` |
| FA Cup headless season | ✅🧪 | `LeagueAndCupIT` |
| Real 1888 player/club names | ✅ | Mod 18 — **ahead of CM1 anonymous numbers** |
| Inbox + mandatory draw | ✅ | `MandatoryGateIT` |
| Job / unemployed path | ✅ | Beyond CM1 — not Layer B gate |
| Save JSON + `HistoricalMode` | ✅🧪 | `SaveLoadIT` |

> **Note:** [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) §3.2 still lists season-end as unwired — **superseded** by automation (see [SYSTEMS_AND_FEATURES_REGISTRY.md §0](SYSTEMS_AND_FEATURES_REGISTRY.md)).

### 4.2 Layer A gaps (before Layer B code)

| ID | Gap | Status |
|----|-----|--------|
| A-1 | Manual QA checklist | **Open** |
| A-2 | Continue double-click | **Coded** — QA |
| A-3 | Load game + job init | **Coded** — QA |
| A-4 | Match history → result | **Coded** — QA |
| A-5 | 2-1-0 + goal average in league UI | ✅ **Automated** |
| A-9 | PO Layer A sign-off | **Open** |

### 4.3 Layer B gaps — **the management sim** (CM1 P1)

| ID | CM1 / genre | Code today | Missing work |
|----|-------------|------------|--------------|
| **MVP-10** | Save lineup per club | **Deferred** — add JSON fields in B1; formal versioning later — [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md) |
| **MVP-1** | Pick XI | Sim ignores selection | ✅ `BestElevenSelector`, Squad Best XI, `ClubTacticsSetup` |
| **MVP-3** | Match uses lineup | `MatchSimulator` squad avg | ✅ v2 reads `MatchLineup` only |
| **MVP-8** | Plausible champions | Accrington 8/10 | Re-tune after MVP-3; `TenSeasonReportIT` |
| **MVP-4** | Crowds react to context | Random 60–90% | `AttendanceCalculator` + `mods/derbies.txt` |
| **H-1** | Finances ↔ attendance | Sim overwrites attendance | Single attendance authority |
| **MVP-5** | 0–10 ratings | None | Post-match map on XI |
| **MVP-6** | Result screen | Latest only; history fixed | Scorers, attendance, ratings snippet |
| **MVP-7** | Preview | Stub | Editable XI + opponent auto XI |
| **MVP-2** | Formation pick | Placeholder | `FormationRegistry` + 2–3 templates |
| **MVP-9** | Tactics menu | Not in sidebar | Wire `TacticsScreenTable` |

### 4.4 Explicitly NOT Layer B (defer — CM/FM depth)

| Feature | CM/FM | When |
|---------|-------|------|
| Transfers buy/sell | CM93+ | Post–Layer B |
| Training schedules | CM3 | Post–Layer B |
| Scouting / fog of war | CM01/FM | **H4 KNOW-*** (partial opponent after match in B3 optional) |
| Injuries / fitness sim | CM93+ | Post–Layer B |
| Board sacking depth | CM1 basic | Post–Layer B |
| In-match subs / tactical changes | CM1 limited | Post–Layer B |
| 2D match engine | CM4 | Out of scope v1 |
| Fork vote UI / journalist agency | Futtoboru H4 | **After WE-1** — PO deferred |
| World news / hiatus messages | Futtoboru H4 | **NEWS-2** after WE-1 |
| 1892 Div 2 content | H3 Layer C | After Layer B |
| Knowledge graph full | H4 | KNOW-1+ |

---

## 5. Layer B design — mapped to CM1 menus

CM1 main menu (manual): Continue · **Tactics** · Transfers · Tables · Cups · Resign

| CM1 menu | Layer B Futtoboru screen | MVP |
|----------|--------------------------|-----|
| **Continue** | Top bar Continue | ✅ (Layer A) |
| **Tactics** | Sidebar **Tactics** (`TacticsScreenTable`) | MVP-2, MVP-9 |
| *(lineup)* | Squad screen pick 11 / Best XI | MVP-1 |
| **Tables** | League detail / standings | ✅ 2-1-0 automated (A-5) |
| **Cups** | Competitions / draw | ✅ headless; UI smoke A-6 |
| Match result | Match result + history click | MVP-6, A-4 |
| Match preview | Before match | MVP-7 |
| Transfers | — | **Not Layer B** |
| **Ratings** | On result + optional squad column | MVP-5 |

### 5.1 Data model (B1 — no formal saveVersion yet)

See [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md):

```
FormationTemplate (registry)
ClubTacticsSetup (per club, persisted in JSON)
MatchLineup (snapshot at kickoff → MatchSimulator)
```

**Rule:** `MatchSimulator` reads **`MatchLineup` only** for strength, scorers, ratings.

### 5.2 Sim v2 inputs (MVP-3)

| Input | Source |
|-------|--------|
| XI strength | Avg attributes of 11 in `MatchLineup` |
| Formation | Slot weights from `FormationTemplate` |
| Home advantage | +15% |
| Form / opponent | League rank modifier (light) |
| RNG band | 0.85–1.15 |

### 5.3 Attendance (MVP-4 + FM borrow)

| Input | Source |
|-------|--------|
| Base fill | Match type (league / cup / friendly) |
| Table position | Home club rank |
| Opponent | Away club rank |
| Form | Last 5 results |
| Derby | `mods/derbies.txt` (may be empty 1888) |

**Fix H-1:** ticket sales and sim share one `AttendanceCalculator` output.

### 5.4 Ratings (MVP-5) — CM1 genre hook

| Adjustment | Value |
|------------|-------|
| Base (played in XI) | 6.0 |
| Goal | +0.5 |
| Win / loss | +0.3 / −0.3 |
| Strength delta | ±0.2 |
| Clamp | 4.0 – 9.5 |

Store `lastMatchRatings` on club or match snapshot; show on MVP-6.

---

## 6. Phased delivery (Layer B only)

| Phase | IDs | CM1 equivalent | Exit test |
|-------|-----|----------------|-----------|
| **B0** | A-1, A-5, A-9 | Playable Done + tables | A-5 ✅ automated; A-9 PO sign-off |
| **B1 — Sim core** | DATA-B1, MVP-1, MVP-3, MVP-8 | Lineup affects matches | `MatchSimulatorV2Test`; 10-season diversity |
| **B2 — Crowds** | MVP-4, H-1 | Crowds “feel” real | Attendance IT; finances avg |
| **B3 — Feedback** | MVP-5, MVP-6, MVP-7 | Ratings + result/preview | Manual CM1 loop walkthrough |
| **B4 — Tactics UX** | MVP-2, MVP-9 | Tactics menu | Sidebar + formation picker |

**Parallel allowed:** B2 after B1 starts; B3 after B1; **not** Layer C or fork UI in parallel unless PO adds capacity.

**Estimated:** 6–10 eng-weeks after B0.

---

## 7. Acceptance — “Layer B complete”

PO can call **V1 MVP** done when:

- [ ] Player picks **11** and **formation** before league match  
- [ ] **Stronger XI** measurably wins more in IT  
- [ ] **Attendance** correlates with table/opponent (not random)  
- [ ] **0–10 ratings** appear after every match for XI players  
- [ ] **Result screen** shows score, scorers, attendance, rating snippet  
- [ ] **Tactics** reachable from sidebar  
- [ ] **TenSeasonReportIT** shows more plausible champion spread  
- [ ] Layer A manual QA still green  

Does **not** require: transfers, training, fork UI, 1892, world news, Scotland pack.

---

## 8. After Layer B — do not skip

| Horizon | Next | Prerequisite |
|---------|------|--------------|
| **H3 Layer C** | WE-1 + 1892 Div 2 | Layer B + PO 1892 membership A/B/C/D |
| **H4 World** | NEWS-2, FLEX-2, KNOW-1 | WE-1 |
| **V2 Scotland** | R-H4b | Layer C infra |

---

## 9. Engineering backlog summary

| ID | Task | Phase |
|----|------|-------|
| MVP-10 | Persist + migrate save schema | **Deferred** — see [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md) |
| DATA-B1 | `ClubTacticsSetup` + `FormationRegistry` on JSON save | B1 |
| MVP-1 | Pick XI + best XI | B1 |
| MVP-3 | `MatchSimulator` v2 | B1 |
| MVP-8 | Balance pass | B1 |
| MVP-4 | `AttendanceCalculator` | B2 |
| H-1 | Stop attendance overwrite | B2 |
| MVP-5 | Ratings | B3 |
| MVP-6, MVP-7 | Result + preview UI | B3 |
| MVP-2, MVP-9 | Formation + sidebar | B4 |
| TEST | `MatchSimulatorV2Test` | B1 |

---

## 10. Cross-references

| Doc | Role |
|-----|------|
| [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) | Master roadmap + analyze/design/develop/test |
| [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) | Unit + IT + manual B-9 |
| [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) | Execution plan detail |
| [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) | Schema detail |
| [ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md](ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md) | Ticket revenue |
| [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) | Layer A criteria |
| [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) | H4 — not Layer B |

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial Layer B design from CM1 benchmark + critical path adoption |
| 2026-06-25 | Roadmap + test plan — [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md), [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) |

---

*Canonical “what Layer B must deliver” for engineering and PO sign-off on V1 MVP.*
