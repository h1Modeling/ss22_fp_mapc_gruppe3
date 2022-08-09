package de.feu.massim22.group3.utils.debugger;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.awt.GridBagConstraints;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.plaf.InsetsUIResource;

/** The Class <code>Header</code> defines a Panel which displays general information about the simulation,
 * controls for manual interaction with the simulation and the selection of the displayed agent group. 
 *
 * @author Heinz Stadler
 */
class Header extends JPanel {
    private JButton next;
    private JComboBox<String> groupSelection;
    private JComboBox<String> agentSelection;
    private JLabel stepLabel;
    private JLabel pointLabel;
    private JCheckBox delay;
    private Map<String, Boolean> addedAgents = new HashMap<>();

    /**
     * Instantiates a new Header.
     * 
     * @param debugger the debugger the panel is part of
     */
    Header(IGraphicalDebugger debugger) {

        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        // Step
        stepLabel = new JLabel();
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.weightx = 0.2;
        c.insets = new InsetsUIResource(0, 10, 0, 10);
        add(stepLabel, c);

        // Points
        pointLabel = new JLabel();
        c.gridx = 1;
        add(pointLabel, c);

        // Agent
        JPanel agentPanel = new JPanel();
        c.gridx = 2;
        c.weightx = 1;
        add(agentPanel, c);

        JLabel agentLabel = new JLabel("Agent:");
        agentPanel.add(agentLabel);

        agentSelection = new JComboBox<String>();
        agentSelection.addActionListener(l -> {
            String selection = (String)agentSelection.getSelectedItem();
            debugger.selectAgent(selection);
        });
        agentPanel.add(agentSelection, c);

        // Group
        JPanel groupPanel = new JPanel();
        c.gridx = 3;
        c.weightx = 1;
        add(groupPanel, c);

        JLabel groupLabel = new JLabel("Gruppe:");
        groupPanel.add(groupLabel);

        groupSelection = new JComboBox<>();
        groupSelection.addActionListener(l -> {
            String selection = (String)groupSelection.getSelectedItem();
            debugger.setSelectedGroup(selection);
        });
        groupPanel.add(groupSelection, c);

        // Button
        next = new JButton("sende Aktion");
        c.gridx = 4;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1;
        next.addActionListener(l -> {
            debugger.makeStep();
        });
        next.setVisible(false);
        add(next, c);

        // Checkbox
        delay = new JCheckBox("Verzögern");
        c.gridx = 5;
        c.fill = GridBagConstraints.HORIZONTAL;
        delay.addItemListener(l -> {
            debugger.setDelay(l.getStateChange() == ItemEvent.SELECTED);
        });
        add(delay, c);
    }

    /**
     * Sets the data which should be displayed by the panel.
     * 
     * @param currentStep the current step of the simulation
     * @param maxSteps the last step of the simulation
     * @param score the current score of the team
     */
    void setData(int currentStep, int maxSteps, int score) {
        stepLabel.setText("Schritt: " + currentStep + " / " + maxSteps);
        pointLabel.setText("Punkte: " + score + " $");
    }

    /**
     * Sets the current groups including the selected one.
     * 
     * @param groups all groups
     * @param selectedGroup the currently selected group
     */
    void setGroups(Set<String> groups, String selectedGroup) {
        String[] items = groups.toArray(new String[groups.size()]);
        ComboBoxModel<String> model = new DefaultComboBoxModel<String>(items);
        groupSelection.setModel(model);
        model.setSelectedItem(selectedGroup);
    }

    /**
     * Sets the visibility of the step buttons.
     */
    void showStepButton() {
        next.setVisible(true);
    }

    /**
     * Adds an agent to the agent ComboBox if the agent isn't added yet.
     * 
     * @param agent the name of the agent
     */
    void addAgent(String agent) {
        if (!addedAgents.containsKey(agent)) {
            agentSelection.addItem(agent);
            addedAgents.put(agent, true);
        }
    }
}
