# CM Legacy → Futtoboru Gap Analysis

**Document type:** Competitive gap / roadmap reference  
**Branch audited:** `analyze-ai-branch` (v0.4.0-SNAPSHOT)  
**Baseline:** Legacy CM feature set through **CM4** · **v1 target:** [CM1_REFERENCE.md](CM1_REFERENCE.md)  
**Date:** 2025-06-25 · **Automation refresh:** 2026-06-27 · **Layer B depth refresh:** 2026-06-30  
**Overall legacy parity (CM4-equivalent systems):** **~12%** · **Core season-loop parity (CM1-level):** **~85% automated** · **CM1 management feel:** **~25%**

> **⚠️ Superseded for season loop:** §3.2 rows on unwired `isLeagueComplete` / no rollover are **fixed** — see [SYSTEMS_AND_FEATURES_REGISTRY.md §0](SYSTEMS_AND_FEATURES_REGISTRY.md). **Active gap work:** [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md) (Layer B — XI, sim, ratings, attendance).

> **⚠️ Superseded for Layer B depth (2026-06-30):** §3.2–3.4 still describe pre–Layer B stubs (no XI, stub tactics, no injuries). **Authoritative feature × depth matrix:** [ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md](ANALYSIS_FUTTOBORU_VS_EARLY_CM_FEATURE_DEPTH.md). Binary Layer B checklist: [ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md](ANALYSIS_LAYER_B_EARLY_CM_FM_BENCHMARK.md).

---

## 1. Executive summary

Futtoboru on the feature branch is a **historical league prototype** with strong **mod/data foundations** and a working **day-by-day loop** (advance → sim matches → standings → finances → inbox). It is **not yet** a Championship Manager–class management sim, even at **CM1 (1992)** completeness for season closure.

### The three gaps that matter most (2026-06-27)

| Gap | CM era | Futtoboru today |
|-----|--------|-----------------|
| **Management loop thin** | CM1: lineup → match → **ratings** | ✅ Layer B — XI + ratings + preview/kick-off (**Partial** depth on ratings/injuries) |
| **Tactics / pick XI** | CM1 formations menu | ✅ **Partial** — 3 formations + manual XI; no team instructions |
| **Crowds not credible** | FM-style table context | ✅ **Partial** — `AttendanceCalculator` (FM-inspired model) |

**Season loop (was P0):** ✅ Proven — completion, rollover, 60-season soak. See [DESIGN_LAYER_B_CM1_CORE_LOOP.md](DESIGN_LAYER_B_CM1_CORE_LOOP.md).

### What already exceeds CM1–CM2 in specific areas

- FM-style **player attribute model** (technical/mental/physical) — deeper than CM1 text stats  
- **Historical mod pipeline** (seasons, scripts, nationality modifiers, professions)  
- **Job/career system** for unemployed managers — beyond early CM  
- **Inbox/message system** with mandatory gates (league draw) — CM4-adjacent UX  

---

## 2. Methodology

| Column | Meaning |
|--------|---------|
| **CM debut** | First series appearance ([CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md)) |
| **CM4 expectation** | What a player expects in CM4 in that category |
| **Futtoboru** | Status on feature branch |
| **Gap** | None / Partial / Major / Total |
| **Priority** | **P0** v1 blocker · **P1** legacy core · **P2** CM3–CM4 depth · **P3** defer / N/A 1888 |

Evidence: `futtoboru-core/src`, [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md), [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md).

---

## 3. Master gap matrix

### 3.1 Match engine & presentation

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Text / abstract match resolution | CM1 | ✅ + 2D option | ⚠️ Strength RNG sim | Partial | P1 |
| Post-match player ratings (0–10) | CM1 | ✅ | ❌ No per-player ratings | Total | P1 |
| Match commentary / narrative | CM1 | ✅ text + 2D | ❌ | Total | P3 |
| 2D top-down match view | CM4 | ✅ | ❌ | Total | P3 |
| Action zones / match stats | CM3 | ✅ | ❌ | Total | P2 |
| Match replay | CM03/04 | ✅ | ❌ | Total | P3 |
| Set-piece takers | CM97/98 | ✅ | ❌ | Total | P2 |
| In-match tactical changes | CM4 | ✅ | ❌ No tactics system | Total | P2 |
| Substitutions (5/3 etc.) | CM93/94 | ✅ | ❌ Not modeled | Total | P2 |

