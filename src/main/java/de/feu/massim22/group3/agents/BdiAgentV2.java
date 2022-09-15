package de.feu.massim22.group3.agents;

import eis.iilang.*;

//import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.desires.IDesire;
import de.feu.massim22.group3.agents.desires.desiresV2.DisconnectMultiBlocksDesire;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.agents.utilsV2.*;
import de.feu.massim22.group3.agents.utilsV2.AgentCooperations.Cooperation;
import de.feu.massim22.group3.agents.utilsV2.AgentMeetings.Meeting;
import de.feu.massim22.group3.communication.MailService;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;
import massim.protocol.messages.scenario.Actions;
import de.feu.massim22.group3.map.INaviAgentV2;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * The class <code>BdiAgentV2</code> defines an agent implementation of group 3 in the massim agent contest 2022.
 * The class is one variant out of two implementations of the group. The other implementation is <code>BdiAgentV1</code>.
 * Please be aware, that <code>BdiAgentV2</code> is no successor of <code>BdiAgentV1</code>. Both implementations
 * define a separate approach and are not connected to each other.
 * 
 * @see BdiAgentV1
 * @author Melinda Betz
 */

public class BdiAgentV2 extends BdiAgent implements Supervisable {
    private boolean absolutePositions = false;
    private Point startPosition = new Point(Point.zero());
    private List<Thing> attachedThings = new ArrayList<Thing>();
    
    /**The DesireUtilities */
    public DesireUtilities desireProcessing = new DesireUtilities();    
    
    /** If all the beliefs of a agent are updated.*/
    public boolean beliefsDone;
    
    /** If all the decisions are done (task dependent /task independent).*/
    public boolean decisionsDone;
    
    /** If there ever was a request ( to attach / get a block) made by a certain agent.*/
    public boolean requestMade = false;
    
    /** If a agent is busy with a task.*/
    public boolean isBusy = false;
    
    /**The agent walks right to his target.*/
    public boolean alwaysToTarget = false;
    
    /** If a agent has a block attached.*/
    public boolean blockAttached = false;
    
    /** List of all the attached points.*/
    public List<Point> attachedPoints = new ArrayList<Point>();
    
    /** The number of the step where the agent has last detached a block.*/
    public int lastStepDetach = 0;
    
      
    /** The direction transformed into the right coordinates via modulo.*/
    public int exploreDirection = this.index % 4;
    
    /** The second direction transformed into the right coordinates via modulo.*/
    public int exploreDirection2 = exploreDirection + 1;
    
    
    /** The supervisor.*/
    public Supervisor supervisor;
    
    /** The index.*/
    public int index;
    /** Array of all firstM meetings from agents.*/
    public Meeting[] firstMeeting = new Meeting[11];
    
    
    /**A set of all reachable goal zones.*/
    public Set<java.awt.Point> rgz = new HashSet<>();
    
    /** A set of all reachable dispensers.*/
    public Set<Thing> disp = new HashSet<>();

    /**
     * Initializes a new Instance of BdiAgentV2.
     * 
     * @param name - the name of the agent
     * @param mailbox - the mail service of the agent
     * @param index - the index of the agent in the agent team
     */
    public BdiAgentV2(String name, MailService mailbox, int index) {
        super(name, mailbox);
        this.index = index;
        this.supervisor = new Supervisor(this);
        StepUtilities.allAgents.add(this);
        StepUtilities.allSupervisors.add((Supervisor)this.supervisor);
    }

