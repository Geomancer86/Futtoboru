# Layer B — Recommended Roadmap (CM1 Management Loop)

**Document type:** PM master roadmap — analyze → design → develop → test  
**Date:** 2026-06-25  
**Branch:** `analyze-ai-branch` (**only** — no push, no child branches)  
**Status:** **Wrap-up** — B1/B2 ✅ · B3/B4 pending — [PM_LAYER_B_WRAP_UP_PLAN.md](PM_LAYER_B_WRAP_UP_PLAN.md)  
**Gap analysis:** [ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md](ANALYSIS_LAYER_B_MANAGEMENT_SIM_GAP_V1.md) · **Player condition (B5):** [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md)  
**Goal:** **Pick XI → formation → Continue → match uses your 11 → sensible crowds → 0–10 ratings → readable result/preview**

**Companion docs:** [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) · [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) · [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) · [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md)

---

## 1. Product goal → engineering map

| # | CM1 loop step | MVP | Phase | Automated tests | Manual QA |
|---|---------------|-----|-------|-----------------|-----------|
| 1 | **Pick XI (11)** | MVP-1, DATA-B1 | B1 | `FormationRegistryTest`, `ClubTacticsSetupSaveLoadIT`, `StartingElevenIT` | Squad/Tactics: 11 selected, Best XI works |
| 2 | **Formation** | MVP-2, MVP-9 | B4 (+ default in B1) | `FormationRegistryTest`, `FormationSimWeightTest` | Sidebar Tactics; dropdown changes lineup slots |
| 3 | **Continue** | — (Layer A) | ✅ | `MandatoryGateIT`, continue harness | One day per click (A-2) |
| 4 | **Match uses your 11** | MVP-3, MVP-8 | B1 | `MatchSimulatorV2Test`, `TenSeasonReportIT` regression | Weak XI loses more often in play |
| 5 | **Sensible crowds** | MVP-4, H-1 | B2 | `AttendanceCalculatorTest`, `AttendanceModelIT` | Top club > bottom attendance at same ground |
| 6 | **0–10 ratings** | MVP-5 | B3 | `MatchRatingsTest` | Scorers rate higher on result screen |
| 7 | **Result / preview UI** | MVP-6, MVP-7 | B3 | `MatchFeedbackIT` | Preview edit XI → result shows score, scorers, ratings |

**Deferred (B5+):** MVP-10 save versioning · embedded DB · transfers · training · **player fitness/injuries/fatigue/morale** · per-screen UI polish beyond touched screens.

---

## 2. Delivery method (same as Layer A / season engine)

Every MVP follows **four steps** before merge on `analyze-ai-branch`:

```
Analyze  →  Design  →  Develop  →  Test (unit + headless IT/E2E)
                ↑                           │
                └─────── fix loop ──────────┘
```

| Step | Output |
|------|--------|
| **Analyze** | Gap note in this doc or short `ANALYSIS_*` if new domain |
| **Design** | Update [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) or attendance section in master plan |
| **Develop** | Code + i18n keys if UI-facing |
| **Test** | JUnit unit test(s) + `GameTestHarness` IT where loop touched; `mvn test` green |
| **Manual QA** | Checklist row in [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) |

**Release gate:** `mvn test` + `mvn test -Psoak` (no regression) + **B-9** PO sign-off.

---

## 3. Roadmap timeline

```
┌─────────────────────────────────────────────────────────────────────────┐
│ B0  Layer A close     A-5 ✅ automated  →  A-9 PO sign-off              │
├─────────────────────────────────────────────────────────────────────────┤
│ B1  Sim core (~4–5 wk)  ✅ DONE                                          │
│     DATA-B1 → MVP-1 → MVP-3 → MVP-8                                     │
│     Exit: StartingElevenIT + MatchSimulatorV2Test green                  │
├─────────────────────────────────────────────────────────────────────────┤
│ B2  Crowds (~2–3 wk)  ✅ DONE                                          │
│     MVP-4 + H-1 + economy early-warning (ECON-B2)                        │
│     Exit: AttendanceModelIT + LongRunEconomySoakIT green                 │
├─────────────────────────────────────────────────────────────────────────┤
│ B3  Feedback (~2–3 wk)                                                  │
│     MVP-5 → MVP-6 → MVP-7                                                 │
│     Exit: MatchFeedbackIT + MatchRatingsTest + manual CM1 walkthrough    │
├─────────────────────────────────────────────────────────────────────────┤
│ B4  Tactics UX (~1–2 wk)                                                │
│     MVP-2 → MVP-9                                                         │
│     Exit: Formation picker + sidebar; B-9 PO Layer B sign-off            │
├─────────────────────────────────────────────────────────────────────────┤
│ B5  Player condition (~3–4 wk) — post B-9                               │
│     COND-1 → COND-5 (fitness, sharpness, injuries, fatigue, morale)      │
│     Exit: FitnessSimModifierIT + InjuredPlayerLineupIT green             │
└─────────────────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    Layer C (WE-1 / 1892) — separate roadmap
```

