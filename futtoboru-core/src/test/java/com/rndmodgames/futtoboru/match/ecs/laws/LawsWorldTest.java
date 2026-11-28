package com.rndmodgames.futtoboru.match.ecs.laws;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionIntent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.RestartType;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * E-16 in a whole world: a scripted kick-and-rush match (test script, not the AI) under the 1888-89 laws with the three
 * officials, two five-minute halves, pinned by a golden hash.
 */
class LawsWorldTest {

    /** Each side's nearest man chases and boots the ball upfield; restarts are taken by the nearest man. */
    static final class Script extends EntitySystem implements SimSystem {
        private final MatchWorld w;
        private final Entity[] players;
        private final Entity ballEntity;
        private final RandomSource rnd;
        private final double[] homeX = new double[22];
        private final double[] homeY = new double[22];

        Script(MatchWorld w, Entity[] players, Entity ballEntity) {
            super(SystemPriority.INPUT_COMMAND + 1);
            this.w = w;
            this.players = players;
            this.ballEntity = ballEntity;
            this.rnd = w.random().fork("test:script");
            for (int i = 0; i < players.length; i++) {
                homeX[i] = Laws.body(players[i]).x;
                homeY[i] = Laws.body(players[i]).y;
            }
        }

        @Override
        public void update(float ignored) {
            PlayStateComponent s = Laws.state(ballEntity);
            BallComponent b = Laws.ballOf(ballEntity);
            if (s.fullTime()) {
                return;
            }
            int nearHome = nearest(Team.HOME, b);
            int nearAway = nearest(Team.AWAY, b);
            boolean flip = s.half == 2;
            for (int i = 0; i < players.length; i++) {
                BodyComponent body = Laws.body(players[i]);
                MoveCommand move = players[i].getComponent(MoveCommand.class);
                ActionCommand cmd = Laws.cmd(players[i]);
                boolean chaser = i == nearHome || i == nearAway;
                if (s.stopped() && body.team != s.restartTeam()) {
                    chaser = false;
                }
                if (!chaser) {
                    double hx = flip ? -homeX[i] : homeX[i];
                    double hy = flip ? -homeY[i] : homeY[i];
                    move.toward(body.x, body.y, hx * 0.6 + b.x * 0.4, hy, 3.0);
                    move.charge = false;
                    continue;
                }
                move.toward(body.x, body.y, b.x, b.y, 6.5);
                int sign = s.attackSign(body.team);
                Team other = body.team.other();
                int opp = nearest(other, b);
                double ox = opp >= 0 ? Laws.body(players[opp]).x - body.x : 99;
                double oy = opp >= 0 ? Laws.body(players[opp]).y - body.y : 99;
                move.charge = ox * ox + oy * oy < 4;
                double dx = b.x - body.x;
                double dy = b.y - body.y;
                if (dx * dx + dy * dy < 1.2 && !cmd.armed()) {
                    double goalX = sign * Laws.PITCH.halfLength();
                    if (s.stopped() && s.restartType() == RestartType.THROW_IN) {
                        cmd.set(ActionIntent.THROW_IN, b.x + sign * 15, b.y * 0.5, 0, 0.6, 15);
                    } else if (Math.abs(goalX - b.x) < 25) {
                        cmd.set(ActionIntent.SHOT, goalX, (rnd.nextDouble() - 0.5) * 8, 1.0, 0.8, 15);
                    } else {
                        cmd.set(ActionIntent.PASS, b.x + sign * (15 + 15 * rnd.nextDouble()),
                                b.y + (rnd.nextDouble() - 0.5) * 30, rnd.nextDouble() < 0.3 ? 2 : 0, 0.6, 15);
                    }
                }
            }
        }

        private int nearest(Team team, BallComponent b) {
            int best = -1;
            double bd = Double.MAX_VALUE;
            for (int i = 0; i < players.length; i++) {
                BodyComponent body = Laws.body(players[i]);
                if (body.team != team || players[i].getComponent(InjuryComponent.class).isOff()) {
                    continue;
                }
                double dx = b.x - body.x;
                double dy = b.y - body.y;
                double d = dx * dx + dy * dy;
                if (d < bd) {
                    bd = d;
                    best = i;
                }
            }
            return best;
        }
    }

    /** Counts the laws' records by type (runs after the laws). */
    static final class Tally extends EntitySystem implements SimSystem {
        final int[] byType = new int[16];
        final int[] restarts = new int[RestartType.values().length];
        private final MatchWorld w;

