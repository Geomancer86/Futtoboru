# Analysis — Layer B Management Sim Gap (XI, Tactics, Match, Condition)

**Document type:** Gap analysis + delivery plan  
**Date:** 2026-06-28  
**Branch:** `analyze-ai-branch`  
**Status:** **Active** — PO path after Layer A gates  
**Audience:** PO, PM, Match/Sim, World/Data, QA  

**Companions:** [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) · [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) · [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md) · [LAYER_B_ROADMAP_V1.md](LAYER_B_ROADMAP_V1.md) · [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md)

---

## 1. Executive summary

**Season infrastructure is proven** (fixtures, auto-sim, cup brackets, 2-1-0 rules, headless soak). **The CM1 management layer is ~0% implemented in code** — it exists almost entirely in planning docs.

| User question | Layer B (B1–B4) | Post–Layer B (B5+) |
|---------------|-----------------|---------------------|
| **Initial XI / pick 11** | ✅ **MVP-1** — B1 | — |
| **Tactics / formation** | ✅ **MVP-2, MVP-9** — B4 (+ default in B1) | Mentality, roles, set pieces |
| **Match basic simulation** | ✅ **MVP-3, MVP-8** — B1 | Condition modifiers (B5) |
| **Team form (last 5 W/D/L)** | ✅ **MVP-4** — B2 (attendance + light sim) | — |
| **Post-match ratings 0–10** | ✅ **MVP-5** — B3 | Feed morale later |
| **Result / preview UI** | ✅ **MVP-6, MVP-7** — B3 | — |
| **Player fitness %** | 🔲 Display stub optional B3 | **B5 — COND-1** |
| **Player form / sharpness** | 🔲 Attribute delta display only (today) | **B5 — COND-2** |
| **Injuries** | ❌ Not Layer B | **B5 — COND-3** (CM93 depth) |
| **Fatigue / match load** | ❌ Not Layer B | **B5 — COND-4** |
| **Morale** | ❌ Not Layer B | **B5 — COND-5** |
| **2D match engine** | ❌ Out of scope | Separate R&D track |

**PO lock unchanged:** Layer B = **CM1 minimum loop** ([CM1_REFERENCE.md §4](CM1_REFERENCE.md)). FM borrow = **attendance only** for B2. Player condition depth = **CM93+/FM** → [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md).

---

## 2. CM / FM reference — what the genre did

Sources: [CM1_REFERENCE.md](CM1_REFERENCE.md), [CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md](CM_MANUALS_DEEP_ANALYSIS_BY_RELEASE.md), [CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md](CM_LEGACY_FUTTOBORU_GAP_ANALYSIS.md), [FM_COMPETITIVE_BIBLE.md](FM_COMPETITIVE_BIBLE.md).

| System | CM1 (1992) | CM93+ | FM (modern) | Futtoboru target |
|--------|------------|-------|-------------|------------------|
| Pick XI (11) | ✅ Menu selection | ✅ | ✅ | **B1 MVP-1** |
| Formation presets | ✅ Pyramid etc. | ✅ | ✅ + roles | **B4 MVP-2** (2–3 1888 templates) |
| Abstract match | ✅ Text strength | ✅ | ✅ + xG later | **B1 MVP-3** (lineup strength, not squad avg) |
| 0–10 ratings | ✅ **Genre first** | ✅ | ✅ | **B3 MVP-5** |
| Team form (results streak) | ⚠️ Implicit | ✅ | ✅ → attendance | **B2 MVP-4** |
| Fitness / energy | ✅ Tracked (basic) | ✅ | ✅ Jadedness | **B5 COND-1** |
| Morale | ✅ Tracked | ✅ | ✅ | **B5 COND-5** |
| Injuries | ⚠️ Limited | ✅ | ✅ | **B5 COND-3** |
| Training | ✅ Basic options | ✅ Schedules | ✅ | Post–Layer B |
| Transfers | ✅ | ✅ | ✅ | Post–Layer B |
| 2D pitch view | ❌ | CM4 | ✅ | Out of scope v1 |

**Design rule:** Ship CM1 loop first (XI → sim → ratings). Do not block B1–B4 on injuries/fitness sim.

---

## 3. Code truth today (2026-06-28)

### 3.1 What EXISTS

