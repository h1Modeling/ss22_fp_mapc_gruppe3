package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

class ProcessOnlySubmittableTaskDesire extends BeliefDesire {

    private TaskInfo info;

    public ProcessOnlySubmittableTaskDesire(Belief belief, TaskInfo info) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runSupervisorDecisions - Start ProcessOnlySubmittableTaskDesire");
        this.info = info;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return isExecutable();
    }

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
