package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import massim.protocol.data.*;

public interface INaviAgentV1 extends INavi {
    void updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo);
    
    void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess);
}
