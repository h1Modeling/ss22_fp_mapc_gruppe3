package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.IIntention;
import eis.iilang.Action;

public interface IDesire extends IIntention {
    BooleanInfo isFulfilled();
    BooleanInfo isExecutable();
    BooleanInfo isUnfulfillable();
    void update(String supervisor);
    int getPriority();
    boolean isGroupDesire();
}
