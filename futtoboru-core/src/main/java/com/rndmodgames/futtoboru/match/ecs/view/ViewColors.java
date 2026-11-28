package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.gdx.graphics.g2d.Batch;

/** Sets a batch tint from a 0xRRGGBB palette colour without allocating. */
final class ViewColors {

    private ViewColors() {
    }

    static void set(Batch batch, int rgb, float alpha) {
        batch.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }
}
