package de.feu.massim22.group3.agents.Desires.SubDesires;

import eis.iilang.Action;

public class DodgeClearSubDesire extends SubDesire {

    public DodgeClearSubDesire() {
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
        this.subDesireType = SubDesires.DODGE_CLEAR;
    }
}
