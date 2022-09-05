package de.feu.massim22.group3.agents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.feu.massim22.group3.agents.desires.ActionInfo;
import de.feu.massim22.group3.agents.desires.BooleanInfo;
import de.feu.massim22.group3.agents.desires.DeliverAndConnectBlockDesire;
import de.feu.massim22.group3.agents.desires.DeliverBlockDesire;
import de.feu.massim22.group3.agents.desires.DigFreeDesire;
import de.feu.massim22.group3.agents.desires.EscapeClearDesire;
import de.feu.massim22.group3.agents.desires.ExploreDesire;
import de.feu.massim22.group3.agents.desires.ExploreMapSizeDesire;
import de.feu.massim22.group3.agents.desires.FreedomDesire;
import de.feu.massim22.group3.agents.desires.GetBlockDesire;
import de.feu.massim22.group3.agents.desires.GroupDesireTypes;
import de.feu.massim22.group3.agents.desires.IDesire;
import de.feu.massim22.group3.agents.desires.LooseWeightDesire;
import de.feu.massim22.group3.agents.desires.MeasureMapDesire;
import de.feu.massim22.group3.agents.desires.MeasureMoveDesire;
import de.feu.massim22.group3.agents.desires.ProcessEasyTaskDesire;
import de.feu.massim22.group3.agents.desires.ReceiveAndConnectBlockDesire;
import de.feu.massim22.group3.agents.desires.ReceiveBlockDesire;
import de.feu.massim22.group3.agents.desires.WaitNearGoalZoneDesire;
import de.feu.massim22.group3.agents.desires.WalkByGetRoleDesire;
import de.feu.massim22.group3.agents.desires.GuardGoalZoneDesire;
import de.feu.massim22.group3.agents.desires.GuardDispenserDesire;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.supervisor.AgentReport;
import de.feu.massim22.group3.agents.supervisor.ISupervisor;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.communication.EisSender;
import de.feu.massim22.group3.communication.MailService;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.utils.PerceptUtil;
import de.feu.massim22.group3.utils.debugger.debugData.DesireDebugData;
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
import static java.util.Objects.isNull;
/**
 * The class <code>BdiAgentV1</code> defines an agent implementation of group 3 in the massim agent contest 2022.
 * The class is one variant out of two implementations of the group. The other implementation is <code>BdiAgentV2</code>.
 * Please be aware, that <code>BdiAgentV1</code> is no predecessor of <code>BdiAgentV2</code>. Both implementations
 * define a separate approach and are not connected to each other.
 * 
 * @see BdiAgentV2
 * @author Heinz Stadler
 */
public class BdiAgentV1 extends BdiAgent<IDesire> implements Runnable, Supervisable {
    
    private Queue<BdiAgentV1.PerceptMessage> queue = new ConcurrentLinkedQueue<>();
    private EisSender eisSender;
    private ISupervisor supervisor;
    private int index;
    private boolean merging = false;

    private boolean didSendMeasurementStarted = false;
    
    /**
     * Instantiates a new Instance of BdiAgentV1.
     * 
     * @param name the name of the agent
     * @param mailbox the mail service of the agent
     * @param eisSender the object which sends the calculated action to the server
     * @param index the index of the agent in the agent team
     */

    public BdiAgentV1(String name, MailService mailbox, EisSender eisSender, int index) {
        super(name, mailbox);
        this.eisSender = eisSender;
        this.index = index;
        this.supervisor = new Supervisor(this);
        addBasicDesires();
    }
    
    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public Action step() {
        return null;
    }

    /**
     * This method functionality is not implemented in the agent and should not be used.
     * @deprecated
     */
    @Override
    public void handlePercept(Percept percept) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(Percept message, String sender) {
        queue.add(new PerceptMessage(sender, message));
    }

