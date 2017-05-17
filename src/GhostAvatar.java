package csc165_lab3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import sage.scene.TriMesh;

public class GhostAvatar extends TriMesh{
	public static float[] vrts;
	public static float[] cl;
	public static int[] triangles;
	
	public GhostAvatar(char avatarType){
		
		//Replace with loading avatar model/texture.
		if(avatarType == 'd'){
			vrts = new float[] {0,-1,0,-1,1,1,1,1,1,1,1,-1,-1,1,-1,
											  0,-0.9f,0};
			cl = new float[] {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,0,1,1,
											1,0,0,1};
			triangles = new int[] {0,1,2,0,2,3,0,3,4,0,4,1,
											   5,1,2,5,2,3,5,3,4,5,4,1};
		}
		else if(avatarType == 'p'){
			vrts = new float[] {0,1,0,-1,-1,1,1,-1,1,1,-1,-1,-1,-1,-1};
			cl = new float[] {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,0,1,1};
			triangles = new int[] {0,1,2,0,2,3,0,3,4,0,4,1,1,4,2,4,3,2};
		}
		
		FloatBuffer vertBuf = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vrts);
		FloatBuffer colorBuf = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
		IntBuffer triangleBuf = com.jogamp.common.nio.Buffers.newDirectIntBuffer(triangles);
		
		this.setVertexBuffer(vertBuf);
		this.setColorBuffer(colorBuf);
		this.setIndexBuffer(triangleBuf);
	}
}