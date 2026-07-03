# Futtoboru vs Early CM — Feature Depth Analysis

**Document type:** Competitive gap / depth audit  
**Date:** 2026-06-30 (depth rows) · **status refresh:** 2026-07-01  
**Branch:** `analyze-ai-branch` (B5.2 Complete — 131 tests green)  
**Audience:** PO, PM, Match/Sim, QA, stakeholders  

> **⚠️ Status refresh (2026-07-03):** **R-1888** player-facing injuries = **ME-1 Victorian** (not CM93 menus). CM93 engine ~95% — see [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) · [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md). Fitness non-injury: **~70%** CM93.

**Sources:** [CM1_REFERENCE.md](CM1_REFERENCE.md) · [CM93_94_REFERENCE.md](CM93_94_REFERENCE.md) · [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md) · [CM93 manual extract](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md) · [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) · codebase audit 2026-06-30 · [IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md](IMPLEMENTATION_PLAYER_CONDITION_B5_V1.md) §10  

**Companion:** Binary Layer B checklist → [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md) (now includes depth column)

---

## 1. Executive summary

Layer B closed the **CM1 management loop** (pick XI → preview → kick-off → abstract result → 0–10 ratings → condition tick). That is real, tested work — not placeholders. **Injuries** reached **CM93 Core+** after B5.2 Complete (2026-07-01). **Fitness/condition** (training, morale sim, physio match-fitness) remains **Partial+**.

### Direct answer: “We have injuries — what does that mean?” (updated 2026-07-01)

**Yes — for the CM93 injury *management* loop.** Futtoboru ships weighted injury types, physio estimates, INJURY PRONE, Rehabilitate, inbox, insurance, Lilleshall, and surgeon abroad. See [ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md](ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1.md) §2.

| What CM93 had | Futtoboru B5.2 | Remaining gap |
|---------------|----------------|---------------|
| Injury **description** on squad | ✅ 5 mod types + severity | — |
| Physio **recovery estimate** | ✅ `~N wk (OK physio)` | — |
| **INJURY PRONE** | ✅ hidden 1–20 | — |
| **Insurance** + wages while injured | ✅ | Forced-retirement payout deferred |
| **Lilleshall** / **surgeon abroad** | ✅ | — |
| **Rehabilitate** | ✅ | — |
| Inbox injury news | ✅ MODERATE+ | — |
| In-match **IJ** + commentary | ❌ | Presentation milestone |
| Physio **match fitness** (not injury) | ❌ | M-B5.3 candidate |

<details>
<summary>Pre-B5.2 stub audit (2026-06-30 — historical)</summary>

**No — injuries were not at CM93 parity.** What existed was a **Minimal floor stub**:

| What CM93 had | What Futtoboru had (pre-B5.2) |
|---------------|-------------------------------|
| Injury **description** on squad screen | Always `"Strain"` (hardcoded) |
| Physio **recovery estimate** | Random return date only |
| Multiple injury types / severity | Single type only |
| **INJURY PRONE** | Not modeled |
| **Insurance** / Lilleshall / surgeon | Not modeled |

</details>

### Early CM vs modern FM — body parts?

**Neither CM1 nor CM93 used anatomical body-part simulation.** That is a **modern FM** (and later CM3+) depth feature.

| Era | Injury model |
|-----|--------------|
| **CM1 (1992)** | Squad icon `[IJ]`; post-match flags; team energy/morale aggregates — **no manual injury chapter** |
| **CM93/94** | Text **condition description** + physio **weeks estimate**; insurance; Lilleshall; surgeon abroad; **INJURY PRONE** attribute — still **no body-part map** |
| **CM3+ / FM** | Richer medical centre, rehab plans, later FM adds granular injury types — still not “FM24 body diagram” until FM medical overhaul generations |
| **Modern FM (2010s+)** | Detailed injury types, body areas, rehab phases, medical staff specialisms |

Futtoboru should target **CM93 text-injury floor** for Layer B polish, **not** FM anatomical simulation.

---

## 2. Depth scale

Use this scale everywhere below. It replaces binary ✅/❌ in stakeholder conversations.

