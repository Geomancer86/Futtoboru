package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * BodyComponent - Box2D physics body for entities
 * Based on DarkBlade engine architecture
 */
public class BodyComponent implements Component, Poolable {
    public Body body;
    public boolean isDead = false;
    
    @Override
    public void reset() {
        body = null;
        isDead = false;
    }
}
