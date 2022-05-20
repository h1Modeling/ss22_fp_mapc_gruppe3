package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.Belief.ReachableDispenser;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoDispenser extends Desire {
	String type = null;
	List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
	
    GoDispenser(BdiAgentV2 agent) {
        super("GoDispenser", agent);
    }
    
    GoDispenser(BdiAgentV2 agent, String type) {
        super("GoDispenser", agent);
        this.type = type;
    }

    /**
     * The method proves if a certain Desire is possible.
     *
     * @param desire -  the desire that has to be proven 
     * 
     * @return boolean - the desire is possible or not
     */
    @Override
    public boolean isExecutable(Desire desire) {
    	boolean result = false;
    	
    	if(agent.belief.getReachableDispensers().size() > 0) {
    		//es existiert ein Dispenser  ( den der Agent erreichen kann)
    		List<ReachableDispenser> reachableDispensers = agent.belief.getReachableDispensers();
    		
    		if(type != null) {
    			// bestimmter Blocktyp wird gesucht
    			for( ReachableDispenser typeDispenser : reachableDispensers ) {
    				//alle Dispenser vom gesuchten Typ
    				if(typeDispenser.type().toString().equals(type)) {
    					typeDispensers.add(typeDispenser);
    				}
    			}
    			if(typeDispensers.size() > 0) {
    				//es wurde ein Dispenser vom gesuchten Typ gefunden
    				result = true;
    			}
    		}
    		else {
    			// es wird kein Typ gesucht
    			typeDispensers.addAll(reachableDispensers);
    			result = true;
    			
    		}
    	}
    	return result;
    }
    
    /**
     * The method returns the nextAction that is needed.
     * 
     * @return Action - the action that is needed
     * 
     **/
    @Override
	public Action getNextAction() {
		// Dispenser mit der kürzesten Entfernung zum Agenten
    	ReachableDispenser nearestDispenser = getNearestDispenser(typeDispensers);
		// Richtung zu Dispenser To Do : Hindernissprüfung
		DirectionUtil.getDirection(agent.belief.getPosition(), nearestDispenser.position());
		String direction = DirectionUtil.intToString(nearestDispenser.direction());
		Point p = DirectionUtil.getCellInDirection(direction);
		return new Action("move", new Identifier(String.valueOf(p.x)), new Identifier(String.valueOf(p.y)));
	}
    
    ReachableDispenser getNearestDispenser(List<ReachableDispenser> cellList) {
        int distance = 1000;
        ReachableDispenser result = null;
        
        for (ReachableDispenser cell : (List<ReachableDispenser>) cellList) {
            if (cell.distance() < distance) {
                distance = cell.distance();
                result = cell;
            }
        }
        return result;
    }

}
