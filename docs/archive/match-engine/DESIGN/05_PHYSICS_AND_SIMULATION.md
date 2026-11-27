# Physics and Simulation System Design

**Design Date:** 2025-01-XX  
**Repository:** futtoboru_match_engine  
**Purpose:** Design for physics-based ball and player movement simulation

---

## Executive Summary

The physics and simulation system provides realistic ball and player movement for the 2D match engine. This system ensures that:
- Ball movement is physically accurate
- Player movement is realistic
- Collisions are handled correctly
- Performance is maintained (60 FPS)

**Key Design Principles:**
- **Realistic**: Physics match real-world behavior
- **Performant**: Efficient calculations
- **Accurate**: Ball and player positions correct
- **Smooth**: No jitter or stuttering

---

## 1. Ball Physics

### 1.1 Ball Properties

**Physical Properties:**
```java
public class Ball {
    // Position
    private Vector2 position = new Vector2();
    
    // Velocity
    private Vector2 velocity = new Vector2();
    
    // Acceleration
    private Vector2 acceleration = new Vector2();
    
    // Physical properties
    private float radius = 0.11f; // 11cm (standard football)
    private float mass = 0.43f; // 430g (standard)
    private float coefficientOfRestitution = 0.7f; // Bounce factor
    private float rollingFriction = 0.02f; // Ground friction
    private float airResistance = 0.01f; // Air drag
    
    // State
    private boolean isOnGround = false;
    private boolean isInPlay = true;
}
```

### 1.2 Ball Movement

**Physics Update:**
```java
public class BallPhysics {
    
    public void updateBall(Ball ball, float deltaTime, Pitch pitch) {
        // Apply gravity (if in air)
        if (!ball.isOnGround()) {
            ball.getAcceleration().y = -9.81f; // m/s²
        } else {
            ball.getAcceleration().y = 0.0f;
        }
        
        // Apply friction (when on ground)
        if (ball.isOnGround()) {
            Vector2 friction = new Vector2(ball.getVelocity())
                .nor()
                .scl(-ball.getRollingFriction() * 9.81f);
            ball.getAcceleration().add(friction);
        }
        
        // Apply air resistance
        Vector2 airResist = new Vector2(ball.getVelocity())
            .nor()
            .scl(-ball.getAirResistance() * ball.getVelocity().len2());
        ball.getAcceleration().add(airResist);
        
        // Update velocity
        ball.getVelocity().add(
            ball.getAcceleration().x * deltaTime,
            ball.getAcceleration().y * deltaTime
        );
        
        // Update position
        ball.getPosition().add(
            ball.getVelocity().x * deltaTime,
            ball.getVelocity().y * deltaTime
        );
        
        // Check ground collision
        checkGroundCollision(ball, pitch);
        
        // Check boundary collisions
        checkBoundaryCollisions(ball, pitch);
        
        // Reset acceleration
        ball.getAcceleration().setZero();
    }
}
```

### 1.3 Ball Kicking

**Kick Physics:**
```java
public class BallKick {
    
    public void kickBall(
        Ball ball,
        Vector2 direction,
        float power,
        Player player
    ) {
        // Calculate kick velocity based on power and player attributes
        float kickStrength = calculateKickStrength(power, player);
        
        // Apply velocity
        Vector2 kickVelocity = new Vector2(direction)
            .nor()
            .scl(kickStrength);
        
        ball.getVelocity().set(kickVelocity);
        
        // Ball leaves ground
        ball.setOnGround(false);
    }
    
    private float calculateKickStrength(float power, Player player) {
        // Base strength from power (0-1)
        float baseStrength = power * 20.0f; // Max 20 m/s
        
        // Player attribute modifier
        float kickingAbility = player.getKickingAttribute() / 100.0f;
        baseStrength *= (0.7f + kickingAbility * 0.3f);
        
        // Strength attribute modifier
        float strength = player.getStrengthAttribute() / 100.0f;
        baseStrength *= (0.8f + strength * 0.2f);
        
        return baseStrength;
    }
}
```

