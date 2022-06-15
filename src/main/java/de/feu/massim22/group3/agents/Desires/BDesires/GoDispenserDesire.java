package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.Convert;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

public class GoDispenserDesire extends BeliefDesire {
    private List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
    private BdiAgentV2 agent;
    private Thing block;
    private CellType dispenser;
    private String supervisor;
    private int distance;

    public GoDispenserDesire(Belief belief, Thing block, String supervisor, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoDispenserDesire");
        this.agent = agent;
        this.block = block;
        this.dispenser = Convert.blockNameToDispenser(block);
        this.supervisor = supervisor;
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
        AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Start - Agent: " + agent.getName());
        if (belief.getReachableDispensers().size() > 0) {
            // es existiert ein Dispenser ( den der Agent erreichen kann)
            List<ReachableDispenser> reachableDispensers = belief.getReachableDispensers();

            AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Type gesucht: " + block.type);
            // bestimmter Blocktyp wird gesucht
            AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Dispenser: " + reachableDispensers);

            for (ReachableDispenser reachableDispenser : reachableDispensers) {
                // alle Dispenser vom gesuchten Typ
                String typeDispenser = "b" + reachableDispenser.type().toString().substring(10);

                if (typeDispenser.equals(block.type)) {
                    typeDispensers.add(reachableDispenser);
                }
            }
            AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Type Dispenser: " + typeDispensers);
            if (typeDispensers.size() > 0) {
                // es wurde ein Dispenser vom gesuchten Typ gefunden
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() Start");
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
        distance = Math.abs(dispenserItself.x - agent.belief.getPosition().x)
                + Math.abs(dispenserItself.y - agent.belief.getPosition().y);

        AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() - Agent: "
                + agent.getName() + " , Pos: " + agent.belief.getPosition());
        AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() - dNearest: "
                + nearestDispenser.position() + nearestDispenser.data() + " , dItself: " + dispenserItself);
        AgentLogger.info(
                Thread.currentThread().getName() + ".getNextAction() - Agent: " + agent.getName()
                        + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());

        if (distance == 1) {
            // steht neben einem Dispenser
            AgentLogger.info(Thread.currentThread().getName() + ".getNextAction() - Agent: "
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
                //nextAction = new Action("attach", new Identifier(direction));
                return ActionInfo.ATTACH(direction, getName());
            } else {
                agent.requestMade = true;
                //nextAction = new Action("request", new Identifier(direction));
                return ActionInfo.REQUEST(direction, getName());
            }
            
        } else {
         // steht noch nicht neben einem Dispenser
            String direction = DirectionUtil.firstIntToString(nearestDispenser.direction());
            //nextAction = agent.desireProcessing.getPossibleActionForMove(agent, direction); 
            return getActionForMove(direction, getName());
        }
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