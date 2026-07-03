# Analysis — AI Development Governance & Velocity

**Document type:** Strategy / research synthesis  
**Date:** 2026-07-03  
**Status:** **Active**  
**Audience:** PO, PM, Sponsor, tech leads  

**Trigger:** PO cannot navigate ~175 planning files or approve CM93-F / ME-* rows. Fear that traceability **slows every future feature**.

**Response:** [GOVERNANCE_MODEL_V1.md](GOVERNANCE_MODEL_V1.md) · [PO_BRIEF.md](PO_BRIEF.md) · [AGENTS.md](../../AGENTS.md)

---

## 1. Diagnosis — why it feels slower

| Symptom | Root cause | Cost |
|---------|------------|------|
| PO lost in `F144–F147` / `R-1906 (ME-2)` | **Same information at wrong abstraction** — registry IDs shown to decision-maker | Approval paralysis |
| New feature → 3–5 new markdown essays | **Documentation as output** not as **by-product** of specs | Agent + human context bloat |
| ~175 planning files, overlapping v1/R-1888 | **No progressive disclosure** — everything equally “official” | Onboarding tax doubles each quarter |
| `SYSTEMS_AND_FEATURES_REGISTRY` §1–§6 stale | **Write-only docs** without retirement | Wrong mental model |
| `build.bat` skips tests; 131 tests only in CI | **Local ≠ CI** | Surprise failures |
| No `AGENTS.md` | Each AI session **re-discovers** project from random docs | Token waste, inconsistent diffs |
| Strong test pyramid but no release tags in code | Hard to prove **R-1888 scope** in CI | Scope creep risk |

**Core insight:** Traceability is **good**. **Forcing the PO to be the traceability consumer** is bad. Separate **decision surface** (L0) from **machine ledger** (L2) from **research archive** (L4).

---

## 2. Research — what industry is doing (2025–2026)

### 2.1 Spec-driven development (SDD)

