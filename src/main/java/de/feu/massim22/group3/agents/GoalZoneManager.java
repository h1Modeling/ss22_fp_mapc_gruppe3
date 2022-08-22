package de.feu.massim22.group3.agents;

import java.util.*;

public class GoalZoneManager {
    public static List<GoalZoneManager> goalZones = new ArrayList<>();
    public Point center;
    public List<BdiAgentV2> currentTickets = new ArrayList<>();
    private int maxTickets = 3;

    private GoalZoneManager(Point center) {
        this.center = center;
    }
    
    public static GoalZoneManager getGoalZoneManager(Point center) {
        if (existsGoalZone(center)) {
            return getGoalZone(center);
        } else {
            return new GoalZoneManager(center);
        }        
    }
    
    private static boolean existsGoalZone(Point center) {
        for (GoalZoneManager gzm : goalZones) {
            if (gzm.center.equals(center)) {
                return true;
            }
        }
        return false;
    }
    
    private static GoalZoneManager getGoalZone(Point center) {
        for (GoalZoneManager gzm : goalZones) {
            if (gzm.center.equals(center)) {
                return gzm;
            }
        }
        return null;
    }
    
    public boolean getTicket(BdiAgentV2 agent) {
        if (currentTickets.size() >= maxTickets) {
            return false;
        } else {
            currentTickets.add(agent);
            return true;
        }
    }
    
    public boolean invalidateTicket(BdiAgentV2 agent) {
       /* for () {
            
        }*/
        return true;
    }
}
