package com.rndmodgames.futtoboru.match.ecs.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.body.Studs;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/** E-14: kicks, touches, headers, keeper handling and carrying, lace cuts, records. */
class ActionSystemTest {

    static final int ARM = 15;

    // ---- ground passes ---------------------------------------------------------------------------------------------

    /** Distance the ball travels after a 20 m ground pass, and its speed when it passes the target (0 if short). */
    static double[] groundPass(Surface surface, double wetness) {
        MatchWorld w = Actions.world(Actions.params(0), surface, wetness, 1);
        Entity p = Actions.player(w, Team.HOME, Position.HALF_BACK, 0, 0, 0);
        BallComponent ball = Actions.ball(w, 0.6, 0);
        Actions.cmd(p).set(ActionIntent.PASS, 20.6, 0, 0, 0.5, ARM);
        double atTarget = 0;
        for (int t = 0; t < 600 && !(t > 2 && ball.state == BallComponent.REST); t++) {
            double before = ball.x;
            w.tick();
            if (before < 20.6 && ball.x >= 20.6) {
                atTarget = Math.sqrt(ball.vx * ball.vx + ball.vy * ball.vy);
            }
        }
        assertEquals(ActionCommand.RESULT_DONE, Actions.cmd(p).result);
        assertEquals(0, ball.y, 1e-9, "straight");
        return new double[] {ball.x - 0.6, atTarget};
    }

    @Test
    void groundPassReachesItsTargetOnDryTurfAndFallsShortInMud() {
        double[] dry = groundPass(Surface.DRY, 0);
        assertTrue(dry[0] >= 20, "dry pass reaches the target: " + dry[0]);
        PhysicsParams p = new PhysicsParams();
        assertEquals(p.action.passArrivalMps, dry[1], 0.15, "arrives at the planned pace");
        double[] mud = groundPass(Surface.MUDDY, 0.8);
        assertTrue(mud[0] < 17, "the ball holds up in the mud: " + mud[0]);
        assertEquals(0, mud[1], 0);
    }

    /** Launch speed of the same 20 m pass by the same player, with a ball and boots as given. */
    static double launchSpeed(boolean soakedBall, double bootMassKg) {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity p = Actions.player(w, Team.HOME, Position.HALF_BACK, 0, 0, 0);
        p.add(new KitComponent(bootMassKg, 0, 30, Studs.BARS, false, 0)); // stays at this mass (no soaking)
        BallComponent ball = Actions.ball(w, 0.6, 0);
        if (soakedBall) {
            Actions.saturate(w, ball);
        }
        Actions.cmd(p).set(ActionIntent.PASS, 20.6, 0, 0, 0.5, ARM);
        w.tick();
        ActionRecord r = Actions.system(w).record(0);
        assertEquals(ActionRecord.KICKED, r.outcome);
        return Math.sqrt(r.afterVx * r.afterVx + r.afterVy * r.afterVy + r.afterVz * r.afterVz);
    }

    @Test
    void aSoakedBallLeavesSlowerAndWetBootsKickHarder() {
        double ref = new PhysicsParams().body.bootReferenceKg;
        double dry = launchSpeed(false, ref);
        double soaked = launchSpeed(true, ref);
        double wetBoots = launchSpeed(false, ref * 1.3);
        assertTrue(soaked < dry * 0.95, "soaked " + soaked + " vs dry " + dry);
        assertTrue(wetBoots > dry * 1.015, "wet boots " + wetBoots + " vs " + dry);
    }

    // ---- crosses ---------------------------------------------------------------------------------------------------

