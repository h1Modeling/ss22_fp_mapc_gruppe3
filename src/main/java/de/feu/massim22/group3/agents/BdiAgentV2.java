package de.feu.massim22.group3.agents;

import eis.iilang.*;
import massim.eismassim.Log;

import java.util.Queue;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.*;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * An agent that uses the step()-Method.
 */
public class BdiAgentV2 extends BdiAgent implements Supervisable {

    //private Queue<BdiAgentV2.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    public DesireUtilities desireProcessing = new DesireUtilities();
    public StepUtilities stepLogic = new StepUtilities(desireProcessing);
    
    public Supervisor supervisor;
    public int index;
    
    public List<ADesire> desires;
    public ADesire intention;
    public boolean decisionsDone;
    public boolean beliefsDone;

    public int directionCounter = 0;
    public int circleSize = 5;

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

    @Override
    public Action step() {
        desires = new ArrayList<ADesire>();
        updateBeliefs();
        supervisor.decisionsDone = false;
        decisionsDone = false; // Agent
        beliefsDone = false; // Agent

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
            if (!beliefsDone) {
                     try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                    AgentLogger.info(Thread.currentThread().getName() + " step() PATHFINDER_RESULT - Step: " + belief.getStep() + " , Agent: " + this.getName());

                    Thread t4 = new Thread(() -> desireProcessing.runAgentDecisions(belief.getStep(), this));
                    t4.start();
                    break;
            }
        }

        // warten auf decisions (agent und supervisor)
        while (true) {
            if (!(decisionsDone && supervisor.decisionsDone)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Intention ermitteln (Desire mit höchster Priorität)
                intention = desireProcessing.determineIntention(this);
                break;
            }
        }

        // nächste Action
        AgentLogger.info(Thread.currentThread().getName() + " step() End - Step: " + belief.getStep() + " , Agent: " + this.getName() + " , Intention: " + intention.name + " , Action: " +  intention.outputAction + " , Params: " +  intention.outputAction.getParameters().get(0));
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

    //private record PerceptMessage(String sender, Percept percept) {}

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
