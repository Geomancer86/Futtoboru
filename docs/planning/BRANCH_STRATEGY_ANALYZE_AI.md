# Branch Strategy — `analyze-ai-branch`

**Document type:** PM / dev workflow  
**Date:** 2026-06-25  
**Policy:** PO controls git push — **no remote push from automation**

---

## 1. Current state

| Item | Value |
|------|-------|
| **Active branch** | `analyze-ai-branch` |
| **Tracks** | `origin/feature/competitions-finances-seasons` |
| **Ahead of remote** | **~78 commits** (local only) |
| **`main`** | Stale (`release/july_2023_progress_update` era) — **not** integration target |
| **Working tree** | Clean after Layer B planning commit |

---

## 2. What landed on this branch (summary)

| Track | Commits (themes) |
|-------|------------------|
| **Build / docs** | Windows scripts, planning docs → `docs/planning/` |
| **Layer A fixes** | Continue, load, match history, activateSaveGame |
| **Layer B design** | CM1 core loop design doc |
| **PU-10 HiDPI** | UI scale, R2 revert, R3 200% default, R3b/R3c shell layout |
| **PM / QA** | Eagle view, sprint progress, PU-10 completion |
| **Tests / harness** | ITs, soak profile (from earlier commits on branch) |

This branch is the **effective integration line** for all v1 work — not a short-lived spike.

---

## 3. Recommended workflow going forward

### Option A — **Recommended:** continue locally, branch for Layer B

```
analyze-ai-branch          ← Layer A complete (A-5, A-9), all PU-10 work
        │
        ├── (optional) tag locally: layer-a-qa-pass
        │
        └── feature/layer-b  ← cut from HEAD when B1 starts
                 │
                 └── B1 → B2 → B3 → B4 commits
```

**Why:** Keeps Layer A snapshot identifiable; Layer B is 6–10 weeks — separate feature branch name matches PO horizon map.

**Commands (when PO approves push):**

```bash
git checkout analyze-ai-branch
git checkout -b feature/layer-b
# … B1 work …
# later: push -u origin feature/layer-b
```

### Option B — stay on `analyze-ai-branch`

Acceptable if PO prefers **one long-lived branch** until first remote push. Rename is optional.

### Option C — merge to `main`

**Not recommended yet** — `main` is years behind; would need PO decision to replace `main` with this line or merge via PR into a new default branch.

---

## 4. Remote sync (when PO asks)

| Step | Action |
|------|--------|
| 1 | PO confirms target remote branch (`feature/competitions-finances-seasons` vs new `feature/layer-b`) |
| 2 | `git push -u origin analyze-ai-branch` or `feature/layer-b` |
| 3 | Open PR with summary linking [PU-10_UI_SCALE_COMPLETION_V1.md](PU-10_UI_SCALE_COMPLETION_V1.md) + Layer A checklist |
| 4 | Do **not** force-push `main` |

---

## 5. Layer gates before Layer B code

| Gate | Status |
|------|--------|
| A-5 League 2-1-0 UI | Open |
| A-9 PO Layer A sign-off | Open |
| PU-10 HiDPI | ✅ Done |
| In-game shell scroll | ✅ Done |

**Start B1 coding only after A-9** (A-5 can run in parallel with B1 data model if PO accepts risk — default is sequential).

---

## 6. Documentation on branch

All active PM/design docs live under **`docs/planning/`**. Index: [DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md).

New for Layer B kickoff:

- [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md)
- [ANALYSIS_SAVE_STORAGE_FUTURE_V1.md](ANALYSIS_SAVE_STORAGE_FUTURE_V1.md)
- This file

---

*Update commit count in §1 when pushing or merging.*
