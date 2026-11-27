package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * Restart rules that changed by season (V2 §1, ERA_TIMELINE.md), so later seasons switch by parameter.
 *
 * @param goalDirectFromCorner   no goal direct from a corner 1875-1924
 * @param goalDirectFromGoalKick allowed until IFAB 1890 ("no goal directly from a goal kick"); 1888-89 not verified
 * @param goalDirectFromThrowIn  never allowed here (assumption, not verified for 1888)
 * @param goalDirectFromKickOff  not allowed in 1888-89 (V2 §1)
 * @param goalDirectFromFreeKick every free kick indirect until 1903
 * @param offsideFromThrowIn     a player could be offside from a throw-in 1878-1920
 * @param offsideFromCorner      no offside from a corner since 1883
 * @param offsideFromGoalKick    no offside from a goal kick since 1863
 * @param offsideFromKickOff     offside applies from a kick-off
 * @param throwInTwoHandsOverhead two hands from above the head since 1883
 * @param goalKickSpotYd         goal kick placed this far straight infield from the nearest post (Laws: within 6 yd)
 * @param cornerSpotYd           corner placed this far infield along both lines from the flag (Laws: within 1 yd)
 */
public record RestartLaws(boolean goalDirectFromCorner, boolean goalDirectFromGoalKick, boolean goalDirectFromThrowIn,
        boolean goalDirectFromKickOff, boolean goalDirectFromFreeKick, boolean offsideFromThrowIn,
        boolean offsideFromCorner, boolean offsideFromGoalKick, boolean offsideFromKickOff,
        boolean throwInTwoHandsOverhead, double goalKickSpotYd, double cornerSpotYd) {

    /** Placeholder placement inside the 6 yd allowed (V2 marks goal kicks as abstracted). */
    public static final double DEFAULT_GOAL_KICK_SPOT_YD = 3;
    /** 0.5 yd along both lines: 0.71 yd from the flag, inside the 1 yd allowed. */
    public static final double DEFAULT_CORNER_SPOT_YD = 0.5;

    public RestartLaws {
        if (goalKickSpotYd < 0 || goalKickSpotYd > 6) {
            throw new IllegalArgumentException("goal kick must be within 6 yd of the post: " + goalKickSpotYd);
        }
        if (cornerSpotYd < 0 || cornerSpotYd * Math.sqrt(2) > 1) {
            throw new IllegalArgumentException("corner must be within 1 yd of the flag: " + cornerSpotYd);
        }
    }

    public static RestartLaws laws1888() {
        return forSeason(1888);
    }

    /** The rules of the season starting in {@code seasonStartYear} (only 1888 onward is meaningful). */
    public static RestartLaws forSeason(int seasonStartYear) {
        return new RestartLaws(seasonStartYear >= 1924, seasonStartYear < 1890, false, false, seasonStartYear >= 1903,
                seasonStartYear < 1920, false, false, true, seasonStartYear >= 1883, DEFAULT_GOAL_KICK_SPOT_YD,
                DEFAULT_CORNER_SPOT_YD);
    }

    /** Whether the taker of {@code type} may score without another player touching the ball. */
    public boolean goalDirectFrom(RestartType type) {
        switch (type) {
            case CORNER_KICK:
                return goalDirectFromCorner;
            case GOAL_KICK:
                return goalDirectFromGoalKick;
            case THROW_IN:
                return goalDirectFromThrowIn;
            case KICK_OFF:
                return goalDirectFromKickOff;
            default:
                return goalDirectFromFreeKick;
        }
    }

    /** Whether a player can be offside from {@code type} (free kicks: yes). */
    public boolean offsideFrom(RestartType type) {
        switch (type) {
            case CORNER_KICK:
                return offsideFromCorner;
            case GOAL_KICK:
                return offsideFromGoalKick;
            case THROW_IN:
                return offsideFromThrowIn;
            case KICK_OFF:
                return offsideFromKickOff;
            default:
                return true;
        }
    }
}
