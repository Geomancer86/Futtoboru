# AGENTS.md — Futtoboru

Machine-readable project context for AI coding agents. Humans: start at [docs/planning/PO_BRIEF.md](docs/planning/PO_BRIEF.md).

## Project

- **What:** Historical football management sim. Active tranche: **Futtoboru 1888** (1888–89 Football League start).
- **Stack:** Java 17, Maven, LibGDX 1.12, VisUI. Modules: `futtoboru-core`, `futtoboru-desktop`.
- **Branch truth:** `analyze-ai-branch` = current code + docs. `main` is stale (0.2.0 era).
- **Package root:** `com.rndmodgames.futtoboru`

## Build & test

```powershell
# L1+L2 — run before every commit (152 tests)
mvn test -pl futtoboru-core

# L3 nightly (+5)
mvn test -pl futtoboru-core -Pnightly

# L3 soak (60 seasons) — watch PROGRESS|...|pct=...|eta=... on stderr
mvn test -pl futtoboru-core -Psoak -Dtest=LongRunSoakIT
# or: .\scripts\run-soak-with-preflight.ps1

# Build (prefer test-first workflow)
mvn clean install -pl futtoboru-core,futtoboru-desktop -am
```

Default `build.bat` skips tests — **always run Maven tests separately** before committing.

**E2E note:** `GameTestHarness` is **engine** E2E (no VisUI clicks). Opening the real window and clicking is **human L4** today; proposed L2.5 Scene2D player + optional L5 vision agents are documented in `docs/planning/ANALYSIS_AGENT_PLAYER_E2E_V1.md` — do not implement for R-1888 unless PO schedules it.

## Code conventions

- **Money:** `BigDecimal` only — never `double` for finances.
- **Mod data:** Text files under `futtoboru-core/src/main/resources/mods/`.
- **UI:** VisUI tables in `tables/`, screens in `screens/`.
- **Tests:** JUnit 5. Integration tests use `GameTestHarness`. Tag profiles: `@Tag("nightly")`, `@Tag("soak")`, `@Tag("cm93")`, `@Tag("r1888")`.
- **Style:** Match surrounding code. Minimal diff. No drive-by refactors.
- **Perf:** Never scan an unbounded "every X that ever existed in the save" list (all matches, all messages, all job openings, all seasons) inside a per-day/per-match hot path — the game runs forever, so that cost grows every season. Use a season-scoped watermark/index instead (see `Club.playedMatchesSeasonStartIndex`), or an incrementally-maintained field/cache (see `Club.lastMatchDate`, `FuttoboruGameEngine.mandatoryDrawCandidates`, `JobManager.openJobOpeningsCache`). This exact pattern caused **six** separate real late-soak slowdowns found across 2026-07-15 to 2026-07-19 — the sixth (`JobManager.getAvailableJobs()`) was the dominant one all along (found via JFR profiling, ~85% of sampled CPU time) and fixing it took the 60-season soak from ~80-90 minutes to ~74 seconds; see `docs/planning/ANALYSIS_SOAK_TOOLING_AND_PERF_REQUIREMENTS_V1.md`. When code-reading alone can't explain a slowdown, profile with JFR (`-XX:StartFlightRecording=...settings=profile`, built into the JDK) instead of guessing further. Also avoid copying-then-sorting a full history list just to read the last few entries (`AttendanceCalculator.computeFormRate`) — if the source list is already append-ordered, read its tail instead.
- **Human testers never run or wait on the multi-season soak tests.** Those (`LongRunSoakIT`, `MultiSeasonLeagueAndCupSoakIT`, etc.) are engineering/CI-only nightly artifacts that catch scaling regressions in the background. R-1888 human QA sign-off needs only the fast default suite (`mvn test -pl futtoboru-core`, ~1 min) plus normal manual play of 1-3 seasons in the UI.

## Documentation layers (progressive disclosure)

| Layer | File | Who reads | When |
|-------|------|-----------|------|
| **0 — PO** | `docs/planning/PO_BRIEF.md` | PO | Every week / before decisions |
| **1 — Agent** | `AGENTS.md` (this file) | AI agents | Every session |
| **2 — Feature spec** | `docs/specs/{feature-id}/` | Dev + agent | **Only while building** that feature |
| **3 — Registries** | `docs/registries/` | PM, agents on demand | Lookup / traceability |
| **4 — Research archive** | `docs/research/`, `docs/planning/ANALYSIS_*` | Agents on demand | Never bulk-load into context |

**Do not create new top-level planning essays.** Update registries + PO_BRIEF + a feature spec folder instead.

## Release scope (R-1888)

**In scope:** CM1 loop (XI, tactics, preview, result, ratings), Victorian injuries (Trainer, ME-1), league + cup, procedural seasons.

**Out of scope (later releases):** Transfers, scouts, discipline, reserves, CM93 insurance menus, national teams.

**R-1888 blockers before human QA:** all 8 engineering blockers closed (ME-1 injury UX, C-1 Continue fix, PL-5/PL-7 UI, soak, manual ch.6–7). Only remaining step is PM/PO sign-off on soak evidence — see `docs/registries/blockers.yaml` (A-6).

Full IDs: `docs/registries/release-roadmap.yaml` · PO plain language: `PO_BRIEF.md`.

## Boundaries

### Always

- Run `mvn test -pl futtoboru-core` after code changes.
- Update `PO_BRIEF.md` when PO-visible status changes.
- Update `docs/registries/` when features/bugs/releases change.
- Use human labels in PO-facing text; keep CM93-F / ME-* IDs in registries and specs only.

### Ask first

- Expand R-1888 scope (transfers, insurance UI, new subsystems).
- Add new permanent planning markdown outside `docs/specs/` or `docs/registries/`.
- Push to remote git.
- Merge `analyze-ai-branch` to `main`.

### Never

- Push to remote unless the user explicitly requests it.
- Implement ME-2+ injury insurance UI in R-1888 binary.
- Use `double` for money.
- Force-push `main` or `develop`.
- Skip hooks (`--no-verify`) unless explicitly requested.

## Feature workflow (spec-driven)

When implementing a feature:

1. Create or update `docs/specs/{id}/spec.md` (what/why, acceptance criteria — **not** implementation).
2. Add row to `docs/registries/features.yaml`.
3. Implement + tests.
4. Update `docs/registries/` and `PO_BRIEF.md` if PO-visible.
5. Archive or delete the spec folder when shipped (keep summary in registry).

See [docs/planning/GOVERNANCE_MODEL_V1.md](docs/planning/GOVERNANCE_MODEL_V1.md).

## Key paths

| Path | Purpose |
|------|---------|
| `docs/planning/PO_BRIEF.md` | PO single-page dashboard |
| `docs/registries/features.yaml` | Feature inventory (machine) |
| `docs/registries/blockers.yaml` | Active blockers only |
| `docs/registries/release-roadmap.yaml` | When features ship (machine) |
| `docs/planning/GOVERNANCE_MODEL_V1.md` | Governance model |
| `futtoboru-core/src/test/.../GameTestHarness.java` | Headless E2E harness |
