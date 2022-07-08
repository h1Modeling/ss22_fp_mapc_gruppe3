package de.feu.massim22.group3.utils.debugger;

import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
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


class Header extends JPanel {
    private JButton next;
    private JComboBox<String> groupSelection;
    private JLabel stepLabel;
    private JLabel pointLabel;
    private JCheckBox delay;

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

        // Group
        groupSelection = new JComboBox<>();
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        groupSelection.addActionListener(l -> {
            String selection = (String)groupSelection.getSelectedItem();
            debugger.setSelectedGroup(selection);
        });
        add(groupSelection, c);

        // Button
        next = new JButton("sende Aktion");
        c.gridx = 3;
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
        c.gridx = 4;
        c.fill = GridBagConstraints.HORIZONTAL;
        delay.addItemListener(l -> {
            debugger.setDelay(l.getStateChange() == ItemEvent.SELECTED);
        });
        add(delay, c);
    }

    void setData(int currentStep, int maxSteps, int score) {
        stepLabel.setText("Schritt: " + currentStep + " / " + maxSteps);
        pointLabel.setText("Punkte: " + score + " $");
    }

    void setGroups(Set<String> groups, String selectedGroup) {
        String[] items = groups.toArray(new String[groups.size()]);
        ComboBoxModel<String> model = new DefaultComboBoxModel<String>(items);
        groupSelection.setModel(model);
        model.setSelectedItem(selectedGroup);
    }

    void showStepButton() {
        next.setVisible(true);
    }
}
