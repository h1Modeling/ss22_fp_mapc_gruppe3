package de.feu.massim22.group3.agents.desires.V2;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

import java.awt.Point;

public class SubmitDesire extends BeliefDesire {

    private TaskInfo info;
    private BdiAgentV2 agent;
    
    public SubmitDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - Start SubmitDesire, Step: " + belief.getStep());
        this.info = info;
        this.agent = agent;
    }
  
	@Override
    public BooleanInfo isExecutable() {
        boolean result = false;
        String info = "";

        if (belief.getRole().actions().contains(Actions.SUBMIT)) {
            result = belief.getGoalZones().size() > 0 && belief.getGoalZones().contains(new Point(0, 0));
            info = result ? "" : "not in goal zone";

            if (result == true) {
                for (Thing t : this.info.requirements) {
                    Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
                    if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                        return new BooleanInfo(false, "");
                    }
                }
            }
            
            if (AgentCooperations.exists(this.info, agent, 1)) {
                AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisionsWithTask - proofBlockStructure - ist master");
             // Agent ist als master in einer cooperation 
                Cooperation coop = AgentCooperations.get(this.info, agent, 1);
                
                if (!(coop.statusMaster().equals(Status.Connected) && coop.statusHelper().equals(Status.Detached))) {
                    return new BooleanInfo(false, "");
                }
            }
        }

        return new BooleanInfo(result, info);
    }
 
    @Override
    public ActionInfo getNextActionInfo() {
        return ActionInfo.SUBMIT(info.name, getName());
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }
    
    public TaskInfo getTask() {
        return info;
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }
}
