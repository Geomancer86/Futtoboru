# UI audit and Phase 3 plan (U-01 to U-05)

Written 2026-11-27 from `origin/develop` (WORKSTATION lane, `ws/U-audit`). Docs only, no code changed.
Scope: `futtoboru-core` UI code (`screens/`, `tables/`, `dialogs/`, `menu/`, `components/`,
`darkblade/screens/tables/`, `match/engine/MatchEngineScreen.java`) and `futtoboru-desktop`.
Dead 3D/Bullet demo code (P1-4) is left out of every count.
Paths below are relative to `futtoboru-core/src/main/java/com/rndmodgames/` unless they start with a module name.

> **Source note.** `docs/archive/darkblade-port/**` only covers DarkBlade's ECS engine. It does **not** contain
> DarkBlade's `UiTheme` or `GUIA_DE_INTERFAZ.md`. The proposals in sections 4 and 5 follow the ROADMAP's
> description of those files (a JSON theme with an accessor class, and an interface guide with one
> screenshot per UI item). If the real DarkBlade files are in the private repo, compare them before U-01 is coded.
> The archived `FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md` is an unverified 2025 draft. It got the black background
> and the `pad(5/10/20)` habit right, and section 2 measures both.

---

## 1. Screen inventory and navigation

### 1.1 How navigation works today

There are two layers:

1. **`game/Futtoboru.changeScreen(int)`** swaps libGDX `Screen`s. Each screen builds its own
   `Stage(new ScreenViewport())` and disposes itself in `hide()`.
2. **`menu/MainMenuManager.setActiveMainScreen(int)`** runs inside `MainGameScreen`. It clears one
   content `VisTable` and adds one of about 25 pre-built `*ScreenTable`s. The table is picked by an int constant
   (100 to 10010). The left button column depends on the owner's profession id: `2` is unemployed,
   `3` is manager. Messages can also jump to a table through `message.getActionScreen()`
   (`tables/inbox/InboxScreenTable.java:506,515`).

```
DesktopLauncher (1920x1080 window, then create() resizes it to the saved RESOLUTION, 640x480 by default)
└─ MenuScreen
   ├─ New Game ──> NewGameSeasonScreen (NEW_GAME_SCREEN maps here)
   │               ├─ Create Manager ──> NewManagerScreen ──(save)──> NewGameSeasonScreen
   │               └─ Start ──> NewGameSetupScreen ⇄ NewGameOverviewScreen ──> MainGameScreen
   ├─ Load Game ──> LoadGameDialog (VisDialog) ──> MainGameScreen
   ├─ Settings ──> SettingsScreen (Sound, Mods/Language, Resolution/Fullscreen, Debug logging) ──> Menu
   ├─ Match Engine Debug ──> MatchEngineDebugScreen ──> MatchEngineScreen (2D viewer) ──> back
   └─ Quit
MainGameScreen = MainGameMenuTable (top bar) + left button column + content table
   top bar: Options select box [Save | Load | Settings | Quit to menu | Quit to desktop]
            (only Save is implemented; the rest log "OPTION NOT IMPLEMENTED!", MainGameMenuTable.java:139)
            date widget · Continue / Match preview / Match result / "INBOX -> DRAW" button · busy bar
   left column, unemployed: Home, Inbox, Personal details, Authorities, Club browser, Job board, My applications
   left column, manager:    Home, Inbox, Squad | Schedule, Match history, Competitions | Club info, Finances,
                            Match Engine Debug | Personal details, Authorities
   drill-downs: Competitions → League detail / Cup detail → Cup playoffs; Squad → Player detail;
                Club browser → Club detail; My applications → Job offer ⇄ Negotiation; Inbox → Draw screens
   F12: screenshot (SharingUtils.screenshot) + toast
```

### 1.2 Screens (`implements Screen`)

| Class | Purpose | Reached from | Status |
|---|---|---|---|
| `screens/MenuScreen` | Main menu, build info, social links | start, `MainMenuButton` | working |
| `screens/NewGameSeasonScreen` | Pick season, pick or create manager | Menu → New Game | working |
| `screens/NewManagerScreen` | Create a manager (name, birthplace) | Season screen | working |
| `screens/NewGameSetupScreen` | Pick countries / leagues | Season screen, Overview (back) | working |
| `screens/NewGameOverviewScreen` | Summary, pick a club, start | Setup screen | working (1013 lines) |
| `screens/MainGameScreen` | In-game shell (top bar + menu + content) | Overview, Load dialog | working, `setDebug(true)` on |
| `screens/SettingsScreen` | Resolution, fullscreen, language, logging | Menu | working (U-08: sound and empty gameplay panels hidden) |
| `screens/MatchEngineDebugScreen` | Set up a test match for the 2D viewer | Menu | dev tool (1324 lines, `placeholder` data fallbacks) |
| `match/engine/MatchEngineScreen` | 2D match viewer (ShapeRenderer, `FitViewport`) | Debug screen | dev tool, only a viewer (D5) |
| `screens/NewGameScreen` | Old new-game screen | **nothing** (`NEW_GAME_SCREEN` opens `NewGameSeasonScreen`) | orphan: delete |
| `screens/SplashScreen` | Splash with a red clear colour | **nothing** (commented out, `Futtoboru.java:209`) | orphan: delete or redo |

### 1.3 Content tables (`MainMenuManager`) and widgets

