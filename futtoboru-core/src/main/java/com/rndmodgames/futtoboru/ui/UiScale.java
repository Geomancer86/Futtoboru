package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Global UI zoom (browser-like): one scale factor for layout + text on a {@link Stage}.
 */
public final class UiScale {

    public static final float[] PRESET_SCALES = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

    private UiScale() {
    }

    public static float currentScale() {
        return Futtoboru.UI_SCALE > 0f ? Futtoboru.UI_SCALE : 1f;
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
