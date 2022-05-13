package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class GoToUnknownAreaDesire extends Desire {

    public GoToUnknownAreaDesire() {
        super();
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.GO_TO_UNKNOWN_AREA.getSubDesireObj());
    }
    void setType() {
        this.desireType = Desires.GO_TO_UNKNOWN_AREA;
    }
}
