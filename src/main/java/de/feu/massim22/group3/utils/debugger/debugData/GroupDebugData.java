package de.feu.massim22.group3.utils.debugger.debugData;

import java.util.List;
import java.util.Map;

import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.InterestingPoint;
import de.feu.massim22.group3.map.PathFindingResult;

import java.awt.Point;

/**
 * The Record <code>GroupDebugData</code> defines a data structure to store information about an agent group.
 *
 * @param supervisor the name of the supervisor of the group
 * @param map a two dimensional array of type CellType which define the current map
 * @param mapTopLeft the position of the top left Point in the map in the coordinate system of the agents
 * @param interestingPoints a list of interesting points which are used for path finding
 * @param pathFindingResult an two dimensional array which stores the path finding results of every agent in the group
 * @param agentPosition the position of every agent
 * @param roleZones all discovered role zones by the group
 * @param goalZones all discovered goal zones by the group
 * @param agents a list of all agent names
 * @param marker a list of marker indicating future clear regions
 * 
 * @author Heinz Stadler
 */
public record GroupDebugData(String supervisor, CellType[][] map, Point mapTopLeft, 
    List<InterestingPoint> interestingPoints, PathFindingResult[][] pathFindingResult,
    Map<Point, String> agentPosition, List<Point> roleZones, List<Point> goalZones,
    List<String> agents, List<Point> marker) {
}
