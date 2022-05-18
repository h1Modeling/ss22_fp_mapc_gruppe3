package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class DigFreeDesire extends Desire {

    public DigFreeDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.DIG_FREE.getSubDesireObj(agent));
    }
    void setType() {
        this.desireType = Desires.DIG_FREE;
    }
}
