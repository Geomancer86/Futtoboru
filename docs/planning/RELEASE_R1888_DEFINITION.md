# Futtoboru 1888 — Release Definition (R-1888)

**Document type:** Product scope / acceptance criteria  
**Date:** 2026-07-03  
**Status:** **Active** — first named release tranche  
**Tag:** `1888.0.0` · **Internal id:** `release_1888`  

**Supersedes for active work:** [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) (same criteria, legacy `v1.0` label)  
**Readiness analysis:** [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md)  
**Gap analysis:** [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) · **PO lock:** [PO_DECISION_R1888_RELEASE_ROADMAP.md](PO_DECISION_R1888_RELEASE_ROADMAP.md)  
**PM wrap-up:** [PM_REPORT_R1888_RELEASE_WRAP_UP.md](PM_REPORT_R1888_RELEASE_WRAP_UP.md) · **Gap PM:** [PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md](PM_REPORT_R1888_GAP_AND_RELEASE_ROADMAP.md)  

---

## 1. Release vision

> **Futtoboru 1888** lets a manager start in the **1888–89 Football League**, play league + FA Cup under **historical rules**, manage a **CM1-minimum loop** (XI, tactics, preview, result, ratings, Victorian injuries), and continue **procedurally forever** — without loading a new mod per simulated year.

**Not in R-1888:** 1892 Division Two, transfers, training depth, CM93 insurance UI, world news forks, unemployed start, public distribution (PO: local dev until ready).

---

## 2. Pre-human gate (four stages)

| Stage | Exit criterion |
|-------|----------------|
| **Analysis** | [ANALYSIS_R1888_RELEASE_READINESS_V1.md](ANALYSIS_R1888_RELEASE_READINESS_V1.md) published |
| **Design** | [DESIGN_R1888_RELEASE_WRAP_UP_V1.md](DESIGN_R1888_RELEASE_WRAP_UP_V1.md) §5 signed |
| **Development** | ME-1 injury + Layer B UI blockers closed |
| **Automated tests** | 158 PR + 5 nightly green; soak PM-signed |
| **Human QA** | [LAYER_B_TEST_PLAN_V1.md §5](LAYER_B_TEST_PLAN_V1.md) B-9 — **after** above |

---

## 3. Acceptance criteria (inherited from Layer A + B)

Use [RELEASE_V1_DEFINITION.md](RELEASE_V1_DEFINITION.md) §4 IDs **NG-***, **PL-***, **SC-***, **TR-***, **LP-***, **Cup-***, **LC-*** with these R-1888 overrides:

| Override | R-1888 rule |
|----------|-------------|
| Injury UX | **ME-1 only** — Trainer, no Insure/Lilleshall/Surgeon ([DESIGN_INJURY_HISTORICAL_ERA_GATING.md](DESIGN_INJURY_HISTORICAL_ERA_GATING.md)) |
| Marketing | “Victorian injury care” not “CM93 injuries” |
| Re-election SC-6 | **Defer** or message-only (PO) |
| Nov 1888 points SC-7 | **Defer** |
| Unemployed | **Defer** post-R-1888 |

### 3.1 Layer B additions (R-1888)

| ID | Criterion | Automated evidence |
|----|-----------|-------------------|
| LB-1 | Picked XI used in sim | `StartingElevenIT`, `Cm1ManagementLoopIT` |
| LB-2 | Post-match 0–10 ratings | `MatchRatingsTest`, `MatchFeedbackIT` |
| LB-3 | Preview lineup = sim lineup | `MatchLineupPreviewIT` |
| LB-4 | Attendance uses table/form/derby | `AttendanceModelIT` (nightly) |
| LB-5 | Fitness/injury affects availability | `InjuredPlayerLineupIT`, `FitnessSimModifierIT` |
| LB-6 | 5-season Layer B soak stable | `LayerBMultiSeasonSoakIT` (nightly) |
| LB-7 | **ME-1 injury profile** | `MedicalEraR1888GateTest` (when shipped) |

---

## 4. Test commands (R-1888 sign-off)

```powershell
# L1 + L2 (required every commit)
mvn test -pl futtoboru-core

# L3 nightly (required before human QA)
mvn test -pl futtoboru-core -Pnightly

# L3 soak (engineering-only, once — produces evidence for PM/PO sign-off, not a human-tester task)
.\scripts\run-soak-with-preflight.ps1
```

