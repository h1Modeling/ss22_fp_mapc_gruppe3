package de.feu.massim22.group3.agents.desires.V2;

import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.*;
import de.feu.massim22.group3.utils.DirectionUtil;
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
       //Identifier newDirection = agent.desireProcessing.walkCircles(agent, 60);
       //return agent.desireProcessing.getActionForMove(agent, newDirection.getValue(), newDirection.getValue(), getName());
        //Integer direction = agent.index % 4 ;
        //AgentLogger.info(Thread.currentThread().getName() + "LocalExploreDesire.getNextAction() - Action: move, " +  DirectionUtil.intToString(direction));

       if (agent.getIntention() != null && agent.getIntention().getName().equals("LocalExploreDesire")) {
           if (agent.exploreCount > 40) {
               agent.exploreDirection = (agent.exploreDirection + 1) % 4;
               agent.exploreDirection2 = (agent.exploreDirection2 + 1) % 4;
               agent.exploreCount = 0;
           } else
               agent.exploreCount++;
       } else {
           agent.exploreCount = 0;
  
           if (!agent.belief.getRoleName().equals("default")) 
           // Richtung freie GoalZone für Multi-Block-Tasks
               if (agent.belief.getPosition().y < 28)
                   agent.exploreDirection = DirectionUtil.stringToInt(DirectionUtil.getDirection(agent.belief.getPosition(), new Point(9, 1)));
               else
                   agent.exploreDirection = DirectionUtil.stringToInt(DirectionUtil.getDirection(agent.belief.getPosition(), new Point(28, 54)));
           agent.exploreDirection2 = (agent.exploreDirection2 + 5) % 4;
       }
        
        return agent.desireProcessing.getActionForMove(agent, DirectionUtil.intToString(agent.exploreDirection), DirectionUtil.intToString(agent.exploreDirection2), getName());
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