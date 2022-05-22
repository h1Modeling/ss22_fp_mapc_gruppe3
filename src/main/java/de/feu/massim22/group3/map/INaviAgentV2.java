package de.feu.massim22.group3.map;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Set;
import massim.protocol.data.*;

public interface INaviAgentV2 extends INavi {
    void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo);

    void updateSupervisor(String supervisor);

    // Melinda
    void registerSupervisor(String name, String supervisor);

    Point getPosition(String name, String supervisor);

    List<InterestingPoint> getInterestingPoints(String supervisor, int maxNumberGoals);
    
    Point getTopLeft(String supervisor);
    
    Point getInternalAgentPosition(String supervisor, String agent);
    
    FloatBuffer getMapBuffer(String supervisor);
}