# Analysis — Settings Screen & UI Scale Integration

**Document type:** Platform / UX analysis (implementation plan)  
**Date:** 2026-06-28  
**Branch:** `analyze-ai-branch`  
**Parent:** [ANALYSIS_UI_SCALING_HIDPI_V1.md](ANALYSIS_UI_SCALING_HIDPI_V1.md) · **PU-10**

---

## 1. Executive summary

Futtoboru **does** have a Settings screen with a working **persistence pipeline** (`PreferencesManager` → LibGDX `Preferences` → `flush()`). **Resolution** is the best reference implementation: change in UI → save → apply immediately via `Gdx.graphics`.

**UI scale is not wired yet**, but the same pattern fits naturally in the **Graphics** column of `SettingsScreen`, next to Resolution and Fullscreen.

---

## 2. How to open Settings

| Entry point | Code | Status |
|-------------|------|--------|
| **Main menu** | `MenuScreen` → `SettingsButton` → `game.setScreen(new SettingsScreen(game))` | ✅ Works |
| **In-game Options menu** | `MainGameMenuTable` → Options → “Settings” (index 3) | ❌ Falls through to `default` — **“OPTION NOT IMPLEMENTED!”** |
| **`Futtoboru.changeScreen(SETTINGS_SCREEN)`** | Used if called programmatically | ✅ Works |

**Note:** `SettingsButton` bypasses `Futtoboru.changeScreen()` and constructs `SettingsScreen` directly. Both paths work; prefer `changeScreen(SETTINGS_SCREEN)` for consistency.

---

## 3. Settings screen layout

`SettingsScreen` builds one `Stage` (`ScreenViewport`) and a 5-column `VisTable`:

```
┌─────────────────┬──────────────┬──────────────────┬─────────────┬─────────────────────┐
│ Graphics        │ Sound        │ Gameplay         │ Mods        │ Debug logging       │
│ (inline table)  │ SoundSettings│ (title only)     │ ModsSettings│ DebugLoggingSettings│
│                 │ Table        │                  │ Table       │ Table               │
└─────────────────┴──────────────┴──────────────────┴─────────────┴─────────────────────┘
                              [ Back to main menu — MainMenuButton ]
```

| Panel | Class | Built in |
|-------|-------|----------|
| Graphics | Inline `VisTable` in `SettingsScreen` | Resolution, fullscreen, legacy DarkBlade debug toggles |
| Sound | `SoundSettingsTable` | Master slider (save **commented out**) |
| Gameplay | Empty `VisTable` | Placeholder label only |
| Mods | `ModsSettingsTable` | Language select (save **commented out**) |
| Debug | `DebugLoggingSettingsTable` | Per-category checkboxes — **fully wired** |

On `show()`, only `debugLoggingSettings.refreshSettings()` runs — graphics controls are initialized once in the constructor.

On `hide()`, the screen **disposes** itself (unusual pattern — screen is recreated each visit).

---

## 4. Persistence architecture

### 4.1 Storage

| Item | Value |
|------|--------|
| API | `com.badlogic.gdx.Preferences` via `Gdx.app.getPreferences(...)` |
| File name | `"Futtuboru Preferences"` (typo preserved) |
| Desktop path (typical) | `%USERPROFILE%\.prefs\Futtuboru Preferences` |
| Manager | `PreferencesManager.java` |
| Write pattern | `updateUserPreference(key, value)` → `putString` → **`flush()` immediately** |

Every saved setting is flushed on change — there is **no** “Apply” / “Save on exit” button. Resolution demonstrates **live apply + live save**.

### 4.2 Startup load (`Futtoboru.create()`)

Order matters:

1. `preferences = new PreferencesManager()`
2. `locale = preferences.getSavedLocale()` → load `I18NBundle`
3. `FULLSCREEN = preferences.getFullscreen()`
4. `RESOLUTION = preferences.getResolution()` (fallback static default `640x480` if null)
5. **`setScreen()`** — applies window mode **before** VisUI
6. `VisUI.load("skin/tixel.json")`
7. Load DB, show menu

Static fields on `Futtoboru` (`RESOLUTION`, `FULLSCREEN`) mirror prefs for runtime; prefs are source of truth on disk.

### 4.3 Reference flow — resolution (copy this for UI scale)

```
SettingsScreen ChangeListener
    → Futtoboru.RESOLUTION = selected
    → Futtoboru.updateScreenResolution(res)
        → PreferencesManager.updateScreenResolution(res)
            → prefs.putString("settings.resolution", res); flush()
    → Futtoboru.setScreen()
        → Gdx.graphics.setWindowedMode(w, h)  // or fullscreen
        → preferences.updateFullScreen(FULLSCREEN)
```

Viewport resize: `Game.resize()` → current `Screen.resize()` → `stage.getViewport().update(width, height, true)`.

---

## 5. Settings inventory — what works today

