package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.*;

public abstract class ADesire {
    public String name;
    public BdiAgent agent;
    public boolean groupOrder = false;
    
    public int priority = 1000;
    public Action outputAction;

    public ADesire(String name, BdiAgent agent) {
        this.name = name;
        this.agent = agent;
    }
    
    public ADesire() {
    }
    
    public abstract boolean isExecutable();
    public abstract Action getNextAction();
    
    record ReachablePoint(Point position, int distance, int direction) {}
}

