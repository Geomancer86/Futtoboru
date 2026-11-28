package com.rndmodgames.futtoboru.match.ecs.view;

import com.rndmodgames.futtoboru.match.ecs.MatchSystems;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.ui.gfx.GraphicsQuality;

/**
 * The viewer loop of one match (E-21a, docs/design/MATCH_VIEWER_PLAN.md §2): installs the draw passes
 * ({@link MatchSystems#installDrawPasses(MatchWorld, ViewFrame)}), and per frame runs the due simulation ticks under
 * the {@link ViewerClock}'s caps, captures positions for interpolation after every tick, fills the {@link ViewFrame}
 * and draws. Screens own one viewer per shown match; headless runs never create one.
 */
public final class MatchViewer {

    private final MatchWorld world;
    private final ViewFrame frame;
    private final ViewerClock clock;
    private final InterpolationPass interpolation;
    private final Runnable step;
    private boolean paused;

    /** A viewer with the default clock and the shipped quality knobs. */
    public MatchViewer(MatchWorld world) {
        this(world, new ViewFrame(GraphicsQuality.load()), new ViewerClock(world.tickSeconds()));
    }

    public MatchViewer(MatchWorld world, ViewFrame frame, ViewerClock clock) {
        this(world, frame, clock, new PoseSource[0]);
    }

    /** With extra pose sources (bodies without a simulation pose source yet; the debug viewer's stand-ins). */
    public MatchViewer(MatchWorld world, ViewFrame frame, ViewerClock clock, PoseSource... extraPoses) {
        if (world == null || frame == null || clock == null) {
            throw new IllegalArgumentException("world, frame and clock are required");
        }
        if (clock.stepSeconds() != world.tickSeconds()) {
            throw new IllegalArgumentException("clock step " + clock.stepSeconds() + " != tick " + world.tickSeconds());
        }
        this.world = world;
        this.frame = frame;
        frame.setTickSeconds(world.tickSeconds());
        this.clock = clock;
        MatchSystems.installDrawPasses(world, frame, extraPoses);
        this.interpolation = world.engine().getSystem(InterpolationPass.class);
        if (interpolation == null) {
            throw new IllegalStateException("MatchSystems.installDrawPasses must install the InterpolationPass");
        }
        this.step = () -> {
            world.tick();
            interpolation.capture();
        };
    }

    /**
     * One frame: due ticks (none while paused), then every draw pass.
     *
     * @param frameSeconds real time since the last frame
     * @return ticks run
     */
    public int frame(double frameSeconds) {
        int steps = paused ? 0 : clock.runFrame(frameSeconds, step);
        frame.setPaused(paused);
        frame.setMatchSpeed(clock.speed());
        frame.setWetness(world.pitch().wetness());
        frame.begin(frameSeconds, paused ? 1.0 : clock.alpha(), world.tickCount());
        world.draw(frame);
        return steps;
    }

    /** Paused: no ticks, the view clock stops, passes still draw (at alpha 1, the last tick). */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean paused() {
        return paused;
    }

    public void setSpeed(double speed) {
        clock.setSpeed(speed);
    }

    public MatchWorld world() {
        return world;
    }

    public ViewFrame view() {
        return frame;
    }

    public ViewerClock clock() {
        return clock;
    }

    public InterpolationPass interpolation() {
        return interpolation;
    }

    /** Frees the viewer's GPU art (render thread). */
    public void dispose() {
        frame.disposeArt();
    }
}
