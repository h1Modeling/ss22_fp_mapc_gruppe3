package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import massim.protocol.data.NormInfo;
import massim.protocol.data.TaskInfo;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** The Class <code>SimulationPanel</code> defines a Panel which displays information about the current simulation including
 * norms and tasks. 
 *
 * @author Heinz Stadler
 */
public class SimulationPanel extends JPanel {

    private JPanel tasksPanel = new JPanel();
    private JPanel normsPanel = new JPanel();
    private int currentNormStep = -1;
    private int currentTaskStep = -1;
    private Map<String, String> normsName = new HashMap<>();
    private Map<String, String> tasksName = new HashMap<>();
    private int nextNormUpdate = 0;
    private int nextTaskUpdate = 0;

    /**
     * Instantiates a new SimulationPanel.
     */
    SimulationPanel() {

        MigLayout layout = new MigLayout("insets 10", "[left]");

        setLayout(layout);
        setBackground(new Color(245, 245, 245));

        JLabel normsLabel = new AgentPanel.HeaderLabel("Norms:");
        add(normsLabel, "span 2, grow, wrap");

        normsPanel.setLayout(new MigLayout());
        normsPanel.setBackground(new Color(245, 245, 245));
        add(normsPanel, "wrap, grow");

        JLabel tasksLabel = new AgentPanel.HeaderLabel("Tasks:");
        add(tasksLabel, "span 2, grow, wrap");

        tasksPanel.setLayout(new MigLayout());
        tasksPanel.setBackground(new Color(245, 245, 245));
        add(tasksPanel, "wrap, grow");
    }

    /**
     * Sets the current tasks of the simulation.
     * 
     * @param tasks the current tasks
     * @param step the current step of the simulation
     */
    synchronized void setTasks(Set<TaskInfo> tasks, int step) {
        if (step != currentTaskStep) {
            boolean needsUpdate = nextTaskUpdate <= step;
            int nextUpdate = 1000;
            for (TaskInfo task : tasks) {
                if (!tasksName.containsKey(task.name)) {
                    needsUpdate = true;
                }
                nextUpdate = Math.min(nextUpdate, task.deadline + 1);
            }
            if (needsUpdate) {
                tasksName.clear();
                tasksPanel.removeAll();
                for (TaskInfo task : tasks) {
                    TaskPanel taskPanel = new TaskPanel(task);
                    tasksPanel.add(taskPanel, "wrap");
                    tasksName.put(task.name, task.name);
                }
                this.nextTaskUpdate = nextUpdate;
                revalidate();
            }
            currentTaskStep = step;
        }
    }

    /**
     * Sets the current norms of the simulation.
     * 
     * @param norms the current norms
     * @param step the current step of the simulation
     */
    synchronized void setNorms(Set<NormInfo> norms, int step) {
        if (step != currentNormStep) {
            boolean needsUpdate = nextNormUpdate <= step;
            int nextUpdate = 1000;
            for (NormInfo norm : norms) {
                if (!normsName.containsKey(norm.name)) {
                    needsUpdate = true;
                }
                nextUpdate = Math.min(nextUpdate, norm.until + 1);
            }
            if (needsUpdate) {
                normsName.clear();
                normsPanel.removeAll();
                for (NormInfo norm : norms) {
                    NormsPanel normPanel = new NormsPanel(norm);
                    normsPanel.add(normPanel, "wrap");
                    normsName.put(norm.name, norm.name);
                }
                this.nextNormUpdate = nextUpdate;
                normsPanel.revalidate();
            }
            currentNormStep = step;
        }
    }
}
