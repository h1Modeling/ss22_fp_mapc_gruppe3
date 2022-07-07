package de.feu.massim22.group3.agents.Desires.BDesires;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.AgentMeetings;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import de.feu.massim22.group3.agents.Point;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class HelpMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    
    public HelpMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeBlocksDesire");
        this.info = info;
        this.agent = agent;
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
		boolean rotated= false;
		boolean connect = false;
		
		if(distance <= 3) {
			//Agent1 hat Agent2 in connect-Entfernung#
			int dist = 100;
			int nearest = 0;
			ArrayList<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();
			
			for(int i = 0; i < dirs.size(); i++) {
				if(Point.distance(Point.castToPoint(agent.belief.getPosition()), AgentMeetings.getPositionAgent2(firstMeeting)
						.add(new Point (info.requirements.get(1).x, info.requirements.get(1).y ))
						.add(Point.castToPoint(dirs.get(i)))) < dist) {
					dist = Point.distance(Point.castToPoint(agent.belief.getPosition()), AgentMeetings.getPositionAgent2(firstMeeting)
							.add(new Point (info.requirements.get(1).x, info.requirements.get(1).y ))
							.add(Point.castToPoint(dirs.get(i))));
					nearest = i;
				}
			}
			
			if(agent.belief.getPosition() == AgentMeetings.getPositionAgent2(firstMeeting)
					.add(new Point (info.requirements.get(1).x, info.requirements.get(1).y)).add(Point.castToPoint(dirs.get(nearest))))
					 {
				//Agent2 steht an einer der Positionen
				rotated = true;
				 return ActionInfo.ROTATE_CW(getName());
				 //|| agent.belief.getPosition() == AgentMeetings.getPositionAgent2(firstMeeting).add(new Point (1,2))
					//|| agent.belief.getPosition() == AgentMeetings.getPositionAgent2(firstMeeting).add(new Point (-1,2)))
				//die beiden Agents machen ein Connect
					
					/*if(rotated) {
						connect = true;
						return ActionInfo.CONNECT(getName());
					}
					
					//danach dettached Agent2 seinen Block
					if(connect) {
						return ActionInfo.DETACH(firstMeeting.agent2().belief.toString(), getName());
					}*/
			}else {
				
			}
			
			
		}else {
			//Agent2 ist noch nicht in connect-Entfernung von Agent1
			String direction = DirectionUtil.getDirection(agent.belief.getPosition(),
			        AgentMeetings.getPositionAgent2(firstMeeting));

			return getActionForMove(direction, getName());
		}
		
        return ActionInfo.SKIP(getName());
	}
    
	public BooleanInfo blockStructure(TaskInfo task) {
		BooleanInfo result = new BooleanInfo(false, "");
		boolean found = false;
		int indexFound = 0;

		for (Thing attachedThing : agent.getAttachedThings()) {
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
				if (!agent.getAttachedThings().isEmpty()) {
					for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
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
