package de.feu.massim22.group3.agents.desires;

import eis.iilang.Action;

/**
 * The Class <code>OrDesire</code> models a combined desire of two sub desires.
 * The desire is fulfilled if one of the two subdesires is fulfilled.
 * 
 * @author Heinz Stadler
 * @author Melinda Betz (minor contribution)
 */
public class OrDesire implements IDesire {

    private IDesire d1;
    private IDesire d2;

    /**
     * Instantiates a new OrDesire.
     * 
     * @param d1 the first subdesire
     * @param d2 the second subdesire
     */
    public OrDesire(IDesire d1, IDesire d2) {
        this.d1 = d1;
        this.d2 = d2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        BooleanInfo f1 = d1.isExecutable();
        BooleanInfo f2 = d2.isExecutable();
        if (f1.value()) {
            return d1.getNextActionInfo();
        }
        if (f2.value()) {
            return d2.getNextActionInfo();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return d1.getName() + " or " + d2.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        BooleanInfo f1 = d1.isFulfilled();
        BooleanInfo f2 = d2.isFulfilled();
        if (f1.value()) {
            return f1;
        }
        if (f2.value()) {
            return f2;
        }
        return f1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isExecutable() {
        BooleanInfo f1 = d1.isExecutable();
        BooleanInfo f2 = d2.isExecutable();
        if (f1.value()) {
            return f1;
        }
        if (f2.value()) {
            return f2;
        }
        return f1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isUnfulfillable() {
        BooleanInfo f1 = d1.isUnfulfillable();
        BooleanInfo f2 = d2.isUnfulfillable();
        if (f1.value()) {
            return f1;
        }
        if (f2.value()) {
            return f2;
        }
        return f1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        d1.update(supervisor);
        d2.update(supervisor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return Math.max(d1.getPriority(), d2.getPriority());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return d1.isGroupDesire() && d2.isGroupDesire();
    }
    
    /**
     * {@inheritDoc}
     */  
    @Override
    public void setOutputAction(Action action) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public Action getOutputAction() {return null;}
}
