package de.feu.massim22.group3.agents.utilsV2;

import de.feu.massim22.group3.agents.utilsV2.AgentMeetings.Meeting;

/**
 * The class <code>BdiAgentV2</code> contains some important methods for point processing in BdiAgentV2.
 * 
 * @author Melinda Betz
 */
public class Point extends java.awt.Point {

	 /**
     * Initializes a new Instance of Point with two integers.
     *
     * @param x - coordinate of the point
     * @param y - coordinate of the point
     */
    public Point(int x, int y) {
        super(x, y);
    }

    /**
     * Initializes a new Instance of Point with one Point vector.
     *
     * @param vector - the given Point vector
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
     * @param point - the point that is going to be converted
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
     * Adds a point.
     *
     * @param point - the one to add
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
     * @param point - the one to subtract
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
     * @param b - point to the comparison distance
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
     * @param a - point from the comparison distance
     * @param b - point to the comparison distance
     * @return abs(a.x - b.x)+abs(a.y-b.y)
     */
    public static int distance(Point a, Point b) {
        Point mapSize = AgentCooperations.mapSize;
        //return Math.abs(a.x - b.x) + Math.abs(a.y - b.y); // Manhattan
        return (Math.min(Math.abs(a.x - b.x) % mapSize.x,  Math.abs(mapSize.x - Math.abs(a.x - b.x)) % mapSize.x)
                + Math.min(Math.abs(a.y - b.y) % mapSize.y,  Math.abs(mapSize.y - Math.abs(a.y - b.y)) % mapSize.y)); // Manhattan   
    }
    
    /**
     * Translates a point from a agents meeting point from the agent2 coordinates is translated into a point from the agent1 coordinates.
     *
     * @param meeting - the agents meeting
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
     * @param meeting - the agents meeting
     * 
     * @return the point translated
     */
    public Point translate1To2(Meeting meeting) {
        Point p1 = new Point(meeting.posAgent2());
        Point p2 = new Point(meeting.posAgent1());
        Point p3 = new Point(meeting.relAgent1());
        return this.add(p1.add(p3.sub(p2)));
    }
}