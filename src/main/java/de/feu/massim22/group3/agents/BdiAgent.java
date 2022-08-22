package de.feu.massim22.group3.agents;

import java.util.ArrayList;
import java.util.List;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.intention.IIntention;
import de.feu.massim22.group3.communication.MailService;

/**
 * The abstract Class <code>BdiAgent</code> defines a base class for implementations of the Bdi concept.
 * 
 * @param <T> defines the type of the desires the agent handles.
 * @author Heinz Stadler
 */
public abstract class BdiAgent<T> extends Agent {
    
    protected Belief belief;
    protected List<T> desires = new ArrayList<T>();
    protected IIntention intention;

    /**
     * Defines the standard constructor for a BDI Agent.
     * 
     * @param name the name of the agent
     * @param mailbox the mail service of the agent
     */
	BdiAgent(String name, MailService mailbox) {
		super(name, mailbox);
		belief = new Belief(name);
	}

    /**
     * Gets the belief of the agent.
     * 
     * @return the belief of the agent
     */
    public Belief getBelief() {
        return belief;
    }
    
    public IIntention getIntention() {
       return this.intention;
    }

    /**
     * Sets the intention of the agent.
     * 
     * @param intention the intention of the agent
     */
    public void setIntention(IIntention intention) {
        this.intention = intention;
    }

    /**
     * Adds a desire to the agent.
     * 
     * @param desire the desire
     */
    public void addDesire(T desire) {
        desires.add(desire);
    }
}
