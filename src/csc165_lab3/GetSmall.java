package csc165_lab3;

import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.scene.TriMesh;

public class GetSmall extends BTAction{
	TriMesh npc;
	
	public GetSmall(TriMesh n){
		npc = n;
	}
	
	protected BTStatus update(float elapsedTime){
		//npc.getSmall();
		System.out.print("small");
		npc.scale(0.9f,0.9f,0.9f);
		return BTStatus.BH_SUCCESS;
	}
}