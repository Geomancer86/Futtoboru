# PM Report — V1 Finish Plan, MVP Gaps & V1.5 (1892) Roadmap

**Document type:** PM master plan / product analysis  
**Date:** 2026-06-27  
**Branch:** `analyze-ai-branch`  
**Audience:** PO, PM, all department leads, QA, Design  
**Purpose:** Ground the project in **what remains for V1.0 ship**, **what the true MVP still lacks** (tactics, XI, sim, fans), and **what V1.5 (1892 expansion)** adds — before implementation starts.

**Evidence:** 59 automated tests · [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md) · [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) · code audit `MatchSimulator.java`, stub UI tables

---

## 1. Executive summary

| Layer | Name | What it means | Status |
|-------|------|---------------|--------|
| **A** | **V1.0 complete** | Procedural league + cup loop, employed start, static squads, manual QA | **~90% automated** · **UI polish + QA open** |
| **B** | **V1 MVP complete** | CM1-minimum **management feel**: pick XI, basic tactics, credible match sim, attendance/fans, post-match ratings | **~25%** — mostly stubs / RNG sim |
| **C** | **V1.5 (1892 expansion)** | World evolution + Division Two + new clubs in live save | **0% content** · design ready ([DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md)) |

**PO direction (recorded):** Finish **Layer A**, then **Layer B** (sim + XI + fans), then **Layer C** (1892) on Wave 1.5 infra. **Distribution and git remote pushes are PO-controlled** — docs do not prescribe release timing or `git push`.

**Project constraints (PO):**

- **No public release schedule** in project docs — PO decides when/how anything is shared.
- **Never push to remote git** from automation or agents — local commits only unless PO explicitly runs push.

**Key insight:** The **season loop is proven** (headless). The **management game** is not — sim uses **full-squad average + heavy RNG**, no picked team, attendance is **60–90% random**, tactics screen is a **placeholder**. Accrington won **8/10** procedural seasons — acceptable for systems test, **not** for MVP authenticity.

---

## 2. Terminology (avoid confusion)

| Term | Meaning |
|------|---------|
| **V1.0 milestone** | Layer A — internal milestone when PO accepts checklist |
| **V1 MVP** | Layer A + **Layer B** — CM1-minimum management feel |
| **V1.5** | Layer C — 1892 league expansion + `WorldEvolutionManager` (not a version bump on Layer A alone) |
| **v1.1 / v1.2** | Old doc labels for unemployed, transfers — folded into this plan as **MVP-*** and **1892-*** IDs |

---

## 3. Current situation (honest snapshot)

### 3.1 Proven (automated — do not re-build)

| Capability | Evidence |
|------------|----------|
| Procedural season loop + rollover | `LongRunSoakIT`, `TenSeasonReportIT` |
| League + FA Cup headless season | `LeagueAndCupIT` |
| Mandatory draw gates | `MandatoryGateIT` |
| Save JSON roundtrip | `SaveLoadIT` |
| Rules from `competition_rules.txt` | `CompetitionRulesLoaderTest` |
| Weekly finances / wages | 10-season PNE report |
| 556 players / 35 clubs mod | Season 18 inventory |

### 3.2 Thin or broken (player-facing)

| Area | Today | MVP gap |
|------|-------|---------|
| **Match simulation** | All-squad attribute avg; 30% RNG goals; random scorers | No XI, no tactics, no form/table context |
| **Tactics / XI** | `TacticsScreenTable` = one placeholder button; **not in sidebar** | CM1 core loop step missing |
| **Fans / attendance** | `MatchSimulator`: 60–90% capacity random; **overwrites** ticket sales (H-1) | No league position, opponent, or match-type factors |
| **Post-match ratings** | None | CM1 genre-defining feature missing |
| **Match preview / result UI** | “Home vs Away” stub | No lineups, scorers list, attendance display |
| **Production UI** | C-1 double-click, load broken (C-3/H-9), history clicks (H-3) | Blocks manual QA |
| **Sim balance** | Accrington 8/10 champions | Needs XI + tuning, not just RNG tweak |

