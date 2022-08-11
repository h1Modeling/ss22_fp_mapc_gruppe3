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

/**
 * The Record <code>AgentReport</code> provides a data structure to store important information about the agent.
 * 
 * @param attachedThings a list of attached things to the agent
 * @param energy the current energy level of the agent
 * @param deactivated true if the agent is deactivated
 * @param actions the set of actions the agent is able to perform
 * @param position the position of the agent
 * @param distanceDispenser the distance to all dispenser types
 * @param distanceGoalZone the smallest distance to a goal zone
 * @param groupDesireType the current group desire of the agent
 * @param step the current step of the simulation
 * @param agentActionName the name of the last action sent to the server
 * @param nearestGoalZone the position of the nearest gaol zone
 * @param groupDesireBlock the block type of the current group desire
 * 
 * @author Heinz Stadler
 */
public record AgentReport(List<Thing> attachedThings, int energy, boolean deactivated,
        Set<String> actions, Point position, int[] distanceDispenser, int distanceGoalZone,
        String groupDesireType, int step, String agentActionName, Point nearestGoalZone, String groupDesireBlock) {
    
    /**
    * Creates a Percept Function from the data in the record.

    * @return the percept function
    */
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
        Parameter pointXGoalZone = new Numeral(nearestGoalZone.x);
        Parameter pointYGoalZone = new Numeral(nearestGoalZone.y);
        Parameter groupDesireBlockPara = new Identifier(groupDesireBlock);
        
        return new Function(SupervisorEventName.REPORT.name(), attachedPara, energyPara, deactivatedPara,
            actionPara, posXPara, posYPara, distancePara, distanceGoalZonePara, groupDesirePara, stepPara,
            agentActionNamePara, pointXGoalZone, pointYGoalZone, groupDesireBlockPara);
    }

    /**
     * Creates an AgentReport Record from the provided Percept.
     * 
     * @param f the percept containing the record data
     * @return the agent report
     */
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
        int goalX = PerceptUtil.toNumber(paras, 11, Integer.class);
        int goalY = PerceptUtil.toNumber(paras, 12, Integer.class);
        Point nearestGoalZone = new Point(goalX, goalY);
        String groupDesireBlock = PerceptUtil.toStr(paras, 13);
        return new AgentReport(attached, energy, deactivated, actions, position, distanceDispenser,
            distanceGoalZone, groupDesire, step, agentActionName, nearestGoalZone, groupDesireBlock);
    }
}


            