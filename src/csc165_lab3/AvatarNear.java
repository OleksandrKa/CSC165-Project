package csc165_lab3;

import MyGameEngine.GameServerTCP;
import graphicslib3D.Point3D;
import sage.ai.behaviortrees.BTCondition;
import sage.scene.TriMesh;

public class AvatarNear extends BTCondition{
	//GameClientTCP server;
	MyGame game;
	NPCcontroller npcc;
	//NPC npc;
	Point3D npcLoc;
	
	public AvatarNear(MyGame g, NPCcontroller c, Point3D n, boolean toNegate){
		super(toNegate);
		game = g;
		npcc = c;
		npcLoc = n;
	}
	
	protected boolean check(){
		Point3D npcP = new Point3D(npcLoc.getX(),npcLoc.getY(),npcLoc.getZ());
		game.checkAvatarNear(npcP);
		return npcc.getNearFlag();
	}
}