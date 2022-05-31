package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class ReactToNormDesire extends Desire {

    public ReactToNormDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        subDesires.add(SubDesires.REACT_TO_NORM.getSubDesireObj(agent));
    }

    @Override
    protected void setType() {
        this.desireType = Desires.REACT_TO_NORM;
    }
}
