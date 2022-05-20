package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.agents.Belief.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;
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
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step
                + " , Supervisor: " + supervisor.getName());
        supervisor.decisionsDone = true;
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

    ReachableGoalZone getNearestGoalZone(List<ReachableGoalZone> inZoneList) {
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
    
    ReachableRoleZone getNearestRoleZone(List<ReachableRoleZone> inZoneList) {
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

    ReachableDispenser getNearestDispenser(List<ReachableDispenser> inZoneList) {
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
    
    Identifier walkCircles(BdiAgentV2 agent, int stepWidth) {
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

class DirectionUtil {
    static String intToString(int direction) {
        String s = String.valueOf(direction).replaceAll("1", "n").replaceAll("2", "e").replaceAll("3", "s")
                .replaceAll("4", "w");

        return new StringBuilder(s).reverse().toString();
    }

    static int stringToInt(String direction) {
        String s = String.valueOf(direction).replaceAll("n", "1").replaceAll("e", "2").replaceAll("s", "3")
                .replaceAll("w", "4");

        return Integer.parseInt(s);
    }

    static Point getCellInDirection(String direction) {
        int x = 0;
        int y = 0;
        ;

        switch (direction) {
        case "n":
            x = 0;
            y = -1;
            break;
        case "e":
            x = 1;
            y = 0;
            break;
        case "s":
            x = 0;
            y = 1;
            break;
        case "w":
            x = -1;
            y = 0;
        }
        return new Point(x, y);
    }

    static String getDirection(Point from, Point to) {
        String result = " ";
        Point pointTarget = new Point(to.x - from.x, to.y - from.y);

        if (pointTarget.x == 0) {
            if (pointTarget.y < 0)
                result = "n";
            else
                result = "s";
        }

        if (pointTarget.y == 0) {
            if (pointTarget.x < 0)
                result = "w";
            else
                result = "e";
        }

        if (pointTarget.x != 0 && pointTarget.y != 0) {
            if (java.lang.Math.abs(pointTarget.x) > Math.abs(pointTarget.y))
                if (pointTarget.x < 0)
                    result = "w";
                else
                    result = "e";
            else if (pointTarget.y < 0)
                result = "n";
            else
                result = "s";
        }

        return result;
    }
}
