package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class DogeOtherAgentDesire extends Desire {

    public DogeOtherAgentDesire() {
        super();
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.DODGE_OTHER_AGENT.getSubDesireObj());
    }
    void setType() {
        this.desireType = Desires.DODGE_OTHER_AGENT;
    }
}
