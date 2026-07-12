# PM Report — Full Project Status & Forward Plan

**Document type:** PM status (all stakeholders)  
**Date:** 2026-07-12  
**Branch:** `analyze-ai-branch` (~111 commits ahead of tracked remote)  
**Release:** **Futtoboru 1888** (`R-1888` / tag `1888.0.0`)  

**PO one-pager:** [PO_BRIEF.md](PO_BRIEF.md)  
**Eagle view:** [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md)  
**Registries:** [docs/registries/](../registries/)  

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **Where are we?** | A playable **1888–89 Football League manager** with CM1 loop (pick XI → tactics → match → ratings), league + FA Cup, procedural seasons, and a full CM93-grade injury **engine**. **Not** ready for human playtesters yet. |
| **How ready is R-1888?** | **~92% pre-human.** Analysis and design are done. Development and automated sign-off have **8 open blockers**. |
| **What is the critical path?** | Ship **Victorian injury UX (ME-1)** + fix Continue / history / save UI + soak + manual → invite human QA. |
| **What must we not do?** | Do not add transfers, CM93 insurance menus, or expand scope into R-1888. Do not invite human testers before Tier A is green. |
| **How do we move forward?** | **Sprint A** (P0 engineering) → **Sprint B** (P1/P2 + soak) → **B-9 human QA** → **R-1888.1 polish** → then **R-1892** design. |

**Bottom line for the table:** The product is real and testable in automation. The next ~2 weeks of disciplined engineering unlock human QA. After that, the roadmap is already decided in plain language — 1892 expansion, later medical eras — without needing the PO to read 175 files.

---

## 2. Stakeholder map

| Stakeholder | What they need | Where to look | Decision power |
|-------------|----------------|---------------|----------------|
| **PO** | Scope, blockers, yes/no calls | **[PO_BRIEF.md](PO_BRIEF.md)** only | Scope lock, human QA gate, next release |
| **Sponsor** | Progress vs north star, risk | This report §1 + §8 | Funding / priority |
| **PM** | Schedule, traffic lights, owners | This report + registries | Sprint plan, gate enforcement |
| **Match/Sim** | ME-1 injury skin + tests | Blockers A-1, A-2 | Implementation |
| **Platform/UX** | Continue, history, save/load | Blockers A-3, A-4, A-5 | Implementation |
| **QA** | Test pyramid, soak, load path | A-6, A-8 + L1–L4 policy | Sign-off soak; B-9 invite |
| **Manual / UX writing** | Ch.6–7 match shipped game | A-7 | Manual accuracy |
| **AI agents** | Constitution + registries | [AGENTS.md](../../AGENTS.md) | Execute specs; no scope expand |

---

## 3. Where we are (by horizon)

### 3.1 Product horizons

```
NOW     R-1888 — Victorian FL manager (CM1 loop + ME-1 injuries)     ← WE ARE HERE (~92%)
NEXT    R-1888.1 — polish from human QA
MID     R-1892 — Div 2, reserves, basic transfers
LATER   R-1906 insurance · R-1919 discipline · R-1951 Lilleshall · R-1965 subs · R-1993 CM93 menus
FAR     Full CM + FM historical completeness
```

### 3.2 Four-stage gate (R-1888 only)

| Stage | Readiness | Blocks human QA? | Notes |
|-------|-----------|------------------|-------|
| **Analysis** | ~95% | No | CM93 catalogue (106 IDs), medical eras, gap/roadmap locked |
| **Design** | ~92% | No | ME-1 design ready; yearly release model live |
| **Development** | ~88% | **Yes** | ME-1 skin + 3 UI bugs open |
| **Automated tests** | ~93% | **Yes** | 131+5 green; soak unsigned; ME-1 tests missing |
| **Human QA (L4)** | 0% | — | Correctly blocked |

**Overall pre-human: ~92%.**

### 3.3 What already works (shipped)

| Area | Status |
|------|--------|
| 1888–89 start, 12-club FL, FA Cup | Shipped |
| Procedural season rollover (∞) | Shipped + ITs |
| Pick XI, formations, tactics | Shipped (partial depth OK for R-1888) |
| Match preview → result → ratings | Shipped |
| Fitness / morale / fatigue | Partial — playable |
| CM93 injury **engine** (B5.2) | Shipped in **dev** (insurance/Lilleshall code exists — must be **hidden** for R-1888) |
| Wages, gate receipts, weekly accounts | Shipped |
| Save/load (engine) | Shipped; **UI paths** still blocker |
| Automated suite | **131** PR + **5** nightly green (documented) |
| CI | Maven Test + nightly soak workflows |

