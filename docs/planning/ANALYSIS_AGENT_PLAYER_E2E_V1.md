# Analysis — Agent-as-Player End-to-End Testing

**Document type:** Analysis / design options (no implementation)  
**Date:** 2026-07-12  
**Status:** **Active** — research + recommended path; **not scheduled for R-1888**  
**Audience:** PO, PM, QA, Platform, AI agents  

**Question:** Can our E2E tests open the game, watch screens, and click like Claude (or agents) acting as players? How should we design that if we want it?

---

## 1. Short answer

| Question | Answer |
|----------|--------|
| Do today’s automated “E2E” tests open the real game UI and click? | **No.** |
| What do they do? | Drive the **engine** headlessly (`GameTestHarness`) — same *logic* as Continue / match / seasons, **no VisUI screens**. |
| Can Claude “computer use” play Futtoboru like a human today? | **Possible in theory** (screenshot + mouse on the LWJGL window), **poor fit** for CI: OpenGL canvas, no accessibility tree, flaky, expensive. |
| Should we build agent-as-player testing? | **Yes, later** — as a **new tier**, not a replacement for L1–L3. Start with **deterministic Scene2D clicks**, not vision agents. |

**Bottom line:** “E2E” in Futtoboru today means **career/engine E2E**, not **GUI E2E**. Human B-9 still owns “open game and click.” Agent-as-player is a **designed future layer** (proposed L2.5 → L5 below).

---

## 2. What we have today

### 2.1 Automated path (L1–L3)

| Piece | Path | Opens window? | Clicks UI? |
|-------|------|---------------|------------|
| Unit tests | JUnit | No | No |
| `GameTestHarness` | `futtoboru-core/.../harness/GameTestHarness.java` | No | No |
| Headless bootstrap | `HeadlessGdxTestSupport` → `HeadlessApplication` | No | No |
| Match / season ITs | Engine APIs | No | No |
| Soak | Same harness, long run | No | No |

**How Continue is “tested” today:** `continueOneDay()` → `FuttoboruGameEngine.continueGame()` — not the Continue button.

**How draw gates are cleared:** `acknowledgeMandatoryDraws()` flips message flags — not inbox UI clicks.

**VisUI in headless:** `Futtoboru.create()` skips skin and `MENU_SCREEN` when `Gdx.gl == null`. Screens never exist in CI.

### 2.2 Real game path (what humans / Claude would see)

| Piece | Path |
|-------|------|
| Launcher | `futtoboru-desktop/.../DesktopLauncher.java` → `Lwjgl3Application` |
| Window | ~1920×1080, real GL |
| UI | VisUI + Scene2D tables (`MainGameScreen`, squad, tactics, …) |

This path is **not** wired into Maven ITs.

### 2.3 Explicit prior product stance

From [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md):

- GUI E2E is **not** the primary gate  
- **Explicitly not v1:** Sikuli / Robot full GUI E2E  
- Optional idea noted: synthetic touch to Continue actor — **never implemented**

[LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md): L4 Human = only tier that walks real screens (B-9 checklist).

---

## 3. Why “Claude clicks the game” is hard for LibGDX

Industry 2025–2026 pattern for agent tooling (cost hierarchy):

```
Native API  →  structured UI tree  →  browser DOM  →  screenshot computer-use  →  human
   cheap         reliable              good           expensive / flaky           last
```

| Channel | Works on Futtoboru? | Why |
|---------|---------------------|-----|
| **Engine API** (today) | ✅ | What harness already uses |
| **Scene2D actor tree** (inject `touchDown`/`touchUp`) | ✅ Possible | Same toolkit as VisUI; deterministic; CI-friendly if GL/skin bootstrapped |
| **OS accessibility tree** (Playwright-desktop / Tarsier / SimuLang style) | ❌ / weak | LibGDX draws an **OpenGL canvas** — OS usually sees one blank window, not “Continue” / “Squad” buttons |
| **Screenshot + vision click** (Claude computer use) | ⚠ Possible | Sees pixels; works on games; high cost, DPI/layout brittle, not a CI gate |
| **Sikuli / Java Robot** | ⚠ Possible | Same pixel fragility; already rejected as v1 gate |

