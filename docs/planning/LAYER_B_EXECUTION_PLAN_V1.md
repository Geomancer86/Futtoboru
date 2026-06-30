# Layer B — Execution Plan (V1 MVP Management Feel)

**Document type:** PM / engineering execution plan  
**Date:** 2026-06-25  
**Branch:** `analyze-ai-branch`  
**Status:** **Active** — **Layer B started** per [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) (2026-06-30)  
**Gap analysis:** [ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md](ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md)  
**Roadmap:** [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) · **Tests:** [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) · **Condition (B5):** [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md)

---

## 1. Goal

Deliver the **CM1-minimum loop**: pick XI → formation → advance time → match uses your team → credible attendance → **0–10 ratings** → readable result/preview.

**Not in this track:** transfers, training, world news, 1892 expansion, fork UI, per-screen UI polish at 200%.

---

## 2. PO decisions captured (2026-06-25)

| Topic | Decision |
|-------|----------|
| **MVP-10 save schema versioning** | **Deferred** — add fields to existing JSON save first; formal `saveVersion` + migrations later |
| **Future storage** | Consider **embedded document DB** (MongoDB-compatible or similar) for large saves — **V2+ release**, not Layer B blocker — see [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md) |
| **Layer A** | A-5 automated ✅; UI polish/bugs fixed **as needed** — **not a release gate** |
| **UI scale / shell** | ✅ Done (PU-10) — no further global UI work unless a screen is touched |
| **Match Engine Debug** | Hidden until NPE fixed (`DEBUG_SCREEN_UI_ENABLED`) |

---

## 3. Current code truth (starting point)

| Component | Today | Layer B change |
|-----------|-------|----------------|
| `MatchSimulator` | Full-squad attribute average; RNG 0.7–1.3; random scorers; attendance 60–90% random | Read **`MatchLineup`** only; reduced RNG; shared attendance |
| `TacticsScreenTable` | Placeholder; **not in sidebar** | MVP-2, MVP-9 |
| `SquadScreenTable` | List only; no XI pick | MVP-1 UI hooks |
| `MatchPreviewScreenTable` / `MatchResultScreenTable` | Stubs / latest match only | MVP-6, MVP-7 |
| `SaveGame` / `SaveLoadSystem` | LibGDX **JSON** files under `Documents/RndModGames/Futtuboru/savegames/` | Add **`ClubTacticsSetup`** on `Club` (serializable, no version field yet) |
| `MatchScheduler` / ticket sales | Sets attendance; **H-1** sim overwrites | Single `AttendanceCalculator` |

**Key files today:**

- `engine/simulation/MatchSimulator.java`
- `system/SaveGame.java`, `SaveLoadSystem.java`
- `menu/MainMenuManager.java` (screen routing)
- `tables/squad/SquadScreenTable.java`, `tables/main/TacticsScreenTable.java`
- `tables/match/preview/MatchPreviewScreenTable.java`, `tables/match/result/MatchResultScreenTable.java`

---

## 4. Revised phasing (MVP-10 deferred)

Formal save versioning (**MVP-10**) is **out of B1 entry**. Persist new objects via existing JSON roundtrip (`SaveLoadIT` must stay green). When MVP-10 lands later, it migrates the same fields — no throwaway prototype.

```
Layer A close (A-5, A-9)
        │
        ▼
┌───────────────────────────────────────────────────────────┐
│ B1 — Sim core (4–5 weeks)                                  │
│   FormationRegistry + ClubTacticsSetup on Club             │
│   MVP-1 Pick XI + auto Best XI                             │
│   MVP-3 MatchSimulator v2 (MatchLineup input)              │
│   MVP-8 Balance pass (TenSeasonReportIT)                   │
│   Tests: MatchSimulatorV2Test, StartingElevenIT            │
├───────────────────────────────────────────────────────────┤
│ B2 — Crowds & money (2–3 weeks) — may overlap late B1      │
│   MVP-4 AttendanceCalculator + mods/derbies.txt            │
│   H-1 Single attendance authority (ticket sales + sim)     │
│   Tests: AttendanceModelTest                               │
├───────────────────────────────────────────────────────────┤
│ B3 — Player feedback (2–3 weeks)                           │
│   MVP-5 Post-match ratings 0–10                            │
│   MVP-6 Result UI (score, scorers, attendance, ratings)    │
│   MVP-7 Preview UI (editable XI, opponent auto XI)         │
│   Manual: CM1 loop walkthrough                             │
├───────────────────────────────────────────────────────────┤
│ B4 — Tactics UX (1–2 weeks)                                │
│   MVP-2 Formation picker (2–3 templates)                   │
│   MVP-9 Wire Tactics in sidebar                            │
├───────────────────────────────────────────────────────────┤
│ B5 — Player condition (3–4 weeks) — after B-9 only         │
│   COND-1 fitness → COND-5 morale; injury generator         │
│   Tests: FitnessSimModifierIT, InjuredPlayerLineupIT       │
└───────────────────────────────────────────────────────────┘
        │
        ▼
   Layer B PO sign-off → Layer C (WE-1 / 1892)
```

**Total estimate:** 6–10 eng-weeks (unchanged), starting after Layer A.

---

## 5. Deliverable detail by MVP

### B1 — Sim core

