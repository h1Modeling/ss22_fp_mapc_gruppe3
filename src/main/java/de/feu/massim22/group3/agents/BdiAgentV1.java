package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.EisSender;
import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;
import de.feu.massim22.group3.agents.Desires.BDesires.BooleanInfo;
import de.feu.massim22.group3.agents.Desires.BDesires.DigFreeDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ExploreDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.FreedomDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.IDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.LooseWeightDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ProcessComplexTaskDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ProcessEasyTaskDesire;
import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.DesireDebugData;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

public class BdiAgentV1 extends BdiAgent<IDesire> implements Runnable, Supervisable {
    
    private Queue<BdiAgentV1.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    private EisSender eisSender;
    private ISupervisor supervisor;
    private int index;
    private boolean merging = false;
    //private DesireHandler desireHandler;

    private boolean test = false;
    
    public BdiAgentV1(String name, MailService mailbox, EisSender eisSender, int index) {
        super(name, mailbox);
        //desireHandler = new DesireHandler(this);
        this.eisSender = eisSender;
        this.index = index;
        this.supervisor = new Supervisor(this);
        addBasicDesires();
    }
    
    // Not needed for multi-threaded Agent 
    @Override
    public Action step() {
        return null;
    }

    @Override
    public void handlePercept(Percept percept) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleMessage(Percept message, String sender) {
        queue.add(new PerceptMessage(sender, message));
    }

    @Override
    public void run() {
        while (true) {

            // Send Action if already calculated
            if (intention != null) {
                ActionInfo info = intention.getNextActionInfo();
                if (info != null) {
                    Action nextAction = info.value();
                    if (nextAction != null && !Navi.<INaviAgentV1>get().isWaitingOrBusy()) {
                        AgentLogger.info("Next Action for agent " + getName() + "is: " + nextAction);
                        eisSender.send(this, nextAction);
                        belief.setLastActionIntention(info.info());
                        setIntention(null);
                    }
                }
            }

            // Read Messages
            if (queue.isEmpty()) {
                // Take a break
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Perform Task
                BdiAgentV1.PerceptMessage message = queue.poll();
                performEvent(message.percept, message.sender);
            }	
        }
    }
    
    // TODO Add Agent Logic 
    private void performEvent(Percept event, String sender) {
        String taskKey = event.getName();
        EventName taskName = EventName.valueOf(taskKey);
        switch (taskName) {
        case UPDATE:
            merging = false;
            updatePercepts();
            // Send Agent Report
            AgentReport report = belief.getAgentReport();
            supervisor.reportAgentData(getName(), report);
            supervisor.reportTasks(belief.getTaskInfo());
            Navi.<INaviAgentV1>get().updateAgentDebugData(getName(), supervisor.getName(), belief.getRoleName(), belief.getEnergy(), belief.getLastActionDebugString(), belief.getLastActionResult(), belief.getLastActionIntention());
            /*
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
             */  
            break;
        case TO_SUPERVISOR:
            this.supervisor.handleMessage(event, sender);
            break;
        case PATHFINDER_RESULT: {
            List<Parameter> parameters = event.getParameters();
            belief.updateFromPathFinding(parameters);
            AgentLogger.fine(belief.reachablesToString());
            updateDesires();
            findIntention();
            break;
        }
        case MERGE_SUGGESTION: {
            
            List<Parameter> parameters = event.getParameters();
            String key = ((Identifier)parameters.get(2)).getValue();
            // Allow only on merge per step
            if (!merging && !test) {
                //if (supervisor)
                Navi.<INaviAgentV1>get().acceptMerge(key, getName());
                merging = true;
            } else {
                Navi.<INaviAgentV1>get().rejectMerge(key, getName());
            }
            break;
        }
        case UPDATE_GROUP: {
            List<Parameter> parameters = event.getParameters();
            String newSupervisor = ((Identifier)parameters.get(0)).getValue();
            int offsetX = (int)((Numeral)parameters.get(1)).getValue();
            int offsetY = (int)((Numeral)parameters.get(2)).getValue();
            Point oldPosition = belief.getPosition();
            Point newPosition = new Point(oldPosition.x + offsetX, oldPosition.y + offsetY);
            belief.setPosition(newPosition);
            supervisor.setName(newSupervisor);
            break;
        }
        case ADD_GROUP_MEMBERS: {
            List<Parameter> parameters = event.getParameters();
            String agent = ((Identifier)parameters.get(0)).getValue();
            supervisor.addAgent(agent);
            test = false;
            break;
        }
        default:
            throw new IllegalArgumentException("Message is not handled!");
        }
    }

