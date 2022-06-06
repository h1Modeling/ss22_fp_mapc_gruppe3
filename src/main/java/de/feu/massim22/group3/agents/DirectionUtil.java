package de.feu.massim22.group3.agents;

import java.awt.Point;

public class DirectionUtil {
	public static String intToString(int direction) {
        String s = String.valueOf(direction).replaceAll("1", "n").replaceAll("2", "e").replaceAll("3", "s")
                .replaceAll("4", "w").replace("0", "");

        return new StringBuilder(s).reverse().toString();
    }

	public static int stringToInt(String direction) {
        String s = String.valueOf(direction).replaceAll("n", "1").replaceAll("e", "2").replaceAll("s", "3")
                .replaceAll("w", "4");

        return Integer.parseInt(s);
    }

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

    public static Point rotateCW(Point p) {
        return new Point(-p.y, p.x);
    }

    public static Point rotateCCW(Point p) {
        return new Point(p.y, -p.x);
    }

	public static String getDirection(Point from, Point to) {
        String result = " ";
        Point pointTarget = new Point(to.x - from.x, to.y - from.y);

        if (pointTarget.x == 0) {
            if (pointTarget.y < 0)
                result = "n";
            else
                result = "s";
        }

        if (pointTarget.y == 0) {
            if (pointTarget.x < 0)
                result = "w";
            else
                result = "e";
        }

        if (pointTarget.x != 0 && pointTarget.y != 0) {
            if (Math.abs(pointTarget.x) > Math.abs(pointTarget.y))
                if (pointTarget.x < 0)
                    result = "w";
                else
                    result = "e";
            else if (pointTarget.y < 0)
                result = "n";
            else
                result = "s";
        }

        return result;
    }
}
