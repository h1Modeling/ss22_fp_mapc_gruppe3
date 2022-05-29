package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;
import massim.protocol.data.*;

public interface INaviAgentV1 extends INavi {
    PathFindingResult[][] updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo, List<Point> attachedPoints);
    
    void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess);
    void acceptMerge(String mergeKey, String name);
    void rejectMerge(String mergeKey, String name);
    boolean isWaitingOrBusy();
}
