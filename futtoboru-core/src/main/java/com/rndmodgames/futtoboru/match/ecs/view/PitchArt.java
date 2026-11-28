package com.rndmodgames.futtoboru.match.ecs.view;

import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.dither;
import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.mix;
import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.mul;
import static com.rndmodgames.futtoboru.match.ecs.view.ArtMath.noise;

import com.rndmodgames.futtoboru.match.ecs.pitch.EraMarking;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchEnd;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.Rect;

/**
 * Pitch P1 (D26): the whole ground as one generated top-down image at {@link #PIXELS_PER_METRE} (D28), grass A1
 * (the ground_art palette in dithered flat bands), wear and mud as parameters, slope SL1 (dithered darker bands
 * downhill) and, optionally, the markings of the era from {@link PitchGeometry}: touchlines and goal lines always; the
 * halfway line only when {@link Style#halfwayLine} asks for it (none in 1888-89, D26); later {@link EraMarking}s when
 * the season has them.
 *
 * <p>Pure Java (an RGB array), deterministic for a style and seed, testable headless; the pitch pass uploads it into
 * the runtime atlas. Row 0 is the far edge ({@code y = maxY}), column 0 the left end ({@code x = minX}).
 */
public final class PitchArt {

    /** D28: the pitch is baked at the default view scale (10 px/m). */
    public static final int PIXELS_PER_METRE = 10;
    /** Ground beyond each goal line inside the image. */
    public static final double END_MARGIN_M = 16;
    /** Ground beyond the near touchline. */
    public static final double NEAR_MARGIN_M = 6;
    /** Ground beyond the far touchline (up to the skyline). */
    public static final double FAR_MARGIN_M = 10;
    /** Grass verge beyond the lines before bare earth. */
    public static final double VERGE_M = 3;
    /** Chalk line width. */
    public static final double LINE_M = 0.12;

    /** The state of the turf (the prototype's six surfaces). */
    public enum Turf {
        DRY(new int[] { 0x767C42, 0x8E9052, 0xA6A264 }, 0, false, false, false),
        NORMAL(new int[] { ArtMath.GRASS_DK, ArtMath.GRASS, ArtMath.GRASS_HI }, 0, false, false, false),
        WORN(new int[] { ArtMath.GRASS_DK, ArtMath.GRASS, ArtMath.GRASS_HI }, 1, false, false, false),
        MUDDY(new int[] { 0x465C34, 0x546A3C, 0x627844 }, 1.6, true, false, false),
        FROZEN(new int[] { 0x687C70, 0x84968E, 0xB0BEC0 }, 0, false, true, false),
        SNOW(new int[] { 0xC8CCD2, 0xDEE0E4, 0xF0F0F4 }, 0, false, false, true);

        final int[] bands;
        final double wear;
        final boolean mud;
        final boolean frost;
        final boolean snow;

        Turf(int[] bands, double wear, boolean mud, boolean frost, boolean snow) {
            this.bands = bands;
            this.wear = wear;
            this.mud = mud;
            this.frost = frost;
            this.snow = snow;
        }
    }

    /**
     * How the pitch looks.
     *
     * @param turf        surface state
     * @param wear        wear multiplier on top of the turf's own (goalmouths and centre; 0 = none)
     * @param slopeFallM  metres the pitch falls from the left end to the right end (negative: uphill to the right)
     * @param markings    draw the lines into the image (the side-on pass draws its own crisp lines instead)
     * @param halfwayLine draw a halfway line (an era parameter; none in 1888-89)
     * @param seed        noise seed (per ground)
     */
    public record Style(Turf turf, double wear, double slopeFallM, boolean markings, boolean halfwayLine, int seed) {

        public Style {
            if (turf == null) {
                throw new IllegalArgumentException("turf");
            }
        }

        /** 1888-89 defaults: normal turf with a little wear, flat, lines drawn, no halfway line. */
        public static Style of1888() {
            return new Style(Turf.WORN, 0.6, 0, true, false, 7);
        }

        public Style withMarkings(boolean on) {
            return new Style(turf, wear, slopeFallM, on, halfwayLine, seed);
        }

        public Style withSlope(double fallM) {
            return new Style(turf, wear, fallM, markings, halfwayLine, seed);
        }

        public Style withTurf(Turf t, double w) {
            return new Style(t, w, slopeFallM, markings, halfwayLine, seed);
        }

        public Style withHalfwayLine(boolean on) {
            return new Style(turf, wear, slopeFallM, markings, on, seed);
        }
    }