| Level | Meaning | Example in Futtoboru |
|-------|---------|----------------------|
| **None** | Not implemented | Transfers engine |
| **Stub** | Placeholder UI or dead code path | `TrainingScreenTable` — one button |
| **Minimal** | One hardcoded path; design incomplete | `InjuryGenerator` — strain only |
| **Partial** | Core loop works; missing CM-equivalent sub-features | Fitness (tracked + sim hook; no training recovery) |
| **Core** | Matches CM1/CM93 floor for that feature | 0–10 ratings, XI drives sim |
| **Beyond** | Exceeds early CM | FM-style attribute model (technical/mental/physical) |

**Gap severity** (for prioritisation):

| Gap | Meaning |
|-----|---------|
| **None** | At parity or beyond for early CM target |
| **Minor** | Playable; polish / sub-feature missing |
| **Major** | CM floor incomplete; affects management feel |
| **Total** | Feature absent or stub only |

---

## 3. Master feature depth matrix

**Target columns:** CM1 = 1992 floor · CM93 = 1993/94 floor · **Futtoboru** = `analyze-ai-branch` as of 2026-06-30.

| # | Feature | CM1 depth | CM93 depth | Futtoboru depth | Gap vs CM93 | Missing (priority) |
|---|---------|-----------|------------|-----------------|-------------|-------------------|
| **Management loop** |
| 1 | Advance time / continue | Core | Core | **Core** | None | Loading indicator; AM/PM granularity (defer) |
| 2 | Season completes + rollover | Core | Core | **Core** | None | Multi-league concurrent sim polish |
| 3 | Hotseat multiplayer | Core (4 players) | Core | **None** | Total | P3 — post Layer B |
| **Team selection & tactics** |
| 4 | Pick starting XI (11) | Core | Core | **Core** | None | Human QA sign-off only |
| 5 | Formation presets | Core (predefined) | Core (TCT editor) | **Partial** (3× 1888 templates) | Minor | More formations; CM93-style position icons |
| 6 | Tactics screen / menu | Core (STYLE, push/pull) | Core (DEF/MID/ATT L/R) | **Partial** (formation + manual slots) | Major | Team instructions; push/pull; in-match changes |
| 7 | Captain / set pieces | Basic | Basic | **None** | Total | P2 |
| 8 | Compare players for selection | — | — | **Partial** (attributes visible) | Minor | Side-by-side compare UI |
| 9 | Lineup persists in save | Core | Core | **Core** | None | — |
| 10 | Injured excluded from XI | Core (IJ) | Core | **Core** (engine) | Minor | Tactics UI grey-out (B5 polish) |
| **Match flow & presentation** |
| 11 | Match uses your XI | Core | Core | **Core** | None | — |
| 12 | Match preview before kick-off | Core | Core | **Partial** (lineups + Kick off) | Minor | Opponent form hints; kit colours |
| 13 | Abstract / text result + score | Core (text commentary) | Core (+ injury time) | **Partial** (score + scorers; no commentary) | Major | Text commentary stream; injury time |
| 14 | Named scorers on result | Core | Core | **Core** | None | — |
| 15 | Post-match 0–10 ratings | Core (genre first) | Core | **Partial** (win/loss/goal heuristic) | Minor | Performance vs opponent; minutes; cards |
| 16 | Ratings on result UI | Core | Core | **Partial** (user club XI only) | Minor | Full both squads; sortable |
| 17 | In-match subs / tactical changes | Core (limited) | Core | **None** | Total | P2 |
| 18 | Match replay / printer transcript | Core (printer) | Core | **None** | Total | P3 |
| **Crowds & economy** |
| 19 | Attendance / crowds | Partial (shown on stats) | Partial | **Partial** (FM-inspired model) | Minor | Historical 1888 gate receipts accuracy |
| 20 | Ticket income tied to attendance | Core | Core | **Partial** | Minor | Verify finance integration end-to-end |
| **Player condition** |
| 21 | Fitness tracked | Core (team energy avg) | Core (per player) | **Partial** (per-player %; match cost + rest) | Minor | Training recovery hook |
| 22 | Morale tracked | Core | Core | **Partial** (post-match deltas from result/rating) | Minor | Board/transfer feeds |
| 23 | Match fatigue | Implicit | Core | **Partial** (fatigue %; injury risk modifier) | Minor | Sharpness not in sim strength |
| 24 | Sharpness / match form | — | Partial | **Partial** (tracked; not in sim) | Major | Wire sharpness into `LineupStrengthCalculator` |
| 25 | **Injuries** | Partial (IJ icon) | **Core** (full subsystem) | **Minimal** | **Major** | See §4.1 — types, physio, UI, inbox |
| 26 | Condition affects match sim | Core | Core | **Partial** (fitness multiplier only) | Minor | Fatigue/sharpness/morale sim hooks |
| 27 | Squad UI fit / morale / injury | Core | Core | **Partial** (Fit%, Mor, injury label) | Minor | Sharpness column; return date column |
| 28 | Result screen injury line | Core (IJ flag) | Core | **None** | Major | B5 polish item |
| **Training & development** |
| 29 | Training menus | Basic options | Basic + reserves link | **Stub** | Total | Layer C+ |
| 30 | Attribute development via training | Implicit | Coach-driven | **Partial** (daily random drift placeholder) | Major | Replace with training system |
| 31 | Reserve squad | — | Core | **None** | Total | Layer C+ |
| **Transfers & contracts** |
| 32 | Buy / sell players | Core | Core (intl pool) | **None** | Total | Layer C+ |
| 33 | Transfer list / bidding | Core | Core | **None** | Total | Layer C+ |
| 34 | Contract negotiation | Core | Core | **Partial** (model exists; jobs context) | Major | Layer C+ |
| 35 | Wage / transfer budgets | Basic | Basic | **Partial** (club balance) | Major | Separate budgets |
| **Staff** |
| 36 | Coach / physio / scouts in world | Core (~650 staff DB) | Core (ability ratings) | **Partial** (data + wages) | Major | Hiring UI; physio recovery effect |
| 37 | Staff hiring screen | — | Core | **Stub** | Total | Layer C+ |
| 38 | Physio affects injury recovery | — | Core | **None** | Major | Tie to injury subsystem |
| **Scouting & knowledge** |
| 39 | Scouts / shortlist / search | — | Core (guidance) | **Stub** | Total | Layer C+ |
| 40 | Fog of war / hidden attributes | — | Partial | **None** (all visible) | Total | P3 for 1888 |
| **Discipline & availability** |
| 41 | Suspensions (bookings) | — | Core (5/10/20 pts) | **None** | Total | Layer C+ |
| 42 | Unhappy / transfer request flags | Core (U, R icons) | Core | **None** | Total | Layer C+ |
| **Board & career** |
| 43 | Board expectations / sacking | Core | Core | **Stub** | Total | Layer C+ |
| 44 | Manager jobs / unemployed | — | Core | **Beyond** (Futtoboru job system) | None | — |
| 45 | Resign | Core | Core | **Partial** | Minor | Verify menu path |
| **Competitions & world** |
| 46 | League + cups playable | Core | Core | **Partial** (FA Cup path; league strong) | Minor | Friendlies vs completion |
| 47 | Friendlies arrange | — | Core | **Partial** (hardcoded) | Major | CM93 Arrange Friendly |
| 48 | European / intl management | Partial (cups) | Partial | **None** / defer | Total | Historical scope |
| **UX & persistence** |
| 49 | Inbox / news | — | Core (injuries, sackings) | **Partial** (mandatory gates + messages) | Minor | Injury news items |
| 50 | Save / load game state | Core (season disk) | Core | **Partial** | Minor | Production load path polish |
| **Data model** |
| 51 | Player attributes | Core (skill/pace text) | Core (expanded) | **Beyond** (FM-style axes) | None | Visibility rules for era |
| 52 | Real historical names (1888) | — | — | **Beyond** (mod pipeline) | N/A | — |

