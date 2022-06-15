package de.feu.massim22.group3.agents;

import java.util.Set;

import de.feu.massim22.group3.EventName;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;

public interface ISupervisor {

    void handleMessage(Percept message, String sender);
    void setName(String name);
    String getName();
    void receiveConfirmation(String agent, EventName task);
    void initStep();
    void addAgent(String name);
    void reportAgentData(String agent, AgentReport report);
    void reportTasks(Set<TaskInfo> tasks);
}
