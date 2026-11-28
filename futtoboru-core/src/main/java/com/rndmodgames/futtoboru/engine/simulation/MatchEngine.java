package com.rndmodgames.futtoboru.engine.simulation;

import com.rndmodgames.futtoboru.engine.random.RandomSource;

/**
 * Decides the result of one match (E-17a, D30): the contract both the instant result ({@link InstantMatchEngine})
 * and the ECS engine ({@link EcsMatchEngineAdapter}) implement. {@link MatchSimulator} picks one
 * ({@link MatchEngines#selected()}), writes the outcome into the {@code Match} and adds goal minutes and the gate;
 * {@link MatchResultRecorder} never sees which engine played.
 *
 * Contract: the outcome is a pure function of the fixture and {@code rng} (same seed, same outcome); goals are 0 or
 * more; every scorer id is the person id of a player of that side, at most one per goal and in goal order (a goal
 * without a known scorer, such as an own goal, has none).
 */
public interface MatchEngine {

    /** Value of {@link MatchEngines#PROPERTY} for the instant result (the default). */
    String INSTANT = "instant";
    /** Value of {@link MatchEngines#PROPERTY} for the ECS engine (D20). */
    String ECS = "ecs";

    /** {@link #INSTANT} or {@link #ECS}. */
    String id();

    /**
     * @param rng the match's own stream, {@code game.random().fork("match:" + fixture.matchKey())}
     */
    MatchOutcome play(MatchFixture fixture, RandomSource rng);
}
