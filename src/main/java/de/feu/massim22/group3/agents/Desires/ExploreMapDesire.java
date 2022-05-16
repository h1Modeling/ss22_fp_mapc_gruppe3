package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.BdiAgent;

public class ExploreMapDesire extends Desire {

    public ExploreMapDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    void defineSubDesires() {
        // TODO Auto-generated method stub
    }
    void setType() {
        this.desireType = Desires.EXPLORE_MAP;
    }
}
