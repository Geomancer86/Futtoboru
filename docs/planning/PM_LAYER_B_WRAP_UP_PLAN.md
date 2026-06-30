# PM Plan — Layer B Wrap-Up

**Document type:** PM master plan (analysis + design index + execution)  
**Date:** 2026-06-30  
**Branch:** `analyze-ai-branch`  
**Direction:** [PO_PRODUCT_DIRECTION.md](PO_PRODUCT_DIRECTION.md) — no release; finish CM1 loop locally  
**Audience:** PO, PM, Match/Sim, Platform/UX, QA

> **Decision meeting:** Use §6 (three PO choices) after reading §2–§4. Engineering detail in linked design docs.

---

## 1. What “Layer B complete” means

Player can run the **CM1 weekly loop in the UI**:

```
Squad/Tactics → set XI + formation → match preview → Continue → result with scorers + ratings
```

**Engine half is largely done.** **Player-facing half is not.** Layer B is **~55% complete** by deliverable count (see §2).

**Out of Layer B complete:** transfers, training, injuries/fitness sim (B5), 1892, world news, public release.

---

## 2. Status at a glance

| Phase | Scope | Engine / tests | UI / flow | Overall |
|-------|--------|----------------|-----------|---------|
| **B1** | XI + sim v2 | ✅ Done | ⚠️ **Partial** — Best XI only; no manual pick | **~75%** |
| **B2** | Attendance + economy | ✅ Done | ✅ Result shows attendance | **100%** |
| **B3** | Ratings + preview/result | ❌ Not started | ❌ Stubs | **0%** |
| **B4** | Formation + Tactics menu | ✅ Registry exists | ❌ Placeholder screen; not in sidebar | **~15%** |

### Deliverable checklist

| ID | Deliverable | Status | Notes |
|----|-------------|--------|-------|
| DATA-B1 | `FormationRegistry`, `ClubTacticsSetup`, `MatchLineup` | ✅ | On `Club`; save roundtrip IT green |
| MVP-1 | Pick XI | ⚠️ | **Harness + Best XI** ✅; **manual 11 pick UI** ❌ |
| MVP-3 | `MatchSimulator` v2 | ✅ | XI-only strength; scorers from XI |
| MVP-8 | Balance | ✅ | `TenSeasonReportIT` regression |
| MVP-4 | `AttendanceCalculator` | ✅ | Table, opponent, form, derby |
| H-1 | Single attendance authority | ✅ | `AttendanceOverwriteIT` |
| ECON-B2 | Economy health scan | ✅ | FL solvency gates |
| **MVP-5** | Post-match 0–10 ratings | ❌ | Design: [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) |
| **MVP-6** | Result UI (scorers, ratings) | ⚠️ | Score + attendance ✅; **scorers/ratings** ❌ |
| **MVP-7** | Preview UI + confirm XI | ⚠️ | Title only; **no edit / no confirm flow** ❌ |
| **MVP-2** | Formation picker | ❌ | Default `2-3-5-1888` only in code |
| **MVP-9** | Tactics in sidebar | ❌ | `TacticsScreenTable` placeholder |
| MVP-10 | Save versioning | ⏸️ Deferred | JSON fields only for now |

### Test inventory

| Phase | Automated | Status |
|-------|-----------|--------|
| B1 | 6 tests | ✅ All green |
| B2 | 5 tests | ✅ All green |
| B3 | 3 tests planned | ❌ Not implemented |
| B4 | 1 unit + manual | ❌ Not implemented |

Full matrix: [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)

---

## 3. Gap analysis — what was missed

Prior docs assumed **B1 = done** because headless tests pass. For **Layer B complete**, these UI/flow gaps remain:

### 3.1 Match day flow (critical)

**Today:** Continue → `FuttoboruGameEngine.getMatchResult()` → sim runs **immediately** — preview screen is bypassed.

**CM1 target:** Match day → **Preview** (edit XI) → **Play / Continue** → sim → **Result** (scorers + ratings).

