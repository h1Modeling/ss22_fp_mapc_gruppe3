package de.feu.massim22.group3.agents.desires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.utils.DirectionUtil;
import de.feu.massim22.group3.agents.belief.reachable.ReachableGoalZone;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;


/**
 * Desire to get the best clearing role and then wait in a goal zone for enemies with 
 * blocks, then use clear action on them until they are deactivated and then use 
 * clear action on the blocks they were carrying.
 * 
 * @author Phil Heger
 *
 */
public class GuardGoalZoneDesire extends BeliefDesire {


    boolean lastActionWasClearOnBlock = false;
    int clearedBlocks = 0;
    int[] clearDamage = new int[] {32, 16, 8, 4, 2, 1};
    int clearEnergyCost = 2;
    int stepRecharge = 1;
    // Set max. Attack distance (distance at which agent will be attacking)
    final int maxAttackDistance = 1;

    // Flag to indicate if the target GZ was reached once (after that the agent can move more freely)
    boolean initialReachedGZ = false;

    // State of current explore goal zone (when no enemy with blocks is visible)
    final String[] directionArray = {"n", "s", "e", "w"};
    String curDirection;
    int curExploreDirection = 0;

    // Information about current targetEnemy
    Point targetEnemyLastPosition = null;
    int targetEnemyEnergy = 100;
    int lastDistToTargetEnemy = 100;
    boolean lastActionWasClearOnEnemy = false;
    List<Point> blocksToClear = null;

    // Positions of all enemies during the previous step
    List<Point> oldEnemyPositions = new ArrayList<Point>();

    // For path finding
    Point oldDestPoint = null;
    Stack<String> dirStack = null;