    /**
     * All that happens in one step.( Complete agent processing per step)
     * 
     * @return the action for that step
     */
    @Override
    public Action step() {
        AgentLogger.info(Thread.currentThread().getName() + " step() Start in neuem Thread - Step: " + (belief.getStep()+1) + " , Agent: " + this.getName());
        desires = new ArrayList<IDesire>();
        updateBeliefs();
        supervisor.setDecisionsDone(false);
        decisionsDone = false; // Agent
        beliefsDone = false; // Agent
        desireProcessing.moveIteration = 0;

        AgentLogger.info(Thread.currentThread().getName() + " step() Start in neuem Thread - Step: " + belief.getStep() + " , Agent: " + this.getName());
        // map update with updateAgent (without startCalculating?)
        updateMap();

        // If the last agent is done then do Group processing 
        if (StepUtilities.reportMapUpdate(this, belief.getStep(), belief.getTeamSize())) {
           Thread t2 = new Thread(() -> new StepUtilities(new DesireUtilities()).doGroupProcessing(belief.getStep()));
            t2.start();
        }

        // wating for PATHFINDER_RESULT 
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

        // wating for decisions done (agent and supervisor)
        AgentLogger.info(Thread.currentThread().getName() + " step() Waiting for decisionsDone - Step: " + belief.getStep() + " , Agent: " + this.getName());
        
        while (true) {
            if (!(decisionsDone && supervisor.getDecisionsDone())) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // tasks expired?
                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.master().equals(this)) {
                        AgentLogger.info(Thread.currentThread().getName() + " step() coop: " + coop.toString());

                        if (desireProcessing.taskReachedDeadline(this, coop.task())) {
                            AgentLogger.info(Thread.currentThread().getName() + " step() task reached deadline ");

                            if (coop.statusMaster().equals(Status.Connected) 
                                    && desireProcessing.doDecision(this, new DisconnectMultiBlocksDesire(coop.task(), this))) {
                                AgentLogger.info(Thread.currentThread().getName() + " Desire added - Agent: "
                                        + this.getName() + " , DisconnectMultiBlocksDesire , Action: "
                                        + this.getDesires().get(this.getDesires().size() - 1).getOutputAction().getName()
                                        + " , Parameter: "
                                        + this.getDesires().get(this.getDesires().size() - 1).getOutputAction().getParameters()
                                        + " , Task: " + coop.task().name + " , Prio: " + desireProcessing
                                                .getPriority(this.getDesires().get(this.getDesires().size() - 1), this));
                            } else
                                AgentLogger.info(Thread.currentThread().getName() + " Desire not added - Agent: "
                                        + this.getName() + " , DisconnectMultiBlocksDesire");
                            
                            AgentCooperations.remove(coop);
                        }
                    }
                }
                
                // Delete expired Desires
                desires.removeIf(d -> d.isUnfulfillable().value());

