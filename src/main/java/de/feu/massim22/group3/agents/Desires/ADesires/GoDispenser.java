package de.feu.massim22.group3.agents.Desires.ADesires;

import java.util.*;
import java.awt.Point;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoDispenser extends ADesire {
    String type = null;
    List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
    
    public GoDispenser(BdiAgentV2 agent) {
        super("GoDispenser", agent);
    }
    
    public GoDispenser(BdiAgentV2 agent, String type) {
        super("GoDispenser", agent);
        this.type = type;
        groupOrder = true;
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
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() Start - Agent: " + agent.getName());
        if (agent.belief.getReachableDispensers().size() > 0) {
            // es existiert ein Dispenser ( den der Agent erreichen kann)
            List<ReachableDispenser> reachableDispensers = agent.belief.getReachableDispensers();

            if (type != null) {
                AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() Type gesucht: " + type);
                // bestimmter Blocktyp wird gesucht
                if (!agent.desireProcessing.analysisDone) {
                    agent.desireProcessing.analyseAttachedThings();
                    agent.desireProcessing.analysisDone = true;
                }

/*                if (agent.desireProcessing.goodPositionBlocks.size() > 0 
                        && agent.desireProcessing.badBlocks.size() == 0
                        && agent.desireProcessing.missingBlocks.size() > 0) {*/
                AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() Dispenser: " + reachableDispensers);

                    for (ReachableDispenser dispenser : reachableDispensers) {                     
                        // alle Dispenser vom gesuchten Typ
                        String typeDispenser = "b" + dispenser.type().toString().substring(10);
                        
                        if (typeDispenser.equals(type)) {
                            typeDispensers.add(dispenser);
                        }
                    }
                    AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() Type Dispenser: " + typeDispensers);
                    if (typeDispensers.size() > 0) {
                        // es wurde ein Dispenser vom gesuchten Typ gefunden
                        result = true;
                    }
//                }
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
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() Start");
        Action nextAction = null;
        boolean attachPossible = false;
        // Dispenser mit der kürzesten Entfernung zum Agenten
        ReachableDispenser nearestDispenser = agent.desireProcessing.getNearestDispenser(typeDispensers);
        Point dispenserItself = DirectionUtil.getDispenserItself(nearestDispenser);

        if (agent.requestMade && agent.lastUsedDispenser != nearestDispenser.position()) {
            for (Thing thing : agent.belief.getThings()) {
                if (thing.type.equals(Thing.TYPE_DISPENSER)
                        && thing.x == agent.lastUsedDispenser.x - agent.belief.getPosition().x
                        && thing.y == agent.lastUsedDispenser.y - agent.belief.getPosition().y) {
                    dispenserItself = new Point(agent.lastUsedDispenser.x, agent.lastUsedDispenser.y);
                    agent.requestMade = false;
                    break;
                }
            }
        }

        agent.lastUsedDispenser = dispenserItself;
        int distance = Math.abs(dispenserItself.x - agent.belief.getPosition().x)
                + Math.abs(dispenserItself.y - agent.belief.getPosition().y);

        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: "
                + agent.getName() + " , Pos: " + agent.belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - dNearest: "
                + nearestDispenser.position() + nearestDispenser.data() + " , dItself: " + dispenserItself);
        AgentLogger.info(
                Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName()
                        + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());

        if (distance == 1) {
            // steht neben einem Dispenser
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: "
                    + agent.getName() + " , Things: " + agent.belief.getThings());
            for (Thing thing : agent.belief.getThings()) {
                if (thing.type.equals(Thing.TYPE_BLOCK) && thing.x == dispenserItself.x - agent.belief.getPosition().x
                        && thing.y == dispenserItself.y - agent.belief.getPosition().y) {
                    if (!agent.desireProcessing.attachedThings.contains(thing)) {
                        attachPossible = true;
                        break;
                    }
                }
            }

            String direction = DirectionUtil.getDirection(agent.belief.getPosition(), dispenserItself);

            if (attachPossible) {
                nextAction = new Action("attach", new Identifier(direction));
            } else {
                agent.requestMade = true;
                nextAction = new Action("request", new Identifier(direction));
            }
            
        } else {
         // steht noch nicht neben einem Dispenser
            String direction = DirectionUtil.firstIntToString(nearestDispenser.direction());
            nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction);  
        }

        return nextAction;
    }
}

