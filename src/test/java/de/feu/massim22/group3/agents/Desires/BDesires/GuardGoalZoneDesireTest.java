package de.feu.massim22.group3.agents.Desires.BDesires;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Desires.BDesires.ActionDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.GuardGoalZoneDesire.AdjacentThings;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;

/**
 * The Class <code>GuardGoalZoneDesireTest</code> provides methods for testing the class <code>GuardGoalZoneDesire</code>.
 * Symbols:
 * A: Agent
 * E: Enemy
 * ○: Attached Block
 * ●: Unattached Block
 * ■: Obstacle
 * 
 * @author Phil Heger
 */

public class GuardGoalZoneDesireTest {

    @Test
    public void testGetCornerPoint() {
        List<Point> pointList = new ArrayList<>();
        pointList.add(new Point(0, 0));
        pointList.add(new Point(1, 0));
        pointList.add(new Point(-1, 0));
        pointList.add(new Point(0, 1));
        pointList.add(new Point(0, -1));
        
        Belief b = mock(Belief.class);
        when(b.getGoalZones()).thenReturn(pointList);
        
        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "w", "supervisor dummy");
        assertEquals(new Point(-1, 0), d.getCornerPoint("w"));
        assertEquals(new Point(1, 0), d.getCornerPoint("e"));
        assertEquals(new Point(0, 1), d.getCornerPoint("s"));
        assertEquals(new Point(0, -1), d.getCornerPoint("n"));
    }

    /**
     *   E●●
     *  A ●
     */
    @Test
    public void testGetAllAdjacentThings1() {
        
        Belief b = mock(Belief.class);

        when(b.getThingAt(any(Point.class))).thenReturn(null);
        when(b.getThingAt(eq(new Point(2, 0)))).thenReturn(new Thing(2, 0, "block", "b1"));
        when(b.getThingAt(eq(new Point(2, -1)))).thenReturn(new Thing(2, -1, "block", "b0"));
        when(b.getThingAt(eq(new Point(3, -1)))).thenReturn(new Thing(3, -1, "block", "b2"));
        when(b.getThingAt(eq(new Point(1, -1)))).thenReturn(new Thing(1, -1, "entity", "2"));
        when(b.getThingAt(eq(new Point(0, 0)))).thenReturn(new Thing(0, 0, "entity", "1"));
        when(b.getTeam()).thenReturn("1");

        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        AdjacentThings adjThings = d.getAllAdjacentThings(new Point(1, -1));
        System.out.println(adjThings);
        assertEquals(3, adjThings.numOfAdjBlocks());
        assertEquals(0, adjThings.numOfAdjFriendlyAgents());
        assertEquals(1, adjThings.numOfAdjEnemyAgents());
        assertTrue(adjThings.getBlocks().contains(new Point(2, -1)));
        assertTrue(adjThings.getBlocks().contains(new Point(2, 0)));
        assertTrue(adjThings.getBlocks().contains(new Point(3, -1)));
    }

    /**
     *    E
     *   E●●A■
     *  A ●A
     */
    @Test
    public void testGetAllAdjacentThings2() {
        
        Belief b = mock(Belief.class);

        when(b.getThingAt(any(Point.class))).thenReturn(null);
        when(b.getThingAt(eq(new Point(2, 0)))).thenReturn(new Thing(2, 0, "block", "b1"));
        when(b.getThingAt(eq(new Point(2, -1)))).thenReturn(new Thing(2, -1, "block", "b0"));
        when(b.getThingAt(eq(new Point(3, -1)))).thenReturn(new Thing(3, -1, "block", "b2"));
        when(b.getThingAt(eq(new Point(1, -1)))).thenReturn(new Thing(1, -1, "entity", "2"));
        when(b.getThingAt(eq(new Point(2, -2)))).thenReturn(new Thing(2, -2, "entity", "2"));
        when(b.getThingAt(eq(new Point(0, 0)))).thenReturn(new Thing(0, 0, "entity", "1"));
        when(b.getThingAt(eq(new Point(3, 0)))).thenReturn(new Thing(3, 0, "entity", "1"));
        when(b.getThingAt(eq(new Point(4, -1)))).thenReturn(new Thing(4, -1, "entity", "1"));
        when(b.getThingAt(eq(new Point(5, -1)))).thenReturn(new Thing(5, -1, "obstacle", ""));
        when(b.getTeam()).thenReturn("1");

        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        AdjacentThings adjThings = d.getAllAdjacentThings(new Point(1, -1));
        System.out.println(adjThings);
        assertEquals(3, adjThings.numOfAdjBlocks());
        assertEquals(2, adjThings.numOfAdjFriendlyAgents());
        assertEquals(2, adjThings.numOfAdjEnemyAgents());
        assertTrue(adjThings.getBlocks().contains(new Point(2, -1)));
        assertTrue(adjThings.getBlocks().contains(new Point(2, 0)));
        assertTrue(adjThings.getBlocks().contains(new Point(3, -1)));
        assertTrue(adjThings.getFriendlyAgents().contains(new Point(4, -1)));
    }

    /**
     * Own Agent is not being counted as connected
     *    E
     *  ●E●●A■
     *  A ●A
     */
    @Test
    public void testGetAllAdjacentThings3() {
        
        Belief b = mock(Belief.class);

        when(b.getThingAt(any(Point.class))).thenReturn(null);
        when(b.getThingAt(eq(new Point(2, 0)))).thenReturn(new Thing(2, 0, "block", "b1"));
        when(b.getThingAt(eq(new Point(2, -1)))).thenReturn(new Thing(2, -1, "block", "b0"));
        when(b.getThingAt(eq(new Point(3, -1)))).thenReturn(new Thing(3, -1, "block", "b2"));
        when(b.getThingAt(eq(new Point(0, -1)))).thenReturn(new Thing(0, -1, "block", "b2"));
        when(b.getThingAt(eq(new Point(1, -1)))).thenReturn(new Thing(1, -1, "entity", "2"));
        when(b.getThingAt(eq(new Point(2, -2)))).thenReturn(new Thing(2, -2, "entity", "2"));
        when(b.getThingAt(eq(new Point(0, 0)))).thenReturn(new Thing(0, 0, "entity", "1"));
        when(b.getThingAt(eq(new Point(3, 0)))).thenReturn(new Thing(3, 0, "entity", "1"));
        when(b.getThingAt(eq(new Point(4, -1)))).thenReturn(new Thing(4, -1, "entity", "1"));
        when(b.getThingAt(eq(new Point(5, -1)))).thenReturn(new Thing(5, -1, "obstacle", ""));
        when(b.getTeam()).thenReturn("1");

        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        AdjacentThings adjThings = d.getAllAdjacentThings(new Point(1, -1));
        System.out.println(adjThings);
        assertEquals(4, adjThings.numOfAdjBlocks());
        assertEquals(2, adjThings.numOfAdjFriendlyAgents());
        assertEquals(2, adjThings.numOfAdjEnemyAgents());
        assertTrue(adjThings.getBlocks().contains(new Point(2, -1)));
        assertTrue(adjThings.getBlocks().contains(new Point(2, 0)));
        assertTrue(adjThings.getBlocks().contains(new Point(3, -1)));
        assertTrue(adjThings.getFriendlyAgents().contains(new Point(4, -1)));
    }

    /**Own agent does not move but enemy moves 1 field
     * 
     * A E  ->  A  
     *            E
     */
    @Test
    public void testGetNewPositionOfTargetEnemy1() {
        
        Belief b = mock(Belief.class);
        Set <Thing> curThingsList = new HashSet<Thing>();
        curThingsList.add(new Thing(2, 1, "entity", "2"));
        when(b.getThings()).thenReturn(curThingsList);
        when(b.getTeam()).thenReturn("1");
        when(b.getLastAction()).thenReturn("skip");
        when(b.getLastActionParams()).thenReturn(new ArrayList<String>());
        when(b.getLastActionResult()).thenReturn(ActionResults.SUCCESS);
        
        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        d.oldEnemyPositions.add(new Point(2, 0));
        
        assertEquals(new Point(2, 1), d.getNewPositionOfTargetEnemy(new Point(2, 0)));
    }

    /**Own agent moves 1 field and enemy moves 1 field
     * 
     * A E  ->     
     *          A E
     */
    @Test
    public void testGetNewPositionOfTargetEnemy2() {
        
        Belief b = mock(Belief.class);
        Set <Thing> curThingsList = new HashSet<Thing>();
        curThingsList.add(new Thing(2, 0, "entity", "2"));
        when(b.getThings()).thenReturn(curThingsList);
        when(b.getTeam()).thenReturn("1");

        when(b.getLastAction()).thenReturn("move");
        when(b.getLastActionParams()).thenReturn(Arrays.asList("s"));
        when(b.getLastActionResult()).thenReturn(ActionResults.SUCCESS);
        
        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        d.oldEnemyPositions.add(new Point(2, 0));
        
        assertEquals(new Point(2, 0), d.getNewPositionOfTargetEnemy(new Point(2, 0)));
    }

    /**
     * Enemy agent comes into sight --> was not in sight before and thus is not in the newPositions
     * List
     */
    @Test
    public void testGetNewPositionOfTargetEnemy3() {
        
        Belief b = mock(Belief.class);
        Set <Thing> thingsList = new HashSet<Thing>();
        thingsList.add(new Thing(3, 2, "entity", "2"));
        when(b.getThings()).thenReturn(thingsList);
        when(b.getTeam()).thenReturn("1");
        
        // Create and test Desire
        GuardGoalZoneDesire d = new GuardGoalZoneDesire(b, "n", "supervisor dummy");
        // oldEnemyPositons are empty
//        d.oldEnemyPositions.add(new Point(2, 0));
        
        assertEquals(new Point(2, 1), d.getNewPositionOfTargetEnemy(new Point(2, 0)));
    }
}