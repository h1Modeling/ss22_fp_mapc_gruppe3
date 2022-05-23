package de.feu.massim22.group3.agents.Desires.SubDesires;

import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Belief;

public class ChooseTaskSubDesire extends SubDesire {

    public ChooseTaskSubDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    public Action getNextAction() {
        // TODO improve algorithm to choose Task
        return null;
    }

    @Override
    public boolean isExecutable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDone() {

        return false;
    }
    void setType() {
        this.subDesireType = SubDesires.CHOOSE_TASK;
    }
}
