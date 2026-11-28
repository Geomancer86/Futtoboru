package com.rndmodgames.futtoboru.match.ecs.laws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.pitch.Restart;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.match.ecs.pitch.Vec3;

/** E-16: the offside law as geometry and in a world (snapshot when the ball is played, involvement, restarts). */
class OffsideTest {

    static final double HL = Laws.PITCH.halfLength();
    static final double HW = Laws.PITCH.halfWidth();
    static final RulesProfile R1888 = RulesProfile.forSeason(1888);

    /** Margin of a home player attacking east (+x). */
    static double m(RulesProfile r, double playerX, double ballX, double... opponents) {
        return OffsideRule.margin(playerX, ballX, opponents, opponents.length, 1, r, HL);
    }

    @Test
    void threeOpponentsKeepHimOnsideInBothHalvesIn1888() {
        // keeper + two backs between him and the goal line: onside
        assertTrue(m(R1888, 30, 0, 50, 40, 35) <= 0);
        // level with the third opponent: onside (level counts)
        assertEquals(0, m(R1888, 35, 0, 50, 40, 35), 1e-12);
        // only two in front: offside by the distance to the third (behind him) or to the ball, whichever is less
        assertEquals(5, m(R1888, 35, 0, 50, 40, 30), 1e-12);
        // behind the ball: never offside
        assertTrue(m(R1888, 10, 20, 50) < 0);
        // level with the ball: onside
        assertEquals(0, m(R1888, 20, 20, 50), 1e-12);
        // fewer than three opponents on the field: offside whenever ahead of the ball
        assertEquals(10, m(R1888, 30, 20, 50, 40), 1e-12);
        // own half in 1888: offside all the same (the exemption came in 1907)
        assertTrue(m(R1888, -5, -30, 50, -2, -40) > 0);
    }

    @Test
    void ownHalfExemptionFrom1907AndTwoPlayerRuleFrom1925() {
        RulesProfile r1907 = RulesProfile.forSeason(1907);
        assertTrue(m(r1907, -5, -30, 50, -2, -40) <= 0, "1907: no offside in his own half");
        assertTrue(m(r1907, 5, -30, 50, 2, -40) > 0, "1907: still offside in the opponents' half");
        RulesProfile r1924 = RulesProfile.forSeason(1924);
        RulesProfile r1925 = RulesProfile.forSeason(1925);
        assertTrue(m(r1924, 30, 0, 50, 35, 10) > 0, "1924: two in front is not enough");
        assertTrue(m(r1925, 30, 0, 50, 35, 10) <= 0, "1925: two in front keep him onside");
    }

    @Test
    void nthSmallest() {
        OffsideRule.NthSmallest n = new OffsideRule.NthSmallest(3);
        assertEquals(Double.POSITIVE_INFINITY, n.value(), 0);
        for (double v : new double[] {9, 3, 7, 1, 8, 2}) {
            n.add(v);
        }
        assertEquals(3, n.value(), 0);
        n.reset();
        n.add(5);
        n.add(4);
        assertEquals(Double.POSITIVE_INFINITY, n.value(), 0);
    }

    // ---- in a world -------------------------------------------------------------------------------------------------

    /** Home taker, home forward at (40, -10), away keeper and backs; the laws started. */
    static final class Scene {
        final MatchWorld w;
        final Entity taker;
        final Entity forward;
        final Entity ball;

        Scene(PhysicsParams p, double takerX, double takerY, double takerFacing, double fwdX, double fwdY,
                double... awayX) {
            w = Laws.world(p, 7);
            taker = Laws.player(w, Team.HOME, Position.HALF_BACK, takerX, takerY, takerFacing);
            forward = Laws.player(w, Team.HOME, Position.CENTRE_FORWARD, fwdX, fwdY, 0);
            for (int i = 0; i < awayX.length; i++) {
                Laws.player(w, Team.AWAY, i == 0 ? Position.GOALKEEPER : Position.FULL_BACK, awayX[i], 5 + 4 * i,
                        StrictMath.PI);
            }
            ball = Laws.ball(w, 0, 0, 45);
            w.tick(); // laws start: kick-off awaited
        }

        PlayStateComponent state() {
            return Laws.state(ball);
        }

        /** Awards {@code type} to home at (x, y) and has the taker play it toward the forward. */
        void restartAndPlay(RestartType type, double x, double y) {
            boolean offside = w.params().rules.offsideFrom(type);
            Laws.laws(w).award(new Restart(type, Team.HOME, new Vec3(x, y, 0), offside, false,
                    type == RestartType.THROW_IN));
            ActionIntent intent = type == RestartType.THROW_IN ? ActionIntent.THROW_IN : ActionIntent.PASS;
            Laws.cmd(taker).set(intent, Laws.body(forward).x, Laws.body(forward).y, 0, 0.5, 15);
            w.tick();
            assertNotNull(Laws.find(w, LawRecord.RESTART_TAKEN), type + " taken");
            assertTrue(state().inPlay());
        }

