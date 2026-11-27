package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Global UI zoom via {@link ScreenViewport#setUnitsPerPixel(float)} so layout, drawing, and
 * hit-testing stay aligned. Use {@link #addOverflowScroll} on wide roots when scale clips content.
 */
public final class UiScale {

    public static final float[] PRESET_SCALES = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

    /** stored UI scale meaning "follow the monitor and the window" (the default) */
    public static final float AUTO = 0f;

    /** Auto keeps at least this logical window (DarkBlade AJU-12: half of 1920x1080) */
    public static final int MIN_LOGICAL_WIDTH = 960;
    public static final int MIN_LOGICAL_HEIGHT = 540;

    /** Auto never goes above this */
    public static final float MAX_AUTO_SCALE = 3f;

    private UiScale() {
    }

    public static boolean isAuto(float stored) {
        return stored <= 0f;
    }

    /** the zoom used now: the chosen preset, or the Auto scale for the current window */
    public static float currentScale() {
        if (!isAuto(Futtoboru.UI_SCALE)) {
            return Futtoboru.UI_SCALE;
        }
        if (Gdx.graphics == null) {
            return 1f;
        }
        return autoScale(Futtoboru.SYSTEM_UI_SCALE, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
    }

    /**
     * Auto: a HiDPI monitor (the OS scale, e.g. 200% on a 2880x1800 laptop) is followed as is; on a 100% monitor,
     * x2 when the window still leaves 960x540 logical pixels (from 1920x1080), else x1, as DarkBlade's AJU-12
     */
    public static float autoScale(float systemScale, int width, int height) {
        if (systemScale > 1.01f) {
            return Math.min(MAX_AUTO_SCALE, systemScale);
        }
        return width / 2 >= MIN_LOGICAL_WIDTH && height / 2 >= MIN_LOGICAL_HEIGHT ? 2f : 1f;
    }

    public static ScreenViewport newViewport() {
        ScreenViewport viewport = new ScreenViewport();
        applyUnitsPerPixel(viewport);
        return viewport;
    }

    public static void applyUnitsPerPixel(ScreenViewport viewport) {
        viewport.setUnitsPerPixel(1f / currentScale());
    }

    public static void updateStageViewport(Stage stage, int width, int height) {
        Viewport viewport = stage.getViewport();
        if (viewport instanceof ScreenViewport) {
            applyUnitsPerPixel((ScreenViewport) viewport);
        }
        viewport.update(width, height, true);
    }

    /**
     * Scroll overflow within the scaled viewport. Does not apply {@code setScale} — safe with
     * {@code unitsPerPixel} zoom. Content must not use {@code setFillParent(true)}.
     */
    public static VisScrollPane newOverflowScrollPane(Actor content) {
        VisScrollPane scroll = new VisScrollPane(content);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        return scroll;
    }

    public static VisScrollPane addOverflowScroll(Stage stage, Actor content) {
        VisScrollPane scroll = newOverflowScrollPane(content);
        scroll.setFillParent(true);
        stage.addActor(scroll);
        return scroll;
    }

    public static String formatPercent(float scale) {
        return Math.round(scale * 100f) + "%";
    }

    public static float parsePercent(String value) {
        if (value == null || value.isEmpty()) {
            return 1f;
        }
        return Float.parseFloat(value.replace("%", "").trim()) / 100f;
    }

    public static String nearestPresetLabel(float scale) {
        float nearest = PRESET_SCALES[0];
        float bestDiff = Math.abs(scale - nearest);
        for (float preset : PRESET_SCALES) {
            float diff = Math.abs(scale - preset);
            if (diff < bestDiff) {
                bestDiff = diff;
                nearest = preset;
            }
        }
        return formatPercent(nearest);
    }

    public static int scaled(int pixels) {
        return Math.round(pixels * currentScale());
    }
}
