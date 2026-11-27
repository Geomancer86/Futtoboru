package com.rndmodgames.futtoboru.match.ecs;

/**
 * Result of {@link EcsMatchEngine#play}. Stand-in for the V2 {@code MatchResult}: no events, goals or stats yet
 * (E-17); it already carries what saves and replays need (§5.3) and the ball's mass for the report ("the leather
 * grew heavy").
 */
public record EcsMatchResult(String matchKey, String engineVersion, long seed, long ticks, long finalStateHash,
        double halfTimeBallMassKg, double fullTimeBallMassKg) {
}
