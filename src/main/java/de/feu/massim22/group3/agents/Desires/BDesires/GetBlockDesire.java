package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Thing;

import java.awt.Point;

public class GetBlockDesire extends BeliefDesire {

    private String block;

    public GetBlockDesire(Belief belief, String block, String supervisor) {
        super(belief);
        this.block = block;
        String[] neededActions = {"request"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block, supervisor),
            new AttachSingleBlockFromDispenserDesire(belief, new Thing(0, 0, block, "")))
        );
        precondition.add(new GoToGoalZoneDesire(belief));
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : belief.getAttachedThings()) {
            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block) && belief.getGoalZones().contains(new Point(0, 0))) {
                return new BooleanInfo(true, getName());
            } 
        }
        return new BooleanInfo(false, "Block not attached yet");
    }

    public ActionInfo getNextActionInfo() {
        return fulfillPreconditions();
    }

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
    
    @Override
    public int getPriority() {
        return 950;
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }
}
