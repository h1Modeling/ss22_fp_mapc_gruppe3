package de.feu.massim22.group3.agents.Desires.SubDesires;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoToUnkownAreaSubDesire extends SubDesire {

    public GoToUnkownAreaSubDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    public Action getNextAction() {
        // TODO Auto-generated method stub
        return new Action("move", new Identifier("n"));
    }

    @Override
    public boolean isExecutable() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }
    void setType() {
        this.subDesireType = SubDesires.GO_TO_UNKNOWN_AREA;
    }
}
