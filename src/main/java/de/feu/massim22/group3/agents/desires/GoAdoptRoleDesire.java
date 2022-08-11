package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableRoleZone;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;

//import java.awt.Point;

public class GoAdoptRoleDesire extends BeliefDesire {
    BdiAgentV2 agent;
    String role;

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

        if (belief.getRoleZones().contains(Point.zero())) {
            // already in rolezone
            AgentLogger.info(Thread.currentThread().getName() + "GoAdoptRoleDesire - Adopt Role");
            return ActionInfo.ADOPT(role, getName());
        } else {
            // not yet in rolezone
            if (belief.getReachableRoleZones().size() > 0 || belief.getRoleZones().size() > 0) {
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
            }
        }
        
        return ActionInfo.SKIP(getName());
    }
}
