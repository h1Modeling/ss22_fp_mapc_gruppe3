package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import eis.iilang.Identifier;

//TODO Klassenlogik
public class RemoveObstacle extends ADesire {
    RemoveObstacle(BdiAgentV2 agent, DesireUtilities desireProcessing) {
        super("RemoveObstacle", agent, desireProcessing);
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
