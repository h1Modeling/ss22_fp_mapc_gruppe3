package de.feu.massim22.group3.agents;

import eis.iilang.*;

class Desire {

	public String name;
	public int priority;
	public BdiAgent agent;
	public Action outputAction;
	
	Desire(String name, BdiAgent agent) {
		this.name = name;
		this.agent = agent;
		
	}

}
