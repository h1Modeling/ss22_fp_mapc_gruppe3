package de.feu.massim22.group3.agents.Desires.BDesires;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.feu.massim22.group3.agents.Belief;
import massim.protocol.data.Thing;

class AttachBlocksFromDispenserDesireTest {

    Belief belief;
    List<Thing> requirements;
    String supervisor = "31";
    private AttachBlocksFromDispenserDesire abfdd;

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
    }

    @BeforeEach
    void setUp() throws Exception {
        // create empty mock object of the Belief class
        belief = mock(Belief.class);
        
        requirements = new ArrayList<Thing>();
    }

    @Test
    void testConstructor1() {
        requirements.add(new Thing(0, 1, "block", "b1"));
        requirements.add(new Thing(0, 2, "block", "b0"));
        requirements.add(new Thing(1, 1, "block", "b1"));
        requirements.add(new Thing(1, 2, "block", "b0"));
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        System.out.println(abfdd.taskBlocks.toString());
        assertEquals(3, abfdd.taskBlocks.size());
        assertEquals(2, abfdd.taskBlocks.get(2).size());
        assertEquals("b0", abfdd.taskBlocks.get(3).get(0).details);
    }

    @Test
    void testConstructor2() {
        requirements.add(new Thing(0, -1, "block", "b1"));
        requirements.add(new Thing(0, -2, "block", "b0"));
        requirements.add(new Thing(1, -1, "block", "b1"));
        requirements.add(new Thing(1, -2, "block", "b0"));
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        System.out.println(abfdd.taskBlocks.toString());
        assertEquals(3, abfdd.taskBlocks.size());
        assertEquals(2, abfdd.taskBlocks.get(2).size());
        assertEquals("b0", abfdd.taskBlocks.get(3).get(0).details);
    }

    @Test
    void testDetermineNextNotAttachedBlocks1() {
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(0, -1, "block", "b1"));
        when(belief.getThings()).thenReturn(things);
        requirements.add(new Thing(0, -1, "block", "b1"));
        requirements.add(new Thing(0, -2, "block", "b0"));
        requirements.add(new Thing(1, -1, "block", "b1"));
        requirements.add(new Thing(1, -2, "block", "b0"));
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        assertEquals(2, abfdd.determineNextNotAttachedBlocks().size());
        
        assertEquals(0, abfdd.determineNextNotAttachedBlocks().get(0).x);
        assertEquals(-2, abfdd.determineNextNotAttachedBlocks().get(0).y);
        assertEquals("b0", abfdd.determineNextNotAttachedBlocks().get(0).details);
        
        assertEquals(1, abfdd.determineNextNotAttachedBlocks().get(1).x);
        assertEquals(-1, abfdd.determineNextNotAttachedBlocks().get(1).y);
        assertEquals("b1", abfdd.determineNextNotAttachedBlocks().get(1).details);
    }

    @Test
    void testDetermineNextNotAttachedBlocks2() {
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(0, -1, "block", "b1"));
        things.add(new Thing(0, -2, "block", "b0"));
        things.add(new Thing(1, -1, "block", "b1"));
        things.add(new Thing(1, -2, "block", "b0"));
        when(belief.getThings()).thenReturn(things);
        requirements.add(new Thing(0, -1, "block", "b1"));
        requirements.add(new Thing(0, -2, "block", "b0"));
        requirements.add(new Thing(1, -1, "block", "b1"));
        requirements.add(new Thing(1, -2, "block", "b0"));
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        assertNull(abfdd.determineNextNotAttachedBlocks());
    }

    @Test
    void testDetermineNextNotAttachedBlocks3() {
        Set<Thing> things = new HashSet<>();
        when(belief.getThings()).thenReturn(things);
        requirements.add(new Thing(0, -1, "block", "b1"));
        requirements.add(new Thing(0, -2, "block", "b0"));
        requirements.add(new Thing(1, -1, "block", "b1"));
        requirements.add(new Thing(1, -2, "block", "b0"));
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        assertEquals(1, abfdd.determineNextNotAttachedBlocks().size());
        
        assertEquals(0, abfdd.determineNextNotAttachedBlocks().get(0).x);
        assertEquals(-1, abfdd.determineNextNotAttachedBlocks().get(0).y);
        assertEquals("b1", abfdd.determineNextNotAttachedBlocks().get(0).details);
    }

    @Test
    void testTransformBlockCoords() {
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        Thing t0 = abfdd.transformBlockCoords(new Thing(1, 2, "block", "b1"), 1);
        assertEquals(1, t0.x);
        assertEquals(2, t0.y);

        Thing t1 = abfdd.transformBlockCoords(new Thing(1, 2, "block", "b1"), 2);
        assertEquals(2, t1.x);
        assertEquals(-1, t1.y);

        Thing t2 = abfdd.transformBlockCoords(new Thing(1, 2, "block", "b1"), 3);
        assertEquals(-1, t2.x);
        assertEquals(-2, t2.y);

        Thing t3 = abfdd.transformBlockCoords(new Thing(1, 2, "block", "b1"), 4);
        assertEquals(-2, t3.x);
        assertEquals(1, t3.y);
    }

    @Test
    void testDetermineRelativePosition() {
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        assertEquals(0, abfdd.determineDirectionCode(0, 0));
        
        assertEquals(1, abfdd.determineDirectionCode(-1, 1));
        assertEquals(1, abfdd.determineDirectionCode(0, 1));
        assertEquals(1, abfdd.determineDirectionCode(-3, 3));
        assertEquals(1, abfdd.determineDirectionCode(2, 3));
        
        assertEquals(2, abfdd.determineDirectionCode(1, 0));
        assertEquals(2, abfdd.determineDirectionCode(1, 1));
        assertEquals(2, abfdd.determineDirectionCode(3, -2));
        assertEquals(2, abfdd.determineDirectionCode(2, 2));
        
        assertEquals(3, abfdd.determineDirectionCode(0, -1));
        assertEquals(3, abfdd.determineDirectionCode(1, -1));
        assertEquals(3, abfdd.determineDirectionCode(-1, -2));
        assertEquals(3, abfdd.determineDirectionCode(2, -2));
        
        assertEquals(4, abfdd.determineDirectionCode(-1, 0));
        assertEquals(4, abfdd.determineDirectionCode(-1, -1));
        assertEquals(4, abfdd.determineDirectionCode(-2, -2));
        assertEquals(4, abfdd.determineDirectionCode(-3, 2));
    }

    @Test
    void testDetermineEmptySides() {
        List<Thing> things = new ArrayList<>();
        things.add(new Thing(0, -1, "block", "b1"));
        things.add(new Thing(1, 0, "block", "b1"));
        when(belief.getAttachedThings()).thenReturn(things);
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        assertFalse(abfdd.determineEmptyAgentSides().contains(0));
        assertTrue(abfdd.determineEmptyAgentSides().contains(1));
        assertFalse(abfdd.determineEmptyAgentSides().contains(2));
        assertFalse(abfdd.determineEmptyAgentSides().contains(3));
        assertTrue(abfdd.determineEmptyAgentSides().contains(4));
    }

    @Test
    void testGetOppositeDirecitonCode() {
        abfdd = new AttachBlocksFromDispenserDesire(belief, requirements, supervisor);
        
        assertEquals(0, abfdd.getOppositeDirecitonCode(0));
        assertEquals(1, abfdd.getOppositeDirecitonCode(3));
        assertEquals(2, abfdd.getOppositeDirecitonCode(4));
        assertEquals(3, abfdd.getOppositeDirecitonCode(1));
        assertEquals(4, abfdd.getOppositeDirecitonCode(2));
    }
}
