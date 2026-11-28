package com.rndmodgames.futtoboru.match.ecs;

/**
 * Result of {@link EcsMatchEngine#play}. Stand-in for the V2 {@code MatchResult}: the score but no events or stats yet
 * (E-17); it already carries what saves and replays need (§5.3) and the ball's mass for the report ("the leather
 * grew heavy").
 *
 * @param homeGoals goals by the home side (own goals by the away side included), from the stand-in referee
 * @param ballActions ball actions played (kicks, touches, headers, keeper handling), a rough measure of play
 * @param restarts    throw-ins, goal kicks and corners
 */
public record EcsMatchResult(String matchKey, String engineVersion, long seed, long ticks, long finalStateHash,
        double halfTimeBallMassKg, double fullTimeBallMassKg, int homeGoals, int awayGoals, long ballActions,
        int restarts) {
}
