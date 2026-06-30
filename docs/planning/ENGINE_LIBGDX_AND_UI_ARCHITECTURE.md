# Engine, LibGDX & UI Architecture — Canonical Reference

**Document type:** Technical architecture (engine + UI)  
**Date:** 2026-06-27  
**Status:** **Active** — single source of truth for “how Futtoboru is coded”  
**Audience:** All engineers, PO (limitations section), Wave 1.5+ implementers  
**Supersedes scattered notes in:** `ANALYSIS_COMPREHENSIVE.md` §2, `PROJECT_MASTER_ANALYSIS.md` §3–4 (partial), outdated Platform dept §2

**Companion UI docs (not duplicated here):**
- [FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md](FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md) — colours, fonts, visibility rules  
- [ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md](ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md) — widget cookbook + examples  
- [SAVELOAD_ANALYSIS.md](SAVELOAD_ANALYSIS.md) — persistence deep dive  

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| **Is there one perfect doc today?** | **This file** — previously info was split across 5+ docs, some outdated. |
| **Stack** | Java **17**, LibGDX **1.12.0** (latest upstream **1.14.2** — see §2.1), VisUI **1.5.2**, Maven multi-module, LWJGL3 desktop. |
| **UI model** | LibGDX **Scene2D** + **VisUI** widgets; one `MainGameScreen` with **sidebar table switching** (`MainMenuManager`). |
| **Game model** | **Simulation-first**: `FuttoboruGameEngine` + managers mutate `SaveGame`; UI reads save and refreshes tables. |
| **Can we build CM/FM depth + Wave 1.5 (1892 expansion)?** | **Yes** on desktop — LibGDX is not the blocker. Blockers are **engine/data patterns** (save-as-truth, world evolution) and **UI refresh conventions**, not framework limits. |
| **Hard LibGDX limits** | Single-thread game loop, no native OS widgets, manual list virtualization at huge scale, JSON save quirks — all manageable with known patterns. |

---

## 2. Technology stack (verified from `pom.xml`)

### 2.1 Version matrix (2026-06-28)

