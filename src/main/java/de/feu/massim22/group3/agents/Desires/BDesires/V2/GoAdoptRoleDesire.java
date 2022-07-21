package de.feu.massim22.group3.agents.Desires.BDesires.V2;

import java.util.ArrayList;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Desires.BDesires.*;
import de.feu.massim22.group3.agents.Reachable.ReachableGoalZone;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;

//import java.awt.Point;

public class GoAdoptRoleDesire extends BeliefDesire {
    BdiAgentV2 agent;
    String role;
    
    BdiAgentV2 roleZoneAgent = null;
    Point posAgentOld;
    Point nearestRoleZoneRoleZoneAgentNew;    
    Point posRoleZoneAgentOld;
    Point realtiveRoleZoneAgentOld;
    Point nearestRoleZone;

    public GoAdoptRoleDesire(Belief belief, BdiAgentV2 agent, String role) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - Start GoAdoptRoleDesire, Step: " + belief.getStep());
        this.agent = agent;
        this.role = role;
    }

    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - GoAdoptRoleDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getRole().name().equals(role);
        String info = result ? "" : "role already adopted";
        return new BooleanInfo(result, info);
    }
    
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - GoAdoptRoleDesire.isExecutable, Step: " + belief.getStep());
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " manageAgentRoles - GoAdoptRoleDesire.getNextActionInfo, Step: " + belief.getStep());
        AgentLogger
                .info(Thread.currentThread().getName() + "GoAdoptRoleDesire - AgentPosition: " + belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - reachableRoleZones: "
                + belief.getReachableRoleZones());
        AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - RoleZones: " + belief.getRoleZones());
        
        Point roleZone = null;

        if (belief.getRoleZones().contains(Point.zero())) {
            // already in rolezone
            AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - Adopt Role");
            return ActionInfo.ADOPT(role, getName());
        } else {
            // not yet in rolezone
            if (belief.getReachableRoleZones().size() > 0 || belief.getRoleZones().size() > 0) {
                AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - RoleZone visible");
                Point p = Point.castToPoint(belief.getNearestRelativeManhattenRoleZone());
                int manhattenDistance = p == null ? 1000 : Math.abs(p.x) + Math.abs(p.y);

                if (manhattenDistance < 1000) {
                    // Manhatten
                    String direction = getDirectionToRelativePoint(p);
                    String dirAlt = "";

                    for (int i = 1; i < belief.getRoleZones().size(); i++) {
                        dirAlt = getDirectionToRelativePoint(belief.getRoleZones().get(i));

                        if (!dirAlt.equals(direction)) {
                            break;
                        }
                    }

                    AgentLogger.info(Thread.currentThread().getName()
                            + "GoRoleZoneDesire - nextActionDirectionManhatten: " + direction);
                    direction = DirectionUtil.proofDirection(direction, agent);
                    this.agent.desireProcessing.lastWishDirection = direction;
                    AgentLogger.info(
                            Thread.currentThread().getName() + "GoRoleZoneDesire - after proofDirection: " + direction);

                    if (dirAlt.equals(""))
                        return agent.desireProcessing.getActionForMove(agent, direction, direction, getName());
                    else
                        return agent.desireProcessing.getActionForMoveWithAlternate(agent, direction, dirAlt, getName());
                }

                // Data from Pathfinding
                ReachableRoleZone zone = belief.getNearestRoleZone();
                String direction = DirectionUtil.intToString(zone.direction());

                if (direction.length() > 0) {
                    AgentLogger.info(Thread.currentThread().getName()
                            + "GoRoleZoneDesire - nextActionDirectionPathfinding: " + direction);
                    direction = DirectionUtil.proofDirection(direction, agent);
                    this.agent.desireProcessing.lastWishDirection = direction;
                    AgentLogger.info(
                            Thread.currentThread().getName() + "GoRoleZoneDesire - after proofDirection: " + direction);
                    if (direction.length() > 1)     
                        return agent.desireProcessing.getActionForMove(agent, direction.substring(0, 1), direction.substring(1, 2), getName());
                    else
                        return agent.desireProcessing.getActionForMove(agent, direction.substring(0, 1), direction.substring(0, 1), getName());
                    
              
                }
            } else {
                // RoleZone from strange agent
                AgentLogger.info(Thread.currentThread().getName() + "GoRoleZoneDesire - RoleZone from strange agent: "
                        + agent.getName());
                boolean found = false;
                nearestRoleZone = getNearestRoleZoneFromMeeting(agent);

                if (nearestRoleZone != null) 
                    found = true;
                else {
                    for (Meeting meeting : AgentMeetings.find(agent)) {
                        roleZone = getNearestRoleZoneFromMeeting(meeting.agent2());
                        
                        if (roleZone != null) {
                            nearestRoleZone = roleZone.translate2To1(meeting);
                            found = true;
                            break; 
                        }
                    }
                }

                if (found) {
                    String direction = DirectionUtil.getDirection(agent.belief.getPosition(), nearestRoleZone);
                    int distance = Point.distance(Point.castToPoint(agent.belief.getPosition()), nearestRoleZone);
                    AgentLogger.info(Thread.currentThread().getName() + "GoRoleZoneDesire - RoleZone from strange agent: "
                                    + nearestRoleZone + " , dir: " + direction + " , dist: " + distance);

                    if (distance < 40) 
                        return getActionForMove(direction, getName());
                }
            }
        }
        
        return ActionInfo.SKIP(getName());
    }

private Point getNearestRoleZoneFromMeeting(BdiAgentV2 inAgent) {       
    AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 3: " + AgentMeetings.find(inAgent) + " , " + AgentMeetings.find(inAgent).size());  
    Point result = null;
    Point roleZone = null;
    int distance = 1000;
    
    for (Meeting meeting : AgentMeetings.find(inAgent)) {
        ArrayList<ReachableRoleZone> rrz = new ArrayList<ReachableRoleZone>(meeting.agent2().belief.getReachableRoleZones());
        AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 4 - agent: " + rrz);
        
        if (!rrz.isEmpty()) {  
            roleZone = Point.castToPoint(meeting.agent2().belief.getNearestRoleZone().position()).translate2To1(meeting);
            AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 5: " + meeting.agent2().belief.getNearestRoleZone() + " , dist: " + Point.distance(Point.castToPoint(inAgent.belief.getPosition()), roleZone) + " , min: " + distance);
            
            if (Point.distance(Point.castToPoint(inAgent.belief.getPosition()), roleZone) < distance) {
                AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 6");  
                roleZoneAgent = meeting.agent2();
                distance = Point.distance(Point.castToPoint(inAgent.belief.getPosition()), roleZone);
            }
        } 
    }
    
    if (distance < 1000)
        result = roleZone;
    
    return result;
}
}
