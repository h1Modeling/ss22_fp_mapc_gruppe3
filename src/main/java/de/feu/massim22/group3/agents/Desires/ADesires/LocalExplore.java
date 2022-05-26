 package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;

    public class LocalExplore extends SubDesire {
        
    	public LocalExplore(BdiAgent agent){
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
        // Norden To Do: richtige Richtung
        @Override
        public Action getNextAction() {
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Agent: " + agent.getName());
            Identifier newDirection = agent.desireProcessing.walkCircles(agent, 2);
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Action: move, " + newDirection);
            return new Action("move", newDirection);
        }
        
        @Override
        public boolean isDone() {
            return true;
        }
        
        @Override
        public void setType() {
            //this.subDesireType = SubDesires.DIG_FREE;
        }
    }


