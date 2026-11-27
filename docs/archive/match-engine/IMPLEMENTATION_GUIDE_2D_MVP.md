# 2D Match Engine MVP - Quick Implementation Guide

**Quick Reference:** Step-by-step implementation guide for 2D Match Engine MVP

---

## Phase 1: Core Rendering (Week 1-3)

### Step 1: Setup Match Screen (Day 1)

**Create Match2DScreen:**
```java
package com.rndmodgames.futtoboru.screens.match;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.rndmodgames.futtoboru.data.Match;

public class Match2DScreen implements Screen {
    
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Viewport viewport;
    
    // Match data
    private Match match;
    
    // Constants (1888 pitch dimensions - variable, max 200×100 yards)
    private static final float PITCH_WIDTH = 100f;  // 100 yards = 91.44 meters
    private static final float PITCH_HEIGHT = 200f; // 200 yards = 182.88 meters
    
    public Match2DScreen(Match match) {
        this.match = match;
    }
    
    @Override
    public void show() {
        // Setup camera
        camera = new OrthographicCamera();
        viewport = new FitViewport(PITCH_WIDTH, PITCH_HEIGHT, camera);
        viewport.apply();
        camera.position.set(PITCH_WIDTH / 2f, PITCH_HEIGHT / 2f, 0);
        camera.update();
        
        // Setup batch
        batch = new SpriteBatch();
    }
    
    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        // Render match
        batch.begin();
        // TODO: Render pitch, players, ball
        batch.end();
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(PITCH_WIDTH / 2f, PITCH_HEIGHT / 2f, 0);
        camera.update();
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        batch.dispose();
    }
}
```

**Integrate with Game:**
```java
// In MainGameScreen or wherever match is triggered
public void showMatch2D(Match match) {
    Match2DScreen matchScreen = new Match2DScreen(match);
    game.setScreen(matchScreen);
}
```

### Step 2: Pitch Rendering (Day 2-3)

**Create PitchRenderer:**
```java
package com.rndmodgames.futtoboru.matchengine.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rndmodgames.futtoboru.data.Stadium;

public class PitchRenderer {
    
    private ShapeRenderer shapeRenderer;
    
    public PitchRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, Stadium stadium, float pitchWidth, float pitchHeight) {
        // Render pitch background (green)
        batch.setColor(0.2f, 0.6f, 0.2f, 1.0f); // Green grass
        // Draw filled rectangle for pitch
        // Note: LibGDX doesn't have direct rectangle drawing in SpriteBatch
        // Use ShapeRenderer or create a texture
        
        // Switch to ShapeRenderer for lines
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        // Center line (if applicable in 1888)
        shapeRenderer.line(pitchWidth / 2f, 0, pitchWidth / 2f, pitchHeight);
        
        // Center circle (if applicable in 1888)
        // Note: 1888 may not have center circle, check historical rules
        float centerCircleRadius = 9.15f; // 10 yards = 9.15 meters
        shapeRenderer.circle(pitchWidth / 2f, pitchHeight / 2f, centerCircleRadius);
        
        // Goal lines (top and bottom)
        shapeRenderer.line(0, 0, pitchWidth, 0);
        shapeRenderer.line(0, pitchHeight, pitchWidth, pitchHeight);
        
        // Touchlines (left and right)
        shapeRenderer.line(0, 0, 0, pitchHeight);
        shapeRenderer.line(pitchWidth, 0, pitchWidth, pitchHeight);
        
        // Goal areas (6-yard box) - if standardized in 1888
        // Note: Check if goal areas existed in 1888
        float goalAreaDepth = 5.4864f; // 6 yards = 5.4864 meters
        float goalWidth = 7.32f; // Standard goal width = 7.32 meters
        
        // Bottom goal area
        float goalAreaLeft = (pitchWidth - goalWidth) / 2f;
        shapeRenderer.rect(goalAreaLeft, 0, goalWidth, goalAreaDepth);
        
        // Top goal area
        shapeRenderer.rect(goalAreaLeft, pitchHeight - goalAreaDepth, goalWidth, goalAreaDepth);
        
        // Penalty areas (18-yard box) - if standardized in 1888
        // Note: Penalty areas may not have existed in 1888
        float penaltyAreaDepth = 16.4592f; // 18 yards = 16.4592 meters
        shapeRenderer.rect(goalAreaLeft - (penaltyAreaDepth - goalAreaDepth), 0, 
                          goalWidth + 2 * (penaltyAreaDepth - goalAreaDepth), penaltyAreaDepth);
        shapeRenderer.rect(goalAreaLeft - (penaltyAreaDepth - goalAreaDepth), 
                          pitchHeight - penaltyAreaDepth, 
                          goalWidth + 2 * (penaltyAreaDepth - goalAreaDepth), penaltyAreaDepth);
        
        shapeRenderer.end();
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
```

