package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.rndmodgames.futtoboru.engine.random.RandomSource;
import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.Surface;
import com.rndmodgames.futtoboru.match.ecs.UniformPitch;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;
import com.rndmodgames.futtoboru.ui.gfx.NoGl;

/**
 * E-21b draw passes, headless: camera follow, skyline smoke, shadows, depth-sorted actors, the state hash untouched
 * with every pass on (stand-in players included), headless runs never ask for art, and the art loads under
 * {@link NoGl} onto one atlas page.
 */
class ViewPassesTest {

    @AfterEach
    void reset() {
        ArtGuard.forbid(false);
    }

    /** 22 players, a lofted ball, three officials. */
    static MatchWorld world() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, PitchConditions.DAMP),
                RandomSource.seeded(1888L).fork("match:view"));
        MatchSystems.installSimulation(w);
        for (int i = 0; i < 22; i++) {
            w.spawn(i < 11 ? SpawnRole.HOME_PLAYER : SpawnRole.AWAY_PLAYER);
        }
        BallComponent b = BallComponent.atRest(0, 0, w.params().ballDryMassKg);
        w.spawn(SpawnRole.BALL).add(b);
        for (int i = 0; i < 3; i++) {
            w.spawn(SpawnRole.OFFICIAL);
        }
        w.engine().getSystem(BallSystem.class).physics().kick(b, 9.0, Math.toRadians(35), Math.toRadians(15), 3);
        return w;
    }

    static MatchViewer viewer(MatchWorld w) {
        ViewFrame frame = new ViewFrame(GraphicsQuality.load());
        MatchViewer v = new MatchViewer(w, frame, new ViewerClock(w.tickSeconds(), 16, 60, 1.0, () -> 0L),
                new StandInPlayers(w, frame.pitch().halfLength(), frame.pitch().halfWidth()));
        v.view().viewCamera().resize(1280, 720);
        return v;
    }

    static BallComponent ball(MatchWorld w) {
        return w.engine().getEntities().get(22).getComponent(BallComponent.class);
    }

    @Test
    void everyPassWithStandInsLeavesTheSimulationAlone() {
        MatchWorld headless = world();
        headless.runTicks(300);
        MatchWorld viewed = world();
        MatchViewer v = viewer(viewed);
        viewed.setDrawGuard(true);
        ArtGuard.forbid(true); // headless (no batch): no pass may ask for art
        while (viewed.tickCount() < 300) {
            v.frame(1.0 / 60);
        }
        assertEquals(300, viewed.tickCount());
        assertEquals(headless.stateHash(), viewed.stateHash());
        assertEquals(26, v.interpolation().size(), "ball, 22 stand-ins and 3 officials have view transforms");
    }

    @Test
    void cameraFollowsTheBall() {
        MatchWorld w = world();
        MatchViewer v = viewer(w);
        v.frame(1.0 / 60);
        ViewCamera cam = v.view().viewCamera();
        assertEquals(0, cam.x(), 0.5, "snapped to the ball at kick-off");
        for (int i = 0; i < 180; i++) {
            v.frame(1.0 / 60);
        }
        BallComponent b = ball(w);
        assertTrue(b.x > 5, "the ball travelled: " + b.x);
        assertTrue(cam.x() > 2 && cam.x() <= b.x + 0.5, "camera moved toward the ball: " + cam.x() + " vs " + b.x);
    }

    @Test
    void actorsAreSortedFarToNearWithGoalsAndFlags() {
        MatchWorld w = world();
        MatchViewer v = viewer(w);
        for (int i = 0; i < 20; i++) {
            v.frame(1.0 / 60);
        }
        ActorPass actors = w.engine().getSystem(ActorPass.class);
        assertEquals(26 + 8, actors.size(), "26 actors, 4 posts, 4 corner flags");
        double lastY = Double.POSITIVE_INFINITY;
        int balls = 0;
        for (int i = 0; i < actors.size(); i++) {
            ViewTransform t = actors.transformAt(i);
            if (t != null) {
                assertTrue(t.y <= lastY + 1e-6, "far (larger y) first");
                lastY = t.y;
                balls += actors.kindAt(i) == ActorPass.BALL ? 1 : 0;
            }
        }
        assertEquals(1, balls);
    }

    @Test
    void ballShadowShrinksAndFadesWithHeight() {
        MatchWorld w = world();
        MatchViewer v = viewer(w);
        ShadowPass shadows = w.engine().getSystem(ShadowPass.class);
        v.frame(1.0 / 60);
        int ballIndex = ballShadow(v);
        float groundWidth = shadows.quad(ballIndex, 2);
        float groundAlpha = shadows.quad(ballIndex, 4);
        double topZ = 0;
        float topWidth = groundWidth;
        float topAlpha = groundAlpha;
        for (int i = 0; i < 90; i++) {
            v.frame(1.0 / 60);
            double z = ball(w).z;
            if (z > topZ) {
                topZ = z;
                topWidth = shadows.quad(ballIndex, 2);
                topAlpha = shadows.quad(ballIndex, 4);
            }
        }
        assertTrue(topZ > 3, "the ball climbed: " + topZ);
        assertTrue(topWidth < groundWidth, topWidth + " < " + groundWidth);
        assertTrue(topAlpha < groundAlpha);
        assertEquals(26, shadows.count());
        v.view().quality().switches().setModule(ShadowPass.MODULE, false);
        v.frame(1.0 / 60);
        assertEquals(0, shadows.count(), "module off: no shadows");
    }

    /** Index of the ball among the shadow quads (the ball's pose source comes first). */
    private static int ballShadow(MatchViewer v) {
        for (int i = 0; i < v.interpolation().size(); i++) {
            if (v.interpolation().get(i).entity.getComponent(BallComponent.class) != null) {
                return i;
            }
        }
        throw new AssertionError("no ball");
    }

    @Test
    void smokeDriftComesFromTheCosmeticSourceOnly() {
        MatchWorld a = world();
        MatchWorld b = world();
        MatchViewer va = viewer(a);
        MatchViewer vb = viewer(b);
        vb.view().reseedCosmetic(99);
        SkylinePass sa = a.engine().getSystem(SkylinePass.class);
        SkylinePass sb = b.engine().getSystem(SkylinePass.class);
        boolean differed = false;
        for (int i = 0; i < 1200; i++) {
            va.frame(1.0 / 60);
            vb.frame(1.0 / 60);
            differed |= Math.abs(sa.drift() - sb.drift()) > 1e-6;
        }
        assertTrue(differed, "another cosmetic seed, other gusts");
        assertEquals(a.stateHash(), b.stateHash(), "the match does not care");
    }

    @Test
    void artLoadsOnOnePageAndNeverInsideATick() {
        NoGl.install();
        try {
            ViewFrame frame = new ViewFrame(GraphicsQuality.load());
            ViewArt art = frame.art();
            assertNotNull(art.pitch());
            assertSame(art.white().getTexture(), art.pitch().getTexture(), "pitch on the white piece's page");
            assertSame(art.white().getTexture(), art.skyline().getTexture());
            assertSame(art.white().getTexture(), art.ball(3).getTexture());
            assertEquals(1, art.atlas().pages().size());
            assertEquals(art.pitchArt().width, art.pitch().getRegionWidth());
            frame.disposeArt();
            ArtGuard.enterSimulation();
            try {
                assertThrows(IllegalStateException.class, frame::art);
            } finally {
                ArtGuard.exitSimulation();
            }
        } finally {
            NoGl.uninstall();
        }
    }
}
