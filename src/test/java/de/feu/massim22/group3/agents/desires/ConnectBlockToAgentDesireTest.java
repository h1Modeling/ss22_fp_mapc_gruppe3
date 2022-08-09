package de.feu.massim22.group3.agents.desires;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.supervisor.Supervisable;
import de.feu.massim22.group3.agents.supervisor.SupervisableAdapter;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.utils.PerceptUtil;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import massim.protocol.data.TaskInfo;
import massim.protocol.data.Thing;

/**
 * The Class <code>ConnectBlockToAgentDesireTest</code> provides methods for testing the class <code>ConnectBlockToAgentDesire</code>.
 * Symbols:
 * A: Agent
 * E: Enemy
 * T: Team Mate
 * ○: Attached Block
 * ●: Unattached Block
 * ■: Obstacle
 * 
 * @author Heinz Stadler
 */
public class ConnectBlockToAgentDesireTest {

    /**
     * Tests if the agent sends a connect if the goal position is reached.
     * - T - - -
     * - ○ - - -
     * - ○ A - -
     * - - - - -
     */
    @Test
    public void testConnectBlock1() {
        // Create Belief
        Belief b = new Belief("Agent");
        b.setPosition(new Point(-1, -1));

        // Team Mate Position
        Parameter p = new Function("pointResult", new Identifier(CellType.TEAMMATE.name()), new TruthValue(false), new Numeral(-2), new Numeral(-3), new Numeral(3), new Numeral(0), new Identifier("B"));
        List<Parameter> paras = new ArrayList<>();
        paras.add(p);
        b.updateFromPathFinding(paras);

        // Set Attached
        var percept = new ArrayList<Percept>();
        Percept t1 = PerceptUtil.fromThing(new Thing(-1, 0, Thing.TYPE_BLOCK, "b0"));
        Percept a2 = PerceptUtil.fromAttachedPoint(new Point(-1, 0));
        percept.add(t1);
        percept.add(a2);
        b.update(percept);

        // Task
        Set<Thing> things = new HashSet<>();
        things.add(new Thing(0, 1, "b0", ""));
        things.add(new Thing(0, 2, "b0", ""));
        TaskInfo info = new TaskInfo("T", 999, 0, things);

        // Communicator
        Supervisable sup = new SupervisableAdapter();

        // Instantiate
        ConnectBlockToAgentDesire d = new ConnectBlockToAgentDesire(b, "B", "B", info, new Thing(0, 2, "b0", ""), sup) {
            @Override
            public BooleanInfo isFulfilled() {
                ActionInfo result = getNextActionInfo();
                assertEquals("connect", result.value().getName());
                assertEquals("B", PerceptUtil.toStr(result.value().getParameters(), 0));
                assertEquals(-1, PerceptUtil.toNumber(result.value().getParameters(), 1, Integer.class));
                assertEquals(0, PerceptUtil.toNumber(result.value().getParameters(), 2, Integer.class));
                return null;
            }
        };
        d.isFulfilled();
    }
}
