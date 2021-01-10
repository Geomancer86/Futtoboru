package com.rndmodgames.tools;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Generates a Mesh array that will be used for a fast paced automatic-infinite scroll
 *
 * @author Geomancer86
 */
public class InfiniteScrollMeshGenerator {

	/**
	 * @return an infinite scroll mesh array
	 */
	public static Mesh [] generateMeshes(int n, int offset) {
		
		Mesh [] meshes = new Mesh [n];
		
		for (int a = 0; a < n; a++) {
			Mesh mesh = new Mesh(true, 10000, 10000, 
								 new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
								 new VertexAttribute(Usage.Normal, 3, ShaderProgram.NORMAL_ATTRIBUTE),
								 new VertexAttribute(Usage.ColorUnpacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
								 new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE));
	
			// add the mesh with the required offset (a)
			mesh.setVertices(generateVertices(40, 2, 0, a - offset));
		    mesh.setIndices(generateIndices(40, 2));
		    
		    meshes[a] = mesh;
		}
		
		return meshes;
	}
	
	public static float [] generateVertices(int width, int height, int xOffset, int yOffset) {
		
		int index = 0;
		float vertices [] = new float[width * height * 12];

	    for(int i = 0; i < width; i ++) {
	        for(int j = 0; j < height; j++) {

	            //vertex coordinates
				vertices[index] = i - xOffset;
				vertices[index + 1] = 0;
				vertices[index + 2] = j + yOffset;

				// normal
				vertices[index + 3] = 0;
				vertices[index + 4] = 1;
				vertices[index + 5] = 0;

				// vertex colors (RGBA)
				vertices[index + 6] = 1;
				vertices[index + 7] = 1;
				vertices[index + 8] = 1;
				vertices[index + 9] = 1;
				
				// texture coordinates
				vertices[index + 10] = i;
				vertices[index + 11] = j;

				// remember to increment by the size of the vertices array
	            index += 12;
	        }
	    }
	    
	    return vertices;
	}
	
	public static short[] generateIndices(int width, int height) {
		
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
}