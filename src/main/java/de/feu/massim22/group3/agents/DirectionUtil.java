package de.feu.massim22.group3.agents;

import java.awt.Point;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;

public class DirectionUtil {
	public static String intToString(int direction) {
        String s = String.valueOf(direction).replaceAll("0", "w").replaceAll("1", "n").replaceAll("2", "e").replaceAll("3", "s")
                .replaceAll("4", "w").replaceAll("5", "n");

        return new StringBuilder(s).reverse().toString();
    }
	
	public static String firstIntToString(int direction) {
        String s = intToString(direction);

        return s.substring(0,1);
    }

	public static int stringToInt(String direction) {
        String s = String.valueOf(direction).replaceAll("n", "1").replaceAll("e", "2").replaceAll("s", "3")
                .replaceAll("w", "4");

        return Integer.parseInt(s);
    }

	public static Point getCellInDirection(String direction) {
        int x = 0;
        int y = 0;

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
	
	   public static Point getCellInDirection(Point point, String direction) {
	        int x = point.x;
	        int y = point.y;

	        switch (direction) {
	        case "n":
	            x += 0;
	            y += -1;
	            break;
	        case "e":
	            x += 1;
	            y += 0;
	            break;
	        case "s":
	            x += 0;
	            y += 1;
	            break;
	        case "w":
	            x += -1;
	            y += 0;
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
	
	// ist a soll b  cw clockwise  ccw counter clockwise
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
	
	//inDirection Wert in data bei ReachableDispenser
	public static String getDirectionDispenser(String inDirection) {
        String result = " ";
        
        switch(inDirection) {
        case"n":
        	result = "s";
        	break;
        case"e":
        	result = "w";
        	break;
        case"s":
        	result = "n";
        	break;
        case"w":
        	result = "e";
        	break;
        }

        return result;
    }
	
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
}
