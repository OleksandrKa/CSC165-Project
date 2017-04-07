package csc165_lab3;

import sage.input.action.AbstractInputAction;
import sage.app.AbstractGame;
import net.java.games.input.Event;

public class QuitGameAction extends AbstractInputAction {

	private AbstractGame game;

	public QuitGameAction(AbstractGame g) // constructor
	{
		this.game = g;
	}

	@Override
	public void performAction(float time, Event event) {
		{
			game.setGameOver(true);
		}

	}

}
