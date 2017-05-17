package csc165_lab3;

import graphicslib3D.Point3D;
import sage.ai.behaviortrees.BTCondition;

public class AvatarNear extends BTCondition{
	//GameClientTCP server;
	MyGame game;
	NPCcontroller npcc;
	//NPC npc;
	
		super(toNegate);
		game = g;
		npcc = c;
		npcLoc = n;
	}
	
	protected boolean check(){
		return npcc.getNearFlag();
	}
}