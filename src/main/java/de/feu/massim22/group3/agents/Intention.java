package de.feu.massim22.group3.agents;

import eis.iilang.Action;
import massim.protocol.data.TaskInfo;

public class Intention {
    
    private Action nextAction;
    private TaskInfo currentTask = null;

    
    Action getNextAction() {
        return nextAction;
    }

    void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }

    void clear() {
        nextAction = null;
    }

    public TaskInfo getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(TaskInfo t) {
        currentTask = t;
    }
}
