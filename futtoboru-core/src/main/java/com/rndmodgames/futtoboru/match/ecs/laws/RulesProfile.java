package com.rndmodgames.futtoboru.match.ecs.laws;

import com.rndmodgames.futtoboru.match.ecs.action.ActionParams.KeeperHandling;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartLaws;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;

/**
 * The Laws of one season that the match engine plays by (E-16; MATCH_ENGINE_V2.md §1, ERA_TIMELINE.md). Part of
 * {@link com.rndmodgames.futtoboru.match.ecs.PhysicsParams#rules}; every season-dependent law lives here, so a later
 * season is a parameter, never a code path.
 *
 * @param seasonStartYear       the season (1888 = 1888-89)
 * @param offsideOpponents      opponents (keeper included) that must be level or nearer their goal line: 3 from 1866,
 *                              2 from 1925 (IFAB 1925; H)
 * @param offsideOwnHalfExempt  no offside in a player's own half (IFAB 1907; H). False in 1888-89: both halves
 * @param refereeOnField        the referee on the field with full power and the umpires turned linesmen (IFAB June
 *                              1891; H). False in 1888-89: two club umpires decide on appeal, the referee off the field
 *                              settles their disagreements
 * @param penalties             penalty kick for a foul or handball by the defenders near their goal (IFAB June 1891; H)
 * @param keeperChargeProtected the keeper may be charged only when playing the ball or obstructing (1892 IFAB / 1894
 *                              FA, sources disagree; L: 1892 used). False in 1888-89: charging the keeper is fair
 * @param keeperHandling        where the keeper may use his hands (own half 1887-1912, penalty area from 1912; H)
 * @param restartLaws           the restart rules of the season: goal direct from corner / goal kick / free kick (direct
 *                              free kicks 1903), offside from throw-ins (until 1920), corners and goal kicks (never)
 */
public record RulesProfile(int seasonStartYear, int offsideOpponents, boolean offsideOwnHalfExempt,
        boolean refereeOnField, boolean penalties, boolean keeperChargeProtected, KeeperHandling keeperHandling,
        RestartLaws restartLaws) {

    public RulesProfile {
        if (offsideOpponents < 1 || offsideOpponents > 3) {
            throw new IllegalArgumentException("offside opponents must be 1..3: " + offsideOpponents);
        }
        if (keeperHandling == null || restartLaws == null) {
            throw new IllegalArgumentException("keeper handling and restart laws are required");
        }
    }

    /** The Laws of the season starting in {@code seasonStartYear} (only 1888 onward is meaningful). */
    public static RulesProfile forSeason(int seasonStartYear) {
        int y = seasonStartYear;
        return new RulesProfile(y, y >= 1925 ? 2 : 3, y >= 1907, y >= 1891, y >= 1891, y >= 1892,
                y >= 1912 ? KeeperHandling.PENALTY_AREA : KeeperHandling.OWN_HALF, RestartLaws.forSeason(y));
    }

    /**
     * The Laws from an era profile (G-11: {@code EraProfile.at(date)}). Only parameters whose meaning matches are
     * read; the restart laws still come from the season. TODO (era names not yet mapped): rules.offsideFromThrowIn
     * and goalFromCorner/goalFromGoalKick/directFreeKicks (RestartLaws), keeperHandlingPurpose, keeperSteps,
     * refereeActsWithoutAppeal, refereeFreeKickWithoutAppeal, refereeDecisionFinal, advantage, maxSubstitutes,
     * minutes, addedTime, and all of ball.* / kit.* (grams vs kg in PhysicsParams).
     */
    public static RulesProfile fromEra(com.rndmodgames.futtoboru.system.data.EraProfile.Resolved era,
            int seasonStartYear) {
        return new RulesProfile(seasonStartYear, era.getInt("rules.offsideOpponents"),
                !era.getBoolean("rules.offsideOwnHalf"), !"UMPIRES_AND_REFEREE".equals(era.getString("rules.officials")),
                era.getBoolean("rules.penaltyKick"), era.getBoolean("rules.keeperChargeProtected"),
                "PENALTY_AREA".equals(era.getString("rules.keeperHandling")) ? KeeperHandling.PENALTY_AREA
                        : KeeperHandling.OWN_HALF,
                RestartLaws.forSeason(seasonStartYear));
    }

    /** 1888-89, the starting season. */
    public static RulesProfile season1888() {
        return forSeason(1888);
    }

    /** The two club umpires decide (on appeal), the referee only settles a disagreement (until 1891). */
    public boolean umpiresDecide() {
        return !refereeOnField;
    }

    /** Free kicks for fouls and handball are direct (1903); every free kick was indirect before. */
    public boolean directFreeKicks() {
        return restartLaws.goalDirectFromFreeKick();
    }

    public boolean goalDirectFromCorner() {
        return restartLaws.goalDirectFromCorner();
    }

    public boolean goalDirectFromGoalKick() {
        return restartLaws.goalDirectFromGoalKick();
    }

    /** Whether a player can be offside from {@code type} (throw-in until 1920; never from a corner or goal kick). */
    public boolean offsideFrom(RestartType type) {
        return restartLaws.offsideFrom(type);
    }
}
