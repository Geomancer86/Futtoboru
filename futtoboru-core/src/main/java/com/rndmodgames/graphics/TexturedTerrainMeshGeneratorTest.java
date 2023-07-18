package com.rndmodgames.graphics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.rndmodgames.darkblade.SpriteManager;

public class TexturedTerrainMeshGeneratorTest extends ApplicationAdapter {

	ShaderProgram shader;
	Mesh [] meshes;
	Matrix4 matrix = new Matrix4();
	
	// test textures
	Texture texture;
	TextureRegion grass;
	TextureRegion water;
	
	// Assets Manager
	AssetManager manager;
	SpriteManager spriteManager; 
	
	String vertexShader = "uniform float u_even; \n"
			+ "attribute vec4 a_position; \n"
			+ "attribute vec4 a_color; \n"
			+ "attribute vec2 a_texCoord;\n"
			+ "attribute vec4 a_normal;\n"
			+ "uniform mat4 u_worldView; \n"
			+ "varying vec4 v_color; \n"
			+ "varying vec2 v_texCoords; \n"
			+ "void main() \n"
			+ "{ \n"
			+ "   v_color = a_color; \n"
			+ "   v_texCoords = a_texCoord; \n"
			+ "   gl_Position =  u_worldView * a_position; \n"
			+ "} \n";

	String fragmentShader = "#ifdef GL_ES \n"
			  + "precision mediump float; \n"
			  + "#endif \n"
			  + "varying vec4 v_color; \n"
			  + "varying vec2 v_texCoords; \n"
			  + "uniform sampler2D u_texture; \n"
			  + "void main() \n"
			  + "{ \n"
			  + "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords) ; \n"
			  + "}";

    // camera input
	public PerspectiveCamera cam;
    public CameraInputController camController;
	
	@Override
	public void create () {
		
		// Create Asset Manager
		manager = new AssetManager();
		
		// Create Sprite Manager
		spriteManager = new SpriteManager(manager);
		
		// Load the spritesheet
		manager.load("spritesheet.txt", TextureAtlas.class);
		
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		// test shader compilation integrity
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}

		// Awaits for anything else to reload
		manager.finishLoading();
		
		// Load Sprite Cache
		spriteManager.loadTextureAtlas();
		
		// texture loading
		grass = spriteManager.getSprite("grass");
		water = spriteManager.getSprite("water");
		
		texture = grass.getTexture();
		
		// camera & input
		cam = new PerspectiveCamera(60,
									Gdx.graphics.getWidth() * 10,
									Gdx.graphics.getHeight() * 10);
		
        cam.position.set(10f, 10f, 10f);
        
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        
        cam.update();
		
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
        // load meshes
        loadMeshes();
	}
	
	public void loadMeshes() {
		// meshes array
		meshes = new Mesh[4];
		
		// first mesh
		Mesh mesh = new Mesh(true, 10000, 10000, 
			    			 new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
			    			 new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
			    			 new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			    			 new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));

		mesh.setVertices(generateVertices(3, 3, 0, 0, false));
	    mesh.setIndices(generateIndices(3, 3));
	    
	    // second mesh
	    Mesh mesh2 = new Mesh(true, 10000, 10000, 
				   			  new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				   			  new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				   			  new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
				   			  new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));

	    mesh2.setVertices(generateVertices(3, 3, 2, 2, true));
	    mesh2.setIndices(generateIndices(3, 3));
	    
	    // third mesh
	    Mesh mesh3 = new Mesh(true, 10000, 10000, 
   			 new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
   			 new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
   			 new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

	    mesh3.setVertices(generateVertices(24, 24, 0, 23, false));
	    mesh3.setIndices(generateIndices(24, 24));
	    
	    // fourth mesh
	    Mesh mesh4 = new Mesh(true, 10000, 10000, 
   			 new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
   			 new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
   			 new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

	    mesh4.setVertices(generateVertices(24, 24, 23, 0, false));
	    mesh4.setIndices(generateIndices(24, 24));
	    
	    // fill meshes array
	    meshes[0] = mesh;
	    meshes[1] = mesh2;
//	    meshes[2] = mesh3;
//	    meshes[3] = mesh4;
	}
	
	@Override
	public void render() {
		
		// update the camera
		camController.update();
		
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			
		// required to render
		shader.bind();
		
		// Wrap mode allows to fill the mesh
		texture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		texture.bind();
		
		shader.setUniformMatrix("u_worldView", camController.camera.combined);
		shader.setUniformi("u_texture", 0);
		
		// render all meshes
		for (Mesh mesh : meshes) {
			
			if (mesh != null) {
				mesh.render(shader, GL20.GL_TRIANGLES);
			}
		}
	}

	/**
	 * Test generating vertices
	 */
	private float [] generateVertices(int width, int height, int xOffset, int yOffset, boolean isWater) {
		
	    int index = 0;
		float vertices [] = new float[width * height * 12];

	    for(int i = 0; i < width; i ++) {
	        for(int j = 0; j < height; j++) {
	        	
	        	float maxAltitude = 1.2f;
	        	float altitude = MathUtils.random(0.8f, maxAltitude);
	        	
	            //vertex coordinates
				vertices[index] = i + xOffset;
				vertices[index + 1] = altitude;
				vertices[index + 2] = j + yOffset;

				// normal
				vertices[index + 3] = 0;
				vertices[index + 4] = 1;
				vertices[index + 5] = 0;

				// random colors!!!
				vertices[index + 6] = altitude / maxAltitude;
				vertices[index + 7] = altitude / maxAltitude;
				vertices[index + 8] = altitude / maxAltitude;
				vertices[index + 9] = 1;
				
				// texture coordinates
//				vertices[index + 10] = i;
//				vertices[index + 11] = j;
				
				if (isWater) {
					if (i % 2 == 0) {
						vertices[index + 10] = water.getU2();
						
					} else {
						vertices[index + 10] = water.getU();
					}
					
					if (j % 2 == 0) {
						vertices[index + 11] = water.getV();
						
					} else {
						vertices[index + 11] = water.getV2();
					}
				} else {
					if (i % 2 == 0) {
						vertices[index + 10] = grass.getU2();
						
					} else {
						vertices[index + 10] = grass.getU();
					}
					
					if (j % 2 == 0) {
						vertices[index + 11] = grass.getV();
						
					} else {
						vertices[index + 11] = grass.getV2();
					}
				}

				// remember to increment by the size of the vertices array
	            index += 12;
	        }
	    }
	    
	    return vertices;
	}
	
	/**
	 * Test generating indices
	 */
	private short[] generateIndices(int width, int height) {
		
		int index = 0;
		short indices[] = new short[(width - 1) * (height - 1) * 3 * 2];
		
		for (int i = 0; i < width - 1; i++) {
			for (int j = 0; j < height - 1; j++) {
				
				indices[index] = (short) ((j * height) + i);
				indices[index + 1] = (short) ((j * height) + i + 1);
				indices[index + 2] = (short) (((j + 1) * height) + i);

				indices[index + 3] = (short) (((j + 1) * height) + i);
				indices[index + 4] = (short) ((j * height) + i + 1);
				indices[index + 5] = (short) (((j + 1) * height) + i + 1);
				index += 6;
				
			}
		}
		
		return indices;
	}
	
	@Override
	public void dispose () {
		
		for (Mesh mesh : meshes) {
			mesh.dispose();
		}
		
		shader.dispose();
		texture.dispose();
	}
}