package de.feu.massim22.group3.agents;

import eis.iilang.*;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;

//import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import de.feu.massim22.group3.*;
import de.feu.massim22.group3.agents.*;
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
    public boolean connected = false;
    public boolean blockAttached = false;
    public boolean beliefsDone;
    
    public Point lastUsedDispenser;
    public List<Thing> attachedThings = new ArrayList<Thing>();
    public List<Point> attachedPoints = new ArrayList<Point>();
    
    public int exploreCount = 0;    
    public int exploreDirection = this.index % 4;
    public int exploreDirection2 = exploreDirection + 1;
    
    public Supervisor supervisor;
    public int index;


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
        belief.updatePositionFromExternal();
        refreshAttached();
   
        if (belief.getLastAction() != null) {
            if (belief.getLastAction().equals(Actions.ATTACH)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                blockAttached = true;
            }

            if (belief.getLastAction().equals(Actions.DETACH)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                blockAttached = false;
                
                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.helper().equals(this))
                        AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                                coop.statusMaster(), coop.helper(), Status.Detached));
                }
            }

            if (belief.getLastAction().equals(Actions.SUBMIT)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                blockAttached = false;

                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.master().equals(this) && coop.task().name.equals(belief.getLastActionParams().get(0))) 
                        AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                                Status.Submitted, coop.helper(), coop.statusHelper()));
                }
            }
            
            if (belief.getLastAction().equals(Actions.CONNECT)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.master().equals(this))
                        AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                                Status.Connected, coop.helper(), coop.statusHelper()));
                    else
                        AgentCooperations.setCooperation(new AgentCooperations.Cooperation(coop.task(), coop.master(),
                                coop.statusMaster(), coop.helper(), Status.Connected));
                }
            }
        }
        
        
        
        AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - blockAttached: " + blockAttached  + " , Agent: " + this.getName()+ " , Step: " + belief.getStep());
        
        for (Percept percept : percepts) {
            if (percept.getName() == "attached"){
            AgentLogger.info(this.getName(),
                    "Percept - attached: " +
                    String.format("%s - %s", percept.getName(), percept.getParameters()));
            }
        }
        //AgentLogger.info(belief.toString());
    }

    /**
     * Update the Map.
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
        return attachedThings;
    }

    public List<Point> getAttachedPoints() {     
        return attachedPoints;
    }
    
    public void refreshAttached() {
        attachedPoints = new ArrayList<Point>();
        attachedThings = new ArrayList<Thing>();
        
        for (java.awt.Point p : belief.getAttachedPoints()) {
            Point attachedPoint = Point.castToPoint(p);
            
            if ((attachedPoint.x == 0 || attachedPoint.y == 0)
                && Math.abs(attachedPoint.x) <= 1
                && Math.abs(attachedPoint.y) <= 1) {
               
                for (Thing t : belief.getThings()) {
                    if (t.type.equals(Thing.TYPE_BLOCK) 
                            && t.x == attachedPoint.x 
                            && t.y == attachedPoint.y) {
                        attachedPoints.add(attachedPoint);
                        attachedThings.add(t); 
                    }                   
                }
            }
        }
    }

    @Override
    public void initSupervisorStep() {
    }

    @Override
    public void forwardMessage(Percept message, String receiver, String sender) {
    }

    @Override
    public void handlePercept(Percept percept) {
    }

    @Override
    public void handleMessage(Percept message, String sender) {
    }
}