        Tally(MatchWorld w) {
            super(SystemPriority.LAWS + 1);
            this.w = w;
        }

        @Override
        public void update(float ignored) {
            LawsSystem laws = Laws.laws(w);
            for (int k = 0; k < laws.recordCount(); k++) {
                LawRecord r = laws.record(k);
                byType[r.type]++;
                if (r.restartType >= 0 && r.type != LawRecord.RESTART_TAKEN) {
                    restarts[r.restartType]++;
                }
            }
        }
    }

    static final class Run {
        long hash;
        Tally tally;
        PlayStateComponent state;
    }

    static MatchWorld build(Run r) {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.WET, 0.5),
                RandomSource.seeded(1888).fork("match:laws-world"));
        MatchSystems.installSimulation(w);
        Entity[] players = BodySpawner.spawnDefault(w, Laws.PITCH);
        Entity ball = w.spawn(SpawnRole.BALL);
        ball.add(BallComponent.atRest(0, 0, w.params().ballDryMassKg));
        MatchEnds ends = new MatchEnds(Team.HOME);
        Officials.spawn(w, Laws.PITCH, ends);
        r.state = PlayStateComponent.attach(ball, ends, Team.HOME, 5.0, w.params().tickHz);
        w.addSimulation(new Script(w, players, ball));
        r.tally = w.addSimulation(new Tally(w));
        return w;
    }

    static Run run() {
        Run r = new Run();
        MatchWorld w = build(r);
        w.runTicks(2 * 5 * 60 * w.params().tickHz);
        r.hash = w.stateHash();
        return r;
    }

    @Test
    void lawsAllocateAlmostNothingPerTick() {
        Run r = new Run();
        MatchWorld w = build(r);
        w.runTicks(1800); // warm up: caches built, first restarts done
        com.sun.management.ThreadMXBean mx = (com.sun.management.ThreadMXBean) java.lang.management.ManagementFactory
                .getThreadMXBean();
        long tid = Thread.currentThread().getId();
        int outs0 = r.tally.byType[LawRecord.OUT_OF_PLAY] + r.tally.byType[LawRecord.FOUL];
        long bytes0 = mx.getThreadAllocatedBytes(tid);
        int ticks = 3600;
        w.runTicks(ticks);
        long bytes = mx.getThreadAllocatedBytes(tid) - bytes0;
        int events = r.tally.byType[LawRecord.OUT_OF_PLAY] + r.tally.byType[LawRecord.FOUL] - outs0;
        System.out.printf("ECS laws match: %d bytes allocated over %d ticks, %d stoppages%n", bytes, ticks, events);
        // stoppages build a few small records (restart, line crossing); nothing per tick
        assertTrue(bytes < 64 * 1024 + 2048L * events, "allocated " + bytes + " bytes over " + ticks + " ticks");
    }

    /**
     * Two five-minute halves of the scripted match. Same rule as BallPhysicsTest.GOLDEN: identical on every machine;
     * update only in a PR that changes laws, officials, action, ball or body physics on purpose (and bump
     * EcsMatchEngine.ENGINE_VERSION).
     */
    static final long GOLDEN_LAWS_MATCH = 0x8688ade850349a32L; // E-DET (ecs-0.6): own Gaussian, same on every JDK

    @Test
    void scriptedMatchUnderTheLawsIsDeterministicAndPinned() {
        Run a = run();
        Run b = run();
        assertEquals(a.hash, b.hash, "same seed, same match");
        Tally t = a.tally;
        assertTrue(t.byType[LawRecord.RESTART_TAKEN] >= 2, "kick-off and restarts taken: "
                + t.byType[LawRecord.RESTART_TAKEN]);
        assertTrue(t.byType[LawRecord.OUT_OF_PLAY] + t.byType[LawRecord.GOAL] + t.byType[LawRecord.DISPUTED_GOAL] > 0,
                "the ball went out of play");
        assertTrue(t.byType[LawRecord.FOUL] > 0, "charges were appealed");
        assertEquals(1, t.byType[LawRecord.HALF_TIME]);
        assertEquals(1, t.byType[LawRecord.FULL_TIME]);
        assertEquals(0, t.restarts[RestartType.PENALTY_KICK.ordinal()], "no penalties in 1888-89");
        assertTrue(a.state.fullTime());
        assertEquals(GOLDEN_LAWS_MATCH, a.hash, () -> "laws match " + Long.toHexString(a.hash) + " records "
                + java.util.Arrays.toString(t.byType));
    }
}
