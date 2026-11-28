package com.rndmodgames.futtoboru.match.ecs.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitParams;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.body.TeamSheet;
import com.rndmodgames.futtoboru.match.ecs.laws.Officials;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;
import com.rndmodgames.futtoboru.system.Log;

/** E-15: tactics (playing short, passengers), perception, decisions in a whole match world, cost per tick. */
class AiSystemsTest {

    static final PitchGeometry PITCH = new PitchGeometry(PitchParams.defaults1888());

    /** A full match world as {@code EcsMatchEngine.play} builds it, with handles for the tests. */
    static final class Match {
        final MatchWorld w;
        final Entity[] players;
        final BallComponent ball;
        final PlayStateComponent play;
        final PossessionComponent poss;
        final TeamTacticsComponent home;
        final TeamTacticsComponent away;

        Match(long seed, TeamStyle hs, TeamStyle as, double halfMinutes) {
            PhysicsParams params = new PhysicsParams();
            w = new MatchWorld(params, new UniformPitch(Surface.DRY, PitchConditions.DRY),
                    RandomSource.seeded(seed).fork("match:ai"));
            MatchSystems.installSimulation(w, PITCH);
            MatchEnds ends = new MatchEnds(Team.HOME);
            players = BodySpawner.spawnTeams(w, PITCH, TeamSheet.stub(Shape.TWO_THREE_FIVE),
                    TeamSheet.stub(Shape.TWO_THREE_FIVE), KitParams.season1888(), ends, Team.HOME);
            Entity be = w.spawn(SpawnRole.BALL);
            ball = BallComponent.atRest(0, 0, params.ballDryMassKg);
            be.add(ball);
            Officials.spawn(w, PITCH, ends);
            play = PlayStateComponent.attach(be, ends, Team.HOME, halfMinutes, params.tickHz);
            AiSetup.install(w, new AiSetup.Side(Shape.TWO_THREE_FIVE, hs), new AiSetup.Side(Shape.TWO_THREE_FIVE, as),
                    ends);
            poss = be.getComponent(PossessionComponent.class);
            ImmutableArray<Entity> teams = w.engine().getEntitiesFor(Family.all(TeamTacticsComponent.class).get());
            home = teams.get(0).getComponent(TeamTacticsComponent.class);
            away = teams.get(1).getComponent(TeamTacticsComponent.class);
        }
    }

    // ---- tactics ----------------------------------------------------------------------------------------------

    @Test
    void aBackOffIsReplacedByTheNearestForwardAndTheKeeperByThePick() {
        TeamTacticsComponent t = new TeamTacticsComponent(Team.HOME, Shape.TWO_THREE_FIVE, TeamStyle.DRIBBLING,
                new MatchEnds(Team.HOME));
        assertEquals(1, t.attackSign);
        t.goneOff(1, -1); // right back (lateral -0.25): the inside right (-0.30) drops back
        assertEquals(TeamTacticsComponent.NO_SLOT, t.slotOf[1]);
        assertEquals(1, t.slotOf[7]);
        assertEquals(7, t.playerIn(1));
        assertEquals(-1, t.playerIn(7), "the forward line plays short");
        t.goneOff(0, 4); // keeper off: the centre half goes in goal, the centre forward fills the half
        assertEquals(0, t.slotOf[4]);
        assertEquals(4, t.slotOf[8]);
        assertEquals(9, t.onField());
        t.goneOff(0, 4);
        assertEquals(9, t.onField(), "off once");
    }

    @Test
    void aPassengerGoesOutToTheWing() {
        TeamTacticsComponent t = new TeamTacticsComponent(Team.AWAY, Shape.TWO_THREE_FIVE, TeamStyle.COMBINATION,
                new MatchEnds(Team.HOME));
        assertEquals(-1, t.attackSign);
        t.passenger(4); // centre half (lateral 0) swaps with the outside left
        assertEquals(10, t.slotOf[4]);
        assertEquals(4, t.slotOf[10]);
        t.passenger(0); // the keeper stays in goal
        assertEquals(0, t.slotOf[0]);
    }

