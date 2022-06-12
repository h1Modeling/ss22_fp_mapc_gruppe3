package de.feu.massim22.group3.agents;

import java.util.LinkedList;
import java.util.List;

import de.feu.massim22.group3.agents.Desires.*;
import de.feu.massim22.group3.agents.Desires.SubDesires.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;

class DesireHandler {

    private BdiAgent agent;
    private Desire currentTeamGoal = null;
    private List<Desire> highPrioAgentGoals = new LinkedList<Desire>();
    private List<Desire> lowPrioAgentGoals = new LinkedList<Desire>();

    public DesireHandler(BdiAgent agent) {
        this.agent = agent;
        setHighPrioAgentGoals();
        setLowPrioAgentGoals();
        setTeamGoal(Desires.PROCESS_TASK);
    }

    public void setTeamGoal(Desires newDesire) {
        currentTeamGoal = newDesire.getDesireObj(agent);
    }

    public void setNextAction() {
        // Create List with all Desires
        List<Desire> allGoalsList = new LinkedList<Desire>();
        allGoalsList.addAll(highPrioAgentGoals);
        if (currentTeamGoal != null) {
            allGoalsList.add(currentTeamGoal);
        }
        allGoalsList.addAll(lowPrioAgentGoals);

        // Iterate of all goals and their Subgoals
        for (Desire desire : allGoalsList) {
            for (SubDesire subDesire : desire.getSubDesires()) {
                // buffer precondition (so it is not calculated multiple times for each if
                // clause)
                boolean isExecuteable = subDesire.isExecutable();
                // buffer postcondition (so it is not calculated multiple times for each if
                // clause)
                boolean isDone = subDesire.isDone();

                // If the preconditions of one SubDesire are not met then all subsequent
                // SubDesires of the Desire
                // cannot be executed because they depend on the postconditions of the previous
                // SubDesires of one Desire
                // If the SubDesire is not executable but done it is OK. The next SubDesire can
                // be executed.
                if (!isExecuteable && !isDone) {
                    AgentLogger.fine(agent.getName(), String.format(
                            "Preconditons and postconditions of SubDesire %s not fulfilled. Desire %s will not be executed.",
                            subDesire.toString(), desire.toString()));
                    // Stop SubDesire loop and check SubDesires of next Desire
                    break;
                }
                // Execute SubDesire if preconditions (isExecutable()) are fulfilled but the
                // postconditions (isDone()) not
                else if (isExecuteable && !isDone) {
                    AgentLogger.info(agent.getName(), String.format("Exectung SubDesire %s of Desire %s",
                            subDesire.toString(), desire.toString()));
                    agent.setIntention(subDesire);
                    return;
                }
            }
        }
        AgentLogger.warning(agent.getName(), "No valid Desire/SubDesire to execute.");
    }

    // Define high priority goals and their order here
    private void setHighPrioAgentGoals() {
        this.highPrioAgentGoals.add(Desires.DIG_FREE.getDesireObj(agent));
        this.highPrioAgentGoals.add(Desires.DODGE_CLEAR.getDesireObj(agent));
        this.highPrioAgentGoals.add(Desires.DODGE_OTHER_AGENT.getDesireObj(agent));
        this.highPrioAgentGoals.add(Desires.REACT_TO_NORM.getDesireObj(agent));
    }

    // Define low priority goals and their order here
    private void setLowPrioAgentGoals() {
        this.lowPrioAgentGoals.add(Desires.SPONTANEOUS_HINDER_ENEMY.getDesireObj(agent));
        this.lowPrioAgentGoals.add(Desires.GO_TO_UNKNOWN_AREA.getDesireObj(agent));
    }
}
