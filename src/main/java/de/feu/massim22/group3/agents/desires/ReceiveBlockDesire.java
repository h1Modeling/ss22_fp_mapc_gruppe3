package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.messages.scenario.ActionResults;

/**
 * The Class <code>ReceiveBlockDesire</code> models the desire to receive a block from a team mate
 * and use the block to submit a single block task.
 * 
 * @author Heinz Stadler
 */
public class ReceiveBlockDesire extends BeliefDesire {

    private TaskInfo info;
    private boolean submitted;

    /**
     * Instantiates a new ReceiveBlockDesire.
     * 
     * @param belief the belief of the agent
     * @param info the task the belief is based on
     * @param teammate the name of the team mate which provides the block
     * @param supervisor the supervisor of the agent group
     */
    public ReceiveBlockDesire(Belief belief, TaskInfo info, String teammate, String supervisor) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        boolean lastActionSuccess = belief.getLastActionResult().equals(ActionResults.SUCCESS);
        return new BooleanInfo(submitted && lastActionSuccess, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a == null) {
            submitted = true;
            return ActionInfo.SUBMIT(info.name, getName());
        }
        return a;
    }   
    
    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return isFulfilled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        super.update(supervisor);
        TaskInfo t = belief.getTask(info.name);
        if (t == null) {
            info.deadline = -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 950;
    }
}
