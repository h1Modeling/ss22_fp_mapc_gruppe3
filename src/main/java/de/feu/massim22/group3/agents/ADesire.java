package de.feu.massim22.group3.agents;

import java.awt.Point;

import eis.iilang.*;

abstract class ADesire {
    DesireUtilities desireProcessing;
    
    public String name;
    public int priority;
    
    public BdiAgentV2 agent;
    public Action outputAction;

    ADesire(String name, BdiAgentV2 agent, DesireUtilities desireProcessing) {
        this.name = name;
        this.agent = agent;
        this.desireProcessing = desireProcessing;
    }
    
    public abstract boolean isExecutable();
    public abstract Action getNextAction();
    
    record ReachablePoint(Point position, int distance, int direction) {}
}

