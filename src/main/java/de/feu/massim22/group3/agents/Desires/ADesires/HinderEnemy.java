package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Desires.SubDesires.SubDesire;
import de.feu.massim22.group3.agents.DesireUtilities;
import eis.iilang.Action;
import eis.iilang.Identifier;

//TODO Klassenlogik
public class HinderEnemy extends SubDesire {
	public HinderEnemy(BdiAgent agent) {
        super("HinderEnemy", agent);
    }

    @Override
    public boolean isExecutable() {
        return false;
    }

    @Override
    public Action getNextAction() {
        Identifier x = new Identifier("0");
        Identifier y = new Identifier("-1");

        return new Action(" ", x, y);
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