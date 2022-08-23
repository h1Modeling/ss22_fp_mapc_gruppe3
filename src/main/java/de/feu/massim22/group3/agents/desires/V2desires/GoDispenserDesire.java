package de.feu.massim22.group3.agents.desires.V2desires;

//import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings;
import de.feu.massim22.group3.agents.V2utils.Point;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>GoDispenserDesire</code> models the desire to go to a dispenser.
 * 
 * @author Melinda Betz
 */
public class GoDispenserDesire extends BeliefDesire {
    private List<ReachableDispenser> typeDispensers = new ArrayList<ReachableDispenser>();
    private BdiAgentV2 agent;
    private String block;
//    private CellType dispenser;
    private String supervisor;
    private int distance;
    private boolean strangeAgent = false;
    //private StepUtilities stepUtilities;

    /**
     * Instantiates a new GoDispenserDesire.
     * 
     * @param belief the belief of the agent
     * @param block the block the agent wants to attach
     * @param supervisor the supervisor of the group
     * @param agent the agent who wants to go to a dispenser
     * 
     */
    public GoDispenserDesire(Belief belief, String block, String supervisor, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start GoDispenserDesire, Step: " + belief.getStep());
        this.agent = agent;
        this.block = block;
//        this.dispenser = Convert.blockNameToDispenser(block);
        this.supervisor = supervisor;
        //this.stepUtilities = stepUtilities;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + ".isFulfilled() attached agent: " + agent.getAttachedThings());
        AgentLogger.info(Thread.currentThread().getName() + ".isFulfilled() attached belief: " + agent.getBelief().getAttachedThings());
        for (Thing t : agent.getAttachedThings()) {
            if ((t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_DISPENSER)) && (t.x == 0 || t.y == 0)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block + " attached");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + "GoDispenserDesire.isExecutable() Start - Agent: "
                + agent.getName());
        if (agent.getBelief().getRole().actions().contains(Actions.REQUEST)
                && agent.getBelief().getRole().actions().contains(Actions.ATTACH)) {
            if (belief.getReachableDispensersX().size() > 0) {
                // there is a dispenser that can be reached by the agent
                List<ReachableDispenser> reachableDispensers = belief.getReachableDispensersX();

                AgentLogger.info(Thread.currentThread().getName() + ".isExecutable() Type gesucht: " + block);
                // AgentLogger.info(Thread.currentThread().getName() + ".isExecutable()
                // Dispenser: " + reachableDispensers);

                for (ReachableDispenser reachableDispenser : reachableDispensers) {
                    // all dispensers from the sought-after type
                    String typeDispenser = "b" + reachableDispenser.type().toString().substring(10);

                    if (typeDispenser.equals(block)) {
                        typeDispensers.add(reachableDispenser);
                    }
                }
                AgentLogger
                        .info(Thread.currentThread().getName() + ".isExecutable() Type Dispenser: " + typeDispensers);
                if (typeDispensers.size() > 0) {
                    // a dispenser from the sought-after type has been found
                    return new BooleanInfo(true, "");
                } /*else {
                    if (agent.absolutePositions) {
                        for (Supervisor a : StepUtilities.allSupervisors) {
                            if (!a.equals(agent.supervisor)) {
                                reachableDispensers = a.getParent().getBelief().getReachableDispensersX();

                                for (ReachableDispenser reachableDispenser : reachableDispensers) {
                                    // all dispensers from the sought-after type
                                    String typeDispenser = "b" + reachableDispenser.type().toString().substring(10);

                                    if (typeDispenser.equals(block)) {
                                        typeDispensers.add(reachableDispenser);
                                    }
                                }

                                if (typeDispensers.size() > 0) {
                                    // a dispenser from the sought-after type has been found
                                    strangeAgent = true;
                                    return new BooleanInfo(true, "");
                                }
                            }
                        }
                    }
                }*/
            }
        }
        return new BooleanInfo(false, "");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + "GoDispenserDesire.getNextAction() Start");

        boolean attachPossible = false;
        Point dispenserItself = null;
        ReachableDispenser nearestDispenser = null;
        distance = 1000;
        
        // dispenser which has the smallest distance to the agent
        AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 0,1");
        Point visionDispenser = Point.castToPoint(belief.getNearestRelativeManhattanDispenser(block));
        AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 0,2");
        
        if (visionDispenser != null) {
            AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 1");
            distance = Math.abs(visionDispenser.x) + Math.abs(visionDispenser.y);
            dispenserItself = new Point(belief.getPosition().x + visionDispenser.x, belief.getPosition().y + visionDispenser.y);            
        } else {
            AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 2");
            nearestDispenser = agent.desireProcessing.getNearestDispenser(typeDispensers);
            dispenserItself = Point.castToPoint(nearestDispenser.position());
            distance = Point.distance(dispenserItself, Point.castToPoint(agent.getBelief().getPosition()));           
        }
        
        if (distance == 1 && visionDispenser != null) {
            //agent is next to a dispenser
            AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 6");           
            for (Thing thing : agent.getBelief().getThings()) {
                if (thing.type.equals(Thing.TYPE_BLOCK) && thing.x == visionDispenser.x
                        && thing.y == visionDispenser.y) {
                    if (!agent.desireProcessing.attachedThings.contains(thing)) {
                        attachPossible = true;
                        break;
                    }
                }
            }
            AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 7");
            String direction = DirectionUtil.getDirection(Point.zero(), visionDispenser);

            if (attachPossible) {
                Point pos = Point.castToPoint(agent.getBelief().getPosition());
                AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 8: " + pos + " , " + direction);               
                
                Point d1 = new Point(0, 0);
                Point d2 = new Point(0, 0);
                Point d3 = new Point(0, 0);
                Point r1 = new Point(0, 0);
                Point r2 = new Point(0, 0);
                Point r3 = new Point(0, 0);
                
                if (direction.equals("n")) {
                    d1 = new Point(pos.x + 1, pos.y - 1);
                    r1 = new Point(-1, 0);
                    d2 = new Point(pos.x, pos.y - 2);
                    r2 = new Point(0, 1);
                    d3 = new Point(pos.x - 1, pos.y - 1);
                    r3 = new Point(1, 0);
                }
                if (direction.equals("e")) {
                    d1 = new Point(pos.x + 1, pos.y + 1);
                    r1 = new Point(0, -1);
                    d2 = new Point(pos.x + 2, pos.y);
                    r2 = new Point(-1, 0);
                    d3 = new Point(pos.x + 1, pos.y - 1);
                    r3 = new Point(0, 1);
                }
                if (direction.equals("s")) {
                    d1 = new Point(pos.x - 1, pos.y + 1);
                    r1 = new Point(1, 0);
                    d2 = new Point(pos.x, pos.y + 2);
                    r2 = new Point(0, -1);
                    d3 = new Point(pos.x + 1, pos.y + 1);
                    r3 = new Point(-1, 0);
                }
                if (direction.equals("w")) {
                    d1 = new Point(pos.x - 1, pos.y - 1);
                    r1 = new Point(0, 1);
                    d2 = new Point(pos.x - 2, pos.y);
                    r2 = new Point(1, 0);
                    d3 = new Point(pos.x - 1, pos.y + 1);
                    r3 = new Point(0, -1);
                }

                ArrayList<Integer> met = new ArrayList<Integer>();
                int i = 0;
                boolean alreadyAttached = false;

                for (Meeting meeting : AgentMeetings.find(agent)) {
                    Point metAgent = Point.castToPoint(meeting.agent2().getBelief().getPosition()).translate2To1(meeting);
                    AgentLogger.info(Thread.currentThread().getName() + " Test.getNextAction() 8.1: "
                            + meeting.agent2().getName() + " , " + metAgent);

                    if (d1.equals(metAgent) || d2.equals(metAgent) || d3.equals(metAgent)) {
                        met.add(meeting.agent2().index);
                        i++;
                        
                        if ((d1.equals(metAgent) && meeting.agent2().attachedPoints.contains(r1))
                        || (d2.equals(metAgent) && meeting.agent2().attachedPoints.contains(r2))
                        || (d3.equals(metAgent) && meeting.agent2().attachedPoints.contains(r3)))
                            return ActionInfo.SKIP("block already attached by other agent");
                    }
                }
                AgentLogger.info(Thread.currentThread().getName() + " Test.getNextAction() 8.2: " + agent.index
                        + " , " + met);

                for (int metIndex : met) {
                    if (agent.index < metIndex) return ActionInfo.SKIP("other agent is attaching");
                }

                return ActionInfo.ATTACH(direction, getName());            
            } else {
                AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 9");
                agent.requestMade = true;
                return ActionInfo.REQUEST(direction, getName());
            }
         
        } else {
             // agent is not yet next to a dispenser
            String direction = "";
            AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 10"); 
            
            if (visionDispenser != null) {
                AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 11a"); 
                direction = DirectionUtil.getDirection(Point.zero(), visionDispenser);
            } else {
                AgentLogger.info(Thread.currentThread().getName() + "Test.getNextAction() 11b");  
                direction = DirectionUtil.getDirection(belief.getPosition(), dispenserItself);
            }
            
            if (distance > 3)
                return this.agent.desireProcessing.getActionForMove(agent, direction, direction, getName());
            else
                return this.agent.desireProcessing.getActionForMove(agent, direction, getName());
        }
    }
    
    /**
     * Gets the priority .
     * 
     * @return priority
     */
    @Override
    public int getPriority() {
        return distance - 1;
    }
    
    /**
     * Gets a block .
     * 
     * @return block as String
     */
    public String getBlock() {
        return block;
    }

    /**
     * Updates supervisor .
     * 
     * @param supervisor the new supervisor
     */
    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}