### Layer B items — depth vs binary benchmark

The [Layer B benchmark](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md) marked row 13 “Basic injuries” as Code ✅. **Corrected interpretation:**

| Benchmark row | Old read | Depth read |
|---------------|----------|------------|
| Basic injuries | “Done” | **Minimal** — stub path only |
| Squad shows fit / injured | “Done” | **Partial** — columns; no sharpness |
| Match fatigue / sharpness | “Done” | **Partial** — fatigue partial; sharpness not in sim |
| Tactics menu | “Done” | **Partial** — no instructions |

---

## 4. Category deep dives

### 4.1 Injuries (the example feature)

#### CM93 manual-confirmed behaviour

From [CM93_Manual_LemonAmiga_Extract.md](../research/manuals/cm93-94/CM93_Manual_LemonAmiga_Extract.md):

1. Squad screen shows **injury description** + physio **recovery estimate** (not body parts).
2. Hidden **INJURY PRONE** attribute affects susceptibility.
3. **Physio** staff quality reduces recovery time.
4. **Insurance** covers Lilleshall fees, operations, wages during treatment, forced retirement compensation.
5. **Lilleshall** — optional rehab to shorten serious injuries (cost shown).
6. **Surgeon abroad** — rich-club option for very serious injuries.
7. Match commentary: player **“taken off injured”** with `[IJ]` marker.
8. News feed can report squad injuries.

