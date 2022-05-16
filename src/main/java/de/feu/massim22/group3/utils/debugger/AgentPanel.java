package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Dimension;

class AgentPanel extends JPanel {

    private JLabel role;
    private JLabel name;
    private JLabel energy;
    private JLabel lastAction;
    private JLabel lastActionSuccess;
    
    AgentPanel() {

        MigLayout layout = new MigLayout("insets 10", "[left] [left]");

        setLayout(layout);
        setBackground(new Color(245, 245, 245));

        JLabel agentDataLabel = new HeaderLabel("Status:");
        add(agentDataLabel, "span 2, grow, wrap");

        JLabel nameLabel = new JLabel("Name:");
        add(nameLabel);

        name = new JLabel();
        add(name, "wrap");
        name.setMinimumSize(new Dimension(100, 10));

        JLabel roleLabel = new JLabel("Rolle:");
        add(roleLabel);

        role = new JLabel();
        add(role, "wrap");

        JLabel energyLabel = new JLabel("Energie:");
        add(energyLabel);

        energy = new JLabel();
        add(energy, "wrap");

        JLabel lastActionLabel = new JLabel("Letzte Aktion:");
        add(lastActionLabel);

        lastAction = new JLabel();
        add(lastAction, "wrap");

        JLabel lastActionSuccessLabel = new JLabel("Aktion geglückt:");
        add(lastActionSuccessLabel);

        lastActionSuccess = new JLabel();
        add(lastActionSuccess, "wrap");

        JLabel desireDataLabel = new HeaderLabel("Desires:");
        add(desireDataLabel, "span 2, grow, wrap, gapTop 10");
    }

    void setAgentData(AgentDebugData data) {
        if (data != null) {
            name.setText(data.name());
            role.setText(data.role());
            energy.setText(String.valueOf(data.energy()));
            lastAction.setText(data.lastAction());
            lastActionSuccess.setText(data.lastActionSuccess());
            revalidate();
        } else {
            clear();
        }
    }

    void clear() {
        name.setText("");
        role.setText("");
        energy.setText("");
        lastAction.setText("");
        lastActionSuccess.setText(""); 
    }

    static class HeaderLabel extends JLabel {
        HeaderLabel(String title) {
            super(title);
            setFont(getFont().deriveFont(0, 16));
            setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));
        }
    }
}
