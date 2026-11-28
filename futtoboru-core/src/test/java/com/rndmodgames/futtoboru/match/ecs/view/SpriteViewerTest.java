package com.rndmodgames.futtoboru.match.ecs.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodySpawner;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.ui.gfx.ArtGuard;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;
import com.rndmodgames.futtoboru.ui.gfx.NoGl;
import com.rndmodgames.futtoboru.ui.gfx.atlas.RuntimeAtlas;

/**
 * E-21c: the sprite index loads (pure, and onto atlas pages under {@link NoGl}), kits resolve with a fallback, the
 * real E-13 bodies drive the actor sprites, and every draw pass, crowd and touchline included, leaves the state hash
 * alone, headless and while really drawing sprites into a recording batch.
 */
class SpriteViewerTest {

    @AfterEach
    void reset() {
        ArtGuard.forbid(false);
    }

    // ---- index -----------------------------------------------------------------------------------------------------

    @Test
    void indexParsesWithEveryClubKitAndConsistentLayouts() {
        SpriteIndex index = SpriteIndex.load();
        assertEquals(java.util.List.of("S", "SE", "E", "NE", "N", "NW", "W", "SW"), index.directions());
        assertEquals(java.util.List.of("S", "SE", "E", "NE", "N", "NW", "W", "SW"),
                java.util.List.of(TouchlinePass.DIRS));
        assertNotNull(index.kit("1888-89", "preston-north-end", 1));
        assertNotNull(index.kit("1888-89", "preston-north-end", 2));
        assertNotNull(index.ofKind("overlay", 2));
        for (SpriteIndex.Sheet s : index.sheets()) {
            SpriteIndex.Layout layout = index.layout(s.layout());
            assertNotNull(layout, s.id());
            for (SpriteIndex.Animation a : layout.animations().values()) {
                int dirs = a.dirs() > 0 ? 8 : 1;
                for (int d = 0; d < dirs; d++) {
                    for (int f = 0; f < a.frames(); f++) {
                        int[] r = layout.regions().get(index.regionName(a, d, f));
                        assertNotNull(r, s.id() + " " + index.regionName(a, d, f));
                        assertTrue(r[4] >= 0 && r[4] < r[2] && r[5] >= 0 && r[5] < r[3], "pivot inside the region");
                    }
                }
            }
        }
        // the animations the viewer asks for exist in both doll layouts and the official/people layouts
        for (String layout : new String[] { "doll_1x", "doll_2x" }) {
            for (String anim : new String[] { "player/idle", "player/run", "player/charge", "player/fall",
                    "player/injured", "keeper/idle", "keeper/run", "keeper/fall" }) {
                assertNotNull(index.layout(layout).animations().get(anim), layout + " " + anim);
            }
        }
        for (String who : IndexActorSprites.OFFICIALS) {
            assertNotNull(index.layout("officials_1x").animations().get(who + "/walk"), who);
        }
        for (int i = 0; i < TouchlinePass.PEOPLE.length; i++) {
            String moving = i == TouchlinePass.BOY ? "/run" : "/walk";
            assertNotNull(index.layout("people_2x").animations().get(TouchlinePass.PEOPLE[i] + moving));
        }
        for (String prop : TouchlinePass.PROPS) {
            assertNotNull(index.layout("touchline_1x").regions().get("prop/" + prop + "/0"), prop);
        }
        for (int v = 0; v < CrowdPass.LOOKS; v++) {
            for (String mood : CrowdPass.MOODS) {
                assertNotNull(index.layout("crowd_1x").regions().get(String.format("crowd/v%02d/%s/0", v, mood)));
            }
        }
    }

    @Test
    void missingKitsFallBackToAnotherClubNeverTheOtherSides() {
        SpriteIndex index = SpriteIndex.load();
        assertEquals("accrington", index.resolveKit("1888-89", "accrington", null));
        String home = index.resolveKit("1888-89", "no-such-club", null);
        assertNotNull(home);
        String away = index.resolveKit("1888-89", "another-missing-club", home);
        assertNotNull(away);
        assertTrue(!home.equals(away), home + " vs " + away);
        IndexActorSprites sprites = new IndexActorSprites(index, "1888-89", "no-such-club", "another-missing-club");
        assertEquals(home, sprites.homeClub());
        assertEquals(away, sprites.awayClub());
        assertNull(index.resolveKit("1700-01", "accrington", null), "no kits that season");
        assertThrows(IllegalArgumentException.class,
                () -> new IndexActorSprites(index, "1700-01", "accrington", "everton"));
        assertThrows(IllegalArgumentException.class, () -> SpriteIndex.parse("{\"format\":2}"));
    }

