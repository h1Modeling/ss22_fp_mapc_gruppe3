package de.feu.massim22.group3.agents;

import eis.iilang.*;
import massim.eismassim.Log;

import java.util.Queue;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * An agent that uses the step()-Method.
 */
public class BdiAgentV2 extends BdiAgent implements Supervisable {

    private Queue<BdiAgentV2.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    public StepUtilities stepLogic = new StepUtilities();
    private ISupervisor supervisor;
    public int index;
    public Desire intention;

    /**
     * Constructor.
     * 
     * @param name    - the agent's name
     * @param mailbox - the mail facility
     */
    public BdiAgentV2(String name, MailService mailbox, int index) {
        super(name, mailbox);
        this.index = index;
        this.supervisor = new Supervisor(this);
        StepUtilities.allAgents.add(this);
        StepUtilities.allSupervisors.add((Supervisor)this.supervisor);
    }

    Supervisor getSupervisor() {
        return (Supervisor) supervisor;
    }
    
    void setSupervisor(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    @Override
    public Action step() {
        updateBeliefs();
        getSupervisor().decisionsDone = false;
        decisionsDone = false; // Agent
        reachablesDone = false; // Agent

        AgentLogger.info(Thread.currentThread().getName() + " step() Start in neuem Thread - Step: " + belief.getStep() + " , Agent: " + this.getName());
        // Mapupdate über updateAgent (wenn möglich, ohne startCalculation auszulösen?)
        stepLogic.updateMap(this);

        // Wenn es der letzte Agent war kommt die Gruppenverarbeitung
        if (StepUtilities.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
           Thread t2 = new Thread(() -> stepLogic.doGroupProcessing(belief.getStep()));
            t2.start();
        }

        // warten auf PATHFINDER_RESULT 
        AgentLogger.info(Thread.currentThread().getName() + " step() Waiting for PATHFINDER_RESULT - Step: " + belief.getStep() + " , Agent: " + this.getName());
        while (true) {
            if (!reachablesDone) {
                     try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                    AgentLogger.info(Thread.currentThread().getName() + " step() PATHFINDER_RESULT - Step: " + belief.getStep() + " , Agent: " + this.getName());

                    Thread t4 = new Thread(() -> stepLogic.runAgentDecisions(belief.getStep(), this));
                    t4.start();
                    break;
            }
        }

        // warten auf decisions (agent und supervisor)
        while (true) {
            if (!(decisionsDone && getSupervisor().decisionsDone)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Intention ermitteln (Desire mit höchster Priorität)
                intention = stepLogic.determineIntention(this);
                break;
            }
        }

        // nächste Action
        AgentLogger.info(Thread.currentThread().getName() + " step() End - Step: " + belief.getStep() + " , Agent: " + this.getName());
        return intention.outputAction;
    }

    /**
     * Update all the beliefs.
     *
     */
    private void updateBeliefs() {
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        //AgentLogger.info(belief.toString());
    }

    private record PerceptMessage(String sender, Percept percept) {
    }

    @Override
    public void initSupervisorStep() {
    }

    @Override
    public void forwardMessageFromSupervisor(Percept message, String receiver, String sender) {
    }

    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }
}
