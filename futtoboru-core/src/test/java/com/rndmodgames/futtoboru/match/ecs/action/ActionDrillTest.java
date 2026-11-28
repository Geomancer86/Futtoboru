package com.rndmodgames.futtoboru.match.ecs.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.rndmodgames.futtoboru.data.Position;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.system.Log;

/**
 * E-14 in a whole world: a scripted drill in the rain (ground passes, crosses and headers, dribbles, shots, keeper
 * catches and punts, a presser who charges) pinned by a golden hash, and its cost and allocation per tick.
 */
class ActionDrillTest {

    static final int ARM = 10;

    /** Two home players pass, cross, dribble and shoot at the home keeper's goal; one away player chases. */
    static final class Drill {
        final MatchWorld w;
        final BallComponent ball;
        final BodyComponent[] body = new BodyComponent[4];
        final MoveCommand[] move = new MoveCommand[4];
        final ActionCommand[] act = new ActionCommand[4];
        final PossessionComponent[] poss = new PossessionComponent[1];
        final double[] homeX = {-46, -18, -18, -8};
        final double[] homeY = {0, -9, 9, 0};
        final Entity[] ent = new Entity[4];
        int passes;
        int heldTicks;
        int restarts;

        Drill() {
            this(1888);
        }

        Drill(long seed) {
            w = Actions.world(Actions.params(1), Surface.WET, PitchConditions.RAIN, seed);
            Entity k = Actions.player(w, Team.HOME, Actions.skilled(Position.GOALKEEPER, 16), homeX[0], homeY[0], 0);
            Entity a = Actions.player(w, Team.HOME, Actions.skilled(Position.INSIDE_LEFT, 15), homeX[1], homeY[1], 0);
            Entity b = Actions.player(w, Team.HOME, Actions.skilled(Position.INSIDE_RIGHT, 11), homeX[2], homeY[2],
                    0);
            Entity c = Actions.player(w, Team.AWAY, Position.HALF_BACK, homeX[3], homeY[3], StrictMath.PI);
            Entity[] es = {k, a, b, c};
            for (int i = 0; i < 4; i++) {
                ent[i] = es[i];
                body[i] = es[i].getComponent(BodyComponent.class);
                move[i] = es[i].getComponent(MoveCommand.class);
                act[i] = es[i].getComponent(ActionCommand.class);
            }
            ball = Actions.ball(w, homeX[1] + 0.6, homeY[1]);
        }

        void tick() {
            if (poss[0] == null) {
                w.tick(); // the action system adds the possession component on its first tick
                poss[0] = Actions.possession(w);
                return;
            }
            PossessionComponent p = poss[0];
            if (Math.abs(ball.x) > 50 || Math.abs(ball.y) > 32) {
                restarts++;
                Actions.place(ball, body[1].x + 0.6, body[1].y, 0, 0, 0, 0);
            }
            // keeper: holds a while, then punts upfield; otherwise covers his goal and waits for shots
            if (p.held) {
                move[0].set(0, 0);
                if (++heldTicks == 15) {
                    act[0].set(ActionIntent.CLEAR, -15, (passes % 3) * 6 - 6, 0, 1, ARM);
                }
            } else {
                heldTicks = 0;
                double gy = Math.max(-3, Math.min(3, ball.y));
                move[0].toward(body[0].x, body[0].y, homeX[0], gy, 3);
                if (!act[0].armed() && ball.x < -38 && ball.vx < -4) {
                    act[0].set(ActionIntent.KEEPER_CATCH, 0, 0, 0, 0, 3 * ARM);
                }
            }
            // the nearer of A and B goes to the ball, the other returns to his spot; a player the presser injured off
            // the field takes no further part (otherwise he stays "nearer" for ever and the drill stalls)
            double da = off(1) ? Double.MAX_VALUE : dist(1);
            double db = off(2) ? Double.MAX_VALUE : dist(2);
            int near = da <= db ? 1 : 2;
            int other = 3 - near;
            if (p.held) {
                move[1].toward(body[1].x, body[1].y, homeX[1], homeY[1], 3);
                move[2].toward(body[2].x, body[2].y, homeX[2], homeY[2], 3);
            } else {
                double disc = ball.vz * ball.vz + 2 * 9.81 * (ball.z - 1.6);
                if (ball.z > 0.5 && disc > 0) {
                    // a lofted ball: run to where it comes down to head height
                    double tf = (ball.vz + Math.sqrt(disc)) / 9.81;
                    move[near].toward(body[near].x, body[near].y, ball.x + ball.vx * tf, ball.y + ball.vy * tf, 5);
                } else {
                    move[near].toward(body[near].x, body[near].y, ball.x, ball.y, 5);
                }
                move[other].toward(body[other].x, body[other].y, homeX[other], homeY[other], 3);
                if (!act[near].armed()) {
                    if (ball.z > 1.2 && dist(near) < 3) {
                        act[near].set(ActionIntent.HEADER, body[other].x, body[other].y, 0, 0.5, ARM);
                    } else if (dist(near) < 1.2 && (p.lastTouchIndex != near || w.tickCount() - p.lastTouchTick > 20)) {
                        arm(near, other);
                    }
                }
            }
            // the presser chases the ball and charges every other second
            move[3].toward(body[3].x, body[3].y, ball.x, ball.y, 3.5);
            move[3].charge = (w.tickCount() / 60) % 2 == 0;
            if (!act[3].armed() && dist(3) < 1.0 && !p.held && passes % 4 == 1) {
                // always upfield of the ball: a fixed target he can stand on turned into a tap to himself every tick
                act[3].set(ActionIntent.CLEAR, Math.max(20, ball.x + 15), 0, 0, 1, ARM);
            }
            w.tick();
        }

