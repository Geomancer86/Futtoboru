package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.rndmodgames.futtoboru.match.engine.components.BodyComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;

/**
 * PhysicsSystem - Handles Box2D physics simulation with fixed timestep
 * Based on DarkBlade engine architecture
 */
public class PhysicsSystem extends IteratingSystem {
    
    private static final float FPS = 60f;
    private static final float MAX_STEP_TIME = 1f / FPS; // 0.0167 seconds
    private static float accumulator = 0f;
    
    private ComponentMapper<BodyComponent> bodyMapper;
    private ComponentMapper<TransformComponent> transformMapper;
    
    private World world;
    
    public PhysicsSystem(World world) {
        super(Family.all(BodyComponent.class, TransformComponent.class).get());
        this.world = world;
        this.bodyMapper = ComponentMapper.getFor(BodyComponent.class);
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
    }
    
    @Override
    public void update(float deltaTime) {
        // Fixed timestep physics simulation
        float frameTime = Math.min(deltaTime, 0.25f); // Cap to avoid spiral of death
        accumulator += frameTime;
        
        // Step physics at fixed intervals
        while (accumulator >= MAX_STEP_TIME) {
            world.step(MAX_STEP_TIME, 8, 3); // velocity iterations: 8, position iterations: 3
            accumulator -= MAX_STEP_TIME;
        }
        
        // Update entity transforms from physics bodies
        super.update(deltaTime);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BodyComponent bodyComp = bodyMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        
        if (bodyComp.body != null && !bodyComp.isDead) {
            Body body = bodyComp.body;
            // Update transform from physics body
            transform.position.set(body.getPosition().x, body.getPosition().y);
            transform.rotation = (float) Math.toDegrees(body.getAngle());
        }
    }
}