| Class | Purpose | Reached from | Status |
|---|---|---|---|
| `tables/main/HomeScreenTable` | Home dashboard | Home button (default screen) | **stub**: one button reading "HOME screen placeholder" |
| `tables/inbox/InboxScreenTable` | Messages, categories, actions | Inbox button, top bar | working |
| `tables/person/PersonDetailsScreenTable` | Owner's personal details | Personal details button | working |
| `tables/authority/AuthorityScreenTable` | Football authorities and leagues | Authorities button | partial (TODO: season is hard-coded, line 145) |
| `tables/squad/SquadScreenTable` + `PlayersListTable` | Squad list | Squad button | working |
| `tables/player/PlayerDetailScreenTable` | Player attributes, contract | Squad → player | working |
| `tables/schedule/ScheduleScreenTable` + `FixturesTable`, `ArrangeFriendlyTable` | Fixtures, friendlies | Schedule button | working (U-08: arrange, letter answer, play, gate) |
| `tables/match/preview/MatchPreviewScreenTable` | Before a match | top bar | working |
| `tables/match/result/MatchResultScreenTable` | After a match | top bar | working |
| `tables/match/history/MatchHistoryScreenTable` | Past results | Match history button | working |
| `tables/competitions/CompetitionsScreenTable` | List of leagues and cups | Competitions button | working |
| `tables/competitions/LeagueDetailScreenTable` | League table | Competitions | working |
| `tables/competitions/CupDetailScreenTable`, `CupPlayoffsTable` | Cup rounds and bracket | Competitions | working |
| `tables/draw/LeagueDrawScreenTable`, `CupDrawScreenTable` | Mandatory fixture and cup draws | "INBOX -> DRAW" button, inbox | working |
| `tables/club/ClubInfoScreenTable` | Own club, ground, finances summary | Club info button | working |
| `tables/finances/FinancesScreenTable` | Income and expenses (shows `$`, P2-7 is about `£`) | Finances button | working |
| `tables/jobs/ClubBrowserScreenTable`, `club/ClubDetailScreenTable` | Browse clubs, apply | unemployed menu | working, `setDebug(true)` on |
| `tables/jobs/JobBoardScreenTable`, `MyApplicationsScreenTable`, `JobOfferScreenTable`, `NegotiationScreenTable` | Job system | unemployed menu | working, `setDebug(true)` on |
| `tables/match/engine/MatchEngineDebugScreenTable` | In-game debug panel | manager menu, "Match Engine Debug" | **placeholder**: a disabled button reading "Match Engine Coming Soon" |
| `menu/topmenu/MainGameMenuTable` | Top bar | MainGameScreen | partial: 4 of 5 options are not implemented |
| `tables/widgets/CurrentDateAndTimeWidget` | Game date | top bar | working |
| `tables/settings/DebugLoggingSettingsTable` | Log categories | Settings | working |
| `darkblade/screens/tables/ModsSettingsTable` | Language select | Settings | working |
| `darkblade/screens/tables/SoundSettingsTable` | Volume sliders | Settings | hidden (U-08) until the game has audio; class kept |
| `dialogs/LoadGameDialog`, `SaveGameDialog` | Load / save (VisDialog) | Menu / top bar | working (save has TODOs: name, overwrite) |
| `components/*` | Menu buttons, `CountryAndCitySelectBox`, `AutoFocusScrollPane`, `SocialNetworkLinksComponent` | various | working |

### 1.4 The 11 stub screens (ROADMAP U-04)

All 11 are in `tables/main/`. Each is about 34 lines with a single `VisTextButton("<X> screen placeholder")`.
**Only `HomeScreenTable` is reachable.** The other ten are not referenced anywhere in the code.

| # | Class | Reachable | Recommendation (see U-04) |
|---|---|---|---|
| 1 | `HomeScreenTable` | yes, the default in-game screen | **finish**: a small dashboard built from existing data |
| 2 | `SquadScreenTable` (`tables/main`) | no; duplicates the working `tables/squad/SquadScreenTable` | **delete** |
| 3 | `TacticsScreenTable` | no | keep hidden; build with engine v2 (formation and roles feed the match) |
| 4 | `TrainingScreenTable` | no | keep hidden; build later (1888 clubs did little formal training) |
| 5 | `StaffScreenTable` | no | keep hidden; build later (secretary, trainer, umpire) |
| 6 | `BoardScreenTable` | no | keep hidden; build later (committee expectations) |
| 7 | `TeamReportScreenTable` | no | delete; Club info and Squad already cover it |
| 8 | `ScoutingScreenTable` | no | delete for now (anachronistic for 1888) |
| 9 | `TransfersScreenTable` | no | delete for now; a "registrations" screen can come later |
| 10 | `BTeamScreenTable` | no | delete (anachronistic) |
| 11 | `Under18TeamScreenTable` | no | delete (anachronistic) |

There are also two empty classes, `tables/authority/ConfederationsTable` and `tables/authority/LeagueTable` (5 lines,
no body), the unused skin `resources/uiskin.{json,atlas,png}` (nothing loads it), and a dead `MainMenuManager`
constant pair (`LEAGUE_SCREEN = 500`, `WORLD_SCREEN = 600`, private and never used).

---

## 2. Hard-coded styling: counts

The measurements come from a regex scan of the UI scope. Comments are skipped.

### 2.1 Summary

