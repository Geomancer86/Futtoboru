# Analysis — UI Scale vs Browser / IDE Zoom (Screen Coverage)

**Document type:** Platform / UX analysis (decision support)  
**Date:** 2026-06-28  
**Branch:** `analyze-ai-branch`  
**Question:** Will the proposed `VisUI.setScale` + Settings solution behave like **browser zoom** or **Cursor IDE scale**, and will **all** in-game screens scale correctly?

**Related:** [ANALYSIS_UI_SCALING_HIDPI_V1.md](ANALYSIS_UI_SCALING_HIDPI_V1.md) · [ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md](ANALYSIS_SETTINGS_SCREEN_AND_UI_SCALE_V1.md)

---

## 1. Short answer (for PO decision)

| Question | Answer |
|----------|--------|
| Does **PU-10a as originally proposed** (`VisUI.setScale` + skin reload only) work like browser/Cursor zoom? | **No — not fully.** It scales **skin defaults** (fonts, button chrome) but **not the whole layout tree uniformly**. |
| Will **all** management UI screens look correct at 150%? | **Mostly readable**, but **not guaranteed** — ~**150+ hardcoded column widths**, **~35 `setFontScale` overrides**, and **one non-VisUI screen** will behave inconsistently. |
| What **would** behave like browser/Cursor zoom? | **One global scale applied at the Stage viewport** (or root actor), **plus** optional `VisUI.setScale` for skin assets — implemented via a **single `UiScale` helper** used by every `Screen`. |
| Are we **sure** without implementation? | **No.** Requires a **PU-10 acceptance pass** at 100% / 150% / 200% on menu, in-game, inbox, league table, draw, dialogs, toasts. |

**Recommendation:** Approve PU-10 only with **revised approach PU-10-R** (§5 below), not VisUI-only.

---

## 2. How browser / Cursor scaling works

| Product | Mechanism | Effect |
|---------|-----------|--------|
| **Browser** (Ctrl + +/-) | Page zoom: root layout + text + images scaled **together** | Proportional; reflow may occur but **one zoom factor** applies tree-wide |
| **Cursor / VS Code** | Electron `zoomLevel` on renderer | **Entire UI** (editor, sidebar, tabs) scales uniformly |
| **Windows display scale** | OS compositor scales app window | App may be bitmap-scaled (blurry) unless app is DPI-aware |

Common property: **single multiplier on the presentation root** — not separate knobs for “font size” vs “column width” vs “button skin”.

---

## 3. What the original PU-10a proposal actually scales

Original plan (from settings analysis):

```java
VisUI.setScale(UI_SCALE);
VisUI.load("skin/tixel.json");
// on change: dispose, reload, rebuild screen
```

### What VisUI.setScale affects (VisUI 1.5.2)

- Bitmap font generation / skin metrics at **load time**
- Default VisUI widget sizes derived from skin (buttons, paddings in skin JSON)
- New `VisLabel` / `VisButton` created **after** reload use larger fonts

### What it does **not** uniformly scale

| Pattern | Prevalence in Futtoboru | At 150% global scale |
|---------|-------------------------|----------------------|
| **Hardcoded `.width(N)`** on columns | **~150+** usages across league, inbox, jobs, finances, player detail, etc. | Column stays **N scene units** while text grows → **wrapping, clipping, misaligned tables** |
| **`setFontScale(x)` on titles** | **~35** usages (e.g. `MatchEngineDebugScreen`, `FinancesScreenTable`) | **Multiplies** on top of skin scale → **inconsistent heading vs body** (e.g. 1.5 × 1.5 = 2.25× on some titles only) |
| **`pad(50)` / layout constants** | Settings, New Manager, many tables | Padding **fixed** in code; may look too tight vs scaled text |
| **`ToastManager` screen padding** | `setScreenPadding(10, 60)` hardcoded | Toasts may overlap menu at high scale |
| **Non-VisUI widgets** | `MatchEngineScreen` uses `new BitmapFont()` + LibGDX `Label` | **Unchanged** by VisUI — overlay text stays tiny |
| **Match pitch rendering** | `FitViewport` + Box2D world (`MatchEngineScreen`) | **Separate coordinate system** — scaling UI must not scale field physics (expected) |

### Architecture note: many Stages, one pattern

| Screen type | Stage setup | Scales with VisUI-only? |
|-------------|-------------|-------------------------|
| Menu, Settings, New Game*, Main game | `new Stage(new ScreenViewport())` | Partial (skin only) |
| Main game content | Single `MainGameScreen` stage; tables are `VisTable` children | Partial |
| Dialogs (`VisDialog`) | Shown on parent stage | Partial (same skin) |
| Toasts | `ToastManager(stage)` on main game stage | Partial |
| Match engine UI overlay | Separate `uiStage` + default `BitmapFont` | **No** |
| Match pitch | `FitViewport` + `SpriteBatch` | N/A (game world, not management UI) |

**Conclusion:** VisUI-only is closer to “change the theme font size” than “zoom the whole app like Chrome.”

---

## 4. Screen-by-screen coverage matrix (expected at 150%)

Legend: ✅ Good · ⚠️ Readable but layout issues · ❌ Won’t scale · N/A Not management UI

