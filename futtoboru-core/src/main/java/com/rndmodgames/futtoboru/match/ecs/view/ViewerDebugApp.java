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
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * Debug viewer (E-21b): runs the ECS match world live under the {@link MatchViewer} so the art can be seen moving.
 * The simulation has no players yet, so the ball is kicked about between ticks (a debug input on the render thread)
 * and {@link StandInPlayers} gives the player and official entities view-side positions.
 *
 * <p>Keys: {@code +}/{@code -} magnification, {@code A} automatic magnification, {@code SPACE} pause, {@code 1}-{@code 4}
 * quality level, {@code ESC} quit. System properties: {@code futtoboru.viewer.shot=<png>} saves a screenshot after
 * {@code futtoboru.viewer.shotAfter} seconds (default 6); {@code futtoboru.viewer.seconds=<n>} quits after n seconds;
 * {@code futtoboru.seed} seeds the match fork and the kicks.
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

    @Override
    public void create() {
        long seed = Long.getLong("futtoboru.seed", 1888L);
        pitch = new PitchGeometry(PitchParams.defaults1888());
        world = new MatchWorld(new PhysicsParams(), new UniformPitch(Surface.DRY, PitchConditions.DAMP),
                RandomSource.seeded(seed).fork("match:viewer-debug"));
        MatchSystems.installSimulation(world);
        for (int i = 0; i < 11; i++) {
            world.spawn(SpawnRole.HOME_PLAYER);
        }
        for (int i = 0; i < 11; i++) {
            world.spawn(SpawnRole.AWAY_PLAYER);
        }
        ball = BallComponent.atRest(0, 0, world.params().ballDryMassKg);
        Entity ballEntity = world.spawn(SpawnRole.BALL);
        ballEntity.add(ball);
        for (int i = 0; i < 3; i++) {
            world.spawn(SpawnRole.OFFICIAL);
        }
        ballSystem = world.engine().getSystem(BallSystem.class);
        kicks = new Random(seed);

        ViewFrame frame = new ViewFrame(GraphicsQuality.load());
        frame.setPitch(pitch, PitchArt.Style.of1888(), seed);
        batch = new SpriteBatch(4000);
        frame.setBatch(batch);
        viewer = new MatchViewer(world, frame, new ViewerClock(world.tickSeconds()),
                new StandInPlayers(world, pitch.halfLength(), pitch.halfWidth()));
        world.setDrawGuard(Boolean.getBoolean("futtoboru.viewer.guard"));
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

    /** Kicks the ball toward a new spot once it has nearly stopped (or rolled off the pitch). */
    private void kickWhenIdle(double dt) {
        boolean slow = ball.speed() < 1.0 && ball.z < 0.05;
        restSeconds = slow ? restSeconds + dt : 0;
        if (restSeconds > 0.6) {
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
