package de.feu.massim22.group3.agents.Desires;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.Test;

import de.feu.massim22.group3.agents.Belief;
import de.feu.massim22.group3.agents.Desires.BDesires.ActionInfo;
import de.feu.massim22.group3.agents.Desires.BDesires.BeliefDesire;
import de.feu.massim22.group3.agents.Desires.BDesires.BooleanInfo;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import massim.protocol.data.Thing;

public class BeliefDesireTest {
    
    @Test
    public void testGetActionForMoveRotate() {
        // Create Belief
        Belief b = new Belief("Agent");
        var percept = new ArrayList<Percept>();
        Percept t1 = new Percept("thing", new Numeral(1), new Numeral(0), new Identifier(Thing.TYPE_BLOCK), new Identifier("b1"));
        Percept t2 = new Percept("thing", new Numeral(2), new Numeral(0), new Identifier(Thing.TYPE_OBSTACLE), new Identifier(""));
        Percept a2 = new Percept("attached", new Numeral(1), new Numeral(0));
        percept.add(t1);
        percept.add(t2);
        percept.add(a2);
        b.update(percept);
        BeliefDesire d = new BeliefDesire(b) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo test = this.getActionForMove("e", "");
                assertEquals(test.value().getName(), "rotate");
                return null;
            }
        };
        d.isFulfilled();
    }

    @Test
    public void testGetActionForMove1() {
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