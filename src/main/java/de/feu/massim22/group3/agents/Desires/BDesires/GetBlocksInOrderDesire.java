package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import eis.iilang.Action;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class GetBlocksInOrderDesire extends BeliefDesire {

    private TaskInfo info;
    
    public GetBlocksInOrderDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
    }

    @Override
    public boolean isFullfilled() {
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(t.type) || !atAgent.details.equals(t.details)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Action getNextAction() {
        // TODO Auto-generated method stub
        return null;
    }
}
