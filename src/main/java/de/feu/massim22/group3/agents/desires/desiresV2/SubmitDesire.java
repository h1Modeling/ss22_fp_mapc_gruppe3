package de.feu.massim22.group3.agents.desires.desiresV2;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations;
import de.feu.massim22.group3.agents.utilsV2.Status;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations.Cooperation;
//import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

import java.awt.Point;

/**
 * The class <code>SubmitDesire</code> models the desire to submit a task ( block structure).
 * 
 * @author Melinda Betz
 */
public class SubmitDesire extends BeliefDesire {

    private TaskInfo info;
    private BdiAgentV2 agent;
    
    /**
     * Instantiates a new SubmitDesire.
     * 
     * @param info - the task the agent is currently working on ( wants to submit)
     * @param agent - the agent who wants to submit
     */
    public SubmitDesire(TaskInfo info, BdiAgentV2 agent) {
        super(agent.getBelief());
        //AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start SubmitDesire, Step: " + belief.getStep());
        this.info = info;
        this.agent = agent;
    }
  
    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    //@SuppressWarnings("hiding")
	@Override
    public BooleanInfo isExecutable() {
        boolean result = false;
        String text = "";

        if (belief.getRole().actions().contains(Actions.SUBMIT)) {
            result = belief.getGoalZones().size() > 0 && belief.getGoalZones().contains(new Point(0, 0));
            text = result ? "" : "not in goal zone";

            if (result == true) {
                for (Thing t : this.info.requirements) {
                    Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
                    if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                        result = false;
                    }
                }
            }
            
            if (result == true) {
                if (this.info.requirements.size() > 1) {
                    if (!AgentCooperations.exists(this.info, agent, 1)) {
                        result = false;
                    } else {
                        /*AgentLogger.info(Thread.currentThread().getName()
                                + " runSupervisorDecisions - proofBlockStructure - ist master");*/
                        // Agent is master in a cooperation of this task
                        Cooperation coop = AgentCooperations.get(this.info, agent, 1);

                        if (!(coop.statusMaster().equals(Status.Connected)
                                && coop.statusHelper().equals(Status.Detached)
                                && (coop.statusHelper2().equals(Status.Detached)
                                        || coop.statusHelper2().equals(Status.No2)))) {
                            result = false;
                        }
                    }
                }
            }
        }

        return new BooleanInfo(result, text);
    }
 
	 /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        return ActionInfo.SUBMIT(info.name, String.valueOf(info.requirements.size()));
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }
 
    /**
     * Gets the active task.
     * 
     * @return the task
     */
    public TaskInfo getTask() {
        return info;
    }

    /**
     * Checks if the desire is unfulfillable.
     * 
     * @return if it is unfulfillable or not
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }
}
