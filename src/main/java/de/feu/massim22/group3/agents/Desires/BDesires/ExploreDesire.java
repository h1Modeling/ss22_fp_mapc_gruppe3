package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;

public class ExploreDesire extends BeliefDesire {

    private String agent;
    private String supervisor;

    public ExploreDesire(Belief belief, String supervisor, String agent) {
        super(belief);
        this.agent = agent;
        this.supervisor = supervisor;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public ActionInfo getNextActionInfo() {
        String dir = Navi.<INaviAgentV1>get().getDirectionToNearestUndiscoveredPoint(supervisor, agent);
        return getActionForMove(dir, getName());
    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public int getPriority() {
        return 10;
    }
}
