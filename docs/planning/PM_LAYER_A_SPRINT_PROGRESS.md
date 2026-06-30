# PM Report — Layer A Sprint Progress

**Document type:** PM sprint report  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Sequence:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) · [PM_REPORT_HISTORICAL_MODE_PO_LOCK.md](PM_REPORT_HISTORICAL_MODE_PO_LOCK.md)

---

## 1. Executive summary

Work started on the **recommended order**: **Layer A (UI blockers)** first, with **foundational FLEX-1 / NEWS-8** stubs that do not block Layer B.

| Track | Status this sprint |
|-------|-------------------|
| **Layer A** | **A-2, A-3, A-4** implemented in code — manual QA pending |
| **Layer B** | Not started (next after Layer A sign-off) |
| **FLEX-1** | `HistoricalMode` on `SaveGame` — **done** |
| **NEWS-8** | `NewsLatencyResolver` + mod table + `scheduleWorldMessage` — **stub done** |
| **Tests** | **62/63** green PR suite; +2 new tests (`NewsLatencyResolverTest`, `SaveLoadIT` historical mode) |

---

## 2. Layer A deliverables

| ID | Item | Result |
|----|------|--------|
| **A-2** | C-1 Continue double-click | **Fixed** — single `touchDown` action, 500 ms cooldown, stale-operation guard |
| **A-3** | C-3 / H-9 load + job init | **Fixed** — `Futtoboru.activateSaveGame()`; `LoadGameDialog` + in-game **Load** menu option |
| **A-4** | H-3 match history → result | **Fixed** — `MatchHistoryScreenTable` → `MainMenuManager.showMatchResult()` |
| **A-1** | Manual QA — UI scale / HiDPI | ✅ Done — [PU-10_UI_SCALE_COMPLETION_V1.md](PU-10_UI_SCALE_COMPLETION_V1.md) |
| **A-1b** | In-game shell scroll (top bar + left nav fixed) | ✅ R3b/R3c |
| **A-5** | 2-1-0 UI verify | **Open** — next Layer A item |
| **A-9** | PO Layer A sign-off | **Open** — after A-5 |

### Code touchpoints

| File | Change |
|------|--------|
| `MainGameMenuTable.java` | C-1 continue guard; top-menu load (case 2) |
| `Futtoboru.java` | `activateSaveGame()` |
| `LoadGameDialog.java` | Uses `activateSaveGame` |
| `MatchHistoryScreenTable.java` | Opens result for clicked match |
| `MatchResultScreenTable.java` | `setDisplayMatch` / history support |
| `MainMenuManager.java` | `showMatchResult`, `clearLiveMatchResultSelection` |

---

## 3. Foundation (parallel, per PO lock)

| ID | Deliverable | Status |
|----|-------------|--------|
| **FLEX-1** | `HistoricalMode` enum + `SaveGame.historicalMode` default `SANDBOX_FIRST` | ✅ |
| **NEWS-8** | `NewsLatencyResolver`, `mods/news_latency.txt`, `MessageManager.scheduleWorldMessage` | ✅ stub |
| **WE-1** | `WorldEvolutionManager` | 🔲 Next after Layer B |
| **FLEX-2–4** | Fork scripts + agency UI | 🔲 After WE-1 |

---

## 4. Test evidence

```
NewsLatencyResolverTest     — same-region 0 days; SA→BI ≥14 days
SaveLoadIT                  — roundTripPreservesHistoricalMode
Full suite                  — 62 pass, 1 pre-existing PreferenceManagerTests asset error (unrelated)
```

---

## 5. Next steps (strict order)

1. **A-5** — Verify **2-1-0 + goal average** in league UI ([RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md))  
2. **A-9** — PO **Layer A sign-off** (A-2, A-3, A-4, A-1, A-1b done)  
3. **Layer B** — MVP-10 save schema → MVP-1 XI → MVP-3 sim v2 → MVP-4 attendance ([DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md))  
4. **WE-1** + NEWS-2 hook + FLEX-2 fork scripts  
5. **1892 England** — pending PO membership decision  
6. **Optional:** re-enable Match Engine Debug when NPE fixed (`MatchEngineVersion.DEBUG_SCREEN_UI_ENABLED`)  

---

## 6. Git

Local commit on `analyze-ai-branch` — **no remote push** (PO policy).

---

*Next PM update: after Layer A manual QA or Layer B MVP-1 start.*
