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
import de.feu.massim22.group3.utils.PerceptUtil;
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
    
    /**
     * Initializes a new Supervisor.
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

        int maxDistance = 30;
        int maxDistanceGoalZone = 50;
        // put Repots in Lists
        for (Entry<String, AgentReport> entry : reports.entrySet()) {
            AgentReport r = entry.getValue();

            if (r.groupDesireType().equals(GroupDesireTypes.NONE) && !r.deactivated()) {
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
        
        //List<Point> meetingPoints = Navi.<INaviAgentV1>get().getMeetingPoints(name);

        // Only do tasks if GoalZone is discovered
        if (agentsNearGoalZone.size() == 0) return;

        // Test if single Block tasks exists
        boolean singleBlockTaskExists = false;
        for (TaskInfo info: tasks) {
            if (info.requirements.size() == 1) {
                singleBlockTaskExists = true;
                break;
            }
        }

        // Test Tasks
        for (TaskInfo info : tasks) {
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
                    // Collect Blocks from Dispenser
                    if ((!sameBlock && agentsBlock1.size() == 0) || agentsBlock1.size() < 2) {
                        if (block1.equals("b0")) sendGetBlockTask(agentsNearDispenser0, "b0");
                        if (block1.equals("b1")) sendGetBlockTask(agentsNearDispenser1, "b1");
                        if (block1.equals("b2")) sendGetBlockTask(agentsNearDispenser2, "b2");
                        if (block1.equals("b3")) sendGetBlockTask(agentsNearDispenser3, "b3");
                        if (block1.equals("b4")) sendGetBlockTask(agentsNearDispenser4, "b4");
                    }
                    // Collect Blocks from Dispenser
                    if (!sameBlock && agentsBlock2.size() == 0) {
                        if (block2.equals("b0")) sendGetBlockTask(agentsNearDispenser0, "b0");
                        if (block2.equals("b1")) sendGetBlockTask(agentsNearDispenser1, "b1");
                        if (block2.equals("b2")) sendGetBlockTask(agentsNearDispenser2, "b2");
                        if (block2.equals("b3")) sendGetBlockTask(agentsNearDispenser3, "b3");
                        if (block2.equals("b4")) sendGetBlockTask(agentsNearDispenser4, "b4");
                    }
                }
            }
        }
    }

    private void sendGetBlockTask(List<Entry<String, AgentReport>> agents, String block) {
        // only first agent which is free will get the task
        for (var entry : agents) {
            String agent = entry.getKey();
            // Has no task yet
            if (agentsWithTask.get(agent) == null) {
                Parameter blockPara = new Identifier(block);
                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GET_BLOCK.name(), blockPara);
                parent.forwardMessage(message, agent, name);
                agentsWithTask.put(agent, true);
                break;
            }
        }
    }

    private List<String[]> getAgentsFor2BlockTask(int maxWaitingTime, List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2) {
        List<String[]> taskAgents = new ArrayList<>();
        for (var agent1 : agents1) {
            if (agentsWithTask.containsKey(agent1.getKey())) continue;
            for (var agent2 : agents2) {
                if (agentsWithTask.containsKey(agent2.getKey()) || agent2.getKey().equals(agent1.getKey())) continue;
                var report1 = agent1.getValue();
                var report2 = agent2.getValue();
                var waitingTime = Math.abs(report1.distanceGoalZone() - report2.distanceGoalZone());
                var distanceBetweenGoalZones = Math.abs(report1.nearestGoalZone().x - report2.nearestGoalZone().y) + Math.abs(report1.nearestGoalZone().y - report2.nearestGoalZone().y);
                if (waitingTime < maxWaitingTime && distanceBetweenGoalZones < 25) {
                    String[] data1 = {agent1.getKey(), report1.agentActionName()};
                    taskAgents.add(data1);
                    String[] data2 = {agent2.getKey(), report2.agentActionName()};
                    taskAgents.add(data2);
                    return taskAgents;
                }
            }
        }
        return taskAgents;
    }

    private void doTwoBlockTaskDecision(List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2, TaskInfo info, boolean identicalBlocks, boolean singleBlockTaskExists) {
        
        int maxWaitingTime = singleBlockTaskExists ? 10 : 30;
        List<String[]> taskAgents = getAgentsFor2BlockTask(maxWaitingTime, agents1, agents2);
        if (taskAgents.size() > 1) {
           String[] agent1 = taskAgents.get(0);
           String[] agent2 = taskAgents.get(1);
           agentsWithTask.put(agent1[0], true);
           agentsWithTask.put(agent2[0], true);

            // Send message to agents
            Parameter taskPara = new Identifier(info.name);
            Parameter agent1Para = new Identifier(agent1[0]);
            Parameter agent2Para = new Identifier(agent2[0]);
            Parameter agent1FullPara = new Identifier(agent1[1]);
            Parameter agent2FullPara = new Identifier(agent2[1]);

            Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK.name(), taskPara, agent2Para, agent2FullPara);
            Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK.name(), taskPara, agent1Para, agent1FullPara);
            parent.forwardMessage(message1, agent1[0], name);
            parent.forwardMessage(message2, agent2[0], name);
        }
    }

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
                /* 
                parent.forwardMessage(messageGoal, goalAgent, name);
                parent.forwardMessage(messageDispenser, dispenserAgent, name); */
            }
        }
    }

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
