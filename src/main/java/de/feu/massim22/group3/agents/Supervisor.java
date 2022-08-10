package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.SupervisorEventName;
import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
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

public class Supervisor implements ISupervisor {
    
    private String name;
    private Supervisable parent;
    private List<String> agents = new ArrayList<>();
    // List that is updated later to see in supervisor if and which agents are new
    // This is needed for GuardGoalZoneDesire assignment
    private List<String> oldAgents = new ArrayList<>();
    private List<ConfirmationData> confirmationData = new ArrayList<>();
    private Set<TaskInfo> tasks = new HashSet<>();
    private Map<String, AgentReport> reports = new HashMap<>();
    private Map<Point, Integer> attachedRequest = new HashMap<>(); 
    private int step;
    private int[] agentReportCount =  new int[1000];
    Map<String, Boolean> agentsWithTask = new HashMap<>();
    
    public Supervisor(Supervisable parent) {
        this.parent = parent;
        this.name = parent.getName();
        agents.add(name);
        initConfirmationData();
    }
    
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

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void receiveConfirmation(String agent, EventName task) {
        // Forward to active Supervisor
        if (!isActive()) {
            Percept message = this.createConfirmationMessage(task);
            this.parent.forwardMessage(message, name, agent);
        }
        switch (task) {
        default:
            throw new IllegalArgumentException("Confirmation " + task.name() + " is not implemented yet");
        }	
    }

    private boolean isActive() {
        return this.name.equals(this.parent.getName());
    }

