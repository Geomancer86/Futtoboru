package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisScrollPane;

/**
 * U-03a / D18: one UI scale for every {@link Stage}. The scale is applied with
 * {@link ScreenViewport#setUnitsPerPixel(float)}, so skin, fonts, theme pads and hit-testing grow together and the
 * layout keeps working in "design pixels" (1280x720 at 1x).
 *
 * <p>Only integer steps (1x, 2x): the skin atlas is filtered {@code Nearest}, so 1.25x or 1.5x would blur or shimmer.
 * {@link #AUTO} (the default) follows the monitor's OS content scale rounded to 1 or 2 (Windows 200% gives 2x), else
 * 2x when the window still leaves {@value #MIN_LOGICAL_WIDTH}x{@value #MIN_LOGICAL_HEIGHT} logical pixels.
 *
 * <p>Every screen builds its stage with {@code new Stage(UiScale.newViewport())} and calls
 * {@link #updateStageViewport(Stage, int, int)} from {@code resize}. Content that may not fit at 2x goes in
 * {@link #addOverflowScroll(Stage, Actor)} (or one of the overflow scroll panes) instead of {@code setFillParent}.
 */
public final class UiScale {

    /** stored setting meaning "follow the monitor and the window" (the default) */
    public static final float AUTO = 0f;

    /** the fixed steps offered in Settings next to Auto */
    public static final float[] PRESET_SCALES = {1f, 2f};

    /** D18: smallest supported window, in window pixels */
    public static final int MIN_WIDTH = 1280;
    public static final int MIN_HEIGHT = 720;
    public static final String DEFAULT_RESOLUTION = MIN_WIDTH + "x" + MIN_HEIGHT;

    /** Auto picks 2x on a 100% monitor only when this logical window remains (half of 1920x1080) */
    public static final int MIN_LOGICAL_WIDTH = 960;
    public static final int MIN_LOGICAL_HEIGHT = 540;

    /** the saved setting: {@link #AUTO} or one of {@link #PRESET_SCALES} */
    private static float setting = AUTO;

    /** the monitor's OS content scale (2.0 at Windows 200%), set by the desktop launcher; 1 when unknown */
    private static float systemScale = 1f;

    private UiScale() {
    }

    public static boolean isAuto(float stored) {
        return stored <= 0f;
    }

    /** the saved setting: {@link #AUTO} or a fixed step */
    public static float getSetting() {
        return setting;
    }

    /** sets the setting; anything that is not Auto snaps to the nearest integer step */
    public static void setSetting(float stored) {
        setting = normalize(stored);
    }

    /** {@link #AUTO} for Auto (zero, negative, unparsable), else the nearest step of {@link #PRESET_SCALES} */
    public static float normalize(float stored) {
        if (isAuto(stored) || Float.isNaN(stored)) {
            return AUTO;
        }
        return stored >= 1.5f ? 2f : 1f;
    }

    public static float getSystemScale() {
        return systemScale;
    }

    public static void setSystemScale(float scale) {
        systemScale = scale > 0f && !Float.isNaN(scale) ? scale : 1f;
    }

    /** the scale in use now: the fixed step, or what Auto picks for this monitor and window */
    public static float currentScale() {
        if (!isAuto(setting)) {
            return setting;
        }
        return currentAutoScale();
    }