    private final PitchGeometry pitch;
    private final Style style;
    public final double minX;
    public final double maxY;
    public final int width;
    public final int height;
    /** 0xRRGGBB per pixel, row-major from the far left corner. */
    public final int[] rgb;

    public PitchArt(PitchGeometry pitch, Style style) {
        this.pitch = pitch;
        this.style = style;
        this.minX = -(pitch.halfLength() + END_MARGIN_M);
        this.maxY = pitch.halfWidth() + FAR_MARGIN_M;
        this.width = (int) Math.ceil(2 * (pitch.halfLength() + END_MARGIN_M) * PIXELS_PER_METRE);
        this.height = (int) Math.ceil((pitch.width() + NEAR_MARGIN_M + FAR_MARGIN_M) * PIXELS_PER_METRE);
        this.rgb = new int[width * height];
        paint();
    }

    /** World {@code x} at the centre of image column {@code px}. */
    public double worldX(int px) {
        return minX + (px + 0.5) / PIXELS_PER_METRE;
    }

    /** World {@code y} at the centre of image row {@code py}. */
    public double worldY(int py) {
        return maxY - (py + 0.5) / PIXELS_PER_METRE;
    }

    /** Image column of world {@code x} (fractional). */
    public double column(double wx) {
        return (wx - minX) * PIXELS_PER_METRE;
    }

    /** Image row of world {@code y} (fractional). */
    public double row(double wy) {
        return (maxY - wy) * PIXELS_PER_METRE;
    }

    public double minY() {
        return maxY - (double) height / PIXELS_PER_METRE;
    }

    public double maxX() {
        return minX + (double) width / PIXELS_PER_METRE;
    }

    public int at(int px, int py) {
        return rgb[py * width + px];
    }

    public Style style() {
        return style;
    }

    private void paint() {
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        double half = 0.5 / PIXELS_PER_METRE;
        for (int py = 0; py < height; py++) {
            double wy = worldY(py);
            for (int px = 0; px < width; px++) {
                double wx = worldX(px);
                double outX = Math.abs(wx) - hl;
                double outY = Math.abs(wy) - hw;
                int col;
                if (outX > VERGE_M || outY > VERGE_M) {
                    col = earth(wx, wy, px, py);
                } else {
                    col = turf(wx, wy, px, py);
                    if (outX <= 0 && outY <= 0) {
                        col = slope(col, wx, px, py);
                    }
                    if (style.markings) {
                        int line = lineAt(wx, wy, half);
                        if (line >= 0) {
                            col = line;
                        }
                    }
                }
                rgb[py * width + px] = col;
            }
        }
    }

    /** Wear of the goalmouths and the centre (prototype {@code wearAt}), 0..1, scaled to this pitch's length. */
    double wearAt(double wx, double wy) {
        double g = pitch.halfLength() - 3;
        double g1 = Math.max(0, 1 - Math.hypot((wx + g) / 5, wy / 4.5));
        double g2 = Math.max(0, 1 - Math.hypot((wx - g) / 5, wy / 4.5));
        double c = Math.max(0, 1 - Math.hypot(wx / 12, wy / 3.2)) * 0.7;
        return Math.min(1, g1 + g2 + c);
    }

    private int turf(double x, double y, int px, int py) {
        Turf s = style.turf;
        int sd = style.seed;
        double v = 0.55 * noise(x * 0.45, y * 0.45, sd) + 0.45 * noise(x * 2.2, y * 2.2, sd + 3);
        double t = v + (dither(px, py) - 0.5) * 0.35;
        int col = t < 0.38 ? s.bands[0] : (t < 0.68 ? s.bands[1] : s.bands[2]);
        double wearAmount = s.wear + style.wear;
        if (wearAmount > 0) {
            double w = wearAt(x, y) * wearAmount + (noise(x * 1.3, y * 1.3, sd + 5) - 0.5) * 0.6;
            if (w > 0.45) {
                double t2 = noise(x * 3, y * 3, sd + 1) + (dither(px, py) - 0.5) * 0.4;
                col = t2 < 0.45 ? mul(ArtMath.MUD, 0.82) : (t2 < 0.75 ? ArtMath.MUD : 0x7C6A4C);
                if (s.mud && w > 0.85 && noise(x * 0.9, y * 0.9, sd + 11) > 0.62) {
                    col = dither(px, py) > 0.5 ? 0x6E767C : 0x848C92; // standing water
                }
            } else if (w > 0.3 && dither(px, py) > 0.45) {
                col = mix(col, ArtMath.MUD, 0.5);
            }
        }
        if (s.frost && ArtMath.hash(px, py, sd + 2) > 0.86) {
            col = 0xD6DEE2;
        }
        if (s.snow) {
            if (noise(x * 1.6, y * 1.6, sd + 4) > 0.78 && dither(px, py) > 0.6) {
                col = ArtMath.GRASS_DK;
            } else if (wearAt(x, y) > 0.55 && dither(px, py) > 0.5) {
                col = 0xB0B4BA;
            }
        }
        return col;
    }

