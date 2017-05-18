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
		npc.growingOrShrinking = 'g';
		
		return BTStatus.BH_SUCCESS;
	}
}