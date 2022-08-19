package de.feu.massim22.group3.map;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.*;

import massim.protocol.data.*;

public interface INaviAgentV2 extends INavi {
    void updateMap(String supervisor, String agent, int agentIndex, Point position, int vision, Set<Thing> things,
            List<Point> goalPoints, List<Point> rolePoints, int step, String team, int maxSteps, int score,
            Set<NormInfo> normsInfo, Set<TaskInfo> taskInfo, List<Point> attachedPoints);

    // Melinda
    List<CalcResult> updateSupervisor(String supervisor);

    void registerSupervisor(String name, String supervisor);

    List<InterestingPoint> getInterestingPoints(String supervisor, int maxNumberGoals);
    
    Point getTopLeft(String supervisor);
    
    Point getInternalAgentPosition(String supervisor, String agent); 
    Point getPosition(String agent, String supervisor);  
    FloatBuffer getMapBuffer(String supervisor);
    Map<String, GameMap> getMaps();
}