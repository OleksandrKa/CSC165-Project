package csc165_lab3;

import java.util.UUID;
import graphicslib3D.Vector3D;
import csc165_lab3.*;
import com.jogamp.common.nio.Buffers.*;
import java.nio.*;
import sage.scene.*;
import sage.event.*;
import graphicslib3D.*;

public class GhostAvatar extends TriMesh{
	public UUID id;
	public Vector3D pos;
	
	
	public static float[] vrts = new float[] {0,-1,0,-1,1,1,1,1,1,1,1,-1,-1,1,-1,
											  0,-0.9f,0};
	public static float[] cl = new float[] {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,0,1,1,
											1,0,0,1};
	public static int[] triangles = new int[] {0,1,2,0,2,3,0,3,4,0,4,1,
											   5,1,2,5,2,3,5,3,4,5,4,1};
	public float truckSize = 5.0f;
	public float sizeChangeTime = 0.0f;
	
	public GhostAvatar(UUID ghostID, Vector3D ghostPosition){
		id = ghostID;
		pos = ghostPosition;
		
		
		int i;
		FloatBuffer vertBuf = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(vrts);
		FloatBuffer colorBuf = com.jogamp.common.nio.Buffers.newDirectFloatBuffer(cl);
		IntBuffer triangleBuf = com.jogamp.common.nio.Buffers.newDirectIntBuffer(triangles);
		
		this.setVertexBuffer(vertBuf);
		this.setColorBuffer(colorBuf);
		this.setIndexBuffer(triangleBuf);
	}
	
	
	
}