### 3.3 Match engine today (code truth)

From `MatchSimulator.java`:

```
teamStrength = average(all squad attributes)   // NOT picked XI
homeStrength *= 1.15
random 0.7–1.3 per team
goals from fixed distribution (30% 0, 30% 1, …) adjusted by strength ratio
attendance = capacity * (0.6 + random*0.3)     // ignores table position & opponent
scorers = random player from full squad
```

**Verdict:** Functionally a **season loop driver**, not a **management sim**. Fine for Layer A; **insufficient for Layer B MVP**.

---

## 4. Layer A — Finish V1.0 (current scope)

**Goal:** Tag `v1.0.0` — employed manager completes 1888 league + cup, procedural years, save works in happy path.

### 4.1 Remaining deliverables

| ID | Deliverable | Dept | Acceptance | Priority |
|----|-------------|------|------------|----------|
| **A-1** | Manual QA checklist green | QA | [RELEASE_V1_DEFINITION.md §9](RELEASE_V1_DEFINITION.md) | P0 |
| **A-2** | Fix **C-1** Continue double-click | Platform | One day per click | P0 |
| **A-3** | Fix **C-3/C-4/H-9** load + job init + top-menu load | Platform | Load career → jobs work | P0 |
| **A-4** | Fix **H-3** match history → result | Platform | Click opens correct match | P0 |
| **A-5** | Verify **2-1-0 + goal average** in league UI | Seasons | Table matches mod rules | P0 |
| **A-6** | Cup UI smoke (headless already ✅) | Platform | Bracket/draw visible in manual QA | P1 |
| **A-7** | Inbox policy for long careers | Platform | Cap/archive or PO waiver BLK-4 | P1 |
| **A-8** | Update stale docs (`RELEASE_V1_DEFINITION` §4 rows) | PM | Point to automation truth | P1 |
| **A-9** | PO sign-off on Layer A checklist | PO | Manual QA + automation green | P0 |

### 4.2 Explicitly NOT required for V1.0 tag (PO locked)

- Pick XI / tactics affecting sim  
- Post-match ratings  
- Dynamic attendance model  
- 1892 / second division  
- Unemployed start  
- Transfers, training, scouting  

*These move to Layer B unless PO promotes them to tag gate.*

### 4.3 Layer A exit criteria

```
✅ 59 PR tests green
✅ Manual QA §9 checklist (league + cup + 3 procedural seasons UI)
✅ No C-1 / C-3 / H-3 / H-9 blockers
✅ PO accepts Layer A complete (sim depth continues in Layer B)
```

**Estimated effort:** 1–2 weeks (mostly Platform/UX + QA).

---

## 5. Layer B — V1 MVP complete (missing management features)

**Goal:** A player **chooses a team**, **picks who plays**, sees **credible results and crowds**, gets **ratings** — CM1-minimum loop per [CM1_REFERENCE.md §11](CM1_REFERENCE.md).

### 5.1 Feature matrix — MVP gaps

| ID | Feature | CM1 / FM ref | Code today | MVP spec (minimum) |
|----|---------|--------------|------------|-------------------|
| **MVP-1** | **Pick starting XI (11)** | CM1 team selection | ❌ No model; sim uses full squad | `Club.selectedPlayerIds` (11); UI on squad or tactics; default auto-best-11 |
| **MVP-2** | **Basic tactics / formation** | CM1 formations menu | 🔲 Placeholder | **2–3 era formations now** on extensible schema — [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) |
| **MVP-3** | **Match sim v2** | CM1 abstract match | ⚠️ RNG + squad avg | XI + formation weights + form + opponent; reduced RNG |
| **MVP-4** | **Fan / attendance model** | FM table + opponent + form + derbies | ⚠️ Random % capacity | **PO locked:** table position + opponent + **recent form** + **derby flag**; fix H-1 |
| **MVP-5** | **Post-match player ratings (0–10)** | CM1 genre first | ❌ | Compute from XI performance proxy; show on result screen + optional squad column |
| **MVP-6** | **Match result UI** | CM1 text result | ⚠️ Latest only | Score, scorers by name, attendance, ratings snippet |
| **MVP-7** | **Match preview UI** | CM1 preview | 🔲 Stub | Both XIs (yours editable), opponent auto, strength hint |
| **MVP-8** | **Sim balance pass** | — | Accrington 8/10 | Re-run `TenSeasonReportIT`; champion diversity ± historical plausibility |
| **MVP-9** | **Tactics in sidebar** | CM1 menu | ❌ | Wire `TacticsScreenTable` → `MainMenuManager` |
| **MVP-10** | **Save schema for XI/tactics/ratings** | — | ❌ | `SaveGame` version field; persist selections per club |

