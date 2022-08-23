package de.feu.massim22.group3.agents.V2utils;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.V2utils.AgentMeetings.Meeting;
import de.feu.massim22.group3.map.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;
import massim.protocol.data.*;

//import java.awt.*;
import java.nio.FloatBuffer;

/**
 * The class <code>StepUtilities</code> contains all the methods that are necessary for the correct sequence of a single step .
 * 
 * @author Melinda Betz
 */
public class StepUtilities {
    DesireUtilities desireProcessing;
    INaviAgentV2 navi;
    public static ArrayList<BdiAgentV2> allAgents = new ArrayList<BdiAgentV2>();
    public static ArrayList<Supervisor> allSupervisors = new ArrayList<Supervisor>();
    private static int countAgent = 0;
    private static int countAgent2 = 0;
    public static boolean DecisionsDone;
    boolean mergeGroups = true;
    boolean alwaysAgentMeetings = true;
    
    public StepUtilities(DesireUtilities desireProcessing) {
        this.desireProcessing = desireProcessing;
        //AgentLogger.info(Thread.currentThread().getName() + " StepUtilities() Constructor ");
        this.navi = Navi.<INaviAgentV2>get();
    }

    /**
     * The agent has initiated his map update.
     * 
     * @param agent the agent which has updated the map
     * @param step the step in which the program is at the moment
     * @param teamSize the size of the team of which the agent is part of
     * 
     * @return the agent is done updating the map
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
     * The agent has initiated the decisions done.
     * 
     * @param agent  the agent which has done the decisions
     * @param step the step in which the program is at the moment
     * @param teamSize the size of the team of which the agent is part of
     */
    public static synchronized void reportDecisionsDone(BdiAgentV2 agent, int step, int teamSize) {
        countAgent2++;
        
        if (countAgent2 == teamSize) {
            countAgent2 = 0;
            DecisionsDone = true; 
        } 
    }
    
