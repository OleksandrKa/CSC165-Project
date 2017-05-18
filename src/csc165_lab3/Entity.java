package csc165_lab3;

import java.util.UUID;

import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.display.IDisplaySystem;
import sage.model.loader.OBJLoader;
import sage.scene.TriMesh;
import sage.scene.state.RenderState.RenderStateType;
import sage.scene.state.TextureState;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class Entity{
	public UUID id;
	
	public TriMesh model;
	
	public Entity(UUID remoteID, Vector3D remotePosition, int rotateDegrees, char avatarType, IDisplaySystem display){
		id = remoteID;
		
		OBJLoader loader = new OBJLoader();
		Texture modelTexture = null;
		
		if(avatarType == 'h'){
			model = loader.loadModel("./images/hero.obj");
			modelTexture = TextureManager.loadTexture2D("./images/heroTexture.png");
		}
		if(avatarType == 'r'){
			model = loader.loadModel("./images/robot.obj");
			modelTexture = TextureManager.loadTexture2D("./images/robotTexture.png");
		}
		
		model.updateLocalBound();
		
		//Apply Texture
		TextureState modelState;
		modelTexture.setWrapMode(Texture.WrapMode.Repeat);
		modelTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		modelState = (TextureState) display.getRenderer().createRenderState(RenderStateType.Texture);
		modelState.setTexture(modelTexture, 0);
		modelState.setEnabled(true);
		model.setRenderState(modelState);
		
		updatePosition(remotePosition, rotateDegrees);
	}
	
	public void updatePosition(Vector3D ghostPosition, int rotateDegrees){
		Matrix3D translationM = new Matrix3D();
		translationM.translate(ghostPosition.getX(), ghostPosition.getY(), ghostPosition.getZ());
		model.setLocalTranslation(translationM);
		Matrix3D rotationM = new Matrix3D();
		rotationM.rotate(rotateDegrees, new Vector3D(0,1,0));
		model.setLocalRotation(rotationM);
	}
	
}