package de.feu.massim22.group3.agents.Desires.ADesires;

import java.util.*;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoDispenser extends ADesire {
    String type = null;
    List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
    
    public GoDispenser(BdiAgent agent) {
        super("GoDispenser", agent);
    }
    
    public GoDispenser(BdiAgent agent, String type) {
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
    public boolean isExecutable() {
        boolean result = false;
        
        if(agent.belief.getReachableDispensers().size() > 0) {
            //es existiert ein Dispenser  ( den der Agent erreichen kann)
            List<ReachableDispenser> reachableDispensers = agent.belief.getReachableDispensers();
            
            if(type != null) {
                // bestimmter Blocktyp wird gesucht (wie findet man Blocktype?)
                for( ReachableDispenser dispenser : reachableDispensers ) {
                    //alle Dispenser vom gesuchten Typ
                	
                	String typeDispenser = "b" + dispenser.type().toString().substring(10);
                    if(typeDispenser.equals(type)) {
                        typeDispensers.add(dispenser);
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
        ReachableDispenser nearestDispenser = agent.desireProcessing.getNearestDispenser(typeDispensers);
        String direction = DirectionUtil.firstIntToString(nearestDispenser.direction());
        return new Action("move", new Identifier(direction));
    }       
}

