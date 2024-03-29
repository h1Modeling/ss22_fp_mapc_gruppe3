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
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import de.feu.massim22.group3.utils.logging.AgentLogger;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;


/**
 * The Class <code>GuardGoalZoneDesire</code> models the desire to get
 * the best clearing role and then wait in a goal zone for enemies with 
 * blocks, then use clear action on them until they are deactivated and then use 
 * clear action on the blocks they were carrying.
 * 
 * @author Phil Heger
 *
 */
public class GuardGoalZoneDesire extends BeliefDesire {

    // Basic game parameters that are not in belief or percepts
    private int[] clearDamage = new int[] {32, 16, 8, 4, 2, 1};
    private int stepRecharge = 1;

    // Point of the goal zone that was assigned to this agent
    private Point assignedGoalZonePoint;

    // Set max. Attack distance (distance at which agent will be attacking)
    private final int maxAttackDistance = 1;

    // Flag to indicate if the target GZ was reached once (after that the agent can move more freely)
    private boolean initialReachedGZ = false;

    // State of current explore goal zone (when no enemy with blocks is visible)
    private final String[] directionArray = {"n", "e", "s", "w"};
    private int curExploreDirection = 0;

    // State instance variables about current targetEnemy
    private Point targetEnemyLastPosition = null;
    private int targetEnemyEnergy = 100;
    private int lastDistToTargetEnemy = 100;
    private boolean lastActionWasClearOnEnemy = false;

    // State instance variables about clear actions on block
    private boolean lastActionWasClearOnBlock = false;
    private int clearedBlocks = 0;
    private List<Point> blocksToClear = null;

    // Information about last attacked enemy to avoid attacking again
    private Point lastClearedEnemyPosition = null;
    private int stepsSinceCompleteClear = 0;
    private final int noAttackTime = 10;

    // Positions of all enemies during the previous step
    private List<Point> oldEnemyPositions = new ArrayList<Point>();

    // State instance variables for simple path finding
    private Point oldDestPoint = null;
    private Stack<String> dirStack = new Stack<String>();

    /**
     * Instantiates a new GuardGoalZoneDesire.
     * 
     * @param belief the belief of the agent
     * @param assignedGoalZonePoint the goal zone that was assigned to this agent from the supervisor
     * @param supervisor the supervisor of the agent group
     */
    public GuardGoalZoneDesire(Belief belief, Point assignedGoalZonePoint, String supervisor) {
        super(belief);
        this.assignedGoalZonePoint = assignedGoalZonePoint;
        String[] neededActions = {"clear"};
        precondition.add(new DisconnectAllDesire(belief));
        precondition.add(new ActionDesire(belief, neededActions));
    }

