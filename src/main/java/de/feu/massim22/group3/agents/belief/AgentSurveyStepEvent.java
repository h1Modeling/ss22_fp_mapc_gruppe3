package de.feu.massim22.group3.agents.belief;

/**
 * The Record <code>AgentSurveyStepEvent</code> stores information about a surveyed agent received by the simulation server.
 *
 * @param name the name of the agent
 * @param role the role of the agent
 * @param energy the current energy of the agent
 * 
 * @author Heinz Stadler
 */
public record AgentSurveyStepEvent(String name, String role, int energy) implements StepEvent {
    public String toString() {
        return "Agent " + name + " with role " + role + " and energy " + energy;
    }
}
