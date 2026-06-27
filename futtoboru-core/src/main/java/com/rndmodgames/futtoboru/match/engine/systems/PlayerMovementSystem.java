package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.rndmodgames.futtoboru.match.engine.components.BodyComponent;
import com.rndmodgames.futtoboru.match.engine.components.PlayerComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;

/**
 * PlayerMovementSystem - Basic AI movement for players
 * Players will move toward the ball
 */
public class PlayerMovementSystem extends IteratingSystem {
    
    private ComponentMapper<BodyComponent> bodyMapper;
    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<PlayerComponent> playerMapper;
    
    private Entity ballEntity;
    private Vector2 ballPosition = new Vector2();
    
    public PlayerMovementSystem(Entity ballEntity) {
        super(Family.all(BodyComponent.class, TransformComponent.class, PlayerComponent.class).get());
        this.ballEntity = ballEntity;
        this.bodyMapper = ComponentMapper.getFor(BodyComponent.class);
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
        this.playerMapper = ComponentMapper.getFor(PlayerComponent.class);
    }
    
    @Override
    public void update(float deltaTime) {
        // Update ball position
        if (ballEntity != null) {
            TransformComponent ballTransform = transformMapper.get(ballEntity);
            if (ballTransform != null) {
                ballPosition.set(ballTransform.position);
            }
        }
        
        super.update(deltaTime);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        BodyComponent bodyComp = bodyMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        PlayerComponent player = playerMapper.get(entity);
        
        if (bodyComp.body == null || bodyComp.isDead) {
            return;
        }
        
        Body body = bodyComp.body;
        Vector2 playerPos = transform.position;
        
        // Calculate direction to ball
        Vector2 direction = new Vector2(ballPosition).sub(playerPos);
        float distance = direction.len();
        
        // Only move if ball is not too close (avoid jitter)
        if (distance > 1.0f) {
            direction.nor(); // Normalize direction
            
            // Apply force toward ball
            float force = player.speed * 50f; // Adjust force multiplier as needed
            Vector2 impulse = new Vector2(direction).scl(force * deltaTime);
            
            // Apply impulse to body
            body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
            
            // Limit max speed
            Vector2 velocity = body.getLinearVelocity();
            if (velocity.len() > player.maxSpeed) {
                velocity.nor().scl(player.maxSpeed);
                body.setLinearVelocity(velocity);
            }
        }
    }
}
