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
import de.feu.massim22.group3.agents.Desires.BDesires.DeliverAndConnectBlockDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.DeliverBlockDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.DigFreeDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ExploreDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.FreedomDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.GetBlockDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
import de.feu.massim22.group3.agents.Desires.BDesires.IDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.LooseWeightDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ProcessEasyTaskDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ReceiveAndConnectBlockDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.ReceiveBlockDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.WalkByGetRoleDesire;
import de.feu.massim22.group3.EventName;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.PerceptUtil;
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
import massim.protocol.messages.scenario.Actions;

public class BdiAgentV1 extends BdiAgent<IDesire> implements Runnable, Supervisable {
    
    private Queue<BdiAgentV1.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    private EisSender eisSender;
    private ISupervisor supervisor;
    private int index;
    private boolean merging = false;
    
    public BdiAgentV1(String name, MailService mailbox, EisSender eisSender, int index) {
        super(name, mailbox);
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
                        // If Attach ask supervisor for permission to avoid connection to other agent
                        if (nextAction.getName().equals(Actions.ATTACH)) {
                            Point position = belief.getPosition();
                            var parameters = nextAction.getParameters();
                            if (parameters.size() > 0) {
                                String direction = PerceptUtil.toStr(parameters, 0);
                                Point offset = DirectionUtil.getCellInDirection(direction);
                                Point p = new Point(position.x + offset.x, position.y + offset.y);
                                supervisor.askForAttachPermission(getName(), p, direction);
                            }
                        } else {
                            AgentLogger.info("Next Action for agent " + getName() + "is: " + nextAction);
                            eisSender.send(this, nextAction);
                            belief.setLastActionIntention(info.info());
                            setIntention(null);
                        }
                    }
                }
            }

            // Read Messages
            if (queue.isEmpty()) {
                // Take a break
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // Perform Task
                BdiAgentV1.PerceptMessage message = queue.poll();
                performEvent(message.percept, message.sender);
            }

            // Test for Sim End
            if (belief.isSimEnd()) {
                // Reset Supervisor
                this.supervisor = new Supervisor(this);
                // Reset Desires
                desires = new ArrayList<>();
                addBasicDesires();
                // Reset Navi
                String team = belief.getTeam();
                Navi.<INaviAgentV1>get().resetAgent(getName(), team);
            }
        }
    }
    
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
            Navi.<INaviAgentV1>get().updateAgentDebugData(getName(), supervisor.getName(), belief.getRoleName(), belief.getEnergy(),
                    belief.getLastActionDebugString(), belief.getLastActionResult(), belief.getLastActionIntention(), belief.getGroupDesireType());
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
            break;
        }
        case SUPERVISOR_PERCEPT_DELIVER_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.TASK);
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            TaskInfo taskInfo = belief.getTask(task);
            Thing block = taskInfo.requirements.get(0);
            desires.add(new DeliverBlockDesire(belief, block, supervisor.getName(), agent, this));
            break;
        }
        case SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE: {
            // Remove group desire if teammate has finished or canceled their group desire
            String desireName = ReceiveBlockDesire.class.getSimpleName();
            desires.removeIf(d -> d.getName().equals(desireName));
            break;
        }
        case SUPERVISOR_PERCEPT_DONE_OR_CANCELED: {
            desires.removeIf(d -> d.isGroupDesire());
            break;
        }
        case SUPERVISOR_PERCEPT_RECEIVE_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.TASK);
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            TaskInfo taskInfo = belief.getTask(task);
            desires.add(new ReceiveBlockDesire(belief, taskInfo, agent, getName(), supervisor.getName()));
            break;
        }
        case SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.DELIVER_ATTACH);
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            String agentFullName = PerceptUtil.toStr(parameters, 2);
            TaskInfo taskInfo = belief.getTask(task);
            Thing block = null;
            for (Thing t : taskInfo.requirements) {
                if (Math.abs(t.x) + Math.abs(t.y) > 1) {
                    block = t;
                    break;
                }
            }
            if (block != null) {
                desires.add(new DeliverAndConnectBlockDesire(belief, taskInfo, agent, agentFullName, supervisor.getName(), block, this));
            }
            break;
        }
        case SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.RECEIVE_ATTACH);
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            String agentFullName = PerceptUtil.toStr(parameters, 2);
            TaskInfo taskInfo = belief.getTask(task);
            Thing block = null;
            for (Thing t : taskInfo.requirements) {
                if (Math.abs(t.x) + Math.abs(t.y) == 1) {
                    block = t;
                    break;
                }
            }
            if (block != null) {
                desires.add(new ReceiveAndConnectBlockDesire(belief, taskInfo, agent, agentFullName, supervisor.getName(), block, this));
            }
            break;
        }
        case SUPERVISOR_PERCEPT_GET_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.GET_BLOCK);
            List<Parameter> parameters = event.getParameters();
            String block = PerceptUtil.toStr(parameters, 0);
            desires.add(new GetBlockDesire(belief, block, supervisor.getName()));
            break;         
        }
        case REPORT_POSSIBLE_CONNECTION: {
            belief.addPossibleConnection(event);
            break;
        }
        case ATTACH_REPLY: {
            List<Parameter> parameters = event.getParameters();
            boolean result = PerceptUtil.toBool(parameters, 0);
            String direction = PerceptUtil.toStr(parameters, 1);
            ActionInfo a = result 
                ? ActionInfo.ATTACH(direction, "")
                : ActionInfo.SKIP("");
            eisSender.send(this, a.value());
            belief.setLastActionIntention(a.info());
            setIntention(null);
            break;
        }
        default:
            throw new IllegalArgumentException("Message is not handled: " + taskName);
        }
    }

    private void addBasicDesires() {
        desires.add(new ExploreDesire(belief, supervisor.getName(), getName()));
        desires.add(new LooseWeightDesire(belief));
        desires.add(new DigFreeDesire(belief));
        desires.add(new FreedomDesire(belief));
        // TODO remove / modify if sim roles change
        String[] actions = {"request", "attach", "connect", "disconnect", "submit"};
        desires.add(new WalkByGetRoleDesire(belief, actions));
    }

    private void updateDesires() {

        // Create new Task Desires
        for (TaskInfo info : belief.getTaskInfo()) {
            // Simple Task
            if (info.requirements.size() == 1) {
                boolean alreadyAdded = false;
                for (IDesire d : desires) {
                    if (d instanceof ProcessEasyTaskDesire) {
                        ProcessEasyTaskDesire p = (ProcessEasyTaskDesire)d;
                        if (info.equals(p.getTaskInfo())) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                }
                if (!alreadyAdded) {
                    desires.add(new ProcessEasyTaskDesire(belief, info, supervisor.getName()));
                }
            }
        }

        // Delete expired Desires
        desires.removeIf(d -> d.isUnfulfillable().value() || (d.isGroupDesire() && d.isFulfilled().value()));

        // Sort Desires
        desires.sort((a, b) -> a.getPriority() - b.getPriority());

        // Set group Desire flag
        if (!hasGroupDesire()) {
            belief.setGroupDesireType(GroupDesireTypes.NONE);
        }
    }

    private boolean hasGroupDesire() {
        for (IDesire d : desires) {
            if (d.isGroupDesire()) {
                return true;
            }
        }
        return false;
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
        List<Point> attachedThings = belief.getOwnAttachedPoints();
        Navi.<INaviAgentV1>get().updateMapAndPathfind(this.supervisor.getName(), this.getName(), index, position, vision, things, goalPoints,
                rolePoints, step, team, maxSteps, score, normsInfo, taskInfo, attachedThings);
    }

    private record PerceptMessage(String sender, Percept percept) {
    }

    @Override
    public synchronized void forwardMessage(Percept message, String receiver, String sender) {
        this.sendMessage(message, receiver, sender);
    }

    @Override
    public void initSupervisorStep() {
        int step = belief.getStep();
        this.supervisor.initStep(step);
    }
}