    /**
     * Permanently checks for new messages, handles them and calculates the current intention of the agent. 
     */
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
                    Thread.sleep(10);
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
                    belief.getLastActionDebugString(), belief.getLastActionResult(), belief.getLastActionIntention(), belief.getGroupDesireType(),
                    belief.getGroupDesirePartner(), belief.getGroupDesireBlockDetail(), belief.getAttachedThingsDebugString());
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
            belief.setGroupDesirePartner(agent);
            belief.setGroupDesireBlockDetail(block.type);
            desires.add(new DeliverBlockDesire(belief, block, supervisor.getName(), agent, this));
            break;
        }
        case SUPERVISOR_PERCEPT_DELIVER_BLOCK_DONE: {
            // Remove group desire if teammate has finished or canceled their group desire
            String desireName = ReceiveBlockDesire.class.getSimpleName();
            belief.setGroupDesireBlockDetail("");
            belief.setGroupDesirePartner("");
            desires.removeIf(d -> d.getName().equals(desireName));
            break;
        }
        case SUPERVISOR_PERCEPT_DONE_OR_CANCELED: {
            belief.setGroupDesireBlockDetail("");
            belief.setGroupDesirePartner("");
            desires.removeIf(d -> d.isGroupDesire());
            break;
        }
        case SUPERVISOR_PERCEPT_RECEIVE_BLOCK: {
            belief.setGroupDesireType(GroupDesireTypes.TASK);
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            TaskInfo taskInfo = belief.getTask(task);
            belief.setGroupDesirePartner(agent);
            desires.add(new ReceiveBlockDesire(belief, taskInfo, agent, supervisor.getName()));
            break;
        }
        case SUPERVISOR_PERCEPT_DELIVER_TWO_BLOCK: {
            List<Parameter> parameters = event.getParameters();
            String task = PerceptUtil.toStr(parameters, 0);
            String agent = PerceptUtil.toStr(parameters, 1);
            String agentFullName = PerceptUtil.toStr(parameters, 2);
            TaskInfo taskInfo = belief.getTask(task);
            Thing block = null;
            if (taskInfo != null) {
                for (Thing t : taskInfo.requirements) {
                    if (Math.abs(t.x) + Math.abs(t.y) > 1) {
                        block = t;
                        break;
                    }
                }
                if (block != null) {
                    belief.setGroupDesireType(GroupDesireTypes.DELIVER_ATTACH);
                    belief.setGroupDesirePartner(agent);
                    belief.setGroupDesireBlockDetail(block.type);
                    desires.add(new DeliverAndConnectBlockDesire(belief, taskInfo, agent, agentFullName, supervisor.getName(), block, this));
                }
            }
            break;
        }
        case SUPERVISOR_PERCEPT_RECEIVE_TWO_BLOCK: {
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
                belief.setGroupDesireType(GroupDesireTypes.RECEIVE_ATTACH);
                belief.setGroupDesirePartner(agent);
                belief.setGroupDesireBlockDetail(block.type);
                desires.add(new ReceiveAndConnectBlockDesire(belief, taskInfo, agent, agentFullName, supervisor.getName(), block, this));
            }
            break;
        }
        case SUPERVISOR_PERCEPT_GET_BLOCK: {
            List<Parameter> parameters = event.getParameters();
            String block = PerceptUtil.toStr(parameters, 0);
            belief.setGroupDesireType(GroupDesireTypes.GET_BLOCK);
            belief.setGroupDesireBlockDetail(block);
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
        case SUPERVISOR_PERCEPT_GUARD_GOAL_ZONE: {
            belief.setGroupDesireType(GroupDesireTypes.GUARD_GOAL_ZONE);
            List<Parameter> parameters = event.getParameters();
            int pointX_gz = PerceptUtil.toNumber(parameters, 0, Integer.class);
            int pointY_gz = PerceptUtil.toNumber(parameters, 1, Integer.class);
            desires.add(new GuardGoalZoneDesire(belief, new Point(pointX_gz, pointY_gz),
                    supervisor.getName()));
            break;
        }
        case SUPERVISOR_PERCEPT_GUARD_DISPENSER: {
            belief.setGroupDesireType(GroupDesireTypes.GUARD_DISPENSER);
            List<Parameter> parameters = event.getParameters();
            String block = PerceptUtil.toStr(parameters, 0);
            desires.add(new GuardDispenserDesire(belief, block, supervisor.getName()));
            break;
        }
        case SUPERVISOR_PERCEPT_DELETE_GROUP_DESIRES:{
            for (Iterator<IDesire> iterator = desires.iterator(); iterator.hasNext();) {
                if (iterator.next().isGroupDesire()) {
                    iterator.remove();
                }
            }
            break;
        }
        case SUPERVISOR_PERCEPT_EXPLORE_MAP_SIZE: {
            belief.setGroupDesireType(GroupDesireTypes.EXPLORE);
            List<Parameter> parameters = event.getParameters();
            String teamMate = PerceptUtil.toStr(parameters, 0);
            String teamMateShort = PerceptUtil.toStr(parameters, 1);
            String direction = PerceptUtil.toStr(parameters, 2);
            int guideOffset = PerceptUtil.toNumber(parameters, 3, Integer.class);
            desires.add(new ExploreMapSizeDesire(belief, teamMateShort, teamMate, supervisor.getName(), direction, guideOffset));
            break;
        }
        case MAP_SIZE_DISCOVERED: {
            List<Parameter> parameters = event.getParameters();
            int x = PerceptUtil.toNumber(parameters, 0, Integer.class);
            int y = PerceptUtil.toNumber(parameters, 1, Integer.class);
            belief.setMapSize(x, y);
            break;
        }
        case MEASURE_MOVE: {

            List<Parameter> parameters = event.getParameters();
            if (parameters.size() > 0) {
                String direction = PerceptUtil.toStr(parameters, 0);
                int initialDistance = Integer.parseInt(PerceptUtil.toStr(parameters, 1));
                int basepos = Integer.parseInt(PerceptUtil.toStr(parameters, 2));
                String counterPartAgent = PerceptUtil.toStr(parameters, 3);
                say("will set desire for direction:"+direction);

                addDesire(new MeasureMoveDesire(belief, this.getName(), this.getName(), this.supervisor.getName(), direction, initialDistance, basepos, (Supervisor)supervisor,counterPartAgent));
            }
            break;
        }
            case MEASURE_MEET: {
                if (!this.getName().equals(this.supervisor.getName())) {
                        for (IDesire d: desires) {
                            if (d.getName().equals("MeasureMoveDesire")) {
                                ((MeasureMoveDesire) d).checkForMeeting(event,belief);
                            }
                        }
                    }
                break;
            }

/*
            case MEASURE_DONE: {
                IDesire temp = getDesireByName("MeasureMoveDesire");
                if (!isNull(temp)) {
                    MeasureMoveDesire mmd = (MeasureMoveDesire)temp;
                    mmd.setFulfilled(true);
                }
                break;
            }

 */

        case SIZE_SEND: {
            say("size send recieved");
/*
            List<Parameter> parameters = event.getParameters();
            if (parameters.size() > 0) {
                String axis = PerceptUtil.toStr(parameters, 0);
                int value = Integer.parseInt(PerceptUtil.toStr(parameters, 1));
                switch (axis.toLowerCase()) {
                    case "x" :
                        navi.setHorizontalMapSize(value);
                        break;
                    case "y" :
                        navi.setVerticalMapSize(value);
                        break;
                }

 */
/*
            Navi navi = Navi.get();
            if (!navi.isVerticalMapSizeInDiscover() && !navi.isHorizontalMapSizeInDiscover()) {
                IDesire temp = getDesireByName("MeasureMapDesire");
                if (!isNull(temp)) {
                    MeasureMapDesire mmd = (MeasureMapDesire)temp;
                    mmd.setFulfilled(true);
                }
            }

 */
            break;
/*
            IDesire temp = getDesireByName("MeasureMapDesire");

            if (!isNull(temp)) {
                MeasureMapDesire mmd = (MeasureMapDesire)temp;
                mmd.SizeValueSend(event);
            }
            break;

 */
        }
            case SEND_MEASUREMENT_STARTED: {
                didSendMeasurementStarted = true;
                List<Parameter> parameterList = new ArrayList<Parameter>();
                parameterList.add(new Identifier(getName()));
                Percept message = new Percept(EventName.MEASUREMENT_STARTED.toString(),parameterList);
                broadcast(message,getName());
                break;
            }

            case MEASUREMENT_STARTED: {

                Boolean doSomething = true;

                if (sender.equals(supervisor.getName())) {
                    doSomething = false;
                } else {
                    if (sender.compareTo(supervisor.getName()) == 1){
                        doSomething = false;
                    }
                }

                if (doSomething) {
                    List<Parameter> parameters = event.getParameters();

                    if (parameters.size() > 0) {
                        String MeasurementSupervisor = PerceptUtil.toStr(parameters, 0);

                        if (!MeasurementSupervisor.equals(getName()) && !MeasurementSupervisor.equals(supervisor.getName())) {

                            MeasureMapDesire MMapDesire = null;
                            MeasureMoveDesire MMoveDesire = null;

                            for (IDesire d : desires) {
                                if (d.getName().equals("MeasureMapDesire")) {
                                    MMapDesire = (MeasureMapDesire) d;
                                    break;
                                }
                                if (d.getName().equals("MeasureMoveDesire")) {
                                    MMoveDesire = (MeasureMoveDesire) d;
                                    break;
                                }

                            }
                            if (!isNull(MMapDesire)) {
                                MMapDesire.setUnfulfillable(true);
                            }
                            if (!isNull(MMoveDesire)) {
                                MMoveDesire.setUnfulfillable(true);
                            }
                        }
                    }
                }
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
        desires.add(new WaitNearGoalZoneDesire(belief));
        desires.add(new FreedomDesire(belief));

        String[] actions = {"request", "attach", "connect", "disconnect", "submit"};
        desires.add(new WalkByGetRoleDesire(belief, actions));
        desires.add(new MeasureMapDesire(belief,(Supervisor) supervisor,Navi.get()));
        desires.add(new EscapeClearDesire(belief));
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
                        if (info.name.equals(p.getTaskInfo().name)) {
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

        //Delete Measurement Desire if Agent is not its supervisor
        desires.removeIf(d -> d.getName().equals("MeasurementMapDesire") && !supervisor.getName().equals(getName()));
        // Sort Desires
        desires.sort((a, b) -> a.getPriority() - b.getPriority());

        // Set group Desire flag
        if (!hasGroupDesire()) {
            belief.setGroupDesireBlockDetail("");
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
                BooleanInfo isFulfilled = d.isFulfilled();
                BooleanInfo isExecutable = d.isExecutable();
                DesireDebugData data = new DesireDebugData(d.getName(), isExecutable);
                debugData.add(data);
                if (!isFulfilled.value() && isExecutable.value()) {
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
        List<Point> marker = belief.getMarkerPoints();
        Navi.<INaviAgentV1>get().updateMapAndPathfind(this.supervisor.getName(), this.getName(), index, position, vision, things, goalPoints,
                rolePoints, step, team, maxSteps, score, normsInfo, taskInfo, attachedThings, marker);
    }

    private record PerceptMessage(String sender, Percept percept) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void forwardMessage(Percept message, String receiver, String sender) {
        this.sendMessage(message, receiver, sender);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSupervisorStep() {
        int step = belief.getStep();
        this.supervisor.initStep(step);
    }

    public IDesire getDesireByName(String DesireName) {
            for (IDesire d : desires) {
                if (d.getName().equals(DesireName)) {
                    return d;
                }
            }
        return null;
    }
}
