package de.feu.massim22.group3.agents.supervisor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.agents.events.SupervisorEventName;
import de.feu.massim22.group3.utils.PerceptUtil;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;

import java.awt.Point;
import massim.protocol.data.Thing;

public record AgentReport(List<Thing> attachedThings, int energy, boolean deactivated,
        Set<String> actions, Point position, int[] distanceDispenser, int distanceGoalZone, String groupDesireType, int step, String agentActionName) {
    
    public Function createMessage() {
        Parameter energyPara = new Numeral(energy);
        Parameter deactivatedPara = new TruthValue(deactivated);
        Parameter posXPara = new Numeral(position.x);
        Parameter posYPara = new Numeral(position.y);
        Parameter groupDesirePara = new Identifier(groupDesireType);
        Parameter distanceGoalZonePara = new Numeral(distanceGoalZone);
        Parameter agentActionNamePara = new Identifier(agentActionName);
        List<Parameter> attachedParas = new ArrayList<>();
        for (Thing t: attachedThings) {
            Parameter x = new Numeral(t.x);
            Parameter y = new Numeral(t.y);
            Parameter type = new Identifier(t.type);
            Parameter detail = new Identifier(t.details);
            Function f = new Function("Thing", x, y, type, detail);
            attachedParas.add(f);
        }
        Parameter attachedPara = new ParameterList(attachedParas);
        List<Parameter> actionParas = new ArrayList<>();
        for (String a : actions) {
            Parameter para = new Identifier(a);
            actionParas.add(para);
        }
        Parameter actionPara = new ParameterList(actionParas);
        List<Parameter> distanceParas = new ArrayList<>();
        for (int d : distanceDispenser) {
            Parameter para = new Numeral(d);
            distanceParas.add(para);
        }
        Parameter distancePara = new ParameterList(distanceParas);
        Parameter stepPara = new Numeral(step);
        
        return new Function(SupervisorEventName.REPORT.name(), attachedPara, energyPara, deactivatedPara,
            actionPara, posXPara, posYPara, distancePara, distanceGoalZonePara, groupDesirePara, stepPara, agentActionNamePara);
    }

    public static AgentReport fromPercept(Percept f) {
        List<Parameter> paras = f.getParameters();
        List<Thing> attached =  new ArrayList<>(PerceptUtil.toThingSet(paras, 0));
        int energy = PerceptUtil.toNumber(paras, 1, Integer.class);
        boolean deactivated = PerceptUtil.toBool(paras, 2);
        Set<String> actions = new HashSet<>(PerceptUtil.toStrList(paras, 3));
        int posX = PerceptUtil.toNumber(paras, 4, Integer.class);
        int posY = PerceptUtil.toNumber(paras, 5, Integer.class);
        Point position = new Point(posX, posY);
        int distanceGoalZone = PerceptUtil.toNumber(paras, 7, Integer.class);
        String groupDesire = PerceptUtil.toStr(paras, 8);
        List<Integer> distList = PerceptUtil.toIntList(paras, 6);
        int[] distanceDispenser = new int[5];
        for (int i = 0; i < 5; i++) {
            distanceDispenser[i] = distList.get(i);
        }
        int step = PerceptUtil.toNumber(paras, 9, Integer.class);
        String agentActionName  = PerceptUtil.toStr(paras, 10);
        return new AgentReport(attached, energy, deactivated, actions, position, distanceDispenser, distanceGoalZone, groupDesire, step, agentActionName);
    }
}


            