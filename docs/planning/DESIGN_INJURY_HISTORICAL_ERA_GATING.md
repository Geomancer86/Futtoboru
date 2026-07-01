# Design — Injury Historical Era Gating (M-B5.3-HIST)

**Document type:** Architecture / product design  
**Date:** 2026-06-25  
**Status:** **Proposed** — analysis complete; **not implemented**  
**Audience:** Match/Sim, World/Data, PO, QA  

**Research:** [RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md)  
**CM93 baseline (shipped):** [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10 · [DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md](DESIGN_PLAYER_CONDITION_B5_2_CM93_PARITY.md)  
**Parity audit:** [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §9  

---

## 1. Problem statement

B5.2 delivers **honest CM93 injury parity** (~95% management loop). The **1888–89 start mod** requires **time-accurate injury presentation and economy**:

| Layer | Status today | Required |
|-------|--------------|----------|
| CM93 mechanics (roll, severity, weeks, treatments) | ✅ Shipped | Keep as engine |
| Period-accurate **availability** of actions | ❌ All menus always on | Era gates |
| Period-accurate **labels** (Trainer vs Physio) | ❌ CM93 strings | Era string table |
| Period-accurate **costs** | ❌ CM93 £ in “1888” files | Era cost tables |
| Period-accurate **recovery harshness** | ❌ CM93 baseline | Era multipliers |

**Design goal:** One injury engine; **medical era overlay** driven by in-game calendar so a 60-season save naturally unlocks ME-2 → ME-6 features without a second codebase.

---

## 2. Design principles

1. **Eras, not years** — eight bands ME-0…ME-7 ([research §4](../research/RESEARCH_FOOTBALL_INJURY_MEDICAL_ERAS.md#4-medical-era-timeline-me-0--me-7)).
2. **Mechanics vs presentation** — `InjuryTreatmentService` logic unchanged; gates + labels + costs resolved at runtime.
3. **Mod-first** — era tables in `mods/injuries/`; no hard-coded 1906 in Java except default fallback.
4. **Fail visible** — disabled actions hidden or greyed with era tooltip (“Injury insurance becomes common after 1906”).
5. **Do not break CM93 claim** — when `medicalEraId == ME-6`, behaviour matches B5.2 regression tests (or explicit ME-6 test profile).

---

## 3. Architecture

```
                    ┌─────────────────────┐
                    │  gameDate (save)    │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │ MedicalEraRegistry  │  ← mods/injuries/medical_eras.txt
                    │ resolve(date)→ME-n  │
                    └──────────┬──────────┘
           ┌───────────────────┼───────────────────┐
           │                   │                   │
  ┌────────▼────────┐ ┌────────▼────────┐ ┌────────▼────────┐
  │ EraFeatureGate  │ │ EraCostTable    │ │ EraStrings      │
  │ insure, lillesh │ │ rehab, specialist│ │ Physio→Trainer │
  └────────┬────────┘ └────────┬────────┘ └────────┬────────┘
           │                   │                   │
           └───────────────────┼───────────────────┘
                               │
                    ┌──────────▼──────────┐
                    │ InjuryTreatment     │
                    │ PhysioService       │
                    │ ExpenseCalculator   │
                    │ PlayerDetail UI     │
                    └─────────────────────┘
```

### 3.1 New components (proposed)

| Component | Responsibility |
|-----------|----------------|
| `MedicalEraRegistry` | Load era definitions; `resolve(LocalDate)` → `MedicalEra` |
| `MedicalEra` | id, startYear, endYear, feature flags, cost file key, string key, recovery multipliers |
| `MedicalEraFeatureGate` | `canInsure()`, `canRehabilitate()`, `canLilleshall()`, `canSurgeonAbroad()` |
| `MedicalEraStrings` | UI + inbox templates per era |
| `InjuryEconomyRegistry` (extend) | Select cost table by era key |

### 3.2 Integration points (existing)

| File | Change |
|------|--------|
| `InjuryTreatmentService` | Consult gate before each action; apply era recovery multiplier on week math |
| `PhysioService` | Apply `baseWeeksMultiplier` from era; optional trainer ability cap |
| `ExpenseCalculator` | Insurance premium only if gate allows |
| `InjuryEconomyRegistry` | Load `rehab_costs_me1.txt`, etc. |
| `PlayerDetailScreenTable` | Hide/disable buttons; era labels |
| `PlayersListTable` | “Return estimate” column header from era strings |
| `ClubStaffScreen` | “Trainer” vs “Physio” row title |

**Out of scope for M-B5.3-HIST:** in-match injury event, 10-men-no-subs match engine (separate **M-B5.4-MATCH** note).

---

## 4. Mod format — `medical_eras.txt` (sketch)

```
# era_id|start_year|end_year|label|rehab_costs|treatment_costs|strings|base_weeks_mult|rehab_minor_delta|rehab_mod_delta
ME-1|1885|1905|Early League|rehab_costs_me1.txt|treatment_costs_me1.txt|strings_me1.txt|1.20|0|1
ME-2|1906|1914|Compensation era|rehab_costs_me2.txt|treatment_costs_me2.txt|strings_me2.txt|1.10|1|2
...
ME-6|1993|1999|CM93 layer|rehab_costs.txt|treatment_costs.txt|strings_cm93.txt|1.00|1|2
```

**Empty treatment file** or `# DISABLED` → gate off for that action.

### 4.1 ME-1 cost targets (1888 money)

Align treatment fees to **fraction of weekly wage**, not CM93 absolutes:

| Action | Proposed ME-1 cost | Notes |
|--------|-------------------|-------|
| Rehabilitate MINOR | **£0.25** (5 shillings) | ~½ avg weekly wage |
| Rehabilitate MODERATE | **£0.75** | Trainer + doctor call |
| Lilleshall | — | gated off |
| Surgeon abroad | — | gated off |
| Insurance premium | — | gated off |

Move current `rehab_costs.txt` / `treatment_costs.txt` values to **ME-6** profile only (rename or duplicate).

### 4.2 ME-1 string examples (`strings_me1.txt`)

| Key | ME-1 text |
|-----|-----------|
| `staff_role` | Trainer |
| `estimate_column` | Trainer’s estimate |
| `action_rehabilitate` | Send to trainer |
| `action_insure` | *(hidden)* |
| `action_lilleshall` | *(hidden)* |
| `inbox_injury_lead` | `{player} is laid up — trainer says ~{weeks} weeks |

---

## 5. Feature gate matrix (authoritative for implementation)

| Feature | ME-1 | ME-2 | ME-3 | ME-4 | ME-5 | ME-6 | ME-7 |
|---------|------|------|------|------|------|------|------|
| Injury roll + severity | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Trainer/physio estimate | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Rehabilitate | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Insure / Uninsure | ❌ | ⚠️ PO: optional narrative “club covers wages” without premium UI | ⚠️ | ✅ | ✅ | ✅ | ✅ |
| Wages skipped when “insured” | ❌ | ⚠️ | ⚠️ | ✅ | ✅ | ✅ | ✅ |
| Lilleshall | ❌ | ❌ | ❌ | ⚠️ **1951** PO | ✅ **1986 strict** | ✅ | ✅ |
| Surgeon abroad | ❌ | ❌ | ❌ | ⚠️ | ✅ | ✅ | ✅ |
| Staff label Physio | ❌ use Trainer | ⚠️ | ⚠️ | ✅ | ✅ | ✅ | ✅ |

**PO decision logged (recommended defaults):**

- **Insurance UI:** off until **ME-4 (1945)** or **ME-6 (1993)** if we want strict CM93 parity only in ME-6; **compromise: ME-2** enables “club compensation” flavour without weekly premium until ME-6.
- **Lilleshall:** gate **ME-4 start 1951**; flavour text “National Sports Centre rehab” until ME-5 renames Lilleshall.

---

## 6. Recovery mathematics

```text
baseWeeks     = InjuryGenerator roll (unchanged)
eraAdjusted   = ceil(baseWeeks * era.baseWeeksMultiplier)
physioAdjusted= PhysioService.apply(eraAdjusted)   // existing formula
afterRehab    = physioAdjusted - era.rehabMinorDelta | rehabModDelta
```

ME-1 defaults: `baseWeeksMultiplier = 1.20`, `rehabMinorDelta = 0`, `rehabModDelta = 1` (weaker expedited recovery than CM93).

Regression: when resolved era is **ME-6**, multipliers and deltas must reproduce B5.2 `InjuryEconomyB52Test` expectations.

---

## 7. Testing strategy

| Test | Purpose |
|------|---------|
| `MedicalEraRegistryTest` | Boundary years: 1888→ME-1, 1906→ME-2, 1993→ME-6 |
| `MedicalEraGateTest` | ME-1: insure/Lilleshall throw or return `NOT_AVAILABLE_IN_ERA` |
| `MedicalEraCostTest` | ME-1 rehab £0.25 debits club balance correctly |
| `MedicalEraRecoveryTest` | Same seed, ME-1 weeks ≥ ME-6 weeks |
| `InjuryEconomyB52Test` | Run with **gameDate pinned to 1993-08-01** (ME-6) — existing 8 cases green |

Harness: add `GameTestHarness.setGameDate(LocalDate)` for era tests.

---

## 8. Documentation & manual

| Artifact | Update |
|----------|--------|
| Manual ch.6–7 | Era-aware wording; footnote “features unlock over decades” |
| [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) | §9 historical accuracy (done) |
| [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) | §11 gap + link here when shipped |
| [FEATURE_TRACEABILITY_MATRIX.md](FEATURE_TRACEABILITY_MATRIX.md) | M-B5.3-HIST row |

---

## 9. Phased delivery

| Phase | Deliverable | Exit criteria |
|-------|-------------|---------------|
| **HIST-a** | `MedicalEraRegistry` + ME-1/ME-6 only | 1888 save hides insurance/Lilleshall/surgeon |
| **HIST-b** | ME-1 cost tables + recovery multiplier | Costs ≤ 1 week wage for MINOR rehab |
| **HIST-c** | Full ME-2…ME-5 gates + strings | Calendar advance unlocks features in soak test |
| **HIST-d** | Manual + regression ME-6 profile | 131+ tests green; CM93 claim footnoted |

**Estimate:** HIST-a+b ≈ 1 sprint; full timeline ≈ 2 sprints with QA soak.

---

## 10. PO sign-off checklist

- [ ] Confirm insurance first season (**ME-2 vs ME-4 vs ME-6**)
- [ ] Confirm Lilleshall gate year (**1951 vs 1986**)
- [ ] Confirm ME-1 rehab week deltas (0/1 vs CM93 1/2)
- [ ] Confirm fatality / career-ending injury narrative (**out of scope** recommended)
- [ ] Confirm 10-men-no-subs match rule deferred to match engine milestone

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-06-25 | Initial design — dual-layer engine + era overlay |

---

*Implement after PO signs §10. Until then, treat B5.2 as CM93 reference implementation, not 1888 historical simulation.*
