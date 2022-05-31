package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.BdiAgent;

public class GoalZoneGuardDesire extends Desire {

    public GoalZoneGuardDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void setType() {
        this.desireType = Desires.GOAL_ZONE_GUARD;
    }
}
