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
    
    /**
     * Instantiates a new Instance of BdiAgentV1.
     * 
     * @param name the name of the agent
     * @param mailbox the mail service of the agent
     * @param eisSender the object which sends the calculated action to the server
     * @param index the index of the agent in the agent team
     */

    private boolean forcedIntention = false;
    private int moveStepCount = 0;
    private int moveStartDistance = 0;

    private int myBaseLine = 0;
    private String mybaseDirection = "";

    private int finalWidthX= -99;
    private int finalWidthY= -99;

    private int measure_move_count = -1;
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
/*
        if (isNull(intention)) {
            say ("Intention is NUll");
        } else {
            say("My intention" + intention.getName() );
        }

 */
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
            measure_move_count++;
            // ggf muessen wir zaehlen wie oft der agent an seiner startline vorbeikommt
            // durhc diese Zahl muessen wir die gesamtzahl teilen, damit wir die richtige breite/Hoehe bekommen.
            // und warumhat We mal einen zuviel ud mal einen zu wenig??

//            say("GetLastActioNIntention:" + belief.getLastActionIntention());

            List<Parameter> parameters = event.getParameters();
            if (parameters.size() > 0) {
                String direction = PerceptUtil.toStr(parameters, 0);
                //            say("LastAction:" +  belief.getLastAction() + " Result: "+ belief.getLastActionResult() +" para0: " + direction);
                moveStartDistance = Integer.valueOf(PerceptUtil.toStr(parameters, 1));
                myBaseLine = Integer.valueOf(PerceptUtil.toStr(parameters, 2));

                if (belief.getLastActionIntention().equals("MeasureMoveDesire")) {
                    // last step was a successfull move to the direction given to the intention
                    // this couldm be wwrong, because the intention could be given a  walk around
                    // we need to communicate some kind of Base direction and a base coordinate!
                    // second one for aligning the movement on a straigt line to be sure to meet opponent again!!
                    Set<Thing> things = belief.getThings();
                    Point myPosition = belief.getPosition();
                    String la = belief.getLastAction();
                    String lar = belief.getLastActionResult();
                    String lap1 = "@";
                    if (belief.getLastActionParams().size() > 0) {
                        lap1 = belief.getLastActionParams().get(0);
                    }
//                    if (mybaseDirection.equals("n") || mybaseDirection.equals("s"))
                        say(belief.getStep() + " "+ direction+ " MyPosition: " + myPosition.toString() + "Direction: " + direction + " Aktueller StepCount " + moveStepCount + " LastAction:" + la + " LastResult:" + lar + " Last Dir:" + lap1);
                    if (belief.getLastAction().equals("move")
                            // but know at least the we size was to low, maybe there the is... Check is wrong ...
                            && belief.getLastActionResult().equals("success")) {

                        if (belief.getLastActionParams().get(0).equals(mybaseDirection)) {
                            moveStepCount = moveStepCount + 1;
//                            if (mybaseDirection.equals("n") || mybaseDirection.equals("s"))
                            say(belief.getStep() + " "+ "Added  1 - result:" + moveStepCount);
                        }
                        if (belief.getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(mybaseDirection))) {
                            if (measure_move_count > 0)
                                moveStepCount = moveStepCount - 1;
//                            if (mybaseDirection.equals("n") || mybaseDirection.equals("s"))
                            say(belief.getStep() + " "+ "Subtracted  1 - result:" + moveStepCount);
                        }
                    }
                    for (Thing t : things) {
                        if (t.type.equals(Thing.TYPE_ENTITY) && !(t.x == 0 && t.y == 0)) {
                            if (t.details.equals(belief.getTeam())) {
                                if (isPositionInMoveDirection(t.x, t.y, direction)) {
                                    ////                                say(belief.getStep() + " Position is in Move Direction ("+direction+") " + t.toString());
                                    // THis percept needs more parameter ..
                                    // we need the position to check ... ( and need to know what coordinat-system we are in (ours or initial)
                                    // we need th sender name if we can get it directly in the even
                                    // we need a new Event .. MEASURE_MEET or something like that
                                    // maybe we need to send the steps with it, to end the counting a early as paossible


                                    // die Frage sit nicht siehst du an der Inversen Position einen Thing?
                                    // Sondern die Frage ist, ist deine Position x,y und siehst du ein Thing das
                                    // mit deiner Position , a,b ergibt
                                    // spricht wunsch position des Object senden und eigene Position sendne!!
                                    // das hier drunter ist demnach falsch!!

                                    List<Parameter> parameterList = new ArrayList<Parameter>();
                                    parameterList.add(new Identifier(direction));
                                    parameterList.add(new Identifier(getName()));
                                    Integer value = t.x * -1;
                                    parameterList.add(new Identifier(value.toString()));
                                    value = t.y * -1;
                                    parameterList.add(new Identifier(value.toString()));
                                    parameterList.add(new Identifier(Integer.valueOf(moveStepCount).toString()));
                                    parameterList.add(new Identifier(Integer.valueOf(moveStartDistance).toString()));
                                    Percept p = new Percept(EventName.MEASURE_MEET.toString(), parameterList);
                                    broadcast(p, getName());
                                    //verschhicke  die Frage, welcher Agent mich an der Genannten Position sieht
                                    ////                                say(belief.getStep() + " We found possible Agent at (" + t.x + ","+t.y+")");
                                }
                            }
                            // broadcast  an alle Agenten, melde dich bei mir   wenn ihr einen Agenten auf pos x,y seht.
                            // so meldt sich der agent auf der Psoition von thing. wenn dieser Agent einen passenden Namen hat ( nord sucht sued etc)
                            // und die relatibve position auf der anderen Seite ist ( ggf sogar nur dann broadcast schidken) dann haben wir unser gegenstueck gefunden !!)
                            // auf diese weise soltle es moeglich sein dass sich die agenten finden un die entsprechenden positionen bestimmt werden koennen!!


                        }
                    }

                }

                //            say("Measure Move:" + direction + " " + event.getParameters().toString());
                forcedIntention = true;
                setIntention(new MeasureMoveDesire(belief, /*taskInfo,*/ this.getName(), this.getName(), supervisor.getName(), direction, moveStartDistance, myBaseLine));
                mybaseDirection = direction;
            }
            break;
        }
            case MEASURE_MEET: {
                if (!this.getName().equals(this.supervisor.getName())) {

                    String sendDirection;
                    String myDirection = mybaseDirection;
                    List<Parameter> parameters = event.getParameters();
                    if (parameters.size() > 0) {
                        sendDirection = String.valueOf(parameters.get(0));

                        for (Thing t : belief.getThings()) {
                            // hier muss noch type prüfung und zugehörigkeit rein
                            if (t.type.equals(Thing.TYPE_ENTITY) && t.details.equals(belief.getTeam())) {
                                if ((t.x == Integer.parseInt(String.valueOf(parameters.get(2))))
                                        && (t.y == Integer.parseInt(String.valueOf(parameters.get(3))))) {
//                            say(belief.getStep() + " MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                                    if (finalWidthX == -99) {
                                        if ((myDirection.equals("w") && sendDirection.equals("e") && t.x < 0)
                                                || (myDirection.equals("e") && sendDirection.equals("w") && t.x > 0)) {
                                            say(belief.getStep() + " WE-MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                                            int westeastSize = moveStepCount  // stepcount of this agent
                                                    + Integer.valueOf(String.valueOf(parameters.get(4))) //moveStepCount of Other agent
                                                    + Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) // StartDistance as given by other Agent
                                                      // unclear wether the Math.abs is helpful around the start distance or not ...
                                                    + Math.abs(t.x);
                                            //found matching agent, size = calculated
                                            say(belief.getStep() + " The we Size is" + westeastSize);
                                            finalWidthX = westeastSize;
                                            sendPartialSize("X",westeastSize);

                                            //TODO: calculation is wrong so we need to look into that to correct that
                                            // and than find a way to set the measure to navi and to end the desires
                                            // maybe we also need to find a tweak to relase two agents
                                            // without killing the Desire conditions for MeasureMApDesire.
                                            // but for the first run we would hold the two agents that already finished their job

                                            // have to look into the calculations ..
                                            // have to implement the baseline logic

                                            break;
                                        }
                                    }
                                    if (finalWidthY == -99) {
                                        say(belief.getStep() + " T:" + t.toString()); // we need to put the right sings into the output here, to see what we need to do ..
                                        if ((myDirection.equals("s") && sendDirection.equals("n") && t.y > 0)
                                                || (myDirection.equals("n") && sendDirection.equals("s") && t.y < 0)) {
                                            say(belief.getStep() + " NS-MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                                            //found matching agent, size = calculated
                                            int northsouthSize = moveStepCount  // stepcount of this agent
                                                    + Integer.valueOf(String.valueOf(parameters.get(4))) //moveStepCount of Other agent
                                                    + Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) // StartDistance as given by other Agent
                                                    + Math.abs(t.y);
                                            say(belief.getStep() + " The ns Size is" + northsouthSize);
                                            finalWidthY = northsouthSize;
                                            sendPartialSize("Y",northsouthSize);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
//                        System.out.println("WE:"+finalWidthX +  " NS:"+finalWidthY);
/*
                        if (finalWidthX != -99 && finalWidthY != -99) {

                            say("Final X:"+finalWidthX+" Y:"+finalWidthY);
                            List<Parameter> parameterList = new ArrayList<Parameter>();
                            parameterList.add(new Identifier(Integer.valueOf(finalWidthX).toString()));
                            parameterList.add(new Identifier(Integer.valueOf(finalWidthY).toString()));
                            Percept p = new Percept(EventName.MEASURE_DONE.toString(), parameterList);
                            sendMessage(p,this.supervisor.getName(),this.getName());

                            // sende nachricht an den Supervisor to end the desire
                            // send the maxvalues with you
                            // let the desires stop
                        }
 */



                        // broadcast worked, parameter lifting worked.
                        // maybe we need to send stepcount with it or other things like that.
                        // wenn s and n meet or w and e meet and find the oposite point
                        // then we should generate a match and find a way to handle the result !
                    }
                }
                break;
            }
            case MEASURE_DONE: {
                IDesire foundDesire = null;
                if (this.getName().equals(this.supervisor.getName())) {
                    List<String> agents = ((Supervisor) this.supervisor).getAgents();

                    for (IDesire d : desires) {
                        if (d.getName().equals("MeasureMapDesire")) {
                            ((MeasureMapDesire) d).setFulfilled(true);
                            foundDesire = d;
                        }
                    }
                    if (!isNull(foundDesire)) {
                        desires.remove(foundDesire);
                        foundDesire = null;
                        for (int i = 0; i < agents.size(); i++) {
                            sendMessage(event, agents.get(i), this.getName());
                        }
                    }
                }

                if (forcedIntention) {
//                    say(this.step() +"Measure Done Recieved");
                    forcedIntention = false;
                    setIntention(null);

                    for (IDesire d : desires) {
                        if (d.getName().equals("MeasureMoveDesire")) {
                            ((MeasureMoveDesire) d).setFulfilled(true);
                            foundDesire = d;
                        }
                    }
                    if (!isNull(foundDesire)) {
                        desires.remove(foundDesire);
                    }
                }
                break;
            }
        case SIZE_SEND: {
            List<Parameter> parameters = event.getParameters();
            String CorrXY = PerceptUtil.toStr(parameters, 0);
            int value = Integer.valueOf(PerceptUtil.toStr(parameters, 1));

            switch (CorrXY) {
                case "X" : finalWidthX = value;
                break;
                case "Y" : finalWidthY = value;
            }

            if (finalWidthX != -99 && finalWidthY != -99) {

                say(this.step() +"Final X:"+finalWidthX+" Y:"+finalWidthY);
                List<Parameter> parameterList = new ArrayList<Parameter>();
                parameterList.add(new Identifier(Integer.valueOf(finalWidthX).toString()));
                parameterList.add(new Identifier(Integer.valueOf(finalWidthY).toString()));
                Percept p = new Percept(EventName.MEASURE_DONE.toString(), parameterList);
                sendMessage(p,this.supervisor.getName(),this.getName());
            }
            break;
        }
        default:
            throw new IllegalArgumentException("Message is not handled: " + taskName);
        }
    }

    private void sendPartialSize(String axis,int size) {

        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Identifier(axis));
        parameterList.add(new Identifier(Integer.valueOf(size).toString()));
        Percept p = new Percept(EventName.SIZE_SEND.toString(), parameterList);
        sendMessage(p,this.supervisor.getName(),this.getName());
    }

	
    private boolean isPositionInMoveDirection( int x, int y,String direction) {

        switch(direction) {
            case "n" :
                return ( y <= 0);
//                return (y >= 0);
            case "s":
                return (y >= 0);
//                return ( y <= 0);
            case "e":
                return (0 <= x);
            case "w":
                 return (0 >= x);
       }
        return false;
    }
    private void addBasicDesires() {
        desires.add(new MeasureMapDesire(belief,(Supervisor) supervisor,Navi.get()));
        desires.add(new ExploreDesire(belief, supervisor.getName(), getName()));
        desires.add(new LooseWeightDesire(belief));
        desires.add(new DigFreeDesire(belief));
        desires.add(new WaitNearGoalZoneDesire(belief));
        desires.add(new FreedomDesire(belief));

        String[] actions = {"request", "attach", "connect", "disconnect", "submit"};
        desires.add(new WalkByGetRoleDesire(belief, actions));
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
        if (!forcedIntention) {
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
}
