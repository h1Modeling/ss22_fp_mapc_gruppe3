package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableRoleZone;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoRoleZone extends SubDesire {
    
	public GoRoleZone(BdiAgent agent) {
        super("GoRoleZone", agent);
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
        ReachableRoleZone nearestRoleZone = agent.desireProcessing.getNearestRoleZone(agent.belief.getReachableRoleZones());
        String direction = DirectionUtil.intToString(nearestRoleZone.direction());
        return new Action("move", new Identifier(direction));
    }
    
    @Override
    public boolean isDone() {
        return true;
    }
    
    @Override
    public void setType() {
        //this.subDesireType = SubDesires.DIG_FREE;
    }
}