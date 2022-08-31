package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.communication.MailService;
import de.feu.massim22.group3.agents.Agent;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.supervisor.ISupervisor;
import de.feu.massim22.group3.agents.supervisor.Supervisor;
import de.feu.massim22.group3.agents.events.EventName;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.PerceptUtil;
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

    int widthX = -99;
    int widthY = -99;


    private BooleanInfo fulfilled = new BooleanInfo(false, "No done yet");
    private Boolean unfulfillable = false;

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
       return new BooleanInfo(!(widthX == -99 || widthY == -99),"Fulfillmentstate");
    }

    public BooleanInfo isUnfulfillable() {
        return new BooleanInfo(unfulfillable, getName());
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

            // send all other, not to start measurement too
            List<Parameter> parameterList = new ArrayList<Parameter>();
            parameterList.add(new Identifier(supervisor.getName()));
            Percept message = new Percept(EventName.SEND_MEASUREMENT_STARTED.toString(), parameterList);
            supervisor.sendMessage(message, supervisor.getName(),supervisor.getName());

        String ActionAgent = "";
        String CounterAgent = "";
        String ActionDirection = "";
        int Distance = 0;
        int BasePos = 0;
        Percept p;


        for(Map.Entry m:AgentDirections.entrySet()){
                ActionAgent = m.getValue().toString();
                ActionDirection = m.getKey().toString();
                switch (m.getKey().toString()) {
                    case "n" :  CounterAgent = AgentDirections.get("s");
                                Distance = NorthSouthDistance;
                                BasePos = NorthSouthBasePos;
                                break;
                    case "s" :  CounterAgent = AgentDirections.get("n");
                                Distance = NorthSouthDistance;
                                BasePos = NorthSouthBasePos;
                                break;
                    case "e" :  CounterAgent = AgentDirections.get("w");
                                Distance = WestEastDistance;
                                BasePos = WestEastBasePos;
                                break;
                    case "w" :  CounterAgent = AgentDirections.get("e");
                                Distance = WestEastDistance;
                                BasePos = WestEastBasePos;
                                break;
                }

                p = BuildMeasureMovePercept(ActionDirection,Distance,BasePos,CounterAgent);
                this.supervisor.sendMessage(p,ActionAgent,supervisor.getName());

        }
       }

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
            if (!this.AgentList.get(i).equals(this.supervisor.getName())) {
                AgentPositions.add(this.navi.getPosition(this.AgentList.get(i), this.supervisor.getName()));
                freeAgents.add(this.AgentList.get(i));
            }
        }

        for (int i = 0; i<freeAgents.size(); i++) {
            System.out.println(freeAgents.get(i) + " "+ AgentPositions.get(i).toString());
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
    }

    private Percept BuildMeasureMovePercept(String direction,int InitialDistance, int BasePos, String CounterAgentName) {
        List<Parameter> parameterList = new ArrayList<Parameter>();
        parameterList.add(new Identifier(direction));
        parameterList.add(new Identifier(Integer.valueOf(InitialDistance).toString()));
        parameterList.add(new Identifier(Integer.valueOf(BasePos).toString()));
        parameterList.add(new Identifier(CounterAgentName));
        return new Percept(EventName.MEASURE_MOVE.toString(),parameterList);
    }

    public void SizeValueSend(Percept event) {

        List<Parameter> parameters = event.getParameters();
        String CorrXY = PerceptUtil.toStr(parameters, 0);
        int value = Integer.valueOf(PerceptUtil.toStr(parameters, 1));

        switch (CorrXY) {
            case "X" : widthX = value;
                break;
            case "Y" : widthY = value;
        }

        System.out.println("Widthx:"+widthX+" WidthY:"+widthY+ " Isfullfilled"+isFulfilled().toString());
        if (isFulfilled().value()) {
           System.out.println("Final X:"+widthX+" Y:"+widthY);
            List<Parameter> parameterList = new ArrayList<Parameter>();
            Percept p = new Percept(EventName.MEASURE_DONE.toString(), parameterList);
            for (String agentName : AgentList) {
                supervisor.sendMessage(p,agentName, this.getName());
            }
        }
    }

    public void setUnfulfillable(Boolean value) { unfulfillable = value;}


}
