package com.rndmodgames.futtoboru.match.ecs;

import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.match.ecs.action.ActionCommand;
import com.rndmodgames.futtoboru.match.ecs.action.ActionSystem;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.action.SkillComponent;
import com.rndmodgames.futtoboru.match.ecs.ai.AiSetup;
import com.rndmodgames.futtoboru.match.ecs.ai.DecisionSystem;
import com.rndmodgames.futtoboru.match.ecs.ai.PerceptionComponent;
import com.rndmodgames.futtoboru.match.ecs.ai.PerceptionSystem;
import com.rndmodgames.futtoboru.match.ecs.ai.TeamTacticsComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchGeometry;
import com.rndmodgames.futtoboru.match.ecs.pitch.PitchParams;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
import com.rndmodgames.futtoboru.match.ecs.view.ActorPass;
import com.rndmodgames.futtoboru.match.ecs.view.BallPose;
import com.rndmodgames.futtoboru.match.ecs.view.BodyPose;
import com.rndmodgames.futtoboru.match.ecs.view.CameraPass;
import com.rndmodgames.futtoboru.match.ecs.view.CrowdPass;
import com.rndmodgames.futtoboru.match.ecs.view.InterpolationPass;
import com.rndmodgames.futtoboru.match.ecs.view.MatchViewer;
import com.rndmodgames.futtoboru.match.ecs.view.PitchPass;
import com.rndmodgames.futtoboru.match.ecs.view.PoseSource;
import com.rndmodgames.futtoboru.match.ecs.view.ShadowPass;
import com.rndmodgames.futtoboru.match.ecs.view.SkylinePass;
import com.rndmodgames.futtoboru.match.ecs.view.TouchlinePass;
import com.rndmodgames.futtoboru.match.ecs.view.ViewFrame;
import com.rndmodgames.futtoboru.match.ecs.body.AthleteComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.ContactSystem;
import com.rndmodgames.futtoboru.match.ecs.body.InjuryComponent;
import com.rndmodgames.futtoboru.match.ecs.body.KitComponent;
import com.rndmodgames.futtoboru.match.ecs.body.LocomotionSystem;
import com.rndmodgames.futtoboru.match.ecs.body.MoveCommand;
import com.rndmodgames.futtoboru.match.ecs.laws.LawsSystem;
import com.rndmodgames.futtoboru.match.ecs.laws.OfficialComponent;
import com.rndmodgames.futtoboru.match.ecs.laws.OfficialSystem;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;

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
            PossessionComponent.class,
            // E-16 laws and officials
            PlayStateComponent.class,
            OfficialComponent.class,
            // E-15 player AI
            TeamTacticsComponent.class,
            PerceptionComponent.class);

    private MatchSystems() {
    }

    /** Adds every simulation system to {@code world}, on the default 1888 ground (110 x 70 yd). */
    public static void installSimulation(MatchWorld world) {
        installSimulation(world, new PitchGeometry(PitchParams.defaults1888()));
    }

    /**
     * Adds every simulation system to {@code world}. The AI systems act only once {@link AiSetup#install} has given
     * the world its team entities; worlds without them (drills, tests) are driven by hand as before.
     */
    public static void installSimulation(MatchWorld world, PitchGeometry pitch) {
        world.addSimulation(new PerceptionSystem(world));
        world.addSimulation(new DecisionSystem(world, pitch));
        world.addSimulation(new OfficialSystem(world));
        world.addSimulation(new LocomotionSystem(world));
        world.addSimulation(new ActionSystem(world));
        world.addSimulation(new BallSystem(world)).setPitch(pitch);
        world.addSimulation(new ContactSystem(world));
        world.addSimulation(new LawsSystem(world)).setPitch(pitch);
        world.engine().getSystem(ActionSystem.class).setPitch(pitch);
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
     * pose source (the debug viewer's stand-ins). Officials with an {@link OfficialComponent} (E-16) are read last, so
     * they win over a stand-in for the same entity.
     */
    public static void installDrawPasses(MatchWorld world, ViewFrame frame, PoseSource... extraPoses) {
        PoseSource[] poses = new PoseSource[3 + extraPoses.length];
        poses[0] = new BallPose();
        poses[1] = new BodyPose();
        System.arraycopy(extraPoses, 0, poses, 2, extraPoses.length);
        poses[2 + extraPoses.length] = new OfficialPose();
        InterpolationPass interpolation = world.addDrawPass(new InterpolationPass(world, poses));
        world.addDrawPass(new CameraPass(frame, interpolation));
        world.addDrawPass(new SkylinePass(frame));
        world.addDrawPass(new PitchPass(frame));
        world.addDrawPass(new ShadowPass(frame, interpolation));
        world.addDrawPass(new TouchlinePass.Buildings(frame));
        world.addDrawPass(new CrowdPass(frame));
        world.addDrawPass(new TouchlinePass(frame));
        world.addDrawPass(new ActorPass(frame, interpolation));
        world.addDrawPass(new CrowdPass.Near(frame));
    }

    /**
     * An official's ground position (E-16) for the interpolation pass. Lives here because simulation packages may not
     * import the viewer. TODO(viewer): officials near the near touchline are drawn with true perspective and can look
     * larger than the capped near crowd row; a near-actor scale cap may be needed in ActorPass.
     */
    public static final class OfficialPose implements PoseSource {
        private static final Family FAMILY = Family.all(OfficialComponent.class).get();
        private final ComponentMapper<OfficialComponent> officials = ComponentMapper.getFor(OfficialComponent.class);

        @Override
        public Family family() {
            return FAMILY;
        }

        @Override
        public void read(Entity entity, double[] xyz) {
            OfficialComponent o = officials.get(entity);
            xyz[0] = o.x;
            xyz[1] = o.y;
            xyz[2] = 0.0;
        }
    }
}
