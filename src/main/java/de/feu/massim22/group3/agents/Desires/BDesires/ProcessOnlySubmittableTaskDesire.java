package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.TaskInfo;

class ProcessOnlySubmittableTaskDesire extends BeliefDesire {

    private TaskInfo info;

    public ProcessOnlySubmittableTaskDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
    }

    @Override
    public boolean isFullfilled() {
        return isExecutable();
    }

    @Override
    public boolean isExecutable() {
        return info.deadline >= belief.getStep() && belief.getReachableGoalZones().size() > 0;
    }
}
