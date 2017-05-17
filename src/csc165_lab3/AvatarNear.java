package csc165_lab3;

import graphicslib3D.Vector3D;
import sage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition{
	//GameClientTCP server;
	MyGame game;
	NPCcontroller npcc;
	//NPC npc;
	Vector3D npcLoc;
	
	public AvatarNear(MyGame g, NPCcontroller c, Vector3D n, boolean toNegate){
		super(toNegate);
		game = g;
		npcc = c;
		npcLoc = n;
	}
	
	protected boolean check(){
		game.checkAvatarNear(npcc, npcLoc);
		return npcc.getNearFlag();
	}
}