#### Futtoboru implementation map

| CM93 element | Status | File / note |
|--------------|--------|-------------|
| Post-match injury occurrence | ✅ Minimal | `InjuryGenerator.maybeInjure()` |
| Injury type variety | ❌ | Always `strain` |
| Weeks out | ✅ Minimal | Random 1–5 weeks |
| Exclude from XI | ✅ Core | `BestElevenSelector`, `PlayerConditionService.isAvailableForLineup()` |
| Squad injury column | ✅ Partial | `PlayersListTable` — display name only |
| Return date in UI | ❌ | Data exists; column not shown |
| Physio staff effect | ❌ | Staff in DB; no link |
| Insurance / treatment | ❌ | — |
| Lilleshall / surgery | ❌ | — |
| Injury prone attribute | ❌ | — |
| In-match injury event | ❌ | Post-match roll only |
| Result screen injury line | ❌ | Design in B5 §5; not built |
| Inbox message | ❌ | — |
| Mod injury table | ❌ | Design: `mods/injuries/types.txt` |
| Save/load injury state | ✅ Core | `PlayerConditionSaveLoadIT` |

**Verdict:** Injuries are a **Minimal stub** with a **Partial** data model and **Core** XI exclusion. CM93 parity requires **Partial → Core** uplift (~2–3 dev days for floor: 3–5 types, result line, tactics grey-out, return date column, inbox stub).

---

### 4.2 Tactics & XI

| Aspect | CM1/93 | Futtoboru |
|--------|--------|-----------|
| Formations | CM1 predefined; CM93 TCT with DEF/MID/ATT | 3 templates (`2-3-5`, `2-2-6`, `3-2-5`) |
| Manual XI | Shirt pick 1–14 | Slot-based picker + Best XI |
| Team instructions | STYLE, push/pull FORW/BACK | Not implemented |
| Sim integration | Lineup drives match | ✅ `MatchLineupBuilder` → `LineupStrengthCalculator` |
| Formation slot weights | Position aptitude | ✅ `FormationRegistry.scorePlayerForSlot()` |

**Depth:** **Partial** — Core for “XI matters”; Major gap vs CM93 tactics depth.

---

### 4.3 Post-match ratings

| Aspect | CM1 | Futtoboru |
|--------|-----|-----------|
| Scale | 0–10 (manual says 1–10) | 4.0–9.5 float |
| Inputs | Match performance narrative | Win/loss + goals only |
| UI | Post-match screen | User club XI on result screen |

**Depth:** **Partial** — genre hook present (**Core** for “ratings exist”); heuristic is **Minimal** vs CM match performance inference.

---

### 4.4 Fitness, morale, fatigue, sharpness

| Field | Design (B5 doc) | Implemented | Sim hook |
|-------|-----------------|-------------|----------|
| Fitness | Daily rest + match cost | ✅ | ✅ `fitnessStrengthFactor` |
| Morale | Win/loss/goals/rating | ✅ post-match | ❌ not in sim |
| Fatigue | Match bump + daily decay | ✅ | ❌ injury risk only |
| Sharpness | Match gain; idle decay | ✅ tracked | ❌ not in sim |

