package de.feu.massim22.group3.agents;

import eis.iilang.Percept;

public interface Supervisable {
    void forwardMessageFromSupervisor(Percept message, String receiver, String sender);
    String getName();
    void initSupervisorStep();
}
