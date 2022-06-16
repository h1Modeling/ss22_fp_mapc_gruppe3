package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.IIntention;

public interface IDesire extends IIntention {
    BooleanInfo isFulfilled();
    BooleanInfo isExecutable();
    BooleanInfo isUnfulfillable();
    void update(String supervisor);
    int getPriority();
    //Melinda
    /*void setPriority(int priority);
    void setOutputAction(Action action);
    Action getOutputAction();*/
    //Melinda Ende
}
