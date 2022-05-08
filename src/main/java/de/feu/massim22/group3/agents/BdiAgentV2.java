package de.feu.massim22.group3.agents;

import eis.iilang.*;

import java.awt.Point;
import java.util.List;

import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * A very basic agent.
 */
public class BdiAgentV2 extends BdiAgent implements Supervisable {

	public StepUtilities stepLogic = new StepUtilities();
	private ISupervisor supervisor;

	/**
	 * Constructor.
	 * 
	 * @param name - the agent's name
	 * @param mailbox -  the mail facility
	 */
	public BdiAgentV2(String name, MailService mailbox) {
		super(name, mailbox);
	}

	Supervisor getSupervisor() {
		return (Supervisor) supervisor;
	}

	@Override
	public void forwardMessageFromSupervisor(Percept message, String receiver) {
	}

	@Override
	public void handlePercept(Percept percept) {
	}

	@Override
	public void handleMessage(Percept message, String sender) {
	}

	@Override
	public Action step() {
		updateBeliefs();
		getSupervisor().decisionsDone = false;
		decisionsDone = false;//Agent

		// Map updaten fehlt noch (Methodenaufruf)

		if (stepLogic.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
			Thread t2 = new Thread(() -> stepLogic.proofGroupMerge(belief.getStep()));
			t2.start();
			
			
			
			Thread t4= new Thread(() -> stepLogic.runAgentDecisions(this));
			t4.start();
			/*if(stepLogic.runSupervisorDecisions() && stepLogic.runAgentDecisions(this)) {// alle Descision durchgeführt
				//Intention ermitteln (Goal mit höchster Priorität 
				//Action ermitteln (1.Action des Plans)
				
				//return new Action
			}*/
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
