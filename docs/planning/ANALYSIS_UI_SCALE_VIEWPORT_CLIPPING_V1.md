# Analysis — UI Scale Viewport Clipping & Responsive Layout

**Document type:** Platform / UX analysis (fix proposal)  
**Date:** 2026-06-28  
**Status:** ✅ **Mitigated by PU-10-R3/R3b** — see [PU-10_UI_SCALE_COMPLETION_V1.md](PU-10_UI_SCALE_COMPLETION_V1.md).
**Branch:** `analyze-ai-branch`  
**Symptom:** After raising **UI scale** in Settings, widgets grow but **content is cut off** — large parts of the UI are outside the visible area and unusable.

**Related:** [ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md](ANALYSIS_UI_SCALE_BROWSER_PARITY_V1.md) · [ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md](ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md) · [ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md](ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md)

---

## 1. Executive summary

| Question | Answer |
|----------|--------|
| Why does scaled UI clip? | Current `UiScale` shrinks the **logical canvas** while layouts still assume **full-window** width (especially Settings’ 5-column row). |
| Is this a LibGDX bug? | **No** — wrong scaling strategy for fixed-width layouts. |
| HTML responsive equivalent in LibGDX? | **`Table` layout** (flex-like) + **`ScrollPane`** (overflow) + optional **`ExtendViewport`** (design resolution). There is no CSS `@media` — you engineer it. |
| Recommended fix (native, not over-engineered) | **PU-10-R2** attempted — **reverted**; next try must keep **viewport-level** scale (not actor `setScale` + ScrollPane root) |
| Upgrade LibGDX/VisUI now? | **Optional separate bump** — fix clipping does not require 1.14.2; upgrade path documented in §6. |

---

## 2. Root cause — previous `UiScale` (fixed in PU-10-R2)

**Before PU-10-R2**, scaling used:
```java
viewport.setUnitsPerPixel(1f / currentScale());
```

**After PU-10-R2 (reverted):** actor `setScale` inside `VisScrollPane` did **not** enlarge skin/fonts reliably and **broke click hit-testing**. Production code uses **`unitsPerPixel = 1/scale`** again. One-time pref migration resets `settings.ui_scale` to **100%** on next launch (`settings.ui_scale_migration_v4`).

```text
worldWidth  = screenWidth  × unitsPerPixel
worldHeight = screenHeight × unitsPerPixel
```

| User scale | unitsPerPixel | 1920×1080 window → logical world |
|------------|---------------|----------------------------------|
| 100% | 1.0 | 1920 × 1080 |
| 150% | 0.667 | **1280 × 720** |
| 200% | 0.5 | **960 × 540** |

Widgets **draw larger** (each scene unit occupies more screen pixels), but the **layout coordinate system shrinks**. Anything laid out for “full HD width” (Settings: **five panels in one row**, `mainContainer.pad(50)`, fixed `.width(N)` columns) **does not fit** in 1280×720 logical units → **clipped at viewport edges**.

This is the opposite of browser zoom: Chrome at 150% keeps the layout viewport and applies a **visual transform**, then shows **scrollbars** if content exceeds the window.

---

## 3. LibGDX vs HTML — mental model

| Web (HTML/CSS) | LibGDX / Scene2D / VisUI |
|----------------|---------------------------|
| `%` / `flex` / `grid` | `Table`/`VisTable` — `expand`, `fill`, `grow`, `colspan` |
| `overflow: auto` | `ScrollPane` / `VisScrollPane` |
| `zoom: 150%` / browser zoom | **No single property** — combine viewport + actor `setScale` or skin size |
| `@media (max-width: …)` | Different layouts per breakpoint (manual) or `ExtendViewport` + design size |
| `rem` / root font size | Skin font (`tixel.json` / `BitmapFont`) or `Label.setFontScale` |
| Viewport meta / responsive | `ExtendViewport`, `FitViewport`, `ScalingViewport` |

**VisUI** adds styled widgets and built-in **`SkinScale.X1` / `X2`** (discrete 2× built-in skins). Futtoboru uses a **custom** `skin/tixel.json`, so `SkinScale.X2` does not apply without duplicating atlas assets.

**Scene2D is not a browser.** Responsive UI is ** achievable** with native tools — but it is **explicit layout work**, not automatic.

---

## 4. What already works in Futtoboru

| Pattern | Examples | Scales well today? |
|---------|----------|-------------------|
| `VisScrollPane` on content | `LeagueDetailScreenTable`, `JobBoardScreenTable`, `PlayerDetailScreenTable`, `MatchEngineDebugScreen` | **Content scrolls** — still affected by viewport shrink |
| `setFillParent(true)` shell | Most screens | Fills **logical** world — shrinks with current `UiScale` |
| Fixed `.width(100)` columns | League, inbox, finances, squad (~150+ uses) | **Breaks** when logical width shrinks |
| Settings 5-column row | `SettingsScreen` | **Worst case** for clipping |

`MatchEngineDebugScreen` is the **best in-repo reference**: `mainTable` → `VisScrollPane` → `contentTable` — correct pattern for zoom + overflow.

---

## 5. Fix options (native LibGDX + VisUI only)

### Option A — **Scroll + root scale** (recommended MVP fix)

**Idea:** Browser zoom on a fixed-layout page — everything grows; **scroll** to reach clipped areas.

| Step | Change |
|------|--------|
| A1 | `UiScale`: keep viewport **1:1** (`unitsPerPixel = 1`) |
| A2 | Apply `content.setScale(userScale, userScale)` on inner root (not the scroll pane) |
| A3 | Wrap screen root in `VisScrollPane` (`setFillParent(true)`) |
| A4 | On scale change, `invalidateHierarchy()` + scroll fade bars |