    @Test
    void aCrossArrivesNearTheTargetPointAndHeight() {
        for (double spin : new double[] {0, 6}) {
            MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
            Entity p = Actions.player(w, Team.HOME, Actions.skilled(Position.RIGHT_WING, 20), 32, -18,
                    StrictMath.PI / 4);
            BallComponent ball = Actions.ball(w, 32.5, -17.6);
            double tx = 44;
            double ty = 2;
            double tz = 1.8;
            Actions.cmd(p).set(ActionIntent.CROSS, tx, ty, tz, 0.5, ARM).withSpin(spin);
            double dx = tx - ball.x;
            double dy = ty - ball.y;
            double d = Math.sqrt(dx * dx + dy * dy);
            double ux = dx / d;
            double uy = dy / d;
            double x0 = ball.x;
            double y0 = ball.y;
            double prevProg = 0;
            double px = ball.x;
            double py = ball.y;
            double pz = ball.z;
            boolean seen = false;
            for (int t = 0; t < 200 && !seen; t++) {
                w.tick();
                double prog = (ball.x - x0) * ux + (ball.y - y0) * uy;
                if (prog >= d && ball.groundContacts == 0) {
                    double f = (d - prevProg) / (prog - prevProg);
                    double xa = px + f * (ball.x - px);
                    double ya = py + f * (ball.y - py);
                    double za = pz + f * (ball.z - pz);
                    assertEquals(tx, xa, 0.3, "spin " + spin + " x");
                    assertEquals(ty, ya, 0.3, "spin " + spin + " y");
                    assertEquals(tz, za, 0.2, "spin " + spin + " height");
                    seen = true;
                }
                prevProg = prog;
                px = ball.x;
                py = ball.y;
                pz = ball.z;
            }
            assertTrue(seen, "the cross reached the target in the air; landed at " + ball.contactX + ", "
                    + ball.contactY + " miss " + ball.groundContacts);
            assertTrue(Actions.system(w).totalActions() == 1);
        }
    }

    // ---- shot scatter ----------------------------------------------------------------------------------------------

    /** RMS direction error (rad) of {@code n} shots from one spot, against the noise-free launch. */
    static double shotScatter(double kicking, double stamina, boolean wetBall, int n) {
        double[] ref = shotLaunch(0, kicking, 1, wetBall, 1, 1)[0];
        double[][] shots = shotLaunch(1, kicking, stamina, wetBall, n, 7);
        double sum = 0;
        for (double[] v : shots) {
            double dot = v[0] * ref[0] + v[1] * ref[1] + v[2] * ref[2];
            double cx = v[1] * ref[2] - v[2] * ref[1];
            double cy = v[2] * ref[0] - v[0] * ref[2];
            double cz = v[0] * ref[1] - v[1] * ref[0];
            double angle = Math.sqrt(cx * cx + cy * cy + cz * cz) / dot;
            sum += angle * angle;
        }
        return Math.sqrt(sum / n);
    }

    static double[][] shotLaunch(double noise, double kicking, double stamina, boolean wetBall, int n, long seed) {
        MatchWorld w = Actions.world(Actions.params(noise), Surface.DRY, 0, seed);
        Entity p = Actions.player(w, Team.HOME, Actions.skilled(Position.CENTRE_FORWARD, kicking), 30, 0, 0);
        BallComponent ball = Actions.ball(w, 30.6, 0);
        AthleteComponent a = p.getComponent(AthleteComponent.class);
        BodyComponent b = Actions.body(p);
        double[][] out = new double[n][];
        for (int k = 0; k < n; k++) {
            Actions.place(ball, 30.6, 0, 0, 0, 0, 0);
            if (wetBall) {
                Actions.saturate(w, ball);
            }
            b.x = 30;
            b.y = 0;
            b.vx = 0;
            b.vy = 0;
            a.stamina = stamina;
            a.reserve = stamina;
            Actions.cmd(p).set(ActionIntent.SHOT, 50.3, 2.0, 1.2, 0.8, ARM);
            w.tick();
            ActionRecord r = Actions.system(w).record(0);
            out[k] = new double[] {r.afterVx, r.afterVy, r.afterVz};
        }
        return out;
    }

