package de.feu.massim22.group3.agents.supervisor;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.feu.massim22.group3.agents.desires.GroupDesireTypes;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.events.SupervisorEventName;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.PerceptUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/** 
 * The Class <code>Supervisor</code> defines logic to coordinate a group of agents. 
 *
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 * @author Phil Heger (minor contribution)
 */
public class Supervisor implements ISupervisor {
    
    private String name;
    private Supervisable parent;
    private List<String> agents = new ArrayList<>();
    private Set<TaskInfo> tasks = new HashSet<>();
    private Map<String, AgentReport> reports = new HashMap<>();
    private Map<Point, Integer> attachedRequest = new HashMap<>(); 
    private int step;
    private int[] agentReportCount =  new int[1000];
    private Map<String, Boolean> agentsWithTask = new HashMap<>();
    private List<Point> assignedGoalZones = new ArrayList<>();

    // Agents with GuardGoalZoneDesire already assigned
    private List<String> agentsGGZD = new ArrayList<>();
    // Agents for GuardGoalZoneDesire
    private String agentGroupForGGZD = "31";
    // Number of designated GuardGoalZone agents (only 0, 1 or 2 implemented)
    private int numOfGgzdAgents = 1;

    /**
     * Instantiates a new Supervisor.
     * 
     * @param parent the parent agent
     */
    public Supervisor(Supervisable parent) {
        this.parent = parent;
        this.name = parent.getName();
        agents.add(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(Percept message, String sender) {
        // This Supervisor is retired - forward to new supervisor
        if (!isActive()) {
            this.parent.forwardMessage(message, name, sender);
        } else {
            Percept data = unpackMessage(message);
            var parameter = data.getParameters();
            String taskKey = data.getName();
            SupervisorEventName taskName = SupervisorEventName.valueOf(taskKey);
            switch (taskName) {
            case REPORT: {
                AgentReport report = AgentReport.fromPercept(data);
                reportAgentData(sender, report);
                break;
            }
            case ATTACH_REQUEST: {
                String agent = PerceptUtil.toStr(parameter, 0);
                int x = PerceptUtil.toNumber(parameter, 1, Integer.class);
                int y = PerceptUtil.toNumber(parameter, 2, Integer.class);
                String direction = PerceptUtil.toStr(parameter, 3);
                askForAttachPermission(agent, new Point(x, y), direction);
                break;
            }
            default:
                throw new IllegalArgumentException("Supervisor can't handle Message " + taskName);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    private boolean isActive() {
        return this.name.equals(this.parent.getName());
    }

    private Percept unpackMessage(Percept task) {
        List<Parameter> fromParas = task.getParameters();
        if (fromParas.size() > 0) {
            Parameter p = fromParas.get(0);
            if (!(p instanceof Function)) {
                throw new IllegalArgumentException("Supervisor Messages must contain a function in Body");
            }
            Function f = (Function)p;
            String name = f.getName();
            return new Percept(name, f.getParameters());
        }
        return null;
    }

    private Percept packMessage(Function message) {
        return new Percept(EventName.TO_SUPERVISOR.name(), message);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initStep(int step) {
        if (isActive()) {
            this.step = step;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAgent(String name) {
        this.agents.add(name);
    }
    
    // Melinda Betz     
    private boolean decisionsDone;
    
    /**
     * Sets if the decision process is finished.
     * 
     * @param decisionsDone true if the decision process has finished
     */
    public void setDecisionsDone(boolean decisionsDone) {
        this.decisionsDone = decisionsDone;
    }

    /**
     * Gets if the decision process is finished.
     * 
     * @return decisionsDone true if the decision process has finished
     */
    public boolean getDecisionsDone() {
        return this.decisionsDone;
    }

    /**
     * Gets the name of the agents the supervisor is in charge of.
     * 
     * @return a list of agent names
     */
    public List<String> getAgents() {
        return agents;
    }
    
    /**
     * Sets the name of the agents the supervisor is in charge of.
     * 
     * @param agents the list of agents
     */
    public void setAgents(List<String> agents) {
        this.agents = agents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportAgentData(String agent, AgentReport report) {
        if (isActive()) {
            agentReportCount[step] += 1;
            reports.put(agent, report);
            // Start decision making
            if (agentReportCount[step] == agents.size()) {
                doDecision();
            }
        } else {
            Function f = report.createMessage();
            Percept message = packMessage(f);
            parent.forwardMessage(message, name, agent);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reportTasks(Set<TaskInfo> tasks) {
        this.tasks = tasks;
    }

    private void doDecision() {
        List<TaskInfo> sortedTasks = this.tasks.stream().sorted((a, b) -> b.deadline - a.deadline).toList();
        agentsWithTask.clear();
        List<Entry<String, AgentReport>> agentsNearGoalZone = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsNearDispenser0 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsNearDispenser1 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsNearDispenser2 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsNearDispenser3 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsNearDispenser4 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsCarryingBlock0 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsCarryingBlock1 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsCarryingBlock2 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsCarryingBlock3 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsCarryingBlock4 = new ArrayList<>();
        List<Entry<String, AgentReport>> agentsAllowedForGroupTask = new ArrayList<>();

        // Flags if agents are tasked to get a block with type according to the array index.
        boolean[] gettingBlock = {false, false, false, false, false};
        // Stores the nearest goal zone of the agents which get a certain block
        List<List<Point>> gettingBlockNearestGoalZone = new ArrayList<List<Point>>();
        for (int i = 0; i < 5; i++) {
            gettingBlockNearestGoalZone.add(new ArrayList<Point>());
        }

        int maxDistance = 50;
        int maxDistanceGoalZone = 50;
        boolean roleZoneVisible = false;

        // put Reports in Lists
        for (Entry<String, AgentReport> entry : reports.entrySet()) {
            AgentReport r = entry.getValue();

            // Test for role zone
            if (r.distanceRoleZone() < 100) {
                roleZoneVisible = true;
            }

            // Testing get Block group desire
            if (r.groupDesireType().equals(GroupDesireTypes.GET_BLOCK)) {
                for (int i = 0; i < gettingBlock.length; i++) {
                    if (r.groupDesireBlock().equals("b" + i)) {
                        gettingBlock[i] = true;
                        gettingBlockNearestGoalZone.get(i).add(r.nearestGoalZone());
                    }
                }
            }

            if (r.groupDesireType().equals(GroupDesireTypes.NONE) && !r.deactivated()) {
                // Disallow A31 because of guard goal zone
                //if (!entry.getKey().equals(agentGroupForGGZD)) {
                    agentsAllowedForGroupTask.add(entry);
                //}

                if (r.distanceGoalZone() < maxDistanceGoalZone) {
                    agentsNearGoalZone.add(entry);
                }
                // Agent don't carry block
                if (r.attachedThings().size() == 0) {
                    int[] distDispenser = r.distanceDispenser();
                    if (distDispenser[0] < maxDistance) {
                        agentsNearDispenser0.add(entry);
                    }
                    if (distDispenser[1] < maxDistance) {
                        agentsNearDispenser1.add(entry);
                    }
                    if (distDispenser[2] < maxDistance) {
                        agentsNearDispenser2.add(entry);
                    }
                    if (distDispenser[3] < maxDistance) {
                        agentsNearDispenser3.add(entry);
                    }
                    if (distDispenser[4] < maxDistance) {
                        agentsNearDispenser4.add(entry);
                    }
                }
                if (r.attachedThings().size() == 1) {
                    Thing t = r.attachedThings().get(0);
                    
                    if (t.details.equals("b0")) {
                        agentsCarryingBlock0.add(entry);
                    }
                    if (t.details.equals("b1")) {
                        agentsCarryingBlock1.add(entry);
                    }
                    if (t.details.equals("b2")) {
                        agentsCarryingBlock2.add(entry);
                    }
                    if (t.details.equals("b3")) {
                        agentsCarryingBlock3.add(entry);
                    }
                    if (t.details.equals("b4")) {
                        agentsCarryingBlock4.add(entry);
                    }
                }
            }
        }
        // Sort
        agentsNearGoalZone.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());
        agentsNearDispenser0.sort((a, b) -> a.getValue().distanceDispenser()[0] - b.getValue().distanceDispenser()[0]);
        agentsNearDispenser1.sort((a, b) -> a.getValue().distanceDispenser()[1] - b.getValue().distanceDispenser()[1]);
        agentsNearDispenser2.sort((a, b) -> a.getValue().distanceDispenser()[2] - b.getValue().distanceDispenser()[2]);
        agentsNearDispenser3.sort((a, b) -> a.getValue().distanceDispenser()[3] - b.getValue().distanceDispenser()[3]);
        agentsNearDispenser4.sort((a, b) -> a.getValue().distanceDispenser()[4] - b.getValue().distanceDispenser()[4]);
        agentsCarryingBlock0.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());
        agentsCarryingBlock1.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());
        agentsCarryingBlock2.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());
        agentsCarryingBlock3.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());
        agentsCarryingBlock4.sort((a, b) -> a.getValue().distanceGoalZone() - b.getValue().distanceGoalZone());

        // Discover vertical Map Size
        if (roleZoneVisible && agentsAllowedForGroupTask.size() > 1) {
            boolean verticalNotExplored = Navi.<INaviAgentV1>get().setVerticalMapSizeInDiscover(true);
            if (verticalNotExplored) {
                var agent1Report = agentsAllowedForGroupTask.get(0);
                var agent2Report = agentsAllowedForGroupTask.get(1);
                Point a1Pos = agent1Report.getValue().position();
                Point a2Pos = agent2Report.getValue().position();
                String a1Dir = a1Pos.getY() < a2Pos.getY() ? "n" : "s";
                String a2Dir = a1Pos.getY() < a2Pos.getY() ? "s" : "n";
                String agent1 = agent1Report.getValue().agentActionName();
                String agent2 = agent2Report.getValue().agentActionName();
                String agent1Short = agent1Report.getKey();
                String agent2Short = agent2Report.getKey();
                // Send message
                sendExploreMapSizeTask(agent1Short, agent2, agent2Short, a1Dir, (int)Math.floor((a1Pos.x - a2Pos.x) / 2));
                sendExploreMapSizeTask(agent2Short, agent1, agent1Short, a2Dir, (int)Math.ceil((a2Pos.x - a1Pos.x) / 2));
                
                agentsAllowedForGroupTask.remove(agent1Report);
                agentsAllowedForGroupTask.remove(agent2Report);
            }
        }

        // Discover horizontal Map Size
        if (roleZoneVisible && agentsAllowedForGroupTask.size() > 1) {
            boolean horizontalNotExplored = Navi.<INaviAgentV1>get().setHorizontalMapSizeInDiscover(true);
            if (horizontalNotExplored) {
                var agent1Report = agentsAllowedForGroupTask.get(0);
                var agent2Report = agentsAllowedForGroupTask.get(1);
                Point a1Pos = agent1Report.getValue().position();
                Point a2Pos = agent2Report.getValue().position();
                String a1Dir = a1Pos.getX() < a2Pos.getX() ? "w" : "e";
                String a2Dir = a1Pos.getX() < a2Pos.getX() ? "e" : "w";
                String agent1 = agent1Report.getValue().agentActionName();
                String agent2 = agent2Report.getValue().agentActionName();
                String agent1Short = agent1Report.getKey();
                String agent2Short = agent2Report.getKey();
                // Send message
                sendExploreMapSizeTask(agent1Short, agent2, agent2Short, a1Dir, (int)Math.floor((a1Pos.y - a2Pos.y) / 2));
                sendExploreMapSizeTask(agent2Short, agent1, agent1Short, a2Dir, (int)Math.ceil((a2Pos.y - a1Pos.y) / 2));

                agentsAllowedForGroupTask.remove(agent1Report);
                agentsAllowedForGroupTask.remove(agent2Report);
            }
        }

        boolean mapSizeInDiscover = Navi.get().isVerticalMapSizeInDiscover() && Navi.get().isHorizontalMapSizeInDiscover();
        // GuardGoalZoneDesire (GGZD) is only assigned in one supervisor-group to avoid
        // that it is assigned to too many agents at the same time
        if (numOfGgzdAgents >= 1 && getName().equals(agentGroupForGGZD) && mapSizeInDiscover && reports.size() > 3) {
            // Get map size
            Point gameMapSize = Navi.<INaviAgentV1>get().getGameMapSize(getName());
            // Extract goal zones from AgentReport
            List<Point> goalZones = new ArrayList<Point>();
            // Get report of Agent 1
            AgentReport r = null;
            if(reports.containsKey(getName())) {
                r = reports.get(getName());
                if (r.numOfDistinctGoalZones() > 0) {
                    goalZones.add(r.nearestGoalZone());
                }
                if (r.numOfDistinctGoalZones() > 1) {
                    goalZones.add(r.goalZone2());
                }
            }
            AgentLogger.fine(getName() + " Supervisor",
                    "goalZones: " + goalZones.toString());
            AgentLogger.fine(getName() + " Supervisor",
                    "assignedGoalZones: " + assignedGoalZones.toString());
            // If GGZD not already assigned
            if (!agentsGGZD.contains(agentGroupForGGZD)
                    && goalZones.size() > 0
                    && assignedGoalZones.size() == 0
                    && r != null && r.groupDesireType().equals(GroupDesireTypes.NONE)
                    && !agentsWithTask.containsKey(agentGroupForGGZD)) {
                sendGuardGoalZoneTask(agentGroupForGGZD, goalZones.get(0));
            }
            else if (numOfGgzdAgents >= 2
                    && agents.size() > 1
                    && goalZones.size() > 1
                    && assignedGoalZones.size() == 1) {
                // Find new goal zone
                Point target_gz = null;
                for (Point gz : goalZones) {
                    if (!DirectionUtil.pointsWithinDistance(
                            gz, assignedGoalZones.get(0), gameMapSize, 15)) {
                        target_gz = gz;
                        break;
                    }
                }
                // Find agent to assign GGZD
                String agentForGGZD = null;
                for (String agent : agents) {
                    if(!agentsGGZD.contains(agent)) {
                        agentForGGZD = agent;
                        break;
                    }
                }
                var report = reports.get(agentForGGZD);
                boolean isFree = report.groupDesireType().equals(GroupDesireTypes.NONE) && !agentsWithTask.containsKey(agentGroupForGGZD);
                if (target_gz != null && isFree) {
                    sendGuardGoalZoneTask(agentForGGZD, target_gz);
                }
            }
        }

        // Test if single Block tasks exists
        boolean singleBlockTaskExists = false;
        int twoBlockTasksCount = 0;
        for (TaskInfo info: tasks) {
            if (info.requirements.size() == 1) {
                singleBlockTaskExists = true;
            }
            if (info.requirements.size() == 2) {
                twoBlockTasksCount += 1;
            }
        }

        // Test Tasks
        for (TaskInfo info : sortedTasks) {
            // Ignore Single agent groups
            if (agents.size() < 2) {
                break;
            }
            // Deliver Single Block to Agent nearer to Goal zone
            if (info.requirements.size() == 1) {
                String blockDetail = info.requirements.get(0).type;
                if (blockDetail.equals("b0")) {
                    //doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock0, "b0", info);
                }
                if (blockDetail.equals("b1")) {
                    //doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock1, "b1", info);
                }
                if (blockDetail.equals("b2")) {
                    //doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock2, "b2", info);
                }
                if (blockDetail.equals("b3")) {
                    //doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock3, "b3", info);
                }
                if (blockDetail.equals("b4")) {
                    //doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock4, "b4", info);
                }
            }
            // Three Block Task
            if (info.requirements.size() == 3) {
                //                                            ○           ○
                // Only work on 3 block tasks of the form:  A ○    or   A ○ ○    and rotated versions. 
                //                                            ○
                int countDist1 = 0;
                int countDist2 = 0;
                for (Thing t : info.requirements) {
                    int dist = Math.abs(t.x) + Math.abs(t.y);
                    if (dist == 1) countDist1 += 1;
                    if (dist == 2) countDist2 += 1;
                }
                if (countDist1 != 1 || countDist2 != 2) {
                    continue;
                }
                System.out.println("Possible Task available");
                String block1 = info.requirements.get(0).type;
                String block2 = info.requirements.get(1).type;
                String block3 = info.requirements.get(2).type;
                List<Entry<String, AgentReport>> agentsBlock1 = null;
                List<Entry<String, AgentReport>> agentsBlock2 = null;
                List<Entry<String, AgentReport>> agentsBlock3 = null;
                switch (block1) {
                    case "b0": agentsBlock1 = agentsCarryingBlock0; break;
                    case "b1": agentsBlock1 = agentsCarryingBlock1; break;
                    case "b2": agentsBlock1 = agentsCarryingBlock2; break;
                    case "b3": agentsBlock1 = agentsCarryingBlock3; break;
                    case "b4": agentsBlock1 = agentsCarryingBlock4; break;
                }
                switch (block2) {
                    case "b0": agentsBlock2 = agentsCarryingBlock0; break;
                    case "b1": agentsBlock2 = agentsCarryingBlock1; break;
                    case "b2": agentsBlock2 = agentsCarryingBlock2; break;
                    case "b3": agentsBlock2 = agentsCarryingBlock3; break;
                    case "b4": agentsBlock2 = agentsCarryingBlock4; break;
                }
                switch (block3) {
                    case "b0": agentsBlock3 = agentsCarryingBlock0; break;
                    case "b1": agentsBlock3 = agentsCarryingBlock1; break;
                    case "b2": agentsBlock3 = agentsCarryingBlock2; break;
                    case "b3": agentsBlock3 = agentsCarryingBlock3; break;
                    case "b4": agentsBlock3 = agentsCarryingBlock4; break;
                }

                boolean sameBlock13 = block1.equals(block3);
                boolean sameBlock12 = block1.equals(block2);
                boolean sameBlock23 = block2.equals(block3);
                if ((agentsBlock1.size() > 0 && agentsBlock2.size() > 0 && agentsBlock3.size() > 0 && !sameBlock12 && !sameBlock13 && !sameBlock23)
                    || (sameBlock13 && agentsBlock1.size() > 1 && agentsBlock2.size() > 1)
                    || (sameBlock23 && agentsBlock1.size() > 0 && agentsBlock2.size() > 1) 
                    || (sameBlock12 && agentsBlock1.size() > 1 && agentsBlock3.size() > 0)
                    || (sameBlock12 && sameBlock13 && agentsBlock1.size() > 2)) {
                    doThreeBlockTaskDecision(agentsBlock1, agentsBlock2, agentsBlock3, info);
                }
                else {
                    List<String> missingBlocks = new ArrayList<>();
                    if (!sameBlock12 && !sameBlock13 && !sameBlock23) {
                        if (agentsBlock1.size() == 0) missingBlocks.add(block1);
                        if (agentsBlock2.size() == 0) missingBlocks.add(block2);
                        if (agentsBlock3.size() == 0) missingBlocks.add(block3);
                    }
                    else if (sameBlock12 && sameBlock13) {
                        if (agentsBlock1.size() < 3) missingBlocks.add(block1);
                        if (agentsBlock1.size() < 2) missingBlocks.add(block1);
                        if (agentsBlock1.size() == 0) missingBlocks.add(block1);
                    }
                    else if (sameBlock13) {
                        if (agentsBlock1.size() < 2) missingBlocks.add(block1);
                        if (agentsBlock1.size() == 0) missingBlocks.add(block1);
                        if (agentsBlock2.size() == 0) missingBlocks.add(block2);
                    }
                    else if (sameBlock23) {
                        if (agentsBlock2.size() < 2) missingBlocks.add(block2);
                        if (agentsBlock2.size() == 0) missingBlocks.add(block2);
                        if (agentsBlock1.size() == 0) missingBlocks.add(block1);
                    }
                    else if (sameBlock12) {
                        if (agentsBlock1.size() < 2) missingBlocks.add(block1);
                        if (agentsBlock1.size() == 0) missingBlocks.add(block1);
                        if (agentsBlock3.size() == 0) missingBlocks.add(block3);
                    }

                    // Randomly work on task if two block tasks exist
                    boolean getBlocks = true; // !singleBlockTaskExists && twoBlockTasksCount == 0 ? true : Math.random() < 0.5 - twoBlockTasksCount * 0.1;
                    
                    if (getBlocks) {
                        for (String block : missingBlocks) {
                            if (block.equals("b0")) sendGetBlockTask(agentsNearDispenser0, "b0");
                            if (block.equals("b1")) sendGetBlockTask(agentsNearDispenser1, "b1");
                            if (block.equals("b2")) sendGetBlockTask(agentsNearDispenser2, "b2");
                            if (block.equals("b3")) sendGetBlockTask(agentsNearDispenser3, "b3");
                            if (block.equals("b4")) sendGetBlockTask(agentsNearDispenser4, "b4");
                        }
                    }
                }
            }
            // Two Block Task - assign task or assign get block if not carrying already
            if (info.requirements.size() == 2) {
                String block1 = info.requirements.get(0).type;
                String block2 = info.requirements.get(1).type;
                List<Entry<String, AgentReport>> agentsBlock1 = null;
                List<Entry<String, AgentReport>> agentsBlock2 = null;
                switch (block1) {
                    case "b0": agentsBlock1 = agentsCarryingBlock0; break;
                    case "b1": agentsBlock1 = agentsCarryingBlock1; break;
                    case "b2": agentsBlock1 = agentsCarryingBlock2; break;
                    case "b3": agentsBlock1 = agentsCarryingBlock3; break;
                    case "b4": agentsBlock1 = agentsCarryingBlock4; break;
                }
                switch (block2) {
                    case "b0": agentsBlock2 = agentsCarryingBlock0; break;
                    case "b1": agentsBlock2 = agentsCarryingBlock1; break;
                    case "b2": agentsBlock2 = agentsCarryingBlock2; break;
                    case "b3": agentsBlock2 = agentsCarryingBlock3; break;
                    case "b4": agentsBlock2 = agentsCarryingBlock4; break;
                }

                boolean sameBlock = block1.equals(block2);
                if ((sameBlock && agentsBlock1.size() > 1) || (!sameBlock && agentsBlock1.size() > 0 && agentsBlock2.size() > 0)) {
                    doTwoBlockTaskDecision(agentsBlock1, agentsBlock2, info, sameBlock, singleBlockTaskExists);
                } else {
                    // If less two block tasks are available more agents should collect the block of the task
                    boolean forceGet = Math.random() < 0.4 - twoBlockTasksCount * 0.1;
                    // Collect Blocks from Dispenser
                    if ((!sameBlock && agentsBlock1.size() == 0) || agentsBlock1.size() < 2) {
                        if (block1.equals("b0") && ((!gettingBlock[0] && agentsCarryingBlock0.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser0, "b0");
                        if (block1.equals("b1") && ((!gettingBlock[1] && agentsCarryingBlock1.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser1, "b1");
                        if (block1.equals("b2") && ((!gettingBlock[2] && agentsCarryingBlock2.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser2, "b2");
                        if (block1.equals("b3") && ((!gettingBlock[3] && agentsCarryingBlock3.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser3, "b3");
                        if (block1.equals("b4") && ((!gettingBlock[4] && agentsCarryingBlock4.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser4, "b4");
                    }
                    // Collect Blocks from Dispenser
                    if (!sameBlock && agentsBlock2.size() == 0) {
                        if (block2.equals("b0") && ((!gettingBlock[0] && agentsCarryingBlock0.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser0, "b0");
                        if (block2.equals("b1") && ((!gettingBlock[1] && agentsCarryingBlock1.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser1, "b1");
                        if (block2.equals("b2") && ((!gettingBlock[2] && agentsCarryingBlock2.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser2, "b2");
                        if (block2.equals("b3") && ((!gettingBlock[3] && agentsCarryingBlock3.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser3, "b3");
                        if (block2.equals("b4") && ((!gettingBlock[4] && agentsCarryingBlock4.size() < 3) || (!singleBlockTaskExists && Math.random() < 0.3) || forceGet)) sendGetBlockTask(agentsNearDispenser4, "b4");
                    }
                }
            }
        }
    }

    private void sendExploreMapSizeTask(String agent, String teamMate, String teamMateShort, String direction, int guideOffset) {
        Parameter teamMatePara = new Identifier(teamMate);
        Parameter teamMateShortPara = new Identifier(teamMateShort);
        Parameter directionPara = new Identifier(direction);
        Parameter guideOffsetPara = new Numeral(guideOffset);
        Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_EXPLORE_MAP_SIZE.name(),
            teamMatePara, teamMateShortPara, directionPara, guideOffsetPara);
        parent.forwardMessage(message, agent, name);
        agentsWithTask.put(agent, true);
    }

    private void sendGuardGoalZoneTask(String agent, Point gz) {
        // GGZD...GuardGoalZoneDesire
        assignedGoalZones.add(gz);
        agentsGGZD.add(agent);
        AgentLogger.fine(getName() + " Supervisor",
                "sendGuardGoalZoneTask(): " + agent + "  " + gz.toString());
        Parameter pointX_gz = new Numeral(gz.x);
        Parameter pointY_gz = new Numeral(gz.y);
        Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GUARD_GOAL_ZONE.name(),
                pointX_gz, pointY_gz);
        parent.forwardMessage(message, agent, name);
        agentsWithTask.put(agent, true);
    }

    /*
    private void sendGuardDispenserTask(List<Entry<String, AgentReport>> agents, String block) {
        // Only first agent which is free will get the task
        for (var entry : agents) {
            String agent = entry.getKey();
            // Has no task yet
            if (agentsWithTask.get(agent) == null) {
                Parameter blockPara = new Identifier(block);
                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GUARD_DISPENSER.name(), blockPara);
                parent.forwardMessage(message, agent, name);
                agentsWithTask.put(agent, true);
                break;
            }
        }
    }
    */

    private void sendGetBlockTask(List<Entry<String, AgentReport>> agents, String block) {
        // only first agent which is free will get the task
        for (var entry : agents) {
            String agent = entry.getKey();
            AgentReport r = entry.getValue();
            // Has no task yet
            if (agentsWithTask.get(agent) == null && r.attachedThings().size() == 0) {
                Parameter blockPara = new Identifier(block);
                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GET_BLOCK.name(), blockPara);
                parent.forwardMessage(message, agent, name);
                agentsWithTask.put(agent, true);
                break;
            }
        }
    }

    private String[][] getAgentsFor3BlockTask(int maxWaitingTime, List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2, List<Entry<String, AgentReport>> agents3) {
        String[][] taskAgents = new String[3][2];
        int minDiff = 999;
        for (var agent1 : agents1) {
            if (agentsWithTask.containsKey(agent1.getKey())) continue;
            for (var agent2 : agents2) {
                if (agentsWithTask.containsKey(agent2.getKey()) || agent2.getKey().equals(agent1.getKey())) continue;
                for (var agent3 : agents3) {
                    if (agentsWithTask.containsKey(agent3.getKey()) || agent3.getKey().equals(agent1.getKey()) || agent3.getKey().equals(agent2.getKey())) continue;

                    var report1 = agent1.getValue();
                    var report2 = agent2.getValue();
                    var report3 = agent3.getValue();
                    var waitingTime1 = Math.abs(report1.distanceGoalZone() - report2.distanceGoalZone());
                    var waitingTime2 = Math.abs(report1.distanceGoalZone() - report3.distanceGoalZone());
                    var distanceBetweenGoalZones1 = Math.abs(report1.nearestGoalZone().x - report2.nearestGoalZone().x) + Math.abs(report1.nearestGoalZone().y - report2.nearestGoalZone().y);
                    var distanceBetweenGoalZones2 = Math.abs(report1.nearestGoalZone().x - report3.nearestGoalZone().x) + Math.abs(report1.nearestGoalZone().y - report3.nearestGoalZone().y);
                    var distBetweenAgents1 = Math.abs(report1.position().x - report2.position().x) + Math.abs(report1.position().y - report2.position().y);
                    var distBetweenAgents2 = Math.abs(report1.position().x - report3.position().x) + Math.abs(report1.position().y - report3.position().y);

                    // Agents near Different Goal Zones or if both are in goal zone use distance to get closest match
                    if (distanceBetweenGoalZones1 > 15 || report1.distanceGoalZone() == 0 || report2.distanceGoalZone() == 0 ||  
                        report1.distanceGoalZone() < 5 && report2.distanceGoalZone() < 5) {
                        waitingTime1 = 1000;
                    }
                    if (distanceBetweenGoalZones2 > 15 || report1.distanceGoalZone() == 0 || report3.distanceGoalZone() == 0 ||  
                        report1.distanceGoalZone() < 5 && report3.distanceGoalZone() < 5) {
                        waitingTime2 = 1000;
                    }
                    // Use agents which are similar close to the goal zone or near each other
                    waitingTime1 = Math.min(waitingTime1, distBetweenAgents1);
                    waitingTime2 = Math.min(waitingTime2, distBetweenAgents2);
                    int waitingTimeCombined = Math.max(waitingTime1, waitingTime2);

                    if (waitingTimeCombined < maxWaitingTime && waitingTimeCombined < minDiff) {
                        minDiff = waitingTimeCombined;
                        String[] data1 = {agent1.getKey(), report1.agentActionName()};
                        taskAgents[0] = data1;
                        String[] data2 = {agent2.getKey(), report2.agentActionName()};
                        taskAgents[1] = data2;
                        String[] data3 = {agent3.getKey(), report3.agentActionName()};
                        taskAgents[2] = data3;
                    }
                }
            }
        }
        return taskAgents;
    }

    private String[][] getAgentsFor2BlockTask(int maxWaitingTime, List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2) {
        String[][] taskAgents = new String[2][2];
        int minDiff = 999;
        for (var agent1 : agents1) {
            if (agentsWithTask.containsKey(agent1.getKey())) continue;
            for (var agent2 : agents2) {
                if (agentsWithTask.containsKey(agent2.getKey()) || agent2.getKey().equals(agent1.getKey())) continue;
                var report1 = agent1.getValue();
                var report2 = agent2.getValue();
                var waitingTime = Math.abs(report1.distanceGoalZone() - report2.distanceGoalZone());
                var distanceBetweenGoalZones = Math.abs(report1.nearestGoalZone().x - report2.nearestGoalZone().x) + Math.abs(report1.nearestGoalZone().y - report2.nearestGoalZone().y);
                var distBetweenAgents = Math.abs(report1.position().x - report2.position().x) + Math.abs(report1.position().y - report2.position().y);
                // Agents near Different Goal Zones or if both are in goal zone use distance to get closest match
                if (distanceBetweenGoalZones > 15 || report1.distanceGoalZone() == 0 || report2.distanceGoalZone() == 0 ||  
                        report1.distanceGoalZone() < 5 && report2.distanceGoalZone() < 5) {
                    waitingTime = 1000;
                }
                // Use agents which are similar close to the goal zone or near each other
                waitingTime = Math.min(waitingTime, distBetweenAgents);
                if (waitingTime < maxWaitingTime && waitingTime < minDiff) {
                    minDiff = waitingTime;
                    String[] data1 = {agent1.getKey(), report1.agentActionName()};
                    taskAgents[0] = data1;
                    String[] data2 = {agent2.getKey(), report2.agentActionName()};
                    taskAgents[1] = data2;
                }
            }
        }
        return taskAgents;
    }

    private void doThreeBlockTaskDecision(List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2, List<Entry<String, AgentReport>> agents3, TaskInfo info) {

        int maxWaitingTime = 30;
        String[][] taskAgents = getAgentsFor3BlockTask(maxWaitingTime, agents1, agents2, agents3);

        if (taskAgents[0][0] != null) {
            String[] agent1 = taskAgents[0];
            String[] agent2 = taskAgents[1];
            String[] agent3 = taskAgents[2];
            agentsWithTask.put(agent1[0], true);
            agentsWithTask.put(agent2[0], true);
            agentsWithTask.put(agent3[0], true);
            agents1.removeIf(r -> r.getKey().equals(agent1[0]));
            agents2.removeIf(r -> r.getKey().equals(agent1[0]));
            agents3.removeIf(r -> r.getKey().equals(agent1[0]));
            agents1.removeIf(r -> r.getKey().equals(agent2[0]));
            agents2.removeIf(r -> r.getKey().equals(agent2[0]));
            agents3.removeIf(r -> r.getKey().equals(agent2[0]));

            // Send message to agents
            Parameter taskPara = new Identifier(info.name);
            Parameter agent1Para = new Identifier(agent1[0]);
            Parameter agent2Para = new Identifier(agent2[0]);
            Parameter agent3Para = new Identifier(agent3[0]);
            Parameter agent1FullPara = new Identifier(agent1[1]);
            Parameter agent2FullPara = new Identifier(agent2[1]);
            Parameter agent3FullPara = new Identifier(agent3[1]);

            Thing req0 = info.requirements.get(0);
            Thing req1 = info.requirements.get(1);
            Parameter index1 = new Numeral(0);
            Parameter index2 = new Numeral(1);
            Parameter index3 = new Numeral(2);

            // Agent 1 is Receiver
            if (Math.abs(req0.x) + Math.abs(req0.y) == 1) {
                Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_THREE_BLOCK.name(), taskPara, agent2Para, agent2FullPara, index2, agent3Para, agent3FullPara, index3, index1);
                Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent1Para, agent1FullPara, index2);
                Percept message3 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent1Para, agent1FullPara, index3);
                parent.forwardMessage(message1, agent1[0], name);
                parent.forwardMessage(message2, agent2[0], name);
                parent.forwardMessage(message3, agent3[0], name);
            }
            // Agent 2 is Receiver
            else if (Math.abs(req1.x) + Math.abs(req1.y) == 1) {
                Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent2Para, agent2FullPara, index1);
                Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_THREE_BLOCK.name(), taskPara, agent1Para, agent1FullPara, index1, agent3Para, agent3FullPara, index3, index2);
                Percept message3 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent2Para, agent2FullPara, index3);
                parent.forwardMessage(message1, agent1[0], name);
                parent.forwardMessage(message2, agent2[0], name);
                parent.forwardMessage(message3, agent3[0], name);
            }
            // Agent 3 is Receiver
            else {
                Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent3Para, agent3FullPara, index1);
                Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_THREE_BLOCK.name(), taskPara, agent3Para, agent3FullPara, index2);
                Percept message3 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_THREE_BLOCK.name(), taskPara, agent1Para, agent1FullPara, index1, agent2Para, agent2FullPara, index2, index3);
                parent.forwardMessage(message1, agent1[0], name);
                parent.forwardMessage(message2, agent2[0], name);
                parent.forwardMessage(message3, agent3[0], name);
            }
        }
    }

    private void doTwoBlockTaskDecision(List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2, TaskInfo info, boolean identicalBlocks, boolean singleBlockTaskExists) {

        int maxWaitingTime = singleBlockTaskExists ? 10 : 30;
        String[][] taskAgents = getAgentsFor2BlockTask(maxWaitingTime, agents1, agents2);

        if (taskAgents[0][0] != null) {
           String[] agent1 = taskAgents[0];
           String[] agent2 = taskAgents[1];
           agentsWithTask.put(agent1[0], true);
           agentsWithTask.put(agent2[0], true);
           agents1.removeIf(r -> r.getKey().equals(agent1[0]));
           agents2.removeIf(r -> r.getKey().equals(agent1[0]));
           agents1.removeIf(r -> r.getKey().equals(agent2[0]));
           agents2.removeIf(r -> r.getKey().equals(agent2[0]));

            // Send message to agents
            Parameter taskPara = new Identifier(info.name);
            Parameter agent1Para = new Identifier(agent1[0]);
            Parameter agent2Para = new Identifier(agent2[0]);
            Parameter agent1FullPara = new Identifier(agent1[1]);
            Parameter agent2FullPara = new Identifier(agent2[1]);

            Thing req0 = info.requirements.get(0);
            if (Math.abs(req0.x) + Math.abs(req0.y) == 1) {
                Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK.name(), taskPara, agent2Para, agent2FullPara);
                Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK.name(), taskPara, agent1Para, agent1FullPara);
                parent.forwardMessage(message1, agent1[0], name);
                parent.forwardMessage(message2, agent2[0], name);
            } else {
                Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK.name(), taskPara, agent1Para, agent1FullPara);
                Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK.name(), taskPara, agent2Para, agent2FullPara);
                parent.forwardMessage(message2, agent1[0], name);
                parent.forwardMessage(message1, agent2[0], name);
            }
        }
    }

    /*
    private void doDeliverSimpleTaskDecision(List<Entry<String, AgentReport>> agentsNearGoalZone, List<Entry<String, AgentReport>> agentsCarryingBlock, String block, TaskInfo info) {
        
        while (agentsNearGoalZone.size() > 0 && agentsCarryingBlock.size() > 0) {
            Entry<String, AgentReport> entryGoalZone = agentsNearGoalZone.get(0);
            Entry<String, AgentReport> entryBlock = agentsCarryingBlock.get(0);

            // Remove from lists        
            agentsNearGoalZone.remove(entryGoalZone);
            agentsCarryingBlock.remove(entryBlock);
            
            // Send message to agents
            Parameter taskPara = new Identifier(info.name);
            String goalAgent = entryGoalZone.getKey();
            Parameter goalAgentPara = new Identifier(goalAgent);
            String dispenserAgent = entryBlock.getKey();
            Parameter dispenserAgentPara = new Identifier(dispenserAgent);

            agentsWithTask.put(goalAgent, true);
            agentsWithTask.put(dispenserAgent, true);
            
            if (!goalAgent.equals(dispenserAgent)) {
                Percept messageGoal = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_BLOCK.name(), taskPara, dispenserAgentPara);
                Percept messageDispenser = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_BLOCK.name(), taskPara, goalAgentPara); 
                parent.forwardMessage(messageGoal, goalAgent, name);
                parent.forwardMessage(messageDispenser, dispenserAgent, name);
            }
        }
    }
    */

    /**
     * {@inheritDoc}
     */
    @Override
    public void askForAttachPermission(String agent, Point p, String direction) {
        if (isActive()) {
            Integer savedStep = attachedRequest.get(p);
            boolean result = savedStep == null || savedStep < step;
            if (result) {
                attachedRequest.put(p, step);
            }
            Percept message = new Percept(EventName.ATTACH_REPLY.name(), new TruthValue(result), new Identifier(direction));
            parent.forwardMessage(message, agent, parent.getName());
        } else {
            Parameter namePara = new Identifier(agent);
            Parameter xPara = new Numeral(p.x);
            Parameter yPara = new Numeral(p.y);
            Parameter dirPara = new Identifier(direction);
            Function f = new Function(SupervisorEventName.ATTACH_REQUEST.name(), namePara, xPara, yPara, dirPara);
            Percept message = packMessage(f);
            parent.forwardMessage(message, name, agent);
        }
    }
}
