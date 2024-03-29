package de.feu.massim22.group3.agents.utilsV2;

import java.util.*;

import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.map.*;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;
import massim.protocol.data.*;


/**
 * The class <code>StepUtilities</code> contains all the methods that are necessary for the correct sequence of a single step .
 * 
 * @author Melinda Betz
 */
public class StepUtilities {
    private DesireUtilities desireProcessing;
    private INaviAgentV2 navi;
    private boolean mergeGroups = true;
    private boolean alwaysAgentMeetings = true;
    private static int countAgent = 0;
    private static int countAgent2 = 0;
    
    /** A list of all the active agents from type the BdiAgentV2.*/
    public static ArrayList<BdiAgentV2> allAgents = new ArrayList<BdiAgentV2>();
    
    /** A list of all the active supervisors.*/
    public static ArrayList<Supervisor> allSupervisors = new ArrayList<Supervisor>();
    
    /** If all the decisions are done (task dependent /task independent).*/
    public static boolean DecisionsDone;

    /** A array to write all attached blocks into.*/
    public static String[] attachedBlock = new String[11];
    
    /** The exploring of the horizontal map size has started.*/
    public static boolean exploreHorizontalMapSizeStarted = false;
    
    /** The exploring of the vertical map size has started.*/
    public static boolean exploreVerticalMapSizeStarted = false;
    
    /** The exploring of the horizontal map size is finished.*/
    public static boolean exploreHorizontalMapSizeFinished = false;
    
    /** The exploring of the vertical map size is finished.*/
    public static boolean exploreVerticalMapSizeFinished = false;
    
    /** Default value for exploreHorizontalMapSize.*/
    public static TaskInfo exploreHorizontalMapSize = new TaskInfo("exploreHorizontalMapSize", 1000, 0, new HashSet<Thing>());
    
    /** Default value for exploreVerticalMapSize.*/
    public static TaskInfo exploreVerticalMapSize = new TaskInfo("exploreVerticalMapSize", 1000, 0, new HashSet<Thing>());
  
    /**
     * The constructor.
     * 
     * @param desireProcessing - the DesireUtilities of the active agent
     */
    public StepUtilities(DesireUtilities desireProcessing) {
        this.desireProcessing = desireProcessing;
        //AgentLogger.info(Thread.currentThread().getName() + " StepUtilities() Constructor ");
        this.navi = Navi.<INaviAgentV2>get();
    }