---

## 2. Player Physics

### 2.1 Player Movement

**Movement Physics:**
```java
public class PlayerMovement {
    
    public void updatePlayer(Player player, float deltaTime) {
        // Calculate target position (from AI or tactics)
        Vector2 targetPos = player.getTargetPosition();
        Vector2 currentPos = player.getPosition();
        
        // Calculate direction
        Vector2 direction = new Vector2(targetPos)
            .sub(currentPos)
            .nor();
        
        // Calculate desired speed
        float desiredSpeed = calculateDesiredSpeed(player, targetPos);
        
        // Apply acceleration
        float acceleration = player.getAccelerationAttribute() / 100.0f * 10.0f;
        Vector2 currentVelocity = player.getVelocity();
        
        // Accelerate towards target
        Vector2 desiredVelocity = new Vector2(direction)
            .scl(desiredSpeed);
        
        Vector2 velocityChange = new Vector2(desiredVelocity)
            .sub(currentVelocity)
            .limit(acceleration * deltaTime);
        
        player.getVelocity().add(velocityChange);
        
        // Apply friction
        player.getVelocity().scl(0.95f); // Ground friction
        
        // Update position
        player.getPosition().add(
            player.getVelocity().x * deltaTime,
            player.getVelocity().y * deltaTime
        );
        
        // Update stamina
        updateStamina(player, deltaTime);
    }
    
    private float calculateDesiredSpeed(Player player, Vector2 target) {
        float distance = player.getPosition().dst(target);
        
        // Base speed from attribute
        float maxSpeed = player.getSpeedAttribute() / 100.0f * 6.0f; // m/s
        
        // Stamina effect
        float stamina = player.getStamina() / 100.0f;
        maxSpeed *= (0.7f + stamina * 0.3f);
        
        // Fatigue effect
        float fatigue = player.getFatigue() / 100.0f;
        maxSpeed *= (1.0f - fatigue * 0.3f);
        
        // Distance-based speed (slow down near target)
        if (distance < 2.0f) {
            maxSpeed *= (distance / 2.0f);
        }
        
        return maxSpeed;
    }
}
```

### 2.2 Player Collisions

**Player-Player Collision:**
```java
public class PlayerCollision {
    
    public void handlePlayerCollision(Player player1, Player player2) {
        // Calculate collision normal
        Vector2 normal = new Vector2(player2.getPosition())
            .sub(player1.getPosition())
            .nor();
        
        // Calculate relative velocity
        Vector2 relativeVel = new Vector2(player2.getVelocity())
            .sub(player1.getVelocity());
        
        // Calculate separation velocity
        float separationVel = relativeVel.dot(normal);
        
        // If players moving apart, no collision
        if (separationVel > 0) return;
        
        // Calculate impulse
        float restitution = 0.3f; // Low bounce (players don't bounce)
        float impulse = -(1.0f + restitution) * separationVel;
        
        // Apply impulse
        float mass1 = player1.getMass();
        float mass2 = player2.getMass();
        
        Vector2 impulse1 = new Vector2(normal).scl(impulse / mass1);
        Vector2 impulse2 = new Vector2(normal).scl(-impulse / mass2);
        
        player1.getVelocity().add(impulse1);
        player2.getVelocity().add(impulse2);
        
        // Separate players
        float overlap = player1.getRadius() + player2.getRadius() - 
            player1.getPosition().dst(player2.getPosition());
        if (overlap > 0) {
            Vector2 separation = new Vector2(normal).scl(overlap * 0.5f);
            player1.getPosition().sub(separation);
            player2.getPosition().add(separation);
        }
    }
}
```

---

## 3. Collision Detection

### 3.1 Spatial Partitioning

