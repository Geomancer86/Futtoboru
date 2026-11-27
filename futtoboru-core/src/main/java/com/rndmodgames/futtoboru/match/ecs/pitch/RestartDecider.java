package com.rndmodgames.futtoboru.match.ecs.pitch;

/**
 * Turns a {@link LineCrossing} and the last touch into the decision of the 1888-89 Laws (V2 §1), and gives the
 * kick-offs (start, after a goal, second half with change of ends). Data in, data out; no player logic.
 *
 * <ul>
 * <li>Touchline: throw-in against the side that put it out (1873), from where it crossed, two hands from above the
 * head (1883), offside possible (1878-1920).</li>
 * <li>Goal line between the posts under the bar: a goal for the side attacking that goal (own goals count), unless
 * the ball came straight from a restart that cannot score direct (corner, kick-off, throw-in, free kick in 1888-89).
 * Kick-off by the side that lost the goal.</li>
 * <li>Goal line otherwise (or a void goal): last touched by the attackers: goal kick within 6 yd of the nearest post;
 * by the defenders: corner within 1 yd of the flag on that side. No offside from either.</li>
 * <li>A disputed crossing (no nets, close to the frame) carries the decision if the umpires reverse it.</li>
 * </ul>
 */
public final class RestartDecider {

    private final PitchGeometry pitch;
    private final RestartLaws laws;

    public RestartDecider(PitchGeometry pitch, RestartLaws laws) {
        if (pitch == null || laws == null) {
            throw new IllegalArgumentException("pitch and laws are required");
        }
        this.pitch = pitch;
        this.laws = laws;
    }

    public RestartLaws laws() {
        return laws;
    }

    /**
     * @param crossing   the ball wholly over a line ({@link PitchGeometry#sweep})
     * @param lastTouch  the side that touched the ball last (keeper included)
     * @param directFrom the restart the ball comes straight from with no other player touching it since, else null
     * @param ends       the ends of the current half
     */
    public LawDecision decide(LineCrossing crossing, Team lastTouch, RestartType directFrom, MatchEnds ends) {
        if (crossing == null || lastTouch == null || ends == null) {
            throw new IllegalArgumentException("crossing, lastTouch and ends are required");
        }
        if (crossing.line() == LineCrossing.Line.TOUCHLINE) {
            return new LawDecision(false, null, false, throwIn(lastTouch.other(), crossing), null);
        }
        PitchEnd end = crossing.end();
        Team attacker = ends.attackerOf(end);
        boolean directVoid = directFrom != null && !laws.goalDirectFrom(directFrom);
        LawDecision goal = new LawDecision(true, attacker, false, kickOff(attacker.other()), null);
        LawDecision noGoal = new LawDecision(false, null, false, outOverGoalLine(crossing, lastTouch, attacker), null);
        boolean geometricGoal = crossing.betweenPosts();
        // A goal straight from a restart that cannot score is void whatever the umpires see: nothing to dispute.
        boolean disputed = crossing.disputed() && !directVoid;
        LawDecision stands = geometricGoal && !directVoid ? goal : noGoal;
        if (!disputed) {
            return stands;
        }
        LawDecision reversed = stands == goal ? noGoal : goal;
        return new LawDecision(stands.goal(), stands.scoringTeam(), true, stands.restart(), reversed);
    }

    /** Kick-off from the centre by {@code team}: at the start, after a goal (the side that lost it) or after half-time. */
    public Restart kickOff(Team team) {
        return new Restart(RestartType.KICK_OFF, team, pitch.centre(), laws.offsideFrom(RestartType.KICK_OFF),
                laws.goalDirectFrom(RestartType.KICK_OFF), false);
    }

    /** Half-time: ends change and the side that did not kick off at the start kicks off. */
    public HalfTime halfTime(MatchEnds firstHalf, Team kickedOffFirst) {
        return new HalfTime(firstHalf.changed(), kickOff(kickedOffFirst.other()));
    }

    /** The second half's ends and its kick-off. */
    public record HalfTime(MatchEnds ends, Restart kickOff) {
    }

    private Restart throwIn(Team team, LineCrossing crossing) {
        double x = Math.max(-pitch.halfLength(), Math.min(pitch.halfLength(), crossing.ballCentre().x()));
        Vec3 spot = new Vec3(x, pitch.touchlineY(crossing.flank()), 0);
        return new Restart(RestartType.THROW_IN, team, spot, laws.offsideFrom(RestartType.THROW_IN),
                laws.goalDirectFrom(RestartType.THROW_IN), laws.throwInTwoHandsOverhead());
    }

    private Restart outOverGoalLine(LineCrossing crossing, Team lastTouch, Team attacker) {
        PitchEnd end = crossing.end();
        Flank flank = crossing.flank();
        if (lastTouch == attacker) {
            double x = pitch.goalLineX(end) - end.sign() * laws.goalKickSpotYd() * PitchParams.YARD;
            Vec3 spot = new Vec3(x, flank.sign() * pitch.postCentreY(), 0);
            return new Restart(RestartType.GOAL_KICK, attacker.other(), spot, laws.offsideFrom(RestartType.GOAL_KICK),
                    laws.goalDirectFrom(RestartType.GOAL_KICK), false);
        }
        double in = laws.cornerSpotYd() * PitchParams.YARD;
        Vec3 flag = pitch.cornerFlag(end, flank);
        Vec3 spot = new Vec3(flag.x() - end.sign() * in, flag.y() - flank.sign() * in, 0);
        return new Restart(RestartType.CORNER_KICK, attacker, spot, laws.offsideFrom(RestartType.CORNER_KICK),
                laws.goalDirectFrom(RestartType.CORNER_KICK), false);
    }
}
