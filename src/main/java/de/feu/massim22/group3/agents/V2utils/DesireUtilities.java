package de.feu.massim22.group3.agents.V2utils;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.agents.desires.ActionInfo;
import de.feu.massim22.group3.agents.desires.DigFreeDesire;
import de.feu.massim22.group3.agents.desires.FreedomDesire;
import de.feu.massim22.group3.agents.desires.IDesire;
import de.feu.massim22.group3.agents.desires.LooseWeightDesire;
import de.feu.massim22.group3.agents.desires.V2desires.*;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>DesireUtilities</code> contains all the methods that are necessary for working the desires .
 * 
 * @author Melinda Betz
 * @author Heinz Stadler (minor contribution)
 */
//@SuppressWarnings("hiding")
public class DesireUtilities {
    //private StepUtilities stepUtilities;
    private TaskInfo task;
    private int maxTaskBlocks = 3;
    private int maxTypes = AgentCooperations.getMaxTypes();
    private String directionCircle = "cw";
    private int directionCounter = 0;
    private int circleSize = 30;
    private String dir2;
    private boolean dir2Used = false;
    private boolean inDirection = true;
    private int count = 0;
    private String lastWish = null;
    
    /**How many steps an agent wants to move forward.*/
    public int moveIteration = 0;
    
    /**The agent tried to move in its last wanted direction.*/
    public boolean tryLastWanted = true; 
    
    /**All the things that one agent has attached.*/
    public List<Thing> attachedThings = new ArrayList<Thing>();
    
    /** The last wish direction of the agent.*/
    public String lastWishDirection = null;
    
    /**
     * The method runs all task independent decisions.
     *
     * @param step - the active step
     * @param agent - the agent who wants to make the decisions
     * 
     * @return the decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgentV2 agent) {
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() Start - Step: " + step
                + " , Agent: " + agent.getName());

        if (doDecision(agent, new DigFreeDesire(agent.getBelief()))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , DigFreeDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , DigFreeDesire");
        
        if (doDecision(agent, new FreedomDesire(agent.getBelief()))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , FreedomDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , FreedomDesire");
        
        if (doDecision(agent, new LocalExploreDesire(agent.supervisor.getName(), agent))) {
            AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
            + " , LocalExploreDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
            + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
            + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
        } else
            AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
            + " , LocalExploreDesire");
        
        if (agent.blockAttached && agent.desireProcessing.attachedThings.size() > maxTaskBlocks 
                && doDecision(agent, new LooseWeightDesire(agent.getBelief()))) {
                AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                + " , LooseWeightDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters() 
                + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
            } else
                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , LooseWeightDesire");
        
            if (agent.getBelief().getRole().name().equals("default")
                    && doDecision(agent, new GoAdoptRoleDesire(agent, "worker"))) {
                AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                        + " , GoAdoptRoleDesire , Action: "
                        + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName()
                        + " , Parameter: "
                        + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                        + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
            } else
                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , GoAdoptRoleDesire - worker");

            if ((StepUtilities.exploreHorizontalMapSizeStarted || StepUtilities.exploreVerticalMapSizeStarted)
                    && !(StepUtilities.exploreHorizontalMapSizeFinished && StepUtilities.exploreVerticalMapSizeFinished)
                    && doDecision(agent, new ExploreMapSizeDesire(agent))) {
                AgentLogger
                        .info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                                + " , ExploreMapSizeDesire , Action: "
                                + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName()
                                + " , Parameter: "
                                + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                                 + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
            } else
                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , ExploreMapSizeDesire");
        
        agent.decisionsDone = true;
        return result;
    }
 

    /**
     * Checks if a decision (desire) can actually be done.
     *
     * @param agent - the agent himself
     * @param inDesire - the desire that is being checked
     * 
     * @return if the decision can be done or not
     */
    public boolean doDecision(BdiAgentV2 agent, IDesire inDesire) {
        boolean result = false;
      
        if (!inDesire.isFulfilled().value() 
                && !inDesire.isUnfulfillable().value() 
                && inDesire.isExecutable().value()) { // desire ist möglich , hinzufügen
            inDesire.setOutputAction(inDesire.getNextActionInfo().value());
            agent.getDesires().add(inDesire);
            result = true;
        }
        
        return result;
    }
    

