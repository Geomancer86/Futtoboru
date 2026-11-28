package com.rndmodgames.futtoboru.match.ecs.view;

import java.util.Random;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * Debug viewer (E-21b, E-21c): runs the ECS match world live under the {@link MatchViewer} so the art can be seen
 * moving. The 22 players are the real E-13 bodies ({@link BodySpawner}); until the AI (E-15) and actions (E-14) land,
 * this app is their debug input on the render thread: every frame each body gets a {@link MoveCommand} (hold a 2-3-5
 * place that shifts with the ball; the nearest two of each side run at it, one straight, one in support), and a player
 * who reaches the ball kicks it toward the other half. The officials are still {@link StandInPlayers} (E-16). Sprites
 * are the baked sheets ({@link IndexActorSprites}); the crowd cheers when the ball nears a goal.
 *
 * <p>Keys: {@code +}/{@code -} magnification, {@code A} automatic magnification, {@code SPACE} pause, {@code 1}-{@code 4}
 * quality level, {@code ESC} quit. System properties: {@code futtoboru.viewer.shot=<png>} saves a screenshot after
 * {@code futtoboru.viewer.shotAfter} seconds (default 6); {@code futtoboru.viewer.seconds=<n>} quits after n seconds;
 * {@code futtoboru.seed} seeds the match fork and the kicks; {@code futtoboru.viewer.home} / {@code .away} pick the
 * clubs' kits (club ids of {@code art/match/index.json}); {@code futtoboru.viewer.crowd} the attendance;
 * {@code futtoboru.viewer.wetness} the pitch wetness (0..1, mud on the kits); {@code futtoboru.viewer.magnification}
 * fixes the magnification.
 */
public final class ViewerDebugApp extends ApplicationAdapter {

    private MatchWorld world;
    private MatchViewer viewer;
    private SpriteBatch batch;
    private BallComponent ball;
    private BallSystem ballSystem;
    private Random kicks;
    private PitchGeometry pitch;
    private double elapsed;
    private double restSeconds;
    private boolean shotTaken;
    private Entity[] players;
    private double kickCooldown;
    private double mood;
    private ViewFrame frame;

