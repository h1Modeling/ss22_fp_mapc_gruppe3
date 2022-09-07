package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import massim.protocol.data.Role;

/**
 * The Class <code>ActionDesire</code> models the desire to obtain a role which enables certain actions.
 * 
 * @author Heinz Stadler
 */
public class ActionDesire extends BeliefDesire {

    private Role possibleRole;
    private String[] actions;

    /**
     * Instantiates a new ActionDesire.
     * 
     * @param belief the belief of the agent
     * @param actions the actions the agent wants to perform
     */
    public ActionDesire(Belief belief, String[] actions) {
        super(belief);
        possibleRole = belief.getRoleByActions(actions);
        this.actions = actions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        Role r = belief.getRole();

        if (actions[0].equals("clear") && !r.name().equals(possibleRole.name())) {
            if (precondition.size() == 0) {
                precondition.add(new GoToRoleZoneDesire(belief));
            }
            return new BooleanInfo(false, "Did not adopt the best clear role yet");
        }
        for (String a : actions) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a == null) {
            return ActionInfo.ADOPT(possibleRole.name(), getName());
        }
        return a;
    }

    /**
     * {@inheritDoc}
     */
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