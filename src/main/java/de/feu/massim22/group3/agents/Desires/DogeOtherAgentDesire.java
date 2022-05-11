package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class DogeOtherAgentDesire extends Desire {

	public DogeOtherAgentDesire(Desires desireType) {
		super(desireType);
	}

	@Override
	void defineSubDesires() {
		subDesires.add(SubDesires.DODGE_OTHER_AGENT.getSubDesireObj());
	}

}
