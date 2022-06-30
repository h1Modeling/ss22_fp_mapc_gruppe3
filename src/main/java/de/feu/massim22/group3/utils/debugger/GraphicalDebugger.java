package de.feu.massim22.group3.utils.debugger;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import java.awt.Point;

import de.feu.massim22.group3.agents.Desires.BDesires.BooleanInfo;
import de.feu.massim22.group3.agents.Desires.BDesires.GroupDesireTypes;
import de.feu.massim22.group3.map.CellType;
import de.feu.massim22.group3.map.InterestingPoint;
import de.feu.massim22.group3.map.PathFindingResult;
import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;

public class GraphicalDebugger extends JFrame implements Runnable, IGraphicalDebugger {

    private Header header;
    private AgentPanel agentPanel;
    private SimulationPanel simulationPanel;
    private MapPanel mapPanel;

    private Map<String, AgentDebugData> agentData = new HashMap<>();
    private Map<String, GroupDebugData> groupData = new HashMap<>();
    private Set<String> groups = new HashSet<>();
    private String selectedGroup = "";
    private String selectedAgent = "";
    private DebugStepListener listener;

    public GraphicalDebugger() {
        setTitle("Debugger - Massim 22 - Gruppe 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public void run() {
        header = new Header(this);
        add(header, BorderLayout.NORTH);

        agentPanel = new AgentPanel();
        agentPanel.setMinimumSize(new Dimension(450, 0));
        add(new JScrollPane(agentPanel), BorderLayout.WEST);

        simulationPanel = new SimulationPanel();
        JScrollPane scrollableSimPanel = new JScrollPane(simulationPanel);
        add(scrollableSimPanel, BorderLayout.EAST);

        mapPanel = new MapPanel(this);
        add(mapPanel);

        setPreferredSize(new Dimension(1000, 800));
        setSize(1000, 800);
        setResizable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        toFront();
    }

    @Override
    public void selectAgent(String agent) {
        AgentDebugData data = agentData.get(agent);
        selectedAgent = agent;
        agentPanel.setAgentData(data);
    }

    public static record GroupDebugData(String supervisor, CellType[][] map, Point mapTopLeft, 
        List<InterestingPoint> interestingPoints, PathFindingResult[][] pathFindingResult,
        Map<Point, String> agentPosition, List<Point> roleZones, List<Point> goalzones,
        List<String> agents) { }

    public static record AgentDebugData(
        String name,
        String supervisor,
        String role,
        int energy,
        String lastAction,
        String lastActionSuccess,
        String lastActionDesire,
        String groupDesireType
    ) {}

    public static record DesireDebugData(
        String name,
        BooleanInfo isExecutable
    ) {}

    @Override
    public synchronized void setGroupData(GroupDebugData data) {
        groupData.put(data.supervisor, data);

        // Sets first data as current group
        if (selectedGroup == "") {
            selectedGroup = data.supervisor;
        }

        // Update Groups
        if (!groups.contains(data.supervisor)) {
            groups.add(data.supervisor);
            header.setGroups(groups, selectedGroup);
        }

        // update current view
        if (data.supervisor() == selectedGroup) {
            mapPanel.setData(data);
        }
    }

    @Override
    public void setSimInfo(int currentStep, int maxSteps, int points) {
        header.setData(currentStep, maxSteps, points);
    }

    @Override
    public void setNorms(Set<NormInfo> norms, int step) {
        simulationPanel.setNorms(norms, step);
    }

    @Override
    public void setTasks(Set<TaskInfo> tasks, int step) {
        simulationPanel.setTasks(tasks, step);
    }

    @Override
    public synchronized void removeSupervisor(String name, String newGroup) {
        groups.remove(name);
        if (selectedGroup.equals(name)) {
            selectedGroup = newGroup;
        }
        header.setGroups(groups, selectedGroup);
    }

    @Override
    public void setSelectedGroup(String name) {
        GroupDebugData data = groupData.get(name);
        if (data != null) {
            selectedGroup = name;
            mapPanel.setData(data);
        }
    }

    @Override
    public void makeStep() {
        listener.debugStep();
    }

    @Override
    public void setDebugStepListener(DebugStepListener listener) {
        this.listener = listener;
        header.showStepButton();
    }

    @Override
    public synchronized void setAgentData(AgentDebugData data) {
        agentData.put(data.name, data);
        if (data.name.equals(selectedAgent)) {
            selectAgent(data.name);
        }
    }

    @Override
    public void setAgentDesire(List<DesireDebugData> data, String agent) {
        if (agent.equals(selectedAgent)) {
            agentPanel.setDesireData(data);
        }
    }

    @Override
    public String getAgentGroupDesireType(String agent) {
        AgentDebugData data = agentData.get(agent);
        if (data != null) {
            return data.groupDesireType;
        }
        return GroupDesireTypes.NONE;
    }
}
