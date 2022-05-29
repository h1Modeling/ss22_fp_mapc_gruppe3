package de.feu.massim22.group3.agents.Desires.ADesires;

import java.util.*;
import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Subject.Type;
import massim.protocol.data.Thing;
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

        if (agent.belief.getReachableDispensers().size() > 0) {
            // es existiert ein Dispenser ( den der Agent erreichen kann)
            List<ReachableDispenser> reachableDispensers = agent.belief.getReachableDispensers();

            if (type != null) {
                // bestimmter Blocktyp wird gesucht (wie findet man Blocktype?)
                if (!agent.desireProcessing.analysisDone) {
                    agent.desireProcessing.analyseAttachedThings();
                    agent.desireProcessing.analysisDone = true;
                }

                if (agent.desireProcessing.goodPositionBlocks.size() > 0 
                        && agent.desireProcessing.badBlocks.size() == 0
                        && agent.desireProcessing.missingBlocks.size() > 0) {

                    for (ReachableDispenser dispenser : reachableDispensers) {
                        // alle Dispenser vom gesuchten Typ

                        String typeDispenser = "b" + dispenser.type().toString().substring(10);
                        if (typeDispenser.equals(type)) {
                            typeDispensers.add(dispenser);
                        }
                    }

                    if (typeDispensers.size() > 0) {
                        // es wurde ein Dispenser vom gesuchten Typ gefunden
                        result = true;
                    }
                }
            } else {
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
        boolean attachPossible = false;
        // Dispenser mit der kürzesten Entfernung zum Agenten
        ReachableDispenser nearestDispenser = agent.desireProcessing.getNearestDispenser(typeDispensers);
        Point dispenserItself = DirectionUtil.getDispenserItself(nearestDispenser);
        int distance = Math.abs(dispenserItself.x - agent.belief.getPosition().x) + Math.abs(dispenserItself.y - agent.belief.getPosition().y);
        
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName() + " , Pos: " + agent.belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - dNearest: " + nearestDispenser.position() + nearestDispenser.data() + " , dItself: " + dispenserItself);
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName() + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());

        if (distance == 1) {
        	AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName() + " , Things: " + agent.belief.getThings());
            for (Thing thing : agent.belief.getThings()) {
                if (thing.type.equals(Thing.TYPE_BLOCK) && thing.x == dispenserItself.x && thing.y == dispenserItself.y) {  
                    attachPossible = true;
                    break;
                }                
            }

            String direction = DirectionUtil.getDirection(agent.belief.getPosition(), dispenserItself);

            if (attachPossible) {
                return new Action("attach", new Identifier(direction));  
            } else {
                return new Action("request", new Identifier(direction));                 
            }
        } else {
            String direction = DirectionUtil.firstIntToString(nearestDispenser.direction());
            return new Action("move", new Identifier(direction));         
        }
    }       
}

