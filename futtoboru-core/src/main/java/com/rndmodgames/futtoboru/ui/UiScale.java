package com.rndmodgames.futtoboru.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.widget.VisScrollPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.rndmodgames.futtoboru.game.Futtoboru;

/**
 * Global UI zoom: full-pixel viewport (1:1) with scaled content inside {@link VisScrollPane} roots.
 */
public final class UiScale {

    public static final float[] PRESET_SCALES = {1.0f, 1.25f, 1.5f, 1.75f, 2.0f};

    private UiScale() {
    }

    public static float currentScale() {
        return Futtoboru.UI_SCALE > 0f ? Futtoboru.UI_SCALE : 1f;
    }

    /** 1:1 screen pixels — no {@code unitsPerPixel} zoom. */
    public static ScreenViewport newViewport() {
        return new ScreenViewport();
    }

    public static void updateStageViewport(Stage stage, int width, int height) {
        Viewport viewport = stage.getViewport();
        viewport.update(width, height, true);
        refreshScrollableRoots(stage, viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    /**
     * Adds a scrollable, scaled root that fills the viewport at 100% and scrolls when zoom exceeds the window.
     */
    public static VisScrollPane addScrollableRoot(Stage stage, Actor content) {
        return addScrollableRoot(stage, content, true);
    }

    /**
     * @param fillViewport when {@code true}, content is laid out to the current world size;
     *                       when {@code false}, content keeps its natural packed size (wide forms).
     */
    public static VisScrollPane addScrollableRoot(Stage stage, Actor content, boolean fillViewport) {
        float worldW = stage.getViewport().getWorldWidth();
        float worldH = stage.getViewport().getWorldHeight();
        VisScrollPane scroll = createScrollableRoot(content, worldW, worldH, fillViewport);
        stage.addActor(scroll);
        return scroll;
    }

    static VisScrollPane createScrollableRoot(Actor content, float layoutWidth, float layoutHeight,
            boolean fillViewport) {
        ScrollRootLayout layout = new ScrollRootLayout(content, layoutWidth, layoutHeight, fillViewport);
        layout.relayout();

        VisScrollPane scroll = new VisScrollPane(layout.wrapper);
        scroll.setFillParent(true);
        scroll.setFadeScrollBars(false);
        scroll.setOverscroll(false, false);
        scroll.setUserObject(layout);
        return scroll;
    }

    static void refreshScrollableRoots(Stage stage, float worldWidth, float worldHeight) {
        for (Actor child : stage.getRoot().getChildren()) {
            if (!(child instanceof VisScrollPane)) {
                continue;
            }
            Object meta = child.getUserObject();
            if (meta instanceof ScrollRootLayout) {
                ScrollRootLayout layout = (ScrollRootLayout) meta;
                if (layout.fillViewport) {
                    layout.layoutWidth = worldWidth;
                    layout.layoutHeight = worldHeight;
                    layout.relayout();
                }
            }
        }
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

    static final class ScrollRootLayout {
        final Actor content;
        final VisTable wrapper;
        float layoutWidth;
        float layoutHeight;
        final boolean fillViewport;

        ScrollRootLayout(Actor content, float layoutWidth, float layoutHeight, boolean fillViewport) {
            this.content = content;
            this.layoutWidth = layoutWidth;
            this.layoutHeight = layoutHeight;
            this.fillViewport = fillViewport;
            this.wrapper = new VisTable();
        }

        void relayout() {
            float scale = currentScale();
            content.setOrigin(Align.bottomLeft);
            content.setScale(scale, scale);

            wrapper.clear();
            wrapper.top().left();

            if (fillViewport) {
                wrapper.add(content).width(layoutWidth).height(layoutHeight);
                wrapper.setSize(layoutWidth * scale, layoutHeight * scale);
            } else {
                wrapper.add(content);
                wrapper.pack();
                wrapper.setSize(wrapper.getWidth() * scale, wrapper.getHeight() * scale);
            }
            wrapper.invalidateHierarchy();
        }
    }
}