    @Test
    void shotsScatterMoreWithLowSkillFatigueAndAWetBall() {
        int n = 400;
        double base = shotScatter(20, 1, false, n);
        double lowSkill = shotScatter(5, 1, false, n);
        double tired = shotScatter(20, 0.1, false, n);
        double wet = shotScatter(20, 1, true, n);
        assertTrue(base > 0.005 && base < 0.06, "base " + base);
        assertTrue(lowSkill > base * 1.6, "low skill " + lowSkill + " vs " + base);
        assertTrue(tired > base * 1.4, "tired " + tired + " vs " + base);
        assertTrue(wet > base * 1.3, "wet ball " + wet + " vs " + base);
        // same seed, same scatter
        assertEquals(base, shotScatter(20, 1, false, n), 0);
    }

    // ---- first touch -----------------------------------------------------------------------------------------------

    /** Speed the ball keeps after a dribble touch on a ball arriving at {@code incoming} m/s. */
    static double afterTouch(double incoming, double control) {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity p = Actions.player(w, Team.HOME, Actions.skilled(Position.INSIDE_RIGHT, control), 0, 0, 0);
        BallComponent ball = Actions.ball(w, 0.6, 0);
        Actions.place(ball, 0.6, 0, 0, -incoming, 0, 0);
        Actions.cmd(p).set(ActionIntent.DRIBBLE_TOUCH, 5, 0, 0, 0, ARM);
        w.tick();
        ActionRecord r = Actions.system(w).record(0);
        assertEquals(ActionRecord.TOUCHED, r.outcome);
        return r.afterVx;
    }

    @Test
    void aSlowBallIsCushionedAFastOneBouncesOffAndControlHelps() {
        double slow = afterTouch(4, 13);
        PhysicsParams p = new PhysicsParams();
        assertEquals(p.action.dribbleTouchMin, slow, 1e-9, "a slow ball goes where he wants it");
        double fastPoor = afterTouch(20, 3);
        double fastGood = afterTouch(20, 23);
        assertTrue(fastPoor < 0, "a poor touch lets a fast ball run off him: " + fastPoor);
        assertTrue(fastGood > fastPoor, "good control keeps more of it: " + fastGood + " vs " + fastPoor);
    }

    // ---- headers ---------------------------------------------------------------------------------------------------

    static int header(double ballZ) {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity p = Actions.player(w, Team.HOME, Position.CENTRE_FORWARD, 0, 0, 0);
        BallComponent ball = Actions.ball(w, 0.4, 0);
        Actions.place(ball, 0.4, 0, ballZ, -8, 0, 0);
        Actions.cmd(p).set(ActionIntent.HEADER, 12, 0, 0, 0.5, 1);
        w.tick();
        if (Actions.system(w).recordCount() > 0) {
            assertEquals(ActionRecord.HEADED, Actions.system(w).record(0).outcome);
            ActionRecord r = Actions.system(w).record(0);
            double out = Math.sqrt(r.afterVx * r.afterVx + r.afterVy * r.afterVy + r.afterVz * r.afterVz);
            assertTrue(out <= p.getComponent(SkillComponent.class).heading * 5 + 4 + 0.45 * 8 + 1e-9,
                    "header pace is limited: " + out);
            assertTrue(r.afterVx > 0, "sent back the way it came");
        }
        return Actions.cmd(p).result;
    }

    @Test
    void headersOnlyWithTheBallInHeadRange() {
        assertEquals(ActionCommand.RESULT_EXPIRED, header(0.0));
        assertEquals(ActionCommand.RESULT_EXPIRED, header(0.9));
        assertEquals(ActionCommand.RESULT_DONE, header(1.4));
        assertEquals(ActionCommand.RESULT_DONE, header(1.9));
        assertEquals(ActionCommand.RESULT_EXPIRED, header(2.6));
    }

    // ---- lace --------------------------------------------------------------------------------------------------------

