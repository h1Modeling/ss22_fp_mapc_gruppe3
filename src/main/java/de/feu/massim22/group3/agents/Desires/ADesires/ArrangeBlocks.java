package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;

//TODO Klassenlogik
public class ArrangeBlocks extends SubDesire {

	public ArrangeBlocks(BdiAgent agent) {
		super("ArrangeBlocks", agent);
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
		if(!agent.desireProcessing.analysisDone) {
			agent.desireProcessing.analyseAttachedThings();
			agent.desireProcessing.analysisDone = true;
		}
		
		if (agent.desireProcessing.goodPositionBlocks.size() > 0 
				&& agent.desireProcessing.badPositionBlocks.size() > 0 
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
        Action nextAction = null;
        
        //wenn ein Block nur an der falsche Stelle ist, aber  vom Typ her passt
         nextAction = new Action("rotate", new Identifier("b1"));
         
         //wenn ein Block mit einem anderen den Platz tauschen soll
         //Block lösen
         nextAction = new Action("detach", new Identifier("b1"));
         
         //dann rotieren so das er passt
         nextAction = new Action("rotate", new Identifier("b1"));
         
         //Block an passender Stelle einfügen
         nextAction = new Action("attach", new Identifier("b1"));
         
         // falschen Block entfernen
         nextAction = new Action("detach", new Identifier("b1"));
         
         return nextAction;
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
