package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;

import massim.protocol.data.NormInfo;
import massim.protocol.data.Subject;
import net.miginfocom.swing.MigLayout;

import java.awt.*;

public class NormsPanel extends JPanel {

    NormsPanel(NormInfo norm) {
        MigLayout layout = new MigLayout("insets 10", "[left] [right]");
        setLayout(layout);
        setBackground(Color.WHITE);

        JLabel title = new JLabel(norm.name);
        add(title, "grow, wrap");

        JLabel deadline = new JLabel("von " + norm.start + " bis " + norm.until);
        add(deadline, "grow, wrap");

        for (Subject s : norm.requirements) {
            JLabel infos = new JLabel(s.type.name() + ", max " + s.quantity);
            infos.setMinimumSize(new Dimension(150, 10));
            infos.setMaximumSize(new Dimension(150, 10));
            add(infos, "span, wrap");

            JLabel infos2 = new JLabel(s.details);
            add(infos2, "span, wrap");
        }

        JLabel punishment = new JLabel(String.valueOf("-" + norm.punishment));
        punishment.setFont(punishment.getFont().deriveFont(20f));
        add(punishment, "cell 1 0 1 2, wrap");
    }   
}