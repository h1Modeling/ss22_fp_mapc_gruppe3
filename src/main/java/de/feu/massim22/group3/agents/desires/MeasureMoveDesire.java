package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.Agent;
import de.feu.massim22.group3.agents.BdiAgentV1;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.desires.ActionInfo;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.agents.supervisor.ISupervisor;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.communication.MailService;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;
import eis.iilang.Parameter;
import eis.iilang.Identifier;
import eis.iilang.Percept;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static java.util.Objects.isNull;


// needs final compilation and tests ..

public class MeasureMoveDesire extends BeliefDesire {
    private String agent;
    private String agentFullName;
    private String direction;

    private Boolean fullfilled = false;

    private Boolean unfulfillable = false;

    private int InitialDistance = 0;

    private int basePos = 0;

    private int measure_move_count = -1;

    private int moveStepCount = 0;


    private int finalWidthX= -99;
    private int finalWidthY= -99;

    private Supervisor supervisorObject;

    private MailService mailbox;

    private String counterPartAgent;

    public MeasureMoveDesire(Belief belief, String agent, String agentFullName, String supervisor, String direction , int InitialDistance, int BasePos, Supervisor supervisorObject,String counterPartAgent) {
        super(belief);
        this.agent = agent;
        this.agentFullName = agentFullName;
        this.supervisorObject = supervisorObject;
        this.direction = direction;
        this.InitialDistance = InitialDistance;
        this.basePos = BasePos;
        this.counterPartAgent = counterPartAgent;
        String[] neededActions = {"connect"};
    }

    public ActionInfo getNextActionInfo() {

        ActionInfo ai = null;

        //bewerte letzten Step
        scoreLastStep();

        // check for attached blocks and remove
        ai = checkForNeedOfDetach();
        if (!isNull(ai)) { return ai; }

        ai = handleThingInDirection();
        if (!isNull(ai)) {return ai;}

        checkForMeasureMates(); // maybe this has to be done earlier

        // check for Basesituation
        ai = adjustTOBaseline();
        if (!isNull(ai)) { return  ai; }

//        checkForMeasureMates(); // maybe this has to be done earlier

        return ActionInfo.MOVE(direction, getName());
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo((fullfilled ), getName());
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        return new BooleanInfo(unfulfillable, getName());
    }


    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public void update(String supervisor) {
        super.update(supervisor);
    }

    public String getDirection() {
        return direction;
    }

    public int GetInitialDistance() { return InitialDistance;}

    public void setFulfilled(Boolean value) { fullfilled = value;}
    public void setUnfulfillable(Boolean value) { unfulfillable = value;}


    private ActionInfo checkForNeedOfDetach() {

//        System.out.println(this.agent+ " Mypos:" + mypos.toString());
        for ( Point gap :belief.getAttachedPoints()) {
//            System.out.print(this.agent + " GAP" + gap.toString());
            if ((Math.abs(gap.x) == 1 && gap.y == 0) || (Math.abs(gap.y) == 1 && gap.x == 0)) {
//was                String d = DirectionUtil.getDirection(new Point(0,9),gap);
                String d = DirectionUtil.getDirection(new Point(0,0),gap);
                return ActionInfo.DETACH(d, getName());
            }
        }

        return null; // Nothing to detach
    }

