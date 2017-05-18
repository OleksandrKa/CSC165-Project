package csc165_lab3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import sage.model.loader.OBJLoader;
import sage.scene.TriMesh;
import sage.scene.state.TextureState;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class GhostAvatar{
	//public static float[] vrts;
	//public static float[] cl;
	//public static int[] triangles;
	TriMesh avatarModel 
	
	public GhostAvatar(char avatarType){
		
		OBJLoader loader = new OBJLoader();
		Texture modelTexture = null;
		TriMesh 
		
		if(avatarType == 'h'){
			this = loader.loadModel("./images/hero.obj");
			modelTexture = TextureManager.loadTexture2D("./images/heroTexture.png");
		}
		if(avatarType == 'r'){
			model = loader.loadModel("./images/robot.obj");
			modelTexture = TextureManager.loadTexture2D("./images/robotTexture.png");
			//model.rotate(90, new Vector3D(0,1,0));
		}
		
		model.updateLocalBound();
		
		//Apply Texture
		TextureState modelState;
		modelTexture.setWrapMode(Texture.WrapMode.Repeat);
		modelTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		modelState = (TextureState) display.getRenderer().createRenderState(RenderStateType.Texture);
		modelState.setTexture(modelTexture, 0);
		modelState.setEnabled(true);
		this.setRenderState(modelState);
		
		/*//Replace with loading avatar model/texture.
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
		this.setIndexBuffer(triangleBuf);*/
	}
}