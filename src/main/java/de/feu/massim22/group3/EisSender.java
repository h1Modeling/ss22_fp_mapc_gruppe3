package de.feu.massim22.group3;

import de.feu.massim22.group3.agents.Agent;
import eis.iilang.Action;

public interface EisSender {
	void send(Agent agent, Action action);
}
