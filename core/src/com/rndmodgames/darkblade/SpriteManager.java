package com.rndmodgames.darkblade;

import java.util.HashMap;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Sprite Manager v1
 * 
 * @author Geomancer86
 */
public class SpriteManager {

	private AssetManager manager;
	private TextureAtlas atlas;
	
	private boolean DEBUG = true;
	
	private HashMap<String, TextureRegion> singleCacheSprite = new HashMap<>();
	
	public SpriteManager(AssetManager manager) {
		
		this.manager = manager;
	}
	
	public void loadTextureAtlas() {
		
		this.atlas = manager.get("spritesheet.txt", TextureAtlas.class);
	}
	
	/**
	 * Gets Sprite From Cache or Create and Store on Cache
	 */
	public TextureRegion getSprite(String sprite) {
		
		if (DEBUG) {
			System.out.println("Getting " + sprite);
		}
		
		// RETURN CACHED
		if (singleCacheSprite.containsKey(sprite)) {
			
			if (DEBUG) {
				System.out.println("Getting " + sprite + " sprite from Sprite Cache!");
			}
			
			return singleCacheSprite.get(sprite); 
		}
		
		TextureRegion region = atlas.findRegion(sprite);
		
		if (DEBUG) {
			System.out.println("Getting " + sprite + " sprite from SpriteBatch!");
		}
		
		// CACHE
		singleCacheSprite.put(sprite, region);
		
		// RETURN FRESH
		return region;
	}
}