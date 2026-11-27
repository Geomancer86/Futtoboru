# 2D Top-Down Match Engine Design v1.0

**Phase:** 2 (2D Top-Down Engine)  
**Status:** DESIGN  
**Priority:** CRITICAL - Core Feature of the Game

---

## Overview

The 2D Top-Down Engine provides full visual representation of football matches from a top-down perspective. This is the **core feature** of the match engine and must provide smooth, realistic visualization of match play with proper physics, player movement, and ball dynamics.

## Goals

- **Realistic Physics:** Ball physics, player movement, collisions
- **Smooth Visualization:** 60 FPS performance with 22 players + ball
- **Formation Visualization:** Clear representation of player positions and formations
- **Real-Time Simulation:** Match plays out in real-time (with speed controls)
- **Visual Feedback:** Clear indication of events (shots, goals, fouls, etc.)
- **Historical Accuracy:** Support for 1888 pitch sizes and rules
- **Scalable Performance:** Works with different pitch sizes and detail levels

## Architecture

### Core Components

1. **PhysicsEngine** - Physics simulation (ball, players, collisions)
2. **MatchRenderer2D** - 2D graphics rendering
3. **PlayerSprite** - Player visualization and animation
4. **BallPhysics** - Ball physics and movement
5. **FormationRenderer** - Formation visualization
6. **CameraController** - Camera system (zoom, pan, follow)

### Technology Stack

- **Graphics Framework:** LibGDX (already used in main game)
- **Physics Engine:** JBox2D (Java port of Box2D)
- **Rendering:** LibGDX SpriteBatch, ShapeRenderer
- **Animation:** LibGDX Animation system

## Physics Design

### Physics World Setup

```java
public class PhysicsEngine {
    private World world; // JBox2D world
    private float timeStep = 1/60f; // 60 FPS
    private int velocityIterations = 6;
    private int positionIterations = 2;
    
    // Bodies
    private List<PlayerBody> playerBodies; // 22 players
    private BallBody ballBody;
    private List<Body> pitchBoundaries; // Pitch walls
    
    public void step(float deltaTime) {
        world.step(timeStep, velocityIterations, positionIterations);
    }
}
```

### Player Physics

**Body Properties:**
- **Shape:** Circle or capsule (representing player)
- **Mass:** Based on player attributes (strength, weight)
- **Friction:** Based on pitch condition
- **Density:** Realistic player density
- **Linear Damping:** Air resistance
- **Angular Damping:** Rotation resistance

**Movement:**
- **Acceleration:** Based on player speed attribute
- **Max Speed:** Based on player speed + sprint capability
- **Deceleration:** Natural slowing when not actively moving
- **Turning:** Angular velocity based on player agility

```java
public class PlayerBody {
    private Body body; // JBox2D body
    private PlayerAttributes attributes;
    
    public void applyMovement(float targetX, float targetY, float deltaTime) {
        // Calculate direction
        Vector2 currentPos = body.getPosition();
        Vector2 direction = new Vector2(targetX - currentPos.x, targetY - currentPos.y);
        direction.nor(); // Normalize
        
        // Calculate force based on acceleration
        float acceleration = attributes.getAcceleration() * ACCELERATION_MULTIPLIER;
        Vector2 force = direction.scl(acceleration * body.getMass());
        
        // Apply force
        body.applyForceToCenter(force, true);
        
        // Limit max speed
        Vector2 velocity = body.getLinearVelocity();
        float maxSpeed = attributes.getSpeed() * MAX_SPEED_MULTIPLIER;
        if (velocity.len() > maxSpeed) {
            velocity.nor().scl(maxSpeed);
            body.setLinearVelocity(velocity);
        }
    }
}
```

### Ball Physics

**Body Properties:**
- **Shape:** Circle (football)
- **Mass:** Realistic ball mass (~0.43 kg)
- **Friction:** Based on pitch condition
- **Restitution:** Bounciness (based on ball type, pitch)
- **Linear Damping:** Air resistance
- **Angular Damping:** Spin decay

