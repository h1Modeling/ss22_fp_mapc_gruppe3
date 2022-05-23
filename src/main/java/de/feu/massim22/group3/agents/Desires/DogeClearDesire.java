package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class DogeClearDesire extends Desire {

    public DogeClearDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.DODGE_CLEAR.getSubDesireObj(agent));
    }
    void setType() {
        this.desireType = Desires.DODGE_CLEAR;
    }
}