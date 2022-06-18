package de.feu.massim22.group3.agents.Desires.SubDesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.IIntention;
import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;
import eis.iilang.Action;

public abstract class SubDesire implements IIntention {

    protected SubDesires subDesireType;
    BdiAgent agent;

    SubDesire(BdiAgent agent) {
        this.agent = agent;
        setType();
    }

    @Override
    public String toString() {
        return subDesireType.name();
    }

    public ActionInfo getNextActionInfo() {
        return new ActionInfo(getNextAction(), "");
    }

    // Whole logic of the subgoal is implemented in this method (in each subclass)
    protected abstract Action getNextAction();

    // Check preconditions, if false next subgoal in hierarchy is checked
    public abstract boolean isExecutable();

    // Check post conditions, if true next subgoal in hierarchy is checked
    public abstract boolean isDone();

    abstract void setType();

    public String getName() {
        return subDesireType.name();
    }
    
    @Override
    public void setOutputAction(Action action) {}
    @Override
    public Action getOutputAction() {return null;}
}