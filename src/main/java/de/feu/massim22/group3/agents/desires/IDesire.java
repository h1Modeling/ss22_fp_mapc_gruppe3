package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.intention.IIntention;

public interface IDesire extends IIntention {
    BooleanInfo isFulfilled();
    BooleanInfo isExecutable();
    BooleanInfo isUnfulfillable();
    void update(String supervisor);
    int getPriority();
    boolean isGroupDesire();
}