**Total:** 6–10 eng-weeks after B0.

---

## 4. Phase detail — analyze · design · develop · test

### B0 — Layer A gates (before B1 code)

| ID | Analyze | Design | Develop | Test |
|----|---------|--------|---------|------|
| **A-5** | League UI vs `competition_rules.txt` 2-1-0 | — | `LeagueCompetitionRulesFormatter` | ✅ `HistoricalLeagueRulesIT`, `LeagueCompetitionRulesFormatterTest`, `LeagueStandingsManagerTest` |
| **A-9** | Checklist [RELEASE_V1_DEFINITION.md §9](RELEASE_V1_DEFINITION.md) | — | — | PO sign-off |

---

### B1 — Sim core ✅ (engine); UI partial

| ID | Analyze | Design | Develop | Unit tests | IT / E2E | Status |
|----|---------|--------|---------|------------|----------|--------|
| **DATA-B1** | §4.3 Layer B design | [DESIGN_TACTICS §3](DESIGN_TACTICS_AND_FORMATIONS.md) | Registry + setup on `Club` | `FormationRegistryTest` | `ClubTacticsSetupSaveLoadIT` | ✅ |
| **MVP-1** | Squad XI | §3 + [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md) | Best XI ✅; **manual slots** ❌ | `BestElevenSelectorTest` | `StartingElevenIT` | ⚠️ |
| **MVP-3** | Sim v1 gap | §3.5 | `MatchSimulator` v2 | `MatchSimulatorV2Test` | `StartingElevenIT` | ✅ |
| **MVP-8** | Champion spread | Tune weights | Constants | — | `TenSeasonReportIT` | ✅ |

**B1 exit (engine):** ✅ **B1 UI exit:** manual XI on Tactics — pending B4.

---

### B2 — Sensible crowds

| ID | Analyze | Design | Develop | Unit tests | IT / E2E |
|----|---------|--------|---------|------------|----------|
| **MVP-4** | [ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md](ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md); random 60–90% | Master plan §5.3 formula | `AttendanceCalculator`; `mods/derbies.txt` loader | `AttendanceCalculatorTest` | `AttendanceModelIT` ✅ |
| **H-1** | Sim overwrites ticket attendance | Single authority in design | Remove overwrite in `MatchSimulator`; scheduler uses calculator | — | `AttendanceOverwriteIT` ✅ |
| **ECON-B2** | No bankruptcy sim yet; need early warning | Report-only for non-FL clubs | `EconomyHealthMonitor`; harness asserts on FL roster | `EconomyHealthMonitorTest` | `LongRunEconomySoakIT`, `MultiSeasonSoakIT`, `TenSeasonReportIT` |

**B2 exit:** Top-table home attendance > bottom-table at same stadium (IT).

---

### B3 — Ratings + result / preview

Design: [DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md](DESIGN_MATCH_RATINGS_AND_FEEDBACK_UI_V1.md)

| ID | Analyze | Design | Develop | Unit tests | IT / E2E | Status |
|----|---------|--------|---------|------------|----------|--------|
| **MVP-5** | No ratings | §3 calculator | `MatchRatingsCalculator` | `MatchRatingsTest` | `MatchFeedbackIT` | ❌ |
| **MVP-6** | Result partial | §2.4 | Scorers + ratings on result | — | Manual + A-4 | ❌ |
| **MVP-7** | Preview stub; Continue bypasses | §2 flow | Preview + kick off | — | `MatchLineupPreviewIT` | ❌ |

