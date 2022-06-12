package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;
import de.feu.massim22.group3.agents.BdiAgent;

public class ProcessTaskDesire extends Desire {

    public ProcessTaskDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        subDesires.add(SubDesires.GET_ROLE.getSubDesireObj(agent));
        subDesires.add(SubDesires.CHOOSE_TASK.getSubDesireObj(agent));
        subDesires.add(SubDesires.GET_BLOCKS.getSubDesireObj(agent));
        subDesires.add(SubDesires.ASSEMBLE_TASK.getSubDesireObj(agent));
        subDesires.add(SubDesires.GO_TO_GOAL_AREA.getSubDesireObj(agent));
        subDesires.add(SubDesires.SUBMIT_TASK.getSubDesireObj(agent));
    }

    @Override
    protected void setType() {
        this.desireType = Desires.PROCESS_TASK;
    }
}
