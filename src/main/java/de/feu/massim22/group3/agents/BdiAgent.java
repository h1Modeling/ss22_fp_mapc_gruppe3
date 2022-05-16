package de.feu.massim22.group3.agents;

//import java.util.ArrayList;
//import java.util.List;

import de.feu.massim22.group3.MailService;

public abstract class BdiAgent extends Agent {
    
    protected Belief belief;
    protected DesireHandler desireHandler;
    protected Intention intention;

    BdiAgent(String name, MailService mailbox) {
        super(name, mailbox);
        belief = new Belief(name);
        intention = new Intention();
        desireHandler = new DesireHandler(this);
    }
    
    Intention getIntention() {
        // TODO return default if intention == null
        return intention;
    }
    public Belief getAgentBelief() {
        return belief;
    }
}
