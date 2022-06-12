package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.BdiAgent;

public class DetermineMapSizeDesire extends Desire {

    public DetermineMapSizeDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void setType() {
        this.desireType = Desires.DETERMINE_MAP_SIZE;
    }
}
