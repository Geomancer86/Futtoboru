package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.match.ecs.ball.BallComponent;

/** The ball's position (z = height of its lowest point) for {@link InterpolationPass}. */
public final class BallPose implements PoseSource {

    private static final Family FAMILY = Family.all(BallComponent.class).get();
    private final ComponentMapper<BallComponent> balls = ComponentMapper.getFor(BallComponent.class);

    @Override
    public Family family() {
        return FAMILY;
    }

    @Override
    public void read(Entity entity, double[] xyz) {
        BallComponent b = balls.get(entity);
        xyz[0] = b.x;
        xyz[1] = b.y;
        xyz[2] = b.z;
    }
}