    /** SL1: downhill gets darker in four dithered bands; stronger with a bigger fall (full at 3 m). */
    private int slope(int col, double wx, int px, int py) {
        double fall = style.slopeFallM;
        if (fall == 0) {
            return col;
        }
        double along = (wx + pitch.halfLength()) / pitch.length();
        double downhill = fall > 0 ? along : 1 - along;
        double strength = Math.min(1, Math.abs(fall) / 3.0);
        double q = Math.floor(ArtMath.clamp(downhill, 0, 1) * 4) / 4;
        return dither(px, py) < q * 0.9 * strength ? mul(col, 0.84) : col;
    }

    private int earth(double x, double y, int px, int py) {
        double t = noise(x * 0.8, y * 0.8, 21) + (dither(px, py) - 0.5) * 0.3;
        return t < 0.5 ? 0x565042 : 0x645C4A;
    }

    /** Chalk colour of this turf (dark on snow). */
    public int lineColour() {
        if (style.turf == Turf.SNOW) {
            return 0x5A4634;
        }
        return style.turf == Turf.MUDDY ? 0xCEC8B8 : ArtMath.LINE;
    }

    /** The chalk colour when (x, y) lies on a line, else -1. {@code half} = half a pixel in metres. */
    private int lineAt(double x, double y, double half) {
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        double lw = Math.max(LINE_M / 2, half) + 1e-9; // a line on a pixel edge lights both sides
        boolean inX = x >= -hl - lw && x <= hl + lw;
        boolean inY = y >= -hw - lw && y <= hw + lw;
        boolean on = inX && (Math.abs(y - hw) <= lw || Math.abs(y + hw) <= lw);
        on |= inY && (Math.abs(x - hl) <= lw || Math.abs(x + hl) <= lw);
        on |= style.halfwayLine && inY && Math.abs(x) <= lw;
        if (!on) {
            on = eraLine(x, y, lw);
        }
        return on ? lineColour() : -1;
    }

    private boolean eraLine(double x, double y, double lw) {
        double hw = pitch.halfWidth();
        if (pitch.centreCircleRadius().isPresent()) {
            double r = Math.hypot(x, y);
            if (Math.abs(r - pitch.centreCircleRadius().getAsDouble()) <= lw || r <= Math.max(0.15, lw)) {
                return true;
            }
        }
        for (PitchEnd end : PitchEnd.values()) {
            if (pitch.penaltyLineX(end).isPresent() && Math.abs(x - pitch.penaltyLineX(end).getAsDouble()) <= lw
                    && Math.abs(y) <= hw) {
                return true;
            }
            if (pitch.goalAreaArcRadius().isPresent()) {
                double gx = pitch.goalLineX(end);
                double r = pitch.goalAreaArcRadius().getAsDouble();
                double py = pitch.postCentreY();
                boolean inside = Math.abs(x) <= pitch.halfLength() && Math.abs(x - gx) <= r;
                if (inside && (Math.abs(Math.hypot(x - gx, y - py) - r) <= lw
                        || Math.abs(Math.hypot(x - gx, y + py) - r) <= lw)) {
                    return true;
                }
            }
            if (onRect(pitch.penaltyArea(end).orElse(null), x, y, lw)
                    || onRect(pitch.goalAreaBox(end).orElse(null), x, y, lw)) {
                return true;
            }
        }
        return false;
    }

    private static boolean onRect(Rect r, double x, double y, double lw) {
        if (r == null) {
            return false;
        }
        boolean inX = x >= r.minX() - lw && x <= r.maxX() + lw;
        boolean inY = y >= r.minY() - lw && y <= r.maxY() + lw;
        return (inX && (Math.abs(y - r.minY()) <= lw || Math.abs(y - r.maxY()) <= lw))
                || (inY && (Math.abs(x - r.minX()) <= lw || Math.abs(x - r.maxX()) <= lw));
    }
}
