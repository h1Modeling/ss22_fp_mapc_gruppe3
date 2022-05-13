package de.feu.massim22.group3.utils.debugger;

import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.InsetsUIResource;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

class Header extends JPanel {
    private int step = 0;
    private int points = 0;
    private JButton next;
    private JComboBox groupSelection;

    Header() {
        setLayout(new GridBagLayout());
        //setBackground(Color.RED);

        GridBagConstraints c = new GridBagConstraints();

        // Step
        JLabel stepLabel = new JLabel("Step: 10/200");
        //stepLabel.setHorizontalAlignment(SwingConstants.LEFT);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.weightx = 0.2;
        c.insets = new InsetsUIResource(0, 10, 0, 10);
        //stepLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(stepLabel, c);

        // Points
        JLabel pointLabel = new JLabel("Punkte: 0");
        c.gridx = 1;
        add(pointLabel, c);

        // Group
        groupSelection = new JComboBox<>();
        c.gridx = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(groupSelection, c);

        // Button
        next = new JButton("sende Aktion");
        c.gridx = 3;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        c.weightx = 1;
        add(next, c);
        
    }
}
