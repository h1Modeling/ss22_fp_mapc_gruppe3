package de.feu.massim22.group3.agents;

import java.awt.Point;

public class DirectionUtil {
	public static String intToString(int direction) {
        String s = String.valueOf(direction).replaceAll("1", "n").replaceAll("2", "e").replaceAll("3", "s")
                .replaceAll("4", "w");

        return new StringBuilder(s).reverse().toString();
    }

	public static int stringToInt(String direction) {
        String s = String.valueOf(direction).replaceAll("n", "1").replaceAll("e", "2").replaceAll("s", "3")
                .replaceAll("w", "4");

        return Integer.parseInt(s);
    }

	public static Point getCellInDirection(String direction) {
        int x = 0;
        int y = 0;
        ;

        switch (direction) {
        case "n":
            x = 0;
            y = -1;
            break;
        case "e":
            x = 1;
            y = 0;
            break;
        case "s":
            x = 0;
            y = 1;
            break;
        case "w":
            x = -1;
            y = 0;
        }
        return new Point(x, y);
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
            if (java.lang.Math.abs(pointTarget.x) > Math.abs(pointTarget.y))
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