    /** Headers in a row with a dry or saturated ball; returns {strikes, cuts}. */
    static long[] headers(boolean wet, int n) {
        MatchWorld w = Actions.world(Actions.params(1), Surface.DRY, 0, wet ? 11 : 12);
        Entity p = Actions.player(w, Team.HOME, Position.CENTRE_FORWARD, 0, 0, 0);
        BallComponent ball = Actions.ball(w, 0.4, 0);
        BodyComponent b = Actions.body(p);
        for (int k = 0; k < n; k++) {
            Actions.place(ball, 0.4, 0, 1.5, -8, 0, 0);
            if (wet) {
                Actions.saturate(w, ball);
            }
            b.x = 0;
            b.y = 0;
            Actions.cmd(p).set(ActionIntent.HEADER, 12, 0, 0, 0.5, 1);
            w.tick();
        }
        ActionSystem s = Actions.system(w);
        assertEquals(n, s.totalActions());
        assertEquals(s.laceCuts(), p.getComponent(InjuryComponent.class).count, "each cut is a knock");
        return new long[] {s.laceStrikes(), s.laceCuts()};
    }

    @Test
    void laceCutsAreRareAndMoreFrequentWithAWetBall() {
        int n = 20_000;
        long[] dry = headers(false, n);
        long[] wet = headers(true, n);
        ActionParams a = new ActionParams();
        // ~1 in 8 headers meets the lace (BKP §3)
        assertEquals(a.laceStrikeShare * n, dry[0], 4 * Math.sqrt(n * 0.125 * 0.875));
        assertEquals(a.laceStrikeShare * n, wet[0], 4 * Math.sqrt(n * 0.125 * 0.875));
        // rare: expected 0.25 % dry, 0.5 % wet
        assertTrue(dry[1] > 15 && dry[1] < 100, "dry cuts " + dry[1]);
        assertTrue(wet[1] < n / 100, "wet cuts still rare: " + wet[1]);
        assertTrue(wet[1] > dry[1] * 1.3, "wet " + wet[1] + " vs dry " + dry[1]);
        MatchWorld w = Actions.world(new PhysicsParams(), Surface.DRY, 0, 1);
        assertEquals(2 * a.laceCutChance, Actions.system(w).laceCutChance(1), 1e-12);
    }

    // ---- keeper ----------------------------------------------------------------------------------------------------