    /**
     * {@inheritDoc}
     */
    public ActionInfo getNextActionInfo() {
        ActionInfo a = fulfillPreconditions();
        if (a != null) {
            return a;
        }
        AgentLogger.info(belief.getAgentShortName() + " GGZD", "");
        AgentLogger.info(belief.getAgentShortName() + " GGZD",  "##### GGZD new step #####");


        // Count successfully cleared blocks
        if (lastActionWasClearOnBlock) {
            if (belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                clearedBlocks += 1;
                AgentLogger.info(belief.getAgentShortName() + " GGZD", "Cleared blocks: " + clearedBlocks);
            }
            else {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Result of last clear on block " + belief.getLastActionResult());
            }
        lastActionWasClearOnBlock = false;
        }


        // Keep track of energy level of targetEnemy
        //------------------------------------------
        if (lastActionWasClearOnEnemy
                && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Last clear was a success. Previous energy level: " + targetEnemyEnergy);
            if (lastDistToTargetEnemy <= clearDamage.length){
                targetEnemyEnergy = targetEnemyEnergy - clearDamage[lastDistToTargetEnemy] + stepRecharge;
                AgentLogger.info(belief.getAgentShortName() + " GGZD",
                        "New presumed energy level of targetAgent: " + targetEnemyEnergy);
            }
            // this case should never occur:
            else {
                AgentLogger.severe(belief.getAgentShortName() + " GGZD", "Distance during last clear too high.");
            }
        }
        // In case of unsuccessful clear the enemy recovers
        else if (lastActionWasClearOnEnemy
                && !belief.getLastActionResult().equals(ActionResults.SUCCESS)){
            targetEnemyEnergy += stepRecharge;
                }
        lastActionWasClearOnEnemy = false;

        // Keep track of last attacked enemy
        stepsSinceCompleteClear = lastClearedEnemyPosition != null ? ++stepsSinceCompleteClear : 0;
        if (lastClearedEnemyPosition != null) {
            lastClearedEnemyPosition = correctPointByLastMovement(lastClearedEnemyPosition);
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Enemy attacked and cleared before " + lastClearedEnemyPosition.toString());
        }


        // Go to GZ (initially)
        //---------------------
//        // Set flag when agent reaches GZ for the first time
//        if (!initialReachedGZ && isInGoalZone()) {
//            AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Agent reached GZ");
//            initialReachedGZ = true;
//        }
        // If not yet in GZ --> go to GZ
        if (!initialReachedGZ) {
            AgentLogger.info(belief.getAgentShortName() + " GGZD", "Agent was not yet in GZ");
            AgentLogger.info(belief.getAgentShortName() + " GGZD",
                    " Assigned goal zone at " + assignedGoalZonePoint.toString());
            Point gameMapSize = Navi.<INaviAgentV1>get().getGameMapSize(belief.getAgentShortName());
            // Choose correct ReachableGoalZone that was assigned to this agent
            AgentLogger.fine(belief.getAgentShortName() + " GGZD", "ReachableGoalZones " + belief.getReachableGoalZones().toString());
            for (ReachableGoalZone rgz : belief.getReachableGoalZones()) {
                // First condition to see if ReachableGoalZone was assigned to this agent
                // Second condition for to see how far away the agent is from this point
                if (DirectionUtil.pointsWithinDistance(rgz.position(), assignedGoalZonePoint, gameMapSize, 15) && rgz.distance() > 4) {
                    String dir = DirectionUtil.intToString(rgz.direction());
                    if (dir.length() > 0) {
                        return getActionForMove(dir.substring(0, 1), getName());
                    }
                }
                // if agent is close enough the goal zone should be visible in his beliefs and
                // he can start partolling.
                else if (DirectionUtil.pointsWithinDistance(rgz.position(), assignedGoalZonePoint, gameMapSize, 15) && rgz.distance() <= 4) {
                    initialReachedGZ = true;
                    AgentLogger.info(belief.getAgentShortName() + " GGZD", "Agent reached GZ");
                    break;
                }
                else {
                    AgentLogger.severe(belief.getAgentShortName() + " GGZD", "no ReachableGoalZone with given coordinates found.");
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
                targetEnemyLastPosition = new Point(targetEnemy);
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
                targetEnemyLastPosition = new Point(targetEnemy);
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
            AgentLogger.info(belief.getAgentShortName() + " GGZD",
                    "Explore GZ going " + directionArray[curExploreDirection]);
            Point destPoint = getPatrolCornerPoint(directionArray[curExploreDirection], 3);
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
            destPoint = getPatrolCornerPoint(directionArray[curExploreDirection], 3);
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
            AgentLogger.info(belief.getAgentShortName() + " GGZD",
                    "Clear on target enemy " + targetEnemy.toString());
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
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "blocksToClear " + blocksToClear.toString());
                Point clearBlock = blocksToClear.get(0);
                AgentLogger.info(belief.getAgentShortName() + " GGZD",
                        "Clear on block " + clearBlock.x + " " + clearBlock.y);
                blocksToClear.remove(clearBlock);
                // Reset blocksToClear-List and 
                if (blocksToClear.size() == 0) {
                    lastClearedEnemyPosition = targetEnemy;
                    stepsSinceCompleteClear = 0;
                    resetTargetEnemy();
                }
                lastActionWasClearOnBlock = true;
                return ActionInfo.CLEAR(new Point(clearBlock.x, clearBlock.y), "Clear on block");
            }
        }
        return ActionInfo.SKIP("Agent is stuck in getNextActionInfo of GuardDispenserDesire or does not have enough Energy for clear.");
    }

    // Simple path finding algorithm for GGZD (because with method of BeliefDesire the
    // agent always gets stuck when a block is between itself and the enemy that it wants
    // to go to and cases like rotations and attached things do not need to be regarded)
    private ActionInfo makeMove(Point destPoint) {
        // For safety if agent runs away from goal zone because of interaction with other agent 
        if (destPoint == null) {
            // go back to goal zone
            initialReachedGZ = false;
            return ActionInfo.SKIP("Agent is stuck in makeMove of GuardDispenserDesire.");
        }
        AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                "makeMove() with destPoint " + destPoint.toString());
        // Correct oldDestPoint for last movement of agent
        if (oldDestPoint != null) {
            oldDestPoint = correctPointByLastMovement(oldDestPoint);
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Corrected oldDestPoint " + oldDestPoint.toString());
        }