**Files:** `engine/simulation/MatchSimulator.java` — no tactics input, caps goals at 5, overwrites attendance.

---

### 3.2 Season, league & competition loop

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Single league season playable | CM1 | ✅ | ⚠️ 12-club double RR works | Partial | P0 |
| Correct league rules (points, tie-break) | CM1 | ✅ | ❌ 3-1-0 not 1888 2-1-0 + goal avg | Major | P0 |
| Season end detection | CM1 | ✅ | ❌ Unwired | Major | P0 |
| Champion declared / table frozen | CM1 | ✅ | ❌ `getChampion()` unwired | Major | P0 |
| Next season / rollover | CM1 | ✅ | ❌ No `SeasonManager` | Total | P0 |
| Multi-season forever | CM3 | ✅ | ❌ Only season id 18 in mods | Total | P0 |
| Per-season rules from data | CM2+ | ✅ | ❌ No `competition_rules.txt` | Major | P0 |
| Re-election (1888 bottom 4) | — | — | ❌ Comments only | Total | P1 |
| Mid-season rule change (Nov 1888) | — | — | ❌ | Total | P1 |
| Promotion / relegation | CM1 | ✅ | ❌ | Total | P2 |
| FA Cup / knockout cups | CM1 | ✅ | ⚠️ Data loads; no gameplay | Major | P1 |
| Friendlies | CM2 | ✅ | ⚠️ Hardcoded opponents; breaks completion | Partial | P1 |
| Multiple concurrent leagues | CM97/98 | ✅ 39 nations | ⚠️ Infra only; 1 playable | Partial | P3 |
| League draw ceremony | — | — | ✅ Mandatory UI | None | — |
| Fixture list / schedule UI | CM1 | ✅ | ⚠️ Partial date window | Partial | P1 |

**Critical code paths:**

```228:228:futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/ScriptsManager.java
            com.rndmodgames.futtoboru.data.CompetitionRules.createDefaultRules();
```

```138:138:futtoboru-core/src/main/java/com/rndmodgames/futtoboru/engine/simulation/LeagueStandingsManager.java
    public boolean isLeagueComplete(League league) {
```

`isLeagueComplete()` never called from `FuttoboruGameEngine.continueGame()`.

---

### 3.3 Tactics & team selection

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Formations | CM1 | ✅ 20+ presets | ❌ `TacticsScreenTable` stub | Total | P1 |
| Team instructions (passing, mentality…) | CM3 | ✅ full set | ❌ | Total | P2 |
| Per-player match instructions | CM3 | ✅ | ❌ | Total | P2 |
| Captain / set pieces | CM97/98 | ✅ | ❌ | Total | P2 |
| Compare players (squad selection) | CM01/02 | ✅ | ⚠️ Attributes visible; no compare UI | Partial | P2 |
| Lineup / pick XI | CM1 | ✅ | ⚠️ Sim uses squad strength, not picked XI | Major | P1 |

**Files:** `tables/main/TacticsScreenTable.java` — placeholder, not in sidebar.

---

### 3.4 Training & player development

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Training schedules | CM3 | ✅ 25+ session types | ❌ Stub screen | Total | P2 |
| Position-specific training | CM4 | ✅ | ❌ | Total | P2 |
| Delegate to assistant | CM3 | ✅ | ❌ | Total | P3 |
| Fitness / match sharpness | CM3 | ✅ | ⚠️ No fitness model in sim | Major | P2 |
| Attribute development | CM3 | ✅ | ⚠️ Random daily drift placeholder | Major | P2 |
| Injuries | CM93/94 | ✅ | ❌ | Total | P2 |
| Surgery option | CM01/02 | ✅ | ❌ | Total | P3 |

