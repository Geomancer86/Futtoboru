package com.rndmodgames.scenes.prologue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.utils.Array;
import com.rndmodgames.graphics.BaseG3dTest;
import com.rndmodgames.graphics.HeightField;

/**
 * Innsmouth Height Map Scale v1
 * 
 * 1.0 mm    =   1.8 m       (wooden pier as scale reference)
 * 1.0 inch  = 400.0 feet (old american grid system reference chart)
 * 2.45 cm = 121.9 m
 * 
 * Ship on Pier: 12mm = 21.6 meters
 * 
 * ---
 * Innsmouth Height Map Scale v2 - Lyndonia (1920 ship) 70.104 m lenght
 * 
 * Map Perimeter: 25 cm x 18.5cm 
 * 
 * Ship on Pier: 12.0mm = 70.1 meters = [5.84 m per 1.0 mm]
 *
 * Map Perimeter Scaled: 1460 x 1080 m
 * 
 * Rounding Down to HeightMap Chunk at Different Sizes & Resolutions
 * 
 * 1 pixel 1 meter
 * 
 * 64px chunks
 * 
 * 22.81 x 16.87 == 23 x 17 ~= 24 x 18
 * 
 * 24 x 18 = 1536x1152
 * 
 * ---
 * 8-bit == 256 height levels only
 * 
 * 
 * ---
 * Manuxet River Height Difference Estimation:
 * 
 * Sources:
 * 	- number of waterfalls from original black and white map
 *  - height eyebaled from isometric map drawing
 * 
 * 3 Waterfalls
 * 	1st = 8 meters
 *  2nd = 5 meters
 *  3rd = 11 meters
 *  
 *  - width:
 *  	- largest at the estuary region at the west: 15mm == 88.6 meters
 *  	- average width in the town                :  5mm == 29.2 meters
 *      - shortest width in the town               :  2mm == 11.6 meters
 *      - largest width in the town                :  7mm == 40.9 meters
 *      
 *   - Cape Opening: 11mm ==  64.2 meters
 *   	- Width    : 40mm == 233.6 meters
 *      - Height   : 45mm == 262.8 meters
 *      
 *   - Delta Opening: 45mm == 262.8 meters
 *   
 * ---
 * Lighthouse Reference: Cape Ann Light Station [https://en.wikipedia.org/wiki/Cape_Ann_Light_Station]
 * 
 * Height                : 38 m
 * Focal Height          : 51 m
 * Range	             : 17 nautical miles (31 km; 20 mi)
 * Fog signal            : HORN: 2 every 60s
 * Area	50 acres         : (20 ha)
 * Built                 : 1860
 * Year first constructed: 1771
 * Year first lit        : 1861 (current structure)
 * Automated             : 1988
 * Deactivated           : 1932-1988
 * Foundation            : Granite (surface rock)
 * Construction	         : Cut granite
 * Tower shape           : Conical
 * Markings / pattern    : Natural, unpainted granite
 * 
 * ---
 * First thought: Newburyport was the inspiration for Innsmouth 
 * Murray thinks that Lovecraft actually based Innsmouth on Gloucester, Massachusetts [Cape Ann]
 *
 * ---
 * Tourist's Guide to Innsmouth
 * 
 * https://www.cthulhufiles.com/innsmth.htm
 *
 * ---
 * 
 * 254 mm = 121.9 m
 *   1 mm =   0.5 m
 * 
 * ---
 * City Block Scale Reference:
 * 
 * Portland Block Size (2.54cm == 400')
 * 	- 1.5 cm = 236 feet == 71 m
 * 
 * 
 * @author Geomancer86
 */
public class PrologueHeightMapV1 extends BaseG3dTest {
	
	HeightField field;
	Renderable ground;
	Environment environment;
	boolean morph = true;
	Texture texture;

	@Override
	public void create () {
		
		super.create();
		
		environment = new Environment();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, 0.1f, 0.1f, 0.1f, 0.2f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		cam.position.set(30f, 100f, 30f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 25000f;
		cam.update();

		// /data/prologue/terrain/grass.jpg
//		texture = new Texture(Gdx.files.internal("data/prologue/terrain/grass.jpg"));
		texture = new Texture(Gdx.files.internal("textures/grass_texture_0.png"));
		
		texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

		int w = 25, h = 25;
		
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmap_v2.png"));
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/hm4.png"));
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/hm3.jpg"));
		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/heightmap180.png"));
		
		field = new HeightField(true, data, true, Usage.Position | Usage.Normal | Usage.ColorUnpacked | Usage.TextureCoordinates);
		data.dispose();
		
		/**
		 * 1 Unit == 1 Meter
		 */
		field.corner00.set(-10f * h, 0, -10f * w);
		field.corner10.set(10f * h, 0, -10f * w);
		field.corner01.set(-10f * h, 0, 10f * w);
		field.corner11.set(10f * h, 0, 10f * w);
		
		/**
		 * This ties the Heightmap coordinates to the 4 corners
		 */
		field.color00.set(1, 1, 1, 1);
		field.color01.set(1, 1, 1, 1);
		field.color10.set(1, 1, 1, 1);
		field.color11.set(1, 1, 1, 1);
		
		/**
		 *  This represents how much will the axis will change by the heightmap
		 */
		field.magnitude.set(0f, 60f, 0f);
		
		field.update();

		ground = new Renderable();
		
		/**
		 * This generates the HeightMap for the Ground
		 */
		ground.environment = environment;
		ground.meshPart.mesh = field.mesh;
		ground.meshPart.primitiveType = GL20.GL_TRIANGLES;
		ground.meshPart.offset = 0;
		ground.meshPart.size = field.mesh.getNumIndices();
		ground.meshPart.update();
		
		/**
		 * This creates the Ground Material based on the Texture
		 */
		ground.material = new Material(TextureAttribute.createDiffuse(texture));
	}

	@Override
	protected void render (ModelBatch batch, Array<ModelInstance> instances) {
		
		batch.render(instances);
		batch.render(ground);
		
		
//		System.out.println("FPS: " + Gdx.graphics.getFramesPerSecond());
	}
	
	@Override
	public void dispose () {
		
		super.dispose();
		texture.dispose();
		field.dispose();
		
	}
}