    private Percept createConfirmationMessage(EventName task) {
        Function data = new Function(task.name());
        return packMessage(data);
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

    private class ConfirmationData {
        private String agent;
        
        ConfirmationData(String agent) {
            this.agent = agent;
        }

        void clear() {
        }
    }

    private void initConfirmationData() {
        for (String a: agents) {
            confirmationData.add(new ConfirmationData(a));
        }
    }

    @Override
    public void initStep(int step) {
        if (isActive()) {
            this.confirmationData.forEach(d -> d.clear());
            this.step = step;
        }
    }

    @Override
    public void addAgent(String name) {
        this.agents.add(name);
    }
    
    // Melinda Betz     
    private boolean decisionsDone;
    
    public void setDecisionsDone(boolean decisionsDone) {
        this.decisionsDone = decisionsDone;
    }

    public boolean getDecisionsDone() {
        return this.decisionsDone;
    }

    public List<String> getAgents() {
        return agents;
    }
    
    public void setAgents(List<String> agents) {
        this.agents = agents;
    }

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

        int maxDistance = 50;
        int maxDistanceGoalZone = 50;
        // put Repots in Lists
        for (Entry<String, AgentReport> entry : reports.entrySet()) {
            AgentReport r = entry.getValue();

            if (r.groupDesireType().equals(GroupDesireTypes.NONE) && !r.deactivated()) {
                // Agent don't carry block
                if (r.attachedThings().size() == 0) {
                    if (r.distanceGoalZone() < maxDistanceGoalZone) {
                        agentsNearGoalZone.add(entry);
                    }
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

//        // TODO Get closest agent to each dispenser that is needed for a current task
//        // Find most needed dispenser for current tasks
//        Map<String, Integer> blocks = new HashMap<>();
//        blocks.put("b0", 0);
//        blocks.put("b1", 0);
//        blocks.put("b2", 0);
//        blocks.put("b3", 0);
//        blocks.put("b4", 0);
//        
//        for (TaskInfo task : tasks) {
//            for (Thing t : task.requirements) {
//                // block types for 1-block-tasks count double
//                if (task.requirements.size() == 1) {
//                    blocks.put(t.type, blocks.get(t.type) + 2);
//                }
//                else {
//                    blocks.put(t.type, blocks.get(t.type) + 1);
//                }
//            }
//        }
//        // Sort Hash Map by turning it into a List of Entries
//        List<Entry<String, Integer>> blockList = new ArrayList<>(blocks.entrySet());
//        blockList.sort(Entry.comparingByValue());
//        Collections.reverse(blockList);
//
//        // If information about tasks exists (in the first step it does not exist yet):
//        if (blockList.get(0).getValue() != 0) {
//            AgentLogger.info("Most valuable block types are (blockList): " + blockList);
//            // Two block types are equally important
//            if (blockList.get(0).getValue() == blockList.get(1).getValue()) {
//                switch (blockList.get(1).getKey()) {
//                case "b0": sendGuardDispenserTask (agentsNearDispenser0, "b0"); break;
//                case "b1": sendGuardDispenserTask (agentsNearDispenser1, "b1"); break;
//                case "b2": sendGuardDispenserTask (agentsNearDispenser2, "b2"); break;
//                case "b3": sendGuardDispenserTask (agentsNearDispenser3, "b3"); break;
//                case "b4": sendGuardDispenserTask (agentsNearDispenser4, "b4"); break;
//                }
//            }
//            // Only one most important block type
//            switch (blockList.get(0).getKey()) {
//            case "b0": sendGuardDispenserTask (agentsNearDispenser0, "b0"); break;
//            case "b1": sendGuardDispenserTask (agentsNearDispenser1, "b1"); break;
//            case "b2": sendGuardDispenserTask (agentsNearDispenser2, "b2"); break;
//            case "b3": sendGuardDispenserTask (agentsNearDispenser3, "b3"); break;
//            case "b4": sendGuardDispenserTask (agentsNearDispenser4, "b4"); break;
//            }
//        }

        // Only assign GuardGoalZone Tasks if more than certain amount of agents are in one group
        // and if GoalZone is discovered
        if (agents.size() > 4 && agentsNearGoalZone.size() != 0) {
            sendGuardGoalZoneTask();
        }

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
                    doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock0, "b0", info);
                }
                if (blockDetail.equals("b1")) {
                    doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock1, "b1", info);
                }
                if (blockDetail.equals("b2")) {
                    doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock2, "b2", info);
                }
                if (blockDetail.equals("b3")) {
                    doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock3, "b3", info);
                }
                if (blockDetail.equals("b4")) {
                    doDeliverSimpleTaskDecision(agentsNearGoalZone, agentsCarryingBlock4, "b4", info);
                }
            }
            // Two Block Task - assign task or assign get block if not carrying already
            if (info.requirements.size() == 2 && !singleBlockTaskExists) {
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
                    doTwoBlockTaskDecision(agentsBlock1, agentsBlock2, info, sameBlock);
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

    private List<String> getAgentsWithGuardGoalZoneDesire(List<String> agents) {
        List<String> agentsWithGuardGoalZoneDesire = new ArrayList<>();
        for (String agent : agents) {
            AgentReport agentReport = reports.get(agent);
            if (agentReport.groupDesireType() == "guard_goal_zone") {
                agentsWithGuardGoalZoneDesire.add(agent);
            }
        }
        return agentsWithGuardGoalZoneDesire;
    }

    private void sendGuardGoalZoneTask() {
        // GGZD...GuardGoalZoneDesire
        int numAgentsGGZD = 1;
        String[] directionArr = {"n", "s"};

        List<String> newGroupAgents = new ArrayList<>(agents);
        newGroupAgents.removeAll(oldAgents);

        List<String> newAgentsWithGGZD = getAgentsWithGuardGoalZoneDesire(newGroupAgents);
        List<String> oldAgentsWithGGZD = getAgentsWithGuardGoalZoneDesire(oldAgents);

        // Delete Group Desire from agents of group that was merged to this supervisor
        if (newAgentsWithGGZD.size() > 0) {
            for (String agent : newAgentsWithGGZD) {
                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DELETE_GROUP_DESIRES.name());
                parent.forwardMessage(message, agent, name);
                agentsWithTask.remove(agent);
            }
        }

        // Only first agents which are free will get the task and only if the GGZD
        // was not assigned to the max. number of agents (numAgentsGGZD) already
        for (int i = oldAgentsWithGGZD.size(); i < numAgentsGGZD; i++) {
            for (String agent : agents) {
                // Has no task yet
                if (agentsWithTask.get(agent) == null) {
                    Parameter dir = new Identifier(directionArr[i % 2]);
                    Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GUARD_GOAL_ZONE.name(), dir);
                    parent.forwardMessage(message, agent, name);
                    agentsWithTask.put(agent, true);
                    break;
                }
            }
        }
        oldAgents = agents;
    }
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

    private void doTwoBlockTaskDecision(List<Entry<String, AgentReport>> agents1, List<Entry<String, AgentReport>> agents2, TaskInfo info, boolean identicalBlocks) {
        List<String[]> taskAgents = new ArrayList<>(); 

        while (taskAgents.size() < 2 && agents1.size() > 0 && agents2.size() > 0) {
            String agent1 = agents1.get(0).getKey();
            if (agentsWithTask.get(agent1) == null) {
                String agentActionName = agents1.get(0).getValue().agentActionName();
                String[] data = {agent1, agentActionName};
                taskAgents.add(data);
            }
            agents1.remove(0);
            String agent2 = agents2.get(0).getKey();
            if (agentsWithTask.get(agent2) == null) {
                String agentActionName = agents2.get(0).getValue().agentActionName();
                String[] data = {agent2, agentActionName};
                taskAgents.add(data);
            }
            agents2.remove(0);
        }

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
