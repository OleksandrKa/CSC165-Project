package csc165_lab3;

import graphicslib3D.Point3D;
import sage.ai.behaviortrees.BTCompositeType;
import sage.ai.behaviortrees.BTSequence;
import sage.ai.behaviortrees.BehaviorTree;
import sage.scene.TriMesh;

public class NPCcontroller{
	BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	long currentTime, lastUpdateTime;
	long lastThinkUpdateTime, lastTickUpdateTime;
	//NPC npc;
	TriMesh npc;
	Point3D npcLoc;
	//GameClientTCP server;
	MyGame game;
	boolean nearFlag = false;

	public NPCcontroller(MyGame myGame, TriMesh heroNPC, Point3D heroLoc) {
		game = myGame;
		npc = heroNPC;
		npcLoc = heroLoc;
	}

	public void startNPControl(){
		currentTime = System.nanoTime();
		lastUpdateTime = currentTime;
		setupNPC();
		setupBehaviorTree();
		npcLoop();
	}
	
	public void setupNPC(){
		//npc = new NPC();
	}
	
	public void npcLoop(){
		//while(true){
			currentTime = System.nanoTime();
			float elapsedThinkMilliSecs = (currentTime-lastThinkUpdateTime)/(1000000.0f);
			float elapsedTickMilliSecs = (currentTime-lastTickUpdateTime)/(1000000.0f);
			
			//TICK
			if(elapsedTickMilliSecs >= 50.0f){
				lastTickUpdateTime = currentTime;
				//npc.updateLocation();
				//server.sendNPCinfo();
			}
			
			//THINK
			if(elapsedThinkMilliSecs >= 500.0f){
				System.out.print("Think");
				lastThinkUpdateTime = currentTime;
				bt.update(elapsedThinkMilliSecs);
			}
			
			Thread.yield();
		//}
	}
	//If Avatar is nearby, walk toward it. Otherwise, walk in a random direction.
	//For now, just gets big and small when avatar is nearby.
	public void setupBehaviorTree(){
		bt.insertAtRoot(new BTSequence(10));
		bt.insertAtRoot(new BTSequence(20));
		bt.insert(10, new AvatarNear(game,this,npcLoc,false));
		//bt.insert(10, new WalkToward(npc));
		bt.insert(10, new GetBig(npc));
		//bt.insert(20, new WalkRandom(npc));
		bt.insert(20, new GetSmall(npc));
	}

	public void setNearFlag(boolean f){
		nearFlag = f;
	}
	public boolean getNearFlag() {
		return nearFlag;
	}
}