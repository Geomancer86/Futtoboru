package com.rndmodgames.futtoboru.match.ecs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;

/** E-10: world, registration rules, draw passes vs simulation, clock, state hash, entry point golden hash. */
class MatchWorldTest {

    static MatchWorld world(PitchConditions pitch) {
        return new MatchWorld(new PhysicsParams(), pitch, RandomSource.seeded(1888L).fork("match:test"));
    }

    /** A world with the simulation installed, 22 player stubs, the ball kicked off and an umpire stub. */
    static MatchWorld kickedOff() {
        MatchWorld w = world(new UniformPitch(Surface.WET, PitchConditions.RAIN));
        MatchSystems.installSimulation(w);
        for (int i = 0; i < 11; i++) {
            w.spawn(SpawnRole.HOME_PLAYER);
        }
        for (int i = 0; i < 11; i++) {
            w.spawn(SpawnRole.AWAY_PLAYER);
        }
        BallComponent b = BallComponent.atRest(0, 0, w.params().ballDryMassKg);
        w.spawn(SpawnRole.BALL).add(b);
        w.spawn(SpawnRole.OFFICIAL);
        BallSystem balls = w.engine().getSystem(BallSystem.class);
        balls.physics().kick(b, 8.0, StrictMath.toRadians(30), StrictMath.toRadians(20), 5.0);
        return w;
    }

    // ---- draw passes ---------------------------------------------------------------------------------------------

    /** Reads the ball every frame, writes nothing. */
    static final class BallReaderPass extends EntitySystem implements DrawPass {
        final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);
        ImmutableArray<Entity> entities;
        int frames;
        int updates;
        double lastX;