**Implication:** Agents that behave well on **web apps** (DOM) do **not** automatically transfer to Futtoboru. We need either:

1. A **Player Action API** over Scene2D / screen controllers, or  
2. Vision computer-use as **exploratory** QA only.

---

## 4. Design options (analysis — not implement yet)

### Option A — Keep status quo (L1–L3 + human L4)

| Pros | Cons |
|------|------|
| Already strong engine coverage | UI bugs (C-1, PL-5, PL-7) only caught by humans |
| Cheap CI | Agent cannot “play” the game |

**When:** Default through **R-1888** human QA.

### Option B — L2.5 Scene2D / VisUI synthetic player (**recommended next**)

**Idea:** Boot enough GL/skin to construct real screens (or extract Stage from `MainGameScreen`), then:

```
findActor("continueButton") → stage.touchDown / touchUp → assert gameDate +1 day
```

| Pros | Cons |
|------|------|
| Deterministic; same CI as Maven | Needs GL in CI (or framebuffer / mock GL research) |
| Catches Continue double-click, grey-outs, disabled buttons | Does not “see” the screen like a human |
| Agents can call named actions without vision | Work to expose stable actor names / `PlayerUiFacade` |

**Agent-as-player shape:** Scripted scenarios in Java or YAML:

```yaml
# docs/specs/player-scenarios/cm1_one_match.yaml (concept)
- action: open_squad
- action: select_best_xi
- action: open_tactics
- action: set_formation
  args: { id: "4-4-2" }
- action: continue_until_match
- action: kick_off
- action: assert_result_visible
```

An AI agent **writes/maintains scenarios**; the **runner** executes clicks — agent is not in the hot loop every CI run.

**Effort:** Medium (Platform + Match/Sim). **Release:** Propose **R-1888.1 or R-1892**, not Tier A for R-1888.

### Option C — Player Action Facade (engine + UI adapters)

**Idea:** One interface agents and tests share:

```text
PlayerSession
  continueDay()
  openSquad()
  setLineup(ids)
  kickOff()
  saveGame(slot)
```

- **Headless adapter** → today’s harness (engine only)  
- **Ui adapter** → Option B Scene2D clicks  
- **Vision adapter** (optional) → screenshot + computer-use  

**Pros:** Agents “behave as players” at the **intent** level; backends swap.  
**Cons:** Requires disciplined UI refactor (name actors, thin controllers).  

**This is the durable architecture** if we invest in agent-player QA.

### Option D — Vision / computer-use agents (Claude-like)

**Idea:** Launch `DesktopLauncher`, capture window, multimodal model decides clicks.

| Pros | Cons |
|------|------|
| Closest to “watch screens and click” | Expensive tokens; slow; flaky on CI |
| Finds “weird” UX no one scripted | No accessibility tree → pixel only |
| Good for **nightly exploratory** | Bad as merge gate |

**When:** Optional **L5 Exploratory** after L2.5 exists — never blocks merge. Human still owns feel.

### Option E — Record / playback (gdx.automation style)

Record human input once; replay in tests. Useful for demos; brittle for layout changes. **Lower priority** than B/C.

---

## 5. Recommended testing pyramid extension

```
                         ┌──────────────────────────┐
                         │ L5 Agent vision (opt.)   │  exploratory nightly
                         │ screenshot + computer-use│  NEVER merge gate
                         └────────────┬─────────────┘
                                      │
                         ┌────────────▼─────────────┐
                         │ L4 Human (B-9)           │  feel, authenticity
                         └────────────┬─────────────┘
                                      │
                         ┌────────────▼─────────────┐
                         │ L2.5 Scene2D player      │  PROPOSED — named UI actions
                         │ (PlayerUiFacade clicks)  │  catches Continue / history / save UI
                         └────────────┬─────────────┘
                                      │
                         ┌────────────▼─────────────┐
                         │ L3 Long / soak           │  exists
                         ├──────────────────────────┤
                         │ L2 Engine E2E (harness)  │  exists — NOT GUI
                         ├──────────────────────────┤
                         │ L1 Unit                  │  exists
                         └──────────────────────────┘
```

