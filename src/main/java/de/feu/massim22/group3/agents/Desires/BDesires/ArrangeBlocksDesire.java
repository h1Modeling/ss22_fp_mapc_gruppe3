package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class ArrangeBlocksDesire extends BeliefDesire {

    private TaskInfo info;
    
    public ArrangeBlocksDesire(Belief belief, TaskInfo info) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ArrangeBlocksDesire");
        this.info = info;
    }


    @Override
    public BooleanInfo isExecutable() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.isExecutable");
        return new BooleanInfo(true, "");
    }
    
    @Override
    public BooleanInfo isFulfilled() {
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.isFulfilled");
        for (Thing t : info.requirements) {
            Thing atAgent = belief.getThingAt(new Point(t.x, t.y));
            if (atAgent == null || !atAgent.type.equals(Thing.TYPE_BLOCK) || !atAgent.details.equals(t.type)) {
                String ea = atAgent == null ? t.details + " not at agent" : "";
                String et = atAgent != null && !atAgent.type.equals(Thing.TYPE_BLOCK) ?  "Attached is no block" : "";
                String ed = atAgent != null && !atAgent.details.equals(t.type) ? "Wrong Block attached" : "";
                return new BooleanInfo(false, ea + et + ed);
            }
        }
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(
                Thread.currentThread().getName() + " runSupervisorDecisions - ArrangeBlocksDesire.getNextActionInfo");
        Point taskBlock = new Point(info.requirements.get(0).x, info.requirements.get(0).y);
        Point agentBlock = belief.getAttachedPoints().get(0);
        String clockDirection = DirectionUtil.getClockDirection(agentBlock, taskBlock);

        if (clockDirection == "") {
            return ActionInfo.SKIP(getName());
        } else {
            Thing cw = belief.getThingCRotatedAt(agentBlock);
            Thing ccw = belief.getThingCCRotatedAt(agentBlock);

            if (clockDirection == "cw") {
                if (isFree(cw)) {
                    return ActionInfo.ROTATE_CW(getName());
                } else {
                    if (cw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = DirectionUtil.rotateCW(agentBlock);
                        return ActionInfo.CLEAR(target, getName());
                    }
                }
            }
            if (clockDirection == "ccw") {
                if (isFree(ccw)) {
                    return ActionInfo.ROTATE_CCW(getName());
                } else {
                    if (ccw.type.equals(Thing.TYPE_OBSTACLE)) {
                        Point target = DirectionUtil.rotateCCW(agentBlock);
                        return ActionInfo.CLEAR(target, getName());
                    }
                }
            }
            return ActionInfo.SKIP(getName());
        }
    }
}