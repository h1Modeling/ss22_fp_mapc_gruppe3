package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

import java.awt.Point;

public class SubmitDesire extends BeliefDesire {

    private TaskInfo info;
    
    public SubmitDesire(Belief belief, TaskInfo info) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start SubmitDesire, Step: " + belief.getStep());
        this.info = info;
    }
  
	@Override
	public BooleanInfo isExecutable() {
		boolean result = belief.getGoalZones().size() > 0 && belief.getGoalZones().contains(new Point(0, 0));
		String info = result ? "" : "not in goal zone";

		if (belief.getRole().actions().contains(Actions.SUBMIT)){
			if (result == true) {
				for (Thing t : this.info.requirements) {
					Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
					if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
						String ea = atAgent == null ? t.details + " not at agent" : "";
						String et = atAgent != null && !atAgent.type.equals(Thing.TYPE_BLOCK) ? "Attached is no block"
								: "";
						String ed = atAgent != null && !atAgent.details.equals(t.type) ? "Wrong Block attached" : "";
						return new BooleanInfo(false, ea + et + ed);
					}
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

    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }
}
