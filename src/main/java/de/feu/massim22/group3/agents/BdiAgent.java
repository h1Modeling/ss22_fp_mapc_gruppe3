package de.feu.massim22.group3.agents;

import java.util.List;
import java.awt.Point;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.agents.Desires.ADesires.IDesire;

public abstract class BdiAgent extends Agent {
    // Melinda
    public DesireUtilities desireProcessing = new DesireUtilities();
    public List<IDesire> desires;
    public boolean decisionsDone;
    public boolean requestMade = false;
    public Point lastUsedDispenser;
    
    public Belief belief;
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
