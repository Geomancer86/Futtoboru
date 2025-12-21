package com.rndmodgames.futtoboru.match.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * TextureComponent - Sprite/texture rendering data
 * Based on DarkBlade engine architecture
 */
public class TextureComponent implements Component, Poolable {
    public TextureRegion region;
    public float offsetX = 0f;
    public float offsetY = 0f;
    
    @Override
    public void reset() {
        region = null;
        offsetX = 0f;
        offsetY = 0f;
    }
}
