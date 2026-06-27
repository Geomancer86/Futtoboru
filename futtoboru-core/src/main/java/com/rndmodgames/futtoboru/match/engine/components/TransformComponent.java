package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * TransformComponent - Position, rotation, scale for entities
 * Based on DarkBlade engine architecture
 */
public class TransformComponent implements Component, Poolable {
    public Vector2 position = new Vector2();
    public Vector2 scale = new Vector2(1.0f, 1.0f);
    public float rotation = 0.0f;
    public boolean isHidden = false;
    
    @Override
    public void reset() {
        position.set(0, 0);
        scale.set(1.0f, 1.0f);
        rotation = 0.0f;
        isHidden = false;
    }
}
