package de.feu.massim22.group3.agents.V2utils;

import de.feu.massim22.group3.agents.V2utils.AgentMeetings.Meeting;

/**
 * The class <code>BdiAgentV2</code> contains all the important methods for point processing in BdiAgentV2.
 * 
 * @author Melinda Betz
 */
public class Point extends java.awt.Point {

	 /**
     * Initializes a new Instance of Point with two integers.
     *
     * @param x coordinate of the point
     * @param y coordinate of the point
     */
    public Point(int x, int y) {
        super(x, y);
    }

    /**
     * Initializes a new Instance of Point with one Point vector.
     *
     * @param vector the given Point vector
     */
    public Point(Point vector) {
        super(vector.x, vector.y);
    }

    /**
     * Initializes a concrete Instance of Point the point zero.
     * 
     * @return new Instance of Point with x and y being zero
     */
    public static Point zero() {
        return new Point(0, 0);
    }
    
    /**
     * Converts a certain Point into a String .
     *
     * @param point the point that is going to be converted
     * 
     * @return the converted point
     */
    public static String toString(Point point) {
        return "[" + point.x + "," + point.y + "]";
    }
    
    /**
     * Prints the coordinates of a certain point .
     *
     * @return the coordinates of a certain point
     */
    @Override
    public String toString() {
        return "[" + this.x + "," + this.y + "]";
    }
    
    /**
     * Casts a certain Point into a point from type java.awt.Point .
     *
     * @param inPoint - the point that is going to be casted
     * 
     * @return the casted point
     */
    public static Point castToPoint(java.awt.Point inPoint) {
        return inPoint != null ? new Point(inPoint.x, inPoint.y) : null;
    }

    /**
     * Calculates the minimum of two points  .
     *
     * @param point1 the first point
     * @param point2 the second point 
     * 
     * @return the smaller point
     */
    public static Point min(Point point1, Point point2) {
        return new Point(Math.min(point1.x, point2.x), Math.min(point1.y, point2.y));
    }

    /**
     * Calculates the maximum of two points.
     *
     * @param point1 the first point
     * @param point2 the second point 
     * 
     * @return the bigger point
     */
    public static Point max(Point point1, Point point2) {
        return new Point(Math.max(point1.x, point2.x), Math.max(point1.y, point2.y));
    }

    /**
     * Proves if a vector of type Point is positive.
     *
     * @param vector the vector that is going to be proved
     * 
     * @return if the vector is positive or not
     */
    public static boolean isPositive(Point vector) {
        return vector.x >= 0 && vector.y >= 0;
    }

    /**
     * Returns the corner point.
     * 
     * @return the point of the map corner
     */
    public static Point unitY() {
        return new Point(0, 1);
    }

    /**
     * Adds a point.
     *
     * @param point the one to add
     * 
     * @return the solution of the addition
     */
    public Point add(Point point) {
        this.x += point.x;
        this.y += point.y;
        return this;
    }

    /**
     * Subtracts a point.
     *
     * @param point the one to subtract
     * 
     * @return the solution of the subtraction
     */
    public Point sub(Point point) {
        this.x -= point.x;
        this.y -= point.y;
        return this;
    }

    /**
     * Manhattan distance
     *
     * @param b point to the comparison distance
     * @return abs(a.x - b.x)+abs(a.y-b.y)
     */
    public int distance(Point b) {
        Point mapSize = AgentCooperations.mapSize;
        //return Math.abs(this.x - b.x) + Math.abs(this.y - b.y); // Manhattan
        return (Math.min(Math.abs(this.x - b.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(this.x - b.x)) % mapSize.x)
                + Math.min(Math.abs(this.y - b.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(this.y - b.y)) % mapSize.y)); // Manhattan
    }
    
    /**
     * Manhattan distance
     *
     * @param b point to the comparison distance
     * @return abs(a.x - b.x)+abs(a.y-b.y)
     */
    public static int distance(Point a, Point b) {
        Point mapSize = AgentCooperations.mapSize;
        //return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan
        return (Math.min(Math.abs(a.x - b.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(a.x - b.x)) % mapSize.x)
                + Math.min(Math.abs(a.y - b.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(a.y - b.y)) % mapSize.y)); // Manhattan   
    }

    /**
     * Translates a point from another group.
     *
     * @param point the one to translate
     */
    public void translate(Point point) {
        this.add(point);
    }
    
    /**
     * Translates a point from a agents meeting point from the agent2 coordinates is translated into a point from the agent1 coordinates.
     *
     * @param meeting the agents meeting
     * 
     * @return the point translated
     */
    public Point translate2To1(Meeting meeting) {
        Point p1 = new Point(meeting.posAgent1());
        Point p2 = new Point(meeting.posAgent2());
        Point p3 = new Point(meeting.relAgent2());
        return this.add(p1.add(p3.sub(p2)));
    }
    
    /**
     * Translates a point from a agents meeting point from the agent1 coordinates is translated into a point from the agent2 coordinates.
     *
     * @param meeting the agents meeting
     * 
     * @return the point translated
     */
    public Point translate1To2(Meeting meeting) {
        Point p1 = new Point(meeting.posAgent2());
        Point p2 = new Point(meeting.posAgent1());
        Point p3 = new Point(meeting.relAgent1());
        return this.add(p1.add(p3.sub(p2)));
    }

    /**
     * new difference vector
     *
     * @param diffTo target
     * @return vector
     */
    public Point diff(Point diffTo) {
        return new Point(this.x - diffTo.x, this.y - diffTo.y);
    }

    /**
     * new sum vector
     *
     * @param point target
     * @return vector
     */
    public Point sum(Point point) {
        return new Point(this.x + point.x, this.y + point.y);
    }
}