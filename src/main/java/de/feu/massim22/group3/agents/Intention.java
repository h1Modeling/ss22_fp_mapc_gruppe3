package de.feu.massim22.group3.agents;

import eis.iilang.Action;

class Intention {
    
    private Action nextAction;

    
    Action getNextAction() {
        return nextAction;
    }

    void setNextAction(Action nextAction) {
        this.nextAction = nextAction;
    }
    
    void clear() {
        nextAction = null;
    }
}
