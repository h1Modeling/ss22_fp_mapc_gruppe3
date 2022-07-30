package de.feu.massim22.group3.utils.debugger;

import java.util.List;
import java.util.Set;
import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import de.feu.massim22.group3.utils.debugger.debugData.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.debugData.DesireDebugData;
import de.feu.massim22.group3.utils.debugger.debugData.GroupDebugData;
import eis.iilang.Action;

/**
 * The Interface <code>IGraphicalDebugger</code> defines methods for communication between the debugger
 * and the <code>Navi</code>.
 *
 * @see GraphicalDebugger
 * @see de.feu.massim22.group3.map.Navi
 * @author Heinz Stadler
 */
public interface IGraphicalDebugger {
    /**
     * Selects the agent in the debugger and shows its debug information.
     * 
     * @param agent the name of the agent
     */
    void selectAgent(String agent);

    /**
     * Sets a manual Action for the agent.
     * 
     * @param agent the name of the agent
     * @param a the action which should be set
     */
    void setActionForAgent(String agent, Action a);

    /**
     * Sets debug information of an agent group.
     * 
     * @param data the debug data of the group
     */
    void setGroupData(GroupDebugData data);

    /**
     * Sets debug information of a single agent.
     * 
     * @param data the debug data of the agent
     */
    void setAgentData(AgentDebugData data);

    /**
     * Sets debug information of the decision making of a single agent.
     * 
     * @param data the desire debug data
     * @param agent the name of the agent
     */
    void setAgentDesire(List<DesireDebugData> data, String agent);

    /**
     * Sets basic information about the current simulation.
     * 
     * @param currentStep the current step of the simulation
     * @param maxSteps the number of steps of the simulation
     * @param score the current score of the agent team
     */
    void setSimInfo(int currentStep, int maxSteps, int score);

    /**
     * Sets information about the current norms of the simulation.
     * 
     * @param norms the current Set of NormInfo
     * @param step the current step of the simulation 
     */
    void setNorms(Set<NormInfo> norms, int step);

    /**
     * Sets information about the current tasks of the simulation.
     * 
     * @param tasks the current Set of TaskInfo
     * @param step the current step of the simulation
     */
    void setTasks(Set<TaskInfo> tasks, int step);

    /**
     * Removes the supervisor from the selectable supervisor and merges its group with the provided group.
     * 
     * @param name the name of the supervisor to remove
     * @param newGroup the name of the new group the supervisor should be part of 
     */
    void removeSupervisor(String name, String newGroup);

    /**
     * Sets the provided group as the current visible group and shows it's debug information.
     *  
     * @param name the name of the supervisor of the group
     */
    void setSelectedGroup(String name);

    /**
     * Forces a new step and sends cached Agent Actions to the server. 
     */
    void makeStep();

    /**
     * Sets the <code>DebugStepListener</code> which handles interaction between the debugger and the server.
     * 
     * @param listener the listener
     * @param manualMode true if the listener is set to manual mode
     */
    void setDebugStepListener(DebugStepListener listener, boolean manualMode);

    /**
     * Gets the current group desire of an agent.
     * 
     * @param agent the name of the agent
     * @return the name of the group desire the agent currently has
     */
    String getAgentGroupDesireType(String agent);

    /**
     * Sets if the simulation should be slowed down.
     * 
     * @param value true if the simulation should be slowed down
     */
    void setDelay(boolean value);
}
