# PO Brief — Futtoboru

**One page. Read this.** Everything else is detail for PM, dev, or AI.

**Updated:** 2026-07-12 · **Release:** Futtoboru 1888 · **Branch:** `analyze-ai-branch`

> **PO redirect report (3 points):** [PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md](PM_REPORT_PO_FIRST_RELEASE_TESTING_V1.md) — done / missing / next for first release.

---

## Status

| | |
|-|-|
| 🟢 | Game loop works: league, cup, pick team, play matches, ratings, saves |
| 🟢 | Automated tests: **139 + 5** green on every CI run |
| 🟡 | **1 item** left before human playtesters (soak PM sign-off) |
| 🟢 | Tier A engineering blockers **7/8 closed** — ME-1, C-1, PL-5, PL-7, manual, mod load |
| 🔴 | Human QA not started — **correct**; we finish blockers first |
| ⚪ | No public release — local dev only |

---

## What Futtoboru 1888 is

A **Victorian Football League manager** starting **1888–89**. You pick your team, set tactics, play matches, manage fitness and **Trainer-style injury care**, and keep playing season after season.

**It is not** Championship Manager 93. We are building toward that horizon over many releases — not in this one.

---

## Decisions needed from you

| # | Question | PM recommendation | Your call |
|---|----------|-------------------|-----------|
| 1 | Approve R-1888 scope as locked? (no transfers, no modern injury insurance in this release) | **Yes** | ☐ |
| 2 | Human testers after soak sign-off? | **Yes** | ☐ |
| 3 | Next big tranche after 1888: **1892 expansion** (second division, reserves, basic transfers)? | **Yes** | ☐ |

Reply in chat or initials here — PM updates the decision log. You do **not** need to open other files.

---

## Blockers before human playtest (plain English)

| # | What | Status |
|---|------|--------|
| 1 | **Long-run stability** — 60-season automated run + PM sign-off | 🟡 In progress |

**Closed this sprint (2026-07-12):** Victorian injury screens (ME-1), injury tests, Continue button, match history click, save/load UI, player manual ch.6–7, production mod load check.

**Estimate:** Soak green + your sign-off → invite testers.

---

## Not in Futtoboru 1888 (on purpose)

| Feature | When (roughly) | Why wait |
|---------|----------------|----------|
| Buying/selling players | **1892+** release | Needs economy + squad rules |
| Injury insurance menus | **1906+** release | Historically wrong for 1888 |
| Lilleshall / surgeon abroad | **1951–1993** releases | Modern medicine |
| Bookings / discipline | **1919+** release | Needs match card system |
| Reserve squads | **1892+** with 2nd division | Squad depth feature |
| Scouts / coaching staff | **1892+** stub, full later | CM93 depth is 1990s product |

We **document** all of this for the long roadmap. We **ship** only what fits 1888.

---

## How we avoid slowing down

| Old way (pain) | New way |
|----------------|---------|
| PO reads 175 markdown files | PO reads **this file** |
| Every feature = new ANALYSIS/DESIGN/PM essay | Feature spec **only while building**, then registry row |
| IDs like F144–F147 in PO face | Plain English here; IDs live in `docs/registries/` for dev/AI |
| Docs drift from code | Agents update registries + this brief when shipping |

**Governance detail (optional):** [GOVERNANCE_MODEL_V1.md](GOVERNANCE_MODEL_V1.md)

---

## Changelog

| Date | Change |
|------|--------|
| 2026-07-12 | ME-1 Victorian injury UX shipped; 7/8 Tier A blockers closed; 135+5 tests green |
| 2026-07-12 | Linked full PM status report; status unchanged (8 blockers still open) |
| 2026-07-12 | Note: automated E2E ≠ screen clicks — see ANALYSIS_AGENT_PLAYER_E2E_V1 (post-1888) |
| 2026-07-03 | Created — single PO dashboard; replaces multi-doc PO reading |

---

*PM full status: [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md) · Eagle: [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) · Agent E2E analysis: [ANALYSIS_AGENT_PLAYER_E2E_V1.md](ANALYSIS_AGENT_PLAYER_E2E_V1.md)*
