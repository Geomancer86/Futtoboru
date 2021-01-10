package com.rndmodgames.graphics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.rndmodgames.tools.InfiniteScrollMeshGenerator;

/**
 * Infinite scrolling background mesh
 * 
 * @author Geomancer86
 */
public class InfiniteScrollingFieldTest extends ApplicationAdapter {

	// main shader
	ShaderProgram shader;
	
	// field meshes array, keeps track of off-screen and on-screen meshes
	Mesh [] meshes;
	
	// camera matrix
	Matrix4 matrix = new Matrix4();

	// test textures
	Texture grass0;
	Texture grass1;
	Texture grass2;
	Texture grass3;
	
    // camera input
	PerspectiveCamera cam;
    CameraInputController camController;
    
    // gameplay elements
    boolean fieldUpdated = false;
    float distance;
    
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
    
    @Override
	public void create () {
		
    	// infinite scrolling field shader
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		// test shader compilation integrity
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}

		// grass textures
		grass0 = new Texture(Gdx.files.internal("textures/grass_texture_0.png"));
		grass0.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		
		grass1 = new Texture(Gdx.files.internal("textures/grass_texture_1.png"));
		grass1.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		
		grass3 = new Texture(Gdx.files.internal("textures/grass_texture_3.png"));
		grass3.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		
		// camera & input
		cam = new PerspectiveCamera(60,
									Gdx.graphics.getWidth() * 10,
									Gdx.graphics.getHeight() * 10);
		
		// Camera 1: 10, 4, 64
		float cameraX = 10f;
		float cameraY = 4f;
		float cameraZ = 128f;
		
        cam.position.set(cameraX, cameraY, cameraZ);
        
		cam.lookAt(cameraX, -80, 0);
		
        cam.near = 0f;
        cam.far = 300f;
        
        cam.update();
		
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        
        // generate and load meshes
        meshes = InfiniteScrollMeshGenerator.generateMeshes(128, 0);
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

		shader.setUniformMatrix("u_worldView", camController.camera.combined);
		shader.setUniformi("u_texture", 0);
		
		int meshCount = 0;
		
		// render all meshes
		for (Mesh mesh : meshes) {
			
			if (mesh != null) {

				if (meshCount % 2 == 0) {
					grass0.bind();
				} else {
					grass1.bind();
				}
				
				mesh.render(shader, GL20.GL_TRIANGLES);
				
				meshCount++;
			}
		}
		
		// reset field updated flag
		fieldUpdated = false;
		
		// scroll the camera a little this frame
		float scrollX = 0f;
		float scrollY = 0f;
		float scrollZ = -0.2f;
		
		// translate & update
		cam.translate(scrollX, scrollY, scrollZ);
		cam.update();
		
		// update distance traveled
		distance += Math.abs(scrollZ);

		// update the infinite scrolling meshes
		if (((int) distance % 2 == 0) && !fieldUpdated) {

			for (Mesh mesh : meshes) {
				mesh.dispose();
			}
			
			// TODO: avoid updating many times per frame as once is enough
			meshes = InfiniteScrollMeshGenerator.generateMeshes(128, (int) distance);
			
			// reset field updated flag
			fieldUpdated = true;
		}

		// print camera looking position
//		System.out.println("camera.x: " + cam.position.x + ", y: " + cam.position.y + ", z: " + cam.position.z);
	}
}
