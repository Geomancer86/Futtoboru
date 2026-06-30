# PM Report — Layer A Sprint Progress

**Document type:** PM sprint report  
**Date:** 2026-06-25 (updated)  
**Branch:** `analyze-ai-branch`  
**Sequence:** [PM_V1_MVP_AND_1892_MASTER_PLAN.md](PM_V1_MVP_AND_1892_MASTER_PLAN.md) · [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md)

---

## 1. Executive summary

**Layer A is nearly complete.** UI scale (PU-10), in-game shell layout (R3b/R3c), and code fixes A-2/A-3/A-4 are **done and QA-tested on Galaxy Book**. **Two Layer A gates remain:** league rules UI verify (A-5) and PO sign-off (A-9). **Layer B has not started** — that is the next major product track after sign-off.

| Track | Status |
|-------|--------|
| **Layer A** | **~95%** — A-5 + A-9 open; A-6/A-7/A-8 optional P1 |
| **Layer B (V1 MVP feel)** | **Not started** — XI, tactics, sim v2, attendance, ratings |
| **Layer C (1892 / WE-1)** | **Design only** — blocked on Layer B + PO decisions |
| **PU-10 HiDPI** | ✅ Complete — [PU-10_UI_SCALE_COMPLETION_V1.md](PU-10_UI_SCALE_COMPLETION_V1.md) |
| **FLEX-1 / NEWS-8** | ✅ Stubs on save / latency resolver |
| **Per-screen UI polish** | **Deferred** — fix screens individually when touched |

---

## 2. Layer A deliverables

| ID | Item | Result |
|----|------|--------|
| **A-2** | C-1 Continue double-click | ✅ Fixed + QA |
| **A-3** | C-3 / H-9 load + job init | ✅ Fixed + QA |
| **A-4** | H-3 match history → result | ✅ Fixed + QA |
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

*Next PM update: after A-5 league UI verify or Layer B kickoff.*
