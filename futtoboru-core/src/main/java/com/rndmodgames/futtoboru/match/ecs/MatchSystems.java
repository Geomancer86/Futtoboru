package com.rndmodgames.futtoboru.match.ecs;

import java.util.List;

import com.badlogic.ashley.core.Component;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallSystem;
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
 * Draw passes get their own installer in {@code match.ecs.view} (E-21).
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
            MoveCommand.class);

    private MatchSystems() {
    }

    /** Adds every simulation system to {@code world}. */
    public static void installSimulation(MatchWorld world) {
        world.addSimulation(new LocomotionSystem(world));
        world.addSimulation(new BallSystem(world));
        world.addSimulation(new ContactSystem(world));
    }
}
