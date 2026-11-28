package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.gdx.graphics.g2d.Batch;

/**
 * Picks which baked sheet scale to draw and how big, so sprites stay crisp (no non-integer smearing): every texel of
 * the chosen sheet covers a whole number of <b>screen</b> pixels. The match batch works in art pixels, each
 * {@code magnification} screen pixels wide ({@link ViewCamera}); a sprite wanted at {@code p} art pixels per metre is
 * wanted at {@code p * magnification} screen pixels per metre.
 *
 * <ul>
 * <li><b>Default view (D28, 10 px/m)</b>: the view's halfway scale times the magnification is at least
 * {@link #FINE_PIXELS_PER_METRE}. Then every sprite uses the finest sheet it has (players, officials, people, ball:
 * 2x everywhere, far side included) at {@code t = round(wanted / 10)} screen pixels per 2x texel (at least 1); a
 * 1x-only sheet (crowd, props) draws at {@code 2t} per texel, so it matches the 2x figures beside it.</li>
 * <li><b>Wide view</b> (5 px/m, zoomed out below the default): of the baked sheets (1x = 5 px/m, 2x = 10 px/m) and
 * integer texel sizes, the one closest to the wanted size (by ratio) wins; on a tie the finer sheet.</li>
 * </ul>
 *
 * <p>A choice is one int ({@code sheetScale * 256 + texelScreenPixels}); zero allocation.
 */
public final class SpriteScale {

    /** Pixels per metre of a 1x sheet (S1). */
    public static final double BASE_PIXELS_PER_METRE = 5.0;
    /** Screen pixels per metre of a 2x sheet at one screen pixel per texel: the default view's floor (D28). */
    public static final double FINE_PIXELS_PER_METRE = 10.0;

    private SpriteScale() {
    }

    /**
     * @param artPixelsPerMetre perspective scale at the sprite's depth ({@link ViewCamera#scale(double)})
     * @param magnification     screen pixels per art pixel (at least 1)
     * @param maxSheet          finest sheet scale available (1 or 2)
     * @return the choice: {@link #sheet(int)} and {@link #texel(int)}
     */
    public static int choose(double artPixelsPerMetre, int magnification, int maxSheet) {
        return choose(artPixelsPerMetre, magnification, maxSheet, ViewCamera.WIDE_PIXELS_PER_METRE);
    }

    /**
     * As {@link #choose(double, int, int)}, for a view whose halfway scale is {@code viewPixelsPerMetre}
     * ({@link ViewCamera#pixelsPerMetre()}): at the default scale the finest sheet always (see the class comment).
     */
    public static int choose(double artPixelsPerMetre, int magnification, int maxSheet, double viewPixelsPerMetre) {
        int m = Math.max(1, magnification);
        double wanted = Math.max(1e-3, artPixelsPerMetre * m);
        if (viewPixelsPerMetre * m >= FINE_PIXELS_PER_METRE - 1e-9) {
            int fine = Math.max(1, (int) Math.round(wanted / FINE_PIXELS_PER_METRE));
            int s = Math.max(1, Math.min(2, maxSheet));
            return s * 256 + fine * (2 / s);
        }
        int best = 0;
        double bestErr = Double.POSITIVE_INFINITY;
        for (int s = Math.max(1, maxSheet); s >= 1; s--) {
            double base = BASE_PIXELS_PER_METRE * s;
            int t0 = Math.max(1, (int) Math.floor(wanted / base));
            for (int t = t0; t <= t0 + 1; t++) {
                double err = Math.abs(Math.log(base * t / wanted));
                if (err < bestErr - 1e-9) {
                    bestErr = err;
                    best = s * 256 + t;
                }
            }
        }
        return best;
    }

    /** Sheet scale of a choice (1 or 2). */
    public static int sheet(int choice) {
        return choice >> 8;
    }

    /** Screen pixels per texel of a choice. */
    public static int texel(int choice) {
        return choice & 0xFF;
    }

    /** Art pixels per metre a choice actually draws at. */
    public static double drawnPixelsPerMetre(int choice, int magnification) {
        return BASE_PIXELS_PER_METRE * sheet(choice) * texel(choice) / Math.max(1, magnification);
    }

    /** Snaps an art-pixel coordinate to the screen-pixel grid. */
    public static float snap(double art, int magnification) {
        int m = Math.max(1, magnification);
        return Math.round(art * m) / (float) m;
    }

    /**
     * Draws a sprite with its feet pivot at (sx, sy) in art pixels, each texel {@code texel} screen pixels: the pivot is
     * snapped to the screen grid, so every texel edge is a screen pixel edge.
     */
    public static void draw(Batch batch, ActorSprites.Sprite s, double sx, double sy, int texel, int magnification) {
        int m = Math.max(1, magnification);
        float k = texel / (float) m;
        float px = snap(sx, m);
        float py = snap(sy, m);
        batch.draw(s.region, px - s.pivotX * k, py - s.footY * k, s.width * k, s.height * k);
    }
}