**B3 exit:** Full loop in UI: preview → continue → result with ratings (manual checklist §5).

---

### B4 — Formation UX

Design: [DESIGN_TACTICS_UI_V1.md](DESIGN_TACTICS_UI_V1.md)

| ID | Analyze | Design | Develop | Unit tests | IT / E2E | Status |
|----|---------|--------|---------|------------|----------|--------|
| **MVP-2** | One template | §2.3 | 2–3 templates + dropdown | `FormationSimWeightTest` | Sim weight change | ❌ |
| **MVP-9** | Not in sidebar | §2.1 | `TacticsScreenTable` + button | — | Manual | ❌ |

**B4 exit:** **B-9** PO Layer B sign-off ([§6](#6-exit-criteria-b-9)).

---

### B5 — Player condition (in Layer B)

Design: [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md)

| ID | Analyze | Design | Develop | Unit tests | IT / E2E | Status |
|----|---------|--------|---------|------------|----------|--------|
| **COND-1–5** | CM1 fitness/morale | Condition doc | `PlayerCondition`, tick, sim hook | `PlayerFitnessTest`, etc. | `FitnessSimModifierIT`, `InjuredPlayerLineupIT` | ❌ |

**B5 exit:** Condition affects sim; L3 `LayerBMultiSeasonSoakIT` green.

---

## 5. Harness extensions (`GameTestHarness`)

Add methods as B1–B3 land (spec in [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)):

| Method | Phase | Purpose |
|--------|-------|---------|
| `setClubLineup(clubId, playerIds…)` | B1 | Force XI for IT |
| `getLastSimulatedLineup(clubId)` | B1 | Assert sim used picked players |
| `simulateNextLeagueMatchForClub(clubId)` | B1 | Single-match E2E |
| `getLastMatchAttendance()` | B2 | Attendance assertions |
| `getLastMatchRatings(clubId)` | B3 | Ratings assertions |
| `runCm1LoopSmoke()` | B3 | Preview → sim → result one match |

---

## 6. Exit criteria (B-9)

PO **Layer B complete** when all checked:

- [ ] **Pick XI** — 11 persisted; Best XI; AI auto-fill  
- [ ] **Formation** — 2–3 templates; sidebar Tactics  
- [ ] **Continue** — Layer A still green  
- [ ] **Match uses 11** — `StartingElevenIT` + `MatchSimulatorV2Test`  
- [ ] **Crowds** — `AttendanceModelIT`; not random-only  
- [ ] **Ratings** — `MatchRatingsTest`; visible on result  
- [ ] **Result / preview** — `MatchFeedbackIT`; manual CM1 walkthrough  
- [ ] **Balance** — `TenSeasonReportIT` plausible spread  
- [ ] **`mvn test`** + soak profile — no regression  
- [ ] **PO sign-off B-9**  

---

## 7. Git & docs policy

| Rule | Value |
|------|-------|
| **Branch** | **`analyze-ai-branch` only** — all Layer B commits here |
| **Push** | **None** until PO requests |
| **Commits** | One logical chunk per MVP or test bundle; message cites MVP id |
| **Docs** | Update this roadmap + test plan when phase completes |

See [BRANCH_STRATEGY_ANALYZE_AI.md](BRANCH_STRATEGY_ANALYZE_AI.md).

---

## 8. After Layer B

| Track | Doc |
|-------|-----|
| Layer C / 1892 | [PM_V1_MVP_AND_1892_MASTER_PLAN.md §6](PM_V1_MVP_AND_1892_MASTER_PLAN.md) |
| Save versioning | [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md) |
| World / news | [DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md](DESIGN_HISTORICAL_ACCURACY_NEWS_AND_KNOWLEDGE.md) |

---

## 9. Immediate next actions

| # | Action | Owner |
|---|--------|-------|
| 1 | PO: [PM_LAYER_B_WRAP_UP_PLAN.md §6](PM_LAYER_B_WRAP_UP_PLAN.md) | PO |
| 2 | B3a ratings engine | Match/Sim |
| 3 | B3b match-day flow | Platform + Engine |
| 4 | B4 Tactics UI | Platform |

---

*Canonical Layer B roadmap — analyze, design, develop, test for the CM1 management loop.*
