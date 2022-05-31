package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import eis.iilang.Action;
import eis.iilang.Identifier;
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
    public boolean isFullfilled() {
        for (String a : actions) {
            if (!belief.getRole().actions().contains(a)) {
                // Add Preconditions
                if (precondition.size() == 0) {
                    precondition.add(new GoToRoleZoneDesire(belief));
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public Action getNextAction() {
        Action a = fullfillPreconditions();
        if (a == null) {
            return new Action("adopt", new Identifier(possibleRole.name()));
        }
        return a;
    }

    @Override
    public boolean isExecutable() {
        if (isFullfilled()) {
            return true;
        }
        if (possibleRole != null) {
            return super.isExecutable();
        }
        return false;
    }
}