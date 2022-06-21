package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.StepUtilities.DispenserFlag;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.StepUtilities;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

public class GoDispenserDesire extends BeliefDesire {
    private List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
    private BdiAgentV2 agent;
    private Thing block;
    private CellType dispenser;
    private String supervisor;
    private int distance;
    private StepUtilities stepUtilities;

    public GoDispenserDesire(Belief belief, Thing block, String supervisor, BdiAgentV2 agent, StepUtilities stepUtilities) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoDispenserDesire, Step: " + belief.getStep());
        this.agent = agent;
        this.block = block;
        this.dispenser = Convert.blockNameToDispenser(block);
        this.supervisor = supervisor;
        this.stepUtilities = stepUtilities;
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block.type)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block.type + " attached");
    }

    @Override
	public BooleanInfo isExecutable() {
		AgentLogger.info(Thread.currentThread().getName() + "GoDispenserDesire.isExecutable() Start - Agent: "
				+ agent.getName());
		if (agent.belief.getRole().actions().contains(Actions.REQUEST)
				&& agent.belief.getRole().actions().contains(Actions.ATTACH)) {
			if (belief.getReachableDispensers().size() > 0) {
				// es existiert ein Dispenser ( den der Agent erreichen kann)
				// TODO in Vision nach Dispenser suchen
				List<ReachableDispenser> reachableDispensers = belief.getReachableDispensers();

				AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Type gesucht: " + block.type);
				// bestimmter Blocktyp wird gesucht
				// AgentLogger.info(Thread.currentThread().getName() + ".isExecutable()
				// Dispenser: " + reachableDispensers);

				for (ReachableDispenser reachableDispenser : reachableDispensers) {
					// alle Dispenser vom gesuchten Typ
					String typeDispenser = "b" + reachableDispenser.type().toString().substring(10);

					if (typeDispenser.equals(block.type)) {
						typeDispensers.add(reachableDispenser);
					}
				}
				AgentLogger
						.info(Thread.currentThread().getName() + ".isExecutable() Type Dispenser: " + typeDispensers);
				if (typeDispensers.size() > 0) {
					// es wurde ein Dispenser vom gesuchten Typ gefunden
					return new BooleanInfo(true, "");
				}
			}
		}
		return new BooleanInfo(false, "");
	}

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + "GoDispenserDesire.getNextAction() Start");

        boolean attachPossible = false;
        Point dispenserItself = null;
        ReachableDispenser nearestDispenser = null;
        // Dispenser mit der kürzesten Entfernung zum Agenten
        Point visionDispenser = belief.getNearestRelativeManhattenDispenser(block.type);

        if (visionDispenser != null) {
            distance = Math.abs(visionDispenser.x) + Math.abs(visionDispenser.y);
            dispenserItself = new Point(belief.getPosition().x + visionDispenser.x, belief.getPosition().y + visionDispenser.y);
        } else {
            nearestDispenser = agent.desireProcessing.getNearestDispenser(typeDispensers);
            dispenserItself = DirectionUtil.getDispenserItself(nearestDispenser);

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
            distance = Math.abs(dispenserItself.x - agent.belief.getPosition().x)
                    + Math.abs(dispenserItself.y - agent.belief.getPosition().y);

            /*AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() - Agent: " + agent.getName()
                    + " , Pos: " + agent.belief.getPosition());
            AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() - dNearest: "
                    + nearestDispenser.position() + nearestDispenser.data() + " , dItself: " + dispenserItself);*/
        }

        if (distance == 1) {
            // steht neben einem Dispenser
           
            for (Thing thing : agent.belief.getThings()) {
                if (thing.type.equals(Thing.TYPE_BLOCK) && thing.x == visionDispenser.x
                        && thing.y == visionDispenser.y) {
                    if (!agent.desireProcessing.attachedThings.contains(thing)) {
                        attachPossible = true;
                        break;
                    }
                }
            }

            String direction = DirectionUtil.getDirection(new Point(0, 0), visionDispenser);

            if (attachPossible) {
            	if(!attachMade(dispenserItself)) {
            	    stepUtilities.dFlags.add(new DispenserFlag(dispenserItself,true));
                    return ActionInfo.ATTACH(direction, getName());
            	}else {
            		return ActionInfo.SKIP(getName());
            	}
            } else {
                agent.requestMade = true;
                return ActionInfo.REQUEST(direction, getName());
            }
            
        } else {
             // steht noch nicht neben einem Dispenser
            String direction = "";
                    
            if (visionDispenser != null) {
                direction = DirectionUtil.getDirection(new Point(0, 0), visionDispenser);
            } else {
                direction = DirectionUtil.firstIntToString(nearestDispenser.direction());
            }
            
            return getActionForMove(direction, getName());
        }
    }
    
    private boolean attachMade(Point dispenser) {
    	boolean result = false;
    	for(DispenserFlag dFlag : stepUtilities.dFlags ) {
    		if((dFlag.position().equals(dispenser) && dFlag.attachMade())) {
    			result = true;
    			break;
    		}
    	}
    	return result;
    }
    @Override
    public int getPriority() {
        return distance - 1;
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}