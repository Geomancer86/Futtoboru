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
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.rndmodgames.graphics.BaseG3dTest;
import com.rndmodgames.graphics.HeightField;

/**
 * Prologue Height Map V2 Loader Demo
 * 
 * 
 * @author Geomancer86
 */
public class PrologueHeightMapV2 extends BaseG3dTest {

	ShaderProgram shader;
	
	//
	HeightField field;
	Renderable ground;
	Environment environment;
	boolean morph = true;
	Texture texture;
	
	String vertexShader = "uniform float u_even; \n"
			+ "attribute vec4 a_position; \n"
			+ "attribute vec4 a_color; \n"
			+ "attribute vec4 a_normal;\n"
			+ "attribute vec4 a_texCoord;\n"
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
	public void create() {
		
		super.create();
		
		shader = new ShaderProgram(vertexShader, fragmentShader);
		
		// test shader compilation integrity
		if (shader.isCompiled() == false) {
			Gdx.app.log("ShaderTest", shader.getLog());
			Gdx.app.exit();
		}
		
		/**
		 * Heightmap
		 */
		environment = new Environment();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1.f));
		environment.set(new ColorAttribute(ColorAttribute.Fog, 0.4f, 0.1f, 0.1f, 0.2f));
		
		environment.add(new DirectionalLight().set(1.0f, 0.0f, 1.0f, -10f, -10.8f, -10.2f));

		cam.position.set(3f, 10f, 3f);
		cam.lookAt(0, 0, 0);
		cam.near = 0.1f;
		cam.far = 25000f;
		cam.update();

		// /data/prologue/terrain/grass.jpg
//		texture = new Texture(Gdx.files.internal("data/prologue/terrain/grass.jpg"));
		texture = new Texture(Gdx.files.internal("textures/grass_texture_0.png"));

		int w = 5, h = 5;
		
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmap_v2.png"));
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/hm4.png"));
		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/hm3.jpg"));
//		Pixmap data = new Pixmap(Gdx.files.internal("data/prologue/heightmaps/heightmap180.png"));

		/**
		 * new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
		   new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
		   new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
		   new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE)
		 */
		field = new HeightField(true, data, true, Usage.Position | Usage.Normal | Usage.ColorUnpacked | Usage.TextureCoordinates);
		data.dispose();
		
		/**
		 * 1 Unit == 1 Meter
		 * 
		 * This allows to enlarge or shrink the final field mesh
		 */
		field.corner00.set(-10f * h, 0, -10f * w);
		field.corner10.set(10f * h, 0, -10f * w);
		field.corner01.set(-10f * h, 0, 10f * w);
		field.corner11.set(10f * h, 0, 10f * w);
		
		/**
		 * This colors the Heightmap on the 4 corners
		 */
//		field.color00.set(1, 1, 1, 1);
//		field.color01.set(1, 1, 1, 1);
//		field.color10.set(1, 1, 1, 1);
//		field.color11.set(1, 1, 1, 1);
		
		/**
		 *  This represents how much will the axis will change by the heightmap
		 */
		field.magnitude.set(0f, 15f, 0f);
		
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
	protected void render(ModelBatch batch, Array<ModelInstance> instances) {
		
		batch.render(instances);
		batch.render(ground);
		
	}
}
