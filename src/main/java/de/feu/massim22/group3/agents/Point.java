package de.feu.massim22.group3.agents;

public class Point extends java.awt.Point {

    public Point(int x, int y) {
        super(x, y);
    }

    public Point(Point vector) {
        super(vector.x, vector.y);
    }

    public static Point zero() {
        return new Point(0, 0);
    }
    
    public static Point castToPoint(java.awt.Point inPoint) {
        return inPoint != null ? new Point(inPoint.x, inPoint.y) : null;
    }

    public static Point min(Point point1, Point point2) {
        return new Point(Math.min(point1.x, point2.x), Math.min(point1.y, point2.y));
    }

    public static Point max(Point point1, Point point2) {
        return new Point(Math.max(point1.x, point2.x), Math.max(point1.y, point2.y));
    }

    public static boolean isPositive(Point vector) {
        return vector.x >= 0 && vector.y >= 0;
    }

    public static Point unitY() {
        return new Point(0, 1);
    }

    public Point add(Point point) {
        this.x += point.x;
        this.y += point.y;
        return this;
    }

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
        return (Math.abs(this.x - b.x) + Math.abs(this.y - b.y)); // Manhattan
    }
    
    /**
     * Manhattan distance
     *
     * @param b - point to the comparison distance
     * @return abs(a.x - b.x)+abs(a.y-b.y)
     */
    public static int distance(Point a, Point b) {
        return (Math.abs(a.x - b.x) + Math.abs(a.y - b.y)); // Manhattan
    }

    public void translate(Point vector) {
        this.add(vector);
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