    @Test
    void installNeedsTheLaws() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, 0),
                RandomSource.seeded(1L).fork("match:x"));
        MatchSystems.installSimulation(w);
        w.spawn(SpawnRole.BALL).add(BallComponent.atRest(0, 0, 0.4));
        assertThrows(IllegalStateException.class, () -> AiSetup.install(w,
                new AiSetup.Side(Shape.TWO_THREE_FIVE, TeamStyle.DRIBBLING),
                new AiSetup.Side(Shape.ONE_TWO_SEVEN, TeamStyle.DRIBBLING), new MatchEnds(Team.HOME)));
    }

    // ---- perception ---------------------------------------------------------------------------------------------

    @Test
    void perceptionIsDelayedNoisyAndStaggered() {
        Match m = new Match(7, TeamStyle.DRIBBLING, TeamStyle.COMBINATION, 45);
        PhysicsParams p = m.w.params();
        m.w.runTicks(600);
        int refreshed = 0;
        int off = 0;
        long tick = m.w.tickCount();
        m.w.tick();
        double sumErr = 0;
        for (int i = 0; i < 22; i++) {
            PerceptionComponent pc = m.players[i].getComponent(PerceptionComponent.class);
            InjuryComponent inj = m.players[i].getComponent(InjuryComponent.class);
            if (inj != null && inj.isOff()) {
                off++; // injured off the field: he no longer perceives (his beliefs freeze), whatever the seed
                continue;
            }
            if (pc.seenTick == tick - p.ai.reactionTicks) {
                refreshed++;
                BodyComponent me = m.players[i].getComponent(BodyComponent.class);
                assertEquals(me.prevX, pc.x[i], 0, "his own position is the truth (as perception saw it, before moving)");
            } else {
                long age = tick - p.ai.reactionTicks - pc.seenTick;
                assertTrue(age > 0 && age < p.thinkEveryTicks, "refreshed at the think rate: age " + age);
            }
            sumErr += Math.abs(pc.ballX - m.ball.x) + Math.abs(pc.ballY - m.ball.y);
        }
        assertTrue(off <= 2, "injuries off are rare: " + off);
        assertTrue(refreshed >= 6 - off && refreshed <= 8, "a third of the players think per tick: " + refreshed);
        assertTrue(sumErr > 0, "beliefs are not the truth");
        assertTrue(sumErr / (22 - off) < 10, "but close to it: " + sumErr / (22 - off));
    }

    // ---- decisions in a match ----------------------------------------------------------------------------------

    @Test
    void theKickOffIsTakenAndBothSidesPlay() {
        Match m = new Match(11, TeamStyle.DRIBBLING, TeamStyle.COMBINATION, 45);
        m.w.runTicks(30 * 30);
        assertTrue(m.poss.touches > 5, "the ball is played: " + m.poss.touches);
        int moving = 0;
        for (Entity e : m.players) {
            MoveCommand mc = e.getComponent(MoveCommand.class);
            moving += mc.targetVx != 0 || mc.targetVy != 0 ? 1 : 0;
        }
        assertTrue(moving > 10, "players move: " + moving);
        m.w.runTicks(30 * 60 * 8);
        long actions = m.w.engine().getSystem(ActionSystem.class).totalActions();
        assertTrue(actions > 200, "eight minutes of football: " + actions);
        assertEquals(0, m.poss.carryLimitReleases, "keepers let the ball go within two steps");
    }

    @Test
    void aPlayerOffLeavesTheShape() {
        Match m = new Match(3, TeamStyle.DRIBBLING, TeamStyle.DRIBBLING, 45);
        m.w.runTicks(100);
        InjuryComponent inj = m.players[2].getComponent(InjuryComponent.class); // home left back
        inj.state = InjuryComponent.OFF;
        m.w.runTicks(3);
        assertEquals(TeamTacticsComponent.NO_SLOT, m.home.slotOf[2]);
        int filler = m.home.playerIn(2);
        assertTrue(filler >= 6, "a forward drops back: " + filler);
        assertEquals(10, m.home.onField());
        assertEquals(11, m.away.onField());
    }

    @Test
    void sameSeedSameMatch() {
        Match a = new Match(5, TeamStyle.COMBINATION, TeamStyle.DRIBBLING, 2);
        Match b = new Match(5, TeamStyle.COMBINATION, TeamStyle.DRIBBLING, 2);
        Match c = new Match(6, TeamStyle.COMBINATION, TeamStyle.DRIBBLING, 2);
        a.w.runTicks(30 * 60 * 4);
        b.w.runTicks(30 * 60 * 4);
        c.w.runTicks(30 * 60 * 4);
        assertEquals(a.w.stateHash(), b.w.stateHash());
        assertNotEquals(a.w.stateHash(), c.w.stateHash());
        assertTrue(a.play.fullTime());
        assertEquals(-1, a.home.attackSign, "ends changed at half-time");
    }

    @Test
    void costAndAllocationPerTick() {
        Match m = new Match(9, TeamStyle.DRIBBLING, TeamStyle.COMBINATION, 45);
        int ticks = 30 * 60 * 3;
        m.w.runTicks(3 * ticks); // JIT warm-up
        com.sun.management.ThreadMXBean mx = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long tid = Thread.currentThread().getId();
        long bytes0 = mx.getThreadAllocatedBytes(tid);
        long t0 = System.nanoTime();
        m.w.runTicks(ticks);
        long ns = System.nanoTime() - t0;
        long bytes = mx.getThreadAllocatedBytes(tid) - bytes0;
        Log.info(Log.ENGINE_MATCH, "AiSystemsTest", String.format(Locale.ROOT,
                "ECS match with AI + laws: %.2f us per tick, %d bytes allocated over %d ticks", ns / (double) ticks
                        / 1000, bytes, ticks));
        // Only the laws' out-of-play sweep allocates (a few records per ball out of play).
        assertTrue(bytes < 512 * 1024, "allocated " + bytes + " bytes over " + ticks + " ticks");
    }
}
