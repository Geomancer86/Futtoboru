# Analysis — UI Scaling & HiDPI (Desktop)

**Document type:** Platform / UX analysis  
**Date:** 2026-06-28  
**Reported on:** Samsung Galaxy Book3 Pro (high-DPI laptop)  
**Branch:** `analyze-ai-branch`  
**Status:** **PU-10 MVP implemented** (2026-06-28) — `settings.ui_scale`, `UiScale` viewport helper, Settings select box. VisUI 1.5.2 has no `setScale()`; zoom is viewport-based. Match engine excluded per PO.

**Related:** [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md) · [DEPARTMENT_PLATFORM_AND_UX_V1.md](DEPARTMENT_PLATFORM_AND_UX_V1.md) · [FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md](FUTTOBORU_UI_UX_STYLE_GUIDE_V1.md) · [ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md](ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md) (Settings screen deep dive + integration plan)

---

## 1. Question

> Can the player scale up the UI / fonts on a high-resolution laptop?

**Short answer: No — not today.** Settings only offer **window resolution** and **fullscreen**. There is no **UI scale**, **font size**, or **HiDPI** preference.

---

## 2. What exists today

| Control | Where | Effect |
|---------|-------|--------|
| **Resolution list** | Settings → Graphics (`SettingsScreen.java`, `mods/resolutions.txt`) | Changes **window pixel size** (e.g. 1920×1080). Does **not** scale fonts or widgets. |
| **Fullscreen** | Same screen | Uses monitor native mode. Does **not** enlarge UI elements. |
| **Default resolution** | `PreferencesManager.DEFAULT_RESOLUTION` = `1280x1024` | Saved to user prefs on first run. |
| **Launcher default** | `DesktopLauncher.java` | Starts at **1920×1080** before prefs apply in `Futtoboru.create()`. |

### Rendering model

- All main UI screens use **`ScreenViewport`** — **1 scene unit = 1 back-buffer pixel**.
- Skin font: **`droid-sans-13.fnt`** (~13 px cap height) via `skin/tixel.json`.
- Some tables call **`setFontScale(1.2f–1.5f)`** on individual title labels only; there is **no global scale**.
- **`VisUI.setScale()`** is **never called** in the codebase.

### Preferences surface

`PreferencesManager` stores: fullscreen, resolution, locale, sound, debug toggles. **No** `uiScale`, `fontScale`, or `dpi` keys.

---

## 3. Why Galaxy Book3 Pro feels tiny

Typical setup: **2880×1800 (or similar) panel** with **125–200% Windows display scaling**.

LibGDX **1.12.0** + **LWJGL3** desktop launcher:

- **`Lwjgl3ApplicationConfiguration.setHdpiMode(...)` is not used** in `DesktopLauncher.java`.
- The game draws UI at **raw pixel sizes** (13 px text, fixed skin assets).
- On HiDPI Windows, an “unaware” Java/LWJGL window often occupies a **small physical area** while still using **small pixel fonts** — the OS does not automatically scale in-app widgets the way it scales some native apps.

Changing **in-game resolution** resizes the **window**, not the **relative size of text vs. the screen**. Lower resolution → smaller window, **same tiny fonts inside it**.

---

## 4. Code references

| Area | File | Notes |
|------|------|-------|
| Launcher | `futtoboru-desktop/.../DesktopLauncher.java` | `setWindowedMode(1920, 1080)`; no HiDPI config |
| Prefs / apply | `Futtoboru.java` | `setScreen()` → `Gdx.graphics.setWindowedMode(w, h)` |
| Settings UI | `SettingsScreen.java` | Resolution + fullscreen only |
| Prefs storage | `PreferencesManager.java` | No UI scale pref |
| Viewport | All `*Screen.java`, `*ScreenTable.java` | `new Stage(new ScreenViewport())` |
| Skin | `skin/tixel.json` | `droid-sans-13.fnt` |
| Resolution mod | `mods/resolutions.txt` | Comment: below 640×480 “break the UI” |

---

## 5. Interim workarounds (no code change)

| Workaround | Pros | Cons |
|------------|------|------|
| **Windows Magnifier** (Win + +) | Immediate | Clumsy for long QA sessions |
| **Java.exe → Properties → Compatibility → Override high DPI scaling → System** | Can enlarge whole window | Blurry; per-Java install path |
| **Lower Windows display scale** (100%) | Larger effective UI | Affects entire desktop; eye strain on 3K |
| **In-game lower resolution** | — | **Does not fix** tiny fonts; shrinks window |

**For Layer A manual QA:** document which workaround QA uses until **PU-10** ships.

---

## 6. Recommended fix (implementation — not in scope of this analysis)

Priority for **Platform & UX** — blocks comfortable manual QA on HiDPI laptops.

| ID | Deliverable | Approach | Effort |
|----|-------------|----------|--------|
| **PU-10a** | **UI scale setting** in Settings | Persist + apply — **must use PU-10-R** (viewport + VisUI), not VisUI-only — [ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md](ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md) | **Medium** |
| **PU-10b** | **HiDPI mode** in launcher | `configuration.setHdpiMode(HdpiMode.Pixels)` or `PixelsPerPoint` + test on Galaxy Book / 125% Windows | **Small** |
| **PU-10c** | **Density-aware skin** (optional) | Larger bitmap fonts in skin JSON for “large UI” preset | **Medium** |
| **PU-10d** | **Stage root scale** | Scale root `VisTable` on all screens if VisUI scale insufficient for custom widgets | **Medium** |

**Suggested order:** **PU-10a + PU-10b** first (covers QA laptop); skin swap later if crispness matters.

**Acceptance (PU-10):**

- Settings shows **UI scale**; change applies without restart (or with one documented restart).
- At **150%** scale, default menu text readable at **125% Windows scaling** on Galaxy Book3 Pro class hardware.
- Regression: 100% scale unchanged on standard 1080p monitor.

---

## 7. Impact on Layer A manual QA

| Layer A item | HiDPI impact |
|--------------|--------------|
| A-1 Manual QA checklist | **At risk** — tiny UI may cause mis-clicks, missed labels |
| A-5 2-1-0 standings verify | Hard to read points/GD columns |
| Continue / draw / load flows | Functional but **poor UX** |

**PO decision:** Treat **PU-10** as **Layer A QA enabler** (parallel small sprint) or accept OS-level workaround for one machine.

---

## 8. Summary

| Question | Answer |
|----------|--------|
| Can we scale UI in-game today? | **No** |
| Does resolution setting help? | **Only window size** — not font/UI scale |
| Is this a bug or missing feature? | **Missing feature** (UX-1 / PU-10) — common on LibGDX desktop without HiDPI + scale |
| Blocks release logic? | **No** for headless ITs; **Yes** for comfortable manual QA on HiDPI laptops |
