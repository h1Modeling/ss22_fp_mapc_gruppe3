package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Point;
import java.util.List;
import java.util.ArrayList;

public class AgentMeetings {
    public static List<Meeting> meetings = new ArrayList<Meeting>();
    
    static int exists;
    
    public static void add(Meeting meeting) {
        if (exists(meeting)) {
            meetings.remove(exists);
        }
        
        meetings.add(meeting);
    }
    
    public static boolean exists(Meeting meeting) {
        boolean result = false;
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.equals(meeting.agent1) && meetings.get(i).agent2.equals(meeting.agent2)) {
                exists = i;
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static int getDistance(Meeting meeting) {
        int distance = 0;
        Point nowAgent1 = Point.castToPoint(meeting.agent1().belief.getPosition());
        Point nowAgent2 = getPositionAgent2(meeting);
        distance = Point.distance(nowAgent1, nowAgent2);
        
        return distance;
    }
    
    public static Point getPositionAgent2(Meeting meeting) {
        return new Point(Point.castToPoint(meeting.agent2().belief.getPosition()))
                .add(new Point(meeting.posAgent1()).add(new Point(meeting.relAgent2()).sub(meeting.posAgent2())));
    }
    
    public static List<Meeting> find(BdiAgentV2 agent) {
        List<Meeting> resultList = new ArrayList<Meeting>(); 
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.equals(agent)) {
                resultList.add(meetings.get(i));
            } 
        }
        
        return resultList;
    }  
    
    public record Meeting(BdiAgentV2 agent1, Point relAgent1,  Point posAgent1, BdiAgentV2 agent2, Point relAgent2,  Point posAgent2) {}
}


