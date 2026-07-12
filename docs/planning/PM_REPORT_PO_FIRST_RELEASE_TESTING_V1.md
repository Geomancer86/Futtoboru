# PM Report to PO — First Release Testing Reality

**Date:** 2026-07-12  
**Audience:** Product Owner (redirect effort)  
**Scope:** Futtoboru 1888 first release — end-to-end, unit coverage, historic data, scenarios  
**Out of scope:** AI / vision “click the screen” testing (deferred; not needed for first release)

**Detail backlog:** [docs/registries/blockers.yaml](../registries/blockers.yaml) · [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)  
**PO one-pager:** [PO_BRIEF.md](PO_BRIEF.md)

---

## Three points

### 1. What is done

We already have a **strong automated core** for the first-release *management loop*:

- **Unit + engine E2E:** **131** tests on every commit + **5** nightly — green.  
- **Headless career scenarios covered:** new game → continue days → pick XI / tactics → match → ratings → league + FA Cup → save/load → multi-season / soak paths.  
- **Historic 1888–89 start data** is in the mod and exercised by rules / season ITs (12-club Football League, 2-1-0, goal average).  
- **Product loop works** in the engine: this is a real playable Victorian season sim, not a prototype stub.

**PO translation:** The *simulation* of a first release is largely proven. What is *not* proven is Victorian injury presentation, a few UI paths, soak sign-off, and human walkthrough.

---

### 2. What is missing (for “first release complete / fully tested”)

Do **not** confuse “131 tests” with “first release finished.” Gaps that still block a PO-grade first release:

| Gap | Why it matters to you |
|-----|------------------------|
| **Victorian injury (ME-1) + its unit/E2E tests** | Engine still has modern CM93 insurance/rehab; 1888 must hide that and prove Trainer-era costs in tests |
| **Three player-facing UI paths** | Continue double-click, match history click, save/load UI — harness never clicks screens |
| **60-season soak signed off** | Long-run exists; PM has not stamped it for release |
| **Production mod load check + manual ch.6–7** | Real data + tester instructions must match the shipped game |
| **Human end-to-end walkthrough (B-9)** | Only a person can confirm the *full* first-release experience |

**Also missing if “complete historic database researched” means *all football history*:** that is **years** of work — **not** R-1888. For this release, “historic” means **1888–89 researched and in the mod**, which is largely done; later years stay documented for later releases.

**E2E doubling (no AI):** Missing engine/UI-adjacent scenarios to add after ME-1 ships — Victorian injury season path, cup+league multi-season soak sign-off, production-mod bootstrap IT, Continue-day regression via engine-equivalent gates. Scene2D click tests remain a **post-1888** upgrade, not the first-release gate.

---

### 3. What is next (redirect effort here)

**Lock first-release scope to Futtoboru 1888 only**, then in order:

1. **Build ME-1 Victorian injury + the three missing ME-1 unit tests** (highest leverage).  
2. **Fix Continue / history / save UI** and run **soak preflight → PM sign**.  
3. **Manual ch.6–7 + production load check → invite human B-9**.  
4. Only after B-9: tag `1888.0.0` / call first release “complete enough”; then polish or start 1892.

**Do not redirect** into transfers, full CM93 parity, all-history database build-out, or AI screen-click agents until that path is green.

**Ask of PO:** Confirm the three decisions on [PO_BRIEF.md](PO_BRIEF.md) (scope lock, testers after blockers, 1892 next) so engineering does not reopen the backlog.

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-12 | Three-point PO report — done / missing / next for first-release testing |

---

*Eagle view: [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) · Full status: [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md)*
