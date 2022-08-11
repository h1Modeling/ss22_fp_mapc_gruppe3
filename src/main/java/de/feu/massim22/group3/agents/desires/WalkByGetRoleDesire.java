package de.feu.massim22.group3.agents.desires;

import massim.protocol.data.Role;

import java.awt.Point;

import de.feu.massim22.group3.agents.belief.Belief;

/**
 * The Class <code>WalkByGetRoleDesire</code> models the desire to get a role which enables certain actions
 * if the agent has a role zone in vision.
 * 
 * @author Heinz Stadler
 */
public class WalkByGetRoleDesire extends BeliefDesire {

    private String[] actions;
    private Role possibleRole;

    /**
     * Instantiates a new WalkByGetRoleDesire.
     * 
     * @param belief the belief of the agent
     * @param actions the actions which are desireable 
     */
    public WalkByGetRoleDesire(Belief belief, String[] actions) {
        super(belief);
        this.actions = actions;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        boolean value = possibleRole != null && belief.getRoleZones().size() > 0;
        String info = value ? getName() : "No role zone in vision";
        return new BooleanInfo(value, info);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 3000;
    }
}
