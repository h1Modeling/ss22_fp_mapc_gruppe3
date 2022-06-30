package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.map.Navi;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class ReceiveAndAttachBlockDesire extends BeliefDesire {
    
    private String agent;
    private String supervisor;
    private TaskInfo task;
    private boolean submitted = false;
    private int waiting = 0;

    public ReceiveAndAttachBlockDesire(Belief belief, TaskInfo task, String agent, String supervisor) {
        super(belief);
        this.agent = agent;
        this.task = task;
        String[] neededActions = {"submit", "attach"};
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new GoToGoalZoneDesire(belief));
        // Create new Task for Rotation with single Block
        Set<Thing> requirements = new HashSet<>();
        for (Thing t : task.requirements) {
            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1) {
                requirements.add(t);
                break;
            }
        }
        if (requirements.size() == 0) {
            System.out.println(task.requirements.size());
            throw new IllegalArgumentException();
        }
        TaskInfo info = new TaskInfo(task.name, task.deadline, task.reward, requirements);
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a == null) {
            // Get teammate Position
            Point posTeammate = Navi.get().getPosition(agent, supervisor);
            Point pos = belief.getPosition();
            int dist = getDistance(pos, posTeammate);
            if (dist > belief.getVision()) {
                // Dance around to avoid clearing
                waiting += 1;
                String dir = waiting % 2 == 0 ? "e" : "w";
                Point p = getPointFromDirection(dir);
                Thing t = belief.getThingAt(p);
                return isFree(t) ? ActionInfo.MOVE(dir, getName()) : ActionInfo.SKIP(getName());
            } else {
                // Wait
                return ActionInfo.SKIP(getName());
            }
        }
        return a;
    }   
    
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > task.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
        super.update(supervisor);
        TaskInfo t = belief.getTask(task.name);
        if (t == null) {
            task.deadline = -1;
        }
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1100;
    }

    @Override
    public BooleanInfo isFulfilled() {
        // TODO REMOVE
        return new BooleanInfo(false, "REMovE");
        /* 
        return submitted && belief.getAttachedPoints().size() == 0
            ? new BooleanInfo(true, getName())
            : new BooleanInfo(false, "not submitted yet");
            */
    }
}
