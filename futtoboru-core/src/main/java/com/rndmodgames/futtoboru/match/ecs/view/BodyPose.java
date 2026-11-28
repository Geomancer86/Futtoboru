package com.rndmodgames.futtoboru.match.ecs.view;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.rndmodgames.futtoboru.match.ecs.body.BodyComponent;

/** A player's (or official's) ground position (z = 0) for {@link InterpolationPass}. */
public final class BodyPose implements PoseSource {

    private static final Family FAMILY = Family.all(BodyComponent.class).get();
    private final ComponentMapper<BodyComponent> bodies = ComponentMapper.getFor(BodyComponent.class);

    @Override
    public Family family() {
        return FAMILY;
    }

    @Override
    public void read(Entity entity, double[] xyz) {
        BodyComponent b = bodies.get(entity);
        xyz[0] = b.x;
        xyz[1] = b.y;
        xyz[2] = 0.0;
    }
}