**Expected:** 168 + 5 = **173** automated methods green before L4.

**Note:** L4 human testers run/verify **only** the L1+L2 command above (~1 min) plus normal manual play.
They never run the nightly (L3) or soak commands — those are engineering/CI artifacts that generate the
evidence PM/PO use to sign off, produced before testers are ever invited. See
[ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md](ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md) §4.

---

## 5. Changelog

| Date | Change |
|------|--------|
| 2026-07-03 | Initial R-1888 release definition |
| 2026-07-03 | Cross-links to gap analysis + PO roadmap decision |
| 2026-07-15 | Test count 131+5 → 152+5 after QA hardening pass (cup-replay tests, save-corruption tests, soak fail-fast/progress, player-ID persistence assertion) |
| 2026-07-19 | Test count 152+5 → 158+5 after JFR-profiling-driven fix of a sixth soak perf hotspot (JobManagerTest); 60-season soak now ~74s (was ~80-90 min) |
| 2026-07-19 | Test count 158+5 → 165+5 after implementing all 7 pre-game menu fix units (`ManagerBirthDateValidatorTest` +7); see `docs/specs/pregame_menus_polish/spec.md` |
| 2026-07-19 | Test count 165+5 → 166+5 after fixing a real launch-blocking crash: packaged desktop jar failed to start (`build-and-run.bat`) because a language bundle filename's case didn't match what `I18NBundle` requests — a class of bug invisible to `mvn test` since it runs off loose `target/classes`, not the packaged jar, on a case-insensitive filesystem. Confirmed pre-existing (not a regression), fixed the filenames, added `LanguageBundleFileNamingTest` as a permanent regression guard, and verified by actually launching the built jar. See A-10 in `docs/registries/blockers-resolved.yaml` |
| 2026-07-19 | No test count change (still 167+5) after a third real bug found by a human clicking through the same New Game wizard post-A-11: `NewGameSetupScreen` (Step 2) crashed on construction from a VisUI API misuse (`Tooltip(String)` used for plain text instead of `Tooltip.Builder`), which once fixed revealed a second bug (double-`attach()` to the same target). Also fixed the A-11 debug hook itself, which had been skipping Step 2 entirely instead of routing through it like the real Start button does. See A-12 in `docs/registries/blockers-resolved.yaml` |
| 2026-07-19 (latest) | Test count 167+5 → 168+5 after a report that the cup draw soft-locked Continue and that a full season never finished. Found and fixed 3 stacked bugs: a test-harness-only "two independent game engines" artifact that was corrupting the trustworthiness of every mandatory-message test (new `CupDrawUiFlowIT` + null-guards in `Futtoboru.create()`); cup-draw-result messages for different Round 1 ties sharing one duplicate-detected title so only the first ever reached an inbox; and `AuthorityManager.competitionScheduler` being declared but never assigned, so every cup's Round 2+ draw threw and was silently swallowed — meaning **no cup could ever complete a full bracket**. Manually re-ran 15 procedural seasons afterward: 15/15 league champions and 15/15 FA Cup champions, zero health issues. See A-13 in `docs/registries/blockers-resolved.yaml` |
| 2026-07-19 | Test count 166+5 → 167+5 after a user report that New Game still "failed" post-A-10: turned out the game no longer crashed, but ~36 UI labels across the main game screen rendered as raw untranslated keys (`home`, `squad`, `nationality`, ...) because those keys were never added to any language bundle — found by actually driving the full New Game → Start Game flow end to end via a new debug tool (`-Dfuttoboru.debug.startScreen=NEW_GAME_FULL_FLOW`), not just reading code. Fixed all 4 bundles (en_US/en_UK/sp_LA/sp_SP, real translations) and added `LanguageBundleKeyCoverageTest`, a static-analysis regression guard that checks every literal `LanguageModLoader.getValue(...)` key against every bundle. See A-11 in `docs/registries/blockers-resolved.yaml` |

---

*Out-of-scope features and release timing: [ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md](ANALYSIS_R1888_RELEASE_GAP_AND_ROADMAP_V1.md) §3. Human B-9 is the final gate — not a substitute for automated L1–L3.*
