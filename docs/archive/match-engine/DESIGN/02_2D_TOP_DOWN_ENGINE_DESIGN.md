# 2D Top-Down Match Engine Design - Phase 2

**Design Date:** 2025-01-XX  
**Repository:** futtoboru_match_engine  
**Phase:** 2 - 2D Top-Down Visual Match Engine  
**Priority:** CRITICAL - Core Element of Game

---

## Executive Summary

The 2D top-down match engine is the **core visual element** of Futtoboru. It provides a real-time visual representation of matches with physics-based ball movement, player positioning, and tactical visualization. This engine builds upon the text engine foundation and adds visual depth and immersion.

**Key Design Principles:**
- **Physics-Based**: Realistic ball and player physics
- **Real-Time**: 60 FPS target for smooth visualization
- **Tactical**: Clear visualization of formations and tactics
- **Historical**: 1888 pitch dimensions and conditions
- **Performant**: Efficient rendering and simulation

---

## 1. Architecture Overview

### 1.1 System Components

```
2DMatchEngine
├── PhysicsEngine (Ball and player physics)
├── RenderingEngine (Visual rendering)
├── PlayerController (Player movement and AI)
├── BallController (Ball physics and movement)
├── TacticalRenderer (Formation and tactical visualization)
├── MatchSimulator (Match simulation integration)
└── EventRenderer (Event visualization)
```

### 1.2 Integration with Text Engine

**Layered Approach:**
- Text engine provides simulation logic
- 2D engine provides visual representation
- Both engines share same factor system
- Both engines use same event system
- 2D engine visualizes text engine events

---

## 2. Physics System

### 2.1 Ball Physics

**Ball Properties:**
```java
public class Ball {
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 acceleration;
    private float radius = 0.11f; // 11cm radius (standard)
    private float mass = 0.43f; // 430g (standard)
    private float friction = 0.98f; // Ground friction
    private float airResistance = 0.99f; // Air resistance
    private boolean isInPlay = true;
}
```

**Physics Calculations:**
```java
public class BallPhysics {
    
    public void updateBall(Ball ball, float deltaTime, Pitch pitch) {
        // Apply gravity
        ball.getAcceleration().y = -9.81f; // m/s²
        
        // Apply friction (when on ground)
        if (ball.isOnGround(pitch)) {
            ball.getVelocity().mul(ball.getFriction());
        }
        
        // Apply air resistance
        ball.getVelocity().mul(ball.getAirResistance());
        
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
        
        // Handle collisions
        handleCollisions(ball, pitch);
    }
    
    private void handleCollisions(Ball ball, Pitch pitch) {
        // Ground collision
        if (ball.getPosition().y < ball.getRadius()) {
            ball.getPosition().y = ball.getRadius();
            ball.getVelocity().y = -ball.getVelocity().y * 0.7f; // Bounce
        }
        
        // Pitch boundaries
        if (ball.getPosition().x < 0) {
            ball.getPosition().x = 0;
            ball.getVelocity().x = -ball.getVelocity().x * 0.8f;
        }
        if (ball.getPosition().x > pitch.getLength()) {
            ball.getPosition().x = pitch.getLength();
            ball.getVelocity().x = -ball.getVelocity().x * 0.8f;
        }
        
        // Goal line collision (goal or out of bounds)
        if (ball.getPosition().y < 0 || ball.getPosition().y > pitch.getWidth()) {
            handleGoalLineCollision(ball, pitch);
        }
    }
}
```

### 2.2 Player Physics

**Player Properties:**
```java
public class PlayerPhysics {
    private Vector2 position;
    private Vector2 velocity;
    private Vector2 targetPosition; // AI target
    private float maxSpeed; // Based on speed attribute
    private float acceleration; // Based on acceleration attribute
    private float stamina; // Current stamina
    private float radius = 0.3f; // Player radius (approximate)
}
```

