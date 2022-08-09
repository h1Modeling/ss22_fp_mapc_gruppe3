package de.feu.massim22.group3.agents.desires;

import massim.protocol.data.Thing;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;

/**
 * The Class <code>GetBlockDesire</code> models the desire to get a block of a certain type and to bring it to the goal zone.
 * 
 * @author Heinz Stadler
 */
public class GetBlockDesire extends BeliefDesire {

    private String block;

    /**
     * Instantiates a new GetBlockDesire.
     * 
     * @param belief the belief of the agent
     * @param block the block type which is desireable
     * @param supervisor the supervisor of the agent group
     */
    public GetBlockDesire(Belief belief, String block, String supervisor) {
        super(belief);
        this.block = block;
        String[] neededActions = {"request"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block, supervisor),
            new RequestBlockFromDispenserDesire(belief, new Thing(0, 0, block, "")))
        );
        precondition.add(new GoToGoalZoneDesire(belief));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block) && belief.getGoalZones().contains(new Point(0, 0))) {
                belief.setGroupDesireBlockDetail("");
                return new BooleanInfo(true, getName());
            } 
        }
        return new BooleanInfo(false, "Block not attached yet");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        return fulfillPreconditions();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        for (var info : belief.getTaskInfo()) {
            if (info.requirements.size() <= 2) {
                for (var req : info.requirements) {
                    if (req.type.equals(block)) {
                        return new BooleanInfo(false, "");
                    }
                }
            }
        }
        return new BooleanInfo(true, "attached block not useful");
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 950;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }
}