| ID | Work | Acceptance |
|----|------|------------|
| **DATA-B1** | `FormationTemplate`, `FormationRegistry`, `ClubTacticsSetup`, `MatchLineup` classes | Serializable on `Club`; `copySaveGame` roundtrip |
| **MVP-1** | 11 selected player IDs per club; Squad or Tactics UI pick; **Best XI** auto-fill | User club has persisted XI; AI clubs auto-fill at sim time |
| **MVP-3** | `MatchSimulator` reads `MatchLineup`; slot weights from formation; RNG 0.85–1.15; scorers from XI | `MatchSimulatorV2Test`: stronger XI wins more often |
| **MVP-8** | Re-run `TenSeasonReportIT`; tune weights | Champion spread more plausible than Accrington 8/10 |

**Engine rule (locked):** `MatchSimulator` never reads full squad for strength — only **`MatchLineup`**.

### B2 — Crowds

| ID | Work | Acceptance |
|----|------|------------|
| **MVP-4** | `AttendanceCalculator` — table rank, opponent, last-5 form, derby flag | Top-table home club > bottom at same ground (IT) |
| **H-1** | Remove random overwrite in `MatchSimulator`; ticket path uses same calculator | Finances avg attendance correlates with table |

**Data:** `mods/derbies.txt` (may be empty for 1888).

### B3 — Feedback

| ID | Work | Acceptance |
|----|------|------------|
| **MVP-5** | Ratings map on XI after match (base 6.0, goal +0.5, W/L ±0.3, clamp 4–9.5) | Scorer rating > non-scorer in test |
| **MVP-6** | Result screen: score, named scorers, attendance, rating snippet | History click still works (A-4) |
| **MVP-7** | Preview before user's next match; editable XI | Confirm → sim uses snapshot |

### B4 — Tactics UX

| ID | Work | Acceptance |
|----|------|------------|
| **MVP-2** | Formation dropdown; 2–3 templates (`2-3-5-1888`, …) | Changes sim weights |
| **MVP-9** | `TacticsScreenTable` in `MainMenuManager` sidebar | CM1 menu parity |

### B5 — Player condition (post B-9)

Full spec: [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md).

| ID | Work | Acceptance |
|----|------|------------|
| **COND-1** | `PlayerCondition` on `Player`; fitness tick; sim strength modifier | `FitnessSimModifierIT` |
| **COND-2** | Sharpness (match form) tick | `PlayerFormTest` |
| **COND-3** | `InjuryGenerator`; squad UI; exclude from XI | `InjuredPlayerLineupIT` |
| **COND-4** | Fatigue from minutes; daily recovery | `FatigueRecoveryTest` |
| **COND-5** | Morale from results + ratings | `MoraleUpdateTest` |

---

## 6. Tests to add (Layer B gate)

| Test | Phase | Proves | Status |
|------|-------|--------|--------|
| `HistoricalLeagueRulesIT` | B0/A-5 | 2-1-0 rules + points | ✅ |
| `LeagueCompetitionRulesFormatterTest` | B0/A-5 | League UI rules text | ✅ |
| `MatchSimulatorV2Test` | B1 | XI strength affects goals | ✅ |
| `StartingElevenIT` | B1 | Picked players used in sim | ✅ |
| `ClubTacticsSetupSaveLoadIT` | B1 | New fields survive JSON roundtrip | ✅ |
| `AttendanceModelTest` | B2 | Table/form/derby affect attendance | Pending |
| `MatchRatingsTest` | B3 | Rating rules | Pending |
| `TenSeasonReportIT` (regression) | B1+ | No deadlock after sim v2 | ✅ (re-run after B1) |
| `FitnessSimModifierIT` | B5 | Condition affects sim | Planned |

---

## 7. Department ownership

| Phase | Primary | Support |
|-------|---------|---------|
| B1 DATA + sim | Match/Sim | Platform (Squad UI hooks) |
| B2 attendance | Match/Sim | Seasons (standings), Finances |
| B3 UI feedback | Platform/UX | Match/Sim (ratings data) |
| B4 tactics UX | Platform/UX | Match/Sim (registry) |
| QA | QA | All phases — manual CM1 loop at end |

---

## 8. Layer B exit criteria (PO)

Same as [DESIGN_LAYER_B_CM1_CORE_LOOP.md §7](DESIGN_LAYER_B_CM1_CORE_LOOP.md):

- [ ] Pick **11** + **formation** before league match  
- [ ] Stronger XI wins more in IT  
- [ ] Attendance correlates with table/opponent (not random)  
- [ ] **0–10 ratings** after every match for XI  
- [ ] Result screen CM1-like  
- [ ] Tactics in sidebar  
- [ ] `TenSeasonReportIT` plausible spread  
- [ ] Layer A manual QA still green  

---

## 9. After Layer B

| Next | Doc |
|------|-----|
| **WE-1** WorldEvolutionManager | [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) |
| **1892** Layer C | [PM_V1_MVP_AND_1892_MASTER_PLAN.md §6](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |
| **MVP-10** save versioning | When PO schedules — aligns with [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md) |
| **Embedded DB** | V2+ — not before Layer C unless save size blocks QA |

---

## 10. Immediate next actions

| # | Action | Owner |
|---|--------|-------|
| 1 | **A-5** — 2-1-0 + goal average UI verify | QA |
| 2 | **A-9** — Layer A PO sign-off | PO |
| 3 | **B1** — follow [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) §9 | Match/Sim + Platform |
| 4 | Tests per [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) with each MVP | All |

**Git:** all commits on **`analyze-ai-branch`** — [BRANCH_STRATEGY_ANALYZE_AI.md](BRANCH_STRATEGY_ANALYZE_AI.md)
