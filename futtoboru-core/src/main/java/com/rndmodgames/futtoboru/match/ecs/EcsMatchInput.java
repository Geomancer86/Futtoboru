package com.rndmodgames.futtoboru.match.ecs;

import com.rndmodgames.futtoboru.match.ecs.ai.TeamStyle;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.body.TeamSheet;

/**
 * Input of {@link EcsMatchEngine#play}. Stand-in for the V2 {@code MatchInput} (docs/design/MATCH_ENGINE_V2.md §3,
 * E-02a, not written yet); rules profile and kit join it at E-16/E-17.
 *
 * @param matchKey       stable id of the match (the caller forks {@code "match:" + matchKey})
 * @param pitch          ground and weather
 * @param ballCarryShare water left in the ball from an earlier wet match, share of dry mass (BKP §1.3: 0..0.31)
 * @param halfMinutes    length of a half (45 in 1888-89)
 * @param home           home side: team sheet (shape + eleven) and style
 * @param away           away side
 * @param neutral        played at a neutral ground (cup semi-final or final): no home advantage (E-20)
 */
public record EcsMatchInput(String matchKey, PitchConditions pitch, double ballCarryShare, int halfMinutes, Side home,
        Side away, boolean neutral) {

    /** One side: its eleven in their shape and its style (E-15). */
    public record Side(TeamSheet sheet, TeamStyle style) {

        public Side {
            if (sheet == null || style == null) {
                throw new IllegalArgumentException("sheet and style are required");
            }
        }

        /** Eleven average players in 2-3-5 playing the dribbling game: the stub until E-17 brings real squads. */
        public static Side stub() {
            return new Side(TeamSheet.stub(Shape.TWO_THREE_FIVE), TeamStyle.DRIBBLING);
        }
    }

    public EcsMatchInput {
        if (matchKey == null || pitch == null || home == null || away == null) {
            throw new IllegalArgumentException("matchKey, pitch and both sides are required");
        }
        if (!(ballCarryShare >= 0 && ballCarryShare <= 1) || halfMinutes < 0) {
            throw new IllegalArgumentException("carry " + ballCarryShare + ", half " + halfMinutes);
        }
    }

    /** On the home side's ground. */
    public EcsMatchInput(String matchKey, PitchConditions pitch, double ballCarryShare, int halfMinutes, Side home,
            Side away) {
        this(matchKey, pitch, ballCarryShare, halfMinutes, home, away, false);
    }

    /** Two stub sides ({@link Side#stub()}). */
    public EcsMatchInput(String matchKey, PitchConditions pitch, double ballCarryShare, int halfMinutes) {
        this(matchKey, pitch, ballCarryShare, halfMinutes, Side.stub(), Side.stub());
    }

    /** A 1888-89 match between two stub sides: two halves of 45 minutes, dry ball at kick-off. */
    public EcsMatchInput(String matchKey, PitchConditions pitch) {
        this(matchKey, pitch, 0.0, 45);
    }
}