**Grid-Based Partitioning:**
```java
public class SpatialGrid {
    private int gridWidth;
    private int gridHeight;
    private float cellSize;
    private List<List<List<Entity>>> grid;
    
    public void updateGrid(List<Entity> entities) {
        // Clear grid
        for (List<List<Entity>> row : grid) {
            for (List<Entity> cell : row) {
                cell.clear();
            }
        }
        
        // Add entities to grid
        for (Entity entity : entities) {
            int x = (int)(entity.getPosition().x / cellSize);
            int y = (int)(entity.getPosition().y / cellSize);
            
            if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                grid.get(y).get(x).add(entity);
            }
        }
    }
    
    public List<Entity> getNearbyEntities(Entity entity, float radius) {
        List<Entity> nearby = new ArrayList<>();
        
        int minX = (int)((entity.getPosition().x - radius) / cellSize);
        int maxX = (int)((entity.getPosition().x + radius) / cellSize);
        int minY = (int)((entity.getPosition().y - radius) / cellSize);
        int maxY = (int)((entity.getPosition().y + radius) / cellSize);
        
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (x >= 0 && x < gridWidth && y >= 0 && y < gridHeight) {
                    nearby.addAll(grid.get(y).get(x));
                }
            }
        }
        
        return nearby;
    }
}
```

### 3.2 Collision Detection Algorithms

**Circle-Circle Collision:**
```java
public class CollisionDetector {
    
    public boolean checkCircleCollision(
        Vector2 pos1, float radius1,
        Vector2 pos2, float radius2
    ) {
        float distance = pos1.dst(pos2);
        return distance < (radius1 + radius2);
    }
    
    public CollisionInfo getCollisionInfo(
        Vector2 pos1, float radius1,
        Vector2 pos2, float radius2
    ) {
        float distance = pos1.dst(pos2);
        float overlap = (radius1 + radius2) - distance;
        
        if (overlap > 0) {
            Vector2 normal = new Vector2(pos2).sub(pos1).nor();
            return new CollisionInfo(normal, overlap);
        }
        
        return null;
    }
}
```

---

## 4. Simulation Loop

### 4.1 Fixed Timestep

**Fixed Timestep Physics:**
```java
public class MatchSimulation {
    private static final float FIXED_TIMESTEP = 1.0f / 60.0f; // 60 FPS
    private float accumulator = 0.0f;
    
    public void update(float deltaTime) {
        accumulator += deltaTime;
        
        // Fixed timestep updates
        while (accumulator >= FIXED_TIMESTEP) {
            updatePhysics(FIXED_TIMESTEP);
            accumulator -= FIXED_TIMESTEP;
        }
        
        // Interpolate for smooth rendering
        float alpha = accumulator / FIXED_TIMESTEP;
        render(alpha);
    }
    
    private void updatePhysics(float deltaTime) {
        // Update ball
        ballPhysics.updateBall(ball, deltaTime, pitch);
        
        // Update players
        for (Player player : players) {
            playerMovement.updatePlayer(player, deltaTime);
        }
        
        // Check collisions
        checkCollisions();
    }
}
```

### 4.2 Performance Optimization

**Optimization Strategies:**
- Spatial partitioning for collision detection
- Fixed timestep for consistent physics
- Object pooling for temporary objects
- Efficient data structures
- Culling (don't update off-screen objects)

---

## 5. Integration with Match Engine

### 5.1 Event Synchronization

**Physics Events:**
- Ball kicked → Apply kick physics
- Goal scored → Stop ball, reset
- Out of bounds → Reset ball position
- Foul → Pause physics, show event

### 5.2 State Management

**Match State:**
- Ball position synchronized with match state
- Player positions synchronized
- Physics state matches match events

---

## 6. Success Criteria

### 6.1 Realism

- ✅ Ball movement realistic
- ✅ Player movement realistic
- ✅ Collisions handled correctly

### 6.2 Performance

- ✅ 60 FPS maintained
- ✅ Smooth gameplay
- ✅ No lag or stuttering

### 6.3 Accuracy

- ✅ Physics calculations accurate
- ✅ Positions correct
- ✅ Collisions detected correctly

---

**Status:** ✅ Design Complete  
**All Design Documents Complete**



