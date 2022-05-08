package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.MailService;

public abstract class BdiAgent extends Agent {
	
	protected Belief belief;
	protected List<Desire> desire = new ArrayList<>();
	protected Intention intention;
	//Melinda Betz 07.05.2022
	protected boolean decisionsDone;

	BdiAgent(String name, MailService mailbox) {
		super(name, mailbox);
		belief = new Belief();
		intention = new Intention();
	}
	
	void addDesire(Desire d) {
		desire.add(d);
	}
	
	Intention getIntention() {
		// TODO return default if intention == null
		return intention;
	}
}