| Setting | UI control | Pref key | Saved? | Applied live? | Notes |
|---------|------------|----------|--------|---------------|-------|
| **Resolution** | `VisSelectBox` | `settings.resolution` | ✅ | ✅ | List from `mods/resolutions.txt`; forces windowed on change |
| **Fullscreen** | `VisCheckBox` | `settings.fullscreen` | ✅ | ✅ | **`setChecked(saved)` commented out** — UI may not match saved state |
| Master sound | `VisSlider` | `settings.master_sound_level` | ⚠️ getter exists | ❌ | `updateMasterSoundLevel` call commented in `SoundSettingsTable` |
| Language | `VisSelectBox` | `settings.language_bundle` | ⚠️ getter/save exist | ❌ on change | Locale applied only at **`create()`** |
| Debug logging | `VisCheckBox` × N | via `DebugLogManager` | ✅ | ✅ | Best-maintained panel; `refreshSettings()` on show |
| Day/night, shadows, damage counters, debug physics/path/sight, table debug | `VisCheckBox` | Various keys in `PreferencesManager` | ⚠️ getters exist | ❌ | **DarkBlade leftovers** — listeners empty or commented |

### Known gaps (settings-related)

| ID | Issue |
|----|--------|
| **SET-1** | In-game Options → Settings not implemented (`MainGameMenuTable` case 3) |
| **SET-2** | Fullscreen checkbox not initialized from `preferences.getFullscreen()` |
| **SET-3** | `Futtoboru.RESOLUTION` static default `640x480` vs `PreferencesManager.DEFAULT_RESOLUTION` `1280x1024` mismatch before first save |
| **SET-4** | Sound / language UI does not call save hooks |

---

## 6. Why resolution does not fix HiDPI tiny UI

Resolution only calls `Gdx.graphics.setWindowedMode(width, height)`. It changes **how many pixels the window has**, not **how large widgets are drawn inside those pixels**.

- UI uses `ScreenViewport` (1 unit = 1 pixel) + **13 px** skin font.
- **`VisUI.setScale()` is never called** (VisUI **1.5.2** on LibGDX **1.12.0** supports it).
- No `settings.ui_scale` preference exists.

See [ANALYSIS_UI_SCALING_HIDPI_V1.md](ANALYSIS_UI_SCALING_HIDPI_V1.md) for Galaxy Book3 Pro context.

---

## 7. How to add UI scale in Settings (recommended design)

### 7.1 Product

Add to **Graphics** panel (below Fullscreen):

- Label: `UI scale` (i18n key `ui_scale`)
- Control: `VisSelectBox<String>` with **`100%` / `125%` / `150%` / `175%` / `200%`** (or `VisSlider` 100–200 step 25)

Default: **`100%`** on 1080p; optional auto-detect later (Windows DPI → suggest 150%).

### 7.2 Preference layer

In `PreferencesManager`:

```java
public static final String SETTINGS_UI_SCALE_PREF = "settings.ui_scale";
public static final float DEFAULT_UI_SCALE = 1.0f;

public float getUiScale() { /* parse float, default 1.0 */ }
public void updateUiScale(float scale) { updateUserPreference(..., Float.toString(scale)); }
```

Store as float string (same pattern as master sound level).

In `Futtoboru`:

```java
public static float UI_SCALE = 1.0f;
```

Load in `create()` **before** `VisUI.load()`.

### 7.3 Apply scale — recommended: PU-10-R (browser-like)

> **Decision support:** [ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md](ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md) — original VisUI-only plan is **not** equivalent to browser/Cursor zoom.

**Do not ship VisUI-only** if the goal is Cursor/browser behaviour. Use **viewport scale + VisUI.setScale**:

#### Primary — Stage viewport (uniform zoom)

```java
// UiScale helper — use in every Screen constructor + resize()
ScreenViewport vp = new ScreenViewport();
vp.setUnitsPerPixel(1f / Futtoboru.UI_SCALE);
stage = new Stage(vp);
```

Scales **layout + text + hardcoded `.width()`** together (browser-like).

#### Secondary — VisUI skin (crisp widgets)

```java
VisUI.setScale(UI_SCALE);
VisUI.load(Gdx.files.internal("skin/tixel.json"));
```

#### On change from Settings

Save pref → update `UI_SCALE` → reload VisUI → `changeScreen(current)` (rebuild).

#### Legacy options (not recommended alone)

<details>
<summary>Option A — VisUI global scale only (insufficient alone)</summary>

**Startup:**

```java
VisUI.setScale(UI_SCALE);
VisUI.load(Gdx.files.internal("skin/tixel.json"));
```

**On change from Settings** (mirror resolution live-apply):

```java
public void updateUiScale(float scale) {
    UI_SCALE = scale;
    preferences.updateUiScale(scale);
    applyUiScale();
}

public void applyUiScale() {
    VisUI.setScale(UI_SCALE);
    VisUI.dispose();
    VisUI.load(Gdx.files.internal("skin/tixel.json"));
    // Rebuild current screen so all actors pick up new skin metrics
    int current = /* track active screen id or enum */;
    changeScreen(current);
}
```

