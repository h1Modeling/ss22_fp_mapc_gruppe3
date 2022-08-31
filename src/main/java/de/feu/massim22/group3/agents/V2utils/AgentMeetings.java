package de.feu.massim22.group3.agents.V2utils;

import de.feu.massim22.group3.agents.BdiAgentV2;
import de.feu.massim22.group3.agents.V2utils.AgentCooperations.Cooperation;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import java.util.List;
import java.util.ArrayList;

/**
 * The class <code>AgentMeetings</code> contains all the important methods for a AgentMeeting. 
 * When two agents have met they will most of the time merge into one group.
 * 
 * @author Melinda Betz
 */
public class AgentMeetings {
    public static List<Meeting> meetings = new ArrayList<Meeting>();
    
    /**
     * A meeting is being added.
     * 
     * @param meeting the meeting to add
     */
    public static synchronized void add(Meeting meeting) {
        if (remove(meeting))
            evaluateMapSize(meeting);
        else
            meeting.agent1.firstMeeting[meeting.agent2.index] = newMeeting(meeting);
            
        meetings.add(meeting);
    }
    
    /**
     * Proves if a meeting already exists.
     * 
     * @param meeting the meeting to be proved
     * 
     * @result if the meeting is already existing or not
     */
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
    
    /**
     * Converts a meeting into a String.
     * 
     * @param meeting the meeting to convert to String
     * 
     * @return the converted meeting
     */
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
    
    /**
     * Removes a meeting.
     * 
     * @param meeting the meeting to be removed
     * 
     * @return if the removing was successful or not
     */
    public static boolean remove(Meeting meeting) { 
        boolean result = false;
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.getName().equals(meeting.agent1.getName()) && meetings.get(i).agent2.getName().equals(meeting.agent2.getName())) {
                meetings.remove(i);
                result = true;
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Gets the distance between the two agents in a certain meeting.
     * 
     * @param meeting the meeting in which the two agents are
     * 
     * @return the distance between them
     */
    public static int getDistance(Meeting meeting) {
        int distance = 0;
        Point nowAgent1 = Point.castToPoint(meeting.agent1().getBelief().getPosition());
        Point nowAgent2 = getPositionAgent2(meeting);
        distance = Point.distance(nowAgent1, nowAgent2);
        
        return distance;
    }
    
    /**
     * Gets the position of the second agent in a certain meeting.
     * 
     * @param meeting the meeting in which the second agent is
     * 
     * @return the position of the second agent
     */
    public static Point getPositionAgent2(Meeting meeting) {
        return new Point(Point.castToPoint(meeting.agent2().getBelief().getPosition()))
                .add(new Point(meeting.posAgent1()).add(new Point(meeting.relAgent2()).sub(meeting.posAgent2())));
    }
    
    /**
     * Searches for a certain agent in all meetings.
     * 
     * @param agent the agent that is being searched for
     * 
     * @return the entry in the meeting list which contains the agent
     */
    public static List<Meeting> find(BdiAgentV2 agent) {
        List<Meeting> resultList = new ArrayList<Meeting>(); 
        
        for (int i = 0; i < meetings.size(); i++) {
            if (meetings.get(i).agent1.getName().equals(agent.getName())) {
                resultList.add(meetings.get(i));
            } 
        }
        
        return resultList;
    }  
    
    /**
     * Gets a meeting for two certain agents.
     * 
     * @param agent1 the first agent
     * @param agent2 the second agent
     * 
     * @return the meeting record of the two agents
     */
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
    
    private static void evaluateMapSize(Meeting meeting) {
        Meeting fm = newMeeting(meeting.agent1.firstMeeting[meeting.agent2().index]);
        Meeting m = newMeeting(meeting);

        AgentLogger.info(Thread.currentThread().getName() + " AgentMeetings.evaluateMapSize - firstMeeting: "
                + fm.toString() + " , meeting: " + m.toString());

        int width = Math.abs(m.nmpAgent2.x - fm.nmpAgent2.x) - (m.nmpAgent1.x - fm.nmpAgent1.x)
                - (m.relAgent2.x - fm.relAgent2.x);
        int height = Math.abs(m.nmpAgent2.y - fm.nmpAgent2.y) - (m.nmpAgent1.y - fm.nmpAgent1.y)
                - (m.relAgent2.y - fm.relAgent2.y);

        AgentLogger
                .info(Thread.currentThread().getName() + " AgentMeetings - width: " + width + " , height: " + height);

        if (AgentCooperations.exists(StepUtilities.exploreHorizontalMapSize, meeting.agent1)
                && AgentCooperations.exists(StepUtilities.exploreHorizontalMapSize, meeting.agent2)) {
            if (height > 0 && height != AgentCooperations.mapSize.y) {
                AgentCooperations.setMapSize(new Point(AgentCooperations.mapSize.x, height));
                StepUtilities.exploreHorizontalMapSizeFinished = true;
            }
        }

        if (AgentCooperations.exists(StepUtilities.exploreVerticalMapSize, meeting.agent1)
                && AgentCooperations.exists(StepUtilities.exploreVerticalMapSize, meeting.agent2)) {
            if (width > 0 && width != AgentCooperations.mapSize.x) {
                AgentCooperations.setMapSize(new Point(width, AgentCooperations.mapSize.y));
                StepUtilities.exploreVerticalMapSizeFinished = true;
            }
        }
    }
    
    private static Meeting newMeeting(Meeting meeting) {
        return new Meeting(meeting.agent1, meeting.relAgent1,  meeting.posAgent1,  meeting.nmpAgent1, meeting.agent2, meeting.relAgent2,  meeting.posAgent2,  meeting.nmpAgent2);
    }
    
    /**
     * The meeting record of all agent meetings.
     * 
     * @param agent1 the first agent
     * @param relAgent1 the relative position of the first agent
     * @param posAgent1 the actual position of the first agent
     * @param nmpAgent1 non modulo position of the first agent
     * @param agent2 the second agent
     * @param relAgent2 the relative position of the second agent
     * @param posAgent2 the actual position of the second agent
     * @param nmpAgent2 non modulo position of the second agent
     */
    public record Meeting(BdiAgentV2 agent1, Point relAgent1,  Point posAgent1,  Point nmpAgent1, BdiAgentV2 agent2, Point relAgent2,  Point posAgent2,  Point nmpAgent2) {       
        @Override
        public String toString() {
            String result = "";
            
           result = agent1.getName() + " , " 
                   + Point.toString(relAgent1) + " , " 
                   + Point.toString(posAgent1) + " , " 
                   + Point.toString(nmpAgent1) + " , " 
                   + agent2.getName() + " , " 
                   + Point.toString(relAgent2) + " , " 
                   + Point.toString(posAgent2) + " , "
                   + Point.toString(nmpAgent2);
            
            return result;
        }
    }
}