### 5.2 Match simulation v2 — design sketch

**Inputs:**

| Factor | Source | Weight (tunable) |
|--------|--------|------------------|
| XI strength | Avg attributes of 11 picked players | Base |
| Formation | Position weights (GK/DEF/MID/FWD) | ±10% |
| Home advantage | Constant | +15% (keep) |
| League form | Club points / max possible or rank | ±5–15% |
| Opponent quality | Opponent XI strength vs yours | Ratio modifier |
| Match type | League / cup / friendly | Goal rate multiplier |
| Randomness | Reduced band | 0.85–1.15 (not 0.7–1.3) |

**Outputs:** goals, scorers (weighted by position), attendance (see MVP-4), per-player rating (MVP-5).

**Tests:** `MatchSimulatorV2Test` — stronger XI beats weaker; home wins more; top-table club draws bigger attendance.

### 5.3 Fan / attendance model — design sketch

**PO locked:** include **league position**, **opponent quality**, **recent form**, and **derbies** — not a full dynamic fan loyalty sim.

```
attendance = stadiumCapacity
           * baseFill(matchType)           // league ~0.55, cup ~0.65, friendly ~0.12
           * positionFactor(leagueRank)    // 1st +12%, mid 0%, bottom −8%
           * opponentFactor(opponentRank)  // top-3 +10%
           * formFactor(last5Results)      // WW +8%, LL −10%, mixed 0%
           * derbyFactor(isDerby)          // +18% if derby pair (mod/script registry)
           * clamp(0.08, 0.98)
```

**Derby registry (data):** `mods/derbies.txt` or season mod — pairs of club ids (e.g. Liverpool–Everton later; 1888 pack may be empty/minimal until clubs exist). Engine reads pairs; MVP can ship with **0 derbies** in 1888 and structure ready for 1892+.

**Recent form:** last 5 competitive results (W/D/L) for home club — points rate vs max.

- **Ticket revenue:** attendance × price ([ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md](ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md)).  
- **Fix H-1:** `MatchSimulator` does not overwrite attendance already set by ticket sales.  
- **UI:** finances avg attendance; match result shows count + “Derby” label when applicable.

### 5.4 Tactics & XI — design sketch

**Full extensible design:** [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md)

**MVP implements:**

- `ClubTacticsSetup` — formation template id + 11 slot assignments + empty `TeamInstructions` (future tactics)
- `FormationRegistry` — 2–3 templates (default `2-3-5-1888`)
- `MatchLineup` snapshot consumed by sim

**UI:** sidebar Tactics; formation dropdown; pick 11; “Best XI” auto-fill.

### 5.5 Post-match ratings — design sketch

| Rating | Rule (v1 MVP) |
|--------|----------------|
| 6.0 | Base for XI player in match |
| +0.5 | Scored goal |
| +0.3 | Win bonus (XI) |
| −0.3 | Loss |
| ±0.2 | Strength vs opponent delta |
| Clamp | 4.0 – 9.5 |

Store: `Map<Long, Float> lastMatchRatings` on club or player; display in result UI. Full history deferred.

### 5.6 Layer B phased delivery

