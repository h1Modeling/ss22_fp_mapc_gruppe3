package de.feu.massim22.group3.agents;

import eis.iilang.*;

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
        decisionsDone = false;// Agent

        // Mapupdate über updateAgent (wenn möglich, ohne startCalculation auszulösen?)
        stepLogic.updateMap(this);

        // Wenn es der letzte Agent war kommt die Gruppenverarbeitung
        if (stepLogic.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
            Thread t2 = new Thread(() -> stepLogic.doGroupProcessing(belief.getStep()));
            t2.start();
        }

        // warten auf PATHFINDER_RESULT Message
        while (true) {
            if (queue.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                BdiAgentV2.PerceptMessage message = queue.poll();

                if (TaskName.valueOf(message.percept.getName()) == TaskName.PATHFINDER_RESULT) {
                    List<Parameter> parameters = message.percept.getParameters();
                    belief.updateFromPathFinding(parameters);
                    AgentLogger.info(belief.reachablesToString());

                    Thread t4 = new Thread(() -> stepLogic.runAgentDecisions(this));
                    t4.start();
                    break;
                }
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
        return intention.outputAction;
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
