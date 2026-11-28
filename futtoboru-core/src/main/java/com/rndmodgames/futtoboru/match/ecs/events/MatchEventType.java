package com.rndmodgames.futtoboru.match.ecs.events;

/**
 * Kinds of {@link MatchEvent} (docs/design/MATCH_ENGINE_ECS.md §5.1, D19-9). The codes are saved (compact event log
 * on the match): never renumber one, only append.
 */
public enum MatchEventType {
    /** The ball played from the centre: side = the side kicking off; data bit 0 = it opened a half. */
    KICK_OFF(1),
    /** A kick (or header) at the goal mouth: actor = the shooter; data bit 0 = a header, bits 1.. = ball speed dm/s. */
    SHOT(2),
    /** The keeper stopped a shot: actor = the keeper, other = the shooter; data = the ActionRecord outcome. */
    SAVE(3),
    /**
     * A goal given: side = the side it counts for, actor = the last man to play the ball (an own goal when he is of
     * the other side); data bit 0 = own goal, bit 1 = it stood after a dispute.
     */
    GOAL(4),
    /** A goal (or no goal) disputed on the line (no nets): side = the side appealing; votes in data. */
    DISPUTED_GOAL(5),
    /** An offside appeal: side = the side appealing, actor = the man said to be offside; votes in data. */
    OFFSIDE_APPEAL(6),
    /**
     * A foul appeal (charge from behind, keeper charge, rough charge, handball, carrying): side = the offender's side,
     * actor = the offender, other = the man wronged; votes and the foul kind in data.
     */
    FOUL(7),
    /**
     * A man hurt: side = his side, actor = him, other = the man he collided with; data = severity (1 knock, 2
     * passenger, 3 carried off).
     */
    INJURY(8),
    /** The lace cut a header (he takes a knock): actor = the header. */
    LACE_CUT(9),
    /** The wet leather grew heavy: data = the ball's mass in grams. */
    HEAVY_BALL(10),
    /** Half-time: data = home goals | away goals &lt;&lt; 8. */
    HALF_TIME(11),
    /** Full time: data = home goals | away goals &lt;&lt; 8. */
    FULL_TIME(12),
    /** A man sent off by the referee: side = his side, actor = him. */
    SENDING_OFF(13);

    private static final MatchEventType[] BY_CODE = new MatchEventType[32];
    static {
        for (MatchEventType t : values()) {
            BY_CODE[t.code] = t;
        }
    }

    private final int code;

    MatchEventType(int code) {
        this.code = code;
    }

    /** The saved code. */
    public int code() {
        return code;
    }

    /** @return the type of a saved code, null when unknown (a newer save) */
    public static MatchEventType ofCode(int code) {
        return code > 0 && code < BY_CODE.length ? BY_CODE[code] : null;
    }
}
