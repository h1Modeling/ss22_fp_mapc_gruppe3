package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.agents.IIntention;
import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;
import eis.iilang.*;

public abstract class ADesire implements IIntention {
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

    public ActionInfo getNextActionInfo() {
        return new ActionInfo(getNextAction(), "");
    }
    
    record ReachablePoint(Point position, int distance, int direction) {}

    public String getName() {
        return name;
    }
}

