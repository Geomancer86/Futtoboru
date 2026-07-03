# PM Report — Yearly Release Scope & Injury Next Steps

**Document type:** PM status / planning  
**Date:** 2026-07-03  
**Branch:** `analyze-ai-branch` (~104 commits ahead of tracking)  
**Audience:** PO, PM, Sponsor, Match/Sim, World/Data, QA  

**Triggers:** PO clarification — research ledger ≠ release scope; **yearly named releases**; first release **1888 only** (no 1906 insurance in dev now).

**Authoritative wrap-up:** [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) · [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md)

---

## 1. Executive summary

| Topic | Decision / status |
|-------|-------------------|
| **Historical research** | ✅ Complete for injury medical eras ME-0→ME-7 ([research](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md)) |
| **CM93 injury engine (B5.2)** | ✅ Shipped — ~95% CM93 management parity |
| **1888 historical injury skin** | ❌ Not shipped — CM93 menus/costs anachronistic at game start |
| **Release naming** | **Locked:** `Futtoboru {YYYY}` / tag `{YYYY}.0.0` / internal `R-YYYY` |
| **First release** | **R-1888** — start mod 1888–89; **ME-1 injury only**; no 1906+ systems in code |
| **1906 insurance** | 📚 Documented · **not built** until **R-1906** release tranche |

**One-line PO message:** We have the **full history book**; we ship **one chapter per release year**.

---

## 2. Analysis — why this changes the injury plan

### 2.1 What we had (2026-06-25)

[M-B5.3-HIST design](DESIGN_INJURY_HISTORICAL_ERA_GATING.md) proposed a **calendar-driven era registry** unlocking ME-2 at 1906, ME-4 at 1951, etc., within a single long save.

### 2.2 What PO clarified (2026-07-03)

| Assumption | Revised |
|------------|---------|
| Build all era gates; calendar unlocks features | **Implement only eras for the current R-YYYY release** |
| HIST-c ships ME-2…ME-5 in one sprint | **Deferred** — each ships with its release year |
| Procedural save to 1920 gets 1906 insurance | **Not in R-1888 binary** — ceiling at ME-1 until R-1906 |

### 2.3 What stays valid

- **Dual layer:** CM93 mechanics engine + historical presentation overlay.  
- **ME-1 profile:** Trainer labels, shilling-scale rehab, hide insurance/Lilleshall/surgeon.  
- **Research ledger:** Unchanged — still cite 1906 Act, 1951 NSC, etc.  
- **B5.2 regression:** Pin tests to ME-6 / CM93 profile in dev branch — not player-facing in R-1888.

### 2.4 Risk retired

| Risk | Mitigation |
|------|------------|
| Engineering builds 1906 insurance while PM still on 1888 QA | **Release ceiling** in manifest + PM checklist |
| Marketing overclaims “historical injuries” | Split **CM93 engine** vs **R-1888 ME-1 skin** |
| Doc drift (v1.0 vs 1892 vs 1888) | **R-YYYY** naming in all new PM slices |

---

## 3. R-1888 scope snapshot (injury pillar)

| Item | R-1888 target | Today (B5.2) | Gap |
|------|---------------|--------------|-----|
| Post-match injury roll | ✅ | ✅ | — |
| Trainer label + estimate | ✅ | ❌ “Physio” | HIST-b strings |
| Rehabilitate (ME-1 costs) | ✅ ~£0.25–0.75 | ❌ £25–75 | HIST-b costs |
| Hide Insure / Lilleshall / Surgeon | ✅ | ❌ always visible | HIST-a UI gate |
| ME-1 recovery multiplier | ✅ ×1.20 | ❌ CM93 baseline | HIST-b math |
| Insurance / Lilleshall code | **Not in R-1888** | ✅ shipped | **Remove from player UI**; keep engine behind flag or dev-only |
| CM93 parity claim (player-facing) | ❌ not for R-1888 | — | Honest: “Victorian injury care” |

**Tests:** 131 green today; add ME-1 profile tests; keep CM93 suite under `medicalEra=ME-6` or `@Tag("cm93")`.

---

## 4. Release roadmap (injury & related — yearly tranche)

