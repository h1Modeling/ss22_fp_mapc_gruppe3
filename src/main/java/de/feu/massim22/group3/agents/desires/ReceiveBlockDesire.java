package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.messages.scenario.ActionResults;

public class ReceiveBlockDesire extends BeliefDesire {

    private TaskInfo info;
    private boolean submitted;

    public ReceiveBlockDesire(Belief belief, TaskInfo info, String teammate, String agent, String supervisor) {
        super(belief);
        this.info = info;
        String[] neededActions = {"submit"};
        String blockDetail = info.requirements.get(0).type;
        precondition.add(new ProcessOnlySubmittableTaskDesire(belief, info));
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new MeetAgentToReceiveBlockDesire(belief, teammate));
        precondition.add(new AttachAbandonedBlockDesire(belief, blockDetail, supervisor));
        precondition.add(new GoToGoalZoneDesire(belief));
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    @Override
    public BooleanInfo isFulfilled() {
        boolean lastActionSuccess = belief.getLastActionResult().equals(ActionResults.SUCCESS);
        return new BooleanInfo(submitted && lastActionSuccess, getName());
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a == null) {
            submitted = true;
            return ActionInfo.SUBMIT(info.name, getName());
        }
        return a;
    }   
    
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return isFulfilled();
    }

    @Override
    public void update(String supervisor) {
        super.update(supervisor);
        TaskInfo t = belief.getTask(info.name);
        if (t == null) {
            info.deadline = -1;
        }
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 950;
    }
}