    private ActionInfo handleThingInDirection() {

        Set<Thing> things = this.belief.getThings();
        Thing nextThing = null;
        Point p = this.belief.getPosition();// This is a Point on the Map

        for (Thing t : things) { // Point in things are RELATIVE(!) to the Agent!!
            if (((t.x ==0) && (t.y == 1) && direction.equals("s")) ||
                    ((t.x == 0) && (t.y == - 1) && direction.equals("n")) ||
                    ((t.y == 0) && (t.x == 1) && direction.equals("e")) ||
                    ((t.y == 0) && (t.x == - 1) && direction.equals("w"))) {
                nextThing = t;
                break;
            }
        }
        if (!isNull(nextThing)) {
            String alternativeDirection = "";
            List<String> northsouth = new ArrayList<String>();
            List<String> eastwest = new ArrayList<String>();
            northsouth.add("n");
            northsouth.add("s");
            eastwest.add("e");
            eastwest.add("w");

            switch (nextThing.type) {
                case Thing.TYPE_ENTITY:
                    switch(direction) {
                        case "n" :
                        case "s" : alternativeDirection = eastwest.get(new Random().nextInt(eastwest.size()));
                            break;
                        case "e" :
                        case "w" : alternativeDirection = northsouth.get(new Random().nextInt(northsouth.size()));
                    }
                    return ActionInfo.MOVE(alternativeDirection, getName());
                case Thing.TYPE_BLOCK: // clear?
                case Thing.TYPE_OBSTACLE: // dig??
                    return ActionInfo.CLEAR(new Point(nextThing.x, nextThing.y),"");
                case Thing.TYPE_DISPENSER:
                case Thing.TYPE_MARKER:
                    return ActionInfo.MOVE(direction, getName());
            }

        }

        return null;
    }

    @Override
    public int getPriority() {
        return 4001;
    }


    private ActionInfo adjustTOBaseline(){

        int eps = 2;
        String nextDirection = direction; // default to measurement direction
        String lap = "";
        String las = "";

        Point mypos = this.belief.getPosition();
        String la = belief.getLastAction();
        if (la.equals("move")) {
            lap = belief.getLastActionParams().get(0);
            las = belief.getLastActionResult();
        }

        if (direction.equals("n") || direction.equals("s")) {
            if (mypos.x < (basePos - eps) ) { nextDirection = "e";}
            if (mypos.x > (basePos + eps) ) { nextDirection = "w";}
        }
        else { // west or east
            if (mypos.y < (basePos - eps)) {
                nextDirection = "s";
            }
            if (mypos.y > (basePos + eps)) {
                nextDirection = "n";
            }
        }

        if (!direction.equals(nextDirection)) {
            return ActionInfo.MOVE(nextDirection, getName());
        }

        return null;
    }