    /** A home keeper (defends west) tries to catch a slow ball at x; returns the command result. */
    static int keeperCatch(PhysicsParams params, double x, double y) {
        MatchWorld w = Actions.world(params, Surface.DRY, 0, 1);
        Entity k = Actions.player(w, Team.HOME, Position.GOALKEEPER, x, y, StrictMath.PI);
        BallComponent ball = Actions.ball(w, x - 0.5, y);
        Actions.place(ball, x - 0.5, y, 0.8, 3, 0, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        int result = Actions.cmd(k).result;
        PossessionComponent poss = Actions.possession(w);
        if (result == ActionCommand.RESULT_DONE) {
            assertTrue(poss.held);
            w.runTicks(5);
            assertTrue(poss.held, "still in his hands");
            assertEquals(params.action.holdZ, ball.z, 1e-12, "the ball stays in his hands, not on the ground");
            assertEquals(0, ball.groundContacts);
        } else {
            assertFalse(poss.held);
            assertEquals(0, Actions.system(w).totalActions());
        }
        return result;
    }

    @Test
    void keeperHandlesOnlyInHisOwnHalfIn1888AndInHisAreaFrom1912() {
        PhysicsParams p1888 = Actions.params(0);
        assertEquals(ActionCommand.RESULT_DONE, keeperCatch(p1888, -10, 0));
        assertEquals(ActionCommand.RESULT_DONE, keeperCatch(p1888, -45, 20), "anywhere in his half");
        assertEquals(ActionCommand.RESULT_ILLEGAL_HANDLING, keeperCatch(p1888, 10, 0), "not in the opponents' half");

        PhysicsParams p1912 = Actions.params(0);
        p1912.action = ActionParams.forSeason(1912);
        p1912.action.noiseScale = 0;
        assertEquals(ActionCommand.RESULT_DONE, keeperCatch(p1912, -45, 0));
        assertEquals(ActionCommand.RESULT_ILLEGAL_HANDLING, keeperCatch(p1912, -25, 0), "outside the area");
        assertEquals(ActionCommand.RESULT_ILLEGAL_HANDLING, keeperCatch(p1912, -45, 25), "wide of the area");
        assertEquals(ActionParams.KeeperHandling.OWN_HALF, ActionParams.forSeason(1888).keeperHandling);
        assertEquals(ActionParams.KeeperHandling.OWN_HALF, ActionParams.forSeason(1911).keeperHandling);
    }

    @Test
    void outfieldPlayersNeverHandleAndAFastShotIsParried() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity back = Actions.player(w, Team.HOME, Position.FULL_BACK, -10, 0, 0);
        Entity k = Actions.player(w, Team.HOME, Position.GOALKEEPER, -40, 0, 0);
        BallComponent ball = Actions.ball(w, -10.5, 0);
        Actions.cmd(back).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        assertEquals(ActionCommand.RESULT_ILLEGAL_HANDLING, Actions.cmd(back).result);

        Actions.place(ball, -39.5, 0, 1.0, -30, 0, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        ActionRecord r = Actions.system(w).record(0);
        assertEquals(ActionRecord.PARRIED, r.outcome);
        assertTrue(r.handled);
        assertTrue(r.afterVx > 0 && r.afterVx < 30 * 0.5, "comes back off his hands slower: " + r.afterVx);
        assertFalse(Actions.possession(w).held);
    }

    @Test
    void keeperCarriesTwoStepsThenMustLetGoAndABounceRestartsTheCount() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity k = Actions.player(w, Team.HOME, Position.GOALKEEPER, -40, 0, 0);
        BallComponent ball = Actions.ball(w, -39.5, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        PossessionComponent poss = Actions.possession(w);
        assertTrue(poss.held);
        ActionParams a = w.params().action;
        double limitM = a.maxCarrySteps * a.stepLengthM;

        // walk forward: he gets ~1.6 m, then the ball drops
        k.getComponent(com.rndmodgames.futtoboru.match.ecs.body.MoveCommand.class).set(2, 0);
        double startX = Actions.body(k).x;
        int releasedAt = -1;
        for (int t = 0; t < 120 && releasedAt < 0; t++) {
            w.tick();
            ActionSystem s = Actions.system(w);
            for (int r = 0; r < s.recordCount(); r++) {
                if (s.record(r).outcome == ActionRecord.RELEASED_CARRY_LIMIT) {
                    releasedAt = t;
                    assertTrue(s.record(r).carrySteps > a.maxCarrySteps);
                    assertTrue(s.record(r).carrySteps < a.maxCarrySteps + 0.3);
                }
            }
        }
        assertTrue(releasedAt > 0, "released");
        assertFalse(poss.held);
        assertEquals(1, poss.carryLimitReleases);
        assertEquals(limitM, Actions.body(k).x - startX, 0.25);

        // catch again, bounce after one step: the count starts again
        k.getComponent(com.rndmodgames.futtoboru.match.ecs.body.MoveCommand.class).set(0, 0);
        w.runTicks(30);
        BodyComponent b = Actions.body(k);
        Actions.place(ball, b.x + 0.5, b.y, 0.5, 0, 0, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        assertTrue(poss.held);
        Actions.cmd(k).set(ActionIntent.DRIBBLE_TOUCH, 0, 0, 0, 0, ARM);
        w.tick();
        assertEquals(ActionRecord.BOUNCED, Actions.system(w).record(0).outcome);
        assertFalse(poss.held);
        assertEquals(0, poss.carriedM, 0);
    }

    @Test
    void aKeeperChargedToTheGroundDropsTheBall() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity k = Actions.player(w, Team.HOME, Position.GOALKEEPER, -40, 0, 0);
        BallComponent ball = Actions.ball(w, -39.5, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        assertTrue(Actions.possession(w).held);
        Actions.body(k).downTicks = 20;
        w.tick();
        ActionRecord r = Actions.system(w).record(0);
        assertEquals(ActionRecord.DROPPED_KNOCKED_DOWN, r.outcome);
        assertFalse(Actions.possession(w).held);
        w.runTicks(30);
        assertTrue(ball.groundContacts > 0, "the ball fell");
    }

    @Test
    void aKeeperPuntsFromHisHands() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity k = Actions.player(w, Team.HOME, Actions.skilled(Position.GOALKEEPER, 20), -40, 0, 0);
        BallComponent ball = Actions.ball(w, -39.5, 0);
        Actions.cmd(k).set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, ARM);
        w.tick();
        Actions.cmd(k).set(ActionIntent.CLEAR, -12, 5, 0, 1, ARM);
        w.tick();
        ActionRecord r = Actions.system(w).record(0);
        assertEquals(ActionRecord.KICKED, r.outcome);
        assertTrue(r.handled, "out of his hands");
        assertFalse(Actions.possession(w).held);
        for (int t = 0; t < 300 && ball.groundContacts == 0; t++) {
            w.tick();
        }
        assertEquals(1, ball.groundContacts);
        assertEquals(-12, ball.contactX, 0.3, "the punt landed where he aimed");
        assertEquals(5, ball.contactY, 0.3);
    }

