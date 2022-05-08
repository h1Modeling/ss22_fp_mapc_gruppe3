package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.TaskName;
import eis.iilang.Percept;

public interface ISupervisor {

	void handleMessage(Percept message, String sender);
	void setName(String name);
	String getName();
	void receiveConfirmation(String agent, TaskName task);
	void initStep();
}
