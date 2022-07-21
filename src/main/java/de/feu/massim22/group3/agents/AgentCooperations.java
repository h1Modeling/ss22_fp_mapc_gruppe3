package de.feu.massim22.group3.agents;

import de.feu.massim22.group3.agents.Point;
import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.util.List;
import java.util.ArrayList;
import massim.protocol.data.TaskInfo;

public class AgentCooperations {
    public static List<Cooperation> cooperations = new ArrayList<Cooperation>();
    
    public static synchronized void setCooperation(Cooperation cooperation) {
        remove(cooperation);
        cooperations.add(cooperation);
    }
    
    public static synchronized void setStatusMaster(TaskInfo task, BdiAgentV2 agent, Status status) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName()))) {
                setCooperation(cooperations.get(i).setStatusMaster(status));
                break;
            } 
        }
    }
    
    public static synchronized void setStatusHelper(TaskInfo task, BdiAgentV2 agent, Status status) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName()))) {
                setCooperation(cooperations.get(i).setStatusHelper(status));
                break;
            } 
        }
    }
    
    public static boolean exists(TaskInfo task, BdiAgentV2 agent, int sel) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (sel == 1 && cooperations.get(i).master.getName().equals(agent.getName())
                    || sel == 2 && cooperations.get(i).helper.getName().equals(agent.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static boolean exists(TaskInfo task, BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static boolean exists(BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) 
                || cooperations.get(i).helper.getName().equals(agent.getName())) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static String toString(Cooperation cooperation) {
        String result = "";
        
       result = cooperation.task.name + " , " 
               + cooperation.master.getName() + " , " 
               + cooperation.statusMaster + " , " 
               + cooperation.helper.getName() + " , " 
               + cooperation.statusHelper;
        
        return result;
    }
    
    public static String toString(List<Cooperation> cooperations) {
        String result = "";

        for (Cooperation coop : cooperations)
       result = result + " ### " + toString(coop);
        
        return result;
    }
    
    public static boolean exists(Cooperation cooperation) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(cooperation.task.name) 
                    && cooperations.get(i).master.getName().equals(cooperation.master.getName()) 
                    && cooperations.get(i).helper.getName().equals(cooperation.helper.getName())) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    public static Cooperation get(TaskInfo task, BdiAgentV2 agent, int sel) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (sel == 1 && cooperations.get(i).master.getName().equals(agent.getName())
                    || sel == 2 && cooperations.get(i).helper.getName().equals(agent.getName()))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    public static Cooperation get(TaskInfo task, BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName()))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    public static Cooperation get(BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) || cooperations.get(i).helper.getName().equals(agent.getName())) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    public static synchronized void remove(Cooperation cooperation) {       
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(cooperation.task.name) 
                    && cooperations.get(i).master.getName().equals(cooperation.master.getName()) 
                    && cooperations.get(i).helper.getName().equals(cooperation.helper.getName())) {
                cooperations.remove(i);
                break;
            } 
        }
    }
    
    public static Status getStatusMaster(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper) {  
        AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster - para: " + task.name + " , " + master.getName() + " , " + helper.getName());
        for (int i = 0; i < cooperations.size(); i++) {
            AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster: " + cooperations.get(i).task.name + " , " + cooperations.get(i).master.getName() + " , " + cooperations.get(i).helper.getName());
            if (cooperations.get(i).task.name.equals(task.name) 
                    && cooperations.get(i).master.getName().equals(master.getName()) 
                    && cooperations.get(i).helper.getName().equals(helper.getName())) {
                AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster - in return ");
                return  cooperations.get(i).statusMaster;
            } 
        }
        
        return Status.New;
    }
    
    public static Status getStatusHelper(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper) {    
        AgentLogger.info(Thread.currentThread().getName() + " getStatusHelper - para: " + task.name + " , " + master.getName() + " , " + helper.getName());
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && cooperations.get(i).master.getName().equals(master.getName()) 
                    && cooperations.get(i).helper.getName().equals(helper.getName())) {
                return  cooperations.get(i).statusHelper;
            } 
        }
        
        return Status.New;
    }
       
    public record Cooperation(TaskInfo task, BdiAgentV2 master, Status statusMaster, BdiAgentV2 helper,
            Status statusHelper) {
        @Override
        public String toString() {
            String result = "";

            result = task.name + " , " + master.getName() + " , " + statusMaster + " , " + helper.getName() + " , "
                    + statusHelper;

            return result;
        }

        public Cooperation setStatusMaster(Status status) {
            return new Cooperation(task(), master(), status, helper(), statusHelper());
        }
        
        public Cooperation setStatusHelper(Status status) {
            return new Cooperation(task(), master(), statusMaster(), helper(), status);
        }
    }
}



