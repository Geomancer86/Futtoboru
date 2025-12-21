package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * SteeringComponent - LibGDX AI steering behavior for entities
 * Based on DarkBlade engine architecture
 * 
 * Provides goal-based movement with acceleration physics
 */
public class SteeringComponent implements Component, Poolable {
    
    // Steering behavior (Seek, Arrive, etc.)
    public SteeringBehavior<Vector2> behavior;
    
    // Target position for steering
    public Vector2 targetPosition = new Vector2();
    
    // Movement parameters
    public float maxLinearSpeed = 5.0f; // meters per second
    public float maxLinearAcceleration = 10.0f; // meters per second squared
    public float maxAngularSpeed = 3.0f; // radians per second
    public float maxAngularAcceleration = 5.0f; // radians per second squared
    
    // Arrival parameters
    public float arrivalTolerance = 0.5f; // Distance to consider "arrived"
    public float decelerationRadius = 2.0f; // Start slowing down at this distance
    
    // Obstacle avoidance
    public float avoidanceRadius = 1.5f; // Radius to check for obstacles
    public float avoidanceLookahead = 2.0f; // How far ahead to look
    
    // Steerable adapter for LibGDX AI (the entity itself)
    public Steerable<Vector2> steerable;
    
    // Target location for steering behaviors (Seek/Arrive target)
    public Steerable<Vector2> targetLocation;
    
    @Override
    public void reset() {
        behavior = null;
        targetPosition.set(0, 0);
        maxLinearSpeed = 5.0f;
        maxLinearAcceleration = 10.0f;
        maxAngularSpeed = 3.0f;
        maxAngularAcceleration = 5.0f;
        arrivalTolerance = 0.5f;
        decelerationRadius = 2.0f;
        avoidanceRadius = 1.5f;
        avoidanceLookahead = 2.0f;
        steerable = null;
        targetLocation = null;
    }
}
