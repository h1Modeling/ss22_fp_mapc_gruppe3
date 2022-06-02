package de.feu.massim22.group3.agents.Desires.SubDesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.ADesires.IDesire;
import eis.iilang.Action;

public abstract class SubDesire {   
//public abstract class SubDesire implements IDesire {     // Melinda

    protected SubDesires subDesireType;
    protected BdiAgent agent;
    
     SubDesire(BdiAgent agent) {
        this.agent = agent;
        setType();
    }

    @Override
    public String toString() {
        return subDesireType.name();
    }

    // Whole logic of the subgoal is implemented in this method (in each subclass)
    public abstract Action getNextAction();

    // Check preconditions, if false next subgoal in hierarchy is checked
    public abstract boolean isExecutable();

    // Check post conditions, if true next subgoal in hierarchy is checked
    public abstract boolean isDone();

	abstract void setType();
}