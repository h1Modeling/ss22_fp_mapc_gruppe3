package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesires;

public class ProcessTaskDesire extends Desire {

    public ProcessTaskDesire() {
        super();
    }

    @Override
    void defineSubDesires() {
        subDesires.add(SubDesires.GET_ROLE.getSubDesireObj());
        subDesires.add(SubDesires.CHOOSE_TASK.getSubDesireObj());
        subDesires.add(SubDesires.GET_BLOCKS.getSubDesireObj());
        subDesires.add(SubDesires.ASSEMBLE_TASK.getSubDesireObj());
        subDesires.add(SubDesires.GO_TO_GOAL_AREA.getSubDesireObj());
        subDesires.add(SubDesires.SUBMIT_TASK.getSubDesireObj());
    }
    void setType() {
        this.desireType = Desires.PROCESS_TASK;
    }
}
