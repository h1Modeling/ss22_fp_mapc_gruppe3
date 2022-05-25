package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.EventName;
import eis.iilang.Percept;

public interface ISupervisor {

    void handleMessage(Percept message, String sender);
    void setName(String name);
    String getName();
    void receiveConfirmation(String agent, EventName task);
    void initStep();
    void addAgent(String name);
}