    /**
     * The agent has initiated his map update.
     * 
     * @param agent - the agent which has updated the map
     * @param step - the step in which the program is at the moment
     * @param teamSize - the size of the team of which the agent is part of
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
     * @param agent - the agent which has done the decisions
     * @param step - the step in which the program is at the moment
     * @param teamSize - the size of the team of which the agent is part of
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
     * @param step - the step in which the program is at the moment
     */
    public void doGroupProcessing(int step) {
        BdiAgentV2 agent1;
        BdiAgentV2 agent2;

        //AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() in neuem Thread - Step: " + step);
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
                /*AgentLogger.info(
                        Thread.currentThread().getName() + " doGroupProcessing() Start - Agent: " + agent.getName() 
                        + " , Position: " + agent.getBelief().getPosition());*/
                things = agent.getBelief().getThings();
                
                for (Thing thing : things) {
                    // agent has another agent in his vision
                    if (thing.type.equals(Thing.TYPE_ENTITY)) {
                        // that is from the same team
                        if (thing.details.equals(agent.getBelief().getTeam())) {
                            //that is not the agent personally
                            if (thing.x != 0 && thing.y != 0) {
                                /*AgentLogger
                                        .info(Thread.currentThread().getName() + " doGroupProcessing() Found - Agent: "
                                                + agent.getName() + " , FoundPos: " + new Point(thing.x, thing.y));*/
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

                        /*AgentLogger.info(
                                Thread.currentThread().getName() + " doGroupProcessing() meeting in vision - Agent1: "
                                        + agent1.getName() + " , Agent2: " + agent2.getName());*/

                        // Can the agents be clearly identified? 
                        if (countMeetings(foundAgent, foundAgent.get(j).position) == 1) {                           
                            // Are the agents both from different groups ?
                            if (mergeGroups && !(agent1.supervisor == agent2.supervisor)) {
                                // if true then, merge the smaller group into the bigger group
                                if (agent1.supervisor.getAgents().size() >= agent2.supervisor.getAgents().size()) {
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 2 in 1 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());*/
                                    // merge group from agent2 into group from agent1 
                                    exSupervisors.add(agent2.supervisor);
//                                    Point posOld = Point.castToPoint(agent2.getBelief().getPosition());
                                    mergeGroups(agent1.supervisor, agent2.supervisor, agent1, agent2, foundAgent.get(j).position);
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent2: "
                                            + agent2.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent2.getBelief().getPosition());*/
                                } else {
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() merge 1 in 2 - Supervisor1: "
                                            + agent1.supervisor.getName() + " , Supervisor2: "
                                            + agent2.supervisor.getName());*/
                                    // merge group from agent1 into group from agent2
                                    exSupervisors.add(agent1.supervisor);
//                                    Point posOld = Point.castToPoint(agent1.getBelief().getPosition());
                                    mergeGroups(agent2.supervisor, agent1.supervisor, agent2, agent1, foundAgent.get(k).position);
                                    /*AgentLogger.info(Thread.currentThread().getName()
                                            + " doGroupProcessing() Agent1: "
                                            + agent1.getName() + " , PositionOld: "
                                            + posOld  + " , PositionNew: "
                                            + agent1.getBelief().getPosition());*/
                                }
                            } else
                                /*AgentLogger.info(Thread.currentThread().getName()
                                        + " doGroupProcessing() no merge, already in same group - Supervisor1: "
                                        + agent1.supervisor.getName() + " , Supervisor2: "
                                        + agent2.supervisor.getName());*/
                            
                            // starting explore map size (meeting data will be saved in AgentMeetings by recordAgentMeeting)
                            if ((!exploreHorizontalMapSizeStarted || !exploreVerticalMapSizeStarted)
                                    && !agent1.isBusy && !agent2.isBusy && !agent1.blockAttached && !agent2.blockAttached) {
                                /*AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() explore map possible - Agent1: "
                                        + agent1.getName() + " , Agent2: " + agent2.getName());*/
                                
                                if (!exploreHorizontalMapSizeStarted) {
                                    AgentCooperations.setCooperation(new AgentCooperations.Cooperation(exploreHorizontalMapSize,
                                                    agent1, Status.Explore, agent2, Status.Wait, null, Status.No2));
                                    /*AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() explore map horizontal: "
                                            + AgentCooperations.get(exploreHorizontalMapSize, agent1, 1));*/
                                    exploreHorizontalMapSizeStarted = true;
                                } else {
                                    AgentCooperations.setCooperation(new AgentCooperations.Cooperation(exploreVerticalMapSize,
                                                    agent1, Status.Explore, agent2, Status.Wait, null, Status.No2));
                                    /*AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() explore map horizontal: "
                                            + AgentCooperations.get(exploreVerticalMapSize, agent1, 1));*/
                                    exploreVerticalMapSizeStarted = true;
                                }
                                
                                agent1.isBusy = true;
                                agent2.isBusy = true;
                                agent1.getBelief().setMapSizePosition(Point.zero());
                                agent2.getBelief().setMapSizePosition(Point.zero());
                            }
                            
                            // record meeting data
                            recordAgentMeeting(agent1, agent2, foundAgent.get(j).position);
                            recordAgentMeeting(agent2, agent1, foundAgent.get(k).position);

                            // finishing explore map size (evaluation was done in AgentMeetings)
                            if (exploreHorizontalMapSizeFinished
                                    && AgentCooperations.exists(exploreHorizontalMapSize, agent1)
                                    || exploreVerticalMapSizeFinished
                                            && AgentCooperations.exists(exploreVerticalMapSize, agent1)) {
                                Cooperation coop = null;

                                if (exploreHorizontalMapSizeFinished
                                        && AgentCooperations.exists(exploreHorizontalMapSize, agent1))
                                    coop = AgentCooperations.get(exploreHorizontalMapSize, agent1);
                                else
                                    coop = AgentCooperations.get(exploreVerticalMapSize, agent1);

                                AgentCooperations.remove(coop);
                                agent1.isBusy = false;
                                agent2.isBusy = false;
                                
                                AgentLogger.info(Thread.currentThread().getName()
                                        + " doGroupProcessing() explore map - known map size: "
                                        + AgentCooperations.mapSize.toString());
                         
                                if (exploreHorizontalMapSizeFinished && exploreVerticalMapSizeFinished) {
                                    for (BdiAgentV2 agent : allAgents) {
                                        /*
                                         * AgentLogger.info(Thread.currentThread().getName() +
                                         * " doGroupProcessing() explore map - agent: " + agent.getName() + " , nmp: " +
                                         * agent.getBelief().getNonModPosition() + " , pos: " +
                                         * agent.getBelief().getPosition());
                                         */
                                        agent.getBelief().setPosition(agent.getBelief().calcPositionModulo(
                                                new Point(Point.castToPoint(agent.getBelief().getNonModPosition()))));
                                        calcDispModulo(agent);
                                        calcRgzModulo(agent);
                                        /*
                                         * AgentLogger.info(Thread.currentThread().getName() +
                                         * " doGroupProcessing() explore map - agent: " + agent.getName() + " , nmp: " +
                                         * agent.getBelief().getNonModPosition() + " , pos: " +
                                         * agent.getBelief().getPosition());
                                         */
                                    }
                                }
                            }
                        } else {
                            /*AgentLogger.info(Thread.currentThread().getName()
                                    + " doGroupProcessing() no merge, more than one possibility - Supervisor1: "
                                    + agent1.supervisor.getName() + " , Supervisor2: " + agent2.supervisor.getName());*/
                        }
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
         * loop for all groups (after merge) with map update and group decisions
         */
        for (Supervisor supervisor : allSupervisors) {
            /*AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() Loop - Supervisor: "
                    + supervisor.getName());*/

            Runnable runnable = () -> { //calculate group map           
                List<CalcResult> agentCalcResults = calcGroup(supervisor);                
                //List<CalcResult> agentCalcResults = Navi.<INaviAgentV2>get().updateSupervisor(supervisor.getName());

                for (CalcResult agentCalcResult : agentCalcResults) {                   
                    BdiAgentV2 agent = getAgent(agentCalcResult.agent());
                    List<Parameter> parameters = agentCalcResult.percepts().getParameters(); 
                    //AgentLogger.info(Thread.currentThread().getName() + " vor updateFromPathFinding: " + agent.getName());
                    agent.getBelief().updateFromPathFinding(parameters);
                    agent.beliefsDone = true;
                    //AgentLogger.info(Thread.currentThread().getName() + " nach updateFromPathFinding: " + agent.getName());
                    //AgentLogger.info(Thread.currentThread().getName() + agent.getBelief().reachablesToString());
                    
                    // update goalzones for supervisor  
                    List<java.awt.Point> list = new ArrayList<java.awt.Point>(agent.getBelief().getGoalZones());
                    List<java.awt.Point> listNew = new ArrayList<>();
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , list alt: " + list);  
                    
                    for (java.awt.Point p : list) {  
                        //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , p alt: " + p);
 
                        p = new java.awt.Point(agent.getBelief().getNonModPosition().x + p.x,
                                agent.getBelief().getNonModPosition().y + p.y);
                        listNew.add(p);
                        
                     //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , p neu: " + p);  
                    } 

                    //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , list neu: " + listNew);                 
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + supervisor.getName() 
                    //        + " , rgz alt: " + ((BdiAgentV2) supervisor.getParent()).rgz); 
                    
                    //((BdiAgentV2) agent.supervisor.getParent()).rgz.addAll(listNew);
                    listAddAllPoint(((BdiAgentV2) agent.supervisor.getParent()).rgz, listNew);
                    calcRgzModulo((BdiAgentV2) agent.supervisor.getParent());
                    
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + supervisor.getName() 
                    //        + " , rgz neu: " + ((BdiAgentV2) supervisor.getParent()).rgz);   
                    
                    // update dispenser for supervisor
                    List<Thing> thingList = new ArrayList<>();
                    if (agent.getBelief().getDispenser() != null) 
                        thingList = new ArrayList<Thing>(agent.getBelief().getDispenser());
                    List<Thing> thingListNew = new ArrayList<>();
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , list alt: " + thingList);  
                    
                    for (Thing thing : thingList) { 
                        Point p = new Point(thing.x, thing.y);
                        //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , p alt: " + p);
                        
                        p = new Point(agent.getBelief().getNonModPosition().x + thing.x,
                                agent.getBelief().getNonModPosition().y + thing.y);
                        thingListNew.add(new Thing(p.x, p.y, thing.type, thing.details));
                        
                        //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , p neu: " + p);  
                    } 

                    //AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() + " , list neu: " + thingListNew);                 
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + supervisor.getName() 
                     //       + " , disp alt: " + ((BdiAgentV2) supervisor.getParent()).disp); 
                    
                    //((BdiAgentV2) agent.supervisor.getParent()).disp.addAll(thingListNew);
                    listAddAllThing(((BdiAgentV2) agent.supervisor.getParent()).disp, thingListNew);
                    calcDispModulo((BdiAgentV2) agent.supervisor.getParent());
                    
                    //AgentLogger.info(Thread.currentThread().getName() + " - " + supervisor.getName() 
                     //       + " , disp neu: " + ((BdiAgentV2) supervisor.getParent()).disp);   
                }
                
                ((BdiAgentV2) supervisor.getParent()).rgz.sort((a, b) -> (a.x - b.x == 0 ? a.y - b.y : a.x - b.x));
                
                AgentLogger.info(Thread.currentThread().getName() + " vor updateRgz() - SV: "
                        + ((BdiAgentV2) supervisor.getParent()).getName() + " , " 
                        + ((BdiAgentV2) supervisor.getParent()).getBelief().getPosition() + " , " +  supervisor.getAgents() + " , " 
                        + ((BdiAgentV2) supervisor.getParent()).getBelief().getStep() + " , rgz: " 
                        + ((BdiAgentV2) supervisor.getParent()).rgz);
                AgentLogger.info(Thread.currentThread().getName() + " vor updateDisp() - SV: "
                + ((BdiAgentV2) supervisor.getParent()).getName() + " , " 
                + ((BdiAgentV2) supervisor.getParent()).getBelief().getPosition() + " , " +  supervisor.getAgents() + " , " 
                + ((BdiAgentV2) supervisor.getParent()).getBelief().getStep() + " , disp: " 
                + ((BdiAgentV2) supervisor.getParent()).disp);
                
                for (String strA : supervisor.getAgents()) {
                    BdiAgentV2 a = getAgent(strA);
                    a.getBelief().updateRgz(((BdiAgentV2) supervisor.getParent()).rgz);
                    a.getBelief().updateDisp(((BdiAgentV2) supervisor.getParent()).disp);
                }
    
                /*AgentLogger.info(Thread.currentThread().getName() + " nach updateRgz() - SV: "
                        + ((BdiAgentV2) supervisor.getParent()).getName() + " , " 
                        + ((BdiAgentV2) supervisor.getParent()).getBelief().getPosition() + " , " +  supervisor.getAgents() + " , "
                        + ((BdiAgentV2) supervisor.getParent()).getBelief().getStep() + " , ReachableGoalZones: "  
                        + ((BdiAgentV2) supervisor.getParent()).getBelief().getReachableGoalZones());*/
                

                
                desireProcessing.runSupervisorDecisions(step, supervisor, this);
            };
            
            Thread t3 = new Thread(runnable);
            t3.start();
            
            //AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Supervisor: " + supervisor.getName());
        }

        //AgentLogger.info(Thread.currentThread().getName() + " doGroupProcessing() End - Step: " + step);
    }
    
    /*
     * update dispencer positions with new known mapsize
     */
    private void calcDispModulo(BdiAgentV2 agent) {       
        boolean alreadyExists = false;
        
        for (int i = 0; i < agent.disp.size(); i++) {
            int x = agent.disp.get(i).x < 300 ? agent.disp.get(i).x : ((500 - agent.disp.get(i).x) * -1);
            int y = agent.disp.get(i).y < 300 ? agent.disp.get(i).y : ((500 - agent.disp.get(i).y) * -1);
            agent.disp.get(i).x = (((x % AgentCooperations.mapSize.x) + AgentCooperations.mapSize.x) % AgentCooperations.mapSize.x);
            agent.disp.get(i).y = (((y % AgentCooperations.mapSize.y) + AgentCooperations.mapSize.y) % AgentCooperations.mapSize.y);
            
            for (int j = 0; j < i; j++) {
                if (agent.disp.get(j).x == agent.disp.get(i).x && agent.disp.get(j).y == agent.disp.get(i).y) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (alreadyExists) {
                agent.disp.remove(i);
                alreadyExists = false;
            }
        }
    }
    
    /*
     * update goal zone positions with new known mapsize
     */
    private void calcRgzModulo(BdiAgentV2 agent) {
        boolean alreadyExists = false;
        
        for (int i = agent.rgz.size() - 1; i >= 0; i--) {
            int x = agent.rgz.get(i).x < 300 ? agent.rgz.get(i).x : ((500 - agent.rgz.get(i).x) * -1);
            int y = agent.rgz.get(i).y < 300 ? agent.rgz.get(i).y : ((500 - agent.rgz.get(i).y) * -1);
            agent.rgz.get(i).x = (((x % AgentCooperations.mapSize.x) + AgentCooperations.mapSize.x) % AgentCooperations.mapSize.x);
            agent.rgz.get(i).y = (((y % AgentCooperations.mapSize.y) + AgentCooperations.mapSize.y) % AgentCooperations.mapSize.y);
        
            for (int j = 0; j < i; j++) {
                if (agent.rgz.get(j).x == agent.rgz.get(i).x && agent.rgz.get(j).y == agent.rgz.get(i).y) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (alreadyExists) {
                agent.rgz.remove(i);
                alreadyExists = false;
            }
        }
    }

    /*
     * addAll with a Set did not work (it added duplicates?!)
     */
    private void listAddAllThing(List<Thing> inList, List<Thing> inListNew) {
        boolean alreadyExists = false;
        
        for (int i = 0; i < inListNew.size(); i++) {
            for (int j = 0; j < inList.size(); j++) {
                if (inList.get(j).x == inListNew.get(i).x && inList.get(j).y == inListNew.get(i).y) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists) {
                inList.add(inListNew.get(i));
            } else {
                alreadyExists = false;
            }
        }
    }
   
    /*
     * addAll with a Set did not work (it added duplicates?!)
     */
    private void listAddAllPoint(List<java.awt.Point> inList, List<java.awt.Point> inListNew) {
        boolean alreadyExists = false;
        
        for (int i = 0; i < inListNew.size(); i++) {
            for (int j = 0; j < inList.size(); j++) {
                if (inList.get(j).x == inListNew.get(i).x && inList.get(j).y == inListNew.get(i).y) {
                    alreadyExists = true;
                    break;
                }
            }
            
            if (!alreadyExists) {
                inList.add(inListNew.get(i));
            } else {
                alreadyExists = false;
            }
        }
    }
    
    private void recordAgentMeeting( BdiAgentV2 agent1, BdiAgentV2 agent2, Point realtivePositionAgent2) {
        AgentMeetings.add(new AgentMeetings.Meeting(agent1, Point.zero(), Point.castToPoint(agent1.getBelief().getPosition()), 
                Point.castToPoint(agent1.getBelief().getMapSizePosition()), agent2, realtivePositionAgent2,  
                Point.castToPoint(agent2.getBelief().getPosition()), Point.castToPoint(agent2.getBelief().getMapSizePosition())));
    }
    
    private int countMeetings(ArrayList<AgentMeeting> foundAgent, Point reverseFound) {
        int counter = 0;

        for (int x = 0; x < foundAgent.size(); x++) {
            if (foundAgent.get(x).position.equals(reverseFound)) {
                counter++;
            }
        }

        return counter;
    }

    /**
     * Update the map.
     * 
     * @param agent - the agent that wants to update the map
     *
     */
    public void updateMap(BdiAgentV2 agent) {
        // because of ConcurrentModificationException
        Set<Thing> things = new HashSet<>(agent.getBelief().getThings());
        navi.updateMap(agent.supervisor.getName(), agent.getName(), agent.index,
                agent.getBelief().getPosition(), agent.getBelief().getVision(), things,
                agent.getBelief().getGoalZones(), agent.getBelief().getRoleZones(), agent.getBelief().getStep(),
                agent.getBelief().getTeam(), agent.getBelief().getSteps(),  (int)agent.getBelief().getScore(), agent.getBelief().getNormsInfo(),
                agent.getBelief().getTaskInfo(), agent.getBelief().getAttachedPoints());
    }

    /**
     * The method merges two groups together
     *
     * @param supervisorGroup - the supervisor of the group that the other group
     *                          is going to be merged into
     * @param supervisorToMerge - the supervisor of the group that is going to be
     *                          merged into the other group
     * @param baseAgent - the agent of the group that the other group
     *                          is going to be merged into
     * @param agentFound - the agent of the group that is going to be
     *                          merged into the other group 
     * @param foundPosition - the position of the agent found
     */
    public void mergeGroups(Supervisor supervisorGroup, Supervisor supervisorToMerge, BdiAgentV2 baseAgent, BdiAgentV2 agentFound, Point foundPosition) {
        /*AgentLogger.info(
                Thread.currentThread().getName() + " mergeGroups() Start - Supervisor: " + supervisorGroup.getName()
                        + " , OldSupervisor: " + supervisorToMerge.getName() + " , " + foundPosition);*/
 
        Point newPosAgentFound = new Point(baseAgent.getBelief().getPosition().x + foundPosition.x,
                baseAgent.getBelief().getPosition().y + foundPosition.y);     
        Point newPosAgent = null;
        
        Point newNonModPosAgentFound = new Point(baseAgent.getBelief().getNonModPosition().x + foundPosition.x,
                baseAgent.getBelief().getNonModPosition().y + foundPosition.y);     
        Point newNonModPosAgent = null;
               
        GameMap newMap = navi.getMaps().get(supervisorGroup.getName());
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
                    newNonModPosAgent = newNonModPosAgentFound;
                } else {
                    newPosAgent = new Point(newPosAgentFound.x + (agent.getBelief().getPosition().x - agentFound.getBelief().getPosition().x),
                            newPosAgentFound.y + (agent.getBelief().getPosition().y - agentFound.getBelief().getPosition().y));
                    newNonModPosAgent = new Point(newNonModPosAgentFound.x + (agent.getBelief().getNonModPosition().x - agentFound.getBelief().getNonModPosition().x),
                            newNonModPosAgentFound.y + (agent.getBelief().getNonModPosition().y - agentFound.getBelief().getNonModPosition().y));
                }
                
                if (agent.getName().equals(supervisorToMerge.getName())) {
                    // recalculate rgz for new supervisor
                    // AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                    // " , " + newPosAgentFound + " , " + agentFound.getBelief().getPosition());
                    List<java.awt.Point> listNew = new ArrayList<>();

                    for (java.awt.Point p : agent.rgz) {
                         /*AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                         " , p alt: " + p);*/

                        p = new java.awt.Point(
                                newNonModPosAgentFound.x + (p.x - agentFound.getBelief().getNonModPosition().x),
                                newNonModPosAgentFound.y + (p.y - agentFound.getBelief().getNonModPosition().y));
                        listNew.add(p);

                         /*AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                         " , p neu: " + p);*/
                    }

                     /*AgentLogger.info(Thread.currentThread().getName() + " - " +
                     supervisorGroup.getName()
                     + " , rgz alt: " + ((BdiAgentV2) supervisorGroup.getParent()).rgz);*/

                    // ((BdiAgentV2) supervisorGroup.getParent()).rgz.addAll(listNew);
                    listAddAllPoint(((BdiAgentV2) supervisorGroup.getParent()).rgz, listNew);

                     /*AgentLogger.info(Thread.currentThread().getName() + " - " +
                     supervisorGroup.getName()
                     + " , rgz neu: " + ((BdiAgentV2) supervisorGroup.getParent()).rgz);*/

                    // recalculate disp for new supervisor
                    // AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                    // " , " + newPosAgentFound + " , " + agentFound.getBelief().getPosition());
                    List<Thing> thingListNew = new ArrayList<>();

                    for (Thing thing : agent.disp) {
                        Point p = new Point(thing.x, thing.y);
                        // AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                        // " , p alt: " + p);

                        p = new Point(
                                newNonModPosAgentFound.x + (thing.x - agentFound.getBelief().getNonModPosition().x),
                                newNonModPosAgentFound.y + (thing.y - agentFound.getBelief().getNonModPosition().y));
                        thingListNew.add(new Thing(p.x, p.y, thing.type, thing.details));

                        // AgentLogger.info(Thread.currentThread().getName() + " - " + agent.getName() +
                        // " , p neu: " + p);
                    }

                    // AgentLogger.info(Thread.currentThread().getName() + " - " +
                    // supervisorGroup.getName()
                    // + " , disp alt: " + ((BdiAgentV2) supervisorGroup.getParent()).disp);

                    // ((BdiAgentV2) supervisorGroup.getParent()).disp.addAll(thingListNew);
                    listAddAllThing(((BdiAgentV2) supervisorGroup.getParent()).disp, thingListNew);

                    // AgentLogger.info(Thread.currentThread().getName() + " - " +
                    // supervisorGroup.getName()
                    // + " , disp neu: " + ((BdiAgentV2) supervisorGroup.getParent()).disp);
                }

                newPosAgent.x = (((newPosAgent.x % AgentCooperations.mapSize.x) + AgentCooperations.mapSize.x) % AgentCooperations.mapSize.x);
                newPosAgent.y = (((newPosAgent.y % AgentCooperations.mapSize.y) + AgentCooperations.mapSize.y) % AgentCooperations.mapSize.y);
                agent.getBelief().setPosition(newPosAgent);               
                agent.getBelief().setNonModPosition(newNonModPosAgent);
                
                updateMap(agent);
            }
        }
        
        calcRgzModulo((BdiAgentV2) supervisorGroup.getParent());
        calcDispModulo((BdiAgentV2) supervisorGroup.getParent());
    }

