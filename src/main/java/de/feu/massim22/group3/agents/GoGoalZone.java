package de.feu.massim22.group3.agents;
import java.util.*;
import massim.protocol.data.Position;

public class GoGoalZone {
	
	public static List<Position> goalZone = new ArrayList<Position>();
	
	public boolean goGoalZone(Belief beliefs) {
		boolean result = false;
		
		goalZone = beliefs.getGoalZones(); // alle goalZones aus den Beliefs
		if(!goalZone.isEmpty()) {//es exisitieren goalZones
			
		}
		
		
		
		return result;
	}
}
