package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;


public class Explore extends ADesire {

    public Explore(BdiAgent agent) {
        super("Explore", agent);
    }

    public Explore(BdiAgent agent, int prio) {
        super("Explore", agent);
        priority = prio;
    }

    /**
     * The method proves if a certain Desire is possible.
     *
     * @param desire - the desire that has to be proven
     * 
     * @return boolean - the desire is possible or not
     */
    @Override
    public boolean isExecutable() {
        return true;
    }

    /**
     * The method returns the nextAction that is needed.
     * 
     * @return Action - the action that is needed
     * 
     **/
    // Norden TODO: richtige Richtung
    @Override
    public Action getNextAction() {
        AgentLogger.info(
                Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName());
        Identifier newDirection = agent.desireProcessing.walkCircles(agent, 2);
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Action: move, "
                + newDirection);
        return new Action("move", newDirection);
    }
}