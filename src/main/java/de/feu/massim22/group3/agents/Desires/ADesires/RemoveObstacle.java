package de.feu.massim22.group3.agents.Desires.ADesires;

import de.feu.massim22.group3.agents.BdiAgentV2;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.Thing;
import java.awt.Point;

//TODO Klassenlogik
public class RemoveObstacle extends ADesire {
	Point obstacle = null;

	public RemoveObstacle(BdiAgentV2 agent) {
		super("RemoveObstacle", agent);
	}

	@Override
	public boolean isExecutable() {
		for (Thing thing : agent.belief.getThings()) {
			if (thing.type.equals(Thing.TYPE_OBSTACLE)) {
				// an diesem Punkt ist das Hindernis
				obstacle = new Point(thing.x, thing.y);
				if (agent.belief.getPosition() != obstacle) {
					// Agent steht vor Hinderniss
					return true;
				} else {
					return false;
				}

			}
		}

		return false;
	}

	@Override
	public Action getNextAction() {

		return new Action("clear", new Identifier(String.valueOf(obstacle.x)),
				new Identifier(String.valueOf(obstacle.y)));

	}
}