| Phase | IDs | Exit |
|-------|-----|------|
| **B1 — Sim core** | MVP-1, MVP-3, MVP-8, MVP-10 | IT: XI affects outcome; 10-season diversity improves |
| **B2 — Crowds & money** | MVP-4, H-1, H-2 | Attendance correlates with table; finances show avg |
| **B3 — Player feedback** | MVP-5, MVP-6, MVP-7 | Result screen CM1-like |
| **B4 — Tactics UX** | MVP-2, MVP-9 | Sidebar tactics; formation picker |

**Estimated effort:** 6–10 weeks (1 engineer + QA), parallelizable B2/B3 after B1.

**Tests to add:**

| Test | Proves |
|------|--------|
| `StartingElevenIT` | Picked XI used in sim |
| `AttendanceModelTest` | Top club > bottom club attendance at same ground |
| `MatchRatingsTest` | Scorer rating > non-scorer |
| `TenSeasonReportIT` (regression) | No deadlock after sim v2 |

---

## 6. Layer C — V1.5 (1892 expansion)

**Goal:** Playing from earliest season, **Division Two (1892–93)** and new clubs enter the **live save** — PO leaning per [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md).

**Prerequisite:** Layer B sim + XI recommended (24 clubs × credible results).

### 6.1 Engineering (Wave 1.5)

| ID | Deliverable | Blocks |
|----|-------------|--------|
| **WE-1** | `WorldEvolutionManager` | All activations |
| **WE-2–WE-3** | Dormant clubs + lazy new-game load | Scale |
| **WE-4** | Script types 1003–1007 | 1892 timeline |
| **WE-5** | Rollover uses current membership | Multi-div |
| **WE-6–WE-8** | Save-as-truth, schema, `WorldEvolutionIT` | Quality |
| **DB-1–DB-2** | Global IDs + validation CI | Content |

### 6.2 Content & research (1892 pack)

| ID | Deliverable | Notes |
|----|-------------|-------|
| **1892-R1** | Division Two clubs + squads (1892–93) | ~12 new league clubs |
| **1892-R2** | Timeline script pack | Div 2 creation, club activations, election if PO wants |
| **1892-R3** | `HISTORICAL_DATA_SOURCE_LEDGER` rows | Two-source squads |
| **1892-R4** | Rules snapshot for second division | Same 2-1-0 era |

### 6.3 Gameplay & UI (1892-specific)

| ID | Feature | Depends on |
|----|---------|------------|
| **1892-G1** | Second league in `SaveGame` / authority | WE-4, WE-5 |
| **1892-G2** | Competitions UI — multi-league list | [ENGINE_LIBGDX §10](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) |
| **1892-G3** | Division membership rules | PO decision — [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) |
| **1892-G4** | Cup participant pool expands with new clubs | WE-1 |
| **1892-G5** | Per-division fixture draw gates | Existing draw pattern ×2 |
| **1892-G6** | Sim v2 at 24+ clubs | MVP-3 |

### 6.4 Layer C exit criteria

```
✅ Start 1888 → play to 1892 → Division Two appears in save (no new career)
✅ New clubs have squads + fixtures + cup eligibility
✅ Competitions UI shows Div 1 + Div 2
✅ WorldEvolutionIT + extended TenSeasonReport through 1892 calendar
✅ Validation CI V-1–V-13 green on new content
```

**Estimated effort:** 8–12 weeks after DB-1/DB-2 + Layer B sim (content-heavy).

---

## 7. Recommended master sequence

```
┌─────────────────────────────────────────────────────────────┐
│  NOW → 2 wk     Layer A: V1.0 complete (UI bugs + manual QA)│
├─────────────────────────────────────────────────────────────┤
│  +6–10 wk       Layer B: MVP sim + XI + fans + ratings       │
│                 (parallel: DB-1/DB-2 infra for 1892)         │
├─────────────────────────────────────────────────────────────┤
│  +8–12 wk       Layer C: V1.5 1892 expansion                │
└─────────────────────────────────────────────────────────────┘
```