**Pros:** Small diff; uses existing VisUI scroll; matches user expectation at 150–200%.  
**Cons:** User may need to scroll on Settings / wide menus (acceptable for MVP).

**Screens to wrap first:** `SettingsScreen`, `MenuScreen`, `NewGame*`, `MainGameScreen` shell.

---

### Option B — **ExtendViewport + design resolution** (responsive “canvas”)

**Idea:** Fixed design size (e.g. **1920×1080** or **1280×720** reference); viewport scales to **fit window**; user UI scale adjusts a **content scale factor** inside.

```java
// LibGDX native
new ExtendViewport(1920, 1080, camera);
// + inner content scale from settings
```

**Pros:** Consistent layout coordinates across window sizes; good for “FM window resized”.  
**Cons:** Letterboxing on odd aspect ratios; still need **ScrollPane** when `userScale > 1`; more refactor than A.

**When:** After A, if PO wants resize-stable layouts.

---

### Option C — **Fit cap** (“always fit on screen”)

**Idea:** `effectiveScale = min(userScale, fitScale)` so nothing clips — never exceeds window.

**Pros:** No scroll; always usable.  
**Cons:** **Not** browser zoom — user picks 200% but gets less; poor for HiDPI accessibility.

**When:** Optional Settings toggle: **“Fit to window”** vs **“Zoom (scroll)”**.

---

### Option D — **Layout refactor** (HTML-responsive, longer arc)

**Idea:** Remove fixed widths; stack Settings columns vertically under a width threshold; use `%`-like `expandX`/`grow`.

| Task | Effort |
|------|--------|
| Settings: 5 columns → 2+3 or vertical stack + scroll | Medium |
| Audit `.width(N)` on data tables | Medium–high |
| Shared `ScreenShell` (scroll + scaled root) | Low once pattern exists |

**Pros:** Best long-term UX; less scroll at high scale.  
**Cons:** Not a one-line fix; screen-by-screen QA.

**When:** Parallel with A — prioritize Settings + main menu.

---

### Option E — **VisUI SkinScale X2 only**

**Idea:** `VisUI.load(SkinScale.X2)` for built-in VisUI skin.

**Verdict:** **Not sufficient** — custom `tixel.json`, no X2 variant; only **100% / 200%** discrete; does not replace user slider or fix layout width.

---

## 6. Dependency versions (docs refresh)

Verified **2026-06-28** against Maven Central / upstream CHANGES.

| Dependency | **Project today** | **Latest stable** | Notes |
|------------|-------------------|-------------------|--------|
| LibGDX | **1.12.0** | **1.14.2** (Jun 2026) | [libGDX 1.14.1 & 1.14.2 release notes](https://libgdx.com/news/2026/05/gdx-1-14-1) |
| VisUI | **1.5.2** (targets 1.12.0) | **1.5.4** (1.12.1) · **1.5.6** (1.13.1) · **1.5.10-SNAPSHOT** (1.14.1) | [vis-ui CHANGES](https://github.com/kotcrab/vis-ui/blob/master/ui/CHANGES.md) |

**Recommendation:**

| Phase | Action |
|-------|--------|
| **PU-10-R2** (clipping fix) | **No version bump required** — viewport/ScrollPane only |
| **TECH-UPG-1** (scheduled) | LibGDX **1.12.0 → 1.14.2** + VisUI **→ 1.5.10** (or latest matching row in CHANGES); full `mvn test` + manual smoke |
| **Docs** | Keep `ENGINE_LIBGDX_AND_UI_ARCHITECTURE.md` §2 in sync after upgrade |

VisUI **does not expose `setScale(float)`** on 1.5.x — scaling is **`SkinScale` enum** (built-in skins) or **layout/viewport** (our approach).

---

## 7. Proposed implementation plan (PO decision)

| ID | Deliverable | Approach | Effort |
|----|-------------|----------|--------|
| **PU-10-R2a** | Fix clipping | Option **A** — ScrollPane + root scale | ❌ **Reverted** — no visual scale, broken clicks |
| **PU-10-R2b** | `ScreenShell` helper | `UiScale.addScrollableRoot` | ❌ **Reverted** |
| **PU-10-R2c** | Settings layout | Option **D** — stack panels or 2-row grid; highest impact | **Medium** |
| **PU-10-R3** | Optional fit mode | Option **C** toggle | **Small** |
| **PU-10-R4** | ExtendViewport | Option **B** if window resize stability matters | **Medium** |
| **TECH-UPG-1** | LibGDX/VisUI bump | §6 — separate PR | **Medium** (regression risk) |

**Do not overcomplicate:** Option **A + Settings layout tweak** unblocks Galaxy Book QA; defer ExtendViewport and dependency bump unless PO prioritizes.

---

## 8. Acceptance criteria (after PU-10-R2)

On Galaxy Book3 Pro (or 1920×1080 window), **UI scale 150%**:

| # | Check |
|---|--------|
| 1 | Settings — all five sections reachable (scroll OK) |
| 2 | Main menu — all buttons visible or scrollable |
| 3 | In-game — top menu + sidebar usable |
| 4 | League / inbox — existing `ScrollPane` tables still work |
| 5 | 100% scale — unchanged from pre-scale-fix baseline |
| 6 | Pref persists across restart |

---

## 9. Summary

The current implementation uses **`ScreenViewport.setUnitsPerPixel(1/scale)`**, which **shrinks the layout canvas** while UI is built for **full window width** — hence clipping. Browser/Cursor-like behaviour needs **full-pixel logical space** plus **zoom transform** and **overflow scroll** (`ScrollPane`), using **native LibGDX/VisUI** only. Layout refactors (fewer fixed widths) improve responsiveness over time but are not required for the first fix.
