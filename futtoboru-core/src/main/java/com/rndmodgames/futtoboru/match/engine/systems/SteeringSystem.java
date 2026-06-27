package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.rndmodgames.futtoboru.match.engine.components.BodyComponent;
import com.rndmodgames.futtoboru.match.engine.components.SteeringComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;

/**
 * SteeringSystem - Processes LibGDX AI steering behaviors
 * Based on DarkBlade engine architecture
 * 
 * Uses acceleration-based physics (not direct velocity manipulation)
 */
public class SteeringSystem extends IteratingSystem {
    
    private ComponentMapper<SteeringComponent> steeringMapper;
    private ComponentMapper<BodyComponent> bodyMapper;
    private ComponentMapper<TransformComponent> transformMapper;
    
    // Steering acceleration (reused to avoid allocations)
    private SteeringAcceleration<Vector2> steeringAcceleration;
    
    public SteeringSystem() {
        super(Family.all(SteeringComponent.class, BodyComponent.class, TransformComponent.class).get());
        this.steeringMapper = ComponentMapper.getFor(SteeringComponent.class);
        this.bodyMapper = ComponentMapper.getFor(BodyComponent.class);
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
        this.steeringAcceleration = new SteeringAcceleration<Vector2>(new Vector2(), 0);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update LibGDX AI timepiece
        GdxAI.getTimepiece().update(deltaTime);
        super.update(deltaTime);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SteeringComponent steering = steeringMapper.get(entity);
        BodyComponent bodyComp = bodyMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        
        if (steering.behavior == null || bodyComp.body == null || bodyComp.isDead) {
            return;
        }
        
        Body body = bodyComp.body;
        
        // Update steerable adapter from physics body
        if (steering.steerable != null) {
            steering.steerable.getPosition().set(transform.position);
            steering.steerable.setOrientation(body.getAngle());
        }
        
        // Calculate steering behavior
        steering.behavior.calculateSteering(steeringAcceleration);
        
        // Apply acceleration-based movement (not direct velocity!)
        // Force = mass * acceleration
        Vector2 linearAcceleration = steeringAcceleration.linear;
        if (linearAcceleration.len2() > 0) {
            // Clamp acceleration to max
            if (linearAcceleration.len() > steering.maxLinearAcceleration) {
                linearAcceleration.nor().scl(steering.maxLinearAcceleration);
            }
            
            // Apply force: F = m * a
            float mass = body.getMass();
            Vector2 force = new Vector2(linearAcceleration).scl(mass);
            body.applyForce(force, body.getWorldCenter(), true);
        }
        
        // Limit speed through physics (check after force application)
        Vector2 velocity = body.getLinearVelocity();
        float currentSpeed = velocity.len();
        if (currentSpeed > steering.maxLinearSpeed) {
            velocity.nor().scl(steering.maxLinearSpeed);
            body.setLinearVelocity(velocity);
        }
        
        // Apply angular steering (rotation)
        float angularAcceleration = steeringAcceleration.angular;
        if (Math.abs(angularAcceleration) > 0.001f) {
            // Clamp angular acceleration
            if (Math.abs(angularAcceleration) > steering.maxAngularAcceleration) {
                angularAcceleration = Math.signum(angularAcceleration) * steering.maxAngularAcceleration;
            }
            
            // Apply angular impulse
            float angularVelocity = body.getAngularVelocity();
            float maxAngularChange = steering.maxAngularAcceleration * deltaTime;
            float desiredAngularVelocity = Math.max(-steering.maxAngularSpeed, 
                Math.min(steering.maxAngularSpeed, angularVelocity + angularAcceleration * deltaTime));
            
            if (Math.abs(desiredAngularVelocity - angularVelocity) > maxAngularChange) {
                desiredAngularVelocity = angularVelocity + Math.signum(desiredAngularVelocity - angularVelocity) * maxAngularChange;
            }
            
            body.setAngularVelocity(desiredAngularVelocity);
        }
    }
}