        // Determine if the new destPoint equals the same target (either GZ-point when
        // agent is patrolling or the same target enemy when agent is moving towards enemy
        // agent to attack)
        // !oldDestPoint.equals(destPoint) // does not work because vision shifts so perceived points of GZ can be different
        // therefore a small tolerance of 2 fields is introduced
        boolean sameTarget = oldDestPoint != null && getDistance(oldDestPoint, destPoint) <= 2;
        if (oldDestPoint == null || !sameTarget) {
            dirStack = new Stack<String>();
        }
        oldDestPoint = destPoint;

        // First see if some "unclearable" stuff has to be circled around
        if (dirStack.size() > 0) {
            ActionInfo actInf = circleAround();
            if (actInf != null) {
                return actInf;
            }
        }

        // Move in zick zack and not straight lines towards the destPoint if possible so the agent
        // patrolles inside of the goal zone and does not leave it
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
            String blockedDirForStack = getDirectionFromPoint(nextPoint);
            // try other direction if first one is not possible 
            // (deviate from zick zack course)
            if (destPoint.x != 0) {
                nextPoint = new Point(signX, 0);
                nextMove = moveTo(nextPoint);
                if (nextMove != null) {
                    return nextMove;
                }
            }
            dirStack.push(blockedDirForStack);
        }
        else if (Math.abs(destPoint.x) > Math.abs(destPoint.y)) {
            nextPoint = new Point(signX, 0);
            nextMove = moveTo(nextPoint);
            // If move is possible
            if (nextMove != null) {
                return nextMove;
            }
            String blockedDirForStack = getDirectionFromPoint(nextPoint);
            if (destPoint.y != 0) {
                nextPoint = new Point(0, signY);
                nextMove = moveTo(nextPoint);
                if (nextMove != null) {
                    return nextMove;
                }
            }
            dirStack.push(blockedDirForStack);
        }

        // If direct motion is not possible then circle around
        ActionInfo actInf = circleAround();
        if (actInf != null) {
            return actInf;
        }

        AgentLogger.fine(belief.getAgentShortName() + " GGZD", "Agent is stuck in makeMove of GuardDispenserDesire.");
        return ActionInfo.SKIP("Agent is stuck in makeMove of GuardDispenserDesire.");
    }

    // Helper-method for makeMove()
    private ActionInfo moveTo(Point nextPoint) {
        // Determine Action
        if (nextPoint != null && isClearable(nextPoint)) {
            return ActionInfo.CLEAR(new Point(nextPoint.x, nextPoint.y),
                    "Clear on abandoned block");
        }
        else if (nextPoint != null && belief.getThingAt(nextPoint) == null) {
            String dir = getDirectionToRelativePoint(nextPoint);
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Moving to Point : " + nextPoint.toString() + " direction: " + dir);
            return ActionInfo.MOVE(dir, "Moving");
        }
        return null;
    }

    // Circle around unclearable stuff
    private ActionInfo circleAround() {
        // Circle around clockwise something is in the direct way
        int maxStackSize = 5;
        while (dirStack.size() <= maxStackSize) {
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "Stack: " + dirStack.toString());
            // Check if movement to last direction on stack is possible
            String lastDir = dirStack.peek();
            ActionInfo nextMove = moveTo(getPointFromDirection(lastDir));
            if (nextMove != null) {
                dirStack.pop();
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Popping direction from Stack: " + lastDir);
                return nextMove;
            }
            // If direction is still blocked then try the next one in clockwise direction
            else {
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "Pushing direction to Stack: " + getCRotatedDirection(lastDir));
                dirStack.push(getCRotatedDirection(lastDir));
            }
        }
        return null;
    }

    // Return true if there is an obstacle or a block without any agent in its proximity at the given point
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

    // Reset all instance variables that are connected to the enemy agent tracking
    private void resetTargetEnemy() {
        AgentLogger.fine(belief.getAgentShortName() + " GGZD", "ResetTargetEnemy()");
        targetEnemyLastPosition = null;
        targetEnemyEnergy = 100;
        lastDistToTargetEnemy = 100;
        lastActionWasClearOnBlock = false;
        blocksToClear = null;
    }

    // Get closest enemy with blocks, that is in the goal zone close to the blocks that cannot
    // be connected to a friendly agent
    private Thing getClosestEnemyWithBlocks(){
        int dist = 100;
        Thing closestEnemy = null;
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity")
                    && isInGoalZone(new Point(t.x, t.y))
                    && !t.details.equals(belief.getTeam())
                    && getDistance(t) < dist
                    && (lastClearedEnemyPosition == null
                        || (lastClearedEnemyPosition != null
                                && stepsSinceCompleteClear > noAttackTime)
                        || (lastClearedEnemyPosition != null
                                && !lastClearedEnemyPosition.equals(new Point(t.x, t.y))))) {
                AdjacentThings adjThings = getAllAdjacentThings(new Point(t.x, t.y));
                // Conditions for selecting an enemy agent
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

    // Get current enemy positions for following algorithm
    private List<Point> getCurrentEnemyPositions() {
        List<Point> currentEnemyPositions = new ArrayList<Point>();
        for (Thing t : belief.getThings()) {
            if (t.type.equals("entity") && ! t.details.equals(belief.getTeam())) {
                currentEnemyPositions.add(new Point(t.x, t.y));
            }
        }
        return currentEnemyPositions;
    }

    /**
     * Check if agent is currently in goal zone
     * 
     * @return true if agent is in goal zone
     */
    boolean isInGoalZone() {
        if (belief.getGoalZones().contains(new Point(0, 0))) {
            return true;
        }
        return false;
    }

    
    /**
     * Check if point is in goal zone
     * 
     * @param p Point to be checked
     * @return true if given point is in goal zone
     */
    boolean isInGoalZone(Point p) {
        if (belief.getGoalZones().contains(p)) {
            return true;
        }
        return false;
    }

    /**
     * Get new position of a certain enemy agent depending on its position in the previous step.
     * The algorithm is based on the simplifying presumption that all agents move only one or zero
     * fields each step (which applies to all current roles when the agent has a block attached).
     * 
     * The algorithm does not always give back the correct position in all cases, especially when
     * multiple enemy agents move close together. However, this case does not influence the overall
     * performance noticeable.
     * 
     * @param oldEnemyPosition position of agent that is to be tracked in the previous step
     * @return Presumed new position of the agent
     */
    Point getNewPositionOfTargetEnemy(Point oldEnemyPosition) {
        List<Point> currentEnemyPositions = getCurrentEnemyPositions();
        AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                "CurrentEnemyPositions" + currentEnemyPositions.toString());
        List<Point> oldEnemyPositions = new ArrayList<Point>();
        oldEnemyPositions.addAll(this.oldEnemyPositions);
        // Correct for own movement (translate oldEnemyPositions by own movement)
        oldEnemyPosition = correctPointByLastMovement(oldEnemyPosition);
        AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                "oldEnemyPosition corrected by own last move " + oldEnemyPosition.toString());
        for (int i = 0; i < oldEnemyPositions.size(); i++) {
            Point pos = oldEnemyPositions.get(i);
            oldEnemyPositions.set(i, correctPointByLastMovement(pos));
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

        // Something went wrong:
        if (!distancesOldToNewMap.containsKey(oldEnemyPosition)) {
            AgentLogger.severe(belief.getAgentShortName() + " GGZD",
                    "Key oldEnemyPosition not found in distancesOldToNewMap. Will return null.");
            AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                    "distancesOldToNewMap: " + distancesOldToNewMap.toString());
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

        // For now return agent that is closest previous agent position
        for (int d = 0; d < 10; d++) {
            if (distancesOldToNewMap.get(oldEnemyPosition).containsKey(d)) {
                Point closestToOldPos = distancesOldToNewMap.get(oldEnemyPosition).get(d).get(0);
                AgentLogger.fine(belief.getAgentShortName() + " GGZD",
                        "returning closestToOldPos: " + closestToOldPos.toString());
                return closestToOldPos;
            }
        }
        return null;
    }

    /**
     * Correct position of another object in the previous step by own movement
     * 
     * @param p point to be corrected
     * @return corrected point
     */
    Point correctPointByLastMovement(Point p) {
        if (belief.getLastAction().equals("move")
                && belief.getLastActionResult().equals(ActionResults.SUCCESS)){
            Point lastMove = getPointFromDirection(belief.getLastActionParams().get(0));
            return new Point(p.x - lastMove.x, p.y - lastMove.y);
        }
        else {
            return p;
        }
    }

    /**
     * Calculate a destination point for patrolling the goal zone. Those points are the corner points
     * of the goal zone (if they are visible).
     * 
     * @param direction "s" of south, "n" for north, "e" for east, "w" for west
     * @param offset offset towards the agent
     * @return goal zone point that is located the farthest (minus the offset) to the given direction.
     */
    Point getPatrolCornerPoint(String direction, int offset) {
        List<Point> gz = belief.getGoalZones();
        if (gz.size() > 0) {
            switch (direction){
            case "n":
                gz.sort((a, b) -> a.y - b.y);
                return new Point(gz.get(0).x, gz.get(0).y + offset);
            case "s":
                gz.sort((a, b) -> a.y - b.y);
                return new Point(gz.get(gz.size() - 1).x, gz.get(gz.size() - 1).y - offset);
            case "w":
                gz.sort((a, b) -> a.x - b.x);
                return new Point(gz.get(0).x + offset, gz.get(0).y);
            case "e":
                gz.sort((a, b) -> a.x - b.x);
                return new Point(gz.get(gz.size() - 1).x - offset, gz.get(gz.size() - 1).y);
            default:
                return null;
            }
        }
        return null;
    }

    
    /**
     * Calculates all adjacent things to the specified point
     * 
     * @param p point which adjacent things are to be determined
     * @return AdjacentThings object with all adjacent friendly and enemy agents as well as blocks
     */
    AdjacentThings getAllAdjacentThings(Point p) {
        AdjacentThings adjThings = new AdjacentThings();
        adjThings = getAdjacentThings(p, adjThings);
        return adjThings;
    }

    // Method for recursive call from getAllAdjacentThings()
    private AdjacentThings getAdjacentThings(Point p, AdjacentThings adjThings) {
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


    /**
     * Setter for instance variable oldEnemyPositons (enemy positions during last step)
     * 
     * @param oldEnemyPositions List of points of the old enemy positions
     */
    public void setOldEnemyPositions(List<Point> oldEnemyPositions) {
        this.oldEnemyPositions = oldEnemyPositions; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        return new BooleanInfo(false, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return "Guard Goal Zone"; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 5000;
    }

    /**
     * The Class <code>AdjacentThings</code> is a data class that contains information about
     * the blocks, friendly agents and enemy agents that are adjacent to a given
     * position.
     * 
     * @author philh
     *
     */
    class AdjacentThings {
        
        private List<Point> blocksList;
        private List<Point> friendlyAgentsList;
        private List<Point> enemyAgentsList;
        
        /**
         * Instantiates a new AdjacentThings object
         * 
         */
        public AdjacentThings() {
            this.blocksList = new ArrayList<Point>();
            this.friendlyAgentsList = new ArrayList<Point>();
            this.enemyAgentsList = new ArrayList<Point>();
        }

        /**
         * Add a found block.
         * 
         * @param postion point where to add the block
         * @return return true if the block did not exist before
         */
        public boolean addBlock(Point postion) {
            if (!containsBlockAt(postion)){
                blocksList.add(postion);
                return true;
            }
            return false;
        }

        /**
         * Add a friendly agent.
         * 
         * @param postion point where to add the friendly agent
         * @return return true if the friendly agent did not exist before
         */
        public boolean addFriendlyAgent(Point postion) {
            if (!containsFriendlyAgentAt(postion)){
                friendlyAgentsList.add(postion);
                return true;
            }
            return false;
        }

        /**
         * Add a enemy agent.
         * 
         * @param postion point where to add the enemey agent
         * @return return true if the enemy agent did not exist before
         */
        public boolean addEnemyAgent(Point postion) {
            if (!containsEnemyAgentAt(postion)){
                enemyAgentsList.add(postion);
                return true;
            }
            return false;
        }

        /**
         *Get a list of all blocks
         * 
         * @return List of block positions
         */
        public List<Point> getBlocks() {
            return blocksList;
        }

        /**
         * Get a list of all friendly agents
         * 
         * @return List of all friendly agents
         */
        public List<Point> getFriendlyAgents() {
            return friendlyAgentsList;
        }

        /**
         * Get number of all adjacent blocks
         * 
         * @return number of all adjacent blocks
         */
        public int numOfAdjBlocks() {
            return blocksList.size();
        }

        /**
         * Get number of all friendly agents
         * 
         * @return number of all friendly agents
         */
        public int numOfAdjFriendlyAgents() {
            return friendlyAgentsList.size();
        }

        /**
         * Get number of all enemy agents
         * 
         * @return number of all enemy agents
         */
        public int numOfAdjEnemyAgents() {
            return enemyAgentsList.size();
        }

        public String toString() {
            return "Blocks: " + blocksList.toString() + "  FriendlyAgents: " + friendlyAgentsList+ "  EnemyAgents: " + enemyAgentsList;
        }


        private boolean containsBlockAt(Point positon) {
            if (blocksList.contains(positon)) {return true;}
            return false;
        }

        private boolean containsFriendlyAgentAt(Point positon) {
            if (friendlyAgentsList.contains(positon)) {return true;}
            return false;
        }

        private boolean containsEnemyAgentAt(Point positon) {
            if (enemyAgentsList.contains(positon)) {return true;}
            return false;
        }
    }
}