    @Test
    void sheetsLoadOntoAtlasPagesUnderNoGlAndNeverInsideATick() {
        NoGl.install();
        RuntimeAtlas atlas = new RuntimeAtlas();
        try {
            IndexActorSprites sprites = new IndexActorSprites();
            ArtGuard.enterSimulation();
            try {
                assertThrows(IllegalStateException.class, () -> sprites.load(atlas));
            } finally {
                ArtGuard.exitSimulation();
            }
            sprites.load(atlas);
            assertTrue(sprites.loaded());
            assertTrue(atlas.pages().size() >= 1 && atlas.pages().size() <= 4, atlas.pages().size() + " pages");
            ActorSprites.Sprite rope = sprites.sprite("touchline", "prop/rope/0", 2);
            assertNotNull(rope, "a 1x-only sheet answers a 2x ask with its 1x piece");
            assertEquals(16, rope.width);
            assertEquals(1, rope.scale);
            assertEquals(0, rope.footY, "pivot on the bottom row");
            ActorSprites.Sprite trainer = sprites.sprite("people", "trainer/idle/S/0", 2);
            assertEquals(2, trainer.scale);
            assertNull(sprites.sprite("people", "nobody/idle/S/0", 1));

            // a running home outfielder near the camera at magnification 1: the 2x run, facing E
            ViewFrame frame = new ViewFrame(GraphicsQuality.load());
            frame.setWetness(PitchConditions.RAIN);
            frame.begin(0.1, 1, 0);
            sprites.begin(frame);
            ActorSprites.Pose p = new ActorSprites.Pose().clear();
            p.key = 5;
            p.role = SpawnRole.HOME_PLAYER;
            p.index = 5;
            p.vx = 6;
            p.hasFacing = true;
            p.facing = 0;
            p.pixelsPerMetre = 10.3;
            ActorSprites.Frame f = sprites.actor(p);
            assertEquals("player/run", f.animation);
            assertEquals(2, f.direction);
            assertEquals(2, f.sprite.scale);
            assertEquals(1, f.texel);
            assertNotNull(f.overlay, "mud on a wet pitch");
            assertEquals(IndexActorSprites.MUD_MAX_ALPHA, f.overlayAlpha, 1e-6);
            int before = f.frame;
            frame.begin(0.1, 1, 0);
            sprites.begin(frame);
            assertTrue(sprites.actor(p).frame != before, "the run cycle advances with time and speed");

            // the keeper (slot 0) standing on the far side: keeper set, 2x in the default view (D28), 1x when wide
            p.clear();
            p.key = 0;
            p.role = SpawnRole.AWAY_PLAYER;
            p.index = 0;
            p.hasFacing = true;
            p.facing = Math.PI;
            p.pixelsPerMetre = 6.6;
            f = sprites.actor(p);
            assertEquals("keeper/idle", f.animation);
            assertEquals(6, f.direction);
            assertEquals(2, f.sprite.scale);
            assertEquals(1, f.texel);
            p.viewPixelsPerMetre = ViewCamera.WIDE_PIXELS_PER_METRE;
            p.pixelsPerMetre = 3.3;
            assertEquals(1, sprites.actor(p).sprite.scale, "wide view: 1x");

            // a knocked-down, hurt player: falls, then lies injured
            p.clear();
            p.key = 9;
            p.role = SpawnRole.HOME_PLAYER;
            p.index = 9;
            p.down = true;
            p.injury = 1;
            p.pixelsPerMetre = 5;
            assertEquals("player/fall", sprites.actor(p).animation);
            for (int i = 0; i < 10; i++) {
                frame.begin(0.1, 1, 0);
                sprites.begin(frame);
            }
            assertEquals("player/injured", sprites.actor(p).animation);

            // officials and the ball
            p.clear();
            p.role = SpawnRole.OFFICIAL;
            p.index = 0;
            p.vy = -2;
            f = sprites.actor(p);
            assertEquals("referee/walk", f.animation);
            assertEquals(0, f.direction, "walking toward the camera");
            assertNull(f.overlay, "officials keep clean");
            p.clear();
            p.role = SpawnRole.BALL;
            p.z = 4;
            p.saturation = 0.5;
            p.pixelsPerMetre = 5;
            p.magnification = 2;
            f = sprites.ball(p);
            assertEquals("ball/high/soaked", f.animation);
            assertEquals(2, f.sprite.scale, "magnification 2: the 2x ball at half size");
        } finally {
            atlas.dispose();
            NoGl.uninstall();
        }
    }

    // ---- passes with real bodies -----------------------------------------------------------------------------------

