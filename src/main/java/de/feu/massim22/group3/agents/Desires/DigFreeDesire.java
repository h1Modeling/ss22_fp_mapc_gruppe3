package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class DigFreeDesire extends Desire {

    public DigFreeDesire() {
        super();
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.DIG_FREE.getSubDesireObj());
    }
    void setType() {
        this.desireType = Desires.DIG_FREE;
    }
}
