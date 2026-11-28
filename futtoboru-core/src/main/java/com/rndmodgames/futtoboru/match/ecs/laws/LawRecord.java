package com.rndmodgames.futtoboru.match.ecs.laws;

/**
 * One decision of the Laws in the current tick (E-16), for event detection (E-17: OFFSIDE_POSITION, APPEAL with both
 * umpires and the referee, FOUL, GOAL / DISPUTED_GOAL, OUT, SENDING_OFF, half-time, full time). Instances are
 * preallocated and reused by {@link LawsSystem}; copy what you keep.
 */
public final class LawRecord {

    /** Types. */
    public static final int OUT_OF_PLAY = 1, GOAL = 2, DISPUTED_GOAL = 3, OFFSIDE = 4, FOUL = 5, HANDBALL = 6,
            CARRYING = 7, SENDING_OFF = 8, HALF_TIME = 9, FULL_TIME = 10, RESTART_TAKEN = 11;
    /** Foul kinds. */
    public static final int FOUL_NONE = 0, FOUL_FROM_BEHIND = 1, FOUL_KEEPER_CHARGE = 2,
            /** A fair charge that floored a man, claimed as a foul. */
            FOUL_ROUGH = 3;
    /** Votes. */
    public static final int NO_VOTE = -1, VOTE_NO = 0, VOTE_YES = 1;

    public long tick;
    /** One of the type constants. */
    public int type;
    /** FOUL only: one of the foul kinds. */
    public int foulKind;
    /** An appeal was made (offside, fouls, handball, carrying, disputed goal); false for an unappealed offside. */
    public boolean appealed;
    /** {@code Team.ordinal()} of the side appealing (claiming), -1 none. */
    public int appealingTeam = -1;
    /** Creation indices of the offender and the man wronged, -1 none. */
    public int offender = -1, victim = -1;
    /** Where it happened. */
    public double x, y;
    /** The truth the officials try to see (offside: metres beyond the line; foul: +1/-1; goal: metres in/out). */
    public double evidence;
    /** Votes of the home and away club umpires ({@link #NO_VOTE} when they did not vote). */
    public int homeUmpireVote = NO_VOTE, awayUmpireVote = NO_VOTE;
    /** The referee was asked (umpires disagreed, or he decides alone from 1891), and his vote. */
    public boolean refereeCalled;
    public int refereeVote = NO_VOTE;
    /** The appeal was upheld (for a disputed goal: the decision on the line was reversed). */
    public boolean given;
    /** The restart that follows ({@code RestartType.ordinal()}, -1 none: play goes on), its team and spot. */
    public int restartType = -1, restartTeam = -1;
    public double restartX, restartY;
    /** GOAL / DISPUTED_GOAL: the side credited with the goal, -1 no goal. */
    public int scoringTeam = -1;

    void clear() {
        tick = 0;
        type = foulKind = 0;
        appealed = refereeCalled = given = false;
        appealingTeam = offender = victim = restartType = restartTeam = scoringTeam = -1;
        x = y = evidence = restartX = restartY = 0;
        homeUmpireVote = awayUmpireVote = refereeVote = NO_VOTE;
    }
}
