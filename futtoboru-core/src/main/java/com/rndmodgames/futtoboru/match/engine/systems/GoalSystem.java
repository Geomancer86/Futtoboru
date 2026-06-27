package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.Arrive;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.badlogic.gdx.math.Vector2;
import com.rndmodgames.futtoboru.match.engine.components.BodyComponent;
import com.rndmodgames.futtoboru.match.engine.components.GoalComponent;
import com.rndmodgames.futtoboru.match.engine.components.SteeringComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;
import com.rndmodgames.futtoboru.match.engine.utils.LocationAdapter;

/**
 * GoalSystem - Updates player goals and steering targets
 * Based on DarkBlade engine architecture
 * 
 * Processes goals and updates steering behaviors accordingly
 */
public class GoalSystem extends IteratingSystem {
    
    private ComponentMapper<GoalComponent> goalMapper;
    private ComponentMapper<SteeringComponent> steeringMapper;
    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<BodyComponent> bodyMapper;
    
    private Entity ballEntity; // Reference to ball for CHASE_BALL goal
    
    public GoalSystem(Entity ballEntity) {
        super(Family.all(GoalComponent.class, SteeringComponent.class, TransformComponent.class).get());
        this.ballEntity = ballEntity;
        this.goalMapper = ComponentMapper.getFor(GoalComponent.class);
        this.steeringMapper = ComponentMapper.getFor(SteeringComponent.class);
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
        this.bodyMapper = ComponentMapper.getFor(BodyComponent.class);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        GoalComponent goal = goalMapper.get(entity);
        SteeringComponent steering = steeringMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        BodyComponent bodyComp = bodyMapper.get(entity);
        
        if (!goal.isActive || bodyComp.body == null) {
            return;
        }
        
        // Update target position based on goal type
        Vector2 targetPos = new Vector2();
        boolean hasTarget = false;
        
        switch (goal.type) {
            case CHASE_BALL:
                if (ballEntity != null) {
                    TransformComponent ballTransform = transformMapper.get(ballEntity);
                    if (ballTransform != null) {
                        targetPos.set(ballTransform.position);
                        hasTarget = true;
                    }
                }
                break;
                
            case MOVE_TO_POSITION:
            case DEFEND_POSITION:
            case ATTACK_POSITION:
            case FORMATION_POSITION:
                targetPos.set(goal.targetPosition);
                hasTarget = true;
                break;
                
            case MARK_PLAYER:
                if (goal.targetEntity != null) {
                    TransformComponent targetTransform = transformMapper.get(goal.targetEntity);
                    if (targetTransform != null) {
                        targetPos.set(targetTransform.position);
                        hasTarget = true;
                    }
                }
                break;
                
            case SUPPORT_TEAMMATE:
                if (goal.targetEntity != null) {
                    TransformComponent teammateTransform = transformMapper.get(goal.targetEntity);
                    if (teammateTransform != null) {
                        // Position slightly behind teammate
                        Vector2 direction = new Vector2(teammateTransform.position).sub(transform.position);
                        if (direction.len() > 0) {
                            direction.nor().scl(-2.0f); // 2 meters behind
                            targetPos.set(teammateTransform.position).add(direction);
                            hasTarget = true;
                        }
                    }
                }
                break;
                
            case IDLE:
                // No movement
                hasTarget = false;
                break;
        }
        
        // Update steering behavior target
        if (hasTarget && steering.behavior != null) {
            steering.targetPosition.set(targetPos);
            
            // Update behavior target location
            // Update the target location's position directly
            if (steering.targetLocation != null) {
                steering.targetLocation.getPosition().set(targetPos);
            }
            
            // Check if goal is completed
            float distance = transform.position.dst(targetPos);
            goal.isCompleted = distance <= goal.completionDistance;
        }
    }
    
    public void setBallEntity(Entity ballEntity) {
        this.ballEntity = ballEntity;
    }
}
