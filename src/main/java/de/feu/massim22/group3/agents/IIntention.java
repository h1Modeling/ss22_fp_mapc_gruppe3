package de.feu.massim22.group3.agents;

import eis.iilang.Action;

public interface IIntention {
    Action getNextAction();
    String getName();
}
