package de.feu.massim22.group3.agents.Desires.BDesires;

import de.feu.massim22.group3.agents.Belief;
import eis.iilang.Action;
import eis.iilang.Identifier;
import massim.protocol.data.TaskInfo;

public class ProcessEasyTaskDesire extends BeliefDesire {

    private TaskInfo info;

    public ProcessEasyTaskDesire(Belief belief, TaskInfo info) {
        super(belief);
        this.info = info;
        String[] neededActions = {"submit", "request"};
        precondition.add(new ProcessOnlySubmittableTaskDesire(belief, info));
        precondition.add(new ActionDesire(belief, neededActions));
        precondition.add(new AttachSingleBlockDesire(belief, info.requirements.get(0)));
        precondition.add(new GoToGoalZoneDesire(belief));
        precondition.add(new GetBlocksInOrderDesire(belief, info));
    }

    public Action getNextAction() {
        Action a = fullfillPreconditions();
        if (a == null) {
            return new Action("submit", new Identifier(info.name));
        }
        return a;
    }

    @Override
    public boolean isFullfilled() {
        return false;
    }
}
