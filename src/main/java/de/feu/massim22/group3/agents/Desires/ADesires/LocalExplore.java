 package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;

    public class LocalExplore extends ADesire {
        
    	public LocalExplore(BdiAgentV2 agent){
            super("LocalExplore", agent);
        }

        /**
         * The method proves if a certain Desire is possible.
         *
         * @param desire -  the desire that has to be proven 
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
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName());
            Identifier newDirection = agent.desireProcessing.walkCircles(agent, 2);
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Action: move, " + newDirection);
            
            return  agent.desireProcessing.getPossibleActionForMove(agent, newDirection.getValue()); 
        }  
    }


