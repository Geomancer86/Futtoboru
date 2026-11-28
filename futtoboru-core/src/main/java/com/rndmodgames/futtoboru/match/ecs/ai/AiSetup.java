package com.rndmodgames.futtoboru.match.ecs.ai;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.SpawnRole;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;
import com.rndmodgames.futtoboru.match.ecs.body.Shape;
import com.rndmodgames.futtoboru.match.ecs.laws.PlayStateComponent;
import com.rndmodgames.futtoboru.match.ecs.pitch.MatchEnds;
import com.rndmodgames.futtoboru.match.ecs.pitch.Team;

/**
 * Wires the player AI (E-15) into a world whose players, ball and officials are spawned and whose ball carries the
 * laws' {@link PlayStateComponent} (E-16): a {@link SpawnRole#TEAM} entity per side with its
 * {@link TeamTacticsComponent}, a {@link PerceptionComponent} per player, and the ball's {@link PossessionComponent}.
 * The laws run the clock, restarts and half-time; the AI reads them.
 */
public final class AiSetup {

    private AiSetup() {
    }

    /** One side's choices before the match (no tactics changes during it yet). */
    public record Side(Shape shape, TeamStyle style) {

        public Side {
            if (shape == null || style == null) {
                throw new IllegalArgumentException("shape and style are required");
            }
        }
    }

    /**
     * Call after the players (with bodies), the ball, the officials and {@link PlayStateComponent#attach}, before the
     * first tick.
     *
     * @param ends the ends of the first half
     */
    public static void install(MatchWorld world, Side home, Side away, MatchEnds ends) {
        ImmutableArray<Entity> balls = world.engine().getEntitiesFor(Family.all(BallComponent.class).get());
        if (balls.size() == 0 || balls.get(0).getComponent(PlayStateComponent.class) == null) {
            throw new IllegalStateException("spawn the ball and attach the laws (PlayStateComponent) first");
        }
        ImmutableArray<Entity> players = world.engine().getEntitiesFor(Family.all(BodyComponent.class).get());
        int n = players.size();
        for (int i = 0; i < n; i++) {
            players.get(i).add(new PerceptionComponent(n));
        }
        world.spawn(SpawnRole.TEAM).add(new TeamTacticsComponent(Team.HOME, home.shape(), home.style(), ends));
        world.spawn(SpawnRole.TEAM).add(new TeamTacticsComponent(Team.AWAY, away.shape(), away.style(), ends));
        Entity ball = balls.get(0);
        if (ball.getComponent(PossessionComponent.class) == null) {
            ball.add(new PossessionComponent());
        }
    }
}
