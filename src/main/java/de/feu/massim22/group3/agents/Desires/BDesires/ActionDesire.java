package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Role;

public class ActionDesire extends BeliefDesire {

    private Role possibleRole;
    private String[] actions;

    public ActionDesire(Belief belief, String[] actions) {
        super(belief);
        possibleRole = belief.getRoleByActions(actions);
        this.actions = actions;
    }

    @Override
    public BooleanInfo isFulfilled() {
        for (String a : actions) {
            Role r = belief.getRole();
            if (r == null || !r.actions().contains(a)) {
                // Add Preconditions
                if (precondition.size() == 0) {
                    precondition.add(new GoToRoleZoneDesire(belief));
                }
                return new BooleanInfo(false, "No role with Action " + a);
            }
        }
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fullfillPreconditions();
        if (a == null) {
            return ActionInfo.ADOPT(possibleRole.name(), getName());
        }
        return a;
    }

    @Override
    public BooleanInfo isExecutable() {
        BooleanInfo r = isFulfilled();
        if (r.value()) {
            return r;
        }
        if (possibleRole != null) {
            return super.isExecutable();
        }
        return r;
    }
}