package de.feu.massim22.group3.agents;

import java.util.*;
import java.util.List;

import de.feu.massim22.group3.map.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;

import java.awt.*;
import java.nio.FloatBuffer;

import massim.protocol.data.Thing;

public class StepUtilities {
    public static ArrayList<BdiAgentV2> allAgents = new ArrayList<BdiAgentV2>();
    public static Set<Supervisor> allSupervisors = new HashSet<Supervisor>();
    private static int countAgent = 0;

    /**
     * The agent has initiated his map update.
     * 
     * @param agent    - the agent which has updated the map
     * @param step     - the step in which the program is at the moment
     * @param teamSize - the size of the team of which the agent is part of
     * 
     * @return boolean - the agent is done updating the map
     */
    public static synchronized boolean reportMapUpdate(BdiAgentV2 agent, int step, int teamSize) {
        boolean result = false;
        countAgent++;

        if (countAgent == teamSize) {
            countAgent = 0;
            result = true;
        }

        return result;
    }

    /**
     * All the things that have to be done to merge two groups together, update the
     * maps for the resulting groups and do some group/supervisor decisions.
     *
     * @param step - the step in which the program is at the moment
     * 
     * @return boolean - group merge was a success
     */
    public void doGroupProcessing(int step) {
        BdiAgentV2 agent1;
        BdiAgentV2 agent2;

        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() in neuem Thread - Step: " + step);
        ArrayList<AgentMeeting> foundAgent = new ArrayList<AgentMeeting>();
        Set<Supervisor> exSupervisors = new HashSet<Supervisor>();
        Set<Thing> things = new HashSet<>();
        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() allSupervisors: " + allSupervisors);        

        if (allSupervisors.size() > 1) {
         // Noch gibt es mehr als einen Supervisor
            for (BdiAgentV2 agent : allAgents) {
                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Start - Agent: " + agent.getName());
                things = agent.belief.getThings();
                for (Thing thing : things) {
                    // Agent hat in seiner Vision einen anderen Agent
                    if (thing.type.equals(Thing.TYPE_ENTITY)) {
                        // dieser ist aus dem gleichen Team
                        if (thing.details.equals(agent.belief.getTeam())) {
                            // und nicht er selbst
                            if (thing.x != 0 && thing.y != 0) {
                                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Found - Agent: " + agent.getName()
                                        + " , Position: " + new Point(thing.x, thing.y));
                                // also ein Kandidat zum mergen
                                foundAgent.add(new AgentMeeting(agent, new Point(thing.x, thing.y)));
                            }
                        }
                    }
                }
            }

            // Agents suchen die sich getroffen haben
            for (int j = 0; j < foundAgent.size(); j++) {
                for (int k = j + 1; k < foundAgent.size(); k++) {
                    // bei einem Treffen müssen sich beide gesehen haben und die relativen Koordinaten
                    // dürfen sich nur im Vorzeichen unterscheiden
                    if ((foundAgent.get(k).position.x == -foundAgent.get(j).position.x)
                            && (foundAgent.get(k).position.y == -foundAgent.get(j).position.y)) {
                        agent1 = foundAgent.get(j).agent;
                        agent2 = foundAgent.get(k).agent;

                        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() meeting in vision - Agent1: " + agent1.getName() + " , Agent2: "
                                + agent2.getName());
                        // Agents sind aus unterschiedlichen Gruppen ?
                        if (!(agent1.getSupervisor() == agent2.getSupervisor())) {
                            // dann die kleinere in die größere Gruppe mergen
                            if (agent1.getSupervisor().getAgents().size() >= agent2.getSupervisor().getAgents()
                                    .size()) {
                                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() merge 2 in 1 - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                        + agent2.getSupervisor().getName());
                                // Gruppe von agent2 in Gruppe von agent1 mergen
                                exSupervisors.add(agent2.getSupervisor());
                                mergeGroups(agent1.getSupervisor(), agent2.getSupervisor(), foundAgent.get(j).position);                               
                            } else {
                                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() merge 1 in 2 - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                        + agent2.getSupervisor().getName());
                                // Gruppe von agent1 in Gruppe von agent2 mergen
                                exSupervisors.add(agent1.getSupervisor());
                                mergeGroups(agent2.getSupervisor(), agent1.getSupervisor(), foundAgent.get(k).position);
                            }
                        } else AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() no merge, already in same group - Supervisor1: " + agent1.getSupervisor().getName() + " , Supervisor2: "
                                + agent2.getSupervisor().getName());
                    }
                }
            }
            
            allSupervisors.removeAll(exSupervisors);
        }
        /*
         * loop for all groups (after merge) with map update and group gecisions
         */
        for (Supervisor supervisor : allSupervisors) {
            AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Loop - Supervisor: " + supervisor.getName());
            
            /* Aufruf Pathfinder (GLFW / glDispatchCompute) */
            //AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Befor Test updateSupervisor()");
            //Navi.<INaviAgentV2>get().updateSupervisor(supervisor.getName());
            //AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() After Test updateSupervisor()");

            Runnable runnable = () -> { // Gruppenmap berechnen 
                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Before calcGroup() - Supervisor: " + supervisor.getName());
                List<CalcResult> agentCalcResults = calcGroup(supervisor);
                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() After calcGroup() - Supervisor: " + supervisor.getName()); 
                
                for (CalcResult agentCalcResult : agentCalcResults) {
                    AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Loop - agentCalcResult: " + agentCalcResult.agent());
                    BdiAgentV2 agent = getAgent(agentCalcResult.agent());
                    List<Parameter> parameters = agentCalcResult.percepts().getParameters();
                    AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Before updateFromPathFinding() - agentCalcResult: " + agentCalcResult.agent());
                    agent.belief.updateFromPathFinding(parameters);
                    agent.beliefsDone = true;
                    AgentLogger.info(agent.belief.reachablesToString());
                }
                AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Before runSupervisorDecisions() - Supervisor: " + supervisor.getName());                
                runSupervisorDecisions(step, supervisor);
            };
            Thread t3 = new Thread(runnable);
            t3.start();
        }

        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Step: " + step);
    }

    /**
     * The method runs the different agent decisions.
     *
     * @param agent - the agent who wants to make the decisions
     * 
     * @return boolean - the agent decisions are done
     */
    public synchronized boolean runAgentDecisions(int step, BdiAgentV2 agent) {
        boolean result = false;
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() Start - Step: " + step + " , Supervisor: " + agent.getName());
        
        DodgeClear dodge = new DodgeClear(agent);
        if (dodge.isExecutable(dodge)) { // desire ist möglich , hinzufügen
            dodge.outputAction = dodge.getNextAction();
            getPriority(dodge);
            agent.desires.add(dodge);
        }

        DigFree dig = new DigFree(agent);
        if (dig.isExecutable(dig)) { // desire ist möglich , hinzufügen
            dig.outputAction = dig.getNextAction();
            getPriority(dig);
            agent.desires.add(dig);
        }

        HinderEnemy hinder = new HinderEnemy(agent);
        
        GoGoalZone goGoalZone = new GoGoalZone(agent);
        if (dig.isExecutable(goGoalZone)) { // desire ist möglich , hinzufügen
            dig.outputAction = goGoalZone.getNextAction();
            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() - Action: " + goGoalZone.outputAction);
            getPriority(goGoalZone);
            agent.desires.add(goGoalZone);
        }
        
        GoRoleZone goRoleZone = new GoRoleZone(agent);
        if (dig.isExecutable(goRoleZone)) { // desire ist möglich , hinzufügen
            dig.outputAction = goRoleZone.getNextAction();
            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() - Action: " + goRoleZone.outputAction);
            getPriority(goRoleZone);
            agent.desires.add(goRoleZone);
        }
        
        GoDispenser goDispenser = new GoDispenser(agent);
        if (dig.isExecutable(goDispenser)) { // desire ist möglich , hinzufügen
            dig.outputAction = goDispenser.getNextAction();
            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() - Action: " + goDispenser.outputAction);
            getPriority(goDispenser);
            agent.desires.add(goDispenser);
        }
        
        // RemoveObstacle removeObstacle = new RemoveObstacle(agent);
        
        LocalExplore expl = new LocalExplore(agent);
        if (expl.isExecutable(expl)) { // desire ist möglich , hinzufügen
            expl.outputAction = expl.getNextAction();
            AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions() - Action: " + expl.outputAction);
            getPriority(expl);
            agent.desires.add(expl);
        }

        agent.decisionsDone = true;
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
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions() Start - Step: " + step + " , Supervisor: " + supervisor.getName());
        supervisor.decisionsDone = true;
        return result;
    }

    /**
     * Update the Map.
     * 
     * @param agent - the Agent that wants to update the map
     *
     */
    public void updateMap(BdiAgentV2 agent) {
        AgentLogger.info(Thread.currentThread().getName() + " Before updateMap() - Step: " + agent.belief.getStep() + " , Agent: " + agent.getName());
        Navi.<INaviAgentV2>get().updateMap(agent.getSupervisor().getName(), agent.getName(), agent.index,
                agent.belief.getPosition(), agent.belief.getVision(), agent.belief.getThings(),
                agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep());
        AgentLogger.info(Thread.currentThread().getName() + " After updateMap() - Step: " + agent.belief.getStep() + " , Agent: " + agent.getName());
    }

    /**
     * The method has a certain priority for every desire.
     *
     * @param desire - the desire that needs a priority 
     * 
     * @return int - the priority
     */
    public int getPriority(Desire desire) {
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
        case "ReactToNorm":
            result = 40;
        case "GoDispenser":
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
    public Desire determineIntention(BdiAgentV2 agent) {
        Desire result = null;
        int priority = 1000;
        for (Desire desire : agent.desires) {
            AgentLogger.info(Thread.currentThread().getName() + " determineIntention() - Agent: " + agent.getName() + " , Desire: " + desire.name + " , Action: " +  desire.outputAction);
            if (desire.priority < priority) {
                result = desire;
                priority = desire.priority;
            }
        }
        
        AgentLogger.info(Thread.currentThread().getName() + " determineIntention() End - Agent: " + agent.getName() + " , Intention: " + result.name + " , Action: " +  result.outputAction);
        return result;
    }
    
    /**
     * The method merges two groups together
     *
     * @param supervisorGroup - the supervisor of the group that the other group is going to be merged into
     * @param supervisorToMerge - the supervisor of the group that is going to be merged into the other group
     * 
     */
	public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge, Point relativOldSupervisor) {
        AgentLogger.info(Thread.currentThread().getName() + " mergeGroups() Start - Supervisor: " + supervisorGroup.getName() + " , OldSupervisor: " + supervisorToMerge.getName() + " , " + relativOldSupervisor);
		List<String> agentsSupervisorGroup = supervisorGroup.getAgents();
		List<String> agentsSupervisorToMerge = supervisorToMerge.getAgents();

		// Agents von agentsSupervisorToMerge in die Liste der Agents von agentsSupervisorGroup
		agentsSupervisorGroup.addAll(agentsSupervisorToMerge);
        Point posNewSupervisor = Navi.<INaviAgentV2>get().getPosition(supervisorGroup.getName(), supervisorGroup.getName());
        
		// neuer Supervisor für agents der agentsSupervisorToMerge Liste
        for (BdiAgentV2 agent : allAgents) {
        	if (agentsSupervisorToMerge.contains(agent.getName())) {
        		agent.setSupervisor(supervisorGroup);
        		Navi.<INaviAgentV2>get().registerSupervisor(agent.getName(), supervisorGroup.getName());      		
                Point oldPosAgent = agent.belief.getPosition();
                Point newPosAgent = new Point(oldPosAgent.x + relativOldSupervisor.x, oldPosAgent.y + relativOldSupervisor.y);
                agent.belief.setPosition(newPosAgent);
        		updateMap(agent);
        	}
        }
	}
	
	   /**
     * 
     *
     * @param supervisor - the supervisor of the group 
     * @return  List<CalcResult>
     * 
     */
    public synchronized List<CalcResult> calcGroup(Supervisor supervisor) {
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() Start - Supervisor: " +  supervisor.getName()); 
        INaviAgentV2 navi = Navi.<INaviAgentV2>get();
        GameMap map = navi.getMap(supervisor.getName());
        List<String> agents = supervisor.getAgents();
        List<Percept> percepts = new ArrayList<>();
        List<CalcResult> calcResults = new ArrayList<>();
        FloatBuffer mapBuffer = map.getMapBuffer();

        int maxNumberGoals = 32;
        List<InterestingPoint> interestingPoints = map.getInterestingPoints(maxNumberGoals);
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - interestingPoints.size(): " +  interestingPoints.size()); 
        
        if (interestingPoints.size() > 0) {
            PathFindingResult[] agentResultData = new PathFindingResult[interestingPoints.size()];
            Point mapTopLeft = map.getTopLeft();
            AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - agents.size(): " +  agents.size());  
            
            for (int i = 0; i < agents.size(); i++) {
                Point agentPos = map.getInternalAgentPosition(agents.get(i));
                AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - Loop Agent: " +  agents.get(i) + " , Position: " + agentPos); 
                
                for (int j = 0; j < interestingPoints.size(); j++) {
                    Point targetPos =  interestingPoints.get(j).point();
                    int distance = Math.abs(targetPos.x - agentPos.x) + Math.abs(targetPos.y - agentPos.y);
                    String direction = DirectionUtil.getDirection(agentPos, targetPos);
                    agentResultData[j] = new PathFindingResult(distance, direction);
  
                AgentLogger.info(Thread.currentThread().getName() + " InterestingPoint: " + interestingPoints.get(j));                   
                AgentLogger.info(Thread.currentThread().getName() + " PathFindingResult: " + agentResultData[j].distance());
                AgentLogger.info(Thread.currentThread().getName() + " PathFindingResult: " + agentResultData[j].direction());
                AgentLogger.info(Thread.currentThread().getName() + " -------------------------");
                }

                percepts.add(pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft));
                calcResults.add(new CalcResult(agents.get(i) ,pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft)));
            }
        }
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() End - Supervisor: " +  supervisor.getName());        
        return calcResults;
    }
    
   
    
    private Percept pathFindingResultToPercept(String agent, PathFindingResult[] agentResultData, List<InterestingPoint> interestingPoints, Point mapTopLeft) {
        AgentLogger.info(Thread.currentThread().getName() + " pathFindingResultToPercept() Start - Agent: " + agent);
        List<Parameter> data = new ArrayList<>();
        
        // Generate Percept
        for (int j = 0; j < interestingPoints.size(); j++) {
            PathFindingResult resultData = agentResultData[j];
            
            // Result was found
            if (resultData.distance() > 0) {
                InterestingPoint ip = interestingPoints.get(j);
                Parameter distance = new Numeral(resultData.distance());
                Parameter direction = new Numeral(DirectionUtil.stringToInt(resultData.direction()));
                boolean iZ = ip.cellType().equals(CellType.UNKNOWN);
                Parameter isZone = new TruthValue(iZ);
                String det = iZ ? ip.zoneType().name() : ip.cellType().name();
                Parameter detail = new Identifier(det);
                Parameter pointX = new Numeral(ip.point().x + mapTopLeft.x);
                Parameter pointY = new Numeral(ip.point().y + mapTopLeft.y);
                // Generate Data for Point
                Parameter f = new Function("pointResult", detail, isZone, pointX, pointY, distance, direction);
                data.add(f);
            }
        }                

        AgentLogger.info(Thread.currentThread().getName() + " pathFindingResultToPercept() End - Result: " + data);
        return new Percept("PATHFINDER_RESULT", data);
    }
    
    /**
     * 
     *
     * 
     * 
     * 
     */
    public BdiAgentV2 getAgent(String inAgent) {
        BdiAgentV2 result = null;

        for (BdiAgentV2 agent : allAgents) {
            if (agent.getName() == inAgent) {
                result = agent;
                break;
            }
        }

        return result;
    }
}

class AgentMeeting {
    BdiAgentV2 agent;
    Point position;

    AgentMeeting(BdiAgentV2 agent, Point position) {
        this.agent = agent;
        this.position = position;
    }
}

class DirectionUtil {   
    static String intToString(int direction) {
        String s = String.valueOf(direction)
            .replaceAll("1", "n")
            .replaceAll("2", "e")
            .replaceAll("3", "s")
            .replaceAll("4", "w");
        
        return new StringBuilder(s)
            .reverse()
            .toString();
    }
    
    static int stringToInt(String direction) {
        String s = String.valueOf(direction)
            .replaceAll("n", "1")
            .replaceAll("e", "2")
            .replaceAll("s", "3")
            .replaceAll("w", "4");
        
        return Integer.parseInt(s);
    }
   
    static Point getCellInDirection(String direction) {
        int x = 0;
        int y = 0;;

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

record PathFindingResult(int distance, String direction) { }
record CalcResult(String agent, Percept percepts) { }