| Area | Evidence |
|------|----------|
| Season + sim loop | `FuttoboruGameEngine.simulateMatchesForDate()`, `MatchSimulator`, `LeagueStandingsManager` |
| 1888 rules 2-1-0 | `CompetitionRulesLoader`, `HistoricalLeagueRulesIT` ✅ |
| Headless E2E | `GameTestHarness`, `SeasonPlaythroughIT`, `LeagueAndCupIT`, `TenSeasonReportIT`, `MultiSeasonSoakIT` |
| Squad list UI | `SquadScreenTable`, `PlayerDetailScreenTable` |
| Attribute snapshots | `PlayerAttributeSnapshot`, weekly tracking (display “form” as attr delta) |
| Match data | `Match` stores scorers, attendance, goals — result UI does not show scorers/ratings |
| 2D engine prototype | `match/engine/*` — **not** wired to career sim |

### 3.2 What is STUB or MISSING

| Area | Code | Gap |
|------|------|-----|
| **Starting XI** | No `ClubTacticsSetup`, no `selectedPlayerIds` | Sim uses **full squad** average |
| **Formation** | `TacticsScreenTable` placeholder; not in sidebar | No `FormationRegistry` |
| **Match sim v2** | `MatchSimulator.calculateTeamStrength()` over all players | No `MatchLineup` input; RNG 0.7–1.3 |
| **Scorers** | Random from full squad | Should prefer XI forwards |
| **Attendance** | Random 60–90% capacity | **H-1** overwrites ticket sales |
| **Ratings** | None | MVP-5 |
| **Preview / result UI** | Minimal text | MVP-6, MVP-7 |
| **Fitness / injury / fatigue** | Comments in `SquadScreenTable` TODO | No fields on `Player` |
| **Layer B tests** | Only season/regression ITs | 15+ planned classes **not written** |

**Key files:**

- `engine/simulation/MatchSimulator.java` — career sim (not 2D engine)
- `data/Club.java`, `data/Player.java` — no tactics/condition fields
- `tables/main/TacticsScreenTable.java` — stub
- `menu/MainMenuManager.java` — no Tactics sidebar button

---

## 4. Missing work — mapped to delivery phases

### B0 — Layer A close ✅ (A-5 automated)

| ID | Item | Status |
|----|------|--------|
| A-5 | 2-1-0 + goal average | ✅ `HistoricalLeagueRulesIT`, `LeagueCompetitionRulesFormatterTest`, extended `LeagueStandingsManagerTest` |
| A-9 | PO Layer A sign-off | **Open** |

### B1 — Sim core (~4–5 eng-weeks)

**Goal:** Match uses **your 11**, not full squad.

| ID | Deliverable | Design | Tests |
|----|-------------|--------|-------|
| DATA-B1 | `FormationTemplate`, `FormationRegistry`, `ClubTacticsSetup`, `MatchLineup` on `Club` | [DESIGN_TACTICS §3](DESIGN_TACTICS_AND_FORMATIONS.md) | `FormationRegistryTest`, `ClubTacticsSetupSaveLoadIT` |
| MVP-1 | `BestElevenSelector`; persist 11; Squad/Tactics hooks | §3.3–3.4 | `BestElevenSelectorTest`, `StartingElevenIT` |
| MVP-3 | `MatchSimulator` v2 reads `MatchLineup`; scorers from XI | §3.5 | `MatchSimulatorV2Test` |
| MVP-8 | Champion diversity tune | `TenSeasonReportIT` regression | soak `-Psoak` |

**Exit:** `MatchSimulator` never uses full squad for strength; `StartingElevenIT` green.

### B2 — Crowds (~2–3 eng-weeks)

| ID | Deliverable | Tests |
|----|-------------|-------|
| MVP-4 | `AttendanceCalculator` (table position, opponent, **team form**, derby mod) | `AttendanceCalculatorTest`, `AttendanceModelIT` |
| H-1 | Single attendance authority (fix sim overwrite) | `AttendanceOverwriteIT` |

**Team form** here = club last-5 W/D/L for **crowds** — not player sharpness.

### B3 — Feedback (~2–3 eng-weeks)

| ID | Deliverable | Tests |
|----|-------------|-------|
| MVP-5 | Post-match 0–10 ratings on XI | `MatchRatingsTest` |
| MVP-6 | Result UI: score, scorers, attendance, ratings | `MatchFeedbackIT` |
| MVP-7 | Preview: editable XI, opponent auto XI | `MatchLineupPreviewIT` |

### B4 — Tactics UX (~1–2 eng-weeks)

| ID | Deliverable | Tests |
|----|-------------|-------|
| MVP-2 | Formation picker (2–3 1888 templates) | `FormationSimWeightTest` |
| MVP-9 | Tactics in sidebar | Manual + persistence via `SaveLoadIT` |

**Exit:** **B-9** PO Layer B sign-off — CM1 loop walkthrough.

### B5 — Player condition (post–Layer B, ~3–4 eng-weeks)

