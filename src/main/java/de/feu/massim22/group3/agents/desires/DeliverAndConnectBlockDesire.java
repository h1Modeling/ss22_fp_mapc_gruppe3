package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class DeliverAndConnectBlockDesire extends BeliefDesire {

    private TaskInfo task;

    public DeliverAndConnectBlockDesire(Belief belief, TaskInfo task, String agent, String agentFullName, String supervisor, Thing block, Supervisable communicator) {
        super(belief);
        this.task = task;
        
        String[] neededActions = {"connect"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block.type, supervisor),
            new AttachSingleBlockFromDispenserDesire(belief, block))
        );
        precondition.add(new MeetAgentAtGoalZoneDesire(belief, agent));
        precondition.add(new ConnectBlockToAgentDesire(belief, agent, agentFullName, task, block, communicator));
    }

    public ActionInfo getNextActionInfo() {
        return fulfillPreconditions();
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (IDesire d : precondition) {
            if (!d.isFulfilled().value()) {
                return d.isFulfilled();
            }
        }
        return new BooleanInfo(true, getName());
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > task.deadline) {
            return new BooleanInfo(true, "deadline has passed");
        }
        return new BooleanInfo(false, getName());
    }

    @Override
    public int getPriority() {
        return 1100; // 950;
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public void update(String supervisor) {
        super.update(supervisor);
    }
}
