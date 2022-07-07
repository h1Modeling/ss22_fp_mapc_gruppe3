package de.feu.massim22.group3.agents;

import java.util.Set;

import de.feu.massim22.group3.EventName;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import java.awt.Point;

public interface ISupervisor {

    void handleMessage(Percept message, String sender);
    void setName(String name);
    String getName();
    void receiveConfirmation(String agent, EventName task);
    void initStep(int step);
    void addAgent(String name);
    void reportAgentData(String agent, AgentReport report);
    void reportTasks(Set<TaskInfo> tasks);
    void askForAttachPermission(String agent, Point p, String direction);
}
