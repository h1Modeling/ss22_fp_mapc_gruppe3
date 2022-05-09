package de.feu.massim22.group3.agents;

import eis.iilang.*;
import massim.protocol.data.Thing;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.EisSender;
import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * A very basic agent.
 */
public class BdiAgentV2 extends BdiAgent implements Supervisable {

	public StepUtilities stepLogic = new StepUtilities();
	private ISupervisor supervisor;
	public int index;

	/**
	 * Constructor.
	 * 
	 * @param name - the agent's name
	 * @param mailbox -  the mail facility
	 */
	public BdiAgentV2(String name, MailService mailbox, int index) {
		super(name, mailbox);
		this.index = index;
	}

	Supervisor getSupervisor() {
		return (Supervisor) supervisor;
	}
	@Override
	public void initSupervisorStep() {}

	@Override
	public void forwardMessageFromSupervisor(Percept message, String receiver,String sender) {
	}

	@Override
	public void handlePercept(Percept percept) {}

	@Override
	public void handleMessage(Percept message, String sender) {}

	@Override
	public Action step() {
		updateBeliefs();
		getSupervisor().decisionsDone = false;
		decisionsDone = false;// Agent

		stepLogic.updateMap(this);// Mapupdate

		if (stepLogic.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
			Thread t2 = new Thread(() -> stepLogic.proofGroupMerge(belief.getStep()));
			t2.start();

			Thread t4 = new Thread(() -> stepLogic.runAgentDecisions(this));
			t4.start();

			// Intention ermitteln (Goal mit höchster Priorität
			
			// Action ermitteln (1.Action des Plans)

			// return new Action

		}

	}

	/**
	 * Update all the beliefs.
	 *
	 */
	private void updateBeliefs() {
		List<Percept> percepts = getPercepts();
		belief.update(percepts);
		AgentLogger.info(belief.toString());
	}
}
