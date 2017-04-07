package csc165_lab3;

import sage.event.AbstractGameEvent;

public class GatherEvent extends AbstractGameEvent {

	int score;

	public GatherEvent(int playerScore) {
		score = playerScore;
	}

	public int getTreasureCount() {
		return score;
	}

}