**Ball Interactions:**
- **Player Contact:** Ball kicked/passed by player
- **Ground Contact:** Ball bounces on pitch
- **Goal Post Contact:** Ball bounces off posts/crossbar
- **Out of Bounds:** Ball goes out, triggers throw-in/corner/goal kick

```java
public class BallPhysics {
    private Body body;
    private float radius = 0.11f; // ~11cm radius (standard football)
    
    public void kick(Vector2 direction, float power, float spin) {
        // Apply impulse
        Vector2 impulse = direction.nor().scl(power * body.getMass());
        body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
        
        // Apply spin (angular velocity)
        float angularImpulse = spin * body.getInertia();
        body.applyAngularImpulse(angularImpulse, true);
    }
    
    public void pass(PlayerBody fromPlayer, PlayerBody toPlayer, float power) {
        Vector2 direction = calculateDirection(fromPlayer, toPlayer);
        kick(direction, power, 0); // No spin for passes
    }
    
    public void shoot(PlayerBody player, Vector2 target, float power) {
        Vector2 direction = calculateDirection(player, target);
        float spin = calculateSpin(player, direction); // Top spin, side spin, etc.
        kick(direction, power, spin);
    }
}
```

### Pitch Physics

**Pitch Boundaries:**
- **Touchlines:** Left and right boundaries (throw-ins)
- **Goal Lines:** Top and bottom boundaries (goal kicks, corners, goals)
- **Goal Area:** Goal posts and crossbar (collision detection)

**Pitch Surface:**
- **Friction:** Based on pitch condition
- **Bounce:** Based on pitch condition (wet pitch = less bounce)
- **Slope:** Can model pitch slope (if data available)

## Rendering Design

### Coordinate System

- **World Coordinates:** Physics world (meters)
- **Screen Coordinates:** Screen pixels
- **Camera:** Transforms world to screen coordinates

### Pitch Rendering

```java
public class PitchRenderer {
    private float pitchLength; // Meters (1888: 91-183m)
    private float pitchWidth; // Meters (1888: 46-91m)
    
    public void render(ShapeRenderer shapeRenderer, Camera camera) {
        // Draw pitch outline
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(0, 0, pitchLength, pitchWidth);
        
        // Draw center circle
        shapeRenderer.circle(pitchLength / 2, pitchWidth / 2, 9.15f); // 10 yards radius
        
        // Draw penalty areas (if applicable for 1888)
        // Draw goal areas
        // Draw corner arcs
        // Draw center line
    }
}
```

### Player Rendering

```java
public class PlayerSprite {
    private Texture playerTexture; // Player sprite/texture
    private PlayerBody playerBody;
    private Color teamColor;
    private int jerseyNumber;
    
    public void render(SpriteBatch batch, Camera camera) {
        Vector2 position = playerBody.getPosition();
        float angle = playerBody.getAngle();
        
        // Transform world to screen coordinates
        Vector2 screenPos = camera.project(position);
        
        // Draw player sprite
        batch.setColor(teamColor);
        batch.draw(playerTexture, 
            screenPos.x - PLAYER_SIZE / 2, 
            screenPos.y - PLAYER_SIZE / 2,
            PLAYER_SIZE / 2, PLAYER_SIZE / 2, // Origin
            PLAYER_SIZE, PLAYER_SIZE, // Size
            1, 1, // Scale
            MathUtils.radiansToDegrees * angle); // Rotation
        
        // Draw jersey number
        drawJerseyNumber(batch, screenPos, jerseyNumber);
        
        // Draw player name (optional, if space allows)
        if (camera.getZoom() > ZOOM_THRESHOLD_FOR_NAMES) {
            drawPlayerName(batch, screenPos, playerBody.getPlayerName());
        }
    }
}
```

### Ball Rendering

```java
public class BallRenderer {
    private Texture ballTexture;
    private BallPhysics ballPhysics;
    
    public void render(SpriteBatch batch, Camera camera) {
        Vector2 position = ballPhysics.getPosition();
        Vector2 screenPos = camera.project(position);
        
        // Draw ball with rotation (spin)
        float rotation = ballPhysics.getAngle() * MathUtils.radiansToDegrees;
        batch.draw(ballTexture,
            screenPos.x - BALL_SIZE / 2,
            screenPos.y - BALL_SIZE / 2,
            BALL_SIZE / 2, BALL_SIZE / 2,
            BALL_SIZE, BALL_SIZE,
            1, 1,
            rotation);
    }
}
```