    /** E-13 bodies (22), the ball, three officials; everyone gets a run toward the ball. */
    static MatchWorld bodiesWorld() {
        MatchWorld w = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, PitchConditions.DRIZZLE),
                RandomSource.seeded(1888L).fork("match:view-bodies"));
        MatchSystems.installSimulation(w);
        PitchGeometry pitch = new PitchGeometry(PitchParams.defaults1888());
        Entity[] players = BodySpawner.spawnDefault(w, pitch);
        BallComponent b = BallComponent.atRest(0, 0, w.params().ballDryMassKg);
        w.spawn(SpawnRole.BALL).add(b);
        for (int i = 0; i < 3; i++) {
            w.spawn(SpawnRole.OFFICIAL);
        }
        for (int i = 0; i < players.length; i++) {
            BodyComponent body = players[i].getComponent(BodyComponent.class);
            players[i].getComponent(MoveCommand.class).toward(body.x, body.y, 3 * (i % 3), 2 * (i % 5) - 4,
                    2 + i % 4);
        }
        w.engine().getSystem(BallSystem.class).physics().kick(b, 9.0, Math.toRadians(35), Math.toRadians(15), 3);
        return w;
    }

    static MatchViewer viewer(MatchWorld w, Batch batch, ActorSprites sprites) {
        ViewFrame frame = new ViewFrame(GraphicsQuality.load());
        frame.setSprites(sprites);
        frame.setBatch(batch);
        frame.setCrowdSize(9000);
        MatchViewer v = new MatchViewer(w, frame, new ViewerClock(w.tickSeconds(), 16, 60, 1.0, () -> 0L),
                new StandInPlayers(w, frame.pitch().halfLength(), frame.pitch().halfWidth()));
        v.view().viewCamera().resize(1280, 720);
        return v;
    }

    /** A batch that records draw calls and does nothing else (SpriteBatch needs a real GL). */
    static final class Recorder {
        int draws;
        int begins;
        int ends;
        final Batch batch = (Batch) Proxy.newProxyInstance(Batch.class.getClassLoader(), new Class<?>[] { Batch.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "draw" -> draws++;
                        case "begin" -> begins++;
                        case "end" -> ends++;
                        default -> {
                        }
                    }
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) {
                        return false;
                    }
                    if (type == float.class) {
                        return 0f;
                    }
                    if (type == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    @Test
    void headlessPassesWithBodiesCrowdAndTouchlineLeaveTheSimulationAlone() {
        MatchWorld headless = bodiesWorld();
        headless.runTicks(240);
        MatchWorld viewed = bodiesWorld();
        MatchViewer v = viewer(viewed, null, ActorSprites.NONE);
        viewed.setDrawGuard(true);
        ArtGuard.forbid(true);
        v.view().setCrowdMood(0.6);
        while (viewed.tickCount() < 240) {
            v.frame(1.0 / 60);
        }
        assertEquals(headless.stateHash(), viewed.stateHash());
        assertEquals(26, v.interpolation().size(), "22 bodies, the ball and 3 stand-in officials, each once");
        CrowdPass far = viewed.engine().getSystem(CrowdPass.class);
        CrowdPass near = viewed.engine().getSystem(CrowdPass.Near.class);
        assertTrue(far.figures() > 500, "a 9000 gate fills the far bank: " + far.figures());
        assertTrue(near.figures() > 0 && near.figures() < far.figures());
        assertTrue(far.drawn() > 0 && far.drawn() <= far.figures(), "culled to the view");
        assertTrue(far.moodCount(CrowdPass.CHEER) > 0 && far.moodCount(CrowdPass.CALM) > 0,
                "a lively crowd: some cheer, some stay calm");
        for (int i = 1; i < far.figures(); i++) {
            assertTrue(far.figureY(i) <= far.figureY(i - 1) + 0.25, "far bank drawn back to front");
        }
        assertTrue(viewed.engine().getSystem(TouchlinePass.class).pieces() > 0);
        v.view().quality().switches().setModule(CrowdPass.MODULE, false);
        v.view().quality().switches().setModule(TouchlinePass.MODULE, false);
        v.frame(1.0 / 60);
        assertEquals(0, far.drawn(), "crowd module off");
        assertEquals(0, viewed.engine().getSystem(TouchlinePass.class).pieces(), "touchline module off");
    }

    @Test
    void crowdDensityFollowsTheGateAndComesFromTheCosmeticSource() {
        MatchWorld w = bodiesWorld();
        MatchViewer v = viewer(w, null, ActorSprites.NONE);
        v.view().setCrowdSize(2000);
        v.frame(1.0 / 60);
        int small = w.engine().getSystem(CrowdPass.class).figures();
        v.view().setCrowdSize(14000);
        v.frame(1.0 / 60);
        int big = w.engine().getSystem(CrowdPass.class).figures();
        assertTrue(big > 4 * small, small + " -> " + big);
        v.view().setCrowdSize(0);
        v.frame(1.0 / 60);
        assertEquals(0, w.engine().getSystem(CrowdPass.class).figures(), "an empty ground");
    }

    @Test
    void drawingRealSpritesIntoABatchLeavesTheStateHashAlone() {
        NoGl.install();
        try {
            MatchWorld headless = bodiesWorld();
            headless.runTicks(120);
            MatchWorld viewed = bodiesWorld();
            Recorder recorder = new Recorder();
            MatchViewer v = viewer(viewed, recorder.batch, new IndexActorSprites());
            viewed.setDrawGuard(true);
            while (viewed.tickCount() < 120) {
                v.frame(1.0 / 60);
            }
            assertEquals(headless.stateHash(), viewed.stateHash(), "sprites, crowd and touchline drawn: same match");
            assertEquals(recorder.begins, recorder.ends);
            assertTrue(recorder.draws > 1000, "pitch rows, actors, crowd and props drawn: " + recorder.draws);
            ViewArt art = v.view().art();
            assertSame(art.white().getTexture(), art.atlas().pages().get(0), "flat pieces on the first page");
            v.dispose();
        } finally {
            NoGl.uninstall();
        }
    }
}