| Category | Count | Files | Notes |
|---|---|---|---|
| `setDebug(true)` | **12** active (+1 commented) | 8 | Two are on the shell (`MainGameScreen`, `MainMenuManager`), so debug lines show on **every** in-game screen |
| Colour literals | **80** | 22 | 11 `glClearColor`, 13 `Color.*` constants, 56 `setColor(r,g,b,a)` with numbers. 6 of the 56 are ShapeRenderer calls in the 2D viewer |
| Distinct literal RGBA values | about 30 | | They map onto about 20 semantic tokens (4.3) |
| `setFontScale(x)` | **36** | 15 | 6 values: 1.5 ×4, 1.3 ×5, 1.2 ×12, 1.1 ×9, 1.05 ×4, 0.9 ×2 |
| Other fonts | 1 | 1 | `new BitmapFont()` (the libGDX built-in default font) in `MatchEngineScreen.java:115` |
| Pixel paddings `pad*(n)` | **330** | 31 | 5 ×138, 10 ×93, 2 ×54, 20 ×24, 0 ×8, 50 ×6, 15 ×6, 100 ×1 |
| Pixel sizes `width/height/...(n)` | **216** | 23 | 150 ×67, 200 ×28, 100 ×22, 10 ×18, 120 ×16, 70 ×14, 40 ×12, 80 ×8 … |
| `new VisTable(true)` / `new VisTable()` | 92 / 14 | 38 / 8 | Spacing is mixed: VisUI default spacing in some tables, none in others |
| Explicit skin style names | **0** | 0 | Every widget uses the `default` style. Only `LinkLabel` picks up `link-label` (social links) |
| Tooltips | 0 | 0 | |
| Windows / dialogs | 4 | 4 | `LoadGameDialog`, `SaveGameDialog`, plus ad-hoc `VisDialog` in `ClubDetailScreenTable:552` and `JobBoardScreenTable:293` |
| Unused per-table `Stage` | 12 | 12 | Each stub and several tables build `new Stage(new ScreenViewport())` they never use. This wastes a SpriteBatch |
| Hard-coded English labels | about 283 `new VisLabel/VisTextButton("...")` | | Compared with 124 `LanguageModLoader.getValue(...)` calls. i18n is out of Phase 3 scope, but U-02 touches the same lines |

### 2.2 `setDebug(true)`

| File | Lines |
|---|---|
| `futtoboru/menu/MainMenuManager.java` | 187 (`parentTable`: the content area of every in-game screen) |
| `futtoboru/screens/MainGameScreen.java` | 76 (`gameWindowTable`: the whole in-game window) |
| `futtoboru/tables/club/ClubDetailScreenTable.java` | 49 |
| `futtoboru/tables/jobs/ClubBrowserScreenTable.java` | 43, 58 |
| `futtoboru/tables/jobs/JobBoardScreenTable.java` | 46, 67 |
| `futtoboru/tables/jobs/JobOfferScreenTable.java` | 44, 58 |
| `futtoboru/tables/jobs/MyApplicationsScreenTable.java` | 40, 54 |
| `futtoboru/tables/jobs/NegotiationScreenTable.java` | 40 |
| `components/CountryAndCitySelectBox.java` | 35 (commented out) |

### 2.3 Colour literals per file

| File | # | Lines (value) |
|---|---|---|
| `tables/inbox/InboxScreenTable.java` | 15 | 144,166,191,283 (0.5,0.8,1 selected) · 272 (0.2,0.6,1 unread) · 274 (0.5 grey read) · 286 (0.9 grey) · 289 (0.6 grey) · 727,743 `WHITE` · 732 `CYAN` · 734 `GOLD` · 736 `ORANGE` · 738 `GREEN` · 740 `MAGENTA` (message categories) |
| `tables/player/PlayerDetailScreenTable.java` | 13 | 333 orange / 336 red (contract expiring) · 523,525 green/red · 641–651 attribute rating tiers (gold, green, light green, white, light orange, light red) · 662–666 trend up/down/flat |
| `match/engine/MatchEngineScreen.java` | 8 | 126 `WHITE` · 176 (0.2,0.6,1 home) · 183 white · 190,447 (0.2,0.7,0.2 grass) · 263 (1,0.2,0.2 away) · 449 white · 442 clear |
| `tables/finances/FinancesScreenTable.java` | 6 | 80,92 green profit · 82,94,196 red loss/expense · 279 (0.7 grey, "not implemented") |
| `tables/club/ClubDetailScreenTable.java` | 6 | 182 green asset · 211,229 (0.3,0.5,1 link) · 223 (0.5,0.7,1 link hover) · 271 red · 273 green |
| `tables/competitions/CupPlayoffsTable.java` | 5 | 138 `GOLD` (winner) · 205,207 `GREEN` (match winner) · 212,213 `GRAY` (teams not yet known) |
| `screens/MatchEngineDebugScreen.java` | 5 | 168,430 (0.7 grey) · 457 white · 599 (0.8,0.8,0.6) · 1294 clear |
| `tables/club/ClubInfoScreenTable.java` | 4 | 150 green asset · 179,197 link · 191 link hover (same values as ClubDetail) |
| `tables/competitions/CompetitionsScreenTable.java` | 2 | 112,167 (0.7,0.7,1 section heading) |
| `tables/jobs/MyApplicationsScreenTable.java` | 2 | 119 green / 121 red (application status) |
| `tables/squad/PlayersListTable.java` | 2 | 409 green / 411 red |
| `screens/MenuScreen.java` | 2 | 175 (0.7 grey build info) · 200 clear |
| `tables/competitions/CupDetailScreenTable.java` | 1 | 211 (0.7,0.7,1 heading) |
| `tables/draw/CupDrawScreenTable.java` | 1 | 422 (0.7,0.9,0.7 opponent) |
| `glClearColor(0,0,0,1)`, one per screen | 9 | `MainGameScreen:173`, `NewGameOverviewScreen:864`, `NewGameScreen:153`, `NewGameSeasonScreen:300`, `NewGameSetupScreen:264`, `NewManagerScreen:309`, `SettingsScreen:370` (+ Menu, Debug and Viewer above) |
| `SplashScreen.java` | 1 | 27 `glClearColor(1,0,0,1)` (red) |

