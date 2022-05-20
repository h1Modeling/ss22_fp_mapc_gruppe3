package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;

//TODO Klassenlogik
public class HinderEnemy extends ADesire {
    HinderEnemy(BdiAgentV2 agent, DesireUtilities desireProcessing) {
        super("HinderEnemy", agent, desireProcessing);
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