package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.communication.MailService;
import de.feu.massim22.group3.agents.Agent;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.supervisor.ISupervisor;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.map.Navi;
import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import massim.protocol.data.Thing;

import java.awt.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class MeasureMapDesire  extends BeliefDesire {

    private String block;
    private Supervisor supervisor;
    private Navi navi;

//    private MailService mailservice;

    private boolean initialized = false;

/*
    private Agent NorthAgent;
    private Agent SouthAgent;
    private Agent WestAgent;
    private Agent EastAgent;
*/
    private Map<String,String> AgentDirections = new HashMap<String,String>();


    private List<String> AgentList;

    private int WestEastDistance = 0;
    private int NorthSouthDistance = 0;

    private int NorthSouthBasePos = 0;
    private int WestEastBasePos = 0;

    private String supervisorDirection = "";

    private BooleanInfo fulfilled = new BooleanInfo(false, "No done yet");

//    public String getSupervisorDirection() {return supervisorDirection;}

/*
    private Percept pn = new Percept(EventName.MEASURE_MOVE.toString(),new Identifier("n"));
    private Percept ps = new Percept(EventName.MEASURE_MOVE.toString(),new Identifier("s"));
    private Percept pe = new Percept(EventName.MEASURE_MOVE.toString(),new Identifier("e"));
    private Percept pw = new Percept(EventName.MEASURE_MOVE.toString(),new Identifier("w"));
*/


    public MeasureMapDesire(Belief belief, Supervisor supervisor, Navi navi /* ,MailService mailservice */) {
        super(belief);
        this.supervisor = supervisor;
        this.AgentList = this.supervisor.getAgents();
        this.navi = navi;
    }
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 4000;
    }


    @Override
    public BooleanInfo isFulfilled() {
       return fulfilled;
    }

    @Override
    public BooleanInfo isExecutable() {

        if (this.AgentList.size() >4) // Need 5 Agents for 4 directions and a supervisor
            return new BooleanInfo(true, "Enough Agents");
        else
            return new BooleanInfo(false, "Not enough Agents");
    }

    @Override
    public ActionInfo getNextActionInfo() {


        if (!this.initialized)
        {
            this.initialize();
        }

        String thisDirection = "";
        String thisDirAgent = "";
        Percept p;

        for(Map.Entry m:AgentDirections.entrySet()){

/*
            if (m.getValue().equals(supervisor.getName())){
                thisDirection = m.getKey().toString();
                thisDirAgent = m.getValue().toString();
            }

 */
//            else {
//                p = pn;
                switch (m.getKey().toString()) {
                    case "n" :
                    case "s" :
                        p = BuildMeasureMovePercept(m.getKey().toString(),NorthSouthDistance,NorthSouthBasePos);
                    break;
                    case "e" :
                    case "w" :
                    default:
                        p = BuildMeasureMovePercept(m.getKey().toString(),WestEastDistance,WestEastBasePos);
                    break;
                }

                this.supervisor.sendMessage(p,m.getValue().toString(),supervisor.getName());
  //          }
        }
//        this.supervisorDirection = thisDirection;
//        return getActionForMove(thisDirection, thisDirAgent); // Supervisor hold the MapDesire
        return ActionInfo.SKIP("SuperVisor does not move");
    }

     public void update(Supervisor supervisor) {
        this.supervisor = supervisor;
    }

    private void initialize() {

        int northindex = -1;
        int southindex = -1;
        int westindex = -1;
        int eastindex = -1;


        List<String> freeAgents = new ArrayList<String>();
        List<Integer> freeIndexes = new ArrayList<Integer>();

        List<java.awt.Point> AgentPositions = new ArrayList<Point>();
        for(int i = 0; i<5; i++) {
            //Why does this not work??
            if (!this.AgentList.get(i).equals(this.supervisor.getName())) {
//                System.out.println("Me:" + this.AgentList.get(i) + " Supervisor:"+this.supervisor.getName());
                AgentPositions.add(this.navi.getPosition(this.AgentList.get(i), this.supervisor.getName()));
                freeAgents.add(this.AgentList.get(i));
            }
        }

        if (AgentPositions.get(0).y < AgentPositions.get(1).y) {
            northindex = 0;
            southindex = 1;
        } else {
            northindex = 1;
            southindex = 0;
        }

        for (int i = 2; i<4 ; i++) {
            if (AgentPositions.get(i).y < AgentPositions.get(northindex).y) { northindex = i;}
            if (AgentPositions.get(i).y > AgentPositions.get(southindex).y) { southindex = i;}
        }

        for (int i= 0; i<4; i++) {
            if (i!= northindex && i!=southindex) {
                freeIndexes.add(new Integer(i));
            }
        }

        int freeindex1 = freeIndexes.get(0).intValue();
        int freeindex2 = freeIndexes.get(1).intValue();


        if (AgentPositions.get(freeindex1).x < AgentPositions.get(freeindex2).x) {
            westindex = freeindex1;
            eastindex = freeindex2;

        } else {
            westindex = freeindex2;
            eastindex = freeindex1;
        }

        AgentDirections.put("n",freeAgents.get(northindex));
        AgentDirections.put("s",freeAgents.get(southindex));
        AgentDirections.put("w",freeAgents.get(westindex));
        AgentDirections.put("e",freeAgents.get(eastindex));

        NorthSouthDistance = AgentPositions.get(northindex).y - AgentPositions.get(southindex).y;
        NorthSouthBasePos = (AgentPositions.get(northindex).x + AgentPositions.get(southindex).x)/2;
        WestEastDistance = AgentPositions.get(eastindex).x - AgentPositions.get(westindex).x;
        WestEastBasePos = (AgentPositions.get(eastindex).y+ AgentPositions.get(westindex).y)/2;

        for(Map.Entry m:AgentDirections.entrySet()){
            System.out.println(m.getKey()+" "+m.getValue());
        }
        System.out.println("INIT: NSDist:"+NorthSouthDistance+ " NSBasePos: "+ NorthSouthBasePos + "INIT: WEDist:"+WestEastDistance+ " WEBasePos: "+ WestEastBasePos);
        initialized = true;


/* old version , worked so lala
        int northindex = 0;
        int southindex = 0;
        int westindex = 0;
        int eastindex = 0;

        List<String> freeAgents = new ArrayList<String>();

        List<java.awt.Point> AgentPositions = new ArrayList<Point>();
        for(int i = 0; i<5; i++) {
            //Why does this not work??
            if (!this.AgentList.get(i).equals(this.supervisor.getName())) {
                System.out.println("Me:" + this.AgentList.get(i) + " Supervisor:"+this.supervisor.getName());
                AgentPositions.add(this.navi.getPosition(this.AgentList.get(i), this.supervisor.getName()));
                freeAgents.add(this.AgentList.get(i));
            }
        }

        for (int i = 1 ; i<4; i++) {
            if (AgentPositions.get(i).x < AgentPositions.get(westindex).x) { westindex = i;}
            if (AgentPositions.get(i).x > AgentPositions.get(eastindex).x) { eastindex = i;}
            if (AgentPositions.get(i).y > AgentPositions.get(southindex).y) {southindex = i;}  // switched, neg. Y = Norden!
            if (AgentPositions.get(i).y < AgentPositions.get(northindex).y) {northindex = i;}

        }

//        Set<Integer> availIndicies = new LinkedHashSet<Integer>();
        Set<Integer> availIndicies = new LinkedHashSet<Integer>();
        Set<String> availDirections = new LinkedHashSet<String>();
        for (int i = 0; i<4; i++) {
            availIndicies.add(i);
        }
        availDirections.add("n");
        availDirections.add("s");
        availDirections.add("e");
        availDirections.add("w");

        AgentDirections.put("n",freeAgents.get(northindex));
        availDirections.remove("n");
        availIndicies.remove(northindex);

        if (availIndicies.contains(southindex)) {
            AgentDirections.put("s",freeAgents.get(southindex));
            availDirections.remove("s");
            availIndicies.remove(southindex);
        }

        if (availIndicies.contains(westindex)) {
            AgentDirections.put("w", freeAgents.get(westindex));
            availDirections.remove("w");
            availIndicies.remove(westindex);
        }

        if (availIndicies.contains(eastindex)) {
            AgentDirections.put("e", freeAgents.get(eastindex));
            availDirections.remove("e");
            availIndicies.remove(eastindex);
        }

        if (availIndicies.size() >0 ) {
            String dir = "";
            for (int i : availIndicies) {
                if (availDirections.contains("s")) {
                    dir = "s";
                    southindex = i;
                } else if (availDirections.contains("e")) {
                    dir = "e";
                    eastindex = i;
                } else if (availDirections.contains("w")) {
                    dir = "w";
                    westindex = i;
                }
                AgentDirections.put(dir, freeAgents.get(i));
                availDirections.remove(dir);
            }

        }

        NorthSouthDistance = AgentPositions.get(northindex).y - AgentPositions.get(southindex).y;
        NorthSouthBasePos = (AgentPositions.get(northindex).x + AgentPositions.get(southindex).x)/2;
        WestEastDistance = AgentPositions.get(eastindex).x - AgentPositions.get(westindex).x;
        WestEastBasePos = (AgentPositions.get(eastindex).y+ AgentPositions.get(westindex).y)/2;

        for(Map.Entry m:AgentDirections.entrySet()){
            System.out.println(m.getKey()+" "+m.getValue());
        }
        System.out.println("INIT: NSDist:"+NorthSouthDistance+ " NSBasePos: "+ NorthSouthBasePos + "INIT: WEDist:"+WestEastDistance+ " WEBasePos: "+ WestEastBasePos);
        initialized = true;
  //      Navi.get().

 */
    }

    private Percept BuildMeasureMovePercept(String direction,int InitialDistance, int BasePos) {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Identifier(direction));
        parameterList.add(new Identifier(Integer.valueOf(InitialDistance).toString()));
        parameterList.add(new Identifier(Integer.valueOf(BasePos).toString()));
        return new Percept(EventName.MEASURE_MOVE.toString(),parameterList);
    }

    public void setFulfilled(boolean status) {fulfilled = new BooleanInfo(status, "done");}
}
