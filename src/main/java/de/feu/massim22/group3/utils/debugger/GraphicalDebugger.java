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

import java.awt.Toolkit;
import java.awt.event.*;

import de.feu.massim22.group3.agents.desires.GroupDesireTypes;
import de.feu.massim22.group3.map.Disposable;
import de.feu.massim22.group3.utils.debugger.debugData.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.debugData.DesireDebugData;
import de.feu.massim22.group3.utils.debugger.debugData.GroupDebugData;
import eis.iilang.Action;
import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;

/** 
 * The Class <code>GraphicalDebugger</code> defines a Frame to view debug information of the current simulation
 * and especially of Agents, their path finding and decision making. 
 *
 * @author Heinz Stadler
 */
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
    private boolean initialized = false;

    /**
     * Instantiates a new GraphicalDebugger, sets the look and feel and adds window listener.
     * 
     * @param mapDisposer an object that holds data which needs to be disposed at program end 
     */
    public GraphicalDebugger(Disposable mapDisposer) {
        setTitle("Debugger - Massim 22 - Gruppe 3");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Free Resources after Close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mapDisposer.dispose();
                e.getWindow().dispose();
            }
        });

        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {}
    }

    /**
     * {@inheritDoc}
     * Instantiates the different parts of the debugger and shows the panel.
     */
    @Override
    public void run() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
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

        setPreferredSize(new Dimension((int)screenSize.getWidth(), (int)screenSize.getHeight() - 500));
        
        setResizable(true);
        pack();
        setVisible(true);
        toFront();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectAgent(String agent) {
        AgentDebugData data = agentData.get(agent);
        selectedAgent = agent;
        agentPanel.setAgentData(data);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setGroupData(GroupDebugData data) {
        groupData.put(data.supervisor(), data);

        // Sets first data as current group
        if (selectedGroup == "") {
            selectedGroup = data.supervisor();
        }

        // Update Groups
        if (!groups.contains(data.supervisor())) {
            groups.add(data.supervisor());
            header.setGroups(groups, selectedGroup);
        }

        // update current view
        if (data.supervisor() == selectedGroup) {
            mapPanel.setData(data);
        }

        // Select Agent at Start
        if (!initialized) {
            initialized = true;
            selectAgent(data.supervisor());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSimInfo(int currentStep, int maxSteps, int points) {
        header.setData(currentStep, maxSteps, points);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setNorms(Set<NormInfo> norms, int step) {
        simulationPanel.setNorms(norms, step);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTasks(Set<TaskInfo> tasks, int step) {
        simulationPanel.setTasks(tasks, step);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeSupervisor(String name, String newGroup) {
        groups.remove(name);
        if (selectedGroup.equals(name)) {
            selectedGroup = newGroup;
        }
        header.setGroups(groups, selectedGroup);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedGroup(String name) {
        GroupDebugData data = groupData.get(name);
        if (data != null) {
            selectedGroup = name;
            mapPanel.setData(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeStep() {
        listener.debugStep();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDebugStepListener(DebugStepListener listener, boolean manualMode) {
        this.listener = listener;
        if (manualMode) {
            header.showStepButton();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void setAgentData(AgentDebugData data) {
        agentData.put(data.name(), data);
        if (data.name().equals(selectedAgent)) {
            selectAgent(data.name());
        }
        header.addAgent(data.name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAgentDesire(List<DesireDebugData> data, String agent) {
        if (agent.equals(selectedAgent)) {
            agentPanel.setDesireData(data);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAgentGroupDesireType(String agent) {
        AgentDebugData data = agentData.get(agent);
        if (data != null) {
            return data.groupDesireType();
        }
        return GroupDesireTypes.NONE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDelay(boolean value) {
        if (this.listener != null) {
            listener.setDelay(value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActionForAgent(String agent, Action action) {
        if (listener != null) {
            listener.setAction(agent, action);
        }
    }
}
