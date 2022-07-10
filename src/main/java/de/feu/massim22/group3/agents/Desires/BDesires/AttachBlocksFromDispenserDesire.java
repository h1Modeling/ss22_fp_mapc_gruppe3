package de.feu.massim22.group3.agents.Desires.BDesires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import de.feu.massim22.group3.utils.logging.AgentLogger;
import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.DirectionUtil;
import de.feu.massim22.group3.agents.Reachable.ReachableDispenser;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.Convert;
import massim.protocol.data.Thing;
import massim.protocol.data.TaskInfo;

public class AttachBlocksFromDispenserDesire extends BeliefDesire {

    private Thing block;
    private List<CellType> dispensers = new ArrayList<CellType>();
    private List<Thing> requirements;
    private String supervisor;
    SortedMap<Integer, ArrayList<Thing>> taskBlocks = new TreeMap<Integer, ArrayList<Thing>>();
    private boolean blockRequested = false;
    
    // Current task orientation relative to agent. If 0 then agent could submit task
    // immediately. in all other cases the agent has to rotate to the correct orientation first.
    //     3
    // 4 Agent 2  if agent is on dispenser then 0
    //     1
    int curTaskOrientation = 1;

    public AttachBlocksFromDispenserDesire(Belief belief, List<Thing> requirements, String supervisor) {
        super(belief);
        this.requirements = requirements;
        this.supervisor = supervisor;
        
        for (Thing t : requirements) {
            dispensers.add(Convert.blockNameToDispenser(t));
        }

        // Adding TaskBlocks according to order in which they are needed (Manhattan distance)
        for (Thing thing : requirements) {
            int manhattenDist = Math.abs(thing.x) + Math.abs(thing.y);
            if(taskBlocks.containsKey(manhattenDist)) {
                taskBlocks.get(manhattenDist).add(thing);
            }
            else {
                taskBlocks.put(manhattenDist, new ArrayList<Thing>());
                taskBlocks.get(manhattenDist).add(thing);
            }
        }
//        AgentLogger.fine(belief.getAgentName(), "taskBlocks: " + taskBlocks.toString());
    }

    @Override
    public BooleanInfo isFulfilled() {
        //TODO instert check for correct shape of attached things
//        for (Thing t : belief.getAttachedThings()) {
//            if (Math.abs(t.x) <= 1 && Math.abs(t.y) <= 1 && t.type.equals(Thing.TYPE_BLOCK)
//                && t.details.equals(block.type)) {
//                return new BooleanInfo(true, "");
//            }
//        }
        return new BooleanInfo(false, "Method not implemented correctly yet");
    }

    @Override
    public BooleanInfo isExecutable() {
        for (CellType dispenser : dispensers) {
            ReachableDispenser rd = belief.getNearestDispenser(dispenser);
            Point p = belief.getNearestRelativeManhattenDispenser(Convert.cellTypeToThingDetail(dispenser));
            boolean value = rd != null || p != null;
            String info = !value ? "No dispenser " + Convert.cellTypeToThingDetail(dispenser) + " visible" : ""; 
            if(value == false) {
                return new BooleanInfo(value, info);
            }
        }
        return new BooleanInfo(true, "");
    }

    List<Thing> determineNextNotAttachedBlocks() {
        // List of Things with smallest Manhatten distance that are not attached yet
        List<Thing> nextNotAttachedBlocks = new ArrayList<Thing>();
        int manhDistFirstNotAttached = 0; // Manhatten distance of first not attached block
        // Iterate over needed Blocks of task in the order they are needed (Manhatten distance
        // relative to agent)
        Iterator<Integer> iterator = taskBlocks.keySet().iterator();
        while(iterator.hasNext()) {
            int curManhattenDist = (int)iterator.next();
            ArrayList<Thing> things = taskBlocks.get(curManhattenDist);
            for (Thing taskThing : things) {
                // Check if already attached
                boolean alreadyAttached = false;
                for (Thing attachedThing : belief.getThings()) {
                    if (attachedThing.type.equals(taskThing.type) &&
                            attachedThing.details.equals(taskThing.details) &&
                            attachedThing.x == taskThing.x &&
                            attachedThing.y == taskThing.y) {
                        alreadyAttached = true;
                        break; // break attached things loop
                    }
                }
                if (!alreadyAttached) {
                    nextNotAttachedBlocks.add(taskThing);
                    manhDistFirstNotAttached = curManhattenDist;
                }
            }
            // if not attached things where found for the current Manhatten distance
            if (manhDistFirstNotAttached != 0 && curManhattenDist == manhDistFirstNotAttached) {
                    return nextNotAttachedBlocks;
            }
        }
        // If all things from Task are already attached
        return null;
    }

