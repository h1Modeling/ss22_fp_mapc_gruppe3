package de.feu.massim22.group3.agents.Desires.SubDesires;

import eis.iilang.Action;

public class GoToGoalAreaSubDesire extends SubDesire {

    public GoToGoalAreaSubDesire() {
        super();
    }

    @Override
    public Action getNextAction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isExecutable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }
    void setType() {
        this.subDesireType = SubDesires.GO_TO_GOAL_AREA;
    }
}
