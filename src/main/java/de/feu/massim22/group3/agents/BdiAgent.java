package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.List;

//import java.util.ArrayList;
//import java.util.List;

import de.feu.massim22.group3.MailService;

public abstract class BdiAgent<T> extends Agent {
    
    public Belief belief;
    protected List<T> desires = new ArrayList<T>();
    protected IIntention intention;

	BdiAgent(String name, MailService mailbox) {
		super(name, mailbox);
		belief = new Belief(name);
	}

    public Belief getAgentBelief() {
        return belief;
    }

    public void setIntention(IIntention intention) {
        this.intention = intention;
    }

    public void addDesire(T desire) {
        desires.add(desire);
    }
}
