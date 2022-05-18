package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoGoalZone extends Desire {
    GoGoalZone(BdiAgentV2 agent) {
        super("GoGoalZone", agent);
    }

    @Override
    public boolean isExecutable(Desire desire) {
        return true;
    }

    @Override
    public Action getNextAction() {
        Identifier x = new Identifier("0");
        Identifier y = new Identifier("-1");

        return new Action(" ", x, y);
    }
}
