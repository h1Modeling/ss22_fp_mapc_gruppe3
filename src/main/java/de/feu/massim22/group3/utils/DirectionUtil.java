package de.feu.massim22.group3.utils;

import java.awt.Point;
import java.util.*;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.V2utils.AgentCooperations;
import de.feu.massim22.group3.agents.belief.reachable.ReachableDispenser;
import de.feu.massim22.group3.utils.logging.AgentLogger;

/**
 * The Class <code>DirectionUtil</code> contains static methods to convert Between string directions, int code directions and Point directions.
 *
 * @author Melinda Betz
 * @author Heinz Stadler
 */
public class DirectionUtil {
    //public static Point mapSize = new Point(500, 500);

    /**
     * Translates a direction code from pathfinding into a string containing the direction chars.
     * 
     * @param direction the direction code
     * @return the direction string
     */
	public static String intToString(int direction) {
        String s = String.valueOf(direction).replaceAll("0", "w").replaceAll("1", "n").replaceAll("2", "e").replaceAll("3", "s")
                .replaceAll("4", "w").replaceAll("5", "n");

        return new StringBuilder(s).reverse().toString();
    }
	
    /**
     * Gets the first direction as a String from a direction code.
     * 
     * @param direction the direction code
     * @return the direction string containing one char
     */
	public static String firstIntToString(int direction) {
        String s = intToString(direction);

        return s.substring(0,1);
    }

    /**
     * Creates a direction code from a direction String.
     * 
     * @param direction the direction String
     * @return the direction code
     */
	public static int stringToInt(String direction) {
        String s = String.valueOf(direction).replaceAll("n", "1").replaceAll("e", "2").replaceAll("s", "3")
                .replaceAll("w", "4");

        return Integer.parseInt(s);
    }

    /**
     * Translates a direction String into a direction Point.
     * 
     * @param direction the direction String
     * @return the direction Point
     */
	public static Point getCellInDirection(String direction) {
        switch (direction) {
        case "n":
            return new Point(0, -1);
        case "e":
            return new Point(1, 0);
        case "s":
            return new Point(0, 1);
        case "w":
            return new Point(-1, 0);
        default:
            return new Point(0, 0);
        }
    }
	
    /**
     * Gets a List containing all four direction Points.
     *  
     * @return the list of direction points.
     */
	public static List<Point> getCellsIn4Directions() {
		ArrayList <Point> result = new ArrayList<>();

           result.add(new Point(-1,0));
           result.add(new Point(0,-1));
           result.add(new Point(1,0));
           result.add(new Point(0,1));
           
           return result;
    }
	
    /**
     * Gets the index of the provided direction Point in the direction List.
     *  
     * @param inCell the direction Point
     * @return the index in the direction list
     * @see #getCellsIn4Directions
     */
    public static int getDirectionForCell(Point inCell) {
        if (inCell.equals(new Point(0, -1)))
        return 1;
        else if (inCell.equals(new Point(1, 0)))
        return 2;
        else if (inCell.equals(new Point(0, 1)))
        return 3;
        else if (inCell.equals(new Point(-1, 0))) 
        return 4;
        
        return 0;
    }
	
    /**
     * Rotates the Point clock wise.
     * 
     * @param p the point to rotate
     * @return the rotated point
     */
    public static Point rotateCW(Point p) {
        return new Point(-p.y, p.x);
    }

    /**
     * Rotates the Point counter clock wise.
     * 
     * @param p the point to rotate
     * @return the rotated point
     */
    public static Point rotateCCW(Point p) {
        return new Point(p.y, -p.x);
    }
	
    /**
     * Translates a Point by a direction String.
     * 
     * @param point the point to translate
     * @param direction the direction
     * @return the translated point
     */
    public static Point getCellInDirection(Point point, String direction) {
        switch (direction) {
            case "n": return new Point(point.x, point.y - 1);
            case "e": return new Point(point.x + 1, point.y);
            case "s": return new Point(point.x, point.y + 1);
            case "w": return new Point(point.x - 1, point.y);
        }
        return point;
    }


