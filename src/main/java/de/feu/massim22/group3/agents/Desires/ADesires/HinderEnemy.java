package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgent;
import eis.iilang.Action;
import eis.iilang.Identifier;

//TODO Klassenlogik
public class HinderEnemy extends ADesire {
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
}