    /** what Auto picks for this monitor and window right now (1 without a window, e.g. headless tests) */
    public static float currentAutoScale() {
        if (Gdx.graphics == null || Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) {
            return 1f;
        }
        float backBufferScale = Gdx.graphics.getBackBufferWidth() / (float) Gdx.graphics.getWidth();
        return autoScale(systemScale, backBufferScale, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Auto: the monitor's OS scale rounded to 1 or 2 (a 2880x1800 laptop at Windows 200% gives 2x whatever the
     * window); on a 100% monitor 2x when the window keeps {@value #MIN_LOGICAL_WIDTH}x{@value #MIN_LOGICAL_HEIGHT}
     * logical pixels at 2x, else 1x.
     *
     * @param systemScale     the monitor's OS content scale (1 = 100%)
     * @param backBufferScale back-buffer pixels per window unit: 2 on macOS Retina, where the OS already scales the
     *                        window and the OS scale must not be applied twice; 1 on Windows and Linux
     * @param width           window width in window units (what {@code resize} gets)
     * @param height          window height in window units
     */
    public static float autoScale(float systemScale, float backBufferScale, int width, int height) {
        float notYetApplied = systemScale / Math.max(1f, backBufferScale);
        if (Math.round(notYetApplied) >= 2) {
            return 2f;
        }
        return width / 2 >= MIN_LOGICAL_WIDTH && height / 2 >= MIN_LOGICAL_HEIGHT ? 2f : 1f;
    }

    /** a {@link ScreenViewport} at the current scale; use for every UI stage */
    public static ScreenViewport newViewport() {
        ScreenViewport viewport = new ScreenViewport();
        apply(viewport);
        return viewport;
    }

    public static void apply(ScreenViewport viewport) {
        viewport.setUnitsPerPixel(1f / currentScale());
    }

    /** call from {@code Screen.resize}: re-reads the scale (Auto depends on the window) and updates the viewport */
    public static void updateStageViewport(Stage stage, int width, int height) {
        Viewport viewport = stage.getViewport();
        if (viewport instanceof ScreenViewport) {
            apply((ScreenViewport) viewport);
        }
        viewport.update(width, height, true);
    }

    /**
     * A scroll pane that scrolls only when its content is larger than the (scaled) window, both ways. It never uses
     * {@code setScale}, so it is safe with the viewport zoom. The content must not use {@code setFillParent(true)}:
     * the pane gives it at least its own size, so the content still fills and aligns as before when it fits.
     */
    public static VisScrollPane newOverflowScrollPane(Actor content) {
        VisScrollPane scroll = new VisScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setFlickScroll(false);
        return scroll;
    }

    /**
     * Like {@link #newOverflowScrollPane(Actor)} but only sideways: the content keeps the pane's height, so the
     * content's own vertical scroll panes keep working.
     */
    public static VisScrollPane newWideOverflowScrollPane(Actor content) {
        VisScrollPane scroll = newOverflowScrollPane(content);
        scroll.setScrollingDisabled(false, true);
        return scroll;
    }

    /** the screen root: {@code content} fills the stage and scrolls when it does not fit (instead of fill-parent) */
    public static VisScrollPane addOverflowScroll(Stage stage, Actor content) {
        VisScrollPane scroll = newOverflowScrollPane(content);
        scroll.setFillParent(true);
        stage.addActor(scroll);
        return scroll;
    }

    /** "100%", "200%" */
    public static String formatPercent(float scale) {
        return Math.round(scale * 100f) + "%";
    }

    /**
     * A saved "WIDTHxHEIGHT" as a window size of at least {@value #MIN_WIDTH}x{@value #MIN_HEIGHT} (D18); a missing,
     * malformed or smaller value (an old "640x480") gives the default 1280x720.
     */
    public static int[] windowSize(String resolution) {
        if (resolution != null) {
            String[] parts = resolution.trim().toLowerCase().split("x");
            if (parts.length == 2) {
                try {
                    int width = Integer.parseInt(parts[0].trim());
                    int height = Integer.parseInt(parts[1].trim());
                    if (width >= MIN_WIDTH && height >= MIN_HEIGHT) {
                        return new int[] {width, height};
                    }
                } catch (NumberFormatException malformed) {
                    // the default below
                }
            }
        }
        return new int[] {MIN_WIDTH, MIN_HEIGHT};
    }

    /** {@link #windowSize(String)} as "WIDTHxHEIGHT" */
    public static String sanitizeResolution(String resolution) {
        int[] size = windowSize(resolution);
        return size[0] + "x" + size[1];
    }
}