    Thing transformBlockCoords(Thing b, int toPosition) {
        switch (toPosition) {
        case 1:
            return new Thing(b.x, b.y, b.type, b.details);
        case 2:
            return new Thing(b.y, -b.x, b.type, b.details);
        case 3:
            return new Thing(-b.x, -b.y, b.type, b.details);
        case 4:
            return new Thing(-b.y, b.x, b.type, b.details);
        default:
            return null;
        }
    }

    int determineDirectionCode(int x, int y) {
    //     3
    // 4 Agent 2  if agent is on dispenser then 0
    //     1
        if (x == 0 && y == 0) {
            return 0;
        }
        else if (y > 0 && -y <= x && x < y) {
            return 1;
        }
        else if (x > 0 && y<= x && -x < y) {
            return 2;
        }
        else if (y < 0 && -y >= x && x > y) {
            return 3;
        }
        else {return 4;}
    }

    int getOppositeDirecitonCode(int dirCode) {
    //     3
    // 4 Agent 2  if agent is on dispenser then 0
    //     1
        switch (dirCode) {
        case 1:
            return 3;
        case 2:
            return 4;
        case 3:
            return 1;
        case 4:
            return 2;
        default:
            return 0;
        }       
    }

    String getDirectionStringFromCode(int dirCode) {
    //     3
    // 4 Agent 2  if agent is on dispenser then 0
    //     1
        switch (dirCode) {
        case 1:
            return "s";
        case 2:
            return "e";
        case 3:
            return "n";
        case 4:
            return "w";
        default:
            return "";
        }       
    }

    Point getCoordsFromDirectionCode(int dir, int dist) {
        if (dir == 1) {
            return new Point(0, 1 * dist);
        }
        else if (dir == 2) {
            return new Point(1 * dist, 0);
        }
        else if (dir == 3) {
            return new Point(0, -1 * dist);
        }
        else if (dir == 4) {
            return new Point(-1 * dist, 0);
        }
        else {return new Point(0, 0);}
    }

    List<Integer> determineEmptyAgentSides() {
        ArrayList<Integer> emptySides = new ArrayList<>(Arrays.asList(1, 2, 3, 4));
        for (Thing t : belief.getAttachedThings()) {
            //TODO maybe check more outwards too (e. g. x=0 and y=2) in case such tasks exist
            if (t.type.equals("block")) {
                if (t.x == 0 && t.y == 1) {emptySides.remove(Integer.valueOf(1));}
                else if (t.x == 1 && t.y == 0) {emptySides.remove(Integer.valueOf(2));}
                else if (t.x == 0 && t.y == -1) {emptySides.remove(Integer.valueOf(3));}
                else if (t.x == -1 && t.y == 0) {emptySides.remove(Integer.valueOf(4));}
            }
        }
        return emptySides;
    }

    String getRotationDirection(int disDir, List<Integer> emptySides) {
        // TODO better logic possible
        if (emptySides.contains((Integer)disDir)) {
            return "";
        }
       return "CCW";
    }

