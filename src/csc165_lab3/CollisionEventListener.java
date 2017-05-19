package csc165_lab3;

import sage.event.*;
import java.lang.String;

public class CollisionEventListener implements IEventListener {
    public boolean handleEvent(IGameEvent event) {
        PlayerMineEvent c = (PlayerMineEvent) event;
        c.playSound();
        return true;
    }
}
