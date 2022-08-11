package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The Class <code>ProcessOnlySubmittableTaskDesire</code> models the desire to only work on tasks which deadline hasn't passed already.
 * 
 * @author Heinz Stadler
 */
class ProcessOnlySubmittableTaskDesire extends BeliefDesire {

    private TaskInfo info;

    /**
     * Instantiates a new ProcessOnlySubmittableTaskDesire.
     * 
     * @param belief the belief of the agent
     * @param info the task which the desire is based on
     */
    public ProcessOnlySubmittableTaskDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return isExecutable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        boolean zone = belief.getReachableGoalZones().size() > 0 || belief.getGoalZones().size() > 0;
        boolean attached = true;
        String wrongThing = "";
        for (Thing t : belief.getAttachedThings()) {
            // Attached Teammates are allowed
            if (t.type.equals(Thing.TYPE_ENTITY)) {
                continue;
            }
            boolean found = false;
            for (Thing r : info.requirements) {
                if (r.type.equals(t.details)) {
                    found = true;
                }
            }
            if (found == false) {
                attached = false;
                wrongThing = t.details;
                break;
            }
        }
        String zoneInfo = zone ? "" : "No visible Goal zone";
        String attachedInfo = attached ? "" : "Wrong Block " + wrongThing + " attached";
        return new BooleanInfo(zone && attached, zoneInfo + attachedInfo);
    }
}