    /**
     * All the things that have to be done to merge two groups together, update the
     * maps for the resulting groups and do some group/supervisor decisions.
     *
     * @param step the step in which the program is at the moment
     */
    public void doGroupProcessing(int step) {
        BdiAgentV2 agent1;
        BdiAgentV2 agent2;

        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() in neuem Thread - Step: " + step);
        ArrayList<AgentMeeting> foundAgent = new ArrayList<AgentMeeting>();
        Set<Supervisor> exSupervisors = new HashSet<Supervisor>();
        Set<Thing> things = new HashSet<>();
        ArrayList<String> allSupervisorNames = new ArrayList<String>();
        
        DecisionsDone = false;
        
        Collections.sort(allAgents, new Comparator<BdiAgentV2>() {
            @Override
            public int compare(BdiAgentV2 a, BdiAgentV2 b)
            {
                return  a.getName().compareTo(b.getName());
            }
        });
        
        for (Supervisor s : allSupervisors) {
            allSupervisorNames.add(s.getName());
        }
        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() allSupervisors: " + allSupervisorNames);

        if (allSupervisors.size() > 1 || alwaysAgentMeetings) {
            //more than one supervisor at the moment
            for (BdiAgentV2 agent : allAgents) {
                AgentLogger.info(
                        Thread.currentThread().getName() + " doGroupProcessing() Start - Agent: " + agent.getName() + " , Position: " + agent.getBelief().getPosition());
                things = agent.getBelief().getThings();
                
                for (Thing thing : things) {
                    // agent has another agent in his vision
                    if (thing.type.equals(Thing.TYPE_ENTITY)) {
                        // that is from the same team
                        if (thing.details.equals(agent.getBelief().getTeam())) {
                            //that is not the agent personally
                            if (thing.x != 0 && thing.y != 0) {
                                AgentLogger
                                        .info(Thread.currentThread().getName() + " doGroupProcessing() Found - Agent: "
                                                + agent.getName() + " , FoundPos: " + new Point(thing.x, thing.y));
                                // found a candidate for merging
                                foundAgent.add(new AgentMeeting(agent, new Point(thing.x, thing.y)));
                            }
                        }
                    }
                }
            }

            // search for agents that have found each other
            for (int j = 0; j < foundAgent.size(); j++) {
                for (int k = j + 1; k < foundAgent.size(); k++) {
                    // to call it a meeting both agents have to have seen each other and 
                    // the coordinates can only differ by the signs
                    if ((foundAgent.get(k).position.x == -foundAgent.get(j).position.x)
                            && (foundAgent.get(k).position.y == -foundAgent.get(j).position.y)) {
                        agent1 = foundAgent.get(j).agent;
                        agent2 = foundAgent.get(k).agent;

                        AgentLogger.info(
                                Thread.currentThread().getName() + " doGroupProcessing() meeting in vision - Agent1: "
                                        + agent1.getName() + " , Agent2: " + agent2.getName());

                        // Can the agents be clearly identified? 
                        if (countMeetings(foundAgent, foundAgent.get(j).position) == 1) {
                            recordAgentMeeting( agent1, agent2, foundAgent.get(j).position);
                            recordAgentMeeting( agent2, agent1, foundAgent.get(k).position);
                        //}
                            // Are the agents both from different groups ?
                            if (mergeGroups && !(agent1.supervisor == agent2.supervisor)) {
                                // if true then, merge the smaller group into the bigger group
                                if (agent1.supervisor.getAgents().size() >= agent2.supervisor.getAgents().size()) {
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 2 in 1 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());
                                    // merge group from agent2 into group from agent1 
                                    exSupervisors.add(agent2.supervisor);
                                    Point posOld = Point.castToPoint(agent2.getBelief().getPosition());
                                    mergeGroups(agent1.supervisor, agent2.supervisor, agent1, agent2, foundAgent.get(j).position);
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent2: "
                                            + agent2.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent2.getBelief().getPosition());
                                } else {
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 1 in 2 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());
                                    // merge group from agent1 into group from agent2
                                    exSupervisors.add(agent1.supervisor);
                                    Point posOld = Point.castToPoint(agent1.getBelief().getPosition());
                                    mergeGroups(agent2.supervisor, agent1.supervisor, agent2, agent1, foundAgent.get(k).position);
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent1: "
                                            + agent1.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent1.getBelief().getPosition());
                                }
                            } else
                                AgentLogger.info(Thread.currentThread().getName()
                                        + " doGroupProcessing() no merge, already in same group - Supervisor1: "
                                        + agent1.supervisor.getName() + " , Supervisor2: "
                                        + agent2.supervisor.getName());
                        } else
                            AgentLogger.info(Thread.currentThread().getName()
                                    + " doGroupProcessing() no merge, more than one possibility - Supervisor1: "
                                    + agent1.supervisor.getName() + " , Supervisor2: " + agent2.supervisor.getName());
                    }
                }
            }

            if (mergeGroups) allSupervisors.removeAll(exSupervisors);
            Collections.sort(allSupervisors, new Comparator<Supervisor>() {
                @Override
                public int compare(Supervisor a, Supervisor b)
                {
                    return  a.getName().compareTo(b.getName());
                }
            });
        }
        
        /*
         * loop for all agents about roles to adopt
         */
        //desireProcessing.manageAgentRoles();
        
        /*
         * loop for all groups (after merge) with map update and group gecisions
         */
        for (Supervisor supervisor : allSupervisors) {
            AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Loop - Supervisor: "
                    + supervisor.getName());

            Runnable runnable = () -> { //calculate group map           
                List<CalcResult> agentCalcResults = calcGroup(supervisor);                
                //List<CalcResult> agentCalcResults = Navi.<INaviAgentV2>get().updateSupervisor(supervisor.getName());

                for (CalcResult agentCalcResult : agentCalcResults) {                   
                    BdiAgentV2 agent = getAgent(agentCalcResult.agent());
                    List<Parameter> parameters = agentCalcResult.percepts().getParameters(); 
                    AgentLogger.info(Thread.currentThread().getName() + " vor updateFromPathFinding: " + agent.getName());
                    agent.getBelief().updateFromPathFinding(parameters);
                    agent.beliefsDone = true;
                    AgentLogger.info(Thread.currentThread().getName() + " nach updateFromPathFinding: " + agent.getName());
                    //AgentLogger.info(Thread.currentThread().getName() + agent.getBelief().reachablesToString());
                    
                    // update goalzones for supervisor
                    Point nearestGoalZone = null;
                    Point nearestGoalZoneRelativ = Point.castToPoint(agent.getBelief().getNearestRelativeManhattanGoalZone());
                    
                    if (nearestGoalZoneRelativ != null) {
                        nearestGoalZone = Point.castToPoint(agent.getBelief().getPosition()).add(nearestGoalZoneRelativ);
                        
                        if (desireProcessing.posDefaultGoalZone1 == null) {
                            desireProcessing.posDefaultGoalZone1 = new Point(nearestGoalZone);
                        } else {
                            if (Point.distance(desireProcessing.posDefaultGoalZone1, nearestGoalZone) > 10) {
                                desireProcessing.posDefaultGoalZone2 = new Point(nearestGoalZone);
                            }
                        }
                        
                        if (desireProcessing.posDefaultGoalZone2 == null) {
                            desireProcessing.posDefaultGoalZone2 = new Point(nearestGoalZone);
                        }
                    } else {
                        if (Point.distance(Point.castToPoint(agent.getBelief().getPosition()), desireProcessing.posDefaultGoalZone1) <= 5) {
                            desireProcessing.posDefaultGoalZone1 = null;
                        }
                        
                        if (Point.distance(Point.castToPoint(agent.getBelief().getPosition()), desireProcessing.posDefaultGoalZone2) <= 5) {
                            desireProcessing.posDefaultGoalZone2 = null;
                        }
                    }
                }
                
                desireProcessing.runSupervisorDecisions(step, supervisor, this);
            };
            
            Thread t3 = new Thread(runnable);
            t3.start();
            
            AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Supervisor: " + supervisor.getName()
            + " , GZ1: " + desireProcessing.posDefaultGoalZone1 +
            " , GZ2: " + desireProcessing.posDefaultGoalZone2);
        }

        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Step: " + step);
    }
    
    private void recordAgentMeeting( BdiAgentV2 agent1, BdiAgentV2 agent2, Point realtivePositionAgent2) {
        AgentMeetings.add(new AgentMeetings.Meeting(agent1, Point.zero(), Point.castToPoint(agent1.getBelief().getPosition()), 
                Point.castToPoint(agent1.getBelief().getNonModuloPosition()), agent2, realtivePositionAgent2,  
                Point.castToPoint(agent2.getBelief().getPosition()), Point.castToPoint(agent2.getBelief().getNonModuloPosition())));
    }
    
    private int countMeetings(ArrayList<AgentMeeting> foundAgent, Point reverseFound) {
        int counter = 0;
        
        /*AgentLogger.info(
                Thread.currentThread().getName() + " countMeetings() - Position: " + reverseFound);*/

        for (int x = 0; x < foundAgent.size(); x++) {
            if (foundAgent.get(x).position.equals(reverseFound)) {
                /*AgentLogger.info(
                        Thread.currentThread().getName() + " countMeetings() - Agent: "
                                + foundAgent.get(x).agent.getName() + " , Position: " + foundAgent.get(x).position);*/
                counter++;
            }
        }

        return counter;
    }

    /**
     * Update the map.
     * 
     * @param agent the agent that wants to update the map
     *
     */
    public void updateMap(BdiAgentV2 agent) {
        navi.updateMap(agent.supervisor.getName(), agent.getName(), agent.index,
                agent.getBelief().getPosition(), agent.getBelief().getVision(), agent.getBelief().getThings(),
                agent.getBelief().getGoalZones(), agent.getBelief().getRoleZones(), agent.getBelief().getStep(),
                agent.getBelief().getTeam(), agent.getBelief().getSteps(),  (int)agent.getBelief().getScore(), agent.getBelief().getNormsInfo(),
                agent.getBelief().getTaskInfo(), agent.getBelief().getAttachedPoints());
    }

    /**
     * The method merges two groups together
     *
     * @param supervisorGroup the supervisor of the group that the other group
     *                          is going to be merged into
     * @param supervisorToMerge the supervisor of the group that is going to be
     *                          merged into the other group
     * 
     */
    public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge, BdiAgentV2 baseAgent, BdiAgentV2 agentFound, Point foundPosition) {
        AgentLogger.info(
                Thread.currentThread().getName() + " mergeGroups() Start - Supervisor: " + supervisorGroup.getName()
                        + " , OldSupervisor: " + supervisorToMerge.getName() + " , " + foundPosition);
        
        Point newPosAgentFound = new Point(baseAgent.getBelief().getPosition().x + foundPosition.x,
                baseAgent.getBelief().getPosition().y + foundPosition.y);     
        Point newPosAgent = null;
        
        GameMap newMap = navi.getMaps().get(supervisorGroup.getName());
        GameMap oldMap = navi.getMaps().get(supervisorToMerge.getName());

        Point refPoint = newPosAgentFound;
        Point foreignRefPoint = Point.castToPoint(agentFound.getBelief().getPosition());
        
        // Merge Map
        Point offset = Point.castToPoint(newMap.mergeIntoMap(oldMap, foreignRefPoint, refPoint));
        navi.getMaps().put(supervisorGroup.getName(), newMap);
         
        List<String> agentsSupervisorGroup = supervisorGroup.getAgents();        
        List<String> agentsSupervisorToMerge = supervisorToMerge.getAgents();
        
        // add agents from agentsSupervisorToMerge to the list of agents from agentsSupervisorGroup
        agentsSupervisorGroup.addAll(agentsSupervisorToMerge);      
        supervisorGroup.setAgents(agentsSupervisorGroup); 

        // new supervisor for the agents of the list agentsSupervisorToMerge 
        for (BdiAgentV2 agent : allAgents) {
            if (agentsSupervisorToMerge.contains(agent.getName())) {
                agent.supervisor = supervisorGroup;
                navi.registerSupervisor(agent.getName(), supervisorGroup.getName());
                
                if (agent.getName().equals(agentFound.getName())) {
                    newPosAgent = newPosAgentFound;
                } else {
                    newPosAgent = new Point(newPosAgentFound.x + (agent.getBelief().getPosition().x - agentFound.getBelief().getPosition().x),
                            newPosAgentFound.y + (agent.getBelief().getPosition().y - agentFound.getBelief().getPosition().y));
                }

                newPosAgent.x = (((newPosAgent.x % Point.mapSize.x) + Point.mapSize.x) % Point.mapSize.x);
                newPosAgent.y = (((newPosAgent.y % Point.mapSize.y) + Point.mapSize.y) % Point.mapSize.y);
                agent.getBelief().setPosition(newPosAgent);
                updateMap(agent);
            }
        }
    }

    /**
     * Calculates the interesting points on the map for a certain supervisor.
     *
     * @param supervisor the supervisor of the group
     * 
     * @return the result of the calculation in a list
     */
    public synchronized List<CalcResult> calcGroup(Supervisor supervisor) {
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() Start - Supervisor: " + supervisor.getName() + " Agents: " + supervisor.getAgents());
        List<String> agents = supervisor.getAgents();
        List<Percept> percepts = new ArrayList<>();
        List<CalcResult> calcResults = new ArrayList<>();
        FloatBuffer mapBuffer = navi.getMapBuffer(supervisor.getName());

        int maxNumberGoals = 64;
        List<InterestingPoint> interestingPoints = navi.getInterestingPoints(supervisor.getName(), maxNumberGoals);

        if (interestingPoints.size() > 0) {
            PathFindingResult[] agentResultData = new PathFindingResult[interestingPoints.size()];
           Point mapTopLeft = Point.castToPoint(navi.getTopLeft(supervisor.getName()));
           AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - mapTopLeft: " + mapTopLeft);

            for (int i = 0; i < agents.size(); i++) {
                //Point agentPos = Point.castToPoint(navi.getInternalAgentPosition(supervisor.getName(), agents.get(i)));
                //absolute position equals internal position
                Point agentPos = Point.castToPoint(getAgent(agents.get(i)).getBelief().getPosition());
                AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - agent: " + agents.get(i) 
                        + " , beliefPos: " + agentPos
                        + " , internalPos: " + navi.getInternalAgentPosition(supervisor.getName(), agents.get(i))
                        + " , mapPos: " + navi.getPosition(agents.get(i), supervisor.getName())
                        + " , absPos: " + getAgent(agents.get(i)).getBelief().getAbsolutePosition());

                for (int j = 0; j < interestingPoints.size(); j++) {
                    Point targetPos = Point.castToPoint(interestingPoints.get(j).point());
                    //int distance = Math.abs(targetPos.x - agentPos.x) + Math.abs(targetPos.y - agentPos.y);
                    int distance = Point.distance(targetPos, agentPos);
                    String direction = DirectionUtil.getDirection(agentPos, targetPos);
                    agentResultData[j] = new PathFindingResult(distance, direction);
                    
                    //if (interestingPoints.get(j).zoneType().equals(ZoneType.GOALZONE))
                       // AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - interestingPoint: " + interestingPoints.get(j).point() + " , data: " + interestingPoints.get(j).data() + " , " + interestingPoints.get(j).cellType().name() + " , " + distance + " , " + direction);
                }

                percepts.add(pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft));
                calcResults.add(new CalcResult(agents.get(i),
                        pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft)));
            }
        }
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() End - Supervisor: " + supervisor.getName());
        return calcResults;
    }

    private Percept pathFindingResultToPercept(String agent, PathFindingResult[] agentResultData,
            List<InterestingPoint> interestingPoints,  Point mapTopLeft) {
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
                Parameter ipData = new Identifier(ip.data());
                // Generate Data for Point
                Parameter f = new Function("pointResult", detail, isZone, pointX, pointY, distance, direction, ipData);
                data.add(f);
                //if (ipData.toString().equals("x"))
                   // AgentLogger.info(Thread.currentThread().getName() + " - " + agent + " , pathFindingResultToPercept: " + f);
            }
        }

        return new Percept("PATHFINDER_RESULT", data);
    }

   // public record DispenserFlag(Point position, Boolean attachMade) {}

    /**
     * Gets all the agents with a certain name.
     * 
     * @param inAgent the name of the agent that we want to get
     * 
     * @result all the agents with the param name
     *
     */
