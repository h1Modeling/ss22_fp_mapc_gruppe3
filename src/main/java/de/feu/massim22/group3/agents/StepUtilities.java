package de.feu.massim22.group3.agents;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.AgentMeetings.Meeting;
import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.map.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;
import massim.protocol.data.*;

//import java.awt.*;
import java.nio.FloatBuffer;

public class StepUtilities {
    DesireUtilities desireProcessing;
    INaviAgentV2 navi;
    public static ArrayList<BdiAgentV2> allAgents = new ArrayList<BdiAgentV2>();
    public static ArrayList<Supervisor> allSupervisors = new ArrayList<Supervisor>();
    private static int countAgent = 0;
    public List< DispenserFlag> dFlags = new ArrayList<DispenserFlag>();
    boolean mergeGroups = false;
    
    public StepUtilities(DesireUtilities desireProcessing) {
        this.desireProcessing = desireProcessing;
        //AgentLogger.info(Thread.currentThread().getName() + " StepUtilities() Constructor ");
        this.navi = Navi.<INaviAgentV2>get();
    }

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
        ArrayList<String> allSupervisorNames = new ArrayList<String>();
        
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

        if (allSupervisors.size() > 1) {
            // Noch gibt es mehr als einen Supervisor
            for (BdiAgentV2 agent : allAgents) {
                AgentLogger.info(
                        Thread.currentThread().getName() + " doGroupProcessing() Start - Agent: " + agent.getName() + " , Position: " + agent.belief.getPosition());
                things = agent.belief.getThings();
                
                for (Thing thing : things) {
                    // Agent hat in seiner Vision einen anderen Agent
                    if (thing.type.equals(Thing.TYPE_ENTITY)) {
                        // dieser ist aus dem gleichen Team
                        if (thing.details.equals(agent.belief.getTeam())) {
                            // und nicht er selbst
                            if (thing.x != 0 && thing.y != 0) {
                                AgentLogger
                                        .info(Thread.currentThread().getName() + " doGroupProcessing() Found - Agent: "
                                                + agent.getName() + " , FoundPos: " + new Point(thing.x, thing.y));
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
                    // bei einem Treffen m??ssen sich beide gesehen haben und die relativen
                    // Koordinaten d??rfen sich nur im Vorzeichen unterscheiden
                    if ((foundAgent.get(k).position.x == -foundAgent.get(j).position.x)
                            && (foundAgent.get(k).position.y == -foundAgent.get(j).position.y)) {
                        agent1 = foundAgent.get(j).agent;
                        agent2 = foundAgent.get(k).agent;

                        AgentLogger.info(
                                Thread.currentThread().getName() + " doGroupProcessing() meeting in vision - Agent1: "
                                        + agent1.getName() + " , Agent2: " + agent2.getName());

                        // Agents sind eindeutig zu identifizieren ?
                        if (countMeetings(foundAgent, foundAgent.get(j).position) == 1) {
                            recordAgentMeeting( agent1, agent2, foundAgent.get(j).position);
                            recordAgentMeeting( agent2, agent1, foundAgent.get(k).position);
                        //}
                            // Agents sind aus unterschiedlichen Gruppen ?
                            if (mergeGroups && !(agent1.supervisor == agent2.supervisor)) {
                                // dann die kleinere in die gr????ere Gruppe mergen
                                if (agent1.supervisor.getAgents().size() >= agent2.supervisor.getAgents().size()) {
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 2 in 1 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());
                                    // Gruppe von agent2 in Gruppe von agent1 mergen
                                    exSupervisors.add(agent2.supervisor);
                                    Point posOld = Point.castToPoint(agent2.belief.getPosition());
                                    mergeGroups(agent1.supervisor, agent2.supervisor, agent1, agent2, foundAgent.get(j).position);
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent2: "
                                            + agent2.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent2.belief.getPosition());
                                } else {
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 1 in 2 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());
                                    // Gruppe von agent1 in Gruppe von agent2 mergen
                                    exSupervisors.add(agent1.supervisor);
                                    Point posOld = Point.castToPoint(agent1.belief.getPosition());
                                    mergeGroups(agent2.supervisor, agent1.supervisor, agent2, agent1, foundAgent.get(k).position);
                                    AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent1: "
                                            + agent1.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent1.belief.getPosition());
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

            Runnable runnable = () -> { // Gruppenmap berechnen             
                List<CalcResult> agentCalcResults = calcGroup(supervisor);                
                //List<CalcResult> agentCalcResults = Navi.<INaviAgentV2>get().updateSupervisor(supervisor.getName());

                for (CalcResult agentCalcResult : agentCalcResults) {                   
                    BdiAgentV2 agent = getAgent(agentCalcResult.agent());
                    List<Parameter> parameters = agentCalcResult.percepts().getParameters(); 
                    AgentLogger.info(Thread.currentThread().getName() + " vor updateFromPathFinding: " + agent.getName());
                    agent.belief.updateFromPathFinding(parameters);
                    agent.beliefsDone = true;
                    AgentLogger.info(Thread.currentThread().getName() + " nach updateFromPathFinding: " + agent.getName());
                    //AgentLogger.info(Thread.currentThread().getName() + agent.belief.reachablesToString());
                }
                
                desireProcessing.runSupervisorDecisions(step, supervisor, this);
            };
            
            Thread t3 = new Thread(runnable);
            t3.start();
        }

        AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Step: " + step);
    }
    
    private void recordAgentMeeting( BdiAgentV2 agent1, BdiAgentV2 agent2, Point realtivePositionAgent2) {
        AgentMeetings.add(new AgentMeetings.Meeting(agent1, Point.zero(), Point.castToPoint(agent1.belief.getPosition()), agent2, realtivePositionAgent2,  Point.castToPoint(agent2.belief.getPosition())));
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
     * Update the Map.
     * 
     * @param agent - the Agent that wants to update the map
     *
     */
    public void updateMap(BdiAgentV2 agent) {
        navi.updateMap(agent.supervisor.getName(), agent.getName(), agent.index,
                agent.belief.getPosition(), agent.belief.getVision(), agent.belief.getThings(),
                agent.belief.getGoalZones(), agent.belief.getRoleZones(), agent.belief.getStep(),
                agent.belief.getTeam(), agent.belief.getSteps(),  (int)agent.belief.getScore(), agent.belief.getNormsInfo(),
                agent.belief.getTaskInfo(), agent.belief.getAttachedPoints());
    }

    /**
     * The method merges two groups together
     *
     * @param supervisorGroup   - the supervisor of the group that the other group
     *                          is going to be merged into
     * @param supervisorToMerge - the supervisor of the group that is going to be
     *                          merged into the other group
     * 
     */
    public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge, BdiAgentV2 baseAgent, BdiAgentV2 agentFound, Point foundPosition) {
        AgentLogger.info(
                Thread.currentThread().getName() + " mergeGroups() Start - Supervisor: " + supervisorGroup.getName()
                        + " , OldSupervisor: " + supervisorToMerge.getName() + " , " + foundPosition);
 
        List<String> agentsSupervisorGroup = supervisorGroup.getAgents();        
        List<String> agentsSupervisorToMerge = supervisorToMerge.getAgents();
        
        // Agents von agentsSupervisorToMerge in die Liste der Agents von
        // agentsSupervisorGroup
        agentsSupervisorGroup.addAll(agentsSupervisorToMerge);      
        supervisorGroup.setAgents(agentsSupervisorGroup); 

        Point newPosAgentFound = new Point(baseAgent.belief.getPosition().x + foundPosition.x,
                baseAgent.belief.getPosition().y + foundPosition.y);     
        Point newPosAgent = null;

        // neuer Supervisor f??r agents der agentsSupervisorToMerge Liste
        for (BdiAgentV2 agent : allAgents) {
            if (agentsSupervisorToMerge.contains(agent.getName())) {
                agent.supervisor = supervisorGroup;
                navi.registerSupervisor(agent.getName(), supervisorGroup.getName());
                
                if (agent.getName().equals(agentFound.getName())) {
                    newPosAgent = newPosAgentFound;
                } else {
                    newPosAgent = new Point(newPosAgentFound.x + (agent.belief.getPosition().x - agentFound.belief.getPosition().x),
                            newPosAgentFound.y + (agent.belief.getPosition().y - agentFound.belief.getPosition().y));
                }
                
                agent.belief.setPosition(newPosAgent);
                updateMap(agent);
            }
        }
    }

    /**
     * 
     *
     * @param supervisor - the supervisor of the group
     * @return List<CalcResult>
     * 
     */
    public synchronized List<CalcResult> calcGroup(Supervisor supervisor) {
        AgentLogger.info(Thread.currentThread().getName() + " calcGroup() Start - Supervisor: " + supervisor.getName() + " Agents: " + supervisor.getAgents());
        List<String> agents = supervisor.getAgents();
        List<Percept> percepts = new ArrayList<>();
        List<CalcResult> calcResults = new ArrayList<>();
        FloatBuffer mapBuffer = navi.getMapBuffer(supervisor.getName());

        int maxNumberGoals = 32;
        List<InterestingPoint> interestingPoints = navi.getInterestingPoints(supervisor.getName(), maxNumberGoals);

        if (interestingPoints.size() > 0) {
            PathFindingResult[] agentResultData = new PathFindingResult[interestingPoints.size()];
           Point mapTopLeft = Point.castToPoint(navi.getTopLeft(supervisor.getName()));

            for (int i = 0; i < agents.size(); i++) {
                Point agentPos = Point.castToPoint(navi.getInternalAgentPosition(supervisor.getName(), agents.get(i)));

                for (int j = 0; j < interestingPoints.size(); j++) {
                    Point targetPos = Point.castToPoint(interestingPoints.get(j).point());
                    int distance = Math.abs(targetPos.x - agentPos.x) + Math.abs(targetPos.y - agentPos.y);
                    String direction = DirectionUtil.getDirection(agentPos, targetPos);
                    agentResultData[j] = new PathFindingResult(distance, direction);
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
            }
        }

        return new Percept("PATHFINDER_RESULT", data);
    }

    /**
     * 
     *
     * 
     * 
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
    
    public record DispenserFlag(Point position, Boolean attachMade) {}
}

class AgentMeeting {
    BdiAgentV2 agent;
    Point position;

    AgentMeeting(BdiAgentV2 agent, Point position) {
        this.agent = agent;
        this.position = position;
    }
}

record PathFindingResult(int distance, String direction) {}

//record CalcResult(String agent, Percept percepts) {}