### 2.4 Font scales per file

| File | # | Lines |
|---|---|---|
| `screens/MatchEngineDebugScreen.java` | 12 | 182 (1.5), 187 (0.9), 218, 254, 378, 812, 919, 1046 (1.2), 431 (0.9), 456, 946, 994 (1.1) |
| `tables/finances/FinancesScreenTable.java` | 5 | 65 (1.2), 105, 160 (1.1), 112, 170 (1.05) |
| `tables/club/ClubInfoScreenTable.java` | 4 | 56 (1.2), 92, 212 (1.1), 161 (1.05) |
| `tables/club/ClubDetailScreenTable.java` | 2 | 124 (1.1), 193 (1.05) |
| `tables/match/result/MatchResultScreenTable.java` | 2 | 75 (1.5), 130 (1.2) |
| `tables/settings/DebugLoggingSettingsTable.java` | 2 | 40 (1.2), 112 (1.1) |
| title labels, 1 each | 9 | `CompetitionsScreenTable:78` (1.2), `CupDetailScreenTable:105` (1.3), `CupPlayoffsTable:90` (1.2), `LeagueDetailScreenTable:98` (1.3), `CupDrawScreenTable:395` (1.3), `LeagueDrawScreenTable:174` (1.3), `MatchEngineDebugScreenTable:47` (1.5), `MatchHistoryScreenTable:96` (1.3), `PlayerDetailScreenTable:85` (1.5) |

Screen titles use 1.2, 1.3 or 1.5 at random. Scaling a 13 px bitmap font with the atlas filter on
`Nearest,Nearest` (`tixel.atlas`) gives uneven glyphs at every non-integer scale.

### 2.5 Paddings and sizes per file (top files; full line lists can be regenerated with the scan in 2.6)

| File | `pad*(n)` | sizes |
|---|---|---|
| `screens/MatchEngineDebugScreen.java` | 43 | 22 |
| `screens/SettingsScreen.java` | 36 | 0 |
| `tables/player/PlayerDetailScreenTable.java` | 29 | 21 |
| `tables/competitions/LeagueDetailScreenTable.java` | 27 | 27 (the league table column widths, 201–210, 223–232) |
| `tables/finances/FinancesScreenTable.java` | 23 | 21 |
| `tables/match/result/MatchResultScreenTable.java` | 22 | 0 |
| `tables/competitions/CupDetailScreenTable.java` | 19 | 8 |
| `tables/competitions/CompetitionsScreenTable.java` | 16 | 13 |
| `tables/draw/CupDrawScreenTable.java` | 16 | 6 |
| `tables/club/ClubInfoScreenTable.java` | 14 | 1 |
| `tables/draw/LeagueDrawScreenTable.java` | 12 | 8 |
| `tables/squad/PlayersListTable.java` | 0 | 20 (squad column widths, 123–169) |
| `tables/inbox/InboxScreenTable.java` | 8 | 16 |
| `tables/jobs/JobBoardScreenTable.java` | 0 | 12 |
| 17 more files | 1–10 each | 1–8 each |
| `menu/topmenu/MainGameMenuTable.java` | 1 (273) | 4 (busy bar 150×4 at 272–273, date widget width 100 at 276) |
| `screens/MainGameScreen.java` | 1 (78) | 1 (`toastManager.setScreenPadding(10, 60)` at 115) |
| `futtoboru-desktop/.../DesktopLauncher.java` | 0 | 1 (`setWindowedMode(1920, 1080)` at 77) |

### 2.6 Reproducing the counts

```sh
grep -rn "setDebug(true" futtoboru-core/src/main/java
grep -rnoE "Color\.[A-Z_]+|new Color\(|setColor\([0-9.f, ]+\)|glClearColor\(" futtoboru-core/src/main/java   # minus graphics/, scenes/, Bullet*
grep -rn "setFontScale" futtoboru-core/src/main/java
grep -rnE "\.pad(Top|Bottom|Left|Right)?\([0-9]" futtoboru-core/src/main/java
```
U-02 should add these as a CI check, a small `tools/ui/lint_ui.sh` that fails on new hits outside `ui/UiTheme.java`.

---

## 3. Skin, fonts, resolution today

- **Skin:** `Futtoboru.create()` calls `VisUI.load(Gdx.files.internal("skin/tixel.json"))`, guarded by `Gdx.gl != null` for
  headless tests, and `VisUI.dispose()` runs in `dispose()`. Tixel skin (VisUI-derived, Apache 2.0, D12):
  `tixel.json`, `tixel.atlas` (512×128, `filter: Nearest,Nearest`), `tixel.png`.
- **Font:** one bitmap font, `droid-sans-13.fnt` (Droid Sans 13 px, lineHeight 13, glyphs packed into `tixel.atlas`).
  `default-font` and `small-font` are **the same font** (`small-font: default-font`). No FreeType at runtime,
  although `gdx-freetype-platform` is still in `futtoboru-core/pom.xml:127`. P1-4 plans to remove it.
- **Skin colours already named** (`tixel.json` `Color` section): `t-white`, `t-black`, `t-highlight` (0.85,0.5,0.05 orange),
  `t-highlight-medium`, `t-highlight-dark`, `t-warn` (red), `t-light` (0.4), `t-medium` (0.2), `t-medium-dark` (0.15),
  `t-dark` (0.1), `t-alpha`, with aliases `white/black/grey/vis-blue/vis-red/menuitem/link-label`. Screens use none of them.
