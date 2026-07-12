# PO Brief — Futtoboru

**One page. Read this.** Everything else is detail for PM, dev, or AI.

**Updated:** 2026-07-12 · **Release:** Futtoboru 1888 · **Branch:** `analyze-ai-branch`

---

## Status

| | |
|-|-|
| 🟢 | Game loop works: league, cup, pick team, play matches, ratings, saves |
| 🟢 | Automated tests: **131 + 5** green on every CI run |
| 🟡 | **8 items** still to finish before human playtesters |
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
| 2 | Human testers after the 8 blockers below? | **Yes** | ☐ |
| 3 | Next big tranche after 1888: **1892 expansion** (second division, reserves, basic transfers)? | **Yes** | ☐ |

Reply in chat or initials here — PM updates the decision log. You do **not** need to open other files.

---

## Blockers before human playtest (plain English)

| # | What | Why it matters |
|---|------|----------------|
| 1 | **Victorian injury screens** — hide modern insurance / rehab menus; show Trainer + old-fashioned costs | 1888 authenticity |
| 2 | **Tests for Victorian injury rules** | So we don't break it silently |
| 3 | **Continue button** — fix double-click bug | Can't start a saved game reliably |
| 4 | **Match history** — click to open past results | Basic navigation |
| 5 | **Save/load UI** — clear save and load paths | Players lose progress |
| 6 | **Long-run stability check** — 60-season automated run signed off | Crashes in year 40 |
| 7 | **Player manual** — chapters 6–7 match what the game actually does | Testers know what to expect |
| 8 | **Load production data** — verify real mod files load | Empty or broken squads |

**Estimate:** ~2 weeks engineering, then invite testers.

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
| 2026-07-12 | Linked full PM status report; status unchanged (8 blockers still open) |
| 2026-07-12 | Note: automated E2E ≠ screen clicks — see ANALYSIS_AGENT_PLAYER_E2E_V1 (post-1888) |
| 2026-07-03 | Created — single PO dashboard; replaces multi-doc PO reading |

---

*PM full status: [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md) · Eagle: [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) · Agent E2E analysis: [ANALYSIS_AGENT_PLAYER_E2E_V1.md](ANALYSIS_AGENT_PLAYER_E2E_V1.md)*
