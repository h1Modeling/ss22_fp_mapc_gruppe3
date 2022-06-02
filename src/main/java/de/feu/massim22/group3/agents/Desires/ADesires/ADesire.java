package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.*;

public abstract class ADesire implements IDesire{
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

 @Override   
    public Action getOutputAction() {
     return this.outputAction;
    }
 @Override     
    public void setOutputAction(Action outputAction) {
     this.outputAction = outputAction;
 }
 @Override    
    public int getPriority() {
     return this.priority;
 }
 @Override
    public void setPriority(int priority) {
     this.priority = priority;
 }
 @Override
 public String getName() {
     return this.name;
 }
 @Override
 public BdiAgent getAgent() {
     return this.agent;
 }
 @Override   
    public boolean getGroupOrder() {
     return this.groupOrder;
 }
    
    record ReachablePoint(Point position, int distance, int direction) {};
}

