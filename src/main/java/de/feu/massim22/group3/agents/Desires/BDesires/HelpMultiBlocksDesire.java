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
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class HelpMultiBlocksDesire extends BeliefDesire {

    private TaskInfo info;    
    private BdiAgentV2 agent;
    private TreeMap<Integer, Meeting> foundMeetings = new TreeMap<>();
    private boolean blockStructureOk = false;    
    private int distanceAgent;
    private Meeting nearestMeeting;
    private boolean onTarget;
    private int distanceNearestTarget;
    private int nearestTarget;
    private Point myBlock;
    private Point block1;
    private Point block2;
    private String dirBlock2;
    
    public HelpMultiBlocksDesire(Belief belief, TaskInfo info, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start HelpMultiBlocksDesire");
        this.info = info;
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isFulfilled");

        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.isExecutable");
        onTarget = false;

        if (belief.getRole().actions().contains(Actions.DETACH) 
                && belief.getRole().actions().contains(Actions.ATTACH)
                && belief.getRole().actions().contains(Actions.CONNECT)) {
            // Mehr Block Task
            if (info.requirements.size() == 2) {
                // Die Blöcke für die Task sind vorhanden
                if (proofBlockStructure(info)) {
                    distanceAgent = foundMeetings.firstKey();
                    nearestMeeting = foundMeetings.get(distanceAgent);
                    distanceNearestTarget = 100;
                    nearestTarget = 0;

                    if (distanceAgent <= 3) {
                        // Agent1 hat Agent2 in connect-Entfernung
                        ArrayList<java.awt.Point> dirs = DirectionUtil.getCellsIn4Directions();

                        for (int i = 0; i < dirs.size(); i++) {
                            Thing t = nearestMeeting.agent2().belief
                                    .getThingAt(new Point(block2.add(Point.castToPoint(dirs.get(i)))));
                            int distanceTarget = Point.distance(Point.castToPoint(agent.belief.getPosition()),
                                    AgentMeetings.getPositionAgent2(nearestMeeting).add(block2)
                                            .add(Point.castToPoint(dirs.get(i))));

                            if (distanceTarget == 0) {
                                onTarget = true;
                                dirBlock2 = DirectionUtil.intToString(i);
                                break;
                            } else {
                                if (isFree(t) && distanceTarget < distanceNearestTarget) {
                                    distanceNearestTarget = distanceTarget;
                                    nearestTarget = i;
                                }
                            }
                        }
                    }
                    
                    return new BooleanInfo(true, "");
                } else
                    // Die Blöcke für die Task sind nicht vorhanden
                    return new BooleanInfo(false, "");
            }
        }

        return new BooleanInfo(false, "");
    }

	@Override
	public ActionInfo getNextActionInfo() {
		AgentLogger.info(
				Thread.currentThread().getName() + " runSupervisorDecisions - HelpMultiBlocksDesire.getNextActionInfo");
	
        if (onTarget) {     //Agent2 steht auf einer der Target-Positionen für den Connect
            if (DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)).equals(dirBlock2)) {
                //Block ist bereits an der richtigen Position
                if (agent.connected) {
                    agent.connected = false;
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(myBlock)), getName());
                } else {
                    agent.connected = true;
                    return ActionInfo.CONNECT(nearestMeeting.agent2().getName(), myBlock, getName());                    
                }
            } else {
                //Block muss noch gedreht werden
            	Point taskBlock = Point.castToPoint(DirectionUtil.getCellInDirection(dirBlock2));
                Point agentBlock = myBlock;
               // Thing agentThing = agent.getAttachedThings().get(0);
                
               /* if (!agentThing.details.equals(info.requirements.get(0).type)) {
                    return ActionInfo.DETACH(DirectionUtil.intToString(DirectionUtil.getDirectionForCell(agentBlock)), getName());
                }*/
                
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
                                Point target = Point.castToPoint(DirectionUtil.rotateCW(agentBlock));
                                return ActionInfo.CLEAR(target, getName());
                            }
                        }
                    }
                    if (clockDirection == "ccw") {
                        if (isFree(ccw)) {
                            return ActionInfo.ROTATE_CCW(getName());
                        } else {
                            if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                                Point target = Point.castToPoint(DirectionUtil.rotateCCW(agentBlock));
                                return ActionInfo.CLEAR(target, getName());
                            }
                        }
                    }
                    return ActionInfo.SKIP(getName());
                }
            }
        } else {
            if (distanceNearestTarget < 100) {
              //gehe zur Target-Position für den Connect
            	
            	
            } else {
             //gehe Richtung Agent  
            	
            	
            }
        }
       return ActionInfo.SKIP(getName());  
	}
    
	public boolean proofBlockStructure(TaskInfo task) {
		boolean result = false;
		boolean found = false;
		int indexFound = 0;

		for (Thing attachedThing : agent.getAttachedThings()) {
			// ich habe einen passenden 2.Block
			for (int i = 0; i < task.requirements.size(); i++) {
			    if (task.requirements.get(i).type.equals(attachedThing.details)
                        && task.requirements.get(i).x != 0
                        && task.requirements.get(i).y != 0
						&& (attachedThing.x == 0
						|| attachedThing.y == 0)) {
					found = true;
					myBlock = new Point(attachedThing.x, attachedThing.y);
					block2 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
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
			                        && (task.requirements.get(i).x == 0
			                        || task.requirements.get(i).y == 0)
			                        && (attachedThing.x == 0
			                        || attachedThing.y == 0)) {
								result = true;
								foundMeetings.put(AgentMeetings.getDistance(meeting), meeting);
			                    block1 = new Point(task.requirements.get(i).x, task.requirements.get(i).y);
								break;
							}
						}
						
						if (result) break;
					}
				}
			}
		}

		blockStructureOk = result;
		return result;
	}
}
