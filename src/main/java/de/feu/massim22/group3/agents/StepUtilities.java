package de.feu.massim22.group3.agents;

import java.util.*;
import java.util.List;

import de.feu.massim22.group3.map.Navi;

import java.awt.*;
import massim.protocol.data.Thing;

public class StepUtilities {
    public static ArrayList<BdiAgentV2> allAgents = new ArrayList<BdiAgentV2>();
    public static Set<Supervisor> allSupervisors = new HashSet<Supervisor>();
    private static int countAgent = 0;

    Supervisor loopSupervisor;

    /**
     * The agent has initiated his map update.
     * 
     * @param agent    - the agent which has updated the map
     * @param step     - the step in which the program is at the moment
     * @param teamSize - the size of the team of which the agent is part of
     * 
     * @return boolean - the agent is done updating the map
     */
    public static synchronized boolean reportMapUpdate(BdiAgentV2 agent, int step, int teamSize) {
        boolean result = false;
        countAgent++;

        if (countAgent == teamSize) {
            countAgent = 0;
            result = true;
        }

        return result;
    }

    /**
     * All the things that have to be done to merge two groups together, update the
     * maps for the resulting groups and do some group/supervisor decisions.
     *
     * @param step - the step in which the program is at the moment
     * 
     * @return boolean - group merge was a success
     */
    public void doGroupProcessing(int step) {
        BdiAgentV2 agent1;
        BdiAgentV2 agent2;

        ArrayList<AgentMeeting> foundAgent = new ArrayList<AgentMeeting>();
        Set<Supervisor> oldSupervisors = new HashSet<Supervisor>();

        for (BdiAgentV2 agent : allAgents) {
            for (Thing thing : agent.belief.getThings()) {
                // Agent hat in seiner Vision ein anderen Agent
                if (thing.type.equals(Thing.TYPE_ENTITY)) {
                 // dieser ist aus dem gleichen Team
                    if (thing.details.equals(agent.belief.getTeam())) {
                     // und nicht er selbst
                        if (thing.x != 0 && thing.y != 0) { 
                            // also ein Kandidat zum mergen
                            foundAgent.add(new AgentMeeting(agent, new Point(thing.x, thing.y)));
                        }
                    }
                }
            }
        }

        // Agents suchen die sich getroffen haben
        for (int j = 0; j < foundAgent.size(); j++) {
            for (int k = j + 1; k < foundAgent.size(); k++) {
                // bei einem Treffen müssen sich beide gesehen haben und die Koordinaten unterscheiden sich nur im Vorzeichen
                if ((foundAgent.get(k).position.x == -foundAgent.get(j).position.x)
                        && (foundAgent.get(k).position.y == -foundAgent.get(j).position.y)) {
                    agent1 = foundAgent.get(j).agent;
                    agent2 = foundAgent.get(k).agent;

					// Agents sind aus unterschiedlichen Gruppen
					if (!(agent1.getSupervisor() == agent2.getSupervisor())) {
						// die kleinere in die größere Gruppe mergen
						if (agent1.getSupervisor().getAgents().size() >= agent2.getSupervisor().getAgents().size()) {
							mergeGroups(agent1.getSupervisor(), agent2.getSupervisor());
							// Gruppe von agent2 in Gruppe von agent1 mergen (Methodenaufruf)
							oldSupervisors.add(agent2.getSupervisor());
						} else {
							mergeGroups(agent2.getSupervisor(), agent1.getSupervisor());
							// Gruppe von agent1 in Gruppe von agent2 mergen (Methodenaufruf)
							oldSupervisors.add(agent1.getSupervisor());
						}
					}
				}
			}
		}

        allSupervisors.removeAll(oldSupervisors);

        /*
         * loop for all groups (after merge) with map update and group gecisions
         */
        for (Supervisor sv : allSupervisors) {
            loopSupervisor = sv;
            Runnable runnable = () -> {

                // Gruppenmap berechnen (Methodenaufruf) z.B. mit updateSupervisor;
                // dahinter dann der Aufruf startCalculation mit der vollständigen Map des
                // Supervisors
                // wodurch für die Agents die PATHFINDER_RESULT Message ausgelöst wird

                Navi.get().updateSupervisor(loopSupervisor.getName());
                runSupervisorDecisions(loopSupervisor);
            };
            Thread t3 = new Thread(runnable);
            t3.start();

        }
    }

    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(BdiAgentV2 agent) {
        boolean result = false;

        DodgeClear dodge = new DodgeClear(agent);
        if (dodge.isDesirePossible(dodge)) { // desire ist möglich , hinzufügen
            dodge.outputAction = dodge.getNextAction();
            getPriority(dodge);
            agent.desires.add(dodge);
        }

        DigFree dig = new DigFree(agent);
        if (dig.isDesirePossible(dig)) { // desire ist möglich , hinzufügen
            dig.outputAction = dig.getNextAction();
            getPriority(dig);
            agent.desires.add(dig);
        }

        HinderEnemy hinder = new HinderEnemy(agent);
        GoGoalZone goGoalZone = new GoGoalZone(agent);
        GoRoleZone goRoleZone = new GoRoleZone(agent);
        // RemoveObstacle removeObstacle = new RemoveObstacle(agent);

        agent.decisionsDone = true;
        return result;
    }

    /**
     * The method runs the different supervisor decisions.
     *
     * @param supervisor - the supervisor who wants to make the decisions
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
        Navi.get().updateAgent(agent.getSupervisor().getName(), agent.getName(), agent.index,
                agent.belief.getPosition(), agent.belief.getVision(), agent.belief.getThings(),
                agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep(), false);
    }

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority 
     * 
     * @return int - the priority
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

    /**
     * The method determines the Intention for a certain agent .
     *
     * @param agent - the agent that needs a intention
     * 
     * @return Desire - the intention
     */
    public Desire determineIntention(BdiAgent agent) {
        Desire result = null;
        int priority = 1000;
        for (Desire desire : agent.desires) {
            if (desire.priority < priority) {
                result = desire;
                priority = desire.priority;
            }
        }
        return result;
    }
    
    /**
     * The method merges two groups together
     *
     * @param supervisorGroup - the supervisor of the group that the other group is going to be merged into
     * @param supervisorToMerge - the supervisor of the group that is going to be merged into the other group
     * 
     */
	// mergen 
	public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge) {
		List<String> agentsSupervisorGroup = supervisorGroup.getAgents();
		List<String> agentsSupervisorToMerge = supervisorToMerge.getAgents();

		// Agents von agentsSupervisorToMerge in die Liste der Agents von agentsSupervisorGroup
		agentsSupervisorGroup.addAll(agentsSupervisorToMerge);
		
		// neuer Supervisor für agents der agentsSupervisorToMerge Liste
        for (BdiAgentV2 agent : allAgents) {
        	if (agentsSupervisorToMerge.contains(agent.getName())) {
        		agent.setSupervisor(supervisorGroup);
        	}
        }
	}
}

class AgentMeeting {
    BdiAgentV2 agent;
    Point position;

    AgentMeeting(BdiAgentV2 agent, Point position) {
        this.agent = agent;
        this.position = position;
    }
}
