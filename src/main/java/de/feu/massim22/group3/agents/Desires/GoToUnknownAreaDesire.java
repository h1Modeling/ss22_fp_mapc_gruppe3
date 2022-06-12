package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class GoToUnknownAreaDesire extends Desire {

    public GoToUnknownAreaDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        subDesires.add(SubDesires.GO_TO_UNKNOWN_AREA.getSubDesireObj(agent));
    }

    @Override
    protected void setType() {
        this.desireType = Desires.GO_TO_UNKNOWN_AREA;
    }
}
