# Governance Model — Progressive Disclosure

**Document type:** Process / governance  
**Date:** 2026-07-03  
**Status:** **Active**  
**Audience:** PO, PM, all contributors, AI agents  

**Problem solved:** ~175 planning markdown files are excellent for traceability but **unusable for the person who approves scope**. This model keeps deep history without forcing the PO to hunt files.

---

## 1. Principle

> **Document everything for machines and specialists. Show the PO one page.**

Inspired by:

- **Progressive disclosure** (NN/g, IxDF) — essential info first, detail on request  
- **Spec-driven development** (2025–2026) — specs drive agents; humans approve specs not 3000-line diffs  
- **AGENTS.md standard** — one portable agent constitution ([agents.md](https://agents.md))  
- **Mini-spec-kit** — lightweight gates per feature, not per release essay  

---

## 2. Five layers

```
┌─────────────────────────────────────────────────────────────┐
│ L0  PO_BRIEF.md          ← PO reads ONLY this routinely   │
├─────────────────────────────────────────────────────────────┤
│ L1  AGENTS.md            ← AI loads every session           │
├─────────────────────────────────────────────────────────────┤
│ L2  docs/registries/*.yaml ← machine truth (features,     │
│                              blockers, roadmap)             │
├─────────────────────────────────────────────────────────────┤
│ L3  docs/specs/{id}/    ← created ONLY while building     │
│     spec.md · plan.md · tasks.md                            │
├─────────────────────────────────────────────────────────────┤
│ L4  docs/research/ · docs/planning/ANALYSIS_* · CM bibles   │
│     ← archive; agents load on demand, never bulk            │
└─────────────────────────────────────────────────────────────┘
```

| Layer | Max PO effort | Update trigger |
|-------|---------------|----------------|
| L0 | **5 min/week** | Blocker change, release gate, PO decision |
| L1 | None (agents) | Stack, boundaries, workflow change |
| L2 | None (PM/agents) | Ship feature, resolve blocker, new release |
| L3 | Optional review of spec | Start/end feature work |
| L4 | Never unless curious | Research only |

---

## 3. What stops being created

| Stop | Replace with |
|------|--------------|
| New `ANALYSIS_*` per question | Update registry + PO_BRIEF; one analysis doc per **major** pivot only |
| New `PM_REPORT_*` per sprint | Update PO_BRIEF + optional short note in git commit |
| New `PO_DECISION_*` per topic | PO_BRIEF § Decisions + `docs/registries/decisions.yaml` when signed |
| Duplicate registries (ISSUES + BUGS + SYSTEMS) | `blockers.yaml` + `features.yaml` active; legacy markdown frozen |
| CM93-F IDs in PO text | Plain English in PO_BRIEF; IDs in YAML only |

**Existing ~175 planning docs:** Keep as **archive**. Do not delete history. Mark superseded headers pointing to L0–L2.

---

## 4. Feature workflow (spec-driven, lightweight)

For any work that produces a `git diff`:

### Phase 1 — Spec (before code)

Create `docs/specs/{feature-id}/spec.md`:

- **What** and **why** (user-visible)
- Acceptance criteria (testable)
- **Release:** R-YYYY
- **Out of scope** explicitly

PO approves **spec summary in PO_BRIEF § Decisions** — not the whole spec unless they want to.

### Phase 2 — Plan + tasks (agent or dev)

- `plan.md` — technical approach (disposable when spec changes)
- `tasks.md` — atomic checklist mapped to spec criteria

### Phase 3 — Implement + test

- Code + tests per [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md)
- Tag tests: `@Tag("r1888")`, `@Tag("cm93")` as appropriate

### Phase 4 — Close

- Update `features.yaml`, `blockers.yaml`, `PO_BRIEF.md`
- Move spec to `docs/specs/_done/{feature-id}/` or delete if trivial
- **No** new permanent planning essay

**Tiny fixes** (typo, one-line bug): skip spec folder; still update blockers if applicable.

---

## 5. PO decision queue

Only **open** decisions appear in PO_BRIEF § Decisions.

When PO answers:

1. PM moves row to `docs/registries/decisions.yaml` with date  
2. Remove from PO_BRIEF queue  
3. Agents proceed without re-asking  

Historical PO essays (`PO_DECISION_R1888_*`) remain archive; new decisions use YAML + PO_BRIEF.

---

## 6. Agent rules (summary)

Full rules: [AGENTS.md](../../AGENTS.md)

- Load AGENTS.md + relevant registry/spec — **not** all of `docs/planning/`  
- Never expose CM93-F IDs to PO in chat unless PO asks  
- Update L0 + L2 when shipping or changing blockers  
- Ask before expanding release scope  

---

## 7. Migration (no big-bang)

| Week | Action |
|------|--------|
| **Now** | PO_BRIEF + AGENTS.md + registries live |
| **Ongoing** | New work uses spec folders; stop new ANALYSIS/PM essays |
| **Next merge** | Add `docs/archive/planning/` banner on superseded docs |
| **R-1888 ship** | Snapshot PO_BRIEF as release note; trim blockers.yaml |

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial progressive governance model |

---

*Strategy research: [ANALYSIS_AI_DEV_GOVERNANCE_V1.md](ANALYSIS_AI_DEV_GOVERNANCE_V1.md)*