Sources: [Levelop SDD Guide 2026](https://levelop.dev/blog/spec-driven-development-complete-guide-2026), [mini-spec-kit](https://github.com/zoomc/mini-spec-kit), GitHub Spec Kit ecosystem.

| Practice | Fit for Futtoboru |
|----------|-------------------|
| **Constitution first** — non-negotiable stack, boundaries | → `AGENTS.md` (done) |
| **Spec before code** — what/why, not implementation | → `docs/specs/{id}/spec.md` per feature |
| **Atomic tasks** — review 18 small diffs not one 3000-line PR | → `tasks.md` in spec folder |
| **Disposable plans** — re-derive plan when spec changes | → `plan.md` not carved in stone |
| **Anti-skip gates** | → PO_BRIEF decision queue + CI tests |

**Not adopting wholesale:** Enterprise Spec Kit CLI, multi-agent orchestrators — overkill for indie game. We take **constitution + spec folders + registries**.

### 2.2 AGENTS.md as single agent context

Sources: [agents.md standard](https://agents.md), [agents-sync spec](https://github.com/googlarz/agents-sync), [agent-rules](https://github.com/btakita/agent-rules).

- One root file: build commands, boundaries (Always / Ask / Never), doc layers  
- `.cursorrules` remains for Cursor-specific git push rules; **AGENTS.md** is portable truth  
- Per-directory AGENTS optional later (`futtoboru-core/AGENTS.md` for match sim)

### 2.3 Progressive disclosure for governance

Sources: [NN/g Progressive Disclosure](https://www.nngroup.com/articles/progressive-disclosure/), agent context patterns (stub-first tool loading).

| Tier | Futtoboru mapping |
|------|-------------------|
| **Primary UI** | `PO_BRIEF.md` — status, blockers, 3 decisions |
| **Secondary** | `PM_EAGLE_VIEW_REPORT.md` — PM reads weekly |
| **Tertiary** | Registries YAML — agents/devs |
| **Archive** | 175 planning docs — search on demand |

**Rule:** Max **one** secondary link from PO_BRIEF. No link farms.

### 2.4 Documentation-as-code with impact checks

Sources: [ai-dev-template](https://github.com/SiCar10mw/ai-dev-template) governance patterns.

**Adopt lightly:**

- Every PR that changes player-visible behaviour → update `PO_BRIEF` or `features.yaml`  
- `docs/docs-impact.md` stub: “no PO impact” checkbox for internal refactors  
- Pre-commit: run unit tests (optional phase 2)

**Skip:** Docusaurus site, M365 governance, enterprise MCP — not yet.

---

## 3. Recommended courses of action

### Course A — Progressive governance (**start now, zero code**)

| Action | Effort | Effect |
|--------|--------|--------|
| PO reads only [PO_BRIEF.md](PO_BRIEF.md) | 5 min/week | Decisions without file hunt |
| Registries in `docs/registries/*.yaml` | Low | Agents lookup IDs; PO sees plain English |
| [GOVERNANCE_MODEL_V1.md](GOVERNANCE_MODEL_V1.md) | Done | Stop new essay sprawl |
| Mark legacy docs “superseded → PO_BRIEF” | Medium | Clarity without deletion |

**PO sees:** “Injury insurance — **1906 release**” not `F144–F147 R-1906 ME-2`.

### Course B — Spec-driven features (**per feature, not per release**)

| Action | Effort | Effect |
|--------|--------|--------|
| `docs/specs/me1-injury/spec.md` for current blocker | 1 hour | ME-1 work reviewable against criteria |
| Template: `docs/specs/_template/spec.md` | 30 min | Repeatable |
| Close spec → update registry → archive folder | Ongoing | No permanent ANALYSIS_* |

**Gate:** PO approves spec **summary line** in PO_BRIEF unless high risk.

### Course C — Testing uplift (**engineering, high ROI**)

Current state is **ahead of many indies** (131 + 5 + soak + CI). Gaps:

| Improvement | Priority | Rationale |
|-------------|----------|-----------|
| `@Tag("r1888")` on all R-1888 acceptance tests | **P0** | CI profile `-Pr1888` proves release scope |
| `@Tag("cm93")` on B5.2 economy tests (already planned) | **P0** | Default CI = Victorian scope |
| Golden/fixture tests for match sim output | P1 | Catch sim drift without soak |
| Mod loader contract tests (TXT → domain) | P1 | Data breaks silently today |
| `build-test.bat` that runs tests (keep `build.bat` fast optional) | P1 | Local = CI |
| Pre-commit hook: `mvn test -pl futtoboru-core -q` | P2 | Optional speed tradeoff |
| Property-based tests for standings/goal avg | P2 | Historical rule edge cases |
| Visual/UI automation | P3 | LibGDX headless IT covers loop; manual B-9 for feel |

**Do not add:** Mutation testing, 100% coverage mandate — slows without matching risk.

### Course D — Development velocity (**engineering**)

| Improvement | Priority | Rationale |
|-------------|----------|-----------|
| `ReleaseScopeRegistry` in code (from DESIGN_YEARLY_RELEASE_SCOPE) | **P0** | Hard ceiling ME-1 — prevents insurance slip |
| Merge `analyze-ai-branch` → `develop` after R-1888 blockers | **P0** | Clone ≠ docs today |
| Feature flags over `#if RELEASE` spaghetti | P1 | Cleaner era gating |
| `docs/specs/` only active specs in agent context | P1 | Token efficiency |
| Archive `docs/planning/` superseded to `docs/archive/` | P2 | Smaller search space |
| Linux soak script parity | P2 | CI already Linux; local dev gap |

### Course E — AI session discipline (**process**)

| Rule | Effect |
|------|--------|
| Agent loads AGENTS.md + one spec + relevant registry | 90% less context noise |
| No new PM_REPORT unless PO-visible pivot | Stops report inflation |
| Human approves spec summary; agent implements tasks | SDD without enterprise overhead |
| Commits reference spec id: `feat(me1): Trainer labels (spec me1-injury)` | Traceability without essays |

---

## 4. What we are **not** doing

| Temptation | Why skip |
|------------|----------|
| Delete 175 planning docs | History + CM research value — **archive**, don’t erase |
| Replace YAML with a database | Over-engineering |
| Full GitHub Spec Kit CLI | Learning curve > benefit now |
| PO reads CM93 catalogue | Wrong audience — agents use it |
| Drop traceability IDs internally | IDs still in registries for CM parity tracking |
| Public release pressure | PO lock unchanged |

---

## 5. Success metrics (3 months)

| Metric | Target |
|--------|--------|
| PO files read per week | **1** (PO_BRIEF) |
| New permanent planning markdowns | **< 2/month** (major pivots only) |
| Active blockers in YAML | Matches PO_BRIEF table |
| CI green on `analyze-ai-branch` | 131 + 5 maintained |
| R-1888 scoped test profile | `-Pr1888` green before B-9 |
| Time from “PO yes” to human QA invite | ≤ 2 weeks after Tier A |

---

## 6. Decision for PO

| # | Proposal | Recommendation |
|---|----------|----------------|
| G-1 | Adopt progressive governance (PO_BRIEF + registries) | **Yes** |
| G-2 | Stop creating ANALYSIS/PM essays for routine work | **Yes** |
| G-3 | Spec folder per feature ≥ 1 day of work | **Yes** |
| G-4 | Plain English to PO; IDs in YAML only | **Yes** |
| G-5 | Invest in `-Pr1888` CI profile next sprint | **Yes** |

Record answer in [PO_BRIEF.md](PO_BRIEF.md) § Decisions or chat.

---

## 7. Document chain

```
ANALYSIS_AI_DEV_GOVERNANCE_V1.md (this doc)
    → GOVERNANCE_MODEL_V1.md
    → AGENTS.md
    → PO_BRIEF.md
    → docs/registries/
```

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial research synthesis + five courses of action |

---

*Prior gap analysis (IDs for devs): [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md)*
