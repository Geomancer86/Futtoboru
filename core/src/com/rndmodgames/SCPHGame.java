package com.rndmodgames;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * This is the main class of the SCPH Game Engine
 * 
 * @author Geomancer86
 */
public class SCPHGame extends ApplicationAdapter {
	
	SpriteBatch batch;
	Texture img;
	
	// random number generation
	static final Random random = new Random();
	
	// 3d camera demo
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
    public ModelInstance instance;
    public Environment environment;
    
    // camera input
    public CameraInputController camController;
    
    // framebuffer test
    FrameBuffer framebuffer;

    // Screen Camera
	OrthographicCamera engineCamera;
	
	/**
	 * Test mesh
	 * 
	 * https://stackoverflow.com/questions/41746812/libgdx-glgs-create-vertex-shader-which-let-objects-wave
	 * 
	 */
	public ShaderProgram shader;
	Mesh mesh;
	
	String vertexShader = "attribute vec4 a_position;    \n" + 
            "attribute vec4 a_color;\n" +
            "attribute vec2 a_texCoord0;\n" + 
            "uniform mat4 u_projTrans;\n" + 
            "varying vec4 v_color;" + 
            "varying vec2 v_texCoords;" + 
            "void main()                  \n" + 
            "{                            \n" + 
            "   v_color = vec4(1, 1, 1, 1); \n" + 
            "   v_texCoords = a_texCoord0; \n" + 
            "   gl_Position =  u_projTrans * a_position;  \n"      + 
            "}                            \n" ;
	
	String fragmentShader = "#ifdef GL_ES\n" +
              "precision mediump float;\n" + 
              "#endif\n" + 
              "varying vec4 v_color;\n" + 
              "varying vec2 v_texCoords;\n" + 
              "uniform sampler2D u_texture;\n" + 
              "void main()                                  \n" + 
              "{                                            \n" + 
              "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" +
              "}";
	
	@Override
	public void create () {
		
		// instance the spritebatch
		batch = new SpriteBatch();
		
		// instance the model batch
		modelBatch = new ModelBatch();
		
		// instantiate the shader
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		// This sets the Game/Engine Camera size to the Device Screen Size, so 1 meter is 16 pixels wide
		engineCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
		
        // create the framebuffer
        framebuffer = new FrameBuffer(Pixmap.Format.RGBA8888, 256, 256, false); // 
        
		// create the 3d camera
		cam = new PerspectiveCamera(60,
									Gdx.graphics.getWidth() * 10,
									Gdx.graphics.getHeight() * 10);
		
        cam.position.set(10f, 10f, 10f);
        
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        
        cam.update();
        
        // create the 3d model
        ModelBuilder modelBuilder = new ModelBuilder();
        
        model = modelBuilder.createBox(5f, 5f, 5f,
        		                       new Material(ColorAttribute.createDiffuse(Color.GREEN)),
        		                       Usage.Position | Usage.Normal);
        
        instance = new ModelInstance(model);
        
        // create the test mesh
        mesh = new Mesh(false, 4, 6,
				VertexAttribute.Position(),
				VertexAttribute.ColorUnpacked(),
				VertexAttribute.TexCoords(0),
				VertexAttribute.Normal());
        
        // initialize the camera input controller 
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render () {
		
		int x = random.nextInt(4) + 1;
		int y = random.nextInt(4) + 1;
		int z = random.nextInt(4) + 1;
		
		// update the mesh
		generateMesh(1, 1, 1, 1);
		
//		generateMesh(1f + random.nextFloat() - random.nextFloat(),
//				     1f + random.nextFloat() - random.nextFloat(),
//				     1f + random.nextFloat() - random.nextFloat(),
//				     1f);
		
		// update the camera
		camController.update();
		engineCamera.update();
		
		// start the framebuffer
		framebuffer.begin();
		
		// clear the framebuffer
		Gdx.gl.glViewport(0, 0, 256, 256);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // render the models
        modelBatch.begin(cam);
        modelBatch.render(instance, environment);
        modelBatch.end();
        
        // render mesh test
        mesh.render(shader, GL20.GL_TRIANGLES);

        // end the framebuffer
        framebuffer.end();
        
        // clear the screen
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        /**
         *  draw the framebuffer to screen
         */
        int WORLD_HEIGHT = Gdx.graphics.getHeight();
        int WORLD_WIDTH = Gdx.graphics.getWidth();

        batch.begin();

        batch.draw(framebuffer.getColorBufferTexture(), 0, WORLD_HEIGHT, WORLD_WIDTH, -WORLD_HEIGHT);
        
        batch.end();
	}
	
	@Override
	public void dispose () {
		framebuffer.dispose();
		modelBatch.dispose();
		model.dispose();
	}
	
	// test
	private void generateMesh(float xshift_mesh, float yshift_mesh, float zshift_mesh, float size_mesh) {

	    mesh.setVertices(new float[] {
	    	1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 0, 1,
	    	1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1,
	    	1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, -1,
	    	1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, -1
	    });

		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });
	}
}