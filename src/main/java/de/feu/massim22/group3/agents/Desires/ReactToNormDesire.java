package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class ReactToNormDesire extends Desire {

    public ReactToNormDesire(Desires desireType) {
        super(desireType);
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.REACT_TO_NORM.getSubDesireObj());
    }
}
