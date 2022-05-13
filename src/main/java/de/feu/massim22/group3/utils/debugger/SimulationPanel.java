package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.util.Set;

public class SimulationPanel extends JPanel {

    private JPanel tasksPanel = new JPanel();
    private JPanel normsPanel = new JPanel();

    SimulationPanel() {

        MigLayout layout = new MigLayout("insets 10", "[left]");

        setLayout(layout);
        setBackground(new Color(235, 235, 235));

        JLabel normsLabel = new AgentPanel.HeaderLabel("Norms:");
        add(normsLabel, "span 2, grow, wrap");

        add(normsPanel, "wrap, grow");

        JLabel tasksLabel = new AgentPanel.HeaderLabel("Tasks:");
        add(tasksLabel, "span 2, grow, wrap");

        add(tasksPanel);
    }

    void setTasks(Set<TaskInfo> tasks) {
        tasksPanel.removeAll();
        for (TaskInfo info : tasks) {
            TaskPanel taskPanel = new TaskPanel(info);
            tasksPanel.add(taskPanel);
        }
    }

    void setNorms(Set<NormInfo> norms) {
        normsPanel.removeAll();
        for (NormInfo norm : norms) {
            NormsPanel normPanel = new NormsPanel(norm);
            normsPanel.add(normPanel);
        }
    }
}
