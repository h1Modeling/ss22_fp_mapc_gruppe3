package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.AgentMeetings;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.messages.scenario.Actions;

//import java.awt.Point;

public class GoGoalZoneDesire extends BeliefDesire {
    BdiAgentV2 agent;
    
    BdiAgentV2 goalZoneAgent = null;
    Point posAgentOld;
    Point nearestGoalZoneGoalZoneAgentNew;    
    Point posGoalZoneAgentOld;
    Point realtiveGoalZoneAgentOld;

    public GoGoalZoneDesire(Belief belief, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoGoalZoneDesire, Step: " + belief.getStep());
        this.agent = agent;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoGoalZoneDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getGoalZones().contains(Point.zero());
        String info = result ? "" : "not on goal zone";
        return new BooleanInfo(result, info);
    }
    
	@Override
	public BooleanInfo isExecutable() {
		AgentLogger.info(Thread.currentThread().getName()
				+ " runSupervisorDecisions - GoGoalZoneDesire.isExecutable, Step: " + belief.getStep());
		BooleanInfo resultBack = null;
		boolean result = false;
		int distance = 1000;
	
		if (belief.getRole().actions().contains(Actions.DETACH)
				&& belief.getRole().actions().contains(Actions.ATTACH)) {
			result = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
	        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 2"); 			
			
	        if (!result) {
		        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 3: " + AgentMeetings.find(agent) + " , " + AgentMeetings.find(agent).size());  
			    
		        for (Meeting meeting : AgentMeetings.find(agent)) {
                    AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 4 - agent: " + meeting.agent2().getName() + " , rgz: " + meeting.agent2().belief.getReachableGoalZones());
			        
                    if (!meeting.agent2().belief.getReachableGoalZones().isEmpty()) {
                        Point p1 = new Point(meeting.posAgent1());
                        Point p2 = new Point(meeting.posAgent2());
                        Point p3 = new Point(meeting.relAgent2());
                        Point nearestGoalZone = Point.castToPoint(meeting.agent2().belief.getNearestGoalZone().position())
                                .add(p1.add(p3.sub(p2)));
                        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 5: " + meeting.agent2().belief.getNearestGoalZone() + " , dist: " + Point.distance(Point.castToPoint(agent.belief.getPosition()), nearestGoalZone) + " , min: " + distance);
	                    
	                    if (Point.distance(Point.castToPoint(agent.belief.getPosition()), nearestGoalZone) < distance) {
			                AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 6");  
                            posAgentOld = new Point(meeting.posAgent1());
                            goalZoneAgent = meeting.agent2();
    			            posGoalZoneAgentOld = new Point(meeting.posAgent2());
    			            realtiveGoalZoneAgentOld = new Point(meeting.relAgent2());
                            nearestGoalZoneGoalZoneAgentNew = Point.castToPoint(meeting.agent2().belief.getNearestGoalZone().position());
                            distance = Point.distance(Point.castToPoint(agent.belief.getPosition()), nearestGoalZone);
    	                    result = true;
			            }
			        }
			    }
			}
		}
		
        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 7");             
        String info = result ? "" : "no reachable goal zones, no role, ...";
        resultBack = new BooleanInfo(result, info);
        
		return resultBack;
	}
    
    @Override
    public ActionInfo getNextActionInfo() {
        if (goalZoneAgent == null) {
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - GoGoalZoneDesire.getNextActionInfo, Step: " + belief.getStep());
            AgentLogger.info(
                    Thread.currentThread().getName() + "GoGoalZoneDesire - AgentPosition: " + belief.getPosition());
            AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - reachableGoalZones: "
                    + belief.getReachableGoalZones());
            AgentLogger
                    .info(Thread.currentThread().getName() + "GoGoalZoneDesire - GoalZones: " + belief.getGoalZones());

            Point p = Point.castToPoint(belief.getNearestRelativeManhattenGoalZone());
            int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);

            if (manhattenDistance < 1000) {
                // Manhatten
                String direction = getDirectionToRelativePoint(p);
                String dirAlt = "";

                for (int i = 1; i < belief.getGoalZones().size(); i++) {
                    dirAlt = getDirectionToRelativePoint(belief.getGoalZones().get(i));

                    if (!dirAlt.equals(direction)) {
                        break;
                    }
                }

                AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - nextActionDirectionManhatten: "
                        + direction);
                direction = proofDirection(direction);
                this.agent.desireProcessing.lastWishDirection = direction;
                AgentLogger.info(
                        Thread.currentThread().getName() + "GoGoalZoneDesire - after proofDirection: " + direction);

                if (dirAlt.equals(""))
                    return getActionForMove(direction, getName());
                else
                    return getActionForMoveWithAlternate(direction, dirAlt, getName());
            }

            // Data from Pathfinding
            ReachableGoalZone zone = belief.getNearestGoalZone();
            String direction = DirectionUtil.intToString(zone.direction());

            if (direction.length() > 0) {
                AgentLogger.info(Thread.currentThread().getName()
                        + "GoGoalZoneDesire - nextActionDirectionPathfinding: " + direction);
                direction = proofDirection(direction);
                this.agent.desireProcessing.lastWishDirection = direction;
                AgentLogger.info(
                        Thread.currentThread().getName() + "GoGoalZoneDesire - after proofDirection: " + direction);
                return getActionForMove(direction.substring(0, 1), getName());
            }
        } else {
            // GoalZone from strange agent
            AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - GoalZone from strange agent: "
                    + agent.getName());

            Point nearestGoalZoneAgentNew = nearestGoalZoneGoalZoneAgentNew
                    .add(posAgentOld.add(realtiveGoalZoneAgentOld.sub(posGoalZoneAgentOld)));
            String direction = DirectionUtil.getDirection(agent.belief.getPosition(), nearestGoalZoneAgentNew);
            AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - GoalZone from strange agent: "
                    + nearestGoalZoneAgentNew + " , dir: " + direction);
            return getActionForMove(direction, getName());
        }

        return ActionInfo.SKIP(getName());
    }
    
    private String proofDirection(String inDirection) {
        String outDirection = inDirection;
 
        if (this.agent.desireProcessing.lastWishDirection != null) {
            if (belief.getLastAction().equals("move") && belief.getLastActionResult().equals("success")) {
                if (!belief.getLastActionParams().get(0).equals(this.agent.desireProcessing.lastWishDirection)) {
                    if (belief.getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(inDirection))) {
                        outDirection = this.agent.desireProcessing.lastWishDirection;
                    }
                }
            }
        }
        
        return outDirection;
    }
}
