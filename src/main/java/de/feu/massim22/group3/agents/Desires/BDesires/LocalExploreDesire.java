package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Identifier;

public class LocalExploreDesire extends BeliefDesire {

    private BdiAgentV2 agent;
    private String supervisor;

    public LocalExploreDesire(Belief belief, String supervisor, BdiAgentV2 agent) {
        super(belief);
        AgentLogger.info(Thread.currentThread().getName() + " runAgentDecisions - Start LocalExploreDesire");
        this.agent = agent;
        this.supervisor = supervisor;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }
    
    @Override
    public BooleanInfo isExecutable() {
        return new BooleanInfo(true, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        AgentLogger.info(Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Agent: " + agent.getName());
       // Identifier newDirection = agent.desireProcessing.walkCircles(agent, 10);
        Integer direction = agent.index % 4 ;
        AgentLogger.info(Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Action: move, " +  DirectionUtil.intToString(direction));

        return getActionForMove(DirectionUtil.intToString(direction), getName());
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public int getPriority() {
        return 100;
    }
}