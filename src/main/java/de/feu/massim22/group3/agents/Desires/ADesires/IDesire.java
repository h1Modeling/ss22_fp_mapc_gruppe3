package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.Action;

public interface IDesire {
    boolean isExecutable();
    Action getNextAction();
    
    Action getOutputAction();    
    void setOutputAction(Action inAction);
    
    int getPriority();
    void setPriority(int inPriority);
    
    String getName();
    BdiAgent getAgent();
    boolean getGroupOrder();
}
