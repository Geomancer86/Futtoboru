package com.rndmodgames.graphics;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class MeshShaderTest extends ApplicationAdapter {
	
	ShaderProgram shader;
	Mesh mesh, meshCustomVA;
	Texture texture;
	Matrix4 matrix = new Matrix4();

    // camera input
	public PerspectiveCamera cam;
    public CameraInputController camController;
	
	@Override
	public void create() {
		
		String vertexShader = "uniform float u_even; \n"
							+ "attribute vec4 a_position; \n"
							+ "attribute vec4 a_color; \n"
							+ "attribute vec2 a_texCoord0;\n"
							+ "attribute vec4 a_normal;\n"
							+ "uniform mat4 u_worldView; \n"
							+ "varying vec4 v_color; \n"
							+ "varying vec2 v_texCoords; \n"
							+ "void main()                  \n"
							+ "{                            \n"
							+ "   v_color = vec4(1, 1, 1, 1); \n"
							+ "   v_texCoords = a_texCoord0; \n"
							+ "   gl_Position =  u_worldView * a_position;  \n"
							+ "}                            \n";
		
		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n" + "#endif\n" + "varying vec4 v_color;\n"
				+ "varying vec2 v_texCoords;\n" + "uniform sampler2D u_texture;\n"
				+ "void main()                                  \n" + "{                                            \n"
				+ "  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n" + "}";

		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}

		mesh = new Mesh(true, 4, 6, VertexAttribute.Position(), VertexAttribute.ColorUnpacked(),
				VertexAttribute.TexCoords(0));
		
		mesh.setVertices(new float[] { -0.5f, -0.5f, 0, 1, 1, 1, 1, 0, 1, 0.5f, -0.5f, 0, 1, 1, 1, 1, 1, 1, 0.5f, 0.5f,
				0, 1, 1, 1, 1, 1, 0, -0.5f, 0.5f, 0, 1, 1, 1, 1, 0, 0 });
		
		mesh.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

		// Mesh with texCoords wearing a pair of shorts. :)
		meshCustomVA = new Mesh(true, 4, 6,
								VertexAttribute.Position(),
								VertexAttribute.ColorPacked(),
								new VertexAttribute(Usage.TextureCoordinates, 2, GL20.GL_UNSIGNED_SHORT, true,
										ShaderProgram.TEXCOORD_ATTRIBUTE + "0", 0));
		
		meshCustomVA.setVertices(new float[] { -0.5f, -0.5f, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(0, 1), 0.5f,
				-0.5f, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(1, 1), 0.5f, 0.5f, 0, Color.WHITE_FLOAT_BITS,
				toSingleFloat(1, 0), -0.5f, 0.5f, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(0, 0) });
		
		meshCustomVA.setIndices(new short[] { 0, 1, 2, 2, 3, 0 });

		texture = new Texture(Gdx.files.internal("data/bobrgb888-32x32.png"));
		
        // initialize the camera input controller
		// create the 3d camera
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

	private static float toSingleFloat(float u, float v) {
		int vu = ((int) (v * 0xffff) << 16) | (int) (u * 0xffff);
		return NumberUtils.intToFloatColor(vu); // v will lose some precision due to masking
	}

	Vector3 axis = new Vector3(0, 0, 1);
	float angle = 0;

	@Override
	public void render() {
		
		// update the camera
		camController.update();
		
		angle += Gdx.graphics.getDeltaTime() * 45;
		matrix.setToRotation(axis, angle);

		if (Gdx.input.isKeyPressed(Keys.SPACE)) {

			randomMesh();
		}
		
		Mesh meshToDraw = Gdx.input.isKeyPressed(Keys.SPACE) ? meshCustomVA : mesh;
		
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		texture.bind();
		shader.bind();

		shader.setUniformMatrix("u_worldView", camController.camera.combined);
		shader.setUniformi("u_texture", 0);
		
		meshToDraw.render(shader, GL20.GL_TRIANGLES);
	}

	public static Random random = new Random();
	
	public void randomMesh() {
		
		float x = (random.nextFloat() - random.nextFloat()) / 50;
		float y = (random.nextFloat() - random.nextFloat()) / 50;
		
		float x2 = (random.nextFloat() - random.nextFloat()) / 50;
		float y2 = (random.nextFloat() - random.nextFloat()) / 50;
		
		float x3 = (random.nextFloat() - random.nextFloat()) / 50;
		float y3 = (random.nextFloat() - random.nextFloat()) / 50;
		
		float x4 = (random.nextFloat() - random.nextFloat()) / 50;
		float y4 = (random.nextFloat() - random.nextFloat()) / 50;
		
		meshCustomVA.setVertices(new float[] { -0.5f + x, -0.5f + y, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(0, 1),
												0.5f + x2, -0.5f + y2, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(1, 1),
												0.5f + x3, 0.5f + y3, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(1, 0),
												-0.5f + x4, 0.5f + y4, 0, Color.WHITE_FLOAT_BITS, toSingleFloat(0, 0)});
		
	}
	
	@Override
	public void dispose() {
		mesh.dispose();
		texture.dispose();
		shader.dispose();
	}
}