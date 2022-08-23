package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class MeasureMoveDesire extends BeliefDesire {
    private String agent;
    private String agentFullName;
    private String direction;

    private Boolean fullfilled = false;

    private int InitialDistance = 0;

    private int basePos = 0;
    public MeasureMoveDesire(Belief belief, String agent, String agentFullName, String supervisor, String direction , int InitialDistance, int BasePos) {
        super(belief);
        this.agent = agent;
        this.agentFullName = agentFullName;
        this.direction = direction;
        this.InitialDistance = InitialDistance;
        this.basePos = BasePos;
        String[] neededActions = {"connect"};
/*
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new OrDesire(
                new AttachAbandonedBlockDesire(belief, block.type, supervisor),
                new AttachSingleBlockFromDispenserDesire(belief, block, supervisor))
        );
        precondition.add(new MeetAgentAtGoalZoneDesire(belief, agent));
        precondition.add(new ConnectBlockToAgentDesire(belief, agent, agentFullName, task, block, communicator));

 */
    }

    public ActionInfo getNextActionInfo() {
        // check the map into in directionwether empty or not ( move or dig/clear)

        // maybe we need to chekc wether the last action was successfull?
        // just to count our movememnts ..
        Point mypos = this.belief.getPosition();
        System.out.println(this.agent+ " Mypos:" + mypos.toString());
        for ( Point gap :belief.getAttachedPoints()) {
            System.out.print(this.agent + " GAP" + gap.toString());
            if ((Math.abs(gap.x) == 1 && gap.y == 0) || (Math.abs(gap.y) == 1 && gap.x == 0)) {
                String d = DirectionUtil.getDirection(new Point(0,9),gap);
                return ActionInfo.DETACH(d, getName());
            }
        }

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

        if (nextThing == null) {
            int eps = 2;
            String nextDirection = direction; // default to measurement direction
            String lap = "";
            String las = "";

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
                // wir brauchen hier die baseline information um yu prüfen ob die abweichung zu hoch ist
                // ubnd wir ggf korrekturmassnahmen einleiten müssen
                // dazu muessen wir wohl die Baseline auch noch an das Desire übergeben
                // klappt das dann noch mit dem A1??
                // wenn wir die Kurskorrekturen drin haben, klappt es hoffentlich mit dem erkennen
                // dann müssen wir ganz konkret gucken, ob sich die Zusammengehörigen Agenten finden
                // und wenn nein, warum nicht
                // ggf die ausgaben so bauen dass nur für bestimmte richtungen werte ausgegebenw erden
                // wennd as ohne großen aufwand moeglich ist
                // dann bekommen wir nicht soviele informationen angezeigt.
                // ggf auch noch mal die Says ausmisten und guckenw as uns hilft und was an informationen aktuell eher
                // stört

                // letzte Action checken auf success & direction
                // war die letzte action schon in die gewählte richtung und erfolglos, mache erstmal einens chritt in die Sollrichtung

/*
  // the following does not work too
                {
                    if (mypos.x < (basePos - eps)) {
                        if (las.equals("success")) {
                            nextDirection = "e";
                        }
                        if (!las.equals("success")) {
                            if (lap.equals("e")) {
                                nextDirection = "w";
                            }
                            if (lap.equals("w")) {
                                nextDirection = "s";
                            }
                            if (lap.equals("w")) {
                                nextDirection = "n";
                            }
                        }
                    }

                    if (mypos.x > (basePos - eps)) {
                        if (las.equals("success")) {
                            nextDirection = "w";
                        }
                        if (!las.equals("success")) {
                            if (lap.equals("w")) {
                                nextDirection = "e";
                            }
                            if (lap.equals("e")) {
                                nextDirection = "n";
                            }
                            if (lap.equals("n")) {
                                nextDirection = "s";
                            }
                        }
                    }
                }
            }
            else { // west or east
                if (mypos.y < (basePos - eps)) {
                    if (las.equals("success")) {
                        nextDirection = "s";
                    }
                    if (!las.equals("success")) {
                        if (lap.equals("s")) {
                            nextDirection = "n";
                        }
                        if (lap.equals("n")) {
                            nextDirection = "w";
                        }
                        if (lap.equals("w")) {
                            nextDirection = "e";
                        }
                    }
                }

                if (mypos.y > (basePos - eps)) {
                    if (las.equals("success")) {
                        nextDirection = "n";
                    }
                    if (!las.equals("success")) {
                        if (lap.equals("n")) {
                            nextDirection = "s";
                        }
                        if (lap.equals("s")) {
                            nextDirection = "e";
                        }
                        if (lap.equals("e")) {
                            nextDirection = "w";
                        }
                    }
                }
            }
 */

//                if (mypos.y < (basePos - eps) && !lap.equals("s") && las.equals("success")) { nextDirection = "s";}
//                if (mypos.y > (basePos + eps) && !lap.equals("n") && las.equals("success")) { nextDirection = "n";}
//            }
            return ActionInfo.MOVE(nextDirection, getName());
        }
        else {
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
                        case "s" : alternativeDirection= eastwest.get(new Random().nextInt(eastwest.size()));
                        break;
                        case "e" :
                        case "w" : alternativeDirection= northsouth.get(new Random().nextInt(northsouth.size()));

/*
                        case "n" : alternativeDirection = "e";
                        break;
                        case "s" : alternativeDirection = "w";
                        break;
                        case "e" : alternativeDirection = "n";
                        break;
                        case "w" : alternativeDirection = "s";
                        break;

 */
                    }
                    return ActionInfo.MOVE(alternativeDirection, getName());
//                    return ActionInfo.SKIP(getName());
                case Thing.TYPE_BLOCK: // clear?
                    // wenn der Block attached ist zu mir, hab ich ein  Problem
                    // ich muss den Block erstmal loswerden.
                    // die frage ist hier auch ob ich dann clearen muss
                    // Umlaufen wuerde es ja auch tun
                    // auf jeden fall haben wir hier probleme mit clear auf eigene bloecke !!


                case Thing.TYPE_OBSTACLE: // dig??
                    return ActionInfo.CLEAR(new Point(nextThing.x, nextThing.y),"");
                case Thing.TYPE_DISPENSER:
                case Thing.TYPE_MARKER:
                    return ActionInfo.MOVE(direction, getName());
            }
        }
        return null; // should never happen ....
    }

    @Override
    public BooleanInfo isFulfilled() {
         /*
            wenn er seien Spiegelagenten wiederfindet
            also north find south and east finds west  or vice versa

          */

        return new BooleanInfo((fullfilled ), getName());
    }

    @Override
    public BooleanInfo isUnfulfillable() {
        return new BooleanInfo(false, getName());
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

}
