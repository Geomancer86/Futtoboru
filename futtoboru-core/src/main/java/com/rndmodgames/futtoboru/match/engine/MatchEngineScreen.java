package com.rndmodgames.futtoboru.match.engine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.rndmodgames.futtoboru.game.Futtoboru;
import com.rndmodgames.futtoboru.match.engine.MatchConfiguration;
import com.rndmodgames.futtoboru.match.engine.components.BallComponent;
import com.rndmodgames.futtoboru.match.engine.components.BodyComponent;
import com.rndmodgames.futtoboru.match.engine.components.PlayerComponent;
import com.rndmodgames.futtoboru.match.engine.components.TextureComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;
import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.steer.behaviors.Seek;
import com.rndmodgames.futtoboru.match.engine.components.GoalComponent;
import com.rndmodgames.futtoboru.match.engine.components.SteeringComponent;
import com.rndmodgames.futtoboru.match.engine.systems.CollisionAvoidanceSystem;
import com.rndmodgames.futtoboru.match.engine.systems.GoalSystem;
import com.rndmodgames.futtoboru.match.engine.systems.PhysicsSystem;
import com.rndmodgames.futtoboru.match.engine.systems.SteeringSystem;
import com.rndmodgames.futtoboru.match.engine.systems.RenderingSystem;
import com.rndmodgames.futtoboru.match.engine.utils.LocationAdapter;
import com.badlogic.ashley.core.Entity;

/**
 * MatchEngineScreen - 2D Football Match Engine using Ashley ECS and Box2D
 * Based on DarkBlade engine architecture
 * 
 * Features:
 * - Fixed timestep at 60 FPS
 * - Realistic physics with Box2D
 * - Entity Component System (Ashley)
 * - 2 players and ball simulation
 */
public class MatchEngineScreen implements Screen {
    
    private Game game;
    private Futtoboru futtoboru;
    
    // Rendering
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // UI
    private Stage uiStage;
    private BitmapFont font;
    private Label matchInfoLabel;
    
    // ECS Engine
    private Engine engine;
    
    // Physics World
    private World physicsWorld;
    
    // Field dimensions (in meters)
    private static final float FIELD_WIDTH = 105f;  // Standard football field
    private static final float FIELD_HEIGHT = 68f;
    
    // Pixels Per Meter
    private static final float PPM = 16.0f;
    
    // Entities
    private Entity player1;
    private Entity player2;
    private Entity ball;
    
    // Textures (placeholder - will be replaced with proper sprites)
    private Texture playerTexture;
    private Texture ballTexture;
    private Texture fieldTexture;
    
    // Match configuration
    private MatchConfiguration config;
    
    public MatchEngineScreen(Game parent, MatchConfiguration config) {
        this.game = parent;
        this.futtoboru = (Futtoboru) parent;
        this.config = config;
        
        // Initialize rendering
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(FIELD_WIDTH, FIELD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(FIELD_WIDTH / 2f, FIELD_HEIGHT / 2f, 0);
        camera.update();
        
        // Initialize UI
        uiStage = new Stage(new ScreenViewport());
        font = new BitmapFont();
        
        // Create match info label
        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().left().pad(10);
        
        String matchInfo = "Match Engine - Press ESC to return";
        if (config != null && config.homeTeam != null && config.awayTeam != null) {
            matchInfo = config.homeTeam.getName() + " vs " + config.awayTeam.getName() + " | Press ESC to return";
        }
        matchInfoLabel = new Label(matchInfo, new Label.LabelStyle(font, com.badlogic.gdx.graphics.Color.WHITE));
        uiTable.add(matchInfoLabel).left();
        uiStage.addActor(uiTable);
        
        // Initialize physics world (no gravity for football)
        physicsWorld = new World(new Vector2(0, 0), true);
        
        // Initialize ECS engine
        engine = new PooledEngine();
        
        // Create placeholder textures
        createPlaceholderTextures();
        
        // Create field boundaries
        createFieldBoundaries();
        
        // Create players and ball
        createPlayers();
        createBall();
        
        // Log match configuration
        if (config != null) {
            Gdx.app.log("MatchEngine", "Match: " + 
                (config.homeTeam != null ? config.homeTeam.getName() : "Unknown") + " vs " +
                (config.awayTeam != null ? config.awayTeam.getName() : "Unknown"));
        }
        
        // Add systems to engine (order matters: goals -> collision avoidance -> steering -> physics -> rendering)
        engine.addSystem(new GoalSystem(ball));
        
        // Collision avoidance system (needs player list)
        Entity[] playerEntities = new Entity[] { player1, player2 };
        engine.addSystem(new CollisionAvoidanceSystem(playerEntities));
        
        engine.addSystem(new SteeringSystem());
        engine.addSystem(new PhysicsSystem(physicsWorld));
        engine.addSystem(new RenderingSystem(batch, camera));
        
        Gdx.app.log("MatchEngine", "Match Engine initialized");
    }
    
    /**
     * Create placeholder textures (will be replaced with proper sprites)
     */
    private void createPlaceholderTextures() {
        // Create simple colored textures using Pixmap
        com.badlogic.gdx.graphics.Pixmap pixmap;
        
        // Player texture (blue circle)
        pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.6f, 1.0f, 1.0f); // Blue
        pixmap.fillCircle(16, 16, 15);
        playerTexture = new Texture(pixmap);
        pixmap.dispose();
        
        // Ball texture (white circle)
        pixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White
        pixmap.fillCircle(8, 8, 7);
        ballTexture = new Texture(pixmap);
        pixmap.dispose();
        
        // Field texture (green rectangle)
        pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.7f, 0.2f, 1.0f); // Green
        pixmap.fill();
        fieldTexture = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Create field boundaries (walls)
     */
    private void createFieldBoundaries() {
        float thickness = 0.5f;
        
        // Top wall
        createWall(FIELD_WIDTH / 2f, FIELD_HEIGHT, FIELD_WIDTH, thickness);
        // Bottom wall
        createWall(FIELD_WIDTH / 2f, 0, FIELD_WIDTH, thickness);
        // Left wall
        createWall(0, FIELD_HEIGHT / 2f, thickness, FIELD_HEIGHT);
        // Right wall
        createWall(FIELD_WIDTH, FIELD_HEIGHT / 2f, thickness, FIELD_HEIGHT);
    }
    