**Files:** `FuttoboruGameEngine.updatePlayerAttributesDaily()` — TODO replace with training.

---

### 3.5 Transfers & contracts

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Buy/sell players | CM93/94 | ✅ | ❌ No transfer engine | Total | P2 |
| Transfer windows | CM01/02 | ✅ EU | ❌ N/A 1888; no signing period model | Total | P3 |
| Loans | CM4 | ✅ | ❌ | Total | P3 |
| Enquiries / bidding | CM4 | ✅ | ❌ | Total | P2 |
| Contract negotiation | CM2 | ✅ | ⚠️ Model exists; manager jobs only | Partial | P2 |
| Squad status (indispensable…) | CM3 | ✅ | ❌ | Total | P2 |
| Wage + transfer budgets | CM4 | ✅ | ⚠️ Club balance; no separate budgets | Partial | P2 |
| Fog of war / scout to reveal | CM01/02 | ✅ | ❌ All stats visible | Total | P3 |

**Files:** `data/PlayerContract.java` ✅ · `tables/main/TransfersScreenTable.java` 🔲

---

### 3.6 Scouting & player knowledge

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Scout assignments | CM99/00 | ✅ | ❌ Stub | Total | P2 |
| Shortlist | CM3 | ✅ | ❌ | Total | P2 |
| Player search filters | CM3 | ✅ | ❌ | Total | P2 |
| Scout opposition | CM4 | ✅ | ❌ | Total | P3 |
| Player notes | CM01/02 | ✅ | ❌ | Total | P3 |

---

### 3.7 Staff & delegation

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Assistant manager | CM3 | ✅ | ⚠️ Staff model; no delegation | Major | P2 |
| Coaches / scouts / physios | CM3 | ✅ | ⚠️ `ClubStaffManager`; wages in expenses | Partial | P2 |
| Staff hiring | CM3 | ✅ | ❌ Stub `StaffScreenTable` | Total | P2 |
| Board roles (chairman, DOF) | CM4 | ✅ | ❌ | Total | P3 |

---

### 3.8 Board, fans, job security

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Board season expectations | CM1 | ✅ | ❌ Stub `BoardScreenTable` | Total | P1 |
| Sacking for failure | CM1 | ✅ | ❌ | Total | P2 |
| Fan expectations | CM4 | ✅ | ❌ | Total | P3 |
| Job offers / unemployed | CM2 | ✅ | ⚠️ `JobManager` works; breaks on load | Partial | P2 |
| Hall of fame / career stats | CM97/98 | ✅ | ❌ | Total | P3 |

---

### 3.9 Morale, personalities & media

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Player happiness / concerns | CM3 | ✅ | ❌ No morale field | Total | P2 |
| Personalities | CM4 | ✅ | ❌ | Total | P2 |
| Talk to player / promises | CM3 | ✅ | ❌ | Total | P2 |
| Fine / discipline | CM99/00 | ✅ | ❌ | Total | P3 |
| Press conferences / Q&A | CM00/01 | ✅ | ❌ | Total | P3 |
| Praise / criticise form | CM4 | ✅ | ❌ | Total | P3 |
| News ticker | CM4 | ✅ | ⚠️ Inbox only | Partial | P2 |

**Files:** `engine/messages/MessageManager.java` ✅ — no player psychology.

---

### 3.10 Finances

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Club balance | CM1 | ✅ | ✅ | None | — |
| Match-day income | CM1 | ✅ | ⚠️ Ticket model; attendance bugs | Partial | P1 |
| Weekly expenses | CM99/00 | ✅ | ✅ `ExpenseCalculator` | None | — |
| Player wages from contracts | CM93/94 | ✅ | ✅ | None | — |
| Stadium upgrades | CM99/00 | ✅ | ❌ | Total | P3 |
| Sponsors | CM4 | ✅ | ⚠️ Partial in finance model | Partial | P3 |
| Separate transfer/wage budgets | CM4 | ✅ | ❌ | Total | P2 |

---

