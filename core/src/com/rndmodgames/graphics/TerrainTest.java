package com.rndmodgames.graphics;

import java.util.Random;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.TimeUtils;

public class TerrainTest extends ApplicationAdapter {

	TerrainChunk chunk;
	Mesh mesh;
	PerspectiveCamera camera;
	Vector3 intersection = new Vector3();
	boolean intersected = false;
	long lastTime = TimeUtils.nanoTime();
	private Random rand = new Random();
	private float[] heightmap;

	// shader
	ShaderProgram shader;
	
    // camera input
	public PerspectiveCamera cam;
    public CameraInputController camController;
	
	@Override
	public void create () {
		
//		renderer = new ImmediateModeRenderer20(0, intersected, intersected, 0, null);

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
		
		chunk = new TerrainChunk(32, 32, 4);
		this.heightmap = chunk.heightMap;

		int len = chunk.vertices.length;
		
		for (int i = 3; i < len; i += 4) {
	
			chunk.vertices[i] = Color.toFloatBits(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255), 255);
		}
		
		mesh = new Mesh(true, chunk.vertices.length / 3, chunk.indices.length, new VertexAttribute(VertexAttributes.Usage.Position,
			3, "a_position"), new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color"));

		mesh.setVertices(chunk.vertices);
		mesh.setIndices(chunk.indices);

		// initialize the camera input controller
		// create the 3d camera
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(0, 5, 5);
		camera.direction.set(0, 0, 0).sub(camera.position).nor();
		camera.near = 0.005f;
		camera.far = 300;
		
		camera.update();

		// initialize camera input
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
	}

	@Override
	public void render () {
		
		Gdx.gl20.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
		Gdx.gl20.glClearColor(0.2f, 0.2f, 0.2f, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		camera.update();
		
		shader.bind();

		shader.setUniformMatrix("u_worldView", camController.camera.combined);
		shader.setUniformi("u_texture", 0);
		
		mesh.render(shader, GL20.GL_TRIANGLES);

//		handleInput(Gdx.input, Gdx.graphics.getDeltaTime());

		if (TimeUtils.nanoTime() - lastTime > 1000000000) {
			Gdx.app.log("TerrainTest", "fps: " + Gdx.graphics.getFramesPerSecond());
			lastTime = TimeUtils.nanoTime();
		}
	}

	private void handleInput (Input input, float delta) {
		
		if (input.isTouched()) {
			// simply modifying speed  
			delta = delta+0.0485f;
			
			Ray ray = camera.getPickRay(input.getX(), input.getY());
			if (Intersector.intersectRayTriangles(ray, chunk.vertices, chunk.indices, 4, intersection)) {
				intersected = true;
			}
			// camera mouse look
			// find vector 90? to me
			Vector3 v90 = camera.direction.cpy();
			Quaternion q = new Quaternion(camera.up, 90);
			q.transform(v90);

			// go to plane x,z
			v90.y = 0;

			// set rotation up/down
			Quaternion qUpDown = new Quaternion(v90, Gdx.input.getDeltaY());

			// set rotation left/right
			Quaternion qLeftRight = new Quaternion(camera.up, -Gdx.input.getDeltaX());

			// apply the rotations
			qUpDown.transform(camera.direction);
			qLeftRight.transform(camera.direction);

		} else {
			intersected = false;
		}

//		if (input.isKeyPressed(Keys.W)) {
//			Vector3 forward = new Vector3().set(camera.direction).mul(delta);
//			camera.position.add(forward);	
//		}
//		if (input.isKeyPressed(Keys.S)) {
//			Vector3 backward = new Vector3().set(camera.direction).mul(delta);
//			camera.position.sub(backward);		
//		}
//		
//		if (input.isKeyPressed(Keys.A)) {
//			Vector3 left = new Vector3().set(camera.direction.cpy().crs(camera.up).nor()).mul(delta);
//			camera.position.sub(left);					
//		}
//		if (input.isKeyPressed(Keys.D)) {
//			Vector3 right = new Vector3().set(camera.direction.cpy().crs(camera.up).nor()).mul(delta);
//			camera.position.add(right);					
//		}
		
		
//		Vector3 forward = new Vector3().set(camera.direction).mul(delta);
//		
//		camera.position.add(forward);			
//		int nextId = ( MathUtils.floor(camera.position.x) * 33) + MathUtils.floor(camera.position.z);
//		// are we still on our current chunk ? - maybe switching to another chunk 
//		if( nextId > heightmap.length-1 || nextId < 0  ){
//			camera.position.y = 5;			// index not in bounds
//		}else{
//			camera.position.y = this.heightmap[nextId] * 4 + 1; // current pos found			
//		}
		
	}

	final static class TerrainChunk {
		public final float[] heightMap;
		public final short width;
		public final short height;
		public final float[] vertices;
		public final short[] indices;
		public final int vertexSize;
		private Random rand = new Random();

		public TerrainChunk (int width, int height, int vertexSize) {
			if ((width + 1) * (height + 1) > Short.MAX_VALUE)
				throw new IllegalArgumentException("Chunk size too big, (width + 1)*(height+1) must be <= 32767");

			this.heightMap = new float[(width + 1) * (height + 1)];
			this.width = (short)width;
			this.height = (short)height;
			this.vertices = new float[heightMap.length * vertexSize];
			this.indices = new short[width * height * 6];
			this.vertexSize = vertexSize;
			
			buildHeightmap("data/heightmap.png");
			buildIndices();
			buildVertices();
		}

		public void buildHeightmap (String pathToHeightMap) {
			/** get the heightmap from filesystem... should match width and height from current chunk..otherwise its just flat on
			 * missing pixel but no error thrown */

			FileHandle handle = Gdx.files.internal(pathToHeightMap);
			Pixmap heightmapImage = new Pixmap(handle);
			Color color = new Color();
			int idh = 0; // index to iterate

			for (int x = 0; x < this.width + 1; x++) {
				for (int y = 0; y < this.height + 1; y++) {
					// we need seperated channels..
					Color.rgba8888ToColor(color, heightmapImage.getPixel(x, y)); // better way to get pixel ?
					// pick whatever channel..we do have a b/w map
					this.heightMap[idh++] = color.r;
				}
			}
		}

		public void buildVertices () {
			int heightPitch = height + 1;
			int widthPitch = width + 1;

			int idx = 0;
			int hIdx = 0;
			int inc = vertexSize - 3;
			int strength = 4; // multiplier for heightmap			

			for (int z = 0; z < heightPitch; z++) {
				for (int x = 0; x < widthPitch; x++) {
					vertices[idx++] = x;
					vertices[idx++] = heightMap[hIdx++] * strength;
					vertices[idx++] = z;
					idx += inc;
				}
			}
		}

		private void buildIndices () {
			int idx = 0;
			short pitch = (short)(width + 1);
			short i1 = 0;
			short i2 = 1;
			short i3 = (short)(1 + pitch);
			short i4 = pitch;

			short row = 0;

			for (int z = 0; z < height; z++) {
				for (int x = 0; x < width; x++) {
					indices[idx++] = i1;
					indices[idx++] = i2;
					indices[idx++] = i3;

					indices[idx++] = i3;
					indices[idx++] = i4;
					indices[idx++] = i1;

					i1++;
					i2++;
					i3++;
					i4++;
				}

				row += pitch;
				i1 = row;
				i2 = (short)(row + 1);
				i3 = (short)(i2 + pitch);
				i4 = (short)(row + pitch);
			}
		}
	}
//
//	@Override
//	public boolean needsGL20 () {
//		return false;
//	}
}