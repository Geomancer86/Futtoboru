package com.rndmodgames.futtoboru.match.ecs.events;

import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * One event of a match (E-17, docs/design/MATCH_ENGINE_ECS.md §5.1), a read view built on demand from a
 * {@link MatchEventLog} (the log itself keeps primitives only). Players are named by a <i>player ref</i>: {@code
 * team.ordinal() * 16 + slot} in his team sheet ({@link #refTeam}, {@link #refSlot}), -1 for nobody.
 *
 * @param tick   simulation tick
 * @param minute match minute, 1..45 in the first half, 46..90 in the second (no added time in 1888)
 * @param side   the side the event belongs to (see {@link MatchEventType}), null for none
 * @param actor  player ref of the main actor, -1 none
 * @param other  player ref of the second actor (keeper's shooter, foul victim, man collided with), -1 none
 * @param x      where, metres from the centre spot along the length (positive = east)
 * @param y      where, metres across
 * @param data   type-specific bits ({@link MatchEventType}, the accessors below)
 */
public record MatchEvent(long tick, int minute, MatchEventType type, Team side, int actor, int other, double x,
        double y, int data) {

    // ---- data bits of appeals (OFFSIDE_APPEAL, FOUL, DISPUTED_GOAL) -------------------------------------------------

    static final int VOTE_HOME_SHIFT = 0, VOTE_AWAY_SHIFT = 2, VOTE_REF_SHIFT = 4;
    static final int REFEREE_CALLED = 1 << 6, GIVEN = 1 << 7, KIND_SHIFT = 8;
    /** DISPUTED_GOAL: the goal counted in the end. */
    public static final int GOAL_STOOD = 1 << 12;

    /** FOUL kinds beyond the LawRecord foul kinds (1 from behind, 2 keeper charge, 3 rough charge). */
    public static final int FOUL_HANDBALL = 4, FOUL_CARRYING = 5;
    /** Injury severities. */
    public static final int KNOCK = 1, PASSENGER = 2, CARRIED_OFF = 3;

    /** Packs the votes of an appeal (-1 no vote, 0 no, 1 yes) and, for a FOUL, its kind. */
    public static int appeal(int homeVote, int awayVote, boolean refereeCalled, int refereeVote, boolean given, int kind) {
        return ((homeVote + 1) << VOTE_HOME_SHIFT) | ((awayVote + 1) << VOTE_AWAY_SHIFT)
                | ((refereeVote + 1) << VOTE_REF_SHIFT) | (refereeCalled ? REFEREE_CALLED : 0) | (given ? GIVEN : 0)
                | (kind << KIND_SHIFT);
    }

    /** The filter level this event shows at (D19-9). */
    public MatchEventLevel level() {
        switch (type) {
            case KICK_OFF:
                return (data & 1) != 0 ? MatchEventLevel.KEY : MatchEventLevel.FULL;
            case SHOT:
            case SAVE:
            case HEAVY_BALL:
                return MatchEventLevel.EXTENDED;
            case OFFSIDE_APPEAL:
            case FOUL:
                return given() ? MatchEventLevel.EXTENDED : MatchEventLevel.FULL;
            case INJURY:
                return severity() >= PASSENGER ? MatchEventLevel.KEY : MatchEventLevel.EXTENDED;
            default:
                return MatchEventLevel.KEY;
        }
    }

    /** Appeals: upheld (offside or foul given; a disputed goal's line decision reversed). */
    public boolean given() {
        return (data & GIVEN) != 0;
    }

    /** Appeals: the referee was asked. */
    public boolean refereeCalled() {
        return (data & REFEREE_CALLED) != 0;
    }

    /** Appeals: the home club's umpire's vote, -1 none, 0 no, 1 yes. */
    public int homeUmpireVote() {
        return ((data >> VOTE_HOME_SHIFT) & 3) - 1;
    }

    /** Appeals: the away club's umpire's vote. */
    public int awayUmpireVote() {
        return ((data >> VOTE_AWAY_SHIFT) & 3) - 1;
    }

    /** Appeals: the referee's vote, -1 when he was not asked. */
    public int refereeVote() {
        return ((data >> VOTE_REF_SHIFT) & 3) - 1;
    }

    /** FOUL: the kind (1 from behind, 2 keeper charge, 3 rough charge, {@link #FOUL_HANDBALL}, {@link #FOUL_CARRYING}). */
    public int foulKind() {
        return (data >> KIND_SHIFT) & 15;
    }

    /** DISPUTED_GOAL: the goal counted in the end. */
    public boolean goalStood() {
        return (data & GOAL_STOOD) != 0;
    }

    /** GOAL: an own goal (the last touch was by the other side). */
    public boolean ownGoal() {
        return type == MatchEventType.GOAL && (data & 1) != 0;
    }

    /** SHOT: a header. */
    public boolean header() {
        return type == MatchEventType.SHOT && (data & 1) != 0;
    }

    /** INJURY: {@link #KNOCK}, {@link #PASSENGER} or {@link #CARRIED_OFF}. */
    public int severity() {
        return type == MatchEventType.INJURY ? data : 0;
    }

    /** HALF_TIME, FULL_TIME: home goals at that time. */
    public int homeScore() {
        return data & 0xFF;
    }

    /** HALF_TIME, FULL_TIME: away goals at that time. */
    public int awayScore() {
        return (data >> 8) & 0xFF;
    }

    /** True when this event is shown under {@code filter}. */
    public boolean shownAt(MatchEventLevel filter) {
        return filter.shows(level());
    }

    // ---- player refs -----------------------------------------------------------------------------------------------

    /** A player ref for {@code team.ordinal()} and {@code slot}; -1 when either is unknown. */
    public static int ref(int team, int slot) {
        return team < 0 || slot < 0 || slot >= 16 ? -1 : team * 16 + slot;
    }

    /** The team of a player ref, null for -1. */
    public static Team refTeam(int ref) {
        return ref < 0 ? null : Team.values()[ref / 16];
    }

    /** The slot of a player ref, -1 for -1. */
    public static int refSlot(int ref) {
        return ref < 0 ? -1 : ref % 16;
    }
}
