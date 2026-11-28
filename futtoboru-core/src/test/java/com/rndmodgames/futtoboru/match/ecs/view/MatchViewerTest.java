package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.DrawPass;
import com.rndmodgames.futtoboru.match.ecs.FixedStepClock;
import com.rndmodgames.futtoboru.match.ecs.FrameContext;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;
import com.rndmodgames.futtoboru.ui.gfx.NoGl;

/**
 * E-21a viewer scaffold, headless: draw passes never change the state hash with the frame context, interpolation,
 * time-budgeted clock, priority bands, ArtGuard, and a Texture-using pass under {@link NoGl}.
 */
class MatchViewerTest {

    /** Simulation installed, 22 player stubs, a kicked ball and an umpire stub (as MatchWorldTest.kickedOff). */
    static MatchWorld kickedOff() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.WET, PitchConditions.RAIN),
                RandomSource.seeded(1888L).fork("match:test"));
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
        w.engine().getSystem(BallSystem.class).physics().kick(b, 8.0, StrictMath.toRadians(30),
                StrictMath.toRadians(20), 5.0);
        return w;
    }

    static Entity ball(MatchWorld w) {
        return w.engine().getEntities().get(22);
    }

    static ViewFrame frame() {
        return new ViewFrame(GraphicsQuality.load());
    }

    /** A clock with a frozen wall clock: the budget never runs out. */
    static ViewerClock clock(MatchWorld w) {
        return new ViewerClock(w.tickSeconds(), 16, 60, 1.0, () -> 0L);
    }

    @AfterEach
    void resetGuards() {
        ArtGuard.forbid(false);
    }

    /** Records what the frame context says, writes nothing. */
    static final class FrameRecorder extends EntitySystem implements DrawPass {
        final List<double[]> seen = new ArrayList<>();

        FrameRecorder() {
            super(SystemPriority.DRAW_DEBUG);
        }

        @Override
        public void draw(FrameContext frame) {
            seen.add(new double[] { frame.alpha(), frame.frameSeconds(), frame.viewSeconds(), frame.tick() });
        }
    }

    // ---- draw guard with the frame context -----------------------------------------------------------------------

    @Test
    void allDrawPassesWithFrameContextNeverChangeTheStateHash() {
        int ticks = 30 * 20;
        MatchWorld headless = kickedOff();
        long[] expected = new long[ticks + 1];
        expected[0] = headless.stateHash();
        for (int t = 1; t <= ticks; t++) {
            headless.tick();
            expected[t] = headless.stateHash();
        }

        MatchWorld viewed = kickedOff();
        MatchViewer viewer = new MatchViewer(viewed, frame(), clock(viewed));
        FrameRecorder recorder = viewed.addDrawPass(new FrameRecorder());
        viewed.setDrawGuard(true);
        long[] actual = new long[ticks + 1];
        actual[0] = viewed.stateHash();
        double[] frames = { 1.0 / 60, 1.0 / 144, 0.05, 1.0 / 30, 0.0, 0.1 };
        int f = 0;
        while (viewed.tickCount() < ticks) {
            viewer.frame(frames[f++ % frames.length]);
            if (viewed.tickCount() <= ticks) {
                actual[(int) viewed.tickCount()] = viewed.stateHash();
            }
        }
        // Frames that ran several ticks only saw the last hash; compare the ticks a frame ended on.
        for (int t = 0; t <= ticks; t++) {
            if (actual[t] != 0) {
                assertEquals(expected[t], actual[t], "tick " + t);
            }
        }
        assertEquals(f, recorder.seen.size());
        double view = 0;
        for (int i = 0; i < recorder.seen.size(); i++) {
            double[] s = recorder.seen.get(i);
            assertTrue(s[0] >= 0 && s[0] <= 1, "alpha " + s[0]);
            assertEquals(frames[i % frames.length], s[1], 0, "real frame delta reaches the pass");
            view += frames[i % frames.length];
            assertEquals(view, s[2], 1e-9);
        }
    }

    @Test
    void qualityLevelsAndSwitchesNeverChangeTheSimulation() {
        int ticks = 300;
        MatchWorld a = kickedOff();
        MatchWorld b = kickedOff();
        MatchViewer va = new MatchViewer(a, frame(), clock(a));
        MatchViewer vb = new MatchViewer(b, frame(), clock(b));
        va.view().quality().select(GraphicsQuality.ULTRA);
        vb.view().quality().select(GraphicsQuality.LOW);
        vb.view().quality().switches().setMaster(false);
        a.setDrawGuard(true);
        b.setDrawGuard(true);
        while (a.tickCount() < ticks) {
            va.frame(1.0 / 60);
        }
        while (b.tickCount() < ticks) {
            vb.frame(1.0 / 45);
        }
        a.runTicks(ticks + 10 - a.tickCount());
        b.runTicks(ticks + 10 - b.tickCount());
        assertEquals(a.stateHash(), b.stateHash());
    }

    @Test
    void theDrawGuardStillCatchesAWritingPassThroughTheViewer() {
        MatchWorld w = kickedOff();
        MatchViewer viewer = new MatchViewer(w, frame(), clock(w));
        class Nudger extends EntitySystem implements DrawPass {
            Nudger() {
                super(SystemPriority.DRAW_PARTICLES);
            }

            @Override
            public void draw(FrameContext frame) {
                ball(w).getComponent(BallComponent.class).x += 1e-9;
            }
        }
        w.addDrawPass(new Nudger());
        w.setDrawGuard(true);
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> viewer.frame(0.1));
        assertTrue(e.getMessage().contains("Nudger"), e.getMessage());
    }

    // ---- interpolation -------------------------------------------------------------------------------------------

    @Test
    void interpolationBlendsTheLastTwoTicksByAlpha() {
        MatchWorld w = kickedOff();
        InterpolationPass pass = w.addDrawPass(new InterpolationPass(w, new BallPose()));
        BallComponent b = ball(w).getComponent(BallComponent.class);
        w.draw(0.5);
        ViewTransform t = pass.of(ball(w));
        assertNotNull(t);
        assertEquals(1, pass.size());
        assertEquals(0f, t.x, "before the first tick prev = curr");
        assertNull(pass.of(w.engine().getEntities().get(0)), "players have no pose source yet");

        w.runTicks(5);
        w.draw(0); // captures tick 5 after a jump: snap, prev = curr
        assertEquals(t.currX(), t.prevX());
        double x5 = b.x;
        double y5 = b.y;
        double z5 = b.z;
        w.tick();
        w.draw(0.0);
        assertEquals((float) x5, t.x);
        w.draw(1.0);
        assertEquals((float) b.x, t.x);
        assertEquals((float) b.z, t.z);
        w.draw(0.25);
        assertEquals((float) (x5 + (b.x - x5) * 0.25), t.x, 1e-6);
        assertEquals((float) (y5 + (b.y - y5) * 0.25), t.y, 1e-6);
        assertEquals((float) (z5 + (b.z - z5) * 0.25), t.z, 1e-6);
        assertTrue(b.x > x5, "the ball moved");
        w.draw(7.0);
        assertEquals((float) b.x, t.x, "alpha is clamped to 1");
    }

    @Test
    void theViewerCapturesEveryTickSoMultiTickFramesStillBlendTheLastTick() {
        MatchWorld w = kickedOff();
        MatchViewer viewer = new MatchViewer(w, frame(), clock(w));
        BallComponent b = ball(w).getComponent(BallComponent.class);
        viewer.frame(0);
        double[] xs = new double[8];
        // A frame of 4.5 ticks: 4 ticks run, half a tick left.
        MatchWorld shadow = kickedOff();
        for (int i = 0; i < 4; i++) {
            shadow.tick();
            xs[i] = ball(shadow).getComponent(BallComponent.class).x;
        }
        assertEquals(4, viewer.frame(4.5 * w.tickSeconds()));
        ViewTransform t = viewer.interpolation().of(ball(w));
        assertEquals(xs[3], b.x, 0);
        assertEquals(xs[3], t.currX(), 0);
        assertEquals(xs[2], t.prevX(), 0, "previous is the tick before the last, not the frame before");
        assertEquals(0.5, viewer.view().alpha(), 1e-9);
        assertEquals((float) (xs[2] + (xs[3] - xs[2]) * 0.5), t.x, 1e-6);
    }

    @Test
    void pausingStopsTicksAndTheViewClockButStillDraws() {
        MatchWorld w = kickedOff();
        MatchViewer viewer = new MatchViewer(w, frame(), clock(w));
        FrameRecorder recorder = w.addDrawPass(new FrameRecorder());
        viewer.frame(0.1);
        long ticks = w.tickCount();
        double view = viewer.view().viewSeconds();
        viewer.setPaused(true);
        assertEquals(0, viewer.frame(0.5));
        assertEquals(ticks, w.tickCount());
        assertEquals(view, viewer.view().viewSeconds());
        assertEquals(1.0, viewer.view().alpha());
        assertEquals(2, recorder.seen.size());
        viewer.setPaused(false);
        viewer.setSpeed(2);
        assertTrue(viewer.frame(0.1) >= 5, "double speed: 6 ticks in 0.1 s (plus leftover)");
        assertEquals(2.0, viewer.view().matchSpeed());
    }

    // ---- clock ---------------------------------------------------------------------------------------------------

    @Test
    void viewerClockCapsStepsCarriesTheRemainderAndHonoursTheBudget() {
        double step = 1.0 / 30;
        int[] runs = new int[1];
        Runnable count = () -> runs[0]++;

        ViewerClock capped = new ViewerClock(step, 3, 30, 1.0, () -> 0L);
        assertEquals(3, capped.runFrame(10 * step + 1e-9, count));
        assertEquals(7, capped.lastCarried());
        assertEquals(1.0, capped.alpha(), "behind schedule: show the last tick");
        assertEquals(3, capped.runFrame(0, count));
        assertEquals(3, capped.runFrame(0, count));
        assertEquals(1, capped.runFrame(0, count));
        assertEquals(0, capped.lastCarried());
        assertEquals(0, capped.runFrame(0, count));
        assertEquals(10, runs[0], "carried steps run later, none lost");

        ViewerClock backlog = new ViewerClock(step, 4, 8, 1.0, () -> 0L);
        assertEquals(4, backlog.runFrame(100 * step, count));
        assertEquals(4, backlog.lastCarried(), "backlog beyond 8 steps dropped, 4 carried");
        assertEquals(4, backlog.runFrame(0, count));
        assertEquals(0, backlog.runFrame(0, count));

        long[] now = { 0 };
        ViewerClock budget = new ViewerClock(step, 16, 60, 0.010, () -> now[0]);
        Runnable slowStep = () -> now[0] += 4_000_000L; // 4 ms per tick
        assertEquals(3, budget.runFrame(10 * step + 1e-9, slowStep), "10 ms budget: 3 ticks of 4 ms");
        assertEquals(7, budget.lastCarried());
        Runnable fastStep = () -> now[0] += 1L;
        assertEquals(7, budget.runFrame(0, fastStep));

        assertThrows(IllegalArgumentException.class, () -> new ViewerClock(step, 0, 10, 1, () -> 0L));
        assertThrows(IllegalArgumentException.class, () -> new ViewerClock(step, 10, 5, 1, () -> 0L));
        assertThrows(IllegalArgumentException.class, () -> new ViewerClock(step, 1, 5, 0, () -> 0L));

        FixedStepClock raw = new FixedStepClock(step, 5);
        assertThrows(IllegalArgumentException.class, () -> raw.carry(-1));
    }

    @Test
    void theViewerRefusesAClockOfAnotherStep() {
        MatchWorld w = kickedOff();
        assertThrows(IllegalArgumentException.class,
                () -> new MatchViewer(w, frame(), new ViewerClock(1.0 / 60)));
    }

    // ---- priorities ----------------------------------------------------------------------------------------------

    @Test
    void priorityBandsAreUniqueOrderedAndKeepDrawPassesAfterTheSimulation() throws IllegalAccessException {
        List<Integer> sim = new ArrayList<>();
        List<Integer> draw = new ArrayList<>();
        List<String> drawNames = new ArrayList<>();
        for (Field field : SystemPriority.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == int.class) {
                int value = field.getInt(null);
                if (field.getName().startsWith("DRAW_")) {
                    if (!field.getName().equals("DRAW_INTERPOLATION")) { // alias of DRAW_FRAME
                        assertFalse(draw.contains(value), field.getName() + " shares " + value);
                        draw.add(value);
                        drawNames.add(field.getName());
                    }
                } else {
                    sim.add(value);
                }
            }
        }
        int maxSim = sim.stream().mapToInt(Integer::intValue).max().orElseThrow();
        int minDraw = draw.stream().mapToInt(Integer::intValue).min().orElseThrow();
        assertTrue(maxSim < minDraw, "sim " + maxSim + " vs draw " + minDraw);
        assertEquals(SystemPriority.DRAW_FRAME, SystemPriority.DRAW_INTERPOLATION);
        assertTrue(SystemPriority.DRAW_PITCH < SystemPriority.DRAW_MARKS
                && SystemPriority.DRAW_MARKS < SystemPriority.DRAW_SHADOWS
                && SystemPriority.DRAW_SHADOWS < SystemPriority.DRAW_CROWD_BACK
                && SystemPriority.DRAW_CROWD_BACK < SystemPriority.DRAW_ACTORS
                && SystemPriority.DRAW_ACTORS < SystemPriority.DRAW_OVERHEAD
                && SystemPriority.DRAW_OVERHEAD < SystemPriority.DRAW_CROWD_FRONT
                && SystemPriority.DRAW_CROWD_FRONT < SystemPriority.DRAW_ATMOSPHERE
                && SystemPriority.DRAW_ATMOSPHERE < SystemPriority.DRAW_CAMERA_FX
                && SystemPriority.DRAW_CAMERA_FX < SystemPriority.DRAW_DEBUG
                && SystemPriority.DRAW_DEBUG < SystemPriority.DRAW_POST, drawNames.toString());
    }

    // ---- ArtGuard ------------------------------------------------------------------------------------------------

    /** A buggy simulation system that asks for art. */
    static final class ArtAskingSystem extends EntitySystem implements SimSystem {
        ArtAskingSystem() {
            super(SystemPriority.LAWS);
        }

        @Override
        public void update(float delta) {
            ArtGuard.check("player_sprite");
        }
    }

    /** A pass that asks for art, as a real sprite pass will. */
    static final class ArtAskingPass extends EntitySystem implements DrawPass {
        int asked;

        ArtAskingPass() {
            super(SystemPriority.DRAW_ACTORS);
        }

        @Override
        public void draw(FrameContext frame) {
            ArtGuard.check("player_sprite");
            asked++;
        }
    }

    @Test
    void artGuardCatchesAnArtLookupFromSimulationCodeButNotFromADrawPass() {
        MatchWorld w = kickedOff();
        ArtAskingPass pass = w.addDrawPass(new ArtAskingPass());
        w.draw(0);
        assertEquals(1, pass.asked);
        w.addSimulation(new ArtAskingSystem());
        IllegalStateException e = assertThrows(IllegalStateException.class, w::tick);
        assertTrue(e.getMessage().contains("simulation code"), e.getMessage());
        assertFalse(ArtGuard.inSimulation(), "the tick left the simulation scope even when it threw");
        w.draw(0);
        assertEquals(2, pass.asked);

        ArtGuard.forbid(true);
        assertThrows(IllegalStateException.class, () -> w.draw(0), "forbidden everywhere in headless tests");
        ArtGuard.forbid(false);
    }

    // ---- NoGl ----------------------------------------------------------------------------------------------------

    /** A pass that builds a texture when it is constructed, as sprite passes will. */
    static final class TexturePass extends EntitySystem implements DrawPass {
        final Texture texture;
        int frames;

        TexturePass() {
            super(SystemPriority.DRAW_PITCH);
            Pixmap pixmap = new Pixmap(8, 4, Pixmap.Format.RGBA8888);
            pixmap.setColor(0.2f, 0.6f, 0.2f, 1f);
            pixmap.fill();
            texture = new Texture(pixmap);
            pixmap.dispose();
        }

        @Override
        public void draw(FrameContext frame) {
            frames += texture.getWidth() > 0 ? 1 : 0;
        }
    }

    @Test
    void noGlLetsATextureUsingPassConstructAndDrawHeadless() {
        NoGl.install();
        try {
            MatchWorld w = kickedOff();
            MatchViewer viewer = new MatchViewer(w, frame(), clock(w));
            TexturePass pass = w.addDrawPass(new TexturePass());
            w.setDrawGuard(true);
            viewer.frame(0.1);
            assertEquals(8, pass.texture.getWidth());
            assertEquals(4, pass.texture.getHeight());
            assertEquals(1, pass.frames);
            pass.texture.dispose();
        } finally {
            NoGl.uninstall();
        }
    }
}
