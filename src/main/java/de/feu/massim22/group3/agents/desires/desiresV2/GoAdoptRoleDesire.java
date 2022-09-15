package de.feu.massim22.group3.agents.desires.desiresV2;

import java.util.ArrayList;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.belief.reachable.ReachableRoleZone;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.utilsV2.AgentMeetings;
import de.feu.massim22.group3.agents.utilsV2.Point;
import de.feu.massim22.group3.agents.utilsV2.AgentMeetings.Meeting;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;

//import java.awt.Point;

/**
 * The class <code>GoAdoptRoleDesire</code> models the desire to adopt a role.
 * 
 * @author Melinda Betz
 */
public class GoAdoptRoleDesire extends BeliefDesire {
    private BdiAgentV2 agent;
    private String role;
    private Point nearestRoleZone; 

    /**
     * Instantiates a new GoAdoptRoleDesire.
     * 
     * @param agent - the agent who wants to adopt a role
     * @param role - the role which the agent wants to adopt
     */
    public GoAdoptRoleDesire(BdiAgentV2 agent, String role) {
        super(agent.getBelief());
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - Start GoAdoptRoleDesire, Step: " + belief.getStep());
        this.agent = agent;
        this.role = role;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - GoAdoptRoleDesire.isFulfilled, Step: " + belief. getStep());
        boolean result = belief.getRole().name().equals(role);
        String info = result ? "" : "role already adopted";
        return new BooleanInfo(result, info);
    }
    
    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + " manageAgentRoles - GoAdoptRoleDesire.isExecutable, Step: " + belief.getStep());
        return new BooleanInfo(true, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName()
                + " manageAgentRoles - GoAdoptRoleDesire.getNextActionInfo, Step: " + belief.getStep());
        AgentLogger
                .info(Thread.currentThread().getName() + "GoAdoptRoleDesire - AgentPosition: " + belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - reachableRoleZones: "
                + belief.getReachableRoleZonesX());
        AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - RoleZones: " + belief.getRoleZones());
        
        Point roleZone = null;

        if (belief.getRoleZones().contains(Point.zero())) {
            // already in rolezone
            AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - Adopt Role");
            return ActionInfo.ADOPT(role, getName());
        } else {
            // not yet in rolezone
            if (belief.getReachableRoleZonesX().size() > 0 || belief.getRoleZones().size() > 0) {
                AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - RoleZone visible");
                Point p = Point.castToPoint(belief.getNearestRelativeManhattanRoleZone());
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
                ReachableRoleZone zone = belief.getReachableRoleZonesX().get(0);
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
                    String direction = DirectionUtil.getDirection(agent.getBelief().getPosition(), nearestRoleZone);
                    int distance = Point.distance(Point.castToPoint(agent.getBelief().getPosition()), nearestRoleZone);
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
        ArrayList<ReachableRoleZone> rrz = new ArrayList<ReachableRoleZone>(meeting.agent2().getBelief().getReachableRoleZonesX());
        AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 4 - agent: " + rrz);
        
        if (!rrz.isEmpty()) {  
            roleZone = Point.castToPoint(meeting.agent2().getBelief().getReachableRoleZonesX().get(0).position()).translate2To1(meeting);
            AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 5: " + meeting.agent2().getBelief().getReachableRoleZonesX().get(0) + " , dist: " + Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), roleZone) + " , min: " + distance);
            
            if (Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), roleZone) < distance) {
                AgentLogger.info(Thread.currentThread().getName() + " Test.RoleZone 6");  
                //roleZoneAgent = meeting.agent2();
                distance = Point.distance(Point.castToPoint(inAgent.getBelief().getPosition()), roleZone);
            }
        } 
    }
    
    if (distance < 1000)
        result = roleZone;
    
    return result;
}
}