**Missing:**

| Piece | File(s) |
|-------|---------|
| Route Continue to preview when user club has playable match | `FuttoboruGameEngine`, `MainGameMenuTable` |
| “Kick off” on preview calls sim with confirmed `ClubTacticsSetup` | `MatchPreviewScreenTable`, engine |
| After sim, navigate to result screen | `MainMenuManager` |
| Preview snapshot = sim lineup (MVP-7) | `MatchLineupBuilder`, preview UI |

Design: [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md §2](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md)

### 3.2 Manual XI selection (MVP-1 UI)

**Today:** Squad has **Best XI** button only; no slot list, no pick-11 from table.

**CM1 requires:** Manager can **choose** 11, not only auto-fill.

**Missing:** Tactics or Squad UI with 11 slots tied to `ClubTacticsSetup.slotAssignments`.

Design: [DESIGN_TACTICS_UI_V1.md §2](DESIGN_TACTICS_UI_V1.md)

### 3.3 Ratings (MVP-5)

**Today:** `Match` stores scorers and lineup IDs; **no ratings map**.

**Missing:** `MatchRatingsCalculator`, persist on `Match`, invoke after `MatchSimulator.simulateMatch`.

Design: [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md §3](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md)

### 3.4 Tactics UX (B4)

**Today:** `TacticsScreenTable` = placeholder; manager sidebar has no Tactics button.

**Missing:** Formation dropdown (2–3 templates), slot rows, sidebar wire-up.

Design: [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md)

### 3.5 Explicitly not gaps for Layer B wrap-up

| Item | When |
|------|------|
| B5 player condition (fitness, injuries, morale) | After B-9 — [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) |
| Transfers, training, scouting | Post–Layer B |
| 2D match engine | Out of scope |
| MVP-10 formal save migrations | When save size forces it |
| Public release / manual QA sign-off as gate | Not PO direction |

---

## 4. Wrap-up execution plan

**Recommended order:** B3 engine first (ratings), then match-day flow (MVP-7), then UI surfaces (MVP-6, MVP-1 UI, B4).

```
Week 1–2   B3a — MatchRatingsCalculator + Match fields + tests
Week 2–3   B3b — Match-day flow (preview → kick off → result) + MatchFeedbackIT
Week 3–4   B3c — Result UI scorers + ratings (MVP-6)
Week 4–5   B4  — Tactics screen + sidebar + formation picker (MVP-2, MVP-9)
Week 5     B1 UI — Manual XI on Tactics screen + squad XI indicator (MVP-1 UI)
Week 5–6   Integration — manual CM1 loop + fix regressions
```

**Total estimate:** **4–6 eng-weeks** (one developer); less with parallel Match/Sim + UX.

### Sprint breakdown

#### Sprint B3a — Ratings engine

| Task | Owner | Done when |
|------|-------|-----------|
| Add `homePlayerRatings` / `awayPlayerRatings` on `Match` (Map serialized as lists) | Match/Sim | JSON roundtrip in `SaveLoadIT` or dedicated IT |
| Implement `MatchRatingsCalculator` per design formula | Match/Sim | `MatchRatingsTest` green |
| Call calculator at end of `MatchSimulator.simulateMatch` | Match/Sim | Ratings size = XI count in IT |

#### Sprint B3b — Match-day flow

| Task | Owner | Done when |
|------|-------|-----------|
| Detect playable match on Continue → show preview (not auto-sim) | Platform + Engine | Manual: preview appears on match day |
| Preview “Kick off” → sim → result screen | Platform | `MatchLineupPreviewIT` green |
| Harness: `getLastMatchRatings`, `confirmPreviewLineup` | QA/Platform | [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) |

#### Sprint B3c — Result feedback