**Depth:** **Partial** overall — CM1 **Core** for fitness/morale existence; CM93 **Partial** because sharpness/fatigue not fully wired.

---

### 4.5 Attendance

| Aspect | Early CM | Futtoboru |
|--------|----------|-----------|
| Model | Table context implicit | FM-inspired: position, form, opponent, derby |
| Tests | — | Unit + E2E + nightly soak |

**Depth:** **Partial** — arguably **Beyond** early CM for modelling sophistication; **Minor** gap for 1888 historical gate accuracy.

---

### 4.6 Training, transfers, staff, board

All **Stub** or **None** at UI level. CM1 had **basic training options** and **Core transfers** — Futtoboru defer to Layer C+ with intentional scope cut for Layer B.

**Exception:** Futtoboru **Beyond** on unemployed manager job system vs early CM.

---

## 5. What is missing — prioritised backlog

### P0 — Layer B polish (before human B-9)

| Item | Brings depth to |
|------|-----------------|
| Injury type table (3–5 types, mod-driven) | Minimal → Partial |
| Result screen “Injured: X (N weeks)” | None → Partial |
| Tactics screen injured grey-out + warning | Partial UI |
| Squad return-date column | Partial UI |
| Wire sharpness into sim strength | Partial → Partial+ |

### P1 — CM93 injury floor (still Layer B optional / B5.1)

| Item | CM93 reference |
|------|----------------|
| Physio quality → recovery multiplier | Manual §Staff (ii) |
| INJURY PRONE (hidden) on player | Manual attribute list |
| Inbox injury news item | Manual news section |
| Insurance toggle (simplified) | Manual §Insurance |

### P2 — Layer C (CM1 management completeness)

Transfers · training menus · board · suspensions · reserve squad · staff hiring · friendlies · scouting.

### P3 — CM3+ / FM (explicitly out of scope)

Body-part medical model · 2D match engine · custom training schedules · press conferences · dynamic reputation.

---

## 6. Research notes (online + manuals)

| Claim | Evidence | Confidence |
|-------|----------|------------|
| CM93 injuries are text + weeks, not body parts | CM93 manual squad screen §(4); StrategyWiki physio page | ✅ Primary |
| CM93 Lilleshall + surgeon abroad | CM93 manual §Player Injuries | ✅ Primary |
| CM93 INJURY PRONE attribute | CM93 manual attribute list | ✅ Primary |
| CM1 had IJ injured state | CM1 manual menu extract | ✅ Primary |
| CM1 had no insurance/Lilleshall chapter | CM1 manual extract — absent vs CM93 | ✅ Primary |
| Modern FM body-area injuries | Community knowledge; not CM93 | ✅ — out of Futtoboru Layer B scope |

**Web sources consulted:** [Lemon Amiga CM93 manual](https://www.lemonamiga.com/doc/championship-manager-93/315) · [StrategyWiki CM93 Your team](https://strategywiki.org/wiki/Championship_Manager_93/Your_team) · [MobyGames CM93](https://www.mobygames.com/game/1757/championship-manager-93/) · Good Old Days retrospective (insurance gameplay note).

---

## 7. Stale documents superseded by this audit

| Document | Issue | Action |
|----------|-------|--------|
| [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md) §3.2–3.4 | Says no XI, stub tactics, no injuries | **Superseded** for Layer B — see §3 banner |
| [PM_EAGLE_VIEW_REPORT.md](PM_EAGLE_VIEW_REPORT.md) | “Fitness, morale, injuries ❌” | Stale — update separately |
| [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md) §3.3 | Tactics stub | Stale post B4 |

---

## 8. Changelog

| Date | Change |
|------|--------|
| 2026-07-01 | Post-B5.2 status banner; injury §1 rewritten; link to ANALYSIS_FITNESS_INJURY_CM93_PARITY_STATUS_V1 |
| 2026-06-30 | Initial depth audit — 52 features, injury deep dive, CM vs FM clarification |

---

*Update when a feature depth level changes or CM research adds primary evidence.*
