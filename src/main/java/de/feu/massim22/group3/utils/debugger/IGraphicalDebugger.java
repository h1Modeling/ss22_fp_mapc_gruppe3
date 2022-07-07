package de.feu.massim22.group3.utils.debugger;

import java.util.List;
import java.util.Set;
import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.DesireDebugData;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.GroupDebugData;
import eis.iilang.Action;

public interface IGraphicalDebugger {
    void selectAgent(String agent);
    void setActionForAgent(String agent, Action a);
    void setGroupData(GroupDebugData data);
    void setAgentData(AgentDebugData data);
    void setAgentDesire(List<DesireDebugData> data, String agent);
    void setSimInfo(int currentStep, int maxSteps, int score);
    void setNorms(Set<NormInfo> norms, int step);
    void setTasks(Set<TaskInfo> tasks, int step);
    void removeSupervisor(String name, String newGroup);
    void setSelectedGroup(String name);
    void makeStep();
    void setDebugStepListener(DebugStepListener listener, boolean manualMode);
    String getAgentGroupDesireType(String agent);
    void setDelay(boolean value);
}