    private void addBasicDesires() {
        desires.add(new ExploreDesire(belief, supervisor.getName(), getName()));
        desires.add(new LooseWeightDesire(belief));
        desires.add(new DigFreeDesire(belief));
        desires.add(new FreedomDesire(belief));
    }

    private void updateDesires() {

        // Create new Task Desires
        for (TaskInfo info : belief.getNewTasks()) {
            // Simple Task
            if (info.requirements.size() == 1) {
                desires.add(new ProcessEasyTaskDesire(belief, info, supervisor.getName()));
            }
            if (info.requirements.size() > 1) {
                desires.add(new ProcessComplexTaskDesire(belief, info, supervisor.getName()));
            }
        }

        // Delete expired Desires
        desires.removeIf(d -> d.isUnfulfillable().value());

        // Sort Desires
        desires.sort((a, b) -> a.getPriority() - b.getPriority());
        
        AgentLogger.info(this.getName(), "sorted Desire List:");
        String desireString = "";
        for(IDesire desire : desires) {
            desireString += desire.getName() + " " + String.valueOf(desire.getPriority()) + "; ";
        }
        AgentLogger.info(this.getName(), desireString);
    }

    private void findIntention() {
        List<DesireDebugData> debugData = new ArrayList<>();
        for (int i = desires.size() - 1; i >= 0; i--) {
            IDesire d = desires.get(i);
            d.update(this.supervisor.getName());
            BooleanInfo isFullfilled = d.isFulfilled();
            BooleanInfo isExecutable = d.isExecutable();
            DesireDebugData data = new DesireDebugData(d.getName(), isExecutable);
            debugData.add(data);
            if (!isFullfilled.value() && isExecutable.value()) {
                AgentLogger.info("Intention for agent " + getName() + " is " + d.getName());
                setIntention(d);
                break;
            }
        }
        Navi.<INaviAgentV1>get().updateDesireDebugData(debugData, getName());
    }
    
    private void updatePercepts() {

        // Update Percepts
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        AgentLogger.fine(belief.toString());

        // Update Navi
        Set<Thing> things = belief.getThings();
        List<Point> goalPoints = belief.getGoalZones();
        List<Point> rolePoints = belief.getRoleZones();
        Point position = belief.getPosition();
        int vision = belief.getVision();
        int step = belief.getStep();
        String team = belief.getTeam();
        int maxSteps = belief.getSteps();
        int score = (int)belief.getScore();
        Set<NormInfo> normsInfo = belief.getNormsInfo(); 
        Set<TaskInfo> taskInfo = belief.getTaskInfo();
        List<Point> attachedThings = belief.getAttachedPoints();
        Navi.<INaviAgentV1>get().updateMapAndPathfind(this.supervisor.getName(), this.getName(), index, position, vision, things, goalPoints,
                rolePoints, step, team, maxSteps, score, normsInfo, taskInfo, attachedThings);
    }

    private record PerceptMessage(String sender, Percept percept) {
    }

    @Override
    public void forwardMessageFromSupervisor(Percept message, String receiver, String sender) {
        this.sendMessage(message, receiver, sender);
    }

    @Override
    public void initSupervisorStep() {
        this.supervisor.initStep();
    }
}
