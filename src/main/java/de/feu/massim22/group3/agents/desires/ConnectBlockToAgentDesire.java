package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;

import java.awt.Point;

public class ConnectBlockToAgentDesire extends BeliefDesire {

    private String agent;
    private String agentFullName;
    private Thing block;
    private TaskInfo info;
    private Supervisable communicator;
    private boolean detached = false;

    public ConnectBlockToAgentDesire(Belief belief, String agent, String agentFullName, TaskInfo info, Thing block, Supervisable communicator) {
        super(belief);
        this.agent = agent;
        this.agentFullName = agentFullName;
        this.block = block;
        this.info = info;
        this.communicator = communicator;
    }

    public ActionInfo getNextActionInfo() {
        // Detach
        if (belief.getOwnAttachedPoints().size() > info.requirements.size()) {
            for (Point p : belief.getOwnAttachedPoints()) {
                int dist = Math.abs(p.x) + Math.abs(p.y);
                if (dist == 1) {
                    String dir = getDirectionFromPoint(p);
                    detached = true;
                    return ActionInfo.DETACH(dir, getName());
                }
            }
        }

        Thing attached = belief.getAttachedThings().get(0);
        Point agentPos = getAgentPosition(agent);
        boolean horizontalAttached = attached.y == 0;
        boolean eAttached = attached.x == 1;
        boolean nAttached = attached.y == -1;
        boolean isWestFromAgent = agentPos.x > belief.getPosition().x;
        Point goal = new Point(agentPos.x + block.x, agentPos.y + block.y);
        Point pos = belief.getPosition();
        Point attachedPosAbsolute = new Point(pos.x + attached.x, pos.y + attached.y);

        // Move closer to avoid breaking pre condition
        if (getDistance(pos, agentPos) == belief.getVision()) {

            String dir = getDirectionFromPoint(new Point(agentPos.x - pos.x, agentPos.y - pos.y));
            return getActionForMove(dir, getName());
        }

        // Test if in reach
        if (getDistance(pos, goal) == 1) {
            // Connect
            if (goal.equals(attachedPosAbsolute)) {
                Parameter name = new Identifier(belief.getAgentFullName());
                Parameter step = new Numeral(belief.getStep());
                Parameter x1 = new Numeral(attached.x + pos.x);
                Parameter y1 = new Numeral(attached.y + pos.y);
                Parameter x2 = new Numeral(pos.x);
                Parameter y2 = new Numeral(pos.y);
                Percept message = new Percept(EventName.REPORT_POSSIBLE_CONNECTION.name(), name, step, x1, y1, x2, y2);
                communicator.forwardMessage(message, agent, belief.getAgentShortName());
                return ActionInfo.CONNECT(agentFullName, attached, getName());
            }

            // Rotate
            if (pos.x < goal.x) {
                return nAttached ? getActionForCWRotation(getName()) : getActionForCCWRotation(getName());
            }

            // Rotate 
            if (pos.x == goal.x) {
                
                if (pos.y > goal.y) {
                    return eAttached ? getActionForCCWRotation(getName()) : getActionForCWRotation(getName());
                } else {
                    return eAttached ? getActionForCWRotation(getName()) : getActionForCCWRotation(getName());
                }
            }

            // Rotate
            if (pos.x > goal.x) {
                return nAttached ? getActionForCCWRotation(getName()) : getActionForCWRotation(getName());
            }
        }

        // Move to adjacent Position
        if (Math.abs(block.y) == 2) {
            // Problem if obstancle is in the way
            // Rotate

/*             if (!horizontalAttached) {
                return isWestFromAgent && nAttached 
                    ? getActionForCWRotation(getName()) 
                    : getActionForCCWRotation(getName());
            } */
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
        boolean value = detached && belief.getLastActionResult().equals(ActionResults.SUCCESS);
        String info = value ? "Block connected and detached" : "not finished yet";
        return new BooleanInfo(value, info);
    }
    
}