    public GuardGoalZoneDesire(Belief belief, String dir, String supervisor) {
        super(belief);
        curDirection = dir;
        AgentLogger.info(Thread.currentThread().getName() +
                " runSupervisorDecisions - Start GuardGoalZoneDesire with direction "
                + curDirection);
        String[] neededActions = {"clear"};
        precondition.add(new ActionDesire(belief, neededActions));
    }

    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a != null) {
            return a;
        }


        // Count successfully cleared blocks
        if (lastActionWasClearOnBlock) {
            if (belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                clearedBlocks += 1;
                AgentLogger.info(belief.getAgentShortName() + " GGZD", "Cleared Blocks: " + clearedBlocks);
            }
            else {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Result of last Clear on block " + belief.getLastActionResult());
            }
        lastActionWasClearOnBlock = false;
        }


        // Keep track of energy level of targetEnemy
        if (lastActionWasClearOnEnemy && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Last clear was a success: " + targetEnemyEnergy);
            if (lastDistToTargetEnemy <= clearDamage.length){
                targetEnemyEnergy = targetEnemyEnergy - clearDamage[lastDistToTargetEnemy - 1] + stepRecharge;
                AgentLogger.info(belief.getAgentShortName() + " GGZD",
                        "New presumed energy level of targetAgent: " + targetEnemyEnergy);
            }
            // this case should never occur:
            else {
                AgentLogger.severe(belief.getAgentShortName() + " GGZD", "Distance during last clear too high.");
            }
        }
        lastActionWasClearOnEnemy = false;


        // Go to GZ
        //---------
        // Set flag when agent reaches GZ for the first time
        if (!initialReachedGZ && isInGoalZone()) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Agent reached GZ");
            initialReachedGZ = true;
        }
        // If not yet in GZ --> go to GZ
        if (!initialReachedGZ) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Agent was not yet in GZ");
            Point destPoint = getCornerPoint(curDirection);
            
            // Move with vision when close enough
            if (destPoint != null && getDistance(destPoint) > 0) {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Destination point in GuardGoalZoneDesire " + destPoint.toString());
//                String dir = getDirectionToRelativePoint(destPoint);
//                return getActionForMove(dir, getName());
                return makeMove(destPoint);
            }
            // Move with path finder if too far away and 
            if (destPoint == null) {
                // Move to goal zone with path finder
                ReachableGoalZone rgz = belief.getNearestGoalZone();
                if (rgz != null) {
                    AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Moving with path finder.");
                    String dir = DirectionUtil.intToString(rgz.direction());
                    if (dir.length() > 0) {
                        return getActionForMove(dir.substring(0, 1), getName());
                    }
                }
            }
        }


        // Find target Enemy
        //------------------
        // Check if old target enemy exists and get its new position and check if it still is
        // carrying blocks. If there is no old target enemy get a new one. If there is none
        // available set target enemy to null.
        Point targetEnemy = null;
        if (targetEnemyLastPosition != null) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Target enemy old position " + targetEnemyLastPosition.toString());
            targetEnemy = getNewPositionOfTargetEnemy(targetEnemyLastPosition);
            if (targetEnemy != null) {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Target enemy new position " + targetEnemy.toString());
            }
            else {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Target enemy new position could not be calculated. Target Enemy == null.");
            }
            // Check if getNewPositionOfTargetEnemy() was successful (then not null) and 
            // that targetEnemy still has blocks and is within the goal zone
            if (targetEnemy == null
                    || getAllAdjacentThings(targetEnemy).numOfAdjBlocks() == 0
                    || !isInGoalZone(targetEnemy)) {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Target enemy reset.");
                targetEnemy = null;
                resetTargetEnemy();
            }
        }
        // If no targetEnemy yet or reset before --> select new targetEnemy
        if (targetEnemy == null) {
            Thing closestEnemy = getClosestEnemyWithBlocks();
            if (closestEnemy != null) {
                targetEnemy = new Point(closestEnemy.x, closestEnemy.y);
                targetEnemyLastPosition = targetEnemy;
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "New target enemy at " + targetEnemy.x + " " + targetEnemy.y);
            }
        }
        // Set lastDistToTargetEnemy for evaluation during next step
        lastDistToTargetEnemy = targetEnemy != null ? getDistance(targetEnemy) : 100;
        // Save enemy positions for next step
        saveCurrentEnemyPositions();

        // Patrol goal zone
        //-----------------
        // If no old or new target Enemy is available --> explore in GZ
        if (targetEnemy == null) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Explore GZ going " + directionArray[curExploreDirection]);
            Point destPoint = getCornerPoint(directionArray[curExploreDirection]);
            // Change destination if one destination is reached
            if (destPoint != null && destPoint.equals(new Point(0, 0))) {
                curExploreDirection = (curExploreDirection + 1) % directionArray.length;
            }
            // If the destination point close and occupied then go to next destination point
            else if(destPoint != null
                    && getDistance(destPoint) <= 2
                    && belief.getThingAt(destPoint) != null
                    && (belief.getThingAt(destPoint).type.equals(Thing.TYPE_ENTITY)
                            || belief.getThingAt(destPoint).type.equals(Thing.TYPE_BLOCK))) {
                curExploreDirection = (curExploreDirection + 1) % directionArray.length;
            }
            // Update destination point in case it was changed before
            destPoint = getCornerPoint(directionArray[curExploreDirection]);
            return makeMove(destPoint);
        }


        // Approach enemy agent if too far away
        //-------------------------------------
        if (targetEnemy != null && getDistance(targetEnemy) > maxAttackDistance) {
//            String dir = getDirectionToRelativePoint(targetEnemy);
//            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
//                    "Moving towards target enemy " + dir);
//            return getActionForMove(dir, getName());
            return makeMove(targetEnemy);
        }

        // Clear on enemy if presumed Energy level not 0 yet
        //--------------------------------------------------
        if (targetEnemy != null && getDistance(targetEnemy) <= maxAttackDistance
                && belief.getEnergy() > 10
                && targetEnemyEnergy > 0) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Clear on target enemy " + targetEnemy.toString());
            targetEnemyLastPosition = new Point(targetEnemy.x, targetEnemy.y);
            lastActionWasClearOnEnemy = true;
            lastDistToTargetEnemy = getDistance(targetEnemy);
            return ActionInfo.CLEAR(new Point(targetEnemy.x, targetEnemy.y), "Clear on enemy");
        }

        // Clear on blocks
        //----------------
        // If targetEnemy energyLevel is presumed 0 --> Enemy disconnects from all Blocks
        // --> clear on Blocks in enemies vicinity
        if (targetEnemy != null
                && belief.getEnergy() > 10
                && targetEnemyEnergy <= 0) {
            // more than one block might have to be cleared --> save blocks in instance variable
            // to continue clearing in next steps
            if (blocksToClear == null){
                blocksToClear = getAllAdjacentThings(targetEnemy).getBlocks();
            }
            if (blocksToClear.size() > 0) {
                Point clearBlock = blocksToClear.get(0);
                AgentLogger.info(belief.getAgentShortName() + " GGZD",
                        "Clear on block " + clearBlock.x + " " + clearBlock.y);
                blocksToClear.remove(clearBlock);
                // Reset blocksToClear-List and 
                if (blocksToClear.size() == 0) {
                    resetTargetEnemy();
                }
                lastActionWasClearOnBlock = true;
                return ActionInfo.CLEAR(new Point(clearBlock.x, clearBlock.y), "Clear on block");
            }
        }
        return ActionInfo.SKIP("Agent is stuck in getNextActionInfo of GuardDispenserDesire or does not have enough Energy for clear.");
    }

    // Very simple path finding algorithm for GGZD (because agent always gets stuck when
    // a block is between itself and the enemy that it wants to go to and cases like rotations
    // do not need to be regarded)
    private ActionInfo makeMove(Point destPoint) {
        // For safety if agent runs away from goal zone because of interaction with other agent 
        if (destPoint == null) {
            // go back to Goal Zone
            initialReachedGZ = false;
            return ActionInfo.SKIP("Agent is stuck in makeMove of GuardDispenserDesire.");
        }

        // Correct oldDestPoint for last movement of agent
        if (oldDestPoint != null) {
            if (belief.getLastAction().equals("move")
                    && belief.getLastActionResult().equals(ActionResults.SUCCESS)){
                Point lastMove = getPointFromDirection(belief.getLastActionParams().get(0));
                oldDestPoint = new Point(oldDestPoint.x + lastMove.x,
                        oldDestPoint.y + lastMove.y);
            }
        }
        if (oldDestPoint == null
//                || !oldDestPoint.equals(destPoint) // does not work because vision shifts so point is always different
                || getDistance(oldDestPoint, destPoint) <= 2 // max shift because of shifting vision is 2
                || dirStack.size() == 0) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Reinitializing Stack.");
            if (oldDestPoint != null) {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "oldDestPoint " + oldDestPoint.toString());
            }
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "destPoint " + destPoint.toString());
            dirStack = new Stack<String>();
            oldDestPoint = destPoint;
            Point nextPoint = null;
            ActionInfo nextMove = null;
            int signX = Integer.signum(destPoint.x);
            int signY = Integer.signum(destPoint.y);
            if (Math.abs(destPoint.x) <= Math.abs(destPoint.y)) {
                nextPoint = new Point(0, signY);
                nextMove = moveTo(nextPoint);
                // If move is possible
                if (nextMove != null) {
                    return nextMove;
                }
                else {
                    dirStack.push(getDirectionFromPoint(nextPoint));
                }
                if (destPoint.x != 0) {
                    nextPoint = new Point(signX, 0);
                    nextMove = moveTo(nextPoint);
                    if (nextMove != null) {
                        return nextMove;
                    }
                }
            }
            else if (Math.abs(destPoint.x) > Math.abs(destPoint.y)) {
                nextPoint = new Point(signX, 0);
                nextMove = moveTo(nextPoint);
                // If move is possible
                if (nextMove != null) {
                    return nextMove;
                }
                else {
                    dirStack.push(getDirectionFromPoint(nextPoint));
                }
                if (destPoint.x != 0) {
                    nextPoint = new Point(0, signY);
                    nextMove = moveTo(nextPoint);
                    if (nextMove != null) {
                        return nextMove;
                    }
                }
            }
        }

        // Circle around clockwise
        int maxStackSize = 5;
        while (dirStack.size() <= maxStackSize) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Stack: " + dirStack.toString());
            String lastDir = dirStack.peek();
            ActionInfo nextMove = moveTo(getPointFromDirection(lastDir));
            if (nextMove != null) {
                dirStack.pop();
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Popping direction from Stack: " + lastDir);
                return nextMove;
            }
            else {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Pushing direction to Stack: " + getCRotatedDirection(lastDir));
                dirStack.push(getCRotatedDirection(lastDir));
            }
        }

        AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Agent is stuck in makeMove of GuardDispenserDesire.");
        return ActionInfo.SKIP("Agent is stuck in makeMove of GuardDispenserDesire.");
    }

    // helper-method for makeMove()
    private ActionInfo moveTo(Point nextPoint) {
        // Determine Action
        if (nextPoint != null && isClearable(nextPoint)) {
            return ActionInfo.CLEAR(new Point(nextPoint.x, nextPoint.y),
                    "Clear on abandoned block");
        }
        else if (nextPoint != null && belief.getThingAt(nextPoint) == null) {
            return ActionInfo.MOVE(getDirectionToRelativePoint(nextPoint), "Moving");
        }
        return null;
    }

    private boolean isClearable(Point p) {
        Thing t = belief.getThingAt(p);
        if (t != null) {
            if (t.type.equals(Thing.TYPE_OBSTACLE)
                || (t.type.equals(Thing.TYPE_BLOCK)
                        && getAllAdjacentThings(p).numOfAdjEnemyAgents() == 0
                        && getAllAdjacentThings(p).numOfAdjFriendlyAgents() == 0)) {
                return true;
            }
        }
        return false;
    }