**Player Movement:**
```java
public class PlayerMovement {
    
    public void updatePlayer(Player player, float deltaTime) {
        // Calculate direction to target
        Vector2 direction = new Vector2(
            player.getTargetPosition().x - player.getPosition().x,
            player.getTargetPosition().y - player.getPosition().y
        ).nor();
        
        // Apply acceleration
        float speed = calculateSpeed(player);
        player.getVelocity().set(
            direction.x * speed,
            direction.y * speed
        );
        
        // Update position
        player.getPosition().add(
            player.getVelocity().x * deltaTime,
            player.getVelocity().y * deltaTime
        );
        
        // Update stamina
        updateStamina(player, deltaTime);
    }
    
    private float calculateSpeed(Player player) {
        // Base speed from attribute
        float baseSpeed = player.getSpeedAttribute() / 100.0f * 5.0f; // m/s
        
        // Stamina effect
        float staminaMultiplier = player.getStamina() / 100.0f;
        
        // Fatigue effect
        float fatigueMultiplier = 1.0f - (player.getFatigue() / 100.0f * 0.3f);
        
        return baseSpeed * staminaMultiplier * fatigueMultiplier;
    }
}
```

### 2.3 Collision Detection

**Ball-Player Collision:**
```java
public class CollisionDetector {
    
    public boolean checkBallPlayerCollision(Ball ball, Player player) {
        float distance = ball.getPosition().dst(player.getPosition());
        return distance < (ball.getRadius() + player.getRadius());
    }
    
    public void handleBallPlayerCollision(Ball ball, Player player) {
        // Calculate collision normal
        Vector2 normal = new Vector2(
            ball.getPosition().x - player.getPosition().x,
            ball.getPosition().y - player.getPosition().y
        ).nor();
        
        // Calculate relative velocity
        Vector2 relativeVelocity = new Vector2(
            ball.getVelocity().x - player.getVelocity().x,
            ball.getVelocity().y - player.getVelocity().y
        );
        
        // Calculate impulse
        float impulse = relativeVelocity.dot(normal);
        
        // Apply impulse to ball
        ball.getVelocity().add(
            normal.x * impulse * 0.8f, // Ball elasticity
            normal.y * impulse * 0.8f
        );
        
        // Player attributes affect ball control
        float ballControl = player.getBallControlAttribute() / 100.0f;
        ball.getVelocity().mul(1.0f - (1.0f - ballControl) * 0.3f);
    }
}
```

---

## 3. Rendering System

### 3.1 Pitch Rendering

**Pitch Visualization:**
```java
public class PitchRenderer {
    
    public void renderPitch(Pitch pitch, SpriteBatch batch) {
        // Draw pitch background (grass)
        batch.setColor(0.2f, 0.6f, 0.2f, 1.0f); // Green
        batch.draw(
            pitchTexture,
            0, 0,
            pitch.getLength(),
            pitch.getWidth()
        );
        
        // Draw center line
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White
        drawLine(
            batch,
            pitch.getLength() / 2, 0,
            pitch.getLength() / 2, pitch.getWidth()
        );
        
        // Draw center circle
        drawCircle(
            batch,
            pitch.getLength() / 2, pitch.getWidth() / 2,
            pitch.getCenterCircleRadius()
        );
        
        // Draw penalty areas (1888 rules - different from modern)
        drawPenaltyAreas(batch, pitch);
        
        // Draw goal areas
        drawGoalAreas(batch, pitch);
        
        // Draw goals
        drawGoals(batch, pitch);
    }
}
```

### 3.2 Player Rendering (FM 2008 Style - "Blob View")

**Player Representation - Colored Circles:**
```java
public class PlayerRenderer {
    
    private ShapeRenderer shapeRenderer;
    
    public PlayerRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void renderPlayer(PlayerVisual player, SpriteBatch batch) {
        // FM 2008 Style: Colored circles matching team kit colors
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Set player color (team kit color)
        Color teamColor = getTeamColor(player.getTeam());
        shapeRenderer.setColor(teamColor);
        
        // Draw player circle (appropriate size - not oversized like FM 2026)
        float radius = 0.3f; // Appropriate size for visibility
        shapeRenderer.circle(
            player.getPosition().x,
            player.getPosition().y,
            radius
        );
        
        // Optional: Draw player number in center
        if (showPlayerNumbers) {
            // Use BitmapFont to draw number
            drawPlayerNumber(batch, player);
        }
        
        shapeRenderer.end();
        
        // Optional: Draw stamina indicator (subtle, not distracting)
        if (showStaminaBars) {
            drawStaminaBar(batch, player);
        }
    }
    
    private Color getTeamColor(Team team) {
        // Return team's primary kit color
        // Home team: One color, Away team: Different color
        return team.getPrimaryKitColor();
    }
}
```