### Step 3: Player Rendering (Day 4-5)

**Create PlayerVisual:**
```java
package com.rndmodgames.futtoboru.matchengine.data;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.rndmodgames.futtoboru.data.Player;

public class PlayerVisual {
    private Player player;
    private Vector2 position;
    private Color teamColor;
    private float radius = 0.3f; // Player radius in meters
    private int playerNumber;
    
    public PlayerVisual(Player player, Vector2 position, Color teamColor, int playerNumber) {
        this.player = player;
        this.position = position;
        this.teamColor = teamColor;
        this.playerNumber = playerNumber;
    }
    
    // Getters and setters
    public Vector2 getPosition() { return position; }
    public Color getTeamColor() { return teamColor; }
    public float getRadius() { return radius; }
    public int getPlayerNumber() { return playerNumber; }
}
```

**Create PlayerRenderer:**
```java
package com.rndmodgames.futtoboru.matchengine.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rndmodgames.futtoboru.matchengine.data.PlayerVisual;

import java.util.List;

public class PlayerRenderer {
    
    private ShapeRenderer shapeRenderer;
    
    public PlayerRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, List<PlayerVisual> players) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (PlayerVisual player : players) {
            // Set player color
            shapeRenderer.setColor(player.getTeamColor());
            
            // Draw player circle
            shapeRenderer.circle(
                player.getPosition().x,
                player.getPosition().y,
                player.getRadius()
            );
            
            // Optional: Draw player number
            // Would need BitmapFont for text rendering
        }
        
        shapeRenderer.end();
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
```

### Step 4: Ball Rendering (Day 6)

**Create BallVisual:**
```java
package com.rndmodgames.futtoboru.matchengine.data;

import com.badlogic.gdx.math.Vector2;

public class BallVisual {
    private Vector2 position;
    private float radius = 0.11f; // 11cm radius (standard football)
    
    public BallVisual() {
        this.position = new Vector2();
    }
    
    public Vector2 getPosition() { return position; }
    public float getRadius() { return radius; }
}
```

**Create BallRenderer:**
```java
package com.rndmodgames.futtoboru.matchengine.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.rndmodgames.futtoboru.matchengine.data.BallVisual;

public class BallRenderer {
    
    private ShapeRenderer shapeRenderer;
    
    public BallRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, BallVisual ball) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw white ball
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(
            ball.getPosition().x,
            ball.getPosition().y,
            ball.getRadius()
        );
        
        shapeRenderer.end();
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
```

### Step 5: Goals & Referee (Day 7)

**Create GoalRenderer:**
```java
package com.rndmodgames.futtoboru.matchengine.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class GoalRenderer {
    
    private ShapeRenderer shapeRenderer;
    private static final float GOAL_WIDTH = 7.32f; // Standard goal width
    private static final float GOAL_HEIGHT = 2.44f; // Standard goal height
    
    public GoalRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, float pitchWidth, float pitchHeight) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        float goalX = (pitchWidth - GOAL_WIDTH) / 2f;
        
        // Bottom goal (home team)
        // Left post
        shapeRenderer.line(goalX, 0, goalX, GOAL_HEIGHT);
        // Right post
        shapeRenderer.line(goalX + GOAL_WIDTH, 0, goalX + GOAL_WIDTH, GOAL_HEIGHT);
        // Crossbar
        shapeRenderer.line(goalX, GOAL_HEIGHT, goalX + GOAL_WIDTH, GOAL_HEIGHT);
        
        // Top goal (away team)
        // Left post
        shapeRenderer.line(goalX, pitchHeight, goalX, pitchHeight - GOAL_HEIGHT);
        // Right post
        shapeRenderer.line(goalX + GOAL_WIDTH, pitchHeight, goalX + GOAL_WIDTH, pitchHeight - GOAL_HEIGHT);
        // Crossbar
        shapeRenderer.line(goalX, pitchHeight - GOAL_HEIGHT, goalX + GOAL_WIDTH, pitchHeight - GOAL_HEIGHT);
        
        shapeRenderer.end();
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
```

**Create RefereeRenderer:**
```java
package com.rndmodgames.futtoboru.matchengine.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class RefereeRenderer {
    
    private ShapeRenderer shapeRenderer;
    
    public RefereeRenderer() {
        shapeRenderer = new ShapeRenderer();
    }
    
    public void render(SpriteBatch batch, Vector2 position) {
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw referee as black circle
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.circle(position.x, position.y, 0.25f);
        
        shapeRenderer.end();
    }
    
    public void dispose() {
        shapeRenderer.dispose();
    }
}
```

### Step 6: Complete Match2DScreen Integration (Day 8-10)

