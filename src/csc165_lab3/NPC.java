
package csc165_lab3;

import java.awt.Color;

import sage.scene.Group;
import sage.scene.SceneNode;
import sage.scene.shape.Sphere;
import sage.event.*;

public class NPC extends Group implements IEventListener {
	SceneNode npcModel;
	public char growingOrShrinking = 'n';
	
	public NPC(){
		npcModel = new Sphere(1.0,16,16, Color.red);
		npcModel.updateLocalBound();
		npcModel.updateWorldBound();
		this.addChild(npcModel);
	}
	
	public void updateSize(){
		if(growingOrShrinking == 'g')
			this.scale(1.1f,1.1f,1.1f);
		if(growingOrShrinking == 's')
			this.scale(0.9f,0.9f,0.9f);
	}

	public boolean handleEvent(IGameEvent event) {
		CollisionEvent c = (CollisionEvent) event;
        c.playSound();
        return true;
	}
}