**Design Philosophy (FM 2008):**
- **Simple Colored Circles:** Not complex sprites
- **Team Color Coding:** Easy identification
- **Appropriate Size:** Not oversized (learn from FM 2026 mistakes)
- **Clear Visibility:** Easy to see and track
- **Tactical Focus:** Function over form

### 3.3 Ball Rendering

**Ball Visualization:**
```java
public class BallRenderer {
    
    public void renderBall(Ball ball, SpriteBatch batch) {
        // Draw ball
        batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White
        batch.draw(
            ballTexture,
            ball.getPosition().x - ball.getRadius(),
            ball.getPosition().y - ball.getRadius(),
            ball.getRadius() * 2,
            ball.getRadius() * 2
        );
        
        // Draw ball trail (optional, for visibility)
        if (showBallTrail) {
            drawBallTrail(batch, ball);
        }
    }
}
```

### 3.4 Tactical Overlays

**Formation Visualization:**
```java
public class TacticalRenderer {
    
    public void renderFormation(Formation formation, SpriteBatch batch) {
        // Draw formation lines
        for (FormationLine line : formation.getLines()) {
            drawFormationLine(batch, line);
        }
        
        // Draw player roles
        for (PlayerRole role : formation.getRoles()) {
            drawPlayerRole(batch, role);
        }
        
        // Draw tactical zones
        if (showTacticalZones) {
            drawTacticalZones(batch, formation);
        }
    }
}
```

---

## 4. Player AI and Movement

### 4.1 AI Decision-Making

**Player AI States:**
```java
public enum PlayerAIState {
    IDLE,           // Standing still
    MOVING,         // Moving to position
    CHASING_BALL,   // Chasing ball
    SUPPORTING,     // Supporting teammate
    MARKING,        // Marking opponent
    ATTACKING,      // Attacking
    DEFENDING       // Defending
}
```

**AI Controller:**
```java
public class PlayerAIController {
    
    public void updatePlayerAI(Player player, MatchState state) {
        // Determine current state
        PlayerAIState currentState = determineState(player, state);
        
        // Execute state behavior
        switch (currentState) {
            case CHASING_BALL:
                chaseBall(player, state.getBall());
                break;
            case MOVING:
                moveToPosition(player, getTargetPosition(player, state));
                break;
            case MARKING:
                markOpponent(player, findMarkingTarget(player, state));
                break;
            // ... other states
        }
    }
    
    private PlayerAIState determineState(Player player, MatchState state) {
        // Priority: Ball nearby > Position > Tactical role
        
        // Check if ball is nearby
        if (player.getPosition().dst(state.getBall().getPosition()) < 5.0f) {
            return PlayerAIState.CHASING_BALL;
        }
        
        // Check if out of position
        Vector2 targetPos = getTargetPosition(player, state);
        if (player.getPosition().dst(targetPos) > 2.0f) {
            return PlayerAIState.MOVING;
        }
        
        // Check tactical role
        if (player.getRole() == PlayerRole.DEFENDER) {
            return PlayerAIState.MARKING;
        } else if (player.getRole() == PlayerRole.ATTACKER) {
            return PlayerAIState.ATTACKING;
        }
        
        return PlayerAIState.IDLE;
    }
}
```

### 4.2 Positional Play

**Formation-Based Positioning:**
```java
public class PositionalSystem {
    
    public Vector2 getTargetPosition(Player player, MatchState state) {
        Formation formation = state.getFormation(player.getTeam());
        PlayerRole role = player.getRole();
        
        // Get base position from formation
        Vector2 basePosition = formation.getPosition(role);
        
        // Adjust based on ball position (support play)
        Vector2 ballPos = state.getBall().getPosition();
        Vector2 adjustedPosition = adjustForBallPosition(
            basePosition,
            ballPos,
            role
        );
        
        // Adjust based on match state
        if (state.isLosing(player.getTeam())) {
            adjustedPosition = adjustForAttacking(adjustedPosition, role);
        } else if (state.isWinning(player.getTeam())) {
            adjustedPosition = adjustForDefending(adjustedPosition, role);
        }
        
        return adjustedPosition;
    }
}
```

