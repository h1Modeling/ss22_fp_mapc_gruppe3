package de.feu.massim22.group3.agents;

import java.util.*;
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
	 * The class runs the different agent decisions.
	 *
	 * @param agent -  the agent who wants to make the decisions
	 * 
	 * @return boolean - the agent decisions are done
	 */
	public synchronized boolean runAgentDecisions(BdiAgent agent) {
		boolean result = false;
		
		
		
			Iterator i = agent.belief.getThings().iterator();

			while (i.hasNext()) {
				if (((Thing) i.next()).type.equals(Thing.TYPE_MARKER)) {// clearEvent in Vision von Agent
					if (((Thing) i.next()).details.equals("cp")) {// points die der Agent in seiner Vision hat vom clear Event
						if(((Thing) i.next()).x == 0 && ((Thing) i.next()).y == 0 ) {// schon selbst drin
							DodgeClear dodge = new DodgeClear();
						}
					}
						
						
				}
			}
		
						
		
		HinderEnemy hinder = new HinderEnemy();
		GoGoalZone goGoalZone = new GoGoalZone();
		RemoveObstacle remove = new RemoveObstacle();
		
		agent.decisionsDone = true;
		return result;
	}
	
	/**
	 * The class runs the different supervisor decisions.
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

}
