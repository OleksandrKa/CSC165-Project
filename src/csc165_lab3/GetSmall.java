package csc165_lab3;

import graphicslib3D.Matrix3D;
import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.scene.SceneNode;

public class GetSmall extends BTAction{
	NPC npc;
	
	public GetSmall(NPC n){
		npc = n;
	}
	
	protected BTStatus update(float elapsedTime){
		if(npc.getLocalScale().getRow(0).getX() <= 0.01){
			npc.growingOrShrinking = 'n';
		}
		else{
			npc.growingOrShrinking = 's';
		}
		return BTStatus.BH_SUCCESS;
	}
}