package com.rndmodgames.futtoboru.match.ecs.ball;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.rndmodgames.futtoboru.match.ecs.MatchWorld;
import com.rndmodgames.futtoboru.match.ecs.PhysicsParams;
import com.rndmodgames.futtoboru.match.ecs.PitchConditions;
import com.rndmodgames.futtoboru.match.ecs.SimSystem;
import com.rndmodgames.futtoboru.match.ecs.SystemPriority;
import com.rndmodgames.futtoboru.match.ecs.action.PossessionComponent;

/**
 * Ball system (E-11): per tick, soaks every ball by the pitch's wetness, then integrates it in
 * {@link PhysicsParams#ballSubsteps} substeps (30 Hz x 2 = 60 Hz by default, D22). Kicks come from the action
 * system (E-14) through {@link #physics()}. A ball a keeper holds ({@link PossessionComponent#held}) still soaks but
 * does not fly: the action system keeps it in his hands.
 */
public final class BallSystem extends EntitySystem implements SimSystem {

    private final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);
    private final ComponentMapper<PossessionComponent> possession = ComponentMapper.getFor(PossessionComponent.class);
    private final MatchWorld world;
    private final PhysicsParams params;
    private final BallPhysics physics;
    private final int substeps;
    private final double tickSeconds;
    private final double substepSeconds;
    private ImmutableArray<Entity> entities;

    public BallSystem(MatchWorld world) {
        super(SystemPriority.BALL);
        this.world = world;
        this.params = world.params();
        this.physics = new BallPhysics(params);
        this.substeps = params.ballSubsteps;
        this.tickSeconds = world.tickSeconds();
        this.substepSeconds = tickSeconds / substeps;
    }

    @Override
    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(BallComponent.class).get());
    }

    @Override
    public void update(float ignoredFrameDelta) {
        PitchConditions pitch = world.pitch();
        double wetness = pitch.wetness();
        for (int i = 0; i < entities.size(); i++) {
            Entity e = entities.get(i);
            BallComponent b = balls.get(e);
            physics.soak(b, wetness, tickSeconds);
            PossessionComponent held = possession.get(e);
            if (held != null && held.held) {
                continue;
            }
            for (int k = 0; k < substeps; k++) {
                physics.step(b, params.surface(pitch.surfaceAt(b.x, b.y)), substepSeconds);
            }
        }
    }

    public BallPhysics physics() {
        return physics;
    }
}
