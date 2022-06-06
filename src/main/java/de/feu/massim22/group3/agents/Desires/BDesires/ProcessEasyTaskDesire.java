package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.TaskInfo;

public class ProcessEasyTaskDesire extends BeliefDesire {

    private TaskInfo info;

    public ProcessEasyTaskDesire(Belief belief, TaskInfo info, String supervisor) {
        super(belief);
        this.info = info;
        String[] neededActions = {"submit", "request"};
        precondition.add(new ProcessOnlySubmittableTaskDesire(belief, info));
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new AttachSingleBlockDesire(belief, info.requirements.get(0), supervisor));
        precondition.add(new GoToGoalZoneDesire(belief));
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a == null) {
            return ActionInfo.SUBMIT(info.name, getName());
        }
        return a;
    }

    @Override
    public BooleanInfo isFullfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }

    @Override
    public String getName() {
        return "Easy " + info.name; 
    }

    @Override
    public void update(String supervisor) {
        super.update(supervisor);
        // Deadline of task can change so it needs to be updated every step
        // Belief removes outdated Tasks so result of getTask can be null
        TaskInfo t = belief.getTask(info.name);
        if (t == null) {
            info.deadline = -1;
        }
    }

    @Override
    public int getPriority() {
        int taskId = Integer.parseInt(info.name.substring(4));
        return 100 + taskId;
    }
}
