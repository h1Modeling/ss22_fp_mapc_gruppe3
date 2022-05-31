package de.feu.massim22.group3.agents.Desires;
import de.feu.massim22.group3.agents.BdiAgent;

public class HelpTaskAgentDesire extends Desire {

    public HelpTaskAgentDesire(BdiAgent agent) {
        super(agent);
    }

    @Override
    protected void defineSubDesires() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void setType() {
        this.desireType = Desires.HELP_TASK_AGENT;
    }
}