    boolean checkBlockAvailability(Thing block, Point disPos) {
        // Returns true if a block of a certain Type is on a dispenser of the same type
        for (Thing t : belief.getThings()) {
            if (t.type.equals("block") && t.details.equals(block.type)
                    && t.x == block.x && t.y == block.y &&
                    !(belief.getAttachedThings().contains(t))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ActionInfo getNextActionInfo() {

        // Calculate basic information about task in every step
        List<Thing> nextBlocks = determineNextNotAttachedBlocks();
        AgentLogger.fine(belief.getAgentName(), "Next Blocks: " + nextBlocks.toString());

        // Determine Block with closest dispenser
        Thing block = null;
        ReachableDispenser rd = null;
        Point disPos = null;
        Integer manDisBlock = null;
        for (Thing cur_Block : nextBlocks) {
            CellType dispenser = Convert.blockNameToDispenser(cur_Block);
            Point cur_p = belief.getNearestRelativeManhattenDispenser(cur_Block.type);
            int cur_manhattenDistance = cur_p != null ? Math.abs(cur_p.x) + Math.abs(cur_p.y) : 1000;
            ReachableDispenser cur_rd = belief.getNearestDispenser(dispenser);
            if (block == null || cur_manhattenDistance < manDisBlock) {
                block = cur_Block; rd = cur_rd; disPos = cur_p;
                manDisBlock = cur_manhattenDistance;
            }
        }
        
//        if (disPos == null) {
//            // Not exact dispenser position but the cell next to the dispenser!
//            disPos = rd.position();
//            manDisBlock = rd.distance();
//        }

        AgentLogger.fine(belief.getAgentName(), "Closest dispenser of next blocks: " + block.toString());
        
        // Move agent next to dispenser (where no block is attached)
        if (disPos != null) {
            blockRequested = checkBlockAvailability(block, disPos);
        }
        // Activate separate control when close enough to dispenser to move to correct position
        // to request block
        if (!blockRequested && disPos != null && manDisBlock <= 2 * (Math.abs(block.x) + Math.abs(block.y))) {
            // Direction code relative to Agent
            int disDir = determineDirectionCode(disPos.x, disPos.y);
            AgentLogger.fine(belief.getAgentName(), "Getting dispenser in direction of Agent: " + disDir);
            List<Integer> emptySides = determineEmptyAgentSides();
            // TODO what if there are no empty sides?
            Point destRelativeToDispenser = getCoordsFromDirectionCode(getOppositeDirecitonCode(disDir), manDisBlock);
            Point relativeCoords = new Point(disPos.x, disPos.y);
            relativeCoords.translate(destRelativeToDispenser.x, destRelativeToDispenser.y);
            if (!(relativeCoords.equals(new Point(0, 0)))){
                // move to line with dispenser
                AgentLogger.fine(belief.getAgentName(), "Move to line with dispenser.");
                return getActionForMove(getDirectionToRelativePoint(relativeCoords), getName());
            }

            // Rotate with free side to dispenser
            if (getRotationDirection(disDir, emptySides) != "") {
                AgentLogger.fine(belief.getAgentName(), "Rotating to empty side.");
                return ActionInfo.ROTATE_CCW(getName());
            }
            
            // move close to dispenser
            destRelativeToDispenser = getCoordsFromDirectionCode(getOppositeDirecitonCode(disDir), 1);
            relativeCoords = new Point(disPos.x, disPos.y);
            relativeCoords.translate(destRelativeToDispenser.x, destRelativeToDispenser.y);
            if (!(relativeCoords.equals(new Point(0, 0)))) {
                return getActionForMove(getDirectionToRelativePoint(relativeCoords), getName());
            }
            // Request Block
            return ActionInfo.REQUEST(getDirectionStringFromCode(disDir), getName());
        }
        
        
        // Attach next block
        if (blockRequested && disPos != null && manDisBlock <= 2 * (Math.abs(block.x) + Math.abs(block.y))) {
            int disDir = determineDirectionCode(disPos.x, disPos.y);
            return ActionInfo.ATTACH(getDirectionStringFromCode(disDir), getName());
        }
        
        
        // Move towards dispenser
        if (rd != null && rd.distance() < 2 * manDisBlock) {
            String dir = DirectionUtil.intToString(rd.direction());
            if (dir.length() > 0) {
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }

        // Manhatten
        String dir = getDirectionToRelativePoint(disPos);
        return getActionForMove(dir, getName());

    }

    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
    }
}
