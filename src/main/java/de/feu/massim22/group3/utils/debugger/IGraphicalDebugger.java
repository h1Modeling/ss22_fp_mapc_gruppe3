package de.feu.massim22.group3.utils.debugger;

import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.GroupDebugData;

public interface IGraphicalDebugger {
    void selectAgent(String agent);
    void setGroupData(GroupDebugData data);
}