### Formation Visualization

```java
public class FormationRenderer {
    public void renderFormation(ShapeRenderer shapeRenderer, Formation formation, List<PlayerBody> players) {
        // Draw formation lines (optional, can be toggled)
        for (FormationLine line : formation.getLines()) {
            shapeRenderer.setColor(new Color(1, 1, 1, 0.3f)); // Semi-transparent
            shapeRenderer.line(line.getStartX(), line.getStartY(), 
                             line.getEndX(), line.getEndY());
        }
        
        // Highlight player positions
        for (PlayerBody player : players) {
            Vector2 pos = player.getPosition();
            shapeRenderer.setColor(player.getTeamColor());
            shapeRenderer.circle(pos.x, pos.y, 0.5f); // Position indicator
        }
    }
}
```

## Camera System

### Camera Types

1. **Overview Camera:** Shows entire pitch
2. **Follow Ball Camera:** Follows ball with offset
3. **Follow Player Camera:** Follows specific player
4. **Fixed Camera:** Fixed position (user controlled)

### Camera Controls

```java
public class CameraController {
    private OrthographicCamera camera;
    
    public void setZoom(float zoom) {
        camera.zoom = MathUtils.clamp(zoom, MIN_ZOOM, MAX_ZOOM);
    }
    
    public void pan(float deltaX, float deltaY) {
        camera.translate(deltaX, deltaY);
        clampCameraToPitch();
    }
    
    public void followBall(BallPhysics ball) {
        Vector2 ballPos = ball.getPosition();
        camera.position.set(ballPos.x, ballPos.y, 0);
        clampCameraToPitch();
    }
    
    public void followPlayer(PlayerBody player) {
        Vector2 playerPos = player.getPosition();
        camera.position.set(playerPos.x, playerPos.y, 0);
        clampCameraToPitch();
    }
    
    private void clampCameraToPitch() {
        float halfViewportWidth = camera.viewportWidth * camera.zoom / 2;
        float halfViewportHeight = camera.viewportHeight * camera.zoom / 2;
        
        camera.position.x = MathUtils.clamp(camera.position.x,
            halfViewportWidth, pitchLength - halfViewportWidth);
        camera.position.y = MathUtils.clamp(camera.position.y,
            halfViewportHeight, pitchWidth - halfViewportHeight);
    }
}
```

## Real-Time Simulation Loop

```java
public class MatchSimulator2D {
    private PhysicsEngine physics;
    private MatchRenderer2D renderer;
    private CameraController camera;
    private float matchTime = 0; // Match time in seconds
    private float simulationSpeed = 1.0f; // 1.0 = real-time, 2.0 = 2x speed
    
    public void simulateStep(float deltaTime) {
        float adjustedDelta = deltaTime * simulationSpeed;
        matchTime += adjustedDelta;
        
        // Update physics
        physics.step(adjustedDelta);
        
        // Update player AI/movement
        updatePlayerMovements(adjustedDelta);
        
        // Update ball interactions
        updateBallInteractions();
        
        // Check for events (goals, fouls, etc.)
        checkMatchEvents();
        
        // Render
        renderer.render(physics, camera);
        
        // Update camera
        if (cameraMode == FOLLOW_BALL) {
            camera.followBall(physics.getBall());
        }
    }
}
```

## Performance Optimization

### Rendering Optimizations

1. **Culling:** Only render visible objects
2. **Level of Detail (LOD):** Simpler rendering at distance/zoom
3. **Batch Rendering:** Batch similar sprites
4. **Texture Atlas:** Use sprite sheets for players/ball

### Physics Optimizations

1. **Time Step:** Fixed time step for stability
2. **Sleeping Bodies:** Bodies at rest can sleep (not updated)
3. **Broad Phase:** Spatial partitioning for collision detection
4. **Contact Filtering:** Skip unnecessary collisions