---

## 5. Event Visualization

### 5.1 Goal Visualization

**Goal Animation:**
```java
public class GoalRenderer {
    
    public void renderGoal(GoalEvent event, SpriteBatch batch) {
        // Flash effect
        batch.setColor(1.0f, 1.0f, 0.0f, 0.5f); // Yellow flash
        batch.draw(flashTexture, 0, 0, pitchWidth, pitchHeight);
        
        // Ball in net animation
        animateBallInNet(event.getBallPosition(), event.getGoalPosition());
        
        // Celebration particles
        spawnCelebrationParticles(event.getScorerPosition());
        
        // Score update text
        drawScoreText(event.getHomeScore(), event.getAwayScore());
    }
}
```

### 5.2 Card Visualization

**Card Display:**
```java
public class CardRenderer {
    
    public void renderCard(CardEvent event, SpriteBatch batch) {
        // Show card above player
        Texture cardTexture = event.getCardType() == CardType.YELLOW 
            ? yellowCardTexture 
            : redCardTexture;
        
        batch.draw(
            cardTexture,
            event.getPlayerPosition().x,
            event.getPlayerPosition().y + 1.0f,
            0.5f, 0.5f
        );
        
        // Show card count (if multiple)
        if (event.getCardCount() > 1) {
            drawCardCount(event.getCardCount());
        }
    }
}
```

---

## 6. Performance Optimization

### 6.1 Rendering Optimization

**Optimization Strategies:**
- Sprite batching
- Texture atlases
- Level-of-detail (LOD) for distant players
- Culling (don't render off-screen)
- Efficient collision detection

### 6.2 Physics Optimization

**Optimization Strategies:**
- Fixed timestep physics
- Spatial partitioning for collision detection
- Simplified physics for distant objects
- Physics caching

### 6.3 Target Performance

**Performance Targets:**
- 60 FPS during match
- < 16ms per frame
- Smooth ball movement
- Responsive player movement

---

## 7. Integration with Text Engine

### 7.1 Event Synchronization

**Synchronizing Events:**
```java
public class EventSynchronizer {
    
    public void synchronizeEvents(
        List<MatchEvent> textEvents,
        MatchState visualState
    ) {
        // Text engine generates events
        // 2D engine visualizes events
        
        for (MatchEvent event : textEvents) {
            // Trigger visual representation
            switch (event.getType()) {
                case GOAL:
                    triggerGoalAnimation(event);
                    break;
                case SHOT:
                    triggerShotAnimation(event);
                    break;
                // ... other events
            }
        }
    }
}
```

### 7.2 Shared State

**Common Match State:**
- Both engines use same match state
- Text engine updates state
- 2D engine reads state for visualization
- Synchronized updates

---

## 8. User Interface

### 8.1 Match View Controls

**Controls:**
- Pause/Resume
- Speed control (1x, 2x, 4x)
- Camera controls (zoom, pan)
- Statistics overlay toggle
- Tactical overlay toggle

### 8.2 Information Display

**Display Elements:**
- Score
- Time
- Possession
- Shots
- Cards
- Substitutions
- Player names/numbers
- Formation display

---

## 9. Implementation Details

### 9.1 Technology Stack

**Recommended:**
- LibGDX (Java game framework)
- Box2D (Physics engine, optional)
- Custom physics (for more control)

### 9.2 Data Structures

**Visual Match State:**
```java
public class VisualMatchState {
    private MatchState matchState; // Shared with text engine
    private List<PlayerVisual> players;
    private BallVisual ball;
    private Camera camera;
    private float timeScale = 1.0f;
    private boolean isPaused = false;
}
```

---

## 10. Success Criteria

### 10.1 Visual Quality

- ✅ Clear pitch visualization
- ✅ Visible player positions
- ✅ Smooth ball movement
- ✅ Clear tactical information

### 10.2 Performance

- ✅ 60 FPS maintained
- ✅ Smooth gameplay
- ✅ No lag or stuttering

### 10.3 Realism

- ✅ Realistic physics
- ✅ Realistic player movement
- ✅ Realistic match flow

---

**Status:** ✅ Design Complete  
**Next Document:** `03_MATCH_FACTORS_SYSTEM.md`