See [DESIGN_PLAYER_CONDITION_V1.md](DESIGN_PLAYER_CONDITION_V1.md).

| ID | CM/FM analogue | Tests |
|----|----------------|-------|
| COND-1 | Fitness % (CM1 energy) | `PlayerFitnessTest`, `FitnessSimModifierIT` |
| COND-2 | Match sharpness / short form | `PlayerFormTest` |
| COND-3 | Injuries (CM93+) | `InjuryGeneratorTest`, `InjuredPlayerLineupIT` |
| COND-4 | Fatigue / minutes load | `FatigueRecoveryTest` |
| COND-5 | Morale (from ratings + results) | `MoraleUpdateTest` |

**Not started until B-9 signed.**

---

## 5. Match simulation design (abstract — no 2D engine)

### 5.1 v1 today

```
strength = avg(all squad attrs) × homeAdv × RNG(0.7–1.3)
→ score generation → random scorers from full squad
→ random attendance 60–90% (overwrites tickets)
```

### 5.2 v2 target (B1 + B2 + B3)

```
MatchLineup (11 players + formation slots)
  → slot-weighted strength per player
  → formation balance modifier
  → home advantage +15%
  → team form modifier (light, B1)
  → RNG band 0.85–1.15 (reduced)
  → expected goals → discrete score
  → scorers weighted by slot (FWD > MID)
AttendanceCalculator (B2) → single attendance value
PostMatchRatings (B3) → Map<playerId, 0–10>
```

### 5.3 v3 future (B5)

Add `PlayerCondition` modifiers to slot strength:

```
effectiveAttr = baseAttr × fitnessFactor × formFactor × moraleFactor
(injured players excluded from BestElevenSelector)
```

**2D engine** remains debug/R&D — never required for season loop.

---

## 6. Test strategy (unit + headless E2E)

### 6.1 Principles

Same as Layer A / season engine ([V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md)):

1. **No MVP without unit test(s)**
2. **Loop changes need `GameTestHarness` IT**
3. **`mvn test` green every commit; `-Psoak` after B1**
4. Manual CM1 walkthrough → B-9 only after automation green

### 6.2 Test inventory status

| Phase | Planned classes | In codebase |
|-------|-----------------|-------------|
| A-5 | `HistoricalLeagueRulesIT`, formatter tests | ✅ **Done** |
| B1 | 6 classes | ❌ Pending |
| B2 | 3 classes | ❌ Pending |
| B3 | 3 classes | ❌ Pending |
| B4 | 1 class + manual | ❌ Pending |
| B5 | 5+ classes | ❌ Planned |

Full specs: [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md).

### 6.3 `GameTestHarness` extensions (B1+)

| Method | Phase |
|--------|-------|
| `setClubTacticsSetup(clubId, formationId, playerIds)` | B1 |
| `simulateSameFixtureNTimes(n)` | B1 |
| `getLineupUsedInLastSimulation(clubId)` | B1 |
| `getLastMatchRatings(clubId)` | B3 |
| `setPlayerFitness(playerId, pct)` | B5 |

---

## 7. Recommended start order (strict)

```
A-9 PO sign-off (Layer A)
    → B1 DATA-B1 + MVP-1 + MVP-3 + tests
    → B2 MVP-4 + H-1 + tests
    → B3 MVP-5/6/7 + tests
    → B4 MVP-2/9 + B-9 sign-off
    → B5 player condition (optional product decision)
```

**Do not parallel:** 2D engine, transfers, training, 1892 content, fork UI.

---

## 8. Risks

| Risk | Mitigation |
|------|------------|
| `TenSeasonReportIT` breaks after sim v2 | Run soak after MVP-3; tune MVP-8 |
| Save JSON without versioning | Add fields only; `ClubTacticsSetupSaveLoadIT`; MVP-10 later |
| Scope creep (injuries in B1) | PO lock + this doc §1 table |
| UI manual QA bottleneck | Automate rules text + harness ITs first |

---

## 9. Cross-references

| Doc | Role |
|-----|------|
| [LAYER_B_EXECUTION_PLAN_V1.md](LAYER_B_EXECUTION_PLAN_V1.md) | File-level tasks per phase |
| [DEPARTMENT_MATCH_AND_SIMULATION_V1.md](DEPARTMENT_MATCH_AND_SIMULATION_V1.md) | MS-1–7, H-1 |
| [ANALYSIS_MATCH_SIMULATION_STATUS_V2.md](ANALYSIS_MATCH_SIMULATION_STATUS_V2.md) | Sim v1 audit |

---

*Next update: after B1 TAC-1 lands or PO reprioritises B5.*
