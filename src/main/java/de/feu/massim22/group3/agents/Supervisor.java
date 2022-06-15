package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.SupervisorEventName;
import eis.iilang.Function;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;

public class Supervisor implements ISupervisor {
    
    private String name;
    private Supervisable parent;
    private List<String> agents = new ArrayList<>();
    private List<ConfirmationData> confirmationData = new ArrayList<>();
    private Set<TaskInfo> tasks = new HashSet<>();
    private Map<String, AgentReport> reports = new HashMap<>();
    
    public Supervisor(Supervisable parent) {
        this.parent = parent;
        this.name = parent.getName();
        agents.add(name);
        initConfirmationData();
    }
    
    public void handleMessage(Percept message, String sender) {
        // This Supervisor is retired - forward to new supervisor
        if (!isActive()) {
            this.parent.forwardMessageFromSupervisor(message, name, sender);
        } else {
            Percept data = unpackMessage(message);
            String taskKey = data.getName();
            SupervisorEventName taskName = SupervisorEventName.valueOf(taskKey);
            switch (taskName) {
            case REPORT:
                AgentReport report = AgentReport.fromPercept(data);
                reports.put(sender, report);
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
            this.parent.forwardMessageFromSupervisor(message, name, agent);
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
    public void initStep() {
        if (isActive()) {
            this.confirmationData.forEach(d -> d.clear());
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
            reports.put(agent, report);
        } else {
            Function f = report.createMessage();
            Percept message = packMessage(f);
            parent.forwardMessageFromSupervisor(message, name, agent);
        }
    }

    @Override
    public void reportTasks(Set<TaskInfo> tasks) {
        this.tasks = tasks;
    }
}
