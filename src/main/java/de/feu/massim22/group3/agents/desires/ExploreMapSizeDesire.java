package de.feu.massim22.group3.agents.desires;

import de.feu.massim22.group3.agents.belief.AgentSurveyStepEvent;
import de.feu.massim22.group3.agents.belief.Belief;
import de.feu.massim22.group3.agents.belief.StepEvent;
import de.feu.massim22.group3.map.INaviAgentV1;
import de.feu.massim22.group3.map.Navi;
import massim.protocol.data.Thing;
import massim.protocol.messages.scenario.ActionResults;

import java.awt.Point;

public class ExploreMapSizeDesire extends BeliefDesire {

    private String teamMate;
    private String teamMateActionName;
    private String supervisor;
    private boolean explored = false;
    private String direction;
    private int guideOffset;
    private int agentOffset = 0;

    public ExploreMapSizeDesire(Belief belief, String teamMate, String teamMateActionName, String supervisor, String direction, int guideOffset) {
        super(belief);
        this.teamMate = teamMate;
        this.teamMateActionName = teamMateActionName;
        this.supervisor = supervisor;
        this.direction = direction;
        this.guideOffset = guideOffset;

        String[] neededActions = {"survey"};
        precondition.add(new LooseWeightDesire(belief));
        precondition.add(new ActionDesire(belief, neededActions));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanInfo isFulfilled() {
        for (StepEvent e : belief.getStepEvents()) {
            if (e instanceof AgentSurveyStepEvent) {
                String agent = ((AgentSurveyStepEvent)e).name();
                if (agent.equals(teamMateActionName)) {
                    Point agentPosition = belief.getPosition();
                    Point teamMatePosition = Navi.<INaviAgentV1>get().getPosition(teamMate, supervisor);
                    if (direction.equals("w")) {
                        int size = teamMatePosition.x - agentPosition.x - agentOffset;
                        Navi.get().setHorizontalMapSize(size);
                    }
                    if (direction.equals("e")) {
                        int size = agentPosition.x - teamMatePosition.x + agentOffset;
                        Navi.get().setHorizontalMapSize(size);
                    }
                    if (direction.equals("n")) {
                        int size = teamMatePosition.y - agentPosition.y - agentOffset;
                        Navi.get().setVerticalMapSize(size);
                    }
                    if (direction.equals("s")) {
                        int size = agentPosition.y - teamMatePosition.y + agentOffset;
                        Navi.get().setVerticalMapSize(size);
                    }
                    explored = true;
                }
            }
        }
        return new BooleanInfo(explored, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionInfo getNextActionInfo() {
        // Precondition
        ActionInfo a = fulfillPreconditions();
        if (a != null) {
            return a;
        }
        Point pos = belief.getPosition();
        // Test agents in vision
        for (Thing t : belief.getThings()) {
            // Teammate found
            if (t.type.equals(Thing.TYPE_ENTITY) && t.details.equals(belief.getTeam())) {
                Point testPoint = new Point(pos.x + t.x, pos.y + t.y);
                int agentId = Navi.<INaviAgentV1>get().getAgentIdAtPoint(supervisor, testPoint);
                // No Agent Found - possible match to saved team mate
                if (agentId == 0) {
                    agentOffset = direction.equals("w") || direction.equals("e") ? t.x : t.y;
                    return ActionInfo.SURVEY(t, getName());
                }
            }
        }

        // Move closer to guide line
        if (Math.abs(guideOffset) > 1) {
            if (direction.equals("n") || direction.equals("s")) {
                String dir = guideOffset < 0 ? "e" : "w";
                return getActionForMove(dir + dir, getName()); 
            }
            if (direction.equals("e") || direction.equals("w")) {
                String dir = guideOffset < 0 ? "s" : "n";
                return getActionForMove(dir + dir, getName()); 
            }
        }
        // Move forward
        return getActionForMove(direction + direction, getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(String supervisor) {
        this.supervisor = supervisor;
        // Update current offset to guid line
        if (belief.getLastAction().equals("move")) {
            var paras = belief.getLastActionParams();
            if (paras.size() > 0 && (belief.getLastActionResult().equals(ActionResults.SUCCESS) || 
                    belief.getLastActionResult().equals(ActionResults.PARTIAL_SUCCESS))) {
                // Vertical
                if (direction.equals("n") || direction.equals("s")) {
                    if (paras.get(0).equals("e")) {
                        guideOffset += 1;
                    }
                    if (paras.get(0).equals("w")) {
                        guideOffset -= 1;
                    }
                }
                // Horizontal
                if (direction.equals("e") || direction.equals("w")) {
                    if (paras.get(0).equals("s")) {
                        guideOffset += 1;
                    }
                    if (paras.get(0).equals("n")) {
                        guideOffset -= 1;
                    }
                } 
            }
            if (paras.size() > 1 && belief.getLastActionResult().equals(ActionResults.SUCCESS)) {
                // Vertical
                if (direction.equals("n") || direction.equals("s")) {
                    if (paras.get(0).equals("e")) {
                        guideOffset += 1;
                    }
                    if (paras.get(0).equals("w")) {
                        guideOffset -= 1;
                    }
                }
                // Horizontal
                if (direction.equals("e") || direction.equals("w")) {
                    if (paras.get(0).equals("s")) {
                        guideOffset += 1;
                    }
                    if (paras.get(0).equals("n")) {
                        guideOffset -= 1;
                    }
                } 
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPriority() {
        return 4000;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGroupDesire() {
        return true;
    }
}