**Naming clarity for stakeholders:**

| Say | Mean |
|-----|------|
| “E2E tests pass” (today) | Engine loop OK |
| “UI player tests pass” (future L2.5) | Screens opened; buttons clicked in test JVM |
| “Agent played a match” (future) | Scenario runner or vision agent completed CM1 path |
| “Human QA” | Still required for Victorian feel and odd bugs |

---

## 6. How agents should “behave as players” (design principles)

1. **Prefer intent over pixels** — `continueDay` not `click(1420, 980)`.  
2. **Deterministic runner in CI** — LLM may **author** scenarios; CI **replays** them without an LLM.  
3. **Stable names** — every critical control gets `actor.setName("continue")` or registry entry.  
4. **Same scenarios, two backends** — headless engine vs UI facade; assert shared outcomes (date, score, save file).  
5. **Vision last** — API → Scene2D → vision → human ([cost hierarchy](https://artificialcuriositylabs.ai/posts/api-first-computer-use-last/)).  
6. **Do not block R-1888** on L2.5 — human B-9 remains the UI gate for first release.

---

## 7. Suggested delivery slices (when we implement)

| Slice | Deliverable | Unlocks |
|-------|-------------|---------|
| **S0** | Doc only (this analysis) | Shared vocabulary |
| **S1** | `PlayerUiFacade` + name Continue / Squad / History / Save | Agent-readable actions |
| **S2** | 3–5 L2.5 tests: Continue×10 = +10 days; history opens; save roundtrip via UI | Catches A-3/A-4/A-5 class bugs forever |
| **S3** | YAML/JSON player scenarios + runner | Agents author playbooks |
| **S4** | Optional L5 vision smoke (manual/nightly) | Exploratory “Claude plays Futtoboru” |

**R-1888:** S0 only.  
**R-1888.1 / early R-1892:** S1–S2 recommended.  
**Later:** S3–S4.

---

## 8. Risks if we skip this forever

| Risk | Impact |
|------|--------|
| UI bugs only found by scarce humans | C-1 / PL-5 class regressions return |
| Agents cannot verify their own UI changes | More “works in harness, broken on screen” |
| Vision-only agents tried first | High cost, low trust, abandoned |

---

## 9. PO / PM decisions (when ready — not urgent)

| # | Question | PM recommendation |
|---|----------|-------------------|
| P-1 | Keep R-1888 UI gate as **human B-9**? | **Yes** |
| P-2 | After R-1888, schedule **L2.5 Scene2D player** (S1–S2)? | **Yes** |
| P-3 | Invest in Claude-like vision as CI gate? | **No** — optional L5 only |
| P-4 | Build `PlayerSession` facade for agents? | **Yes** as part of S1 |

Record on [PO_BRIEF.md](PO_BRIEF.md) only when scheduling post-1888 work.

---

## 10. Related docs

| Doc | Role |
|-----|------|
| [LAYER_B_TESTING_PYRAMID_V1.md](LAYER_B_TESTING_PYRAMID_V1.md) | Current L1–L4 policy |
| [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) | Prior “no Sikuli” stance |
| [LAYER_B_TEST_PLAN_V1.md](LAYER_B_TEST_PLAN_V1.md) | B-9 human checklist |
| [PM_REPORT_PROJECT_STATUS_2026_07_12.md](PM_REPORT_PROJECT_STATUS_2026_07_12.md) | Current project status |

---

## 11. Changelog

| Date | Change |
|------|--------|
| 2026-07-12 | Initial analysis — harness ≠ GUI; Options A–E; L2.5/L5 proposal |

---

*Analysis only. No code in this commit. Implementation starts only after PO schedules S1 post–R-1888.*
