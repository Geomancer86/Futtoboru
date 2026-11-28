package com.rndmodgames.futtoboru.match.ecs.view;

/**
 * Palette and pixel helpers shared by the code-generated match art (pitch, skyline, placeholders). Colours come from
 * {@code tools/art/ground_art.py} so the match matches the ground illustrations; the hash, value noise and 4x4 Bayer
 * dither follow the producer-approved art prototype (D26). Pure Java, deterministic, no libGDX.
 */
public final class ArtMath {

    // ground_art.py PAL (muted, Victorian), as 0xRRGGBB
    public static final int INK = 0x221E1E;
    public static final int SOOT = 0x342F2E;
    public static final int COAL = 0x46403E;
    public static final int SLATE = 0x545860;
    public static final int SLATE_HI = 0x70747A;
    public static final int STONE = 0x968E80;
    public static final int STONE_HI = 0xB2AA98;
    public static final int BRICK = 0x804636;
    public static final int BRICK_DK = 0x60362C;
    public static final int BRICK_HI = 0x985C46;
    public static final int WOOD = 0x6E5034;
    public static final int WOOD_DK = 0x4E3826;
    public static final int CREAM = 0xE2DAC4;
    public static final int GRASS = 0x5C7842;
    public static final int GRASS_HI = 0x6E8A4C;
    public static final int GRASS_DK = 0x4A6438;
    public static final int MUD = 0x68583E;
    public static final int HILL = 0x707C68;
    public static final int HILL_FAR = 0x8C9486;
    public static final int SKIN = 0xCEA888;
    public static final int SMOKE = 0xA8A4A0;
    public static final int SMOKE_DK = 0x807C7A;
    public static final int RED = 0x96342E;
    public static final int NAVY = 0x2C3456;
    public static final int WHITE = 0xE8E4D6;
    public static final int BLUE = 0x4A6296;
    public static final int BLACK = 0x1C1A1C;
    public static final int GLASS = 0xC4BA8C;
    /** Pitch line chalk. */
    public static final int LINE = 0xECE8DA;
    /** Ball leather (BA2). */
    public static final int LEATHER = 0x986236;

    private static final int[][] BAYER = { { 0, 8, 2, 10 }, { 12, 4, 14, 6 }, { 3, 11, 1, 9 }, { 15, 7, 13, 5 } };

    private ArtMath() {
    }

    /** 4x4 ordered dither threshold at a pixel, in (0, 1). */
    public static double dither(int px, int py) {
        return (BAYER[py & 3][px & 3] + 0.5) / 16.0;
    }

    /** Integer hash of a lattice point and a seed, in [0, 1). */
    public static double hash(int i, int j, int seed) {
        int h = (i * 374761393) ^ (j * 668265263) ^ (seed * 2147483647);
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFFFFL) / 4294967296.0;
    }

    /** Smooth value noise in [0, 1). */
    public static double noise(double x, double y, int seed) {
        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);
        double fx = x - i;
        double fy = y - j;
        double a = hash(i, j, seed);
        double b = hash(i + 1, j, seed);
        double c = hash(i, j + 1, seed);
        double d = hash(i + 1, j + 1, seed);
        double ux = fx * fx * (3 - 2 * fx);
        double uy = fy * fy * (3 - 2 * fy);
        return a + (b - a) * ux + (c - a) * uy + (a - b - c + d) * ux * uy;
    }

    /** Linear mix of two colours (0xRRGGBB). */
    public static int mix(int a, int b, double t) {
        int r = (int) Math.round(red(a) + (red(b) - red(a)) * t);
        int g = (int) Math.round(green(a) + (green(b) - green(a)) * t);
        int bl = (int) Math.round(blue(a) + (blue(b) - blue(a)) * t);
        return rgb(r, g, bl);
    }

    /** A colour times a factor (clamped). */
    public static int mul(int c, double f) {
        return rgb((int) Math.round(red(c) * f), (int) Math.round(green(c) * f), (int) Math.round(blue(c) * f));
    }

    public static int rgb(int r, int g, int b) {
        return (clamp8(r) << 16) | (clamp8(g) << 8) | clamp8(b);
    }

    public static int red(int c) {
        return (c >> 16) & 0xFF;
    }

    public static int green(int c) {
        return (c >> 8) & 0xFF;
    }

    public static int blue(int c) {
        return c & 0xFF;
    }

    /** 0xRRGGBB to libGDX RGBA8888 (opaque). */
    public static int rgba8888(int c) {
        return (c << 8) | 0xFF;
    }

    private static int clamp8(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }

    static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
