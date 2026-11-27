package com.rndmodgames.futtoboru.match.ecs;

/**
 * Input of {@link EcsMatchEngine#play}. Stand-in for the V2 {@code MatchInput} (docs/design/MATCH_ENGINE_V2.md §3,
 * E-02a, not written yet); team sheets, rules profile and kit join it at E-13..E-17.
 *
 * @param matchKey       stable id of the match (the caller forks {@code "match:" + matchKey})
 * @param pitch          ground and weather
 * @param ballCarryShare water left in the ball from an earlier wet match, share of dry mass (BKP §1.3: 0..0.31)
 * @param halfMinutes    length of a half (45 in 1888-89)
 */
public record EcsMatchInput(String matchKey, PitchConditions pitch, double ballCarryShare, int halfMinutes) {

    public EcsMatchInput {
        if (matchKey == null || pitch == null) {
            throw new IllegalArgumentException("matchKey and pitch are required");
        }
        if (!(ballCarryShare >= 0 && ballCarryShare <= 1) || halfMinutes < 0) {
            throw new IllegalArgumentException("carry " + ballCarryShare + ", half " + halfMinutes);
        }
    }

    /** A 1888-89 match: two halves of 45 minutes, dry ball at kick-off. */
    public EcsMatchInput(String matchKey, PitchConditions pitch) {
        this(matchKey, pitch, 0.0, 45);
    }
}