    /**
     * Calculates the interesting points on the map for a certain supervisor.
     *
     * @param supervisor - the supervisor of the group
     * 
     * @return the result of the calculation in a list
     */
    public synchronized List<CalcResult> calcGroup(Supervisor supervisor) {
        //AgentLogger.info(Thread.currentThread().getName() + " calcGroup() Start - Supervisor: " + supervisor.getName() + " Agents: " + supervisor.getAgents());
        List<String> agents = supervisor.getAgents();
        
        List<Percept> percepts = new ArrayList<>();
        List<CalcResult> calcResults = new ArrayList<>();

        int maxNumberGoals = 64;
        List<InterestingPoint> interestingPoints = navi.getInterestingPoints(supervisor.getName(), maxNumberGoals);

        if (interestingPoints.size() > 0) {
            PathFindingResult[] agentResultData = new PathFindingResult[interestingPoints.size()];
           Point mapTopLeft = Point.castToPoint(navi.getTopLeft(supervisor.getName()));
           //AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - mapTopLeft: " + mapTopLeft);

            for (int i = 0; i < agents.size(); i++) {
                Point agentPos = Point.castToPoint(getAgent(agents.get(i)).getBelief().getPosition());
                AgentLogger.info(Thread.currentThread().getName() + " calcGroup() - agent: " + agents.get(i) 
                        + " , beliefPos: " + agentPos
                        + " , beliefNonModPos: " + Point.castToPoint(getAgent(agents.get(i)).getBelief().getNonModPosition())
                        + " , internalPos: " + Point.castToPoint(navi.getInternalAgentPosition(supervisor.getName(), agents.get(i)))
                        + " , mapPos: " + Point.castToPoint(navi.getPosition(agents.get(i), supervisor.getName()))
                        + " , absPos: " + Point.castToPoint(getAgent(agents.get(i)).getBelief().getAbsolutePosition()));

                for (int j = 0; j < interestingPoints.size(); j++) {
                    Point targetPos = Point.castToPoint(interestingPoints.get(j).point());
                    int distance = Point.distance(targetPos, agentPos);
                    String direction = DirectionUtil.getDirection(agentPos, targetPos);
                    agentResultData[j] = new PathFindingResult(distance, direction);
               }

                percepts.add(pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft));
                calcResults.add(new CalcResult(agents.get(i),
                        pathFindingResultToPercept(agents.get(i), agentResultData, interestingPoints, mapTopLeft)));
            }
        }
        //AgentLogger.info(Thread.currentThread().getName() + " calcGroup() End - Supervisor: " + supervisor.getName());
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

    /**
     * Gets all the agents with a certain name.
     * 
     * @param inAgent - the name of the agent that we want to get
     * 
     * @return all the agents with the param name
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

/**
 * Gets all the attached blocks.
 * 
 * @return all the attached blocks
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
 * @param type - the block type that we want to know the amount off attached blocks of
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
    /** The agent.*/
    protected BdiAgentV2 agent;
    
    /** The position of the agent.*/
    protected Point position;
    
    /**
     * Initializes a new Instance of AgentMeeting.
     * 
     * @param agent - the name of the agent
     * @param position - the mail service of the agent
     */
    AgentMeeting(BdiAgentV2 agent, Point position) {
        this.agent = agent;
        this.position = position;
    }
}

/**
 * Record with the Pathfindig result.
 *
 * @param distance - the distance of the path
 * @param direction - the direction of the path
 */
record PathFindingResult(int distance, String direction) {}
