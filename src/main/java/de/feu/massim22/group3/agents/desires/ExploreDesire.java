package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;

/**
 * The Class <code>ExploreDesire</code> models the desire to explore the agents environment.
 * 
 * @author Heinz Stadler
 */
public class ExploreDesire extends BeliefDesire {

    private String agent;
    private String supervisor;

    /**
     * Instantiates a new ExploreDesire.
     * 
     * @param belief the belief of the agent
     * @param supervisor the supervisor of the agent group
     * @param agent the name of the agent which holds the desire
     */
    public ExploreDesire(Belief belief, String supervisor, String agent) {
        super(belief);
        this.agent = agent;
        this.supervisor = supervisor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        String dir = Navi.<INaviAgentV1>get().getDirectionToNearestUndiscoveredPoint(supervisor, agent);
        return getActionForMove(dir + dir, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 10;
    }
}