- **Styles available but unused:** `VisTextButton` `blue`/`toggle`/`menu-bar`, `Label` `small`/`link-label`/`menuitem-shortcut`,
  `Window` `noborder`/`dialog`, `BusyBar` `slim`/`thick`, `Tooltip` `default`, `TabbedPane`, `MenuBar`/`PopupMenu`.
- **VisUI `Sizes`:** `default` (scaleFactor 1) and `x2` (scaleFactor 2) both exist in `tixel.json:179-182`, but
  `VisUI.load(FileHandle)` always uses `default`.
- **Resolution:** `DesktopLauncher` opens 1920×1080. Then `Futtoboru.create()` → `setScreen()` resizes the window to
  `RESOLUTION`: the saved preference, or `"640x480"` as the code default. The resolution list is
  `resources/mods/resolutions.txt` (640×480 to 2560×1440), picked in `SettingsScreen`. Fullscreen uses the
  monitor's display mode.
- **Scaling:** none. Every UI stage uses `new ScreenViewport()` with the default `unitsPerPixel = 1` (22 screen viewports).
  One unit is one pixel, so the UI keeps a fixed pixel size: 13 px text on a 2560×1440 or fullscreen 4K monitor, and
  `width(150)` columns overflow at 640×480. No HiDPI mode is set (`Lwjgl3ApplicationConfiguration` default).
  `SettingsScreen.java:63` already says "TODO: padding relative to resolution".
- **Background:** the black `glClearColor` is repeated in every `render()`. There is no shared base screen class.

---

## 4. Proposal

### 4.1 Design rules (what "done" means for Phase 3)

1. Screens contain **no** colour, font-scale or pixel literals. Everything comes from `UiTheme` tokens.
2. Every screen extends one `BaseScreen` (stage, viewport, clear colour, resize, dispose).
3. One menu: the left column in game, the main menu outside. Nothing reachable is a placeholder.
4. One tooltip layer, one modal window at a time (U-05).
5. The skin is unchanged (D12). Tokens point at skin colours and styles where they exist, so a later own skin only changes JSON.

### 4.2 U-01: theme tokens JSON

File: `futtoboru-core/src/main/resources/ui/theme.json`. This is not under `gamedata/` or `mods/`, so it stays
outside BOOK3PRO's lock. Schema (validated by a unit test, no GL needed):

```jsonc
{
  "schemaVersion": 1,
  "palette": {                 // raw colours, hex RRGGBB or RRGGBBAA; the only place hex appears
    "black": "000000", "white": "FFFFFF", "...": "..."
  },
  "colors": {                  // semantic tokens → palette key or skin colour ("skin:t-highlight")
    "bg.screen": "black", "text.primary": "white", "...": "..."
  },
  "fonts": {                   // role → skin font + scale (U-03 swaps scale for real font files)
    "body":    { "font": "default-font", "scale": 1.0 },
    "title":   { "font": "default-font", "scale": 1.0, "file": null }
  },
  "spacing": { "none": 0, "xxs": 2, "xs": 5, "sm": 10, "md": 15, "lg": 20, "xl": 50 },
  "sizes":   { "col.label": 150, "...": 0 },
  "styles":  { "button.primary": "blue", "button.toggle": "toggle", "label.link": "link-label" },
  "scaling": { "baseWidth": 1280, "baseHeight": 720, "steps": [1.0, 2.0], "default": "auto" }
}
```

**Starter token set.** It comes from the colours actually in use (section 2.3). Duplicate near-equal values are merged.