public static BdiAgentV2 getAgent(String inAgent) {
    BdiAgentV2 result = null;

    for (BdiAgentV2 agent : allAgents) {
        if (agent.getName() == inAgent) {
            result = agent;
            break;
        }
    }

    return result;
}

// array to write all attached blocks into
public static String[] attachedBlock = new String[11];

/**
 * Gets all the attached blocks.
 * 
 * @result all the attached blocks
 *
 */
public static String getAttachedBlocks() {
    String result = "";
    
    for (int i = 1; i <= 10; i++) {
        result = result + "[" + attachedBlock[i] + "] , ";
    }
    
    return result;
}

/**
 * Gets the amount of attached blocks from one block type.
 * 
 * @param type the block type that we want to know the amount off attached blocks of
 *
 *@return number off attached blocks
 */
public static int getNumberAttachedBlocks(String type) {
    int result = 0;
    
    for (int i = 1; i <= 10; i++) {
        if (attachedBlock[i] != null && attachedBlock[i].equals(type)) {
            result++;
        }
    }
    
    return result;
}
}

/**
 * The class <code>AgentMeeting</code> defines the structure of an AgentMeeting.
 * @author Melinda Betz
 */
class AgentMeeting {
    BdiAgentV2 agent;
    Point position;
    
    /**
     * Initializes a new Instance of AgentMeeting.
     * 
     * @param agent the name of the agent
     * @param position the mail service of the agent
     */
    AgentMeeting(BdiAgentV2 agent, Point position) {
        this.agent = agent;
        this.position = position;
    }
}

//record for pathfinding result with distance and direction
record PathFindingResult(int distance, String direction) {}

//record CalcResult(String agent, Percept percepts) {}