        BallReaderPass(int priority) {
            super(priority);
        }

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(Family.all(BallComponent.class).get());
        }

        @Override
        public void update(float delta) {
            updates++; // must never happen: draw passes are not part of the tick
        }

        @Override
        public void draw(double alpha) {
            frames++;
            lastX = balls.get(entities.get(0)).x;
        }
    }

    /** A buggy pass that nudges the ball. */
    static final class MutatingPass extends EntitySystem implements DrawPass {
        ImmutableArray<Entity> entities;

        MutatingPass() {
            super(SystemPriority.DRAW_PARTICLES);
        }

        @Override
        public void addedToEngine(Engine engine) {
            entities = engine.getEntitiesFor(Family.all(BallComponent.class).get());
        }

        @Override
        public void draw(double alpha) {
            entities.get(0).getComponent(BallComponent.class).x += 1e-12;
        }
    }

    static final class Both extends EntitySystem implements DrawPass, SimSystem {
        @Override
        public void draw(double alpha) {
        }
    }

    static final class Sim extends EntitySystem implements SimSystem {
        Sim(int priority) {
            super(priority);
        }
    }

    @Test
    void drawPassesNeverRunInTheTickAndNeverChangeTheSimulation() {
        int ticks = 30 * 20;
        MatchWorld headless = kickedOff();
        long[] expected = new long[ticks];
        for (int t = 0; t < ticks; t++) {
            headless.tick();
            expected[t] = headless.stateHash();
        }

        MatchWorld viewed = kickedOff();
        BallReaderPass ball = viewed.addDrawPass(new BallReaderPass(SystemPriority.DRAW_ACTORS));
        BallReaderPass debug = viewed.addDrawPass(new BallReaderPass(SystemPriority.DRAW_DEBUG));
        viewed.setDrawGuard(true);
        long[] actual = new long[ticks];
        for (int t = 0; t < ticks; t++) {
            viewed.tick();
            viewed.draw(0.5);
            viewed.draw(0.9); // several frames per tick must not matter either
            actual[t] = viewed.stateHash();
        }
        assertArrayEquals(expected, actual);
        assertEquals(0, ball.updates + debug.updates, "a draw pass ran inside the simulation tick");
        assertEquals(2 * ticks, ball.frames);
        assertTrue(ball.lastX > 1, "the pass saw the ball move: " + ball.lastX);
    }

    @Test
    void theDrawGuardCatchesAPassThatWritesSimulationState() {
        MatchWorld w = kickedOff();
        w.addDrawPass(new MutatingPass());
        w.setDrawGuard(true);
        w.tick();
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> w.draw(0));
        assertTrue(e.getMessage().contains("MutatingPass"), e.getMessage());
    }

    @Test
    void registrationRules() {
        MatchWorld w = world(new UniformPitch(Surface.DRY, 0));
        assertThrows(IllegalArgumentException.class, () -> w.addSimulation(new Both()));
        assertThrows(IllegalArgumentException.class, () -> w.addDrawPass(new Both()));
        w.addSimulation(new Sim(SystemPriority.LAWS));
        assertThrows(IllegalArgumentException.class, () -> w.addSimulation(new Sim(SystemPriority.LAWS)));
        w.addDrawPass(new BallReaderPass(SystemPriority.DRAW_DEBUG));
        assertThrows(IllegalArgumentException.class,
                () -> w.addDrawPass(new BallReaderPass(SystemPriority.DRAW_DEBUG)));
    }

    @Test
    void drawPassesRunInPriorityOrderWhateverTheRegistrationOrder() {
        MatchWorld w = world(new UniformPitch(Surface.DRY, 0));
        StringBuilder order = new StringBuilder();
        class Named extends EntitySystem implements DrawPass {
            final String name;

            Named(int priority, String name) {
                super(priority);
                this.name = name;
            }

            @Override
            public void draw(double alpha) {
                order.append(name);
            }
        }
        w.addDrawPass(new Named(SystemPriority.DRAW_DEBUG, "d"));
        w.addDrawPass(new Named(SystemPriority.DRAW_PITCH, "p"));
        w.addDrawPass(new Named(SystemPriority.DRAW_ACTORS, "b"));
        w.addDrawPass(new Named(SystemPriority.DRAW_INTERPOLATION, "i"));
        w.draw(0);
        assertEquals("ipbd", order.toString());
    }

    @Test
    void entitiesAreCreatedInTheFixedOrderAndBeforeTheFirstTick() {
        MatchWorld w = world(new UniformPitch(Surface.DRY, 0));
        Entity home = w.spawn(SpawnRole.HOME_PLAYER);
        Entity ball = w.spawn(SpawnRole.BALL);
        assertEquals(0, home.getComponent(IdentityComponent.class).index);
        assertEquals(1, ball.getComponent(IdentityComponent.class).index);
        assertThrows(IllegalStateException.class, () -> w.spawn(SpawnRole.AWAY_PLAYER));
        w.spawn(SpawnRole.OFFICIAL);
        w.tick();
        assertThrows(IllegalStateException.class, () -> w.spawn(SpawnRole.CROWD_SECTION));
    }

    // ---- clock ---------------------------------------------------------------------------------------------------

    @Test
    void fixedStepClockCarriesLeftoverTimeCapsStepsAndScalesStepsBySpeed() {
        FixedStepClock clock = new FixedStepClock(1.0 / 30, 5);
        assertEquals(0, clock.advance(0.02));
        assertEquals(1, clock.advance(0.02)); // 0.04 s accumulated
        assertEquals((0.04 - 1.0 / 30) * 30, clock.alpha(), 1e-12);
        clock.setSpeed(4);
        assertEquals(2, clock.advance(1.0 / 60)); // 4 x 1/60 + leftover = 2.2 steps
        assertEquals(5, clock.advance(10)); // capped, backlog dropped
        assertEquals(0, clock.alpha(), 0);

        MatchWorld w = world(new UniformPitch(Surface.DRY, 0));
        BallReaderPass pass = w.addDrawPass(new BallReaderPass(SystemPriority.DRAW_ACTORS));
        w.spawn(SpawnRole.BALL).add(BallComponent.atRest(0, 0, 0.4));
        FixedStepClock viewer = new FixedStepClock(w.tickSeconds(), 8);
        int ticks = 0;
        for (int f = 0; f < 60; f++) {
            ticks += w.frame(viewer, 1.0 / 60); // one second at 60 fps
        }
        assertEquals(30, ticks, 1);
        assertEquals(60, pass.frames);
        assertEquals(ticks, w.tickCount());
    }

    // ---- state hash ----------------------------------------------------------------------------------------------

    @Test
    void describeModeShowsTheHashInputsAndAgreesWithTheHash() {
        MatchWorld w = kickedOff();
        w.runTicks(10);
        String text = w.describeState();
        assertTrue(text.startsWith("tick=10\n"), text);
        assertTrue(text.contains("ball.vx="), text);
        assertTrue(text.contains("id.role=" + SpawnRole.OFFICIAL.ordinal()), text);
        assertTrue(text.endsWith("hash=" + Long.toHexString(w.stateHash()) + "\n"), text);
        long before = w.stateHash();
        w.tick();
        assertNotEquals(before, w.stateHash());
    }

    // ---- entry point ---------------------------------------------------------------------------------------------

    /**
     * Golden hash of {@code play} over a short match (two halves of 3 minutes, 10,800 ticks): the full loop with the
     * player AI (E-15) and the laws (E-16); see BallPhysicsTest for the rule. The 90-minute goldens and the wet-ball
     * masses are in {@link EcsPlayGoldenTest} (slow tier). Pinned by E-15 (ecs-0.5); re-pinned by E-16b (ecs-0.6);
     * by E-20 (ecs-0.8): home advantage and a fifth of the injury rate; E-20c (ecs-0.9): shot range 28 to 27 m; E-18 (ecs-0.10): crowd sections.
     */
    static final long GOLDEN_SHORT_PLAY = 0x6fb97b2d6404564eL;

    @Test
    void shortPlayIsDeterministicAndPinnedByAGoldenHash() {
        EcsMatchEngine engine = new EcsMatchEngine();
        RandomSource game = RandomSource.seeded(1888L);
        EcsMatchInput in = new EcsMatchInput("short", new UniformPitch(Surface.DRY, PitchConditions.DRY), 0, 3);
        EcsMatchResult a = engine.play(in, game.fork("match:short"));
        assertEquals(a, engine.play(in, game.fork("match:short")));
        assertEquals(10_800, a.ticks());
        assertEquals(EcsMatchEngine.ENGINE_VERSION, a.engineVersion());
        assertTrue(a.ballActions() > 50, "the AI plays the ball: " + a.ballActions());
        assertEquals(GOLDEN_SHORT_PLAY, a.finalStateHash(), () -> "short " + Long.toHexString(a.finalStateHash()));
    }
}
