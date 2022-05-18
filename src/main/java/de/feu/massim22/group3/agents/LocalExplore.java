 package de.feu.massim22.group3.agents;

    import java.util.Iterator;
import java.util.List;

import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.*;
import massim.protocol.data.Role;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;


    public class LocalExplore extends Desire {
        
        LocalExplore(BdiAgentV2 agent){
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
        public boolean isExecutable(Desire desire) {
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
            Identifier newDirection = walkCircles(2);
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() - Action: move, " + newDirection);
            return new Action("move", newDirection);
        }

        private Identifier walkCircles(int steps) {
            Identifier resultDirection = new Identifier("n");

            if (agent.belief.getLastAction() != null && agent.belief.getLastAction().equals(Actions.MOVE)) {
                AgentLogger.info(Thread.currentThread().getName() + " " + this.name
                        + ".getNextAction() - LastAction: " + agent.belief.getLastAction() + " , LastActionParams: " + agent.belief.getLastActionParams().get(0));
                agent.directionCounter++;
                resultDirection = new Identifier(agent.belief.getLastActionParams().get(0));

                if (agent.belief.getLastAction().equals("move") && agent.directionCounter >= agent.circleSize) {
                    if (agent.belief.getLastActionParams().get(0).equals("n")) resultDirection = new Identifier("e");
                    if (agent.belief.getLastActionParams().get(0).equals("e")) resultDirection = new Identifier("s");
                    if (agent.belief.getLastActionParams().get(0).equals("s")) resultDirection = new Identifier("w");
                    if (agent.belief.getLastActionParams().get(0).equals("w")) {
                        resultDirection = new Identifier("n");
                        agent.circleSize = agent.circleSize + steps;
                    }
                    agent.directionCounter = 0;
                }
            }
            return resultDirection;
        }
    }