| Component | Pinned in repo | Latest stable (upstream) | Upgrade notes |
|-----------|----------------|--------------------------|---------------|
| LibGDX | **1.12.0** | **1.14.2** | [Release notes](https://libgdx.com/news/2026/05/gdx-1-14-1); bump in separate **TECH-UPG-1** PR |
| VisUI | **1.5.2** | **1.5.4** (gdx 1.12.1) · **1.5.10** (gdx 1.14.1) | Match LibGDX row in [vis-ui CHANGES](https://github.com/kotcrab/vis-ui/blob/master/ui/CHANGES.md) |
| Java | **17** | 17 LTS | OK |

**UI scale (PU-10):** ✅ Complete — [PU-10_UI_SCALE_COMPLETION_V1.md](PU-10_UI_SCALE_COMPLETION_V1.md). In-game shell: fixed top bar + fixed left nav; `VisScrollPane` on content panel only.

| Layer | Choice | Location |
|-------|--------|----------|
| Language | Java 17 | Parent `pom.xml` |
| Game framework | LibGDX 1.12.0 | `libgdx.version` |
| UI extension | VisUI 1.5.2 | `vis.ui.version` |
| Desktop backend | LWJGL3 | `futtoboru-desktop/.../DesktopLauncher.java` |
| Headless tests | `gdx-backend-headless` | `futtoboru-core/pom.xml` |
| Build | Maven (`futtoboru-core`, `futtoboru-desktop`) | Root `pom.xml` |
| i18n | LibGDX `I18NBundle` + `LanguageModLoader` | `mods/language/` |
| Skin | Scene2D JSON `tixel.json` | `resources/skin/` |
| Persistence | LibGDX `Json` → save files | `SaveLoadSystem` |
| Tests | JUnit 5, `GameTestHarness`, `@Tag("soak")` | `src/test/.../harness/` |

**Not used:** LibGDX Ashley ECS, LibGDX 3D for match view (2D UI + headless sim today), GWT/HTML5 backend, MongoDB (evaluated — rejected for v1, see [SAVELOAD_ANALYSIS.md](SAVELOAD_ANALYSIS.md)).

---

## 3. Repository layout

```
futtoboru/
├── futtoboru-core/                 # All game logic + UI
│   └── src/main/java/com/rndmodgames/futtoboru/
│       ├── game/                   # Futtoboru extends LibGDX Game
│       ├── screens/                # Full-screen LibGDX Screen implementations (~10)
│       ├── menu/                   # Sidebar buttons + MainMenuManager
│       ├── tables/                 # In-game panels (*ScreenTable.java, ~35)
│       ├── engine/                 # Game loop, sim, scripts, finances, jobs
│       ├── data/                   # Domain POJOs (Serializable)
│       └── system/                 # DatabaseLoader, SaveGame, loaders, save/load
├── futtoboru-desktop/              # LWJGL3 launcher only
└── futtoboru-core/src/main/resources/
    ├── mods/                       # External data (seasons, clubs, language)
    └── skin/tixel.json             # VisUI / Scene2D skin
```

**Rule of thumb:** `screens/` = top-level app navigation; `tables/` = panels inside `MainGameScreen`; `engine/` = must run headless without UI.

---

## 4. LibGDX lifecycle (how the app runs)

### 4.1 Bootstrap

```
DesktopLauncher.main()
  → Lwjgl3Application(Futtoboru, config)
    → Futtoboru.create()
         AssetManager + PreferencesManager
         VisUI.load("skin/tixel.json")   // skipped if Gdx.gl null (tests)
         DatabaseLoader.getInstance()    // static mod registry
         ScriptsManager, AuthorityManager, FuttoboruGameEngine
         changeScreen(MENU_SCREEN)
    → loop: render(delta) on active Screen
```

Entry class: `com.rndmodgames.desktop.DesktopLauncher`  
Game class: `com.rndmodgames.futtoboru.game.Futtoboru` extends `com.badlogic.gdx.Game`

### 4.2 Screen vs in-game panel (critical distinction)

| Layer | Class pattern | Navigation |
|-------|---------------|------------|
| **App screens** | `*Screen.java` implements `Screen` | `Futtoboru.changeScreen(int id)` |
| **In-game panels** | `*ScreenTable.java` extends `VisTable` | `MainMenuManager.setActiveMainScreen(int id)` |

Only **one** LibGDX `Screen` is active during a match (`MainGameScreen`). Squad, inbox, competitions, etc. are **not** separate LibGDX screens — they are **tables swapped** inside `MainGameScreen`.

```
MainGameScreen
├── MainGameMenuTable     (top: Continue, Save, date)
├── MainMenuManager       (sidebar buttons)
└── parentTable           (active *ScreenTable content)
```

### 4.3 App screen IDs (`Futtoboru.java`)

| ID | Constant | Class |
|----|----------|-------|
| 2 | `MENU_SCREEN` | `MenuScreen` |
| 3 | `SETTINGS_SCREEN` | `SettingsScreen` |
| 10–13 | New game flow | `NewGameSeasonScreen` → … → `NewGameOverviewScreen` |
| 100 | `GAME_SCREEN` | `MainGameScreen` |
| 4–5 | Match debug / engine | `MatchEngineDebugScreen`, `MatchEngineScreen` |

**Known debt:** magic `int` IDs; `changeScreen` switch incomplete for some constants.

### 4.4 In-game panel IDs (`MainMenuManager.java`)

Examples: `HOME_SCREEN=100`, `INBOX_SCREEN=200`, `COMPETITIONS_SCREEN=5000`, `LEAGUE_DETAIL_SCREEN=501`, `LEAGUE_DRAW_SCREEN=10008`, job screens `10001+`.

Navigation: `MainMenuManager.setActiveMainScreen(id)` clears `parentTable`, sets `CURRENT_SCREEN`, adds the matching pre-built table instance.

---

## 5. UI architecture (Scene2D + VisUI)

### 5.1 Rendering pattern (every screen)

```java
Gdx.gl.glClearColor(0, 0, 0, 1);   // REQUIRED — see style guide
Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
stage.act();
stage.draw();
```

Each screen owns a `Stage` + `ScreenViewport`. Input: `InputMultiplexer` (stage + keyboard adapter).

### 5.2 Layout

- **Primary container:** `VisTable(true)` with `setFillParent(true)`  
- **Rows/columns:** `table.row()`, `.add(widget).grow().pad()`  
- **Skin:** global VisUI skin from `tixel.json` — no per-widget custom skin yet  

Full visual rules: [FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md](FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md)

### 5.3 Button input (project convention)

**Use `addCaptureListener` + `InputListener`**, not `ClickListener`, for reliable click handling in nested tables:

```java
button.addCaptureListener(new InputListener() {
    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (button == Buttons.LEFT) { /* action */ return true; }
        return false;
    }
});
```

Documented in `PATREON_UPDATE_DRAFT_2024_12_14.md`; used across menu buttons and job tables.

### 5.4 Dynamic UI refresh

When engine mutates `SaveGame`, UI must be told to rebuild:

| Mechanism | When |
|-----------|------|
| `MainMenuManager.updateDynamicComponents()` | Switch on `CURRENT_SCREEN`; calls table-specific updates |
| `*ScreenTable.updateDynamicComponents()` | Reload lists (inbox, squad, competitions) |
| `FuttoboruGameEngine.setMainMenuManager(...)` | Engine holds reference from `MainGameScreen` construction |

**Gap (bugs C-2/C-3):** load game / authority change does not always refresh all tables — fix in Platform dept, not LibGDX.

### 5.5 Adding a new in-game panel (checklist)

1. Create `tables/.../MyFeatureScreenTable.java` extending `VisTable`  
2. Add `public static final int MY_SCREEN = …` to `MainMenuManager`  
3. Instantiate table in `MainMenuManager` constructor  
4. Add `case MY_SCREEN:` in `setActiveMainScreen` and `updateDynamicComponents`  
5. Optional: sidebar `VisTextButton` in `setDynamicButtonsMenu`  
6. Follow style guide (black bg, white text)  
7. Add headless test via `GameTestHarness` if logic-heavy — **no Stage required**

Widget patterns: [ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md](ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md)

---

## 6. Simulation architecture (engine separate from UI)

### 6.1 Ownership

```
Futtoboru (Game)
├── currentGame: SaveGame          ← mutable truth during play
├── gameEngine: FuttoboruGameEngine
├── authorityManager: AuthorityManager
└── scriptsManager: ScriptsManager
```

**Static registry:** `DatabaseLoader` — mod files at startup only.  
**Live state:** `SaveGame` — clubs, persons, leagues, cups, messages, scripts, dates.

See [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) for incorporation rules (Wave 1.5).

### 6.2 Daily loop (`FuttoboruGameEngine.continueGame()`)

High-level order (simplified):

1. Gate: mandatory inbox / draw messages (`getNextGameAction()`)  
2. Advance `gameDate`  
3. `ScriptsManager.checkGameScripts()` — dated timeline events  
4. Match simulation, expenses, attribute snapshots  
5. `AuthorityManager` — league completion, cup, rollover  
6. UI refresh hooks (if `mainMenuManager` set)

**Headless proof:** `GameTestHarness` runs the same engine without any `Stage`.

### 6.3 Managers map

| Manager | Role |
|---------|------|
| `FuttoboruGameEngine` | Day advance, continue gate, orchestration |
| `AuthorityManager` | Leagues, cups, standings, rollover, draws |
| `ScriptsManager` | `BasicScript` types 1000/1001 (+ future 1002–1007) |
| `SeasonManager` | Season index, fixture archive, rules snapshot |
| `MessageManager` | Inbox delivery |
| `JobManager` / `ClubStaffManager` | Unemployed flow |
| `MatchSimulator` | Headless match resolution |

---

## 7. Data & persistence

| Concern | Implementation |
|---------|----------------|
| Mod data | Text files under `resources/mods/` via loaders |
| Save format | LibGDX `Json.toJson(SaveGame)` |
| Save path | User documents folder (see `SaveLoadSystem`) |
| Serialization | All `SaveGame` graph implements `Serializable`; watch LibGDX `Array` vs `ArrayList` (JSON roundtrip bug fixed in harness) |
| Versioning | **Not implemented** — WE-7 / DB-6 needed for long careers |

Scale today: O(n) list scans — fine for hundreds of clubs/players; index maps recommended before 10k+ rows (not a LibGDX limit — app design).

---

## 8. Testing without UI

| Tool | Purpose |
|------|---------|
| `gdx-backend-headless` | Initialize `Gdx.app` without OpenGL |
| `HeadlessGdxTestSupport` | Test bootstrap |
| `GameTestHarness` | New game, advance days, simulate seasons, save/load |
| `@Tag("soak")` + `-Psoak` | Long-run sim profile |

**Principle:** Gameplay and Wave 1.5 data logic **must not require GUI** for automated proof. UI is layered on top.

---

## 9. LibGDX / VisUI limitations vs Futtoboru goals

Honest assessment for PO and roadmap planning.

### 9.1 Not limiting (safe to plan)

| Goal | Verdict |
|------|---------|
| CM/FM-style 2D management UI (tables, inbox, forms) | ✅ Scene2D designed for this |
| Multiple leagues / divisions (1892 expansion UI) | ✅ More rows in `CompetitionsScreenTable` + league picker |
| Historical timeline scripts | ✅ Engine concern; UI shows inbox + detail screens |
| Desktop Windows/Mac/Linux | ✅ LWJGL3 |
| Headless season soak tests | ✅ Proven 60 seasons |
| i18n | ✅ `I18NBundle` wired |
| Custom skin / theming later | ✅ Replace `tixel.json` + atlas |
| Save/load full career | ✅ JSON works; add schema version |

### 9.2 Manageable constraints (plan for, not blockers)

| Constraint | Impact | Mitigation |
|------------|--------|------------|
| **Single game thread** | Long `continueGame()` blocks UI | Batch days in harness; optional progress `BusyBar`; eventually background sim for multi-day skip |
| **No native widgets** | File dialogs, system look | LibGDX file API / custom modals — already acceptable for game UX |
| **Large scroll lists** | 500+ club league tables lag if naive | `ScrollPane` + reuse actors; virtual lists for FM-scale (future) |
| **JSON save size** | Very long careers, big inbox | Compression, prune inbox, schema versioning |
| **LibGDX Json + generics** | `List.of()` immutable breaks load | Use `ArrayList`; test roundtrip (`SaveLoadIT`) |
| **VisUI maintenance** | Tied to LibGDX 1.12 | Pin versions; upgrade as coordinated bump — see §2.1 |
| **UI scale / HiDPI** | ✅ **PU-10-R3** — default 200%, overflow scroll | Manual QA on laptop |
| **Magic screen IDs** | Refactor cost | Enum/router v2 — not blocking features |

### 9.3 Real scope boundaries (choose consciously)

| Goal | LibGDX fit | Notes |
|------|------------|-------|
| **3D match viewer** | ⚠️ Possible (LibGDX 3D) | Separate module; current match is headless + optional 2D debug |
| **Mobile touch-first** | ⚠️ Needs UX pass | Desktop is primary; LibGDX supports Android/iOS |
| **MMO / multiplayer** | ❌ Not framework-level | Custom networking — out of scope |
| **In-game mod editor (FM Editor scale)** | ⚠️ Large UI effort | Feasible in VisUI but months of UI work — Git PR workflow first |
| **Web browser build** | ⚠️ GWT deprecated | Desktop-first PO decision reasonable |

**Verdict for Wave 1.5 (1892 league expansion):** No LibGDX showstopper. Work is **WorldEvolutionManager + scripts + competitions UI refresh**.

---

## 10. Wave 1.5 UI impact (1892 expansion preview)

When PO proceeds with [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) + 1892 content:

| Area | Likely UI change | LibGDX effort |
|------|------------------|---------------|
| Competitions | List **multiple** leagues (Div 1 + Div 2) | Low — extend existing table |
| League detail | Division tabs or selector | Low |
| Authority | Second league under FA | Low |
| Inbox | Expansion / election messages | Low — existing message system |
| Club browser | More clubs | Medium — scroll performance |
| Schedule | More fixtures | Low |
| Draw screens | Per-league draw gates | Medium — already have league draw pattern |
| Sidebar | Unchanged | — |

**No new LibGDX screen type required** — table extensions + `updateDynamicComponents` discipline.

---

## 11. Technical debt register (engine/UI)

| ID | Issue | Blocks Wave 1.5? |
|----|-------|------------------|
| TD-U1 | Magic screen int IDs | No |
| TD-U2 | `MainMenuManager` 1000+ line switch | No — but slows UI work |
| TD-U3 | Incomplete refresh on load (C-2/C-3) | **Yes** for manual QA |
| TD-U4 | Continue double-click (C-1) | QA annoyance |
| TD-U5 | `DatabaseLoader` used during sim | **Yes** for world evolution |
| TD-U6 | No save schema version | Long-save risk |
| TD-U7 | `PROJECT_MASTER_ANALYSIS` % outdated | Docs only |
| TD-U8 | UI scale clipping at high zoom | ✅ **Mitigated** — PU-10-R3/R3b |

Track UI bugs: [BUGS_AND_BLOCKERS_REGISTRY.md](BUGS_AND_BLOCKERS_REGISTRY.md)

---

## 12. Document map (read order for new developers)

| Order | Document | Purpose |
|------:|----------|---------|
| 1 | **This file** | Architecture + LibGDX limits |
| 2 | [FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md](FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md) | Visual rules |
| 3 | [ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md](ANALYSIS_VISUI_PATTERNS_AND_PROTOTYPE_V1.md) | Copy-paste widgets |
| 4 | [DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md](DESIGN_SEASON_MODEL_AND_SCRIPTS_V1.md) | Season loop |
| 5 | [DESIGN_IN_PLAY_WORLD_EVOLUTION.md](DESIGN_IN_PLAY_WORLD_EVOLUTION.md) | Wave 1.5 data model |
| 6 | [V1_TESTING_AND_SIMULATION_PLAN.md](V1_TESTING_AND_SIMULATION_PLAN.md) | Harness / CI |
| 7 | [SAVELOAD_ANALYSIS.md](SAVELOAD_ANALYSIS.md) | Persistence |

Official LibGDX wiki: [Scene2D](https://github.com/libgdx/libgdx/wiki/Scene2d), [Scene2D UI](https://github.com/libgdx/libgdx/wiki/Scene2d.ui), [Table](https://github.com/libgdx/libgdx/wiki/Table)

---

## 13. Changelog

| Date | Change |
|------|--------|
| 2026-06-27 | Initial canonical doc — consolidates scattered architecture notes |

---

*Next update: after Wave 1.5 kickoff or LibGDX version bump.*
