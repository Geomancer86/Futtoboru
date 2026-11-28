package com.rndmodgames.futtoboru.match.ecs.laws;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.match.ecs.HashedComponent;
import com.rndmodgames.futtoboru.match.ecs.StateHash;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * The state of play under the Laws (E-16), on the ball entity: in play or dead, the restart awaited and who takes it,
 * ends, half, score, the offside snapshot of the last time the ball was played, and who has been sent off. Written
 * only by {@link LawsSystem}; a world whose ball has no {@code PlayStateComponent} plays without laws (physics
 * drills). Attach it with {@link #attach} after spawning the ball.
 *
 * <p><b>For the AI (E-15), read-only:</b> {@link #inPlay()}, {@link #stopped()}, {@link #fullTime()},
 * {@link #restartType()}, {@link #restartTeam()} (whose ball at a dead ball), {@link #restartX}/{@link #restartY}
 * (where it is taken), {@link #restartReady(long)} (may be taken now), {@link #ends()} / {@link #attackSign(Team)},
 * {@link #half}, {@link #goals(Team)}, {@link #offsidePosition(int)} and {@link #sentOff(int)}. In play, whose ball
 * it is comes from {@code PossessionComponent.lastTouchTeam}. A restart is taken by a player of
 * {@link #restartTeam()} playing the ball once it is ready (a throw-in only with {@code ActionIntent.THROW_IN}, other
 * restarts with the feet); until then the laws keep the ball on the spot.
 */
public final class PlayStateComponent implements Component, HashedComponent {

    private static final Team[] TEAMS = Team.values();
    private static final RestartType[] RESTARTS = RestartType.values();

    /** Dead ball: waiting for {@link #restartType} by {@link #restartTeam}. */
    public static final int STOPPED = 0;
    public static final int IN_PLAY = 1;
    public static final int FULL_TIME = 2;

    /** Creation indices tracked by the offside snapshot (players are 0..21). */
    public static final int MAX_TRACKED = 32;

    /** The laws system has set up the first kick-off. */
    public boolean started;
    /** {@link #STOPPED}, {@link #IN_PLAY} or {@link #FULL_TIME}. */
    public int phase = STOPPED;
    /** 1 or 2. */
    public int half = 1;
    /** Ticks per half (45 minutes by custom; the referee kept time, no added time in 1888-89). */
    public long halfTicks;
    /** {@code Team.ordinal()} of the side that kicked off the match, and of the side defending the west goal now. */
    public int kickedOffFirst, westDefender;

    /** {@code RestartType.ordinal()} awaited (or last taken), -1 none; its team, spot and flags. */
    public int restartType = -1, restartTeam = -1;
    public double restartX, restartY;
    public boolean restartOffside, restartGoalDirect;
    /** First tick the restart may be taken. */
    public long readyTick;

    /** In play: the restart the ball comes straight from (no other player has touched it since), -1 none. */
    public int directFrom = -1;
    /** Creation index of that restart's taker, -1 none. */
    public int directTaker = -1;

    public int homeGoals, awayGoals;

    /** Ball centre at the end of the last tick (start of the swept out-of-play test). */
    public double prevBallX, prevBallY, prevBallZ;

    /** Offside snapshot: the side that last played the ball, -1 none; the player and tick. */
    public int snapshotTeam = -1, snapshotPasser = -1;
    public long snapshotTick = -1;
    /** Bit per creation index: a team-mate of the passer with a margin in {@link #margin}. */
    public long snapshotMask;
    /** Metres beyond the offside line when the ball was played (> 0 = offside position). */
    public final double[] margin = new double[MAX_TRACKED];

    /** Bit per creation index: sent off (not replaced, D7). */
    public long sentOffMask;

    /** Counters for events and calibration. */
    public int appeals, appealsGiven, offsidesGiven, foulsGiven, sendingsOff, disputedGoals;

    /**
     * Puts the laws on a match: kick-off by {@code kickingOff} with {@code ends} for the first half. Call after spawning
     * the ball, before the first tick.
     */
    public static PlayStateComponent attach(Entity ball, MatchEnds ends, Team kickingOff, double halfMinutes,
            int tickHz) {
        if (ball == null || ends == null || kickingOff == null || !(halfMinutes >= 0) || tickHz <= 0) {
            throw new IllegalArgumentException("ball, ends, kicking-off side, half length and tick rate are required");
        }
        PlayStateComponent s = new PlayStateComponent();
        s.westDefender = ends.westDefender().ordinal();
        s.kickedOffFirst = kickingOff.ordinal();
        s.halfTicks = Math.round(halfMinutes * 60 * tickHz);
        ball.add(s);
        return s;
    }

    public boolean inPlay() {
        return phase == IN_PLAY;
    }

    public boolean stopped() {
        return phase == STOPPED;
    }

    public boolean fullTime() {
        return phase == FULL_TIME;
    }

    /** The restart awaited at a dead ball (or the last one taken while in play), or null. */
    public RestartType restartType() {
        return restartType < 0 ? null : RESTARTS[restartType];
    }

    /** Whose ball at a dead ball (the side taking {@link #restartType()}), or null. */
    public Team restartTeam() {
        return restartTeam < 0 ? null : TEAMS[restartTeam];
    }

    /** True when the awaited restart may be taken at {@code tick}. */
    public boolean restartReady(long tick) {
        return phase == STOPPED && started && tick >= readyTick;
    }

    /** Which side defends which goal in this half. */
    public MatchEnds ends() {
        return new MatchEnds(TEAMS[westDefender]);
    }

    /** +1 if {@code team} attacks the east goal (+x) in this half, -1 if west. Allocation-free. */
    public int attackSign(Team team) {
        return team.ordinal() == westDefender ? 1 : -1;
    }

    public int goals(Team team) {
        return team == Team.HOME ? homeGoals : awayGoals;
    }

    /** The player was in an offside position when a team-mate last played the ball. */
    public boolean offsidePosition(int creationIndex) {
        return creationIndex >= 0 && creationIndex < MAX_TRACKED && (snapshotMask & (1L << creationIndex)) != 0
                && margin[creationIndex] > 0;
    }

    public boolean sentOff(int creationIndex) {
        return creationIndex >= 0 && creationIndex < 64 && (sentOffMask & (1L << creationIndex)) != 0;
    }

    @Override
    public void hashInto(StateHash h) {
        h.mix("play.started", started ? 1 : 0);
        h.mix("play.phase", phase);
        h.mix("play.half", half);
        h.mix("play.halfTicks", halfTicks);
        h.mix("play.kickedOffFirst", kickedOffFirst);
        h.mix("play.west", westDefender);
        h.mix("play.restart", restartType);
        h.mix("play.restartTeam", restartTeam);
        h.mix("play.restartX", restartX);
        h.mix("play.restartY", restartY);
        h.mix("play.restartOffside", restartOffside ? 1 : 0);
        h.mix("play.restartDirect", restartGoalDirect ? 1 : 0);
        h.mix("play.ready", readyTick);
        h.mix("play.directFrom", directFrom);
        h.mix("play.directTaker", directTaker);
        h.mix("play.home", homeGoals);
        h.mix("play.away", awayGoals);
        h.mix("play.prevX", prevBallX);
        h.mix("play.prevY", prevBallY);
        h.mix("play.prevZ", prevBallZ);
        h.mix("play.snapTeam", snapshotTeam);
        h.mix("play.snapPasser", snapshotPasser);
        h.mix("play.snapTick", snapshotTick);
        h.mix("play.snapMask", snapshotMask);
        for (int i = 0; i < MAX_TRACKED; i++) {
            if ((snapshotMask & (1L << i)) != 0) {
                h.mix("play.margin", margin[i]);
            }
        }
        h.mix("play.sentOff", sentOffMask);
        h.mix("play.appeals", appeals);
        h.mix("play.given", appealsGiven);
        h.mix("play.offsides", offsidesGiven);
        h.mix("play.fouls", foulsGiven);
        h.mix("play.sendingsOff", sendingsOff);
        h.mix("play.disputed", disputedGoals);
    }
}
