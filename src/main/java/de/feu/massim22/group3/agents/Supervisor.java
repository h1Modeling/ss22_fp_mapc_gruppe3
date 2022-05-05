package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.TaskName;
import de.feu.massim22.group3.map.Navi;
import eis.iilang.Function;
import eis.iilang.Parameter;
import eis.iilang.Percept;

public class Supervisor implements ISupervisor {
	
	private String name;
	private Supervisable parent;
	private List<String> agents = new ArrayList<>();
	private List<ConfirmationData> confirmationData = new ArrayList<>();
	
	public Supervisor(Supervisable parent) {
		this.parent = parent;
		this.name = parent.getName();
		agents.add(name);
		initConfirmationData();
	}
	
	public void handleMessage(Percept message, String sender) {
		// This Supervisor is retired - forward to new supervisor
		if (!isActive()) {
			this.parent.forwardMessageFromSupervisor(message, name, sender);
		} else {
			Percept data = unpackMessage(message);
			String taskKey = data.getName();
			TaskName taskName = TaskName.valueOf(taskKey);
			switch (taskName) {
			case MAP_SENT_TO_NAVI:
				// TODO: 
				break;
			default:
				throw new IllegalArgumentException("Supervisor can't handle Message");
			}
		}
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void receiveConfirmation(String agent, TaskName task) {
		// Forward to active Supervisor
		if (!isActive()) {
			Percept message = this.createConfirmationMessage(task);
			this.parent.forwardMessageFromSupervisor(message, name, agent);
		}
		switch (task) {
		case MAP_SENT_TO_NAVI:
			// Test if all Agents have already sent data
			boolean allDone = true;
			for (ConfirmationData data : confirmationData) {
				if (data.agent.equals(agent)) {
					data.mapSentToNavi = true;
				}
				if (data.mapSentToNavi == false) {
					allDone = false;
				}
			}
			// Start Navi Calculation
			if (allDone) {
				Navi.get().startCalculation();
			}
			break;
		default:
			throw new IllegalArgumentException("Confirmation " + task.name() + " is not implemented yet");
		}	
	}

	private boolean isActive() {
		return this.name.equals(this.parent.getName());
	}

	private Percept createConfirmationMessage(TaskName task) {
		Parameter data = new Function(TaskName.MAP_SENT_TO_NAVI.name());
		return new Percept(TaskName.TO_SUPERVISOR.name(), data);
	}

	private Percept unpackMessage(Percept task) {
		List<Parameter> fromParas = task.getParameters();
		if (fromParas.size() > 0) {
			Parameter p = fromParas.get(0);
			if (!(p instanceof Function)) {
				throw new IllegalArgumentException("Supervisor Messages must contain a function in Body");
			}
			Function f = (Function)p;
			String name = f.getName();
			return new Percept(name, f.getParameters());
		}
		return null;
	}

	private class ConfirmationData {
		private String agent;
		boolean mapSentToNavi = false;
		
		ConfirmationData(String agent) {
			this.agent = agent;
		}

		void clear() {
			mapSentToNavi = false;
		}
	}

	private void initConfirmationData() {
		for (String a: agents) {
			confirmationData.add(new ConfirmationData(a));
		}
	}

	@Override
	public void initStep() {
		if (isActive()) {
			this.confirmationData.forEach(d -> d.clear());
		}
	}
}
