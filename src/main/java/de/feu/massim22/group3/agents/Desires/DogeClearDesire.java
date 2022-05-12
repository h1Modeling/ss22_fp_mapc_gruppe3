package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class DogeClearDesire extends Desire {

    public DogeClearDesire(Desires desireType) {
        super(desireType);
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.DODGE_CLEAR.getSubDesireObj());
    }
}
