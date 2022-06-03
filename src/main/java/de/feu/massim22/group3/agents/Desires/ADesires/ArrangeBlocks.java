package de.feu.massim22.group3.agents.Desires.ADesires;

import java.awt.Point;
import java.util.List;
import de.feu.massim22.group3.agents.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;

//TODO Klassenlogik für mehr als einen Block
public class ArrangeBlocks extends ADesire {

	public ArrangeBlocks(BdiAgent agent) {
		super("ArrangeBlocks", agent);
        groupOrder = true;
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
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() Start");
		if(!agent.desireProcessing.analysisDone) {
			agent.desireProcessing.analyseAttachedThings();
			agent.desireProcessing.analysisDone = true;
		}
		
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".isExecutable() badPositionBlocks: " + agent.desireProcessing.badPositionBlocks.size() + " , missingBlocks: " + agent.desireProcessing.missingBlocks.size());
		
//		if (agent.desireProcessing.goodPositionBlocks.size() > 0 
		if (agent.desireProcessing.badPositionBlocks.size() > 0 
				&& agent.desireProcessing.missingBlocks.size() == 0) {
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * The method returns the nextAction that is needed.
	 * 
	 * @return Action - the action that is needed
	 * 
	 **/
	@Override
    public Action getNextAction() {
        AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() Start");
        Action nextAction = null;
        
        List<Point> attachedPoints = agent.belief.getAttachedThings();
        if (attachedPoints.size() == 1) {
            AgentLogger.info(Thread.currentThread().getName() + " " + this.name + ".getNextAction() Ein-Block-Task");
            // task besteht aus einem Block
            Point taskBlock = new Point(agent.desireProcessing.task.requirements.get(0).x, agent.desireProcessing.task.requirements.get(0).y);
            Point agentBlock = attachedPoints.get(0);            
            nextAction = new Action("rotate", new Identifier(DirectionUtil.getClockDirection(agentBlock, taskBlock)));
        } else {
// TODO 2-Block-Tasks
        nextAction = new Action("skip");
        
		// wenn ein Block nur an der falsche Stelle ist, aber vom Typ her passt
		//nextAction = new Action("rotate", new Identifier("b1"));

		// wenn ein Block mit einem anderen den Platz tauschen soll
		// Block lösen
		//nextAction = new Action("detach", new Identifier("b1"));

		// dann rotieren so das er passt
		//nextAction = new Action("rotate", new Identifier("b1"));

		// Block an passender Stelle einfügen
		//nextAction = new Action("attach", new Identifier("b1"));

		// falschen Block entfernen
		//nextAction = new Action("detach", new Identifier("b1"));
        }
         
         return nextAction;
    }
}
