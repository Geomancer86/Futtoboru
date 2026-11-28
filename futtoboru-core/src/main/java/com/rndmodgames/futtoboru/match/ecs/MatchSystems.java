package com.rndmodgames.futtoboru.match.ecs;

import java.util.List;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.match.ecs.view.BallPose;
import com.rndmodgames.futtoboru.match.ecs.view.InterpolationPass;
import com.rndmodgames.futtoboru.match.ecs.view.MatchViewer;
import com.rndmodgames.futtoboru.match.ecs.view.ViewFrame;

/**
 * The one registration point of the match simulation (§1.1). Each lane adds its system here (with a priority from
 * {@link SystemPriority}) and its hashed components to {@link #HASHED_COMPONENTS}; screens never register systems.
 * Draw passes are registered here too ({@link #installDrawPasses(MatchWorld, ViewFrame)}, one line per viewer module,
 * E-21); {@link MatchViewer} calls it.
 */
public final class MatchSystems {

    /**
     * Components covered by the state hash, in hash order. Appending changes every golden hash: do it in the PR that
     * adds the component, and update the goldens there.
     */
    public static final List<Class<? extends Component>> HASHED_COMPONENTS = List.of(
            IdentityComponent.class,
            BallComponent.class);

    private MatchSystems() {
    }

    /** Adds every simulation system to {@code world}. */
    public static void installSimulation(MatchWorld world) {
        world.addSimulation(new BallSystem(world));
    }

    /**
     * Adds every draw pass to {@code world} (viewer only; headless runs never call this). Each module checks
     * {@code frame.quality().on(module)} every frame, so a switched-off module stays installed.
     */
    public static void installDrawPasses(MatchWorld world, ViewFrame frame) {
        world.addDrawPass(new InterpolationPass(world, new BallPose()));
    }
}
