package de.feu.massim22.group3.agents.Desires.BDesires;

import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;

public abstract class BeliefDesire implements IDesire {
    protected Belief belief;
    protected List<IDesire> precondition = new ArrayList<>();

    public BeliefDesire(Belief belief) {
        this.belief = belief;
    }

    protected Action fullfillPreconditions() {
        for (IDesire d : precondition) {
            if (!d.isFullfilled()) {
                AgentLogger.info("Next action for agent " + belief.getAgentName() + " from " + d.getName());
                return d.getNextAction();
            }
        }
        return null;
    }

    @Override
    public boolean isExecutable() {
        for (IDesire d : precondition) {
            if (!d.isExecutable()) {
                AgentLogger.info(d.getName() + " is not executable for " + belief.getAgentName());
                return false;
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Action getNextAction() {
        return null;
    }
}
