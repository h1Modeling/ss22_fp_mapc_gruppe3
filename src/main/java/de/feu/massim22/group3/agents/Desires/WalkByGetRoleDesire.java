package de.feu.massim22.group3.agents.Desires;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Role;

import java.awt.Point;

public class WalkByGetRoleDesire extends BeliefDesire {

    private String[] actions;
    private Role possibleRole;

    public WalkByGetRoleDesire(Belief belief, String[] actions) {
        super(belief);
        this.actions = actions;
    }

    @Override
    public BooleanInfo isFulfilled() {
        if (possibleRole == null) {
            possibleRole = belief.getRoleByActions(actions);
        }
        Role r = belief.getRole();
        if (r == null || possibleRole == null) {
            return new BooleanInfo(false, "No Role Found");
        }

        // Test needed actions
        for (String a : actions) {
            if (!r.actions().contains(a)) {
                return new BooleanInfo(false, "Action " + a + " not in Role");
            }
        }

        return new BooleanInfo(true, getName());
    }

    @Override
    public BooleanInfo isExecutable() {
        boolean value = possibleRole != null && belief.getRoleZones().contains(new Point(0, 0));
        String info = value ? getName() : "No Rolezone in vision";
        return new BooleanInfo(value, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        return ActionInfo.ADOPT(possibleRole.name(), getName());
    }

    @Override
    public int getPriority() {
        return 3000;
    }
    
}
