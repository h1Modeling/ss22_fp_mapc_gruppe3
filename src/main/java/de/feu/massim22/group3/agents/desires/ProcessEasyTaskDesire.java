package de.feu.massim22.group3.agents.desires;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;

/**
 * The Class <code>ProcessEasyTaskDesire</code> models the desire to fulfill a single block task.
 * The Desire lets the agent get a block and submit it at a goal zone.
 * 
 * @author Heinz Stadler
 */
public class ProcessEasyTaskDesire extends BeliefDesire {

    private TaskInfo info;

    /**
     * Instantiates a new ProcessEasyTaskDesire.
     * 
     * @param belief the belief of the agent
     * @param info the task which should be fulfilled
     * @param supervisor the supervisor of the agent group
     */
    public ProcessEasyTaskDesire(Belief belief, TaskInfo info, String supervisor) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ProcessEasyTaskDesire");
        this.info = info;
        String blockDetail = info.requirements.get(0).type;
        String[] neededActions = {"submit", "request"};
        precondition.add(new ProcessOnlySubmittableTaskDesire(belief, info));
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
            new AttachAbandonedBlockDesire(belief, blockDetail, supervisor),
            new RequestBlockFromDispenserDesire(belief, info.requirements.get(0)))
        );
        precondition.add(new GoToGoalZoneDesire(belief));
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a == null) {
            return ActionInfo.SUBMIT(info.name, getName());
        }
        return a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        if (belief.getStep() > info.deadline) {
            return new BooleanInfo(true, "Deadline has passed");
        }
        return super.isUnfulfillable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Easy " + info.name; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        super.update(supervisor);
        // Deadline of task can change so it needs to be updated every step
        // Belief removes outdated Tasks so result of getTask can be null
        TaskInfo t = belief.getTask(info.name);
        if (t == null) {
            info.deadline = -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        String detail = info.requirements.get(0).type;
        Point dispenser = belief.getNearestRelativeManhattanDispenser(detail);
        Point abandoned = belief.getAbandonedBlockPosition(detail);
        int dispenserDist = dispenser != null ? Math.abs(dispenser.x) + Math.abs(dispenser.y) : 500;
        int abandonedDist = abandoned != null ? Math.abs(abandoned.x) + Math.abs(abandoned.y) : 500;
        return 100 + (500 - Math.min(dispenserDist, abandonedDist));
    }

    /**
     * Gets the task the desire is based on.
     * 
     * @return the task
     */
    public TaskInfo getTaskInfo() {
        return info;
    }
}
