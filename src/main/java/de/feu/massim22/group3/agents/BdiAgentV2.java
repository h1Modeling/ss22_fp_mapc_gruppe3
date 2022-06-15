package de.feu.massim22.group3.agents;

import eis.iilang.*;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import de.feu.massim22.group3.*;
import de.feu.massim22.group3.agents.Desires.BDesires.IDesire;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * An agent that uses the step()-Method.
 */
public class BdiAgentV2 extends BdiAgent<IDesire> implements Supervisable {

    public DesireUtilities desireProcessing = new DesireUtilities();
    public StepUtilities stepLogic = new StepUtilities(desireProcessing);
    
    public boolean decisionsDone;
    public boolean requestMade = false;
    public Point lastUsedDispenser;
    
    public Supervisor supervisor;
    public int index;
    
    public IDesire intention;
    public boolean beliefsDone;


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
        desires = new ArrayList<IDesire>();
        updateBeliefs();
        supervisor.setDecisionsDone(false);
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
        AgentLogger.info(Thread.currentThread().getName() + " step() Waiting for beliefsDone - Step: " + belief.getStep() + " , Agent: " + this.getName());
        while (true) {
            if (!beliefsDone) {
                     try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                    AgentLogger.info(Thread.currentThread().getName() + " step() beliefsDone - Step: " + belief.getStep() + " , Agent: " + this.getName());
                    Thread t4 = new Thread(() -> desireProcessing.runAgentDecisions(belief.getStep(), this));
                    t4.start();
                    break;
            }
        }

        // warten auf decisions (agent und supervisor)
        AgentLogger.info(Thread.currentThread().getName() + " step() Waiting for decisionsDone - Step: " + belief.getStep() + " , Agent: " + this.getName());
        
        while (true) {
            if (!(decisionsDone && supervisor.getDecisionsDone())) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Delete expired Desires
                desires.removeIf(d -> d.isUnfulfillable().value());
                // Sort Desires
                //desires.sort((a, b) -> a.getPriority() - b.getPriority());
                // Intention ermitteln (Desire mit höchster Priorität)
                intention = desireProcessing.determineIntention(this);
                break;
            }
        }

        // nächste Action

        AgentLogger.info(Thread.currentThread().getName() + " step() End - Step: " + belief.getStep() + " , Agent: " + this.getName() + " , Intention: " + intention.getName() + " , Action: " +  intention.getNextActionInfo() + " , Params: " +  intention.getNextActionInfo().value().getParameters());
        return intention.getNextActionInfo().value();
    }

    /**
     * Update all the beliefs.
     *
     */
    private void updateBeliefs() {
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        
        /*for (Percept percept : percepts) {
            if (percept.getName() == "attached"){
            AgentLogger.info(this.getName(),
                    "Percept " +
                    String.format("%s - %s", percept.getName(), percept.getParameters()));
            }
        }*/
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
