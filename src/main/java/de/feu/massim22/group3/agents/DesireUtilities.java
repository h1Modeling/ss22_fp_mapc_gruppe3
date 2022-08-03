package de.feu.massim22.group3.agents;

//import java.awt.Point;
import java.util.*;

import javax.management.relation.RoleStatus;
import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.belief.reachable.*;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.agents.desires.V2.*;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.data.Role;
import massim.protocol.messages.scenario.Actions;
import massim.protocol.messages.scenario.ActionResults;

public class DesireUtilities {
    public StepUtilities stepUtilities;
    public TaskInfo task;
    public int maxTaskBlocks = 3;
    public String directionCircle = "cw";
    public int directionCounter = 0;
    public int circleSize = 40;
    private String dir2;
    private boolean dir2Used = false;
    private int moveIteration = 0;
    private boolean inDirection = true;
    private int maxTypes = 2;
 
    public List<Thing> attachedThings = new ArrayList<Thing>();
    public List<Thing> goodBlocks = new ArrayList<Thing>();
    public List<Thing> badBlocks = new ArrayList<Thing>();
    public List<Thing> goodPositionBlocks = new ArrayList<Thing>();
    public List<Thing> badPositionBlocks = new ArrayList<Thing>();
    public List<Thing> missingBlocks = new ArrayList<Thing>();
    public boolean typeOk = false;
    public boolean analysisDone = false;
    public boolean dontArrange = false;
    public String nextTry = "ccw";
    public int nextTryDir = 1;
    public int failedPath = 0;
    public String lastWishDirection = null;
    public List< DispenserFlag> dFlags = new ArrayList<DispenserFlag>();
    
    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgentV2 agent) {
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() Start - Step: " + step
                + " , Agent: " + agent.getName());

        if (doDecision(agent, new DigFreeDesire(agent.belief))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , DigFreeDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , DigFreeDesire");
        
        if (doDecision(agent, new FreedomDesire(agent.belief))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , FreedomDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , FreedomDesire");
        
        if (doDecision(agent, new LocalExploreDesire(agent.belief, agent.supervisor.getName(), agent))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , LocalExploreDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , LocalExploreDesire");
        
        if (agent.blockAttached && agent.desireProcessing.attachedThings.size() > maxTaskBlocks 
                && doDecision(agent, new LooseWeightDesire(agent.belief))) {
                AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                + " , LooseWeightDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters() 
                + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
            } else
                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , LooseWeightDesire");
        
        if (agent.belief.getRole().name().equals("default") 
                && doDecision(agent, new GoAdoptRoleDesire(agent.belief, agent, "worker"))) {
                AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                + " , GoAdoptRoleDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
            } else 
                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , GoAdoptRoleDesire - worker");
        
        agent.decisionsDone = true;
        return result;
    }
 
    boolean doDecision(BdiAgentV2 agent, IDesire inDesire) {
        boolean result = false;
      
        if (!inDesire.isFulfilled().value() 
                && !inDesire.isUnfulfillable().value() 
                && inDesire.isExecutable().value()) { // desire ist möglich , hinzufügen
            inDesire.setOutputAction(inDesire.getNextActionInfo().value());
            agent.desires.add(inDesire);
            result = true;
        }
        
        return result;
    }
    

    /**
     * The method runs the different supervisor decisions.
     *
     * @param supervisor - the supervisor who wants to make the decisions
     * 
     * @return boolean - the supervisor decisions are done
     */
    public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor, StepUtilities stepUtilities) {
        this.stepUtilities = stepUtilities;
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());

        BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
        
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Dispenser: " + supervisorAgent.belief.getReachableDispensersX());

        List<String> allGroupAgents = new ArrayList<>(supervisor.getAgents());
        List<String> freeGroupAgents = new ArrayList<>(allGroupAgents);
        List<String> busyGroupAgents = new ArrayList<>();

        // Schleife über alle Tasks
        for (TaskInfo loopTask : supervisorAgent.belief.getTaskInfo()) {
            //Task Deadline erreicht
            if(taskReachedDeadline ( supervisorAgent, loopTask)) {
                 continue;
            }
            task = loopTask;
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Task: " + task.name + " , " + task.requirements.size() + " , " + task.requirements);

           if ( task.requirements.size() > maxTaskBlocks) {
           //if ( task.requirements.size() > 2) {
               continue;
           }
           
            // über alle Agenten einer Gruppe
            for (String agentStr : allGroupAgents) {
                BdiAgentV2 agent = StepUtilities.getAgent(agentStr); 
                
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr + " , Pos: " + agent.belief.getPosition());
                AgentLogger.info(
                        Thread.currentThread().getName() + ".getNextAction() - Agent: " + agent.getName()
                                + " , lA: " + agent.belief.getLastAction() + " , lAR: " + agent.belief.getLastActionResult());
              
                agent.desireProcessing.attachedThings = new ArrayList<Thing>();
                agent.desireProcessing.task = task;
                agent.desireProcessing.attachedThings = agent.getAttachedThings();

                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Agent: " + agentStr + " attachedThings: "
                        + agent.desireProcessing.attachedThings);
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , Things: " + agent.belief.getThings());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , ReachableDispensers: " + agent.belief.getReachableDispensersX());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , GoalZones: " + agent.belief.getGoalZones());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , ReachableGoalZones: " + agent.belief.getReachableGoalZonesX());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , nicht in Zone: " + !agent.belief.getGoalZones().contains(Point.zero()) 
                        + " , in Zone: "+ agent.belief.getGoalZones().contains(Point.zero()) + " , att. Size: "
                        + agent.desireProcessing.attachedThings.size());
                
                                
                if (!agent.blockAttached
                && doDecision(agent, new GoAbandonedBlockDesire(agent, getTaskBlockA(agent, task).type))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoAbandonedBlockDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoAbandonedBlockDesire");
                
                //String bType = (StepUtilities.getNumberAttachedBlocks(getTaskBlock(agent, task).type) < 4 ? getTaskBlock(agent, task).type : "b2");
                
                if (!agent.blockAttached 
                    && doDecision(agent, new GoDispenserDesire(agent.belief, getTaskBlockC(agent, task).type, supervisor.getName(), agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoDispenserDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoDispenserDesire");
                
                if (maxTaskBlocks > 1 && agent.blockAttached && task.requirements.size() > 1
                        && doDecision(agent, new HelpMultiBlocksDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , HelpMultiBlocksDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , HelpMultiBlocksDesire");
                
                if (maxTaskBlocks > 2 && agent.blockAttached && task.requirements.size() > 2
                        && doDecision(agent, new Help2MultiBlocksDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , Help2MultiBlocksDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , Help2MultiBlocksDesire");

                if (agent.blockAttached && !agent.belief.getGoalZones().contains(Point.zero()) 
                    && doDecision(agent, new GoGoalZoneDesire(agent.belief, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoGoalZoneDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoGoalZoneDesire");

                if (agent.blockAttached && agent.belief.getGoalZones().contains(Point.zero())
                    && doDecision(agent, new ArrangeBlocksDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , ArrangeBlocksDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , ArrangeBlocksDesire");
                                
                if (maxTaskBlocks > 1 && agent.blockAttached && agent.belief.getGoalZones().contains(Point.zero())
                        && doDecision(agent, new ArrangeMultiBlocksDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , ArrangeMultiBlocksDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , ArrangeMultiBlocksDesire");
                
                if (maxTaskBlocks > 1 && agent.blockAttached 
                        && doDecision(agent, new ConnectMultiBlocksDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , ConnectMultiBlocksDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , ConnectMultiBlocksDesire");

                if (agent.blockAttached && agent.belief.getGoalZones().contains(Point.zero())
                    && doDecision(agent, new SubmitDesire(agent.belief, task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , SubmitDesire , Action: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.desires.get(agent.desires.size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.desires.get(agent.desires.size() - 1), agent));
                    } else {}
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , SubmitDesire");
            } // Loop agents
        } // Loop tasks

        freeGroupAgents.removeAll(busyGroupAgents);
        busyGroupAgents.clear();

        supervisor.setDecisionsDone(true);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() End - Step: " + step
                + " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());
        return result;
    }
    
    public boolean isPossibleMaster(BdiAgentV2 inAgent) {
        // Variante 1: nur gerade Agenten dürfen Master sein
        if (inAgent.index % 2 == 0)     
            return true;
        
        return false;
    }
    
    public boolean isPossibleMaster() {
        // Variante 2: nur 3 Agenten dürfen gleichzeitigMaster sein
        if (StepUtilities.countMaster < 3)     
            return true;
        
        return false;
    }

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority
     * 
     * @return int - the priority
     */
    // TODO sinnvolle Prioritäten vergeben
    public int getPriority(IDesire desire, BdiAgentV2 agent) {
        int result = 0;

        switch (desire.getName()) {
        
        case "GoAdoptRoleDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else
                result = 1500;
            break;
        case "DigFreeDesire":
            result = 1900;
            break;
        case "FreedomDesire":
            result = 2000;
            break;
        case "LocalExploreDesire":
            result = 100;
            break;            
        case "GoAbandonedBlockDesire":
            if (desire.getOutputAction().getName().equals(Actions.ATTACH))
                result = 290;
            else
                result = 260;        
            break;
        case "GoDispenserDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 200;
            else if (desire.getOutputAction().getName().equals(Actions.ATTACH))
                result = 300;
                else if (desire.getOutputAction().getName().equals(Actions.REQUEST))
                    result = 280;
            else {
                result = 250 - desire.getPriority();
                
               switch (((GoDispenserDesire) desire).getBlock()) {
               case "b0":
                   break;
               case "b1":
                   break;
               case "b2":
                   result = 290;
                   break;
               }
            }
            break;
        case "GoGoalZoneDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else
                result = 400;        
            break;
        case "ArrangeBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 450;
            else
                result = 500;
            break;
        case "HelpMultiBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.CONNECT))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 1000;
            else
                result = 600;
            break;
        case "HelpMultiBlocksDesire2":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.CONNECT))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 1000;
            else
                result = 600;
            break;
        case "ArrangeMultiBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 550;
            else if (desire.getOutputAction().getName().equals(Actions.CONNECT))
                result = 1000;
            else
                result = 600;
            break;
        case "ConnectMultiBlocksDesire":
            result = 1050;
            break;
        case "SubmitDesire":
            if (((SubmitDesire) desire).getTask().requirements.size() == 1)
                result = 950;
            else    
                result = 1100;
            break;
        case "LooseWeightDesire":
            result = 1400;
            break;
        }

        return result;
    }

    /**
     * The method determines the Intention for a certain agent .
     *
     * @param agent - the agent that needs a intention
     * 
     * @return Desire - the intention
     */
    public synchronized IDesire determineIntention(BdiAgentV2 agent) {
        IDesire result = null;
        int priority = 0;
        
        for (IDesire desire : agent.desires) {
            /*AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.getName() + " , Action: " + desire.getOutputAction() + " , Prio: " + getPriority(desire));*/
            if (getPriority(desire, agent) > priority) {
                result = desire;
                priority = getPriority(desire, agent);
            }
        }

        /*AgentLogger.info(Thread.currentThread().getName() + " determineIntention() End - Agent: " + agent.getName()
                + " , Intention: " + result.getName() + " , Action: " + result.getOutputAction());*/
        return result;
    }

    public ReachableGoalZone getNearestGoalZone(List<ReachableGoalZone> inZoneList) {
        int distance = 1000;
        ReachableGoalZone result = null;

        for (ReachableGoalZone zone : (List<ReachableGoalZone>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }
    
    public ReachableRoleZone getNearestRoleZone(List<ReachableRoleZone> inZoneList) {
        int distance = 1000;
        ReachableRoleZone result = null;
        
        for (ReachableRoleZone zone : (List<ReachableRoleZone>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }

    public ReachableDispenser getNearestDispenser(List<ReachableDispenser> inZoneList) {
        int distance = 1000;
        ReachableDispenser result = null;

        for (ReachableDispenser zone : (List<ReachableDispenser>) inZoneList) {
            if (zone.distance() < distance) {
                distance = zone.distance();
                result = zone;
            }
        }
        return result;
    }
    
    public Identifier walkCircles(BdiAgentV2 agent, int stepWidth) {
        String startDirection = DirectionUtil.intToString(agent.exploreDirection);
        /*float random = new Random().nextFloat();
        if (random < 0.25) {
            startDirection = "n";
        } else if (random < 0.5) {
            startDirection = "e";
        } else if (random < 0.75) {
            startDirection = "w";
        } else {
            startDirection = "s";
        }*/
        Identifier resultDirection = new Identifier(startDirection);

        if (agent.belief.getLastAction() != null && agent.belief.getLastAction().equals(Actions.MOVE)) {
            directionCounter++;
            resultDirection = new Identifier(agent.belief.getLastActionParams().get(0));
            
           /* if (agent.belief.getLastActionResult().equals(ActionResults.FAILED_PATH)) {
                if (directionCircle.equals("cw")) {
                    directionCircle = "ccw";
                } else {
                    directionCircle = "cw";
                }
            }*/

            if (directionCircle.equals("cw")) {
                if (agent.belief.getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                    if (agent.belief.getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                    if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                    if (agent.belief.getLastActionParams().get(0).equals("w")) {
                        resultDirection = new Identifier("n");
                        circleSize = circleSize + stepWidth;
                    }
                    directionCounter = 0;
                    directionCircle = "ccw";
                }
            } else {
                if (agent.belief.getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("w");
                    if (agent.belief.getLastActionParams().get(0).equals("w")) resultDirection = new Identifier("s");
                    if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("e");
                    if (agent.belief.getLastActionParams().get(0).equals("e")) {
                        resultDirection = new Identifier("n");
                        circleSize = circleSize + stepWidth;
                    }
                    directionCounter = 0;
                    directionCircle = "cw";
                }
            }
        }
        return resultDirection;
    }
    
    public Thing toTaskBlock(Thing toThingBlock) {           
        return new Thing(toThingBlock.x, toThingBlock.y, toThingBlock.details, "");
    }
    
    public Thing toThingBlock(Thing toTaskBlock) {           
        return new Thing(toTaskBlock.x, toTaskBlock.y, Thing.TYPE_BLOCK, toTaskBlock.type);
    }
    
    public boolean blockInTask(List<Thing> inTaskReqs, Thing inBlock) { 
        for (Thing req : inTaskReqs) {
            if (req.x == inBlock.x && req.y == inBlock.y && req.type.equals(inBlock.details)) {              
                return true;
            }
        }
        
        return false;
    }
    
    public boolean taskReqInList(List<Thing> inList, Thing inTaskReq) {     
        for (Thing block : inList) {
            if (block.x == inTaskReq.x && block.y == inTaskReq.y && block.details.equals(inTaskReq.type)) {
                return true;
            }
        }
        
        return false;
    }
    
    public int countBlockType(List<Thing> inList, String inType) {  
        int count = 0;
        
        for (Thing block : inList) {
            if (block.details.equals(inType) || block.type.equals(inType)) {
                count++;
            }
        }

        return count;
    }
    
    public Thing getContentInDirection(BdiAgentV2 agent, String direction) {
        Point cell = Point.castToPoint(DirectionUtil.getCellInDirection(direction));

        return getContent(agent, cell);
    }
    
    public Thing getContentInDirection(BdiAgentV2 agent, Point from, String direction) {
        Point cell = Point.castToPoint(DirectionUtil.getCellInDirection(from, direction));

        return getContent(agent, cell);
    }
    
    public Thing getContent(BdiAgentV2 agent, Point cell) {      
        //AgentLogger.info(Thread.currentThread().getName() + " getContent() - Position: " + cell);
        
        for (Thing thing : agent.belief.getThings()) {           
            if (thing.type.equals(Thing.TYPE_OBSTACLE) || thing.type.equals(Thing.TYPE_ENTITY) || thing.type.equals(Thing.TYPE_BLOCK)) {
                // an diesem Punkt ist ein Hindernis
                
                if (cell.equals(new Point(thing.x, thing.y))) {
                    //AgentLogger.info(Thread.currentThread().getName() + " getContentInDirection() - Vision: " + thing);
                    // Agent steht vor Hinderniss in Richtung direction
                    return thing;
                } 
            }
        }

        return null;
    }
          
    public boolean taskReachedDeadline (BdiAgentV2 agent,TaskInfo task) {
        boolean result = false;
        if (agent.belief.getStep() > task.deadline) {
            //Task ist abgelaufen 
            result = true;
        }
        return result;
    }
       
    public Thing getTaskBlockA(BdiAgentV2 agent, TaskInfo task) {
        Thing result = task.requirements.get(0);
        // ein Block Task
        if (task.requirements.size() > 1) {
            // Mehr Block Task
            for (Meeting meeting : AgentMeetings.find(agent)) {
                if (!meeting.agent2().getAttachedThings().isEmpty()) {
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        // Kenn ich einen Agenten mit get(0)?
                        if (attachedThing.details.equals(task.requirements.get(0).type)) {
                            result = task.requirements.get(1);
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public Thing getTaskBlockB(BdiAgentV2 agent, TaskInfo task) {
        Thing result = task.requirements.get(0);
        Thing block1 = result;
        Thing block2 = result;
        Thing block3 = result;
        
        for (int i = 0; i < task.requirements.size(); i++) {
            if (DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                block1 = task.requirements.get(i);
            } else
            if (!DirectionUtil.getCellsIn4Directions().contains(new java.awt.Point (task.requirements.get(i).x, task.requirements.get(i).y))
                    && existsCommonEdge(new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                block2 = task.requirements.get(i);
            } else
                block3 = task.requirements.get(i);
        }
        
        // ein Block Task
        if (task.requirements.size() == 1) {
            result = block1;
        } else {       
            // Mehr Block Task
            for (Meeting meeting : AgentMeetings.find(agent)) {
                if (!meeting.agent2().getAttachedThings().isEmpty()) {
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        // Kenn ich einen Agenten mit block1?
                        if (attachedThing.details.equals(block1.type)) {
                            result = block2;
                            break;
                        } else if (task.requirements.size() == 3 && attachedThing.details.equals(block2.type)) {
                            result = block3;
                        } else {
                            result = block1;
                        }
                    }
                }
            }
        }
        return result;
    }
    
    public Thing getTaskBlockC(BdiAgentV2 agent, TaskInfo task) {
        Thing result = task.requirements.get(0);
        List<Thing> reqs = getTaskReqsOrdered(task);

        // ein Block Task
        if (task.requirements.size() == 1) {
            AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out0");
            result = reqs.get(0);
        } else {
            // Mehr Block Task
            for (Meeting meeting : AgentMeetings.find(agent)) {
                AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out0.1");
                
                if (!meeting.agent2().getAttachedThings().isEmpty()) {
                    AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out0.2");
                    
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out0.3");
                        // Kenn ich einen Agenten mit block1?
                        if (attachedThing.details.equals(reqs.get(0).type)) {
                                AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out1");
                                result = reqs.get(1);                                
                                break;                               
                        } else if (task.requirements.size() == 3 && attachedThing.details.equals(reqs.get(1).type)) {
                            AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - out3");
                            result = reqs.get(2);
                        } 
                    }
                }
            }
        }

        result = proofBlockType(result, reqs);
                
        AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - agent: " + agent.getName() + " , task: " + task.name 
                + " , block1: " + reqs.get(0).toString() + " , block2: " + reqs.get(1).toString() + " , block3: " + reqs.get(2).toString() + " , result: " + result.toString());        

        return result;
    }
    
    Thing proofBlockType(Thing inBlock, List<Thing> inReqs) {
        Thing result = inBlock;
        AgentLogger.info(Thread.currentThread().getName() + " proofBlockType - type: " + inBlock.type + " , number: " + StepUtilities.getNumberAttachedBlocks(inBlock.type));  
        
        if (StepUtilities.getNumberAttachedBlocks(inBlock.type) > 2) {
            if (inBlock.equals(inReqs.get(0))) {
                result = proofBlockType(inReqs.get(1), inReqs);
            } else if (inBlock.equals(inReqs.get(1))) {
                result = proofBlockType(inReqs.get(2), inReqs);
            } else if (inBlock.equals(inReqs.get(2))) {
                result = proofBlockType(inReqs.get(0), inReqs);
            }
        }

        return result;
    }
    
    public List<Thing> getTaskReqsOrdered(TaskInfo task) {
        List<Thing> result = new ArrayList<Thing>();
        Thing block1 = null;
        Thing block2 = null;
        Thing block3 = null;

        for (int i = 0; i < task.requirements.size(); i++) {
            if (DirectionUtil.getCellsIn4Directions()
                    .contains(new java.awt.Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                block1 = task.requirements.get(i);
            } else if (!DirectionUtil.getCellsIn4Directions()
                    .contains(new java.awt.Point(task.requirements.get(i).x, task.requirements.get(i).y))
                    && existsCommonEdge(new Point(task.requirements.get(i).x, task.requirements.get(i).y))) {
                block2 = task.requirements.get(i);
            } else
                block3 = task.requirements.get(i);
        }
        
        result.add(block1);
        result.add(block2);
        result.add(block3);
        
        return result;
    }
    
    private ActionInfo getIteratedActionForMove(BdiAgentV2 agent, String dir, String desire) {
        moveIteration++;
        if (moveIteration < 100) {
            return getActionForMove(agent, dir, desire);
        }
        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - getIteratedActionForMove - stuck");
        // TODO AGENT is STuck
        return ActionInfo.SKIP("Agent is Stuck in iterated");
    }
    
    public ActionInfo getActionForMoveWithAlternate(BdiAgentV2 agent, String dir, String dirAlt, String desire) {
        ActionInfo firstTry = getActionForMove(agent, dir, desire);
        String lastRotation = agent.belief.getLastActionParams().size() > 0 ? agent.belief.getLastActionParams().get(0) : "";
       
        if ((firstTry.value().getName().equals(Actions.MOVE) 
                && !firstTry.value().getParameters().get(0).toString().equals(dir)  
                && !firstTry.value().getParameters().get(0).toString().equals(dirAlt))
                || (firstTry.value().getName().equals(Actions.ROTATE) 
                && firstTry.value().getParameters().get(0).toString().equals("cw")
                && lastRotation.equals("ccw")) 
                || (firstTry.value().getName().equals(Actions.ROTATE)  
                && firstTry.value().getParameters().get(0).toString().equals("ccw")
                && lastRotation.equals("cw"))) {
            return getActionForMove(agent, dirAlt, desire);
        } 
        
        return firstTry;
    }

    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String dir2, String desire) {
        this.dir2 = dir2;
        dir2Used = true;
        ActionInfo out = getActionForMove(agent, dir, desire);
        dir2Used = false;
        return out;
    }
    
    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String desire) {
        Point dirPoint = Point.castToPoint(DirectionUtil.getCellInDirection(dir));
        //Melinda 
        List<Point> attached = agent.getAttachedPoints();       
        //List<Point> attached = belief.getAttachedPoints();
        //Melinda Ende
        // Rotate attached
        if (agent.blockAttached) {
            for (Point p : attached) {
                Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
                Thing t = agent.belief.getThingAt(testPoint);
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - Direction: " + dir
                        + " , Block attached: " + p + " , in Richtung: " + testPoint);
                if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                    // Can be rotated
                    Thing cw = agent.belief.getThingCRotatedAt(p);
                    Thing ccw = agent.belief.getThingCCRotatedAt(p);
                    Point cwP = getCRotatedPoint(p);
                    Point ccwP = getCCRotatedPoint(p);
                    AgentLogger.info(
                            Thread.currentThread().getName() + " getActionForMove - cw: " + cwP + " , ccw: " + ccwP);
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - oppositeD: "
                            + DirectionUtil.oppositeDirection(dir) + " , Cell: "
                            + DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)));
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - cw: " + cw + " , Free?: "
                            + isFree(cw));
                    String lastRotation = agent.belief.getLastActionParams().size() > 0
                            ? agent.belief.getLastActionParams().get(0)
                            : "";

                    if (DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)).equals(cwP)) {
                        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if1");
                        if (isFree(cw) && !lastRotation.equals("ccw")) {
                            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rcw");
                            return ActionInfo.ROTATE_CW(desire);
                        } else {
                            if (isFree(ccw) && !lastRotation.equals("cw")) {
                                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rccw");
                                return ActionInfo.ROTATE_CCW(desire);
                            }
                        }
                    } else {
                        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - else1");
                        if (isFree(ccw) && !lastRotation.equals("cw")) {
                            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rccw");
                            return ActionInfo.ROTATE_CCW(desire);
                        } else {
                            if (isFree(cw) && !lastRotation.equals("ccw")) {
                                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - rcw");
                                return ActionInfo.ROTATE_CW(desire);
                            }
                        }
                    }

                    if (cw != null && cw.type.equals(Thing.TYPE_OBSTACLE) && !cwP.equals(dirPoint)) {
                        Point target = Point.castToPoint(DirectionUtil.rotateCW(p));
                        return ActionInfo.CLEAR(target, desire);
                    }
                    if (ccw != null && ccw.type.equals(Thing.TYPE_OBSTACLE) && !ccwP.equals(dirPoint)) {
                        Point target = Point.castToPoint(DirectionUtil.rotateCW(p));
                        return ActionInfo.CLEAR(target, desire);
                    }
                }
            }
        }
        
        // Test Agent
        Thing t = agent.belief.getThingAt(dirPoint);
        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - t: " + t);
        
        if (t != null && (t.type.equals(Thing.TYPE_OBSTACLE) /*|| (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint))*/)) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if2");
            return ActionInfo.CLEAR(dirPoint, desire);
            
        } else if (isFree(t) || attached.contains(dirPoint)) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if3");

            if (dir2Used)
                return ActionInfo.MOVE(dir, dir2, desire);
            else
                return ActionInfo.MOVE(dir, desire);
            
        } else if (t != null && (t.type.equals(Thing.TYPE_ENTITY)                
                || (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint)))) {

            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if4");
            // Try to move around agent
            inDirection = inDirection ? false : true;
            String dir1 = inDirection ? getCRotatedDirection(dir) : getCCRotatedDirection(dir);
            String dir2 = inDirection ? getCCRotatedDirection(dir) : getCRotatedDirection(dir);
            Thing tDir1 = agent.belief.getThingAt(dir1);
            Thing tDir2 = agent.belief.getThingAt(dir2);
            
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove: " + dir1 + " , " + dir2 + " , " + tDir1 + " , " + tDir2);

            if (isFree(tDir1) || isClearable(tDir1)) {
                return getIteratedActionForMove(agent, dir1, desire);
            }

            if (isFree(tDir2) || isClearable(tDir2)) {
                return getIteratedActionForMove(agent, dir2, desire);
            }
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if5");
            return getIteratedActionForMove(agent, getCRotatedDirection(dir), desire);
            //return ActionInfo.SKIP("Agent is stuck");

        } else {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if6");
            return getIteratedActionForMove(agent, getCRotatedDirection(dir), desire);
            //return ActionInfo.SKIP("Agent is stuck");
        }
    }
    
    protected boolean isFree(Thing t) {
        return t == null || t.type.equals(Thing.TYPE_DISPENSER);
    }

    protected boolean isClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_BLOCK) || t.type.equals(Thing.TYPE_OBSTACLE));
    }

    protected Point getCRotatedPoint(Point p) {
        return new Point(-p.y, p.x);
    }

    protected String getCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "e";
            case "e": return "s";
            case "s": return "w";
            default: return "n";
        }
    }

    protected String getCCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "w";
            case "e": return "n";
            case "s": return "e";
            default: return "s";
        }
    }

    protected String getDirectionFromPoint(Point p) {
        if (p.x == 0) {
            return p.y < 0 ? "n" : "s";
        }
        return p.x < 0 ? "w" : "e";
    }

    protected Point getPointFromDirection(String dir) {
        switch (dir) {
            case "n": return new Point(0, -1);
            case "e": return new Point(1, 0);
            case "s": return new Point(0, 1);
            default: return new Point(-1, 0);
        }
    }

    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }
    
    private boolean existsCommonEdge(Point p2) {
        for (java.awt.Point p1 : DirectionUtil.getCellsIn4Directions()) {
            if ((Math.abs(p2.x - p1.x) == 0 && Math.abs(p2.y - p1.y) == 1)
                    ||
                    (Math.abs(p2.y - p1.y) == 0 && Math.abs(p2.x - p1.x) == 1)) {
                    return true;     
                }  
        }

        return false;
    }
    
    public record DispenserFlag(Point position, Boolean attachMade) {}
}
