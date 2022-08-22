package de.feu.massim22.group3.agents.supervisor;

import java.util.Set;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import java.awt.Point;

/**
 * The Interface <code>ISupervisor</code> defines methods to communicate between an Agent and its supervisor.
 * 
 * @author Heinz Stadler
 */
public interface ISupervisor {

    /**
     * Decodes and handles a Percept message.
     * 
     * @param message the percept message
     * @param sender the name of the sender of the message
     */
    void handleMessage(Percept message, String sender);

    /**
     * Sets the name of the supervisor of the agent.
     * 
     * @param name the name of the supervisor
     */
    void setName(String name);

    /**
     * Gets the name of the supervisor of the agent.
     * 
     * @return name the name of the supervisor
     */
    String getName();

    /**
     * Sets the current step of the simulation.
     * 
     * @param step the step of the simulation
     */
    void initStep(int step);

    /**
     * Adds an agent to the agent group.
     * 
     * @param name the name of the agent
     */
    void addAgent(String name);

    /**
     * Sends current information of an agent to the supervisor.
     * 
     * @param agent the name of the agent the data is from
     * @param report the report containing the agent data
     */
    void reportAgentData(String agent, AgentReport report);

    /**
     * Sends information about the current tasks in the simulation to the supervisor.
     * 
     * @param tasks the tasks of the simulation
     */
    void reportTasks(Set<TaskInfo> tasks);

    /**
     * Asks the supervisor for permission to attach a thing.
     * 
     * @param agent the agent which wants to attach a thing 
     * @param p the position of the thing
     * @param direction the direction of the thing relative to the agent
     */
    void askForAttachPermission(String agent, Point p, String direction);
}
