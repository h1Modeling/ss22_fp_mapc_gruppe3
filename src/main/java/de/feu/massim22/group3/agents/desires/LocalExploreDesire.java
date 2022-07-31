package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.belief.Belief;
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
       Identifier newDirection = agent.desireProcessing.walkCircles(agent, 60);
       return agent.desireProcessing.getActionForMove(agent, newDirection.getValue(), newDirection.getValue(), getName());
        //Integer direction = agent.index % 4 ;
        //AgentLogger.info(Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Action: move, " +  DirectionUtil.intToString(direction));
        /*int direction = 0;
        int direction2 = 0;

        if (Math.abs(agent.belief.getPosition().x) > 100 || Math.abs(agent.belief.getPosition().y) > 100) {
            direction = (agent.exploreDirection + 2) % 4;
            direction2 = (agent.exploreDirection2 + 2) % 4;
        } else {
            direction = agent.exploreDirection;
            direction2 = agent.exploreDirection2;
        }*/
        
       // return getActionForMove(DirectionUtil.intToString(agent.exploreDirection), DirectionUtil.intToString(agent.exploreDirection2), getName());
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