package de.feu.massim22.group3.agents;

import eis.iilang.*;

import java.util.List;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * A very basic agent.
 */
public class BasicAgent extends Agent {

    private int lastID = -1;
    
    /**
     * Constructor.
     * @param name    the agent's name
     * @param mailbox the mail facility
     */
    public BasicAgent(String name, MailService mailbox) {
        super(name, mailbox);
    }

    @Override
    public void handlePercept(Percept percept) {}

    @Override
    public void handleMessage(Percept message, String sender) {}

    @Override
    public Action step() {
    	AgentLogger.info(this.getName(), "New step");
    	List<Percept> percepts = getPercepts();
        for (Percept percept : percepts) {
        	if (percept.getName() == "goalZone"){
        	AgentLogger.info(this.getName(),
        			"Percept " +
                    String.format("%s - %s", percept.getName(), percept.getParameters()));
        	}
        	if (percept.getName().equals("actionID")) {
                Parameter param = percept.getParameters().get(0);
                if (param instanceof Numeral) {
                    int id = ((Numeral) param).getValue().intValue();
                    if (id > lastID) {
                        lastID = id;
                        return new Action("move", new Identifier("n"));
                    }
                }
            }
        }
        return null;
    }
}
