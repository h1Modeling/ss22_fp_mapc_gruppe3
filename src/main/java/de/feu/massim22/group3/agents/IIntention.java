package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;
import eis.iilang.Action;

public interface IIntention {
    ActionInfo getNextActionInfo();
    String getName();
    //Melinda
    Action getOutputAction();
    void setOutputAction(Action action);
    //Melinda Ende
}