**Pros:** Scales buttons, labels, paddings from skin.  
**Cons:** Hardcoded `.width(N)` and `setFontScale` do not scale uniformly — **not browser-like**.

</details>

<details>
<summary>Option B — Viewport only (sufficient alone; pair with VisUI for crispness)</summary>

Set on each `Stage` after creation:

```java
((ScreenViewport) stage.getViewport()).setUnitsPerPixel(1f / UI_SCALE);
stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
```

**Pros:** Uniform zoom on all actors on that stage.  
**Cons:** Must use **`UiScale` helper** in every Screen (~10 files) + `MatchEngineScreen.uiStage`.

</details>

**Recommendation:** **PU-10-R = viewport helper + VisUI.setScale + Settings row.** See browser parity doc §5.

### 7.4 SettingsScreen wiring (concrete steps)

1. **Add row** in `graphicsSettings` after Fullscreen (same pattern as resolution):

```java
VisLabel uiScaleLabel = new VisLabel(LanguageModLoader.getValue("ui_scale"));
VisSelectBox<String> uiScaleSelectBox = new VisSelectBox<>();
uiScaleSelectBox.setItems("100%", "125%", "150%", "175%", "200%");
uiScaleSelectBox.setSelected(formatScale(Futtoboru.UI_SCALE));

uiScaleSelectBox.addListener(new ChangeListener() {
    @Override
    public void changed(ChangeEvent event, Actor actor) {
        float scale = parseScale(uiScaleSelectBox.getSelected());
        ((Futtoboru) game).updateUiScale(scale);
    }
});
```

2. **i18n** — add to `BaseBundle_en_us.properties` (and `_en_uk`, `_sp_la`):

```properties
ui_scale=UI scale
```

3. **Helper** — `parseScale("150%") → 1.5f` in `Futtoboru` or small util.

4. **Optional:** HiDPI in `DesktopLauncher` (separate from Settings, ship together):

```java
configuration.setHdpiMode(HdpiMode.Pixels);
```

### 7.5 Files to touch (PU-10 checklist)

| File | Change |
|------|--------|
| **`UiScale.java`** (new) | `newViewport()`, `updateViewport()` |
| `PreferencesManager.java` | `SETTINGS_UI_SCALE_PREF`, get/update |
| `Futtoboru.java` | `UI_SCALE`, load in `create()`, `updateUiScale()`, rebuild screen |
| **All `*Screen.java`** (~10) | Use `UiScale.newViewport()`; resize via helper |
| `MatchEngineScreen.java` | Apply to `uiStage`; replace `BitmapFont` Label or scale overlay |
| `SettingsScreen.java` | Graphics row + listener |
| `MainGameScreen.java` | Toast padding × scale |
| `BaseBundle_*.properties` | `ui_scale` label |
| `PreferenceManagerTests.java` | Round-trip scale pref |

### 7.6 Acceptance

- Change UI scale in Settings → prefs file contains `settings.ui_scale=1.5` (example).
- Restart game → scale restored before menu shown.
- **150%** on Galaxy Book3 Pro: main menu labels readable without OS Magnifier.
- **100%** on 1080p monitor: unchanged from today.

---

## 8. Interaction with other settings

| Setting | Interaction with UI scale |
|---------|---------------------------|
| Resolution | Independent — window pixels × UI scale = final readability. QA may use **1280×1024 + 150% UI** as sweet spot. |
| Fullscreen | Independent — `setScreen()` after scale reload should preserve fullscreen flag. |
| Language | Reload bundle on language change already needs restart pattern — same as scale reload. |
| Debug logging | Unaffected |

---

## 9. Implementation status (2026-06-28)

| Item | Status |
|------|--------|
| `settings.ui_scale` pref | ✅ |
| Settings → UI scale select (100–200%) | ✅ |
| `UiScale` viewport on all management screens | ✅ |
| Persist + reload on startup | ✅ |
| In-game Options → Settings | ✅ |
| Match engine overlay | ⏸ Deferred |
| `VisUI.setScale` | N/A (not in vis-ui 1.5.2) |
| Per-screen layout polish | 🔲 QA follow-up |


| Question | Answer |
|----------|--------|
| Does Settings save values? | **Yes** — via `PreferencesManager` + immediate `flush()` |
| What saves reliably today? | **Resolution**, **fullscreen** (toggle), **debug logging** |
| What is stubbed? | Sound slider, language select, DarkBlade graphics toggles |
| Where to add UI scale? | **Graphics column**, same listener pattern as resolution |
| Best technical approach? | **`VisUI.setScale` + pref + reload skin & current screen** |
| Effort | **Small** (~half day) for PU-10a |
