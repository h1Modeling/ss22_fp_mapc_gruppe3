package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import massim.protocol.data.Thing;

public class ExploreDesire extends BeliefDesire {

    private String agent;
    private String supervisor;

    public ExploreDesire(Belief belief, String supervisor, String agent) {
        super(belief);
        this.agent = agent;
        this.supervisor = supervisor;
    }

    @Override
    public boolean isFullfilled() {
        return false;
    }

    @Override
    public Action getNextAction() {
        String dir = Navi.<INaviAgentV1>get().getDirectionToNearestUndiscoveredPoint(supervisor, agent);
        Point p = DirectionUtil.getCellInDirection(dir);
        // Move or clear obstacle
        Thing t = belief.getThingAt(p);
        if (t != null && t.type.equals(Thing.TYPE_OBSTACLE)) {
            return new Action("clear", new Numeral(p.x), new Numeral(p.y));
        } else {
            return new Action("move", new Identifier(dir));
        }
    }
}