                // Intention ermitteln (Desire mit höchster Priorität)
                intention = desireProcessing.determineIntention(this);
                break;
            }
        }
        
        // next Action
        AgentLogger.info(Thread.currentThread().getName() + " step() End - Step: " + belief.getStep() + " , Agent: " + this.getName() 
        + " , Intention: " + intention.getName() + " , Action: " +  intention.getOutputAction().getName() 
        + " , Params: " +  intention.getOutputAction().getParameters());
        return intention.getOutputAction();
    }

    private void updateBeliefs() {
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        belief.updatePositionFromExternal();
        
        AgentLogger.info(Thread.currentThread().getName() + " updateBeliefs() , Agent: " + this.getName()
        + " , Position: " + belief.getPosition()
        + " , absolute Position: " 
        + ((Point.castToPoint(belief.getAbsolutePosition()) != null) ? Point.castToPoint(belief.getAbsolutePosition()) : ""));
        
        if (belief.getStep() == 0) {
            AgentLogger.info(Thread.currentThread().getName()
                    + " step() updateBeliefs - getAbsolutePosition() false - startPosition: " + startPosition
                    + " , absolute startPosition: "
                    + ((Point.castToPoint(belief.getAbsolutePosition()) != null)
                            ? Point.castToPoint(belief.getAbsolutePosition())
                            : ""));
        }
        
        if (belief.getStep() == 0) {
            if (absolutePositions) {
                if (Point.castToPoint(belief.getAbsolutePosition()) != null) {
                    startPosition = Point.castToPoint(belief.getAbsolutePosition());
                    belief.setPosition(startPosition);
                    
                    AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - getAbsolutePosition() true, dyn - startPosition: " 
                            + startPosition);
                }         
            } else {
                AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - getAbsolutePosition() false - startPosition: " 
                        + startPosition + " , absolute startPosition: " 
                        + ((Point.castToPoint(belief.getAbsolutePosition()) != null) ? Point.castToPoint(belief.getAbsolutePosition()) : ""));
            }
        }
        
        //AgentLogger.info(Thread.currentThread().getName() + " updateBeliefs() AA , Agent: " + this.getName());   
        if (belief.getLastAction() != null) {
            if (belief.getLastAction().equals(Actions.ROTATE)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                if (blockAttached) {
                    Point p = null;
                    
                    if (belief.getLastActionParams().get(0).equals("cw")) {
                        p = this.desireProcessing.getCRotatedPoint(attachedPoints.get(0));
                    } else {
                        p = this.desireProcessing.getCCRotatedPoint(attachedPoints.get(0));                       
                    }  
                    
                    clearAttached();
                    Thing t = belief.getThingWithTypeAt(p, Thing.TYPE_BLOCK);
                    
                    if (t != null) {
                        attachedThings.add(t);
                        attachedPoints.add(p);
                    }
                }
            }
            //AgentLogger.info(Thread.currentThread().getName() + " updateBeliefs() BB , Agent: " + this.getName());             
            if (belief.getLastAction().equals(Actions.ATTACH)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                Thing t = belief.getThingWithTypeAt(belief.getLastActionParams().get(0), Thing.TYPE_BLOCK);
                
                if (t != null) {
                    blockAttached = true;
                    attachedThings.add(t);
                    attachedPoints.add(new Point(t.x, t.y));
                    StepUtilities.attachedBlock[index] = t.details;
                }
            }

            if (belief.getLastAction().equals(Actions.DETACH)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                blockAttached = false;
                clearAttached();
                StepUtilities.attachedBlock[index] = "";
                lastStepDetach = belief.getStep();
                
                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.helper().equals(this)) {
                        AgentCooperations.setStatusHelper(coop.task(), coop.helper(), Status.Detached);
                        alwaysToTarget = false;
                        isBusy = false;
                    }
                    
                    if ((coop.helper2() != null && coop.helper2().equals(this))) {
                        AgentCooperations.setStatusHelper2(coop.task(), coop.helper2(), Status.Detached);
                        alwaysToTarget = false;
                        isBusy = false;
                    }
                }
            }
            //AgentLogger.info(Thread.currentThread().getName() + " updateBeliefs() CC , Agent: " + this.getName()); 
            if (belief.getLastAction().equals(Actions.SUBMIT)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                blockAttached = false;
                clearAttached();
                StepUtilities.attachedBlock[index] = "";

                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);

                    if (coop.master().equals(this) && coop.task().name.equals(belief.getLastActionParams().get(0))) { 
                        AgentCooperations.setStatusMaster(coop.task(), coop.master(), Status.Submitted);
                        AgentCooperations.remove(coop);
                        alwaysToTarget = false;
                        isBusy = false;
                    }
                }
                
                if (belief.getLastActionParams().size() == 2) {
                    AgentCooperations.setScore(Integer.parseInt(belief.getLastActionParams().get(1)));
                    AgentLogger.info(Thread.currentThread().getName() + " Step: " + belief.getStep()
                    + " , Scores: " + AgentCooperations.getScore(1)
                            + " , " + AgentCooperations.getScore(2)
                                    + " , " + AgentCooperations.getScore(3)); 
                }
            }

            AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - belief.getLastAction(): " 
            + belief.getLastAction() + " , " + belief.getLastActionResult() + " , Agent: " + this.getName());

            if (belief.getLastAction().equals(Actions.CONNECT)
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                if (AgentCooperations.exists(this)) {
                    Cooperation coop = AgentCooperations.get(this);
                    AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - coop: " + coop);
                    
                    if (coop.master().getName().equals(this.getName())) {
                        AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - master");
                        AgentCooperations.setStatusMaster(coop.task(), coop.master(), Status.Connected);
                    }
                    
                    if (coop.helper().getName().equals(this.getName())) {
                        AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - helper");
                        AgentCooperations.setStatusHelper(coop.task(), coop.helper(), Status.Connected);
                    }
                    
                    if ((coop.helper2() != null && coop.helper2().getName().equals(this.getName()))) {
                        AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - helper2");
                        AgentCooperations.setStatusHelper2(coop.task(), coop.helper2(), Status.Connected);
                    }
                    coop = AgentCooperations.get(this);
                    AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - coop: " + coop);
                }
            }
        }
        
        
        
        AgentLogger.info(Thread.currentThread().getName() + " step() updateBeliefs - blockAttached: " + blockAttached  + " isBusy: " + isBusy 
                + " , Agent: " + this.getName()+ " , Step: " + belief.getStep() + " , attBlocks: " + StepUtilities.getAttachedBlocks());    
    }

    /**
     * Updates the map.
     * 
     */
    public void updateMap() {
        Navi.<INaviAgentV2>get().updateMap(supervisor.getName(), getName(), index,
                belief.getPosition(), belief.getVision(), belief.getThings(),
                belief.getGoalZones(), belief.getRoleZones(), belief.getStep(),
                belief.getTeam(), belief.getSteps(),  (int)belief.getScore(), belief.getNormsInfo(),
                belief.getTaskInfo(), belief.getAttachedPoints());
    }
    
    /**
     * Gets a list of all attached things.
     * 
     * @return list of all attached things
     */
    public List<Thing> getAttachedThings() {
        return attachedThings;
    }

    /**
     * Gets a list of all attached points.
     * 
     * @return list of all attached points
     */
    public List<Point> getAttachedPoints() {     
        return attachedPoints;
    }
        
    /**
     * Refreshes all attached things and points.
     * 
     */
    public void clearAttached() {
        attachedPoints = new ArrayList<Point>();
        attachedThings = new ArrayList<Thing>();
    }

    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public void initSupervisorStep() {
    }

    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public void forwardMessage(Percept message, String receiver, String sender) {
    }

    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public void handlePercept(Percept percept) {
    }

    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public void handleMessage(Percept message, String sender) {
    }
}
