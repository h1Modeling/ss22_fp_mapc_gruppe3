package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.EisSender;
import de.feu.massim22.group3.MailService;
import de.feu.massim22.group3.TaskName;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import eis.iilang.Action;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.Thing;

public class BdiAgentV1 extends BdiAgent implements Runnable, Supervisable {
    
    private Queue<BdiAgentV1.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    private EisSender eisSender;
    private ISupervisor supervisor;
    private int index;
    
    public BdiAgentV1(String name, MailService mailbox, EisSender eisSender, int index) {
        super(name, mailbox);
        this.eisSender = eisSender;
        this.index = index;
        this.supervisor = new Supervisor(this);
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
            Action nextAction = intention.getNextAction();
            if (nextAction != null) {
                eisSender.send(this, nextAction);
                intention.clear();
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
                performTask(message.percept, message.sender);
            }	
        }
    }
    
    // TODO Add Agent Logic 
    private void performTask(Percept task, String sender) {
        String taskKey = task.getName();
        TaskName taskName = TaskName.valueOf(taskKey);
        switch (taskName) {
        case UPDATE:
            updatePercepts();
            setDummyAction();
            break;
        case TO_SUPERVISOR:
            this.supervisor.handleMessage(task, sender);
            break;
        case PATHFINDER_RESULT:
            List<Parameter> parameters = task.getParameters();
            belief.updateFromPathFinding(parameters);
            AgentLogger.info(belief.reachablesToString());
            // TODO Action after receiving Pathfinding infos
            break;
        default:
            throw new IllegalArgumentException("Message is not handled!");
        }
    }
    
    private void updatePercepts() {

        // Update Percepts
        List<Percept> percepts = getPercepts();
        belief.update(percepts);
        AgentLogger.info(belief.toString());
        
        // Update Navi
        Set<Thing> things = belief.getThings();
        List<Point> goalPoints = belief.getGoalZones();
        List<Point> rolePoints = belief.getRoleZones();
        Point position = belief.getPosition();
        int vision = belief.getVision();
        int step = belief.getStep();
        Navi.get().updateAgent(this.supervisor.getName(), this.getName(), index, position, vision, things, goalPoints, rolePoints, step);
    }
    
    private void setDummyAction() {
        // I need to think a lot
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String dir = "n";
        /*
        List<Point> roleZones = belief.getRoleZones();
        if (roleZones.size() > 0) {
            Point goal = roleZones.get(0);
            if (goal.x > 0) {
                dir = "e";
            }
            if (goal.x < 0) {
                dir = "w";
            }
            if (goal.y > 0) {
                dir = "s";
            }
        }
        */
        Action a = new Action("move", new Identifier(dir));
        /*
        if (roleZones.contains(new Point(0, 0))) {
            a = new Action("adopt", new Identifier("constructor"));
            if (belief.getRole().equals("constructor")) {
                a = new Action("adopt", new Identifier("default"));
            }
        }
        */
        intention.setNextAction(a);
    }
    
    private record PerceptMessage(String sender, Percept percept) {}

    @Override
    public void forwardMessageFromSupervisor(Percept message, String receiver, String sender) {
        this.sendMessage(message, receiver, sender);
    }

    @Override
    public void initSupervisorStep() {
        this.supervisor.initStep();
    }
}
