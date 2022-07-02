package de.feu.massim22.group3.agents;

import eis.iilang.*;
import massim.protocol.data.Thing;

import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import de.feu.massim22.group3.*;
import de.feu.massim22.group3.agents.Desires.BDesires.IDesire;
import de.feu.massim22.group3.map.INaviAgentV2;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * An agent that uses the step()-Method.
 */
public class BdiAgentV2 extends BdiAgent<IDesire> implements Supervisable {

    public DesireUtilities desireProcessing = new DesireUtilities();
    //public StepUtilities stepLogic = new StepUtilities(desireProcessing);
    
    public boolean decisionsDone;
    public boolean requestMade = false;
    public Point lastUsedDispenser;
    
    public Supervisor supervisor;
    public int index;
    
    //public IDesire intention;
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
        updateMap();

        // Wenn es der letzte Agent war kommt die Gruppenverarbeitung
        if (StepUtilities.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
           Thread t2 = new Thread(() -> new StepUtilities(new DesireUtilities()).doGroupProcessing(belief.getStep()));
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
        AgentLogger.info(Thread.currentThread().getName() + " step() End - Step: " + belief.getStep() + " , Agent: " + this.getName() 
        + " , Intention: " + intention.getName() + " , Action: " +  intention.getOutputAction().getName() 
        + " , Params: " +  intention.getOutputAction().getParameters());
        return intention.getOutputAction();
    }

    /**
     * Update all the beliefs.
     *
     */
    private void updateBeliefs() {
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        
        for (Percept percept : percepts) {
            if (percept.getName() == "attached"){
            AgentLogger.info(this.getName(),
                    "Percept - attached: " +
                    String.format("%s - %s", percept.getName(), percept.getParameters()));
            }
            
            if (percept.getName() == "goalZone"){
                AgentLogger.info(this.getName(),
                        "Percept - GoalZone: " +
                        String.format("%s - %s", percept.getName(), percept.getParameters()));
                }
        }
        //AgentLogger.info(belief.toString());
    }

    /**
     * Update the Map.
     * 
     * @param agent - the Agent that wants to update the map
     *
     */
    public void updateMap() {
        Navi.<INaviAgentV2>get().updateMap(supervisor.getName(), getName(), index,
                belief.getPosition(), belief.getVision(), belief.getThings(),
                belief.getGoalZones(), belief.getRoleZones(), belief.getStep(),
                belief.getTeam(), belief.getSteps(),  (int)belief.getScore(), belief.getNormsInfo(),
                belief.getTaskInfo(), belief.getAttachedPoints());
    }
    
    public List<Thing> getAttachedThings() {
        List<Thing> attachedThings = new ArrayList<Thing>();
        
        for (Thing attachedThing : belief.getAttachedThings()) {
            if ((attachedThing.type.equals(Thing.TYPE_BLOCK) || attachedThing.type.equals(Thing.TYPE_DISPENSER)) 
                    && (attachedThing.x == 0
                    || attachedThing.y == 0)) {
                attachedThings.add(attachedThing);
            }
        }
        
        return attachedThings;
    }

    public List<Point> getAttachedPoints() {
        List<Point> attachedPoints = new ArrayList<Point>();
        
        for (Point attachedPoint : belief.getAttachedPoints()) {
            if (attachedPoint.x == 0 || attachedPoint.y == 0) {
                attachedPoints.add(attachedPoint);
            }
        }
        
        return attachedPoints;
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