        private void arm(int me, int mate) {
            switch (passes++ % 5) {
                case 0:
                case 1:
                    act[me].set(ActionIntent.PASS, body[mate].x, body[mate].y, 0, 0.5, ARM);
                    break;
                case 2:
                    act[me].set(ActionIntent.CROSS, homeX[mate], homeY[mate], 1.7, 0.5, ARM).withSpin(3);
                    break;
                case 3:
                    act[me].set(ActionIntent.DRIBBLE_TOUCH, body[mate].x, body[mate].y, 0, 0.6, ARM);
                    break;
                default:
                    act[me].set(ActionIntent.SHOT, -50.3, (passes % 3) - 1.0, 1.0, 0.7, ARM);
                    break;
            }
        }

        private boolean off(int i) {
            InjuryComponent inj = ent[i].getComponent(InjuryComponent.class);
            return inj != null && inj.isOff();
        }

        private double dist(int i) {
            double dx = ball.x - body[i].x;
            double dy = ball.y - body[i].y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }

    static long drillHash(int ticks, long[] stats) {
        return drillHash(1888, ticks, stats);
    }

    static long drillHash(long seed, int ticks, long[] stats) {
        Drill d = new Drill(seed);
        long caught = 0;
        long headed = 0;
        ActionSystem s = Actions.system(d.w);
        for (int t = 0; t < ticks; t++) {
            d.tick();
            for (int r = 0; r < s.recordCount(); r++) {
                int o = s.record(r).outcome;
                caught += o == ActionRecord.CAUGHT ? 1 : 0;
                headed += o == ActionRecord.HEADED ? 1 : 0;
            }
        }
        if (stats != null) {
            stats[0] = s.totalActions();
            stats[1] = caught;
            stats[2] = headed;
            stats[3] = d.restarts;
        }
        return d.w.stateHash();
    }

    /** Sanity bounds on actions in two minutes of drill (any seed gives 26..59; the golden pins the exact run). */
    static final int MIN_ACTIONS = 15;
    static final int MAX_ACTIONS = 150;

    /**
     * Two minutes of the drill in rain. Same rule as BallPhysicsTest.GOLDEN: identical on every machine; update only
     * in a PR that changes action, ball or body physics on purpose (and bump EcsMatchEngine.ENGINE_VERSION).
     */
    static final long GOLDEN_DRILL = 0x875d0939ac2edbd6L;

    @Test
    void drillIsDeterministicAndPinnedByAGoldenHash() {
        long[] stats = new long[4];
        long a = drillHash(30 * 120, stats);
        assertEquals(a, drillHash(30 * 120, null));
        Log.info(Log.ENGINE_MATCH, "ActionDrillTest", "drill: " + stats[0] + " actions, " + stats[1] + " catches, "
                + stats[2] + " headers, " + stats[3] + " restarts, hash " + Long.toHexString(a));
        assertTrue(stats[0] > MIN_ACTIONS && stats[0] < MAX_ACTIONS, "the drill plays the ball: " + stats[0]);
        assertEquals(GOLDEN_DRILL, a, () -> "drill " + Long.toHexString(a));
    }

    /**
     * The drill keeps playing the ball whatever the seed: over 100 seeds it makes 26..59 actions in two minutes, so
     * the bounds catch a stalled drill (too few) or a runaway loop (one player re-kicking every tick: a seed once made
     * 2583) without depending on the exact random sequence.
     */
    @Test
    void drillPlaysTheBallOnEverySeed() {
        for (long seed = 1885; seed < 1893; seed++) {
            long[] stats = new long[4];
            drillHash(seed, 30 * 120, stats);
            assertTrue(stats[0] > MIN_ACTIONS && stats[0] < MAX_ACTIONS, "seed " + seed + ": " + stats[0] + " actions");
        }
    }

    @Test
    void costAndAllocationPerTick() {
        Drill d = new Drill();
        int ticks = 30 * 60 * 5;
        for (int round = 0; round < 3; round++) { // JIT warm-up
            for (int t = 0; t < ticks; t++) {
                d.tick();
            }
        }
        com.sun.management.ThreadMXBean mx = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long tid = Thread.currentThread().getId();
        long actions0 = Actions.system(d.w).totalActions();
        long bytes0 = mx.getThreadAllocatedBytes(tid);
        long t0 = System.nanoTime();
        long sink = 0;
        for (int t = 0; t < ticks; t++) {
            d.tick();
            sink += d.w.stateHash();
        }
        long ns = System.nanoTime() - t0;
        long bytes = mx.getThreadAllocatedBytes(tid) - bytes0;
        long actions = Actions.system(d.w).totalActions() - actions0;
        Log.info(Log.ENGINE_MATCH, "ActionDrillTest", String.format(java.util.Locale.ROOT,
                "ECS action drill: %.3f us per tick with state hash, %d bytes allocated over %d ticks, %d actions"
                        + " (sink %d)",
                ns / (double) ticks / 1000, bytes, ticks, actions, sink & 1));
        assertTrue(actions > 50, "actions in the measured window: " + actions);
        assertTrue(bytes < 64 * 1024, "allocated " + bytes + " bytes over " + ticks + " ticks");
    }
}
