package de.feu.massim22.group3.agents;

import java.awt.Point;
import de.feu.massim22.group3.agents.Belief.*;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoRoleZone extends ADesire {
    
    GoRoleZone(BdiAgentV2 agent, DesireUtilities desireProcessing) {
        super("GoRoleZone", agent, desireProcessing);
    }

    /**
     * The method proves if a certain Desire is possible.
     *
     * @param desire -  the desire that has to be proven 
     * 
     * @return boolean - the desire is possible or not
     */
    @Override
    public boolean isExecutable() {
        //es existiert eine roleZone ( die der Agent erreichen kann)
        if(agent.belief.getReachableRoleZones().size() > 0)
            return true;
        else {
            return false;
        }
    }
    
    /**
     * The method returns the nextAction that is needed.
     * 
     * @return Action - the action that is needed
     * 
     **/
    @Override
    public Action getNextAction() {
        // roleZone mit der kürzesten Entfernung zum Agenten
        ReachableRoleZone nearestRoleZone = desireProcessing.getNearestRoleZone(agent.belief.getReachableRoleZones());
        // Richtung zu roleZone To Do : Hindernissprüfung
        DirectionUtil.getDirection(agent.belief.getPosition(), nearestRoleZone.position());
        String direction = DirectionUtil.intToString(nearestRoleZone.direction());
        Point p = DirectionUtil.getCellInDirection(direction);
        return new Action("move", new Identifier(String.valueOf(p.x)), new Identifier(String.valueOf(p.y)));
    }
}