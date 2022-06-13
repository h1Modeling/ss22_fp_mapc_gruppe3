package de.feu.massim22.group3.agents.Desires.BDesires;

import eis.iilang.Action;

public class OrDesire implements IDesire {

    private IDesire d1;
    private IDesire d2;

    public OrDesire(IDesire d1, IDesire d2) {
        this.d1 = d1;
        this.d2 = d2;
    }

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

    @Override
    public String getName() {
        return d1.getName() + " or " + d2.getName();
    }

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

    @Override
    public void update(String supervisor) {
        d1.update(supervisor);
        d2.update(supervisor);
    }

    @Override
    public int getPriority() {
        return Math.max(d1.getPriority(), d2.getPriority());
    }
    
    //Melinda   
    @Override
    public void setPriority(int priority) {}
    @Override
    public void setOutputAction(Action action) {}
    @Override
    public Action getOutputAction() {return null;}
    //Melinda Ende
}
