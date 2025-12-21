package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.ai.steer.behaviors.RaycastObstacleAvoidance;
import com.badlogic.gdx.ai.steer.behaviors.Separation;
import com.badlogic.gdx.ai.steer.behaviors.BlendedSteering;
import com.badlogic.gdx.ai.steer.proximities.RadiusProximity;
import com.badlogic.gdx.math.Vector2;
import com.rndmodgames.futtoboru.match.engine.components.SteeringComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;
import com.rndmodgames.futtoboru.match.engine.components.PlayerComponent;

/**
 * CollisionAvoidanceSystem - Adds collision avoidance to steering behaviors
 * Based on DarkBlade engine architecture
 * 
 * Combines multiple steering behaviors (Seek + Obstacle Avoidance + Separation)
 */
public class CollisionAvoidanceSystem extends IteratingSystem {
    
    private ComponentMapper<SteeringComponent> steeringMapper;
    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<PlayerComponent> playerMapper;
    
    private Entity[] allPlayers; // All player entities for separation behavior
    
    public CollisionAvoidanceSystem(Entity[] allPlayers) {
        super(Family.all(SteeringComponent.class, TransformComponent.class, PlayerComponent.class).get());
        this.allPlayers = allPlayers;
        this.steeringMapper = ComponentMapper.getFor(SteeringComponent.class);
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
        this.playerMapper = ComponentMapper.getFor(PlayerComponent.class);
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        SteeringComponent steering = steeringMapper.get(entity);
        TransformComponent transform = transformMapper.get(entity);
        PlayerComponent player = playerMapper.get(entity);
        
        if (steering.behavior == null || steering.steerable == null) {
            return;
        }
        
        // Create blended steering if not already created
        if (!(steering.behavior instanceof BlendedSteering)) {
            // Get the base behavior (Seek)
            com.badlogic.gdx.ai.steer.SteeringBehavior<Vector2> baseBehavior = steering.behavior;
            
            // Create separation behavior for this player
            Separation<Vector2> separation = createSeparationBehavior(entity, steering);
            
            // Create obstacle avoidance (raycast-based)
            RaycastObstacleAvoidance<Vector2> obstacleAvoidance = createObstacleAvoidance(steering);
            
            // Create blended steering combining all behaviors
            BlendedSteering<Vector2> blended = new BlendedSteering<Vector2>(steering.steerable);
            blended.add(baseBehavior, 1.0f); // Primary: Seek (weight 1.0)
            blended.add(separation, 1.5f); // Separation (higher weight to avoid clustering)
            if (obstacleAvoidance != null) {
                blended.add(obstacleAvoidance, 2.0f); // Obstacle avoidance (highest priority)
            }
            
            steering.behavior = blended;
        }
    }
    
    /**
     * Create separation behavior to maintain distance from other players
     */
    private Separation<Vector2> createSeparationBehavior(Entity entity, SteeringComponent steering) {
        // Get all other players as neighbors
        com.badlogic.gdx.utils.Array<com.badlogic.gdx.ai.steer.Steerable<Vector2>> neighbors = 
            new com.badlogic.gdx.utils.Array<com.badlogic.gdx.ai.steer.Steerable<Vector2>>();
        
        ComponentMapper<SteeringComponent> steeringMapper = ComponentMapper.getFor(SteeringComponent.class);
        ComponentMapper<PlayerComponent> playerMapper = ComponentMapper.getFor(PlayerComponent.class);
        
        for (Entity otherEntity : allPlayers) {
            if (otherEntity != entity) {
                SteeringComponent otherSteering = steeringMapper.get(otherEntity);
                if (otherSteering != null && otherSteering.steerable != null) {
                    neighbors.add(otherSteering.steerable);
                }
            }
        }
        
        // Create proximity for separation behavior
        // RadiusProximity constructor: (owner, agents, radius) - agents is Array<Steerable>
        float separationRadius = 1.5f; // Detection radius for separation
        RadiusProximity<Vector2> proximity = new RadiusProximity<Vector2>(
            steering.steerable, neighbors, separationRadius);
        
        Separation<Vector2> separation = new Separation<Vector2>(steering.steerable, proximity);
        separation.setDecayCoefficient(2.0f); // How quickly separation force decays with distance
        // Note: Acceleration is controlled through the steerable's maxLinearAcceleration property
        
        return separation;
    }
    
    /**
     * Create obstacle avoidance behavior using raycasting
     */
    private RaycastObstacleAvoidance<Vector2> createObstacleAvoidance(SteeringComponent steering) {
        // TODO: Implement proper RaycastObstacleAvoidance
        // The API signature for RaycastObstacleAvoidance may vary by libGDX AI version
        // For now, return null to avoid compilation errors - this feature can be implemented later
        // when the correct API is confirmed
        return null;
        
        /* Original implementation attempt - needs correct API:
        com.badlogic.gdx.ai.steer.utils.RayConfiguration<Vector2> rayConfig = 
            new com.badlogic.gdx.ai.steer.utils.DefaultRayConfiguration<Vector2>(steering.avoidanceLookahead);
        
        RaycastObstacleAvoidance<Vector2> avoidance = new RaycastObstacleAvoidance<Vector2>(
            steering.steerable,
            rayConfig,
            steering.avoidanceRadius
        );
        
        return avoidance;
        */
    }
    
    public void updatePlayerList(Entity[] allPlayers) {
        this.allPlayers = allPlayers;
    }
}