    // ---- contest and records ---------------------------------------------------------------------------------------

    @Test
    void theNearestPlayerPlaysTheBallAndTheTouchIsRecorded() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity far = Actions.player(w, Team.HOME, Position.HALF_BACK, -0.75, 0, 0);
        Entity near = Actions.player(w, Team.AWAY, Position.HALF_BACK, 0.5, 0, StrictMath.PI);
        BallComponent ball = Actions.ball(w, 0, 0);
        Actions.cmd(far).set(ActionIntent.PASS, 20, 0, 0, 0.5, ARM);
        Actions.cmd(near).set(ActionIntent.PASS, -20, 0, 0, 0.5, ARM);
        w.tick();
        assertEquals(ActionCommand.RESULT_CONTESTED, Actions.cmd(far).result);
        assertEquals(ActionCommand.RESULT_DONE, Actions.cmd(near).result);
        ActionSystem s = Actions.system(w);
        assertEquals(1, s.recordCount());
        ActionRecord r = s.record(0);
        assertEquals(1, r.actor);
        assertEquals(Team.AWAY.ordinal(), r.team);
        assertEquals(ActionIntent.PASS, r.intent);
        assertEquals(-20, r.targetX, 0);
        assertEquals(0, r.beforeVx, 0);
        assertTrue(r.afterVx < 0);
        assertTrue(r.footSpeed > 0 && r.impulseNs > 0);
        PossessionComponent poss = Actions.possession(w);
        assertEquals(1, poss.lastTouchIndex);
        assertEquals(Team.AWAY.ordinal(), poss.lastTouchTeam);
        assertEquals(ActionIntent.PASS.ordinal(), poss.lastTouchIntent);
        assertTrue(ball.vx < 0);
    }

    @Test
    void anActionWaitsForTheBallThenExpires() {
        MatchWorld w = Actions.world(Actions.params(0), Surface.DRY, 0, 1);
        Entity p = Actions.player(w, Team.HOME, Position.HALF_BACK, 0, 0, 0);
        BallComponent ball = Actions.ball(w, 5, 0);
        Actions.place(ball, 5, 0, 0, -6, 0, 0);
        Actions.cmd(p).set(ActionIntent.PASS, 0, 20, 0, 0.5, 40);
        int t = 0;
        while (Actions.cmd(p).armed() && t++ < 40) {
            w.tick();
        }
        assertEquals(ActionCommand.RESULT_DONE, Actions.cmd(p).result, "played when it arrived");
        assertTrue(t > 5);
        Actions.cmd(p).set(ActionIntent.PASS, 0, 20, 0, 0.5, ARM);
        Actions.place(ball, 30, 30, 0, 0, 0, 0);
        w.runTicks(ARM + 1);
        assertEquals(ActionCommand.RESULT_EXPIRED, Actions.cmd(p).result);
    }
}
