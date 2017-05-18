package csc165_lab3;

import graphicslib3D.Matrix3D;
import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.scene.SceneNode;

public class GetBig extends BTAction{
	NPC npc;
	
	public GetBig(NPC n){
		npc = n;
	}
	
	protected BTStatus update(float elapsedTime){
		if(npc.npcModel.getLocalScale().getRow(0).getX() <= 0.1){
			Matrix3D scaleM = new Matrix3D();
			scaleM.scale(0.1, 0.1, 0.1);
			npc.setLocalScale(scaleM);
		}
		npc.growingOrShrinking = 'g';
		return BTStatus.BH_SUCCESS;
	}
}