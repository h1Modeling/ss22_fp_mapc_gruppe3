package de.feu.massim22.group3.map;

import java.awt.Point;
import java.util.List;
import java.util.Set;

import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.DesireDebugData;
import massim.protocol.data.*;

public interface INaviAgentV1 extends INavi {
    PathFindingResult[][] updateMapAndPathfind(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo, List<Point> attachedPoints);
    
    void updateAgentDebugData(String agent, String supervisor, String role, int energy, String lastAction, String lastActionSuccess, String lastActionIntention, String groupDesireType);
    void updateDesireDebugData(List<DesireDebugData> data, String agent);
    void acceptMerge(String mergeKey, String name);
    void rejectMerge(String mergeKey, String name);
    String getDirectionToNearestUndiscoveredPoint(String supervisor, String agent);
    boolean isWaitingOrBusy();
    int getAgentIdAtPoint(String supervisor, Point p);
    List<Point> getMeetingPoints(String supervisor);
}
