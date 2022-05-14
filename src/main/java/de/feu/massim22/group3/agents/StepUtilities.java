package de.feu.massim22.group3.agents;

import java.util.*;
import java.util.List;

import de.feu.massim22.group3.map.INaviAgentV2;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.awt.*;

import massim.eismassim.Log;
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

        AgentLogger.info("doGroupProcessing() in neuem Thread - Step: " + step);
        ArrayList<AgentMeeting> foundAgent = new ArrayList<AgentMeeting>();
        Set<Supervisor> exSupervisors = new HashSet<Supervisor>();
        Set<Thing> things = new HashSet<>();
        AgentLogger.info("doGroupProcessing() allSupervisors: " + allSupervisors);        

        if (allSupervisors.size() > 1) {
         // Noch gibt es mehr als einen Supervisor
            for (BdiAgentV2 agent : allAgents) {
                AgentLogger.info("doGroupProcessing() Start - Agent: " + agent.getName());
                things = agent.belief.getThings();
                for (Thing thing : things) {
                    // Agent hat in seiner Vision einen anderen Agent
                    if (thing.type.equals(Thing.TYPE_ENTITY)) {
                        // dieser ist aus dem gleichen Team
                        if (thing.details.equals(agent.belief.getTeam())) {
                            // und nicht er selbst
                            if (thing.x != 0 && thing.y != 0) {
                                AgentLogger.info("doGroupProcessing() Found - Agent: " + agent.getName()
                                        + " , Position: " + new Point(thing.x, thing.y));
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
                    // bei einem Treffen müssen sich beide gesehen haben und die relativen Koordinaten
                    // dürfen sich nur im Vorzeichen unterscheiden
                    if ((foundAgent.get(k).position.x == -foundAgent.get(j).position.x)
                            && (foundAgent.get(k).position.y == -foundAgent.get(j).position.y)) {
                        agent1 = foundAgent.get(j).agent;
                        agent2 = foundAgent.get(k).agent;

                        AgentLogger.info("doGroupProcessing() meeting in vision - Agent1: " + agent1.getName() + " , Agent2: "
                                + agent2.getName());
                        // Agents sind aus unterschiedlichen Gruppen ?
                        if (!(agent1.getSupervisor() == agent2.getSupervisor())) {
                            // dann die kleinere in die größere Gruppe mergen
                            if (agent1.getSupervisor().getAgents().size() >= agent2.getSupervisor().getAgents()
                                    .size()) {
                                AgentLogger.info("doGroupProcessing() merge 2 in 1 - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                        + agent2.getSupervisor().getName());
                                // Gruppe von agent2 in Gruppe von agent1 mergen
                                exSupervisors.add(agent2.getSupervisor());
                                mergeGroups(agent1.getSupervisor(), agent2.getSupervisor(), foundAgent.get(k).position);                               
                            } else {
                                AgentLogger.info("doGroupProcessing() merge 1 in 2 - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                        + agent2.getSupervisor().getName());
                                // Gruppe von agent1 in Gruppe von agent2 mergen
                                exSupervisors.add(agent1.getSupervisor());
                                mergeGroups(agent2.getSupervisor(), agent1.getSupervisor(), foundAgent.get(j).position);
                            }
                        } else AgentLogger.info("doGroupProcessing() no merge, already in same group - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                + agent2.getSupervisor().getName());
                    }
                }
            }
            
            allSupervisors.removeAll(exSupervisors);
        }
        /*
         * loop for all groups (after merge) with map update and group gecisions
         */
        // for (Supervisor sv : allSupervisors) {
        /*
         * loopSupervisor = sv; Runnable runnable = () -> { // Gruppenmap berechnen
         * (Methodenaufruf) z.B. mit updateSupervisor; // dahinter dann der Aufruf
         * startCalculation mit der vollständigen Map des // Supervisors // wodurch für
         * die Agents die PATHFINDER_RESULT Message ausgelöst wird
         * AgentLogger.info("Before updateSupervisor() in neuem Thread - Step: " + step
         * + " , Supervisor: " + loopSupervisor.getName());
         * Navi.<INaviAgentV2>get().updateSupervisor(loopSupervisor.getName());
         * AgentLogger.info( "After updateSupervisor() - Step: " + step +
         * " , Supervisor: " + loopSupervisor.getName()); runSupervisorDecisions(step,
         * loopSupervisor); }; Thread t3 = new Thread(runnable); t3.start();
         */
        AgentLogger.info("Before updateSupervisor() - Step: " + step + " , Supervisor: " + "A2");
        Navi.<INaviAgentV2>get().updateSupervisor("A2");
        AgentLogger.info("After updateSupervisor() - Step: " + step + " , Supervisor: " + "A2");

        AgentLogger.info("Before updateSupervisor() - Step: " + step + " , Supervisor: " + "A1");
        Navi.<INaviAgentV2>get().updateSupervisor("A1");
        AgentLogger.info("After updateSupervisor() - Step: " + step + " , Supervisor: " + "A1");
        // runSupervisorDecisions(step, "A1");
        // }

        AgentLogger.info("doGroupProcessing() End - Step: " + step);
    }

    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgentV2 agent) {
        boolean result = false;
        AgentLogger.info("runAgentDecisions() Start - Step: " + step + " , Supervisor: " + agent.getName());
        
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
    public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor) {
        boolean result = false;
        AgentLogger.info("runSupervisorDecisions() Start - Step: " + step + " , Supervisor: " + supervisor.getName());
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
        AgentLogger.info("Before updateMap() - Step: " + agent.belief.getStep() + " , Agent: " + agent.getName());
        Navi.<INaviAgentV2>get().updateMap(agent.getSupervisor().getName(), agent.getName(), agent.index,
                agent.belief.getPosition(), agent.belief.getVision(), agent.belief.getThings(),
                agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep());
        AgentLogger.info("After updateMap() - Step: " + agent.belief.getStep() + " , Agent: " + agent.getName());
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
	public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge, Point relativOldSupervisor) {
		List<String> agentsSupervisorGroup = supervisorGroup.getAgents();
		List<String> agentsSupervisorToMerge = supervisorToMerge.getAgents();

		// Agents von agentsSupervisorToMerge in die Liste der Agents von agentsSupervisorGroup
		agentsSupervisorGroup.addAll(agentsSupervisorToMerge);
        Point posNewSupervisor = Navi.<INaviAgentV2>get().getPosition(supervisorGroup.getName(), supervisorGroup.getName());
        
		// neuer Supervisor für agents der agentsSupervisorToMerge Liste
        for (BdiAgentV2 agent : allAgents) {
        	if (agentsSupervisorToMerge.contains(agent.getName())) {
        		agent.setSupervisor(supervisorGroup);
        		Navi.<INaviAgentV2>get().registerSupervisor(agent.getName(), supervisorGroup.getName());
        		
                //Point oldPosAgent = Navi.<INaviAgentV2>get().getPosition(agent.getName(), supervisorToMerge.getName());        		
                //Navi.<INaviAgentV2>get().setPosition(agent.getName(), supervisorGroup.getName()
        		//       , new Point(posNewSupervisor.x + relativOldSupervisor.x + oldPosAgent.x, posNewSupervisor.y + relativOldSupervisor.y + oldPosAgent.x));
        		
                Point oldPosAgent = agent.belief.getPosition();
                Point newPosAgent = new Point(oldPosAgent.x + relativOldSupervisor.x, oldPosAgent.y + relativOldSupervisor.y);
                agent.belief.setPosition(newPosAgent);
        		updateMap(agent);
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
