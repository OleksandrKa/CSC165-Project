package csc165_lab3;

import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.scene.SceneNode;

public class GetBig extends BTAction{
	SceneNode npc;
	
	public GetBig(SceneNode n){
		npc = n;
	}
	
	protected BTStatus update(float elapsedTime){
		//npc.getBig();
		npc.scale(1.1f,1.1f,1.1f);
		return BTStatus.BH_SUCCESS;
	}
}