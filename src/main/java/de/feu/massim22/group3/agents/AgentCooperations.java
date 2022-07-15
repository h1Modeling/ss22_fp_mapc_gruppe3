package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Point;
import java.util.List;
import java.util.ArrayList;
import massim.protocol.data.TaskInfo;

public class AgentCooperations {
    public static List<Cooperation> cooperations = new ArrayList<Cooperation>();
    
    public static synchronized void setCooperation(Cooperation cooperation) {
        remove(cooperation);
        cooperations.add(cooperation);
    }
    
    public static boolean exists(Cooperation cooperation) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.equals(cooperation.task) 
                    && cooperations.get(i).master.equals(cooperation.master) 
                    && cooperations.get(i).helper.equals(cooperation.helper)) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static void remove(Cooperation cooperation) {       
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.equals(cooperation.task) 
                    && cooperations.get(i).master.equals(cooperation.master) 
                    && cooperations.get(i).helper.equals(cooperation.helper)) {
                cooperations.remove(i);
                break;
            } 
        }
    }
    
    public static Status getStatusMaster(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper) {       
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.equals(task) 
                    && cooperations.get(i).master.equals(master) 
                    && cooperations.get(i).helper.equals(helper)) {
                return  cooperations.get(i).statusMaster;
            } 
        }
        
        return Status.New;
    }
    
    public static Status getStatusHelper(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper) {       
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.equals(task) 
                    && cooperations.get(i).master.equals(master) 
                    && cooperations.get(i).helper.equals(helper)) {
                return  cooperations.get(i).statusHelper;
            } 
        }
        
        return Status.New;
    }
       
    public record Cooperation(TaskInfo task, BdiAgentV2 master, Status statusMaster, BdiAgentV2 helper, Status statusHelper) {}
}



