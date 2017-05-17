package csc165_lab3;

import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.scene.SceneNode;

public class GetSmall extends BTAction{
	SceneNode npc;
	
	public GetSmall(SceneNode n){
		npc = n;
	}
	
	protected BTStatus update(float elapsedTime){
		//npc.getSmall();
		npc.scale(0.9f,0.9f,0.9f);
		return BTStatus.BH_SUCCESS;
	}
}