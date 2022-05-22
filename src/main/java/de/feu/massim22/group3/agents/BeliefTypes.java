package de.feu.massim22.group3.agents;

import java.awt.Point;

import de.feu.massim22.group3.map.CellType;

public class BeliefTypes {

	public record ReachableGoalZone(Point position, int distance, int direction) {
		public String toString() {
			String dir = DirectionUtil.intToString(direction);
			return "Reachable Goalzone at (" + position.x + "/" + position.y + ") with distance " + distance
					+ " in direction " + dir;
		}
	}

	public record ReachableRoleZone(Point position, int distance, int direction) {
		public String toString() {
			String dir = DirectionUtil.intToString(direction);
			return "Reachable Rolezone at (" + position.x + "/" + position.y + ") with distance " + distance
					+ " in direction " + dir;
		}
	}

	public record ReachableDispenser(Point position, CellType type, int distance, int direction) {
		public String toString() {
			String dir = DirectionUtil.intToString(direction);
			return "Reachable " + type.name() + " at (" + position.x + "/" + position.y + ") with distance " + distance
					+ " in direction " + dir;
		}
	}
}