### 3.11 Squads & youth

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| First team squad | CM1 | ✅ | ✅ ~16–20 per club | None | — |
| Reserves | CM93/94 | ✅ own league CM4 | ❌ Stub B-team | Total | P3 |
| Youth / U19 | CM3 | ✅ own league CM4 | ❌ Stub U18 | Total | P3 |
| Trialists | CM3 | ✅ | ❌ | Total | P3 |
| Real player names | CM93/94 | ✅ | ✅ Researched 1888 names | None | — |
| Player professions (1888 jobs) | — | — | ✅ Unique to Futtoboru | **Ahead** | — |

---

### 3.12 International

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| National team management | CM3 | ✅ | ❌ | Total | P3 |
| International call-ups | CM3 | ✅ | ❌ | Total | P3 |
| World cup / qualifiers | CM3 | ✅ | ❌ | Total | P3 |

Not expected for 1888 v1.

---

### 3.13 UI, save/load & meta

| Feature | CM debut | CM4 | Futtoboru | Gap | P |
|---------|----------|-----|-----------|-----|---|
| Menu-driven UI | CM1 | ✅ | ✅ LibGDX tables | None | — |
| Skinnable interface | CM4 | ✅ | ❌ | Total | P3 |
| Keyboard shortcuts | CM4 | ✅ | ❌ | Total | P3 |
| Save / load | CM1 | ✅ | ⚠️ JSON; no version; load gaps | Partial | P0 |
| Database editor | CM97/98 | ✅ | ⚠️ Mod txt files (dev-only) | Partial | P3 |
| Multiplayer | CM1 | ✅ network CM4 | ❌ | Total | P3 |
| Go on holiday | CM3 | ✅ | ❌ | Total | P3 |

**Load blockers:** `Futtoboru.initializeJobSystem()` not on load · in-game load not implemented · `AuthorityManager` stale `currentGame`.

---

## 4. Coverage by CM era (% of features in category present)

Estimates weight **functionality**, not UI polish. “Partial” = 0.5.

| CM era | Categories | Futtoboru coverage | Notes |
|--------|------------|-------------------|-------|
| **CM1** (1992) | 8 core | **~40%** | Missing season end, ratings, tactics XI |
| **CM93/94** | +reserves, names, wages | **~35%** | Names/contracts yes; reserves no |
| **CM3** (1999) | +training, int'l, LAN, scouting | **~15%** | Inbox/jobs hint at depth; systems missing |
| **CM01/02** | +windows, fog, notes | **~10%** | Attributes visible (no fog) |
| **CM4** (2003) | +2D, personalities, fans, budgets | **~8%** | Finances/inbox closest |
| **CM03/04** polish | +replay, relationships | **~5%** | — |

**Blended “legacy through CM4” parity: ~12%.**  
**v1 season-loop (CM1 + historical rules): ~35%** per [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md).

---

## 5. What Futtoboru has that CM4 did not

| Futtoboru feature | Value |
|-------------------|-------|
| Historical 1888 FL mod pipeline | Core differentiator |
| Script-driven league creation (17 Apr 1888) | Good CM4-era scripting analogue |
| Player off-pitch professions | Era authenticity |
| Nationality/region attribute modifiers | Research-backed |
| Unemployed manager job board + negotiation | CM2+ career, more structured |
| Mandatory league draw inbox gate | Strong UX pattern |

---

## 6. Priority roadmap to legacy parity

### P0 — v1 blockers (CM1 loop + 1888 rules)

Must ship before claiming “play season after season”:

1. Wire `LeagueStandingsManager.isLeagueComplete()` + champion into `FuttoboruGameEngine`  
2. Implement `SeasonManager` + `currentSeasonId` on `SaveGame`  
3. Apply `createHistoricalRules()` or mod `competition_rules.txt` at league creation  
4. Season rollover + season 19 mod folder  
5. Fix friendly `matchesPlayed` polluting completion  
6. Fix save/load (`AuthorityManager`, job init, continue double-click)  

**Unlocks:** CM1-class “finish a season and start another” at historical scale.

