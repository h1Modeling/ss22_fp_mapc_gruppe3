package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;

import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Supervisable;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class ReceiveAndConnectBlockDesire extends BeliefDesire {
    
    private String agent;
    private String agentFullName;
    private TaskInfo task;
    private boolean submitted = false;
    private Thing block;
    private Supervisable communicator;

    public ReceiveAndConnectBlockDesire(Belief belief, TaskInfo task, String agent, String agentFullName, String supervisor, Thing block, Supervisable communicator) {
        super(belief);
        this.agent = agent;
        this.agentFullName = agentFullName;
        this.communicator = communicator;
        this.task = task;
        this.block = block;
        String[] neededActions = {"submit", "connect"};
        // internal freedom desire which gets removed after preconditions are fulfilled
        precondition.add(new FreedomDesire(belief));
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, block.type, supervisor),
            new AttachSingleBlockFromDispenserDesire(belief, block))
        );
        precondition.add(new GoToGoalZoneDesire(belief));
        // Create new Task for Rotation with single Block
        Set<Thing> requirements = new HashSet<>();
        requirements.add(block);
        TaskInfo info = new TaskInfo(task.name, task.deadline, task.reward, requirements);
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a == null) {
            // Remove freedom desire to make connection possible
            precondition.removeIf(d -> d.getClass().equals(FreedomDesire.class));

            // Submit
            boolean submittable = true;
            for (var p : task.requirements) {
                Thing t = belief.getConnectedThingAt(new Point(p.x, p.y));
                if (t == null) {
                    submittable = false;
                    break;
                }
            }
            if (submittable && task.requirements.size() == belief.getOwnAttachedPoints().size()) {
                submitted = true;
                return ActionInfo.SUBMIT(task.name, getName());
            }

            // Rotate Block in correct Position
            Point attached = belief.getOwnAttachedPoints().get(0);
            Point goal = new Point(block.x, block.y);
            if (!attached.equals(goal)) {
                Point cr = getCRotatedPoint(attached);
                if (cr.equals(goal)) {
                    return getActionForCWRotation(getName());
                }
                return getActionForCCWRotation(getName());
            }

            // Connect
            boolean inOrder = true;
            for (Thing t : task.requirements) {
                Thing f = belief.getThingWithTypeAt(new Point(t.x, t.y), Thing.TYPE_BLOCK);
                if (f == null || !f.details.equals(t.type)) {
                    inOrder = false;
                    break;
                }
            }

            if (inOrder) {
                Point pos = belief.getPosition();
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

            // Test if space around requirements
            int obstacleCountAround = 0;
            for (Thing t : task.requirements) {
                obstacleCountAround += obstacleCountAround(new Point(t.x, t.y));
            }

            // Try to move to better location
            if (obstacleCountAround > 0) {
                // North
                boolean goalZoneN = belief.getGoalZones().contains(new Point(0, -1));
                int testN = 0;
                for (Thing t : task.requirements) {
                    testN += obstacleCountAround(new Point(t.x, t.y - 1));
                }
                if (testN < obstacleCountAround && goalZoneN) {
                    return getActionForMove("n", getName());
                }
                // East
                boolean goalZoneE = belief.getGoalZones().contains(new Point(1, 0));
                int testE = 0;
                for (Thing t : task.requirements) {
                    testE += obstacleCountAround(new Point(t.x + 1, t.y));
                }
                if (testE < obstacleCountAround && goalZoneE) {
                    return getActionForMove("e", getName());
                }
                // South
                boolean goalZoneS = belief.getGoalZones().contains(new Point(0, 1));
                int testS = 0;
                for (Thing t : task.requirements) {
                    testS += obstacleCountAround(new Point(t.x, t.y + 1));
                }
                if (testS < obstacleCountAround && goalZoneS) {
                    return getActionForMove("s", getName());
                }
                // West
                boolean goalZoneW = belief.getGoalZones().contains(new Point(-1, 0));
                int testW = 0;
                for (Thing t : task.requirements) {
                    testW += obstacleCountAround(new Point(t.x - 1, t.y));
                }
                if (testW < obstacleCountAround && goalZoneW) {
                    return getActionForMove("w", getName());
                }
            }

            /*
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
                return ActionInfo.SKIP(getName());
                //return isFree(t) ? ActionInfo.MOVE(dir, getName()) : ActionInfo.SKIP(getName());
            } else {
                // Wait
                return ActionInfo.SKIP(getName());
            }
            */
            return ActionInfo.SKIP(getName());
        }
        return a;
    }   
    
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > task.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        BooleanInfo value = super.isUnfulfillable();
        if (value.value()) {
            Parameter agentPara =  new Identifier(belief.getAgentShortName());
            Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DONE_OR_CANCELED.name(), agentPara);
            communicator.forwardMessage(message, agent, belief.getAgentShortName());
        }
        return value;
    }

    @Override
    public void update(String supervisor) {
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
        return 1500;
    }

    @Override
    public BooleanInfo isFulfilled() {
        boolean value = submitted && belief.getAttachedPoints().size() < task.requirements.size();
        if (value) {
            Parameter agentPara =  new Identifier(belief.getAgentShortName());
            Percept message = new Percept(EventName.SUPERVISOR_PERCEPT_DONE_OR_CANCELED.name(), agentPara);
            communicator.forwardMessage(message, agent, belief.getAgentShortName());
            return new BooleanInfo(true, getName());
        }
        return new BooleanInfo(false, "not submitted yet");
    }

    private int obstacleCountAround(Point p) {
        // Try to avoid dispenser
        Thing atPos = belief.getThingWithTypeAt(new Point(0, 0), Thing.TYPE_DISPENSER);
        int result = atPos == null ? 0 : 20;
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                Point testPoint = new Point(p.x + x, p.y + y);
                Thing t = belief.getThingAt(testPoint);
                if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
                    result += 1;
                }
                // Increase Size for Dispenser because it's not allowed to submit ontop of a dispenser
                Thing d = belief.getThingWithTypeAt(testPoint, Thing.TYPE_DISPENSER);
                if (y == 0 && x == 0 && d != null) {
                    result += 10;
                }
            }
        }
        return result;
    }
}
