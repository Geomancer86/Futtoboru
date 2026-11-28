package com.rndmodgames.futtoboru.match.ecs.view;

import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.dither;
import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.mix;

import java.util.Random;

/**
 * Skyline K1 (D26): a procedural mill town behind the far side of the ground, generated once per ground: dithered sky,
 * far hills, terraces with lit windows, mills, tall chimneys and a church spire. The strip wraps horizontally (its
 * last column joins its first) so the skyline pass can scroll it with parallax forever. The chimney tops are kept for
 * the smoke drawn every frame.
 *
 * <p>Pure Java (an RGBA array, transparent nowhere), deterministic for a seed (per ground); no libGDX. Row 0 is the
 * top of the sky, the last row the ground line where the far terrace meets the pitch surround.
 */
public final class SkylineArt {

    public static final int WIDTH = 1024;
    public static final int HEIGHT = 128;

    /** Autumn sky, top and horizon (ground_art.py SKIES). */
    public static final int SKY_TOP = 0x96A0AA;
    public static final int SKY_HORIZON = 0xD6C8AC;

    public final int width = WIDTH;
    public final int height = HEIGHT;
    /** 0xRRGGBB per pixel, row-major from the top left. */
    public final int[] rgb = new int[WIDTH * HEIGHT];
    /** Chimney tops: x (column) and y (row, from the top) pairs. */
    public final int[] chimneys;
    public final int chimneyCount;

    public SkylineArt(long seed) {
        Random r = new Random(seed);
        int[] tops = new int[64];
        int count = 0;
        sky();
        hills(r.nextInt(1000));
        int base = HEIGHT - 1;
        // terraces: a continuous row of slate roofs
        for (int x = 0; x < WIDTH; x += 14) {
            int h = 6 + (x * 7) % 5;
            rect(x, base - h, 13, h, ArtMath.SLATE);
            rect(x, base - h - 1, 13, 1, ArtMath.SLATE_HI);
            for (int w = 2; w < 12; w += 4) {
                if (r.nextInt(3) > 0) {
                    rect(x + w, base - h + 2, 1, 1, ArtMath.GLASS);
                }
            }
        }
        // mills, each with a chimney
        int x = r.nextInt(40);
        while (x < WIDTH - 10 && count < tops.length / 2) {
            int mw = 14 + r.nextInt(16);
            int mh = 12 + r.nextInt(10);
            rect(x, base - mh, mw, mh, ArtMath.BRICK_DK);
            rect(x, base - mh, mw, 1, ArtMath.BRICK);
            for (int wy = base - mh + 3; wy < base - 2; wy += 4) {
                for (int wx = x + 2; wx < x + mw - 1; wx += 3) {
                    rect(wx, wy, 1, 2, ArtMath.GLASS);
                }
            }
            int cx = x + mw - 4 - r.nextInt(Math.max(1, mw - 6));
            int ch = mh + 12 + r.nextInt(14);
            rect(cx, base - ch, 3, ch - mh, ArtMath.BRICK);
            rect(cx - 1, base - ch, 5, 1, ArtMath.BRICK_HI);
            tops[count * 2] = Math.floorMod(cx + 1, WIDTH);
            tops[count * 2 + 1] = base - ch - 1;
            count++;
            x += mw + 30 + r.nextInt(90);
        }
        // a church spire or two
        int spires = 1 + r.nextInt(2);
        for (int s = 0; s < spires; s++) {
            int sx = r.nextInt(WIDTH);
            int tower = 16 + r.nextInt(6);
            rect(sx, base - tower, 6, tower, ArtMath.STONE);
            rect(sx + 5, base - tower, 1, tower, ArtMath.mul(ArtMath.STONE, 0.8));
            rect(sx + 2, base - tower + 4, 2, 3, ArtMath.SOOT); // belfry opening
            // the spire tapers from the tower's width to a point
            for (int i = 0; i < 14; i++) {
                int w = Math.max(1, 6 - (i * 6) / 14);
                rect(sx + (6 - w) / 2, base - tower - 1 - i, w, 1, i % 4 == 0 ? ArtMath.STONE_HI : ArtMath.SLATE);
            }
        }
        this.chimneys = java.util.Arrays.copyOf(tops, count * 2);
        this.chimneyCount = count;
    }

    private void sky() {
        for (int y = 0; y < HEIGHT; y++) {
            double f = (double) y / HEIGHT;
            for (int x = 0; x < WIDTH; x++) {
                // four dithered bands from the top colour to the horizon
                double band = Math.floor(f * 4) / 4;
                double next = Math.min(1, band + 0.25);
                double within = (f - band) * 4;
                double t = dither(x, y) < within ? next : band;
                rgb[y * WIDTH + x] = mix(SKY_TOP, SKY_HORIZON, t);
            }
        }
    }

    /** Far hills: a periodic ridge (whole sine periods across the strip, so it wraps). */
    private void hills(int phase) {
        for (int x = 0; x < WIDTH; x++) {
            double a = 2 * Math.PI * x / WIDTH;
            double ridge = 4 * Math.sin(3 * a + phase) + 3 * Math.sin(7 * a + phase * 0.37) + 1.5 * Math.sin(17 * a);
            int top = (int) Math.round(HEIGHT - 22 - ridge);
            for (int y = Math.max(0, top); y < HEIGHT; y++) {
                rgb[y * WIDTH + x] = y > HEIGHT - 12 ? ArtMath.HILL : ArtMath.HILL_FAR;
            }
        }
    }

    /** A filled rectangle, wrapping horizontally. */
    private void rect(int x, int y, int w, int h, int col) {
        for (int yy = Math.max(0, y); yy < Math.min(HEIGHT, y + h); yy++) {
            for (int xx = x; xx < x + w; xx++) {
                rgb[yy * WIDTH + Math.floorMod(xx, WIDTH)] = col;
            }
        }
    }

    public int at(int x, int y) {
        return rgb[y * WIDTH + Math.floorMod(x, WIDTH)];
    }
}