    @Override
    public void create() {
        long seed = Long.getLong("futtoboru.seed", 1888L);
        pitch = new PitchGeometry(PitchParams.defaults1888());
        double wetness = Double.parseDouble(System.getProperty("futtoboru.viewer.wetness",
                String.valueOf(PitchConditions.DAMP)));
        world = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, wetness),
                RandomSource.seeded(seed).fork("match:viewer-debug"));
        MatchSystems.installSimulation(world);
        players = BodySpawner.spawnDefault(world, pitch);
        ball = BallComponent.atRest(0, 0, world.params().ballDryMassKg);
        Entity ballEntity = world.spawn(SpawnRole.BALL);
        ballEntity.add(ball);
        for (int i = 0; i < 3; i++) {
            world.spawn(SpawnRole.OFFICIAL);
        }
        ballSystem = world.engine().getSystem(BallSystem.class);
        kicks = new Random(seed);

        frame = new ViewFrame(GraphicsQuality.load());
        frame.setPitch(pitch, PitchArt.Style.of1888(), seed);
        frame.setSprites(new IndexActorSprites(IndexActorSprites.DEFAULT_SEASON,
                System.getProperty("futtoboru.viewer.home", "preston-north-end"),
                System.getProperty("futtoboru.viewer.away", "accrington")));
        frame.setCrowdSize(Integer.getInteger("futtoboru.viewer.crowd", 8000));
        batch = new SpriteBatch(4000);
        frame.setBatch(batch);
        viewer = new MatchViewer(world, frame, new ViewerClock(world.tickSeconds()),
                new StandInPlayers(world, pitch.halfLength(), pitch.halfWidth()));
        world.setDrawGuard(Boolean.getBoolean("futtoboru.viewer.guard"));
        Integer fixed = Integer.getInteger("futtoboru.viewer.magnification");
        if (fixed != null) {
            viewer.view().viewCamera().setMagnification(fixed);
        }
        resize(Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
        kick(0, 0);
    }

    @Override
    public void resize(int width, int height) {
        if (viewer != null && width > 0 && height > 0) {
            viewer.view().viewCamera().resize(width, height);
            Gdx.gl.glViewport(0, 0, width, height);
        }
    }

    @Override
    public void render() {
        double dt = Math.min(0.1, Gdx.graphics.getDeltaTime());
        elapsed += dt;
        input();
        if (!viewer.paused()) {
            steer(dt);
            kickWhenIdle(dt);
        }
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewer.frame(dt);
        screenshot();
        double quitAfter = Double.parseDouble(System.getProperty("futtoboru.viewer.seconds", "0"));
        if (quitAfter > 0 && elapsed >= quitAfter) {
            Gdx.app.exit();
        }
    }

    private void input() {
        ViewCamera cam = viewer.view().viewCamera();
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
            cam.setMagnification(cam.magnification() + 1);
            cam.resize(w, h);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
            cam.setMagnification(cam.magnification() - 1);
            cam.resize(w, h);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            cam.autoMagnification();
            cam.resize(w, h);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            viewer.setPaused(!viewer.paused());
        }
        for (int q = 0; q < GraphicsQuality.QUALITIES.size(); q++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + q)) {
                viewer.view().quality().select(GraphicsQuality.QUALITIES.get(q));
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    /** 2-3-5 place of a slot for the side attacking {@code attack} (+1 / -1), shifted with the ball. */
    private double[] place(int slot, double attack) {
        Shape.Slot s = Shape.TWO_THREE_FIVE.slot(slot);
        double hl = pitch.halfLength();
        double hw = pitch.halfWidth();
        double x = attack * (-hl * (1 - s.depth() * 0.95)) + ball.x * (slot == 0 ? 0.08 : 0.55);
        double y = attack * s.lateral() * hw * 0.85 + ball.y * (slot == 0 ? 0.15 : 0.25);
        return new double[] { clamp(x, -hl + 1, hl - 1), clamp(y, -hw + 1, hw - 1) };
    }

    /**
     * Debug "AI" between ticks: every body holds its place, the two nearest outfield players of each side go for the
     * ball (the nearest straight at it, the other in support), and whoever reaches it kicks it on.
     */
    private void steer(double dt) {
        kickCooldown -= dt;
        for (int side = 0; side < 2; side++) {
            double attack = side == 0 ? 1 : -1; // home defends the west goal
            int first = -1;
            int second = -1;
            double d1 = Double.MAX_VALUE;
            double d2 = Double.MAX_VALUE;
            for (int k = 1; k < 11; k++) {
                BodyComponent b = players[side * 11 + k].getComponent(BodyComponent.class);
                double d = Math.hypot(ball.x - b.x, ball.y - b.y);
                if (d < d1) {
                    second = first;
                    d2 = d1;
                    first = k;
                    d1 = d;
                } else if (d < d2) {
                    second = k;
                    d2 = d;
                }
            }
            for (int k = 0; k < 11; k++) {
                Entity e = players[side * 11 + k];
                BodyComponent b = e.getComponent(BodyComponent.class);
                MoveCommand cmd = e.getComponent(MoveCommand.class);
                if (k == first) {
                    cmd.toward(b.x, b.y, ball.x - attack * 0.4, ball.y, d1 > 12 ? 6.8 : 5.0);
                    if (d1 < 1.1 && ball.z < 0.6 && kickCooldown <= 0) {
                        double tx = attack * (8 + kicks.nextDouble() * (pitch.halfLength() - 12));
                        double ty = (kicks.nextDouble() * 2 - 1) * (pitch.halfWidth() - 6);
                        kick(tx, ty);
                        kickCooldown = 0.8;
                    }
                    continue;
                }
                double[] target = k == second
                        ? new double[] { ball.x - attack * 6, ball.y + (b.y > ball.y ? 5 : -5) }
                        : place(k, attack);
                double d = Math.hypot(target[0] - b.x, target[1] - b.y);
                if (d < 0.8) {
                    cmd.stop();
                } else {
                    cmd.toward(b.x, b.y, target[0], target[1], Math.min(k == second ? 6.0 : 4.2, 0.6 + d * 0.5));
                }
            }
        }
        // the crowd lifts when the ball nears a goal, settles otherwise
        double wanted = Math.abs(ball.x) > pitch.halfLength() - 18 ? 0.9 : 0.0;
        mood += (wanted - mood) * Math.min(1, dt * 1.5);
        frame.setCrowdMood(mood);
    }

    private static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    /** Kicks the ball toward a new spot once it has nearly stopped (or rolled off the pitch). */
    private void kickWhenIdle(double dt) {
        boolean slow = ball.speed() < 1.0 && ball.z < 0.05;
        restSeconds = slow ? restSeconds + dt : 0;
        if (restSeconds > 2.5) {
            double tx = (kicks.nextDouble() * 2 - 1) * (pitch.halfLength() - 8);
            double ty = (kicks.nextDouble() * 2 - 1) * (pitch.halfWidth() - 6);
            kick(tx, ty);
            restSeconds = 0;
        }
    }

    private void kick(double tx, double ty) {
        double heading = Math.atan2(ty - ball.y, tx - ball.x);
        double distance = Math.hypot(tx - ball.x, ty - ball.y);
        boolean lofted = kicks.nextDouble() < 0.5;
        double speed = Math.min(26, 6 + distance * (lofted ? 0.35 : 0.45));
        double elevation = Math.toRadians(lofted ? 25 + kicks.nextDouble() * 20 : 3);
        ballSystem.physics().kick(ball, ball.massKg * speed, elevation, heading, (kicks.nextDouble() - 0.5) * 20);
    }

    private void screenshot() {
        String path = System.getProperty("futtoboru.viewer.shot");
        double after = Double.parseDouble(System.getProperty("futtoboru.viewer.shotAfter", "6"));
        if (path == null || shotTaken || elapsed < after) {
            return;
        }
        shotTaken = true;
        int w = Gdx.graphics.getBackBufferWidth();
        int h = Gdx.graphics.getBackBufferHeight();
        Pixmap shot = Pixmap.createFromFrameBuffer(0, 0, w, h);
        try {
            java.nio.ByteBuffer pixels = shot.getPixels();
            for (int i = 3; i < pixels.limit(); i += 4) {
                pixels.put(i, (byte) 0xFF); // opaque, whatever the passes left in the alpha channel
            }
            PixmapIO.writePNG(Gdx.files.absolute(path), shot, 6, true);
            System.out.println("viewer screenshot " + path);
        } finally {
            shot.dispose();
        }
    }

    @Override
    public void dispose() {
        if (viewer != null) {
            viewer.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
