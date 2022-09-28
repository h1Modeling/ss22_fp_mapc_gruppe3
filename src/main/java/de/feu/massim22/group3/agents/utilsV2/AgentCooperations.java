package de.feu.massim22.group3.agents.utilsV2;

import de.feu.massim22.group3.agents.BdiAgentV2;
//import de.feu.massim22.group3.utils.logging.AgentLogger;

import java.util.List;
import java.util.ArrayList;

import massim.protocol.data.TaskInfo;

/**
 * The class <code>AgentCooperations</code> contains all the important methods for a agents cooperation. 
 * Agents that are in a cooperation are working together on the same multi-block-task.
 * ExploreMapSizeDesire is also done with the help of an AgentCooperation.
 * 
 * @author Melinda Betz
 */
public class AgentCooperations {
    private static List<Cooperation> cooperations = new ArrayList<Cooperation>();
    private static int maxMaster = 2;
    private static int max2BMaster = 1;
    private static int max3BMaster = 1;
    private static int maxTypes = 3;
    private static int[] scores = {0, 0, 0, 0};
    
    /**
     * After exploring the map size, the resulting size is set here.
     */
    public static Point mapSize = new Point(500, 500);
    
    /**
     * A score is set.
     * 
     * @param index - int[] new score 
     */
    public static  void setScore(int index) {
       scores[index]++;
    }
    
    /**
     * A score is returned.
     *
     * @param index - tasks with index amount of blocks
     * @return score
     */
    public static int getScore(int index) {
       return scores[index];
    }
    
    /**
     * A cooperation is being set.
     * 
     * @param cooperation - the cooperation that is being set 
     */
    public static synchronized void setCooperation(Cooperation cooperation) {
        remove(cooperation);
        cooperations.add(cooperation);
    }
    
