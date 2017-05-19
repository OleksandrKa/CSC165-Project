package csc165_lab3;

import java.util.ArrayList;

import sage.event.*;
import sage.scene.SceneNode;
import sage.audio.*;

public class PlayerMineEvent extends AbstractGameEvent {
    private SceneNode node;
    private ArrayList<Sound> sounds;

    public PlayerMineEvent(SceneNode n, ArrayList<Sound> s) {
        node = n;
        sounds = s;
    }

    public String getEntityType() {
        if (node.getName().contains("mine"))
            return "mine";
        else if (node.getName().contains("player"))
            return "player";
        else
            return "Invalid Object";
    }

    public void playSound() {
        /*
        0 - beep
        1 - background
        2 - explosion
        3 - victory
        */
        if (getEntityType() == "mine") {
            sounds.get(2).play();
            System.out.print("Boom");
        }
        if (getEntityType() == "player") {
            sounds.get(3).play();
            System.out.print("Hooray");
        }
    }
}