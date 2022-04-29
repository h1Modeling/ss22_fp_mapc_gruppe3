package de.feu.massim22.group3.agents;

import eis.iilang.Percept;

public interface ISupervisor {

	void handleMessage(Percept message, String sender);
	void setName(String name);
}
