package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;

public class GoRoleZone extends Desire {
    GoRoleZone(BdiAgentV2 agent) {
        super("GoRoleZone", agent);
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