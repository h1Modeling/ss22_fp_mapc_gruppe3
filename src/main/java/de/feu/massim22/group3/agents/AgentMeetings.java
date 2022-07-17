package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.agents.AgentCooperations.Cooperation;

import java.util.List;
import java.util.ArrayList;

public class AgentMeetings {
    public static List<Meeting> meetings = new ArrayList<Meeting>();
    
    public static synchronized void add(Meeting meeting) {
        remove(meeting);
        meetings.add(meeting);
    }
    
    public static boolean exists(Meeting meeting) {
        boolean result = false;
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.getName().equals(meeting.agent1.getName()) && meetings.get(i).agent2.getName().equals(meeting.agent2.getName())) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static String toString(Meeting meeting) {
        String result = "";
        
       result = meeting.agent1.getName() + " , " 
               + Point.toString(meeting.relAgent1) + " , " 
               + Point.toString(meeting.posAgent1) + " , " 
               + meeting.agent2.getName() + " , " 
               + Point.toString(meeting.relAgent2) + " , " 
               + Point.toString(meeting.posAgent2);
        
        return result;
    }
    
    public static void remove(Meeting meeting) {       
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.getName().equals(meeting.agent1.getName()) && meetings.get(i).agent2.getName().equals(meeting.agent2.getName())) {
                meetings.remove(i);
                break;
            } 
        }
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
            if (meetings.get(i).agent1.getName().equals(agent.getName())) {
                resultList.add(meetings.get(i));
            } 
        }
        
        return resultList;
    }  
    
    public static Meeting get(BdiAgentV2 agent1, BdiAgentV2 agent2) {
       Meeting result = null; 
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.getName().equals(agent1.getName()) && meetings.get(i).agent2.getName().equals(agent2.getName())) {
                result = meetings.get(i);
                break;
            } 
        }
        
        return result;
    } 
    
    public record Meeting(BdiAgentV2 agent1, Point relAgent1,  Point posAgent1, BdiAgentV2 agent2, Point relAgent2,  Point posAgent2) {
        @Override
        public String toString() {
            String result = "";
            
           result = agent1.getName() + " , " 
                   + Point.toString(relAgent1) + " , " 
                   + Point.toString(posAgent1) + " , " 
                   + agent2.getName() + " , " 
                   + Point.toString(relAgent2) + " , " 
                   + Point.toString(posAgent2);
            
            return result;
        }
    }
}


