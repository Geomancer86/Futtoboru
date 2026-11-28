package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Sets a batch tint from a 0xRRGGBB palette colour without allocating. The viewer's colours are its art palette
 * ({@link ArtMath}), not UI theme tokens; every tint of the match passes goes through here.
 */
final class ViewColors {

    private ViewColors() {
    }

    private static final float FULL = 1f;

    /** No tint (sprites drawn as baked). */
    static void white(Batch batch) {
        batch.setColor(FULL, FULL, FULL, FULL);
    }

    /** No tint, at an alpha (overlays). */
    static void white(Batch batch, float alpha) {
        batch.setColor(FULL, FULL, FULL, alpha);
    }

    /** Clears the screen to a palette colour (debug viewer). */
    static void clear(int rgb) {
        ScreenUtils.clear(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, FULL);
    }

    static void set(Batch batch, int rgb, float alpha) {
        batch.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }
}
