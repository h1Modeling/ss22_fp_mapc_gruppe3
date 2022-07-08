package de.feu.massim22.group3.agents;

import eis.iilang.Percept;

public interface Supervisable {
    void forwardMessage(Percept message, String receiver, String sender);
    String getName();
    void initSupervisorStep();
}
