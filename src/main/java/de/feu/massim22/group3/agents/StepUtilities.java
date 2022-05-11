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
        Set<Supervisor> allSupervisors = new HashSet<Supervisor>();
        Set<Supervisor> oldSupervisors = new HashSet<Supervisor>();

        for (BdiAgent agent : allAgents) {
            allSupervisors.add(((BdiAgentV2) agent).getSupervisor());

            for (Thing thing : agent.belief.getThings()) {
                // Agent hat in seiner Vision ein anderen Agent
                if (thing.type.equals(Thing.TYPE_ENTITY)) {
                 // dieser ist aus dem gleichen Team
                    if (thing.details.equals(agent.belief.getTeam())) {
                     // und nicht er selbst
                        if (thing.x != 0 && thing.y != 0) { 
                            // also ein Kandidat zum mergen
                            foundAgent.add(new AgentMeeting((BdiAgentV2) agent, new Point(thing.x, thing.y)));
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

                            // Gruppe von agent2 in Gruppe von agent1 mergen (Methodenaufruf)
                            oldSupervisors.add(agent2.getSupervisor());
                        } else {

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

                // Navi.get().updateSupervisor(loopSupervisor.getName());
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
    public synchronized boolean runAgentDecisions(BdiAgent agent) {
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
                agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep());
    }

    /**
     * The method .
     *
     * @param supervisor - the supervisor who wants to make the decisions
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
        for (Desire desire : agent.desires) {
            if (desire.priority < priority) {
                result = desire;
                priority = desire.priority;
            }
        }
        return result;
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