    /**
     * Create a static wall body
     */
    private void createWall(float x, float y, float width, float height) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x, y);
        
        Body body = physicsWorld.createBody(bodyDef);
        
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f, height / 2f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.3f;
        
        body.createFixture(fixtureDef);
        shape.dispose();
    }
    
    /**
     * Create player entities
     */
    private void createPlayers() {
        // Player 1 (Home team - left side)
        player1 = createPlayer(20f, FIELD_HEIGHT / 2f, true);
        
        // Player 2 (Away team - right side)
        player2 = createPlayer(FIELD_WIDTH - 20f, FIELD_HEIGHT / 2f, false);
    }
    
    /**
     * Create a player entity with physics
     */
    private Entity createPlayer(float x, float y, boolean isHomeTeam) {
        Entity entity = engine.createEntity();
        
        // Transform Component
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.position.set(x, y);
        transform.scale.set(1.0f, 1.0f);
        entity.add(transform);
        
        // Texture Component
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        // Create different colored texture for away team
        if (!isHomeTeam) {
            // Create red texture for away team
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(32, 32, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(1.0f, 0.2f, 0.2f, 1.0f); // Red
            pixmap.fillCircle(16, 16, 15);
            Texture awayTexture = new Texture(pixmap);
            pixmap.dispose();
            texture.region = new TextureRegion(awayTexture);
        } else {
            texture.region = new TextureRegion(playerTexture);
        }
        entity.add(texture);
        
        // Player Component
        PlayerComponent player = engine.createComponent(PlayerComponent.class);
        player.isHomeTeam = isHomeTeam;
        player.speed = 5.0f;
        player.maxSpeed = 8.0f;
        entity.add(player);
        
        // Body Component (Box2D physics)
        BodyComponent bodyComp = engine.createComponent(BodyComponent.class);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = false; // Allow rotation
        
        Body body = physicsWorld.createBody(bodyDef);
        
        // Player shape (circle, ~0.3m radius)
        CircleShape shape = new CircleShape();
        shape.setRadius(0.3f);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.5f;
        fixtureDef.restitution = 0.1f; // Low bounce
        
        body.createFixture(fixtureDef);
        shape.dispose();
        
        bodyComp.body = body;
        entity.add(bodyComp);
        
        // Steering Component (LibGDX AI)
        SteeringComponent steering = engine.createComponent(SteeringComponent.class);
        steering.maxLinearSpeed = player.maxSpeed;
        steering.maxLinearAcceleration = 15.0f; // Fast acceleration for responsive movement
        steering.maxAngularSpeed = 5.0f;
        steering.maxAngularAcceleration = 10.0f;
        steering.arrivalTolerance = 0.5f;
        steering.decelerationRadius = 2.0f;
        
        // Create steerable adapter for LibGDX AI
        LocationAdapter steerable = new LocationAdapter();
        steerable.setPosition(transform.position);
        steerable.setOrientation(body.getAngle());
        steerable.setMaxLinearSpeed(steering.maxLinearSpeed);
        steerable.setMaxLinearAcceleration(steering.maxLinearAcceleration);
        steerable.setMaxAngularSpeed(steering.maxAngularSpeed);
        steerable.setMaxAngularAcceleration(steering.maxAngularAcceleration);
        steering.steerable = steerable;
        
        // Create target location for steering behavior
        LocationAdapter targetLocation = new LocationAdapter();
        targetLocation.setPosition(0, 0); // Will be updated by GoalSystem
        steering.targetLocation = targetLocation; // Store reference for GoalSystem to update
        
        // Create Seek behavior (will chase ball via GoalSystem)
        Seek<Vector2> seekBehavior = new Seek<Vector2>(steerable, targetLocation);
        steering.behavior = seekBehavior;
        entity.add(steering);
        
        // Goal Component (goal-based behavior)
        GoalComponent goal = engine.createComponent(GoalComponent.class);
        goal.type = GoalComponent.GoalType.CHASE_BALL; // Default: chase ball
        goal.isActive = true;
        goal.completionDistance = 1.0f;
        entity.add(goal);
        
        engine.addEntity(entity);
        
        return entity;
    }
    
    /**
     * Create ball entity with physics
     */
    private void createBall() {
        ball = engine.createEntity();
        
        // Transform Component
        TransformComponent transform = engine.createComponent(TransformComponent.class);
        transform.position.set(FIELD_WIDTH / 2f, FIELD_HEIGHT / 2f); // Center of field
        transform.scale.set(1.0f, 1.0f);
        ball.add(transform);
        
        // Texture Component
        TextureComponent texture = engine.createComponent(TextureComponent.class);
        texture.region = new TextureRegion(ballTexture);
        ball.add(texture);
        
        // Ball Component
        BallComponent ballComp = engine.createComponent(BallComponent.class);
        ballComp.radius = 0.11f; // Standard football radius
        ballComp.friction = 0.3f;
        ballComp.restitution = 0.5f;
        ballComp.linearDamping = 0.1f;
        ball.add(ballComp);
        
        // Body Component (Box2D physics)
        BodyComponent bodyComp = engine.createComponent(BodyComponent.class);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(FIELD_WIDTH / 2f, FIELD_HEIGHT / 2f);
        bodyDef.fixedRotation = false;
        
        Body body = physicsWorld.createBody(bodyDef);
        
        // Ball shape (circle)
        CircleShape shape = new CircleShape();
        shape.setRadius(ballComp.radius);
        
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0.5f; // Lighter than players
        fixtureDef.friction = ballComp.friction;
        fixtureDef.restitution = ballComp.restitution; // Bouncy
        
        body.createFixture(fixtureDef);
        shape.dispose();
        
        // Apply linear damping for air resistance
        body.setLinearDamping(ballComp.linearDamping);
        
        bodyComp.body = body;
        ball.add(bodyComp);
        
        engine.addEntity(ball);
    }
    
    @Override
    public void show() {
        // Set up input processor for ESC key
        Gdx.input.setInputProcessor(new InputProcessor() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    ((Futtoboru)game).changeScreen(Futtoboru.MATCH_ENGINE_DEBUG_SCREEN);
                    return true;
                }
                return false;
            }
            
            @Override public boolean keyUp(int keycode) { return false; }
            @Override public boolean keyTyped(char character) { return false; }
            @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
            @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
            @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
            @Override public boolean scrolled(float amountX, float amountY) { return false; }
        });
        Gdx.app.log("MatchEngine", "Match Engine Screen shown");
    }
    
    @Override
    public void render(float delta) {
        // Cap FPS to 60 and ensure minimum frame time
        float targetFPS = 60f;
        float targetDelta = 1f / targetFPS;
        if (delta > targetDelta) {
            delta = targetDelta;
        }
        
        // Ensure we don't have zero or negative delta
        if (delta <= 0) {
            delta = targetDelta;
        }
        
        // Clear screen - black background
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Draw field background
        batch.begin();
        batch.setColor(0.2f, 0.7f, 0.2f, 1f); // Green
        batch.draw(fieldTexture, 0, 0, FIELD_WIDTH, FIELD_HEIGHT);
        batch.setColor(1f, 1f, 1f, 1f); // Reset color
        batch.end();
        
        // Update ECS systems (physics and rendering)
        engine.update(delta);
        
        // Draw field markings (center line, center circle)
        drawFieldMarkings();
        
        // Draw UI overlay
        uiStage.act(delta);
        uiStage.draw();
    }
    
    /**
     * Draw field markings
     */
    private void drawFieldMarkings() {
        // This will be enhanced with proper field graphics later
        // For now, just render the entities
    }
    
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.update();
        uiStage.getViewport().update(width, height, true);
    }
    
    @Override
    public void pause() {
        // TODO: Pause match
    }
    
    @Override
    public void resume() {
        // TODO: Resume match
    }
    
    @Override
    public void hide() {
        // Return to debug screen on hide
        ((Futtoboru)game).changeScreen(Futtoboru.MATCH_ENGINE_DEBUG_SCREEN);
    }
    
    @Override
    public void dispose() {
        batch.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (ballTexture != null) ballTexture.dispose();
        if (fieldTexture != null) fieldTexture.dispose();
        if (physicsWorld != null) physicsWorld.dispose();
        if (uiStage != null) uiStage.dispose();
        if (font != null) font.dispose();
        Gdx.app.log("MatchEngine", "Match Engine disposed");
    }
}
