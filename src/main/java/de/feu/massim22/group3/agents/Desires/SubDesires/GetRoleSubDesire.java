package de.feu.massim22.group3.agents.Desires.SubDesires;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

public class GetRoleSubDesire extends SubDesire {

    private String requiredRole;

    public GetRoleSubDesire(BdiAgent agent) {
        super(agent);
    }

    public void setRoleToGet(String requiredRole) {
        // TODO where is the assignment of roles to Desires defined? Maybe active Norms
        // should be checked too;
        this.requiredRole = requiredRole;
    }

    @Override
    public Action getNextAction() {
        // TODO Auto-generated method stub
        return new Action("move", new Identifier("e"));
    }

    @Override
    public boolean isExecutable() {
        // TODO Auto-generated method stub

        // true to test DesireHandler
        return true;
    }

    @Override
    public boolean isDone() {
        // TODO Auto-generated method stub
        return false;
    }
    
    void setType() {
        this.subDesireType = SubDesires.GET_ROLE;
    }
}
