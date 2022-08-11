package de.feu.massim22.group3.agents.desires;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.utils.PerceptUtil;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import massim.protocol.data.Thing;
import java.awt.Point;

/**
 * The Class <code>BeliefDesireTest</code> provides methods for testing the class <code>BeliefDesire</code>.
 * Symbols:
 * A: Agent
 * E: Enemy
 * ○: Attached Block
 * ●: Unattached Block
 * ■: Obstacle
 * 
 * @author Heinz Stadler
 */
class BeliefDesireTest {
    
    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: A○■ <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove1() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(2, 0, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("rotate", test.value().getName());
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed vertical move when blocked by block. <BR>
     * Konfiguration: A● <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove2() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b1"));
        percept.add(t1);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("move", test.value().getName());
                assertTrue(Arrays.asList("s", "n").contains(PerceptUtil.toStr(test.value().getParameters(), 0)));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: ○■ <BR>
     *                A <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove3() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, -1));
        percept.add(t1);
        percept.add(t2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("rotate", test.value().getName());
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: ○■ <BR>
     *                A■ <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove4() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, -1));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("rotate", test.value().getName());
                assertEquals("ccw", PerceptUtil.toStr(test.value().getParameters(), 0));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Clear with attached block. <BR>
     * Konfiguration:  ■ <BR>
     *               ○A■ <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove5() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(-1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("clear", test.value().getName());
                assertEquals(1, PerceptUtil.toNumber(test.value().getParameters(), 0, Integer.class));
                assertEquals(0, PerceptUtil.toNumber(test.value().getParameters(), 1, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Move with attached block - Agent should not clear blocks if not needed. <BR>
     * Konfiguration:  ■ <BR>
     *               ○A● <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove6() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b2"));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(-1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("move", test.value().getName());
                assertEquals("s", PerceptUtil.toStr(test.value().getParameters(), 0));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Clear with attached block - Agent should not clear blocks if not needed. <BR>
     * Konfiguration:  ■ <BR>
     *               ○A● <BR>
     *                ■ <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove7() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b2"));
        Percept t4 = PerceptUtil.fromThing(new Thing(0, 1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(-1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(t4);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("clear", test.value().getName());
                assertEquals(0, PerceptUtil.toNumber(test.value().getParameters(), 0, Integer.class));
                assertEquals(1, PerceptUtil.toNumber(test.value().getParameters(), 1, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Clear with enemies and blocks around - Agent should not clear blocks if not needed. <BR>
     * Konfiguration: ■ <BR>
     *               EA● <BR>
     *                E <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove8() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t2 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_ENTITY, "B"));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b2"));
        Percept t4 = PerceptUtil.fromThing(new Thing(0, 1, Thing.TYPE_ENTITY, "B"));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(t4);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("clear", test.value().getName());
                assertEquals(0, PerceptUtil.toNumber(test.value().getParameters(), 0, Integer.class));
                assertEquals(-1, PerceptUtil.toNumber(test.value().getParameters(), 1, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: ○■ <BR>
     *                ○ <BR>
     *                A <BR>
     * Move Direction: ->
     */
    @Test
    void testGetActionForMove9() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -2, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(0, -2, Thing.TYPE_BLOCK, "b1"));
        Percept a1 = PerceptUtil.fromAttachedPoint(new Point(0, -1));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, -2));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a1);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals("rotate", test.value().getName());
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Move with attached block. <BR>
     * Konfiguration: ■ <BR>
     *                ○ <BR>
     *                A <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove10() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(0, -2, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("move", test.value().getName());
                assertTrue(Arrays.asList("e", "w").contains(PerceptUtil.toStr(test.value().getParameters(), 0)));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed horizontal move when blocked by block. <BR>
     * Konfiguration: ● <BR>
     *                A <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove11() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        percept.add(t1);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("move", test.value().getName());
                assertTrue(Arrays.asList("e", "w").contains(PerceptUtil.toStr(test.value().getParameters(), 0)));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration:  ■ <BR>
     *                A○ <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove12() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("rotate", test.value().getName());
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: ■■ <BR>
     *                A○ <BR>
     * Move Direction: ↑ 
     */
    @Test
    void testGetActionForMove13() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(1, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("rotate", test.value().getName());
                assertEquals("cw", PerceptUtil.toStr(test.value().getParameters(), 0));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Clear with attached block. <BR>
     * Konfiguration:  ■■ <BR>
     *                 A <BR>
     *                 ○ <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove14() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, 1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, 1));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("clear", test.value().getName());
                assertEquals(0, PerceptUtil.toNumber(test.value().getParameters(), 0, Integer.class));
                assertEquals(-1, PerceptUtil.toNumber(test.value().getParameters(), 1, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Move with attached block - Agent should not clear blocks if not needed. <BR>
     * Konfiguration: ●■ <BR>
     *                A <BR>
     *                ○ <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove15() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, 1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b2"));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, 1));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("move", test.value().getName());
                assertTrue(Arrays.asList("e", "w").contains(PerceptUtil.toStr(test.value().getParameters(), 0)));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Move with attached block - Agent should not clear blocks if not needed. <BR>
     * Konfiguration: ●■ <BR>
     *               ■A <BR>
     *                ○ <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove16() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(1, -1, Thing.TYPE_BLOCK, "b2"));
        Percept t4 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_OBSTACLE, ""));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(0, 1));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(t4);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("move", test.value().getName());
                assertEquals("e", PerceptUtil.toStr(test.value().getParameters(), 0));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Clear with enemies and blocks around - Agent should not clear blocks if not needed. <BR>
     * Konfiguration: ● <BR>
     *               EA■ <BR>
     *                E <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove17() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(1, 0, Thing.TYPE_OBSTACLE, ""));
        Percept t2 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_ENTITY, "B"));
        Percept t3 = PerceptUtil.fromThing(new Thing(0, -1, Thing.TYPE_BLOCK, "b2"));
        Percept t4 = PerceptUtil.fromThing(new Thing(0, 1, Thing.TYPE_ENTITY, "B"));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(t4);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("clear", test.value().getName());
                assertEquals(1, PerceptUtil.toNumber(test.value().getParameters(), 0, Integer.class));
                assertEquals(0, PerceptUtil.toNumber(test.value().getParameters(), 1, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for needed Rotation with attached block. <BR>
     * Konfiguration: ■ <BR>
     *                ○○A <BR>
     * Move Direction: ↑
     */
    @Test
    void testGetActionForMove18() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_BLOCK, "b1"));
        Percept t2 = PerceptUtil.fromThing(new Thing(-2, -1, Thing.TYPE_OBSTACLE, ""));
        Percept t3 = PerceptUtil.fromThing(new Thing(-2, 0, Thing.TYPE_BLOCK, "b1"));
        Percept a1 = PerceptUtil.fromAttachedPoint(new Point(-1, 0));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(-2, 0));
        percept.add(t1);
        percept.add(t2);
        percept.add(t3);
        percept.add(a1);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals("rotate", test.value().getName());
                return null;
            }
        };
        d.isFulfilled();
    }

    /**
     * Test for move. <BR>
     * Konfiguration: E  <BR>
     *                ● <BR>
     *                  <BR>
     *                 A○ <BR>
     * Move Direction: ^
     */
    @Test
    void testGetActionForMove19() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept e1 = new Percept("thing", new Numeral(-1), new Numeral(-3), new Identifier(Thing.TYPE_ENTITY), new Identifier("A"));
        Percept b1 = new Percept("thing", new Numeral(-1), new Numeral(-2), new Identifier(Thing.TYPE_BLOCK), new Identifier("b2"));
        Percept b2 = new Percept("thing", new Numeral(1), new Numeral(0), new Identifier(Thing.TYPE_BLOCK), new Identifier("b2"));
        Percept a2 = new Percept("attached", new Numeral(1), new Numeral(0));
        percept.add(e1);
        percept.add(b1);
        percept.add(b2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("n", "");
                assertEquals(test.value().getName(), "move");
                return null;
            }
        };
        d.isFulfilled();
    }
}