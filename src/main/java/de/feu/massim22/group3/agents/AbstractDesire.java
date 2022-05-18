package de.feu.massim22.group3.agents;

import java.awt.Point;

import eis.iilang.*;

abstract class AbstractDesire {
    public String name;
    public int priority;
    public BdiAgent agent;
    public Action outputAction;

    AbstractDesire(String name, BdiAgent agent) {
        this.name = name;
        this.agent = agent;
    }
    
    public abstract boolean isExecutable(Desire desire);
    public abstract Action getNextAction();
    
    record ReachablePoint(Point position, int distance, int direction) {}
}
