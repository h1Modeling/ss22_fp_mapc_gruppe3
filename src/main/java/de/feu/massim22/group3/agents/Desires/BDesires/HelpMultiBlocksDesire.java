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

public class HelpMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    
    public HelpMultiBlocksDesire(Belief belief, TaskInfo info) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeBlocksDesire");
        this.info = info;
    }


	@Override
	public BooleanInfo isExecutable() {
		AgentLogger
				.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.isExecutable");
		if (belief.getRole().actions().contains(Actions.DETACH)
				&& belief.getRole().actions().contains(Actions.ATTACH)
				&& belief.getRole().actions().contains(Actions.CONNECT)) {
			//Ein Block Task
			if(info.requirements.size() == 1) {
				return new BooleanInfo(false, "");
			}
			else {
				return blockStructure(info);
			}
		}
		return new BooleanInfo(false, "");
	}

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.isFulfilled");

        return new BooleanInfo(true, "");
    }

	@Override
	public ActionInfo getNextActionInfo() {
		AgentLogger.info(
				Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.getNextActionInfo");
	
		int distance = foundMeetings.firstKey();
		Meeting firstMeeting = foundMeetings.get(distance);
		
		if(distance <= 3) {
			//Agent1 hat Agent2 in connect-Entfernung
			
			
			
		}else {
			//Agent2 ist noch nicht in connect-Entfernung von Agent1
			String direction = DirectionUtil.getDirection(agent.belief.getPosition(),
			        AgentMeetings.getPositionAgent2(firstMeeting));// 2.Agent muss auf Koordinaten des Ersten umgerechnet werden

			return getActionForMove(direction, getName());
		}
		
        return ActionInfo.SKIP(getName());
	}
    
	public BooleanInfo blockStructure(TaskInfo task) {
		BooleanInfo result = new BooleanInfo(false, "");
		boolean found = false;
		int indexFound = 0;

		for (Thing attachedThing : agent.belief.getAttachedThings()) {
			// ich habe einen passenden 2.Block
			for (int i = 0; i < task.requirements.size(); i++) {
				if (attachedThing.details.equals(task.requirements.get(i).type) 
						&& attachedThing.x != 0
						&& attachedThing.y != 0) {
					found = true;
					break;
				}
			}

			if (found)
				break;
		}

		if (found) {
			for (Meeting meeting : AgentMeetings.find(agent)) {
				if (!agent.belief.getAttachedThings().isEmpty()) {
					for (Thing attachedThing : meeting.agent2().belief.getAttachedThings()) {
						// anderer Agent hat den dazu passenden 1.Block
						for (int i = 0; i < task.requirements.size(); i++) {
							if (task.requirements.get(i).type.equals(attachedThing.details)
									&& (attachedThing.x == 0 && attachedThing.y == 1
											|| attachedThing.x == 0 && attachedThing.y == -1
											|| attachedThing.x == 1 && attachedThing.y == 0
											|| attachedThing.x == -1 && attachedThing.y == 0)) {
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