//    // boolean firstCall is to avoid endless loop
//    private ActionInfo patrolGZ(int startDirection, boolean firstCall) {
//        // To avoid endless loop due to recursive method call when agent is surrounded by blocks
//        // with friendly agents
//        if (!firstCall && startDirection == curExploreDirection) {
//            return ActionInfo.SKIP("Agent is stuck in parol goal zone of GuardGoalZoneDesire.");
//        }
//        
//        AgentLogger.fine(belief.getAgentShortName() + " GGZD",
//                "Explore GZ going " + directionArray[curExploreDirection]);
//        Point destPoint = getCornerPoint(directionArray[curExploreDirection]);
//        // Change destination if one destination is reached
//        if (destPoint != null && destPoint.equals(new Point(0, 0))) {
//            curExploreDirection = (curExploreDirection + 1) % directionArray.length;
//        }
//        // Move to point
//        if (destPoint != null && !destPoint.equals(new Point(0, 0))
//                && getDistance(destPoint) > 0) {
//            String dir = getDirectionToRelativePoint(destPoint);
//
//            // If abandoned block in front of agent --> clear on block (before it always
//            // got stuck)
//            Point destForMove = getPointFromDirection(dir);
//            if (belief.getThingAt(destForMove) != null
//                    && belief.getThingAt(destForMove).type.equals(Thing.TYPE_BLOCK)){
//                int numOfAdjFriendlyAgents = getAllAdjacentThings(destForMove).numOfAdjFriendlyAgents();
//                // If block is not close to friendly agent (and thus could be attached)
//                if (numOfAdjFriendlyAgents == 0) {
//                    return ActionInfo.CLEAR(new Point(destForMove.x, destForMove.y),
//                            "AbandonedBlock");
//                }
//                // If there is a friendly agent close to the block then change explore direction
//                else if (numOfAdjFriendlyAgents > 0) {
//                    curExploreDirection = (curExploreDirection + 1) % 4;
//                    return patrolGZ(startDirection, false);
//                }
//            }
//            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
//                    "Patrolling goal zone " + destPoint.toString());
//            return getActionForMove(dir, getName());
//        }
//        return ActionInfo.SKIP("Agent is stuck in parol goal zone of GuardGoalZoneDesire.");
//    }


    private void resetTargetEnemy() {
        AgentLogger.fine(belief.getAgentShortName() + " GGZD", "ResetTargetEnemy()");
        targetEnemyLastPosition = null;
        targetEnemyEnergy = 100;
        lastDistToTargetEnemy = 100;
        blocksToClear = null;
    }

    // Get closest enemy with Blocks, that is in the GZ
    // Close to the Blocks cannot be a friendly agent
    private Thing getClosestEnemyWithBlocks(){
        int dist = 100;
        Thing closestEnemy = null;
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity")
                    && isInGoalZone(new Point(t.x, t.y))
                    && !t.details.equals(belief.getTeam())
                    && getDistance(t) < dist) {
                AdjacentThings adjThings = getAllAdjacentThings(new Point(t.x, t.y));
                // conditions for selecting an enemy agent
                if (adjThings.numOfAdjBlocks() >= 1
//                        && adjThings.numOfAdjBlocks() >= adjThings.numOfAdjEnemyAgents()
//                        && adjThings.numOfAdjFriendlyAgents() == 0
                        ) {
                    dist = getDistance(t);
                    closestEnemy = t;
                }
            }
        }
        return closestEnemy;
    }

    // Save enemy positions of this step to enable enemy following algorithm in next step
    private void saveCurrentEnemyPositions() {
        oldEnemyPositions.clear();
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity") && ! t.details.equals(belief.getTeam())) {
                oldEnemyPositions.add(new Point(t.x, t.y));
            }
        }
    }

    // Save enemy positions of this step to enable enemy following algorithm in next step
    private List<Point> getCurrentEnemyPositions() {
        List<Point> currentEnemyPositions = new ArrayList<Point>();
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity") && ! t.details.equals(belief.getTeam())) {
                currentEnemyPositions.add(new Point(t.x, t.y));
            }
        }
        return currentEnemyPositions;
    }

    boolean isInGoalZone() {
        if (belief.getGoalZones().contains(new Point(0, 0))) {
            return true;
        }
        return false;
    }

    boolean isInGoalZone(Point p) {
        if (belief.getGoalZones().contains(p)) {
            return true;
        }
        return false;
    }

    /**
     * Get new position of a certain enemy agent depending on its position in the previous step.
     * The algorithm is based on the simplifying presumption that all agents move only one or zero
     * fields each step.
     * 
     * The algorithm does not always give back the correct position due to the presumptions it is more
     * like a good guess.
     * 
     * @param oldPosition position of agent that is to be tracked in the previous step
     * @return Presumed new position of the agent
     */
    Point getNewPositionOfTargetEnemy(Point oldEnemyPosition) {
        List<Point> currentEnemyPositions = getCurrentEnemyPositions();
        List<Point> oldEnemyPositions = new ArrayList<Point>();
        oldEnemyPositions.addAll(this.oldEnemyPositions);
        // Correct for own movement (translate oldEnemyPositions by own movement)
        if (belief.getLastAction().equals("move")
                && belief.getLastActionResult().equals(ActionResults.SUCCESS)){
            Point lastMove = getPointFromDirection(belief.getLastActionParams().get(0));
            oldEnemyPosition = new Point(oldEnemyPosition.x - lastMove.x,
                    oldEnemyPosition.y - lastMove.y);
            for (int i = 0; i < oldEnemyPositions.size(); i++) {
                Point pos = oldEnemyPositions.get(i);
                oldEnemyPositions.set(i, new Point(pos.x - lastMove.x, pos.y - lastMove.y));
            }
        }
        
        // Outer map that holds a separate map for each old position point and for each of those old points an inner
        // map with distances to new points and the corresponding new points is saved
        // {<oldPoint1> : 
        //      {0 : [<newPoint1>],
        //       1 : [<newPoint2>, <newPoint3>],
        //       3 : [<newPoint4>]}} 
        Map<Point, Map<Integer, List<Point>>> distancesOldToNewMap= new HashMap<Point, Map<Integer, List<Point>>>();
        for (Point oldPosition : oldEnemyPositions) {
            Map<Integer, List<Point>> oldPointDistancesMap = new HashMap<Integer, List<Point>>();
            for (Point newPosition : currentEnemyPositions) {
                int distance = getDistance(oldPosition, newPosition);
                if (!oldPointDistancesMap.containsKey(distance)) {
                    oldPointDistancesMap.put(distance, new ArrayList<Point>());
                }
                oldPointDistancesMap.get(distance).add(newPosition);
            }
            distancesOldToNewMap.put(oldPosition, oldPointDistancesMap);
        }

//        AgentLogger.fine(belief.getAgentShortName() + " GGZD",
//                "Trying to access " + oldEnemyPosition.toString() + " in Map " + distancesOldToNewMap.toString());

        // Simple cases when there are no other enemy agents around the tracked enemy
        if (!distancesOldToNewMap.containsKey(oldEnemyPosition)) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Key oldEnemyPosition not found in distancesOldToNewMap. Will return null.");
            return null;
        }

        // Simple cases when there are no other enemy agents around the tracked enemy
        int numOfDist0 = distancesOldToNewMap.get(oldEnemyPosition).containsKey(0) ?
                distancesOldToNewMap.get(oldEnemyPosition).get(0).size() : 0;
        int numOfDist1 = distancesOldToNewMap.get(oldEnemyPosition).containsKey(1) ?
                distancesOldToNewMap.get(oldEnemyPosition).get(1).size() : 0;

        if (numOfDist0 == 1 && numOfDist1 == 0) {
            return distancesOldToNewMap.get(oldEnemyPosition).get(0).get(0);
        }
        if (numOfDist0 == 0 && numOfDist1 == 1) {
            return distancesOldToNewMap.get(oldEnemyPosition).get(1).get(0);
        }
