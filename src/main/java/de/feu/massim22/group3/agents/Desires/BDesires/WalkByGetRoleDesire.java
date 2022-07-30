package de.feu.massim22.group3.agents.Desires.BDesires;

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
        boolean value = possibleRole != null && belief.getRoleZones().size() > 0;
        String info = value ? getName() : "No role zone in vision";
        return new BooleanInfo(value, info);
    }

    @Override
    public ActionInfo getNextActionInfo() {
        // Adopt
        if (belief.getRoleZones().contains(new Point(0, 0))) {
            return ActionInfo.ADOPT(possibleRole.name(), getName());
        }
        // Move to Zone
        belief.getRoleZones().sort((a, b) -> (Math.abs(a.x) + Math.abs(a.y)) - (Math.abs(b.x) + Math.abs(b.y)));
        Point nearest = belief.getRoleZones().get(0);
        String dir = getDirectionToRelativePoint(nearest);
        return getActionForMove(dir, getName());
    }

    @Override
    public int getPriority() {
        return 3000;
    }
    
}