| Track | Lead dept | Runs parallel? |
|-------|-----------|----------------|
| A — Ship | Platform, QA | — |
| B — MVP sim/UX | Match/Sim, Platform | After A starts |
| DB-1/DB-2 | World & Data | During B |
| C — 1892 | World & Data, Seasons | After B1 + DB-1 |

---

## 8. PO decisions

### 8.1 Locked (2026-06-27)

| # | Decision | Choice |
|---|----------|--------|
| 1 | **Formations MVP** | Basic (2–3 historical templates + pick XI) on **extensible schema** — [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) |
| 2 | **Attendance model** | Table position + opponent + **recent form** + **derbies** (structure + formula; derby data file) |
| 3 | **Distribution / git** | PO decides when/how to share builds; **no remote git push** from agents |

### 8.2 Open — needs PO (1892 membership)

**Plain-language brief:** [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md)

**One-sentence question:** When Division Two starts in 1892, do Div 1/Div 2 club lists follow **real history**, **your save’s league table**, or a **hybrid**?

| Option | Summary |
|--------|---------|
| **A** | Historical script — real 1892–93 split regardless of your results |
| **B** | Re-election flavour 1888–91, then historical 1892 split |
| **C** | Save-driven — your table decides who is in each division |
| **D** | Hybrid — real new clubs always; PO picks historical vs save split |

| # | Other open items | Default |
|---|------------------|---------|
| 4 | Nov 1888 rules amendment | Defer (C) |
| 5 | Promotion/relegation after 1892 | Defer until post-1892 milestone |

---

## 9. Department assignment summary

| Department | Layer A | Layer B | Layer C |
|------------|---------|---------|---------|
| **Platform / UX** | C-1, C-3, H-3, H-9, result UI | Tactics screen, preview | Multi-league UI |
| **Match / Sim** | — | MVP-3–5, MVP-8 | Scale to 24+ clubs |
| **Seasons / Rules** | UI 2-1-0 | Match type factors | Div 2 scripts |
| **World / Data** | — | — | 1892 content, DB-1/2, WE-* |
| **QA** | Manual checklist | New sim ITs | 1892 E2E |
| **Design** | — | CM1 ratings/XI spec | 1892 research pack |

---

## 10. Metrics

| Metric | Now | After A | After B | After C |
|--------|-----|---------|---------|---------|
| PR tests | 59 | 59+ | ~65+ | ~70+ |
| CM1 loop (automated) | ~85% | ~90% | ~95% | ~95% |
| CM1 **management** feel | ~25% | ~25% | **~70%** | ~75% |
| Pick XI affects sim | ❌ | ❌ | ✅ | ✅ |
| Attendance vs table + form + derby | ❌ | ❌ | ✅ | ✅ |
| Playable leagues | 1 | 1 | 1 | **2** (1892+) |
| Manual QA | Not started | ✅ | Re-run | Re-run |

---

## 11. Document map

| Need | Read |
|------|------|
| **This plan** | **PM_V1_MVP_AND_1892_MASTER_PLAN.md** |
| V1.0 acceptance (Layer A) | [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) |
| Feature inventory | [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md) |
| Sim evidence | [TEN_SEASON_V1_ANALYSIS.md](TEN_SEASON_V1_ANALYSIS.md) |
| Attendance research | [ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md](ANALYSIS_MATCH_INCOME_ATTENDANCE_V1.md) |
| CM1 benchmark | [CM1_REFERENCE.md](CM1_REFERENCE.md) |
| Tactics / formations architecture | [DESIGN_TACTICS_AND_FORMATIONS.md](DESIGN_TACTICS_AND_FORMATIONS.md) |
| 1892 membership PO brief | [PO_DECISION_1892_LEAGUE_MEMBERSHIP.md](PO_DECISION_1892_LEAGUE_MEMBERSHIP.md) |
| 1892 world model | [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) |
| UI/engine | [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) |
| Bugs | [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) |

---

## 12. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | PO locks: extensible formations, attendance+form+derbies; 1892 brief; no release/push prescriptive language |

---

*Next PM update: after PO decisions §8 and Layer A manual QA start.*
