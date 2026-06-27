package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * BallComponent - Ball-specific data
 */
public class BallComponent implements Component, Poolable {
    public float radius = 0.11f; // Standard football radius in meters
    public float friction = 0.3f;
    public float restitution = 0.5f; // Bounciness
    public float linearDamping = 0.1f; // Air resistance
    
    @Override
    public void reset() {
        radius = 0.11f;
        friction = 0.3f;
        restitution = 0.5f;
        linearDamping = 0.1f;
    }
}