| Token | Value | Replaces |
|---|---|---|
| `bg.screen` | `000000` | 11 `glClearColor` (drop the red splash) |
| `text.primary` | `FFFFFF` | `Color.WHITE`, `setColor(1,1,1,1)` |
| `text.secondary` | `B3B3B3` (0.7) | Menu build info, Debug screen, Finances "not implemented" |
| `text.muted` | `808080` (0.5) | Inbox read indicator, `Color.GRAY` (0.5) in CupPlayoffs |
| `text.read` | `999999` (0.6) | Inbox read titles |
| `text.unread` | `E6E6E6` (0.9) | Inbox unread titles |
| `text.heading` | `B3B3FF` (0.7,0.7,1) | Competitions and CupDetail section headings |
| `accent` | `skin:t-highlight` (`D9800D`) | (new; the skin's own highlight, for selection and focus) |
| `state.positive` | `33FF33` (0.2,1,0.2) | profit, asset value, win, application accepted (also replaces `00FF00` and `Color.GREEN`) |
| `state.negative` | `FF3333` (1,0.2,0.2) | loss, expense, decline (also replaces `FF0000`) |
| `state.warning` | `FF8000` (1,0.5,0) | contract under 90 days |
| `state.winner` | `FFD700` (`Color.GOLD`) | cup winner, champion |
| `link.normal` / `link.hover` | `4D80FF` / `80B3FF` | ground source URL (ClubInfo, ClubDetail) |
| `selection` | `80CCFF` (0.5,0.8,1) | Inbox selected message |
| `indicator.unread` | `3399FF` (0.2,0.6,1) | Inbox unread dot |
| `draw.opponent` | `B3E6B3` (0.7,0.9,0.7) | CupDraw second team |
| `rating.exceptional/excellent/good/average/below/poor` | `FFD700` / `33FF33` / `80FF80` / `FFFFFF` / `FFCC80` / `FF8080` | PlayerDetail 641–651 |
| `trend.up/down/flat` | `33FF33` / `FF3333` / `CCCCCC` | PlayerDetail 662–666 |
| `category.league/cup/authority/match/job/system` | `00FFFF` / `FFD700` / `FFA500` / `33FF33` / `FF00FF` / `FFFFFF` | Inbox `getCategoryColor` |
| `pitch.grass/line/home/away` | `33B333` / `FFFFFF` / `3399FF` / `FF3333` | 2D viewer (dev tool, low priority) |
| `debug.note` | `CCCC99` | Debug screen only |

Fonts: `display` 1.5, `title` 1.3, `heading` 1.2, `subheading` 1.1, `body` 1.0, `small` 0.9. Drop 1.05 and use
`subheading`. Titles become `title`, so the random 1.2/1.3/1.5 choice goes away.
Spacing: `xxs 2`, `xs 5`, `sm 10`, `md 15`, `lg 20`, `xl 50`. Screen margin is `lg`, section gap `sm`, cell gap `xs`.
Sizes: `col.label 150`, `col.value 80`, `col.short 40`, `col.mid 70`, `col.wide 200`, `col.number 100`, `col.long 120`,
`col.xwide 300`, `indicator 20`, `busybar.w 150`, `busybar.h 4`, `widget.date 100`, `toast.padX 10`, `toast.padY 60`,
plus named column sets for the two big tables (`table.league.*`, `table.squad.*`) instead of 20+ literals each.

**`UiTheme` accessor** (new package `com.rndmodgames.futtoboru.ui`):

```java
public final class UiTheme {
    public static void load(FileHandle themeJson);          // in Futtoboru.create(), right after VisUI.load(...)
    public static Color color(String token);                // UiTheme.color("state.positive"); unknown token → magenta + one log line
    public static float space(String token);                // table.add(x).pad(UiTheme.space("sm"))
    public static float size(String token);                 // .width(UiTheme.size("col.label"))
    public static String style(String token);               // new VisTextButton(t, UiTheme.style("button.primary"))
    public static float fontScale(String role);             // until U-03 ships real fonts
    public static VisLabel label(String text, String role, String colorToken); // the one-liner screens use
    public static float uiScale();                          // U-03
    public static void clearScreen();                       // glClearColor(bg.screen)
}
// plus constants for the common keys, e.g. UiTheme.C_POSITIVE = "state.positive", to catch typos at compile time
```

Test: `UiThemeTest` parses `theme.json`, checks every `colors` value resolves (palette or a `skin:` name listed in
`tixel.json`), and checks that every `UiTheme.color("...")` / `space("...")` literal found in `src/main/java` exists in the JSON.

**U-01a implemented (2026-11-27, `ws/U-01a-ui-theme`).** `resources/ui/theme.json` holds the starter tokens above,
plus `label.small` in `styles` and five `table.league.*` / four `table.squad.*` column sizes. `ui/UiTheme` loads it
in `Futtoboru.create()` (`UiTheme.loadDefault()`, no GL needed) and offers `color`, `space`, `size`, `style`,
`fontScale`, `font` and `clearScreen`. Two changes from the sketch above: an unknown token **throws**
`IllegalArgumentException` (no magenta fallback), and loading is strict (unknown or missing fields, duplicate keys,
bad hex, unknown palette/skin colour, font or style names all throw). Merged colours to keep in mind in U-02c/d:
pure `00FF00`/`FF0000` (PlayersList, PlayerDetail, MyApplications, ClubDetail) become `state.positive`/`state.negative`
(`33FF33`/`FF3333`), PlayerDetail gold `FFD600` becomes `FFD700`, and `Color.GRAY` (`7F7F7F`) becomes `808080`.
`label(...)`, `uiScale()` and key constants are left for U-02c and U-03a.

**U-02b implemented (2026-11-27, `ws/U-02b-tokens`).** All UI colour, clear, font-scale, padding and size literals
now come from `UiTheme` (the save/load dialogs under the P2-5 lock are allow-listed). No colour merges after all:
palette hex values are what libGDX packs for the old floats (`(int) (f * 255)`, so 0.7 is `B2`, 0.5 is `7F`,
which also equals `Color.GRAY`), and pure `00FF00`/`FF0000` and `FFD600` got their own palette entries and
tokens (`status.*`, `application.*`, `modifier.*`, `match.winner`, `state.critical`, `trend.up/down`,
`rating.excellent/exceptional`). Lint: `UiThemeTest.noStyleLiteralsInUiSources` (JUnit, so CI runs it) fails on
new literals in the UI packages.

### 4.3 U-02: cleanup approach

1. Delete the 12 `setDebug(true)` calls. Optionally put a global toggle behind `Futtoboru.DEBUG_MODE`: F11
   switches `stage.setDebugAll()`, which helps U-05 screenshots of the layout.
2. Replace colour literals file by file with tokens (table 4.2). Move `InboxScreenTable.getCategoryColor` and the
   PlayerDetail rating ladder into `UiTheme` helpers (`categoryColor(MessageCategory)`, `ratingColor(int)`), so the
   thresholds live in one place.
3. Replace `setFontScale` with `UiTheme.label(text, role, color)`.
4. Replace `pad/width` literals with tokens. Standardise containers: `new VisTable(true)` everywhere except
   the dense data tables, which get `defaults().pad(UiTheme.space("xxs"))`.
5. Add `ui/BaseScreen` (stage, `ScreenViewport`, clear, `resize`, `hide→dispose`) and move the 8 live UI screens onto it
   (the 2D viewer keeps its `FitViewport` and only uses `UiTheme.clearScreen()`).
   Drop the 12 unused per-table `Stage`s.
6. Add `tools/ui/lint_ui.sh` to CI (section 2.6 patterns, allow-list `ui/**`) so literals don't come back.

### 4.4 U-03: scaling per resolution

- **Step 1, cheap, no new assets:** one `uiScale` for all stages. `ScreenViewport.setUnitsPerPixel(1f / uiScale)`
  scales skin, paddings and font together, so layout tokens stay in "design pixels".
  `uiScale = auto ? (min(w/1280, h/720) >= 1.75 ? 2 : 1) : setting`. Use integer steps only (1×, 2×), because the atlas is
  `Nearest`. A 2× step keeps the pixel look crisp; 1.25 or 1.5 would blur or shimmer. Add a "UI scale"
  option (Auto / 100 % / 200 %) to `SettingsScreen`, saved by `PreferencesManager`. When the scale is 2, the
  VisUI widgets that read `Sizes` (spinners, menus, file chooser) need the skin's `x2` entry (`skin.get("x2", Sizes.class)`).
  Minimum supported design size is 1280×720 at 1×. Below that (640×480, 800×600), wrap the content table in a
  `ScrollPane`, or drop the sub-720p entries from `resolutions.txt` (an open decision for the producer).
- **Step 2, optional, sharper text at 1.5×:** offline-generated Droid Sans bitmap fonts at 13/20/26 px with Hiero
  (Apache 2.0, already credited). They are added to the skin as `font-13/20/26`, and `fonts.*.file` in `theme.json`
  selects them per scale step. This needs **no runtime FreeType**, so it does not conflict with P1-4 removing
  `gdx-freetype-platform`. If the producer prefers runtime FreeType, P1-4 has to keep that dependency. Decide this
  before P1-4 merges.
- Fix the startup double-resize (launcher 1920×1080, then `create()` sets 640×480): pick one source, the saved
  preference, and default it to 1280×720.

### 4.5 U-04: navigation

- **Main menu:** New Game, Load Game, Settings, Quit. Show "Match Engine Debug" only when `DEBUG_MODE`
  (or `-Dfuttoboru.dev=true`). It stays a dev tool (D5).
- **In-game top bar:** make the Options select box a `PopupMenu` (skin style exists) and implement Load, Settings, Quit to menu
  and Quit to desktop. Until they work, hide them. Today 4 of 5 options do nothing.
- **Left column:** keep the two profession variants, but build both from one list
  (`MenuEntry(id, labelKey, professions, devOnly)`) instead of two hand-written blocks. Hide "Match Engine Debug" unless dev.
  Highlight the active entry (`toggle` style), because nothing shows where you are today.
- **Back:** `PREVIOUS_SCREEN` exists but there is no Back button. Add one to drill-down tables (League/Cup detail,
  Player detail, Club detail, Job offer).
- **Stubs:** finish `HomeScreenTable` as a dashboard (next fixture, unread inbox count, league position, balance).
  All of these exist in `SaveGame`. Delete the 6 anachronistic or duplicate stubs (1.4). Delete the 4 "later" stubs
  too and list them in ROADMAP as future screens. Unreachable placeholder classes only cost upkeep.
  Delete `NewGameScreen`, `SplashScreen` (or rebuild it on `BaseScreen` as the loading screen), `ConfederationsTable`,
  `LeagueTable`, `uiskin.*`. Hide `SoundSettingsTable` until the game has audio.
- **Int screen ids** (100 to 10010, two overlapping ranges `10000`/`10001`): replace with an enum `GameScreenId` when
  MainMenuManager is refactored. Inbox messages store `actionScreen` ints, so the enum must keep the same numeric values
  (save compatibility; coordinate with P2-5).

### 4.6 U-05: interface guide outline (`docs/design/INTERFACE_GUIDE.md`, later)

1. Principles: black background, one font family, tokens only, 1888 tone (no modern football jargon).
2. Layout: the screen grid (top bar, left menu, content), margins and gaps (`spacing.*`), title and section patterns.
3. Components, one entry each with a screenshot: buttons (default, primary, toggle, disabled), labels by role, links,
   select boxes, data tables (header, row, numeric alignment), dialogs, toasts, busy bar.
4. Colour semantics: what each `colors.*` token means. For example, green means good for the player's club, not
   "is a number".
5. Windows: one modal at a time (`VisDialog` only through a `UiDialogs` helper), the toast position, and the rule for
   when to use a dialog versus an inbox message.
6. Tooltips: one tooltip layer (VisUI `Tooltip`, shared delay and style), on every abbreviated column header (Pld, W, D, L, GF, GA, Pts).
7. Navigation map (section 1.1, updated) and keyboard keys (F12 screenshot, Esc back/close).
8. Screenshot procedure: F12 in game (`SharingUtils.screenshot`) at 1280×720 1× and 2560×1440 2×. Images go in
   `docs/design/img/`, and they are our own captures only.
9. Checklist for a new screen: extends `BaseScreen`, has a menu entry, i18n keys, no literals (lint passes), has a screenshot.

---

## 5. PR slices

Lock legend: **LOCK** means the file is in a lock listed in `docs/HANDOFF.md` "Files in use". **soft** means the file
calls a locked class (`DatabaseLoader`, `SaveLoadSystem`, `PersonGenerator`, `ScriptsLoader`). Slice 2 may change those
APIs and edit the file to keep it compiling, so expect merge conflicts. Plain style edits there merge easily, but
avoid restructuring those files before slice 2. "soft" files: `dialogs/LoadGameDialog`, `dialogs/SaveGameDialog`,
`game/Futtoboru`, `screens/MatchEngineDebugScreen`, `screens/NewGame*` (4), `screens/NewManagerScreen`,
`tables/authority/AuthorityScreenTable`, `tables/club/ClubDetailScreenTable`, `tables/jobs/JobBoard|JobOffer|MyApplications`,
`tables/match/history/*`, `tables/match/preview/*`, `tables/person/*`, `tables/schedule/ArrangeFriendlyTable|FixturesTable`,
`tables/widgets/CurrentDateAndTimeWidget`.

| PR | Content | Files | Lock | When |
|---|---|---|---|---|
| **U-01a** | `UiTheme` + `theme.json` (starter tokens 4.2) + `UiThemeTest`; one `UiTheme.load` line in `create()` | new `ui/UiTheme.java`, new `resources/ui/theme.json`, new test, `game/Futtoboru.java` (+1 line) | Futtoboru: soft (1 line) | **now** |
| U-02a | Remove the 12 `setDebug(true)` | `MainMenuManager`, `MainGameScreen`, `ClubDetailScreenTable`, `ClubBrowserScreenTable`, `JobBoardScreenTable`, `JobOfferScreenTable`, `MyApplicationsScreenTable`, `NegotiationScreenTable` | 3 soft (one-line deletions) | **now** |
| U-02b | `BaseScreen` + `UiTheme.clearScreen()`; move the 8 live screens; drop the unused per-table `Stage`s | new `ui/BaseScreen.java`, `screens/*` (7), `match/engine/MatchEngineScreen`, `tables/**` (12 stage lines) | NewGame*/NewManager/MatchEngineDebug soft | MenuScreen, SettingsScreen, MainGameScreen **now**; the rest **after slice 2** |
| U-02c | Tokens for colours and fonts, part 1: competitions, draw, finances, club info, inbox, squad list, player detail, match result | `tables/competitions/*` (4), `tables/draw/*` (2), `tables/finances/*`, `tables/club/ClubInfoScreenTable`, `tables/inbox/*`, `tables/squad/PlayersListTable`, `tables/player/*`, `tables/match/result/*`, `tables/settings/*` | none | **now** (after U-01a) |
| U-02d | Tokens part 2: jobs, club detail, debug screen, menu screen, 2D viewer | `tables/jobs/*` (5), `tables/club/ClubDetailScreenTable`, `screens/MatchEngineDebugScreen`, `screens/MenuScreen`, `match/engine/MatchEngineScreen`, `tables/match/engine/*` | several soft | **after slice 2** |
| U-02e | Spacing and size tokens (330 pads, 216 sizes), same file split as c/d; `tools/ui/lint_ui.sh` + CI step | as c/d + `MainGameMenuTable` (busy bar, date widget), `.github/workflows/ci.yml`, new `tools/ui/` | `MainGameMenuTable` **LOCK (WS P2-1)** | non-locked files **now**; `MainGameMenuTable` after P2-1 merges |
| U-03a | UI scale setting (Auto/1×/2×), `setUnitsPerPixel` in `BaseScreen`, `Sizes x2`, single startup resolution | `ui/BaseScreen`, `ui/UiTheme`, `screens/SettingsScreen`, `PreferencesManager`, `game/Futtoboru`, `futtoboru-desktop/.../DesktopLauncher`, `mods/resolutions.txt` | `mods/resolutions.txt` matches the lock pattern `mods/**/*.txt` (meant for data). Ask BOOK3PRO, or leave the file alone and filter in code | after U-02b |
| U-03b | (optional) Droid Sans 13/20/26 bitmap fonts in the skin, `fonts.*.file` | `resources/skin/*`, `theme.json`, `THIRD_PARTY_NOTICES.txt` | P1-4 owns `pom.xml` deps (only relevant if FreeType is chosen instead) | after U-03a; **decide the FreeType question before P1-4 merges** |
| U-04a | Navigation cleanup: delete the 10 unreachable stubs, `NewGameScreen`, `SplashScreen`, the 2 empty authority classes, `uiskin.*`; dev-only Match Engine Debug (main menu + left column); hide the sound table | `tables/main/*` (10), `screens/NewGameScreen`, `screens/SplashScreen`, `tables/authority/{Confederations,League}Table`, `resources/uiskin.*`, `screens/MenuScreen`, `menu/MainMenuManager`, `screens/SettingsScreen` | NewGameScreen soft (deleting it removes a soft file) | **now** |
| U-04b | Menu-entry list + active highlight + Back button on drill-downs | `menu/MainMenuManager`, `menu/buttons/*`, drill-down tables | some soft | after slice 2 |
| U-04c | Home dashboard (finish `HomeScreenTable`) | `tables/main/HomeScreenTable` (+ read-only `SaveGame` getters) | reads `SaveGame` (LOCK, read only) | **after slice 2** (data ids change) |
| U-04d | Options `PopupMenu` with Load / Settings / Quit wired (hide the rest until done) | `menu/topmenu/MainGameMenuTable`, `dialogs/LoadGameDialog` | **LOCK (WS P2-1)**; LoadGameDialog soft and P2-5 saves | after P2-1 and P2-5 |
| U-05 | `docs/design/INTERFACE_GUIDE.md` + our own screenshots in `docs/design/img/` | docs only | none | outline **now**; screenshots after U-02/U-03 so they show the final look |

**Can start now, with no lock overlap:** U-01a, U-02a, U-02c, U-04a, the U-05 outline, and U-02b for the three main screens.
**After BOOK3PRO's P2-4 slice 2:** U-02b (rest), U-02d, U-04b, U-04c. **After WORKSTATION P2-1:** U-02e for
`MainGameMenuTable`, U-04d. **Producer decisions needed:** (1) runtime FreeType vs offline fonts, before P1-4 merges;
(2) drop sub-720p resolutions or scroll; (3) delete the "later" stubs (recommended) or keep them hidden.

**Recommended first PR: U-01a.** It is inert, adds the `UiTheme` + `theme.json` foundation and its test, and every
later slice depends on it. U-02a (debug lines off) is a 10-minute warm-up that can go in the same day and gives the
most visible improvement right away.
