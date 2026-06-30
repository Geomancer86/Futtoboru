# Branch Strategy — `analyze-ai-branch`

**Document type:** PM / dev workflow  
**Date:** 2026-06-25 (updated)  
**Policy:** **All work stays on `analyze-ai-branch`** — **no push** until PO requests

---

## 1. Current state

| Item | Value |
|------|-------|
| **Active branch** | `analyze-ai-branch` (**only** — Layer A, PU-10, Layer B) |
| **Tracks** | `origin/feature/competitions-finances-seasons` |
| **Ahead of remote** | **~79+ commits** (local only) |
| **`main`** | Stale — **not** integration target |
| **Child branches** | **None** — PO decision 2026-06-25: do not create `feature/layer-b` |

---

## 2. What landed on this branch (summary)

| Track | Themes |
|-------|--------|
| Build / docs | Windows scripts, `docs/planning/` hub |
| Layer A | Continue, load, match history, activateSaveGame |
| PU-10 | UI scale 200%, shell layout R3b/R3c |
| Layer B planning | CM1 loop design, roadmap, test plan |
| Tests / harness | League/cup/soak ITs, `GameTestHarness` |

This branch is the **single integration line** for v1 through Layer B.

---

## 3. Workflow (locked)

```
analyze-ai-branch
    │
    ├── Layer A gates (A-5, A-9)
    ├── B1 → B2 → B3 → B4 (Layer B MVPs + tests)
    ├── commits: analyze → design doc → code → unit + IT
    └── (future) Layer C on same branch until PO says push/merge
```

| Rule | Value |
|------|-------|
| **Branch** | Never fork for Layer B — commit sequentially on `analyze-ai-branch` |
| **Push** | PO-only; automation does not push |
| **Commits** | Reference MVP id (e.g. `MVP-3: MatchSimulator reads MatchLineup`) |
| **Tests** | Same commit series as feature — see [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) |

Optional local tag when Layer A passes: `git tag layer-a-qa-pass` (no push required).

---

## 4. Remote sync (when PO asks)

| Step | Action |
|------|--------|
| 1 | PO confirms push to `origin/feature/competitions-finances-seasons` or other |
| 2 | `git push -u origin analyze-ai-branch` |
| 3 | PR summary: PU-10 + Layer A + Layer B progress |
| 4 | Do **not** force-push `main` |

---

## 5. Gates

| Gate | Status |
|------|--------|
| A-5 League 2-1-0 UI | Open |
| A-9 PO Layer A sign-off | Open |
| B1–B4 Layer B + tests | Planned — [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) |
| B-9 PO Layer B sign-off | After B4 + manual M1–M8 |

---

## 6. Key docs on this branch

| Doc | Role |
|-----|------|
| [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) | **Master Layer B roadmap** |
| [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) | Unit + IT + manual QA |
| [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) | Phase detail |
| [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md) | Full index |

---

*PO policy: one branch, no push, test every MVP.*
