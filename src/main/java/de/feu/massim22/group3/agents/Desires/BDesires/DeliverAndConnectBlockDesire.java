package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Supervisable;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class DeliverAndConnectBlockDesire extends BeliefDesire {

    private String agent;
    private TaskInfo task;
    private Thing block;
    private Supervisable communicator;

    public DeliverAndConnectBlockDesire(Belief belief, TaskInfo task, String agent, String supervisor, Thing block, Supervisable communicator) {
        super(belief);
        this.agent = agent;
        this.task = task;
        this.block = block;
        this.communicator = communicator;
        String[] neededActions = {"connect"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block.type, supervisor),
            new AttachSingleBlockFromDispenserDesire(belief, block, supervisor))
        );
        precondition.add(new MeetAgentAtGoalZoneDesire(belief, agent));
        precondition.add(new ConnectBlockToAgentDesire(belief, agent, task, block));
    }

    public ActionInfo getNextActionInfo() {
        return fullfillPreconditions();
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "REMOVE");
/*         
        for (IDesire d : precondition) {
            if (!d.isFulfilled().value()) {
                return d.isFulfilled();
            }
        }
        return new BooleanInfo(true, ""); */
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        return new BooleanInfo(false, "REMOVE");
/*         if (belief.getStep() > task.deadline) {
            return new BooleanInfo(true, "deadline has passed");
        }
        var attached = belief.getAttachedThings();
        boolean value = attached.size() == 0 || !attached.get(0).details.equals(block.type);

        // Inform team mate
        if (value) {      
            Parameter desire = new Identifier(ReceiveAndAttachBlockDesire.class.getSimpleName());
            Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE.name(), desire);
            communicator.forwardMessage(message, this.agent, belief.getAgentName());
        }
        return new BooleanInfo(value, getName()); */
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
