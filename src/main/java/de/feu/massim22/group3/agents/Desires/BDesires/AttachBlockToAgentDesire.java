package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

import java.awt.Point;

public class AttachBlockToAgentDesire extends BeliefDesire {

    private String agent;
    private Thing block;
    private TaskInfo info;

    public AttachBlockToAgentDesire(Belief belief, String agent, TaskInfo info, Thing block) {
        super(belief);
        this.agent = agent;
        this.block = block;
        this.info = info;
    }

    public ActionInfo getNextActionInfo() {
        Thing attached = belief.getAttachedThings().get(0);
        Point agentPos = getAgentPosition(agent);
        boolean horizontalAttached = attached.x != 0;
        boolean eAttached = attached.x == 1;
        boolean nAttached = attached.y == -1;
        boolean isWestFromAgent = agentPos.x > belief.getPosition().x;
        Point goal = new Point(agentPos.x + block.x, agentPos.y + block.y);
        Point pos = belief.getPosition();
        Point attachedPosAbsolute = new Point(pos.x + attached.x, pos.y + attached.y);
        // Test if in reach
        if (getDistance(pos, goal) == 1) {
            // Connect
            if (goal.equals(attachedPosAbsolute)) {
                return ActionInfo.CONNECT(agent, attached, getName());
            }

            // Rotate
            if (pos.x < goal.x) {
                return nAttached ? getActionForCWRotation(getName()) : getActionForCCWRotation(getName());
            }
        }

        // Move to adjacent Position
        if (Math.abs(block.y) == 2) {
            // Rotate
            if (!horizontalAttached) {
                return isWestFromAgent && nAttached 
                    ? getActionForCWRotation(getName()) 
                    : getActionForCCWRotation(getName());
            }
            // Move
            return isWestFromAgent && eAttached
                ? getActionForMove(new Point(goal.x - 1, goal.y), getName())
                : getActionForMove(new Point(goal.x + 1, goal.y), getName());
            
        }
        if (Math.abs(block.y) < 2) {
            // Rotate
            if ((block.y == -1 && nAttached) || (block.y == 1 && !nAttached && !horizontalAttached)) {
                return getActionForCWRotation(getName());
            }
            // Move easy
            if (isWestFromAgent && block.x <= -1) {
                return getActionForMove(new Point(goal.x - 1, goal.y), getName());
            }
            if (!isWestFromAgent && block.x >= 1) {
                return getActionForMove(new Point(goal.x + 1, goal.y), getName());
            }
            // Move around
            return block.y == -1
                ? getActionForMove(new Point(goal.x, goal.y - 1), getName())
                : getActionForMove(new Point(goal.x, goal.y + 1), getName());
            
        }
        return ActionInfo.SKIP(getName());
    }

    @Override
    public BooleanInfo isFulfilled() {
        // TODO Auto-generated method stub
        return new BooleanInfo(false, "TODO");
    }
    
}
