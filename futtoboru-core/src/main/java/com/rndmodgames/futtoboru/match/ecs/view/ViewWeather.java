package com.rndmodgames.futtoboru.match.ecs.view;

import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;

/**
 * What the sky looks like (E-21g, D26 W1/W2), a pure function of the match's pitch conditions: wetness and surface,
 * the values {@code MatchWeather} fixed from the match input. No seed of its own; the cosmetic random only scatters
 * drops. Pattern of DarkBlade's {@code WindSystem}/{@code SceneTint}: lean and gusts from slow sines of the view clock.
 */
public final class ViewWeather {

    /** Kinds of sky, lightest first. */
    public enum Kind {
        CLEAR(0, 0), DRIZZLE(0.3, 0.10), RAIN(0.65, 0.16), HEAVY(1.0, 0.26), SNOW(0.5, 0.12);

        /** Share of the particle budget this weather fills. */
        public final double intensity;
        /** Alpha of the overcast tint. */
        public final double tint;

        Kind(double intensity, double tint) {
            this.intensity = intensity;
            this.tint = tint;
        }
    }

    /** Overcast tint colour: cool grey-blue dusk. */
    public static final int TINT_RGB = 0x2A3342;
    /** Snow tint colour: pale and bright. */
    public static final int SNOW_TINT_RGB = 0x8C97A6;
    /** Match seconds after which the mud has built up fully (ninety minutes). */
    public static final double MUD_FULL_SECONDS = 5400;

    private ViewWeather() {
    }

    /** The weather for a pitch: a frozen ground is snowing, otherwise wetness picks the rain. */
    public static Kind of(double wetness, Surface surface) {
        if (surface == Surface.FROZEN) {
            return Kind.SNOW;
        }
        if (wetness >= 0.6) {
            return Kind.HEAVY;
        }
        if (wetness >= PitchConditions.RAIN * 0.7) {
            return Kind.RAIN;
        }
        if (wetness >= PitchConditions.DRIZZLE * 0.7) {
            return Kind.DRIZZLE;
        }
        return Kind.CLEAR;
    }

    /** Fog thickness 0..0.5: thin mist on a damp ground, thicker in rain and snow, none when dry. */
    public static double fog(double wetness, Surface surface) {
        if (surface == Surface.FROZEN) {
            return 0.30;
        }
        if (wetness < PitchConditions.DAMP) {
            return 0;
        }
        return Math.min(0.5, 0.10 + 0.4 * wetness);
    }

    /** Wind lean of falling rain: pixels sideways per pixel fallen; stronger in heavy rain, gusting. */
    public static double lean(double wetness, double viewSeconds) {
        double gust = 0.5 + 0.5 * Math.sin(viewSeconds * 0.3) * Math.sin(viewSeconds * 0.11 + 1.3);
        return 0.10 + 0.30 * Math.min(1, wetness) * (0.6 + 0.4 * gust);
    }

    /** Mud on the pitch 0..1: grows with the wetness and builds up over the match (from a third at kick-off). */
    public static double mud(double wetness, double matchSeconds) {
        double build = Math.min(1, 0.35 + 0.65 * Math.max(0, matchSeconds) / MUD_FULL_SECONDS);
        double w = Math.min(1, wetness / PitchConditions.WATERLOGGED);
        return w < 0.05 ? 0 : w * build;
    }

    /**
     * Mud alpha at a ground point: worse in the goalmouths and the centre circle where the play wears the turf, with
     * patches from a hash of the ground seed (no random source, so a frame repeats).
     */
    public static float mudAlpha(double mud, double wx, double wy, double halfLength, long groundSeed) {
        if (mud <= 0) {
            return 0;
        }
        double goal = Math.max(Math.exp(-sq((Math.abs(wx) - halfLength) / 12)) * Math.exp(-sq(wy / 8)),
                Math.exp(-sq(wx / 9)) * Math.exp(-sq(wy / 9)));
        double patch = hash(groundSeed, (long) Math.floor(wx / 4), (long) Math.floor(wy / 3));
        double a = mud * (0.30 + 0.45 * patch + 0.35 * goal);
        return (float) Math.min(0.6, a * 0.6);
    }

    private static double sq(double v) {
        return v * v;
    }

    private static double hash(long seed, long x, long y) {
        long h = seed * 0x9E3779B97F4A7C15L + x * 0xC2B2AE3D27D4EB4FL + y * 0x165667B19E3779F9L;
        h ^= h >>> 29;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 32;
        return (h & 0xFFFFFF) / (double) 0x1000000;
    }
}