### P1 — CM1–CM93/94 completeness

- Post-match player ratings feeding morale later  
- Pick XI / basic formation affecting sim  
- Board expectation + sack  
- FA Cup minimum (or explicit defer in v1)  
- Friendlies that don’t break league completion  

### P2 — CM3–CM4 management depth (legacy target)

Ordered by dependency:

1. Tactics screen → sim input  
2. Training → attribute development (replace random drift)  
3. Transfers + squad status + budgets  
4. Scouting + shortlist  
5. Morale / personalities / concerns  
6. Staff hiring + assistant delegation  
7. Media / praise-shame (1888-appropriate flavour)  

### P3 — Defer or adapt

2D engine · 39 nations · international · multiplayer · editor GUI · fog of war · stadium upgrades · youth/reserve leagues (1888 amateur context differs)

---

## 7. Category scorecard (visual)

```text
CM1 baseline     [████████░░░░░░░░░░░░]  40%
CM93/94          [███████░░░░░░░░░░░░░]  35%
CM3 systems      [███░░░░░░░░░░░░░░░░░]  15%
CM01/02          [██░░░░░░░░░░░░░░░░░░]  10%
CM4 full         [█░░░░░░░░░░░░░░░░░░░]   8%

Futtoboru v1 target (season loop)  [███████░░░░░░░░░░░░░]  35% today → 100% at v1
Futtoboru legacy target (CM4 depth) [██░░░░░░░░░░░░░░░░░░]  12% today
```

---

## 8. Recommended doc/code actions

| Action | Owner |
|--------|-------|
| Treat [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md) as feature checklist | Design |
| Map P0 items to [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md) | Eng |
| Add `competition_rules.txt` schema to season 18 mods | World/Data |
| Implement `GameTestHarness` season playthrough test | Platform |
| Expand [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md) as CM4 checklist for P2 | Design |

---

## 9. Sources

- [CM_LEGACY_REFERENCE.md](CM_LEGACY_REFERENCE.md)  
- [CM4_COMPLETE_REFERENCE.md](CM4_COMPLETE_REFERENCE.md)  
- [SYSTEMS_AND_FEATURES_REGISTRY.md](SYSTEMS_AND_FEATURES_REGISTRY.md)  
- [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md)  
- [PROJECT_MASTER_ANALYSIS.md](PROJECT_MASTER_ANALYSIS.md)  
- `futtoboru-core/src/main/java/com/rndmodgames/futtoboru/`

---

## Changelog

| Date | Change |
|------|--------|
| 2025-06-25 | Link CM1 v1 benchmark + PRE_CM1 genre history |
| 2025-06-25 | Initial gap analysis vs feature branch |
| 2026-06-27 | **Post-automation addendum** — see §10 |

---

## 10. Post-automation update (2026-06-27)

After **59 PR tests** and **10-season simulation**, the **P0 gap list in §6 is largely resolved in code**:

| Former P0 gap | Status today |
|-----------------|--------------|
| Wire `isLeagueComplete()` | ✅ Done + IT |
| `SeasonManager` + save fields | ✅ Done |
| `competition_rules.txt` | ✅ Done (season 18 mod) |
| Season rollover | ✅ Procedural — **not** season 19 mod folder |
| Friendly completion pollution (RB-7) | ✅ Fixed |
| Save/load | ✅ JSON IT; **UI load still open** (C-3) |

**Revised parity (automated season loop):**

| Target | Was (2025-06-25) | Now (2026-06-27) |
|--------|------------------|------------------|
| CM1 season loop | ~35% | **~85%** (automated) |
| CM1 full features | ~40% | **~45%** (ratings, XI still missing) |
| CM4 depth | ~12% | **~12%** (management stubs unchanged) |

**Still valid from §6:** P1 (ratings, XI, board, friendlies UX), P2 (tactics, training, transfers…), P3 (2D, multi-nation).

**Forward roadmap:** [PM_ROADMAP_AND_FEATURES_REPORT.md](PM_ROADMAP_AND_FEATURES_REPORT.md)

---
