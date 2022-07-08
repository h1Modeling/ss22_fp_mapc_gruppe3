package de.feu.massim22.group3.utils.debugger;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

import de.feu.massim22.group3.agents.Desires.BDesires.BooleanInfo;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.AgentDebugData;
import de.feu.massim22.group3.utils.debugger.GraphicalDebugger.DesireDebugData;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

class AgentPanel extends JPanel {

    private JLabel role;
    private JLabel name;
    private JLabel energy;
    private JLabel lastAction;
    private JLabel lastActionSuccess;
    private JLabel lastActionIntention;
    private List<JLabel> desires = new ArrayList<>();
    private List<JLabel> desireLabels = new ArrayList<>();
    private int maxDesireCount = 10;
    
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
        name.setMinimumSize(new Dimension(200, 10));

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

        JLabel lastActionIntentionLabel = new JLabel("Letzte Intention:");
        add(lastActionIntentionLabel);

        lastActionIntention = new JLabel();
        add(lastActionIntention, "wrap");

        JLabel desireDataLabel = new HeaderLabel("Desires:");
        add(desireDataLabel, "span 2, grow, wrap, gapTop 10");

        for (int i = 0; i < maxDesireCount; i++) {
            JLabel easyTaskDesireLabel = new JLabel("Easy Task:");
            easyTaskDesireLabel.setVisible(false);
            add(easyTaskDesireLabel);
            desireLabels.add(easyTaskDesireLabel);
            
            JLabel easyTaskDesire = new JLabel("");
            add(easyTaskDesire, "wrap");
            desires.add(easyTaskDesire);
            easyTaskDesire.setVisible(false);

        }
    }

    void setAgentData(AgentDebugData data) {
        if (data != null) {
            name.setText(data.name());
            role.setText(data.role());
            energy.setText(String.valueOf(data.energy()));
            lastAction.setText(data.lastAction());
            lastActionSuccess.setText(data.lastActionSuccess());
            lastActionIntention.setText(data.lastActionDesire());
            revalidate();
        } else {
            clear();
        }
    }

    void setDesireData(List<DesireDebugData> list) {
        int easyTaskIndex = 0;
        for (DesireDebugData data : list) {
            if (data != null) {
                BooleanInfo isExecutable = data.isExecutable();
                String text = isExecutable.value() ? "possible" : isExecutable.info();
                if (easyTaskIndex < maxDesireCount) {
                    JLabel label = desireLabels.get(easyTaskIndex);
                    label.setText(data.name());
                    label.setVisible(true);
                    JLabel value = desires.get(easyTaskIndex);
                    value.setVisible(true);
                    value.setText(text);
                    easyTaskIndex ++;
                }
            }
        }
        // Hide not used
        for (int i = easyTaskIndex; i < maxDesireCount; i++) {
            desireLabels.get(i).setVisible(false);
            desires.get(i).setVisible(false);
        }
        revalidate();
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
