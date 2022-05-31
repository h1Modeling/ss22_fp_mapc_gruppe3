package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.EisSender;
import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.agents.Desires.BDesires.ExploreDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.IDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ProcessEasyTaskDesire;
import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
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
                Action nextAction = intention.getNextAction();
                if (nextAction != null && !Navi.<INaviAgentV1>get().isWaitingOrBusy()) {
                    AgentLogger.info("Next Action for agent " + getName() + "is: " + nextAction);
                    eisSender.send(this, nextAction);
                    setIntention(null);
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
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Navi.<INaviAgentV1>get().updateAgentDebugData(getName(), supervisor.getName(), belief.getRoleName(), belief.getEnergy(), belief.getLastAction(), belief.getLastActionResult());
            //desireHandler.setNextAction();
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
            if (!merging) {
                Navi.<INaviAgentV1>get().acceptMerge(key, getName());
            } else {
                Navi.<INaviAgentV1>get().rejectMerge(key, getName());
            }
            merging = true;
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
            break;
        }
        default:
            throw new IllegalArgumentException("Message is not handled!");
        }
    }

    private void addBasicDesires() {
        desires.add(new ExploreDesire(belief, supervisor.getName(), getName()));
    }

    private void updateDesires() {
        // Test if already working on a simple task
        boolean workingOnSimpleTask = false;
        for (IDesire d : desires) {
            if (d.getName().equals(ProcessEasyTaskDesire.class.getSimpleName())) {
                workingOnSimpleTask = true;
                break;
            }
        }
        if (!workingOnSimpleTask) {
            for (TaskInfo info : belief.getTaskInfo()) {
                // Simple Task
                if (info.requirements.size() == 1) {
                    desires.add(new ProcessEasyTaskDesire(belief, info));
                    break;
                }
            }
        }
    }

    private void findIntention() {
        for (int i = desires.size() - 1; i >= 0; i--) {
            IDesire d = desires.get(i);
            if (!d.isFullfilled() && d.isExecutable()) {
                AgentLogger.info("Intention for agent " + getName() + " is " + d.getName());
                setIntention(d);
                break;
            }
        }
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
        Navi.<INaviAgentV1>get().updateMapAndPathfind(this.supervisor.getName(), this.getName(), index, position, vision, things, goalPoints,
                rolePoints, step, team, maxSteps, score, normsInfo, taskInfo);
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