    /**
     * Gets the direction from a Point to another Point.
     * 
     * @param from the point the direction is calculated of
     * @param to the point the direction is calculated to
     * @return the direction
     */
    public static String getDirection(Point from, Point to) {
        String result = " ";
        Point pointTarget = new Point(to.x - from.x, to.y - from.y);
        Point pointTargetAround = new Point(to.x - from.x - AgentCooperations.mapSize.x, to.y - from.y - AgentCooperations.mapSize.y);

        /*AgentLogger.info(Thread.currentThread().getName() + " getDirection - from/to: " + from.toString() + " , "
                + to.toString() + " , pointTarget/pointTargetAround: " + pointTarget.toString() + " , "
                + pointTargetAround.toString());*/

        if (pointTarget.x == 0) {
            if ((pointTarget.y < 0 && Math.abs(pointTargetAround.y) >= Math.abs(pointTarget.y))
                    || (pointTargetAround.y < 0 && Math.abs(pointTargetAround.y) < Math.abs(pointTarget.y)))
                result = "n";
            else
                result = "s";
        }

        if (pointTarget.y == 0) {
            if ((pointTarget.x < 0 && Math.abs(pointTargetAround.x) >= Math.abs(pointTarget.x))
                    || (pointTargetAround.x < 0 && Math.abs(pointTargetAround.x) < Math.abs(pointTarget.x)))
                result = "w";
            else
                result = "e";
        }

        if (pointTarget.x != 0 && pointTarget.y != 0) {
            if (java.lang.Math.abs(pointTarget.x) > Math.abs(pointTarget.y))
                if ((pointTarget.x < 0 && Math.abs(pointTargetAround.x) >= Math.abs(pointTarget.x))
                        || (pointTargetAround.x < 0 && Math.abs(pointTargetAround.x) < Math.abs(pointTarget.x)))
                    result = "w";
                else
                    result = "e";
            else if ((pointTarget.y < 0 && Math.abs(pointTargetAround.y) >= Math.abs(pointTarget.y))
                    || (pointTargetAround.y < 0 && Math.abs(pointTargetAround.y) < Math.abs(pointTarget.y)))
                result = "n";
            else
                result = "s";
        }

        return result;
    }
	
    /**
     * Get rotation direction between two Points.
     * 
     * @param a the start point
     * @param b the finish point
     * @return the shortest rotation between the two points
     */
	public static String getClockDirection(Point a, Point b) {
	    String result = "";
	    
	    if (a.x == 0 && b.x == 0 || a.y == 0 && b.y == 0) {
	        result = "cw";
	    } else if (a.x == 0 && a.y == 1) {
	        if (b.x == -1 && b.y == 0) result = "cw"; 
	        if (b.x == 1 && b.y == 0) result = "ccw"; 
        } else if (a.x == -1 && a.y == 0) {
            if (b.x == 0 && b.y == -1) result = "cw"; 
            if (b.x == 0 && b.y == 1) result = "ccw"; 
        } else if (a.x == 0 && a.y == -1) {
            if (b.x == 1 && b.y == 0) result = "cw"; 
            if (b.x == -1 && b.y == 0) result = "ccw";  
        } else if (a.x == 1 && a.y == 0) {
            if (b.x == 0 && b.y == 1) result = "cw"; 
            if (b.x == 0 && b.y == -1) result = "ccw"; 
        }
	    return result;
	}
	
    /**
     * Gets the dispenser position of a ReachableDispenser
     * 
     * @param inDispenser the reachable dispenser 
     * @return the position of the dispenser
     */
	public static Point getDispenserItself(ReachableDispenser inDispenser) {
		int x = 0;
		int y = 0;
        
        switch(inDispenser.data()) {
        case "n":
        	x = inDispenser.position().x;
        	y = inDispenser.position().y + 1;
        	break;
        case "e":
        	x = inDispenser.position().x - 1;
        	y = inDispenser.position().y;
        	break;
        case "s":
        	x = inDispenser.position().x;
        	y = inDispenser.position().y - 1;
        	break;
        case "w":
        	x = inDispenser.position().x + 1;
        	y = inDispenser.position().y;
        	break;
        }

        return new Point(x, y);
    }
	
    /**
     * Get the opposite direction from a direction String
     * 
     * @param inDirection the original direction
     * @return the opposite direction
     */
    public static String oppositeDirection(String inDirection) {
        String outDirection = inDirection;
        
        if (inDirection.equals("n")) outDirection = "s";
        if (inDirection.equals("e")) outDirection = "w";
        if (inDirection.equals("s")) outDirection = "n";
        if (inDirection.equals("w")) outDirection = "e";
        
        return outDirection;
    }
    
    // TODO add Method description 
    /**
     * 
     * @param inDirection the direction to test
     * @param agent the agent
     * @return
     */
    public static String proofDirection(String inDirection, BdiAgentV2 agent) {
        String outDirection = inDirection;
 
        if (agent.desireProcessing.lastWishDirection != null) {
            if (agent.getBelief().getLastAction().equals("move") && agent.getBelief().getLastActionResult().equals("success")) {
                if (!agent.getBelief().getLastActionParams().get(0).equals(agent.desireProcessing.lastWishDirection)) {
                    if (agent.getBelief().getLastActionParams().get(0).equals(DirectionUtil.oppositeDirection(inDirection))) {
                        outDirection = agent.desireProcessing.lastWishDirection;
                    }
                }
            }
        }
        
        return outDirection;
    }
}