### 3.4 What is missing (three tiers — do not conflate)

| Tier | Meaning | Count | Action |
|------|---------|-------|--------|
| **A — R-1888 blockers** | In scope, not done | **8** | Close before human QA |
| **B — Intentional defer** | Documented for later R-YYYY | ~55 CM93 features | Do **not** pull into R-1888 |
| **C — Full CM93 parity** | North star | ~66 at None/Stub | Multi-year; not a claim for 1888 |

---

## 4. Open blockers (Tier A) — owners & order

| ID | Priority | Item | Owner | Sprint |
|----|----------|------|-------|--------|
| A-1 | P0 | Victorian injury UX (ME-1) — hide insurance/Lilleshall/surgeon; Trainer; shilling costs | Match/Sim | A |
| A-2 | P0 | ME-1 automated tests | Match/Sim | A |
| A-3 | P0 | Continue double-click (C-1) | Platform | A |
| A-4 | P1 | Match history click (PL-5) | Platform | B |
| A-5 | P1 | Save/load UI (PL-7) | Platform | B |
| A-6 | P1 | 60-season soak PM sign-off | QA / PM | B |
| A-7 | P2 | Manual ch.6–7 R-1888 wording | Manual | B |
| A-8 | P2 | Production mod load happy-path | QA | B |

**Estimate:** Sprint A ≈ 1 week · Sprint B ≈ 1 week · then invite B-9.

**Dependency:** A-1 before A-2. A-1–A-5 before B-9. A-6 preferred before B-9 (PM may waive only with PO note).

---

## 5. Traffic lights (all departments)

| Area | Light | Comment |
|------|-------|---------|
| CM1 management loop (automated) | 🟢 | Proven in harness |
| CM1 loop (human feel) | ⚪ | Waiting on B-9 |
| Victorian injury (ME-1) | 🔴 | Engine yes; era skin no |
| Transfers / market | 🟢 Out of scope | Correctly deferred to R-1892+ |
| League + cup + rollover | 🟢 | Shipped |
| UI regressions (Continue, history, save) | 🔴 | A-3–A-5 open |
| L1+L2 tests (131) | 🟢 | Green |
| L3 nightly (5) | 🟢 | Green |
| L3 soak (60 seasons) | 🟡 | Exists; PM unsigned |
| Human QA | ⚪ | Blocked by policy |
| Public release | ⚪ | No — local only (PO lock) |
| Documentation / governance | 🟢 | PO_BRIEF + registries live |
| PO decisions signed | 🟡 | Scope + governance still pending initials |
| Branch hygiene | 🟡 | `analyze-ai-branch` is truth; `main` is 2023-era |
| AI agent onboarding | 🟢 | AGENTS.md + YAML registries |

---

## 6. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| ME-1 slips; CM93 insurance remains visible in 1888 | Historical authenticity failure | Hard release ceiling; A-1 is P0 |
| Scope creep (transfers “just one more thing”) | Delays human QA months | PO_BRIEF + Ask-first in AGENTS.md |
| Doc sprawl returns | PO paralysis | No new ANALYSIS/PM essays for routine work |
| Branch never merges to `develop`/`main` | Clone ≠ docs | Merge after R-1888 blockers + B-9 |
| Soak never signed | Silent long-run bugs | Sprint B preflight script |
| PO decisions stay unsigned | Agents re-ask; ambiguity | Sign PO_BRIEF § Decisions this week |

---

## 7. How we move forward

### Phase 0 — This week (governance)

| # | Action | Owner | Exit |
|---|--------|-------|------|
| 0.1 | PO signs decisions on [PO_BRIEF.md](PO_BRIEF.md) | PO | Decisions → `decisions.yaml` signed |
| 0.2 | Create `docs/specs/me1-injury/spec.md` from template | Match/Sim + agent | Spec approved (summary on PO_BRIEF if needed) |
| 0.3 | Freeze R-1888 scope verbally with team | PM | No Tier B work scheduled |

### Phase 1 — Sprint A (P0 — ~1 week)

