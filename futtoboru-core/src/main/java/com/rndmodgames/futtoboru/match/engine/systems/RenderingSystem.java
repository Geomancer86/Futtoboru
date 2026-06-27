package com.rndmodgames.futtoboru.match.engine.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.SortedIteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.rndmodgames.futtoboru.match.engine.components.TextureComponent;
import com.rndmodgames.futtoboru.match.engine.components.TransformComponent;

import java.util.Comparator;

/**
 * RenderingSystem - Handles 2D sprite rendering with Z-sorting
 * Based on DarkBlade engine architecture
 */
public class RenderingSystem extends SortedIteratingSystem {
    
    public static final float PPM = 16.0f; // Pixels Per Meter
    
    private ComponentMapper<TransformComponent> transformMapper;
    private ComponentMapper<TextureComponent> textureMapper;
    
    private SpriteBatch batch;
    private OrthographicCamera camera;
    
    public RenderingSystem(SpriteBatch batch, OrthographicCamera camera) {
        super(Family.all(TransformComponent.class, TextureComponent.class).get(), 
              new ZComparator());
        this.batch = batch;
        this.camera = camera;
        this.transformMapper = ComponentMapper.getFor(TransformComponent.class);
        this.textureMapper = ComponentMapper.getFor(TextureComponent.class);
    }
    
    @Override
    public void update(float deltaTime) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        
        batch.begin();
        super.update(deltaTime);
        batch.end();
    }
    
    @Override
    protected void processEntity(Entity entity, float deltaTime) {
        TransformComponent transform = transformMapper.get(entity);
        TextureComponent texture = textureMapper.get(entity);
        
        if (texture.region != null && !transform.isHidden) {
            float width = texture.region.getRegionWidth() / PPM;
            float height = texture.region.getRegionHeight() / PPM;
            float originX = width / 2f;
            float originY = height / 2f;
            
            batch.draw(texture.region,
                      transform.position.x - originX + texture.offsetX / PPM,
                      transform.position.y - originY + texture.offsetY / PPM,
                      originX, originY,
                      width, height,
                      transform.scale.x, transform.scale.y,
                      transform.rotation);
        }
    }
    
    /**
     * Z-Comparator for rendering order
     */
    private static class ZComparator implements Comparator<Entity> {
        private ComponentMapper<TransformComponent> transformMapper = 
            ComponentMapper.getFor(TransformComponent.class);
        
        @Override
        public int compare(Entity e1, Entity e2) {
            TransformComponent t1 = transformMapper.get(e1);
            TransformComponent t2 = transformMapper.get(e2);
            // Lower Y position renders on top (isometric-like)
            return Float.compare(t1.position.y, t2.position.y);
        }
    }
}
