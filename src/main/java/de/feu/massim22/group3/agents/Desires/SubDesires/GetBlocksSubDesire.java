package de.feu.massim22.group3.agents.Desires.SubDesires;

import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

import java.awt.Point;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;

import de.feu.massim22.group3.agents.BdiAgent;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Belief.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.logging.AgentLogger;

public class GetBlocksSubDesire extends SubDesire {

    Map<Integer, ArrayList<Thing>> taskBlocks = new TreeMap<Integer, ArrayList<Thing>>();
    private Belief belief;
    private TaskInfo curTask;
    // Steps 
    private int deadlineTol = 10;

    public GetBlocksSubDesire(BdiAgent agent) {
        super(agent);
    }

    void setType() {
        this.subDesireType = SubDesires.GET_BLOCKS;
    }

    @Override
    public Action getNextAction() {
        return goToClosestRequiredDispenser();
    }

    @Override
    public boolean isExecutable() {
        // Copy reference to agent beliefs and current Task in instance variable for easier access
        belief = agent.getAgentBelief();
        curTask = agent.getIntention().getCurrentTask();

        // Check if a Task is already in the Intension and if it still exists in game
        if (curTask != null) {
            for (TaskInfo taskInfo : belief.getTaskInfo()) {
                // Check if tasks are equal and deadline 
                //TODO better Deadline check by estimating the steps necessary to complete the Task
                if (tasksAreEqual(taskInfo, curTask) && curTask.deadline > belief.getStep() + deadlineTol) {
                    AgentLogger.info(agent.getName(), "Continuing working on Task " + taskInfo.toJSON());
                    return true;
                }
            }
        }
        // If task does not exist anymore choose a new task if dispenser locations are known:
        // For now: Take first task were all the required block dispensers are known
        for (TaskInfo taskInfo : belief.getTaskInfo()) {
            if (taskDispenserPositionsKnown(taskInfo) && taskInfo.deadline > belief.getStep() + deadlineTol) {
                agent.getIntention().setCurrentTask(taskInfo);
                AgentLogger.info(agent.getName(), "Starting to work on Task " + taskInfo.toJSON());
                fillTaskBlockHashMap(taskInfo);
                return true;
            }
        }
        // If no task exists where all dispenser locations are known SubDesire will not be executed
        return false;
    }

    @Override
    public boolean isDone() {
        // TODO Check attached blocks and blocks in taskBlocks --> if equal then all blocks are available
        return false;
    }

    private void fillTaskBlockHashMap(TaskInfo taskInfo) {
        taskBlocks.clear();
        // Adding TaskBlocks according to order in which they are needed
        for (Thing thing : taskInfo.requirements) {
            if(taskBlocks.containsKey(thing.x + thing.y)) {
                taskBlocks.get(thing.x + thing.y).add(thing);
            }
            else {
                taskBlocks.put(thing.x + thing.y, new ArrayList<Thing>());
                taskBlocks.get(thing.x + thing.y).add(thing);
            }
        }
        AgentLogger.fine(agent.getName(), "taskBlocks: " + taskBlocks.toString());
    }

    private boolean taskDispenserPositionsKnown(TaskInfo taskInfo) {
        for (Thing thing : taskInfo.requirements) {
            if (!dispenserPositionKnown(getDispenserFromThing(thing))) {
                return false;
            }
        }
        return true;
    }

    // Convert Block-String to Dispenser Type
    private CellType getDispenserFromThing(Thing thing) {
        int blockTypeNumber = Integer.parseInt(thing.type.substring(1, 2));
        return CellType.valueOf("DISPENSER_" + blockTypeNumber);
    }

    private boolean dispenserPositionKnown(CellType dispenserType) {
        for (ReachableDispenser reachableDispenser : agent.getAgentBelief().getReachableDispensers()) {
            if (reachableDispenser.type() == dispenserType) {
                return true;
            }
        }
        return false;
    }

    // It seems like task names cannot occur multiple times. Therefore simple compare of task name is sufficient.
    private boolean tasksAreEqual(TaskInfo taskInfo1, TaskInfo taskInfo2) {
        if (taskInfo1.name.equals(taskInfo2.name)){
            return true;
        }
        else {
            return false;
        }
    }

    private boolean alreadyAttached(Thing thing) {
        //TODO
        return false;
    }

    private Action goToClosestRequiredDispenser(){
        // Find closest dispenser
        int minDist = Integer.MAX_VALUE;
        ReachableDispenser closestDispenser = null;
        // Go through Tree Map from closest to more distant blocks
        for (int blockPosition : taskBlocks.keySet()) {
            for (Thing thing : taskBlocks.get(blockPosition)) {
                if (alreadyAttached(thing)) {
                    continue;
                }
                CellType dispenserType = getDispenserFromThing(thing);
                List<ReachableDispenser> reachableDispensers = belief.getReachableDispensers();
                for (ReachableDispenser reachableDispenser : reachableDispensers) {
                    if (dispenserType.equals(reachableDispenser.type())
                            && reachableDispenser.distance() < minDist) {
                        minDist = reachableDispenser.distance();
                        closestDispenser = reachableDispenser;
                    }
                }
                if (closestDispenser != null) {
                    break;
                }
            }
            if (closestDispenser != null) {
                break;
            }
        }
        String nextDirs = closestDispenser.nextDirections();
        return new Action("move", new Identifier(nextDirs.substring(0, 1)));
    }
}
