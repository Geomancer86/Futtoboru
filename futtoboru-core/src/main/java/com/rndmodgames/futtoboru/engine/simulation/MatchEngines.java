package com.rndmodgames.futtoboru.engine.simulation;

import java.util.Locale;

import com.rndmodgames.futtoboru.system.Log;

/**
 * Which {@link MatchEngine} plays the game's matches (E-17a, D33-1): {@code -Dfuttoboru.match.engine=instant|ecs},
 * read once when this class loads (at the first match); default {@code instant}. The ECS engine becomes the default
 * only when the producer flips it after calibration and the nightly soaks (D33-8). {@link #select(String)} is the
 * settings hook (and what soak runs and tests use); {@link #reset()} goes back to the startup choice.
 */
public final class MatchEngines {

    /** System property naming the engine. */
    public static final String PROPERTY = "futtoboru.match.engine";

    private static final String STARTUP = parse(System.getProperty(PROPERTY));
    private static volatile String selected = STARTUP;

    private MatchEngines() {
    }

    /** {@link MatchEngine#INSTANT} or {@link MatchEngine#ECS}; anything else (or nothing) is instant. */
    static String parse(String value) {
        if (value == null || value.isBlank()) {
            return MatchEngine.INSTANT;
        }
        String v = value.trim().toLowerCase(Locale.ROOT);
        if (MatchEngine.ECS.equals(v)) {
            return MatchEngine.ECS;
        }
        if (!MatchEngine.INSTANT.equals(v)) {
            Log.warn(Log.ENGINE_MATCH, "MatchEngines", "Unknown " + PROPERTY + "=" + value + ", using instant");
        }
        return MatchEngine.INSTANT;
    }

    /** Id of the engine new {@link MatchSimulator}s use. */
    public static String selectedId() {
        return selected;
    }

    /** A new engine of the selected kind. */
    public static MatchEngine selected() {
        return create(selected);
    }

    /** A new engine for {@code id} ({@link #parse} rules). */
    public static MatchEngine create(String id) {
        return MatchEngine.ECS.equals(parse(id)) ? new EcsMatchEngineAdapter() : new InstantMatchEngine();
    }

    /** Settings hook: use {@code id} for the matches played from now on. */
    public static void select(String id) {
        selected = parse(id);
    }

    /** Back to the engine chosen at startup. */
    public static void reset() {
        selected = STARTUP;
    }
}
