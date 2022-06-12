package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;

public interface IIntention {
    ActionInfo getNextActionInfo();
    String getName();
}
