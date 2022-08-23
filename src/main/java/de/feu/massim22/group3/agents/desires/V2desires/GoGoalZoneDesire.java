package de.feu.massim22.group3.agents.desires.V2desires;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings;
import de.feu.massim22.group3.agents.V2utils.Point;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;
import java.util.*;

//import java.awt.Point;

/**
 * The class <code>GoGoalZoneDesire</code> models the desire to go to (into) a goal zone.
 * 
 * @author Melinda Betz
 */
public class GoGoalZoneDesire extends BeliefDesire {
    BdiAgentV2 agent;
    
    BdiAgentV2 goalZoneAgent = null;
    Point nearestGoalZone;
    boolean defaultGoalZone = false;
    Point posDefaultGoalZone1;
    Point posDefaultGoalZone2;

    /**
     * Instantiates a new GoGoalZoneDesire.
     * 
     * @param belief the belief of the agent
     * @param agent the agent who wants to go to a goal zone
     * 
     */
    public GoGoalZoneDesire(Belief belief, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoGoalZoneDesire, Step: " + belief.getStep());
        this.agent = agent;
        posDefaultGoalZone1 = this.agent.desireProcessing.posDefaultGoalZone1;
        posDefaultGoalZone2 = this.agent.desireProcessing.posDefaultGoalZone2;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - GoGoalZoneDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getGoalZones().contains(Point.zero());
        String info = result ? "" : "not on goal zone";
        return new BooleanInfo(result, info);
    }
    
    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName()
                + " runSupervisorDecisions - GoGoalZoneDesire.isExecutable, Step: " + belief.getStep());
        BooleanInfo resultBack = null;
        boolean result = false;
        Point goalZone = null;

        if (belief.getRole().actions().contains(Actions.DETACH)
                && belief.getRole().actions().contains(Actions.ATTACH)) {
            result = belief.getReachableGoalZonesX().size() > 0 || belief.getGoalZones().size() > 0;
            AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 2");

            if (!result) {
                nearestGoalZone = getNearestGoalZoneFromMeeting(agent);

                if (nearestGoalZone != null) 
                    result = true;
                else {
                    for (Meeting meeting : AgentMeetings.find(agent)) {
                        goalZone = getNearestGoalZoneFromMeeting(meeting.agent2());
                        
                        if (goalZone != null) {
                            nearestGoalZone = goalZone.translate2To1(meeting);
                            result = true;
                            break;                                                     
                        }
                    }
                }
            }
         
            if (posDefaultGoalZone1 != null || posDefaultGoalZone2 != null) {
                defaultGoalZone = true;
                result = true;

                if (posDefaultGoalZone1 != null || posDefaultGoalZone2 == null) {
                    nearestGoalZone = posDefaultGoalZone1;
                } else {
                    if (posDefaultGoalZone1 == null || posDefaultGoalZone2 != null) {
                        nearestGoalZone = posDefaultGoalZone2;
                    } else {
                        if (Point.distance(Point.castToPoint(agent.getBelief().getPosition()), posDefaultGoalZone1) 
                                < Point.distance(Point.castToPoint(agent.getBelief().getPosition()), posDefaultGoalZone2))
                            nearestGoalZone = posDefaultGoalZone1;
                        else
                            nearestGoalZone = posDefaultGoalZone2;
                    }
                }
            }
        }

        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 7");
        String info = result ? "" : "no reachable goal zones, no role, ...";
        resultBack = new BooleanInfo(result, info);

        return resultBack;
    }
    
    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        if (goalZoneAgent == null && !defaultGoalZone) {
            AgentLogger.info(Thread.currentThread().getName()
                    + " runSupervisorDecisions - GoGoalZoneDesire.getNextActionInfo, Step: " + belief.getStep());
            AgentLogger.info(
                    Thread.currentThread().getName() + " GoGoalZoneDesire - AgentPosition: " + belief.getPosition());
            //AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - reachableGoalZones: "
               //     + belief.getReachableGoalZonesX());
            //AgentLogger
               //     .info(Thread.currentThread().getName() + " GoGoalZoneDesire - GoalZones: " + belief.getGoalZones());

            Point p = Point.castToPoint(belief.getNearestRelativeManhattanGoalZone());
            int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);

            if (manhattenDistance < 1000) {
                // Manhatten
                String direction = getDirectionToRelativePoint(p);
                String dirAlt = "";
                Point gz = null;

                for (int i = 1; i < belief.getGoalZones().size(); i++) {
                    dirAlt = getDirectionToRelativePoint(belief.getGoalZones().get(i));
                    gz = Point.castToPoint(belief.getGoalZones().get(i));

                    if (!dirAlt.equals(direction)) {
                        break;
                    }
                }

                AgentLogger.info(Thread.currentThread().getName() + " GoGoalZoneDesire - nextActionDirectionManhatten: "
                        + direction + " , " + gz);
                direction = proofDirection(direction);
                this.agent.desireProcessing.lastWishDirection = direction;
                AgentLogger.info(
                        Thread.currentThread().getName() + " GoGoalZoneDesire - after proofDirection: " + direction);

                if (dirAlt.equals(""))
                    return agent.desireProcessing.getActionForMove(agent, direction, getName());
                else
                    return agent.desireProcessing.getActionForMoveWithAlternate(agent, direction, dirAlt, getName());
            }

            // Data from Pathfinding
            //ReachableGoalZone zone = belief.getNearestGoalZone();
            ReachableGoalZone zone = belief.getReachableGoalZonesX().get(0);
            String direction = DirectionUtil.intToString(zone.direction());

            if (direction.length() > 0) {
                AgentLogger.info(Thread.currentThread().getName()
                        + " GoGoalZoneDesire - nextActionDirectionPathfinding: " + direction + " , " + zone);
                direction = proofDirection(direction);
                this.agent.desireProcessing.lastWishDirection = direction;
                AgentLogger.info(
                        Thread.currentThread().getName() + " GoGoalZoneDesire - after proofDirection: " + direction);
                return getActionForMove(direction.substring(0, 1), getName());
            }
        } else {
            if (goalZoneAgent != null) 
            // GoalZone from strange agent
                AgentLogger.info(Thread.currentThread().getName() + " GoGoalZoneDesire - GoalZone from strange agent: "
                    + goalZoneAgent.getName() + " , " + goalZoneAgent.getBelief().getPosition());
            else
             // default GoalZone 
                AgentLogger.info(Thread.currentThread().getName() + "GoGoalZoneDesire - GoalZone from strange agent: "
                        + agent.getName() + " , " + agent.getBelief().getPosition());
                       
            String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), nearestGoalZone);
            int distance = Point.distance(Point.castToPoint(agent.getBelief().getPosition()), nearestGoalZone);
            AgentLogger.info(Thread.currentThread().getName() + " GoGoalZoneDesire - GoalZone from strange agent: "
               + ((goalZoneAgent != null) ? goalZoneAgent.getName() : null) + " , nearestGoalZone: " + nearestGoalZone + " , dir: " + direction + " , dist: " + distance);
            
            if (distance < 40)
                return getActionForMove(direction, getName());
        }

        return ActionInfo.SKIP(getName());
    }
    
    private String proofDirection(String inDirection) {
        String outDirection = inDirection;
 
        if (this.agent.desireProcessing.lastWishDirection != null) {
            if (belief.getLastAction().equals(Actions.MOVE) && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                if (!belief.getLastActionParams().get(0).equals(this.agent.desireProcessing.lastWishDirection)) {
                    if (belief.getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(inDirection))) {
                        outDirection = this.agent.desireProcessing.lastWishDirection;
                    }
                }
            }
        }
        
        return outDirection;
    }
    
    private Point getNearestGoalZoneFromMeeting(BdiAgentV2 inAgent) {       
        AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 3: " + AgentMeetings.find(inAgent) + " , " + AgentMeetings.find(inAgent).size());  
        Point result = null;
        Point goalZone = null;
        int distance = 1000;
        
        for (Meeting meeting : AgentMeetings.find(inAgent)) {
            ArrayList<ReachableGoalZone> rgz = new ArrayList<ReachableGoalZone>(meeting.agent2().getBelief().getReachableGoalZonesX());
            AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 4 - agent: " + rgz);
            
            if (!rgz.isEmpty()) {               
                goalZone = Point.castToPoint(meeting.agent2().getBelief().getReachableGoalZonesX().get(0).position()).translate2To1(meeting);
                AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 5: " + meeting.agent2().getBelief().getReachableGoalZonesX().get(0) + " , dist: " + Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), goalZone) + " , min: " + distance);
                
                if (Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), goalZone) < distance) {
                    AgentLogger.info(Thread.currentThread().getName() + " Test.GoalZone 6");  
                    goalZoneAgent = meeting.agent2();
                    distance = Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), goalZone);
                }
            } 
        }
        
        if (distance < 1000)
            result = goalZone;
        
        return result;
    }
}