| Screen / area | Primary UI tech | VisUI-only PU-10a | PU-10-R (viewport + VisUI) |
|---------------|-----------------|-------------------|----------------------------|
| Main menu | VisUI / ScreenViewport | ⚠️ | ✅ |
| Settings | VisUI | ⚠️ | ✅ |
| New game flow (4 screens) | VisUI | ⚠️ | ✅ |
| Main game shell (top menu + side nav) | VisUI | ⚠️ | ✅ |
| Inbox, draw, league, cup, finances | VisUI + many `.width()` | ⚠️ | ✅ (verify wide tables) |
| Player / club detail | VisUI + `.width()` + `setFontScale` | ⚠️ | ⚠️ (verify attribute grids) |
| Job board / applications | VisUI tables | ⚠️ | ✅ |
| Save / load dialogs | `VisDialog` | ⚠️ | ✅ |
| Toasts | VisUI `ToastManager` | ⚠️ | ✅ (adjust padding helper) |
| Match engine debug | VisUI + heavy `setFontScale` | ⚠️ | ⚠️ |
| **Match engine (in-match)** | Box2D + `BitmapFont` Label | ❌ overlay | ⚠️ (needs explicit uiStage scale) |
| Stub main/* tables (Home, Tactics…) | VisUI placeholders | ⚠️ | ✅ |

**We are not sure all screens scale properly until QA walks this matrix** — especially wide data tables (league standings, finances, squad list).

---

## 5. Revised approach — PU-10-R (browser-like parity)

To match browser/Cursor behaviour, apply **one global UI scale factor** at the **Stage viewport** (or stage root), not only at skin load.

### 5.1 Central helper (new)

```java
public final class UiScale {
    public static ScreenViewport newViewport() {
        ScreenViewport vp = new ScreenViewport();
        vp.setUnitsPerPixel(1f / Futtoboru.UI_SCALE);
        return vp;
    }
    public static void updateViewport(Stage stage, int width, int height) {
        Viewport vp = stage.getViewport();
        if (vp instanceof ScreenViewport) {
            ((ScreenViewport) vp).setUnitsPerPixel(1f / Futtoboru.UI_SCALE);
        }
        vp.update(width, height, true);
    }
}
```

**Effect:** Same as shrinking the logical resolution inside the window — **text, tables, padding, and `.width(100)` all grow together** (browser-like).

Alternative equivalent: `stage.getRoot().setScale(UI_SCALE, UI_SCALE)` with camera adjustment — viewport approach is usually cleaner for input.

### 5.2 Still use VisUI.setScale (optional but recommended)

Call **`VisUI.setScale(UI_SCALE)` before `VisUI.load()`** so skin assets match viewport scale (crisper integer scales: 125%, 150%, 200%).

### 5.3 Settings apply flow

On UI scale change in Settings:

1. Save pref (same as resolution)
2. Update `Futtoboru.UI_SCALE`
3. **`VisUI.dispose()` / `VisUI.setScale()` / `VisUI.load()`**
4. **`changeScreen(currentScreenId)`** to rebuild active screen (same as proposed)
5. On every `Screen.resize()`, call `UiScale.updateViewport(stage, w, h)`

### 5.4 Follow-up cleanup (PU-10-R2, not blocking MVP)

| Item | Why |
|------|-----|
| Audit / reduce `setFontScale` on titles | Avoid double-scaling vs global factor |
| Replace hardcoded `.width()` with `prefWidth` / `%` columns where tables break | League/finances at 200% |
| `MatchEngineScreen`: use `VisLabel` or scale `uiStage` viewport | Overlay parity |
| `ToastManager.setScreenPadding(10 * scale, 60 * scale)` | Avoid overlap |
| HiDPI in `DesktopLauncher` | Complements in-app scale on Windows |

### 5.5 What PU-10-R does **not** need to scale

- **Match pitch** (Box2D world) — only the **overlay** and **management UI** should zoom; pitch can stay world-accurate.
- **Headless tests** — unaffected.

---

## 6. Comparison table

| Approach | Browser-like? | All screens? | Effort | Risk |
|----------|---------------|------------|--------|------|
| **A. VisUI.setScale only** (original PU-10a) | ❌ Partial | ⚠️ Most, not all | Small | Layout breaks on wide tables |
| **B. Viewport `unitsPerPixel` only** | ✅ Yes | ✅ All stages using helper | Medium | Must touch ~10 Screen constructors + resize |
| **C. PU-10-R: B + VisUI.setScale** | ✅ Yes | ✅ + crisper skin | Medium | **Recommended** |
| **D. OS / Java DPI only** | ⚠️ Whole window bitmap | ✅ | Small | Blurry; not user-controlled in Settings |

---

## 7. Acceptance criteria (before PO signs off PU-10)

Manual QA on **Galaxy Book3 Pro** (or equivalent HiDPI) at **125% Windows scale**:

| # | Check |
|---|--------|
| 1 | 100% in-game UI scale matches today’s baseline on 1080p external monitor |
| 2 | 150% — main menu, in-game top bar, Continue button readable without OS Magnifier |
| 3 | 150% — league standings + inbox + draw screen: no clipped columns (scroll OK) |
| 4 | 150% — save/load dialog buttons clickable, text readable |
| 5 | 200% — no soft-lock; tables still navigable |
| 6 | Change scale in Settings → visible change **without** restart (screen rebuild OK) |
| 7 | Pref persists across `build-and-run` |
| 8 | Match engine overlay text readable OR documented exception |

---

## 8. Decision summary

| If PO wants… | Choose… |
|--------------|---------|
| “Good enough for QA laptop quickly” | PU-10a might **unblock QA** but **not** browser parity — **layout surprises likely** |
| “Works like browser / Cursor zoom” | **PU-10-R** (viewport helper + Settings + VisUI.setScale) |
| “100% certainty all screens” | PU-10-R **plus** acceptance matrix (§7) — **no shortcut** |

**Honest answer:** The originally proposed VisUI-only solution is **not** equivalent to browser or Cursor scaling and **we cannot guarantee** all screens without the viewport-based approach and a focused QA pass.
