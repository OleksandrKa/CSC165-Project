package csc165_lab3;

import sage.ai.behaviortrees.BTCompositeType;
import sage.ai.behaviortrees.BTSequence;
import sage.ai.behaviortrees.BehaviorTree;
import sage.scene.SceneNode;

public class NPCcontroller{
	BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
	long currentTime, lastUpdateTime;
	long lastThinkUpdateTime, lastTickUpdateTime;
	//NPC npc;
	NPC npc;
	//GameClientTCP server;
	MyGame game;
	boolean nearFlag = false;

	public NPCcontroller(MyGame myGame, NPC mine) {
		game = myGame;
		npc = mine;
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
			if(elapsedTickMilliSecs >= 20.0f){
				lastTickUpdateTime = currentTime;
				npc.updateSize();
				//server.sendNPCinfo();
			}
			
			//THINK
			if(elapsedThinkMilliSecs >= 500.0f){
				lastThinkUpdateTime = currentTime;
				bt.update(elapsedThinkMilliSecs);
			}
			
			Thread.yield();
		//}
	}
	public void setupBehaviorTree(){
		bt.insertAtRoot(new BTSequence(10));
		bt.insertAtRoot(new BTSequence(20));
		bt.insert(10, new AvatarNear(game,this,npc.getLocalTranslation().getCol(3),false));
		bt.insert(10, new GetBig(npc));
		bt.insert(20, new GetSmall(npc));
	}

	public void setNearFlag(boolean f){
		nearFlag = f;
	}
	public boolean getNearFlag() {
		return nearFlag;
	}
}