    /**
     * Sets the status of the master.
     * 
     * @param task - the task which is to be done
     * @param agent - the agent which is the master
     * @param status - the status which is being set 
     */
    public static synchronized void setStatusMaster(TaskInfo task, BdiAgentV2 agent, Status status) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName())
                    ||  (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName())))) {
                setCooperation(cooperations.get(i).setStatusMaster(status));
                break;
            } 
        }
    }
    
    /**
     * Sets the status of the helper.
     * 
     * @param task - the task which is to be done
     * @param agent - the agent which is the helper
     * @param status - the status which is being set 
     */
    public static synchronized void setStatusHelper(TaskInfo task, BdiAgentV2 agent, Status status) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName())
                    ||  (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName())))) {
                setCooperation(cooperations.get(i).setStatusHelper(status));
                break;
            } 
        }
    }
    
    /**
     * Sets the status of the helper2.
     * 
     * @param task - the task which is to be done
     * @param agent - the agent which is the helper2
     * @param status - the status which is being set 
     */
    public static synchronized void setStatusHelper2(TaskInfo task, BdiAgentV2 agent, Status status) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || cooperations.get(i).helper.getName().equals(agent.getName())
                    ||  (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName())))) {
                setCooperation(cooperations.get(i).setStatusHelper2(status));
                break;
            } 
        }
    }
    
    /**
     * Gets the number of active masters for a certain task.
     * 
     * @param taskSize - the size of a certain task
     * 
     *  @return the number of active masters
     */
    public static int getCountMaster(int taskSize) {
        int result = 0;
        
        for (Cooperation coop : cooperations) {
            if (taskSize == 0 || coop.task().requirements.size() == taskSize)
                result++;
        }
        
        return result;
    }
    
    /**
     * Gets the maximum count  of active masters for a certain task.
     * 
     * @param taskSize - the size of a certain task
     * 
     *  @return the max of active masters
     */
    public static int getMaxMaster(int taskSize) {
        int result = 0;
        
        if (taskSize == 0)
            result = maxMaster;
        else if  (taskSize == 2)
            result = max2BMaster;
        else if  (taskSize == 3)
            result = max3BMaster;
       
        return result;
    }
    
    /**
     * Gets the maximum amount of blocks form one block type that can be used at the same time.
     * 
     *  @return the amount of blocks
     */
    public static int getMaxTypes() {      
        return maxTypes;
    }
    
    /**
     * Proves if a agent is a possible master.
     * 
     * @return if the agent is a possible master or not
     */
    public static boolean anotherMasterIsPossible() {
        if (cooperations.size() < maxMaster)  
            return true;
        
        return false;
    }
    
    /**
     * Checks if a cooperation exists for a certain task.
     * 
     * @param task - the info from the task which the cooperation is working on
     * @param agent - the agent himself
     * @param sel - the agent has to be either master or helper or helper2 that the result is true 
     * 
     *  @return if it exists or not
     */
    public static synchronized boolean exists(TaskInfo task, BdiAgentV2 agent, int sel) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (sel == 1 && cooperations.get(i).master.getName().equals(agent.getName())
                    || sel == 2 && cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached))
                    || sel == 3 && cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    /**
     * Checks if a cooperation exists for a certain task.
     * 
     * @param task - the info from the task which the cooperation is working on
     * @param agent - the agent himself
     * 
     *  @return if it exists or not
     */
    public static synchronized boolean exists(TaskInfo task, BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || (cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached)))
                    || (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName())))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    /**
     * Checks if a certain agent is part of a cooperation.
     * 
     * @param agent - the agent himself
     * 
     *  @return if he is a part or not
     */
    public static synchronized boolean exists(BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) 
                || (cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached)))
                || (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    /**
     * Detaches  a certain agent form his cooperation.
     * 
     * @param agent - the agent that is to be detached
     * 
     *  @return if the detaching was successful or not
     */
    public static synchronized boolean detachedExists(BdiAgentV2 agent) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) 
                || cooperations.get(i).helper.getName().equals(agent.getName()) && cooperations.get(i).statusHelper.equals(Status.Detached)
                ||  (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    /**
     * Converts the data of a cooperation into Strings.
     * 
     * @param cooperation - the cooperation whose data we want to print out
     * 
     *  @return the data to print out
     */
    public static String toString(Cooperation cooperation) {
        String result = "";
        
       result = cooperation.task.name + " , " 
               + cooperation.master.getName() + " , " 
               + cooperation.statusMaster + " , " 
               + cooperation.helper.getName() + " , " 
               + cooperation.statusHelper + " , "
               + (cooperation.helper2 != null ? cooperation.helper2.getName() : "") + " , " 
               + cooperation.statusHelper2;
        
        return result;
    }
    
    /**
     * Converts the data of all cooperation into Strings.
     * 
     * @param inCooperations - all the cooperation whose data we want to print out
     * 
     *  @return the data to print out
     */
    public static String toString(List<Cooperation> inCooperations) {
        String result = "";

        for (Cooperation coop : inCooperations)
       result = result + " ### " + toString(coop);
        
        return result;
    }
    
    /**
     * Updates the mapsize field.
     * 
     * @param mapSize -  new mapSize to set
     */
    public static void setMapSize(Point mapSize) {
        AgentCooperations.mapSize = mapSize;
    }
    
    /**
     * Checks if a cooperation exists .
     * 
     * @param cooperation - the cooperation we want to check on
     * 
     *  @return if it exists or not
     */
    public static synchronized boolean exists(Cooperation cooperation) {
        boolean result = false;
        
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(cooperation.task.name) 
                    && cooperations.get(i).master.getName().equals(cooperation.master.getName()) 
                    && cooperations.get(i).helper.getName().equals(cooperation.helper.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached))
                    && (cooperations.get(i).helper2 == null || cooperations.get(i).helper2.getName().equals(cooperation.helper2.getName()))) {
                result = true;
                break;
            } 
        }
        
        return result;
    }
    
    /**
     * Gets a certain cooperation.
     * 
     * @param task - the info from the task which the cooperation is working on
     * @param agent - the agent himself
     * @param sel - the agent has to be either master , helper or helper2
     * 
     *  @return the cooperation we wanted
     */
    public static synchronized Cooperation get(TaskInfo task, BdiAgentV2 agent, int sel) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (sel == 1 && cooperations.get(i).master.getName().equals(agent.getName())
                    || sel == 2 && cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached))
                    || sel == 3 && cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    /**
     * Gets a certain cooperation.
     * 
     * @param task - the info from the task which the cooperation is working on
     * @param agent - the agent himself
     * 
     *  @return the cooperation we wanted
     */
    public static synchronized Cooperation get(TaskInfo task, BdiAgentV2 agent) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && (cooperations.get(i).master.getName().equals(agent.getName())
                    || (cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached)))
                    || (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName())))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    /**
     * Gets a certain cooperation.
     * 
     * @param agent - the agent himself
     * 
     *  @return the cooperation we wanted
     */
    public static synchronized Cooperation get(BdiAgentV2 agent) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) 
                    || (cooperations.get(i).helper.getName().equals(agent.getName()) && !(cooperations.get(i).statusHelper.equals(Status.Detached)))
                    || (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    /**
     * Gets all detached cooperation.
     * 
     * @param agent - the agent himself
     * 
     *  @return the cooperations we wanted
     */
    public static synchronized Cooperation getDetached(BdiAgentV2 agent) {
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).master.getName().equals(agent.getName()) 
                    || (cooperations.get(i).helper.getName().equals(agent.getName()) && cooperations.get(i).statusHelper.equals(Status.Detached))
                    || (cooperations.get(i).helper2 != null && cooperations.get(i).helper2.getName().equals(agent.getName()))) {
                return cooperations.get(i);
            } 
        }
        
        return null;
    }
    
    /**
     * Removes a certain cooperation.
     * 
     * @param cooperation - the cooperation we want to remove
     */
    public static synchronized void remove(Cooperation cooperation) {       
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(cooperation.task.name) 
                    && cooperations.get(i).master.getName().equals(cooperation.master.getName()) 
                    && cooperations.get(i).helper.getName().equals(cooperation.helper.getName())
                    && (cooperations.get(i).helper2 == null || cooperations.get(i).helper2.getName().equals(cooperation.helper2.getName()))) {
                cooperations.remove(i);
                break;
            } 
        }
    }
    
    /**
     * Gets the status of the master.
     * 
     * @param task - the task which is being worked on by the master
     * @param master - the master himself
     * @param helper - the helper from the cooperation
     * @param helper2 - the second helper from the cooperation
     * 
     * @return the status of the master
     */
    public static synchronized Status getStatusMaster(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper, BdiAgentV2 helper2) {  
        //AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster - para: " + task.name + " , " + master.getName() + " , " + helper.getName());
        for (int i = 0; i < cooperations.size(); i++) {
            //AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster: " + cooperations.get(i).task.name + " , " + cooperations.get(i).master.getName() + " , " + cooperations.get(i).helper.getName());
            if (cooperations.get(i).task.name.equals(task.name) 
                    && cooperations.get(i).master.getName().equals(master.getName()) 
                    && cooperations.get(i).helper.getName().equals(helper.getName())
                    && (cooperations.get(i).helper2 == null || cooperations.get(i).helper2.getName().equals(helper2.getName()))) {
                //AgentLogger.info(Thread.currentThread().getName() + " getStatusMaster - in return ");
                return  cooperations.get(i).statusMaster;
            } 
        }
        
        return Status.New;
    }
    
    /**
     * Gets the status of the helper.
     * 
     * @param task - the task which is being worked on by the helper
     * @param master - the master of this helper
     * @param helper - the helper himself
     * @param helper2 - the second helper from the cooperation
     * 
     * @return the status of the helper
     */
    public static synchronized Status getStatusHelper(TaskInfo task, BdiAgentV2 master, BdiAgentV2 helper, BdiAgentV2 helper2) {    
        //AgentLogger.info(Thread.currentThread().getName() + " getStatusHelper - para: " + task.name + " , " + master.getName() + " , " + helper.getName());
        for (int i = 0; i < cooperations.size(); i++) {
            if (cooperations.get(i).task.name.equals(task.name) 
                    && cooperations.get(i).master.getName().equals(master.getName()) 
                    && cooperations.get(i).helper.getName().equals(helper.getName())
                    && (cooperations.get(i).helper2 == null || cooperations.get(i).helper2.getName().equals(helper2.getName()))) {
                return  cooperations.get(i).statusHelper;
            } 
        }
        
        return Status.New;
    }
       
    /**
     * Record with all the important data of a cooperation.
     * 
     * @param task - the task which is being worked by a cooperation
     * @param master - the master of this cooperation
     * @param statusMaster - the status of the master
     * @param helper - the helper 
     * @param statusHelper - the status of the helper
     * @param helper2 - the second helper 
     * @param statusHelper2 - the status of the second helper
     */
    public record Cooperation(TaskInfo task, BdiAgentV2 master, Status statusMaster, BdiAgentV2 helper,
            Status statusHelper, BdiAgentV2 helper2, Status statusHelper2) {
        @Override
        public String toString() {
            String result = "";

            result = task.name + " , " + master.getName() + " , " + statusMaster + " , " + helper.getName() + " , "
                    + statusHelper + " , " +  (helper2 != null ? helper2.getName() : "") + " , " + statusHelper2;

            return result;
        }

        /**
         * Sets a new Cooperation with a certain master status.
         * 
         * @param status - the master status we want to use for this new cooperation
         * 
         * @return a new cooperation
         */
        public Cooperation setStatusMaster(Status status) {
            return new Cooperation(task(), master(), status, helper(), statusHelper(), helper2(), statusHelper2());
        }
        
        /**
         * Sets a new Cooperation with a certain helper status.
         * 
         * @param status - the helper status we want to use for this new cooperation
         * 
         * @return a new cooperation
         */
        public Cooperation setStatusHelper(Status status) {
            return new Cooperation(task(), master(), statusMaster(), helper(), status, helper2(), statusHelper2());
        }
        
        /**
         * Sets a new Cooperation with a certain helper2 status.
         * 
         * @param status - the helper2 status we want to use for this new cooperation
         * 
         * @return a new cooperation
         */
        public Cooperation setStatusHelper2(Status status) {
            return new Cooperation(task(), master(), statusMaster(), helper(), statusHelper(), helper2(), status);
        }
    }
}