**Complete Match2DScreen:**
```java
public class Match2DScreen implements Screen {
    
    // ... existing code ...
    
    // Renderers
    private PitchRenderer pitchRenderer;
    private PlayerRenderer playerRenderer;
    private BallRenderer ballRenderer;
    private GoalRenderer goalRenderer;
    private RefereeRenderer refereeRenderer;
    
    // Visual state
    private List<PlayerVisual> homePlayers;
    private List<PlayerVisual> awayPlayers;
    private BallVisual ball;
    private Vector2 refereePosition;
    
    @Override
    public void show() {
        // ... existing camera setup ...
        
        // Initialize renderers
        pitchRenderer = new PitchRenderer();
        playerRenderer = new PlayerRenderer();
        ballRenderer = new BallRenderer();
        goalRenderer = new GoalRenderer();
        refereeRenderer = new RefereeRenderer();
        
        // Initialize visual state
        initializeVisualState();
    }
    
    private void initializeVisualState() {
        // Get clubs
        Club homeClub = gameInstance.getCurrentGame().getClubById(match.getHomeClubId());
        Club awayClub = gameInstance.getCurrentGame().getClubById(match.getAwayClubId());
        
        // Initialize players (11 per team)
        homePlayers = createPlayerVisuals(homeClub, Color.BLUE, true);
        awayPlayers = createPlayerVisuals(awayClub, Color.RED, false);
        
        // Initialize ball (center of pitch)
        ball = new BallVisual();
        ball.getPosition().set(PITCH_WIDTH / 2f, PITCH_HEIGHT / 2f);
        
        // Initialize referee (center of pitch)
        refereePosition = new Vector2(PITCH_WIDTH / 2f, PITCH_HEIGHT / 2f);
    }
    
    private List<PlayerVisual> createPlayerVisuals(Club club, Color teamColor, boolean isHome) {
        List<PlayerVisual> players = new ArrayList<>();
        List<Player> clubPlayers = club.getPlayers();
        
        // Get first 11 players (or all if less than 11)
        int playerCount = Math.min(11, clubPlayers.size());
        
        // Simple formation: 2-3-5 Pyramid (1888 common formation)
        // Position players in formation
        for (int i = 0; i < playerCount; i++) {
            Vector2 position = calculateFormationPosition(i, isHome);
            PlayerVisual playerVisual = new PlayerVisual(
                clubPlayers.get(i),
                position,
                teamColor,
                i + 1
            );
            players.add(playerVisual);
        }
        
        return players;
    }
    
    private Vector2 calculateFormationPosition(int playerIndex, boolean isHome) {
        // Simple 2-3-5 formation positioning
        // This is a placeholder - implement proper formation system
        float x = PITCH_WIDTH / 2f;
        float y = isHome ? 20f : PITCH_HEIGHT - 20f; // Rough positioning
        
        // Adjust based on player index and formation
        // TODO: Implement proper formation positioning
        
        return new Vector2(x, y);
    }
    
    @Override
    public void render(float delta) {
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update camera
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        // Get stadium
        Club homeClub = gameInstance.getCurrentGame().getClubById(match.getHomeClubId());
        Stadium stadium = homeClub.getStadium();
        
        // Render match
        batch.begin();
        pitchRenderer.render(batch, stadium, PITCH_WIDTH, PITCH_HEIGHT);
        goalRenderer.render(batch, PITCH_WIDTH, PITCH_HEIGHT);
        batch.end();
        
        playerRenderer.render(batch, homePlayers);
        playerRenderer.render(batch, awayPlayers);
        ballRenderer.render(batch, ball);
        refereeRenderer.render(batch, refereePosition);
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        pitchRenderer.dispose();
        playerRenderer.dispose();
        ballRenderer.dispose();
        goalRenderer.dispose();
        refereeRenderer.dispose();
    }
}
```

---

## Quick Start Checklist

### Week 1: Foundation
- [ ] Create `Match2DScreen` class
- [ ] Setup camera and viewport
- [ ] Create `PitchRenderer` class
- [ ] Render pitch with markings
- [ ] Create `PlayerRenderer` class
- [ ] Render 22 players as circles
- [ ] Test with different teams

### Week 2: Core Elements
- [ ] Create `BallRenderer` class
- [ ] Render ball
- [ ] Create `GoalRenderer` class
- [ ] Render goals
- [ ] Create `RefereeRenderer` class
- [ ] Render referee
- [ ] Create `StadiumRenderer` class (optional)
- [ ] Render stadium background

### Week 3: Integration
- [ ] Integrate with match simulation
- [ ] Convert match data to visual state
- [ ] Test with real matches
- [ ] Polish graphics
- [ ] Performance optimization

---

## Next Steps After MVP

1. **Phase 2:** Add physics and movement
2. **Phase 3:** Add camera controls and UI
3. **Future:** Add animations, tactical overlays, etc.

---

**Status:** Ready for Implementation  
**Start:** Phase 1, Week 1, Day 1