    private void scoreLastStep() {

        measure_move_count++;
        Point myPosition = belief.getPosition();
        String la = belief.getLastAction();
        String lar = belief.getLastActionResult();
        String lap1 = "@";
        if (belief.getLastActionParams().size() > 0) {
            lap1 = belief.getLastActionParams().get(0);
        }
        if (belief.getLastAction().equals("move")
                && belief.getLastActionResult().equals("success")) {
            if (belief.getLastActionParams().get(0).equals(direction)) {
                moveStepCount = moveStepCount + 1;
            }
            if (belief.getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(direction))) {
                if (measure_move_count > 0)
                    moveStepCount = moveStepCount - 1;
            }
        }
    }

    private void checkForMeasureMates() {

        System.out.println(belief.getAgentShortName() +" " + belief.getPosition().toString() + " (checkForMeasureMates)");

        Set<Thing> things = belief.getThings();
        for (Thing t : things) {
            if (t.type.equals(Thing.TYPE_ENTITY) && !(t.x == 0 && t.y == 0)) {
                if (t.details.equals(belief.getTeam())) {
                    if (isPositionInMoveDirection(t.x, t.y, direction)) {
                        List<Parameter> parameterList = new ArrayList<Parameter>();
                        parameterList.add(new Identifier(direction));
                        parameterList.add(new Identifier(getName()));
                        Integer value = t.x * -1;
                        parameterList.add(new Identifier(value.toString()));
                        value = t.y * -1;
                        parameterList.add(new Identifier(value.toString()));
                        parameterList.add(new Identifier(Integer.valueOf(moveStepCount).toString()));
                        parameterList.add(new Identifier(Integer.valueOf(InitialDistance).toString()));
                        Percept p = new Percept(EventName.MEASURE_MEET.toString(), parameterList);
                        supervisorObject.sendMessage(p, counterPartAgent , getName());
                    }
                }

                //irgendwo implementieren:
                // - andere informieren, das das Desire vergeben ist !

            }
        }
    }

    private boolean isPositionInMoveDirection( int x, int y,String direction) {

        switch(direction) {
            case "n" :
//                return ( y <= 0);
                return ( y < 0);
            case "s":
//                return (y >= 0);
                return (y > 0);
            case "e":
//                return (0 <= x);
                return (0 < x);
            case "w":
//                return (0 >= x);
                return (0 > x);
        }
        return false;
    }

    /*
    - test agains complex card ( lets see wether he diggs or not and so on ..
     ( so we need to change the card ! )
     */

    public void checkForMeeting(Percept event, Belief belief ) {
        String sendDirection;
        String myDirection = direction; // to be refactored!!
        List<Parameter> parameters = event.getParameters();
        if (parameters.size() > 0) {
            sendDirection = String.valueOf(parameters.get(0));

            for (Thing t : belief.getThings()) {
                if (t.type.equals(Thing.TYPE_ENTITY) && t.details.equals(belief.getTeam())) {
                    if ((t.x == Integer.parseInt(String.valueOf(parameters.get(2))))
                            && (t.y == Integer.parseInt(String.valueOf(parameters.get(3))))) {
                            System.out.println(agent + " " +belief.getStep() + " MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                        if (finalWidthX == -99) {
                            System.out.println(agent + " in FinalWidthX");
                            if ((myDirection.equals("w") && sendDirection.equals("e") && t.x <= 0)
                                    || (myDirection.equals("e") && sendDirection.equals("w") && t.x >= 0)) {
                                System.out.println(belief.getStep() + " WE-MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                                int westeastSize = moveStepCount  // stepcount of this agent
                                        + Integer.valueOf(String.valueOf(parameters.get(4))) //moveStepCount of Other agent
                                        + Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) // StartDistance as given by other Agent
                                        // unclear wether the Math.abs is helpful around the start distance or not ...
                                        + Math.abs(t.x);
                                System.out.println("west:"+moveStepCount + "+" + Integer.valueOf(String.valueOf(parameters.get(4))) +"+"+Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) + "+"+ Math.abs(t.x));
                                //found matching agent, size = calculated
                                System.out.println(belief.getStep() + " The we Size is" + westeastSize);
                                finalWidthX = westeastSize;
                                sendPartialSize("X",westeastSize);
                                break;
                            }
                        }
                        if (finalWidthY == -99) {
                            System.out.println(agent + " in FinalWidthY");
                            System.out.println(agent + " " +belief.getStep() + " T:" + t.toString()); // we need to put the right sings into the output here, to see what we need to do ..
                            if ((myDirection.equals("s") && sendDirection.equals("n") && t.y >= 0)
                                    || (myDirection.equals("n") && sendDirection.equals("s") && t.y <= 0)) {
                                System.out.println(agent +" "+ belief.getStep() + " NS-MEET:" + parameters.get(0) + " " + parameters.get(1) + " " + parameters.get(2) + " " + parameters.get(3));
                                //found matching agent, size = calculated
                                int northsouthSize = moveStepCount  // stepcount of this agent
                                        + Integer.valueOf(String.valueOf(parameters.get(4))) //moveStepCount of Other agent
                                        + Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) // StartDistance as given by other Agent
                                        + Math.abs(t.y);
                                System.out.println("north:"+moveStepCount + "+" + Integer.valueOf(String.valueOf(parameters.get(4))) +"+"+Math.abs(Integer.valueOf(String.valueOf(parameters.get(5)))) + "+"+ Math.abs(t.y));
                                System.out.println(belief.getStep() + " The ns Size is" + northsouthSize);
                                finalWidthY = northsouthSize;
                                sendPartialSize("Y",northsouthSize);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendPartialSize(String axis,int size) {

        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Identifier(axis));
        parameterList.add(new Identifier(Integer.valueOf(size).toString()));
        Percept p = new Percept(EventName.SIZE_SEND.toString(), parameterList);
        supervisorObject.sendMessage(p,supervisorObject.getName(),agent);
        System.out.println("sendPartialSize by "+supervisorObject.getName()+ " Value:"+size + " Axis:"+axis);
    }


}
