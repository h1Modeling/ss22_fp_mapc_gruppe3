package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.AgentMeetings;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class ArrangeMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private Map<Integer, Meeting> foundMeetings = new TreeMap<>();
    
    public ArrangeMultiBlocksDesire(Belief belief, TaskInfo info) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeMultiBlocksDesire");
        this.info = info;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeMultiBlocksDesire.isFulfilled");
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {            	
                return new BooleanInfo(false, "");
            }
        }
        return new BooleanInfo(true, "");
    }

	@Override
	public BooleanInfo isExecutable() {
		AgentLogger
				.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeMultiBlocksDesire.isExecutable");
		if (belief.getRole().actions().contains(Actions.DETACH)
				&& belief.getRole().actions().contains(Actions.ATTACH)
				&& belief.getRole().actions().contains(Actions.CONNECT)) {
			//2 Block Tasks
			if(info.requirements.size() == 2) {
				return proofBlockStructure(info);
			}
		}
		return new BooleanInfo(false, "");
	}

    @Override
	public ActionInfo getNextActionInfo() {
		AgentLogger.info(Thread.currentThread().getName()
				+ " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo");
		Point taskBlock = new Point(info.requirements.get(0).x, info.requirements.get(0).y);
		Point agentBlock = agent.getAttachedPoints().get(0);
		Thing agentThing = agent.getAttachedThings().get(0);

		AgentLogger.info(Thread.currentThread().getName()
				+ " runSupervisorDecisions - ArrangeMultiBlocksDesire.getNextActionInfo agentBlocks: "
				+ agent.getAttachedThings() + " , taskBlocks: " + info.requirements);

		if (!agentThing.details.equals(info.requirements.get(0).type)) {
			return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(agentBlock)),
					getName());
		}
		//TODO
		if (taskBlock.equals(agentBlock)) {
			
			return ActionInfo.CONNECT(getName(), agentBlock, getName());
		} else {
			String clockDirection = DirectionUtil.getClockDirection(agentBlock, taskBlock);

			if (clockDirection == "") {
				return ActionInfo.SKIP(getName());
			} else {
				Thing cw = belief.getThingCRotatedAt(agentBlock);
				Thing ccw = belief.getThingCCRotatedAt(agentBlock);

				if (clockDirection == "cw") {
					if (isFree(cw)) {
						return ActionInfo.ROTATE_CW(getName());
					} else {
						if (cw.type.equals(Thing.TYPE_OBSTACLE)) {
							Point target = DirectionUtil.rotateCW(agentBlock);
							return ActionInfo.CLEAR(target, getName());
						}
					}
				}
				if (clockDirection == "ccw") {
					if (isFree(ccw)) {
						return ActionInfo.ROTATE_CCW(getName());
					} else {
						if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
							Point target = DirectionUtil.rotateCCW(agentBlock);
							return ActionInfo.CLEAR(target, getName());
						}
					}
				}
				return ActionInfo.SKIP(getName());
			}
		}
	}
    
	public BooleanInfo proofBlockStructure(TaskInfo task) {
		BooleanInfo result = new BooleanInfo(false, "");
		boolean found = false;
		int indexFound = 0;

		for (Thing attachedThing : agent.getAttachedThings()) {
			// ich habe einen passenden Block
			for (int i = 0; i < task.requirements.size(); i++) {
				if (task.requirements.get(i).type.equals(attachedThing.details)
						&& (attachedThing.x == 0 && attachedThing.y == 1
								|| attachedThing.x == 0 && attachedThing.y == -1
								|| attachedThing.x == 1 && attachedThing.y == 0
								|| attachedThing.x == -1 && attachedThing.y == 0)) {
					found = true;
					indexFound = i;
					break;
				}
			}
			
			if (found) break;
		}
		
		if (found) {
			for (Meeting meeting : AgentMeetings.find(agent)) {
				if (!meeting.agent2().getAttachedThings().isEmpty()) {
					for (Thing attachedThing2 : meeting.agent2().getAttachedThings()) {
						// anderer Agent hat den Block der mir noch fehlt
						for (int i = 0; i < task.requirements.size(); i++) {
							if (i != indexFound && attachedThing2.details.equals(task.requirements.get(i).type)) {
								result = new BooleanInfo(true, "");
								foundMeetings.put(AgentMeetings.getDistance(meeting), meeting);
								break;
							}
						}
						
						if (result.value()) break;
					}
				}
			}
		}	

		return result;
	}
}