    /**
     * The method runs all task dependent decisions.
     *
     * @param step - the active step
     * @param supervisor - the supervisor who wants to make the decisions
     * @param stepUtilities - the supervisors StepUtilities
     * 
     * @return the decisions are done
     */
    public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor, StepUtilities stepUtilities) {
        //this.stepUtilities = stepUtilities;
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName() + " , Agents: " + supervisor.getAgents());

        BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
        
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Dispenser: " + supervisorAgent.getBelief().getReachableDispensersX());

        List<String> allGroupAgents = new ArrayList<>(supervisor.getAgents());
        List<String> freeGroupAgents = new ArrayList<>(allGroupAgents);
        List<String> busyGroupAgents = new ArrayList<>();

        // loop over all tasks
        for (TaskInfo loopTask : supervisorAgent.getBelief().getTaskInfo()) {
            //the task has reached its deadline
            if(taskReachedDeadline ( supervisorAgent, loopTask)) {
                 continue;
            }
            task = loopTask;
            AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Task: " + task.name + " , " + task.requirements.size() + " , " + task.requirements);

           if ( task.requirements.size() > maxTaskBlocks) {
           //if ( task.requirements.size() > 2) {
               continue;
           }
           
            // all agents from one group
            for (String agentStr : allGroupAgents) {
                BdiAgentV2 agent = StepUtilities.getAgent(agentStr); 
                
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Agent: " + agentStr 
                        + " , Pos: " + agent.getBelief().getPosition()+ " , abs: " 
                                + ((Point.castToPoint(agent.getBelief().getAbsolutePosition()) != null) ? Point.castToPoint(agent.getBelief().getAbsolutePosition()) : "") + " , Step: " + agent.getBelief().getStep());
                AgentLogger.info(
                        Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: " + agent.getName()
                                + " , lA: " + agent.getBelief().getLastAction() + " , lAR: " + agent.getBelief().getLastActionResult());
                for (String para : agent.getBelief().getLastActionParams()) {
                    AgentLogger.info(
                            Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: " + agent.getName()
                                    + " , para: " + para);           
                }
              
                agent.desireProcessing.attachedThings = new ArrayList<Thing>();
                agent.desireProcessing.task = task;
                agent.desireProcessing.attachedThings = agent.getAttachedThings();

                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Agent: " + agentStr + " attachedThings: "
                        + agent.desireProcessing.attachedThings);
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , Things: " + agent.getBelief().getThings());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , ReachableDispensers: " + agent.getBelief().getReachableDispensersX());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , GoalZones: " + agent.getBelief().getGoalZones());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , ReachableGoalZones: " + agent.getBelief().getReachableGoalZones());
                AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() - Agent: "
                        + agent.getName() + " , nicht in Zone: " + !agent.getBelief().getGoalZones().contains(Point.zero()) 
                        + " , in Zone: "+ agent.getBelief().getGoalZones().contains(Point.zero()) + " , att. Size: "
                        + agent.desireProcessing.attachedThings.size());
                                
                if (!agent.blockAttached
                && doDecision(agent, new GoAbandonedBlockDesire(agent, getTaskBlockA(agent, task).type))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoAbandonedBlockDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoAbandonedBlockDesire");
                
                //String bType = (StepUtilities.getNumberAttachedBlocks(getTaskBlock(agent, task).type) < 4 ? getTaskBlock(agent, task).type : "b2");
                
                if (!agent.blockAttached 
                    && doDecision(agent, new GoDispenserDesire(getTaskBlockC(agent, task).type, supervisor.getName(), agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoDispenserDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoDispenserDesire");
                
                if (maxTaskBlocks > 1 && agent.blockAttached && task.requirements.size() > 1
                        && doDecision(agent, new HelperMultiBlocksDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , HelperMultiBlocksDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , HelperMultiBlocksDesire");
                
                if (maxTaskBlocks > 2 && agent.blockAttached && task.requirements.size() > 2
                        && doDecision(agent, new Helper2MultiBlocksDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , Helper2MultiBlocksDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                        + " , Helper2MultiBlocksDesire");

                if (agent.blockAttached && !agent.getBelief().getGoalZones().contains(Point.zero()) 
                    && doDecision(agent, new GoGoalZoneDesire(agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , GoGoalZoneDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , GoGoalZoneDesire");

                if (agent.blockAttached && task.requirements.size() == 1 && agent.getBelief().getGoalZones().contains(Point.zero())
                    && doDecision(agent, new ArrangeBlockDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , ArrangeBlockDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                    } else
                    AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                            + " , ArrangeBlockDesire");
                                
                if (maxTaskBlocks > 1 && task.requirements.size() > 1 && agent.blockAttached && agent.getBelief().getGoalZones().contains(Point.zero())
                        && doDecision(agent, new MasterMultiBlocksDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , MasterMultiBlocksDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , MasterMultiBlocksDesire");
                
                if (maxTaskBlocks > 1 && task.requirements.size() > 1 && agent.blockAttached 
                        && doDecision(agent, new ConnectMultiBlocksDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , ConnectMultiBlocksDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
                        } else
                        AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: " + agent.getName()
                                + " , ConnectMultiBlocksDesire");

                if (agent.blockAttached && agent.getBelief().getGoalZones().contains(Point.zero())
                    && doDecision(agent, new SubmitDesire(task, agent))) {
                    AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: " + agent.getName()
                    + " , SubmitDesire , Action: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getName() 
                    + " , Parameter: " + agent.getDesires().get(agent.getDesires().size() - 1).getOutputAction().getParameters()
                    + " , Task: " + task.name + " , Prio: " + getPriority(agent.getDesires().get(agent.getDesires().size() - 1), agent));
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
    
    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority
     * @param agent - the agent the desire is for
     * 
     * @return the priority
     */
    public int getPriority(IDesire desire, BdiAgentV2 agent) {
        int result = 0;

        switch (desire.getName()) {
        
        case "DigFreeDesire": // this is a BdiAgentV1-Desire
            result = 1900;
            break;
        case "FreedomDesire": // this is a BdiAgentV1-Desire
            result = 2000;
            break;
        case "LocalExploreDesire":
            result = 100;
            break; 
        case "GoAdoptRoleDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else
                if (desire.getOutputAction().getName().equals(Actions.ADOPT))
                    // get new role even during explore map size which has priority 2000
                    result = 2500;
                else
                    result = 1500;
            break;
        case "ExploreMapSizeDesire":
            result = 2000;
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
        case "ArrangeBlockDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 10;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 450;
            else
                result = 500;
            break;
        case "HelperMultiBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.CONNECT))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 1000;
            else
                result = 600;
            break;
        case "Helper2MultiBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.CONNECT))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 1000;
            else
                result = 600;
            break;
        case "MasterMultiBlocksDesire":
            if (desire.getOutputAction().getName().equals(Actions.SKIP))
                result = 1000;
            else if (desire.getOutputAction().getName().equals(Actions.DETACH))
                result = 550;
            else if (((MasterMultiBlocksDesire) desire).getTask().requirements.size() == 2)
                result = 600;
            else
                result = 700;
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
        case "LooseWeightDesire": // this is a BdiAgentV1-Desire
            result = 1400;
            break;
        case "DisconnectMultiBlocksDesire": // this is not used anymore
            result = 2500;
            break;
        }

        return result;
    }

    /**
     * The method determines the Intention for a certain agent .
     *
     * @param agent - the agent that needs a intention
     * 
     * @return the intention
     */
    public synchronized IDesire determineIntention(BdiAgentV2 agent) {
        IDesire result = null;
        int priority = 0;
        
        for (IDesire desire : agent.getDesires()) {
            if (getPriority(desire, agent) > priority) {
                result = desire;
                priority = getPriority(desire, agent);
            }
        }
        
        return result;
    }

    /**
     * Gets the nearest dispenser from the list of reachableDispensers.
     *
     * @param inZoneList - a list of all dispensers in reach
     * 
     * @return the nearest dispenser
     */
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
    
    /**
     * Determines the direction in which the agent should walk circles while exploring.
     *
     * @param agent - the agent that wants to walk the circles
     * @param stepWidth - how big every step of the agent is
     * 
     * @return the direction for the next circle
     */
    public Identifier walkCircles(BdiAgentV2 agent, int stepWidth) {
        String startDirection = DirectionUtil.intToString(agent.exploreDirection);
        Identifier resultDirection = new Identifier(startDirection);

        if (agent.getBelief().getLastAction() != null && agent.getBelief().getLastAction().equals(Actions.MOVE)) {
            directionCounter++;
            resultDirection = new Identifier(agent.getBelief().getLastActionParams().get(0));

            if (directionCircle.equals("cw")) {
                if (agent.getBelief().getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.getBelief().getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                    if (agent.getBelief().getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                    if (agent.getBelief().getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                    if (agent.getBelief().getLastActionParams().get(0).equals("w")) {
                        resultDirection = new Identifier("n");
                        circleSize = circleSize + stepWidth;
                    }
                    directionCounter = 0;
                    directionCircle = "ccw";
                }
            } else {
                if (agent.getBelief().getLastAction().equals("move") && directionCounter >= circleSize) {
                    if (agent.getBelief().getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("w");
                    if (agent.getBelief().getLastActionParams().get(0).equals("w")) resultDirection = new Identifier("s");
                    if (agent.getBelief().getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("e");
                    if (agent.getBelief().getLastActionParams().get(0).equals("e")) {
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
       
    /**
     * Has the task reached its deadline?
     *
     *@param agent - the agent that wants to do the task
     * @param task - the task that is being examined
     * 
     * @return it has expired or not
     */   
    public boolean taskReachedDeadline (BdiAgentV2 agent,TaskInfo inTask) {
        boolean result = false;
        if (agent.getBelief().getStep() > inTask.deadline) {
            //task has expired
            result = true;
        }
        return result;
    }
       
    /**
     * Gets the first block for a certain task.
     *
     *@param agent - the agent that wants to get the block
     * @param task - the task that is being done
     * 
     * @return the required block
     */
    private Thing getTaskBlockA(BdiAgentV2 agent, TaskInfo inTask) {
        List<Thing> reqs = getTaskReqsOrdered(inTask);
        // get block1 by default
        Thing result = reqs.get(0);
        
        if (inTask.requirements.size() > 1) {
            // Multi Block Tasks
            for (Meeting meeting : AgentMeetings.find(agent)) {
                if (!meeting.agent2().getAttachedThings().isEmpty()) {
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        // Do I know a agent with block type of block1, get block2
                        if (attachedThing.details.equals(reqs.get(0).type)) {
                            result = reqs.get(1);                                
                            break;  
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Decides which block for a certain task should be fetched.
     *
     *@param agent - the agent that wants to get the block
     * @param task - the task that is being done
     * 
     * @return the required block
     */
    private Thing getTaskBlockC(BdiAgentV2 agent, TaskInfo inTask) {
        List<Thing> reqs = getTaskReqsOrdered(inTask);
        // get block1 by default
        Thing result = reqs.get(0);

        if (inTask.requirements.size() > 1) {
            // Multi Block Tasks
            for (Meeting meeting : AgentMeetings.find(agent)) {               
                if (!meeting.agent2().getAttachedThings().isEmpty()) {                    
                    for (Thing attachedThing : meeting.agent2().getAttachedThings()) {
                        // Do I know a agent with block type of block1, get block2
                        if (attachedThing.details.equals(reqs.get(0).type)) {
                                result = reqs.get(1);                                
                                break;   
                                // Do I know a agent with block type of block2, get block3
                        } else if (inTask.requirements.size() == 3 && attachedThing.details.equals(reqs.get(1).type)) {
                            result = reqs.get(2);
                        } 
                    }
                }
            }
        }

        result = proofBlockType(result, reqs);
                
        AgentLogger.info(Thread.currentThread().getName() + " getTaskBlockC - agent: " + agent.getName() + " , task: " + inTask.name 
                + " , block1: " + reqs.get(0).toString() + " , block2: " + (inTask.requirements.size() >= 2 ? reqs.get(1).toString() : "") 
                + " , block3: " + (inTask.requirements.size() >= 3 ? reqs.get(2).toString() : "") + " , result: " + result.toString());        

        return result;
    }
  
    /**
     * Is one more block type allowed?
     *
     * @param inBlock - the block to be fetched
     * @param inReqs - the requirements of the task 
     * 
     * @return a block with an allowed block type
     */
    private Thing proofBlockType(Thing inBlock, List<Thing> inReqs) {
        Thing result = inBlock;
        AgentLogger.info(Thread.currentThread().getName() + " proofBlockType - type: " + inBlock.type 
                + " , number: " + StepUtilities.getNumberAttachedBlocks(inBlock.type) 
                + " , reqs: " + inReqs); 
        
        count++;
        
        if (count <= 3) {
            // should prevent that 3-block-tasks with three blocks of the same block type and a low maxTypes het stuck
            if (StepUtilities.getNumberAttachedBlocks(inBlock.type) >= maxTypes) {
                if (inBlock.equals(inReqs.get(0))) {
                    if (inReqs.size() > 1)
                        result = proofBlockType(inReqs.get(1), inReqs);
                    else
                        result = proofBlockType(inReqs.get(0), inReqs);
                } else if (inBlock.equals(inReqs.get(1))) {
                    if (inReqs.size() > 2)
                        result = proofBlockType(inReqs.get(2), inReqs);
                    else
                        result = proofBlockType(inReqs.get(0), inReqs);
                } else if (inBlock.equals(inReqs.get(2))) {
                    result = proofBlockType(inReqs.get(0), inReqs);
                }
            }
        } else {
            count = 0;
        }

        return result;
    }
    
    /**
     * Gets task requirements and orders them.
     *
     * @param task - the requirements of the task that are to be ordered
     * 
     * @return the ordered requirements
     */
    public List<Thing> getTaskReqsOrdered(TaskInfo inTask) {
        List<Thing> result = new ArrayList<Thing>();
        Thing block1 = null;
        Thing block2 = null;
        Thing block3 = null;

        for (int i = 0; i < inTask.requirements.size(); i++) {
            if (DirectionUtil.getCellsIn4Directions()
                    .contains(new java.awt.Point(inTask.requirements.get(i).x, inTask.requirements.get(i).y))) {
                block1 = inTask.requirements.get(i);
            } else if (block2 == null && !DirectionUtil.getCellsIn4Directions()
                    .contains(new java.awt.Point(inTask.requirements.get(i).x, inTask.requirements.get(i).y))
                    && existsCommonEdge(new Point(inTask.requirements.get(i).x, inTask.requirements.get(i).y))) {
                block2 = inTask.requirements.get(i);
            } else
                block3 = inTask.requirements.get(i);
        }
        
        result.add(block1);
        
        if (inTask.requirements.size() > 1) 
            result.add(block2);
        
        if (inTask.requirements.size() > 2) 
            result.add(block3);
        
        return result;
    }
    
    private ActionInfo getIteratedActionForMove(BdiAgentV2 agent, String dir, String desire) {
        tryLastWanted = false;
        moveIteration++;
        
        if (moveIteration < 4) {
            return getActionForMove(agent, dir, desire);
        }

        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - getIteratedActionForMove - stuck");
        return ActionInfo.SKIP("Agent is Stuck in iterated");
    }
    
    /**
     * Determines what a agent should do if it has the action move and it has got a alternate direction.
     *
     * @param agent - the agent that wants to do a move
     * @param dir - the direction in which the agent wants to move first
     * @param dirAlt - the alternate direction 
     * @param desire - the desire which the agent wants to do
     * 
     * @return the action to do
     */
    public ActionInfo getActionForMoveWithAlternate(BdiAgentV2 agent, String dir, String dirAlt, String desire) {
        ActionInfo firstTry = getActionForMove(agent, dir, desire);
        String lastRotation = agent.getBelief().getLastActionParams().size() > 0 ? agent.getBelief().getLastActionParams().get(0) : "";
       
        if ((firstTry.value().getName().equals(Actions.MOVE) 
                && !((Identifier) firstTry.value().getParameters().get(0)).toProlog().equals(dir)  
                && !((Identifier) firstTry.value().getParameters().get(0)).toProlog().equals(dirAlt))
                || (firstTry.value().getName().equals(Actions.ROTATE) 
                && ((Identifier) firstTry.value().getParameters().get(0)).toProlog().equals("cw")
                && lastRotation.equals("ccw")) 
                || (firstTry.value().getName().equals(Actions.ROTATE)  
                && ((Identifier) firstTry.value().getParameters().get(0)).toProlog().equals("ccw")
                && lastRotation.equals("cw"))) {
            return getActionForMove(agent, dirAlt, desire);
        } 
       
        return firstTry;
    }

    /**
     * Determines what a agent should do if it has the action move and wants to move two steps.
     *
     * @param agent - the agent that wants to do a move
     * @param dir - the direction in which the agent wants to move first
     * @param dir2 - the direction in which the agent wants to move second
     * @param desire - the desire which the agent wants to do
     * 
     * @return the action to do
     */
    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String inDir2, String desire) {
        this.dir2 = inDir2;
        dir2Used = true;
        ActionInfo out = getActionForMove(agent, dir, desire);
        dir2Used = false;
        return out;
    }
    
    /**
     * Determines what a agent should do if it has the action move.
     *
     * @param agent - the agent that wants to do a move
     * @param dir - the direction in which the agent wants to move
     * @param desire - the desire which the agent wants to do
     * 
     * @return the action to do
     */
    public ActionInfo getActionForMove(BdiAgentV2 agent, String dir, String desire) {
        Point dirPoint = Point.castToPoint(DirectionUtil.getCellInDirection(dir));
        //Melinda start
        List<Point> attached = agent.getAttachedPoints();       
        //List<Point> attached = belief.getAttachedPoints();
        //Melinda end
        // Rotate attached
        if (agent.blockAttached) {
            for (Point p : attached) {
                Point testPoint = new Point(p.x + dirPoint.x, p.y + dirPoint.y);
                Thing t = agent.getBelief().getThingAt(testPoint);
                AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - Direction: " + dir
                        + " , Block attached: " + p + " , in Richtung: " + testPoint);
                if (!isFree(t) && !testPoint.equals(new Point(0, 0))) {
                    // Can be rotated
                    Thing cw = agent.getBelief().getThingCRotatedAt(p);
                    Thing ccw = agent.getBelief().getThingCCRotatedAt(p);
                    Point cwP = getCRotatedPoint(p);
                    Point ccwP = getCCRotatedPoint(p);
                    AgentLogger.info(
                            Thread.currentThread().getName() + " getActionForMove - cw: " + cwP + " , ccw: " + ccwP);
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - oppositeD: "
                            + DirectionUtil.oppositeDirection(dir) + " , Cell: "
                            + DirectionUtil.getCellInDirection(DirectionUtil.oppositeDirection(dir)));
                    AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - cw: " + cw + " , Free?: "
                            + isFree(cw));
                    String lastRotation = agent.getBelief().getLastActionParams().size() > 0
                            ? agent.getBelief().getLastActionParams().get(0)
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
        Thing t = agent.getBelief().getThingAt(dirPoint);
        String lastDir = "";
        String lastWantedDir = "";
        //boolean tryLastWanted = this.tryLastWanted;
        //this.tryLastWanted = true;
              
        if (agent.getBelief().getLastAction() != null && agent.getBelief().getLastAction().equals(Actions.MOVE) 
                && !agent.getBelief().getLastActionResult().equals(ActionResults.FAILED)) {
            lastDir = agent.getBelief().getLastActionParams().get(0);
            lastWantedDir = lastWish;
            //lastWish = dir;
        }
        
        AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - t: " + t + " , " + dir + " , "
        + lastDir + " , " + lastWantedDir + " , " + DirectionUtil.oppositeDirection(lastDir));
        
        if (t != null && (t.type.equals(Thing.TYPE_OBSTACLE) /*|| (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint))*/)) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if2");
            return ActionInfo.CLEAR(dirPoint, desire);
            
        } else if ((isFree(t) || attached.contains(dirPoint)) 
                && (lastDir.equals(lastWantedDir) || !(dir.equals(DirectionUtil.oppositeDirection(lastDir))))) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if3");
            lastWish = dir;

            if (dir2Used)
                return ActionInfo.MOVE(dir, dir2, desire);
            else                 
                return ActionInfo.MOVE(dir, desire);

        } else if (tryLastWanted && lastWantedDir != null && !lastDir.equals(lastWantedDir) && dir.equals(DirectionUtil.oppositeDirection(lastDir))) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if3.5");
            
            return getIteratedActionForMove(agent, lastWantedDir, desire);
            
        } else if (t != null && (t.type.equals(Thing.TYPE_ENTITY)                
                || (t.type.equals(Thing.TYPE_BLOCK) && !attached.contains(dirPoint)))) {
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove - if4");
            
            // Try to move around agent
            inDirection = inDirection ? false : true;
            String dir1 = inDirection ? getCRotatedDirection(dir) : getCCRotatedDirection(dir);
            String lDir2 = inDirection ? getCCRotatedDirection(dir) : getCRotatedDirection(dir);
            Thing tDir1 = agent.getBelief().getThingAt(dir1);
            Thing tDir2 = agent.getBelief().getThingAt(lDir2);
            Thing tDir1next = agent.getBelief().getThingAt(DirectionUtil.getCellInDirection(DirectionUtil.getCellInDirection(dir), dir1));
            Thing tDir2next = agent.getBelief().getThingAt(DirectionUtil.getCellInDirection(DirectionUtil.getCellInDirection(dir), lDir2));
            
            AgentLogger.info(Thread.currentThread().getName() + " getActionForMove: " + dir1 + " , " + lDir2 + " , " + tDir1 + " , " + tDir2 + " , " + tDir1next + " , " + tDir2next);

            if ((isFree(tDir1) || attached.contains(new Point(tDir1.x, tDir1.y)) || isSaveClearable(tDir1)) 
                    && (isFree(tDir1next) || isSaveClearable(tDir1next))) {
                return getIteratedActionForMove(agent, dir1, desire);
            }

            if ((isFree(tDir2) || attached.contains(new Point(tDir2.x, tDir2.y)) || isSaveClearable(tDir2)) 
                    && (isFree(tDir2next) || isSaveClearable(tDir2next))) {
                return getIteratedActionForMove(agent, lDir2, desire);
            }
            
            if (isFree(tDir1) || attached.contains(new Point(tDir1.x, tDir1.y)) || isSaveClearable(tDir1)) {
                return getIteratedActionForMove(agent, dir1, desire);
            }

            if (isFree(tDir2) || attached.contains(new Point(tDir2.x, tDir2.y)) || isSaveClearable(tDir2)) {
                return getIteratedActionForMove(agent, lDir2, desire);
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
    
    private boolean isFree(Thing t) {
        return t == null || t.type.equals(Thing.TYPE_DISPENSER);
    }
   
    private boolean isSaveClearable(Thing t) {
        return t != null && (t.type.equals(Thing.TYPE_OBSTACLE));
    }

    /**
     * Rotates a certain point clockwise.
     *
     * @param p - the point that is going to be rotated
     * 
     * @return the rotated point
     */
    public Point getCRotatedPoint(Point p) {
        return new Point(-p.y, p.x);
    }
    
    /**
     * Rotates a certain point counter clockwise.
     *
     * @param p - the point that is going to be rotated
     * 
     * @return the rotated point
     */
    public Point getCCRotatedPoint(Point p) {
        return new Point(p.y, -p.x);
    }

    private String getCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "e";
            case "e": return "s";
            case "s": return "w";
            default: return "n";
        }
    }

    private String getCCRotatedDirection(String dir) {
        switch (dir) {
            case "n": return "w";
            case "e": return "n";
            case "s": return "e";
            default: return "s";
        }
    }
    
    private boolean existsCommonEdge(Point p2) {
        for (java.awt.Point p1 : DirectionUtil.getCellsIn4Directions()) {
            if ((Math.abs(p2.x - p1.x) == 0 && Math.abs(p2.y - p1.y) == 1)
                    || (Math.abs(p2.y - p1.y) == 0 && Math.abs(p2.x - p1.x) == 1)) {
                return true;
            }
        }

        return false;
    }
}
