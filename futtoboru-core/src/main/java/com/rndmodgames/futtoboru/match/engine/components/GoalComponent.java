package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * GoalComponent - Goal-based behavior for players
 * Based on DarkBlade engine architecture
 * 
 * Players have goals that determine their movement targets
 */
public class GoalComponent implements Component, Poolable {
    
    public enum GoalType {
        CHASE_BALL,           // Chase the ball
        MOVE_TO_POSITION,     // Move to specific position
        DEFEND_POSITION,       // Defend a position
        ATTACK_POSITION,       // Attack a position
        MARK_PLAYER,          // Mark/track an opponent
        SUPPORT_TEAMMATE,     // Support a teammate
        FORMATION_POSITION,   // Return to formation position
        IDLE                  // No active goal
    }
    
    public GoalType type = GoalType.IDLE;
    public Vector2 targetPosition = new Vector2();
    public Entity targetEntity; // For marking players, chasing ball, etc.
    public float priority = 1.0f; // Goal priority (higher = more important)
    public boolean isActive = true;
    
    // Goal parameters
    public float completionDistance = 1.0f; // Distance to consider goal complete
    public boolean isCompleted = false;
    
    @Override
    public void reset() {
        type = GoalType.IDLE;
        targetPosition.set(0, 0);
        targetEntity = null;
        priority = 1.0f;
        isActive = true;
        completionDistance = 1.0f;
        isCompleted = false;
    }
}
