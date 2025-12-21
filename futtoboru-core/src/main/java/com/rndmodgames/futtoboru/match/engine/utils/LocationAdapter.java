package com.rndmodgames.futtoboru.match.engine.utils;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.math.Vector2;

/**
 * LocationAdapter - Adapter for LibGDX AI Steerable interface
 * Converts between Box2D physics bodies and LibGDX AI steerable locations
 */
public class LocationAdapter implements Steerable<Vector2> {
    
    private Vector2 position = new Vector2();
    private float orientation = 0f;
    private float maxLinearSpeed = 5.0f;
    private float maxLinearAcceleration = 10.0f;
    private float maxAngularSpeed = 3.0f;
    private float maxAngularAcceleration = 5.0f;
    private float zeroLinearSpeedThreshold = 0.01f;
    private boolean tagged = false;
    private float boundingRadius = 0.3f;
    
    @Override
    public Vector2 getPosition() {
        return position;
    }
    
    @Override
    public float getOrientation() {
        return orientation;
    }
    
    @Override
    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }
    
    public void setPosition(Vector2 position) {
        this.position.set(position);
    }
    
    public void setPosition(float x, float y) {
        this.position.set(x, y);
    }
    
    @Override
    public float vectorToAngle(Vector2 vector) {
        return (float) Math.atan2(-vector.x, vector.y);
    }
    
    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        outVector.x = -(float) Math.sin(angle);
        outVector.y = (float) Math.cos(angle);
        return outVector;
    }
    
    @Override
    public Location<Vector2> newLocation() {
        return new LocationAdapter();
    }
    
    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }
    
    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }
    
    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }
    
    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }
    
    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }
    
    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }
    
    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }
    
    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }
    
    @Override
    public float getZeroLinearSpeedThreshold() {
        return zeroLinearSpeedThreshold;
    }
    
    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        this.zeroLinearSpeedThreshold = value;
    }
    
    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }
    
    // Note: setBoundingRadius is not in Steerable interface, but we keep it for convenience
    // (No @Override annotation since it's not part of the interface)
    public void setBoundingRadius(float boundingRadius) {
        this.boundingRadius = boundingRadius;
    }
    
    @Override
    public boolean isTagged() {
        return tagged;
    }
    
    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }
    
    @Override
    public Vector2 getLinearVelocity() {
        // Return zero velocity - actual velocity comes from physics body
        // This is used by steering behaviors for calculations
        return new Vector2(0, 0);
    }
    
    @Override
    public float getAngularVelocity() {
        // Return zero angular velocity - actual velocity comes from physics body
        // This is used by steering behaviors for calculations
        return 0f;
    }
}