| # | Action | Owner | Exit |
|---|--------|-------|------|
| 1.1 | Implement ME-1 UX (hide CM93 menus; Trainer; costs) | Match/Sim | A-1 closed |
| 1.2 | ME-1 tests + `@Tag("r1888")` / `@Tag("cm93")` split | Match/Sim | A-2 closed; 131+ green |
| 1.3 | Fix Continue double-click | Platform | A-3 closed |
| 1.4 | `mvn test -pl futtoboru-core` (+ nightly) | All | Green before merge |

### Phase 2 — Sprint B (P1/P2 — ~1 week)

| # | Action | Owner | Exit |
|---|--------|-------|------|
| 2.1 | Match history click + save/load UI | Platform | A-4, A-5 closed |
| 2.2 | Run soak preflight; PM signs | QA / PM | A-6 closed |
| 2.3 | Manual ch.6–7 R-1888 slice | Manual | A-7 closed |
| 2.4 | Production mod load verification | QA | A-8 closed |
| 2.5 | Update PO_BRIEF + `blockers.yaml` (empty active list) | PM | Ready flag |

### Phase 3 — Human QA (B-9)

| # | Action | Owner | Exit |
|---|--------|-------|------|
| 3.1 | Invite testers with B-9 checklist | QA | Walkthrough complete |
| 3.2 | Log issues → R-1888.1 vs R-1892 | PM | Backlog sorted |
| 3.3 | Tag `1888.0.0` when PO accepts | PM | Release milestone |

### Phase 4 — After R-1888

| Release | Focus |
|---------|--------|
| **R-1888.1** | Polish from B-9 (icons, result injury line, UX) |
| **R-1892** | Div 2, reserves, **transfers Core** |
| **R-1906+** | Medical / discipline / CM93 menus per roadmap |

---

## 8. Sponsor / north-star snapshot

| Horizon | Status |
|---------|--------|
| **Short** — playable Victorian season | ~92% to human QA |
| **Mid** — 1892 world expansion | Designed, not started |
| **Long** — medical eras + CM93 menus | Documented; ship by R-YYYY |
| **Far** — full CM + FM history | Years; honest north star |

**We are not “behind industry standards” on engineering discipline** (tests, CI, catalogues). We were behind on **PO-facing simplicity** — fixed 2026-07-03 with progressive governance. The remaining gap is **finishing Tier A and inviting humans**.

---

## 9. Messaging (say / do not say)

| Say | Do not say |
|-----|------------|
| Futtoboru 1888 — Victorian Football League manager | Full Championship Manager 93 |
| ~92% ready for human testers; 8 blockers | Ready for players / public release |
| Injury insurance ships in the 1906 release | We have CM93 insurance in 1888 |
| Transfers start with the 1892 expansion | Transfers coming in the next 1888 patch |
| PO reads one page (PO_BRIEF) | Please review the CM93 feature catalogue |

---

## 10. Open PO decisions (blocking clarity, not engineering start)

| # | Question | PM rec |
|---|----------|--------|
| 1 | Lock R-1888 scope (no transfers, no modern insurance UI)? | **Yes** |
| 2 | Human testers only after the 8 blockers? | **Yes** |
| 3 | Next big tranche = **1892** (Div 2, reserves, basic transfers)? | **Yes** |
| 4 | Keep PO_BRIEF + registries as the governance model? | **Yes** |

Engineering may start Sprint A under PM recommendation; PO initials remove rework risk.

---

## 11. Document & registry pointers

| Audience | Doc |
|----------|-----|
| PO | [PO_BRIEF.md](PO_BRIEF.md) |
| PM / Sponsor | This report · [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) |
| Agents | [AGENTS.md](../../AGENTS.md) |
| Features / blockers / roadmap | [docs/registries/](../registries/) |
| Gap tiers (dev IDs) | [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) |
| Readiness math | [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) |
| Governance | [GOVERNANCE_MODEL_V1.md](GOVERNANCE_MODEL_V1.md) |

---

## 12. Changelog

| Date | Change |
|------|--------|
| 2026-07-12 | Full-project PM report — all stakeholders, critical path, four phases |

---

*Refresh when any Tier A blocker closes or PO signs decisions. Do not create a parallel essay for routine sprint updates — update PO_BRIEF + blockers.yaml instead.*
