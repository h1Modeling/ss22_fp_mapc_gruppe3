package de.feu.massim22.group3.agents;

import java.util.*;
import java.util.List;

import de.feu.massim22.group3.map.Navi;

import java.awt.*;
import massim.protocol.data.Thing;

/**
 * 
 * Class for 
 * 
 */

public class StepUtilities {
	private static int countAgent = 0;
	private static ArrayList<BdiAgent> allAgents = new ArrayList<BdiAgent>();

	/**
	 * The agent has updated his map.
	 * 
	 * @param agent - the agent which has updated the map
	 * @param step -  the step in which the program is at the moment
	 * @param teamSize - the size of the team of which the agent is part of
	 * 
	 * @return boolean - the agent is done updating the map
	 */
	public synchronized boolean reportMapUpdate(BdiAgent agent, int step, int teamSize) {
		boolean result = false;
		countAgent++;
		allAgents.add(agent);

		if (countAgent == teamSize) {
			result = true;
			countAgent = 0;
		}
			
		return result;
	}

	/**
	 * All the things that have to be done to merge two groups together.
	 *
	 * @param step -  the step in which the program is at the moment
	 * 
	 * @return boolean - group merge was a success
	 */
	public synchronized boolean proofGroupMerge(int step) {
		BdiAgent agent1 ;
		BdiAgent agent2;
		boolean result = false;
		Iterator i = null;
		ArrayList<BdiAgent> meetsAgent = new ArrayList<BdiAgent>();// überhaupt Agent gefunden
		ArrayList<Point> foundAgent = new ArrayList<Point>();// wo Agent gefunden
		ArrayList<Supervisor> groupsToMerge = new ArrayList<Supervisor>();
		Set<Supervisor> allSupervisors = new HashSet<Supervisor>();

		for (BdiAgent agent : allAgents) {
			i = agent.belief.getThings().iterator();
			allSupervisors.add(((BdiAgentV2)agent).getSupervisor());

			while (i.hasNext()) {
				if (((Thing) i.next()).type.equals(Thing.TYPE_ENTITY)) {// Agent hat in seiner Vision ein anderen Agent
					if (((Thing) i.next()).details.equals(agent.belief.getTeam())) {// gleiches Team
						if (((Thing) i.next()).x != 0 && ((Thing) i.next()).y != 0) { // nicht er selber
							meetsAgent.add(agent);
							foundAgent.add(new Point(((Thing) i.next()).x, ((Thing) i.next()).y));
						}
					}
				}
			}
		}

		for (int j = 0; j < meetsAgent.size(); j++) {// Agents suchen die sich getroffen haben
			for (int k = j + 1; k < meetsAgent.size(); k++) {
				if ((foundAgent.get(k).x == -foundAgent.get(j).x) && (foundAgent.get(k).y == -foundAgent.get(j).y)) {//
					agent1 = meetsAgent.get(j);
					agent2 = meetsAgent.get(k);
					
					if(!(((BdiAgentV2)agent1).getSupervisor() == ((BdiAgentV2)agent2).getSupervisor())) {// unterschiedliche Gruppen
						if(countGroupSize(((BdiAgentV2)agent1).getSupervisor()) >= countGroupSize(((BdiAgentV2)agent2).getSupervisor())) {
							//Gruppe von agent2 in Gruppe von agent1 mergen (Methdoenaufruf)
						}else {
							//Gruppe von agent1 in Gruppe von agent2 mergen (Methdoenaufruf)
						}
					}
				}
			}
		}
		
		//Map update für Gruppen ?
		
		/*Schleife über alle Gruppen (nach dem merge) Group Decisions 
		 * Thread pro Gruppe */
		for (Supervisor supervisor : allSupervisors) {
			Thread t3= new Thread(() -> runSupervisorDecisions(supervisor));
			t3.start();
			
		}
		
		return result;
	}
	
	/**
	 * The class determines the group size of the group with the given supervisor.
	 *
	 * @param step -  the supervisor of the group of which we want to determine the size
	 * 
	 * @return int - the size of the group
	 */
	private static int countGroupSize(Supervisor supervisor) {
		int counter = 0;
		for (BdiAgent agent : allAgents) {
			if (((BdiAgentV2) agent).getSupervisor() == supervisor) {
				counter++;
			}
		}
		return counter;
	}
	
	/**
	 * The method runs the different agent decisions.
	 *
	 * @param agent -  the agent who wants to make the decisions
	 * 
	 * @return boolean - the agent decisions are done
	 */
	public synchronized boolean runAgentDecisions(BdiAgent agent) {
		boolean result = false;

		DodgeClear dodge = new DodgeClear(agent);
		if(dodge.isDesirePossible(dodge)) { //desire ist möglich , hinzufügen
			dodge.outputAction = dodge.getNextAction();
			getPriority(dodge);
			agent.desires.add(dodge);
		}
		
		DigFree dig = new DigFree(agent);
		if(dig.isDesirePossible(dig)) { //desire ist möglich , hinzufügen
			dig.outputAction = dig.getNextAction();
			getPriority(dig);
			agent.desires.add(dig);
		}
		

		HinderEnemy hinder = new HinderEnemy(agent);
		GoGoalZone goGoalZone = new GoGoalZone(agent);
		GoRoleZone goRoleZone = new GoRoleZone(agent);
		//RemoveObstacle removeObstacle = new RemoveObstacle(agent);

		agent.decisionsDone = true;
		return result;
	}
	
	/**
	 * The method runs the different supervisor decisions.
	 *
	 * @param supervisor -  the supervisor who wants to make the decisions
	 * 
	 * @return boolean - the supervisor decisions are done
	 */
	public synchronized boolean runSupervisorDecisions(Supervisor supervisor) {
		boolean result = false;
		
		supervisor.decisionsDone = true;
		return result;
	}
	
	/**
	 * Update the Map.
	 * 
	 * @param agent - the Agent that wants to update the map
	 *
	 */
	public void updateMap(BdiAgentV2 agent) {
		Navi.get().updateAgent(agent.getSupervisor().getName(), agent.getName(), agent.index, agent.belief.getPosition(), agent.belief.getVision(), agent.belief.getThings(), agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep());
	}
	
	/**
	 * The method .
	 *
	 * @param supervisor -  the supervisor who wants to make the decisions
	 * 
	 * @return boolean - the supervisor decisions are done
	 */
	public int getPriority(Desire desire) {
		int result = 0;

		switch (desire.name) {
		case "DigFree":
			result = 10;
		case "DodgeClear":
			result = 20;
		case "LocalHinderEnemy":
			result = 90;
		case "LocalGetBlocks":
			result = 70;
		case "GoGoalZone":
			result = 80;
		case "GoRoleZone":
			result = 30;
		case "ReactToNorm":
			result = 40;
		case "LocalExplore":
			result = 100;
		}

		return result;
	}
	
	public Desire determineIntention(BdiAgent agent) {
		Desire result = null;
		int priority = 1000;
		for(Desire desire : agent.desires) {
			if(desire.priority < priority ) {
				result = desire;
				priority = desire.priority;
			}
		}
		return result;
	}
	
}