| Task | Owner | Done when |
|------|-------|-----------|
| Result screen: named scorers (from `Match.homeScorerIds`) | Platform | Visible in UI + history (A-4) |
| Result screen: XI ratings table for user club | Platform | `MatchFeedbackIT` |
| Preview: show both teams, formation, XI list, edit for user club | Platform | MVP-7 manual pass |

#### Sprint B4 — Tactics UX

| Task | Owner | Done when |
|------|-------|-----------|
| Replace `TacticsScreenTable` placeholder | Platform | 11 slot rows + formation dropdown |
| Add Tactics button to manager sidebar | Platform | `MainMenuManager.setDynamicButtonsMenu` |
| Register 2–3 formations in `FormationRegistry` | Match/Sim | `FormationSimWeightTest` |
| Formation change affects sim weights | Match/Sim | Different template → different strength in test |

#### Sprint B1-UI — Manual XI

| Task | Owner | Done when |
|------|-------|-----------|
| Slot dropdown / pick player per formation slot | Platform | 11 assigned; persists reload |
| Squad list: mark players in XI | Platform | Visual “IN XI” column or highlight |
| Best XI still available | Platform | Regression on `StartingElevenIT` |

---

## 5. Layer B exit criteria (B-9)

Automated **and** playable locally — **not** a public release gate.

| # | Criterion | Test |
|---|-----------|------|
| 1 | Pick **11** manually + Best XI | Manual + `ClubTacticsSetupSaveLoadIT` |
| 2 | **Formation** from 2–3 templates | `FormationSimWeightTest` + manual |
| 3 | **Preview → kick off → result** flow | `MatchLineupPreviewIT` + manual |
| 4 | **Ratings** 0–10 for XI after match | `MatchRatingsTest`, `MatchFeedbackIT` |
| 5 | **Scorers** on result screen | Manual M4 |
| 6 | **Tactics** in sidebar | Manual M2 |
| 7 | **Stronger XI** wins more | `StartingElevenIT`, `MatchSimulatorV2Test` |
| 8 | **Attendance** not random-only | `AttendanceModelIT` |
| 9 | **Ten-season** balance OK | `TenSeasonReportIT` |
| 10 | **`mvn test`** green | PR suite |

Manual checklist: [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md)

---

## 6. Decisions for PO (after this plan)

Pick before engineering starts B3b UI (flow affects UX).

| # | Decision | Options | PM recommendation |
|---|----------|---------|-------------------|
| **D1** | **B5 (player condition)** in Layer B complete? | A) No — B5 after B-9 · B) Yes — extend Layer B | **A** — CM1 loop does not require injuries |
| **D2** | **Manual XI** required for B-9? | A) Yes — full CM1 · B) Best XI + Tactics slots enough for now | **A** — CM1 had pick 11 |
| **D3** | **Auto-sim other clubs** on Continue when no user match? | A) Yes (today) · B) Always preview user matches only | **A** — keep engine behaviour; preview only for user match day |

---

## 7. Risks

| Risk | Mitigation |
|------|------------|
| Preview flow breaks Continue / mandatory gates | Feature flag or branch in `continueGame()`; `MandatoryGateIT` regression |
| Ratings not in save JSON | Add fields early; extend `SaveLoadIT` |
| Tactics UI scope creep (drag-drop pitch) | List/slot UI only — [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) |
| Docs said B1 “done” while UI incomplete | This plan + updated gap analysis |

---

## 8. Document map

| Doc | Role |
|-----|------|
| **This plan** | PM wrap-up — status, gaps, sprints, decisions |
| [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md) | B3 ratings + match-day flow + result/preview UI |
| [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) | B4 + MVP-1 manual XI UI |
| [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) | File-level task detail |
| [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) | Tests + manual B-9 |
| [ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md](ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md) | CM/FM gap reference |

---

## 9. Changelog

| Date | Change |
|------|--------|
| 2026-06-30 | Initial wrap-up plan — B1/B2 engine done; B3/B4 + MVP-1 UI remaining |

---

*Update when B3 lands or PO locks §6 decisions.*
