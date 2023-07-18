package com.rndmodgames.graphics;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;

public class TerrainMeshGeneratorTest extends ApplicationAdapter {

	ShaderProgram shader;
	Mesh mesh;
	Matrix4 matrix = new Matrix4();
	
	String vertexShader = "uniform float u_even; \n"
						+ "attribute vec4 a_position; \n"
						+ "attribute vec4 a_color; \n"
						+ "attribute vec4 a_normal;\n"
						+ "uniform mat4 u_worldView; \n"
						+ "varying vec4 v_color; \n"
						+ "varying vec2 v_texCoords; \n"
						+ "void main() \n"
						+ "{ \n"
						+ "   v_color = a_color; \n"
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
						  + "  gl_FragColor = v_color; \n"
						  + "}";

    // camera input
	public PerspectiveCamera cam;
    public CameraInputController camController;
	
	@Override
	public void create () {
		
		// colored mesh
		mesh = new Mesh(true, 10000, 10000, 
			    new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
				new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
				new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

		mesh.setVertices(generateVertices(14, 14));
	    mesh.setIndices(generateIndices(14, 14));
		
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		// test shader compilation integrity
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}

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
	}
	
	@Override
	public void render() {
		
		// update the camera
		camController.update();
		
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		shader.bind();
		
		shader.setUniformMatrix("u_worldView", camController.camera.combined);
		
		mesh.render( shader, GL20.GL_TRIANGLES );

	}

	/**
	 * Test generating vertices
	 */
	private float[] generateVertices(int width, int height) {
		
	    int index = 0;
		float vertices[] = new float[width * height * 10];

	    for(int i = 0; i < width; i ++) {
	        for(int j = 0; j < height; j++) {
	        	
	            //vertex coordinates
				vertices[index] = i;
				vertices[index + 1] = 0;
				vertices[index + 2] = j;

				// normal
				vertices[index + 3] = 0;
				vertices[index + 4] = 1;
				vertices[index + 5] = 0;

				// random colors!!!
				vertices[index + 6] = MathUtils.random(0.3f, 0.99f);
				vertices[index + 7] = MathUtils.random(0.3f, 0.99f);
				vertices[index + 8] = MathUtils.random(0.3f, 0.99f);
				vertices[index + 9] = 1;

	            index += 10;
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
		
		mesh.dispose();
		shader.dispose();
	}
}