package com.rndmodgames.futtoboru.match.ecs;

import java.util.List;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.action.SkillComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.match.ecs.view.ActorPass;
import com.rndmodgames.futtoboru.match.ecs.view.BallPose;
import com.rndmodgames.futtoboru.match.ecs.view.BodyPose;
import com.rndmodgames.futtoboru.match.ecs.view.CameraPass;
import com.rndmodgames.futtoboru.match.ecs.view.InterpolationPass;
import com.rndmodgames.futtoboru.match.ecs.view.MatchViewer;
import com.rndmodgames.futtoboru.match.ecs.view.PitchPass;
import com.rndmodgames.futtoboru.match.ecs.view.PoseSource;
import com.rndmodgames.futtoboru.match.ecs.view.ShadowPass;
import com.rndmodgames.futtoboru.match.ecs.view.SkylinePass;
import com.rndmodgames.futtoboru.match.ecs.view.ViewFrame;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.ContactSystem;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.body.LocomotionSystem;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;

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
            BallComponent.class,
            // E-13 player bodies
            BodyComponent.class,
            AthleteComponent.class,
            KitComponent.class,
            InjuryComponent.class,
            MoveCommand.class,
            // E-14 ball actions
            ActionCommand.class,
            SkillComponent.class,
            PossessionComponent.class);

    private MatchSystems() {
    }

    /** Adds every simulation system to {@code world}. */
    public static void installSimulation(MatchWorld world) {
        world.addSimulation(new LocomotionSystem(world));
        world.addSimulation(new ActionSystem(world));
        world.addSimulation(new BallSystem(world));
        world.addSimulation(new ContactSystem(world));
    }

    /**
     * Adds every draw pass to {@code world} (viewer only; headless runs never call this). Each module checks
     * {@code frame.quality().on(module)} every frame, so a switched-off module stays installed.
     */
    public static void installDrawPasses(MatchWorld world, ViewFrame frame) {
        installDrawPasses(world, frame, new PoseSource[0]);
    }

    /**
     * As {@link #installDrawPasses(MatchWorld, ViewFrame)}, with extra pose sources for bodies that have no simulation
     * pose source yet (the debug viewer's stand-in players until E-13 bodies land).
     */
    public static void installDrawPasses(MatchWorld world, ViewFrame frame, PoseSource... extraPoses) {
        PoseSource[] poses = new PoseSource[2 + extraPoses.length];
        poses[0] = new BallPose();
        poses[1] = new BodyPose();
        System.arraycopy(extraPoses, 0, poses, 2, extraPoses.length);
        InterpolationPass interpolation = world.addDrawPass(new InterpolationPass(world, poses));
        world.addDrawPass(new CameraPass(frame, interpolation));
        world.addDrawPass(new SkylinePass(frame));
        world.addDrawPass(new PitchPass(frame));
        world.addDrawPass(new ShadowPass(frame, interpolation));
        world.addDrawPass(new ActorPass(frame, interpolation));
    }
}