### Code Optimizations

1. **Object Pooling:** Reuse objects (vectors, bodies, etc.)
2. **Cache Calculations:** Cache expensive calculations
3. **Minimize Allocations:** Avoid allocations in hot paths

## Integration with Match Simulation

### Event Visualization

```java
public class EventVisualizer {
    public void visualizeEvent(MatchEvent event, PhysicsEngine physics, MatchRenderer2D renderer) {
        switch (event.getType()) {
            case GOAL:
                showGoalAnimation(event.getPosition());
                break;
            case SHOT:
                showShotTrail(event.getStartPosition(), event.getEndPosition());
                break;
            case FOUL:
                highlightPlayer(event.getPlayerId(), Color.YELLOW);
                break;
            case CARD:
                showCardAnimation(event.getPlayerId(), event.getCardType());
                break;
        }
    }
}
```

### Match Replay

```java
public class MatchReplay {
    private List<MatchStateSnapshot> snapshots; // Periodic state snapshots
    
    public void recordSnapshot(float timestamp) {
        MatchStateSnapshot snapshot = new MatchStateSnapshot();
        snapshot.timestamp = timestamp;
        snapshot.playerPositions = getPlayerPositions();
        snapshot.ballPosition = getBallPosition();
        snapshot.ballVelocity = getBallVelocity();
        snapshots.add(snapshot);
    }
    
    public void replay(float targetTime) {
        // Find closest snapshot
        MatchStateSnapshot snapshot = findSnapshot(targetTime);
        
        // Restore state
        restoreState(snapshot);
        
        // Simulate forward to target time
        simulateForward(snapshot.timestamp, targetTime);
    }
}
```

## Historical Accuracy (1888)

### Pitch Size Support

- **Variable Pitch Dimensions:** Support 100-200 yards length, 50-100 yards width
- **Dynamic Scaling:** Renderer adapts to pitch size
- **Camera Adaptation:** Camera works with any pitch size

### Formation Visualization

- **2-3-5 Pyramid:** Default formation rendering
- **Position Names:** Show historical position names (half-back, inside forward, etc.)
- **Tactical Lines:** Visual representation of formation lines

## Testing

### Unit Tests

- Test physics calculations
- Test rendering components
- Test camera controls
- Test ball physics

### Integration Tests

- Test full match simulation
- Test performance (FPS, memory)
- Test with different pitch sizes
- Test camera modes

### Visual Tests

- Manual visual inspection
- Screenshot comparison
- Replay verification
- Performance profiling

## Implementation Plan

### Step 1: Physics Setup

1. Set up JBox2D world
2. Create player bodies
3. Create ball body
4. Create pitch boundaries
5. Test basic physics

### Step 2: Basic Rendering

1. Render pitch
2. Render players (simple circles/sprites)
3. Render ball
4. Test rendering performance

### Step 3: Player Movement

1. Implement player movement AI
2. Test player positioning
3. Test formation adherence
4. Optimize movement calculations

### Step 4: Ball Physics

1. Implement ball kicking/passing
2. Implement ball physics interactions
3. Test ball movement realism
4. Test ball-player interactions

### Step 5: Camera System

1. Implement camera controller
2. Implement camera modes
3. Test camera smoothness
4. Test camera bounds

### Step 6: Event Visualization

1. Implement event highlighting
2. Implement goal animations
3. Implement card animations
4. Test event feedback

### Step 7: Performance Optimization

1. Profile performance
2. Implement optimizations
3. Test performance improvements
4. Verify 60 FPS target

### Step 8: Integration

1. Integrate with match simulation
2. Integrate with commentary system
3. Test full match visualization
4. Polish and refine

## Success Criteria

- ✅ Smooth 60 FPS performance
- ✅ Realistic physics (ball, players)
- ✅ Accurate formation visualization
- ✅ Smooth camera movement
- ✅ Clear visual feedback for events
- ✅ Support for variable pitch sizes
- ✅ Real-time simulation capability
- ✅ Match replay functionality

---

**Next:** Begin implementation after Phase 1 (Text Commentary) is complete



