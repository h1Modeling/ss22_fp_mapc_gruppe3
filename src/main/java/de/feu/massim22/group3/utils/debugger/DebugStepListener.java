package de.feu.massim22.group3.utils.debugger;

import eis.iilang.Action;

public interface DebugStepListener {
    void debugStep();
    void setDelay(boolean value);
    void setAction(String agent, Action a);
}
