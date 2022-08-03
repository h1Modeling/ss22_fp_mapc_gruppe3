package de.feu.massim22.group3.agents.desires;

import java.util.List;

import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.reachable.ReachableTeammate;
import de.feu.massim22.group3.utils.DirectionUtil;

import java.awt.Point;
/**
 * The Class <code>MeetAgentAtGoalZoneDesire</code> models the desire to meet a team mate at a goal zone.
 * 
 * @author Heinz Stadler
 */
public class MeetAgentAtGoalZoneDesire extends BeliefDesire {

    private String agent;

    /**
     * Instantiates a new MeetAgentAtGoalZoneDesire.
     * 
     * @param belief the belief of the agent
     * @param agent the name of the agent to meet
     */
    public MeetAgentAtGoalZoneDesire(Belief belief, String agent) {
        super(belief);
        this.agent = agent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        List<ReachableTeammate> mates = belief.getReachableTeammates();
        for (ReachableTeammate m : mates) {
            if (m.name().equals(agent)) {
                // Has Agent and Goal Zone in Vision
                boolean mateInGoalZone = false;
                Point selfPos = belief.getPosition();
                Point matePos = m.position();
                Point offset = new Point(matePos.x - selfPos.x, matePos.y - selfPos.y);
                for (Point p : belief.getGoalZones()) {
                    if (p.equals(offset)) {
                        mateInGoalZone = true;
                        break;
                    }
                }
                return m.distance() <= belief.getVision() && mateInGoalZone
                    ? new BooleanInfo(true, getName())
                    : new BooleanInfo(false, "Teammate not in vision");
            }
        }
        return new BooleanInfo(false, "Teammate not reachable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        for (ReachableTeammate mate : belief.getReachableTeammates()) {
            if (mate.name().equals(agent)) {
                int dirCode = mate.direction();
                String dir = DirectionUtil.intToString(dirCode);
                return getActionForMove(dir.substring(0, 1), getName());
            }
        }
        return ActionInfo.SKIP("Teammate not reachable");
    }
}
