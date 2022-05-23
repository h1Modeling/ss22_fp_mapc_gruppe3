package de.feu.massim22.group3.agents.Desires.SubDesires;

import java.util.List;
import java.awt.Point;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Belief.ReachableRoleZone;

import de.feu.massim22.group3.utils.logging.AgentLogger;

import eis.iilang.Action;
import eis.iilang.Identifier;

public class GetRoleSubDesire extends SubDesire {

    private String requiredRole;

    public GetRoleSubDesire(BdiAgent agent) {
        super(agent);
    }

    public void setRequiredRole(String requiredRole) {
        // TODO where is the assignment of roles to Desires defined? Maybe active Norms
        // should be checked too;
        this.requiredRole = requiredRole;
        AgentLogger.config(agent.getName(), "Set required Role to " + this.requiredRole);
    }

    @Override
    public Action getNextAction() {
        // find closest RoleZone
        int minDist = Integer.MAX_VALUE;
        ReachableRoleZone closestRoleZone = null;
        List<ReachableRoleZone> reachableRoleZones = agent.getAgentBelief().getReachableRoleZones();
        for (ReachableRoleZone reachableRoleZone : reachableRoleZones) {
            if (reachableRoleZone.distance() < minDist) {
                minDist = reachableRoleZone.distance();
                closestRoleZone = reachableRoleZone;
            }
        }
        AgentLogger.fine(agent.getName(), "Distance to RoleZone " + minDist);
        if (agent.getAgentBelief().getRoleZones().contains(new Point(0, 0))) {
            AgentLogger.info(agent.getName(), "Adopting role " + requiredRole);
            return new Action("adopt", new Identifier(requiredRole));
        }
        else {
            String nextDirs = closestRoleZone.nextDirections();
            return new Action("move", new Identifier(nextDirs.substring(0, 1)));
        }
    }

    @Override
    public boolean isExecutable() {
        // TODO Norms should be checked here or in isDone() too

        // RoleZone location already known?
        if (agent.getAgentBelief().getReachableRoleZones().size() > 0) {
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean isDone() {
        // TODO Norms should be checked here or in isDone() too
        if (agent.getAgentBelief().getRole().equals(requiredRole)) {
            AgentLogger.fine(agent.getName(), "requiredRole already adopted.");
            return true;
        }
        else {
            return false;
        }
    }
    void setType() {
        this.subDesireType = SubDesires.GET_ROLE;
    }
}
