package de.feu.massim22.group3.agents.desires.V2;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;

/**
 * The class <code>GoAbandonedBlockDesire</code> models the desire to pick up abandoned blocks.
 * 
 * @author Melinda Betz
 */
public class GoAbandonedBlockDesire extends BeliefDesire {

    private BdiAgentV2 agent;
    private String block;
    private Thing nearest;

    /**
     * Instantiates a new GoAbandonedBlockDesire.
     * 
     * @param agent the agent who wants to go to a abandoned block
     * @param block the abandoned block to pick up
     */
    public GoAbandonedBlockDesire(BdiAgentV2 agent, String block) {
        super(agent.belief);
        this.block = block;
        this.agent = agent;
    }

    /**
     * Checks if the desire is fulfilled.
     * 
     * @return if it is fulfilled or not
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (Thing t : agent.getAttachedThings()) {
            if ((t.x == 0 || t.y == 0) 
                && Math.abs(t.x) <= 1
                && Math.abs(t.y) <= 1 
                && t.type.equals(Thing.TYPE_BLOCK)
                && t.details.equals(block)) {
                return new BooleanInfo(true, "");
            }
        }
        return new BooleanInfo(false, "No block " + block + " attached");
    }

    /**
     * Checks if the desire is executable .
     * 
     * @return if it is executable or not
     */
    @Override
    public BooleanInfo isExecutable() {
        nearest = null;
        int vision = belief.getVision();
        List<Thing> possibleThings = new ArrayList<>();

        for (Thing t : belief.getThings()) {
            int distance = Math.abs(t.x) + Math.abs(t.y);

            if (t.type.equals(Thing.TYPE_BLOCK) && t.details.equals(block) && distance < vision) {
                Thing n = t.x == 0 && t.y == 1 ? null : belief.getThingAt(new Point(t.x, t.y - 1));
                Thing s = t.x == 0 && t.y == -1 ? null : belief.getThingAt(new Point(t.x, t.y + 1));
                Thing e = t.x == -1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x + 1, t.y));
                Thing w = t.x == 1 && t.y == 0 ? null : belief.getThingAt(new Point(t.x - 1, t.y));
                // Test if agent is around
                if ((n == null || !n.type.equals(Thing.TYPE_ENTITY)) && (s == null || !s.type.equals(Thing.TYPE_ENTITY))
                        && (w == null || !w.type.equals(Thing.TYPE_ENTITY))
                        && (e == null || !e.type.equals(Thing.TYPE_ENTITY))) {

                    if (existsTask(t)) {
                        boolean add = true;
                        AgentLogger.info(Thread.currentThread().getName() + " GoAbandonedBlockDesire - isExecutable - task exists for : " + t);
                        
                        if ((belief.getLastAction().equals(Actions.DETACH)
                                && belief.getLastActionResult().equals(ActionResults.SUCCESS))
                                ||  agent.lastStepDetach > belief.getStep() - 10) {
                            if (AgentCooperations.detachedExists(agent)) {
                                Cooperation coop = AgentCooperations.getDetached(agent);
                                AgentLogger.info(Thread.currentThread().getName() + " GoAbandonedBlockDesire - isExecutable - coop : " + coop.toString());
                                
                                if ((coop.helper().equals(agent) && coop.statusHelper().equals(Status.Detached))
                                        || (coop.helper2() != null && coop.helper2().equals(agent)
                                                && coop.statusHelper2().equals(Status.Detached))) {
                                    AgentLogger.info(Thread.currentThread().getName() + " GoAbandonedBlockDesire - isExecutable - noadd ");
                                    add = false;
                                }
                            }
                        }
                        AgentLogger.info(Thread.currentThread().getName() + " GoAbandonedBlockDesire - isExecutable - add ");
                        if (add) possibleThings.add(t);
                    }
                }
            }
        }

        if (possibleThings.size() > 0) {
            possibleThings.sort((a, b) -> Math.abs(a.x) + Math.abs(a.y) - Math.abs(b.x) - Math.abs(b.y));
            nearest = possibleThings.get(0);
            return new BooleanInfo(true, "");
        }

        return new BooleanInfo(false, "No abandoned block in vision");
    }

    /**
     * Gets the next action that has to be done .
     * 
     * @return the next action
     */
    @Override
    public ActionInfo getNextActionInfo() {

        Thing n = belief.getThingWithTypeAt("n", Thing.TYPE_BLOCK);
        Thing e = belief.getThingWithTypeAt("e", Thing.TYPE_BLOCK);
        Thing s = belief.getThingWithTypeAt("s", Thing.TYPE_BLOCK);
        Thing w = belief.getThingWithTypeAt("w", Thing.TYPE_BLOCK);

        // Attach North
        if (n != null && n.x == nearest.x && n.y == nearest.y) {
            return ActionInfo.ATTACH("n", getName());
        }
        // Attach East
        if (e != null && e.x == nearest.x && e.y == nearest.y) {
            return ActionInfo.ATTACH("e", getName());
        }
        // Attach South
        if (s != null && s.x == nearest.x && s.y == nearest.y) {
            return ActionInfo.ATTACH("s", getName());
        }
        // Attach West
        if (w != null && w.x == nearest.x && w.y == nearest.y) {
            return ActionInfo.ATTACH("w", getName());
        }
        
        // Move
        String dir = getDirectionToRelativePoint(new Point(nearest.x, nearest.y));
        return getActionForMove(dir, getName());
    }
        
    private boolean existsTask(Thing block) {
        for (TaskInfo task : belief.getTaskInfo()) {
            if ((task.requirements.size() == 1 
                    && (block.details.equals(task.requirements.get(0).type)))
                    || (task.requirements.size() == 2 
                    && (block.details.equals(task.requirements.get(0).type) || block.details.equals(task.requirements.get(1).type)))
                || (task.requirements.size() == 3 
                && (block.details.equals(task.requirements.get(0).type) || block.details.equals(task.requirements.get(1).type)
                    || block.details.equals(task.requirements.get(2).type)))) {
                return true;
            }
        }
        return false;
    } 
}
