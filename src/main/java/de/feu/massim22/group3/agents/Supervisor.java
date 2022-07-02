package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.SupervisorEventName;
import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class Supervisor implements ISupervisor {
    
    private String name;
    private Supervisable parent;
    private List<String> agents = new ArrayList<>();
    private List<ConfirmationData> confirmationData = new ArrayList<>();
    private Set<TaskInfo> tasks = new HashSet<>();
    private Map<String, AgentReport> reports = new HashMap<>();
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
            String taskKey = data.getName();
            SupervisorEventName taskName = SupervisorEventName.valueOf(taskKey);
            switch (taskName) {
            case REPORT:
                AgentReport report = AgentReport.fromPercept(data);
                reportAgentData(sender, report);
                break;
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
        // put Repots in Lists
        for (Entry<String, AgentReport> entry : reports.entrySet()) {
            AgentReport r = entry.getValue();

            if (r.groupDesireType().equals(GroupDesireTypes.NONE) && !r.deactivated()) {
                // Agent don't carry block
                if (r.attachedThings().size() == 0) {
                    if (r.distanceGoalZone() < maxDistance) {
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
            // Get Block
            if (!singleBlockTaskExists && reports.size() > 1) {
                for (Thing t : info.requirements) {
                    if (t.type.equals("b0")) sendGetBlockTask(agentsNearDispenser0, "b0");
                    if (t.type.equals("b1")) sendGetBlockTask(agentsNearDispenser1, "b1");
                    if (t.type.equals("b2")) sendGetBlockTask(agentsNearDispenser2, "b2");
                    if (t.type.equals("b3")) sendGetBlockTask(agentsNearDispenser3, "b3");
                    if (t.type.equals("b4")) sendGetBlockTask(agentsNearDispenser4, "b4");
                }
            }

            // Deliver Single Block to Agent nearer to Goal zone
            /* 
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
            } */
            // Try two Block Task with same blocks
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
                // Task with same blocks
                if (block1.equals(block2)) {
                    // Do Task
                    if (agentsBlock1.size() > 1) {
                        doTwoIdenticalBlockTaskDecision(agentsBlock1, info);
                    } else {
                        // Collect Blocks from Dispenser
                        List<Entry<String, AgentReport>> agentsDispenser = null;
                        switch (block1) {
                            case "b0": agentsDispenser = agentsNearDispenser0; break;
                            case "b1": agentsDispenser = agentsNearDispenser1; break;
                            case "b2": agentsDispenser = agentsNearDispenser2; break;
                            case "b3": agentsDispenser = agentsNearDispenser3; break;
                            case "b4": agentsDispenser = agentsNearDispenser4; break;
                        }
                        // Search for possible agent
                        for (int i = 0; i < agentsDispenser.size(); i++) {
                            String agent = agentsDispenser.get(0).getKey();
                            if (agentsWithTask.get(agent) == null) {
                                Parameter blockPara = new Identifier(block1);
                                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GET_BLOCK.name(), blockPara);
                                parent.forwardMessage(message, agent, name);
                                agentsWithTask.put(agent, true);
                            }
                        }
                    }
                }
                // Different blocks
                else {
                    /*
                    System.out.println("TASK With different Blocks");
                    if (agentsBlock1.size() > 0 && agentsBlock2.size() > 0) {
                        // TODO
                    } else {

                        // Collect Blocks from Dispenser
                        List<Entry<String, AgentReport>> agentsDispenser1 = null;
                        List<Entry<String, AgentReport>> agentsDispenser2 = null;
                        switch (block1) {
                            case "b0": agentsDispenser1 = agentsNearDispenser0; break;
                            case "b1": agentsDispenser1 = agentsNearDispenser1; break;
                            case "b2": agentsDispenser1 = agentsNearDispenser2; break;
                            case "b3": agentsDispenser1 = agentsNearDispenser3; break;
                            case "b4": agentsDispenser1 = agentsNearDispenser4; break;
                        }
                        switch (block2) {
                            case "b0": agentsDispenser2 = agentsNearDispenser0; break;
                            case "b1": agentsDispenser2 = agentsNearDispenser1; break;
                            case "b2": agentsDispenser2 = agentsNearDispenser2; break;
                            case "b3": agentsDispenser2 = agentsNearDispenser3; break;
                            case "b4": agentsDispenser2 = agentsNearDispenser4; break;
                        }

                        System.out.println("TASKS FOUND " + agentsDispenser1.size() + " " + agentsDispenser2.size());
                        for (int i = 0; i < agentsDispenser1.size(); i++) {
                            String agent = agentsDispenser1.get(0).getKey();
                            if (agentsWithTask.get(agent) == null) {
                                Parameter blockPara = new Identifier(block1);
                                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GET_BLOCK.name(), blockPara);
                                parent.forwardMessage(message, agent, name);
                                agentsWithTask.put(agent, true);
                            }
                        }

                        for (int i = 0; i < agentsDispenser2.size(); i++) {
                            String agent = agentsDispenser2.get(0).getKey();
                            if (agentsWithTask.get(agent) == null) {
                                Parameter blockPara = new Identifier(block2);
                                Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_GET_BLOCK.name(), blockPara);
                                parent.forwardMessage(message, agent, name);
                                agentsWithTask.put(agent, true);
                            }
                        }
                    }
                    */
                }

            }
        }
    }

    private void sendGetBlockTask(List<Entry<String, AgentReport>> agents, String block) {
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

    private void doTwoIdenticalBlockTaskDecision(List<Entry<String, AgentReport>> agents, TaskInfo info) {
        List<String> taskAgents = new ArrayList<>(); 

        while (taskAgents.size() < 2 && agents.size() > 0) {
            String agent = agents.get(0).getKey();
            agents.remove(0);
            if (agentsWithTask.get(agent) == null) {
                taskAgents.add(agent);
            }
        }

        if (taskAgents.size() > 1) {
           String agent1 = taskAgents.get(0);
           String agent2 = taskAgents.get(1);
           agentsWithTask.put(agent1, true);
           agentsWithTask.put(agent2, true);

            // Send message to agents
            Parameter taskPara = new Identifier(info.name);
            Parameter agent1Para = new Identifier(agent1);
            Parameter agent2Para = new Identifier(agent2);

            Percept message1 = new Percept(EventName.SUPERVISOR_PERCEPT_RECEIVE_SAME_TWO_BLOCK.name(), taskPara, agent2Para);
            Percept message2 = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_SAME_TWO_BLOCK.name(), taskPara, agent1Para);
            parent.forwardMessage(message1, agent1, name);
            parent.forwardMessage(message2, agent2, name);
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
}