        /** The forward plays the ball (put at his feet). */
        void forwardPlays() {
            Laws.place(ball, Laws.body(forward).x + 0.5, Laws.body(forward).y);
            Laws.cmd(forward).set(ActionIntent.DRIBBLE_TOUCH, HL - 5, 0, 0, 0.3, 15);
            w.tick();
        }
    }

    @Test
    void offsideFromAThrowInIn1888IsGivenWhenHePlaysTheBall() {
        Scene s = new Scene(Laws.params(1888), 20, -HW + 0.3, StrictMath.PI / 2, 40, -10, 50, 45, 30);
        s.restartAndPlay(RestartType.THROW_IN, 20, -HW);
        int fwd = Laws.index(s.forward);
        assertTrue(s.state().offsidePosition(fwd), "two opponents in front, ahead of the throw");
        assertEquals(10, s.state().margin[fwd], 1e-9);
        s.forwardPlays();
        LawRecord r = Laws.find(s.w, LawRecord.OFFSIDE);
        assertNotNull(r);
        assertTrue(r.appealed && r.given);
        assertEquals(Team.AWAY.ordinal(), r.appealingTeam);
        assertEquals(fwd, r.offender);
        assertEquals(RestartType.FREE_KICK.ordinal(), r.restartType);
        assertEquals(Team.AWAY.ordinal(), r.restartTeam);
        assertTrue(s.state().stopped());
        assertEquals(RestartType.FREE_KICK, s.state().restartType());
        assertEquals(Team.AWAY, s.state().restartTeam());
        assertFalse(s.state().restartGoalDirect, "1888-89: every free kick indirect");
        assertEquals(1, s.state().offsidesGiven);
    }

    @Test
    void noOffsideFromAThrowInFrom1920() {
        Scene s = new Scene(Laws.params(1920), 20, -HW + 0.3, StrictMath.PI / 2, 40, -10, 50, 45, 30);
        s.restartAndPlay(RestartType.THROW_IN, 20, -HW);
        assertFalse(s.state().offsidePosition(Laws.index(s.forward)));
        s.forwardPlays();
        assertNull(Laws.find(s.w, LawRecord.OFFSIDE));
        assertTrue(s.state().inPlay());
    }

    @Test
    void noOffsideFromAGoalKickOrACorner() {
        Scene gk = new Scene(Laws.params(1888), -HL + 2.4, 3, 0, 40, -10, 50, 45, 30);
        gk.restartAndPlay(RestartType.GOAL_KICK, -HL + 3, 3);
        assertFalse(gk.state().offsidePosition(Laws.index(gk.forward)));
        gk.forwardPlays();
        assertNull(Laws.find(gk.w, LawRecord.OFFSIDE));
        assertTrue(gk.state().inPlay(), "play goes on");

        Scene ck = new Scene(Laws.params(1888), HL - 0.5, HW - 1.0, -StrictMath.PI / 2, 40, -10, 50, 45, 30);
        ck.restartAndPlay(RestartType.CORNER_KICK, HL - 0.5, HW - 0.5);
        ck.forwardPlays();
        assertNull(Laws.find(ck.w, LawRecord.OFFSIDE));
        assertTrue(ck.state().inPlay());
    }

    @Test
    void ownHalfOffsideIn1888ButNotFrom1907() {
        for (int season : new int[] {1888, 1907}) {
            Scene s = new Scene(Laws.params(season), -30.6, 0, 0, -5, 10, 50, -2, -40);
            s.restartAndPlay(RestartType.FREE_KICK, -30, 0);
            boolean offside = s.state().offsidePosition(Laws.index(s.forward));
            assertEquals(season < 1907, offside, "season " + season);
            s.forwardPlays();
            assertEquals(season < 1907, Laws.find(s.w, LawRecord.OFFSIDE) != null, "season " + season);
        }
    }

    @Test
    void anUnappealedOffsideIsRecordedAndPlayGoesOn() {
        PhysicsParams p = Laws.params(1888);
        p.laws.appealOffside = 0;
        Scene s = new Scene(p, -30.6, 0, 0, 40, -10, 50, 45, 30);
        s.restartAndPlay(RestartType.FREE_KICK, -30, 0);
        s.forwardPlays();
        LawRecord r = Laws.find(s.w, LawRecord.OFFSIDE);
        assertNotNull(r);
        assertFalse(r.appealed);
        assertTrue(s.state().inPlay());
    }

    @Test
    void anOpponentPlayingTheBallWipesTheOffside() {
        Scene s = new Scene(Laws.params(1888), -30.6, 0, 0, 40, -10, 50, 45, 30);
        s.restartAndPlay(RestartType.FREE_KICK, -30, 0);
        assertTrue(s.state().offsidePosition(Laws.index(s.forward)));
        // an away back plays it: new snapshot for the away side, the home forward is no longer offside
        Entity back = s.w.engine().getEntities().get(3);
        Laws.place(s.ball, Laws.body(back).x - 0.5, Laws.body(back).y);
        Laws.cmd(back).set(ActionIntent.DRIBBLE_TOUCH, 0, 0, 0, 0.1, 15);
        s.w.tick();
        assertEquals(Team.AWAY.ordinal(), s.state().snapshotTeam);
        assertFalse(s.state().offsidePosition(Laws.index(s.forward)));
    }
}