| Release | Primary start | Injury / medical ship | Other notable (from ledger) |
|---------|---------------|----------------------|----------------------------|
| **R-1888** *(current)* | 1888–89 (id 18) | **ME-1 only** — M-B5.3-HIST a+b | Layer B loop, 2-1-0 rules |
| **R-1889** | 1889–90 or procedural-only | ME-1 maintenance | Nov 1888 points rule script? |
| **R-1892** | 1892 Div 1+2 | ME-1 | World evolution, Div 2 |
| **R-1906** | 1906–07 milestone | **ME-2** insurance UI + economy | Compensation framing |
| **R-1951** | Milestone | **ME-4** national rehab | NSC |
| **R-1993** | Milestone | **ME-6** CM93 player-facing parity | Optional CM93 marketing line |

Dates are **product tranches**, not “must ship every calendar year on clock.”

---

## 5. Next steps (ordered)

### 5.1 PO (this week)

| # | Action | Owner |
|---|--------|-------|
| PO-1 | Acknowledge **R-1888** naming + ME-1 ceiling | PO |
| PO-2 | Confirm **hard ceiling** vs calendar-unlock-within-one-binary (recommend **hard ceiling**) | PO |
| PO-3 | Approve player-facing line: **“Victorian injury care”** not “CM93 injuries” for R-1888 | PO |

### 5.2 PM

| # | Action | Owner |
|---|--------|-------|
| PM-1 | Rename active milestone tracking to **R-1888** in sprint docs | PM |
| PM-2 | Add **release_id** column to [FEATURE_TRACEABILITY_MATRIX.md](FEATURE_TRACEABILITY_MATRIX.md) | PM |
| PM-3 | Schedule **M-B5.3-HIST a+b (ME-1 only)** before R-1888 manual sign-off | PM |

### 5.3 Match/Sim engineering (~1 sprint)

| # | Deliverable | Exit criteria |
|---|-------------|---------------|
| ENG-1 | `ReleaseScopeRegistry` or `max_implemented_medical_era=ME-1` | 1888 save never shows insurance/Lilleshall |
| ENG-2 | ME-1 cost table + recovery multiplier | Rehab ≤ 1 week avg wage |
| ENG-3 | ME-1 string table (Trainer) | Squad + player detail copy |
| ENG-4 | Feature flag / hide CM93 economy UI in R-1888 profile | Manual ch.6–7 accurate |
| ENG-5 | Tests: `MedicalEraR1888Test` + existing CM93 tests tagged | Full suite green |

**Explicitly not in this sprint:** ME-2 insurance, ME-4 Lilleshall, HIST-c full timeline.

### 5.4 World/Data / manual

| # | Action |
|---|--------|
| WD-1 | Annotate `mods/injuries/*.txt` — “CM93 reference; R-1888 uses `*_me1.txt`” |
| MAN-1 | Manual §6–7: R-1888 ME-1 only; footnote “1906+ in future releases” |

### 5.5 QA

| # | Action |
|---|--------|
| QA-1 | B-9 injury checklist for **Trainer / no insurance** |
| QA-2 | Regression: no CM93 £25 rehab in 1888 profile |

---

## 6. Traffic lights (R-1888 injury pillar)

| Area | Status | Note |
|------|--------|------|
| Research ledger | 🟢 | ME-0→ME-7 documented |
| Release naming design | 🟢 | This report + DESIGN_YEARLY_RELEASE_SCOPE |
| CM93 engine | 🟢 | B5.2 complete |
| ME-1 player skin | 🔴 | Blocking R-1888 historical honesty |
| 1906+ implementation | ⚪ | Out of scope — correct |
| Manual accuracy | 🟡 | ch.7 describes insurance — needs R-1888 slice |

---

## 7. Document chain (updated)

```
RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md     (full ledger — all eras)
DESIGN_YEARLY_RELEASE_SCOPE.md               (naming + ship-by-release)
DESIGN_INJURY_HISTORICAL_ERA_GATING.md         (engine overlay — §12 release slice)
    → PM_REPORT_YEARLY_RELEASE_AND_INJURY_SCOPE.md (this doc)
    → IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md §11
    → FEATURE_TRACEABILITY_MATRIX.md
    → manual 06 + 07 (R-1888 slice)
```

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial report — yearly release scope, R-1888 next steps, injury plan revision |

---

*Next PM update: after M-B5.3-HIST ME-1 ships or PO locks R-1892 scope.*