//        if (numOfDist1 > 1) {
//            
//        }
        // For now return agent that is closest previous agent position
        for (int d = 0; d == 10; d++) {
            if (distancesOldToNewMap.get(oldEnemyPosition).containsKey(d)) {
                return distancesOldToNewMap.get(oldEnemyPosition).get(d).get(0);
            }
        }
        return null;
    }

    Point getCornerPoint(String direction) {
        List<Point> gz = belief.getGoalZones();
        if (gz.size() > 0) {
            switch (direction){
            case "n":
                gz.sort((a, b) -> a.y - b.y);
                return gz.get(0);
            case "s":
                gz.sort((a, b) -> a.y - b.y);
                return gz.get(gz.size() - 1);
            case "w":
                gz.sort((a, b) -> a.x - b.x);
                return gz.get(0);
            case "e":
                gz.sort((a, b) -> a.x - b.x);
                return gz.get(gz.size() - 1);
            default:
                return null;
            }
        }
        return null;
    }

    AdjacentThings getAllAdjacentThings(Point p) {

        AdjacentThings adjThings = new AdjacentThings(new ArrayList<Point>(),
                new ArrayList<Point>(), new ArrayList<Point>());
        adjThings = getAdjacentThings(p, adjThings);
        return adjThings;
    }

    AdjacentThings getAdjacentThings(Point p, AdjacentThings adjThings) {
        System.out.println("AdjacentThings with point " + p.toString());
        List<Point> pointsToCheck = new ArrayList<Point>();
        pointsToCheck.add(new Point(p.x + 1, p.y));
        pointsToCheck.add(new Point(p.x - 1, p.y));
        pointsToCheck.add(new Point(p.x, p.y + 1));
        pointsToCheck.add(new Point(p.x, p.y - 1));
        for (Point curPoint : pointsToCheck) {
            Thing thingAtPositon = belief.getThingAt(curPoint);
            // Exclude Point (0, 0) because that is the agent itself
            if (thingAtPositon != null && !curPoint.equals(new Point(0, 0))) {
                if (thingAtPositon.type.equals("block")) {
                    boolean thingAdded = adjThings.addBlock(curPoint);
                    if(thingAdded) {
                        getAdjacentThings(curPoint, adjThings);
                    }
                }
                if (thingAtPositon.type.equals("entity")
                        && thingAtPositon.details.equals(belief.getTeam())) {
                    adjThings.addFriendlyAgent(curPoint);
                }
                if (thingAtPositon.type.equals("entity")
                        && !thingAtPositon.details.equals(belief.getTeam())) {
                    adjThings.addEnemyAgent(curPoint);
                }
            }
        }
        return adjThings;
    }

    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    @Override
    public String getName() {
        return "Guard Goal Zone"; 
    }

    @Override
    public boolean isGroupDesire() {
        return true;
    }

    @Override
    public int getPriority() {
        return 1100;
    }
    public void setGoalZoneDirection(String dir) {
        curDirection = dir;
    }

    class AdjacentThings {
        
        private List<Point> blocksList;
        private List<Point> friendlyAgentsList;
        private List<Point> enemyAgentsList;
        
        public AdjacentThings(List<Point> blocksList, List<Point> friendlyAgentsList,
                List<Point> enemyAgentsList) {
            this.blocksList = blocksList;
            this.friendlyAgentsList = friendlyAgentsList;
            this.enemyAgentsList = enemyAgentsList;
        }

        public boolean containsBlockAt(Point positon) {
            if (blocksList.contains(positon)) {return true;}
            return false;
        }

        public boolean containsFriendlyAgentAt(Point positon) {
            if (friendlyAgentsList.contains(positon)) {return true;}
            return false;
        }

        public boolean containsEnemyAgentAt(Point positon) {
            if (enemyAgentsList.contains(positon)) {return true;}
            return false;
        }

        public boolean addBlock(Point postion) {
            if (!containsBlockAt(postion)){
                blocksList.add(postion);
                return true;
            }
            return false;
        }

        public boolean addFriendlyAgent(Point postion) {
            if (!containsFriendlyAgentAt(postion)){
                friendlyAgentsList.add(postion);
                return true;
            }
            return false;
        }

        public boolean addEnemyAgent(Point postion) {
            if (!containsEnemyAgentAt(postion)){
                enemyAgentsList.add(postion);
                return true;
            }
            return false;
        }

        public List<Point> getBlocks() {
            return blocksList;
        }

        public List<Point> getFriendlyAgents() {
            return friendlyAgentsList;
        }

        public int numOfAdjBlocks() {
            return blocksList.size();
        }

        public int numOfAdjFriendlyAgents() {
            return friendlyAgentsList.size();
        }

        public int numOfAdjEnemyAgents() {
            return enemyAgentsList.size();
        }

        public String toString() {
            return "Blocks: " + blocksList.toString() + "  FriendlyAgents: " + friendlyAgentsList+ "  EnemyAgents: " + enemyAgentsList;
        }
    }
}
