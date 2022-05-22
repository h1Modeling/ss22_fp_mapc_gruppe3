package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;
import java.util.*;

import de.feu.massim22.group3.agents.Belief.*;
import de.feu.massim22.group3.agents.Desires.ADesires.ADesire;
import de.feu.massim22.group3.agents.Desires.ADesires.AdoptRole;
import de.feu.massim22.group3.agents.Desires.ADesires.DigFree;
import de.feu.massim22.group3.agents.Desires.ADesires.DodgeClear;
import de.feu.massim22.group3.agents.Desires.ADesires.GetBlock;
import de.feu.massim22.group3.agents.Desires.ADesires.GoDispenser;
import de.feu.massim22.group3.agents.Desires.ADesires.GoGoalZone;
import de.feu.massim22.group3.agents.Desires.ADesires.GoRoleZone;
import de.feu.massim22.group3.agents.Desires.ADesires.LocalExplore;
import de.feu.massim22.group3.agents.Desires.ADesires.Submit;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import de.feu.massim22.group3.agents.BeliefTypes.*;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.messages.scenario.Actions;

public class DesireUtilities {
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
                + " , Supervisor: " + agent.getName());

        doDecision(agent, new DodgeClear(agent, this));
        doDecision(agent, new DigFree(agent, this));
        //doDecision(agent, new HinderEnemy(agent, this));
        doDecision(agent, new GoGoalZone(agent, this));
        doDecision(agent, new GoRoleZone(agent, this));
        doDecision(agent, new GoDispenser(agent, this));
        doDecision(agent, new Submit(agent, this));
        doDecision(agent, new GetBlock(agent, this));
        doDecision(agent, new AdoptRole(agent, this));
        //doDecision(agent, new RemoveObstacle(agent, this));
        doDecision(agent, new LocalExplore(agent, this));
        
        agent.decisionsDone = true;
        return result;
    }
    
    boolean doDecision(BdiAgentV2 agent, ADesire inDesire) {
        boolean result = false;
        
        if (inDesire.isExecutable()) { // desire ist möglich , hinzufügen
            inDesire.outputAction = inDesire.getNextAction();
            getPriority(inDesire);
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
    public synchronized boolean runSupervisorDecisions(int step, Supervisor supervisor) {
        boolean result = false;
        int minBlockCount = 1000;
        TaskInfo minTask;
        
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName());
       
        BdiAgentV2 supervisorAgent = StepUtilities.getAgent(supervisor.getName());
        Set<TaskInfo> set = supervisorAgent.belief.getTaskInfo();
        
        //Schleife über alle Tasks
        for(TaskInfo  t : set) {
        	if(minBlockCount > t.requirements.size()) {
        		//Task mit wenigsten Blöcken
        		minBlockCount = t.requirements.size(); 
        		minTask = t;
        	}
        }
        
        //Schleife über alle Tasks
        for(TaskInfo  task : set) {
        	
        }
        
        supervisor.setDecisionsDone(true);
        return result;
    }

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority
     * 
     * @return int - the priority
     */
    public int getPriority(ADesire desire) {
        int result = 0;

        switch (desire.name) {
        case "DigFree":
            result = 10;
        case "DodgeClear":
            result = 20;
        case "LocalHinderEnemy":
            result = 90;
        case "LocalGetBlocks":
            result = 70;
        case "GoGoalZone":
            result = 80;
        case "GoRoleZone":
            result = 30;
        case "GoDispenser":
            result = 30;
        case "Submit":
            result = 30;  
        case "GetBlock":
            result = 30;
        case "AdoptRole":
            result = 30;
        case "ReactToNorm":
            result = 40;
        case "LocalExplore":
            result = 100;
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
    public ADesire determineIntention(BdiAgentV2 agent) {
        ADesire result = null;
        int priority = 1000;
        for (ADesire desire : agent.desires) {
            AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName()
                    + " , Desire: " + desire.name + " , Action: " + desire.outputAction);
            if (desire.priority < priority) {
                result = desire;
                priority = desire.priority;
            }
        }

        AgentLogger.info(Thread.currentThread().getName() + " determineIntention() End - Agent: " + agent.getName()
                + " , Intention: " + result.name + " , Action: " + result.outputAction);
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
        Identifier resultDirection = new Identifier("n");

        if (agent.belief.getLastAction() != null && agent.belief.getLastAction().equals(Actions.MOVE)) {
            agent.directionCounter++;
            resultDirection = new Identifier(agent.belief.getLastActionParams().get(0));

            if (agent.belief.getLastAction().equals("move") && agent.directionCounter >= agent.circleSize) {
                if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                if (agent.belief.getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                if (agent.belief.getLastActionParams().get(0).equals("w")) {
                    resultDirection = new Identifier("n");
                    agent.circleSize = agent.circleSize + stepWidth;
                }
                agent.directionCounter = 0;
            }
        }
        return resultDirection;
    }
}

