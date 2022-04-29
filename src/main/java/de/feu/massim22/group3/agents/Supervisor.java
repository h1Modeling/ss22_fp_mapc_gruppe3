package de.feu.massim22.group3.agents;

import eis.iilang.Percept;

public class Supervisor implements ISupervisor {
	
	private String name;
	private Supervisable parent;
	
	public Supervisor(Supervisable parent) {
		this.parent = parent;
	}
	
	public void handleMessage(Percept message, String sender) {
		// This Supervisor is retired - forward to new supervisor
		if (!name.equals(parent.getName())) {
			// TODO send message across Agent to active Supervisor
		} else {
			// TODO handle message
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
