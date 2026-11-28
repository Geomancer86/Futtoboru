package com.rndmodgames.futtoboru.engine.simulation;

import java.util.function.Supplier;

import com.rndmodgames.futtoboru.match.ecs.EcsMatchEngine;

/**
 * An ECS match readied to be watched (E-21f): its {@link EcsMatchEngine.Live live world} is ticked by the match screen
 * (in the viewer, or headless when skipped) and, once it is over, {@link #outcome()} is what the engine's
 * {@link MatchEngine#play} gives for the same fixture and stream, applied through the normal path
 * ({@link MatchSimulator#apply}).
 */
public final class WatchedMatch {

    private final EcsMatchEngine.Live live;
    private final Supplier<MatchOutcome> outcome;

    WatchedMatch(EcsMatchEngine.Live live, Supplier<MatchOutcome> outcome) {
        this.live = live;
        this.outcome = outcome;
    }

    public EcsMatchEngine.Live live() {
        return live;
    }

    /** Plays what is left of the match, headless. */
    public void playToEnd() {
        while (!live.finished()) {
            live.tick();
        }
    }

    /** The outcome of the finished match. */
    public MatchOutcome outcome() {
        return outcome.get();
    }
}
