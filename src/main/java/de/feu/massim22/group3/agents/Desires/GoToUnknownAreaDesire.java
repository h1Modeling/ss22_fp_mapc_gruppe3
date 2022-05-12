package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class GoToUnknownAreaDesire extends Desire {

    public GoToUnknownAreaDesire(Desires desireType) {
        super(desireType);
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.GO_TO_UNKNOWN_AREA.getSubDesireObj());
    }
}
